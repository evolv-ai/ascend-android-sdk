package ai.evolv.ascend.android;

import android.support.test.runner.AndroidJUnit4;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonArray;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ai.evolv.ascend.android.exceptions.AscendAllocationException;
import okhttp3.HttpUrl;

@RunWith(AndroidJUnit4.class)
public class AllocatorTest {

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

    AscendConfig setUpMockedAscendConfigWithMockedClient(AscendConfig mockedConfig, HttpParticipantClient mockedClient, AscendConfig actualConfig) {
        when(mockedConfig.getParticipantClient()).thenReturn(mockedClient);
        when(mockedConfig.getAscendParticipant()).thenReturn(actualConfig.getAscendParticipant());
        when(mockedConfig.getHttpScheme()).thenReturn(actualConfig.getHttpScheme());
        when(mockedConfig.getDomain()).thenReturn(actualConfig.getDomain());
        when(mockedConfig.getVersion()).thenReturn(actualConfig.getVersion());
        when(mockedConfig.getEnvironmentId()).thenReturn(actualConfig.getEnvironmentId());
        when(mockedConfig.getTimeout()).thenReturn(actualConfig.getTimeout());
        when(mockedConfig.getAscendAllocationStore())
                .thenReturn(actualConfig.getAscendAllocationStore());

        return mockedConfig;
    }

    HttpUrl createAllocationsUrl(AscendConfig config) {
        return new HttpUrl.Builder()
                .scheme(config.getHttpScheme())
                .host(config.getDomain())
                .addPathSegment(config.getVersion())
                .addPathSegment(config.getEnvironmentId())
                .addPathSegment("allocations")
                .addQueryParameter("uid", config.getAscendParticipant().getUserId())
                .addQueryParameter("sid", config.getAscendParticipant().getSessionId())
                .build();
    }

    ListenableFuture<String> getMockedListenableFuture(final String raw) {
        return new ListenableFuture<String>() {
            @Override
            public void addListener(Runnable listener, Executor executor) {

            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public String get() throws ExecutionException, InterruptedException {
                return null;
            }

            @Override
            public String get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
                return raw;
            }
        };
    }

    @Test
    public void testCreateAllocationsUrl() {
        AscendConfig actualConfig = new AscendConfig.Builder(environmentId).build();
        mockConfig = setUpMockedAscendConfigWithMockedClient(mockConfig, mockParticipantClient,
                actualConfig);

        Allocator allocator = new Allocator(mockConfig);
        HttpUrl url = allocator.createAllocationsUrl();
        Assert.assertEquals(createAllocationsUrl(actualConfig), url);
    }

    @Test
    public void testFetchAllocations() {

        AscendConfig actualConfig = new AscendConfig.Builder(environmentId).build();
        when(mockParticipantClient.executeGetRequest(createAllocationsUrl(actualConfig)))
                .thenReturn(getMockedListenableFuture(rawAllocation));
        mockConfig = setUpMockedAscendConfigWithMockedClient(mockConfig, mockParticipantClient,
                actualConfig);

        try {
            Allocator allocator = new Allocator(mockConfig);
            JsonArray expAllocations = new AllocationsTest().parseRawAllocations(rawAllocation);
            JsonArray allocations = allocator.fetchAllocations();
            Assert.assertEquals(expAllocations, allocations);
        } catch (AscendAllocationException e) {
            Assert.fail(e.getMessage());
        }
    }

}
