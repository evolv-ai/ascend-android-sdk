package ai.evolv.ascend.android;

import android.support.annotation.NonNull;

import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;


class Allocator {

    private final AscendAllocationStore store;
    private final AscendConfig config;
    private final AscendParticipant ascendParticipant;

    Allocator(@NonNull AscendConfig config) {
        this.store = config.getAscendAllocationStore();
        this.config = config;
        this.ascendParticipant = config.getAscendParticipant();
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
                .callTimeout(5000, TimeUnit.MILLISECONDS)
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
                    JsonArray allocations = parser.parse(body).getAsJsonArray();

                    JsonArray previousAllocations = store.get();
                    if (allocationsNotEmpty(previousAllocations)) {
                        allocations = reconcileAllocations(previousAllocations, allocations);
                    }

                    responseFuture.set(allocations);
                } catch (Exception e) {
                    resolveAllocationFailure(responseFuture);
                    Timber.e(e);
                } finally {
                    if (response != null) {
                        response.close();
                    }
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
            //execute stored callbacks with the previous allocation
            future.set(allocations);
        } else {
            Timber.w("Falling back to the supplied defaults.");
            //execute stored callbacks with the default values
            future.set(new JsonArray());
        }
    }

    private JsonArray reconcileAllocations(JsonArray previousAllocations, JsonArray currentAllocations) {
        return null;
    }

    static boolean allocationsNotEmpty(JsonArray allocations) {
        return allocations != null && allocations.size() > 0;
    }



}
