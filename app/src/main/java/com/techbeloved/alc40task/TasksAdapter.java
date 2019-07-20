package com.techbeloved.alc40task;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
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
                    && oldItem.getDetail() != null && oldItem.getDetail().equals(newItem.getDetail())
                    && oldItem.isCompleted() == newItem.isCompleted();
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

        // We use getItem to obtain the current item in list adapter
        Task task = getItem(position);

        // We want to display the title to correspond with the completion status
        if (task.isCompleted()) {
            SpannableStringBuilder builder = new SpannableStringBuilder(task.getTitle());
            builder.setSpan(new StrikethroughSpan(), 0, task.getTitle().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            holder.titleText.setText(builder);
        } else {
            holder.titleText.setText(task.getTitle());
        }

        if (!TextUtils.isEmpty(task.getDetail())) {
            holder.detailText.setText(task.getDetail());
        } else {
            holder.detailText.setVisibility(View.GONE);
        }

        holder.completedCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onCompleteToggle(task);
            }
        });

        if (task.isCompleted()) {
            holder.completedCheckBox.setImageResource(R.drawable.ic_check_black_24dp);
        } else {
            holder.completedCheckBox.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp);
        }


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
        ImageView completedCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.textview_task_title);
            detailText = itemView.findViewById(R.id.textview_task_detail_summary);
            completedCheckBox = itemView.findViewById(R.id.imageview_task_done);
        }
    }
}
