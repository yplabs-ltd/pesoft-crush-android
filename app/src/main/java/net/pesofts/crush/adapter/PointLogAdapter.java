package net.pesofts.crush.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.pesofts.crush.R;
import net.pesofts.crush.model.PointLog;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PointLogAdapter extends RecyclerView.Adapter {
    private Context context;
    private List<PointLog> pointLogList;

    public PointLogAdapter(Context context, List<PointLog> pointLogList) {
        this.context = context;
        this.pointLogList = pointLogList;
    }

    public class PointLogHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.date_text)
        TextView dateText;
        @Bind(R.id.desc_text)
        TextView descText;
        @Bind(R.id.point_diff_text)
        TextView pointDiffText;
        @Bind(R.id.adjust_point_text)
        TextView adjustPointText;

        public PointLogHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public PointLogHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_point_log, parent, false);
        PointLogHolder vh = new PointLogHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final PointLog pointLog = pointLogList.get(position);
        PointLogHolder pointLogHolder = (PointLogHolder) holder;

        pointLogHolder.dateText.setText(getDate(pointLog.getRegDttm()));
        pointLogHolder.descText.setText(pointLog.getDescription());
        pointLogHolder.pointDiffText.setText(pointLog.getPointdiff());
        pointLogHolder.adjustPointText.setText(pointLog.getAdjustpoint());
    }

    @Override
    public int getItemCount() {
        return pointLogList.size();
    }

    private String getDate(long timestamp) {
        String time = "";
        try {
            SimpleDateFormat df = new SimpleDateFormat("MM.dd");
            Timestamp stamp = new Timestamp(timestamp);
            Date date = new Date(stamp.getTime());
            time = df.format(date);
        } catch (Exception e) {
        }

        return time;
    }

}
