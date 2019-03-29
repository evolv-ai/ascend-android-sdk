package ai.evolv.ascend.android;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import ai.evolv.ascend.android.exceptions.AscendKeyError;

class Allocations {

    private final JsonArray allocations;

    Allocations(JsonArray allocations) {
        this.allocations = allocations;
    }

    <T> T getValueFromGenome(String key, Class<T> cls) throws AscendKeyError {
        ArrayList<String> keyParts = new ArrayList<>(Arrays.asList(key.split("\\.")));
        JsonElement value = getGenomeFromAllocations().get("genome");
        for (String part : keyParts) {
            JsonObject jsonObject = value.getAsJsonObject();
            value = jsonObject.get(part);
        }

        if (value == null) {
            throw new AscendKeyError("Could not find value for key:" + key);
        }

        Gson gson = new Gson();
        return gson.fromJson(value, cls);
    }

    JsonObject getGenomeFromAllocations() {
        JsonObject genome = new JsonObject();
        for (JsonElement allocation : allocations) {
            JsonObject originalGenome = allocation.getAsJsonObject().getAsJsonObject("genome");
            Set<Map.Entry<String, JsonElement>> entrySet = originalGenome.entrySet();
            for(Map.Entry<String, JsonElement> entry : entrySet){
                genome.add(entry.getKey(), originalGenome.get(entry.getKey()));
            }
        }
        JsonObject genomeWrapped = new JsonObject();
        genomeWrapped.add("genome", genome);
        return genomeWrapped;
    }
}
