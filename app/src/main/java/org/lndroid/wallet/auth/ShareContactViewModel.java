package org.lndroid.wallet.auth;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;
import org.lndroid.framework.usecases.ActionAddContactInvoice;
import org.lndroid.framework.usecases.rpc.RPCAuthorize;
import org.lndroid.wallet.WalletServer;
import org.lndroid.wallet.WalletViewModelBase;

public class ShareContactViewModel extends WalletViewModelBase {

    private static final String TAG = "ShareContactViewModel";
    private IAuthClient authClient_;

    private ActionAddContactInvoice addContactInvoice_;
    private RPCAuthorize rpcAuthorize_;

    public ShareContactViewModel() {
        super(TAG);

        authClient_ = new AuthClient(WalletServer.getInstance().server());

        // create use cases
        addContactInvoice_ = new ActionAddContactInvoice(pluginClient());
        rpcAuthorize_ = new RPCAuthorize(authClient_);
    }

    @Override
    protected void onCleared() {
        addContactInvoice_.destroy();
        super.onCleared();
    }

    ActionAddContactInvoice addContactInvoice() { return addContactInvoice_; }
    RPCAuthorize rpcAuthorize() { return rpcAuthorize_; }
}

