package ai.evolv;

import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ai.evolv.exceptions.AscendAllocationException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


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

    private boolean confirmationSandbagged = false;
    private boolean contaminationSandbagged = false;

    private AllocationStatus allocationStatus;

    Allocator(AscendConfig config) {
        this.executionQueue = config.getExecutionQueue();
        this.store = config.getAscendAllocationStore();
        this.config = config;
        this.ascendParticipant = config.getAscendParticipant();
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

    HttpUrl createAllocationsUrl() {
        return new HttpUrl.Builder()
                .scheme(config.getHttpScheme())
                .host(config.getDomain())
                .addPathSegment(config.getVersion())
                .addPathSegment(config.getEnvironmentId())
                .addPathSegment("allocations")
                .addQueryParameter("uid", ascendParticipant.getUserId())
                .addQueryParameter("sid", ascendParticipant.getSessionId())
                .build();
    }

    Future<JsonArray> fetchAllocations() {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(false)
                .build();

        final Request request = new Request.Builder()
                .url(createAllocationsUrl())
                .build();

        final SettableFuture<JsonArray> responseFuture = SettableFuture.create();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                resolveAllocationFailure(responseFuture);
            }

            @Override
            public void onResponse(Call call, Response response) {
                String body = "";
                JsonArray allocations = new JsonArray();
                try {
                    try {
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            body = responseBody.string();
                        }

                        if (!response.isSuccessful()) {
                            throw new IOException(String.format(
                                    "Unexpected response when making GET request: %s using url: %s with body: %s",
                                    response, request.url(), body));
                        }

                        JsonParser parser = new JsonParser();
                        allocations = parser.parse(body).getAsJsonArray();

                        JsonArray previousAllocations = store.get();
                        if (allocationsNotEmpty(previousAllocations)) {
                            allocations = Allocations.reconcileAllocations(previousAllocations, allocations);
                        }

                    } catch(Exception e) {
                        throw new AscendAllocationException(e.getMessage());
                    }

                    store.put(allocations);
                    responseFuture.set(allocations);
                    allocationStatus = AllocationStatus.RETRIEVED;

                    if (confirmationSandbagged) {
                        eventEmitter.confirm(allocations);
                    }

                    if (contaminationSandbagged) {
                        eventEmitter.contaminate(allocations);
                    }

                    // could throw an exception due to customer's action logic
                    // always surface any customer implementation errors
                    executionQueue.executeAllWithValuesFromAllocations(allocations);
                } catch (AscendAllocationException e) {
                    resolveAllocationFailure(responseFuture);
                } catch (Exception e) {
                    allocationStatus = AllocationStatus.FAILED;
                    logger.warn("There was an error making an allocation request.",e);
                } finally {
                    response.close();
                }
            }


        });

        return  responseFuture;
    }

    private void resolveAllocationFailure(SettableFuture<JsonArray> future) {
        logger.warn("There was an error while making an allocation request.");

        JsonArray allocations = store.get();
        if (allocationsNotEmpty(allocations)) {
            logger.warn("Falling back to participant's previous allocation.");

            future.set(allocations);

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

            future.set(new JsonArray());

            allocationStatus = AllocationStatus.FAILED;
            executionQueue.executeAllWithValuesFromDefaults();
        }
    }

    static boolean allocationsNotEmpty(JsonArray allocations) {
        return allocations != null && allocations.size() > 0;
    }



}
