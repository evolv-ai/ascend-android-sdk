package ai.evolv.ascend.android;

import com.google.gson.JsonArray;

import ai.evolv.ascend.android.exceptions.AscendKeyError;
import ai.evolv.ascend.android.generics.GenericClass;
import timber.log.Timber;

class Execution<T> {

    private final String key;
    private final T defaultValue;
    private final AscendAction function;

    Execution(String key, T defaultValue, AscendAction<T> function) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.function = function;
    }

    public String getKey() {
        return key;
    }

    void executeWithAllocation(JsonArray allocations) throws AscendKeyError {
        GenericClass<T> cls = new GenericClass(defaultValue.getClass());
        T value = new Allocations(allocations).getValueFromGenome(key, cls.getMyType());
        function.apply(value);
    }

    void executeWithDefault() {
        function.apply(defaultValue);
    }

}
