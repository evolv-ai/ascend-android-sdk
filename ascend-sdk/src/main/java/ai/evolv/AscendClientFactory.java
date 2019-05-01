package ai.evolv;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AscendClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AscendClientFactory.class);

    /**
     * Creates instances of the AscendClient.
     *
     * @param config an instance of AscendConfig
     * @return an instance of AscendClient
     */
    public static AscendClient init(AscendConfig config) {
        LOGGER.debug("Initializing Ascend Client.");
        AscendParticipant participant = AscendParticipant.builder().build();
        return AscendClientFactory.createClient(config, participant);
    }

    /**
     * Creates instances of the AscendClient.
     *
     * @param config an instance of AscendConfig
     * @param participant an instance of AscendParticipant
     * @return an instance of AscendClient
     */
    public static AscendClient init(AscendConfig config, AscendParticipant participant) {
        LOGGER.debug("Initializing Ascend Client.");
        return AscendClientFactory.createClient(config, participant);
    }

    private static AscendClient createClient(AscendConfig config, AscendParticipant participant) {
        AscendAllocationStore store = config.getAscendAllocationStore();
        JsonArray previousAllocations = store.get(participant.getUserId());
        boolean reconciliationNeeded = false;
        if (Allocator.allocationsNotEmpty(previousAllocations)) {
            reconciliationNeeded = true;
        }

        Allocator allocator = new Allocator(config, participant);

        // fetch and reconcile allocations asynchronously
        ListenableFuture<JsonArray> futureAllocations = allocator.fetchAllocations();

        return new AscendClientImpl(config,
                new EventEmitter(config, participant),
                futureAllocations,
                allocator,
                reconciliationNeeded,
                participant);
    }
}
