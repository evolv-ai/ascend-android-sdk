package ai.evolv;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import okhttp3.HttpUrl;

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

    private HttpUrl createEventsUrl(AscendConfig config, String type, double score) {
        return new HttpUrl.Builder()
                .scheme(config.getHttpScheme())
                .host(config.getDomain())
                .addPathSegment(config.getVersion())
                .addPathSegment(config.getEnvironmentId())
                .addPathSegment("events")
                .addQueryParameter("uid", config.getAscendParticipant().getUserId())
                .addQueryParameter("sid", config.getAscendParticipant().getSessionId())
                .addQueryParameter("type", type)
                .addQueryParameter("score", Double.toString(score))
                .build();
    }

    private HttpUrl createEventsUrl(AscendConfig config, String type, String eid, String cid) {
        return new HttpUrl.Builder()
                .scheme(config.getHttpScheme())
                .host(config.getDomain())
                .addPathSegment(config.getVersion())
                .addPathSegment(config.getEnvironmentId())
                .addPathSegment("events")
                .addQueryParameter("uid", config.getAscendParticipant().getUserId())
                .addQueryParameter("sid", config.getAscendParticipant().getSessionId())
                .addQueryParameter("eid", eid)
                .addQueryParameter("cid", cid)
                .addQueryParameter("type", type)
                .build();
    }

    @Test
    public void testGetEventUrl() {

        AscendConfig actualConfig = new AscendConfig.Builder(environmentId).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, mockParticipantClient,
                actualConfig);

        EventEmitter emitter = new EventEmitter(mockConfig);
        HttpUrl url = emitter.getEventUrl(type, score);
        Assert.assertEquals(createEventsUrl(actualConfig, type, score), url);
    }

    @Test
    public void testGetEventUrlWithEidAndCid() {

        AscendConfig actualConfig = new AscendConfig.Builder(environmentId).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, mockParticipantClient,
                actualConfig);

        EventEmitter emitter = new EventEmitter(mockConfig);
        HttpUrl url = emitter.getEventUrl(type, eid, cid);
        Assert.assertEquals(createEventsUrl(actualConfig, type, eid, cid), url);
    }

    @Test
    public void testSendAllocationEvents() {

        AscendConfig actualConfig = new AscendConfig.Builder(environmentId).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, mockParticipantClient,
                actualConfig);

        EventEmitter emitter = new EventEmitter(mockConfig);
        emitter.sendAllocationEvents(type, new AllocationsTest().parseRawAllocations(rawAllocation));

        verify(mockParticipantClient, times(1))
                .executeGetRequest(createEventsUrl(actualConfig, type, eid, cid));
    }

    @Test
    public void testContaminateEvent() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, mockParticipantClient,
                actualConfig);

        EventEmitter emitter = new EventEmitter(mockConfig);
        emitter.contaminate(new AllocationsTest().parseRawAllocations(rawAllocation));

        verify(mockParticipantClient, times(1))
                .executeGetRequest(createEventsUrl(actualConfig, EventEmitter.CONTAMINATE_KEY, eid, cid));
    }

    @Test
    public void testConfirmEvent() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, mockParticipantClient,
                actualConfig);

        EventEmitter emitter = new EventEmitter(mockConfig);
        emitter.confirm(new AllocationsTest().parseRawAllocations(rawAllocation));

        verify(mockParticipantClient, times(1))
                .executeGetRequest(createEventsUrl(actualConfig, EventEmitter.CONFIRM_KEY, eid, cid));
    }

    @Test
    public void testGenericEvent() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, mockParticipantClient,
                actualConfig);

        EventEmitter emitter = new EventEmitter(mockConfig);
        emitter.emit(type);

        verify(mockParticipantClient, times(1))
                .executeGetRequest(createEventsUrl(actualConfig, type, 1.0));
    }

    @Test
    public void testGenericEventWithScore() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId).build();
        mockConfig = new AllocatorTest().setUpMockedAscendConfigWithMockedClient(mockConfig, mockParticipantClient,
                actualConfig);

        EventEmitter emitter = new EventEmitter(mockConfig);
        emitter.emit(type, score);

        verify(mockParticipantClient, times(1))
                .executeGetRequest(createEventsUrl(actualConfig, type, score));
    }


}
