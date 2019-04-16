package ai.evolv;

import ai.evolv.exceptions.AscendRuntimeException;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class Allocator {

    private static Logger logger = LoggerFactory.getLogger(Allocator.class);

    enum AllocationStatus {
        FETCHING, RETRIEVED, FAILED
    }

    private final ExecutionQueue executionQueue;
    private final AscendAllocationStore store;
    private final AscendConfig config;
    private final AscendParticipant ascendParticipant;
    private final EventEmitter eventEmitter;
    private final HttpClient httpClient;

    private boolean confirmationSandbagged = false;
    private boolean contaminationSandbagged = false;

    private AllocationStatus allocationStatus;

    Allocator(AscendConfig config) {
        this.executionQueue = config.getExecutionQueue();
        this.store = config.getAscendAllocationStore();
        this.config = config;
        this.ascendParticipant = config.getAscendParticipant();
        this.httpClient = config.getHttpClient();
        this.allocationStatus = AllocationStatus.FETCHING;
        this.eventEmitter = new EventEmitter(config);

    }

    AllocationStatus getAllocationStatus() {
        return allocationStatus;
    }

    void sandBagConfirmation() {
        confirmationSandbagged = true;
    }

    void sandBagContamination() {
        contaminationSandbagged = true;
    }

    String createAllocationsUrl() {
        try {
            String path = String.format("//%s/%s/%s/allocations", config.getDomain(),
                    config.getVersion(),
                    config.getEnvironmentId());
            String queryString = String.format("uid=%s&sid=%s", ascendParticipant.getUserId(),
                    ascendParticipant.getSessionId());
            URI uri = new URI(config.getHttpScheme(), null, path, queryString, null);
            URL url = uri.toURL();

            return url.toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return "";
        }
    }

    ListenableFuture<JsonArray> fetchAllocations() {
        ListenableFuture<String> responseFuture = httpClient.get(createAllocationsUrl());
        SettableFuture<JsonArray> allocationsFuture = SettableFuture.create();

        responseFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    JsonParser parser = new JsonParser();
                    JsonArray allocations = parser.parse(responseFuture.get()).getAsJsonArray();

                    JsonArray previousAllocations = store.get();
                    if (allocationsNotEmpty(previousAllocations)) {
                        allocations = Allocations.reconcileAllocations(previousAllocations, allocations);
                    }

                    store.put(allocations);
                    allocationStatus = AllocationStatus.RETRIEVED;

                    if (confirmationSandbagged) {
                        eventEmitter.confirm(allocations);
                    }

                    if (contaminationSandbagged) {
                        eventEmitter.contaminate(allocations);
                    }

                    allocationsFuture.set(allocations);

                    // could throw an exception due to customer's action logic
                    try {
                        executionQueue.executeAllWithValuesFromAllocations(allocations);
                    } catch (Exception e) {
                        throw new AscendRuntimeException(e);
                    }
                } catch (AscendRuntimeException e){
                    logger.error(e.getMessage());
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                    allocationsFuture.set(resolveAllocationFailure());
                }
            }
        }, MoreExecutors.directExecutor());

        return allocationsFuture;
    }

    JsonArray resolveAllocationFailure() {
        logger.warn("There was an error while making an allocation request.");

        JsonArray allocations = store.get();
        if (allocationsNotEmpty(allocations)) {
            logger.warn("Falling back to participant's previous allocation.");

            if (confirmationSandbagged) {
                eventEmitter.confirm(allocations);
            }

            if (contaminationSandbagged) {
                eventEmitter.contaminate(allocations);
            }

            allocationStatus = AllocationStatus.RETRIEVED;
            executionQueue.executeAllWithValuesFromAllocations(allocations);
        } else {
            logger.warn("Falling back to the supplied defaults.");

            allocationStatus = AllocationStatus.FAILED;
            executionQueue.executeAllWithValuesFromDefaults();

            allocations = new JsonArray();
        }

        return allocations;
    }

    static boolean allocationsNotEmpty(JsonArray allocations) {
        return allocations != null && allocations.size() > 0;
    }



}
