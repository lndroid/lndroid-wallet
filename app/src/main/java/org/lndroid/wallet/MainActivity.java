package org.lndroid.wallet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.Errors;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.usecases.ActionAddInvoice;
import org.lndroid.framework.usecases.JobOpenChannel;
import org.lndroid.framework.usecases.rpc.RPCGenSeed;
import org.lndroid.framework.usecases.IRequestFactory;
import org.lndroid.framework.usecases.rpc.RPCInitWallet;
import org.lndroid.framework.usecases.rpc.RPCUnlockWallet;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "LnApiMain";

    private TextView lightningBalance_;
    private TextView blockchainBalance_;
    private TextView state_;
    private MainViewModel model_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "main thread "+Thread.currentThread().getId());

        model_ = ViewModelProviders.of(this).get(MainViewModel.class);

        setContentView(R.layout.activity_main);

        lightningBalance_ = findViewById(R.id.lightningBalance);
        blockchainBalance_ = findViewById(R.id.blockchainBalance);
        state_ = findViewById(R.id.state);

        Button button = findViewById(R.id.unlock);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                unlockWallet();
            }
        });
        button = findViewById(R.id.initWallet);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                initWallet();
            }
        });
        button = findViewById(R.id.newAddress);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startNewAddress();
            }
        });
        button = findViewById(R.id.addInvoice);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAddInvoice();
            }
        });
        button = (Button)findViewById(R.id.sendPayment);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startSendPayment();
            }
        });

        button = (Button)findViewById(R.id.listPayments);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startListPayments();
            }
        });

        button = (Button)findViewById(R.id.connectPeer);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startConnectPeer();
            }
        });

        button = (Button)findViewById(R.id.openChannel);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startOpenChannel();
            }
        });

        // watch state changes
        model_.walletState().observe(this, new Observer<WalletData.WalletState>() {
            @Override
            public void onChanged(WalletData.WalletState state) {
                updateState();
            }
        });

        // watch balance changes
        model_.walletBalance().data().observe(this, new Observer<WalletData.WalletBalance>() {
            @Override
            public void onChanged(WalletData.WalletBalance walletBalance) {
                if (walletBalance != null)
                    blockchainBalance_.setText("Blockchain: "+walletBalance.totalBalance());
            }
        });

        model_.channelBalance().data().observe(this, new Observer<WalletData.ChannelBalance>() {
            @Override
            public void onChanged(WalletData.ChannelBalance balance) {
                if (balance != null)
                    lightningBalance_.setText("Lightning: "+balance.balance());
            }
        });

        model_.walletInfo().data().observe(this, new Observer<WalletData.WalletInfo>() {
            @Override
            public void onChanged(WalletData.WalletInfo info) {
                updateState();
            }
        });

        // catch auth errors to show auth UI
        model_.clientError().observe(this, new Observer<WalletData.Error>() {
            @Override
            public void onChanged(WalletData.Error error) {
                if (error != null && Errors.MESSAGE_AUTH.equals(error.code())) {
                    model_.getSessionToken(MainActivity.this);
                }
            }
        });

        // unlock use case
        RPCUnlockWallet walletUnlock = model_.unlockWalletRPC();
        walletUnlock.setCallback(this, new IResponseCallback<WalletData.UnlockWalletResponse>() {
            @Override
            public void onResponse(WalletData.UnlockWalletResponse r) {
                Log.i(TAG, "unlock wallet ok");
                state_.setText("Unlocked");
            }

            @Override
            public void onError(String code, String e) {
                Log.e(TAG, "unlock wallet error "+code+" msg "+e);
                state_.setText("Unlock error");
            }
        });
        walletUnlock.setRequestFactory(this, new IRequestFactory<WalletData.UnlockWalletRequest>() {
            @Override
            public WalletData.UnlockWalletRequest create() {
                WalletData.UnlockWalletRequest r = new WalletData.UnlockWalletRequest();
                // FIXME read from input fields
                r.walletPassword = "12345678".getBytes();
                return r;
            }
        });

        // init wallet use case
        RPCInitWallet initWallet = model_.initWalletRPC();
        initWallet.setCallback(this, new IResponseCallback<WalletData.InitWalletResponse>() {
            @Override
            public void onResponse(WalletData.InitWalletResponse r) {
                Log.i(TAG, "init wallet ok");
                state_.setText("Inited");
            }

            @Override
            public void onError(String code, String e) {
                Log.e(TAG, "init wallet error "+code+" msg "+e);
                state_.setText("init wallet error");
            }
        });
        initWallet.setRequestFactory(this, new IRequestFactory<WalletData.InitWalletRequest>() {
            @Override
            public WalletData.InitWalletRequest create() {
                WalletData.InitWalletRequest r = new WalletData.InitWalletRequest();
                r.cipherSeedMnemonic = model_.genSeedRPC().response().cipherSeedMnemonic;
                r.walletPassword = "12345678".getBytes();
                return r;
            }
        });

        // init wallet use case
        RPCGenSeed genSeed = model_.genSeedRPC();
        genSeed.setCallback(this, new IResponseCallback<WalletData.GenSeedResponse>() {
            @Override
            public void onResponse(WalletData.GenSeedResponse r) {
                Log.i(TAG, "gen seed ok");
                state_.setText("Seed: "+r.cipherSeedMnemonic);
                model_.initWalletRPC().execute();
            }

            @Override
            public void onError(String code, String e) {
                Log.e(TAG, "gen seed error "+code+" msg "+e);
                state_.setText("gen seed error");
            }
        });
        genSeed.setRequestFactory(this, new IRequestFactory<WalletData.GenSeedRequest>() {
            @Override
            public WalletData.GenSeedRequest create() {
                WalletData.GenSeedRequest r = new WalletData.GenSeedRequest();
                return r;
            }
        });

        recoverUseCases();
    }

    private void recoverUseCases() {
        if (model_.unlockWalletRPC().isExecuting())
            unlockWallet();
        if (model_.initWalletRPC().isExecuting() || model_.genSeedRPC().isExecuting())
            initWallet();
    }

    private void updateState() {

        WalletData.WalletState st = model_.walletState().getValue();
        WalletData.WalletInfo info = model_.walletInfo().data().getValue();

        if (st != null)
            Log.i(TAG, "Wallet state "+st.state());
        if (info != null)
            Log.i(TAG, "Wallet info "+info.blockHeight());

        String state = "";
        if (st == null) {
            state = "Starting. ";
        } else {
            switch (st.state()) {
                case WalletData.WALLET_STATE_AUTH:
                    // show auth UI, ask for password
                    // authClient_.setWalletPassword();
                    state = "Locked. ";
                    break;

                case WalletData.WALLET_STATE_INIT:
                    // call genSeed, ask for password, send to server
                    state = "No wallet. ";
                    break;

                case WalletData.WALLET_STATE_ERROR:
                    state = "Wallet state error " + st.code() + " msg " + st.message();
                    break;

                case WalletData.WALLET_STATE_OK:
                    break;

                default:
                    throw new RuntimeException("Unknown wallet state");
            }
        }

        if (!model_.haveSessionToken() && st != null && st.state() == WalletData.WALLET_STATE_OK) {
            model_.getSessionToken(MainActivity.this);
        }

        if (info != null) {
            if (info.syncedToChain() && info.syncedToGraph())
                state += "Block: " + info.blockHeight() + ", synched.";
            else if (info.syncedToChain() && !info.syncedToGraph())
                state += "Block: " + info.blockHeight() + ", synching graph...";
            else if (!info.syncedToChain() && info.syncedToGraph())
                state += "Block: " + info.blockHeight() + ", synching blockchain...";
            else
                state += "Block: " + info.blockHeight() + ", synching...";
        }

        state_.setText(state);
    }

    private void unlockWallet() {
        state_.setText("Unlocking");
        if (model_.unlockWalletRPC().isExecuting())
            model_.unlockWalletRPC().recover();
        else
            model_.unlockWalletRPC().execute();
    }

    private void initWallet() {
        state_.setText("Initing wallet");
        if (model_.initWalletRPC().isExecuting())
            model_.initWalletRPC().recover();
        else if (model_.genSeedRPC().isExecuting())
            model_.genSeedRPC().recover();
        else
            model_.genSeedRPC().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void startAddInvoice() {
        Intent intent = new Intent(this, AddInvoiceActivity.class);
        startActivity(intent);
    }

    private void startSendPayment() {
        Intent intent = new Intent(this, SendPaymentActivity.class);
        startActivity(intent);
    }

    private void startListPayments() {
        Intent intent = new Intent(this, ListPaymentsActivity.class);
        startActivity(intent);
    }

    private void startConnectPeer() {
        Intent intent = new Intent(this, ConnectPeerActivity.class);
        startActivity(intent);
    }

    private void startNewAddress() {
        Intent intent = new Intent(this, NewAddressActivity.class);
        startActivity(intent);
    }

    private void startOpenChannel() {
        Intent intent = new Intent(this, OpenChannelActivity.class);
        startActivity(intent);
    }
}
