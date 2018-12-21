package com.cqupt.ui.common;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.db.DBManager;
import com.cqupt.model.Plan;
import com.cqupt.model.PlanChoice;
import com.cqupt.model.PlanTestArrange;
import com.cqupt.ui.student.IStudentMainActivity;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.DateTimePickDialogUtil;
import com.cqupt.util.DeadlineCheck;
import com.cqupt.util.SyncPlanAsyncTask;
import com.cqupt.view.RoundProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PlanActivity extends IStudentMainActivity {

	private Button mAddPlanButton;
	private TextView mTitleView;
	private TextView mSyncView;
	private ProgressBar mProgressBar;
	
	private DBManager mDb = new DBManager(helper.getConnection());
	private List<Integer> mSelectedPlans;
	private boolean mIsMultiChoiceMode = false;
	
	//------------------------��д����---------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plan_list);
		
		findView();
		loadData();
		setListener();
		
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		switch (v.getId()) {
		case R.id.addButton : 
			
			if(mIsMultiChoiceMode){
				deletePlans();
			}else{
				showDialog();
			}
			
			break;
		case R.id.userIdView :
			
			if(mIsMultiChoiceMode){
				clearMultiChoiceModeView();
			}
			break;
			
		case R.id.sync : 
			
			if(CheckNetwork.isConnectingToInternet(this)){
				showSyncPlanDialog();
			}else{
				Toast.makeText(this, R.string.tip_network_unavailable
						, Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	@Override
	protected void setListener() {
		
		super.setListener();
		mAddPlanButton.setOnClickListener(this);
		userIdView.setOnClickListener(this);
		mSyncView.setOnClickListener(this);
		
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				if(!mIsMultiChoiceMode){
					showMultiChoiceModeView();
				}
				
				return true;
			}
		});	
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				TextView idView = (TextView) view.findViewById(R.id.planID);
				Integer planID = Integer.parseInt(idView.getText().toString());
				
				if(mIsMultiChoiceMode){
					
					CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
					
					if(checkBox.isChecked()){
						checkBox.setChecked(false);
						mSelectedPlans.remove(planID);
					}else{
						checkBox.setChecked(true);
						mSelectedPlans.add(planID);
					}
					
					if(mSelectedPlans.size() != 0){
						mAddPlanButton.setEnabled(true);
						mTitleView.setText(String.valueOf(mSelectedPlans.size()));
					}else{
						mAddPlanButton.setEnabled(false);
						mTitleView.setText("0");
					}
					
				}else{
					//��¼��������ʱ��
					SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					String clickTime = format.format(new Date());
					mDb.setRecentPlan(planID, clickTime);
					
					Intent it = new Intent(PlanActivity.this,PlanDetailActivity.class);
					it.putExtra("planID", planID);
					startActivity(it);
				}
				
			}
		});
		
	}

	@Override
	protected void findView() {
		
		super.findView();
		mAddPlanButton = (Button) findViewById(R.id.addButton);
		mTitleView = (TextView) findViewById(R.id.title);
		mSyncView = (TextView) findViewById(R.id.sync);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		
	}

	@Override
	protected void loadData() {
		super.loadData();
		mSelectedPlans = new ArrayList<Integer>();
	}

	@Override
	protected ArrayList<HashMap<String, String>> getRecentTabData() {		
		return mDb.getRecentPlan(application.getUserID());
	}

	@Override
	protected ArrayList<HashMap<String, String>> getDoingTabData() {		
		return mDb.getPlanList(application.getUserID(), 1);
	}

	@Override
	protected ArrayList<HashMap<String, String>> getDoneTabData() {		
		return mDb.getPlanList(application.getUserID(), 2);
	}

	@Override
	protected BaseAdapter getAdapter() {
		return new TaskListAdapter(this, data);
	}
	
	@Override
	public void onBackPressed() {
		
		if(mIsMultiChoiceMode){
			clearMultiChoiceModeView();
		}else{
			super.onBackPressed();
		}
				
	}
	
	@Override
	protected void onSearchBarClick() {
		
		Intent it = new Intent(this,SearchActivity.class);
		it.putExtra("type", SearchActivity.TYPE_PLAN);
		startActivity(it);
		
	}
	
	//----------------------�Զ��巽��---------------------------
	
	private void showSyncPlanDialog(){
		new AlertDialog.Builder(this)
		.setItems(R.array.plan_sync, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if(which == 0){//�ӿͻ���ͬ����web��
					List<Plan> planList = mDb.getPlanListForWebSync(application.getUserID());
					List<PlanTestArrange> planTestArrangeList = mDb.getPlanTestArrange(application.getUserID());
					List<PlanChoice> planChoiceList = mDb.getPlanChoiceForWebSync(application.getUserID());
					
					new SyncPlanAsyncTask.SyncFromLocalToWebThread(planList,planTestArrangeList,planChoiceList,
							new SyncPlanAsyncTask.OnProcessChangeListener() {
								
								@Override
								public void onTaskStart() {
									
									mProgressBar.setVisibility(View.VISIBLE);
									
								}
								
								@Override
								public void onTaskFinished() {
									
									mProgressBar.setVisibility(View.GONE);
									
									Toast.makeText(PlanActivity.this, R.string.tip_update_success
											, Toast.LENGTH_SHORT).show();
									
									//��ǰ���ڽ�����tab���������ҳ��
									if(selectedTabIndex == 2){
										data.clear();
										data.addAll(getDoingTabData());
										adapter.notifyDataSetChanged();
									}
									
								}
								
								@Override
								public void onTaskFailed() {
									
									mProgressBar.setVisibility(View.GONE);
									Toast.makeText(PlanActivity.this, R.string.tip_connection_timeout
											, Toast.LENGTH_SHORT).show();
									
								}
							},application.getUserID(),0).execute();
				}else {//��web��ͬ�����ͻ���
					new SyncPlanAsyncTask.SyncFromWebToLocalThread(mDb, application.getUserID(),-1,
							new SyncPlanAsyncTask.OnProcessChangeListener() {
								
								@Override
								public void onTaskStart() {
									
									mProgressBar.setVisibility(View.VISIBLE);
									
								}
								
								@Override
								public void onTaskFinished() {
									
									mProgressBar.setVisibility(View.GONE);
									Toast.makeText(PlanActivity.this, R.string.tip_update_success
											, Toast.LENGTH_SHORT).show();
									
									data.clear();
									switch (selectedTabIndex) {
									case 1:
										data.addAll(getRecentTabData());
										break;

									case 2:
										data.addAll(getDoingTabData());
										break;
									case 3:
										data.addAll(getDoneTabData());
									}
									
									adapter.notifyDataSetChanged();
								}
								
								@Override
								public void onTaskFailed() {
									
									mProgressBar.setVisibility(View.GONE);
									Toast.makeText(PlanActivity.this, R.string.tip_connection_timeout
											, Toast.LENGTH_SHORT).show();
									
								}
							}).execute();
				}
				
			}
		})
		.create()
		.show();
	}
	
	@SuppressLint("InflateParams")
	private void showDialog(){
		
		View contentView = LayoutInflater.from(this).inflate(R.layout.layout_add_plan, null);
		final EditText titleView = (EditText) contentView.findViewById(R.id.title);
		final TextView sDateView = (TextView) contentView.findViewById(R.id.startDate);
		final TextView eDateView = (TextView) contentView.findViewById(R.id.endDate);
		
		sDateView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
						PlanActivity.this, "");
				dateTimePicKDialog.dateTimePicKDialog(sDateView);
				
			}
		});
		
		eDateView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
						PlanActivity.this, "");
				dateTimePicKDialog.dateTimePicKDialog(eDateView);
				
			}
		});
		
		AlertDialog log = new AlertDialog.Builder(this)
		.setView(contentView)
		.setPositiveButton(R.string.save,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				String title = titleView.getText().toString().trim();
				String sDate = sDateView.getText().toString();
				String eDate = eDateView.getText().toString();
				
				if(TextUtils.isEmpty(title) || TextUtils.isEmpty(sDate)|| TextUtils.isEmpty(eDate)){
					Toast.makeText(PlanActivity.this,
							R.string.empty_input_error,Toast.LENGTH_SHORT).show();
				}else if(eDate.compareTo(sDate) != 1){
					Toast.makeText(PlanActivity.this,
							R.string.tip_enddate_earlier_than_startdate,Toast.LENGTH_SHORT).show();
				}else
				{
					
					int id = mDb.addPlan(title, application.getUserID(), sDate, eDate);
					
					//�����ǰ���ڽ����мƻ�tab����Ҫ����ˢ��ҳ��
					if(selectedTabIndex == 2){
						
						data.add(mDb.getPlan(id));
						adapter.notifyDataSetChanged();
						
					}
					
					Toast.makeText(PlanActivity.this,
							R.string.save_success,Toast.LENGTH_SHORT).show();
					
				}
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create();
		
		log.show();
		
	}
	
	/**
	 * ��ʾ��listview��ѡģʽ�µ���ͼ
	 */
	private void showMultiChoiceModeView(){
		
		mIsMultiChoiceMode = true;
		setTabMoveable(false);//�����ѡģʽ,���ֹ�ƶ�tab
		adapter.notifyDataSetChanged();
		userIdView.setText(R.string.cancel);
		mTitleView.setText("0");
		mAddPlanButton.setText(R.string.delete);
		mAddPlanButton.setEnabled(false);
		
	}
	
	/**
	 * �����listview��ѡģʽ�µ���ͼ
	 */
	private void clearMultiChoiceModeView(){
		
		mIsMultiChoiceMode = false;
		setTabMoveable(true);
		userIdView.setText(mDb.getUserInfo().getUserName());
		mTitleView.setText(R.string.plan_list);
		mAddPlanButton.setText(R.string.add_plans);
		mAddPlanButton.setEnabled(true);
		mSelectedPlans.clear();
		adapter.notifyDataSetChanged();
		
	}
	
	/**
	 * ɾ��ѡ�еļƻ�
	 */
	private void deletePlans(){
		
		mDb.deletePlans(mSelectedPlans);
		
		for(Integer planID : mSelectedPlans){
			mDb.deletePlanTestArrange(planID);
			mDb.deletePlanChoice(planID);
		}
		
		data.removeAll(data);
		
		switch (selectedTabIndex) {
		case 1 :
			data .addAll(getRecentTabData());
			break;
		case 2 :
			data.addAll(getDoingTabData());
			break;
		case 3 :
			data.addAll(getDoneTabData());
			break;
		}
		
		clearMultiChoiceModeView();

		Toast.makeText(this,R.string.delete_task_success,Toast.LENGTH_SHORT).show();
		
	}
	
	//------------------------------�ڲ���-------------------------------
	
	/**
	 *�����б�������
	 */
	class TaskListAdapter extends BaseAdapter{
		
		private ArrayList<HashMap<String,String>> data;
		private LayoutInflater inflater;
			
		public TaskListAdapter(Context context,ArrayList<HashMap<String,String>> data){
			
			this.data = data;
			inflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			
			return position;
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View myView = inflater.inflate(R.layout.layout_planlist, parent,false);
			RoundProgressBar myProgress = (RoundProgressBar)myView
					.findViewById(R.id.myProgress);
			TextView taskIDView = (TextView)myView.findViewById(R.id.planID);
			TextView title = (TextView)myView.findViewById(R.id.title);
			TextView startDate = (TextView)myView.findViewById(R.id.startDate);
			TextView endDate = (TextView)myView.findViewById(R.id.endDate);
			CheckBox checkBox = (CheckBox) myView.findViewById(R.id.checkBox);
			
			if(!mIsMultiChoiceMode){
				checkBox.setVisibility(View.GONE);
			}else{
				if(mSelectedPlans.contains((Object)data.get(position).get("planID"))){
					checkBox.setChecked(true);
				}else{
					checkBox.setChecked(false);
				}
			}
			
			int max = Integer.parseInt(data.get(position).get("max"));
			//����ҵ���������,����ʾ������
			if(max == 0){
				myProgress.setVisibility(View.INVISIBLE);
			}else{
				myProgress.setMax(max);
				myProgress.setProgress(Integer.parseInt(data.get(position).get("process")));			
			}
			
			taskIDView.setText(data.get(position).get("planID"));
			title.setText(data.get(position).get("title"));
			startDate.setText(data.get(position).get("startDate"));
			String sEndDate = data.get(position).get("endDate");
			
			endDate.setText(sEndDate);
			
			//�Ѿ���ֹ����ҵ,�Խ�ֹ���ڱ��
			if(DeadlineCheck.check(sEndDate)){
				endDate.setTextColor(PlanActivity.this.getResources().getColor(R.color.my_red));
			}else{
				endDate.setTextColor(Color.BLACK);
			}
					
			return myView;
		}	
	}	

}
