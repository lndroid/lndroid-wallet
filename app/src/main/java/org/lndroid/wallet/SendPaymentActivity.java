package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.IRequestFactory;
import org.lndroid.framework.usecases.JobSendPayment;

public class SendPaymentActivity extends WalletActivityBase {

    private static final String TAG = "SendPaymentActivity";

    private EditText payReq_;
    private TextView state_;
    private SendPaymentViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_payment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Send payment");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        model_ = ViewModelProviders.of(this).get(SendPaymentViewModel.class);
        setModel(model_);

        payReq_ = findViewById(R.id.payReq);
        state_ = findViewById(R.id.state);

        Button button = findViewById(R.id.sendPayment);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendPayment();
            }
        });
        Button scan = findViewById(R.id.scan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startScan();
            }
        });

        model_.scanResult().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null && !s.isEmpty()) {
                    if (s.startsWith("lightning:"))
                        s = s.substring("lightning:".length());
                    payReq_.setText(s);
                }
            }
        });

        JobSendPayment sendPaymentJob = model_.sendPaymentJob();
        sendPaymentJob.setCallback(this, new IResponseCallback<WalletData.SendPayment>() {
            @Override
            public void onResponse(WalletData.SendPayment p) {
                Log.i(TAG, "payment "+p);
                state_.setText("id "+p.id()+" state "+p.state());
                showPayment(p.id());
                finish();
            }

            @Override
            public void onError(String code, String e) {
                state_.setText("Error "+code+": "+e);
                Log.e(TAG, "payment error "+code+" msg "+e);
            }
        });
        sendPaymentJob.setRequestFactory(this, new IRequestFactory<WalletData.SendPaymentRequest>() {
            @Override
            public WalletData.SendPaymentRequest create() {
                return WalletData.SendPaymentRequest.builder()
                        .setPaymentRequest(payReq_.getText().toString())
                        .setMaxTries(20)
                        .build();
            }
        });
        // FIXME actionAddInvoice.setAuthCallback();

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.sendPaymentJob().isExecuting())
            sendPayment();
    }

    private void sendPayment() {
        state_.setText("Creating payment...");
        if (model_.sendPaymentJob().isExecuting())
            model_.sendPaymentJob().recover();
        else
            model_.sendPaymentJob().execute("");
    }

    private void showPayment(long id) {
        Intent intent = new Intent(this, GetSendPaymentActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }

    private void startScan(){
        model_.startScan();
        new IntentIntegrator(this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() != null) {
                model_.setScanResult(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
