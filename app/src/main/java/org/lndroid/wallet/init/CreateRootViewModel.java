package org.lndroid.wallet.init;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;
import org.lndroid.framework.usecases.rpc.RPCCreateRoot;
import org.lndroid.wallet.WalletServer;

public class CreateRootViewModel extends ViewModel {

    private static final String TAG = "CreateRootViewModel";
    private IAuthClient authClient_;

    private RPCCreateRoot createRoot_;

    public CreateRootViewModel() {
        super();

        authClient_ = new AuthClient(WalletServer.getInstance().server());
        Log.i(TAG, "auth client "+authClient_);

        // create use cases
        createRoot_ = new RPCCreateRoot(authClient_);
    }

    RPCCreateRoot createRoot() { return createRoot_; }
}


