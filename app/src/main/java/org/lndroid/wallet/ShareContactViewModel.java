package org.lndroid.wallet;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;
import org.lndroid.framework.usecases.ActionAddContactInvoice;
import org.lndroid.framework.usecases.rpc.RPCAuthorize;

public class ShareContactViewModel extends ViewModel {

    private static final String TAG = "ShareContactViewModel";
    private IPluginClient pluginClient_;
    private IAuthClient authClient_;

    private ActionAddContactInvoice addContactInvoice_;
    private RPCAuthorize rpcAuthorize_;

    public ShareContactViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();
        authClient_ = new AuthClient(WalletServer.getInstance().server());

        // create use cases
        addContactInvoice_ = new ActionAddContactInvoice(pluginClient_);
        rpcAuthorize_ = new RPCAuthorize(authClient_);
    }

    @Override
    protected void onCleared() {
        addContactInvoice_.destroy();
    }

    ActionAddContactInvoice addContactInvoice() { return addContactInvoice_; }
    RPCAuthorize rpcAuthorize() { return rpcAuthorize_; }
}

