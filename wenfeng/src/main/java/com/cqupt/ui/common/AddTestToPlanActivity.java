package com.cqupt.ui.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.PlanArrangeTableData;
import com.cqupt.model.Test;
import com.cqupt.model.TestType;
import com.cqupt.net.WebService;
import com.cqupt.util.DownloadTest;
import com.cqupt.util.DownloadTest.OnAddArrangeRelation;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddTestToPlanActivity extends Activity implements OnClickListener{
	
	private TextView mReturnView;
	private TextView mResetView;
	private Spinner mSpinner;
	private ListView mListView;
	private MyListViewAdapter mListViewAdapter;
	private Button mSubmitButton;
	private ProgressBar mProgressBar;
	
	private DBManager mDb;
	private List<PlanArrangeTableData> mListViewData;
	private List<TestType> mSpinnerData;
	private SparseArray<String> mInputResCount;//��¼�û��������Ҫ��ӵ���Դ��Ŀ
	private int mPlanID;
	private boolean mDataChanged;
	
	//--------------------��д����---------------------
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_test_to_plan);
		
		findView();
		loadData();
		setListener();
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.returnView : onBackPressed();break;
		case R.id.add : 
			
			getWindow().getDecorView().findFocus().clearFocus();
			mSubmitButton.requestFocus();
			new AddTestThread().execute();break;
		case R.id.reset: showResetPlanDialog();break;
		}
		
	}
	
	//-----------------------�Զ��巽��-----------------------
	
	private void findView(){
		
		mReturnView = (TextView) findViewById(R.id.returnView);
		mResetView = (TextView) findViewById(R.id.reset);
		mSpinner = (Spinner) findViewById(R.id.spinner1);
		mListView = (ListView) findViewById(R.id.listView);
		mSubmitButton = (Button) findViewById(R.id.add);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		
	}
	
	private void loadData(){
		
		mPlanID = getIntent().getIntExtra("planID", 0);
		mInputResCount = new SparseArray<String>();
		mDb = new DBManager(MySQLiteOpenHelper.getInstance(this).getConnection());
		new GetSpinnerDataThread().execute();
		
	}
	
	private void setListener(){
		
		mReturnView.setOnClickListener(this);
		mSubmitButton.setOnClickListener(this);
		mResetView.setOnClickListener(this);
		
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				int testTypeID = mSpinnerData.get(position).getTestTypeID();
				//mInputResCount.clear();
				new GetListViewDataThread().execute(testTypeID);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	//���üƻ�
	private void showResetPlanDialog(){
		new AlertDialog.Builder(this).setMessage(R.string.reset_plan_confirm)
		.setPositiveButton(R.string.confirm,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mDb.removePlanTests(mPlanID);
				new GetListViewDataThread().execute(mSpinnerData.get(
						mSpinner.getSelectedItemPosition()).getTestTypeID());
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create()
		.show();
	}
	
	//-------------------------�ڲ���----------------------------
	
	class AddTestThread extends AsyncTask<Void, Void, Integer>{
		
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
			StringBuilder addedIds = new StringBuilder("");
			int emptyAddCount = 0;
			
			for(Integer id : mDb.getPlanTestListIDs(mPlanID)){
				addedIds.append(id.toString() + ",");
			}
			
			for(int i = 0; i < mInputResCount.size() ; i++){
				
				int key = mInputResCount.keyAt(i);
				ids.append( mListViewData.get(key).getId() + ",");
				String count = mInputResCount.valueAt(i);
				
				if(count.equals("")){
					count = "0";
					emptyAddCount++;
				}else{
					
					//�����û��ֶ������0,ȥ�� 000XXX ǰ׺
					if(count.length() != 1){
						count = count.replaceFirst("^0*", "");
					}
					
				}
				
				counts.append( count + ",");		
				
			}
			
			if(emptyAddCount != mInputResCount.size()){
				HashMap<String, String> p = new HashMap<String, String>();
				p.put("ids", ids.toString());
				p.put("counts", counts.toString());
				p.put("addedIds", addedIds.toString());
				
				SoapObject result = web.CallWebService("getTestListAtRandom", p);
				
				if(result != null){
					
					List<Test> testList = XMLParser.parseTestList(result);
					
					new DownloadTest(mDb, new OnAddArrangeRelation() {
						
						@Override
						public void addArrangeRelation(int testID) {
							mDb.addPlanTestArrange(mPlanID, testID);
							
						}
					}).downloadTest(testList);
					
					
					return SUCCESS;
				}else{
					return FAIL;
				}
			}
			
			return TASK_CANCEL;
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			switch (result) {
			case SUCCESS:
				//ˢ��listview
				new GetListViewDataThread().execute(
						mSpinnerData.get(mSpinner.getSelectedItemPosition())
						.getTestTypeID()
						);
				
				Toast.makeText(AddTestToPlanActivity.this
						, R.string.save_success, Toast.LENGTH_SHORT).show();
				break;
			case FAIL :
				Toast.makeText(AddTestToPlanActivity.this
					, R.string.tip_network_unavailable, Toast.LENGTH_SHORT).show();
				break;
			case TASK_CANCEL:
				Toast.makeText(AddTestToPlanActivity.this
						,R.string.empty_input_error, Toast.LENGTH_SHORT).show();
				break;
			}
			
			mProgressBar.setVisibility(View.GONE);
		}
		
	}
	
	class GetSpinnerDataThread extends AsyncTask<Void, Void, Integer>{
		
		private final static int SUCCESS = 1;
		private final static int FAIL = 0;
		
		private WebService web = new WebService();

		@Override
		protected Integer doInBackground(Void... params) {
			
			SoapObject result = web.CallWebService("getTestType", null);
			
			if(result != null){
				mSpinnerData = XMLParser.parseTestType(result);
				return SUCCESS;
			}else{
				return FAIL;
			}
		
		}

		@Override
		protected void onPostExecute(Integer result) {	
			
			if(result == SUCCESS){
				List<String> list = new ArrayList<String>();
				
				for(TestType type : mSpinnerData){
					list.add(type.getTestTypeName());
				}
				
				mSpinner.setAdapter(new ArrayAdapter<String>(AddTestToPlanActivity.this,
						android.R.layout.simple_spinner_dropdown_item,list));
			}else{
				Toast.makeText(AddTestToPlanActivity.this,R.string.tip_network_unavailable
						, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	class GetListViewDataThread extends AsyncTask<Integer, Void, Integer>{
		
		private final static int SUCCESS = 1;
		private final static int FAIL = 0;
		
		private WebService web = new WebService();
		

		@Override
		protected Integer doInBackground(Integer... params) {
			
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("resTypeID", params[0].toString());
			
			SoapObject result = web.CallWebService("getPlanArrangeTableData", p);
			
			if(result != null){
				
				List<PlanArrangeTableData> tempList = mDb.getPlanArrangeTableData(
						XMLParser.parseTestSubTypeWithoutTypeId(result),mPlanID);
				
				if(mListViewData == null){
					mListViewData = tempList;
				}else{
					mListViewData.clear();
					mListViewData.addAll(tempList);
				}
				
				return SUCCESS;
			}else{
				return FAIL;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			
			if(result == SUCCESS){
				
				//�������֮�󣬻����ٵ���һ��edittext��onFocusChanged�������Ⱑ
				//���ñ�־λmDataChanged��֪ͨedittextȡ�����μ�¼����
				mInputResCount.clear();
			
				if(mListViewAdapter == null){
					mListViewAdapter = new MyListViewAdapter();
					mListView.setAdapter(mListViewAdapter);
				}else{
					mListViewAdapter.notifyDataSetChanged();
					mDataChanged = true;
				}
						
			}
			
		}
		
	}
	
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

		@SuppressLint("ViewHolder")
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(AddTestToPlanActivity.this)
					.inflate(R.layout.layout_table_4_item, parent,false);
			
			TextView levelView = (TextView) view.findViewById(R.id.level);
			TextView addedTestView = (TextView) view.findViewById(R.id.addedTest);
			TextView availableTestView = (TextView) view.findViewById(R.id.availableTest);
			final EditText toAddEditText = (EditText) view.findViewById(R.id.testToAdd);
			
			toAddEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					
					
					if(!hasFocus){
											
						if(!mDataChanged){
							mInputResCount.append(position, toAddEditText.getText().toString());
						}else{
							mDataChanged = false;
						}					
						
					}
					
				}
			});
			
			levelView.setText(mListViewData.get(position).getLevel());
			addedTestView.setText(mListViewData.get(position).getAddedTest());
			availableTestView.setText(mListViewData.get(position).getAvailableTest());
			toAddEditText.setText(mInputResCount.get(position,""));
			
			return view;
		}
		
	}

}
