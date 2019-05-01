package ai.evolv.ascend.example;

import com.google.gson.JsonArray;

import java.util.HashMap;
import java.util.Map;

import ai.evolv.AscendAllocationStore;

public class CustomAllocationStore implements AscendAllocationStore {
    /*
     A custom in memory allocation store, this is a very basic example. One would likely use
     sqlLite or an application storage implementation instead.
     */

    private final Map<String, JsonArray> allocations;

    CustomAllocationStore() {
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
