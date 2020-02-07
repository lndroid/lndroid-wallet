package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.lndroid.framework.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.ActionDecodePayReq;
import org.lndroid.framework.usecases.IRequestFactory;
import org.lndroid.framework.usecases.rpc.RPCAuthorize;

public class AddAppContactActivity extends AppCompatActivity {

    private static final String TAG = "AddAppContactActivity";

    private int authRequestId_;
    private EditText payReq_;
    private EditText name_;
    private TextView state_;
    private AddAppContactViewModel model_;
    private boolean rejected_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_app);

        model_ = ViewModelProviders.of(this).get(AddAppContactViewModel.class);

        Intent intent = getIntent();
        authRequestId_ = intent.getIntExtra(Application.EXTRA_AUTH_REQUEST_ID, 0);
        if (authRequestId_ == 0) {
            Log.e(TAG, "No auth request id");
            finish();
            return;
        }

        Button confirm = findViewById(R.id.confirm);
        Button cancel = findViewById(R.id.cancel);
        Button scan = findViewById(R.id.scan);
        payReq_ = findViewById(R.id.payreq);
        name_ = findViewById(R.id.name);
        state_ = findViewById(R.id.state);

        model_.scanResult().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null && !s.isEmpty()) {
                    payReq_.setText(s);
                    payReq_.setEnabled(false);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reject();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decodePayReq();
            }
        });
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });

        ActionDecodePayReq decodePayReq = model_.decodePayReq();
        decodePayReq.setCallback(this, new IResponseCallback<WalletData.SendPayment>() {
            @Override
            public void onResponse(WalletData.SendPayment r) {
                Log.i(TAG, "invoice " + r);
                // FIXME instead, show all fields prefilled and let user update them?
                confirm();
            }

            @Override
            public void onError(String code, String e) {
                state_.setText("Error: " + e);
                Log.e(TAG, "decode payreq error " + code + " msg " + e);
            }
        });
        decodePayReq.setRequestFactory(this, new IRequestFactory<String>() {
            @Override
            public String create() {
                String r = payReq_.getText().toString();
                if (r.isEmpty()) {
                    state_.setText("Please specify payreq");
                    return null;
                }
                return r;
            }
        });

        final RPCAuthorize rpcAuthorize = model_.rpcAuthorize();
        rpcAuthorize.setRequestFactory(this, new IRequestFactory<WalletData.AuthResponse>() {
            @Override
            public WalletData.AuthResponse create() {

                if (rejected_) {
                    return WalletData.AuthResponse.builder()
                            .setAuthId(authRequestId_)
                            .setAuthorized(false)
                            .setAuthUserId(WalletServer.getInstance().getCurrentUserId().userId())
                            .build();
                } else {

                    ActionDecodePayReq payReq = model_.decodePayReq();

                    WalletData.Contact r = WalletData.Contact.builder()
                            .setPubkey(payReq.response().destPubkey())
                            .setName(name_.getText().toString())
                            .setRouteHints(payReq.response().routeHints())
                            .setFeatures(payReq.response().features())
                            .build();

                    return WalletData.AuthResponse.builder()
                            .setAuthId(authRequestId_)
                            .setAuthorized(true)
                            .setAuthUserId(WalletServer.getInstance().getCurrentUserId().userId())
                            .setData(r)
                            .build();
                }
            }
        });
        rpcAuthorize.setCallback(this, new IResponseCallback<Boolean>() {
            @Override
            public void onResponse(Boolean r) {
                Log.i(TAG, "auth result accepted");
                finish();
            }

            @Override
            public void onError(String code, String e) {
                Log.e(TAG, "auth rejected e " + code + " m " + e);
                state_.setText("Error: " + e);
            }
        });

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.decodePayReq().isExecuting())
            decodePayReq();
        else if (model_.rpcAuthorize().isExecuting())
            sendResult();
    }

    private void decodePayReq() {
        state_.setText("Decoding...");
        if (model_.decodePayReq().isExecuting())
            model_.decodePayReq().recover();
        else
            model_.decodePayReq().execute("");
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
        reject();
    }

    private void reject() {
        rejected_ = true;
        sendResult();
    }

    private void confirm() {
        rejected_ = false;
        sendResult();
    }

    private void startScan(){
        model_.startScan();
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                reject();
            } else {
                model_.setScanResult(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
