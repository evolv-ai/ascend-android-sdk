package ai.evolv;

import com.google.gson.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

import ai.evolv.exceptions.AscendKeyError;
import ai.evolv.generics.GenericClass;

public class AscendClientFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(AscendClientFactory.class);
    public static AscendClient init(AscendConfig config) {
        LOGGER.info("Initializing Ascend Client.");

        AscendAllocationStore store = config.getAscendAllocationStore();
        Allocator allocator = new Allocator(config);

        JsonArray previousAllocations = store.get();
        boolean reconciliationNeeded = false;
        if (Allocator.allocationsNotEmpty(previousAllocations)) {
            String storedUserId = previousAllocations.get(0).getAsJsonObject().get("uid").getAsString();
            config.getAscendParticipant().setUserId(storedUserId);
            reconciliationNeeded = true;
        }

        // fetch and reconcile allocations asynchronously
        Future<JsonArray> fetchedAllocations = allocator.fetchAllocations();

        return new AscendClientImpl(config, new EventEmitter(config), fetchedAllocations,
                                    allocator, reconciliationNeeded);
    }
}
