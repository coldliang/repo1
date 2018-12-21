package com.cqupt.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.FileUploadWebservice;
import com.cqupt.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioRecordActivity extends Activity implements OnClickListener{
	
	private final int RECORD_NOT_START = 0;
	private final int RECORDING = 1;
	private final int RECORD_STOP = 2;
	private final int PLAYING = 3;
	private final int PLAY_STOP = 4;
	
	private MyApplication application;
	
	private MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(this);
	private MediaRecorder recorder;
	private MediaPlayer player;
	private TextView returnView;
	private Chronometer times;//��¼¼��ʱ��
	private RelativeLayout bottomView;//�ײ�������
	private ImageView functionButton;//¼����Ƶ��ť
	private Button cancelButton;
	private Button sendButton;
	private TextView filePathView;//��ʾ¼���ļ��洢λ�õ���ͼ
	private ProgressBar progressBar;//�ϴ�������
	
	private int state = RECORD_NOT_START;
	private int taskID;
	private String filePath;//��¼¼���ļ��洢λ��
	private String fileName;//��¼¼���ļ�����
	private boolean isDeadline = false;
	private boolean isTaskChanged = false;
	private int mUserType;
	private DBManager mDb;
	
	//��д����
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_audio_record);
		taskID = getIntent().getIntExtra("taskID",0);
		isDeadline = getIntent().getBooleanExtra("isDeadline", false);
		
		application = (MyApplication)getApplication();
		
		mDb = new DBManager(helper.getConnection());
		
		if(mDb.getUserInfo().getUserType().equals("ѧ��")){
			mUserType = 3;
		}else{
			mUserType = 2;
		}
		
		findView();
		setListener();
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.functionButton : 
			switch(state){
			case RECORD_NOT_START : startRecording();break;
			case RECORDING : stopRecording();break;
			case RECORD_STOP : startPlaying();break;
			case PLAYING : stopPlaying();break;
			case PLAY_STOP : startPlaying();
			}
		    break;
		case R.id.returnView : onBackPressed();break;
		case R.id.cancelButton : reset();break;
		case R.id.sendButton : 
			if(!CheckNetwork.isConnectingToInternet(this)){
				Toast.makeText(this,R.string.tip_network_unavailable,Toast.LENGTH_SHORT).show();
			}else{
				
				if(isDeadline){
					Toast.makeText(this, R.string.tip_task_is_over, Toast.LENGTH_SHORT).show();
				}else{
					//����ǰ,���¼���Ƿ����ڲ���
					if(state == PLAYING && player != null){
						stopPlaying();
					}
					new UploadFileThread().execute(0);
				}	
			}
		}
	}

	@Override
	protected void onPause() {
		//¼�������п��ܱ����,��ͻȻ���绰,���������Ҫֹͣ����
		if(state == PLAYING){
			stopPlaying();
		}
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		releaseOnBack();
		
		if(mUserType == 2){
			Intent intent = new Intent();
			intent.putExtra("isTaskChanged", isTaskChanged);
			setResult(Activity.RESULT_OK,intent);
		}
		
		super.onBackPressed();
	}
	
	//�Զ��巽��
	private void findView(){
		returnView = (TextView)findViewById(R.id.returnView);
		times = (Chronometer)findViewById(R.id.chronometer);
		functionButton = (ImageView)findViewById(R.id.functionButton);
		bottomView = (RelativeLayout)findViewById(R.id.bottom);
		cancelButton = (Button)findViewById(R.id.cancelButton);
		sendButton = (Button)findViewById(R.id.sendButton);
		filePathView = (TextView)findViewById(R.id.filePath);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
	}
	
	private void setListener(){
		functionButton.setOnClickListener(this);
		returnView.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		sendButton.setOnClickListener(this);
	}
	
	private void startRecording(){
		//ȷ���ֻ�����SD��
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			
			recorder = new MediaRecorder();
			//һ��Ҫע�����ò�����˳��
			recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
			recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			//��ʽ��¼���ļ���
			Date date = new Date();
	    	SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
	    	fileName = "REC_"+format.format(date)+".amr";
	    	File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/wenfeng/"+application.getUserID()+"/upload");
	    	if(!file.exists()){
	    		file.mkdirs();
	    	}
			filePath = file.getPath()+"/"+fileName;
			recorder.setOutputFile(filePath);
			try{
				recorder.prepare();
				recorder.start();
				times.setBase(SystemClock.elapsedRealtime());
				times.start();
				functionButton.setImageResource(R.drawable.finish_record);//�л�ͼƬΪֹͣ��ť
				state = RECORDING;
			}catch(IOException e){
				e.printStackTrace();
				Toast.makeText(this,R.string.tip_audio_record_setup_fail,Toast.LENGTH_SHORT).show();
			}
		}else{
			Toast.makeText(this,R.string.tip_sdcard_not_exist,Toast.LENGTH_SHORT).show();
		}
	}
	
	private void stopRecording(){
		recorder.stop();
		recorder.reset();
		times.stop();
		times.setBase(SystemClock.elapsedRealtime());//��ʱ������
		functionButton.setImageResource(R.drawable.play);//�л�ͼƬΪ���Ű�ť
		state = RECORD_STOP;
		recorder.release();
		recorder = null;
		filePathView.setText(filePath);
		bottomView.setVisibility(View.VISIBLE);
	}
	
	private void startPlaying(){
		player = new MediaPlayer();
		//�����������,��ʱ�ͷ���Դ
		player.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				stopPlaying();
			}
		});
		try{
			player.setDataSource(filePath);
			player.prepare();
			player.start();
			state = PLAYING;
			functionButton.setImageResource(R.drawable.stop_play);
		}catch(IllegalStateException e){
			e.printStackTrace();
			Toast.makeText(this,R.string.tip_play_audio_error,Toast.LENGTH_SHORT).show();
		}catch(IOException e){
			e.printStackTrace();
			Toast.makeText(this,R.string.tip_file_not_exist,Toast.LENGTH_SHORT).show();
		}
	}
	
	private void stopPlaying(){
		if(player != null){
			player.stop();
			player.release();
			player = null;
			state = PLAY_STOP;
			functionButton.setImageResource(R.drawable.play);
		}	
	}
	
    /**
	 *�˳�activityʱ�ͷ�������Դ
	 */
	private void releaseOnBack(){
		if(state == RECORDING){
			recorder.stop();
			recorder.reset();
			times.stop();
			recorder.release();
		}else{
			if(state == PLAYING && player != null){
				player.stop();
				player.release();
			}
		}
	}
	
	private void reset(){
		
		if(state == PLAYING && player != null){
			player.stop();
			player.release();
		}
		bottomView.setVisibility(View.INVISIBLE);
		filePathView.setText("");
		state = RECORD_NOT_START;
		functionButton.setImageResource(R.drawable.start_record);
	}
	
	//�ڲ���
	
	private class UploadFileThread extends AsyncTask<Integer,Integer,Integer>{
	
		private final int UPLOAD_SUCCESS = 0;
		private final int UPLOAD_FAIL = 1;
		private final int FILE_SIZE_OVER_MAX = 2;
		private final int MAX_FILE_SIZE = 5;
		
		@Override
		protected void onPreExecute() {
			
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			
			File file = new File(filePath);
			
			if(file.length()/(1024*1024) > MAX_FILE_SIZE){
				return FILE_SIZE_OVER_MAX;
			}else{
				
				String fileNewName = FileUtil.getStringFromTimestamp("yyyyMMddHHmmssSSS"
						,file.getName());
				
				if(FileUploadWebservice.upload(mUserType,file, taskID,application.getUserID()
						,fileNewName)){			
					
					Date date = new Date();
					SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
					String time = format.format(date);
					
					mDb.addUploadFiles(application.getUserID(),taskID,filePath,time,fileNewName);
					return UPLOAD_SUCCESS;
				}else{
					return UPLOAD_FAIL;
				}
			} 
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			switch(result){
			case UPLOAD_SUCCESS :
				isTaskChanged = true;
				Toast.makeText(AudioRecordActivity.this,
						R.string.tip_upload_answers_success,Toast.LENGTH_SHORT).show();
				reset();
				break;
			case UPLOAD_FAIL : 
				Toast.makeText(AudioRecordActivity.this,
						R.string.tip_upload_fail,Toast.LENGTH_SHORT).show();
				break;
			case FILE_SIZE_OVER_MAX : 
				Toast.makeText(AudioRecordActivity.this,
						R.string.tip_upload_file_size_over_max,Toast.LENGTH_SHORT).show();
				reset();
				break;
			}
		}	
	}
}
