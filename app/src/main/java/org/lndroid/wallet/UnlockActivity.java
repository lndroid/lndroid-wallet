package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.Errors;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.usecases.IRequestFactory;

public class UnlockActivity extends AppCompatActivity {

    private static final String TAG = "UnlockActivity";
    private EditText password_;
    private TextView state_;
    private UnlockViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock);

        model_ = ViewModelProviders.of(this).get(UnlockViewModel.class);

        Button button = findViewById(R.id.unlock);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unlock();
            }
        });

        password_ = findViewById(R.id.walletPassword);
        state_ = findViewById(R.id.state);

        model_.unlockRPC().setRequestFactory(this, new IRequestFactory<WalletData.UnlockWalletRequest>() {

            @Override
            public WalletData.UnlockWalletRequest create() {
                WalletData.UnlockWalletRequest r = new WalletData.UnlockWalletRequest();
                r.walletPassword = password_.getText().toString().getBytes();
                return r;
            }
        });
        model_.unlockRPC().setCallback(this, new IResponseCallback<WalletData.UnlockWalletResponse>() {
            @Override
            public void onResponse(WalletData.UnlockWalletResponse r) {
                Log.i(TAG, "unlocked ");
                startMain();
            }

            @Override
            public void onError(String s, String s1) {
                Log.e(TAG, "Failed to call unlock: "+s+" err "+s1);
                if (Errors.REJECTED.equals(s)) {
                    state_.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            model_.unlockRPC().execute();
                        }
                    }, 1000);
                } else {
                    state_.setText("Error: " + s1);
                }
            }
        });

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.unlockRPC().isExecuting())
            unlock();
    }

    private void unlock() {
        state_.setText("Unlocking wallet...");
        if (model_.unlockRPC().isExecuting())
            model_.unlockRPC().recover();
        else
            model_.unlockRPC().execute();
    }

    private void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
