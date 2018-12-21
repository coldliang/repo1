package com.cqupt.ui.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.cqupt.R;
import com.cqupt.adapter.MyPagerAdapter;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.Attachment;
import com.cqupt.net.WebService;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.FileUploadWebservice;
import com.cqupt.util.FileUtil;
import com.cqupt.util.LoadUploadedFiles;
import com.cqupt.util.MIMEUtils;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class FileExploreActivity extends Activity implements OnClickListener{
	//�����ļ�������
	private final int TYPE_FILE_ALREADY_UPLOAD = 0;
	private final int TYPE_IMAGE = 1;
	private final int TYPE_DOCUMENT = 2;
	private final int TYPE_AUDIO = 3;
	
	private MyApplication application;

	private final String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
	
	private TextView uploadedTab;
	private TextView imageTab;
	private TextView documentTab;
	private TextView audioTab;
	private TextView returnView;
	private TextView selectedFileSize;//��ʾ��ѡ�ļ���Ŀ�ʹ�С
	private TextView cancelView;//ȡ����ѡ�ļ���ť
	private ViewPager viewPager;//������ͼ����
	private ImageView imageView;//����ָʾ��ͼ��
	private ProgressBar progressBar;//�����ļ�ʱ�Ľ�����
	private Button sendButton;//�����ļ���ť
	private ListView pager1;//ҳ��1
	private ListView pager2;//ҳ��2
	private ListView pager3;//ҳ��3
	private ListView pager4;//ҳ��4
	private MyListViewAdapter pager1Adapter;//ҳ��1������
	private MyListViewAdapter pager2Adapter;//ҳ��2������
	private MyListViewAdapter pager3Adapter;//ҳ��3������
	private MyListViewAdapter pager4Adapter;//ҳ��4������
	private BroadcastReceiver receiver;
	
	private ArrayList<File> documentList;//�洢�ĵ����ļ��ļ���
	private ArrayList<File> audioList;//�洢��Ƶ���ļ��ļ���
	private ArrayList<File> uploadedFileList;//�洢�Ѿ��ϴ��ļ��ļ���
	private ArrayList<File> imageList;//�洢ͼ�����ļ��ļ���
	private HashMap<String,File> selectedFiles;//�洢�û�ѡ����ļ�
	private ArrayList<View> pagerList;
	private int imageWidth;//ͼƬ�Ŀ��
	private int offset;//�����ƶ���ƫ����
	private int currentIndex = 0;// ��ǰҳ�����
	private int taskID;
	private DBManager db;
	private int mUserType;//�û����� 3��ʾѧ�� 2��ʾ��ʦ
	private boolean isTaskChanged = false;//��¼�Ƿ񸽼������仯
	
	//-----------------------------��д����-------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_explore);
		findView();
		loadData();
		setListener();
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(receiver);
		super.onDestroy();	
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.tab_recent : viewPager.setCurrentItem(0);break;
		case R.id.tab_image : viewPager.setCurrentItem(1);break;
		case R.id.tab_document : viewPager.setCurrentItem(2);break;
		case R.id.tab_audio : viewPager.setCurrentItem(3);break;
		case R.id.returnView : onBackPressed();break;
		case R.id.sendButton :
			if(!CheckNetwork.isConnectingToInternet(this)){
				Toast.makeText(this, 
						R.string.tip_network_unavailable,Toast.LENGTH_SHORT).show();
			}else{
				new UploadFilesThread().execute();
			}
			break;
		case R.id.cancel : resetMyView();
		}
	}
	
	@Override
	public void onBackPressed() {
		
		//��ʦ�û���Ҫ����result���Ա�����ҳ�漰ʱ��������
		if(mUserType == 2){
			Intent intent = new Intent();
			intent.putExtra("isTaskChanged", isTaskChanged);
			setResult(Activity.RESULT_OK,intent);
			finish();
		}
		super.onBackPressed();
	}
	
	//--------------------------------�Զ��巽��----------------------------------
	
	private void findView(){
		
		uploadedTab = (TextView)findViewById(R.id.tab_recent);
		imageTab = (TextView)findViewById(R.id.tab_image);
		documentTab = (TextView)findViewById(R.id.tab_document);
		audioTab = (TextView)findViewById(R.id.tab_audio);
		returnView = (TextView)findViewById(R.id.returnView);
		selectedFileSize= (TextView)findViewById(R.id.selectedFiles);
		cancelView = (TextView)findViewById(R.id.cancel);
		imageView = (ImageView)findViewById(R.id.imageView);
		viewPager = (ViewPager)findViewById(R.id.viewPager);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		sendButton = (Button)findViewById(R.id.sendButton);
	}
	
	private void loadData(){
		
		application = (MyApplication)getApplication();
		
		db = new DBManager(MySQLiteOpenHelper.getInstance(this).getConnection());
		
		String userType = db.getUserInfo().getUserType();
		
		if(userType.equals("ѧ��")){
			mUserType = 3;
		}else{
			mUserType = 2;
		}
		
		receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				
				Toast.makeText(FileExploreActivity.this,
						R.string.tip_attachment_download_success,Toast.LENGTH_SHORT).show();
				
				if(pager1Adapter != null){
					//���¼����Ѿ��ϴ����ļ�
					uploadedFileList.clear();
					new loadFileThread().execute(null,TYPE_FILE_ALREADY_UPLOAD);
				}
						
			}
		};
		registerReceiver(receiver,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		taskID = getIntent().getIntExtra("taskID",0);
		
		selectedFiles = new HashMap<String, File>();
		
		//��ʼ��ͼƬλ��
        imageWidth = BitmapFactory.decodeResource(getResources(),R.drawable.tab_select)
        		.getWidth();// ��ȡͼƬ���
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;// ��ȡ�ֱ��ʿ��
        offset = (screenW / 4 - imageWidth) / 2;// ����ƫ����
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        imageView.setImageMatrix(matrix);// ���ö�����ʼλ��
        
        //��ʼ��viewPager
        uploadedFileList = new ArrayList<File>();
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			new loadFileThread().execute(null,TYPE_FILE_ALREADY_UPLOAD);
        }else{
        	Toast.makeText(FileExploreActivity.this,
        			R.string.tip_sdcard_not_exist,Toast.LENGTH_LONG).show();
         }
        pagerList = new ArrayList<View>();
        pager1 = new ListView(this);
        pager2 = new ListView(this);
        pager3 = new ListView(this);
        pager4 = new ListView(this);
        pagerList.add(pager1);
        pagerList.add(pager2);
        pagerList.add(pager3);
        pagerList.add(pager4);
        viewPager.setCurrentItem(0);
        viewPager.setAdapter(new MyPagerAdapter(pagerList));
	}
	
	@SuppressWarnings("deprecation")
	private void setListener(){
		
		uploadedTab.setOnClickListener(this);
		imageTab.setOnClickListener(this);
		documentTab.setOnClickListener(this);
		audioTab.setOnClickListener(this);
		returnView.setOnClickListener(this);
		cancelView.setOnClickListener(this);
		sendButton.setOnClickListener(this);
		pager1.setOnItemClickListener(new MyOnItemClickListener());
		pager2.setOnItemClickListener(new MyOnItemClickListener());
		pager3.setOnItemClickListener(new MyOnItemClickListener());
		pager4.setOnItemClickListener(new MyOnItemClickListener());
		
		viewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
			private int step = offset*2 + imageWidth;
			@Override
			public void onPageSelected(int arg0) {
				
				Animation animation = null;
				switch(arg0){
				case 0 :						
					switch(currentIndex){
					case 1 : animation = new TranslateAnimation(step, 0, 0, 0);break;
					case 2 : animation = new TranslateAnimation(step*2, 0, 0, 0);break;
					case 3 : animation = new TranslateAnimation(step*3, 0, 0, 0);break;
					}
					break;
				case 1 :		
					//��һ�μ��ظ�ҳ��,��Ҫ��ȡ�ļ�����
					if(imageList == null){
						imageList = new ArrayList<File>();
						if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
							new loadFileThread().execute(new File(filePath+"/DCIM"),TYPE_IMAGE);
			            }else{
			            	Toast.makeText(FileExploreActivity.this,
			            			R.string.tip_sdcard_not_exist,Toast.LENGTH_LONG).show();
			             }
					}
					switch(currentIndex){
					case 0 : animation = new TranslateAnimation(0,step, 0, 0);break;
					case 2 : animation = new TranslateAnimation(step*2, step, 0, 0);break;
					case 3 : animation = new TranslateAnimation(step*3, step, 0, 0);break;
					}
					break;
				case 2 :					
					//��һ�μ��ظ�ҳ��,��Ҫ��ȡ�ļ�����
					if(documentList == null){
						documentList = new ArrayList<File>();
						if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
							new loadFileThread().execute(new File(filePath),TYPE_DOCUMENT);
			            }else{
			            	Toast.makeText(FileExploreActivity.this,
			            			R.string.tip_sdcard_not_exist,Toast.LENGTH_LONG).show();
			             }
					}
					switch(currentIndex){
					case 0 : animation = new TranslateAnimation(0, step*2, 0, 0);break;
					case 1 : animation = new TranslateAnimation(step, step*2, 0, 0);break;
					case 3 : animation = new TranslateAnimation(step*3, step*2, 0, 0);break;
					}
					break;
				case 3 :					
					//��һ�μ��ظ�ҳ��,��Ҫ��ȡ�ļ�����
					if(audioList == null){
						audioList = new ArrayList<File>();
						if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
							new loadFileThread().execute(new File(filePath),TYPE_AUDIO);
			            }else{
			            	Toast.makeText(FileExploreActivity.this,
			            			R.string.tip_sdcard_not_exist,Toast.LENGTH_LONG).show();
			             }	
					}
					switch(currentIndex){
					case 0 : animation = new TranslateAnimation(0, step*3, 0, 0);break;
					case 1 : animation = new TranslateAnimation(step, step*3, 0, 0);break;
					case 2 : animation = new TranslateAnimation(step*2, step*3, 0, 0);break;
					}
					break;
				}
				currentIndex = arg0;
				animation.setFillAfter(true);// True:ͼƬͣ�ڶ�������λ��
	            animation.setDuration(300);
	            animation.setInterpolator(FileExploreActivity.this,
	            		android.R.anim.accelerate_interpolator);
	            imageView.startAnimation(animation);
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
					
					if(CheckNetwork.isConnectingToInternet(FileExploreActivity.this)){
						if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
							
							MyFile mFile = (MyFile)file;
							
							DBManager db = new DBManager(MySQLiteOpenHelper
									.getInstance(FileExploreActivity.this).getConnection());
							
							//�����ϴ��ĸ�����ɾ��,����Ҫɾ����Ӧ���ݿ��еļ�¼,���ڸ�������Ӽ�¼
							if(db.isFilePathExisted(file.getPath())){
								db.deleteUploadFilesFromFilePath(file.getPath());
								
								if(mUserType == 3){
									Attachment a = new Attachment();
									a.setId(taskID);
									a.setNewName(mFile.getName());
									a.setOriginName(file.getName());
									a.setUserID(application.getUserID());
									
									db.addAttachment(a);
								}
							
							}	
							
							DownloadManager manager = (DownloadManager) getSystemService(
									Activity.DOWNLOAD_SERVICE);
							
							String downlUrl = null;
							
							if(mUserType == 3){
								downlUrl = "http://202.202.43.245/UploadFiles/Exercise/FeedBack/"
										+ mFile.getNewName();
							}else{
								downlUrl = "http://202.202.43.245/UploadFiles/Exercise/Define/"
							            + mFile.getNewName();
							}
							
							DownloadManager.Request req = new DownloadManager.Request(
									Uri.parse(downlUrl));
							
							File f = new File(Environment.getExternalStorageDirectory().getPath()
									+"/wenfeng/"+application.getUserID()+"/download");
							if(!f.exists()){
								f.mkdirs();
							}
							
							req.setDestinationInExternalPublicDir("wenfeng/"+application
									.getUserID()+"/download", file.getName());
							req.setTitle(file.getName());
							
							Toast.makeText(FileExploreActivity.this
									,R.string.tip_start_download,Toast.LENGTH_SHORT).show();
							
						    manager.enqueue(req);					
							
						}else{//sd card������
							Toast.makeText(FileExploreActivity.this
									,R.string.tip_sdcard_not_exist,Toast.LENGTH_SHORT).show();
						}
				    }else{//δ��������
				    	Toast.makeText(FileExploreActivity.this
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
		.setNegativeButton(R.string.delete,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if(CheckNetwork.isConnectingToInternet(FileExploreActivity.this)){
					new DeleteAttachmentThread().execute(file);
				}else{
					Toast.makeText(FileExploreActivity.this
							,R.string.tip_network_unavailable,Toast.LENGTH_SHORT).show();
				}
				
			}
		})
		.setMessage(R.string.select_operation)
		.create();
		log.show();
	}
	
	/**
	 *���ý���Ԫ��
	 */
	private void resetMyView(){
		
		cancelView.setVisibility(View.GONE);
		selectedFileSize.setText("0");
		sendButton.setEnabled(false);
		selectedFiles.clear();
		refreshListViews();//�����ͼ���Ѿ���ѡ���ļ�
	}
	
	/**
	 *ˢ��listview�б� 
	 */
	private void refreshListViews(){
		pager1Adapter.notifyDataSetChanged();
		if(pager2Adapter != null){
			pager2Adapter.notifyDataSetChanged();
		}
		if(pager3Adapter != null){
			pager3Adapter.notifyDataSetChanged();
		}
		if(pager4Adapter != null){
			pager4Adapter.notifyDataSetChanged();
		}
	}
	
	
	//--------------------------------�ڲ���----------------------------
	
	class MyOnItemClickListener implements OnItemClickListener{
		
		private static final float MAX_FILE_SIZE = 5;
		
	    /**
	     * ��鵥���ļ���С�Ƿ񳬹������ϴ������ֵ
		 */
		private boolean checkFileSize(String fileSize){
			//ȡ���ļ���С�ĵ�λ����
			String unit = fileSize.substring(fileSize.length()-2);
			
			if(unit.equalsIgnoreCase("KB")){
				return true;
			}
			//ȡ���ļ���С�����ֲ���
			String num = fileSize.substring(0,fileSize.length()-2);
			if(Float.parseFloat(num) < MAX_FILE_SIZE){
				return true;
			}
			return false;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
			
			CheckBox checkBox = ((ViewHolder)view.getTag()).checkBox;
			TextView fileSizeView = ((ViewHolder)view.getTag()).fileSizeView;
			switch(currentIndex){
			case 0 :
				//�ļ�������,����ʾ���ذ�ť
				if(!uploadedFileList.get(position).exists()){
					showDialog(FileExploreActivity.this
							.getResources().getString(R.string.download)
							,uploadedFileList.get(position));
				}else{//�ļ���������ʾ�򿪰�ť
					showDialog(FileExploreActivity.this
							.getResources().getString(R.string.open)
							,uploadedFileList.get(position));
				}
				break;					
			case 1 : 			
				if(checkBox.isChecked()){
					checkBox.setChecked(false);
					selectedFiles.remove("p"+currentIndex+position);
				}else{
					if(checkFileSize(fileSizeView.getText().toString())){
						checkBox.setChecked(true);
						selectedFiles.put("p"+currentIndex+position,imageList.get(position));
					}else{
						Toast.makeText(FileExploreActivity.this,
								R.string.tip_upload_file_size_over_max, Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case 2 : 
				if(checkBox.isChecked()){
					checkBox.setChecked(false);
					selectedFiles.remove("p"+currentIndex+position);
				}else{
					if(checkFileSize(fileSizeView.getText().toString())){
						checkBox.setChecked(true);
						selectedFiles.put("p"+currentIndex+position,documentList.get(position));
					}else{
						Toast.makeText(FileExploreActivity.this,
								R.string.tip_upload_file_size_over_max, Toast.LENGTH_SHORT).show();
					}	
				}
				break;
			case 3 : 
				if(checkBox.isChecked()){
					checkBox.setChecked(false);
					selectedFiles.remove("p"+currentIndex+position);
				}else{
					if(checkFileSize(fileSizeView.getText().toString())){
						checkBox.setChecked(true);
						selectedFiles.put("p"+currentIndex+position,audioList.get(position));
					}else{
						Toast.makeText(FileExploreActivity.this,
								R.string.tip_upload_file_size_over_max, Toast.LENGTH_SHORT).show();
					}		
				}
				break;
			}
			
			if(selectedFiles.size() == 0){
				selectedFileSize.setText("0");
				cancelView.setVisibility(View.GONE);
				sendButton.setEnabled(false);
			}else{
				//������ѡ�ļ���С
				long fileSize = 0;
				for(File file : selectedFiles.values()){
					fileSize+=file.length(); 
				}

				if(cancelView.getVisibility() == View.GONE){
					cancelView.setVisibility(View.VISIBLE);
					sendButton.setEnabled(true);
				}
				selectedFileSize.setText(selectedFiles.size()
						+"("+FileUtil.getFileSize(fileSize)+")");
			}

		}
	}
	
	public static class ViewHolder{
		
		public ImageView imageView;
		public TextView fileNameView;
		public TextView fileSizeView;
		public TextView fileTimeView;
		public CheckBox checkBox;
	}
	
	private class MyListViewAdapter extends BaseAdapter{
		
		private int fileType = TYPE_FILE_ALREADY_UPLOAD;
		private ArrayList<File> fileList; 
		
		public MyListViewAdapter(ArrayList<File> fileList,int fileType){
			
			this.fileType = fileType;
			this.fileList = fileList;
		}
		
		public ArrayList<File> getDataList(){
			return fileList;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return fileList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return fileList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				if(fileType == TYPE_IMAGE || fileType == TYPE_FILE_ALREADY_UPLOAD){
					convertView = LayoutInflater.from(FileExploreActivity.this)
							.inflate(R.layout.layout_image_explore, null);
				}else{
					convertView = LayoutInflater.from(FileExploreActivity.this)
							.inflate(R.layout.layout_file_explore, null);
				}
				holder = new ViewHolder();
				holder.imageView = (ImageView)convertView.findViewById(R.id.image);
				holder.fileNameView = (TextView)convertView.findViewById(R.id.title);
				holder.fileSizeView = (TextView)convertView.findViewById(R.id.size);
				holder.fileTimeView = (TextView)convertView.findViewById(R.id.time);
				holder.checkBox = (CheckBox)convertView.findViewById(R.id.checkBox);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
			}
			
			switch(fileType){
			case TYPE_FILE_ALREADY_UPLOAD : 
				holder.checkBox.setVisibility(View.GONE);
				int type = FileUtil.getFileType(fileList.get(position));
				
				if(type == FileUtil.TYPE_DOCUMENT){
					holder.imageView.setImageResource(R.drawable.document);
				}else{
					if(type == FileUtil.TYPE_AUDIO){
						holder.imageView.setImageResource(R.drawable.music);
					}else{
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.drawable.default_image_background);
						Glide.with(FileExploreActivity.this)
                                .setDefaultRequestOptions(requestOptions)
                                .load(fileList.get(position).getPath())
                                .transition(new DrawableTransitionOptions().crossFade())
                                .into(holder.imageView);
						/*holder.imageView.setImageResource(R.drawable.default_image_background);
						new BitmapUtil.BitmapWorkerTask(holder.imageView).execute(fileList.get(position).getPath());*/
					}	
				}
				break;
			case TYPE_IMAGE :
				RequestOptions requestOptions = new RequestOptions();
				requestOptions.placeholder(R.drawable.default_image_background);
				Glide.with(FileExploreActivity.this)
                        .setDefaultRequestOptions(requestOptions)
                        .load(fileList.get(position).getPath())
                        .transition(new DrawableTransitionOptions().crossFade())
                        .into(holder.imageView);
				/*holder.imageView.setImageResource(R.drawable.default_image_background);
				new BitmapUtil.BitmapWorkerTask(holder.imageView).execute(fileList.get(position).getPath());*/
				break;
			case TYPE_DOCUMENT : holder.imageView.setImageResource(R.drawable.document);break;
			case TYPE_AUDIO : holder.imageView.setImageResource(R.drawable.music);break;
			}
			
			holder.fileNameView.setText(fileList.get(position).getName());
			
			long fileSize = fileList.get(position).length();
			String textSize = FileUtil.getFileSize(fileSize);
			holder.fileSizeView.setText(textSize);
			
			//���ϴ����� ����ʾ�ļ��ϴ�ʱ��
			if(fileType == TYPE_FILE_ALREADY_UPLOAD){
				
				holder.fileTimeView.setText(db.getUploadFileTime(
						fileList.get(position).getPath()));
			}else{//����������ʾ�ļ�����ʱ��
				Date fileTime = new Date(fileList.get(position).lastModified());
				SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
				holder.fileTimeView.setText(format.format(fileTime));
			}
						
			//�û�δѡ���κ�һ���ļ�,Ĭ����ͼ��ʾδѡ��״̬
			if(selectedFiles.size() == 0){
				holder.checkBox.setChecked(false);
			}else{
				//�û��Ѿ���ѡ����ļ�,���Ѿ�ѡ��ı�ʾΪѡ��״̬
				if(selectedFiles.keySet().contains("p"+currentIndex+position)){
					holder.checkBox.setChecked(true);
				}else{
					holder.checkBox.setChecked(false);
				}
			}
			
			return convertView;
		}
		
	}
	//�����ļ����ݵĺ�̨�߳�
	class loadFileThread extends AsyncTask<Object,Integer,Integer>{
	    
	    private void loadFiles(File file,int type){
	    	
			File[] files = null;
			switch(type){
			case TYPE_FILE_ALREADY_UPLOAD : 
				LoadUploadedFiles.loadFiles(uploadedFileList, db, taskID, application.getUserID(), mUserType);
				break;
			case TYPE_IMAGE : 
				files = file.listFiles(new MyImageFilter());
				if(files == null) return;
				for(File mFile : files){
					if(mFile.isFile()){
						imageList.add(mFile);
					}else{
						loadFiles(mFile,type);
					}
				}
				break;
			case TYPE_DOCUMENT :
				files = file.listFiles(new MyDocumentFilter());
				if(files == null) return;
				for(File mFile : files){
					if(mFile.isFile()){
						documentList.add(mFile);
					}else{
						loadFiles(mFile,type);
					}
				}
				break;
			case TYPE_AUDIO : 
				files = file.listFiles(new MyAudioFilter());
				if(files == null) return;
				for(File mFile : files){
					if(mFile.isFile()){
						audioList.add(mFile);
					}else{
						loadFiles(mFile,type);
					}
				}
				break;
			}
	    }
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		@Override
		protected Integer doInBackground(Object... params) {

			int type = (Integer)params[1];
			File file = (File)params[0];
			loadFiles(file,type);
			return type;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			switch(result){
			case TYPE_FILE_ALREADY_UPLOAD :
				
				if(pager1Adapter == null){
					pager1Adapter = new MyListViewAdapter(uploadedFileList, TYPE_FILE_ALREADY_UPLOAD);
					pager1.setAdapter(pager1Adapter);
				}else{
					pager1Adapter.notifyDataSetChanged();
				}
				
				break;
			case TYPE_IMAGE : 
				pager2Adapter = new MyListViewAdapter(imageList, TYPE_IMAGE);
				pager2.setAdapter(pager2Adapter);break;
			case TYPE_DOCUMENT : 
				pager3Adapter = new MyListViewAdapter(documentList, TYPE_DOCUMENT);
				pager3.setAdapter(pager3Adapter);break;
			case TYPE_AUDIO : 
				pager4Adapter = new MyListViewAdapter(audioList, TYPE_AUDIO);
				pager4.setAdapter(pager4Adapter);break;
			}
			progressBar.setVisibility(View.GONE);
		}
		
		class MyDocumentFilter implements FilenameFilter{
	
			@Override
			public boolean accept(File dir, String filename) {
				//linux���Ե�ſ�ʼ���ļ���Ĭ��Ϊ����(ֱ�ӹ��˵�)
				if(new File(dir.toString()+"/"+filename).isHidden()){
					return false;
				}
				//���˵�һЩ�����ļ�
				if(filename.equalsIgnoreCase("cache")){
					return false;
				}
				if(new File(dir.toString()+"/"+filename).isFile()){
					String[] buffer = filename.split("\\.");
					String extension = buffer.length > 1 ? buffer[1]:"";//ȡ���ļ��ĺ�׺
					if("doc".equalsIgnoreCase(extension) || "ppt".equalsIgnoreCase(extension) || "pdf".equalsIgnoreCase(extension) ||
					   "pptx".equalsIgnoreCase(extension) || "xlsx".equalsIgnoreCase(extension) || "xls".equalsIgnoreCase(extension) ||
					   "wps".equalsIgnoreCase(extension) || "dps".equalsIgnoreCase(extension) || "et".equalsIgnoreCase(extension) ||
					   "docx".equalsIgnoreCase(extension)
					   )
					{
						return true;
					}else{
						return false;
					}
				}
				return true;
			}
		}
		
		class MyAudioFilter implements FilenameFilter{
	
			@Override
			public boolean accept(File dir, String filename) {
				//linux���Ե�ſ�ʼ���ļ���Ĭ��Ϊ����(ֱ�ӹ��˵�)
				if(new File(dir.toString()+"/"+filename).isHidden()){
					return false;
				}
				//���˵�һЩ�����ļ�
				if(filename.equalsIgnoreCase("cache")){
					return false;
				}
				if(new File(dir.toString()+"/"+filename).isFile()){
					String[] buffer = filename.split("\\.");
					String extension = buffer.length > 1 ? buffer[1]:"";//ȡ���ļ��ĺ�׺
					if("mp3".equalsIgnoreCase(extension) || "amr".equalsIgnoreCase(extension) || "wma".equalsIgnoreCase(extension) ||
					   "wav".equalsIgnoreCase(extension) || "au".equalsIgnoreCase(extension) || "RealAudio".equalsIgnoreCase(extension) ||
					   "midi".equalsIgnoreCase(extension) || "vqf".equalsIgnoreCase(extension)
					   )
					{
						return true;
					}else{
						return false;
					}
				}
				return true;
			}
		}
		
		class MyImageFilter implements FilenameFilter{
	
			@Override
			public boolean accept(File dir, String filename) {
				//linux���Ե�ſ�ʼ���ļ���Ĭ��Ϊ����(ֱ�ӹ��˵�)
				if(new File(dir.toString()+"/"+filename).isHidden()){
					return false;
				}
				if(new File(dir.toString()+"/"+filename).isFile()){
					String[] buffer = filename.split("\\.");
					String extension = buffer.length > 1 ? buffer[1]:"";//ȡ���ļ��ĺ�׺
					if( "bmp".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension) 
					    || extension.contains("jpeg") ||"jpg".equalsIgnoreCase(extension))
					{
						return true;
					}else{
						return false;
					}
					}
				return true;
			}
		}
    }
	
	//���ļ��ϴ��ĺ�̨�߳�
	class UploadFilesThread extends AsyncTask<Void,String,Integer>{
		
		private int sumCount;//�ϴ��ļ��ܸ���
		private int successCount;//�ϴ��ɹ����ļ�
		private int failCount;//�ϴ�ʧ�ܵ��ļ�
		private ProgressDialog dialog;//�ϴ����ȿ�
		
		@Override
		protected void onPreExecute() {
			
			dialog = new ProgressDialog(FileExploreActivity.this);
			dialog.setCancelable(false);//���������ؼ��رոöԻ���
			dialog.setCanceledOnTouchOutside(false);//���������ⲿ����رոöԻ���
			dialog.setTitle(R.string.upload_files_title);
			dialog.show();
		}

		@Override
		protected Integer doInBackground(Void... params) {
			
			sumCount = selectedFiles.size();
			for(File file : selectedFiles.values()){
				publishProgress(file.getName());
				
				String fileNewName = FileUtil.getStringFromTimestamp("yyyyMMddHHmmssSSS"
						,file.getName());
				
				if(FileUploadWebservice.upload(mUserType,file, taskID,application.getUserID()
						,fileNewName)){

					Date date = new Date();
					SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
					String time = format.format(date);
					
					db.addUploadFiles(application.getUserID(),taskID,file.getAbsolutePath(),
							time,fileNewName);
		
					MyFile mFile = new MyFile(file.getPath());
					mFile.setNewName(fileNewName);
					
					pager1Adapter.getDataList().add(mFile);
					successCount++;
				}else{
					failCount++;
				}
			}
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			
			dialog.setMessage(values[0]);
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			isTaskChanged = true;
			dialog.dismiss();
			Toast.makeText(FileExploreActivity.this,
					FileExploreActivity.this.getResources().getString(R.string.upload_file_sum_count)
					+ sumCount + " "
					+ FileExploreActivity.this.getResources().getString(R.string.upload_file_success_count)
					+ successCount + " "
					+ FileExploreActivity.this.getResources().getString(R.string.upload_file_fail_count)
					+ failCount,
					Toast.LENGTH_LONG).show();
			resetMyView();
		}
	}
	
	/**
	 * ɾ�������ĺ�̨�߳�
	 */
	class DeleteAttachmentThread extends AsyncTask<File,Void,Integer>{
		
		private final static int FAIL = -1;
		private final static int SUCCESS = 0;

		private File file;
		
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(File... params) {
			
			file = params[0];
			
			//�ӷ�����ɾ��
			WebService web = new WebService();
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("userID", application.getUserID());
			p.put("type",String.valueOf(mUserType));
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
				
				db.deleteAttachment(taskID, application.getUserID(),newName);
				db.deleteUploadFilesFromFilePath(file.getPath());
					
				return SUCCESS;
			}
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			
			if(result == FAIL){
				Toast.makeText(FileExploreActivity.this
						,R.string.delete_task_fail, Toast.LENGTH_SHORT).show();
			}else{
				isTaskChanged = true;
				Toast.makeText(FileExploreActivity.this
						,R.string.delete_task_success, Toast.LENGTH_SHORT).show();
				uploadedFileList.remove(file);
				pager1Adapter.notifyDataSetChanged();
			}
		}
		
	}
	
	@SuppressWarnings("serial")
	public static class MyFile extends File{
		
		private String newName = "";

		public MyFile(String path) {
			super(path);
		}

		public String getNewName() {
			return newName;
		}

		public void setNewName(String newName) {
			this.newName = newName;
		}	
	}
}
