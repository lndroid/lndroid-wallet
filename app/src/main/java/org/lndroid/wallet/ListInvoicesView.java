package org.lndroid.wallet;

import android.view.View;
import android.widget.TextView;

import org.lndroid.framework.WalletData;

class ListInvoicesView {

    public static class ViewHolder extends ListViewBase.ViewHolder<WalletData.Invoice> {
        private TextView id_;
        private TextView value_;
        private TextView paid_;

        public ViewHolder(View itemView) {
            super(itemView);

            id_ = itemView.findViewById(R.id.invoiceListId);
            value_ = itemView.findViewById(R.id.invoiceListValue);
            paid_ = itemView.findViewById(R.id.invoiceListPaid);
        }

        protected void fillData() {
            id_.setText(Long.toString(data().id()).toString());
            value_.setText(new Long(data().valueSat()).toString());
            paid_.setText(new Long(data().amountPaidMsat()).toString());
        }

        protected void clearData() {
            id_.setText("");
            value_.setText("");
            paid_.setText("");
        }
    }

    public static class Adapter extends ListViewBase.Adapter<WalletData.Invoice, ViewHolder> {

        protected Adapter() {
            super(R.layout.list_invoices, new ListViewBase.IViewHolderFactory<ViewHolder>() {
                @Override
                public ViewHolder create(View view) {
                    return new ViewHolder(view);
                }
            });
        }
    }

}