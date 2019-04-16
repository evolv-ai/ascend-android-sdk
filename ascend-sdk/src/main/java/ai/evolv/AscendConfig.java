package ai.evolv;

public class AscendConfig {

    static final String DEFAULT_HTTP_SCHEME = "https";
    static final String DEFAULT_DOMAIN = "participants.evolv.ai";
    static final String DEFAULT_API_VERSION = "v1";
    static final AscendAllocationStore DEFAULT_ALLOCATION_STORE =
            new DefaultAscendAllocationStore();
    static final AscendParticipant DEFAULT_ASCEND_PARTICIPANT =
            new AscendParticipant.Builder().build();

    private final String httpScheme;
    private final String domain;
    private final String version;
    private final String environmentId;
    private final AscendAllocationStore ascendAllocationStore;
    private final AscendParticipant ascendParticipant;
    private final HttpClient httpClient;
    private final ExecutionQueue executionQueue;

    private AscendConfig(String httpScheme, String domain, String version,
                         String environmentId,
                         AscendAllocationStore ascendAllocationStore,
                         AscendParticipant ascendParticipant,
                         HttpClient httpClient) {
        this.httpScheme = httpScheme;
        this.domain = domain;
        this.version = version;
        this.environmentId = environmentId;
        this.ascendAllocationStore = ascendAllocationStore;
        this.ascendParticipant = ascendParticipant;
        this.httpClient = httpClient;
        this.executionQueue = new ExecutionQueue();
    }

    public static Builder builder(String environmentId, HttpClient httpClient) {
        return new Builder(environmentId, httpClient);
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

    HttpClient getHttpClient() {
        return this.httpClient;
    }

    ExecutionQueue getExecutionQueue() {
        return this.executionQueue;
    }

    public static class Builder {

        private String httpScheme = DEFAULT_HTTP_SCHEME;
        private String domain = DEFAULT_DOMAIN;
        private String version = DEFAULT_API_VERSION;
        private AscendAllocationStore ascendAllocationStore = DEFAULT_ALLOCATION_STORE;
        private AscendParticipant ascendParticipant = DEFAULT_ASCEND_PARTICIPANT;

        private String environmentId;
        private HttpClient httpClient;

        /**
         * Responsible for creating an instance of AscendClientImpl.
         * <p>
         *     Builds an instance of the AscendClientImpl. The only required parameter is the
         *     customer's environment id.
         * </p>
         * @param environmentId unique id representing a customer's environment
         */
        Builder(String environmentId, HttpClient httpClient) {
            this.environmentId = environmentId;
            this.httpClient = httpClient;
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
         * Sets up a custom AscendAllocationStore. Store needs to implement the
         * AscendAllocationStore interface.
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
         * Tells the SDK to use either http or https.
         * @param scheme either http or https
         * @return AscendClientBuilder class
         */
        public Builder setHttpScheme(String scheme) {
            this.httpScheme = scheme;
            return this;
        }

        /**
         * Builds an instance of AscendClientImpl.
         * @return an AscendClientImpl instance
         */
        public AscendConfig build() {
            return new AscendConfig(httpScheme, domain, version,
                    environmentId, ascendAllocationStore, ascendParticipant, httpClient);
        }

    }

}
