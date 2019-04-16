package ai.evolv;

import ai.evolv.exceptions.AscendKeyError;
import ai.evolv.generics.GenericClass;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AscendClientImpl implements AscendClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AscendClientImpl.class);

    private final EventEmitter eventEmitter;
    private final ListenableFuture<JsonArray> futureAllocations;
    private final ExecutionQueue executionQueue;
    private final Allocator allocator;
    private final AscendAllocationStore store;
    private final boolean previousAllocations;

    AscendClientImpl(AscendConfig config, EventEmitter emitter,
                     ListenableFuture<JsonArray> allocations, Allocator allocator,
                     boolean previousAllocations) {
        this.store = config.getAscendAllocationStore();
        this.executionQueue = config.getExecutionQueue();
        this.eventEmitter = emitter;
        this.futureAllocations = allocations;
        this.allocator = allocator;
        this.previousAllocations = previousAllocations;
    }

    @Override
    public <T> T get(String key, T defaultValue) {
        try {
            if (futureAllocations == null) {
                return defaultValue;
            }

            // this is blocking
            JsonArray allocations = futureAllocations.get();
            if (!Allocator.allocationsNotEmpty(allocations)) {
                return defaultValue;
            }

            GenericClass<T> cls = new GenericClass(defaultValue.getClass());
            return new Allocations(allocations).getValueFromGenome(key, cls.getMyType());
        } catch (Exception e) {
            LOGGER.error("There was as error retrieving the requested value. Returning " +
                    "the default.", e);
            return defaultValue;
        }
    }

    @Override
    public <T> void subscribe(String key, T defaultValue, AscendAction<T> function) {
        Execution execution = new Execution<>(key, defaultValue, function);
        if (previousAllocations) {
            try {
                JsonArray allocations = store.get();
                execution.executeWithAllocation(allocations);
            } catch (AscendKeyError e) {
                LOGGER.warn("There was an error retrieving the value of %s from the allocation.",
                        execution.getKey());
                execution.executeWithDefault();
            }
        }

        Allocator.AllocationStatus allocationStatus = allocator.getAllocationStatus();
        if (allocationStatus == Allocator.AllocationStatus.FETCHING) {
            executionQueue.enqueue(execution);
            return;
        } else if (allocationStatus == Allocator.AllocationStatus.RETRIEVED) {
            try {
                JsonArray allocations = store.get();
                execution.executeWithAllocation(allocations);
                return;
            } catch (AscendKeyError e) {
                LOGGER.warn("There was an error retrieving the value of %s from the allocation.",
                        execution.getKey());
            }
        }

        execution.executeWithDefault();
    }

    @Override
    public void emitEvent(String key, Double score) {
        this.eventEmitter.emit(key, score);
    }

    @Override
    public void emitEvent(String key) {
        this.eventEmitter.emit(key);
    }

    @Override
    public void confirm() {
        Allocator.AllocationStatus allocationStatus = allocator.getAllocationStatus();
        if (allocationStatus == Allocator.AllocationStatus.FETCHING) {
            allocator.sandBagConfirmation();
        } else if (allocationStatus == Allocator.AllocationStatus.RETRIEVED) {
            eventEmitter.confirm(store.get());
        }
    }

    @Override
    public void contaminate() {
        Allocator.AllocationStatus allocationStatus = allocator.getAllocationStatus();
        if (allocationStatus == Allocator.AllocationStatus.FETCHING) {
            allocator.sandBagContamination();
        } else if (allocationStatus == Allocator.AllocationStatus.RETRIEVED) {
            eventEmitter.contaminate(store.get());
        }
    }
}
