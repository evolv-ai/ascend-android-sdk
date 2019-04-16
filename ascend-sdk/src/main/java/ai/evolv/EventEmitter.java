package ai.evolv;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EventEmitter {

    private static Logger logger = LoggerFactory.getLogger(ExecutionQueue.class);

    static final String CONFIRM_KEY = "confirmation";
    static final String CONTAMINATE_KEY = "contamination";

    private final HttpClient httpClient;
    private final AscendConfig config;
    private final AscendParticipant ascendParticipant;

    EventEmitter(AscendConfig config) {
        this.httpClient = config.getHttpClient();
        this.config = config;
        this.ascendParticipant = config.getAscendParticipant();
    }

    void emit(String key) {
        String url = getEventUrl(key, 1.0);
        if (url != null) {
            httpClient.get(url);
        }
    }

    void emit(String key, Double score) {
        String url = getEventUrl(key, score);
        if (url != null) {
            httpClient.get(url);
        }
    }

    void confirm(JsonArray allocations) {
        sendAllocationEvents(CONFIRM_KEY, allocations);
    }

    void contaminate(JsonArray allocations)  {
        sendAllocationEvents(CONTAMINATE_KEY, allocations);
    }

    void sendAllocationEvents(String key, JsonArray allocations) {
        for (JsonElement a : allocations) {
            JsonObject allocation = a.getAsJsonObject();
            String experimentId = allocation.get("eid").getAsString();
            String candidateId = allocation.get("cid").getAsString();

            String url = getEventUrl(key, experimentId, candidateId);
            if (url != null) {
                httpClient.get(url);
            }
        }
    }

    String getEventUrl(String type, Double score) {
        try {
            String path = String.format("//%s/%s/%s/events", config.getDomain(),
                    config.getVersion(),
                    config.getEnvironmentId());
            String queryString = String.format("uid=%s&sid=%s&type=%s&score=%s",
                    ascendParticipant.getUserId(),
                    ascendParticipant.getSessionId(), type, score.toString());

            URI uri = new URI(config.getHttpScheme(), null, path, queryString,
                    null);

            URL url = uri.toURL();

            return url.toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    String getEventUrl(String type, String experimentId, String candidateId) {
        try {
            String path = String.format("//%s/%s/%s/events", config.getDomain(),
                    config.getVersion(),
                    config.getEnvironmentId());
            String queryString = String.format("uid=%s&sid=%s&eid=%s&cid=%s&type=%s",
                    ascendParticipant.getUserId(),
                    ascendParticipant.getSessionId(), experimentId, candidateId, type);

            URI uri = new URI(config.getHttpScheme(), null, path, queryString,
                    null);

            URL url = uri.toURL();

            return url.toString();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

}
