package com.stupidtree.hita.activities;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.stupidtree.hita.BaseActivity;
import com.stupidtree.hita.HITAApplication;
import com.stupidtree.hita.hita.Term;
import com.stupidtree.hita.timetable.Subject;
import com.stupidtree.hita.diy.WrapContentLinearLayoutManager;
import com.stupidtree.hita.hita.ChatBotA;
import com.stupidtree.hita.hita.ChatBotB;
import com.stupidtree.hita.hita.TextTools;
import com.stupidtree.hita.R;
import com.stupidtree.hita.timetable.timetable.EventItem;
import com.stupidtree.hita.timetable.timetable.HTime;
import com.stupidtree.hita.adapter.ChatBotListAdapter;
import com.stupidtree.hita.timetable.timetable.Task;
import com.stupidtree.hita.hita.ChatBotMessageItem;
import com.stupidtree.hita.online.ChatMessage;
import com.stupidtree.hita.online.Infos;
import com.stupidtree.hita.online.Teacher;
import com.stupidtree.hita.util.ActivityUtils;



import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

import static com.stupidtree.hita.HITAApplication.timeTableCore;
import static com.stupidtree.hita.hita.ChatBotA.propcessSerchEvents;
import static com.stupidtree.hita.hita.TextTools.BEFORE;
import static com.stupidtree.hita.hita.TextTools.NEXT;
import static com.stupidtree.hita.hita.TextTools.THIS;
import static com.stupidtree.hita.hita.TextTools.TT_BEFORE;
import static com.stupidtree.hita.hita.TextTools.TT_NEXT;
import static com.stupidtree.hita.hita.TextTools.T_BEFORE;
import static com.stupidtree.hita.hita.TextTools.T_NEXT;
import static com.stupidtree.hita.HITAApplication.ChatBotListRes;
import static com.stupidtree.hita.HITAApplication.CurrentUser;
import static com.stupidtree.hita.HITAApplication.now;
import static com.stupidtree.hita.HITAApplication.defaultSP;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_ARRANGEMENT;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_COURSE;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_DEADLINE;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_EXAM;
import static com.stupidtree.hita.timetable.TimetableCore.TIMETABLE_EVENT_TYPE_REMIND;

public class ActivityChatbot extends BaseActivity {
    protected BottomSheetDialog dialog;
    private final int VIEW_TYPE_LEFT = -10;
    private final int VIEW_TYPE_RIGHT = -11;
    private EditText mEtMessageInput;
    private ImageView mBtnSend;
    Toolbar mToolbar;
    LinearLayout textInputLayout;
    LottieAnimationView animationView;
    ChatBotA chatbotA;
    ChatBotB chatbotB;
    ChatBotIteractTask pageTask;
    JsonObject chatBotInfos;


    ChatBotListAdapter ListAdapter;
    RecyclerView ChatList;
    LinearLayoutManager layoutManager;

    public static int State;
    List<EventItem> StateEventList;
    public final static int STATE_SEARCH_COURSE_LIST = 55;
    public final static int STATE_SEARCH_COURSE_SINGLE = 66;
    public final static int STATE_NORMAL = 22;

    CoordinatorLayout rootLayout;


