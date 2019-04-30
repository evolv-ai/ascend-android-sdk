package ai.evolv;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonArray;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AscendClientFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AscendClientFactory.class);

    /**
     * Creates instances of the AscendClient.
     * @param config an instance of AscendConfig
     * @return an instance of AscendClient
     */
    public static AscendClient init(AscendConfig config) {
        LOGGER.debug("Initializing Ascend Client.");

        AscendAllocationStore store = config.getAscendAllocationStore();
        Allocator allocator = new Allocator(config);

        JsonArray previousAllocations = store.get();
        boolean reconciliationNeeded = false;
        if (Allocator.allocationsNotEmpty(previousAllocations)) {
            String storedUserId = previousAllocations.get(0)
                    .getAsJsonObject().get("uid")
                    .getAsString();
            config.getAscendParticipant().setUserId(storedUserId);
            reconciliationNeeded = true;
        }

        // fetch and reconcile allocations asynchronously
        ListenableFuture<JsonArray> fetchedAllocations = allocator.fetchAllocations();

        return new AscendClientImpl(config, new EventEmitter(config), fetchedAllocations,
                allocator, reconciliationNeeded);
    }
}
