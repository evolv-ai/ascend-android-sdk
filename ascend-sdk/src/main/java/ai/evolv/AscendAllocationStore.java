package ai.evolv;

import com.google.gson.JsonArray;

public interface AscendAllocationStore {

    /**
     * Retrieves a JsonArray.
     * <p>
     *     Retrieves a JsonArray that represents the participant's allocations. If there are no stored allocations,
     *     should return an empty JsonArray.
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
