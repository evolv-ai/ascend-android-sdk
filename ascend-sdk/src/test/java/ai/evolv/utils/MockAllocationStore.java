package ai.evolv.utils;

import ai.evolv.AscendAllocationStore;
import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.Map;

public class MockAllocationStore implements AscendAllocationStore {

    private final Map<String, JsonArray> allocations;

    MockAllocationStore() {
        this.allocations = new HashMap<>();
    }

    @Override
    public JsonArray get(String userId) {
        return allocations.get(userId);
    }

    @Override
    public void put(String userId, JsonArray allocations) {
        this.allocations.put(userId, allocations);
    }

}
