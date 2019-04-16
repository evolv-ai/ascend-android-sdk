package ai.evolv;

import ai.evolv.exceptions.AscendKeyError;

import com.google.gson.JsonArray;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                execution.executeWithDefault();
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
