package ai.evolv.ascend.example;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import ai.evolv.ascend.android.AscendAllocationStore;
import ai.evolv.ascend.android.AscendClient;
import ai.evolv.ascend.android.AscendConfig;

public class MainActivity extends AppCompatActivity {

    private AscendClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String myStoredAllocation = "[{\"uid\":\"sandbox_user\",\"eid\":\"experiment_1\",\"cid\":\"candidate_3\",\"genome\":{\"ui\":{\"layout\":\"option_2\",\"buttons\":{\"checkout\":{\"text\":\"Begin Secure Checkout\",\"color\":\"#f3b36d\"},\"info\":{\"text\":\"Product Specifications\",\"color\":\"#f3b36d\"}}},\"search\":{\"weighting\":3.5}},\"excluded\":false}]";
        AscendAllocationStore store = new CustomAllocationStore(myStoredAllocation);

        AscendConfig config = new AscendConfig.Builder("sandbox")
                .setTimeout(5000)
                .setAscendAllocationStore(store)
                .build();
        client = AscendClient.init(config);

        String layoutOption = client.get("ui.layout", "option_1");

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

        String checkoutButtonText = client.get("ui.buttons.checkout.text",
                "Buy Now");
        String checkoutButtonColor = client.get("ui.buttons.checkout.color",
                "#2f5e5d");
        String infoButtonText = client.get("ui.buttons.info.text",
                "Product Info");
        String infoButtonColor = client.get("ui.buttons.info.color",
                "#2f5e5d");

        setButton(R.id.checkoutButton, checkoutButtonText, checkoutButtonColor);
        setButton(R.id.infoButton, infoButtonText, infoButtonColor);

        client.confirm();
    }

    public void pressCheckout(View view) {
        client.emitEvent("convert");
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
