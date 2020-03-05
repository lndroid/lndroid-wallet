package org.lndroid.wallet.auth;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.common.Errors;
import org.lndroid.framework.common.IResponseCallback;
import org.lndroid.framework.defaults.DefaultPlugins;
import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;
import org.lndroid.framework.usecases.ActionDecodePayReq;
import org.lndroid.framework.usecases.rpc.RPCAuthorize;
import org.lndroid.wallet.WalletServer;
import org.lndroid.wallet.WalletViewModelBase;

public class AddAppContactViewModel extends WalletViewModelBase {

    private static final String TAG = "AddAppContactViewModel";
    private IAuthClient authClient_;

    private long authRequestId_;
    private WalletData.AuthRequest authRequest_;
    private WalletData.AddContactRequest request_;

    private MutableLiveData<Boolean> ready_ = new MutableLiveData<>();
    private MutableLiveData<WalletData.Error> error_ = new MutableLiveData<>();

    private boolean scanning_;
    private MutableLiveData<String> scanResult_ = new MutableLiveData<>();
    private ActionDecodePayReq decodePayReq_;
    private RPCAuthorize rpcAuthorize_;

    public AddAppContactViewModel() {
        super(TAG);

        authClient_ = new AuthClient(WalletServer.getInstance().server());

        // create use cases
        decodePayReq_ = new ActionDecodePayReq(pluginClient());
        rpcAuthorize_ = new RPCAuthorize(authClient_);
    }

    @Override
    protected void onCleared() {
        decodePayReq_.destroy();
        super.onCleared();
    }

    private void setError(String label,String code, String err) {
        Log.e(TAG, label+" error "+code+" e "+err);
        error_.setValue(WalletData.Error.create(code, err));
        ready_.setValue(false);
    }

    private void getRequest() {
        authClient_.getAuthTransactionRequest(authRequestId_, WalletData.AddContactRequest.class,
                new IResponseCallback<WalletData.AddContactRequest>() {
                    @Override
                    public void onResponse(WalletData.AddContactRequest r) {
                        request_ = r;
                        // build default one
                        if (request_ == null)
                            request_ = WalletData.AddContactRequest.builder().build();
                        ready_.setValue(true);
                    }

                    @Override
                    public void onError(String code, String e) {
                        setError("getTransactionRequest error", code, e);
                    }
                });
    }

    public void start(long authRequestId) {
        if (authRequestId_ == authRequestId)
            return;

        if (authRequestId_ != 0 && authRequestId != authRequestId_)
            throw new RuntimeException("View model already started");

        authRequestId_ = authRequestId;
        authClient_.getAuthRequest(authRequestId_, new IResponseCallback<WalletData.AuthRequest>() {
            @Override
            public void onResponse(WalletData.AuthRequest r) {
                authRequest_ = r;
                if (r == null) {
                    setError("getAuthRequest", Errors.AUTH_INPUT, "Unknown auth request");
                } else {
                    getRequest();
                }
            }

            @Override
            public void onError(String code, String e) {
                setError("getAuthRequest", code, e);
            }
        });
    }


    boolean isScanning() { return scanning_; }
    void startScan() { scanning_ = true; }
    void setScanResult(String data) { scanResult_.setValue(data); scanning_ = false; }
    LiveData<String> scanResult() { return scanResult_; }

    ActionDecodePayReq decodePayReq() { return decodePayReq_; }
    RPCAuthorize rpcAuthorize() { return rpcAuthorize_; }

    WalletData.AddContactRequest request() { return request_; }
    LiveData<Boolean> ready() { return ready_; }
    LiveData<WalletData.Error> error() { return error_; }
}
