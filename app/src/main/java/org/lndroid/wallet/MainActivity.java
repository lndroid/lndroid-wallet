package org.lndroid.wallet;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.Errors;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.usecases.rpc.RPCGenSeed;
import org.lndroid.framework.usecases.IRequestFactory;
import org.lndroid.framework.usecases.rpc.RPCInitWallet;
import org.lndroid.framework.usecases.rpc.RPCUnlockWallet;

public class MainActivity extends AppCompatActivity {

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
        model_.ensureSessionToken(this);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lightningBalance_ = findViewById(R.id.lightningBalance);
        blockchainBalance_ = findViewById(R.id.blockchainBalance);
        state_ = findViewById(R.id.state);

        Button button = findViewById(R.id.sendPayment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSendPayment();
            }
        });
        button = findViewById(R.id.addInvoice);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAddInvoice();
            }
        });
        button = findViewById(R.id.sendCoins);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSendCoins();
            }
        });
        button = findViewById(R.id.newAddress);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewAddress();
            }
        });

        // watch balance changes
        model_.walletBalance().data().observe(this, new Observer<WalletData.WalletBalance>() {
            @Override
            public void onChanged(WalletData.WalletBalance walletBalance) {
                if (walletBalance != null)
                    blockchainBalance_.setText("Blockchain: "+walletBalance.totalBalance()+" sat");
            }
        });

        model_.channelBalance().data().observe(this, new Observer<WalletData.ChannelBalance>() {
            @Override
            public void onChanged(WalletData.ChannelBalance balance) {
                if (balance != null)
                    lightningBalance_.setText("Lightning: "+balance.balance()+" sat");
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

    }

    private void recoverUseCases() {
    }

    private void updateState() {

        WalletData.WalletInfo info = model_.walletInfo().data().getValue();
        String state = "Starting...";
        if (info != null) {
            state = "";
            Log.i(TAG, "Wallet info " + info.blockHeight());
            if (info.syncedToChain() && info.syncedToGraph())
                state += "Block: " + info.blockHeight() + ", synched.";
            else if (info.syncedToChain() && !info.syncedToGraph())
                state += "Block: " + info.blockHeight() + ", synching graph...";
            else if (!info.syncedToChain() && info.syncedToGraph())
                state += "Block: " + info.blockHeight() + ", synching blockchain...";
            else
                state += "Block: " + info.blockHeight() + ", synching...";

            state += "\nChannels:";
            if ((info.numActiveChannels() + info.numInactiveChannels() + info.numPendingChannels()) == 0)
                state += " none";
            else if (info.numActiveChannels() > 0)
                state += " active " + info.numActiveChannels();
            else if (info.numInactiveChannels() > 0)
                state += " inactive " + info.numInactiveChannels();
            else if (info.numPendingChannels() > 0)
                state += " active " + info.numPendingChannels();
            state += ". Peers: " + info.numPeers();
        }
        state_.setText(state);
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

    private void startSendCoins() {
        Intent intent = new Intent(this, SendCoinsActivity.class);
        startActivity(intent);
    }

    private void startNewAddress() {
        Intent intent = new Intent(this, NewAddressActivity.class);
        startActivity(intent);
    }

    private void startListPayments() {
        Intent intent = new Intent(this, ListPaymentsActivity.class);
        startActivity(intent);
    }

    private void startListPeers() {
        Intent intent = new Intent(this, ListPeersActivity.class);
        startActivity(intent);
    }

    private void startListTransactions() {
        Intent intent = new Intent(this, ListTransactionsActivity.class);
        startActivity(intent);
    }

    private void startListApps() {
        Intent intent = new Intent(this, ListAppsActivity.class);
        startActivity(intent);
    }

    private void startListContacts() {
        Intent intent = new Intent(this, ListContactsActivity.class);
        startActivity(intent);
    }

    private void startListUtxo() {
        Intent intent = new Intent(this, ListUtxoActivity.class);
        startActivity(intent);
    }

    private void startListChannels() {
        Intent intent = new Intent(this, ListChannelsActivity.class);
        startActivity(intent);
    }

    private void startListInvoices() {
        Intent intent = new Intent(this, ListInvoicesActivity.class);
        startActivity(intent);
    }

    private void startGetWalletInfo() {
        Intent intent = new Intent(this, GetWalletInfoActivity.class);
        startActivity(intent);
    }

    private void startAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuWalletInfo:
                startGetWalletInfo();
                return true;
            case R.id.menuApps:
                startListApps();
                return true;
            case R.id.menuContacts:
                startListContacts();
                return true;
            case R.id.menuPayments:
                startListPayments();
                return true;
            case R.id.menuInvoices:
                startListInvoices();
                return true;
            case R.id.menuChannels:
                startListChannels();
                return true;
            case R.id.menuPeers:
                startListPeers();
                return true;
            case R.id.menuTransactions:
                startListTransactions();
                return true;
            case R.id.menuUtxo:
                startListUtxo();
                return true;
            case R.id.menuAbout:
                startAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
