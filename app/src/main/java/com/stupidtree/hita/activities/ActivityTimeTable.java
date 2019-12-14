package com.stupidtree.hita.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.R;
import com.stupidtree.hita.adapter.SubjectsListAdapter;
import com.stupidtree.hita.adapter.TimeTablePagerAdapter;
import com.stupidtree.hita.core.Subject;
import com.stupidtree.hita.fragments.FragmentAddCourse;
import com.stupidtree.hita.fragments.FragmentAddEvent;
import com.stupidtree.hita.fragments.FragmentTimeTablePage;
import com.stupidtree.hita.util.ColorBox;

import java.lang.ref.WeakReference;
import java.util.Calendar;



import static com.stupidtree.hita.HITAApplication.DATA_STATE_HEALTHY;
import static com.stupidtree.hita.HITAApplication.DATA_STATE_NONE_CURRICULUM;
import static com.stupidtree.hita.HITAApplication.DATA_STATE_NULL;
import static com.stupidtree.hita.HITAApplication.TPE;
import static com.stupidtree.hita.HITAApplication.allCurriculum;
import static com.stupidtree.hita.HITAApplication.correctData;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.HITAApplication.getDataState;
import static com.stupidtree.hita.HITAApplication.isThisTerm;
import static com.stupidtree.hita.HITAApplication.mainTimeTable;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.thisCurriculumIndex;
import static com.stupidtree.hita.HITAApplication.thisWeekOfTerm;
import static com.stupidtree.hita.HITAApplication.timeWatcher;

public class ActivityTimeTable extends BaseActivity implements FragmentTimeTablePage.OnFragmentInteractionListener,FragmentAddEvent.OnFragmentInteractionListener {
    /*标志类常量*/
    final int FROM_SPINNER_Curriculum = 1;
    final int FROM_SPINNER_TIMETABLE = 2;
    final int FROM_INIT = 4;
    final int FROM_RESUME = 5;

    /*重要数据变量*/

    int pageWeekOfTerm; //代表当前页面显示的是哪一周
    boolean hasInit = false;

    public AppBarLayout mAppBarLayout;
    Toolbar mToolbar;
    LinearLayout invalidLayout;
    //Button invalidJump;

    ViewPager viewPager;
    TimeTablePagerAdapter pagerAdapter;
    FloatingActionButton fab,fab_add;
    TabLayout tabs;
    RefreshTask pageTask;
    FragmentAddCourse fragmentAddCourse;



