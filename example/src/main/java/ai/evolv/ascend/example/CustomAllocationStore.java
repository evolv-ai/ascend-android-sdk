package ai.evolv.ascend.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import ai.evolv.ascend.android.AscendAllocationStore;

public class CustomAllocationStore implements AscendAllocationStore {

    private JsonArray allocations;

    CustomAllocationStore(String rawAllocations) {
        JsonParser jsonParser = new JsonParser();
        this.allocations = jsonParser.parse(rawAllocations).getAsJsonArray();
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
