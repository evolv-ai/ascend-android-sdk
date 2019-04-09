package ai.evolv;

public interface AscendAction<T> {

    /**
     * Applies a given value to a set of instructions.
     * @param value any value that was requested
     */
    void apply(T value);

}
