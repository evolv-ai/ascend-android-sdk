package ai.evolv.utils;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import ai.evolv.HttpClient;


public class MockHttpClient implements HttpClient {

    private final String responseBody;

    public MockHttpClient(String responseBody) {
        this.responseBody = responseBody;
    }

    @Override
    public ListenableFuture<String> get(String url) {
        SettableFuture<String> futureResponse = SettableFuture.create();
        futureResponse.set(responseBody);

        return futureResponse;
    }

}
