package org.lndroid.wallet;

import android.view.View;
import android.widget.TextView;

import org.lndroid.framework.WalletData;

public class ListPeersView {

    public static class ViewHolder extends ListViewBase.ViewHolder<WalletData.Peer> {
        private TextView pubkey_;
        private TextView address_;
        private TextView state_;

        public ViewHolder(View itemView) {
            super(itemView);

            pubkey_ = itemView.findViewById(R.id.pubkey);
            address_ = itemView.findViewById(R.id.address);
            state_ = itemView.findViewById(R.id.state);
        }

        @Override
        protected void fillData() {
            String pubkey = "";
            if (data().pubkey() != null)
                pubkey = data().pubkey();
            if (pubkey.length() > 12)
                pubkey = pubkey.substring(0, 6) + "..." + pubkey.substring(pubkey.length() - 6);
            pubkey_.setText(pubkey);
            address_.setText(data().address());
            if (data().online())
                state_.setText("online");
            else if (data().disabled())
                state_.setText("disabled");
            else
                state_.setText("offline");
        }

        @Override
        protected void clearData() {
            pubkey_.setText("");
            address_.setText("");
            state_.setText("");
        }
    }

    public static class Adapter extends ListViewBase.Adapter<WalletData.Peer, ViewHolder> {

        protected Adapter() {
            super(R.layout.list_peers, new ListViewBase.IViewHolderFactory<ViewHolder>() {
                @Override
                public ViewHolder create(View view) {
                    return new ViewHolder(view);
                }
            });
        }
    }
}
