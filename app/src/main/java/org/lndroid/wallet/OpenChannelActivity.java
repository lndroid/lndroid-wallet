package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.WalletData;
import org.lndroid.framework.usecases.IRequestFactory;

public class OpenChannelActivity extends WalletActivityBase {

    private static final String TAG = "OpenChannelActivity";

    private EditText pubkey_;
    private EditText amount_;
    private EditText pushAmount_;
    private TextView state_;
    private Switch isPrivate_;
    private OpenChannelViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_channel);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Open channel");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        model_ = ViewModelProviders.of(this).get(OpenChannelViewModel.class);
        setModel(model_);

        pubkey_ = findViewById(R.id.pubkey);
        amount_ = findViewById(R.id.amount);
        pushAmount_ = findViewById(R.id.pushAmount);
        isPrivate_ = findViewById(R.id.isPrivate);
        state_ = findViewById(R.id.state);

        Button button = findViewById(R.id.openChannel);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openChannel();
            }
        });

        model_.openChannel().setCallback(this, new IResponseCallback<WalletData.Channel>() {
            @Override
            public void onResponse(WalletData.Channel c) {
                Log.i(TAG, "channel " + c);
                state_.setText("Opening channel");
                showChannel(c.id());
                finish();
            }

            @Override
            public void onError(String code, String e) {
                state_.setText("Error: " + code + ": " + e);
                Log.e(TAG, "channel error " + code + " msg " + e);
            }
        });
        model_.openChannel().setRequestFactory(this, new IRequestFactory<WalletData.OpenChannelRequest>() {
            @Override
            public WalletData.OpenChannelRequest create() {

                Long amount = 0L;
                Long pushAmount = 0L;
                try {
                    if (!amount_.getText().toString().isEmpty())
                        amount = Long.valueOf(amount_.getText().toString());
                    if (!pushAmount_.getText().toString().isEmpty())
                        pushAmount = Long.valueOf(pushAmount_.getText().toString());
                } catch (NumberFormatException e) {
                    state_.setText("Please enter the amounts as numbers");
                    return null;
                }

                if (amount == 0) {
                    state_.setText("Please specify the amount");
                    return null;
                }

                return WalletData.OpenChannelRequest.builder()
                        .setNodePubkey(pubkey_.getText().toString())
                        .setLocalFundingAmount(amount)
                        .setPushSat(pushAmount)
                        .setIsPrivate(isPrivate_.isChecked())
                        .build();
            }
        });
        // FIXME actionAddInvoice.setAuthCallback();

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.openChannel().isExecuting())
            openChannel();
    }

    private void openChannel() {
        state_.setText("Creating channel...");
        if (model_.openChannel().isExecuting())
            model_.openChannel().recover();
        else
            model_.openChannel().execute("");
    }

    private void showChannel(long id) {
        Intent intent = new Intent(this, GetChannelActivity.class);
        intent.putExtra(Application.ID_MESSAGE, id);
        startActivity(intent);
    }

}
