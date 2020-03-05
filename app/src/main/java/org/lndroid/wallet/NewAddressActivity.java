package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.IRequestFactory;

public class NewAddressActivity extends WalletActivityBase {

    private static final String TAG = "NewAddressActivity";

    private EditText address_;
    private TextView state_;
    private ImageView qrCode_;
    private NewAddressViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_address);

        model_ = ViewModelProviders.of(this).get(NewAddressViewModel.class);
        setModel(model_);

        address_ = findViewById(R.id.address);
        state_ = findViewById(R.id.state);
        qrCode_ = findViewById(R.id.qrCode);

        findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(
                        Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("address", address_.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(view.getContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
            }
        });

        Button button = findViewById(R.id.done);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        model_.newAddress().setCallback(this, new IResponseCallback<WalletData.NewAddress>() {
            @Override
            public void onResponse(WalletData.NewAddress a) {
                Log.i(TAG, "address "+a);
                state_.setText("New address generated");
                address_.setText(a.address());

                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(
                            a.address(), BarcodeFormat.QR_CODE, 600, 600);
                    qrCode_.setImageBitmap(bitmap);
                } catch(Exception e) {
                    state_.setText ("QR code error: "+e);
                }
            }

            @Override
            public void onError(String code, String e) {
                state_.setText("Error: "+code+": "+e);
                Log.e(TAG, "address error "+code+" msg "+e);
            }
        });
        model_.newAddress().setRequestFactory(this, new IRequestFactory<WalletData.NewAddressRequest>() {
            @Override
            public WalletData.NewAddressRequest create() {
                return WalletData.NewAddressRequest.builder()
                        .setType(WalletData.NEW_ADDRESS_WITNESS_PUBKEY_HASH)
                        .build();
            }
        });
        // FIXME actionAddInvoice.setAuthCallback();

        recoverUseCases();
    }

    private void recoverUseCases() {
//        if (model_.newAddress().isExecuting())
        // generate address immediately on activity open
        newAddress();
    }

    private void newAddress() {
        state_.setText("Creating address...");
        if (model_.newAddress().isExecuting())
            model_.newAddress().recover();
        else
            model_.newAddress().execute("");
    }
}
