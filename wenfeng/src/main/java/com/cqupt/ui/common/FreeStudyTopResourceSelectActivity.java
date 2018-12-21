package com.cqupt.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.TestType;
import com.cqupt.net.WebService;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.CommonListViewAdapter;
import com.cqupt.util.DownloadResourceType;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.List;

public class FreeStudyTopResourceSelectActivity extends Activity implements OnClickListener{
	
	private TextView mReturnView;
	private ListView mListView;
	private Button mButton;
	private ProgressBar mProgressBar;
	private TextView mUpdateView;
	
	private CommonListViewAdapter<TestType> mListViewAdapter;
	private List<TestType> mListViewData = new ArrayList<TestType>();
	private SparseIntArray mSelectedResource = new SparseIntArray();
	private DBManager mDb;
	
	
	//---------------------��д����---------------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_free_study_top_resource_select);
		
		findView();
		loadData();
		setListener();
		
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.returnView : onBackPressed();break;
		case R.id.button :
			
			if(mSelectedResource.size() == 0){
				Toast.makeText(this, R.string.tip_no_resource_selected, Toast.LENGTH_SHORT).show();
			}else{
				Intent it = new Intent(this,FreeStudySecondResourceSelectActivity.class);
				
				StringBuilder sb = new StringBuilder();
				for(int i = 0 ; i < mSelectedResource.size(); i++){
					
					if(i == mSelectedResource.size() - 1){
						sb.append(mSelectedResource.valueAt(i));
					}else{
						sb.append(mSelectedResource.valueAt(i) + ",");
					}
					
				}
				
				it.putExtra("testTypeIDs", sb.toString());
				startActivity(it);
			}
			
			break;
		case R.id.update : 
			
			if(CheckNetwork.isConnectingToInternet(this)){
				new UpdateResourceTypeThread().execute();
			}else{
				ShowToastUtil.showNetworkUnavailableToast(this);
			}
			
			break;
		}
		
	}
	
	//------------------------�Զ��巽��-------------------------------
	
	private void findView(){
		
		mReturnView = (TextView) findViewById(R.id.returnView);
		mUpdateView = (TextView) findViewById(R.id.update);
		mListView = (ListView) findViewById(R.id.listView);
		mButton = (Button) findViewById(R.id.button);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		
	}
	
	private void loadData(){
		
		mDb = new DBManager(MySQLiteOpenHelper.getInstance(this).getConnection());
		new GetListViewDataThread().execute();
		
	}
	
	private void setListener(){
		
		mReturnView.setOnClickListener(this);
		mButton.setOnClickListener(this);
		mUpdateView.setOnClickListener(this);
	
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				@SuppressWarnings("unchecked")
				CommonListViewAdapter<TestType>.ViewHolder viewHolder = 
						(CommonListViewAdapter<TestType>.ViewHolder)view.getTag();
				
				CheckBox checkBox = (CheckBox) viewHolder.getView(R.id.checkBox1);
				
				if(checkBox.isChecked()){
					mSelectedResource.delete(position);
					checkBox.setChecked(false);
				}else{
					mSelectedResource.append(position, mListViewData.get(position).getTestTypeID());
					checkBox.setChecked(true);
				}
				
			}
		});
		
	}
	
	//-----------------�ڲ���-----------------------------
	
	class GetListViewDataThread extends AsyncTask<Void, Void, Integer>{
		
		private final static int SUCCESS = 1;
		private final static int FAIL = 0;
		private final static int NO_LOCAL_RES = 2;
		
		private WebService web = new WebService();
		
		@Override
		protected void onPreExecute() {
	
			super.onPreExecute();
			mProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(Void... params) {
			
			if(CheckNetwork.isConnectingToInternet(FreeStudyTopResourceSelectActivity.this)){
				mListViewData = mDb.getTestTopType(-1);
				
				if(mListViewData.size() == 0){//����û�����������web����
					SoapObject result = web.CallWebService("getTestType", null);
					
					if(result != null){
						mListViewData = XMLParser.parseTestType(result);
						
						//����Դ������ӽ��������ݿ�
						if(DownloadResourceType.download(mDb)){
							return SUCCESS;
						}else{
							return FAIL;
						}
						
					}else{
						return FAIL;
					}
				}else{
					return SUCCESS;
				}
				
			}else{
				mListViewData = mDb.getTestTopType(-1);
				
				if(mListViewData.size() == 0){
					return NO_LOCAL_RES;
				}else{
					return SUCCESS;
				}
				
			}			
		
		}

		@Override
		protected void onPostExecute(Integer result) {	
			
			mProgressBar.setVisibility(View.GONE);
			
			switch (result) {
			case SUCCESS:
				
				if(mListViewAdapter == null){
					mListViewAdapter = new CommonListViewAdapter<TestType>(mListViewData,
							FreeStudyTopResourceSelectActivity.this,
							R.layout.layout_free_study_resource_select) {

						@Override
						protected void inflate(
								CommonListViewAdapter<TestType>.ViewHolder viewHolder,
								TestType item, int position) {
							
							CheckBox checkBox = (CheckBox) viewHolder.getView(R.id.checkBox1);
							
							checkBox.setText(item.getTestTypeName());
							
							if(mSelectedResource.get(position, -1) == -1){//����û�б�ѡ��
								checkBox.setChecked(false);
							}else{
								checkBox.setChecked(true);
							}
							
						}
					};
					
					mListView.setAdapter(mListViewAdapter);
				}else{
					mListViewAdapter.notifyDataSetChanged();
				}
				
				break;

			case FAIL:
				Toast.makeText(FreeStudyTopResourceSelectActivity.this,R.string.tip_connection_timeout
						, Toast.LENGTH_SHORT).show();
				break;
			case NO_LOCAL_RES:
				Toast.makeText(FreeStudyTopResourceSelectActivity.this
						, R.string.no_local_resource, Toast.LENGTH_SHORT).show();
				break;
			}
			
			
		}
		
	}
	
	class UpdateResourceTypeThread extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected void onPreExecute() {
			mProgressBar.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			
			if(DownloadResourceType.download(mDb)){
				mListViewData.clear();
				mListViewData.addAll(mDb.getTestTopType(-1)) ;
				return true;
			}
			
			return false;

		}

		

		@Override
		protected void onPostExecute(Boolean result) {
			
			mProgressBar.setVisibility(View.GONE);
			
			if(result){
				mListViewAdapter.notifyDataSetChanged();
				Toast.makeText(FreeStudyTopResourceSelectActivity.this
						, R.string.update_success, Toast.LENGTH_SHORT).show();
			}else{
				ShowToastUtil.showConnectionTimeOutToast(FreeStudyTopResourceSelectActivity.this);
			}
			
		}
		
	}

}
