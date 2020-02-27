package org.lndroid.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.lndroid.framework.WalletData;

public class ListFieldsView {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name_;
        private EditText value_;
        private ImageButton help_;
        private ImageButton copy_;
        private WalletData.Field field_;

        public ViewHolder(final View itemView) {
            super(itemView);

            name_ = itemView.findViewById(R.id.name);
            value_ = itemView.findViewById(R.id.value);
            help_ = itemView.findViewById(R.id.help);
            copy_ = itemView.findViewById(R.id.copy);

            help_.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), field_.help(), 10);
                }
            });

            copy_.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(
                            Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(field_.name(), field_.value());
                    clipboard.setPrimaryClip(clip);
                }
            });

        }

        public void bindTo(WalletData.Field f) {
            field_= f;
            name_.setText(f.name());
            value_.setText(f.value());
        }

        public void clear() {
            field_ = null;
            name_.setText("");
            value_.setText("");
        }
    }

    public static class Adapter extends PagedListAdapter<WalletData.Field, ViewHolder> {

        protected Adapter() {
            super(DIFF_CALLBACK);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View view = inflater.inflate(R.layout.list_fields, parent, false);

            // Return a new holder instance
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder,
                                     int position) {
            WalletData.Field field = getItem(position);

            if (field != null)
                holder.bindTo(field);
            else
                holder.clear();
        }

        private static DiffUtil.ItemCallback<WalletData.Field> DIFF_CALLBACK
                = new DiffUtil.ItemCallback<WalletData.Field>() {
            @Override
            public boolean areItemsTheSame(
                    @NonNull WalletData.Field a, @NonNull WalletData.Field b) {
                return a.id().equals(b.id());
            }

            @Override
            public boolean areContentsTheSame(
                    @NonNull WalletData.Field a, @NonNull WalletData.Field b) {

                return a.equals(b);
            }
        };
    }
}

