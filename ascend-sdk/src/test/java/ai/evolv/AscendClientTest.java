package ai.evolv;

import com.google.gson.JsonArray;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class AscendClientTest {

    private static final String environmentId = "test_12345";
    private static final String rawAllocation = "[{\"uid\":\"test_uid\",\"sid\":\"test_sid\",\"eid\":\"test_eid\",\"cid\":\"test_cid\",\"genome\":{\"search\":{\"weighting\":{\"distance\":2.5,\"dealer_score\":2.5}},\"pages\":{\"all_pages\":{\"header_footer\":[\"blue\",\"white\"]},\"testing_page\":{\"megatron\":\"none\",\"header\":\"white\"}},\"algorithms\":{\"feature_importance\":false}},\"excluded\":false}]";

    @Mock
    private AscendConfig mockConfig;

    @Mock
    private HttpParticipantClient mockParticipantClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        if (mockConfig != null) {
            mockConfig = null;
        }
        if (mockParticipantClient != null) {
            mockParticipantClient = null;
        }
    }

    public AscendAllocationStore createAllocationStoreWithAllocations(final String raw) {
        return new AscendAllocationStore() {

            private JsonArray allocations = new AllocationsTest().parseRawAllocations(raw);

            @Override
            public JsonArray get() {
                return allocations;
            }

            @Override
            public void put(JsonArray allocations) {
                this.allocations = allocations;
            }
        };
    }

    @Test
    public void testClientInitializationPreviousAllocationsSameUser() {
        String returningAllocation = "[{\"uid\":\"returning_uid\",\"sid\":\"test_sid\",\"eid\":\"test_eid\",\"cid\":\"test_cid\",\"genome\":{\"search\":{\"weighting\":{\"distance\":2.5,\"dealer_score\":2.5}},\"pages\":{\"all_pages\":{\"header_footer\":[\"blue\",\"white\"]},\"testing_page\":{\"megatron\":\"none\",\"header\":\"white\"}},\"algorithms\":{\"feature_importance\":false}},\"excluded\":false}]";

        AscendConfig actualConfig = new AscendConfig.Builder(environmentId)
                .setAscendAllocationStore(createAllocationStoreWithAllocations(returningAllocation))
                .build();

        String oldUserId = actualConfig.getAscendParticipant().getUserId();

        AllocatorTest allocatorTest = new AllocatorTest();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig,
                mockParticipantClient, actualConfig);

        AscendClient client = AscendClient.init(mockConfig);
        Assert.assertNotNull(client);
        Assert.assertEquals(new AllocationsTest().parseRawAllocations(returningAllocation),
                actualConfig.getAscendAllocationStore().get());
        Assert.assertNotEquals(oldUserId, actualConfig.getAscendParticipant().getUserId());
        Assert.assertEquals("returning_uid", actualConfig.getAscendParticipant().getUserId());
    }

    @Test
    public void testGetValueFromClientIfErrorReturnDefault() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId).build();
        AllocatorTest allocatorTest = new AllocatorTest();
        when(mockParticipantClient.executeGetRequest(allocatorTest.createAllocationsUrl(actualConfig)))
                .thenReturn(allocatorTest.getMockedListenableFuture(rawAllocation));
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig,
                mockParticipantClient, actualConfig);

        AscendClient client = AscendClient.init(mockConfig);
        Boolean featureImportance = client.get("algorithms.key_error", true);
        Assert.assertNotNull(featureImportance);
        Assert.assertTrue(featureImportance);
    }

}
