package ai.evolv;

import com.google.gson.JsonArray;

public class DefaultAllocationStore implements AscendAllocationStore {

    private static JsonArray allocations;

    DefaultAllocationStore() {
        this.allocations = new JsonArray();
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
