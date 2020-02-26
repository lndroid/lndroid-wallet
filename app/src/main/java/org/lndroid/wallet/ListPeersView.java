package org.lndroid.wallet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.lndroid.framework.WalletData;

public class ListPeersView {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView pubkey_;
        private TextView address_;
        private TextView state_;
        private WalletData.Peer peer_;

        public ViewHolder(View itemView) {
            super(itemView);

            pubkey_ = itemView.findViewById(R.id.pubkey);
            address_ = itemView.findViewById(R.id.address);
            state_ = itemView.findViewById(R.id.state);
        }

        public void bindTo(WalletData.Peer p) {
            peer_= p;
            String pubkey = p.pubkey();
            if (pubkey.length() > 12)
                pubkey = pubkey.substring(0, 6) + "..." + pubkey.substring(pubkey.length() - 6);
            pubkey_.setText(pubkey);
            address_.setText(p.address());
            if (p.online())
                state_.setText("online");
            else if (p.disabled())
                state_.setText("disabled");
            else
                state_.setText("offline");
        }

        public WalletData.Peer peer() {
            return peer_;
        }

        public void clear() {
            peer_ = null;
            pubkey_.setText("");
            address_.setText("");
            state_.setText("");
        }
    }

    public static class Adapter extends PagedListAdapter<WalletData.Peer, ViewHolder> {

        protected Adapter() {
            super(DIFF_CALLBACK);
        }

        private View.OnClickListener itemClickListener_;

        public void setItemClickListener (View.OnClickListener cl) {
            itemClickListener_ = cl;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View view = inflater.inflate(R.layout.list_peers, parent, false);
            if (itemClickListener_ != null)
                view.setOnClickListener(itemClickListener_);

            // Return a new holder instance
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder,
                                     int position) {
            WalletData.Peer peer = getItem(position);

            if (peer != null)
                holder.bindTo(peer);
            else
                holder.clear();
        }

        private static DiffUtil.ItemCallback<WalletData.Peer> DIFF_CALLBACK
                = new DiffUtil.ItemCallback<WalletData.Peer>() {
            @Override
            public boolean areItemsTheSame(
                    @NonNull WalletData.Peer a, @NonNull WalletData.Peer b) {
                return a.id() == b.id();
            }

            @Override
            public boolean areContentsTheSame(
                    @NonNull WalletData.Peer a, @NonNull WalletData.Peer b) {

                return a.equals(b);
            }
        };
    }
}
