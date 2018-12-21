package com.cqupt.ui.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.net.WebService;
import com.cqupt.ui.student.TaskDetailActivity;
import com.cqupt.ui.teacher.TeacherTaskDetailActivity;
import com.cqupt.util.XMLParser;
import com.cqupt.view.RoundProgressBar;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends Activity{
	
	public final static int TYPE_PLAN = 1;
	public final static int TYPE_STUDENT_TASK = 2;
	public final static int TYPE_TEACHER_TASK = 3;
	
	private MyApplication application;
	
	private EditText searchView;
	private TextView returnView;
	private ListView listView;
	private ProgressBar progressBar;
	private TaskListAdapter adapter;
	
	private ArrayList<HashMap<String,String>> data;	
	private MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(this);
	private int type;
	private String keyword;

	
	//-------------------------��д����-------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search);
		
		findView();
		loadData();
		setListener();	
		
	}
	
	@Override
	protected void onStop() {

		super.onStop();
		InputMethodManager m = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		m.hideSoftInputFromWindow(searchView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	//-------------------------------�Զ��巽��------------------------
	
	private void findView(){
		
		searchView = (EditText) findViewById(R.id.includeSearch);
		returnView = (TextView) findViewById(R.id.returnView);
		listView = (ListView) findViewById(R.id.listView1);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
	}
	
	private void loadData(){
		application = (MyApplication)getApplication();
		data = new ArrayList<HashMap<String,String>>();
		type = getIntent().getIntExtra("type", -1);
		adapter = new TaskListAdapter(this, data);
		listView.setAdapter(adapter);
	}
	
	private void setListener(){
		
		searchView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				keyword = s.toString();
				new GetListViewDataThread().execute(keyword);
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
	
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			
			}
		});

		returnView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent it = null;
				
				switch (type) {
				case TYPE_PLAN:
					TextView idView = (TextView) view.findViewById(R.id.planID);
					int iD = Integer.parseInt(idView.getText().toString());
					it = new Intent(SearchActivity.this,PlanDetailActivity.class);
					it.putExtra("planID",iD);
					break;

				case TYPE_STUDENT_TASK:
					idView = (TextView)view.findViewById(R.id.taskID);
					iD = Integer.parseInt(idView.getText().toString());
					it = new Intent(SearchActivity.this,TaskDetailActivity.class);
					it.putExtra("taskID",iD);
					break;
				case TYPE_TEACHER_TASK:
					it = new Intent(SearchActivity.this,TeacherTaskDetailActivity.class);
					it.putExtra("taskID", Integer.parseInt(data.get(position).get("taskID")));
					break;
				}
			
				startActivity(it);
				finish();
			}
		});
		
	}
	
	//-------------------------------�ڲ���----------------------------
	
	class GetListViewDataThread extends AsyncTask<String, Void, Integer>{
		
		private static final int SUCCESS = 1;
		private static final int FAIL = 2;
		
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(String... params) {
			
			String keyword = params[0];
			data.removeAll(data);
			
			if(TextUtils.isEmpty(keyword)){
				return SUCCESS;
			}else{
				if(type == TYPE_PLAN || type == TYPE_STUDENT_TASK){
					
					DBManager db = new DBManager(helper.getConnection());
					data.addAll(type == TYPE_PLAN ? 
							db.getPlanListWithCondition(application.getUserID(),keyword):
							db.getTaskListWithConditon(application.getUserID(),keyword));
					return SUCCESS;
					
				}else{
					
					WebService web = new WebService();
					HashMap<String, String> p = new HashMap<String, String>();
					p.put("userID", application.getUserID());
					p.put("keyword", keyword);
					
					SoapObject result = web.CallWebService("getTaskListWithCondition", p);
					
					if(result == null){
						return FAIL;
					}
					
					data.addAll(XMLParser.parseTeacherTaskListToHashMap(result));
					
					return SUCCESS;	
					
				}
			}

		}

		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			
			if(result == SUCCESS){
				adapter.notifyDataSetChanged();
			}else{
				Toast.makeText(SearchActivity.this, R.string.tip_connection_timeout, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	
	/**
	 *�����б�������
	 */
	class TaskListAdapter extends BaseAdapter{
		
		private ArrayList<HashMap<String,String>> data;
		private LayoutInflater inflater;
		
		public TaskListAdapter(Context context,ArrayList<HashMap<String,String>> data){
			this.data = data;
			inflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressLint("CutPasteId")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View myView = null;
			DBManager db = new DBManager(helper.getConnection());
			
			switch (type) {
			case TYPE_PLAN:
				myView = inflater.inflate(R.layout.layout_planlist, null);
				RoundProgressBar myProgress = (RoundProgressBar)myView.findViewById(R.id.myProgress);
				int max = Integer.parseInt(data.get(position).get("max"));
				//����ҵ���������,����ʾ������
				if(max == 0){
					myProgress.setVisibility(View.GONE);
				}else{
					myProgress.setMax(max);
					myProgress.setProgress(Integer.parseInt(data.get(position).get("process")));
				}

				TextView planIDView = (TextView)myView.findViewById(R.id.planID);
				TextView title = (TextView)myView.findViewById(R.id.title);
				TextView startDate = (TextView)myView.findViewById(R.id.startDate);
				TextView endDate = (TextView)myView.findViewById(R.id.endDate);
				CheckBox checkBox = (CheckBox) myView.findViewById(R.id.checkBox);
				
				checkBox.setVisibility(View.GONE);
				
				String condition = data.get(position).get("condition");//��ùؼ���
				String originTitle = data.get(position).get("title");//���ԭʼ����

				//�����к��йؼ���,�ؼ����ú�ɫ��ʾ
				if(originTitle.contains(condition)){
					int start = originTitle.indexOf(condition);//�ؼ�����ԭʼ�����еĿ�ʼ�±�
					SpannableString newTitle = new SpannableString(originTitle);
					newTitle.setSpan(new ForegroundColorSpan(Color.RED), start, 
							start+condition.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					title.setText(newTitle);
				}else{
					title.setText(originTitle);
				}
				
				planIDView.setText(data.get(position).get("planID"));
				startDate.setText(data.get(position).get("startDate"));
				endDate.setText(data.get(position).get("endDate"));
				
				break;

			case TYPE_STUDENT_TASK:
				int taskID = Integer.parseInt(data.get(position).get("taskID"));
				//û�����ֵ���ҵ��ʾ����
				if(!db.checkTaskScored(application.getUserID(),taskID)){
					myView = inflater.inflate(R.layout.layout_tasklist, null);
					myProgress = (RoundProgressBar)myView.findViewById(R.id.myProgress);
					max = Integer.parseInt(data.get(position).get("max"));
					//����ҵ���������,����ʾ������
					if(max == 0){
						myProgress.setVisibility(View.GONE);
					}else{
						myProgress.setProgress(Integer.parseInt(data.get(position)
								.get("process")));
						myProgress.setMax(max);
					}
				//�Ѿ����ֵ���ҵ��ʾ����
				}else{
					myView = inflater.inflate(R.layout.layout_tasklist_done, null);
					String score = db.getFeedBack(application.getUserID(),taskID).getScore();
					TextView scoreView = (TextView)myView.findViewById(R.id.score);
					scoreView.setText(score);
				}
				TextView taskIDView = (TextView)myView.findViewById(R.id.taskID);
				title = (TextView)myView.findViewById(R.id.title);
				startDate = (TextView)myView.findViewById(R.id.startDate);
				endDate = (TextView)myView.findViewById(R.id.endDate);
				TextView name = (TextView)myView.findViewById(R.id.name);
				
				condition = data.get(position).get("condition");//��ùؼ���
				originTitle = data.get(position).get("title");//���ԭʼ����
				String originName = data.get(position).get("teacherName");//ԭʼ����������
				//�����к��йؼ���,�ؼ����ú�ɫ��ʾ
				if(originTitle.contains(condition)){
					int start = originTitle.indexOf(condition);//�ؼ�����ԭʼ�����еĿ�ʼ�±�
					SpannableString newTitle = new SpannableString(originTitle);
					newTitle.setSpan(new ForegroundColorSpan(Color.RED), start, start+condition.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					title.setText(newTitle);
				}else{
					title.setText(originTitle);
				}
				//�����������к��йؼ���,�ؼ����ú�ɫ��ʾ
				if(originName.contains(condition)){
					int start = originName.indexOf(condition);//�ؼ����ڴ����������еĿ�ʼ�±�
					SpannableString newName = new SpannableString(originName);
					newName.setSpan(new ForegroundColorSpan(Color.RED), start, start+condition.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					name.setText(newName);
				}else{
					name.setText(originName);
				}
				taskIDView.setText(data.get(position).get("taskID"));
				startDate.setText(data.get(position).get("startDate"));
				endDate.setText(data.get(position).get("endDate"));
				break;
			case TYPE_TEACHER_TASK:
				myView = inflater.inflate(R.layout.layout_teacher_tasklist, parent,false);
				TextView sDateView = (TextView) myView.findViewById(R.id.startDate);
				TextView eDateView = (TextView) myView.findViewById(R.id.endDate);
				TextView titleView = (TextView) myView.findViewById(R.id.title);
				TextView stuCountView = (TextView) myView.findViewById(R.id.studentCount);
				TextView examCountView = (TextView) myView.findViewById(R.id.examCount);
				
				sDateView.setText(data.get(position).get("sDate"));
				eDateView.setText(data.get(position).get("eDate"));
				stuCountView.setText(data.get(position).get("stuCount"));
				examCountView.setText(data.get(position).get("examCount"));
				
				originTitle = data.get(position).get("title");
				
				//�Ա����еĹؼ��ֽ��б��
				if(originTitle.contains(keyword)){
					int start = originTitle.indexOf(keyword);//�ؼ�����ԭʼ�����еĿ�ʼ�±�
					SpannableString newTitle = new SpannableString(originTitle);
					newTitle.setSpan(new ForegroundColorSpan(Color.RED), start, 
							start+keyword.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					titleView.setText(newTitle);
				}else{
					titleView.setText(originTitle);
				}
				break;
			}
			
		
			return myView;
		}	
	}
	
	

}
