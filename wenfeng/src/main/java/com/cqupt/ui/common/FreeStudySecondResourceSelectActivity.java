package com.cqupt.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.TestSubType;
import com.cqupt.net.WebService;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.DownloadResourceType;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FreeStudySecondResourceSelectActivity extends Activity implements OnClickListener{
	
	private final int MAX_TEST_COUNT = 50;
	
	private TextView mReturnView;
	private ListView mListView;
	private Button mButton;
	private MyListViewAdapter mListViewAdapter;
	private ProgressBar mProgressBar;
	
	private List<TestSubType> mListViewData = new ArrayList<TestSubType>();
	private SparseIntArray mSelectedResource = new SparseIntArray();
	private String mSelectedTestTypeIDs;
	private List<Integer> mSelectedTestIDs = new ArrayList<Integer>();
	private int mCurrentSelectedCount;//��¼��ʼ�϶�seekbarʱ�ļ�¼��
	private int mSelectedResourceCount;//��¼ѡ����Դ���ܸ���
	private boolean mNeedRollback;//�Ƿ���Ҫ�ع�seekbar
	private DBManager mDb;
	
	
	//---------------���·���-------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_free_study_second_resource_select);
		
		findView();
		loadData();
		setListener();
	
		
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.returnView : onBackPressed();break;
		case R.id.button : 
			
			if(CheckNetwork.isConnectingToInternet(this)){
				new GetTestListThread().execute();
			}else{
				
				StringBuilder ids = new StringBuilder("");
				StringBuilder counts = new StringBuilder("");

				int emptyAddCount = 0;
				
				for(int i = 0; i < mSelectedResource.size() ; i++){
					
					int key = mSelectedResource.keyAt(i);	
					int count = mSelectedResource.get(key);
					
					if(count == 0){
						emptyAddCount++;
					}else{
						ids.append( mListViewData.get(key).getTestSubTypeID() + ",");
						counts.append( count + ",");	
					}
					
				}
				
				if(emptyAddCount != mSelectedResource.size()){
					Intent it = new Intent(FreeStudySecondResourceSelectActivity.this
							,FreeStudyDoingPlanActivity.class);
					it.putExtra("testIds", mDb.getTestIDsAtRandom(ids.toString(), counts.toString()).toString());
					startActivity(it);
					finish();
				}else{
					Toast.makeText(FreeStudySecondResourceSelectActivity.this
							,R.string.empty_input_error, Toast.LENGTH_SHORT).show();
				}
				
			}
			
			break;
		}
		
	}
	
	//-------------�Զ��巽��-------------------
	
	private void findView(){
		
		mReturnView = (TextView) findViewById(R.id.returnView);
		mListView = (ListView) findViewById(R.id.listView);
		mButton = (Button) findViewById(R.id.button);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		
	}
	
	private void loadData(){
		
		mDb = new DBManager(MySQLiteOpenHelper.getInstance(this).getConnection());
		mSelectedTestTypeIDs = getIntent().getStringExtra("testTypeIDs");
		
		if(mSelectedTestTypeIDs == null){
			mSelectedTestTypeIDs = "";
		}
		
		if(!mSelectedResource.equals("")){
			new GetListViewDataThread().execute();
		}
		
	}
	
	private void setListener(){
		
		mReturnView.setOnClickListener(this);
		mButton.setOnClickListener(this);
		
	}
	
	private void showOverMaxToast(){
		
		Toast.makeText(this, R.string.tip_over_max_resource_count, Toast.LENGTH_SHORT).show();
		
	}
	
	//------------------�ڲ���-----------------------------
	
	class GetListViewDataThread extends AsyncTask<Void, Void, Integer>{
		
		private final static int SUCCESS = 1;
		private final static int FAIL = 0;
		
		private WebService web = new WebService();
		
		@Override
		protected Integer doInBackground(Void... params) {
			
			if(CheckNetwork.isConnectingToInternet(FreeStudySecondResourceSelectActivity.this)){
				
				for(String id : mSelectedTestTypeIDs.split(",")){
					mListViewData.addAll(mDb.getTestType(Integer.parseInt(id)));
				}
				
				if(mListViewData.size() != 0){
					return SUCCESS;
				}else{//û�б�����Դ�������������Դ��ͬʱ�����ݵ��뱾��
					
					HashMap<String, String> p = new HashMap<String, String>();
					p.put("resTypeIDs", mSelectedTestTypeIDs);
				
					SoapObject result = web.CallWebService("getPlanArrangeTableData2", null);
					
					if(result != null){
						
						mListViewData = XMLParser.parseTestSubType(result);
						
						//���������ȫ������Դ���ͣ����뱾�����ݿ�
						if(DownloadResourceType.download(mDb)){
							return SUCCESS;
						}else{
							return FAIL;
						}
						
						
					}else{
						return FAIL;
					}
				}
				
			}else{//û������
				
				//�б�����Դ����ֱ�Ӽ��ر�����Դ
				if(mDb.isLocalResourceAvailable()){
					
					for(String id : mSelectedTestTypeIDs.split(",")){
						mListViewData.addAll(mDb.getTestTypeWithoutCount(Integer.parseInt(id)));
					}

				}
				
				return SUCCESS;
				
			}
			
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == SUCCESS){
				
				if(mListViewAdapter != null){
					mListViewAdapter.notifyDataSetChanged();
				}else{
					mListViewAdapter = new MyListViewAdapter();
					mListView.setAdapter(mListViewAdapter);
				}
						
			}else{
				ShowToastUtil.showConnectionTimeOutToast(FreeStudySecondResourceSelectActivity.this);
			}
			
		}
		
	} 
	
	private class ViewHolder{
		public TextView nameView;
		public TextView countView;
		public SeekBar seekBar;
	}
	
	
	class MyListViewAdapter extends BaseAdapter{
	
		@Override
		public int getCount() {
			return mListViewData.size();
		}

		@Override
		public Object getItem(int position) {
			return mListViewData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			ViewHolder viewHolder = null;
			
			if(convertView == null){
				convertView = LayoutInflater.from(FreeStudySecondResourceSelectActivity.this)
						.inflate(R.layout.layout_free_study_second_resource_select, parent,false);
				viewHolder = new ViewHolder();
				viewHolder.nameView = (TextView) convertView.findViewById(R.id.name);
				viewHolder.countView = (TextView) convertView.findViewById(R.id.count);
				viewHolder.seekBar = (SeekBar) convertView.findViewById(R.id.seekBar1);
				
				convertView.setTag(viewHolder);
			}else{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			TestSubType item = mListViewData.get(position);
			final int sumCount = Math.min(item.getCount(), 50);
			viewHolder.nameView.setText(item.getTestSubTypeName());
			
			viewHolder.seekBar.setMax(sumCount);
			//viewHolder.countView.setText(String.valueOf(Math.min(item.getCount(), 50)));
			final TextView countView = viewHolder.countView;
		
			int selectedCount = mSelectedResource.get(position);
			if(selectedCount != 0){
				viewHolder.seekBar.setProgress(selectedCount);
				viewHolder.countView.setText(selectedCount+"/" + sumCount);
			}else{
				viewHolder.seekBar.setProgress(0);
				viewHolder.countView.setText("0/" + sumCount);
			}
			
			viewHolder.seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					
					if(!mNeedRollback){
						int process = seekBar.getProgress();
						mSelectedResource.append(position, process);
						mSelectedResourceCount += process - mCurrentSelectedCount;
					}else{
						seekBar.setProgress(mCurrentSelectedCount);
						mNeedRollback = false;
					}
					
					
				}
				
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {					
					
					mCurrentSelectedCount = seekBar.getProgress();	
					
				}
				
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					
					if(fromUser){
						
						if(MAX_TEST_COUNT - mSelectedResourceCount < progress - mCurrentSelectedCount ){
							mNeedRollback = true;
							showOverMaxToast();
						}else{
							countView.setText(progress + "/" + sumCount );
						}
						
					}
					
				}
			});
			
			return convertView;
		}
		
	}
	
	//�������������Ŀid
	class GetTestListThread extends AsyncTask<Void, Void, Integer>{
		
		private final static int SUCCESS = 1;
		private final static int FAIL = 0;
		private final static int TASK_CANCEL = -1;
		
		private WebService web = new WebService();

		@Override
		protected void onPreExecute() {
			mProgressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(Void... params) {
			
			StringBuilder ids = new StringBuilder("");
			StringBuilder counts = new StringBuilder("");

			int emptyAddCount = 0;
			
			for(int i = 0; i < mSelectedResource.size() ; i++){
				
				int key = mSelectedResource.keyAt(i);	
				int count = mSelectedResource.get(key);
				
				if(count == 0){
					emptyAddCount++;
				}else{
					ids.append( mListViewData.get(key).getTestSubTypeID() + ",");
					counts.append( count + ",");	
				}
				
			}
			
			if(emptyAddCount != mSelectedResource.size()){
				HashMap<String, String> p = new HashMap<String, String>();
				p.put("ids", ids.toString());
				p.put("counts", counts.toString());
				
				SoapObject result = web.CallWebService("getTestListAtRandom2", p);
				
				if(result != null){
					mSelectedTestIDs = XMLParser.parseTestListIds(result);
					return SUCCESS;
				}else{
					return FAIL;
				}
			}
			
			return TASK_CANCEL;
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			mProgressBar.setVisibility(View.GONE);
			switch (result) {
			case SUCCESS:
				Intent it = new Intent(FreeStudySecondResourceSelectActivity.this
						,FreeStudyDoingPlanActivity.class);
				it.putExtra("testIds", mSelectedTestIDs.toString());
				startActivity(it);
				finish();
				break;
			case FAIL :
				Toast.makeText(FreeStudySecondResourceSelectActivity.this
					, R.string.tip_network_unavailable, Toast.LENGTH_SHORT).show();
				break;
			case TASK_CANCEL:
				Toast.makeText(FreeStudySecondResourceSelectActivity.this
						,R.string.empty_input_error, Toast.LENGTH_SHORT).show();
				break;
			}	
			
		}
		
	}

}
