package com.stupidtree.hita.fragments.search;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.stupidtree.hita.R;
import com.stupidtree.hita.online.SearchException;
import com.stupidtree.hita.online.SearchTeacherCore;
import com.stupidtree.hita.online.Teacher;
import com.stupidtree.hita.util.ActivityUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;

import static com.stupidtree.hita.HITAApplication.TPE;

public class FragmentSearchResult_teacher extends FragmentSearchResult{
    private TeacherSearchAdapter adapter;
    private List<Object> listRes;
    private SearchTeacherCore searchTeacherCore;
    public FragmentSearchResult_teacher(String title) {
        super(title);
    }
    SearchTask pageTask;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_result_1,container,false);
        initViews(v);
        searchTeacherCore = new SearchTeacherCore();
        return v;
    }

    private void initViews(View v) {
        listRes = new ArrayList<>();
        adapter = new TeacherSearchAdapter(listRes);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void OnClick(View view, int position) {

            }

            @Override
            public void OnClickTransition(View view, int position, View transition) {
                try {
                    if(listRes.get(position) instanceof SparseArray){
                        SparseArray<String> sa = (SparseArray<String>) listRes.get(position);
                        ActivityUtils.startOfficialTeacherActivity_transition(getActivity(),
                                sa.get(SearchTeacherCore.ID),
                                sa.get(SearchTeacherCore.URL),
                                sa.get(SearchTeacherCore.NAME),
                                transition
                        );
                    }else if(listRes.get(position) instanceof Teacher){
                        ActivityUtils.startTeacherActivity(getActivity(), (Teacher) listRes.get(position));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void OnLongClick(View view, int position) {

            }
        });
        initList(v,adapter);
    }

    @Override
    public void Search(boolean hide) {
        super.Search(hide);
        if(TextUtils.isEmpty(searchText)){
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        if(pageTask!=null&&!pageTask.isCancelled()){
            pageTask.cancel(true);
        }
        pageTask = new SearchTask(searchText,hide);
        pageTask.executeOnExecutor(TPE);

    }

//    @Override
//    public void swipeRefresh() {
//        Search();
//    }

    @Override
    protected void stopTasks() {
        if(pageTask!=null&&!pageTask.isCancelled()){
            pageTask.cancel(true);
            pageTask = null;
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Search();
//    }

    @Override
    public void Refresh() {
        if(!searchText.equals(searchTeacherCore.getLastKeyword()))Search(true);
        else swipeRefreshLayout.setRefreshing(false);
    }

    class SearchTask extends SearchRefreshTask{


        public SearchTask(String keyword, boolean hideContent) {
            super(keyword, hideContent);
        }


        @Override
        protected Object doInBackground(Object[] objects) {
            listRes.clear();
            List<SparseArray<String>> res1 = null;
            List<Teacher> res2 = null;
            try {
                res1 = searchTeacherCore.searchForResult(keyword);
            } catch (SearchException e) {
               return e;
            }
            listRes.addAll(res1);
            try {
                BmobQuery<Teacher> bq = new BmobQuery<>();
                bq.addWhereEqualTo("name",keyword);
                res2 = bq.findObjectsSync(Teacher.class);
                listRes.addAll(res2);
            }catch (Exception e){
                e.printStackTrace();
            }

            return listRes.size();
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            adapter.notifyDataSetChanged();
            list.scheduleLayoutAnimation();
            if(o instanceof SearchException){
                result.setText(((SearchException) o).getMessage());
            }else if(listRes.size()>0){
                result.setText(String.format(getString(R.string.teacher_total_searched),listRes.size()));
            }else{
                result.setText(R.string.nothing_found);
            }
        }
    }


    class TeacherSearchAdapter extends RecyclerView.Adapter<TeacherSearchAdapter.TeacherSearchViewHoler> {
        List<Object> mBeans;
        OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public TeacherSearchAdapter(List<Object> mBeans) {
            this.mBeans = mBeans;
        }

        @NonNull
        @Override
        public TeacherSearchViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layoutId =R.layout.dynamic_teacher_search_result_item;
            View v = getLayoutInflater().inflate(layoutId,parent,false);
            return new TeacherSearchViewHoler(v,viewType);
        }


        @SuppressLint("CheckResult")
        @Override
        public void onBindViewHolder(@NonNull final TeacherSearchViewHoler holder, final int position) {
            if(mBeans.get(position) instanceof SparseArray){
                SparseArray<String> tsa = (SparseArray) mBeans.get(position);
                holder.title.setText(tsa.get(0));
                holder.type.setVisibility(View.GONE);
                holder.department.setText(tsa.get(SearchTeacherCore.DEPARTMENT));
                Glide.with(getContext()).load("http://faculty.hitsz.edu.cn/file/showHP.do?d="+
                        tsa.get(SearchTeacherCore.ID)+"&&w=200&&h=200&&prevfix=200-")
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_account_activated)
                        .into(holder.picture);
                if(onItemClickListener!=null)holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.OnClickTransition(view,position,holder.picture);
                    }
                });
            }else if(mBeans.get(position) instanceof Teacher){
                Teacher t = (Teacher) mBeans.get(position);
                holder.title.setText(t.getName());
                holder.department.setText(t.getSchool());
                holder.type.setVisibility(View.VISIBLE);
                holder.type.setText(R.string.teacher_temp_label);
                Glide.with(getContext()).load(t.getPhotoLink())
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_account_activated)
                        .into(holder.picture);
                if(onItemClickListener!=null)holder.card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onItemClickListener.OnClickTransition(view,position,holder.picture);
                    }
                });
            }

        }


        @Override
        public int getItemCount() {
            return mBeans.size();
        }



        class TeacherSearchViewHoler extends RecyclerView.ViewHolder{
            TextView title;
            TextView department;
            ImageView picture;
            TextView type;
            CardView card;
            int viewType;
            public TeacherSearchViewHoler(@NonNull View itemView, int viewType) {
                super(itemView);
                this.viewType = viewType;
                card = itemView.findViewById(R.id.card);
                title = itemView.findViewById(R.id.name);
                type = itemView.findViewById(R.id.type);
                department = itemView.findViewById(R.id.department);
                picture = itemView.findViewById(R.id.picture);
            }
        }

    }
}
