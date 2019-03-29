package ai.evolv.ascend.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ai.evolv.ascend.android.AscendClient;
import ai.evolv.ascend.android.AscendConfig;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String ascendEnvironmentId = "c2e7f5201d";
        AscendConfig config = new AscendConfig.Builder("c2e7f5201d")
                .setTimeout(5000)
                .setHttpScheme("https")
                .build();
        AscendClient client = AscendClient.init(config);

        Boolean featureImportance = client.get("algorithms.feature_importance", true);

        client.confirm();

        Timber.i("Feature importance is on: %b", featureImportance);
    }
}
