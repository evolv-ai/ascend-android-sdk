package ai.evolv;

import com.google.common.util.concurrent.ListenableFuture;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.HttpUrl;

public class AllocatorTest {

    private static final String environmentId = "test_12345";

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

}
