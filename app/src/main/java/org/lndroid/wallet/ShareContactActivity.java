package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.Errors;
import org.lndroid.framework.usecases.IRequestFactory;

public class ShareContactActivity extends AppCompatActivity {

    private static final String TAG = "ShareContactActivity";

    private long authRequestId_;
    private TextView state_;
    private EditText invoice_;
    private ImageView qrCode_;
    private Button cancel_;
    private ShareContactViewModel model_;
    private boolean generated_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_contact);

        model_ = ViewModelProviders.of(this).get(ShareContactViewModel.class);
        model_.getSessionToken(getApplicationContext());

        Intent intent = getIntent();
        authRequestId_ = intent.getLongExtra(Application.EXTRA_AUTH_REQUEST_ID, 0);
        if (authRequestId_ == 0) {
            Log.e(TAG, "No auth request id");
            finish();
            return;
        }

        final Button confirm = findViewById(R.id.confirm);
        cancel_ = findViewById(R.id.cancel);
        invoice_ = findViewById(R.id.invoice);
        state_ = findViewById(R.id.state);
        qrCode_ = findViewById(R.id.qrCode);

        cancel_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResult();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContactInvoice();
            }
        });

        model_.addContactInvoice().setCallback(this, new IResponseCallback<WalletData.AddContactInvoiceResponse>() {
            @Override
            public void onResponse(WalletData.AddContactInvoiceResponse r) {
                Log.i(TAG, "invoice " + r);

                invoice_.setText(r.paymentRequest());
                generated_ = true;
                cancel_.setText("Done");
                state_.setText("Share this QR code, or copy the string below.");

                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(
                            r.paymentRequest(), BarcodeFormat.QR_CODE, 600, 600);
                    qrCode_.setImageBitmap(bitmap);
                } catch(Exception e) {
                    state_.setText ("QR code error: "+e);
                }
            }

            @Override
            public void onError(String code, String e) {
                state_.setText("Error: " + e);
                Log.e(TAG, "create invoice error " + code + " msg " + e);
            }
        });
        model_.addContactInvoice().setRequestFactory(this, new IRequestFactory<WalletData.AddContactInvoiceRequest>() {
            @Override
            public WalletData.AddContactInvoiceRequest create() {
                return WalletData.AddContactInvoiceRequest.builder().build();
            }
        });

        model_.rpcAuthorize().setRequestFactory(this, new IRequestFactory<WalletData.AuthResponse>() {
            @Override
            public WalletData.AuthResponse create() {

                return WalletData.AuthResponse.builder()
                        .setAuthId(authRequestId_)
                        .setAuthorized(generated_)
                        .setAuthUserId(WalletServer.getInstance().getCurrentUserId().userId())
                        .build();
            }
        });
        model_.rpcAuthorize().setCallback(this, new IResponseCallback<Boolean>() {
            @Override
            public void onResponse(Boolean r) {
                Log.i(TAG, "auth result accepted");
                finish();
            }

            @Override
            public void onError(String code, String e) {
                Log.e(TAG, "auth rejected e " + code + " m " + e);
                if (Errors.TX_TIMEOUT.equals(code))
                    finish();
                else
                    state_.setText("Error: " + e);
            }
        });

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.addContactInvoice().isExecuting())
            addContactInvoice();
        if (model_.rpcAuthorize().isExecuting())
            sendResult();
    }

    private void addContactInvoice() {
        state_.setText("Preparing...");
        if (model_.addContactInvoice().isExecuting())
            model_.addContactInvoice().recover();
        else
            model_.addContactInvoice().execute("");
    }

    private void sendResult(){
        // if we already tried then just exit
        if (model_.rpcAuthorize().request() != null) {
            finish();
            return;
        }

        state_.setText("Please wait...");
        if (model_.rpcAuthorize().isExecuting())
            model_.rpcAuthorize().recover();
        else
            model_.rpcAuthorize().execute();
    }

    public void onBackPressed() {
        sendResult();
    }
}
