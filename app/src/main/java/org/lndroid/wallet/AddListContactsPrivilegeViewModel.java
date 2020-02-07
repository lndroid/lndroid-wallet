package org.lndroid.wallet;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.engine.AuthClient;
import org.lndroid.framework.engine.IAuthClient;
import org.lndroid.framework.usecases.rpc.RPCAuthorize;
import org.lndroid.framework.usecases.user.GetAuthRequestUser;

public class AddListContactsPrivilegeViewModel extends ViewModel {

    private static final String TAG = "AddListContactsPrivVM";
    private IPluginClient pluginClient_;
    private IAuthClient authClient_;

    private GetAuthRequestUser getAuthRequestUser_;
    private RPCAuthorize rpcAuthorize_;

    public AddListContactsPrivilegeViewModel() {
        super();

        pluginClient_ = WalletServer.buildPluginClient();
        authClient_ = new AuthClient(WalletServer.getInstance().server());

        // create use cases
        getAuthRequestUser_ = new GetAuthRequestUser(pluginClient_);
        rpcAuthorize_ = new RPCAuthorize(authClient_);
    }

    @Override
    protected void onCleared() {
        getAuthRequestUser_.destroy();
    }

    GetAuthRequestUser getAuthRequestUser() { return getAuthRequestUser_; }
    RPCAuthorize rpcAuthorize() { return rpcAuthorize_; }

}
