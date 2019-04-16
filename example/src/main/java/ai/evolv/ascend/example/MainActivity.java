package ai.evolv.ascend.example;

import ai.evolv.AscendClientFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ai.evolv.AscendAllocationStore;
import ai.evolv.AscendClient;
import ai.evolv.AscendConfig;
import ai.evolv.HttpClient;
import ai.evolv.OkHttpClientImpl;

public class MainActivity extends AppCompatActivity {

    private AscendClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String myStoredAllocation = "[{\"uid\":\"sandbox_user\",\"eid\":\"experiment_1\",\"cid\":\"candidate_3\",\"genome\":{\"ui\":{\"layout\":\"option_2\",\"buttons\":{\"checkout\":{\"text\":\"Begin Secure Checkout\",\"color\":\"#f3b36d\"},\"info\":{\"text\":\"Product Specifications\",\"color\":\"#f3b36d\"}}},\"search\":{\"weighting\":3.5}},\"excluded\":false}]";
        AscendAllocationStore store = new CustomAllocationStore(myStoredAllocation);

        HttpClient httpClient = new OkHttpClientImpl(3000);

        // build config with custom timeout and custom allocation store
        // set client to use sandbox environment
        AscendConfig config = AscendConfig.builder("sandbox", httpClient)
                .setAscendAllocationStore(store)
                .build();

        // initialize the client
        client = AscendClientFactory.init(config);

        client.subscribe("ui.layout", "option_1", layoutOption -> {
            runOnUiThread(() -> {
                switch (layoutOption) {
                    case "option_1":
                        setContentView(R.layout.layout_one);
                        break;
                    case "option_2":
                        setContentView(R.layout.layout_two);
                        break;
                    default:
                        setContentView(R.layout.layout_one);
                        break;
                }
            });
        });

        client.subscribe("ui.buttons.checkout.text", "Test Message", checkoutButtonText -> {
            runOnUiThread(() -> {
                TextView showCountTextView = findViewById(R.id.checkoutButton);
                showCountTextView.setText(checkoutButtonText);
            });
        });


        client.confirm();
    }

    public void pressCheckout(View view) {
        client.emitEvent("conversion");
        Toast convMessage = Toast.makeText(this, "Conversion!",
                Toast.LENGTH_SHORT);
        convMessage.show();
    }

    public void pressInfo(View view) {
        Toast infoMessage = Toast.makeText(this, "Some really cool product info!",
                Toast.LENGTH_SHORT);
        infoMessage.show();
    }

    private void setButton(int buttonTextId, String text, String colorHex) {
        TextView showCountTextView = findViewById(buttonTextId);
        showCountTextView.setText(text);
        showCountTextView.setBackgroundColor(Color.parseColor(colorHex));
    }
}
