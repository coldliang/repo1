package com.cqupt.ui.teacher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.model.StudentUploadedFiles;
import com.cqupt.net.WebService;
import com.cqupt.thread.MakeFileReadThread;
import com.cqupt.util.CommonListViewAdapter;
import com.cqupt.util.FileUtil;
import com.cqupt.util.MIMEUtils;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class StudentUploadedFilesActivity extends Activity {
	
	private MyApplication mApplication;
	
	private TextView mReturnView;
	private ListView mListView;
	private CommonListViewAdapter<StudentUploadedFiles> mListViewAdapter;
	private List<StudentUploadedFiles> mListViewData;
	private int mTaskID;
	private String mStuUserID;
	private BroadcastReceiver mReceiver;
	private HashMap<Long, Integer> mDownloadQueue;
	
	//----------------��д����------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student_uploaded_files);
		
		findView();
		loadData();
		setListener();
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	
	//---------------------�Զ��巽��----------------------------
	
	private void findView(){
		mListView = (ListView) findViewById(R.id.listView);
		mReturnView = (TextView) findViewById(R.id.returnView);
	}
	
	@SuppressLint("UseSparseArrays")
	private void loadData(){
		
		mApplication = (MyApplication) getApplication();
		mDownloadQueue = new HashMap<Long, Integer>();
		
		mReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				
				Long id = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
				
				if(id != 0L){
					mDownloadQueue.remove(id);
				}
				
				Toast.makeText(StudentUploadedFilesActivity.this
						, R.string.tip_attachment_download_success, Toast.LENGTH_SHORT).show();
				mListViewAdapter.notifyDataSetChanged();
				
			}
		};
		
		registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		Intent intent = getIntent();
		
		mTaskID = intent.getIntExtra("taskID", 0);
		mStuUserID = intent.getStringExtra("userID");
		
		new GetListViewDataThread().execute();
		
	}
	
	private void setListener(){
		
		mReturnView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
	}
	
	//------------------�ڲ���-----------------
	
	class ListViewAdapter extends CommonListViewAdapter<StudentUploadedFiles>{

		public ListViewAdapter(List<StudentUploadedFiles> data,
				Context context, int layoutResId) {
			super(data, context, layoutResId);
			
		}

		@Override
		protected void inflate(ViewHolder viewHolder,final StudentUploadedFiles item,int position) {
			
			final int index = position;
			
			TextView originNameView = (TextView) viewHolder.getView(R.id.originName);
			TextView sizeView =  (TextView) viewHolder.getView(R.id.size);
			TextView timeView = (TextView) viewHolder.getView(R.id.time);
			Button button = (Button) viewHolder.getView(R.id.button);
			ImageView imageView = (ImageView) viewHolder.getView(R.id.image);
			
			originNameView.setText(item.getOriginName());
			sizeView.setText(item.getSize());
			timeView.setText(item.getTime());
			
			//�����ļ����������ò�ͬ��ͼƬ
			
			int type = FileUtil.getFileType(item.getOriginName());
			
			if(type == FileUtil.TYPE_DOCUMENT){
				imageView.setBackgroundResource(R.drawable.document);
			}else{
				if(type == FileUtil.TYPE_AUDIO){
					imageView.setBackgroundResource(R.drawable.music);

				}else{
					File file = new File(Environment.getExternalStorageDirectory() + "/wenfeng/" 
							+ mApplication.getUserID() + "/download/" + item.getOriginName());
					
					if(file.exists()){
						Glide.with(StudentUploadedFilesActivity.this)
					     .load(file)
					     .into(imageView);
					}
					
					/*new BitmapUtil.BitmapWorkerTask(imageView).execute(
							Environment.getExternalStorageDirectory() + "/wenfeng/" 
					+ mApplication.getUserID() + "/download/" + item.getOriginName());*/
				}
			}
			
			//�����ļ��Ƿ��Ѿ�����������button��ʾ������
			final File file = new File(Environment.getExternalStorageDirectory().getPath() +
					"/wenfeng/" + mApplication.getUserID() 
					+ "/download/" + item.getOriginName());
			
			if(file.exists()){
				button.setText(R.string.open);
				button.setEnabled(true);
			}else{
				button.setText(R.string.download);
				
				//�ļ�����������,�������ٴε������
				if(mDownloadQueue.containsValue(position)){
					button.setEnabled(false);
				}else{
					button.setEnabled(true);
				}
			}
			
			final String fileNewName = item.getNewName();
			
			button.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					//�򿪲���
					if(((Button)v).getText().equals(getResources().getString(R.string.open))){
						
						//���ļ����Ϊ�Ѷ�
						MakeFileReadThread.start(item.getNewName());
						
						Intent it = new Intent();
						it.setAction(Intent.ACTION_VIEW);
						it.setDataAndType(Uri.fromFile(file), MIMEUtils.getMIMEType(file));
						startActivity(it);
					}else{//���ز���
						
						//sdcard����
						if(Environment.getExternalStorageState()
								.equals(Environment.MEDIA_MOUNTED)){
							Toast.makeText(StudentUploadedFilesActivity.this
									,R.string.tip_start_download,Toast.LENGTH_SHORT).show();
							v.setEnabled(false);//�ļ����ع����в�������һ�ε��
							DownloadManager dm = (DownloadManager)getSystemService(
									Activity.DOWNLOAD_SERVICE);
							DownloadManager.Request req = new DownloadManager.Request(
									Uri.parse("http://202.202.43.245/UploadFiles/Exercise/" +
											"FeedBack/"+ fileNewName));
							
							String fileSavePath = Environment.getExternalStorageDirectory()
									.getPath()+"/wenfeng/" + mApplication.getUserID() 
									+ "/download/";
							
							File file2 = new File(fileSavePath);
							
							if(!file2.exists()){
								file2.mkdirs();
							}
							
							req.setDestinationInExternalPublicDir("/wenfeng/" 
									+ mApplication.getUserID() + "/download/", file.getName());
							req.setTitle(file.getName());
							Long id = dm.enqueue(req);
							mDownloadQueue.put(id, index);
						}else{//sdcard������
							Toast.makeText(StudentUploadedFilesActivity.this
									,R.string.tip_sdcard_not_exist, Toast.LENGTH_SHORT).show();
						}
						
					}
					
				}
			});
			
		}
		
	}
	
	class GetListViewDataThread extends AsyncTask<Void, Void, Integer>{
		
		private final static int FAIL = -1;
		private final static int SUCCESS = 1;

		@Override
		protected Integer doInBackground(Void... params) {
			WebService web = new WebService();
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("taskID", String.valueOf(mTaskID));
			p.put("userID", mStuUserID);
			
			SoapObject result = web.CallWebService("getStudentUploadedFiles", p);
			
			if(result == null){
				return FAIL;
			}
			
			mListViewData = XMLParser.parseStudentUploadedFiles(result);
			
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(result == SUCCESS){
				mListViewAdapter = new ListViewAdapter(mListViewData
						, StudentUploadedFilesActivity.this
						, R.layout.layout_student_uploaded_files);
				
				mListView.setAdapter(mListViewAdapter);
			}
		}
		
	}

}
