package com.cqupt.ui.teacher;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.util.LongSparseArray;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.cqupt.model.Task;
import com.cqupt.net.WebService;
import com.cqupt.ui.common.AudioRecordActivity;
import com.cqupt.ui.common.CommentListActivity;
import com.cqupt.ui.common.FileExploreActivity;
import com.cqupt.ui.common.PreviewPictureActivity;
import com.cqupt.util.DeadlineCheck;
import com.cqupt.util.LoadUploadedFiles;
import com.cqupt.util.XMLParser;
import com.cqupt.view.AttachmentView;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TeacherTaskDetailActivity extends Activity implements OnClickListener{
	
	private static final int EDIT_TASK = 1;
	private static final int UPLOAD_FILE = 2;
	private static final int CAMERA = 3;
	private static final int PICTURE_PREVIEW = 4;
	private static final int AUDIO_RECORD = 5;
	
	private MyApplication application;
	
	private TextView scoreView;
	private TextView returnView;
	private TextView titleView;
	private TextView stuCountView;
	private TextView testCountView;
	private TextView sDateView;
	private TextView eDateView;
	private TextView contentView;
	private TextView remarkView;
	private CheckBox isDiscussView;
	private Button editButton;
	private ImageButton commentView;
	private ProgressBar progressBar;
	private LinearLayout uploadView;
	private LinearLayout attachmentView;
	private RelativeLayout bottomToolBar;
	private ImageView fileImageView;
	private ImageView cameraImageView;
	private ImageView voiceImageView;
	
	private int taskID;
	private Task task;
	private String uploadFileName;//�ϴ����ļ���(���պ�̬����)
	private boolean isDeletingAttachment = false;
	private LongSparseArray<String> downloadQueue;//�������ض���
	/**
	 * �������ظ������֪ͨ��receiver
	 */
	private BroadcastReceiver mReceiver;
	
	protected void onCreate(Bundle savedInstanceState){
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_teacher_task_detail);
		
		findView();
		loadData();
		setListener();
	}
	
	private void findView(){
		
		scoreView = (TextView) findViewById(R.id.score);
		returnView = (TextView) findViewById(R.id.returnView);
		titleView = (TextView) findViewById(R.id.title);
		stuCountView = (TextView) findViewById(R.id.studentCount);
		testCountView = (TextView) findViewById(R.id.examCount);
		sDateView = (TextView) findViewById(R.id.startDate);
		eDateView = (TextView) findViewById(R.id.endDate);
		contentView = (TextView) findViewById(R.id.taskContent);
		remarkView = (TextView) findViewById(R.id.taskRemark);
		isDiscussView = (CheckBox) findViewById(R.id.isDiscuss);
		editButton = (Button) findViewById(R.id.editButton);
		commentView = (ImageButton) findViewById(R.id.commentButton);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		uploadView = (LinearLayout)findViewById(R.id.uploadView);
		attachmentView = (LinearLayout)findViewById(R.id.lineFour);
		bottomToolBar = (RelativeLayout) findViewById(R.id.bottomToolbar);
		fileImageView = (ImageView) findViewById(R.id.fileImage);
		cameraImageView = (ImageView) findViewById(R.id.cameraImage);
		voiceImageView = (ImageView) findViewById(R.id.voiceImage);
				
	}
	
	private void loadData() {
		
		taskID = getIntent().getIntExtra("taskID",0);
		application = (MyApplication)getApplication();
		downloadQueue = new LongSparseArray<String>();
		
		mReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent) {
				
				Long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
				String newName = downloadQueue.get(id);
				
				if( newName != null){
					
					View view = attachmentView.getChildAt(1).findViewWithTag(newName);
					
					if(view != null){
						
						//�ɹ����غ��ð�ť�ָ�enable״̬
						TextView contentView = (TextView) view.findViewById(R.id.content);
						contentView.setEnabled(true);
						contentView.setTextColor(Color.BLACK);
						
						ImageView downloadImageView = (ImageView) view.findViewById(R.id.downloadImageView);
						downloadImageView.setEnabled(true);
						downloadImageView.setVisibility(View.GONE);
						
						view.findViewById(R.id.imageView).setEnabled(true);
						
					}
					
					downloadQueue.remove(id);
					
					Toast.makeText(TeacherTaskDetailActivity.this,
							R.string.tip_attachment_download_success,Toast.LENGTH_SHORT).show();
				}
				
			}
		};
		registerReceiver(mReceiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		new GetTaskInfoThread().execute();
		new GetAttachmentList().execute();
		
	}
	
	private void setListener() {
		
		scoreView.setOnClickListener(this);
		returnView.setOnClickListener(this);
		editButton.setOnClickListener(this);
		commentView.setOnClickListener(this);
		uploadView.setOnClickListener(this);
		fileImageView.setOnClickListener(this);
		cameraImageView.setOnClickListener(this);
		voiceImageView.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.returnView : onBackPressed();break;
		case R.id.score : 
			
			//��ҵ�Ѿ�������ѧ��
			if(!stuCountView.getText().toString().equals("0")){
				Intent it1 = new Intent(this,ScoreActivity.class);
				it1.putExtra("title", task.getTitle());
				it1.putExtra("taskID",taskID);
				startActivity(it1);
			}else{			//������ҵû�а���ѧ��
				Toast.makeText(this,R.string.no_arranged_students,Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.editButton :
			Intent it = new Intent(this,EditTaskActivity.class);
			it.putExtra("taskID",taskID);
			startActivityForResult(it,EDIT_TASK);
			break;
		case R.id.commentButton :
			Intent it2 = new Intent(this,CommentListActivity.class);
			it2.putExtra("ID",taskID);
			it2.putExtra("type", 1);
			startActivity(it2);
			break;
		case R.id.uploadView : 
			
			if(bottomToolBar.getVisibility() == View.VISIBLE){
				bottomToolBar.setVisibility(View.GONE);
			}else{
				bottomToolBar.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.fileImage :
			bottomToolBar.setVisibility(View.GONE);
			it = new Intent(this,FileExploreActivity.class);
			it.putExtra("taskID", taskID);
			startActivityForResult(it, UPLOAD_FILE);
			break;
		case R.id.cameraImage : 
			bottomToolBar.setVisibility(View.GONE);
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
		    	intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.withAppendedPath(Uri.fromFile(file)
		    			, uploadFileName));
		    	startActivityForResult(intent, CAMERA);
		    } else {
		    	Toast.makeText(TeacherTaskDetailActivity.this,
		    			R.string.tip_sdcard_not_exist, Toast.LENGTH_SHORT).show();
		    }
		    break;
		case R.id.voiceImage : 
			bottomToolBar.setVisibility(View.GONE);
			it = new Intent(this,AudioRecordActivity.class);
			it.putExtra("taskID", taskID);
			startActivityForResult(it, AUDIO_RECORD);
			break;
		}
		
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == EDIT_TASK && resultCode == Activity.RESULT_OK){
			
			//ȷʵ��ҵ�����ı�Ž��н������
			if(data.getBooleanExtra("isTaskChanged", false)){
				new GetTaskInfoThread().execute();
			}
			
		}
		
		if((requestCode == UPLOAD_FILE && resultCode == Activity.RESULT_OK)
				|| (requestCode == AUDIO_RECORD && resultCode == Activity.RESULT_OK)){
			
			//ȷʵ��ҵ�����ı�Ž��н������
			if(data.getBooleanExtra("isTaskChanged", false)){
				new GetAttachmentList().execute();
			}
		}
		
		if(requestCode == PICTURE_PREVIEW && resultCode == Activity.RESULT_OK){
			new GetAttachmentList().execute();
		}
		
		if(requestCode == CAMERA && resultCode == Activity.RESULT_OK){
			Intent it = new Intent(this,PreviewPictureActivity.class);
			it.putExtra("filePath", Environment.getExternalStorageDirectory().getPath()
					+"/wenfeng/"+application.getUserID()+"/upload/"+uploadFileName);
			it.putExtra("taskID",taskID);
			startActivityForResult(it,PICTURE_PREVIEW);
		}
		
	}

	/**
	 * �����ҵ����ĺ�̨�߳�
	 */
	class GetTaskInfoThread extends AsyncTask<Void,Void,Integer>{
		
		private final int CONNECTION_FAIL = -1;
		private final int SUCCESS = 1;
		
		private WebService web = new WebService();
		

		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(Void... params) {
			
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("taskID",String.valueOf(taskID));
			
			SoapObject result = web.CallWebService("getTeacherTaskDetail", p);
			
			if(result == null){
				return CONNECTION_FAIL;
			}
			
			task = XMLParser.parseTeacherTaskDetail(result);
			
			return SUCCESS;
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			
			if(result == CONNECTION_FAIL){
				Toast.makeText(TeacherTaskDetailActivity.this,
						R.string.tip_connection_timeout, Toast.LENGTH_LONG).show();
			}else{
				titleView.setText(task.getTitle());
				stuCountView.setText(String.valueOf(task.getStudents()));
				testCountView.setText(String.valueOf(task.getExams()));
				
				if(task.getIsDiscuss().equalsIgnoreCase("false")){
					isDiscussView.setChecked(false);
				}else{
					isDiscussView.setChecked(true);
				}
				
				sDateView.setText(task.getStartDate());
				
				//���Ѿ���ֹ����ҵ���
				String endDate = task.getEndDate();
				if(DeadlineCheck.check(endDate)){
					eDateView.setTextColor(TeacherTaskDetailActivity.this
							.getResources().getColor(R.color.my_red));
				}else{
					eDateView.setTextColor(Color.BLACK);
				}
				eDateView.setText(endDate);
				
				contentView.setText(task.getContent().equals("null") ? "" : 
					Html.fromHtml(task.getContent()));
				remarkView.setText(task.getRemark().equals("null") ? "" : 
					Html.fromHtml(task.getRemark()));
			}	
		}	
	}
	
	/**
	 * ���ظ����б�ĺ�̨�߳�
	 */
	class GetAttachmentList extends AsyncTask<Void,Void,Integer>{

		private final int FAIL = -1;
		private final int SUCCESS = 0;
		
		private DBManager db = new DBManager(MySQLiteOpenHelper.getInstance(TeacherTaskDetailActivity.this)
				.getConnection());
		
		ArrayList<File> attachments = new ArrayList<File>();
		
		@Override
		protected Integer doInBackground(Void... params) {
			
			LoadUploadedFiles.loadFiles(attachments,db , taskID, application.getUserID(), 2);
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == FAIL){
				Toast.makeText(TeacherTaskDetailActivity.this,R.string.tip_connection_timeout
						,Toast.LENGTH_SHORT).show();
			}else{
				
				if(attachmentView.getChildCount() == 2){
					attachmentView.removeViewAt(1);
				}
				
				//ȷ���и����ٻ���
				if(attachments.size() != 0){
					AttachmentView child = new AttachmentView(TeacherTaskDetailActivity.this);
					
					child.setDatas(attachments,taskID,application.getUserID(),true
							,"http://202.202.43.245/UploadFiles/Exercise/Define",0);
					
					child.setOnItemClickListener(new AttachmentView.OnItemClickListener() {
						
						@Override
						public void onDeleteStart() {

							progressBar.setVisibility(View.VISIBLE);
							
						}
				
						@Override
						public void onDeleteFail() {
							
							progressBar.setVisibility(View.GONE);
							Toast.makeText(TeacherTaskDetailActivity.this
									,R.string.delete_task_fail, Toast.LENGTH_SHORT).show();
							
						}

						@Override
						public void onDeleteSucccess() {
				
							isDeletingAttachment = true;			
							new GetAttachmentList().execute();
							
						}

						@Override
						public void onDownloadStart(long downloadId,String newName) {
							
							Toast.makeText(TeacherTaskDetailActivity.this, R.string.tip_start_download
									,Toast.LENGTH_SHORT).show();
							downloadQueue.append(downloadId, newName);
							
						}
				});
					
					attachmentView.addView(child);
				}	
				
				if(isDeletingAttachment){
					isDeletingAttachment = false;
					progressBar.setVisibility(View.GONE);
					Toast.makeText(TeacherTaskDetailActivity.this
							,R.string.delete_task_success, Toast.LENGTH_SHORT).show();
				}
				
			}
		}
			
	}

}
