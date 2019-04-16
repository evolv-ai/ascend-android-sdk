package ai.evolv;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EventEmitterTest {

    private static final String environmentId = "test_12345";
    private static final String type = "test";
    private static final double score = 10.0;
    private static final String eid = "test_eid";
    private static final String cid = "test_cid";
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


    static String createAllocationEventUrl(AscendConfig config, JsonObject allocation, String event) {
        return String.format("%s://%s/%s/%s/events?uid=%s&sid=%s&eid=%s&cid=%s&type=%s",
                config.getHttpScheme(),
                config.getDomain(),
                config.getVersion(),
                config.getEnvironmentId(),
                config.getAscendParticipant().getUserId(),
                config.getAscendParticipant().getSessionId(),
                allocation.get("eid").getAsString(),
                allocation.get("cid").getAsString(),
                event);
    }

    private String createEventsUrl(AscendConfig config, String type, Double score) {
        return String.format("%s://%s/%s/%s/events?uid=%s&sid=%s&type=%s&score=%s",
                config.getHttpScheme(),
                config.getDomain(),
                config.getVersion(),
                config.getEnvironmentId(),
                config.getAscendParticipant().getUserId(),
                config.getAscendParticipant().getSessionId(),
                type, score.toString());
    }

    @Test
    public void testGetEventUrl() {
        AscendConfig actualConfig = AscendConfig.builder(environmentId, mockHttpClient).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, actualConfig,
                mockExecutionQueue, mockHttpClient, mockAllocationStore);

        EventEmitter emitter = new EventEmitter(mockConfig);
        String url = emitter.getEventUrl(type, score);
        Assert.assertEquals(createEventsUrl(actualConfig, type, score), url);
    }

    @Test
    public void testGetEventUrlWithEidAndCid() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId, mockHttpClient).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, actualConfig, mockExecutionQueue,
                mockHttpClient, mockAllocationStore);
        JsonArray allocations = new JsonParser().parse(rawAllocation).getAsJsonArray();

        EventEmitter emitter = new EventEmitter(mockConfig);
        String url = emitter.getEventUrl(type, eid, cid);
        Assert.assertEquals(createAllocationEventUrl(actualConfig, allocations.get(0).getAsJsonObject(), type), url);
    }

    @Test
    public void testSendAllocationEvents() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId, mockHttpClient).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, actualConfig, mockExecutionQueue,
                mockHttpClient, mockAllocationStore);
        JsonArray allocations = new JsonParser().parse(rawAllocation).getAsJsonArray();

        EventEmitter emitter = new EventEmitter(mockConfig);
        emitter.sendAllocationEvents(type, allocations);

        verify(mockHttpClient, times(1))
                .get(createAllocationEventUrl(actualConfig, allocations.get(0).getAsJsonObject(), type));
    }

    @Test
    public void testContaminateEvent() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId, mockHttpClient).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, actualConfig, mockExecutionQueue,
                mockHttpClient, mockAllocationStore);
        JsonArray allocations = new JsonParser().parse(rawAllocation).getAsJsonArray();

        EventEmitter emitter = new EventEmitter(mockConfig);
        emitter.contaminate(allocations);

        verify(mockHttpClient, times(1))
                .get(createAllocationEventUrl(actualConfig, allocations.get(0).getAsJsonObject(), EventEmitter.CONTAMINATE_KEY));
    }

    @Test
    public void testConfirmEvent() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId, mockHttpClient).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, actualConfig, mockExecutionQueue,
                mockHttpClient, mockAllocationStore);
        JsonArray allocations = new JsonParser().parse(rawAllocation).getAsJsonArray();

        EventEmitter emitter = new EventEmitter(mockConfig);
        emitter.confirm(allocations);

        verify(mockHttpClient, times(1))
                .get(createAllocationEventUrl(actualConfig, allocations.get(0).getAsJsonObject(), EventEmitter.CONFIRM_KEY));
    }

    @Test
    public void testGenericEvent() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId, mockHttpClient).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, actualConfig, mockExecutionQueue,
                mockHttpClient, mockAllocationStore);
        JsonArray allocations = new JsonParser().parse(rawAllocation).getAsJsonArray();

        EventEmitter emitter = new EventEmitter(mockConfig);
        emitter.emit(type);

        verify(mockHttpClient, times(1))
                .get(createEventsUrl(actualConfig, type, 1.0));
    }

    @Test
    public void testGenericEventWithScore() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId, mockHttpClient).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, actualConfig, mockExecutionQueue,
                mockHttpClient, mockAllocationStore);
        JsonArray allocations = new JsonParser().parse(rawAllocation).getAsJsonArray();

        EventEmitter emitter = new EventEmitter(mockConfig);
        emitter.emit(type, score);

        verify(mockHttpClient, times(1))
                .get(createEventsUrl(actualConfig, type, score));
    }


}
