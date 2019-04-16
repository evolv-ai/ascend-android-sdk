package ai.evolv.utils;

import ai.evolv.AscendAllocationStore;
import com.google.gson.JsonArray;

public class MockAllocationStore implements AscendAllocationStore {

    private JsonArray allocations;

    public MockAllocationStore(JsonArray allocations) {
        this.allocations = allocations;
    }

    @Override
    public JsonArray get() {
        return allocations;
    }

    @Override
    public void put(JsonArray allocations) {
        this.allocations = allocations;
    }

}
