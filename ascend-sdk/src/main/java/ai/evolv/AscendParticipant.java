package ai.evolv;

import java.util.UUID;

public class AscendParticipant {

    private final String sessionId;
    private String userId;

    private AscendParticipant(String userId, String sessionId) {
        this.userId = userId;
        this.sessionId = sessionId;
    }

    String getUserId() {
        return userId;
    }

    String getSessionId() {
        return sessionId;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    public static class Builder {

        private String userId = UUID.randomUUID().toString();
        private String sessionId = UUID.randomUUID().toString();

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public AscendParticipant build() {
            return new AscendParticipant(userId, sessionId);
        }

    }

}
