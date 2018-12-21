package com.cqupt.ui.teacher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.Task;
import com.cqupt.model.UserInfo;
import com.cqupt.net.WebService;
import com.cqupt.ui.common.SearchActivity;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.DeadlineCheck;
import com.cqupt.util.LanguageSetting;
import com.cqupt.util.XMLParser;
import com.cqupt.util.pageModel.TeacherTaskListPageModel;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TeacherMainActivity extends Activity implements OnClickListener{
	
	private MyApplication application;

	private int currentTabIndex = 1;
	private int sumPage = 0;
	
	private MySQLiteOpenHelper helper;
	private LanguageSetting ls;
	private TaskListAdapter adapter;
	private TeacherTaskListPageModel pageModel;
	private List<Task> taskList;//�洢��ҵ���б�
	//private SharedPreferences sp;
	private HashMap<String,Integer> selectedTasks;//��¼�Ѿ�ѡ�������
	private boolean isMultiChoiceMode = false;//��¼listview�Ƿ��ڶ�ѡ״̬

	private TextView menuLanguage;
	//private TextView menuLogout;
	private TextView userIdView;
	private TextView userNameView;
	private TextView schoolView;
	private TextView deptView;
	private TextView emailView;
	private TextView setNetworkView;
	private TextView searchView;
	private ListView taskListView;
	private DrawerLayout container;
	private LinearLayout tabDoing;
	private LinearLayout tabDone;
	private TextView tabDoneText;
	private TextView tabDoingText;
	private ImageView tabDoneImage;
	private ImageView tabDoingImage;
	private ProgressBar progressBar;
	private RelativeLayout topHideView;
	private RelativeLayout bottomHideView;
	private TextView cancelView;
	private TextView seletedTaskCountView;
	private Button deleteTaskButton;
	private Button newTaskButton;
	private RadioGroup radioGroup;
	private RadioButton radioScored;
	private RadioButton radioUnscored;
	private RadioButton radioAll;
	private MyOnCheckedChangListener OnCheckedChangeListener;
	private int selectionIndex;//��¼ɸѡ�����±꣨0 ȫ�� 1 ������ 2 δ���֣�
	 
	//--------------------------------��д����-------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_teacher_main);
		
		findView();
		
		if(!CheckNetwork.isConnectingToInternet(this)){
			setNetworkView.setVisibility(View.VISIBLE);
		}
		
		loadData();
		setListener();
		
	}
	
	@Override
	protected void onRestart() {
		
		super.onRestart();
		
		if(!CheckNetwork.isConnectingToInternet(this)){
			setNetworkView.setVisibility(View.VISIBLE);
		}else{
			setNetworkView.setVisibility(View.GONE);
			
			//���¼�������
			reloadTaskList();
		}

	}
	
		@SuppressLint("RtlHardcoded")
		@Override
		public void onClick(View v) {
			
			switch(v.getId()){
			case R.id.menulanguage :
				changeLanguage(((TextView)v).getText().toString());
				break;
			case R.id.userIdView : 
				if(container.isDrawerOpen(Gravity.LEFT)){
					container.closeDrawer(Gravity.LEFT);
				}else{
					container.openDrawer(Gravity.LEFT);
				}
				break;
			case R.id.networkTipView :
				Intent intent = new Intent("android.settings.WIRELESS_SETTINGS");  
		        startActivity(intent);
		        break;
			case R.id.includeSearch :
				intent = new Intent(this,SearchActivity.class);
				intent.putExtra("type", SearchActivity.TYPE_TEACHER_TASK);
			    startActivity(intent);
			    break;
			case R.id.tabDoing : setTabMoveAnimation(currentTabIndex, 1);break;
			case R.id.tabFinished : setTabMoveAnimation(currentTabIndex, 2);break;
			case R.id.cancelView : clearListViewMultiChoiceMode();break;
			case R.id.deleteTask : showDeleteTaskConfirmDialog();break;
			case R.id.newTask :
				intent = new Intent(this,EditTaskActivity.class);
			    startActivity(intent);
			    break;	
			}	
		}
	
	//------------------------------�Զ��巽��------------------------------
	
	private void findView(){
		
		container = (DrawerLayout)findViewById(R.id.container);
		menuLanguage = (TextView)findViewById(R.id.menulanguage);
		userIdView = (TextView)findViewById(R.id.userIdView);
		userNameView = (TextView)findViewById(R.id.userName);
		schoolView = (TextView)findViewById(R.id.school);
		deptView = (TextView)findViewById(R.id.department);
		emailView = (TextView)findViewById(R.id.email);
		setNetworkView = (TextView)findViewById(R.id.networkTipView);
		searchView = (TextView)findViewById(R.id.includeSearch);
		taskListView = (ListView)findViewById(R.id.listView);
		tabDoing = (LinearLayout)findViewById(R.id.tabDoing);
		tabDoingText = (TextView)findViewById(R.id.tabDoingText);
		tabDoingImage = (ImageView)findViewById(R.id.tabDoingImage);
		tabDone = (LinearLayout)findViewById(R.id.tabFinished);
		tabDoneText = (TextView)findViewById(R.id.tabFinishedText);
		tabDoneImage = (ImageView)findViewById(R.id.tabFinishedImage);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		topHideView = (RelativeLayout)findViewById(R.id.hideTop);
		bottomHideView = (RelativeLayout)findViewById(R.id.hideBottom);
		cancelView = (TextView)findViewById(R.id.cancelView);
		seletedTaskCountView = (TextView)findViewById(R.id.selectedItem);
		deleteTaskButton = (Button)findViewById(R.id.deleteTask);
		newTaskButton = (Button)findViewById(R.id.newTask);
		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		radioScored = (RadioButton) findViewById(R.id.radioScored);
		radioUnscored = (RadioButton) findViewById(R.id.radioUnscored);
		radioAll = (RadioButton) findViewById(R.id.radioAll);
		
	}
	
	private void loadData(){
		
		application = (MyApplication)getApplication();
		pageModel = new TeacherTaskListPageModel(application.getUserID());
		
		taskList = new ArrayList<Task>();//��ʼ�������б�
		selectedTasks = new HashMap<String, Integer>();//��ʼ����ѡ�����б�
		new GetTaskListThread().execute();
		
		ls = LanguageSetting.getInstance();
		helper = MySQLiteOpenHelper.getInstance(this);
		DBManager db = new DBManager(helper.getConnection());
		UserInfo info = db.getUserInfo();
		
		String userName = info.getUserName();
		String school = info.getUserSchool();
		String dept = info.getUserDepartment();
		String email = "null".equalsIgnoreCase(info.getUserEmail()) ? "" : info.getUserEmail();
		
		container.setScrimColor(getResources().getColor(R.color.divider));
		
		userIdView.setText(userName);
		userNameView.setText(application.getUserID()+"|"+userName);
		schoolView.setText(school);
		deptView.setText(dept);
		emailView.setText(email);
		
	}
	
	private void setListener(){
		
		menuLanguage.setOnClickListener(this);
		userIdView.setOnClickListener(this);
		setNetworkView.setOnClickListener(this);
		tabDone.setOnClickListener(this);
		tabDoing.setOnClickListener(this);
		cancelView.setOnClickListener(this);
		deleteTaskButton.setOnClickListener(this);
		newTaskButton.setOnClickListener(this);
		searchView.setOnClickListener(this);
		OnCheckedChangeListener = new MyOnCheckedChangListener();
		radioAll.setOnCheckedChangeListener(OnCheckedChangeListener);
		radioScored.setOnCheckedChangeListener(OnCheckedChangeListener);
		radioUnscored.setOnCheckedChangeListener(OnCheckedChangeListener);
		
		taskListView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
				//������һҳ����
				if(pageModel.getLastItemIndex() == taskList.size() -1 
						&& pageModel.getCurrentPage() < sumPage
						&& scrollState == OnScrollListener.SCROLL_STATE_IDLE)
				{
					new GetTaskListThread().execute();
				}	
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				//��¼���һ���ɼ�item���±�
				pageModel.setLastItemIndex(firstVisibleItem + visibleItemCount - 1);
				
			}
		});
		
		taskListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(!isMultiChoiceMode){
					topHideView.setVisibility(View.VISIBLE);
					bottomHideView.setVisibility(View.VISIBLE);
					
					adapter.notifyDataSetChanged();
					
					isMultiChoiceMode = true;
				}
				return true;
			}
		});
		
		taskListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				TaskListAdapter.ViewHolder holder = (TaskListAdapter.ViewHolder)view.getTag();
				int taskID = Integer.valueOf(holder.taskIDView.getText().toString());
				//���ڳ���״̬,�����������ѡ���ɾ���Ľ���
				if(isMultiChoiceMode){
					CheckBox checkBox = holder.checkBox;
								
					if(checkBox.isChecked()){
						checkBox.setChecked(false);
						
						if(currentTabIndex == 1){
							selectedTasks.remove("u" + position);
						}else{
							selectedTasks.remove("s" + position);
						}
						
					}else{
						checkBox.setChecked(true);
						
						if(currentTabIndex == 1){
							selectedTasks.put("u" + position,taskID);
						}else{
							selectedTasks.put("s" + position,taskID);
						}
					}
					
					seletedTaskCountView.setText(String.valueOf(selectedTasks.size()));
					
					if(selectedTasks.size() == 0){
						deleteTaskButton.setEnabled(false);
					}else{
						deleteTaskButton.setEnabled(true);
					}
					
				}else{
					Intent it = new Intent(TeacherMainActivity.this,
							TeacherTaskDetailActivity.class);
					it.putExtra("taskID", taskID);
					startActivity(it);
				}
			}
		});
		
	}
	
	private void changeLanguage(String title){
		
		if(title.equals("English")){
			ls.setCurrentLanguage(this,"en");
		}else{
			ls.setCurrentLanguage(this,"zh");
		}
		recreate();
	}
	
	/**
	 * ���¼�������
	 */
	private void reloadTaskList(){
		
		pageModel.setCurrentPage(1);
		pageModel.setLastItemIndex(0);
		pageModel.setType(currentTabIndex);
		
		if(currentTabIndex == 2){			 
			pageModel.setSelection(selectionIndex);
			
		}
		
		taskList.clear();
		
		new GetTaskListThread().execute();
		
	}
	
		/**
		 * ��ʾɾ��������ʾ�Ի���
		 */
		private void showDeleteTaskConfirmDialog(){
			
			AlertDialog log = new AlertDialog.Builder(this)
			        .setTitle(R.string.delete_confirm)
			        .setNegativeButton(R.string.cancel,null)
			        .setPositiveButton(R.string.confirm,new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new DeleteTaskThread().execute();	
						}
					})
			        .create();
			
			log.show();
			
		}

		/**
		 * ���listview�ڶ�ѡģʽ��״̬
		 */
		private void clearListViewMultiChoiceMode(){
			
			topHideView.setVisibility(View.GONE);
			seletedTaskCountView.setText("0");
			deleteTaskButton.setEnabled(false);
			bottomHideView.setVisibility(View.GONE);
			
			selectedTasks.clear();
			isMultiChoiceMode = false;
			adapter.notifyDataSetChanged();
			
		}
		
		private void resetSelectionBar(){
			radioAll.setChecked(true);
			radioScored.setChecked(false);
			radioUnscored.setChecked(false);
		}
		
		/**
		 * ����tab���л���"����"Ч��
		 * @param from ��ʼ�ƶ���tab��
		 * @param to �ƶ����tab��
		 */
		private void setTabMoveAnimation(int from,int to){
			
			if(from != to){
				resetSelectionBar();
				
				//�ı�tab��ѡ����ɫ
				//��ʼ��tab��ͼ��䰵
				switch(from){
				case 1 :
					tabDoingText.setTextColor(Color.BLACK);
					tabDoingImage.setVisibility(View.INVISIBLE);
					break;
				case 2 :
					tabDoneText.setTextColor(Color.BLACK);
					tabDoneImage.setVisibility(View.INVISIBLE);
					break;
				}
				//�ƶ����tab��ͼ�����
				switch(to){
				case 1 :
					tabDoingText.setTextColor(getResources().getColor(R.color.background_lightgreen));
					tabDoingImage.setVisibility(View.VISIBLE);
					radioGroup.setVisibility(View.GONE);
					break;
				case 2 :
					tabDoneText.setTextColor(getResources().getColor(R.color.background_lightgreen));
					tabDoneImage.setVisibility(View.VISIBLE);
					radioGroup.setVisibility(View.VISIBLE);
					break;
				}
				
				currentTabIndex = to;
				reloadTaskList();
			}
		}
		
