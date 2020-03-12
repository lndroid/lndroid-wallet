package org.lndroid.wallet.init;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;
import org.lndroid.framework.usecases.rpc.RPCGenSeed;
import org.lndroid.framework.usecases.rpc.RPCInitWallet;
import org.lndroid.wallet.WalletServer;

public class GenSeedViewModel extends ViewModel {

    private static final String TAG = "GenSeedViewModel";
    private IAuthClient authClient_;

    private RPCGenSeed genSeedRPC_;
    private RPCInitWallet initWalletRPC_;

    public GenSeedViewModel() {
        super();

        authClient_ = new AuthClient(WalletServer.getInstance().server());
        Log.i(TAG, "auth client "+authClient_);

        // create use cases
        initWalletRPC_ = new RPCInitWallet(authClient_);
        genSeedRPC_ = new RPCGenSeed(authClient_);
    }

    RPCInitWallet initWalletRPC() { return initWalletRPC_; }

    RPCGenSeed genSeedRPC() { return genSeedRPC_; }
}

