package com.cqupt.ui.student;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.Attachment;
import com.cqupt.model.FeedBack;
import com.cqupt.model.GroupItem;
import com.cqupt.model.Task;
import com.cqupt.model.Test;
import com.cqupt.net.WebService;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.DownloadMyAnswers;
import com.cqupt.util.DownloadTest;
import com.cqupt.util.DownloadTest.OnAddArrangeRelation;
import com.cqupt.util.DownloadTest.OnProcessChangeListener;
import com.cqupt.util.XMLParser;
import com.cqupt.util.pageModel.FinishedTaskPageModel;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DownloadFinishedTaskActivity extends Activity implements OnClickListener{
	
	private MyApplication application;
	
	private TextView mReturnView;
	private TextView mCancelView;
	private TextView mSelectedTasksView;
	private ExpandableListView mExpandableListView;
	private Button mDownloadButton;
	private MyExpandableListViewAdapter mListViewAdapter;
	private SwipeRefreshLayout mRefreshLayout;
	private TextView mEmptyView;//û������ʱ��ʾ����ͼ
	
	private List<GroupItem> mListViewGroupData;
	private List<Task> mListViewChildrenData;
	private List<Integer> mSelectedTasks;
	private FinishedTaskPageModel mPageModel;
	private int mSumPages;
	private DBManager mDb;
	
	//--------------------------��д����------------------------
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_finished_task);
		
		findView();
		loadData();
		setListener();
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.returnView : finish();break;
		case R.id.cancel : resetViews();break;
		case R.id.downloadButton :
			
			if(CheckNetwork.isConnectingToInternet(this)){
				new DownloadTaskThread().execute();break;
			}else{
				Toast.makeText(this, R.string.tip_network_unavailable, Toast.LENGTH_SHORT)
				.show();
			}
			break;
			
		}
		
	}

	
	//-----------------------------------�Զ��巽��-------------------------
	
	
	private void findView() {
		
		mReturnView = (TextView) findViewById(R.id.returnView);
		mCancelView = (TextView) findViewById(R.id.cancel);
		mSelectedTasksView = (TextView) findViewById(R.id.selectedTasks);
		mDownloadButton = (Button) findViewById(R.id.downloadButton);
		mExpandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
		mEmptyView = (TextView) findViewById(R.id.emptyView);
		
	}
	
	private void loadData(){
		
		application = (MyApplication) getApplication();
		
		mSelectedTasks = new ArrayList<Integer>();
		mDb = new DBManager(MySQLiteOpenHelper.getInstance(
				DownloadFinishedTaskActivity.this).getConnection());
		
		
		mPageModel = new FinishedTaskPageModel(application.getUserID(),1,15);
		mListViewGroupData = new ArrayList<GroupItem>();
		mExpandableListView.setEmptyView(mEmptyView);
		
		new GetDataThread().execute();
		
	}
	
	private void setListener(){
		
		mReturnView.setOnClickListener(this);
		mCancelView.setOnClickListener(this);
		mDownloadButton.setOnClickListener(this);
		
		mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh() {
							
				mRefreshLayout.setRefreshing(true);
				
				if(CheckNetwork.isConnectingToInternet(DownloadFinishedTaskActivity.this)){
					//�������һҳ��ȥ��ȡ����
					if(mSumPages > mPageModel.getCurrentPage()){
						new GetDataThread().execute();
					}else{
						mRefreshLayout.setRefreshing(false);
						Toast.makeText(DownloadFinishedTaskActivity.this
								,R.string.no_more_data,Toast.LENGTH_SHORT).show();
					}
				}else{
					mRefreshLayout.setRefreshing(false);
					Toast.makeText(DownloadFinishedTaskActivity.this
							,R.string.tip_network_unavailable,Toast.LENGTH_SHORT).show();
				}			
				
			}
		});
		
		mExpandableListView.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				
				MyExpandableListViewAdapter.ChildrenViewHolder holder = 
						(MyExpandableListViewAdapter.ChildrenViewHolder)v.getTag();
				
				CheckBox checkBox = holder.checkBox;
				int taskID = Integer.parseInt(holder.taskIDView.getText().toString());
				
				if(checkBox.isChecked()){
					checkBox.setChecked(false);
					mSelectedTasks.remove((Object)taskID);
				}else{
					checkBox.setChecked(true);
					mSelectedTasks.add(taskID);
				}
				
				mSelectedTasksView.setText(String.valueOf(mSelectedTasks.size()));
				
				if(mSelectedTasks.size() != 0){
					mDownloadButton.setEnabled(true);
					mCancelView.setVisibility(View.VISIBLE);
					
				}else{
					mDownloadButton.setEnabled(false);
					mCancelView.setVisibility(View.GONE);
				}
				
				return false;
			}
		});
		
	}
	
	private void resetViews(){
		
		mCancelView.setVisibility(View.GONE);
		mSelectedTasks.clear();
		mSelectedTasksView.setText("0");
		mDownloadButton.setEnabled(false);
	    mListViewAdapter.notifyDataSetChanged();
		
	}
		
	
	class GetDataThread extends AsyncTask<Void,Void,Integer>{
		
		private static final int SUCCESS = 1;
		
		//���group������
		private void setGroupData(List<Task> list){
			
			String tempDate = null;
			String currentDate = null;
			GroupItem item = null;
			int childCount = 1;
			
			
			for(int i = 0 ; i < list.size() ; i++){
				
				Task task = list.get(i);
				currentDate = task.getEndDate().split(" ")[0];
				
				if(i != 0){
					
					if(!currentDate.equals(tempDate)){

						 item.setChildrenCount(childCount);
						 mListViewGroupData.add(item);
						 item = new GroupItem();
						 item.setText(currentDate);
						 item.setStartChildIndex(i);
						 tempDate = currentDate;
						 childCount = 1;
						 
						 if(i == list.size() - 1){
							 item.setChildrenCount(childCount);
							 mListViewGroupData.add(item);
						 }
						 
					 }else{
						 childCount++;
						 tempDate = currentDate;
						 
						 if(i == list.size() - 1){
							 item.setChildrenCount(childCount);
							 mListViewGroupData.add(item);
						 }
						 
					 }
				}else{
					item = new GroupItem();
					item.setText(currentDate);
					item.setStartChildIndex(i);
					tempDate = currentDate;
					
					if(list.size() == 1){
						item.setChildrenCount(childCount);
						mListViewGroupData.add(item);
					}
					
				}		 
			}		
		};

		@Override
		protected Integer doInBackground(Void... params) {
			
			//ȡ�ñ����Ѿ��е��Ѿ���ֹ����ҵ�б�
			List<HashMap<String, String>> localList = mDb.getTaskList(application.getUserID()
					, 4);
			List<Task> localTaskList = new ArrayList<Task>();
			for(HashMap<String, String> map : localList){
				Task task = new Task();
				task.setTaskID(Integer.parseInt(map.get("taskID")));
				localTaskList.add(task);
			}
			
			if(mListViewChildrenData == null){
				mListViewChildrenData = mPageModel.getDataList();
				mListViewChildrenData.removeAll(localTaskList);//ɾ�������Ѿ��е�task
				mSumPages = mPageModel.getSumPages();
				setGroupData(mListViewChildrenData);
			}else{
				List<Task> tempList = mPageModel.nextPage();
				tempList.removeAll(localTaskList);//ɾ�������Ѿ��е�task
				mListViewChildrenData.addAll(tempList);
				setGroupData(tempList);
			}		
			
			return SUCCESS;

		}

		@Override
		protected void onPostExecute(Integer result) {
			
			mRefreshLayout.setRefreshing(false);
			if(mListViewAdapter == null){
				mListViewAdapter = new MyExpandableListViewAdapter(
						DownloadFinishedTaskActivity.this);
				mExpandableListView.setAdapter(mListViewAdapter);
			}else{
				resetViews();
			}
			
		}
	
		
	}
	class MyExpandableListViewAdapter extends BaseExpandableListAdapter{
		
		private Context mContext;
		
		public MyExpandableListViewAdapter(Context context){
			mContext = context;
		}
		
		final class GroupViewHolder {
			
			public TextView textView;
			
		}
		
		final class ChildrenViewHolder{
			
			public TextView titleView;
			public TextView userView;
			public TextView taskIDView;
			public CheckBox checkBox;
			
		}

		@Override
		public int getGroupCount() {
			return mListViewGroupData.size();
			
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return mListViewGroupData.get(groupPosition).getChildrenCount();
		}

		@Override
		public Object getGroup(int groupPosition) {
			
			return null;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			
			return null;
		}

		@Override
		public long getGroupId(int groupPosition) {
			
			return 0;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			
			GroupViewHolder holder = null;
			if(convertView == null){
				convertView = LayoutInflater.from(mContext)
						.inflate(R.layout.layout_finished_task_group_item, null);
				holder = new GroupViewHolder();
				holder.textView = (TextView) convertView.findViewById(R.id.textView);
				
				convertView.setTag(holder);
			}else{
				holder = (GroupViewHolder) convertView.getTag();
			}
			
			GroupItem item = mListViewGroupData.get(groupPosition);
			
			holder.textView.setText(item.getText() + "  (" + item.getChildrenCount() + ")");
			
			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			
			ChildrenViewHolder holder = null;
			if(convertView == null){
				convertView = LayoutInflater.from(mContext)
						.inflate(R.layout.layout_finished_task_child_item, null);
				holder = new ChildrenViewHolder();
				holder.titleView = (TextView) convertView.findViewById(R.id.title);
				holder.userView = (TextView) convertView.findViewById(R.id.teacher);
				holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
				holder.taskIDView = (TextView) convertView.findViewById(R.id.taskID);
				
				convertView.setTag(holder);
			}else{
				holder = (ChildrenViewHolder) convertView.getTag();
			}
			
			int position = mListViewGroupData.get(groupPosition).getStartChildIndex()
					+ childPosition;
			Task task = mListViewChildrenData.get(position);
			
			holder.titleView.setText(task.getTitle());
			holder.userView.setText(task.getCreateUser());
			holder.taskIDView.setText(String.valueOf(task.getTaskID()));
			
			if(mSelectedTasks.contains(task.getTaskID())){
				holder.checkBox.setChecked(true);
			}else{
				holder.checkBox.setChecked(false);
			}
			
			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			
			return true;
		}
		
	}
	
	class DownloadTaskThread extends AsyncTask<Void,Integer,Integer>{
		
		private final static int SUCCESS = 1;
		private final static int FAIL = -1;
		
		private ProgressDialog log;
		
		private WebService web = new WebService();
		
		@Override
		protected void onPreExecute() {
			
			log = new ProgressDialog(DownloadFinishedTaskActivity.this);
			log.setCancelable(false);//���������ؼ��رոöԻ���
			log.setCanceledOnTouchOutside(false);//���������ⲿ����رոöԻ���
			log.setTitle(R.string.update_exercise_message);
			log.show();
			
		}
		
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			
			Context context = DownloadFinishedTaskActivity.this;
			log.setMessage(context.getResources().getString(values[0]));
			
		}

		@Override
		protected Integer doInBackground(Void... params) {
			
			for(Integer taskID : mSelectedTasks){
				
				mDb.addTaskUserArrangeList(application.getUserID(),taskID);//�������-�û���ϵ��
				
				//������ҵ��ϸ
				publishProgress(R.string.download_exercise);
				
				HashMap<String,String> p = new HashMap<String, String>();
				p.put("taskID",taskID.toString());
				p.put("userID", application.getUserID());
				
				SoapObject result = web.CallWebService("getTask", p);
				
				if(result == null){
					return FAIL;
				}
				
				//������ҵ��ϸ
				publishProgress(R.string.import_exercise);
				mDb.addTask(XMLParser.parseTaskList(result).get(0));
				
				//������ʦ��ѧ���Ѿ��ϴ��ĸ���
				if(downloadAttachment(taskID) == FAIL){
					return FAIL;
				}
				
				//���ط�����Ϣ
				if(downloadFeedBack(taskID) == FAIL){
					return FAIL;
				}
				
				//���ش������
				if(new DownloadMyAnswers(taskID,mDb,application.getUserID()).download() 
						== DownloadMyAnswers.CONNECTION_TIME_OUT){
					return FAIL;
				}
				
				return downloadTest(taskID);
			}
			
			return SUCCESS;
			
		}


		@Override
		protected void onPostExecute(Integer result) {
			
			log.dismiss();
			if(result == FAIL){
				Toast.makeText(DownloadFinishedTaskActivity.this,
						R.string.tip_connection_timeout,Toast.LENGTH_SHORT).show();
			}else{
				mListViewChildrenData = null;
				mListViewGroupData.clear();
				new GetDataThread().execute();
				Toast.makeText(DownloadFinishedTaskActivity.this,
						R.string.tip_update_success, Toast.LENGTH_SHORT).show();
			}
			
		}
		
		private int downloadAttachment(int taskID){
			
			//���ؽ�ʦ�ϴ�����ҵ����
			publishProgress(R.string.download_attachment);
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("id", String.valueOf(taskID));
			p.put("type", String.valueOf(2));//��ʦ��ҵ����
			p.put("userID","");
			
			SoapObject result = web.CallWebService("getAttachment", p);
			
			if(result == null){
				return FAIL;
			}
			
			//�����ʦ����
			publishProgress(R.string.import_attachment);
			mDb.addAttachment(XMLParser.parseAttachment(result));
			
			//����ѧ���Ѿ��ϴ�����
			publishProgress(R.string.download_attachment);
			p = new HashMap<String, String>();
			p.put("id", String.valueOf(taskID));
			p.put("type", String.valueOf(3));//ѧ����ҵ����
			p.put("userID", application.getUserID());
			
			result = web.CallWebService("getAttachment", p);
			
			if(result == null){
				return FAIL;
			}
			
			//����ѧ������
			publishProgress(R.string.import_attachment);
			ArrayList<Attachment> attachmentList = XMLParser.parseAttachment(result);
			
			for(Attachment a : attachmentList){
				 a.setUserID(application.getUserID());
			 }
			
			mDb.addAttachment(attachmentList);
			
			return SUCCESS;
			
		}
		
		private int downloadFeedBack(int taskID){
			
			HashMap<String,String> map = new HashMap<String, String>();
			map.put("taskID", String.valueOf(taskID));
			map.put("userID", application.getUserID());
			 
			SoapObject result = web.CallWebService("getFeedback", map);
			 
			if(result == null){
				return FAIL;				
			}
			
			FeedBack feedback = XMLParser.parseFeedBack(result);
			feedback.setTaskID(taskID);
			mDb.addFeedBack(application.getUserID(), feedback);
			
			return SUCCESS;
		}
		
		private int downloadTest(final int taskID){
			
			//������Ŀ
			publishProgress(R.string.download_test);
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("taskID", String.valueOf(taskID));
			SoapObject result = web.CallWebService("getTestList", map);

			if (result == null) {
				return FAIL;
			}

			ArrayList<Test> testList = XMLParser.parseTestList(result);// web��test�б�		

			if (testList != null) {
				DownloadTest downloadTest = new DownloadTest(mDb, new OnAddArrangeRelation() {
					
					@Override
					public void addArrangeRelation(int testID) {
						
						// �����ҵ��Ŀ��Ӧ��ϵ��
						mDb.addTaskTestArrange(taskID, testID);
						
					}
				});
				
				
				downloadTest.setListener(new OnProcessChangeListener() {
					
					@Override
					public void onProcessChange(int process) {
						
						switch (process) {
						case DownloadTest.IMPORT_TEST:
							publishProgress(R.string.import_test);break;
						case DownloadTest.DOWNLOAD_TEST_ITEM:
							publishProgress(R.string.download_test_item);break;
						case DownloadTest.IMPORT_TEST_ITEM:
							publishProgress(R.string.import_test_item);break;
						case DownloadTest.DOWNLOAD_OPTION:
							publishProgress(R.string.download_test_item_option);break;
						case DownloadTest.IMPORT_OPTION:
							publishProgress(R.string.import_test_item_option);break;
						}
						
					}
				});
				
				return downloadTest.downloadTest(testList);
			}
			return SUCCESS;
		 }
		
	}

}
