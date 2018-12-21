package com.cqupt.ui.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cqupt.R;
import com.cqupt.model.Lesson;
import com.cqupt.model.ResourceType;
import com.cqupt.net.WebService;
import com.cqupt.util.CommonListViewAdapter;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Alan on 2016/5/19.
 */
public class OnlineLearningActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener
,View.OnClickListener{

    private Spinner mSpinner;
    private ImageButton mSearchButton;
    private TextView mReturnView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private MyRecyclerAdapter mRecyclerAdapter;
    private ProgressBar mProgressBar;

    private ArrayList<ResourceType> mSpinnerDatas = new ArrayList<>();
    private ArrayList<Lesson> mRecyclerDatas = new ArrayList<>();
    private WebService mWebService = new WebService();
    private int mSelectedLessonTypeIndex;
    private boolean isDataRefreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_learning);
        initViews();
        loadDatas();
        setListener();
    }

    @Override
    public void onRefresh() {

        if(!isDataRefreshing){
            new GetLessonListThread().execute(mSpinnerDatas.get(mSelectedLessonTypeIndex).getResType());
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.returnView : onBackPressed();break;
        }
    }

    private void initViews(){

        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSearchButton = (ImageButton) findViewById(R.id.searchView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mReturnView = (TextView) findViewById(R.id.returnView);

    }

    private void loadDatas(){

        new GetLessonTypeThread().execute();
        new GetLessonListThread().execute(0);//get all type as default

    }

    private void setListener(){

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(mSelectedLessonTypeIndex != position){
                    mSelectedLessonTypeIndex = position;
                    int typeId = mSpinnerDatas.get(mSelectedLessonTypeIndex).getResType();
                    new GetLessonListThread().execute(typeId);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mReturnView.setOnClickListener(this);

    }

    class MySpinnerAdapter extends CommonListViewAdapter<ResourceType>{

        public MySpinnerAdapter(List<ResourceType> data, Context context, int layoutResId) {
            super(data, context, layoutResId);
        }

        @Override
        protected void inflate(ViewHolder viewHolder, ResourceType item, int position) {
            TextView tv = (TextView) viewHolder.getView(R.id.text);
            tv.setText(item.getResName());
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = LayoutInflater.from(OnlineLearningActivity.this)
                        .inflate(R.layout.item_spinner_dropdown,parent,false);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.textView);
            textView.setText(mSpinnerDatas.get(position).getResName());

            RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.border);
            View indicatorView = convertView.findViewById(R.id.indicator);

            //当前选中的需要显示指示器 背景加深
            if(mSelectedLessonTypeIndex == position){
                relativeLayout.setBackgroundResource(R.color.spinner_dropdown_selected_bc);
                indicatorView.setVisibility(View.VISIBLE);
            }else{
                relativeLayout.setBackgroundResource(R.color.spinner_dropdown_default_bc);
                indicatorView.setVisibility(View.GONE);
            }

            return convertView;

        }
    }



    class GetLessonTypeThread extends AsyncTask<Void,Void,Integer>{

        @Override
        protected Integer doInBackground(Void... params) {

            SoapObject result = mWebService.CallWebService("getLessonType",null);

            if(result == null){
                return WebService.CONNECTION_TIMEOUT;
            }

            ResourceType type = new ResourceType();
            type.setResName("全部");
            type.setResType(0);
            mSpinnerDatas.add(type);

            mSpinnerDatas.addAll(XMLParser.parseLessonType(result));
            return WebService.SUCCESS;
        }

        @Override
        protected void onPostExecute(Integer result) {

            if(result == WebService.CONNECTION_TIMEOUT){
                ShowToastUtil.showConnectionTimeOutToast(OnlineLearningActivity.this);
            }else{

                if(mSpinnerDatas != null){
                    mSpinner.setAdapter(new MySpinnerAdapter(mSpinnerDatas,OnlineLearningActivity.this,R.layout.layout_empty_textview));
                }

            }

        }
    }

    class GetLessonListThread extends AsyncTask<Integer,Void,Integer>{

        private ArrayList<Lesson> tempDatas;

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
            isDataRefreshing = true;
        }

        @Override
        protected Integer doInBackground(Integer... params) {

            HashMap<String,String> p = new HashMap<>();
            p.put("typeId",params[0].toString());
            SoapObject result = mWebService.CallWebService("getLessonList",p);

            if(result == null){
                return WebService.CONNECTION_TIMEOUT;
            }

            tempDatas = (ArrayList<Lesson>) XMLParser.parseLessonList(result);

            return WebService.SUCCESS;

        }

        @Override
        protected void onPostExecute(Integer result) {

            if(result == WebService.CONNECTION_TIMEOUT){
                ShowToastUtil.showConnectionTimeOutToast(OnlineLearningActivity.this);
            }else{

                mRecyclerDatas.clear();

                if(tempDatas != null){
                    mRecyclerDatas.addAll(tempDatas);
                }

                if(mRecyclerAdapter == null){
                    mRecyclerAdapter = new MyRecyclerAdapter();
                    mRecyclerView.setAdapter(mRecyclerAdapter);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(OnlineLearningActivity.this));
                    mRecyclerView.addItemDecoration(new SimpleItemDecoration());
                }

                mRecyclerAdapter.notifyDataSetChanged();

            }

            mProgressBar.setVisibility(View.GONE);
            isDataRefreshing = false;
            mSwipeRefreshLayout.setRefreshing(false);

        }

    }

    class SimpleItemDecoration extends RecyclerView.ItemDecoration{
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(0,0,0,10);
        }
    }

    class MyRecyclerAdapter extends RecyclerView.Adapter<MyViewHolder>{

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(OnlineLearningActivity.this)
                    .inflate(R.layout.item_course_list,parent,false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            final Lesson lesson = mRecyclerDatas.get(position);

            if(lesson != null){

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = OnlineLearningDetailActivity.getStartActivityIntent(OnlineLearningActivity.this,lesson);
                        startActivity(intent);
                    }
                });

                Glide.with(OnlineLearningActivity.this)
                        .load(lesson.getImageUri())
                        .into(holder.image);

                holder.title.setText(lesson.getTitle() == null ? ""  : lesson.getTitle());
                holder.object.setText(lesson.getGoal() == null ? "" : lesson.getGoal());
                holder.course.setText(lesson.getHours() == null ? "" : lesson.getHours());
            }


        }

        @Override
        public int getItemCount() {

            return mRecyclerDatas.size();

        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        private ImageView image;
        private TextView title;
        private TextView object;
        private TextView course;

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            object = (TextView) itemView.findViewById(R.id.target_value);
            course = (TextView) itemView.findViewById(R.id.course_time_value);
        }
    }


}
