package ai.evolv.ascend.android;

import com.google.gson.JsonArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

import ai.evolv.ascend.android.exceptions.AscendKeyError;

class ExecutionQueue {

    private static Logger logger = LoggerFactory.getLogger(ExecutionQueue.class);

    private final ConcurrentLinkedQueue<Execution> queue;

    ExecutionQueue() {
        this.queue = new ConcurrentLinkedQueue<>();
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
                logger.warn("There was an error retrieving the value of %s from the allocation.",
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
