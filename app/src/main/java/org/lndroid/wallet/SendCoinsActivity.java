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

import com.google.common.collect.ImmutableMap;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.usecases.IRequestFactory;

public class SendCoinsActivity extends WalletActivityBase {

    private static final String TAG = "SendCoinsActivity";

    private EditText address_;
    private EditText amount_;
    private EditText confs_;
    private TextView fee_;
    private TextView state_;
    private SendCoinsViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_coins);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Send coins");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        model_ = ViewModelProviders.of(this).get(SendCoinsViewModel.class);
        setModel(model_);

        address_ = findViewById(R.id.address);
        amount_ = findViewById(R.id.amount);
        confs_ = findViewById(R.id.confs);
        fee_ = findViewById(R.id.fee);
        state_ = findViewById(R.id.state);

        Button button = findViewById(R.id.sendCoins);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCoins();
            }
        });
        button = findViewById(R.id.scan);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScan();
            }
        });

        model_.scanResult().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null) {
                    address_.setText(s);
                } else {
                    address_.setText("");
                    state_.setText("Scan failed");
                }
            }
        });

        View.OnFocusChangeListener onFocus = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus)
                    calcFees();
            }
        };

        address_.setOnFocusChangeListener(onFocus);
        amount_.setOnFocusChangeListener(onFocus);
        confs_.setOnFocusChangeListener(onFocus);

        model_.sendCoins().setCallback(this, new IResponseCallback<WalletData.Transaction>() {
            @Override
            public void onResponse(WalletData.Transaction t) {
                Log.i(TAG, "transaction "+t);
                showTransaction(t.id());
                finish();
            }

            @Override
            public void onError(String code, String e) {
                state_.setText("Error "+code+": "+e);
                Log.e(TAG, "sendCoins error "+code+" msg "+e);
            }
        });
        model_.sendCoins().setRequestFactory(this, new IRequestFactory<WalletData.SendCoinsRequest>() {
            @Override
            public WalletData.SendCoinsRequest create() {
                if (address_.getText().toString().isEmpty())
                    return null;

                long amount = 0;
                int confs = 0;
                try {
                    amount = Long.parseLong(amount_.getText().toString());
                    confs = Integer.parseInt(confs_.getText().toString());
                } catch (NumberFormatException e) {
                }

                ImmutableMap.Builder<String, Long> b = ImmutableMap.builder();
                b.put(address_.getText().toString(), amount);

                return WalletData.SendCoinsRequest.builder()
                        .setTargetConf(confs)
                        .setAddrToAmount(b.build())
                        .build();
            }
        });
        // FIXME actionAddInvoice.setAuthCallback();

        model_.estimateFee().setCallback(this, new IResponseCallback<WalletData.EstimateFeeResponse>() {
            @Override
            public void onResponse(WalletData.EstimateFeeResponse r) {
                Log.i(TAG, "fees " + r);
                state_.setText("");
                fee_.setText("Fees: "+r.feeSat()+" sats, "+r.feerateSatPerByte()+" per byte.");
            }

            @Override
            public void onError(String code, String e) {
                state_.setText("Error: " + e);
                Log.e(TAG, "estimate fee error " + code + " msg " + e);
            }
        });
        model_.estimateFee().setRequestFactory(this, new IRequestFactory<WalletData.EstimateFeeRequest>() {
            @Override
            public WalletData.EstimateFeeRequest create() {
                if (address_.getText().toString().isEmpty())
                    return null;

                long amount = 0;
                int confs = 0;
                try {
                    amount = Long.parseLong(amount_.getText().toString());
                } catch (NumberFormatException e) {
                    return null;
                }

                try {
                    confs = Integer.parseInt(confs_.getText().toString());
                } catch (NumberFormatException e) {
                }

                ImmutableMap.Builder<String, Long> b = ImmutableMap.builder();
                b.put(address_.getText().toString(), amount);

                return WalletData.EstimateFeeRequest.builder()
                        .setTargetConf(confs)
                        .setAddrToAmount(b.build())
                        .build();
            }
        });

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.sendCoins().isExecuting())
            sendCoins();
        else if (model_.estimateFee().isExecuting())
            calcFees();
    }

    private void sendCoins() {
        state_.setText("Creating transaction...");
        if (model_.sendCoins().isExecuting())
            model_.sendCoins().recover();
        else
            model_.sendCoins().execute("");
    }

    private void calcFees() {
        if (address_.getText().toString().isEmpty())
            return;

        state_.setText("Estimating fees...");
        if (model_.estimateFee().isExecuting())
            model_.estimateFee().recover();
        else
            model_.estimateFee().execute("");
    }

    private void showTransaction(long id) {
        Intent intent = new Intent(this, GetTransactionActivity.class);
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
            model_.setScanResult(result.getContents());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
