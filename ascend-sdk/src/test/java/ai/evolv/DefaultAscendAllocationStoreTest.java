package ai.evolv;

import com.google.gson.JsonArray;

import org.junit.Assert;
import org.junit.Test;

public class DefaultAscendAllocationStoreTest {

    private static final String rawAllocation = "[{\"uid\":\"test_uid\",\"sid\":\"test_sid\",\"eid\":\"test_eid\",\"cid\":\"test_cid\",\"genome\":{\"search\":{\"weighting\":{\"distance\":2.5,\"dealer_score\":2.5}},\"pages\":{\"all_pages\":{\"header_footer\":[\"blue\",\"white\"]},\"testing_page\":{\"megatron\":\"none\",\"header\":\"white\"}},\"algorithms\":{\"feature_importance\":false}},\"excluded\":false}]";

    @Test
    public void testEmptyStoreRGetsEmptyJsonArray() {
        AscendAllocationStore store = new DefaultAscendAllocationStore();
        Assert.assertNotNull(store.get());
        Assert.assertEquals(0, store.get().size());
        Assert.assertEquals(new JsonArray(), store.get());
    }

    @Test
    public void testPutAndGetOnStore() {
        AscendAllocationStore store = new DefaultAscendAllocationStore();
        JsonArray allocations = new AllocationsTest().parseRawAllocations(rawAllocation);
        store.put(allocations);
        JsonArray storedAllocations = store.get();
        Assert.assertNotNull(storedAllocations);
        Assert.assertNotEquals(new JsonArray(), storedAllocations);
        Assert.assertEquals(allocations, storedAllocations);
    }
}
