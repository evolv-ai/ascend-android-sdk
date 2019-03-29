package ai.evolv.ascend.android;

import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import okhttp3.HttpUrl;

class EventEmitter {

    static final String CONFIRM_KEY = "confirmation";
    static final String CONTAMINATE_KEY = "contamination";

    private final HttpParticipantClient client;
    private final AscendConfig config;
    private final AscendParticipant ascendParticipant;

    EventEmitter(@NonNull AscendConfig config) {
        this.client = config.getParticipantClient();
        this.config = config;
        this.ascendParticipant = config.getAscendParticipant();
    }

    void emit(String key) {
        HttpUrl url = getEventUrl(key, 1);
        client.executeGetRequest(url);
    }

    void emit(String key, Double score) {
        HttpUrl url = getEventUrl(key, score);
        client.executeGetRequest(url);
    }

    void confirm(JsonArray allocations) {
        sendAllocationEvents(CONFIRM_KEY, allocations);
    }

    void contaminate(JsonArray allocations) {
        sendAllocationEvents(CONTAMINATE_KEY, allocations);
    }

    void sendAllocationEvents(String key, JsonArray allocations) {
        for (JsonElement a : allocations) {
            JsonObject allocation = a.getAsJsonObject();
            String experimentId = allocation.get("eid").getAsString();
            String candidateId = allocation.get("cid").getAsString();
            HttpUrl url = getEventUrl(key, experimentId, candidateId);
            client.executeGetRequest(url);
        }
    }

    HttpUrl getEventUrl(String type, double score) {
        return new HttpUrl.Builder()
                .scheme(config.getHttpScheme())
                .host(config.getDomain())
                .addPathSegment(config.getVersion())
                .addPathSegment(config.getEnvironmentId())
                .addPathSegment("events")
                .addQueryParameter("uid", ascendParticipant.getUserId())
                .addQueryParameter("sid", ascendParticipant.getSessionId())
                .addQueryParameter("type", type)
                .addQueryParameter("score", Double.toString(score))
                .build();
    }

    HttpUrl getEventUrl(String type, String experimentId, String candidateId) {
        return new HttpUrl.Builder()
                .scheme(config.getHttpScheme())
                .host(config.getDomain())
                .addPathSegment(config.getVersion())
                .addPathSegment(config.getEnvironmentId())
                .addPathSegment("events")
                .addQueryParameter("uid", ascendParticipant.getUserId())
                .addQueryParameter("sid", ascendParticipant.getSessionId())
                .addQueryParameter("eid", experimentId)
                .addQueryParameter("cid", candidateId)
                .addQueryParameter("type", type)
                .build();
    }

}
