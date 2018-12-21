package com.cqupt.ui.teacher;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.adapter.ItemsShowingAdapter;
import com.cqupt.application.MyApplication;
import com.cqupt.fragment.FeedbackFragment;
import com.cqupt.model.Attachment;
import com.cqupt.model.ItemsShowingModel;
import com.cqupt.model.ScoreStudentItem;
import com.cqupt.net.WebService;
import com.cqupt.net.WebServiceOperation;
import com.cqupt.thread.MakeFileReadThread;
import com.cqupt.ui.common.FileExploreActivity.MyFile;
import com.cqupt.ui.common.FullScreenPreviewActivity;
import com.cqupt.util.AudioPlayManager;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.FileUtil;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.util.XMLParser;
import com.cqupt.util.pageModel.PageModel;
import com.cqupt.util.pageModel.ScoreStuItemPageModel;
import com.cqupt.view.AttachmentView;
import com.cqupt.view.FlowLayout;
import com.cqupt.view.MyAudioPlayView;
import com.cqupt.view.RoundProgressBar;
import com.cqupt.view.TagView;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScoreActivity extends Activity implements OnClickListener{
	
	private static final int CONNECTION_FAIL = -1;
	private static final int SUCCESS = 1;
	
	private static AudioPlayManager audioManager;
	private static boolean isAudioPreparing;
	private static MyAudioPlayView playingView;//��¼��ǰ���ڲ�����Ƶ�����
	
	private TextView returnView;
	private TextView titleView;
	private ListView listView;
	private ListViewAdapter listViewAdapter;
	private ProgressBar progressBar;
	private Button autoScoreButton;
	private Button postScoreButton;
	private RadioButton scoredRadioButton;
	private RadioButton unscoredRadioButton;
	private RadioButton allscoreRadioButton;
	private RadioButton filesRadioButton;
	private RadioButton noFilesRadioButton;
	private RadioButton allFilesRadioButton;
	private BroadcastReceiver mReceiver;
	
	private List<ScoreStudentItem> listViewData;
	private boolean isOpenFraction = false;//�Ƿ񹫲�����
	private ScoreStuItemPageModel pageModel;
	private int sumPages;//��ҳ����ҳ��
	private LongSparseArray<String> mDownloadQueue;
	private int taskID;
	/*
	 * ��¼��ǰ��ɸѡ����
	 */
	private String scoreType = "0";//0ȫ�� 1������ 2δ����
	private String fileType = "0";//0ȫ�� 1�и��� 2�޸���
	private MyApplication application;
	
	protected void onCreate(Bundle savedInstanceState){
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_score);
		
		findView();
		loadData();
		setListener();
		
	}
	
	private void findView() {
		
		returnView = (TextView) findViewById(R.id.returnView);
		titleView = (TextView) findViewById(R.id.title);
		listView = (ListView) findViewById(R.id.listView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		autoScoreButton = (Button) findViewById(R.id.autoScoreButton);
		postScoreButton = (Button) findViewById(R.id.postScoreButton);
		scoredRadioButton = (RadioButton) findViewById(R.id.scored);
		unscoredRadioButton = (RadioButton) findViewById(R.id.unscored);
		allscoreRadioButton = (RadioButton) findViewById(R.id.allScore);
		filesRadioButton = (RadioButton) findViewById(R.id.files);
		noFilesRadioButton = (RadioButton) findViewById(R.id.noFiles);
		allFilesRadioButton = (RadioButton) findViewById(R.id.allFiles);
		
	}
	
	private void loadData() {
		
		Intent it = getIntent();
		taskID = it.getIntExtra("taskID",0);
		titleView.setText(it.getStringExtra("title"));
		application = (MyApplication) getApplication();
		
		mDownloadQueue = new LongSparseArray<String>();
		
		mReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				
				Bundle bundle = intent.getExtras();
				long id = bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
				
				if(mDownloadQueue.get(id) != null){
					
					String newName = mDownloadQueue.get(id);
					
					if(newName != null){
						
						View view = listView.findViewWithTag(newName);
						
						if(view != null){
							//���سɹ��� ��item�ָ��ɵ��״̬
							TextView contentView = (TextView) view.findViewById(R.id.content);
							contentView.setEnabled(true);
							contentView.setTextColor(Color.BLACK);
							
							ShowToastUtil.showDownloadSuccessToast(ScoreActivity.this);
							
						}
						
						
					}
					
				}
				
			}
		};
		
		registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		new GetListViewDataThread().execute();
		
	}

	private void setListener() {
		
		returnView.setOnClickListener(this);
		autoScoreButton.setOnClickListener(this);
		postScoreButton.setOnClickListener(this);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent it = new Intent(ScoreActivity.this,ScoreTestListActivity.class);
				it.putExtra("taskID", taskID);
				
				StringBuilder sb = new StringBuilder();
				
				for(ScoreStudentItem item : listViewData){
					sb.append(item.getName() + ",");
				}
				it.putExtra("userNames", sb.toString());
				it.putExtra("currentPage", position + 1);
				startActivityForResult(it,1);
				
			}
		});
		
		listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
				
				if(pageModel != null && pageModel.getLastItemIndex() == listViewData.size() -1
						&&scrollState == OnScrollListener.SCROLL_STATE_IDLE 
						&& pageModel.getCurrentPage() < sumPages){
					new AppendListViewData().execute();
				}
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
				if(pageModel != null){
					pageModel.setLastItemIndex(firstVisibleItem + visibleItemCount - 1);
				}
				
			}
		});
		
		scoredRadioButton.setOnCheckedChangeListener(new MyRadioButtonCheckedListener());
		unscoredRadioButton.setOnCheckedChangeListener(new MyRadioButtonCheckedListener());
		allscoreRadioButton.setOnCheckedChangeListener(new MyRadioButtonCheckedListener());
		filesRadioButton.setOnCheckedChangeListener(new MyRadioButtonCheckedListener());
		noFilesRadioButton.setOnCheckedChangeListener(new MyRadioButtonCheckedListener());
		allFilesRadioButton.setOnCheckedChangeListener(new MyRadioButtonCheckedListener());
		
	}
	
	private int loadDataForItemShowingView(String userID,List<ItemsShowingModel> data1,List<File> data2){
		
		List<Attachment> attachments = new ArrayList<Attachment>();
		
		WebService web = new WebService();
		//������񸽼��б�
		HashMap<String,String> map = new HashMap<String,String>();
		map.put("id", String.valueOf(taskID));
		map.put("type",String.valueOf(3));
		map.put("userID",userID);
		
		SoapObject result = web.CallWebService("getAttachment2", map);
		
		if(result != null){
			
			attachments = XMLParser.parseAttachment2(result);
			
			for(Attachment a : attachments){
				String url = a.getNewName();
				String fileName = a.getOriginName();
				
				//�����б���ʾ
				if(FileUtil.getFileType(fileName) == FileUtil.TYPE_DOCUMENT){
					
					if(data2 != null){
						MyFile file = new MyFile(Environment.getExternalStorageDirectory()
								+ "/wenfeng/" + application.getUserID() + "/download/" + fileName);
						
						if(url.contains("://")){
							url = url.substring(url.lastIndexOf("//") + 1);
						}
						
						file.setNewName(url);
						
						data2.add(file);
					}
					
					
				}else{//ͼƬԤ������Ƶ����
					
					if(data1 != null){
						ItemsShowingModel model = new ItemsShowingModel();
						model.setFileReaded(a.getIsFileRead());
						model.setOriginName(fileName);
							
						String path = Environment.getExternalStorageDirectory() + "/wenfeng/"
								+ application.getUserID() + "/download/";
						
						if(!url.contains("://")){
							url = "http://202.202.43.245/UploadFiles/Exercise/Feedback/" + url;
						}
						
						File file = new File(path + fileName);
						
						if(file.exists()){
							model.setUrl(file.getPath());
						}else{
							model.setUrl(url);
						}
						
						data1.add(model);
					}
				
				}
				
				
			}
			return SUCCESS;
		}
		
		return CONNECTION_FAIL;
		
	}
	
	@Override
	protected void onPause() {
		
		if(playingView != null){
			audioManager.stop();
			playingView.stop();
			playingView = null;
		}
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		
		case R.id.returnView : onBackPressed();break;
		case R.id.postScoreButton :
			
			if(isOpenFraction){
				new SetOpenfractionThread().execute(0);
			}else{
				new SetOpenfractionThread().execute(1);
			}
			break;
		case R.id.autoScoreButton :new AutoScoreThread().execute();break;
		case R.id.textView4:
		case R.id.attachment:
		case R.id.unreadTip:
		case R.id.unreadAttachment : 
			int position = (Integer)v.getTag();
			String userID = listViewData.get(position).getName().split("\\|")[0];
			
			Intent intent = new Intent(this,StudentUploadedFilesActivity.class);
			intent.putExtra("taskID", taskID);
			intent.putExtra("userID",userID);
			startActivity(intent);
			break;
		case R.id.feedbackButton :
			
			position = (Integer)v.getTag();
			Fragment fragment = getFragmentManager().findFragmentByTag("" + position);
			
			if(fragment == null){
		
				userID = listViewData.get(position).getName().split("\\|")[0];
				new InitialFeedbackFragment().execute(userID,"" + position);
				
			}else{
				getFragmentManager()
				.beginTransaction()
				.show(fragment)
				.commit();
			}
			
			break;
		
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == 1 && resultCode == Activity.RESULT_OK){
			
			if(data.getBooleanExtra("scoreChanged", false)){
				new GetListViewDataThread().execute();
			}
			
		}
		
	}

	/**
	 * ���listview item���ݵĺ�̨�߳�
	 */
	class GetListViewDataThread extends AsyncTask<Void,Void,Integer>{

		private final static int FAIL = -1;
		private final static int NO_NETWORK = -2;
		private final static int SUCCESS = 0;
		
		@Override
		protected void onPreExecute() {
			
			progressBar.setVisibility(View.VISIBLE);
		}


		@Override
		protected Integer doInBackground(Void... params) {
			
			if(!CheckNetwork.isConnectingToInternet(ScoreActivity.this)){
				return NO_NETWORK;
			}
			
			pageModel = new ScoreStuItemPageModel(taskID, scoreType, fileType);
			sumPages = pageModel.getSumPages();
			
			if(sumPages == PageModel.CONNECTION_FAIL){
				return FAIL;
			}
			
			List<ScoreStudentItem> tempList = pageModel.getDataList();
			
			if(tempList == null){
				return FAIL;
			}
			
			if(listViewData == null){
				listViewData = tempList;
			}else{
				listViewData.clear();
				listViewData.addAll(tempList);
			}
			
			try {
				isOpenFraction = WebServiceOperation.checkTaskScored(taskID);
			} catch (Exception e) {				
				return FAIL;
			}
					
			return SUCCESS;
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			switch(result){
			case FAIL :
				Toast.makeText(ScoreActivity.this,R.string.tip_connection_timeout
						,Toast.LENGTH_SHORT).show();
				break;
			case SUCCESS :
				
				if(isOpenFraction){
					postScoreButton.setText(R.string.hide_score);
				}else{
					postScoreButton.setText(R.string.post_score);
				}
				
				if(listViewAdapter == null){
					listViewAdapter = new ListViewAdapter();
					listView.setAdapter(listViewAdapter);
				}else{
					listViewAdapter.notifyDataSetChanged();
				}
				break;
			case NO_NETWORK :
				Toast.makeText(ScoreActivity.this
						,R.string.tip_network_unavailable,Toast.LENGTH_SHORT).show();
				
			}
			
		}
		
	}
	
	class AppendListViewData extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			
			if(listViewData != null){
				listViewData.addAll(pageModel.nextPage());
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			listViewAdapter.notifyDataSetChanged();
			listView.setSelection(pageModel.getLastItemIndex());
		}
		
	}
	
	/**
	 * �����������ط����ĺ�̨�߳�
	 */
	class SetOpenfractionThread extends AsyncTask<Integer,Void,Integer>{

		private static final int FAIL = -1;
		private static final int SUCCESS = 0;
		
		private int state;
		
		@Override
		protected Integer doInBackground(Integer... params) {
			
			state = params[0];
			WebService web = new WebService();
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("state",String.valueOf(params[0]));
			p.put("exerciseID",String.valueOf(taskID));
			
			SoapObject result = web.CallWebService("setOpenfraction", p);
			
			if(result == null){
				return FAIL;
			}
			
			if(XMLParser.parseBoolean(result).equals("true")){
				return SUCCESS;
			}else{
				return FAIL;
			}
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(result == FAIL){
				Toast.makeText(ScoreActivity.this
						,R.string.tip_connection_timeout,Toast.LENGTH_SHORT).show();
			}else{
				isOpenFraction = state == 1 ? true : false;
				
				if(state == 1){
					postScoreButton.setText(R.string.hide_score);
				}else{
					postScoreButton.setText(R.string.post_score);
				}
				
				listViewAdapter.notifyDataSetChanged();
			}
		}
		
	}
	
	//�Զ����ֵĺ�̨�߳�
	class AutoScoreThread extends AsyncTask<Void, Void, Integer>{
		
		private final static int FAIL = -1;
		private final static int SUCCESS = 1;

		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			WebService web = new WebService();
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("exerciseID", String.valueOf(taskID));
			
			SoapObject result = web.CallWebService("autoScore", p);
			
			if(result == null){
				return FAIL;
			}
			
			return XMLParser.parseBoolean(result).equalsIgnoreCase("true") ? SUCCESS : FAIL;
		}

		@Override
		protected void onPostExecute(Integer result) {
			progressBar.setVisibility(View.GONE);
			
			if(result == FAIL){
				Toast.makeText(ScoreActivity.this
						, R.string.tip_connection_timeout, Toast.LENGTH_SHORT).show();
			}else{
				new GetListViewDataThread().execute();
				Toast.makeText(ScoreActivity.this,
						R.string.auto_score_success, Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	/**
	 * ���ĳ��ѧ������Ӧ�ĸ����б�
	 */
	
	class GetAttachmentsForListView extends AsyncTask<Object, Void, Integer>{
	
		private WeakReference<View> reference;
		private WeakReference<AttachmentView> attachmentViewRef;
		private List<ItemsShowingModel> data = new ArrayList<ItemsShowingModel>();
		private List<File> mFiles = new ArrayList<File>();
		private String tag;

		@Override
		protected Integer doInBackground(Object... params) {

			String userID = (String)params[0];
			View view = (View) params[1];
			AttachmentView attachmentView = (AttachmentView) params[2];
			tag = (String) params[3];
			reference = new WeakReference<View>(view);
			attachmentViewRef = new WeakReference<AttachmentView>(attachmentView);
			return loadDataForItemShowingView(userID,data,mFiles) == SUCCESS ? 
					SUCCESS : CONNECTION_FAIL;
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			if(SUCCESS == result){
				
				View view = reference.get();
				
				if(view != null && listView.findViewWithTag(tag) != null){
					
					((FlowLayout)view).setAdapter(new ItemsShowingAdapter(
							ScoreActivity.this, data));
					
					AttachmentView a = attachmentViewRef.get();
					
					if(a != null){
						a.setDatas(mFiles, taskID, application.getUserID(), false
								,"http://202.202.43.245/UploadFiles/Exercise/Feedback"
								,12);
						a.setOnItemClickListener(new AttachmentView.SimpleOnItemClickListener() {
							
							@Override
							public void onDownloadStart(long downloadId, String newName) {
								ShowToastUtil.showDownloadStartToast(ScoreActivity.this);
								mDownloadQueue.append(downloadId, newName);
								MakeFileReadThread.start(newName);
							}
							
						});
					}
				}
				
			}
			
		}
		
	}
	
	/**
	 * ��ʼ������fragment
	 */
	class InitialFeedbackFragment extends AsyncTask<String, Void, Integer>{
		
		private ArrayList<ItemsShowingModel> data = new ArrayList<ItemsShowingModel>();
		private String tag;
		private String userID;

		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Integer doInBackground(String... params) {
			
			userID = params[0];
			tag = params[1];
			return loadDataForItemShowingView(userID,data,null) == SUCCESS ?
					SUCCESS : CONNECTION_FAIL;
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			
			if(SUCCESS == result){
				
				getFragmentManager()
				.beginTransaction()
				.add(R.id.container, FeedbackFragment.getInstance(data,userID,taskID),tag)
				.addToBackStack(null)
				.commit();
				
			}else{
				ShowToastUtil.showConnectionTimeOutToast(ScoreActivity.this);
			}
			
			
		}
		
	}

	
	/**
	 * �Զ���listViewAdapter
	 */
	class ListViewAdapter extends BaseAdapter{
		
		public class ViewHolder{
			
			public TextView nameView;
			public TextView submitTimeView;
			public TextView scoreView;
			public TextView readAttachmentView;
			public TextView readAttachmentTipView;
			public TextView unreadAttachmentTipView;
			public TextView unreadAttachmentView;
			public Button feedbackButton;
			public FlowLayout itemShowingView;
			public RoundProgressBar progressBar;
			public AttachmentView attachmentView;
			
		}

		@Override
		public int getCount() {
			
			return listViewData.size();
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
				
				convertView = LayoutInflater.from(ScoreActivity.this).inflate(
						R.layout.layout_score_student_item, parent,false);
				
				holder = new ViewHolder();
				holder.nameView = (TextView) convertView.findViewById(R.id.name);
				holder.submitTimeView = (TextView) convertView.findViewById(R.id.submitTime);
				holder.scoreView = (TextView) convertView.findViewById(R.id.score);
				holder.progressBar = (RoundProgressBar)convertView.findViewById(R.id.myProgress);
				holder.readAttachmentTipView = (TextView) convertView.findViewById(R.id.textView4);
				holder.readAttachmentView = (TextView) convertView.findViewById(R.id.attachment);
				holder.unreadAttachmentTipView = (TextView) convertView.findViewById(R.id.unreadTip);
				holder.unreadAttachmentView = (TextView) convertView.findViewById(R.id.unreadAttachment);
				holder.feedbackButton = (Button) convertView.findViewById(R.id.feedbackButton);
				holder.itemShowingView = (FlowLayout) convertView.findViewById(R.id.itemShowingView);
				holder.attachmentView = (AttachmentView) convertView.findViewById(R.id.attachmentView);
				
				convertView.setTag(holder);
				
			}else{
				holder = (ViewHolder)convertView.getTag();
			}
			
			ScoreStudentItem item = listViewData.get(position);
			
			//��ǰ���ŵ��������listview����(�����ɼ�)������Ҫֹͣ������Ƶ
			if(playingView != null){
				ViewParent flowLayout = (ViewParent) playingView.getParent().getParent();
				
				if(flowLayout == holder.itemShowingView){
					audioManager.stop();
					playingView.stop();
					playingView = null;
				}
				
			}
			
			holder.itemShowingView.setTag("tag" + position);
			holder.itemShowingView.setOnItemViewClickListener(new SimpleOnItemViewClickListener(ScoreActivity.this));
			holder.itemShowingView.removeAllViews();
			
			holder.attachmentView.removeAllViews();
			
			new GetAttachmentsForListView().execute(item.getName().split("\\|")[0]
					,holder.itemShowingView,holder.attachmentView
					,"tag" + position);
			
			
			holder.nameView.setText(item.getName());
			
			String time = item.getSubmitTime();
			holder.submitTimeView.setText(time.equals("null") ? "" : time);
			
			//�Ѿ������������ú�ɫ��ʾ
			if(isOpenFraction){
				holder.scoreView.setTextColor(ScoreActivity.this.getResources()
						.getColor(R.color.my_red));
			}else{
				holder.scoreView.setTextColor(ScoreActivity.this.getResources()
						.getColor(android.R.color.black));
			}
			holder.scoreView.setText(listViewData.get(position).getScore());
			
			int max = item.getMax();
			
			if(max <= 0){
				holder.progressBar.setVisibility(View.GONE);
				holder.feedbackButton.setVisibility(View.VISIBLE);
			}else{
				holder.progressBar.setMax(max);
				holder.progressBar.setProgress(item.getProcess());
			}
			
			holder.feedbackButton.setOnClickListener(ScoreActivity.this);
			holder.feedbackButton.setTag(position);
			
			holder.unreadAttachmentTipView.setTag(position);
			holder.unreadAttachmentView.setTag(position);
			holder.readAttachmentTipView.setTag(position);
			holder.readAttachmentView.setTag(position);
			
			int readCount = item.getReadedAttachmentCount();
			int unreadCount = item.getNotReadedAttachmentCount();
			
			if(readCount == 0 && unreadCount == 0){
				holder.unreadAttachmentTipView.setVisibility(View.GONE);
				holder.unreadAttachmentView.setVisibility(View.GONE);
				holder.readAttachmentTipView.setVisibility(View.GONE);
				holder.readAttachmentView.setVisibility(View.GONE);
			}else{
				holder.unreadAttachmentTipView.setVisibility(View.VISIBLE);
				holder.unreadAttachmentView.setVisibility(View.VISIBLE);
				holder.readAttachmentTipView.setVisibility(View.VISIBLE);
				holder.readAttachmentView.setVisibility(View.VISIBLE);
				
				holder.readAttachmentView.setText(String.valueOf(readCount));
				holder.unreadAttachmentView.setText(String.valueOf(unreadCount));
			}
			
			holder.readAttachmentTipView.setOnClickListener(ScoreActivity.this);
			holder.readAttachmentView.setOnClickListener(ScoreActivity.this);
			holder.unreadAttachmentTipView.setOnClickListener(ScoreActivity.this);
			holder.unreadAttachmentView.setOnClickListener(ScoreActivity.this);

			return convertView;
		}
		
	}
	
	
	private class MyRadioButtonCheckedListener implements OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			
			if(isChecked){
				
				if(playingView != null){
					audioManager.stop();
					playingView.stop();
					playingView = null;
				}
				
				switch (buttonView.getId()) {
				case R.id.scored : scoreType = "1";break;
				case R.id.unscored : scoreType = "2";break;
				case R.id.allScore : scoreType = "0";break;
				case R.id.files : fileType = "1";break;
				case R.id.noFiles : fileType = "2";break;
				case R.id.allFiles : fileType = "0";break;
				}
				
				new GetListViewDataThread().execute();
			}
			
			
		}
		
	}
	
	public static class SimpleOnItemViewClickListener implements FlowLayout.OnItemViewClickListener{
    	
    	private Context mContext;
    	
    	public SimpleOnItemViewClickListener(Context context){
    		
    		mContext = context;
    	}
		
    	private void handleAudioPlaying(View v,Uri uri){
    		
    		if(!isAudioPreparing){
    			TagView tagView = (TagView)v;
    			MyAudioPlayView audioView = (MyAudioPlayView) tagView.getChildAt(0);
    			
    			//��ʱû����Ƶ�ڲ���
    			if(playingView == null){
    				playingView = audioView;
    				
    				//��Ƶ��������δ��ʼ����
    				if(audioManager == null){
    					audioManager = new AudioPlayManager();
    					audioManager.setOnPreparedListener(new OnPreparedListener() {
							
							@Override
							public void onPrepared(MediaPlayer mp) {
								
								if(playingView != null){
									isAudioPreparing = false;
									audioManager.play();
									playingView.setMaxProcess(mp.getDuration()/1000);
									playingView.play();	
								}
								
								
							}
						});
    					
    					audioManager.setOnCompletionListener(new OnCompletionListener() {
							
							@Override
							public void onCompletion(MediaPlayer mp) {
								
								if(playingView != null){
									playingView.stop();
								}
								
							}
						});
    				}
    				
    				try {
						audioManager.setUri(mContext, uri);
						isAudioPreparing = true;
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
    			}else{//����Ƶ���ڲ���
    				audioManager.stop();
    				playingView.stop();
    				
    				//������ǵ�ǰ���ڲ��ŵ���Ƶ
    				if(playingView == audioView){
    					playingView = null;
    				}else{
    					playingView = audioView;
    					try {
							audioManager.setUri(mContext, uri);
							isAudioPreparing = true;
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
						} catch (IllegalStateException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
    				}
    			}
    		}
    		
    	}
    	
		private void startMyActivity(int type,List<ItemsShowingModel> data,int position){
			
			ArrayList<String> urls = new ArrayList<String>();
			ArrayList<String> originNames = new ArrayList<String>();
			
			for(ItemsShowingModel model : data){
				
				String url = model.getUrl();
				String originName = model.getOriginName();
				if(FileUtil.getFileType(url) == type){
					urls.add(url);
					originNames.add(originName);
				}
				
			}
			
			int index = urls.indexOf(data.get(position).getUrl());  
			
			Intent intent = new Intent(mContext,FullScreenPreviewActivity.class);
			intent.putExtra(FullScreenPreviewActivity.INTENT_FRAGMENT_TYPE, type);
			intent.putExtra(FullScreenPreviewActivity.INTENT_URLS, urls);
			intent.putExtra(FullScreenPreviewActivity.INTENT_ORIGIN_NAMES, originNames);
			intent.putExtra(FullScreenPreviewActivity.INTENT_ITEM_POSITION, index == -1 ? 0 : index);
			mContext.startActivity(intent);
		}
		
		@Override
		public void onClick(View view,List<ItemsShowingModel> data,int position) {
			
			String url = data.get(position).getUrl();
			boolean isRead = data.get(position).isFileReaded();
			
			//���ļ����Ϊ�Ѷ�
			if(!isRead){
				
				String fileNewName = url.contains("202.202.43.245") ? url.substring(url.lastIndexOf("/") + 1) 
						: url;
				MakeFileReadThread.start(fileNewName);
				FrameLayout tagView = (FrameLayout)view;
				tagView.getChildAt(1).setVisibility(View.GONE);
				
			}
			
			int fileType = FileUtil.getFileType(url);
			
			switch (fileType) {
			case FileUtil.TYPE_VIDEO:
				startMyActivity(FileUtil.TYPE_VIDEO,data,position);
				break;
			case FileUtil.TYPE_AUDIO:
				handleAudioPlaying(view,Uri.parse(url));
				break;
			case FileUtil.TYPE_IMAGE:
				startMyActivity(FileUtil.TYPE_IMAGE,data,position);
				break;
			}
			
		}
	
	}

}
