package org.lndroid.wallet;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;
import org.lndroid.framework.usecases.rpc.RPCUnlockWallet;

public class UnlockViewModel extends ViewModel {

    private static final String TAG = "UnlockViewModel";
    private IAuthClient authClient_;

    private RPCUnlockWallet unlockRPC_;

    public UnlockViewModel() {
        super();

        authClient_ = new AuthClient(WalletServer.getInstance().server());
        Log.i(TAG, "auth client "+authClient_);

        // create use cases
        unlockRPC_ = new RPCUnlockWallet(authClient_);
    }

    RPCUnlockWallet unlockRPC() { return unlockRPC_; }
}


