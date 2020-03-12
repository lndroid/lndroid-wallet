package org.lndroid.wallet;

import androidx.lifecycle.ViewModel;

import org.lndroid.framework.client.IPluginClient;
import org.lndroid.framework.usecases.user.ActionAddUser;
import org.lndroid.framework.usecases.user.GetAppUser;

public class AppConnectViewModel extends WalletViewModelBase {
    private static final String TAG = "AppConnectViewModel";

    private ActionAddUser addUser_;
    private GetAppUser getAppUser_;

    public AppConnectViewModel() {
        super(TAG);

        // create use cases
        addUser_ = new ActionAddUser(pluginClient());
        getAppUser_ = new GetAppUser(pluginClient());
    }

    @Override
    protected void onCleared() {
        getAppUser_.destroy();
        addUser_.destroy();
        super.onCleared();
    }

    ActionAddUser addUser() { return addUser_; }
    GetAppUser getAppUser() { return getAppUser_; }
}
