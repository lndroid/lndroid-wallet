package org.lndroid.wallet;

import org.lndroid.framework.WalletData;
import org.lndroid.framework.common.DefaultPlugins;
import org.lndroid.framework.engine.IAuthComponentProvider;

public class AuthComponentProvider implements IAuthComponentProvider {

    @Override
    public void assignAuthComponent(WalletData.AuthRequest.Builder b) {
        if (b.pluginId().equals(DefaultPlugins.ADD_CONTACT_APP)) {
            b.setComponentPackageName(AddAppContactActivity.class.getPackage().getName());
            b.setComponentClassName(AddAppContactActivity.class.getName());
        }
        if (b.pluginId().equals(DefaultPlugins.ADD_LIST_CONTACTS_PRIVILEGE)) {
            b.setComponentPackageName(AddListContactsPrivilegeActivity.class.getPackage().getName());
            b.setComponentClassName(AddListContactsPrivilegeActivity.class.getName());
        }
        if (b.pluginId().equals(DefaultPlugins.ADD_CONTACT_PAYMENTS_PRIVILEGE)) {
            b.setComponentPackageName(AddContactPaymentsPrivilegeActivity.class.getPackage().getName());
            b.setComponentClassName(AddContactPaymentsPrivilegeActivity.class.getName());
        }
        if (b.pluginId().equals(DefaultPlugins.SHARE_CONTACT)) {
            b.setComponentPackageName(ShareContactActivity.class.getPackage().getName());
            b.setComponentClassName(ShareContactActivity.class.getName());
        }
    }
}
