package org.lndroid.wallet;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;
import org.lndroid.framework.usecases.ActionDecodePayReq;
import org.lndroid.framework.usecases.rpc.RPCAuthorize;

public class AddAppContactViewModel extends ViewModel {

    private static final String TAG = "AddAppContactViewModel";
    private IPluginClient pluginClient_;
    private IAuthClient authClient_;

    private boolean scanning_;
    private MutableLiveData<String> scanResult_ = new MutableLiveData<>();
    private ActionDecodePayReq decodePayReq_;
    private RPCAuthorize rpcAuthorize_;

    public AddAppContactViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();
        authClient_ = new AuthClient(WalletServer.getInstance().server());

        // create use cases
        decodePayReq_ = new ActionDecodePayReq(pluginClient_);
        rpcAuthorize_ = new RPCAuthorize(authClient_);
    }

    @Override
    protected void onCleared() {
        decodePayReq_.destroy();
    }

    boolean isScanning() { return scanning_; }
    void startScan() { scanning_ = true; }
    void setScanResult(String data) { scanResult_.setValue(data); scanning_ = false; }
    LiveData<String> scanResult() { return scanResult_; }

    ActionDecodePayReq decodePayReq() { return decodePayReq_; }
    RPCAuthorize rpcAuthorize() { return rpcAuthorize_; }
}
