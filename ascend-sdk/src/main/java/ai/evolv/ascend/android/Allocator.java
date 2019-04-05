package ai.evolv.ascend.android;

import android.support.annotation.NonNull;

import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ai.evolv.ascend.android.exceptions.AscendAllocationException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;


class Allocator {

    enum AllocationStatus {
        FETCHING, RETRIEVED, FAILED
    }

    private final ExecutionQueue executionQueue;
    private final AscendAllocationStore store;
    private final AscendConfig config;
    private final AscendParticipant ascendParticipant;
    private final EventEmitter eventEmitter;

    private boolean confirmationSandbagged;
    private boolean contaminationSandbagged;
    private AllocationStatus allocationStatus;

    Allocator(@NonNull AscendConfig config) {
        this.executionQueue = config.getExecutionQueue();
        this.store = config.getAscendAllocationStore();
        this.config = config;
        this.ascendParticipant = config.getAscendParticipant();
        this.confirmationSandbagged = false;
        this.contaminationSandbagged = false;
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
                .build();

        final Request request = new Request.Builder()
                .url(createAllocationsUrl())
                .build();

        final SettableFuture<JsonArray> responseFuture = SettableFuture.create();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                resolveAllocationFailure(responseFuture);
            }

            @Override public void onResponse(Call call, Response response) {
                String body = "";
                JsonArray allocations = new JsonArray();
                try {
                    try {
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            body = responseBody.string();
                        }
                        if (!response.isSuccessful()) {
                            throw new IOException("Unexpected response when making GET request: " + response + " using url: "
                                    + request.url() + " with body: " + body);
                        }

                        JsonParser parser = new JsonParser();
                        allocations = parser.parse(body).getAsJsonArray();

                        JsonArray previousAllocations = store.get();
                        if (allocationsNotEmpty(previousAllocations)) {
                            allocations = Allocations.reconcileAllocations(previousAllocations, allocations);
                        }
                    } catch(Exception e ) {
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
                    Timber.w(e);
                } catch (Exception e) {
                    allocationStatus = AllocationStatus.FAILED;
                    Timber.e(e);
                } finally {
                    response.close();
                }
            }


        });

        return  responseFuture;
    }

    private void resolveAllocationFailure(SettableFuture<JsonArray> future) {
        Timber.w("There was an error while making an allocation request.");

        JsonArray allocations = store.get();
        if (allocationsNotEmpty(allocations)) {
            Timber.w("Falling back to participant's previous allocation.");

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
            Timber.w("Falling back to the supplied defaults.");

            future.set(new JsonArray());

            allocationStatus = AllocationStatus.FAILED;
            executionQueue.executeAllWithValuesFromDefaults();
        }
    }

    static boolean allocationsNotEmpty(JsonArray allocations) {
        return allocations != null && allocations.size() > 0;
    }



}
