package com.cqupt.ui.teacher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.net.WebService;
import com.cqupt.ui.common.AudioRecordActivity;
import com.cqupt.ui.common.FileExploreActivity;
import com.cqupt.ui.common.FileExploreActivity.MyFile;
import com.cqupt.ui.common.PreviewPictureActivity;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.CommonListViewAdapter;
import com.cqupt.util.LoadUploadedFiles;
import com.cqupt.util.MIMEUtils;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UploadAttachmentActivity extends Activity implements OnClickListener{
	
	private static final int UPLOAD_FILE = 2;
	private static final int CAMERA = 3;
	private static final int PICTURE_PREVIEW = 4;
	private static final int AUDIO_RECORD = 5;
	
	private TextView mReturnView;
	private ListView mListView;
	private ImageView mFileImageView;
	private ImageView mCameraView;
	private ImageView mVoiceView;
	private Button mButton;
	private ProgressBar mProgressBar;
	
	private ArrayList<File> mFiles = new ArrayList<File>();
	private int mTaskID;
	private MyApplication mApplication;
	private DBManager mDb;
	private MyListViewAdapter mListViewAdapter;
	private String uploadFileName;//�ϴ����ļ���(���պ�̬����)
	
	//----------------------��д����-------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_attachment);
		
		findView();
		loadData();
		setListener();
	}
	
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.returnView : onBackPressed();break;
		case R.id.nextButton : 
			
			Intent intent = new Intent(this,ArrangeStudentActivity.class);
			intent.putExtra("taskID", mTaskID);
			startActivity(intent);
			break;
			
		case R.id.fileImage :
			intent = new Intent(this,FileExploreActivity.class);
			intent.putExtra("taskID", mTaskID);
			startActivityForResult(intent, UPLOAD_FILE);
			break;
		case R.id.cameraImage : 

			// ��֤sd���Ƿ���ȷ��װ��
		    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
		    	intent = new Intent("android.media.action.IMAGE_CAPTURE");
		    	File file = new File(Environment.getExternalStorageDirectory().getPath()
		    			+"/wenfeng/"+mApplication.getUserID()+"/upload");
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
		    	Toast.makeText(UploadAttachmentActivity.this,
		    			R.string.tip_sdcard_not_exist, Toast.LENGTH_SHORT).show();
		    }
		    break;
		case R.id.voiceImage : 
			
			intent = new Intent(this,AudioRecordActivity.class);
			intent.putExtra("taskID", mTaskID);
			startActivityForResult(intent, AUDIO_RECORD);
			break;
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if((requestCode == UPLOAD_FILE && resultCode == Activity.RESULT_OK)
				|| (requestCode == AUDIO_RECORD && resultCode == Activity.RESULT_OK)){
			
			//ȷʵ��ҵ�����ı�Ž��н������
			if(data.getBooleanExtra("isTaskChanged", false)){
				new GetListViewDataThread().execute();
			}
		}
		
		if(requestCode == PICTURE_PREVIEW && resultCode == Activity.RESULT_OK){
			new GetListViewDataThread().execute();
		}
		
		if(requestCode == CAMERA && resultCode == Activity.RESULT_OK){
			Intent it = new Intent(this,PreviewPictureActivity.class);
			it.putExtra("filePath", Environment.getExternalStorageDirectory().getPath()
					+"/wenfeng/"+mApplication.getUserID()+"/upload/"+uploadFileName);
			it.putExtra("taskID",mTaskID);
			startActivityForResult(it,PICTURE_PREVIEW);
		}
		
	}
	
	//----------------------�Զ��巽��-----------------------------
	
	private void findView(){
		
		mReturnView = (TextView) findViewById(R.id.returnView);
		mListView = (ListView) findViewById(R.id.listView);
		mFileImageView = (ImageView) findViewById(R.id.fileImage);
		mVoiceView = (ImageView) findViewById(R.id.voiceImage);
		mCameraView = (ImageView) findViewById(R.id.cameraImage);
		mButton = (Button) findViewById(R.id.nextButton);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		
	}
	
	private void loadData(){
	
		mDb = new DBManager(MySQLiteOpenHelper.getInstance(this).getConnection());
		mApplication = (MyApplication) getApplication();
		Intent intent = getIntent();
		mTaskID = intent.getIntExtra("taskID", -1);
		
		new GetListViewDataThread().execute();
		
		
		
	}
	
	private void setListener(){
		
		mReturnView.setOnClickListener(this);
		mButton.setOnClickListener(this);
		mFileImageView.setOnClickListener(this);
		mVoiceView.setOnClickListener(this);
		mCameraView.setOnClickListener(this);
		
	}
	
	private void showDeleteDialog(final File file){
		
		new AlertDialog.Builder(UploadAttachmentActivity.this)
		.setTitle(R.string.delete_confirm)
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				new DeleteAttachmentThread().execute(file);
				mProgressBar.setVisibility(View.VISIBLE);
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create()
		.show();
		
	}
	
	private void showDownloadDialog(final MyFile file,final TextView contentView,final ImageView imageView){
		
		new AlertDialog.Builder(UploadAttachmentActivity.this)
		.setTitle(R.string.download_confirm)
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				Toast.makeText(UploadAttachmentActivity.this, R.string.tip_start_download
						,Toast.LENGTH_SHORT).show();
				downloadFiles(file.getName(), file.getNewName());
				
				// ���ظ�����,��������ʾ������ɫΪ��ɫ,��ʾ�����Ѿ����ع�
				contentView.setTextColor(Color.BLACK);
				imageView.setVisibility(View.GONE);
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create()
		.show();
		
	}
	
	private void downloadFiles(String fileName,String newName){
		
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			DownloadManager dm = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
			String uri = "http://202.202.43.245/UploadFiles/Exercise/Define/" + newName;
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
			File file = new File(Environment.getExternalStorageDirectory().getPath()
					+"/wenfeng/"+mApplication.getUserID()+"/download");
			if(!file.exists()){
				file.mkdirs();
			}
			request.setDestinationInExternalPublicDir("wenfeng/"+mApplication.getUserID()+"/download",fileName);
			request.setTitle(fileName);
			dm.enqueue(request);
		}else{
			Toast.makeText(UploadAttachmentActivity.this
					,R.string.tip_sdcard_not_exist,Toast.LENGTH_SHORT).show();
		}
		
	}
	
	//-----------------------�ڲ���-----------------------
	
	class GetListViewDataThread extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			mFiles.clear();
			LoadUploadedFiles.loadFiles(mFiles, mDb, mTaskID, mApplication.getUserID(), 2);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			if(mListViewAdapter == null){
				mListViewAdapter = new MyListViewAdapter(mFiles, UploadAttachmentActivity.this
						, R.layout.layout_attachment);
				mListView.setAdapter(mListViewAdapter);
			}else{
				mListViewAdapter.notifyDataSetChanged();
			}
			
		}
		
	}
	
	
	class MyListViewAdapter extends CommonListViewAdapter<File>{

		public MyListViewAdapter(List<File> data, Context context,int layoutResId) {
			super(data, context, layoutResId);
			
		}

		@Override
		protected void inflate(CommonListViewAdapter<File>.ViewHolder viewHolder, File item,int position) {
			
			final TextView titleView = (TextView) viewHolder.getView(R.id.content);
			final ImageView downloadImageView = (ImageView) viewHolder.getView(R.id.downloadImageView);
			ImageView deleteImageView = (ImageView) viewHolder.getView(R.id.imageView);
			
			final MyFile myFile = (MyFile)item;
			
			titleView.setText(item.getName());

			deleteImageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					showDeleteDialog(myFile);

				}
			});

			// �����Ѿ�����,�ú�ɫ���ֱ�ʾ
			if (item.exists()) {
				titleView.setTextColor(Color.BLACK);
				downloadImageView.setVisibility(View.GONE);
			} else {
				downloadImageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						if (!CheckNetwork.isConnectingToInternet(UploadAttachmentActivity.this)) {
							Toast.makeText(UploadAttachmentActivity.this,
									R.string.tip_network_unavailable,
									Toast.LENGTH_SHORT).show();
						} else {
							showDownloadDialog(myFile,titleView,downloadImageView);
						}

					}
				});
			}

			titleView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					// ������δ����
					if (!myFile.exists()) {
						if (!CheckNetwork.isConnectingToInternet(UploadAttachmentActivity.this)) {
							Toast.makeText(UploadAttachmentActivity.this
									,R.string.tip_network_unavailable,Toast.LENGTH_SHORT).show();
						} else {
							showDownloadDialog(myFile, titleView, downloadImageView);
						}
						// �����Ѿ����ع�
					} else {

						Intent it = new Intent();
						it.setAction(Intent.ACTION_VIEW);
						it.setDataAndType(Uri.fromFile(myFile),MIMEUtils.getMIMEType(myFile));
						startActivity(it);
		

					}
				}
			});
			
		}

		
	}
	
	class DeleteAttachmentThread extends AsyncTask<File, Void, Integer>{
		
		private final static int FAIL = -1;
		private final static int SUCCESS = 0;
	
		private File file;

		@Override
		protected Integer doInBackground(File... params) {
			
			file = params[0];
			
			//�ӷ�����ɾ��
			WebService web = new WebService();
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("userID", mApplication.getUserID());
			p.put("type","2");//2����ʦ���õ���ҵ 3��ѧ���ϴ��ĸ���
			p.put("fileName",file.getName());
			
			SoapObject result = web.CallWebService("deleteAttachment", p);
			
			if(result == null){
				return FAIL;
			}
			
			if(XMLParser.parseBoolean(result).equalsIgnoreCase("false")){
				return FAIL;
			}else{
				
				//�������ݿ�ɾ��
				String newName = "";
				
				if(file instanceof MyFile){
					MyFile mFile = (MyFile)file;
					newName = mFile.getName();
				}
				
				mDb.deleteAttachment(mTaskID, mApplication.getUserID(),newName);
				mDb.deleteUploadFilesFromFilePath(file.getPath());
				
					
				return SUCCESS;
			}
			
		}

		@Override
		protected void onPostExecute(Integer result) {
		
			if(result == FAIL){
				
				Toast.makeText(UploadAttachmentActivity.this, R.string.delete_task_fail
						, Toast.LENGTH_SHORT).show();
			}else{
				
				mFiles.remove(file);
				mListViewAdapter.notifyDataSetChanged();
				Toast.makeText(UploadAttachmentActivity.this, R.string.delete_task_success
						, Toast.LENGTH_SHORT).show();
				
			}
			
			mProgressBar.setVisibility(View.GONE);
		}
	}

}
