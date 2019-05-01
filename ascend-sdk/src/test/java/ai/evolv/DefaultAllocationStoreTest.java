package ai.evolv;

import com.google.gson.JsonArray;

import org.junit.Assert;
import org.junit.Test;

public class DefaultAllocationStoreTest {

    private static final String rawAllocation = "[{\"uid\":\"test_uid\",\"sid\":\"test_sid\",\"eid\":\"test_eid\",\"cid\":\"test_cid\",\"genome\":{\"search\":{\"weighting\":{\"distance\":2.5,\"dealer_score\":2.5}},\"pages\":{\"all_pages\":{\"header_footer\":[\"blue\",\"white\"]},\"testing_page\":{\"megatron\":\"none\",\"header\":\"white\"}},\"algorithms\":{\"feature_importance\":false}},\"excluded\":false}]";

    @Test
    public void testEmptyStoreRGetsEmptyJsonArray() {
        AscendParticipant participant = AscendParticipant.builder().build();
        AscendAllocationStore store = new DefaultAllocationStore();
        Assert.assertNull(store.get(participant.getUserId()));
    }

    @Test
    public void testPutAndGetOnStore() {
        AscendParticipant participant = AscendParticipant.builder().setUserId("test_uid").build();
        AscendAllocationStore store = new DefaultAllocationStore();
        JsonArray allocations = new AllocationsTest().parseRawAllocations(rawAllocation);
        store.put(participant.getUserId(), allocations);
        JsonArray storedAllocations = store.get(participant.getUserId());
        Assert.assertNotNull(storedAllocations);
        Assert.assertNotEquals(new JsonArray(), storedAllocations);
        Assert.assertEquals(allocations, storedAllocations);
    }
}