//-------------------------�ڲ���--------------------------------	

		/**
		 * ɾ����ҵ�ĺ�̨�߳�
		 */
		class DeleteTaskThread extends AsyncTask<Void,Integer,Integer>{
			
			private final int CONNECTION_FAIL = -1;
			private final int DELETE_SUCCESS = 1;
			private final int DELETE_FAIL = 0;
			
			private WebService web = new WebService();

			@Override
			protected void onPreExecute() {
				progressBar.setVisibility(View.VISIBLE);
			}

			@Override
			protected Integer doInBackground(Void... params) {
				
				HashMap<String,String> p = new HashMap<String, String>();
				p.put("userID",application.getUserID());
				p.put("taskIDs",selectedTasks.values().toString().replace(" ",""));
				
				SoapObject result = web.CallWebService("deleteTask",p);
				
				if(result == null){
					return CONNECTION_FAIL;
				}
				
				//������ɾ���ɹ�
				if(XMLParser.parseBoolean(result).equalsIgnoreCase("true")){
					return DELETE_SUCCESS;
				}else{
					return DELETE_FAIL;
				}
				
			}

			@Override
			protected void onPostExecute(Integer result) {
				
				switch(result){
				case CONNECTION_FAIL :
					Toast.makeText(TeacherMainActivity.this,
							R.string.tip_connection_timeout, Toast.LENGTH_LONG).show();
					break;
				case DELETE_FAIL :
					Toast.makeText(TeacherMainActivity.this,
							R.string.delete_task_fail, Toast.LENGTH_LONG).show();
					break;
				case DELETE_SUCCESS :
					Toast.makeText(TeacherMainActivity.this,
							R.string.delete_task_success, Toast.LENGTH_LONG).show();
					
					reloadTaskList();
					clearListViewMultiChoiceMode();
					break;
				}
			}		
		}
		
		/**
		 * ��ȡ��ҵ��Ϣ�ĺ�̨�߳�
		 */
		class GetTaskListThread extends AsyncTask<Void,Integer,Integer>{
			
			private final int CONNECTION_FAIL = -1;
			private final int SUCCESS = 1;

			@Override
			protected void onPreExecute() {
				progressBar.setVisibility(View.VISIBLE);
			}

			@Override
			protected Integer doInBackground(Void... params) {
				
				sumPage = pageModel.getSumPages();
				if(sumPage == TeacherTaskListPageModel.CONNECTION_FAIL){
					return CONNECTION_FAIL;
				}
				
				List<Task> tempList = null;//�ݴ�ÿһ�����󵽵�����
				//���μ�������
				if(taskList.size() == 0){
					tempList = pageModel.getDataList();
				}else{
					tempList = pageModel.nextPage();
				}
				
				
				if(tempList == null){
					return CONNECTION_FAIL;
				}
				
				taskList.addAll(tempList);
				
				return SUCCESS;
			}

			@Override
			protected void onPostExecute(Integer result) {
				
				progressBar.setVisibility(View.GONE);
				
				switch(result){
				case CONNECTION_FAIL :
					Toast.makeText(TeacherMainActivity.this,
							R.string.tip_connection_timeout, Toast.LENGTH_LONG).show();
					break;
				case SUCCESS :
					if(adapter == null){
						adapter = new TaskListAdapter(TeacherMainActivity.this
								, taskList);
						taskListView.setAdapter(adapter);
						taskListView.setSelection(0);
					}else{
						adapter.notifyDataSetChanged();
						taskListView.setSelection(pageModel.getLastItemIndex());
					}	
				}
			}
	    }
		
		class TaskListAdapter extends BaseAdapter{
			
			public class ViewHolder{
				
				public TextView titleView;
				public TextView sDateView;
				public TextView eDateView;
				public TextView stuCountView;
				public TextView examCountView;
				public TextView taskIDView;
				public CheckBox checkBox;
				
			}
			
			private List<Task> list;
			private Context context;

			public TaskListAdapter(Context context,List<Task> list){
				this.context = context;
				this.list = list;
			}
			
			@Override
			public int getCount() {
				
				return list.size();
			}

			@Override
			public Object getItem(int position) {
				
				return null;
			}

			@Override
			public long getItemId(int position) {
				
				return 0;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				
				ViewHolder holder = null;
				
				if(convertView == null){
					
					convertView = LayoutInflater.from(context).inflate(
							R.layout.layout_teacher_tasklist, parent,false);
					
					holder = new ViewHolder();
					holder.titleView = (TextView)convertView.findViewById(R.id.title);
					holder.sDateView = (TextView)convertView.findViewById(R.id.startDate);
					holder.eDateView = (TextView)convertView.findViewById(R.id.endDate);
					holder.stuCountView = (TextView)convertView.findViewById(R.id.studentCount);
					holder.examCountView = (TextView)convertView.findViewById(R.id.examCount);
					holder.taskIDView = (TextView)convertView.findViewById(R.id.taskID);
					holder.checkBox = (CheckBox)convertView.findViewById(R.id.checkBox);
					
					convertView.setTag(holder);
				}else{
					holder = (ViewHolder)convertView.getTag();
				}
				
				holder.titleView.setText(list.get(position).getTitle());
				holder.sDateView.setText(list.get(position).getStartDate());
				
				String endDate = list.get(position).getEndDate();
				holder.eDateView.setText(endDate);
				
				//���Ѿ���ֹ����ҵ���
				if(DeadlineCheck.check(endDate)){
					holder.eDateView.setTextColor(TeacherMainActivity.this
							.getResources().getColor(R.color.my_red));
				}else{
					holder.eDateView.setTextColor(Color.BLACK);
				}
				
				holder.stuCountView.setText(String.valueOf(list.get(position).getStudents()));
				holder.examCountView.setText(String.valueOf(list.get(position).getExams()));
				holder.taskIDView.setText(String.valueOf(list.get(position).getTaskID()));
				
				//��ѡģʽ��Ҫ��ʾcheckbox
				if(isMultiChoiceMode){
					holder.checkBox.setVisibility(View.VISIBLE);
					
					//��ǰ����δ���ֵ�tab��
					if(currentTabIndex == 1){
						if(selectedTasks.keySet().contains("u" + position)){
							holder.checkBox.setChecked(true);
						}else{
							holder.checkBox.setChecked(false);
						}
					}else{//��ǰ���������ֵ�tab��
						if(selectedTasks.keySet().contains("s" + position)){
							holder.checkBox.setChecked(true);
						}else{
							holder.checkBox.setChecked(false);
						}
					}		
				}else{
					holder.checkBox.setVisibility(View.GONE);
				}
				
				return convertView;
			}
		}
		
		class MyOnCheckedChangListener implements OnCheckedChangeListener{

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				
				if(isChecked){
					
					switch (buttonView.getId()) {
					case R.id.radioAll : selectionIndex = 0;break;
					case R.id.radioScored : selectionIndex = 1;break;
					case R.id.radioUnscored : selectionIndex = 2;break;
					}
					
					reloadTaskList();
				}
				
			}
			
		}

		
		
}
