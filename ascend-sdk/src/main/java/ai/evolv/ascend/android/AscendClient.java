package ai.evolv.ascend.android;

import android.support.annotation.NonNull;

import ai.evolv.ascend.android.exceptions.AscendAllocationException;
import com.google.gson.JsonArray;

import ai.evolv.ascend.android.generics.GenericClass;
import timber.log.Timber;

public class AscendClient implements AscendClientInterface {

    private final EventEmitter eventEmitter;
    private final AscendAllocationStore store;
    private final Boolean clientInitialized;

    private AscendClient(AscendConfig config, EventEmitter emitter,
                         Boolean clientInitialized) {
        this.eventEmitter = emitter;
        this.store = config.getAscendAllocationStore();
        this.clientInitialized = clientInitialized;
    }

    @SuppressWarnings("unused")
    public static synchronized AscendClient init(@NonNull AscendConfig config) {
        boolean clientInitialized = false;

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

        try {
            JsonArray allocations = store.get();
            if (allocations != null && allocations.size() > 0) {
                String storedUserId = allocations.get(0).getAsJsonObject().get("uid").getAsString();
                config.getAscendParticipant().setUserId(storedUserId);
                // TODO negotiate allocations here
            } else {
                allocations = allocator.fetchAllocations();
            }
            store.put(allocations);
            clientInitialized = true;
        } catch (AscendAllocationException e) {
            Timber.e("There was an error fetching allocations, returning defaults from now on.");
            Timber.e(e);
        }

        return new AscendClient(config, new EventEmitter(config), clientInitialized);
    }

    @Override
    public <T> T get(String key, T defaultValue) {
        if (!clientInitialized) {
            Timber.e("Returning default because the client has not been properly initialized.");
            return defaultValue;
        }
        try {
            JsonArray allocations = store.get();
            GenericClass<T> cls = new GenericClass(defaultValue.getClass());
            return new Allocations(allocations).getValueFromGenome(key, cls.getMyType());
        } catch (Exception e) {
            Timber.e("There was as error retrieving the requested value. Returning the default.");
            Timber.e(e);
            return defaultValue;
        }
    }

    @Override
    public void emitEvent(String key, Double score) {
        if (!clientInitialized) {
            Timber.w("Client hasn't been properly initialized.");
        }

        this.eventEmitter.emit(key, score);
    }

    @Override
    public void emitEvent(String key) {
        if (!clientInitialized) {
            Timber.w("Client hasn't been properly initialized.");
        }

        this.eventEmitter.emit(key);
    }

    @Override
    public void confirm() {
        if (!clientInitialized) {
            Timber.e("No confirm event sent as the client was not properly initialized.");
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
