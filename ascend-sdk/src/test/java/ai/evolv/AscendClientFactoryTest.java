package ai.evolv;

import com.google.common.util.concurrent.SettableFuture;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ai.evolv.utils.MockHttpClient;

public class AscendClientFactoryTest {

    private static final String environmentId = "test_12345";
    private static final String rawAllocation = "[{\"uid\":\"test_uid\",\"sid\":\"test_sid\",\"eid\":\"test_eid\",\"cid\":\"test_cid\",\"genome\":{\"search\":{\"weighting\":{\"distance\":2.5,\"dealer_score\":2.5}},\"pages\":{\"all_pages\":{\"header_footer\":[\"blue\",\"white\"]},\"testing_page\":{\"megatron\":\"none\",\"header\":\"white\"}},\"algorithms\":{\"feature_importance\":false}},\"excluded\":false}]";

    @Mock
    private AscendConfig mockConfig;

    @Mock
    private ExecutionQueue mockExecutionQueue;

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private AscendAllocationStore mockAllocationStore;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        if (mockConfig != null) {
            mockConfig = null;
        }

        if (mockExecutionQueue != null) {
            mockExecutionQueue = null;
        }

        if (mockHttpClient != null) {
            mockHttpClient = null;
        }

        if (mockAllocationStore != null) {
            mockAllocationStore = null;
        }
    }

    static String createAllocationsUrl(AscendConfig config, AscendParticipant participant) {
        return String.format("%s://%s/%s/%s/allocations?uid=%s&sid=%s",
                config.getHttpScheme(),
                config.getDomain(),
                config.getVersion(),
                config.getEnvironmentId(),
                participant.getUserId(),
                participant.getSessionId());
    }

    @Test
    public void testClientInit() {
        AscendConfig actualConfig = AscendConfig.builder(environmentId, mockHttpClient).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, actualConfig,
                mockExecutionQueue, mockHttpClient, mockAllocationStore);

        SettableFuture responseFuture = SettableFuture.create();
        responseFuture.set(rawAllocation);
        when(mockHttpClient.get(anyString())).thenReturn(responseFuture);

        AscendClient client = AscendClientFactory.init(mockConfig);
        verify(mockAllocationStore, times(2)).get(anyString());
        Assert.assertTrue(client instanceof AscendClient);
    }

    @Test
    public void testClientInitSameUser() {
        AscendParticipant participant = AscendParticipant.builder().setUserId("test_uid").build();
        HttpClient mockClient = new MockHttpClient(rawAllocation);

        AscendConfig actualConfig = AscendConfig.builder(environmentId, mockHttpClient).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, actualConfig,
                mockExecutionQueue, mockClient, mockAllocationStore);

        JsonArray previousAllocations = new JsonParser().parse(rawAllocation).getAsJsonArray();
        String previousUid = previousAllocations.get(0).getAsJsonObject().get("uid").getAsString();
        when(mockAllocationStore.get(participant.getUserId())).thenReturn(previousAllocations);

        AscendClient client = AscendClientFactory.init(mockConfig, participant);
        verify(mockAllocationStore, times(2)).get(participant.getUserId());
        Assert.assertTrue(client instanceof AscendClient);
    }

}
