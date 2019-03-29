package ai.evolv.ascend.android;

import android.support.annotation.NonNull;

import ai.evolv.ascend.android.exceptions.AscendAllocationException;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;


class Allocator {

    private HttpParticipantClient client;
    private final AscendConfig config;
    private final AscendParticipant ascendParticipant;

    Allocator(@NonNull AscendConfig config) {
        this.client = config.getParticipantClient();
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

    synchronized JsonArray fetchAllocations() throws AscendAllocationException {
        JsonArray allocations;

        try {
            ListenableFuture<String> allocationsFuture = this.client.executeGetRequest(createAllocationsUrl());
            String stringAllocations = allocationsFuture.get(config.getTimeout(), TimeUnit.MILLISECONDS);
            JsonParser jsonParser = new JsonParser();
            allocations = jsonParser.parse(stringAllocations).getAsJsonArray();
        } catch (Exception e){
            throw new AscendAllocationException(e.getMessage());
        }

        return allocations;
    }

}