    @Override
    protected void stopTasks() {
        if (pageTask != null && pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setWindowParams(true, true, false);
        chatbotA = new ChatBotA(this);
        chatbotB = new ChatBotB();
        setContentView(R.layout.activity_chatbot);
        initViews();
        initListAndAdapter();
        initChatBotInfo();

    }

    void initChatBotInfo() {
        BmobQuery<Infos> bq = new BmobQuery<>();
        bq.addWhereEqualTo("name", "chatbot_info");
        bq.findObjects(new FindListener<Infos>() {
            @Override
            public void done(List<Infos> list, BmobException e) {
                if (e == null && list != null && list.size() > 0) {
                    chatBotInfos = list.get(0).getJson();
                } else {
                    Log.e("Bmob错误", e.toString());
                    chatBotInfos = new JsonObject();
                    chatBotInfos.addProperty("message_show", "你好，我是希塔");
                    chatBotInfos.addProperty("hint", "获取提示失败");
                }
                if (ChatBotListRes.size() == 0) {
                    ChatBotMessageItem cmi = new ChatBotMessageItem(VIEW_TYPE_LEFT, chatBotInfos.get("message_show").getAsString());
                    cmi.setHint(chatBotInfos.get("hint").getAsString());
                    addMessageView(cmi, cmi.message, null);
                }
                initHitaAnimation();
            }
        });
    }


    void initHitaAnimation() {
        animationView = findViewById(R.id.hita_animation_view);
        animationView.setImageAssetsFolder("hita_animation/");
        if (chatBotInfos.has("animation"))
            animationView.setAnimationFromJson(chatBotInfos.get("animation").getAsJsonObject().getAsString(), "animation");
        else {
            Log.e("animation load failed", "没有找到在线动画");
            animationView.setAnimation("hita_animation/hita_normal.json");
        }

        animationView.playAnimation();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }












    private void initViews() {
        mEtMessageInput = findViewById(R.id.edit_send);
        mBtnSend = findViewById(R.id.btn_send);
        textInputLayout = findViewById(R.id.textInput);
        rootLayout = findViewById(R.id.chatbot_root_layout);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEtMessageInput.getText().toString();
                if (TextUtils.isEmpty(message)) {
                    Toast.makeText(ActivityChatbot.this, "请输入你要说的话", Toast.LENGTH_SHORT).show();
                    return;
                }
                mEtMessageInput.setText(null);
                addMessageToChat_Right(message);
            }
        });
        // btSpeak.setOnClickListener(this);
        initToolBar();
    }

    private void initListAndAdapter() {
        ListAdapter = new ChatBotListAdapter(this, ChatBotListRes);
        ChatList = findViewById(R.id.chat_recyclerview);
        layoutManager = new WrapContentLinearLayoutManager(this);
        ChatList.setLayoutManager(layoutManager);
        ChatList.setAdapter(ListAdapter);
        ChatList.scrollToPosition(ListAdapter.getItemCount() - 1);
        ListAdapter.setOnUserAvatarClickListener(new ChatBotListAdapter.OnUserAvatarClickListener() {
            @Override
            public void onClick(View v, int position) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ActivityChatbot.this, v, "useravatar");
                Intent i = new Intent(ActivityChatbot.this, ActivityUserCenter.class);
                ActivityChatbot.this.startActivity(i, options.toBundle());
            }
        });
    }


    private void initToolBar() {
        mToolbar = findViewById(R.id.toolbar);
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
        mToolbar.setOnMenuItemClickListener(new OnToolbarMenuClickListener());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_chat_bot, menu);
        return super.onCreateOptionsMenu(menu);
    }


    //****将消息加入当前聊天中****
    public void addMessageToChat_Right(String msg) {
        JsonObject message = new JsonObject();
        message.addProperty("message_show", msg);
        ChatBotMessageItem cmi = new ChatBotMessageItem(VIEW_TYPE_RIGHT, msg);
        addMessageView(cmi, msg, null);
        postToChatBot(msg);
    }


    //把消息post给聊天机器人线程
    private void postToChatBot(final String raw) {
        if (pageTask != null && pageTask.getStatus()!=AsyncTask.Status.FINISHED) pageTask.cancel(true);
        pageTask = new ChatBotIteractTask(raw, ActivityChatbot.this);
        pageTask.executeOnExecutor(
                HITAApplication.TPE);


    }


    //收到聊天机器人的回复后
    private void getReply(JsonObject received) {
        new ProcessReplyMessageTask(received).executeOnExecutor(HITAApplication.TPE);
    }


    private void addMessageView(ChatBotMessageItem message, String textOnShow,
                                String textToRead) {
        if (textToRead == null) textToRead = textOnShow;
        /*语音广播*/
        if (message.type == VIEW_TYPE_RIGHT) message.setMessage("“" + textOnShow + "”");
        else message.setMessage(textOnShow);
        ListAdapter.addMessage(message);
        if (message.type == VIEW_TYPE_RIGHT) {
            ListAdapter.deleteBefore(ChatBotListRes.size() - 1);
        }
       // ChatList.scrollToPosition(ListAdapter.getItemCount() - 1);
    }


    private EventItem propcessAddRemind(JsonObject values) {
        String name = values.get("name").getAsString();
        int fromW = values.get("fW").getAsInt();
        int toW = values.get("tW").getAsInt();
        int fromDOW = values.get("fDOW").getAsInt();
        int toDOW = values.get("tDOW").getAsInt();
        HTime fromT = new HTime(values.get("fH").getAsInt(), values.get("fM").getAsInt());
        HTime toT = new HTime(values.get("tH").getAsInt(), values.get("tM").getAsInt());
        int thisDOW = now.get(Calendar.DAY_OF_WEEK) == 1 ? 7 : now.get(Calendar.DAY_OF_WEEK) - 1;
        if (fromW == BEFORE) fromW = timeTableCore.getThisWeekOfTerm() - 1 <= 0 ? 1 : timeTableCore.getThisWeekOfTerm() - 1;
        if (fromW == THIS) fromW = timeTableCore.isThisTerm() ? timeTableCore.getThisWeekOfTerm() : 1;
        if (fromW == NEXT)
            fromW = timeTableCore.isThisTerm() ? ((timeTableCore.getThisWeekOfTerm() + 1 > timeTableCore.getCurrentCurriculum().getTotalWeeks()) ? timeTableCore.getCurrentCurriculum().getTotalWeeks() : timeTableCore.getThisWeekOfTerm() + 1) : 2;
        if (toW == BEFORE) toW = timeTableCore.getThisWeekOfTerm() - 1 <= 0 ? 1 : timeTableCore.getThisWeekOfTerm() - 1;
        if (toW == THIS) toW = timeTableCore.isThisTerm() ? timeTableCore.getThisWeekOfTerm() : 1;
        if (toW == NEXT)
            toW = timeTableCore.isThisTerm() ? ((timeTableCore.getThisWeekOfTerm() + 1 > timeTableCore.getCurrentCurriculum().getTotalWeeks()) ? timeTableCore.getCurrentCurriculum().getTotalWeeks() : timeTableCore.getThisWeekOfTerm() + 1) : 2;

        if (fromW == -1) {
            if (fromDOW == BEFORE) {
                if (thisDOW < 2) {
                    fromW = timeTableCore.getThisWeekOfTerm() - 1;
                    fromDOW = 7;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW - 1;
                }
            } else if (fromDOW == T_BEFORE) {
                if (thisDOW < 3) {
                    fromW = timeTableCore.getThisWeekOfTerm() - 1;
                    if (thisDOW == 2) fromDOW = 7;
                    else if (thisDOW == 1) fromDOW = 6;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW - 2;
                }
            } else if (fromDOW == TT_BEFORE) {
                if (thisDOW < 4) {
                    fromW = timeTableCore.getThisWeekOfTerm() - 1;
                    if (thisDOW == 3) fromDOW = 7;
                    else if (thisDOW == 2) fromDOW = 6;
                    else if (thisDOW == 1) fromDOW = 5;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW - 3;
                }
            } else if (fromDOW == THIS) {
                fromW = timeTableCore.getThisWeekOfTerm();
                fromDOW = thisDOW;
            } else if (fromDOW == NEXT) {
                if (thisDOW == 7) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 1;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW + 1;
                }
            } else if (fromDOW == T_NEXT) {
                if (thisDOW == 6) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 1;
                } else if (thisDOW == 7) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 2;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW + 2;
                }
            } else if (fromDOW == TT_NEXT) {
                if (thisDOW == 5) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 1;
                } else if (thisDOW == 6) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 2;
                } else if (thisDOW == 7) {
                    fromW = timeTableCore.getThisWeekOfTerm() + 1;
                    fromDOW = 3;
                } else {
                    fromW = timeTableCore.getThisWeekOfTerm();
                    fromDOW = thisDOW + 3;
                }
            }
        }
        if (toW == -1) {
            if (toDOW == BEFORE) {
                if (thisDOW < 2) {
                    toW = timeTableCore.getThisWeekOfTerm() - 1;
                    toDOW = 7;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW - 1;
                }
            } else if (toDOW == T_BEFORE) {
                if (thisDOW < 3) {
                    toW = timeTableCore.getThisWeekOfTerm() - 1;
                    if (thisDOW == 2) toDOW = 7;
                    else if (thisDOW == 1) toDOW = 6;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW - 2;
                }
            } else if (toDOW == TT_BEFORE) {
                if (thisDOW < 4) {
                    toW = timeTableCore.getThisWeekOfTerm() - 1;
                    if (thisDOW == 3) toDOW = 7;
                    else if (thisDOW == 2) toDOW = 6;
                    else if (thisDOW == 1) toDOW = 5;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW - 3;
                }
            } else if (toDOW == THIS) {
                toW = timeTableCore.getThisWeekOfTerm();
                toDOW = thisDOW;
            } else if (toDOW == NEXT) {
                if (thisDOW == 7) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 1;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW + 1;
                }
            } else if (toDOW == T_NEXT) {
                if (thisDOW == 6) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 1;
                } else if (thisDOW == 7) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 2;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW + 2;
                }
            } else if (toDOW == TT_NEXT) {
                if (thisDOW == 5) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 1;
                } else if (thisDOW == 6) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 2;
                } else if (thisDOW == 7) {
                    toW = timeTableCore.getThisWeekOfTerm() + 1;
                    toDOW = 3;
                } else {
                    toW = timeTableCore.getThisWeekOfTerm();
                    toDOW = thisDOW + 3;
                }
            }
        }


        if (toDOW == -1 || fromDOW == -1) {
            if (fromDOW != -1) toDOW = fromDOW;
            else if (fromW == -1 && toW == -1) {
                fromDOW = thisDOW;
                toDOW = fromDOW;
            } else {
                fromDOW = 1;
                toDOW = 7;
            }
        }
        if (fromW == -1 || toW == -1) {
            if (fromW == toW) toW = fromW = timeTableCore.isThisTerm() ? timeTableCore.getThisWeekOfTerm() : 1;
            else if (fromW == -1) fromW = timeTableCore.isThisTerm() ? timeTableCore.getThisWeekOfTerm() : toW;
            else if (toW == -1) toW = fromW;
        }
        boolean wholeday = false;
        if (fromT.hour == -1) {
            wholeday = true;
        }
        if (toT.hour == -1) {
            wholeday = true;
        }
        if (toW > timeTableCore.getCurrentCurriculum().getTotalWeeks())
            toW = timeTableCore.getCurrentCurriculum().getTotalWeeks();
        toW = (toW > timeTableCore.getCurrentCurriculum().getTotalWeeks()) ? timeTableCore.getCurrentCurriculum().getTotalWeeks() : toW;
        System.out.println("解析出的待添加DDL为：name=" + name + "fW=" + fromW + ",fDOW=" + fromDOW + ",fT=" + fromT.tellTime() + ",tW=" + toW + ",tDOW=" + toDOW + ",tT=" + toT.tellTime());
        return new EventItem(null, timeTableCore.getCurrentCurriculum().getCurriculumCode(), TIMETABLE_EVENT_TYPE_REMIND, name, "提醒", "无注释", "无注释", fromT, fromT, fromW, fromDOW, wholeday);
    }


    class ChatBotIteractTask extends AsyncTask<String, Integer, JsonObject> {

        String message;
        WeakReference<Activity> activity;

        ChatBotIteractTask(String message, Activity a) {
            this.message = message;
            activity = new WeakReference(a);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            animationView.cancelAnimation();
//            animationView.setColorFilter(getColorPrimary());
//            animationView.setAnimation("hita_animation/hita_thinking.json");
//            animationView.playAnimation();
        }

        @Override
        protected JsonObject doInBackground(String... strings) {
            if (chatbotA.simpleJudge(message)) {
                return chatbotA.Interact(message);
            } else {
                State = STATE_NORMAL;
                BmobQuery<ChatMessage> cmb = new BmobQuery<>();
                String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
                Pattern p = Pattern.compile(regEx);
                Matcher m = p.matcher(message);
                final String message = m.replaceAll("").trim();
                if (message.length() <= 3) {
                    cmb.addWhereEqualTo("queryText", message);
                } else if (message.length() <= 5) {
                    cmb.addWhereContains("queryText", message);
                }
                // Log.e("where:",chatMessageBmobQuery.getWhere().toString());
                //chatMessageBmobQuery.addWhereEqualTo("queryText",message);
                //Log.e("message:",message);
                List<ChatMessage> dbRespond = cmb.findObjectsSync(ChatMessage.class);
                if (dbRespond != null && dbRespond.size() > 0) {
                    com.google.gson.JsonParser jp = new com.google.gson.JsonParser();
                    String[] answers = dbRespond.get(0).getAnswer().split("\\$\\$");
                    JsonObject jo;
                    if (answers.length > 1) {
                        int index = new Random(System.currentTimeMillis()).nextInt(answers.length);
                        jo = jp.parse(answers[index]).getAsJsonObject();
                    } else {
                        jo = jp.parse(dbRespond.get(0).getAnswer()).getAsJsonObject();
                    }
                    return jo;
                } else {
                    if (defaultSP.getBoolean("ChatBot_useTulin", true)) {
                        JsonObject jo = ChatBotB.InteractTulin(message);
                        if (jo.get("message_show").toString().contains("请求次数"))
                            return ChatBotB.InteractQ(message);
                        else return jo;
                    } else {
                        return ChatBotB.InteractQ(message);
                    }
                }


            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(JsonObject s) {
            super.onPostExecute(s);
//            animationView.setColorFilter(getColorPrimary());
//            animationView.setAnimation("hita_animation/hita_normal.json");
//            animationView.playAnimation();
            if (activity.get() != null && (!activity.get().isDestroyed())) {
                try {
                    getReply(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    class ProcessReplyMessageTask extends AsyncTask {
        JsonObject msg;
        ChatBotMessageItem messagge;
        String textOnShow, textToRead;

        public ProcessReplyMessageTask(JsonObject msg) {
            this.msg = msg;
            messagge = new ChatBotMessageItem(VIEW_TYPE_LEFT, "");
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            if (msg.has("hint")) {
                messagge.setHint(msg.get("hint").getAsString());
            }
            if (msg.has("function")) {
                if (msg.get("function").getAsString().equals("search_event_ww")) {
                    List<EventItem> courseList;
                    final int tag = msg.get("tag").getAsInt();
                    if (!timeTableCore.isDataAvailable()) {
                        textOnShow = "请先导入数据或选择当前日程表！";
                    } else if (!timeTableCore.isThisTerm()) {
                        textOnShow = "别急着问啊，这学期还没开始";
                    } else {
                        courseList = propcessSerchEvents(msg);
                        if (courseList == null || courseList.size() <= 0) {
                            String textTemp_onlyOne = "东西";
                            switch (tag) {
                                case ChatBotA.FUN_SEARCH_EVENT_ALL:
                                    textTemp_onlyOne = "事件";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                                    textTemp_onlyOne = "课程";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                                    textTemp_onlyOne = "规划";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                                    textTemp_onlyOne = "提醒";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                                    textTemp_onlyOne = "考试";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_DDL:
                                    textTemp_onlyOne = "DDL";
                                    break;
                            }
                            textOnShow = "没有" + textTemp_onlyOne + "哦！";
                        } else {
                            if (courseList.size() > 1) State = STATE_SEARCH_COURSE_LIST;
                            else if (courseList.size() == 1) State = STATE_SEARCH_COURSE_SINGLE;
                            StateEventList = courseList;
                            Collections.sort(courseList);
                            if (courseList.size() == 1) {
                                String textTemp_onlyOne = "件事";
                                switch (tag) {
                                    case ChatBotA.FUN_SEARCH_EVENT_ALL:
                                        textTemp_onlyOne = "件事";
                                        break;
                                    case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                                        textTemp_onlyOne = "节课";
                                        break;
                                    case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                                        textTemp_onlyOne = "件事";
                                        break;
                                    case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                                        textTemp_onlyOne = "条提醒";
                                        break;
                                    case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                                        textTemp_onlyOne = "场考试";
                                        break;
                                    case ChatBotA.FUN_SEARCH_EVENT_DDL:
                                        textTemp_onlyOne = "个DDL";
                                        break;
                                }
                                textToRead = "只有一" + textTemp_onlyOne + "，为" + "周" + courseList.get(0).DOW + " " + courseList.get(0).startTime.tellTime() + " 的" + courseList.get(0).mainName;
//                                textOnShow = "只有这"+textTemp_onlyOne+":";
                                textOnShow = "就是他啦！";
//                                textToRead = "就是他啦";
                            } else if (courseList.size() <= 3) {
                                String resultText = "分别是：";
                                for (EventItem cs : courseList) {
                                    resultText += "\n" + "周" + cs.DOW + " " + cs.startTime.tellTime() + " 的" + cs.mainName;
                                }
                                textOnShow = "分别是";
                                textToRead = resultText;
                            } else {
                                textOnShow = ("共查找到如下" + courseList.size() + "个事件:");
                            }
                            messagge.setCourseList(courseList);
                        }
                    }

                } else if (msg.get("function").getAsString().equals("search_event_nextone")) {

                    final int tag = msg.get("tag").getAsInt();
                    if (!timeTableCore.isDataAvailable()) {
                        textOnShow = "请先导入数据或选择当前日程表！";
                    } else if (!timeTableCore.isThisTerm()) {
                        textOnShow = "别急着问啊，这学期还没开始";
                    } else {
                        EventItem nextevent = null;
                        Calendar c = Calendar.getInstance();
                        c.set(Calendar.HOUR_OF_DAY, 23);
                        c.set(Calendar.MINUTE, 59);
                        List<EventItem> tempList = timeTableCore.getEventFrom(now, c, -1);
                        if (tempList == null || tempList.size() == 0) nextevent = null;
                        else {
                            Log.e("!!", tempList.toString());
                            for (EventItem ei : tempList) {
                                Log.e("!!", ei.toString());
                                if (ei.startTime.compareTo(new HTime(now)) < 0) continue;
                                int eventTypeFilter = -99;
                                switch (tag) {
                                    case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                                        eventTypeFilter = TIMETABLE_EVENT_TYPE_COURSE;
                                        break;
                                    case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                                        eventTypeFilter = TIMETABLE_EVENT_TYPE_ARRANGEMENT;
                                        break;
                                    case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                                        eventTypeFilter = TIMETABLE_EVENT_TYPE_REMIND;
                                        break;
                                    case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                                        eventTypeFilter = TIMETABLE_EVENT_TYPE_EXAM;
                                        break;
                                    case ChatBotA.FUN_SEARCH_EVENT_DDL:
                                        eventTypeFilter = TIMETABLE_EVENT_TYPE_DEADLINE;
                                        break;
                                }
                                if (eventTypeFilter != -99) {
                                    if (ei.eventType != eventTypeFilter) continue;
                                }
                                nextevent = ei;
                                break;
                            }
                        }
                        if (nextevent == null) {
                            String textTemp_onlyOne = "东西";
                            switch (tag) {
                                case ChatBotA.FUN_SEARCH_EVENT_ALL:
                                    textTemp_onlyOne = "事件";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                                    textTemp_onlyOne = "课程";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                                    textTemp_onlyOne = "规划";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                                    textTemp_onlyOne = "提醒";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                                    textTemp_onlyOne = "考试";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_DDL:
                                    textTemp_onlyOne = "DDL";
                                    break;
                            }
                            textOnShow = "接下来没有" + textTemp_onlyOne + "了哟！";
                        } else {
                            State = STATE_SEARCH_COURSE_SINGLE;
                            String textTemp_onlyOne = "件事";
                            switch (tag) {
                                case ChatBotA.FUN_SEARCH_EVENT_ALL:
                                    textTemp_onlyOne = "件事";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_COURSE:
                                    textTemp_onlyOne = "节课";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_ARRANGE:
                                    textTemp_onlyOne = "件事";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_REMIND:
                                    textTemp_onlyOne = "条提醒";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_EXAM:
                                    textTemp_onlyOne = "场考试";
                                    break;
                                case ChatBotA.FUN_SEARCH_EVENT_DDL:
                                    textTemp_onlyOne = "个DDL";
                                    break;
                            }
                            textToRead = "下一" + textTemp_onlyOne + "为:" + nextevent.startTime.tellTime() + " 的" + nextevent.mainName;
                            textOnShow = "下一" + textTemp_onlyOne + "为:";
                            List<EventItem> courseList = new ArrayList<>();
                            courseList.add(nextevent);
                            messagge.setCourseList(courseList);
                        }
                    }

                } else if (msg.get("function").getAsString().equals("add_event_remind")) {
                    EventItem ei = propcessAddRemind(msg);
                    if (TextUtils.isEmpty(ei.mainName)) {
                        textOnShow = "给我个提醒的名字鸭";
                        textToRead = "给我个提醒的名字鸭";
                    } else {
                        timeTableCore.addEvent(ei);
                        List<EventItem> add = new ArrayList<>();
                        add.add(ei);
                        messagge.setCourseList(add);
                        textOnShow = "已添加提醒：";
                        textToRead = "好的，提醒你" + ei.mainName;
                    }
                } else if (msg.get("function").getAsString().equals("search_task")) {
                    List<Task> tl = timeTableCore.getUnfinishedTasks();
                    messagge.setTaskList(tl);
                    if (tl.size() > 0) {
                        textOnShow = "还有如下" + tl.size() + "个待办任务";
                        textToRead = "你还有" + tl.size() + "个待办任务";
                    } else {
                        textOnShow = "目前没有任务！";
                        textToRead = textOnShow;
                    }

                } else if (msg.get("function").getAsString().equals("intent_explore")) {
                    textOnShow = "好的，启动探索模式";
                    textToRead = "好的，启动探索模式";
                    Intent i = new Intent(ActivityChatbot.this, ActivityExplore.class);
                    startActivity(i);
                } else if (msg.get("function").getAsString().equals("intent_canteen")) {
                    textOnShow = "好的，发现校内服务";
                    textToRead = "好的，发现校内服务";
                    Intent i = new Intent(ActivityChatbot.this, ActivityLeaderBoard.class);
                    startActivity(i);
                } else if (msg.get("function").getAsString().equals("intent_jwts")) {
                    textOnShow = "好的，进入教务系统";
                    textToRead = "好的，进入教务系统";
                    ActivityUtils.startJWTSActivity(ActivityChatbot.this);
                } else if (msg.get("function").getAsString().equals("intent_laf")) {
                    textOnShow = "好的，进入失物招领";
                    textToRead = "好的，进入失物招领";
                    Intent i = new Intent(ActivityChatbot.this, ActivityLostAndFound.class);
                    startActivity(i);
                } else if (msg.get("function").getAsString().equals("intent_infos")) {
                    textOnShow = "好的，进入信息窗口";
                    textToRead = "好的，进入信息窗口";
                    Intent i = new Intent(ActivityChatbot.this, ActivityNews.class);
                    startActivity(i);
                } else if (msg.get("function").getAsString().equals("search_location")) {
                    textOnShow = "好的，进入校内地点页";
                    if (msg.get("location_objectId") != null) {
                        String id = msg.get("location_objectId").getAsString();
                        ActivityUtils.startLocationActivity_objectId(ActivityChatbot.this, id);
                    } else if (msg.get("location_name") != null) {
                        String name = msg.get("location_name").getAsString();
                        ActivityUtils.startLocationActivity_name(ActivityChatbot.this, name);
                    } else {
                        textOnShow = "抱歉，处理地点信息错误";
                    }
                }else if(msg.get("function").getAsString().equals("query_subject_number_of_subject")){
                   String type = msg.get("type").getAsString();
                   List<Subject> result = null;
                   String desc = "课";
                   if(type!=null){
                       if(type.equals("exam")){
                           result = timeTableCore.getCurrentCurriculum().getSubjects_Exam();
                           //Log.e("exam---", String.valueOf(result));
                           desc = "考试课";
                       }else if(type.equals("mooc")){
                           result = timeTableCore.getCurrentCurriculum().getSubjects_Mooc();
                           desc = "慕课";
                       }else if(type.equals("no_exam")){
                           result = timeTableCore.getCurrentCurriculum().getSubjects_No_Exam();
                           desc = "考查课";
                       }else if(type.equals("comp")){
                           result = timeTableCore.getCurrentCurriculum().getSubjects_Comp();
                           desc = "必修课";
                       }else if(type.equals("alt")){
                           result = timeTableCore.getCurrentCurriculum().getSubjects_Alt();
                           desc = "选秀课";
                       }else if(type.equals("wtv")){
                           result = timeTableCore.getCurrentCurriculum().getSubjects_WTV();
                           desc = "任选课";
                       }else result = timeTableCore.getCurrentCurriculum().getSubjects();
                   }
                    if(result!=null&&result.size()>0){
                        messagge.setSubjectList(result);
                        textOnShow = "这学期共有"+result.size()+"门"+desc;
                    }else{
                        textOnShow = "没有~";
                    }
                }
                else if (msg.get("function").getAsString().equals("say_my_name")) {
                    if (CurrentUser == null) {
                        textOnShow = "你好像还没有登录的亚子，我怎么知道你是谁啊";
                    } else {
                        if (TextUtils.isEmpty(CurrentUser.getNick())) {
                            if (TextUtils.isEmpty(CurrentUser.getRealname()))
                                textOnShow = "你说你没有设置昵称也没有绑定学号登教务，我怎么寄到你叫什么嘛！";
                            else textOnShow = "不介意的话，我就叫你" + CurrentUser.getRealname() + "了";
                        } else textOnShow = "出于礼貌，我就不直呼你的大名了，" + CurrentUser.getNick();
                    }
                    textToRead = textOnShow;
                } else if (msg.get("function").getAsString().equals("search_people")) {
                    if (msg.has("name")) {
                        BmobQuery bq = new BmobQuery();
                        bq.addWhereStartsWith("name",msg.get("name").getAsString());
                        List result = bq.findObjectsSync(Teacher.class);
                        if(result==null||result.size()==0){
                            textOnShow = "没有找到TA的信息(ㆁωㆁ*)";
                            textToRead = "没有找到她的信息";
                        }else{
                            messagge.setTeacherList(result);
                            if(result.size()>1){
                                textOnShow = "共找到"+result.size()+"个结果";
                            }else{
                                textOnShow = "你要找的是不是TA？";
                                textToRead = "你要找的是不是他？";
                            }
                        }
                    } else {
                        textOnShow = "给我个有效的名字啊";
                    }
                }
                if (msg.get("function").getAsString().equals("search_event_context2_classroom")) {
                    if (State == STATE_SEARCH_COURSE_SINGLE) {
                        textOnShow = "就在" + StateEventList.get(0).tag2 + "啊！";
                        State = STATE_NORMAL;
                    }
                }

            }
            if (msg.has("message_show"))
                textOnShow = String.valueOf(msg.get("message_show").getAsString());
            if (msg.has("message_read"))
                textToRead = String.valueOf(msg.get("message_read").getAsString());
            if (msg.has("image_url")) messagge.setImageURI(msg.get("image_url").getAsString());
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            addMessageView(messagge, textOnShow, textToRead);
        }
    }

    class OnToolbarMenuClickListener implements Toolbar.OnMenuItemClickListener {

        BottomSheetDialog ad;
        EditText query, show;
        android.widget.Button cancel, upload;

        OnToolbarMenuClickListener() {
            View v = getLayoutInflater().inflate(R.layout.dialog_chatbot_builddb, null);
            query = v.findViewById(R.id.query);
            show = v.findViewById(R.id.show);
            cancel = v.findViewById(R.id.cancel);
            upload = v.findViewById(R.id.upload);
            ad = new BottomSheetDialog(ActivityChatbot.this);
            ad.setContentView(v);
            try {
                // hack bg color of the BottomSheetDialog
                ViewGroup parent = (ViewGroup) v.getParent();
                parent.setBackgroundResource(android.R.color.transparent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TextUtils.isEmpty(query.getText()) || TextUtils.isEmpty(show.getText())) {
                        Toast.makeText(ActivityChatbot.this, "请补全信息", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        new uploadMessageTask(query.getText().toString(),show.getText().toString(),ad).executeOnExecutor(HITAApplication.TPE);
                    }
                }
            });
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ad.dismiss();
                }
            });
            ad.setCancelable(false);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            ad.show();
            return true;
        }
    }

    class uploadMessageTask extends AsyncTask{
        String query,show;
        BottomSheetDialog ad;

        public uploadMessageTask(String query, String show, BottomSheetDialog ad) {
            this.query = query;
            this.show = show;
            this.ad = ad;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            try {
                for (String q : query.split("@@")) {
                    ChatMessage cm = new ChatMessage();
                    cm.setQueryText(q);
                    List<String> queryArr = new ArrayList<>();
                    for(Term t:TextTools.NaiveSegmentation(query)){
                        queryArr.add(t.getContent());
                    }
                    cm.setQueryArray(queryArr);
                    if (show.contains("@@")) {
                        StringBuilder sb = new StringBuilder();
                        for (String s : show.split("@@")) {
                            JsonObject jo = new JsonObject();
                            jo.addProperty("message_show", s);
                            sb.append(jo.toString()).append("$$");
                        }
                        cm.setAnswer(sb.toString());
                    } else {
                        JsonObject jo = new JsonObject();
                        jo.addProperty("message_show", show);
                        cm.setAnswer(jo.toString());
                    }
                    cm.setTag("user_uploaded");
                    cm.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            Log.e("done",e==null?"success":e.toString());
                        }
                    });
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if ((boolean)o) {
                Toast.makeText(ActivityChatbot.this, "上传成功！", Toast.LENGTH_SHORT).show();
                ad.dismiss();
            } else
                Toast.makeText(ActivityChatbot.this, "上传失败！" , Toast.LENGTH_SHORT).show();

        }
    }

}

