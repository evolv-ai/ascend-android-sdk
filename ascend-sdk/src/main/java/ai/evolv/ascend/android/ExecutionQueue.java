package ai.evolv.ascend.android;

import com.google.gson.JsonArray;

import java.util.LinkedList;

import ai.evolv.ascend.android.exceptions.AscendKeyError;
import timber.log.Timber;

class ExecutionQueue {

    private final LinkedList<Execution> queue;

    ExecutionQueue() {
        this.queue = new LinkedList<>();
    }

    void enqueue(Execution execution) {
        this.queue.add(execution);
    }

    void executeAllWithValuesFromAllocations(JsonArray allocations) {
        while (!queue.isEmpty()) {
            Execution execution = queue.remove();
            try {
                execution.executeWithAllocation(allocations);
            } catch (AscendKeyError e) {
                Timber.w("There was an error retrieving the value of %s from the allocation.",
                        execution.getKey());
            }
        }
    }

    void executeAllWithValuesFromDefaults() {
        while (!queue.isEmpty()) {
            Execution execution = queue.remove();
            execution.executeWithDefault();
        }
    }

}
