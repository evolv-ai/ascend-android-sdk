package ai.evolv.ascend.android;

public class AscendConfig {

    static final String DEFAULT_HTTP_SCHEME = "https";
    static final String DEFAULT_DOMAIN = "participants.evolv.ai";
    static final String DEFAULT_API_VERSION = "v1";
    static final long DEFAULT_TIMEOUT = 1000;
    static final AscendAllocationStore DEFAULT_ALLOCATION_STORE = new DefaultAscendAllocationStore();
    static final AscendParticipant DEFAULT_ASCEND_PARTICIPANT = new AscendParticipant.Builder().build();

    private final String httpScheme;
    private final String domain;
    private final String version;
    private final String environmentId;
    private final long timeout;
    private final AscendAllocationStore ascendAllocationStore;
    private final AscendParticipant ascendParticipant;
    private final HttpParticipantClient participantClient;
    private final ExecutionQueue executionQueue;

    private AscendConfig(String httpScheme, String domain, String version, String environmentId,
                         long timeout, AscendAllocationStore ascendAllocationStore, AscendParticipant ascendParticipant) {
        this.httpScheme = httpScheme;
        this.domain = domain;
        this.version = version;
        this.environmentId = environmentId;
        this.timeout = timeout;
        this.ascendAllocationStore = ascendAllocationStore;
        this.ascendParticipant = ascendParticipant;
        this.participantClient = new HttpParticipantClient();
        this.executionQueue = new ExecutionQueue();
    }

    String getHttpScheme() {
        return httpScheme;
    }

    String getDomain() {
        return domain;
    }

    String getVersion() {
        return version;
    }

    String getEnvironmentId() {
        return environmentId;
    }

    AscendAllocationStore getAscendAllocationStore() {
        return ascendAllocationStore;
    }

    AscendParticipant getAscendParticipant() {
        return ascendParticipant;
    }

    long getTimeout() {
        return timeout;
    }

    HttpParticipantClient getParticipantClient() {
        return this.participantClient;
    }

    ExecutionQueue getExecutionQueue() {
        return this.executionQueue;
    }

    public static class Builder {

        private String httpScheme = DEFAULT_HTTP_SCHEME;
        private String domain = DEFAULT_DOMAIN;
        private String version = DEFAULT_API_VERSION;
        private long timeout = DEFAULT_TIMEOUT;
        private AscendAllocationStore ascendAllocationStore = DEFAULT_ALLOCATION_STORE;
        private AscendParticipant ascendParticipant = DEFAULT_ASCEND_PARTICIPANT;

        private String environmentId;

        /**
         * Responsible for creating an instance of AscendClientInterface.
         * <p>
         *     Builds an instance of the AscendClientInterface. The only required parameter is the
         *     customer's environment id.
         * </p>
         * @param environmentId unique id representing a customer's environment
         */
        public Builder(String environmentId) {
            this.environmentId = environmentId;
        }

        /**
         * Sets the domain of the underlying ascendParticipant api.
         * @param domain the domain of the ascendParticipant api
         * @return AscendClientBuilder class
         */
        public Builder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        /**
         * Version of the underlying ascendParticipant api.
         * @param version representation of the required ascendParticipant api version
         * @return AscendClientBuilder class
         */
        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        /**
         * Sets up a custom AscendAllocationStore. Store needs to implement the AscendAllocationStore interface.
         * @param ascendAllocationStore a custom built allocation store
         * @return AscendClientBuilder class
         */
        public Builder setAscendAllocationStore(AscendAllocationStore ascendAllocationStore) {
            this.ascendAllocationStore = ascendAllocationStore;
            return this;
        }

        /**
         * Sets up a custom AscendParticipant.
         * @param ascendParticipant a custom build ascendParticipant
         * @return AscendClientBuilder class
         */
        public Builder setAscendParticipant(AscendParticipant ascendParticipant) {
            this.ascendParticipant = ascendParticipant;
            return this;
        }

        /**
         * Sets a custom timeout (in milliseconds) for the Allocation call. If the allocation call takes longer
         * than this timeout, the default values are used and the confirmation and contamination events get squashed.
         * @param timeout number of milliseconds to wait for an allocation to occur
         * @return AscendClientBuilder class
         */
        public Builder setTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Tells the SDK to use either http or https.
         * @param scheme either http or https
         * @return AscendClientBuilder class
         */
        public Builder setHttpScheme(String scheme) {
            this.httpScheme = scheme;
            return this;
        }

        /**
         * Builds an instance of AscendClientInterface.
         * @return an AscendClientInterface instance
         */
        public AscendConfig build() {
            return new AscendConfig(httpScheme, domain, version,
                    environmentId, timeout, ascendAllocationStore, ascendParticipant);
        }

    }

}
