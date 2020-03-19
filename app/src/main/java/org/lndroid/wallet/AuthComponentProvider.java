package org.lndroid.wallet;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.defaults.DefaultPlugins;
import org.lndroid.framework.engine.IAuthComponentProvider;
import org.lndroid.wallet.auth.AddAppContactActivity;
import org.lndroid.wallet.auth.AddContactPaymentsPrivilegeActivity;
import org.lndroid.wallet.auth.AddListContactsPrivilegeActivity;
import org.lndroid.wallet.auth.ShareContactActivity;

public class AuthComponentProvider implements IAuthComponentProvider {

    @Override
    public void assignAuthComponent(WalletData.AuthRequest.Builder b) {
        if (b.pluginId().equals(DefaultPlugins.ADD_APP_CONTACT)) {
                b.setComponentPackageName(BuildConfig.APPLICATION_ID);
            b.setComponentClassName(AddAppContactActivity.class.getName());
        }
        if (b.pluginId().equals(DefaultPlugins.ADD_LIST_CONTACTS_PRIVILEGE)) {
            b.setComponentPackageName(BuildConfig.APPLICATION_ID);
            b.setComponentClassName(AddListContactsPrivilegeActivity.class.getName());
        }
        if (b.pluginId().equals(DefaultPlugins.ADD_CONTACT_PAYMENTS_PRIVILEGE)) {
            b.setComponentPackageName(BuildConfig.APPLICATION_ID);
            b.setComponentClassName(AddContactPaymentsPrivilegeActivity.class.getName());
        }
        if (b.pluginId().equals(DefaultPlugins.SHARE_CONTACT)) {
            b.setComponentPackageName(BuildConfig.APPLICATION_ID);
            b.setComponentClassName(ShareContactActivity.class.getName());
        }
    }
}
