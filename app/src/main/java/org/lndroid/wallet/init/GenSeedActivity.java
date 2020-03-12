package org.lndroid.wallet.init;

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
import org.lndroid.wallet.MainActivity;
import org.lndroid.wallet.R;

public class GenSeedActivity extends AppCompatActivity {

    private static final String TAG = "GenSeedActivity";
    private EditText password_;
//    private EditText aezeedPassword_;
    private TextView seed_;
    private TextView state_;
    private GenSeedViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gen_seed);

        model_ = ViewModelProviders.of(this).get(GenSeedViewModel.class);

        Button button = findViewById(R.id.back);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button create = findViewById(R.id.createWallet);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPassword())
                    initWallet();
            }
        });

        password_ = findViewById(R.id.walletPassword);
//        aezeedPassword_ = findViewById(R.id.aezeedPassword);
        state_ = findViewById(R.id.state);
        seed_ = findViewById(R.id.seed);

        updateWalletPassword();

        model_.genSeedRPC().setRequestFactory(this, new IRequestFactory<WalletData.GenSeedRequest>() {

            @Override
            public WalletData.GenSeedRequest create() {
                WalletData.GenSeedRequest r = new WalletData.GenSeedRequest();
//                r.aezeedPassphrase = aezeedPassword_.getText().toString().getBytes();
                return r;
            }
        });
        model_.genSeedRPC().setCallback(this, new IResponseCallback<WalletData.GenSeedResponse>() {
            @Override
            public void onResponse(WalletData.GenSeedResponse genSeedResponse) {
                Log.i(TAG, "seed generated");
                String text = "";
                for(int i = 0; i < genSeedResponse.cipherSeedMnemonic.size(); i++) {
                    String word = genSeedResponse.cipherSeedMnemonic.get(i);
                    text += (i+1)+". "+word+"\n";
                }
                seed_.setText(text);
                state_.setText("");
                updateWalletPassword();
            }

            @Override
            public void onError(String s, String s1) {
                Log.e(TAG, "Failed to call genSeed: "+s+" err "+s1);
                if (Errors.REJECTED.equals(s)) {
                    state_.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                           model_.genSeedRPC().execute();
                        }
                    }, 1000);
                } else {
                    state_.setText("GenSeed error: " + s1);
                }
            }
        });

        model_.initWalletRPC().setRequestFactory(this, new IRequestFactory<WalletData.InitWalletRequest>() {
            @Override
            public WalletData.InitWalletRequest create() {
                WalletData.InitWalletRequest r = new WalletData.InitWalletRequest();
                r.cipherSeedMnemonic = model_.genSeedRPC().response().cipherSeedMnemonic;
                r.walletPassword = password_.getText().toString().getBytes();
//                r.aezeedPassphrase = aezeedPassword_.getText().toString().getBytes();
                return r;
            }
        });
        model_.initWalletRPC().setCallback(this, new IResponseCallback<WalletData.InitWalletResponse>() {
            @Override
            public void onResponse(WalletData.InitWalletResponse initWalletResponse) {
                Log.i(TAG, "wallet created");
                startCreateRoot();
            }

            @Override
            public void onError(String s, String s1) {
                Log.e(TAG, "Failed to call initWallet: "+s+" err "+s1);
                state_.setText("InitWallet error: "+s1);
            }
        });

        recoverUseCases();
    }

    private boolean checkPassword() {
        String password = password_.getText().toString();
        if (password.length() < 8) {
            state_.setText("Seriously, please 8 characters!");
            return false;
        }

        return true;
    }

    private void recoverUseCases() {
        if (model_.genSeedRPC().isExecuting())
            model_.genSeedRPC().recover();
        else if (model_.initWalletRPC().isExecuting())
            initWallet();
        else // gen immediately
            genSeed();
    }

    private void initWallet() {
        state_.setText("Creating wallet...");
        if (model_.initWalletRPC().isExecuting())
            model_.initWalletRPC().recover();
        else
            model_.initWalletRPC().execute();
    }

    private void genSeed() {
        seed_.setText("Generating seed...");
        if (model_.genSeedRPC().isExecuting())
            model_.genSeedRPC().recover();
        else
            model_.genSeedRPC().execute();
    }

    private void startCreateRoot() {
        Intent intent = new Intent(this, CreateRootActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateWalletPassword() {
        final int v = model_.genSeedRPC().response() != null
                ? View.VISIBLE
                : View.GONE;
        findViewById(R.id.title2).setVisibility(v);
        findViewById(R.id.walletPassword).setVisibility(v);
        findViewById(R.id.desc).setVisibility(v);
    }

}
