package ai.evolv.ascend.android;

import android.support.annotation.NonNull;

import com.google.gson.JsonArray;

import java.util.concurrent.Future;

import ai.evolv.ascend.android.exceptions.AscendKeyError;
import ai.evolv.ascend.android.generics.GenericClass;
import timber.log.Timber;

public class AscendClient implements AscendClientInterface {

    private final EventEmitter eventEmitter;
    private final Future<JsonArray> futureAllocations;
    private final ExecutionQueue executionQueue;
    private final Allocator allocator;
    private final AscendAllocationStore store;
    private final boolean previousAllocations;

    private AscendClient(AscendConfig config, EventEmitter emitter,
                         Future<JsonArray> allocations, Allocator allocator,
                         boolean previousAllocations) {
        this.store = config.getAscendAllocationStore();
        this.executionQueue = config.getExecutionQueue();
        this.eventEmitter = emitter;
        this.futureAllocations = allocations;
        this.allocator = allocator;
        this.previousAllocations = previousAllocations;
    }

    public static AscendClient init(@NonNull AscendConfig config) {
        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }

        Timber.i("Initializing Ascend Client." +
                        "\nVersion: %s" +
                        "\nAscendParticipant API Endpoint: %s" +
                        "\nAscendParticipant API Version: %s" +
                        "\nEnvironment ID: %s",
                BuildConfig.VERSION_NAME,
                config.getDomain(),
                config.getVersion(),
                config.getEnvironmentId());

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

        return new AscendClient(config, new EventEmitter(config), fetchedAllocations, allocator, reconciliationNeeded);
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
            Timber.e("There was as error retrieving the requested value. Returning the default.");
            Timber.e(e);
            return defaultValue;
        }
    }

    @Override
    public <T> void subscribe(String key, T defaultValue, AscendAction<T> function) {
        Execution execution = new Execution(key, defaultValue, function);
        if (previousAllocations) {
            try {
                JsonArray allocations = store.get();
                execution.executeWithAllocation(allocations);
            } catch (AscendKeyError e) {
                Timber.w("There was an error retrieving the value of %s from the allocation.",
                        execution.getKey());
            }
        }

        Allocator.AllocationStatus allocationStatus = allocator.getAllocationStatus();
        if (allocationStatus == Allocator.AllocationStatus.FETCHING) {
            executionQueue.enqueue(execution);
            return;
        } else if (allocationStatus == Allocator.AllocationStatus.RETRIEVED){
            try {
                JsonArray allocations = store.get();
                execution.executeWithAllocation(allocations);
                return;
            } catch (AscendKeyError e) {
                Timber.w("There was an error retrieving the value of %s from the allocation.",
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
        } else if (allocationStatus == Allocator.AllocationStatus.RETRIEVED){
            eventEmitter.confirm(store.get());
        }
    }

    @Override
    public void contaminate() {
        Allocator.AllocationStatus allocationStatus = allocator.getAllocationStatus();
        if (allocationStatus == Allocator.AllocationStatus.FETCHING) {
            allocator.sandBagContamination();
        } else if (allocationStatus == Allocator.AllocationStatus.RETRIEVED){
            eventEmitter.contaminate(store.get());
        }
    }
}
