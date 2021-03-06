package com.stupidtree.hita.fragments.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import com.github.lzyzsd.circleprogress.ArcProgress;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.BaseFragment;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.activities.ActivityUserCenter;
import com.stupidtree.hita.diy.CourseDialog;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.fragments.popup.FragmentAddEvent;
import com.stupidtree.hita.R;
import com.stupidtree.hita.activities.ActivityLeaderBoard;
import com.stupidtree.hita.activities.ActivityLogin;
import com.stupidtree.hita.activities.ActivityLoginJWTS;
import com.stupidtree.hita.activities.ActivityMain;
import com.stupidtree.hita.activities.ActivityTimeTable;
import com.stupidtree.hita.adapter.TimeLineWholedayAdapter;
import com.stupidtree.hita.timetable.TimetableCore;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.HTime;
import com.stupidtree.hita.diy.MaterialCircleAnimator;
import com.stupidtree.hita.adapter.TimelineListAdapter;
import com.stupidtree.hita.util.ActivityUtils;
import com.stupidtree.hita.util.RefreshBroadcastReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import tyrantgit.explosionfield.ExplosionField;

import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.HContext;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.adapter.NewsIpNewsListAdapter.dip2px;
import static com.stupidtree.hita.timetable.TimeWatcherService.TIMETABLE_CHANGED;

import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_COURSE;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_DYNAMIC;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_EXAM;


