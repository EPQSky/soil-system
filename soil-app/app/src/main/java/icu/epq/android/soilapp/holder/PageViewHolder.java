package icu.epq.android.soilapp.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import icu.epq.android.soilapp.R;

/**
 * 布局适配器
 *
 * @author EPQ
 */
public class PageViewHolder extends RecyclerView.ViewHolder {

    private TextView titleView;
    private TextView contentView;
    private TextView timeView;

    public PageViewHolder(@NonNull View itemView) {
        super(itemView);
        titleView = itemView.findViewById(R.id.title_view);
        contentView = itemView.findViewById(R.id.content_view);
        timeView = itemView.findViewById(R.id.time_view);
    }

    public TextView getTitleView() {
        return titleView;
    }

    public TextView getContentView() {
        return contentView;
    }

    public TextView getTimeView() {
        return timeView;
    }
}
