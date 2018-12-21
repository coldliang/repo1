package com.cqupt.ui.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.AppInfo;
import com.cqupt.net.WebService;
import com.cqupt.util.AppInfoUtil;
import com.cqupt.util.CheckNetwork;
import com.readystatesoftware.viewbadger.BadgeView;

import java.io.File;

public class SetActivity extends Activity implements OnClickListener{
	
	public static final int LOG_OUT = 1;
	public static final int LANGUAGE = 2;
	public static final int DEFAULT = 3;
	
	private ListView listView;
	private TextView textView;
	private ProgressBar progressBar;
	private MyListViewAdapter adapter;
	private DBManager db;
	
	//��д����

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_set);
		
		listView = (ListView)findViewById(R.id.listView);
		textView = (TextView)findViewById(R.id.returnView);
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		db = new DBManager(MySQLiteOpenHelper.getInstance(this).getConnection());
		
		textView.setOnClickListener(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				
				switch(position){
				case 0 :
					if(!CheckNetwork.isConnectingToInternet(SetActivity.this)){
						Toast.makeText(SetActivity.this,
								R.string.tip_network_unavailable,Toast.LENGTH_SHORT).show();
					}else{
						new CheckAppUpdateThread().execute();
					}
					break;
				case 1 : 
					Intent intent = new Intent(SetActivity.this,FeedbackActivity.class);
					startActivity(intent);
					break;
				/*case 2 : 
					Intent it2 = new Intent();
					it2.putExtra("type", LANGUAGE);
					TextView textView = (TextView)((LinearLayout)view).getChildAt(0);
					it2.putExtra("language", textView.getText().toString());
					setResult(Activity.RESULT_OK,it2);
					finish();	
					break;*/
				case 2 :
					textView = (TextView)((LinearLayout)view).getChildAt(0);
					
					if(textView.getText().toString().equals(getResources().getString(R.string.log_in))){
						Intent it = new Intent(SetActivity.this,LoginActivity.class);
						startActivity(it);
						finish();
					}else{
						Intent it = new Intent();
						it.putExtra("type", LOG_OUT);
						setResult(Activity.RESULT_OK,it);
						finish();
					}
					
					break;

				}
			}
		});
		
		adapter = new MyListViewAdapter();
		listView.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.returnView : onBackPressed();break;
		}
		
	}
	
	class MyListViewAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			
			return 3;
		}

		@Override
		public Object getItem(int position) {
			
			return null;
		}

		@Override
		public long getItemId(int position) {
			
			return 0;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View myView = LayoutInflater.from(SetActivity.this)
					.inflate(R.layout.layout_setview_item, parent,false);
			TextView textView = (TextView)myView.findViewById(R.id.textView1);
			
			switch(position){
			case 0 :
				String currentAppVersion = AppInfoUtil.getLocalVersionName(SetActivity.this);
				textView.setText(SetActivity.this.getResources().
						getString(R.string.check_app_version) + " (" +currentAppVersion + 
						")");
				
				BadgeView badge = new BadgeView(SetActivity.this, textView);
				badge.setText("!");
				
				SharedPreferences sp = getSharedPreferences("my_prefer",Activity.MODE_PRIVATE);
				
				//�а汾����
				if(sp.getBoolean("appUpdate",false)){
					badge.show();
				}
				
				break;
			case 1 : textView.setText(R.string.app_feedback);break;			
			//case 2 : textView.setText(R.string.menu_language);break;
			case 2 : 
				
				//����Ѿ���¼����ʾע����ť
				if(db.login()){
					textView.setText(R.string.menu_logout);
				}else{
					textView.setText(R.string.log_in);
				}
				
				break;

			}
			return myView;
		}	
	}
	
	/**
	 * ���APP�Ƿ���Ҫ���µĺ�̨�߳�
	 */
	class CheckAppUpdateThread extends AsyncTask<Void,String,Integer>{
		
		private final int NO_UPDATE = 1;
		private final int START_UPDATE = 2;
		
		private WebService web = new WebService();
		
		/**
		 * ��¼��������APP�Ļ�����Ϣ
		 */
		private AppInfo info;
		
		@Override
		protected void onPreExecute() {
			
			progressBar.setVisibility(View.VISIBLE);
			
		}

		@Override
		protected Integer doInBackground(Void...voids) {
			
			int webVersionCode = AppInfoUtil.getWebVersionCode(web);
			if(webVersionCode == AppInfoUtil.CONNETION_FAIL){
				return AppInfoUtil.CONNETION_FAIL;
			}
			
			int localVersionCode = AppInfoUtil.getLocalVersionCode(SetActivity.this);
			if(localVersionCode == AppInfoUtil.GET_LOCAL_VERSION_FAIL){
				return AppInfoUtil.GET_LOCAL_VERSION_FAIL;
			}
			
			//��Ҫ���и���
			if(localVersionCode < webVersionCode){
				info = AppInfoUtil.getAppInfo(web);
				if(info == null){
					return AppInfoUtil.CONNETION_FAIL;
				}
				
				return START_UPDATE;
			}
			
			return NO_UPDATE;
		}

		/**
		 * ��ʾ����APP�ĶԻ���
		 */
		@SuppressLint("InflateParams")
		private void showDialog(String date,String versionName,String size,String content){
			
			//�Զ���Ի���������
			View contentView = LayoutInflater.from(SetActivity.this)
					.inflate(R.layout.layout_app_download_dialog,null);
			
			TextView dateView = (TextView)contentView.findViewById(R.id.publishDate);
			TextView versionView = (TextView)contentView.findViewById(R.id.versionName);
			TextView sizeView = (TextView)contentView.findViewById(R.id.size);
			TextView newContentView = (TextView) contentView.findViewById(R.id.content);
			
			dateView.setText(date);
			versionView.setText(versionName);
			sizeView.setText(size);
			newContentView.setText(Html.fromHtml(content));
			
			AlertDialog log = new AlertDialog.Builder(SetActivity.this)
			                  .setTitle(R.string.app_download_title)
			                  .setView(contentView)
			                  .setNegativeButton(R.string.cancel,null)
							  .setPositiveButton(R.string.confirm,
									  new DialogInterface.OnClickListener(){
										@Override
										public void onClick(DialogInterface dialog,int which) {
											new MyDownloadAppThread().execute();
											
											SharedPreferences sp = getSharedPreferences(
													"my_prefer",Activity.MODE_PRIVATE);
											
											SharedPreferences.Editor editor = sp.edit();
											editor.putBoolean("appUpdate",false);
											editor.commit();
											
											adapter.notifyDataSetChanged();	
										}						
							  })
			                  .create();
			log.show();
		}
		
		
		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			switch(result){
			case AppInfoUtil.CONNETION_FAIL :
				Toast.makeText(SetActivity.this,
						R.string.tip_connection_timeout,Toast.LENGTH_SHORT).show();
				break;
			case AppInfoUtil.GET_LOCAL_VERSION_FAIL :
				Toast.makeText(SetActivity.this,
						R.string.tip_get_local_version_code_fail,Toast.LENGTH_SHORT).show();
				break;
			case NO_UPDATE : 
				Toast.makeText(SetActivity.this,
						R.string.no_task_update,Toast.LENGTH_SHORT).show();
				break;
			case START_UPDATE :
				String publishDate = info.getPublishDate();
				String versionName = info.getVersionName();
				String size = info.getSize();
				String content = info.getContent();
				
				showDialog(publishDate, versionName, size,content);
				break;
			}
			
		}
	}
	
	/**
	 * ����APP�ĺ�̨�߳�
	 */
	class MyDownloadAppThread extends AsyncTask<Void,Integer,Integer>{
		
		private final String URL = "http://202.202.43.244/wenfeng.apk";
		
		private final int NO_SD_CARD = 0;
		private final int START_DOWNLOAD = 1;

		@Override
		protected Integer doInBackground(Void...voids) {
			
			return downloadApp() == START_DOWNLOAD ? START_DOWNLOAD : NO_SD_CARD;
		}
		
		/**
		 * ����APP
		 */
		private int downloadApp(){
			
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				DownloadManager dm = (DownloadManager)SetActivity.this
						.getSystemService(Context.DOWNLOAD_SERVICE);
				DownloadManager.Request req = new DownloadManager.Request(Uri.parse(URL));
				
				File file = new File(Environment.getExternalStorageDirectory().getPath()+"/wenfeng");
				if(!file.exists()){
					file.mkdirs();
				}
				
				req.setDestinationInExternalPublicDir("wenfeng","wenfeng.apk");
				req.setTitle("�ķ��ѧӢ��");
				req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				dm.enqueue(req);
				return START_DOWNLOAD;
			}else{
				return NO_SD_CARD;
			}
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			switch(result){
			case NO_SD_CARD :
				Toast.makeText(SetActivity.this,
						R.string.tip_sdcard_not_exist,Toast.LENGTH_SHORT).show();
				break;
			case START_DOWNLOAD :
				Toast.makeText(SetActivity.this,
						R.string.tip_start_download, Toast.LENGTH_SHORT).show();
			}
		}
	}
}
