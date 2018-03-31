package com.kimcy929.simplefileexplorelib.adapter;

import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kimcy929.simplefileexplorelib.R;

import java.io.File;
import java.util.List;

import timber.log.Timber;

public class SegmentAdapter extends RecyclerView.Adapter<SegmentAdapter.ViewHolder> {
    private List<String> segmentList;

    private OnItemClickListener onItemClickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.segment_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindHolder(segmentList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return segmentList != null ? segmentList.size() : 0;
    }

    @SuppressWarnings("unchecked")
    public void addPathSegments(List<String> newData) {
        segmentList = newData;
        notifyDataSetChanged();
        onItemClickListener.scrollToLastSegment();

        /*if(segmentList == null) {
           if (newData != null) {
               segmentList = newData;
               notifyDataSetChanged();
               onItemClickListener.scrollToLastSegment();
           }
        } else {
            if (newData == null) {
                segmentList.clear();
                notifyDataSetChanged();
            } else {
                diffTask = new DiffTask(this);
                diffTask.execute(newData);
            }
        }*/
    }

    @SuppressWarnings("WeakerAccess")
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtSegment;
        AppCompatImageView imageArrowIcon;

        public ViewHolder(View itemView) {
            super(itemView);

            txtSegment = itemView.findViewById(R.id.txtSegment);
            imageArrowIcon = itemView.findViewById(R.id.imageArrowIcon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int adapterPosition = getAdapterPosition();
                    // rebuild path
                    StringBuilder pathBuilder = new StringBuilder();
                    for (int i = 0; i <= adapterPosition; i++) {
                        pathBuilder.append(File.separator).append(segmentList.get(i));
                    }
                    onItemClickListener.pathSegmentClick(pathBuilder.toString());
                }

                /*private void removeRangeItem(int adapterPosition) {
                    int j = 0;
                    int size = segmentList.size();
                    for (int i = adapterPosition + 1; i < size; ++i) {
                        segmentList.remove(i - j);
                        ++j;
                    }
                    notifyItemRangeRemoved(adapterPosition + 1, size);
                }*/
            });
        }

        public void bindHolder(String segment, int position) {

            if (!TextUtils.equals(segment, File.separator)) {
                txtSegment.setText(segment);
            } else {
                txtSegment.setText(R.string.root_folder_name);
            }

            Timber.d("position -> %s, itemCount %s", position, getItemCount());

            if (position < getItemCount() - 1) {
                imageArrowIcon.setVisibility(View.VISIBLE);
                int color = ContextCompat.getColor(txtSegment.getContext(), android.R.color.secondary_text_light_nodisable);
                txtSegment.setTextColor(color);
                imageArrowIcon.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            } else {
                imageArrowIcon.setVisibility(View.GONE);
                int color = ContextCompat.getColor(txtSegment.getContext(), android.R.color.primary_text_light);
                txtSegment.setTextColor(color);
            }

        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void pathSegmentClick(String path);
        void scrollToLastSegment();
    }

    /*
    private DiffTask diffTask;

    public void cancelDiffTask() {
        if (diffTask != null && !diffTask.isCancelled()) {
            diffTask.cancel(true);
        }
    }

    private static class SegmentDiff extends DiffUtil.Callback {

        private List<String> oldSegment;
        private List<String> newSegment;

        @SuppressWarnings("WeakerAccess")
        public SegmentDiff(List<String> oldSegment, List<String> newSegment) {
            this.oldSegment = oldSegment;
            this.newSegment = newSegment;
        }

        @Override
        public int getOldListSize() {
            return oldSegment != null ? oldSegment.size() : 0;
        }

        @Override
        public int getNewListSize() {
            return newSegment != null ? newSegment.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return TextUtils.equals(oldSegment.get(oldItemPosition), newSegment.get(newItemPosition));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return false;
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            Timber.d("oldItemPosition %s, newItemPosition %s", oldItemPosition, newItemPosition);
            Timber.d("getOldSize %s, getNewSize %s", getOldListSize(), getNewListSize());
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }

    private static class DiffTask extends AsyncTask<List<String>, Void, DiffUtil.DiffResult> {
        private WeakReference<SegmentAdapter> segmentAdapterWeakReference;
        private List<String> newData;

        @SuppressWarnings("WeakerAccess")
        public DiffTask(SegmentAdapter segmentAdapter) {
            segmentAdapterWeakReference = new WeakReference<>(segmentAdapter);
        }

        @SafeVarargs
        @Override
        protected final DiffUtil.DiffResult doInBackground(List<String>... lists) {
            if (segmentAdapterWeakReference.get() != null) {
                newData = lists[0];
                return DiffUtil.calculateDiff(new SegmentDiff(segmentAdapterWeakReference.get().segmentList, newData), false);
            }
            return null;
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult) {
            super.onPostExecute(diffResult);
            SegmentAdapter segmentAdapter = segmentAdapterWeakReference.get();
            if (segmentAdapter != null && diffResult != null) {

                segmentAdapter.segmentList.clear();
                segmentAdapter.segmentList.addAll(newData);

                diffResult.dispatchUpdatesTo(segmentAdapter);

                segmentAdapter.onItemClickListener.scrollToLastSegment();
            }
        }
    }*/
}
