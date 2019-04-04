package ai.evolv.ascend.android;

import android.support.annotation.NonNull;

import ai.evolv.ascend.android.exceptions.AscendAllocationException;
import com.google.gson.JsonArray;

import java.util.concurrent.Future;

import ai.evolv.ascend.android.generics.GenericClass;
import timber.log.Timber;

public class AscendClient implements AscendClientInterface {

    private final EventEmitter eventEmitter;
    private final AscendAllocationStore store;
    private final Future<JsonArray> futureAllocations;

    private AscendClient(AscendConfig config, EventEmitter emitter,
                         Future<JsonArray> allocations) {
        this.eventEmitter = emitter;
        this.store = config.getAscendAllocationStore();
        this.futureAllocations = allocations;
    }

    public static synchronized AscendClient init(@NonNull AscendConfig config) {
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
        if (Allocator.allocationsNotEmpty(previousAllocations)) {
            String storedUserId = previousAllocations.get(0).getAsJsonObject().get("uid").getAsString();
            config.getAscendParticipant().setUserId(storedUserId);
        }

        Future<JsonArray> fetchedAllocations = allocator.fetchAllocations();

        return new AscendClient(config, new EventEmitter(config), fetchedAllocations);
    }

    @Override
    public <T> T get(String key, T defaultValue) {
        try {
            if (futureAllocations == null) {
                return defaultValue;
            }

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
    public void submit(String key, String defaultValue) {

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
        try {

        } catch E
        if (futureAllocations == null) {
            return;
        }

        JsonArray allocations = futureAllocations.get();
        if (!Allocator.allocationsNotEmpty(allocations)) {
            return;
        }

        JsonArray allocations = store.get();
        this.eventEmitter.confirm(allocations);
    }

    @Override
    public void contaminate() {
        if (!clientInitialized) {
            Timber.e("No contaminate event sent as the client was not properly initialized.");
            return;
        }

        JsonArray allocations = store.get();
        this.eventEmitter.contaminate(allocations);
    }
}
