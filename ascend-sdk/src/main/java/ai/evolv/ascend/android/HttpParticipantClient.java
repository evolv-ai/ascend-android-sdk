package ai.evolv.ascend.android;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Callback;
import timber.log.Timber;

class HttpParticipantClient {

    private final OkHttpClient httpClient;

    HttpParticipantClient() {
        this.httpClient = new OkHttpClient();
    }

    synchronized ListenableFuture<String> executeGetRequest(HttpUrl url) {
        final Request request = new Request.Builder()
                .url(url)
                .build();

        final SettableFuture<String> responseFuture = SettableFuture.create();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                Timber.e("There was an error while sending a GET request.");
            }

            @Override public void onResponse(Call call, Response response) {
                String body = "";
                try {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        body = responseBody.string();
                    }
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected response when making GET request: " + response + " using url: "
                                + request.url() + " with body: " + body);
                    }

                    responseFuture.set(body);
                } catch (Exception e) {
                    Timber.e(e);
                    responseFuture.setException(e);
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }
            }
        });

        return  responseFuture;
    }

}
