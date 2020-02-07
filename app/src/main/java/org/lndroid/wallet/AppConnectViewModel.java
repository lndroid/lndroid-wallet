package org.lndroid.wallet;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.user.ActionAddUser;
import org.lndroid.framework.usecases.user.GetAppUser;

public class AppConnectViewModel extends ViewModel {
    private static final String TAG = "AppConnectViewModel";
    private IPluginClient pluginClient_;

    private ActionAddUser addUser_;
    private GetAppUser getAppUser_;

    public AppConnectViewModel() {
        super();
        pluginClient_ = WalletServer.buildPluginClient();

        // create use cases
        addUser_ = new ActionAddUser(pluginClient_);
        getAppUser_ = new GetAppUser(pluginClient_);
    }

    @Override
    protected void onCleared() {
        addUser_.destroy();
    }

    ActionAddUser addUser() { return addUser_; }
    GetAppUser getAppUser() { return getAppUser_; }
}