    /*初始化_获取所有控件对象*/
    void initAllViews() {
        fab = findViewById(R.id.fab_thisweek);
        fab_add = findViewById(R.id.fab_add);
        //toolbar_title = findViewById(R.id.toolbar_title);
        mAppBarLayout = findViewById(R.id.app_bar);
        invalidLayout = findViewById(R.id.tt_invalidview);
//        invalidJump = findViewById(R.id.tt_invalid_jump);
//
//        invalidJump.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(ActivityTimeTable.this, ActivityCurriculumManager.class);
//                ActivityTimeTable.this.startActivity(i);
//            }
//        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (pageWeekOfTerm != thisWeekOfTerm && isThisTerm) {
                        pageWeekOfTerm = thisWeekOfTerm;
                        viewPager.setCurrentItem(thisWeekOfTerm-1);
                    }
            }
        });
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new FragmentAddEvent().show(getSupportFragmentManager(),"fae");
            }
        });
        initViewPager();
    }

    void initViewPager() {
        viewPager = findViewById(R.id.timetable_viewpager);
        tabs = findViewById(R.id.timetable_tabs);
       // tabs.setTabTextColors(Color.parseColor("#55000000"),getColorPrimary());
        tabs.setTabIndicatorFullWidth(false);
        if (getDataState() == DATA_STATE_HEALTHY)
            pagerAdapter = new TimeTablePagerAdapter(getSupportFragmentManager(), allCurriculum.get(thisCurriculumIndex).totalWeeks);
        else pagerAdapter = new TimeTablePagerAdapter(getSupportFragmentManager(), 0);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                pageWeekOfTerm = i + 1;
                //toolbar_title.setText("第" + pageWeekOfTerm + "周");
                if (pageWeekOfTerm == thisWeekOfTerm) {
                    fab.hide();
                    //toolbar_title.setTextColor(ContextCompat.getColor(ActivityTimeTable.this, R.color.theme1_colorPrimaryDark));
                } else if(isThisTerm){
                    fab.show();
                    //toolbar_title.setTextColor(ContextCompat.getColor(ActivityTimeTable.this, R.color.material_primary_text));
            }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        tabs.setupWithViewPager(viewPager);
        //if(thisWeekOfTerm-1>=0) viewPager.setCurrentItem(thisWeekOfTerm-1);
    }


    private void initToolBarAndDrawer() {
        mToolbar = findViewById(R.id.main_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//左侧添加一个默认的返回图标
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mToolbar.inflateMenu(R.menu.toolbar_time_table);
        mToolbar.setTitle("");
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                boolean isChecked = !menuItem.isChecked();
                switch (id) {
//                    case R.id.action_add_course:
//                       fragmentAddCourse.show(getSupportFragmentManager(),"fac");
//                        break;
//                    case R.id.action_switch_timetable:
//                        menuItem.setChecked(isChecked);
//                         PreferenceManager.getDefaultSharedPreferences(ActivityTimeTable.this).edit().putBoolean("timetable_curriculumonly",isChecked).apply();
//                        //Toast.makeText(ActivityTimeTable.this,"重新进入本页面生效",Toast.LENGTH_SHORT).show();
//                        //reFreshViewPager();
//                        pagerAdapter.notifyAllFragments();
//                        break;

                    case R.id.action_whole_day:
                        menuItem.setChecked(isChecked);
                        PreferenceManager.getDefaultSharedPreferences(ActivityTimeTable.this).edit().putBoolean("timetable_wholeday",isChecked).apply();
                        //Toast.makeText(ActivityTimeTable.this,"重新进入本页面生效",Toast.LENGTH_SHORT).show();
                        pagerAdapter.notifyAllFragments();
                        // Refresh(FROM_INIT);
                        break;

                    case R.id.action_colorful_mode:
                        menuItem.setChecked(isChecked);
                        defaultSP.edit().putBoolean("timetable_colorful_mode",isChecked).apply();
                        //Toast.makeText(ActivityTimeTable.this,"重新进入本页面生效",Toast.LENGTH_SHORT).show();
                        pagerAdapter.notifyAllFragments();
                        // Refresh(FROM_INIT);
                        break;
                    case R.id.action_draw_now_line:
                        menuItem.setChecked(isChecked);
                        defaultSP.edit().putBoolean("timetable_draw_now_line",isChecked).apply();
                        //Toast.makeText(ActivityTimeTable.this,"重新进入本页面生效",Toast.LENGTH_SHORT).show();
                        pagerAdapter.notifyAllFragments();
                        // Refresh(FROM_INIT);
                        break;
                    case R.id.action_reset_color:
                        AlertDialog ad = new AlertDialog.Builder(ActivityTimeTable.this).setTitle("是否随机生成各科目颜色？")
                                .setNegativeButton("取消",null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new resetColorTask().executeOnExecutor(TPE);;
                                    }
                                }).create();
                        ad.show();
                        break;
                    case R.id.action_reset_color_to_theme:
                        AlertDialog ad2 = new AlertDialog.Builder(ActivityTimeTable.this).setTitle("是否将各科目颜色设置为主题色？")
                                .setNegativeButton("取消",null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new resetColorToThemeTask().executeOnExecutor(HITAApplication.TPE);;
                                    }
                                }).create();
                        ad2.show();
                        break;
                }
                return true;
            }
        });