@SuppressLint("ValidFragment")
public class FragmentTimeLine extends BaseFragment implements
        RefreshBroadcastReceiver.ActionListener, TimelineListAdapter.TimeLineSelf{
    private boolean hasInit = false;
    private boolean isFirst = false;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private NestedScrollView noneLayout;
    private View[] heads;
    private ArcProgress circleProgress;
    private TimelineListAdapter TimeLineListAdapter;
    private TimeLineWholedayAdapter timelineWholedayAdapter;
    private  EventItem nowEvent;
    private  EventItem nextEvent;
    private  float nowProgress;
    private  List<EventItem> todaysEvents;
    private RecyclerView TimeLineList, timelineWholedayList;
    private List<EventItem> timelineRes, wholeDayRes;
    private ImageView bt_bar_timetable, bt_bar_addEvent;
    private LinearLayout head_counting, head_goNow;
    private TextView head_counting_time, head_counting_name,
            head_goQuickly_classroom;
    private CardView head_card;
    private TextView head_title, head_subtitle;
    private ImageView head_image, head_counting_image;
    private static headCardClickListener headCardClickListener;
    private RefreshBroadcastReceiver refreshReceiver;
    private LocalBroadcastManager localBroadcastManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RefreshTask pageTask;

    private OnFragmentInteractionListener onFragmentInteractionListener;
    public FragmentTimeLine() {

    }

    public static FragmentTimeLine newInstance(Boolean isFirst) {
        FragmentTimeLine fl = new FragmentTimeLine();
        Bundle b = new Bundle();
        b.putBoolean("isFirst", isFirst);
        fl.setArguments(b);
        return fl;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) isFirst = getArguments().getBoolean("isFirst");
        //layoutAnimationController = AnimationUtils.loadLayoutAnimation(FragmentTimeLine.this.getContext(), R.anim.recycler_layout_animation_falls_down);

        initReceiver();
    }


    private void initReceiver() {
        refreshReceiver = new RefreshBroadcastReceiver();
        refreshReceiver.setListener(this);
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter iF = new IntentFilter();
       // iF.addAction(TIMELINE_REFRESH);
        iF.addAction(TIMETABLE_CHANGED);
     //   iF.addAction(TIMETABLE_PAGE_REFRESH);
        localBroadcastManager.registerReceiver(refreshReceiver, iF);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_timeline, container, false);
        initViews(v);
        initListAndAdapter(v);

        if (!isFirst) {
            head_card.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        MaterialCircleAnimator.animShow(mAppBarLayout, 500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            head_card.setVisibility(View.INVISIBLE);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Refresh(true,false, false);
    }

    public void showHeadCard() {
        if (!hasInit) return;
       // if(timeServiceBinder==null) return;
        head_card.setVisibility(View.VISIBLE);
        head_card.post(new Runnable() {
            @Override
            public void run() {
                MaterialCircleAnimator.animShow(mAppBarLayout, 500);
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onFragmentInteractionListener = (OnFragmentInteractionListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onFragmentInteractionListener = null;
    }

    @Override
    public void receive(Context context, Intent intent) {
        Log.e("Timeline接收到广播",intent.getAction());
        if(isResumed()){
            if(Objects.equals(intent.getAction(), TIMETABLE_CHANGED)){
                Refresh(true,true, false);
            }
        }

//        else if (Objects.equals(intent.getAction(), TIMELINE_REFRESH)) {
//            Refresh(true,true, false);
//        }
//        else if (intent.getAction().equals(TIMETABLE_PAGE_REFRESH)) {
//            Refresh(true,false, true);
//        }
    }


    @Override
    protected void stopTasks() {
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
    }

    @Override
    public void Refresh() {

    }

    @Override
    public EventItem getNowEvent() {
        return nowEvent;
    }

    @Override
    public float getNowProgress() {
        return nowProgress;
    }

    public interface OnFragmentInteractionListener {

    }

    public void initViews(View v) {
        todaysEvents = new ArrayList<>();
        mAppBarLayout = v.findViewById(R.id.app_bar);
        noneLayout = v.findViewById(R.id.tl_noneview);
        head_card = v.findViewById(R.id.timeline_head_card);
        bt_bar_timetable = v.findViewById(R.id.bt_timetable);
        bt_bar_addEvent = v.findViewById(R.id.bt_add);
        head_title = v.findViewById(R.id.timeline_titile);
        head_subtitle = v.findViewById(R.id.timeline_subtitle);
        head_image = v.findViewById(R.id.timeline_head_image);
        head_goNow = v.findViewById(R.id.timeline_head_gonow);
        mCollapsingToolbarLayout = v.findViewById(R.id.collapsingtoolbar);
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.parseColor("#202020"));
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.parseColor("#00202020"));
        mCollapsingToolbarLayout.setScrimAnimationDuration(280);
        mCollapsingToolbarLayout.setScrimVisibleHeightTrigger(280);

        bt_bar_timetable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Transition explode = TransitionInflater.from(FragmentTimeLine.this.getActivity()).inflateTransition(android.R.transition.explode);
                Intent ii = new Intent(FragmentTimeLine.this.getActivity(), ActivityTimeTable.class);
                startActivity(ii);
            }
        });
        bt_bar_addEvent.setOnClickListener(new addEventClickListener());
        initHead(v);
        hasInit = true;

    }


    @SuppressLint("ClickableViewAccessibility")
    private void initHead(View v) {
        circleProgress = v.findViewById(R.id.circle_progress);
        headCardClickListener = new headCardClickListener();

        head_card.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    headCardClickListener.posX = event.getX();
                    headCardClickListener.posY = event.getY();
                }
                return false;
            }
        });

        head_card.setOnClickListener(headCardClickListener);
        head_counting = v.findViewById(R.id.head_counting);
        heads = new View[]{head_image, head_goNow, circleProgress};
        head_counting_name = v.findViewById(R.id.tl_head_counting_name);
        head_counting_image = v.findViewById(R.id.tl_head_counting_image);
        head_counting_time = v.findViewById(R.id.tl_head_counting_time);
        head_goQuickly_classroom = v.findViewById(R.id.tl_head_gonow_classroom);
    }

    private void initListAndAdapter(View v) {
        LinearLayoutManager layoutManager = new WrapContentLinearLayoutManager(this.getContext());
        LinearLayoutManager layoutManager2 = new WrapContentLinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        TimeLineList = v.findViewById(R.id.timelinelist);
        swipeRefreshLayout = v.findViewById(R.id.timeline_refresh);
        timelineWholedayList = v.findViewById(R.id.timeline_wholeday_list);
        wholeDayRes = new ArrayList<>();
        timelineRes = new ArrayList<>();
        timelineWholedayAdapter = new TimeLineWholedayAdapter(this.getContext(), wholeDayRes);
        TimeLineListAdapter = new TimelineListAdapter(this.getContext(),this, timelineRes);
        TimeLineList.setAdapter(TimeLineListAdapter);
        TimeLineList.setLayoutManager(layoutManager);
        timelineWholedayList.setLayoutManager(layoutManager2);
        timelineWholedayList.setAdapter(timelineWholedayAdapter);
        TimeLineListAdapter.setOnItemClickLitener(new TimelineListAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View card, View time, View name, int position) {
                showEventDialog(FragmentTimeLine.this.getActivity(), timelineRes.get(position), card, name);
            }


        });
        TimeLineListAdapter.setOnItemLongClickLitener(new TimelineListAdapter.OnItemLongClickLitener() {
            @Override
            public void onItemLongClick(View view, int position) {
                if (timelineRes.get(position).eventType != TimetableCore.TIMETABLE_EVENT_TYPE_COURSE && timelineRes.get(position).eventType != TIMETABLE_EVENT_TYPE_DYNAMIC) {
                    ExplosionField ef = ExplosionField.attach2Window(Objects.requireNonNull(FragmentTimeLine.this.getActivity()));
                    ef.explode(view);
                    new DeleteTask_timeline(position).executeOnExecutor(HITAApplication.TPE);
                }
            }
        });
        timelineWholedayAdapter.setOnItemClickListener(new TimeLineWholedayAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View v, int position) {
                showEventDialog(FragmentTimeLine.this.getActivity(), wholeDayRes.get(position), null, null);
            }
        });

        timelineWholedayAdapter.setOnItemLongClickListener(new TimeLineWholedayAdapter.OnItemLongClickListener() {
            @Override
            public boolean OnLongClick(View v, int position) {
                ExplosionField ef = ExplosionField.attach2Window(FragmentTimeLine.this.getActivity());
                ef.explode(v);
                new DeleteTask_wholeday(position).executeOnExecutor(HITAApplication.TPE);
                return true;
            }
        });
        swipeRefreshLayout.setColorSchemeColors(((BaseActivity) Objects.requireNonNull(getActivity())).getColorAccent());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               // Intent i = new Intent(getContext(),);
                Refresh(true,true, true);
            }
        });
    }


    private int getTodayCourseNum() {
        int result = 0;
        for (EventItem ei : todaysEvents) {
            if (ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_COURSE) {
                result++;
            }
        }
        return result;
    }
    @SuppressLint("SetTextI18n")
    private void UpdateHeadView() {
        String titleToSet, subtitltToSet;
        if (CurrentUser == null) {
            titleToSet = getContext().getString(R.string.timeline_head_nulluser_title);
            subtitltToSet = getContext().getString(R.string.timeline_head_nulluser_subtitle);
            switchHeadView(head_image, R.drawable.ic_timeline_head_login);
            //switchToCountingAvailable = false;
            headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.LOG_IN);

        } else if (!timeTableCore.isDataAvailable()) {
            titleToSet = getContext().getString(R.string.timeline_head_nulldata_title);
            subtitltToSet = getContext().getString(R.string.timeline_head_nulldata_subtitle);
            switchHeadView(head_image, R.drawable.ic_timeline_head_nulldata);
            headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.JWTS);
        } else if (!timeTableCore.isThisTerm()) {
            titleToSet = getContext().getString(R.string.timeline_head_notthisterm_title);
            subtitltToSet = getContext().getString(R.string.timeline_head_notthisterm_subtitle);
            switchHeadView(head_image, R.drawable.ic_origami_paper_bird);
            headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.SHOW_NEXT);
        } else if (timelineRes.size() == 0) {
            titleToSet = getContext().getString(R.string.timeline_head_free_title);
            subtitltToSet = getContext().getString(R.string.timeline_head_free_subtitle);
            switchHeadView(head_image, R.drawable.ic_timeline_head_free);
            headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.SHOW_NEXT);
        } else if (nowEvent != null) {
            switchHeadView(circleProgress, -1);
            titleToSet = nowEvent.mainName;
            subtitltToSet = getContext().getString(R.string.timeline_head_ongoing_subtitle);
            circleProgress.setProgress((int) (nowProgress * 100));
//
//            waveView.setWaterLevelRatio(timeServiceBinder.getNowProgress());
//            waveViewHelper.start();

            headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.SHOW_NEXT);
        } else {
            if (new HTime(now).compareTo(new HTime(5, 0)) < 0 && new HTime(now).compareTo(new HTime(0, 0)) > 0) {
                switchHeadView(head_image, R.drawable.ic_moon);
                titleToSet = getContext().getString(R.string.timeline_head_goodnight_title);
                subtitltToSet = getContext().getString(R.string.timeline_head_goodnight_subtitle);
                headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.SHOW_NEXT);
            } else if (new HTime(now).compareTo(new HTime(8, 15)) < 0 && new HTime(now).compareTo(new HTime(5, 00)) > 0) {
                switchHeadView(head_image, R.drawable.ic_sunny);
                titleToSet = getContext().getString(R.string.timeline_head_goodmorning_title);
                subtitltToSet =  String.format(getString(R.string.timelinr_goodmorning_subtitle), getTodayCourseNum());
                headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.SHOW_NEXT);
            } else if (new HTime(now).compareTo(new HTime(12, 15)) > 0 && new HTime(now).compareTo(new HTime(13, 00)) < 0) {
                switchHeadView(head_image, R.drawable.ic_lunch);
                titleToSet = getContext().getString(R.string.timeline_head_lunch_title);
                subtitltToSet = getContext().getString(R.string.timeline_head_lunch_subtitle);
                headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.SHOW_NEXT);
            } else if (new HTime(now).compareTo(new HTime(17, 10)) > 0 && new HTime(now).compareTo(new HTime(18, 10)) < 0) {
                switchHeadView(head_image, R.drawable.ic_lunch);
                titleToSet = getContext().getString(R.string.timeline_head_dinner_title);
                subtitltToSet = getContext().getString(R.string.timeline_head_dinner_subtitle);
                headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.SHOW_NEXT);
            } else if (nextEvent != null) {
                if (nextEvent.startTime.getDuration(new HTime(now)) <= 15 && (nextEvent.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_COURSE || nextEvent.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_EXAM)) {
                    switchHeadView(head_goNow, -1);
                    titleToSet = nextEvent .mainName;
                    // subtitltToSet = getContext().getString(R.string.timeline_head_gonow_subtitle);
                    subtitltToSet = String.format(getString(R.string.timeline_gonow_subtitle) ,nextEvent.startTime.getDuration(new HTime(now)));
                    head_goQuickly_classroom.setText(nextEvent.tag2);
                    headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.NAVI_CLASSROOM);
                } else {
                    titleToSet = getContext().getString(R.string.timeline_head_normal_title);
                    subtitltToSet = getContext().getString(R.string.timeline_head_normal_subtitle);
                    switchHeadView(head_image, R.drawable.ic_sunglasses);
                    headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.SHOW_NEXT);
                }
            } else {
                if (new HTime(now).compareTo(new HTime(23, 00)) > 0 || new HTime(now).compareTo(new HTime(5, 0)) < 0) {
                    switchHeadView(head_image, R.drawable.ic_moon);
                    titleToSet = getContext().getString(R.string.timeline_head_goodnight_title);
                    subtitltToSet = getContext().getString(R.string.timeline_head_goodnight_subtitle);

                } else {
                    switchHeadView(head_image, R.drawable.ic_finish);
                    titleToSet = getContext().getString(R.string.timeline_head_finish_title);
                    subtitltToSet = getContext().getString(R.string.timeline_head_finish_subtitle);
                }
                headCardClickListener.setMode(FragmentTimeLine.headCardClickListener.SHOW_NEXT);
            }
        }
        if (nextEvent != null) {
            String text1 = String.format(
                    getString(R.string.time_format_1),
                    nextEvent.startTime.getDuration(new HTime(now)) / 60,
                    nextEvent.startTime.getDuration(new HTime(now)) % 60);
            String text2 = String.format(
                    getString(R.string.time_format_2),
                   nextEvent.startTime.getDuration(new HTime(now))
            );
            String timeText =nextEvent.startTime.getDuration(new HTime(now)) >= 60 ? text1
                    : text2;
            head_counting_name.setText(nextEvent.mainName);
            head_counting_time.setText(timeText+getString(R.string.timeline_counting_middle));
            head_counting_image.setImageResource(R.drawable.ic_access_alarm_black_24dp);
            // head_counting_name.setVisibility(View.VISIBLE);
        } else {
            head_counting_name.setText("see you");
            head_counting_time.setText(R.string.timeline_counting_free);
            head_counting_image.setImageResource(R.drawable.ic_empty);
        }
        head_title.setText(titleToSet);
        head_subtitle.setText(subtitltToSet);
    }

    void switchHeadView(View view, int imageId) {
        for (int i = 0; i < heads.length; i++) {
            if (heads[i] == view) heads[i].setVisibility(View.VISIBLE);
            else heads[i].setVisibility(View.GONE);
        }
        if (view instanceof ImageView) ((ImageView) view).setImageResource(imageId);
        head_counting.post(new Runnable() {
            @Override
            public void run() {
                MaterialCircleAnimator.animHide(head_counting);
            }
        });

    }

    private void Refresh(boolean notifyAll,boolean anim, boolean swipe) {
        if (!hasInit||getContext()==null) return;
        if (pageTask != null && pageTask.getStatus() != AsyncTask.Status.FINISHED)
            pageTask.cancel(true);
        pageTask = new RefreshTask(notifyAll,anim, swipe);
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }

    private class RefreshTask extends AsyncTask<String, Integer, Integer> {
        boolean swipe;
        boolean notifyAll;
        boolean anim;

        RefreshTask(boolean notifyAll,boolean animation, boolean swipe) {
            this.swipe = swipe;
            this.notifyAll = notifyAll;
            this.anim = animation;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (swipe) swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            if (!timeTableCore.isThisTerm()) return -2;
            int DOW = now.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : now.get(Calendar.DAY_OF_WEEK) - 1;
            todaysEvents.clear();
            todaysEvents.addAll(timeTableCore.getOneDayEvents(timeTableCore.getThisWeekOfTerm(), DOW));
            refreshNowAndNextEvent();
            if (!timeTableCore.isDataAvailable()) return -1;
            timelineRes.clear();
            wholeDayRes.clear();
            for (EventItem ei : todaysEvents) {
                //  Log.e("ei:",ei.mainName+","+ei.isWholeDay);
                if (ei.isWholeDay) wholeDayRes.add(ei);
                else timelineRes.add(ei);
            }
            if (timelineRes.size() == 0) return -5;
            return 0;
        }


        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            UpdateHeadView();
            swipeRefreshLayout.setRefreshing(false);
            if (integer == -1) {
                noneLayout.setVisibility(View.VISIBLE);
                TimeLineList.setVisibility(View.GONE);
                return;
            } else if (integer == -2) {
                noneLayout.setVisibility(View.VISIBLE);
                TimeLineList.setVisibility(View.GONE);
                return;
            } else if (integer == -5) {
                noneLayout.setVisibility(View.VISIBLE);
                TimeLineList.setVisibility(View.GONE);
            } else {
                TimeLineList.setVisibility(View.VISIBLE);
                noneLayout.setVisibility(View.INVISIBLE);
            }
            timelineWholedayAdapter.notifyDataSetChanged();
            if (notifyAll) TimeLineListAdapter.notifyDataSetChanged();
            if (anim && swipe) {
                // TimeLineList.setLayoutAnimation(layoutAnimationController);
                TimeLineList.scheduleLayoutAnimation();
            }

        }
    }

    private void refreshNowAndNextEvent() {
        HTime nowTime = new HTime(now);
        try {
            boolean changed_now = false;
            boolean changed_next = false;
            for (int i = todaysEvents.size() - 1; i >= 0; i--) {
                EventItem ei = todaysEvents.get(i);
                if (ei.hasCross(nowTime) && (!ei.isWholeDay)
                        && ei.eventType != TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE
                        && ei.eventType != TimetableCore.TIMETABLE_EVENT_TYPE_REMIND
                ) {
                    nowEvent = ei;
                    changed_now = true;
                } else if (ei.startTime.compareTo(nowTime) > 0) {
                    nextEvent = ei;
                    changed_next = true;
                }
            }
            if (!changed_next) nextEvent = null;
            if (!changed_now) nowEvent = null;
            if(nowEvent!=null){
                nowProgress = ((float) new HTime(now).getDuration(nowEvent.startTime)) / ((float) nowEvent.endTime.getDuration(nowEvent.startTime));

            }
        } catch (Exception e) {
            e.printStackTrace();
//            nowEvent = null;
//            nextEvent = null;
            return;
        }

        //if(nowEvent==null&&)

    }

    class DeleteTask_timeline extends AsyncTask {
        int position;

        DeleteTask_timeline(int pos) {
            position = pos;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            boolean res = timeTableCore.deleteEvent(timelineRes.get(position), timelineRes.get(position).eventType == TIMETABLE_EVENT_TYPE_DEADLINE);
            if (res) {
                todaysEvents.remove(timelineRes.get(position));
                timelineRes.remove(position);
                refreshNowAndNextEvent();
            }
            return res;

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if ((Boolean) o) {
                TimeLineListAdapter.notifyItemRemoved(position);
                if (position != timelineRes.size()) { // 如果移除的是最后一个，忽略
                    TimeLineListAdapter.notifyItemRangeChanged(position, todaysEvents.size() - position);
                } else {
                    if (position - 1 >= 0) TimeLineListAdapter.notifyItemChanged(position - 1);
                }
                UpdateHeadView();
                if (timelineRes.size() == 0) {
                    noneLayout.setVisibility(View.VISIBLE);
                    TimeLineList.setVisibility(View.GONE);
                }
            }
            Intent mes = new Intent(TIMETABLE_CHANGED);
            //Intent mes2 = new Intent(WATCHER_REFRESH);
            localBroadcastManager.sendBroadcast(mes);
           // localBroadcastManager.sendBroadcast(mes2);
            //if(ftsk!=null&&ftsk.hasInit) ftsk.Refresh();
            ActivityMain.saveData();

        }
    }

    class DeleteTask_wholeday extends AsyncTask {
        int position;

        DeleteTask_wholeday(int pos) {
            position = pos;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            boolean res = timeTableCore.deleteEvent(wholeDayRes.get(position), wholeDayRes.get(position).eventType == TIMETABLE_EVENT_TYPE_DEADLINE);
            if (res) {
                refreshNowAndNextEvent();
                wholeDayRes.remove(position);

            }
            return res;

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (wholeDayRes.size() == 0) {
                timelineWholedayList.setVisibility(View.GONE);
            }
            if ((Boolean) o) {
                timelineWholedayAdapter.notifyItemRemoved(position);
                if (position != wholeDayRes.size()) { // 如果移除的是最后一个，忽略
                    timelineWholedayAdapter.notifyItemRangeChanged(position, todaysEvents.size() - position);
                }
                UpdateHeadView();
                ActivityMain.saveData();
            }
            Intent mes2 = new Intent(TIMETABLE_CHANGED);
            //Intent mes = new Intent(TASK_REFRESH);
          //  localBroadcastManager.sendBroadcast(mes);
            localBroadcastManager.sendBroadcast(mes2);
            // if(ftsk!=null&&ftsk.hasInit) ftsk.Refresh();

        }
    }

    class headCardClickListener implements View.OnClickListener {
        public static final int SHOW_NEXT = 94;
        public static final int LOG_IN = 713;
        public static final int JWTS = 577;
        public static final int CANTEEN = 379;
        public static final int NONE = 264;
        private static final int NAVI_CLASSROOM = 143;
        float posX;
        float posY;
        int mode;

        public void setMode(int mode) {
            this.mode = mode;
        }

        @Override
        public void onClick(View v) {
            switch (mode) {
                case SHOW_NEXT:
                    if (head_counting.getVisibility() == View.VISIBLE) {
                        head_counting.post(new Runnable() {
                            @Override
                            public void run() {
                                MaterialCircleAnimator.animHide(head_counting, 400, posX, posY);
                            }
                        });

                    } else {
                        head_counting.post(new Runnable() {
                            @Override
                            public void run() {
                                MaterialCircleAnimator.animShow(head_counting, 400, posX, posY);
                            }
                        });

                    }
                    break;
                case LOG_IN:
                    Intent i = new Intent(getActivity(), ActivityLogin.class);
                    startActivity(i);
                    break;
                case JWTS:
                    Intent k;
                    if (CurrentUser.getStudentnumber() == null || CurrentUser.getStudentnumber().isEmpty()) {
                        AlertDialog ad = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.attention)).setMessage(getString(R.string.settings_noti_bindfirst)).setPositiveButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(HContext, ActivityUserCenter.class);
                                startActivity(i);
                            }
                        }).create();
                        ad.show();
                    } else {
                        k = new Intent(HContext, ActivityLoginJWTS.class);
                        startActivity(k);
                    }
                    break;
                case CANTEEN:
                    Intent c = new Intent(getActivity(), ActivityLeaderBoard.class);
                    startActivity(c);
                    break;
                case NAVI_CLASSROOM:
                    if (nextEvent != null) {
                        ActivityUtils.startExploreActivity_forNavi(getActivity(), nextEvent.tag2);
                    }

            }

        }
    }

    class addEventClickListener implements View.OnClickListener {
        FragmentAddEvent aef;

        addEventClickListener() {
            aef = FragmentAddEvent.newInstance();
        }

        @Override
        public void onClick(View v) {
            if (timeTableCore.isDataAvailable()) {
                aef.show(getFragmentManager(), "add_event");
                //if (isThisTerm) aef.show(getFragmentManager(), "add_event");
                //else Snackbar.make(v, "这学期还没开始呐，试着切换课表为已开始学期吧！", Snackbar.LENGTH_SHORT).show();
                // Toast.makeText(FragmentTimeLine.this.getContext(), , Toast.LENGTH_SHORT).show();
            } else {
                Snackbar.make(v, getString(R.string.notif_importdatafirst), Snackbar.LENGTH_SHORT).show();
                // Toast.makeText(FragmentTimeLine.this.getContext(), "请先导入课表！", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @SuppressLint("SetTextI18n")
    public static void showEventDialog(final Context a, final EventItem ei, final View transitionCard, View transitionName) {
        View dlgView = null;
        LayoutInflater inflater = LayoutInflater.from(a);
        final AlertDialog dialog = new AlertDialog.Builder(a).create();

        if (ei.eventType == TIMETABLE_EVENT_TYPE_COURSE) {
            new CourseDialog(a, ei).show();
        } else if (ei.eventType == TIMETABLE_EVENT_TYPE_EXAM) {
            dlgView = inflater.inflate(R.layout.dialog_timetable_exam, null);
            final TextView value1 = dlgView.findViewById(R.id.tt_dlg_value1);
            final TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
            value1.setText(ei.tag2);//考场
            value2.setText(ei.tag4.isEmpty() ? a.getString(R.string.none): ei.tag4);//具体考试时间

        } else if (ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_ARRANGEMENT || ei.eventType == TIMETABLE_EVENT_TYPE_DYNAMIC) {
            dlgView = inflater.inflate(R.layout.dialog_timetable_arrangement, null);
            final TextView value1 = dlgView.findViewById(R.id.tt_dlg_value1);
            final TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
            final TextView value3 = dlgView.findViewById(R.id.tt_dlg_value3);
            value1.setText(ei.tag2.isEmpty() ? a.getString(R.string.none) : ei.tag2);//标签1
            value2.setText(ei.tag3.isEmpty() ? a.getString(R.string.none)  : ei.tag3);//标签2
            value3.setText(ei.startTime.tellTime() + "-" + ei.endTime.tellTime());
        } else if (ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE) {
            dlgView = inflater.inflate(R.layout.dialog_timetable_deadline, null);
            final TextView value1 = dlgView.findViewById(R.id.tt_dlg_value1);
            final TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
            final TextView value3 = dlgView.findViewById(R.id.tt_dlg_value3);
            value1.setText(ei.tag2.isEmpty() ? a.getString(R.string.none)  : ei.tag2);//标签1
            value2.setText(ei.tag3.isEmpty() ? a.getString(R.string.none)  : ei.tag3);//标签2
            value3.setText(ei.startTime.tellTime());
        } else if (ei.eventType == TimetableCore.TIMETABLE_EVENT_TYPE_REMIND) {
            dlgView = inflater.inflate(R.layout.dialog_timetable_remind, null);
            final TextView value1 = dlgView.findViewById(R.id.tt_dlg_value1);
            final TextView value2 = dlgView.findViewById(R.id.tt_dlg_value2);
            final TextView value3 = dlgView.findViewById(R.id.tt_dlg_value3);
            value1.setText(ei.tag2.isEmpty() ? a.getString(R.string.none) : ei.tag2);//标签1
            value2.setText(ei.tag3.isEmpty() ? a.getString(R.string.none)  : ei.tag3);//标签2
            value3.setText(ei.startTime.tellTime());
        }

        if (ei.eventType != TIMETABLE_EVENT_TYPE_COURSE) {

            TextView date = dlgView.findViewById(R.id.tt_dlg_date);
            final TextView name = dlgView.findViewById(R.id.tt_dlg_name);
            name.setText(ei.mainName);
            ImageView detail = dlgView.findViewById(R.id.dlg_bt_detail);
            final Calendar c = timeTableCore.getCurrentCurriculum().getDateAtWOT(ei.week, ei.DOW);
            date.setText(a.getResources().getStringArray(R.array.months)[c.get(Calendar.MONTH)] +
                    String.format(a.getString(R.string.date_day),c.get(Calendar.DAY_OF_MONTH)) +
                    "("+
                    String.format(a.getString(R.string.week),ei.week) +" "+
                    a.getResources().getStringArray(R.array.dow1)[ei.DOW - 1] + ")");
            detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu pm = new PopupMenu(a, v);
                    int menuId;
                    switch (ei.eventType) {
                        case TIMETABLE_EVENT_TYPE_COURSE:
                            menuId = R.menu.menu_opr_dialog_detail_course;
                            break;
                        case TIMETABLE_EVENT_TYPE_EXAM:
                            menuId = R.menu.menu_opr_dialog_detail_exam;
                            break;
                        default:
                            menuId = R.menu.menu_opr_dialog_detail_normal;
                    }
                    pm.inflate(menuId);
                    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.opr_delete) {
                                android.app.AlertDialog ad = new android.app.AlertDialog.Builder(a).
                                        setNegativeButton(a.getString(R.string.button_cancel), null)
                                        .setPositiveButton(a.getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface d, int which) {
                                                if (timeTableCore.deleteEvent(ei, ei.eventType == TIMETABLE_EVENT_TYPE_DEADLINE)) {
                                                    Toast.makeText(a, a.getString(R.string.notif_delete_success), Toast.LENGTH_SHORT).show();
                                                    Intent i = new Intent();
                                                    i.putExtra("week", ei.week);
                                                    i.setAction(TIMETABLE_CHANGED);
                                                    //Intent i2 = new Intent();
                                                    LocalBroadcastManager.getInstance(a).sendBroadcast(i);
                                                    dialog.dismiss();
                                                }
                                            }
                                        }).create();
                                ad.setTitle(a.getString(R.string.dialog_title_sure_delete));
                                if (ei.eventType == TIMETABLE_EVENT_TYPE_COURSE) {
                                    ad.setMessage(a.getString(R.string.dialog_message_sure_delete));
                                }
                                ad.show();
                            } else if (item.getItemId() == R.id.opr_subject) {
                                if (ei.eventType == TIMETABLE_EVENT_TYPE_COURSE) {
                                    ActivityUtils.startSubjectActivity_name(a, ei.mainName);
                                } else if (ei.eventType == TIMETABLE_EVENT_TYPE_EXAM) {
                                    if (ei.tag3.startsWith("科目代码：")) {
                                        ActivityUtils.startSubjectActivity_code(a, ei.tag3.substring(5));
                                    } else if (ei.tag3.startsWith("科目名称：")) {
                                        ActivityUtils.startSubjectActivity_name(a, ei.tag3.substring(5));
                                    } else {
                                        ActivityUtils.startSubjectActivity_name(a, ei.tag3);
                                    }
                                }

                            }
//                            else if (item.getItemId() == R.id.opr_detail) {
//                                Intent i = new Intent(a, ActivityCourse.class);
//                                Bundle b = new Bundle();
//                                b.putSerializable("eventitem", ei);
//                                i.putExtra("showSubject", true);
//                                i.putExtras(b);
//                                a.startActivity(i);
//                            }
                            return true;
                        }
                    });
                    pm.show();
                }
            });
        }

        if (ei.isWholeDay) {
            final TextView value3 = dlgView.findViewById(R.id.tt_dlg_value3);
            value3.setText(a.getString(R.string.wholeday));
        }

        if (ei.eventType != TIMETABLE_EVENT_TYPE_COURSE) {
            dialog.setView(dlgView);
            dialog.show();
            dialog.getWindow().setLayout(dip2px(a, 320), LinearLayout.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background_radius);
        }


    }


}
