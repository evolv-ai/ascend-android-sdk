package ai.evolv.ascend.android;

import com.google.gson.JsonArray;

public interface AscendAllocationStore {

    /**
     * Retrieves a JsonArray.
     * <p>
     *     Retrieves a JsonArray that represents the participant's allocations.
     * </p>
     * @return a participant's allocations
     */
    JsonArray get();

    /**
     * Stores a JsonArray.
     * <p>
     *     Stores the given JsonArray.
     * </p>
     * @param allocations a participant's allocations
     */
    void put(JsonArray allocations);

}
