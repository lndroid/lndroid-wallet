package org.lndroid.wallet;

import android.view.View;
import android.widget.TextView;

import org.lndroid.framework.WalletData;

public class ListContactsView {

    public static class ViewHolder extends ListViewBase.ViewHolder<WalletData.Contact> {
        private TextView name_;
        private TextView pubkey_;

        public ViewHolder(View itemView) {
            super(itemView);

            name_ = itemView.findViewById(R.id.name);
            pubkey_ = itemView.findViewById(R.id.pubkey);
        }

        @Override
        protected void fillData() {
            String pubkey = data().pubkey();
            if (pubkey.length() > 12)
                pubkey = pubkey.substring(0, 6) + "..." + pubkey.substring(pubkey.length() - 6);

            pubkey_.setText(pubkey);
            name_.setText(data().name());
        }

        @Override
        protected void clearData() {
            pubkey_.setText("");
            name_.setText("");
        }
    }

    public static class Adapter extends ListViewBase.Adapter<WalletData.Contact, ViewHolder> {

        public Adapter() {
            super(R.layout.list_contacts, new ListViewBase.IViewHolderFactory<ViewHolder>() {
                @Override
                public ViewHolder create(View view) {
                    return new ViewHolder(view);
                }
            });
        }
    }
}


