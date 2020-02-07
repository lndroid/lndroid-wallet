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

class InvoiceListView {

    public static class InvoiceListViewHolder extends RecyclerView.ViewHolder {
        private TextView id_;
        private TextView value_;
        private TextView paid_;

        public InvoiceListViewHolder(View itemView) {
            super(itemView);

            id_ = itemView.findViewById(R.id.invoiceListId);
            value_ = itemView.findViewById(R.id.invoiceListValue);
            paid_ = itemView.findViewById(R.id.invoiceListPaid);
        }

        public void bindTo(WalletData.Invoice i) {
            id_.setText(Long.toString(i.id()).toString());
            value_.setText(new Long(i.valueSat()).toString());
            paid_.setText(new Long(i.amountPaidMsat()).toString());
        }

        public void clear() {
            id_.setText("");
            value_.setText("");
            paid_.setText("");
        }
    }

    public static class InvoiceListAdapter extends PagedListAdapter<WalletData.Invoice, InvoiceListViewHolder> {

        protected InvoiceListAdapter() {
            super(DIFF_CALLBACK);
        }

        @NonNull
        @Override
        public InvoiceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View view = inflater.inflate(R.layout.list_invoices, parent, false);

            // Return a new holder instance
            return new InvoiceListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InvoiceListViewHolder holder,
                                     int position) {
            WalletData.Invoice invoice = getItem(position);

            if (invoice != null)
                holder.bindTo(invoice);
            else
                holder.clear();
        }

        private static DiffUtil.ItemCallback<WalletData.Invoice> DIFF_CALLBACK
            = new DiffUtil.ItemCallback<WalletData.Invoice>() {
            @Override
            public boolean areItemsTheSame(
                    @NonNull WalletData.Invoice a, @NonNull WalletData.Invoice b) {
                return a.id() == b.id();
            }

            @Override
            public boolean areContentsTheSame(
                    @NonNull WalletData.Invoice a, @NonNull WalletData.Invoice b) {
                return a.equals(b);
/*
                // NOTE: your apps should define their own equality criteria
                // based upon the fields you display, so that if any field changes
                // this would return false. Plus consider immutable fields
                // as those never need to be compared. In our case we compare
                // these mutable fields
                return a.settleIndex == b.settleIndex
                        && a.amountPaidMsat == b.amountPaidMsat
                        && a.authUserId == b.authUserId
                        && a.settleTime == b.settleTime
                        && a.htlcsCount == b.htlcsCount
                        && a.state == b.state;

 */
            }
        };
    }

}