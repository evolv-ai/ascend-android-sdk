package ai.evolv.ascend.android;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

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

    static JsonArray reconcileAllocations(JsonArray previousAllocations, @NotNull JsonArray currentAllocations) {
        /*
         * Check the current allocations for any allocations that belong to experiments in the previous
         * allocations. If there are, keep the previous allocations. If there are any live experiments
         * that are not in the previous allocations add the new allocation to the allocations list.
         */
        JsonArray allocations = new JsonArray();

        for (JsonElement ca : currentAllocations) {
            JsonObject currentAllocation = ca.getAsJsonObject();
            String currentEid = currentAllocation.get("eid").toString();
            boolean previousFound = false;

            for (JsonElement pa : previousAllocations) {
                JsonObject previousAllocation = pa.getAsJsonObject();
                String previousEid = previousAllocation.get("eid").toString();

                if (previousEid.equals(currentEid)) {
                    allocations.add(pa.getAsJsonObject());
                    previousFound = true;
                }
            }

            if (!previousFound) {
                allocations.add(ca.getAsJsonObject());
            }
        }

        return allocations;
    }
}
