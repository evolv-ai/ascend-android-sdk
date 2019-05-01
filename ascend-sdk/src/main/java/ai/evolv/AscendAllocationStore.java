package ai.evolv;

import com.google.gson.JsonArray;

public interface AscendAllocationStore {

    /**
     * Retrieves a JsonArray.
     * <p>
     *     Retrieves a JsonArray that represents the participant's allocations.
     *     If there are no stored allocations, should return null.
     * </p>
     * @param userId the participant's unique id
     * @return a participant's allocations
     */
    JsonArray get(String userId);

    /**
     * Stores a JsonArray.
     * <p>
     *     Stores the given JsonArray and maps it to the provided userId.
     * </p>
     * @param userId the participant's unique id
     * @param allocations a participant's allocations
     */
    void put(String userId, JsonArray allocations);

}
