package com.cqupt.ui.student;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.Attachment;
import com.cqupt.model.FeedBack;
import com.cqupt.model.Task;
import com.cqupt.model.Test;
import com.cqupt.net.WebService;
import com.cqupt.ui.common.AudioRecordActivity;
import com.cqupt.ui.common.CommentListActivity;
import com.cqupt.ui.common.FileExploreActivity;
import com.cqupt.ui.common.FileExploreActivity.MyFile;
import com.cqupt.ui.common.PreviewPictureActivity;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.DeadlineCheck;
import com.cqupt.util.DownloadMyAnswers;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.util.SingleTaskUpdate;
import com.cqupt.util.SingleTaskUpdate.OnProcessChangeListener;
import com.cqupt.util.XMLParser;
import com.cqupt.view.AttachmentView;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskDetailActivity extends Activity implements OnClickListener{
	
	private MyApplication application;

	private TextView returnView;//���ذ�ť���
	private TextView titleView;//�������
	private TextView createUserView;//���������
	private TextView sumCountView;//����Ŀ�����
	private TextView finishedCountView;//�����Ŀ�����
	private TextView sDateView;//��ʼʱ�����
	private TextView eDateView;//����ʱ�����
	private TextView answersUploadTimeView;//���ϴ�ʱ�����
	private TextView contentView;//��ҵ�������
	private TextView remarkView;//��ҵ��ע���
	private Button startButton;//��ʼ���ⰴť
	private ImageButton commentButton;//���۰�ť
	private TextView syncView;//ͬ�����
	private ProgressBar progressBar;//ͬ��ʱ�Ľ�����
	private LinearLayout attachmentView;//������
	private TextView feedBackView;//������Ϣ��ͼ
	private TextView scoreView;//�÷���ͼ
	private TextView scoreTimeView;//��������
	private LinearLayout uploadView;//������������ť
	private RelativeLayout toolBarView;//��������ͼ
	private ImageView cameraImage;//��������ͷ��ť
	private ImageView voiceImage;//����¼������ť
	private ImageView fileImage;//�����ļ�����ť
	
	private int taskID;
	private Map<Long,String> downloadQueue;//���ض���
	private String uploadFileName;//�ϴ����ļ���(���պ�̬����)
	private DBManager db;
	private boolean isDeadline;//��ҵ�Ƿ��ֹ�ı�־
	
	/**
	 * �������ظ������֪ͨ��receiver
	 */
	private BroadcastReceiver mReceiver;
	private MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(this);
	
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);//ȡ��������
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_detail);
		findView();
		initial();
		loadData();
		setListener();
		
	}
	
    /**
	 *��ʼ�������,��һ������Activityʱ��ִ��,������oncreateʱ
	 */
	@SuppressLint("UseSparseArrays")
	private void initial(){
		
		application = (MyApplication)getApplication();
		downloadQueue = new HashMap<Long, String>();
		
		mReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle bundle = intent.getExtras();
				long id = bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
				if(downloadQueue.containsKey(id)){
					
					String newName = downloadQueue.get(id);
					
					if(newName != null){
						
						View view = attachmentView.findViewWithTag(newName);
						
						//���سɹ��� ��item�ָ��ɵ��״̬
						TextView contentView = (TextView) view.findViewById(R.id.content);
						contentView.setEnabled(true);
						contentView.setTextColor(Color.BLACK);
						
						Toast.makeText(TaskDetailActivity.this,
								R.string.tip_attachment_download_success,Toast.LENGTH_SHORT).show();
					}
					
				}
			}
		};
		
		registerReceiver(mReceiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		Intent it = getIntent();
		taskID = it.getIntExtra("taskID",0);
		db = new DBManager(helper.getConnection());
		
		//���ô��ϴ���ʱ��
		String answersUploadTime = db.getAnswersUploadTime(application.getUserID(),taskID);
		if(answersUploadTime != null){
			answersUploadTimeView.setText(answersUploadTime);
		}
		
		
	}
	public void findView(){
		
		titleView = (TextView)findViewById(R.id.title);
		createUserView = (TextView)findViewById(R.id.createUser);
		sumCountView = (TextView)findViewById(R.id.sumCount);
		finishedCountView = (TextView)findViewById(R.id.finishedCount);
		sDateView = (TextView)findViewById(R.id.startDate);
		eDateView = (TextView)findViewById(R.id.endDate);
		answersUploadTimeView = (TextView)findViewById(R.id.answerUploadDate);
		contentView = (TextView)findViewById(R.id.taskContent);
		remarkView = (TextView)findViewById(R.id.taskRemark);
		startButton = (Button)findViewById(R.id.startButton);
		returnView = (TextView)findViewById(R.id.returnView);
		commentButton= (ImageButton)findViewById(R.id.commentButton);
		syncView = (TextView)findViewById(R.id.syncView);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		attachmentView = (LinearLayout)findViewById(R.id.lineFour);
		feedBackView = (TextView)findViewById(R.id.feedBack);
		scoreView = (TextView)findViewById(R.id.score);
		scoreTimeView = (TextView)findViewById(R.id.scoreTime);
		uploadView = (LinearLayout)findViewById(R.id.uploadView);
		toolBarView = (RelativeLayout)findViewById(R.id.bottomToolbar);
		cameraImage = (ImageView)findViewById(R.id.cameraImage);
		voiceImage = (ImageView)findViewById(R.id.voiceImage);
		fileImage = (ImageView)findViewById(R.id.fileImage);
		
	}
	public void loadData(){
		
		sumCountView.setText(String.valueOf(db.getTestsCount(taskID)));
		finishedCountView.setText(String.valueOf(db.getFinishedTestCount(application.getUserID(),taskID)));
		Task task = db.getTaskDetail(taskID);
		
		isDeadline = DeadlineCheck.check(task.getEndDate());
		titleView.setText(task.getTitle());
		createUserView.setText(task.getCreateUser());
		sDateView.setText(task.getStartDate());
		eDateView.setText(task.getEndDate());
		
		//���Ѿ���ֹ����ҵ,����ֹ�����ú�ɫ��ʾ
		if(isDeadline){
			eDateView.setTextColor(getResources().getColor(R.color.my_red));
		}
		contentView.setText(Html.fromHtml(task.getContent()));
		remarkView.setText(Html.fromHtml(task.getRemark()));
		
		String p = db.getProcessPercent(taskID,application.getUserID());
		
		//����û��������ҵ,����ʾ�����ͬ����ť
		if(p == null){
			startButton.setVisibility(View.GONE);
			syncView.setVisibility(View.GONE);			
		}else{
			//��ҵ�Ѿ���ֹ,��������ͬ����,�ϴ��ļ�,������ʾ�鿴�����ť
			if(isDeadline){
				syncView.setVisibility(View.GONE);
				//uploadView.setVisibility(View.GONE);
				startButton.setText(R.string.answers);
			}else{
				startButton.setText(R.string.start_the_answer);
			}
		}
		//�������������������۰�ť
		if(task.getIsDiscuss().equalsIgnoreCase("false")){
			commentButton.setVisibility(View.GONE);
		}
		makeAttachmentLayout();
		
		FeedBack feedBack = db.getFeedBack(application.getUserID(),taskID);
		String score = feedBack.getScore();
		
		//�Ѿ�����������ʦ�Ѿ���������
		if(!(score == null||"".equals(score)) && feedBack.getOpenFraction().equalsIgnoreCase("true")){
			scoreView.setText(score);//��ʾ����
		}
		
		String fContent = feedBack.getFeedBackContent();
		String scoreTime = feedBack.getScoreTime();
		String time = (scoreTime == null ? "" :scoreTime.split(" ")[0]);
		feedBackView.setText(fContent == null ? "" : fContent);//��ʾ������Ϣ
		scoreTimeView.setText(time);//��ʾ����ʱ��
		
	}
	
   /**
	*��̬���ɸ����б���ͼ
	*/
	private void makeAttachmentLayout(){
		
		List<File> attachments = new ArrayList<File>();
		String path = Environment.getExternalStorageDirectory() + "/wenfeng/" + application.getUserID() +
				"/download/";
		
		for(Attachment a : db.getAttachment(taskID)){
			MyFile file = new MyFile(path + a.getOriginName());
			file.setNewName(a.getNewName());
			attachments.add(file);
		}
		
		AttachmentView child = new AttachmentView(TaskDetailActivity.this);
		
		child.setDatas(attachments,taskID,application.getUserID(),false
				,"http://202.202.43.245/UploadFiles/Exercise/Define",0);
		
		child.setOnItemClickListener(new AttachmentView.OnItemClickListener() {
			
			@Override
			public void onDeleteStart() {}
	
			@Override
			public void onDeleteFail() {}

			@Override
			public void onDeleteSucccess() {}

			@Override
			public void onDownloadStart(long downloadId,String newName) {
				
				Toast.makeText(TaskDetailActivity.this, R.string.tip_start_download
						,Toast.LENGTH_SHORT).show();
				downloadQueue.put(downloadId, newName);
				
			}
	});
		
		attachmentView.addView(child);
	}
	
	public void setListener(){
		
		returnView.setOnClickListener(this);
		startButton.setOnClickListener(this);
		commentButton.setOnClickListener(this);
		syncView.setOnClickListener(this);
		uploadView.setOnClickListener(this);
		cameraImage.setOnClickListener(this);
		voiceImage.setOnClickListener(this);
		fileImage.setOnClickListener(this);
		
	}
	/**
	 *���ش𰸵ĺ�̨�߳�
	 */
	class DownloadThread extends AsyncTask<Integer,Integer,Integer>{
		
		private DBManager db = new DBManager(helper.getConnection());
		
		@Override
		protected Integer doInBackground(Integer... params) {
			
			return new DownloadMyAnswers(taskID,db,application.getUserID()).download();
			
		}
		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			switch(result){
			case DownloadMyAnswers.CONNECTION_TIME_OUT : 
				Toast.makeText(getApplicationContext(),
						R.string.tip_connection_timeout,Toast.LENGTH_SHORT).show();break;
			case DownloadMyAnswers.DOWNLOAD_SUCCESS : 
				Toast.makeText(getApplicationContext(),
						R.string.tip_attachment_download_success,Toast.LENGTH_SHORT).show();
				//������Ŀ�����
				DBManager db = new DBManager(helper.getConnection());
				finishedCountView.setText(String.valueOf(db.getFinishedTestCount(application.getUserID(),taskID)));
			}
		}
	}
	/**
	 *�ϴ��𰸵ĺ�̨�߳�
	 */
	class UploadThread extends AsyncTask<Integer,Integer,Integer>{
		
		private static final int NO_ANSWERS = 0;
		private static final int UPLOAD_SUCCESS = 1;
		private static final int CHANGES_FROM_WEB = -1;
		private static final int CONNECTION_FAIL = -2;
		
		private ArrayList<Test> testList;//�洢����б�
		private ArrayList<HashMap<String, String>> questionList;//������Ŀ��ѡ���
		private DBManager db = new DBManager(helper.getConnection());
		private WebService web = new WebService();
		
		@Override
		protected Integer doInBackground(Integer... params) {
			
			ArrayList<Integer> tests = new ArrayList<Integer>();
			ArrayList<Integer> items = new ArrayList<Integer>();
			ArrayList<Integer> options = new ArrayList<Integer>();
			
			//��ñ��صĸô����������testID��itemID��optionID ���ں�web�˶Ա�
			tests = db.getTestListIDs(taskID);
			for(int id : tests){
				items.addAll(db.getItemListIDs(id));
			}
			for(int id : items){
				options.addAll(db.getOptionListIDs(id));
			}
			
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("userID", application.getUserID());
			p.put("taskID", String.valueOf(taskID));
			p.put("testIDs", tests.toString().replace(" ",""));
			p.put("itemIDs", items.toString().replace(" ",""));
			p.put("optionIDs", options.toString().replace(" ",""));
			SoapObject result = web.CallWebService("checkTaskChanged",p);
			String r = XMLParser.parseBoolean(result);
			
			if(r == null){
				return CONNECTION_FAIL;
			}
			
			//���web����ҵû�з����ı�������ϴ���
			if(r.equalsIgnoreCase("false")){
				testList = db.getTestListByTaskID(taskID);//���ݿ��ȡ����б�
				int s = 0;//��¼�û��Ƿ��Ѿ�������Ŀ
				for(Test test:testList){
					questionList = db.getQuestions(test.getTestID());
					for(int i = 0;i < questionList.size();i++){
						String answer = db.getAnswer(application.getUserID(),
								Integer.parseInt(questionList.get(i).get("itemID")),taskID);
						//�û���δ����,���ϴ���
						if("".equals(answer)) continue;
						s = 1;
						p = new HashMap<String,String>();
						p.put("taskID", String.valueOf(taskID));
						p.put("testItemID", questionList.get(i).get("itemID"));
						p.put("userID", application.getUserID());
						p.put("answer",answer);
						p.put("submitTime", db.getSubmitTime(application.getUserID(),
								taskID, Integer.parseInt(questionList.get(i).get("itemID"))));
						result = web.CallWebService("uploadMyChoice",p);
						//���ӷ�������ʱ
						if(result == null){
							return CONNECTION_FAIL;
						}
					}
				}
				//�������û��ύ�׾�
				if(s == 0){
					return NO_ANSWERS;
				}else{
					return UPLOAD_SUCCESS;
				}
			}
			return CHANGES_FROM_WEB;
		}
		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			if(result == CONNECTION_FAIL){
				Toast.makeText(TaskDetailActivity.this,
						R.string.tip_connection_timeout,Toast.LENGTH_SHORT).show();
			}else{
				if(result == NO_ANSWERS){
					Toast.makeText(TaskDetailActivity.this,R.string.tip_upload_no_answer,Toast.LENGTH_LONG).show();
				}else{
					if(result == UPLOAD_SUCCESS){
						//��ʽ�����ϴ�ʱ��
						Date date = new Date();
						SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
						String uploadTime = format.format(date);
						
						db.setAnswersUploadTime(application.getUserID(),taskID, uploadTime);//���ݿ��¼���ϴ�ʱ��
						answersUploadTimeView.setText(uploadTime);//���½���
						Toast.makeText(TaskDetailActivity.this,
								R.string.tip_upload_answers_success,Toast.LENGTH_SHORT).show();
					}else{
						showDialog();
					}
				}	
			}
		}
		
		private void showDialog(){
			AlertDialog  log = new AlertDialog.Builder(TaskDetailActivity.this)
			                                 .setMessage(R.string.tip_changes_from_web)
			                                 .setPositiveButton(R.string.confirm,new DialogInterface.OnClickListener() {
												
												public void onClick(DialogInterface dialog, int which) {
													new UpdateTaskThread().execute();
												}
											})
			                                 .setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
			                                	 
												public void onClick(DialogInterface dialog, int which) {
												}	
											})
			                                 .create();
			log.show();
		}
	}
	/**
	 *��ȡ������Ϣ�ĺ�̨�߳�
	 */
	class GetFeedBackThread extends AsyncTask<Integer,String,Integer>{
		
		private int NOT_SCORED = 0;
		private int CONNECTION_FAIL = 1;
		private int SUCCESS = 2;

		@Override
		protected Integer doInBackground(Integer... params) {
			WebService web = new WebService();
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("taskID",String.valueOf(taskID));
			p.put("userID",application.getUserID());
			SoapObject result = web.CallWebService("getFeedback", p);
			//���ӷ�������ʱ
			if(result == null){
				return CONNECTION_FAIL;
			}
			FeedBack feedBack = XMLParser.parseFeedBack(result);
			String score = feedBack.getScore();
			String fContent = feedBack.getFeedBackContent();
			String time = (feedBack.getScoreTime().split(" "))[0];
			String openFraction = feedBack.getOpenFraction();
			//��ʦ��δ���
			if(openFraction.equalsIgnoreCase("False")){
				return NOT_SCORED;
			}
			//��ʦ�Ѹ�������,���������ڱ������ݿ�
			DBManager db = new DBManager(helper.getConnection());
			feedBack.setTaskID(taskID);
			
			db.addFeedBack(application.getUserID(),feedBack);
			publishProgress(score,fContent,time);
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {

			if(result == CONNECTION_FAIL){
				Toast.makeText(TaskDetailActivity.this,
						R.string.tip_connection_timeout,Toast.LENGTH_SHORT).show();
			}else{
				//if(result == NOT_SCORED){
					//Toast.makeText(TaskDetailActivity.this,
							//R.string.tip_exercise_checking,Toast.LENGTH_SHORT).show();
				//}else{
					
					Intent it = new Intent(TaskDetailActivity.this,DoneTaskActivity.class);
					it.putExtra("ID",taskID);
					startActivity(it);
				//}
			}
		}

		@Override
		protected void onProgressUpdate(String... values) {
			scoreView.setText(values[0]);//��ʾ����
			feedBackView.setText(values[1]);//��ʾ������Ϣ
			scoreTimeView.setText(values[2]);//��ʾ����ʱ��	
		}
		
	}
	
	/**
	 *���θ�����ҵ�ĺ�̨�߳�
	 */
	class UpdateTaskThread extends AsyncTask<Void,Integer,Integer>{
		
		private final int CONNECTION_TIMEOUT = 0; 
		private final int UPDATE_SUCCESS = 1;
		private final int TASK_CANCELED = 2;

		protected Integer doInBackground(Void... params) {
			publishProgress(0);
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("taskID",String.valueOf(taskID));
			p.put("userID",application.getUserID());
			WebService web = new WebService();
			SoapObject result = web.CallWebService("getTask",p);
			if(result == null){
				return CONNECTION_TIMEOUT;
			}else{
				ArrayList<Task> task = XMLParser.parseTaskList(result);
				//��ʦ�Ѿ�ȡ����ѧ���ô���ҵ
				if(task == null){
					return TASK_CANCELED;
				}else{
					new SingleTaskUpdate(db,new OnProcessChangeListener() {
						
						public void onProcessChange(int process) {	
						}
					},application.getUserID()).updateTask(task.get(0));
				}
			 }
			return UPDATE_SUCCESS;
		}

		protected void onPostExecute(Integer result) {
			progressBar.setVisibility(View.GONE);
			switch(result){
			case CONNECTION_TIMEOUT : 
				ShowToastUtil.showConnectionTimeOutToast(TaskDetailActivity.this);break;
			case UPDATE_SUCCESS : 
				Toast.makeText(TaskDetailActivity.this,R.string.tip_update_success, Toast.LENGTH_SHORT).show();
				//���¼�������ǰ����ո����б�
				attachmentView.removeViews(1, attachmentView.getChildCount()-1);
				loadData();//���º����¼���һ������
				break;
			case TASK_CANCELED : 
				Toast.makeText(TaskDetailActivity.this,R.string.tip_task_cancel, Toast.LENGTH_LONG).show();
				//ɾ�����ݿ��������
				db.deleteTaskUserArrange(application.getUserID(),taskID);
				db.deleteMyChoice(application.getUserID(),taskID);
				db.deleteUploadFiles(application.getUserID(),taskID);
				finish();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressBar.setVisibility(View.VISIBLE);
		}
		
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if(toolBarView.getVisibility() == View.VISIBLE){
			toolBarView.setVisibility(View.GONE);
		}
		finishedCountView.setText(String.valueOf(
				db.getFinishedTestCount(application.getUserID(),
				taskID)));
	}
	@Override
	public void onBackPressed() {

		if(toolBarView.getVisibility() == View.VISIBLE){
			toolBarView.setVisibility(View.GONE);
		}else{
			super.onBackPressed();
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1 && resultCode == RESULT_OK){
			Intent it = new Intent(this,PreviewPictureActivity.class);
			it.putExtra("filePath", Environment.getExternalStorageDirectory().getPath()
					+"/wenfeng/"+application.getUserID()+"/upload/"+uploadFileName);
			it.putExtra("taskID",taskID);
			it.putExtra("isDeadline", isDeadline);
			startActivity(it);
		}
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.returnView : finish();break;
		case R.id.startButton : 
			String s = startButton.getText().toString();
			if(s.equals(getResources().getString(R.string.start_the_answer))){
				Intent it = new Intent(this,DoingTaskActivity.class);
				it.putExtra("ID", taskID);
				startActivity(it);
			}else{
				DBManager db = new DBManager(helper.getConnection());
				String score = db.getFeedBack(application.getUserID(),taskID).getScore();
				//��һ�ε���鿴���(��δ�ӷ��������ؽ�ʦ������Ϣ)
				if(score == null ||score.equals("")){
					if(!CheckNetwork.isConnectingToInternet(this)){
						Toast.makeText(this,R.string.tip_network_unavailable
								, Toast.LENGTH_SHORT).show();
					}else{
						new GetFeedBackThread().execute(0);
					}
				}else{
					Intent it = new Intent(this,DoneTaskActivity.class);
					it.putExtra("ID", taskID);
					startActivity(it);
				}
			}
			break;
		case R.id.commentButton : 
			Intent it = new Intent(this,CommentListActivity.class);
			it.putExtra("ID", taskID);
			it.putExtra("type",1);
			startActivity(it);
			break;
		case R.id.syncView : 
			if(!CheckNetwork.isConnectingToInternet(this)){
				Toast.makeText(this,R.string.tip_network_unavailable
						, Toast.LENGTH_SHORT).show();
			}else{
				AlertDialog log = new AlertDialog.Builder(TaskDetailActivity.this)
				.setItems(new String[]{
						TaskDetailActivity.this.getResources().getString(R.string.menu_download_answers)
					   ,TaskDetailActivity.this.getResources().getString(R.string.menu_upload_answers)
					   ,TaskDetailActivity.this.getResources().getString(R.string.menu_update_exercises)
				                       }
						 ,new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						progressBar.setVisibility(View.VISIBLE);
						switch(which){
						case 0 : new DownloadThread().execute(0);break;
						case 1 : new UploadThread().execute(0);break;
						case 2 : new UpdateTaskThread().execute();
						}
					}
				})					
				.create();
				log.show();
			}
			break;
		case R.id.uploadView :
			
			if(isDeadline){
				Intent intent = new Intent(this,UploadedFileActivity.class);
				intent.putExtra("taskID",taskID);
				startActivity(intent);
			}else{
				if(toolBarView.getVisibility() == View.GONE){
					toolBarView.setVisibility(View.VISIBLE);
				}else{
					toolBarView.setVisibility(View.GONE);
				}
			}
			
			break;
		case R.id.cameraImage :
			// ��֤sd���Ƿ���ȷ��װ��
		    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
		    	Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
		    	File file = new File(Environment.getExternalStorageDirectory().getPath()
		    			+"/wenfeng/"+application.getUserID()+"/upload");
		    	if(!file.exists()){
		    		file.mkdirs();
		    	}
		    	//����ͼƬ��
		    	Date date = new Date();
		    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		    	uploadFileName = "PIC_"+format.format(date)+".jpg";
	            //ָ����Ƭ���λ��(�����ļ�������)
		    	intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.withAppendedPath(Uri.fromFile(file), uploadFileName));
		    	startActivityForResult(intent, 1);
		    } else {
		    	Toast.makeText(TaskDetailActivity.this,
		    			R.string.tip_sdcard_not_exist, Toast.LENGTH_SHORT).show();
		    }
		    break;
		case R.id.voiceImage :
			Intent it2 = new Intent(this,AudioRecordActivity.class);
			it2.putExtra("taskID",taskID);
			it2.putExtra("isDeadline", isDeadline);
			startActivity(it2);
			break;
		case R.id.fileImage : 
			Intent it3 = new Intent(this,FileExploreActivity.class);
			it3.putExtra("taskID",taskID);			
			startActivity(it3);
			break;
		}
	}
}
