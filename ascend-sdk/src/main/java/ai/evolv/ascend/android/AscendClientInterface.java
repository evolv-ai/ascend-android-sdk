package ai.evolv.ascend.android;

public interface AscendClientInterface {

    /**
     * Retrieves a value from the participant's allocation, returns a default upon error.
     * <p>
     *     Given a unique key this method will retrieve the key's associated value. A default value
     *     can also be specified in case any errors occur during the values retrieval. If the allocation
     *     call times out or fails the default value is always returned.
     * </p>
     * @param key a unique key identifying a specific value in the participants allocation
     * @param defaultValue a default value to return upon error
     * @return a value associated with the given key
     */
    <T> T get(String key, T defaultValue);

    void submit(String key, String defaultValue);

    /**
     * Emits a generic event to be recorded by Ascend.
     * <p>
     *     Sends an event to Ascend to be recorded and reported upon. Also records a generic score
     *     value to be associated with the event.
     * </p>
     * @param key the identifier of the event
     * @param score a score to be associated with the event
     */
    void emitEvent(String key, Double score);

    /**
     * Emits a generic event to be recorded by Ascend.
     * <p>
     *     Sends an event to Ascend to be recorded and reported upon.
     * </p>
     * @param key the identifier of the event
     */
    void emitEvent(String key);

    /**
     * Sends a confirmed event to Ascend.
     * <p>
     *     Method produces a confirmed event which confirms the participant's allocation. Method will not do anything
     *     in the event that the allocation timed out or failed.
     * </p>
     */
    void confirm();

    /**
     * Sends a contamination event to Ascend.
     * <p>
     *     Method produces a contamination event which will contaminate the participant's
     *     allocation. Method will not do anything in the event that the allocation timed out or failed.
     * </p>
     */
    void contaminate();

}
