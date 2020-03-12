package org.lndroid.wallet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.lndroid.framework.WalletDataDecl;

public class ListViewBase {

    public static abstract class ViewHolder<Entity extends WalletDataDecl.EntityBase>
            extends RecyclerView.ViewHolder {

        private Entity data_;

        protected abstract void fillData();
        protected abstract void clearData();

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void bindTo(Entity e) {
            data_ = e;
            fillData();
        }
        public void clear() {
            data_ = null;
            clearData();
        }
        public Entity data() { return data_; }
    }

    public interface IViewHolderFactory<ViewHolder> {
        ViewHolder create(View view);
    }

    public static class Adapter<Entity extends WalletDataDecl.EntityBase, VH extends ListViewBase.ViewHolder<Entity>>
            extends PagedListAdapter<Entity, VH> {

        private int layoutResource_;
        private IViewHolderFactory<VH> factory_;

        private class EmptyView extends RecyclerView.AdapterDataObserver {
            private View emptyView_;

            public EmptyView(View ev) {
                emptyView_ = ev;
                checkIfEmpty();
            }

            private void checkIfEmpty() {
                boolean emptyViewVisible = Adapter.this.getItemCount() == 0;
                emptyView_.setVisibility(emptyViewVisible ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onChanged() {
                checkIfEmpty();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                checkIfEmpty();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                checkIfEmpty();
            }
        }

        private View.OnClickListener itemClickListener_;

        public Adapter(int layoutResource, IViewHolderFactory<VH> factory) {
            super(new DiffUtil.ItemCallback<Entity>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull Entity a, @NonNull Entity b) {
                    return a.id() == b.id();
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Entity a, @NonNull Entity b) {

                    return a.equals(b);
                }
            });

            layoutResource_ = layoutResource;
            factory_ = factory;
        }

        public void setEmptyView(View view) {
            registerAdapterDataObserver(new EmptyView(view));
        }

        public void setItemClickListener (View.OnClickListener cl) {
            itemClickListener_ = cl;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View view = inflater.inflate(layoutResource_, parent, false);
            if (itemClickListener_ != null)
                view.setOnClickListener(itemClickListener_);

            // Return a new holder instance
            return factory_.create(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Entity e = getItem(position);

            if (e != null)
                holder.bindTo(e);
            else
                holder.clear();
        }
    }
}
