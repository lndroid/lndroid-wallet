package org.lndroid.wallet;

import android.view.View;
import android.widget.TextView;

import org.lndroid.framework.WalletData;

public class ListAppsView {

    public static class ViewHolder extends ListViewBase.ViewHolder<WalletData.User> {
        private TextView name_;
        private TextView packageName_;

        public ViewHolder(View itemView) {
            super(itemView);

            name_ = itemView.findViewById(R.id.name);
            packageName_ = itemView.findViewById(R.id.packageName);
        }

        @Override
        protected void fillData() {
            name_.setText(data().appLabel());
            packageName_.setText(data().appPackageName());
        }

        @Override
        protected void clearData() {
            name_.setText("");
            packageName_.setText("");
        }
    }

    public static class Adapter extends ListViewBase.Adapter<WalletData.User, ViewHolder> {

        protected Adapter() {
            super(R.layout.list_apps, new ListViewBase.IViewHolderFactory<ViewHolder>() {
                @Override
                public ViewHolder create(View view) {
                    return new ViewHolder(view);
                }
            });
        }
    }
}

