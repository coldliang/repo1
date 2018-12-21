package com.cqupt.ui.student;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.db.DBManager;
import com.cqupt.model.FeedBack;
import com.cqupt.model.Task;
import com.cqupt.net.WebService;
import com.cqupt.ui.common.SearchActivity;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.CheckTaskUpdateThread;
import com.cqupt.util.DeadlineCheck;
import com.cqupt.util.DownloadResourceType;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.util.SingleTaskUpdate;
import com.cqupt.util.SingleTaskUpdate.OnProcessChangeListener;
import com.cqupt.util.XMLParser;
import com.cqupt.view.RoundProgressBar;
import com.readystatesoftware.viewbadger.BadgeView;

import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TaskActivity extends IStudentMainActivity{

	/**
	 * ������ҵ��ť
	 */
	private Button downloadButton;
	/**
	 * ������ҵʱ�Ľ��ȿ�
	 */
	private ProgressDialog log;
	/**
	 * ��ʾ��ҵ�������ʱ��
	 */
	private TextView updateTimeView;
	
	private BadgeView downloadButtonBadge;
	private TextView historyView;
	
	private DBManager db = new DBManager(helper.getConnection());
	
	
	//--------------------------------------��д����---------------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tasklist);
		
		findView();
		loadData();//��������
		setListener();
    }
	
	
	@Override
	protected void onRestart(){
			
		super.onRestart();

        int newTaskCount = sp.getInt("newTaskCount",0);
		
		//���µ���ҵ
		if(newTaskCount > 0){
			downloadButtonBadge.setText(String.valueOf(newTaskCount));
			downloadButtonBadge.show();
		}
	}

	
	@Override
	public void onClick(View v) {
		
		super.onClick(v);
		switch(v.getId()){

		case R.id.download : 
			if(!CheckNetwork.isConnectingToInternet(TaskActivity.this)){
				Toast.makeText(this,R.string.tip_network_unavailable, Toast.LENGTH_LONG).show();
			}else{
				log = new ProgressDialog(TaskActivity.this);
				log.setCancelable(false);//���������ؼ��رոöԻ���
				log.setCanceledOnTouchOutside(false);//���������ⲿ����رոöԻ���
				log.setTitle(R.string.update_exercise_message);
				log.show();
				new UpdateDataTask().execute();
			}
			break;
		case R.id.history:
			Intent intent = new Intent(this,DownloadFinishedTaskActivity.class);
			startActivity(intent);
			break;
		}
	}
	
	@Override
	protected ArrayList<HashMap<String, String>> getRecentTabData() {
		return db.getRecentTask(application.getUserID());
	}


	@Override
	protected ArrayList<HashMap<String, String>> getDoingTabData() {		
		return db.getTaskList(application.getUserID(),3);
	}


	@Override
	protected ArrayList<HashMap<String, String>> getDoneTabData() {		
		return db.getTaskList(application.getUserID(),4);
	}
	
	@Override
	protected BaseAdapter getAdapter(){
		return new TaskListAdapter(this, data);
	}
	
	@Override
	protected void setListener(){
		
		super.setListener();
		downloadButton.setOnClickListener(this);	
		listView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				DBManager db = new DBManager(helper.getConnection());
				TextView taskIDView = (TextView)view.findViewById(R.id.taskID);
				int taskID = Integer.parseInt(taskIDView.getText().toString());
				//��¼���������ʱ��
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				String time = format.format(date);
				db.setRecentTask(application.getUserID(),taskID, time);
				
				Intent it = new Intent(TaskActivity.this,TaskDetailActivity.class);
				it.putExtra("taskID",taskID);
				startActivity(it);
			}
		});
		
		historyView.setOnClickListener(this);
		
	}

	@Override
    protected void findView(){
    	
		super.findView();
		downloadButton = (Button)findViewById(R.id.download);
		updateTimeView = (TextView)findViewById(R.id.updateTime);
		historyView = (TextView) findViewById(R.id.history);
	}

	@Override
	protected void loadData(){
		
		super.loadData();
		//wifi״̬���Զ������ҵ�Ƿ��и���
		if(CheckNetwork.isWifiAvailable(this)){
			new CheckTaskUpdateThread(this,application.getUserID()).start();
		}
		
		DBManager db = new DBManager(helper.getConnection());
		//��鵽����ҵ����ʱ�����ʾ
		String time = db.getExerciseDownloadTime(application.getUserID());
		if(!(time == null || time.length() == 0)){
			updateTimeView.setText(getResources().getString(R.string.exercise_uptate_time)
					+ db.getExerciseDownloadTime(application.getUserID()));
		}
		
		downloadButtonBadge = new BadgeView(this,downloadButton);
		int newTaskCount = sp.getInt("newTaskCount",0);
		
		//���µ���ҵ
		if(newTaskCount > 0){
			downloadButtonBadge.setText(String.valueOf(newTaskCount));
			downloadButtonBadge.show();
		}
		
	}
	
	@Override
	protected void onSearchBarClick() {
		
		Intent it = new Intent(this,SearchActivity.class);
		it.putExtra("type", SearchActivity.TYPE_STUDENT_TASK);
		startActivity(it);
		
	}
	
	//-------------------------------�ڲ���----------------------------------
	
	/**
	 *������ҵ��̨�߳�
	 */
	class UpdateDataTask extends AsyncTask<Void, Integer, Integer>{
		
		public static final int UPDATE_SUCCESS = 1;//���³ɹ�
		public static final int CONNECTION_FAIL = 0;//���ӷ�������ʱ
		
		private int flag = UPDATE_SUCCESS;
		WebService webService = new WebService();

		@Override
		protected void onProgressUpdate(Integer... values) {
			
			log.setMessage(getResources().getString(values[0]));
		}

		@Override
		protected Integer doInBackground(Void... params) {
			
			ArrayList<Task> taskList = null;//web�������б�
			ArrayList<Integer> taskIDs = null;//���������б�
			
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("userID",application.getUserID());
			map.put("type", String.valueOf(2));
			
			publishProgress(R.string.download_exercise);
			SoapObject result = webService.CallWebService("getTaskList", map);
			
			if(result == null){
				return CONNECTION_FAIL;
			}else{
				DBManager db = new DBManager(helper.getConnection());
				taskList = XMLParser.parseTaskList(result);//web�������б�
				taskIDs = db.getTaskListIDs(application.getUserID(),3);//���������б�
				if(taskList != null){
					
					//������Դ����
					DownloadResourceType.download(db);
					
				for(Task task:taskList){
					taskIDs.remove((Object)task.getTaskID());
					//��������
					flag = new SingleTaskUpdate(db,new OnProcessChangeListener() {
						public void onProcessChange(int process) {
							switch(process){							
							case SingleTaskUpdate.IMPORT_TASK : 
								publishProgress(R.string.import_exercise);break;
							case SingleTaskUpdate.DOWNLOAD_TEST : 
								publishProgress(R.string.download_test);break;
							case SingleTaskUpdate.IMPORT_TEST : 
								publishProgress(R.string.import_test);break;
							case SingleTaskUpdate.DOWNLOAD_ITEM : 
								publishProgress(R.string.download_test_item);break;
							case SingleTaskUpdate.IMPORT_ITEM : 
								publishProgress(R.string.import_test_item);break;
							case SingleTaskUpdate.DOWNLOAD_OPTION : 
								publishProgress(R.string.download_test_item_option);break;
							case SingleTaskUpdate.IMPORT_OPTION :
								publishProgress(R.string.import_test_item_option);
							}
						}
					},application.getUserID()).updateTask(task);

				}
				
				//�������-�û���ϵ��
				db.addTaskUserArrangeList(application.getUserID(),taskList);
				
				}
				//ɾ�����غͷ�������ͬ����ҵ(ʼ�ձ����������һ��)
				for(int id : taskIDs){
					db.deleteTaskUserArrange(application.getUserID(),id);
					db.deleteMyChoice(application.getUserID(),id);
					db.deleteUploadFiles(application.getUserID(),id);
					db.deleteAttachment(application.getUserID(), id);
					db.deleteFeedBack(application.getUserID(), id);
				}
				return flag;
			}
		}
		
		protected void onPostExecute(Integer result) {
			
			if(result == CONNECTION_FAIL){
				log.dismiss();
				ShowToastUtil.showConnectionTimeOutToast(TaskActivity.this);
			}else{
				DBManager db = new DBManager(helper.getConnection());
				updateDateForTab();
				
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
				String time = format.format(date);
				updateTimeView.setText(TaskActivity.this.getResources()
						.getString(R.string.exercise_uptate_time)+time);
				
				//���³ɹ�,���ظ��°�ť����ʾ�ֶ�
				downloadButtonBadge.hide();
				//���ݿ��и���
				sp.edit().putInt("newTaskCount",0).commit();
				
				db.setExerciseDownloadTime(application.getUserID(),time);
				log.dismiss();
				Toast.makeText(TaskActivity.this,
						R.string.tip_update_success, Toast.LENGTH_SHORT).show();	

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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View myView = null;
			DBManager db = new DBManager(helper.getConnection());
			int taskID = Integer.parseInt(data.get(position).get("taskID"));
			
			
			//û�����ֵ���ʾ����
			if(!db.checkTaskScored(application.getUserID(),taskID)){
				
				myView = inflater.inflate(R.layout.layout_tasklist, parent,false);
				RoundProgressBar myProgress = (RoundProgressBar)myView
						.findViewById(R.id.myProgress);
				int max = Integer.parseInt(data.get(position).get("max"));
				
				//����ҵ���������,����ʾ������
				if(max == 0){
					myProgress.setVisibility(View.GONE);
				}else{
					myProgress.setMax(max);
					myProgress.setProgress(Integer.parseInt(data.get(position).get("process")));			
				}
							
			}else{//�����ֵ���ʾ�÷�
				FeedBack feedBack = db.getFeedBack(application.getUserID(),taskID);
				myView = inflater.inflate(R.layout.layout_tasklist_done, parent,false);
				
				//��������������ʾ
				if(feedBack.getOpenFraction().equalsIgnoreCase("true")){
					String score = feedBack.getScore();
					
					TextView scoreView = (TextView)myView.findViewById(R.id.score);
					scoreView.setText(score);
				}
				
			}
			TextView taskIDView = (TextView)myView.findViewById(R.id.taskID);
			TextView title = (TextView)myView.findViewById(R.id.title);
			TextView startDate = (TextView)myView.findViewById(R.id.startDate);
			TextView endDate = (TextView)myView.findViewById(R.id.endDate);
			TextView name = (TextView)myView.findViewById(R.id.name);
			taskIDView.setText(data.get(position).get("taskID"));
			title.setText(data.get(position).get("title"));
			startDate.setText(data.get(position).get("startDate"));
			String sEndDate = data.get(position).get("endDate");
			
			//�Ѿ���ֹ����ҵ,�Խ�ֹ���ڱ��
			if(DeadlineCheck.check(sEndDate)){
				endDate.setTextColor(TaskActivity.this.getResources().getColor(R.color.my_red));
			}else{
				endDate.setTextColor(Color.BLACK);
			}
			endDate.setText(sEndDate);
			
			name.setText(data.get(position).get("teacherName"));
			
			return myView;
		}	
	}	
    
}
