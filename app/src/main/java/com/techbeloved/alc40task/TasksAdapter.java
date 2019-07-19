package com.techbeloved.alc40task;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class TasksAdapter extends ListAdapter<Task, TasksAdapter.ViewHolder> {

    private static final DiffUtil.ItemCallback<Task> TASK_ITEM_DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getTitle() != null && oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getDetail() != null && oldItem.getDetail().equals(newItem.getDetail());
        }
    };

    private final ClickListener<Task> clickListener;

    TasksAdapter(ClickListener<Task> clickListener) {
        super(TASK_ITEM_DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_task, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Task task = getItem(position);
        holder.titleText.setText(task.getTitle());
        if (!TextUtils.isEmpty(task.getDetail())) {
            holder.detailText.setText(task.getDetail());
        } else {
            holder.detailText.setVisibility(View.GONE);
        }
        holder.completedCheckBox.setChecked(task.isCompleted());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onClick(task);
            }
        });
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleText;
        TextView detailText;
        CheckBox completedCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.textview_task_title);
            detailText = itemView.findViewById(R.id.textview_task_detail_summary);
            completedCheckBox = itemView.findViewById(R.id.checkbox_task_done);
        }
    }
}
