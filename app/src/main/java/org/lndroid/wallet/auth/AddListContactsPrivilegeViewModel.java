package org.lndroid.wallet.auth;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;
import org.lndroid.framework.usecases.rpc.RPCAuthorize;
import org.lndroid.framework.usecases.user.GetAuthRequestUser;
import org.lndroid.wallet.WalletServer;
import org.lndroid.wallet.WalletViewModelBase;

public class AddListContactsPrivilegeViewModel extends WalletViewModelBase {

    private static final String TAG = "AddListContactsPrivVM";
    private IAuthClient authClient_;

    private GetAuthRequestUser getAuthRequestUser_;
    private RPCAuthorize rpcAuthorize_;

    public AddListContactsPrivilegeViewModel() {
        super(TAG);

        authClient_ = new AuthClient(WalletServer.getInstance().server());

        // create use cases
        getAuthRequestUser_ = new GetAuthRequestUser(pluginClient());
        rpcAuthorize_ = new RPCAuthorize(authClient_);
    }

    @Override
    protected void onCleared() {
        getAuthRequestUser_.destroy();
        super.onCleared();
    }

    GetAuthRequestUser getAuthRequestUser() { return getAuthRequestUser_; }
    RPCAuthorize rpcAuthorize() { return rpcAuthorize_; }

}
