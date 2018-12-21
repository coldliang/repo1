package com.cqupt.ui.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.PaintDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ZoomControls;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.Attachment;
import com.cqupt.model.Test;
import com.cqupt.model.TestItem;
import com.cqupt.model.TestItemOption;
import com.cqupt.net.WebService;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.DensityUtil;
import com.cqupt.util.FileUtil;
import com.cqupt.util.MyImageGetter;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.util.XMLParser;
import com.cqupt.view.MyAudioPlayer;
import com.cqupt.view.MyAudioPlayer.OnDragListener;

import org.ksoap2.serialization.SoapObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ITestHandlingActivity extends Activity implements OnClickListener{
	
    public final static int VIDEO_FULL_SCREEN = 1;
	
	protected final String multimediaDownloadUri = "http://202.202.43.245/UploadFiles/Test/";
	
	protected MyApplication application;
	
	protected ScrollView sv;//���ݴ��ڹ�����ͼ
	protected LinearLayout svCenter;//���ݴ��ڹ�����ͼ�е�linearlayout
	protected TextView returnView;//�������
	protected ImageView menuListView;//��ʾ��Ŀ�б��ͼ��
	protected ZoomControls zoomView;//�����������
	protected TextView titleView;//��Ŀ����
	protected TextView levelView;//��Ŀ�Ѷȵȼ�
	protected TextView fromView;//������
	protected TextView tests;//��ʾ��ǰ��Ŀ������Ŀ��
	protected TextView contentView;//��ʾ��Ŀ���
	protected MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(this);//���ݿ��������
	protected ImageButton commentButton;//�鿴���۰�ť
	protected ImageView multimediaListView;//��ʾ��ý���б��ͼ��
	protected VideoView videoView;
	protected MediaController controller;
	protected LinearLayout videoCoverView;
	protected PopupWindow multimediaPopupWindow;
	protected PopupWindow menuListPopupWindow;
	protected RelativeLayout buttomView;
	protected ProgressBar progressBar;
	protected MultimediaListViewAdapter multimediaListViewAdapter;
	protected OrientationEventListener orientationListener;
	private GestureDetector gestureDetector;
	private MyAudioPlayer audioPlayer;
	
	protected ArrayList<HashMap<String, String>> questionList;//������Ŀ��ѡ���
	protected List<Test> testList;//�洢����б�
	protected List<Attachment> multimediaList;//�洢��Ŀ�еĶ�ý���ļ�
	protected int ID;//�����ƻ�ID
	protected float textSize = 18;//Ĭ�������С
	protected int currentPage = 1;//��¼��ǰҳ�ı���
	protected int sumPage = 0;//��¼����Ŀ���ı���
	protected boolean isFromFullScreen = false;
	//video�Ĳ���״̬
	protected int currentMultimediaIndex;//��¼��ǰ���ŵĶ�ý���ļ�
	protected boolean isVideoPlaying = false;
	protected int currentVideoPosition = 1;
	protected boolean isSeekbarDragging = false;//��¼�Ƿ������϶���Ƶ(��Ƶ)���ŵĽ�����
	private boolean isLoadingData;
	protected DBManager mDb = new DBManager(helper.getConnection());
	protected WebService mWeb = new WebService();
	
	
	//---------------------���󷽷�--------------------
		
	/**
	 *��̬������Ŀ�б�
	 */
	abstract protected void makeQuestionListView();
	
	/**
	 * �����Ŀ�б�
	 */
	abstract protected List<Test> getTestList();
	abstract protected boolean isTestFinished(int id,int testID,String userID);
	
	//------------------------------��д����--------------------------
	
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);//ȡ��������
			
		}
		
		@Override
		public void onClick(View v) {
			
			switch(v.getId()){
			case R.id.returnView : finish();break;
			case R.id.comment :
				
				if(videoView != null && videoView.isPlaying()){
					videoView.stopPlayback();
				}
				
				if(audioPlayer != null && audioPlayer.isPlaying()){
					audioPlayer.pause();
				}
				
				Intent it  = new Intent(this,CommentListActivity.class);
				it.putExtra("ID",testList.get(currentPage-1).getTestID());
				startActivity(it);
				break;
			case R.id.multimediaList :
				toggleMultimediaPopupWindow();
				break;
			case R.id.menuListView:
				toggleMenuListPopupWindow();
				break;
			}
		}
		
		@Override
		public void onBackPressed() {
			
			if(menuListPopupWindow != null && menuListPopupWindow.isShowing()){
				menuListPopupWindow.dismiss();
			}else if(multimediaPopupWindow != null && multimediaPopupWindow.isShowing()){
				multimediaPopupWindow.dismiss();
			}else{//�˳���ǰactivity
				
				//�ͷ���Դ
				if(videoView != null && videoView.isPlaying()){
					videoView.stopPlayback();
					videoView = null;
				}
				
				if(audioPlayer != null){
					audioPlayer.stop();
					audioPlayer = null;
				}
				
				super.onBackPressed();
			} 
		
		}

		
		@Override
		protected void onRestart() {
			
			if(!isFromFullScreen){
				recoverVideoView();		
			}else{
				isFromFullScreen = false;
			}

			super.onResume();
		}

		@Override
		protected void onPause() {
			
			if(!isFromFullScreen){
				saveVideoViewState();
			}
			
			super.onPause();
		}
		
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			
			//�ӳ�3������¼����Ļ��ת�¼�
			new Handler().postDelayed(new Runnable() {
				
				@Override
				public void run() {
					
					if(orientationListener != null){
						orientationListener.enable();
					}
					
				}
			}, 3000);

			if(requestCode == VIDEO_FULL_SCREEN && resultCode == Activity.RESULT_OK){
				isFromFullScreen = true;
				isVideoPlaying = data.getBooleanExtra("isVideoPlaying", false);
				currentVideoPosition = data.getIntExtra("currentVideoPosition", 0);
				
				recoverVideoView();
			}
			
		}
		
		@Override
		protected void onDestroy() {
			orientationListener.disable();
			orientationListener = null;
			
			if(videoView != null && videoView.isPlaying()){
				videoView.stopPlayback();
			}
			
			if(audioPlayer != null && audioPlayer.isPlaying()){
				audioPlayer.stop();
			}
			
			super.onDestroy();
		}
		
		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			
			gestureDetector.onTouchEvent(ev);
			return super.dispatchTouchEvent(ev);
			
		}
				

		//----------------------------�Զ��巽��----------------------
		
		private ArrayList<HashMap<String, String>> getQuestionList(int position) {
			
			ArrayList<HashMap<String, String>> list =  mDb.getQuestions(testList.get(position).getTestID());
			
			if(list.size() == 0){
				HashMap<String, String> p = new HashMap<String, String>();
				p.put("testID", String.valueOf(testList.get(position).getTestID()));
				
				SoapObject result = mWeb.CallWebService("getTestItem", p);
				ArrayList<TestItem> itemList = null;
				
				if(result != null){
					itemList = XMLParser.parseTestItem(result);
					
					if(itemList != null){
						for(int i = 0;i < itemList.size();i ++){
							HashMap<String,String> map = new HashMap<String,String>();
							map.put("title", itemList.get(i).getTestItemContent());
							map.put("itemID", String.valueOf(itemList.get(i).getTestItemID()));
							
							p.clear();
							p.put("testItemID", String.valueOf(itemList.get(i).getTestItemID()));
							
							result = mWeb.CallWebService("getTestItemOption", p);
							
							ArrayList<TestItemOption> optionList = null;
							
							if(result != null){
								optionList = XMLParser.parseTestItemOption(result);
								
								if(optionList != null){
									for(int j = 0; j < optionList.size();j++){
										map.put("c"+j+"ID", String.valueOf(optionList.get(j)
												.getTestItemOptionID()));
										map.put("c"+j, optionList.get(j).getTestItemOptionContent());
									}
								}
								
							}
							
							list.add(map);
							}
					}
					
				}

				return list ;
			}
			
			return list;
			
		}
		
		private List<Attachment> getMultimediaList(int position) {
			
			List<Attachment> list = mDb.getAttachment(testList.get(position).getTestID());
			
			//���������
			if(list.size() == 0){
				HashMap<String, String> p = new HashMap<String, String>();
				p.put("id",String.valueOf(testList.get(position).getTestID()));
				p.put("type", String.valueOf(1));
				p.put("userID", "");
				
				SoapObject result = mWeb.CallWebService("getAttachment", p);
				
				if(result != null){
					list = XMLParser.parseAttachment(result);
				}
				
				return list;
				
			}
			
			return list;
			
		}
		
		
		private void toggleMenuListPopupWindow(){
				
			if(menuListPopupWindow == null){
				ListView contentView = new ListView(this);
				contentView.setBackgroundColor(Color.WHITE);
				final MenuListAdapter adapter = new MenuListAdapter();
				contentView.setAdapter(adapter);
				
				contentView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
							
						if(!isLoadingData){
							//�л����ǲ�ͬҳʱ�Ž���ˢ��
							if(position + 1 != currentPage){	
								
								pageMove(currentPage,position + 1);							
								adapter.notifyDataSetChanged();											
								
							}
						}
								
					}
				});
				
				menuListPopupWindow = new PopupWindow(contentView, DensityUtil.getWindowWidth(this)/2, 
						ViewGroup.LayoutParams.MATCH_PARENT, true);
				
				menuListPopupWindow.setBackgroundDrawable(new PaintDrawable());
				
				menuListPopupWindow.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss() {
						
						menuListView.setImageResource(R.drawable.show_menu);
						
					}
				});
				
			}
			
			if(menuListPopupWindow.isShowing()){
				menuListPopupWindow.dismiss();
				menuListView.setImageResource(R.drawable.show_menu);
			}else{
				menuListPopupWindow.showAsDropDown(findViewById(R.id.top1), 
						DensityUtil.getWindowWidth(this)/2, 0);
				menuListView.setImageResource(R.drawable.hide_menu);
			}
			
		}

		/**
		 *��������
		 */
		protected void loadData(){
			
			application = (MyApplication)getApplication();
			
			gestureDetector = new GestureDetector(this,new SimpleOnGestureListener(){
				
				public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
		                float velocityY) {
					
					//isSeekbarDragging�ж��Ƿ��ڻ�����Ƶ���ŵĽ�����
					//isLoadingData�ж��Ƿ�ǰҳ���ڼ�������					
					if(!isSeekbarDragging && !isLoadingData 
							&& Math.abs(velocityX) > 300.0){
						float startX = e1.getX();
						float endX = e2.getX();
						
						if(startX - endX > 200 && currentPage < testList.size()){//���󻬶�����һҳ
							pageMove(currentPage,currentPage + 1);					
						}else if(endX - startX > 200 && currentPage > 1){//���һ�������һҳ
							pageMove(currentPage,currentPage - 1);					
						}
						
					}
					
		            return true;
		        }
				
			});
							
			Intent it = getIntent();
			ID = it.getIntExtra("ID",0);
			
			orientationListener = new OrientationEventListener(this) {
				
				@Override
				public void onOrientationChanged(int orientation) {
					
					if(orientation > 270 && orientation < 280){
						
						//����Ƶ��ȫ������
						if(FileUtil.getFileType(multimediaList.get(currentMultimediaIndex)
								.getNewName()) != FileUtil.TYPE_AUDIO)
						{
							isFromFullScreen = true;
							orientationListener.disable();
							
							Intent it = new Intent(ITestHandlingActivity.this
									,FullScreenVideoActivity.class);
							it.putExtra("isVideoPlaying", videoView.isPlaying());
							it.putExtra("currentVideoPosition", videoView.getCurrentPosition());
							it.putExtra("videoUri",(String)videoView.getTag());
							startActivityForResult(it, VIDEO_FULL_SCREEN);
						}
						
					}
					
				}
			};
			
			new GetInfoThread().execute();
			
		}
		
		
		/**
		 *��ȡXML�е����
		 */
		protected void findView(){
			
			svCenter = (LinearLayout) findViewById(R.id.svCenter);
			sv = (ScrollView)findViewById(R.id.scrollView);
		    titleView = (TextView)findViewById(R.id.title);
			fromView = (TextView)findViewById(R.id.from);
			levelView = (TextView)findViewById(R.id.level);
			zoomView = (ZoomControls)findViewById(R.id.zoomControls1);
			returnView = (TextView)findViewById(R.id.returnView);
			tests = (TextView)findViewById(R.id.textView2);
		    contentView = (TextView)findViewById(R.id.content1);
		    //contentView.setTextIsSelectable(true);
		    commentButton = (ImageButton)findViewById(R.id.comment);		    
		    multimediaListView = (ImageView) findViewById(R.id.multimediaList);
		    buttomView = (RelativeLayout) findViewById(R.id.bottom);
		    progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		    menuListView = (ImageView) findViewById(R.id.menuListView);
		    
		}
		
		/**
		 *��Ӽ���
		 */
		protected void setListener(){
			
			returnView.setOnClickListener(this);			
			commentButton.setOnClickListener(this);
			multimediaListView.setOnClickListener(this);
			menuListView.setOnClickListener(this);
			
			zoomView.setOnZoomInClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					contentView.setTextSize(++textSize);
				}
			});
			zoomView.setOnZoomOutClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					contentView.setTextSize(--textSize);
				}
			});	
		
		}

		/**
		 * ��ʾ���Ŷ�ý��Ĵ���
		 */
		private void prepareMultimediaView(){
			//currentMultimediaIndex = 0;

			// ��Ƶ�ļ�����ʾ ��Ƶ������
			if (FileUtil.getFileType(multimediaList.get(currentMultimediaIndex)
					.getNewName()) == FileUtil.TYPE_AUDIO) {
				
				showAudioView();
				hideVideoView();
				hideVideoCoverView();
					
			} else {// ��Ƶ�ļ�����ʾ ��Ƶ������
				showVideoView();
				hideAudioView();
			}
	
			String originName = multimediaList.get(currentMultimediaIndex).getOriginName();
			String newName = multimediaList.get(currentMultimediaIndex).getNewName();
	
			setMultimediaResource(originName, newName);

	  }
		
		//������Ƶ���Ŵ���
		private void hideVideoView(){
			if(videoView != null && videoView.getVisibility() == View.VISIBLE){
				videoView.setVisibility(View.GONE);
				
				if(videoView.isPlaying()){
					videoView.stopPlayback();
				}
				
			}
		}
		
		//��ʾ��Ƶ���Ŵ���
		private void showVideoView(){
			if (videoView == null) {
				ViewStub viewStub = (ViewStub) findViewById(R.id.viewStub);
				videoView = (VideoView) viewStub.inflate();
				controller = new MediaController(this);
				videoView.setMediaController(controller);
				controller.setMediaPlayer(videoView);
				videoView.setOnPreparedListener(new OnPreparedListener() {

					@Override
					public void onPrepared(MediaPlayer mp) {
						
						if(videoCoverView != null){
							videoCoverView.setVisibility(View.GONE);
						}	

					}
				});
			}
			
			if(videoView.getVisibility() == View.GONE){
				videoView.setVisibility(View.VISIBLE);
			}
		}
		
		//��ʾ��ƵcoverView
		private void showVideoCoverView(){
			
			if(videoCoverView == null){					
				ViewStub viewStub = (ViewStub) findViewById(R.id.viewStub2); 
				videoCoverView = (LinearLayout) viewStub.inflate();
			}
			
			if(videoCoverView.getVisibility() == View.GONE){
				videoCoverView.setVisibility(View.VISIBLE);
			}
			
		}
		
		//������ƵcoverView
		private void hideVideoCoverView(){
			if(videoCoverView != null && videoCoverView.getVisibility() == View.VISIBLE){
				videoCoverView.setVisibility(View.GONE);
			}
		}
		
		//��ʾ��Ƶ���Ŵ���
		private void showAudioView(){
			
			if (audioPlayer == null) {
				ViewStub viewStub = (ViewStub) findViewById(R.id.viewStub3);
				audioPlayer = (MyAudioPlayer) viewStub.inflate();
				audioPlayer.setOnDragListener(new OnDragListener() {
					
					@Override
					public void onDragStop() {
						
						isSeekbarDragging = false;
						
					}
					
					@Override
					public void onDragStart() {
						
						isSeekbarDragging = true;
						
					}
				});
			}
			
			if(audioPlayer.getVisibility() == View.GONE){
				audioPlayer.setVisibility(View.VISIBLE);
			}
			
		}
		
		//������Ƶ���Ŵ���
		private void hideAudioView(){
			if(audioPlayer != null && audioPlayer.getVisibility() == View.VISIBLE){
				audioPlayer.setVisibility(View.GONE);
				audioPlayer.stop();
			}
		}
		
		/**
		 * ���ò��ŵ���Դ�ļ�
		 */
		private void setMultimediaResource(String localName,String netName){
			
			boolean isForVideoView = true;
			
			//���ж��ǲ�����Ƶ���ǲ�����Ƶ
			
			if(videoView != null && videoView.getVisibility() == View.VISIBLE){
				
				showVideoCoverView();
				
				if(videoView.isPlaying()){
					videoView.stopPlayback();
				}
				
			}else{
				
				isForVideoView = false;
				if(audioPlayer != null){
					audioPlayer.stop();
				}
				
			}
			
			File file = new File(Environment.getExternalStorageDirectory().getPath()
					+ "/wenfeng/"+application.getUserID()+"/download/"+ localName);
			
			//���ȼ��ر����ļ�
			if(file.exists()){
				
				if(isForVideoView){
					videoView.setVideoPath(file.getPath());
					videoView.setTag(file.getPath());//��¼����·��������ȫ������ʱ���
				}else{
					try {
						audioPlayer.setLocalFile(file.getPath());
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(this, R.string.tip_file_cannot_play, Toast.LENGTH_SHORT).show();
					}
				}
				
			}else{
				//wifi״̬�²���������Ƶ
				if(CheckNetwork.isWifiAvailable(this)){
					
					if(netName.startsWith("http://") || netName.startsWith("ftp://")){
						
						if(isForVideoView){
							videoView.setVideoURI(Uri.parse(netName));
							videoView.setTag(netName);//��¼����·��������ȫ������ʱ���
						}else{
							try {
								audioPlayer.setUri(this, Uri.parse(netName));
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(this, R.string.tip_file_cannot_play, Toast.LENGTH_SHORT).show();
							}
						}
						
					}else{
						
						if(isForVideoView){
							videoView.setVideoURI(Uri.parse(multimediaDownloadUri + netName));
							videoView.setTag(multimediaDownloadUri + netName);//��¼����·��������ȫ������ʱ���
						}else{
							try {
								audioPlayer.setUri(this, Uri.parse(multimediaDownloadUri + netName));
							} catch (Exception e) {
								e.printStackTrace();
								Toast.makeText(this, R.string.tip_file_cannot_play, Toast.LENGTH_SHORT).show();
							}
						}
						
					}
					
					
				}
			}	
			
			if(isForVideoView){
				videoView.seekTo(1);		
			}

			
		};
		
		/**
		 * ���ز��Ŷ�ý��Ĵ���
		 */
		private void clearMultimediaView(){
			
			hideAudioView();
			hideVideoCoverView();
			hideVideoView();
			
		}
		
		private void toggleMultimediaPopupWindow(){
			
			if(multimediaPopupWindow == null){
				ListView contentView = new ListView(this);
				contentView.setBackgroundColor(getResources().getColor(R.color.divider));
				multimediaListViewAdapter = new MultimediaListViewAdapter();
				contentView.setAdapter(multimediaListViewAdapter);
				
				multimediaPopupWindow = new PopupWindow(contentView,RelativeLayout.LayoutParams.MATCH_PARENT, 
						300,false);	
				multimediaPopupWindow.setAnimationStyle(R.style.popupWindowAnimationFadeInBottomToTop);
				multimediaPopupWindow.setTouchable(true);
		
			}
			
			if(multimediaPopupWindow.isShowing()){
				multimediaPopupWindow.dismiss();
			}else{
				multimediaPopupWindow.showAsDropDown(buttomView,0,-300);
			}
				
		}
		
		private void downloadFile(String originName,String newName){
			
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				DownloadManager dm = (DownloadManager) getSystemService(
						Activity.DOWNLOAD_SERVICE);	
				
				String uri = null;
				if(newName.startsWith("http://") || newName.startsWith("ftp://")){
					uri = newName;
				}else{
					uri = multimediaDownloadUri + newName;
				}
				
				DownloadManager.Request req = new DownloadManager.Request(
						Uri.parse(uri));
				req.setTitle(originName);
				
				File file = new File(Environment.getExternalStorageDirectory() + "/wenfeng/download");
				
				if(!file.exists()){
					file.mkdirs();
				}
				
				req.setDestinationInExternalPublicDir("/wenfeng/download/", originName);
				req.setNotificationVisibility(DownloadManager.Request
						.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				dm.enqueue(req);
				Toast.makeText(this, R.string.tip_start_download, Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this,R.string.tip_sdcard_not_exist,Toast.LENGTH_SHORT).show();
			}	
			
		}
		
		/**
		 * �ָ���Ƶ����״̬
		 */
		private void recoverVideoView(){
			
			if(videoView != null && videoView.getVisibility() == View.VISIBLE){		
				String originName = multimediaList.get(currentMultimediaIndex).getOriginName();
				String newName = multimediaList.get(currentMultimediaIndex).getNewName();
				
				setMultimediaResource(originName, newName);
				videoView.seekTo(currentVideoPosition);
				
				if(isVideoPlaying){
					videoView.start();				
				}			
			}
				
		}
		
		private void saveVideoViewState(){
			
			//�������Ƶ�����¼��Ƶ����״̬
			if(videoView != null && videoView.getVisibility() == View.VISIBLE){
				isVideoPlaying = videoView.isPlaying();
				currentVideoPosition = videoView.getCurrentPosition();
			}
			
		}
		
		/**
		 * ��Ŀ��ҳ���л�
		 */
		protected void pageMove(int from,int to){
			
			zoomView.setEnabled(false);
			
			currentPage = to;
			
			if(multimediaPopupWindow != null && multimediaPopupWindow.isShowing()){
				multimediaPopupWindow.dismiss();
			}
			
			if(menuListPopupWindow != null && menuListPopupWindow.isShowing()){
				menuListPopupWindow.dismiss();
			}
			
			if(videoView != null && videoView.isPlaying()){
				videoView.stopPlayback();
			}
			
			if(audioPlayer != null){
				audioPlayer.stop();
			}
			
			currentMultimediaIndex = 0;
			
			//�������ݽ���
			tests.setText(currentPage+"/"+sumPage);
			titleView.setText(testList.get(currentPage-1).getTestTitle());
			fromView.setText(testList.get(currentPage-1).getCreateUser());
			levelView.setText(testList.get(currentPage-1).getTestTypeName());
			
			String content = testList.get(currentPage-1).getTestContent();
			contentView.setText(Html.fromHtml(content,new MyImageGetter(content
					, contentView,this),null));
			
			new ReloadInfoThread().execute();
			
		}
		
		//----------------------------�ڲ���----------------------------------------
		
		class MultimediaListViewAdapter extends BaseAdapter {
			
			public class ViewHolder {
				TextView titleView;
				ImageView downloadView;
			}
			
			private OnClickListener listener = new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					currentMultimediaIndex = (Integer)v.getTag();

					String originName = multimediaList.get(currentMultimediaIndex)
							.getOriginName();
					String newName = multimediaList.get(currentMultimediaIndex).getNewName();
					
					if(v.getId() == R.id.title){
						//setMultimediaResource(originName, newName);
						prepareMultimediaView();
						multimediaListViewAdapter.notifyDataSetChanged();
					}else{
						
						if(CheckNetwork.isConnectingToInternet(ITestHandlingActivity.this)){
							downloadFile(originName, newName);
						}else{
							ShowToastUtil.showNetworkUnavailableToast(ITestHandlingActivity.this);
						}
						
					}
				}
			};
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return multimediaList.size();
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
					convertView = LayoutInflater.from(ITestHandlingActivity.this)
							.inflate(R.layout.layout_multimedia_list,parent,false);
					
					holder = new ViewHolder();
					holder.titleView = (TextView) convertView.findViewById(R.id.title);
					holder.downloadView = (ImageView) convertView.findViewById(R.id.download);
					
					convertView.setTag(holder);
					
				}else{
					holder = (ViewHolder) convertView.getTag();
				}
				
				//���ڲ��ŵ��ļ�����ɫ��ʾ
				if(currentMultimediaIndex == position){
					holder.titleView.setTextColor(ITestHandlingActivity.this
							.getResources().getColor(R.color.background_darkblue));
				}else{
					holder.titleView.setTextColor(ITestHandlingActivity.this.getResources()
							.getColor(android.R.color.black));
				}
				
				holder.titleView.setText(multimediaList.get(position).getOriginName());
				holder.titleView.setTag(position);
				holder.titleView.setOnClickListener(listener);

				
				File file = new File(Environment.getExternalStorageDirectory().getPath()
						+ "/wenfeng/download/" + multimediaList.get(position).getOriginName());
				
				//�Ѿ����ع��Ĳ�����ʾ���ذ�ť
				if(file.exists()){
					holder.downloadView.setVisibility(View.GONE);
				}else{
					holder.downloadView.setVisibility(View.VISIBLE);
					holder.downloadView.setTag(position);
					holder.downloadView.setOnClickListener(listener);
				}			
				
				return convertView;
			}

		}
		
		/**
		 *�Զ�����ҵ�˵��б�������
		 */
		public class MenuListAdapter extends BaseAdapter{	
			
			@Override
			public int getCount() {
				// TODO Auto-generated method stub
				return testList.size();
			}

			@Override
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@Override
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}

			@SuppressLint("ViewHolder")
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				
				View myView = LayoutInflater.from(ITestHandlingActivity.this)
						.inflate(R.layout.layout_test_handling_menu, parent,false);
				
				if(currentPage-1 == position){
					myView.setBackgroundColor(getResources().getColor(R.color.divider));					
				}
				
				TextView title = (TextView)myView.findViewById(R.id.title);
				TextView page = (TextView)myView.findViewById(R.id.page);
				TextView user = (TextView)myView.findViewById(R.id.create_user);
				TextView level = (TextView)myView.findViewById(R.id.level);
				ImageView image = (ImageView)myView.findViewById(R.id.imageView1);
				title.setText(testList.get(position).getTestTitle());
				page.setText(String.valueOf(position+1));
				user.setText(testList.get(position).getCreateUser());
				level.setText(testList.get(position).getTestTypeName());
				
				int testID = testList.get(position).getTestID();
				String userID = ((MyApplication)getApplication()).getUserID();
				
				if(isTestFinished(ID,testID,userID)){
					image.setBackgroundResource(R.drawable.finish1);
				}

				return myView;
				
			}
			
		}
		
		//----------------------�ڲ���-------------------------------
		
		class GetInfoThread extends AsyncTask<Void, Void, Void>{
			
			@Override
			protected void onPreExecute() {
                progressBar.setVisibility(View.VISIBLE);              
                isLoadingData = true;
				super.onPreExecute();
			}

			@Override
			protected Void doInBackground(Void... params) {
				testList = getTestList();//���ݿ��ȡ����б�
				questionList = getQuestionList(0);
				multimediaList = getMultimediaList(0);				
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				sumPage = testList.size();
				tests.setText(String.valueOf(currentPage)+"/"+String.valueOf(sumPage));
				String testContent = testList.get(currentPage-1).getTestContent();
				String testTitle = testList.get(currentPage-1).getTestTitle();
				titleView.setText(testTitle);
				fromView.setText(testList.get(currentPage-1).getCreateUser());
				levelView.setText(testList.get(currentPage-1).getTestTypeName());
				
				//��ȡhtml��ʽ
				contentView.setText(Html.fromHtml(testContent
						,new MyImageGetter(testContent,contentView,ITestHandlingActivity.this),null));
				contentView.setMovementMethod(ScrollingMovementMethod.getInstance());//��ӹ�����
				
				//��ʾ��ý�岥����
				if(multimediaList.size() != 0){
					prepareMultimediaView();
					orientationListener.enable();
				}else{
					multimediaListView.setVisibility(View.GONE);
				}		
				
				//��̬������Ŀ�б�
				makeQuestionListView();

				progressBar.setVisibility(View.GONE);
				isLoadingData = false;				
			}
			
		}
		
		//�л���Ŀʱ���¼�������
		class ReloadInfoThread extends AsyncTask<Void, Void, Void>{

			@Override
			protected void onPreExecute() {
				progressBar.setVisibility(View.VISIBLE);
				isLoadingData = true;
				super.onPreExecute();
			}
			@Override
			protected Void doInBackground(Void... params) {
				questionList = getQuestionList(currentPage-1);
				multimediaList = getMultimediaList(currentPage - 1);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				
				makeQuestionListView();
				
				if(multimediaList.size() != 0){
					prepareMultimediaView();
					orientationListener.enable();
					multimediaListView.setVisibility(View.VISIBLE);
				}else{
					orientationListener.disable();
					multimediaListView.setVisibility(View.GONE);
					clearMultimediaView();
				}
				
				//�ص���Ŀ�ײ�
				sv.smoothScrollTo(0, 0);			
				
				progressBar.setVisibility(View.GONE);
				isLoadingData = false;
			}
			
		}

}
