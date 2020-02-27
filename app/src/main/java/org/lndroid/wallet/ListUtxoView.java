package org.lndroid.wallet;

import android.view.View;
import android.widget.TextView;


import org.lndroid.framework.WalletData;

public class ListUtxoView {

    public static class ViewHolder extends ListViewBase.ViewHolder<WalletData.Utxo> {
        private TextView amount_;
        private TextView outpoint_;
        private TextView address_;
        private TextView confs_;

        public ViewHolder(View itemView) {
            super(itemView);

            amount_ = itemView.findViewById(R.id.amount);
            outpoint_ = itemView.findViewById(R.id.outpoint);
            address_ = itemView.findViewById(R.id.address);
            confs_ = itemView.findViewById(R.id.confs);
        }

        protected void fillData() {
            String hash = data().txidHex();
            if (hash.length() > 12)
                hash = hash.substring(0, 6) + "..." + hash.substring(hash.length() - 6);
            hash += data().outputIndex();
            outpoint_.setText(hash);

            String address = data().address();
            if (address.length() > 12)
                address = address.substring(0, 6) + "..." + address.substring(address.length() - 6);
            address_.setText(address);

            amount_.setText(Long.toString(data().amountSat()));
            confs_.setText("Confs: " + data().confirmations());
        }

        @Override
        protected void clearData() {
            outpoint_.setText("");
            address_.setText("");
            amount_.setText("");
            confs_.setText("");
        }
    }

    public static class Adapter extends ListViewBase.Adapter<WalletData.Utxo, ViewHolder> {

        public Adapter() {
            super(R.layout.list_utxo, new ListViewBase.IViewHolderFactory<ViewHolder>() {
                @Override
                public ViewHolder create(View view) {
                    return new ViewHolder(view);
                }
            });
        }
    }
}


