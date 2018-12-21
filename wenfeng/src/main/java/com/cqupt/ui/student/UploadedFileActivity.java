package com.cqupt.ui.student;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.Attachment;
import com.cqupt.model.UploadFiles;
import com.cqupt.ui.common.FileExploreActivity.MyFile;
import com.cqupt.ui.common.FileExploreActivity.ViewHolder;
import com.cqupt.util.BitmapUtil;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.FileUtil;
import com.cqupt.util.MIMEUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UploadedFileActivity extends Activity implements OnClickListener{
	
	private MyApplication application;
	
	private ListView mListView;
	private MyListViewAdapter mListViewAdapter;
	private TextView mReturnView;
	private BroadcastReceiver mReceiver;
	
	private List<File> mListViewData;
	private int taskID;
	private DBManager db;
	
	//--------------------------------���·���-----------------------------
	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_uploaded_file);
		
		findView();
		loadData();
		setListener();
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.returnView : onBackPressed();break;
		}
		
	}
	

	@Override
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
	
	//--------------------------------�Զ��巽��--------------------------------
	

	private void findView() {
		
		mReturnView = (TextView) findViewById(R.id.returnView);
		mListView = (ListView) findViewById(R.id.listView);
		
	}
	
	private void loadData() {
		
		db = new DBManager(MySQLiteOpenHelper.getInstance(this).getConnection());
		application = (MyApplication)getApplication();
		mListViewData = new ArrayList<File>();
		mListViewAdapter = new MyListViewAdapter();
		
		mReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				
				Toast.makeText(UploadedFileActivity.this,
						R.string.tip_attachment_download_success,Toast.LENGTH_SHORT).show();
				mListViewAdapter.notifyDataSetChanged();
				
			}
		};
		registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		taskID = getIntent().getIntExtra("taskID", 0);	
		loadFiles();	
		mListView.setAdapter(mListViewAdapter);
		
	}
	
	private void setListener() {
		
		mReturnView.setOnClickListener(this);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				//�ļ�������,����ʾ���ذ�ť
				if(!mListViewData.get(position).exists()){
					showDialog(UploadedFileActivity.this
							.getResources().getString(R.string.download)
							,mListViewData.get(position));
				}else{//�ļ���������ʾ�򿪰�ť
					showDialog(UploadedFileActivity.this
							.getResources().getString(R.string.open)
							,mListViewData.get(position));
				}
				
			}
		});
		
	}
	
	private void showDialog(final String positiveButton,final File file){
		Dialog log  = new AlertDialog.Builder(this).setPositiveButton(positiveButton
				,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//�����ļ�
				if(!file.exists()){
					
					if(CheckNetwork.isConnectingToInternet(UploadedFileActivity.this)){
						if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
							MyFile mFile = (MyFile)file;	
							
							//�����ϴ��ĸ�����ɾ��,����Ҫɾ����Ӧ���ݿ��еļ�¼
							if(db.isFilePathExisted(file.getPath())){
								db.deleteUploadFilesFromFilePath(file.getPath());
								
								Attachment a = new Attachment();
								a.setId(taskID);
								a.setNewName(mFile.getName());
								a.setOriginName(file.getName());
								a.setUserID(application.getUserID());
								
								db.addAttachment(a);
								
								mListViewData.clear();
								loadFiles();
							}
							
							
							DownloadManager manager = (DownloadManager) getSystemService(
									Activity.DOWNLOAD_SERVICE);
							DownloadManager.Request req = new DownloadManager.Request(
									Uri.parse("http://202.202.43.245/UploadFiles/Exercise/FeedBack/"
											+ mFile.getNewName()));
							
							File f = new File(Environment.getExternalStorageDirectory().getPath()+"/wenfeng/"+application.getUserID()+"/download");
							if(!f.exists()){
								f.mkdirs();
							}
							
							req.setDestinationInExternalPublicDir("wenfeng/"+application
									.getUserID()+"/download", file.getName());
							req.setTitle(file.getName());
							
							Toast.makeText(UploadedFileActivity.this
									,R.string.tip_start_download,Toast.LENGTH_SHORT).show();
							manager.enqueue(req);
							
						}else{
							Toast.makeText(UploadedFileActivity.this
									,R.string.tip_sdcard_not_exist,Toast.LENGTH_SHORT).show();
						}
				    }else{
				    	Toast.makeText(UploadedFileActivity.this
				    			,R.string.tip_network_unavailable, Toast.LENGTH_SHORT).show();
				    }
				}else{//�鿴�ļ�
					Intent it = new Intent();
					it.setAction(Intent.ACTION_VIEW);
					it.setDataAndType(Uri.fromFile(file), MIMEUtils.getMIMEType(file));
					startActivity(it);
				}
			
			}
		})
		.setNegativeButton(R.string.cancel,null)
		.setMessage(R.string.select_operation)
		.create();
		log.show();
	}
	
	private void loadFiles(){
		ArrayList<UploadFiles> uploadFiles = db.getUploadFile(application.getUserID(),
				taskID);//�����ϴ��ĸ���
		ArrayList<Attachment> attachments = db.getAttachment(
				taskID,application.getUserID());//web���ϴ��ĸ���
		
		Iterator<Attachment> ait = attachments.iterator();
		Iterator<UploadFiles> fit = uploadFiles.iterator();
		
		while(fit.hasNext()){
			String fileNewName = fit.next().getFileNewName();
			while(ait.hasNext()){
				String fileName = ait.next().getNewName();
				if(fileNewName.equals(fileName)){
					ait.remove();
				}
			}
		}
		
		for(UploadFiles f : uploadFiles){
			MyFile mFile = new MyFile(f.getFilePath());
			mFile.setNewName(f.getFileNewName());
			mListViewData.add(mFile);	
		}
		
		for(Attachment a : attachments){
			MyFile mFile = new MyFile(Environment.getExternalStorageDirectory()
					.getPath() + "/wenfeng/" + application.getUserID()
					+ "/download/" + a.getOriginName());
			mFile.setNewName(a.getNewName());
			mListViewData.add(mFile);
		}
	}
	
	//--------------------------------�ڲ���----------------------------------------
	
	class MyListViewAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mListViewData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			
			if(convertView == null){
				convertView = LayoutInflater.from(UploadedFileActivity.this)
						.inflate(R.layout.laout_uploaded_file, null);
				holder = new ViewHolder();
				holder.imageView = (ImageView)convertView.findViewById(R.id.image);
				holder.fileNameView = (TextView)convertView.findViewById(R.id.title);
				holder.fileSizeView = (TextView)convertView.findViewById(R.id.size);
				holder.fileTimeView = (TextView)convertView.findViewById(R.id.time);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
		    }
			
			int type = FileUtil.getFileType(mListViewData.get(position));
			
			if(type == FileUtil.TYPE_DOCUMENT){
				holder.imageView.setImageResource(R.drawable.document);
			}else{
				if(type == FileUtil.TYPE_AUDIO){
					holder.imageView.setImageResource(R.drawable.music);
				}else{
					holder.imageView.setImageResource(R.drawable.default_image_background);
					new BitmapUtil.BitmapWorkerTask(holder.imageView)
					.execute(mListViewData.get(position).getPath());
				}	
			}
			
			holder.fileNameView.setText(mListViewData.get(position).getName());
			
			long fileSize = mListViewData.get(position).length();
			String textSize = FileUtil.getFileSize(fileSize);
			holder.fileSizeView.setText(textSize);
			
			holder.fileTimeView.setText(db.getUploadFileTime(
					mListViewData.get(position).getPath()));
			
			return convertView;
		}
		
	}
	
}
