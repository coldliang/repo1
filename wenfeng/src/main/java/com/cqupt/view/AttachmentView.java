package com.cqupt.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.net.WebService;
import com.cqupt.ui.common.FileExploreActivity.MyFile;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.FileUtil;
import com.cqupt.util.MIMEUtils;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;

public class AttachmentView extends LinearLayout {
	
	private static final int DEFAULT_TEXT_SIZE = 17;
	
	public interface OnItemClickListener{
		public void onDeleteStart();
		public void onDeleteFail();
		public void onDeleteSucccess();
		public void onDownloadStart(long downloadId,String newName);
	}
	
	private OnItemClickListener mCallback;
	private Context mContext;
	private String mUserID;
	private String mURL;
	private int mTaskID;
	private List<File> mData;
	private boolean mShowImageViews = true;
	private int mTextSize;
	
	public AttachmentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setOrientation(LinearLayout.VERTICAL);
	}

	public AttachmentView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AttachmentView(Context context) {
		this(context, null);
	}

	/**
	 * 
	 * @param textSize "unit sp"
	 */
	public void setDatas(List<File> data,int taskID,String userID,boolean showImageviews,String downloadURL
			,int textSize){
		mUserID = userID;
		mTaskID = taskID;
		mData = data;
		mShowImageViews = showImageviews;
		mURL = downloadURL;
		mTextSize = textSize <= 0 ? DEFAULT_TEXT_SIZE : textSize;		
		initViews();
	}
	
	public void setOnItemClickListener(OnItemClickListener listener){
		mCallback = listener;
	}
	
	@SuppressLint("InflateParams")
	private void initViews(){
		
		for (File file : mData) {

			final MyFile myFile = (MyFile) file;

			View myView = LayoutInflater.from(mContext).inflate(
					R.layout.layout_attachment, null);
			myView.setTag(myFile.getNewName());
			
			final TextView contentView = (TextView) myView
					.findViewById(R.id.content);
			final ImageView deleteImageView = (ImageView) myView
					.findViewById(R.id.imageView);
			final ImageView downloadImageView = (ImageView) myView
					.findViewById(R.id.downloadImageView);
			
			contentView.setTextSize(mTextSize);
			if(FileUtil.checkFileType(file, "amr")){
				contentView.setText(R.string.audio_record);
			}else{
				contentView.setText(file.getName());
			}
			
			if(mShowImageViews){
				
				deleteImageView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						showDeleteDialog(myFile);

					}
				});
				
				// �����Ѿ�����,�ú�ɫ���ֱ�ʾ
				if (myFile.exists()) {
					contentView.setTextColor(Color.BLACK);
					downloadImageView.setVisibility(View.GONE);
				} else {
					downloadImageView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							if (!CheckNetwork.isConnectingToInternet(mContext)) {
								Toast.makeText(mContext,
										R.string.tip_network_unavailable,
										Toast.LENGTH_SHORT).show();
							} else {
								showDownloadDialog(myFile,contentView,downloadImageView,deleteImageView);
							}

						}
					});
				}
				
			}else{
				deleteImageView.setVisibility(View.GONE);
				downloadImageView.setVisibility(View.GONE);
				
				// �����Ѿ�����,�ú�ɫ���ֱ�ʾ
				if (myFile.exists()) {
					contentView.setTextColor(Color.BLACK);
				} 
				
			}

			contentView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					// ������δ����
					if (!myFile.exists()) {
						if (!CheckNetwork.isConnectingToInternet(mContext)) {
							Toast.makeText(mContext,R.string.tip_network_unavailable,Toast.LENGTH_SHORT).show();
						} else {
							showDownloadDialog(myFile, contentView, downloadImageView,deleteImageView);
						}
						
					// �����Ѿ����ع�
					} else {

						Intent it = new Intent();
						it.setAction(Intent.ACTION_VIEW);
						it.setDataAndType(Uri.fromFile(myFile),MIMEUtils.getMIMEType(myFile));
						mContext.startActivity(it);
		

					}
				}
			});

			addView(myView);
		}
	}
	
	private void showDeleteDialog(final File file){
		
		new AlertDialog.Builder(mContext)
		.setTitle(R.string.delete_confirm)
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				new DeleteAttachmentThread().execute(file);
				
				if(mCallback != null){
					mCallback.onDeleteStart();
				}
				
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create()
		.show();
		
	}
	
	private void showDownloadDialog(final MyFile file,final TextView contentView,final ImageView downloadView,
			final ImageView deleteView){
		
		new AlertDialog.Builder(mContext)
		.setTitle(R.string.download_confirm)
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				downloadFiles(file.getName(), file.getNewName());
				
				// �������ض��е�item�ݲ�������
				contentView.setEnabled(false);
				downloadView.setEnabled(false);
				deleteView.setEnabled(false);
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create()
		.show();
		
	}
	
	private void downloadFiles(String fileName,String newName){
		
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			DownloadManager dm = (DownloadManager)mContext.getSystemService(Context.DOWNLOAD_SERVICE);
			//String uri = "http://202.202.43.245/UploadFiles/Exercise/Define/" + newName;
			String uri = mURL + "/" + newName;
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));
			File file = new File(Environment.getExternalStorageDirectory().getPath()
					+"/wenfeng/"+mUserID+"/download");
			if(!file.exists()){
				file.mkdirs();
			}
			request.setDestinationInExternalPublicDir("wenfeng/"+mUserID+"/download",fileName);
			request.setTitle(fileName);
			
			if(mCallback != null){
				mCallback.onDownloadStart(dm.enqueue(request),newName);
			}
			
		}else{
			Toast.makeText(mContext,R.string.tip_sdcard_not_exist,Toast.LENGTH_SHORT).show();
		}
		
	}
	
	class DeleteAttachmentThread extends AsyncTask<File, Void, Integer>{
		
		private final static int FAIL = -1;
		private final static int SUCCESS = 0;
		
		private DBManager db = new DBManager(MySQLiteOpenHelper.getInstance(mContext).getConnection());
		private File file;

		@Override
		protected Integer doInBackground(File... params) {
			
			file = params[0];
			
			//�ӷ�����ɾ��
			WebService web = new WebService();
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("userID", mUserID);
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
				
				db.deleteAttachment(mTaskID, mUserID,newName);
				db.deleteUploadFilesFromFilePath(file.getPath());
					
				return SUCCESS;
			}
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == FAIL){
				
				if(mCallback != null){
					mCallback.onDeleteFail();
				}
				
			}else{
				
				if(mCallback != null){
					mCallback.onDeleteSucccess();
				}
				
			}
		}
	}
	
	public static class SimpleOnItemClickListener implements OnItemClickListener{

		@Override
		public void onDeleteStart() {
			
		}

		@Override
		public void onDeleteFail() {
			
		}

		@Override
		public void onDeleteSucccess() {
			
		}

		@Override
		public void onDownloadStart(long downloadId, String newName) {
			
		}
		
	}
    
}
