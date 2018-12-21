package com.cqupt.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.FileUploadWebservice;
import com.cqupt.util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PreviewPictureActivity extends Activity implements OnClickListener{
	
	private MyApplication application;
	
	private ProgressBar progressBar;
	private Bitmap photo;
	private MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(this);
	
	private int taskID;
	private String filePath;
	private boolean isDeadline = false;
	
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_preview_picture);
		
		application = (MyApplication)getApplication();
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		Button sendButton = (Button)findViewById(R.id.sendButton);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		
		cancelButton.setOnClickListener(this);
		sendButton.setOnClickListener(this);

		ImageView imageView = (ImageView)findViewById(R.id.imageView);
		Intent it = getIntent();
		filePath = it.getStringExtra("filePath");
		taskID = it.getIntExtra("taskID",0);
		isDeadline = it.getBooleanExtra("isDeadline", false);
		
		//ѹ��ͼƬ
		BitmapFactory.Options options=new BitmapFactory.Options();
		options.inSampleSize = 4;
		photo = BitmapFactory.decodeFile(filePath, options);
		
		imageView.setImageBitmap(photo);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.cancelButton : finish();break;
		case R.id.sendButton : 
			if(!CheckNetwork.isConnectingToInternet(this)){
				Toast.makeText(this,R.string.tip_network_unavailable,Toast.LENGTH_SHORT).show();
			}else{
				
				if(isDeadline){
					Toast.makeText(this, R.string.tip_task_is_over, Toast.LENGTH_SHORT).show();
				}else{
					new UploadFileThread().execute(0);
				}
				
			}
		}
	}
	
	class UploadFileThread extends AsyncTask<Integer,Integer,Integer>{
		
		private final int UPLOAD_SUCCESS = 0;
		private final int UPLOAD_FAIL = 1;
		private final int MAX_FILE_SIZE = 100;
		
		private int type;
		
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(Integer... params) {
			
			boolean flag;
			DBManager db = new DBManager(helper.getConnection());
			File file = new File(filePath);
			
			String userType = db.getUserInfo().getUserType();
			
			if(userType.equals("ѧ��")){
				type = 3;
			}else{
				type = 2;
			}
			
			String fileNewName = FileUtil.getStringFromTimestamp("yyyyMMddHHmmssSSS"
					,file.getName());
			
			//��Ƭ����100KB�ͽ���ѹ�������ϴ���������
			if(file.length()/1024 > MAX_FILE_SIZE){
				String fileName = filePath.split("/")[filePath.split("/").length-1];
				flag = FileUploadWebservice.upload(type,photo, fileName, 
						taskID,application.getUserID(),fileNewName);
			}else{			
				flag = FileUploadWebservice.upload(type,file, taskID,application.getUserID()
						,fileNewName);
				}
			if(flag){				
				Date date = new Date();
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
				String time = format.format(date);
				
				db.addUploadFiles(application.getUserID(),taskID,filePath,time,fileNewName);
				return UPLOAD_SUCCESS;
			}else{
				return UPLOAD_FAIL;
			}
			}

		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			switch(result){
			case UPLOAD_SUCCESS :
				Toast.makeText(PreviewPictureActivity.this,
						R.string.tip_upload_answers_success, Toast.LENGTH_SHORT).show();
				
				//��ʦ�û���Ҫ����result�Ա�����ҳ�漰ʱ����
				if(type == 2){					
					setResult(Activity.RESULT_OK);
				}
				
				finish();
				break;
			case UPLOAD_FAIL :
				Toast.makeText(PreviewPictureActivity.this,
						R.string.tip_upload_fail, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