//        setDrawerLeftEdgeSize(FragmentTimeTable.this.getActivity(),mDrawerLayout,0,false);


    }

    /*刷新课表视图函数*/
    public void Refresh(int from) {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
        pageTask = new RefreshTask(from);
        pageTask.executeOnExecutor(HITAApplication.TPE);
    }


    void reFreshViewPager() {
        //curriculuManagerPagerAdapter.notifyAllFragments();
        viewPager.setCurrentItem(pageWeekOfTerm - 1);

    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onCalledRefresh() {
        pagerAdapter.notifyAllFragments();
    }


    class InitTask extends AsyncTask<String, Integer, String> {

        WeakReference<ActivityTimeTable> weakReference;
        InitTask(ActivityTimeTable at){
            weakReference = new WeakReference<>(at);
        }
        @Override
        protected String doInBackground(String... strings) {

            if (thisWeekOfTerm > 0) pageWeekOfTerm = thisWeekOfTerm;
            return null;
        }


        @Override
        protected void onPostExecute(String s) {
            ActivityTimeTable at = weakReference.get();
            if(at==null||at.isDestroyed()||at.isFinishing()) return;
            super.onPostExecute(s);
            initToolBarAndDrawer();
            hasInit = true;
            if(!isThisTerm) fab.hide();
            Refresh(FROM_INIT);
            Guide();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class RefreshTask extends AsyncTask<String, Integer, Integer> {

        int from;

        RefreshTask(int from) {
            this.from = from;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            if (!hasInit || getDataState() == DATA_STATE_NULL) return -1;
            if (getDataState() == DATA_STATE_NONE_CURRICULUM) {
                thisWeekOfTerm = -1;
                return DATA_STATE_NONE_CURRICULUM;
            }
            /*刷新必备代码--刷新数据*/
            now.setTimeInMillis(System.currentTimeMillis());
            correctData();
            isThisTerm = allCurriculum.get(thisCurriculumIndex).Within(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
            if (from == FROM_INIT || from == FROM_SPINNER_Curriculum) {
                try {
                    thisWeekOfTerm = allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now);
                    pageWeekOfTerm = isThisTerm ? thisWeekOfTerm : 1;
                } catch (Exception e) {
                    thisWeekOfTerm = -1;
                    pageWeekOfTerm = 1;
                }
            }
            if (from == FROM_SPINNER_Curriculum || from == FROM_SPINNER_TIMETABLE || from == FROM_INIT) {
                timeWatcher.refreshProgress(true,true);
            }
            return 99;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(Integer i) {
            if (i == -1) return;
            if (i == DATA_STATE_NONE_CURRICULUM) {
                invalidLayout.setVisibility(View.VISIBLE);
                viewPager.setVisibility(View.GONE);
                thisWeekOfTerm = -1;
                return;
            } else {
                invalidLayout.setVisibility(View.GONE);
                viewPager.setVisibility(View.VISIBLE);
            }
            if (pageWeekOfTerm == thisWeekOfTerm) {
                fab.hide();
                //toolbar_title.setTextColor(ContextCompat.getColor(ActivityTimeTable.this, R.color.theme1_colorPrimaryDark));
            } else if(isThisTerm){
                fab.show();
                //toolbar_title.setTextColor(ContextCompat.getColor(ActivityTimeTable.this, R.color.material_primary_text));
            }
            if(from!=FROM_RESUME) reFreshViewPager();
            super.onPostExecute(i);
        }

    }


    @Override
    protected void stopTasks() {
        if(pageTask!=null&&pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragmentAddCourse = FragmentAddCourse.newInstance();
        fragmentAddCourse.setCancelable(false);
//        requestWindowFeature(Window.FEATURE_CONTENT_TRANSITIONS);//申请动画
//        Transition explode = TransitionInflater.from(ActivityTimeTable.this).inflateTransition(android.R.transition.slide_left);
//        getWindow().setEnterTransition(explode);
        setWindowParams(true,true,false);
        setContentView(R.layout.activity_time_table);
        initAllViews();
        if (allCurriculum.size() <= 0) {
            pageWeekOfTerm = 1;
        } else {
            pageWeekOfTerm = allCurriculum.get(thisCurriculumIndex).getWeekOfTerm(now);
            if (pageWeekOfTerm < 0) {
                Snackbar.make(fab,"这个学期还没有开始哟",Snackbar.LENGTH_LONG).show();
                //Toast.makeText(this, , Toast.LENGTH_SHORT).show();
                isThisTerm = false;
                pageWeekOfTerm = 1;

            }
        }

        InitTask it = new InitTask(this);
        it.executeOnExecutor(HITAApplication.TPE);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

    }

    private void Guide(){
        if(defaultSP.getBoolean("timetable_first_open_20190916",true)){
            //TapTarget.forToolbarMenuItem()
            TapTargetView.showFor(this,TapTarget.forToolbarOverflow(mToolbar,"课程表现在支持分科目着色啦","如果觉得不好看，可以点这里选择关闭或重新生成，也可以到科目页面自定义哟~").outerCircleColor(R.color.blue_primary).descriptionTextColor(R.color.material_text_icon_white).cancelable(false)
            );
            defaultSP.edit().putBoolean("timetable_first_open_20190916",false).apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_time_table,menu);
//        MenuItem item = (MenuItem) menu.findItem(R.id.action_switch_timetable);
//        item.setActionView(R.layout.util_dynamictimetable_toolbar_actionlayout);
//        final Switch switchA = item
//                .getActionView().findViewById(R.id.action_layout_switch);
//        switchA.setChecked(!defaultSP.getBoolean("curriculumsonly",true));
//        switchA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
//                PreferenceManager.getDefaultSharedPreferences(ActivityTimeTable.this).edit().putBoolean("curriculumsonly",!isChecked).apply();
//                Toast.makeText(ActivityTimeTable.this,"重新进入本页面生效",Toast.LENGTH_SHORT).show();
//                //Refresh(FROM_INIT);
//            }
//        });
       // menu.findItem(R.id.action_switch_timetable).setChecked(defaultSP.getBoolean("timetable_curriculumonly",false));
        menu.findItem(R.id.action_whole_day).setChecked(defaultSP.getBoolean("timetable_wholeday",false));
        menu.findItem(R.id.action_colorful_mode).setChecked(defaultSP.getBoolean("timetable_colorful_mode",true));
        menu.findItem(R.id.action_draw_now_line).setChecked(defaultSP.getBoolean("timetable_draw_now_line",true));

        return super.onCreateOptionsMenu(menu);
    }

    class resetColorTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            for(Subject s:mainTimeTable.core.getSubjects()){
                defaultSP.edit().putInt("color:"+s.name, ColorBox.getRandomColor_Material()).commit();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            pagerAdapter.notifyAllFragments();
            super.onPostExecute(o);
        }
    }
    class resetColorToThemeTask extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] objects) {
            for(Subject s:mainTimeTable.core.getSubjects()){
                defaultSP.edit().putInt("color:"+s.name, getColorPrimary()).commit();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            pagerAdapter.notifyAllFragments();
            super.onPostExecute(o);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Refresh(FROM_RESUME);
    }
}
