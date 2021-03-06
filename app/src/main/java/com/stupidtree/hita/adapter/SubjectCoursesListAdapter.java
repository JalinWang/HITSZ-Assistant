package com.stupidtree.hita.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.timetable.EventItem;

import java.util.Calendar;
import java.util.List;

import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeTableCore;


public class SubjectCoursesListAdapter extends RecyclerView.Adapter<SubjectCoursesListAdapter.CoursesViewHolder> {

    List<EventItem> mBeans;
    LayoutInflater mInflater;
    Context mContext;
    OnItemClickListener mOnItemClickListener;

    public SubjectCoursesListAdapter(Context context, List<EventItem> list){
        mBeans = list;
        mInflater = LayoutInflater.from(context);
    }
    public interface OnItemClickListener{
        void OnClick(View v, int position, EventItem ei);
    }

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    @NonNull
    @Override
    public CoursesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = mInflater.inflate(R.layout.dynamic_subject_courseitem,viewGroup,false);
        return new CoursesViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CoursesViewHolder coursesViewHolder, final int i) {
        coursesViewHolder.week.setText(String.format(HContext.getString(R.string.week),mBeans.get(i).week)
                + HContext.getResources().getStringArray(R.array.dow2)[mBeans.get(i).DOW-1]);
        Calendar c = timeTableCore.getCurrentCurriculum().getDateAtWOT(mBeans.get(i).week,mBeans.get(i).DOW);
        coursesViewHolder.date.setText(
                HContext.getResources().getStringArray(R.array.months)[c.get(Calendar.MONTH)]
                        +" "+String.format(HContext.getString(R.string.date_day),c.get(Calendar.DAY_OF_MONTH)));
        if(mBeans.get(i).hasPassed(now)) coursesViewHolder.mark.setVisibility(View.VISIBLE);
        else coursesViewHolder.mark.setVisibility(View.INVISIBLE);
        if(mOnItemClickListener!=null){
            coursesViewHolder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.OnClick(v,i,mBeans.get(i));
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mBeans.size();
    }

    class CoursesViewHolder extends RecyclerView.ViewHolder{
        TextView week,date;
        ImageView mark;
        LinearLayout item;
        public CoursesViewHolder(@NonNull View itemView) {
            super(itemView);
            week = itemView.findViewById(R.id.subject_courselist_week);
            mark = itemView.findViewById(R.id.subject_courselist_mark);
            date = itemView.findViewById(R.id.subject_courselist_month);
            item = itemView.findViewById(R.id.subject_courseItem);
        }
    }
}
