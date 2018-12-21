package com.cqupt.ui.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.Plan;
import com.cqupt.model.PlanChoice;
import com.cqupt.model.PlanTestArrange;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.DateTimePickDialogUtil;
import com.cqupt.util.DeadlineCheck;
import com.cqupt.util.SyncPlanAsyncTask;

import java.util.HashMap;
import java.util.List;

public class PlanDetailActivity extends Activity implements OnClickListener{
	
	private TextView mReturnView;
	private TextView mEditView;
	private TextView mTitleView;
	private TextView mStartDateView;
	private TextView mEndDateView;
	private TextView mRecentTimeView;
	private TextView mSumCountView;
	private TextView mFinishedCountView;
	private ImageView mAddTestButton;
	private ImageView mSyncButton;
	private Button mStartAnswerButton;
	private ProgressBar mProgressBar;
	
	private DBManager mDb;
	private int mPlanID;
	private MyApplication mApplication;
	
	//------------------��д����----------------------
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plan_detail);
		
		findView();
		loadData();
		setListener();
		
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.returnView:onBackPressed();break;
		case R.id.edit : showEditDialog();break;
		case R.id.addTest:
			
			if(CheckNetwork.isConnectingToInternet(this)){
				Intent it = new Intent(this,AddTestToPlanActivity.class);
				it.putExtra("planID", mPlanID);
				startActivity(it);
			}else{
				Toast.makeText(this, R.string.tip_network_unavailable
						, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.startButton:
			
			Intent it = null;
			//��ʼ����
			if(mStartAnswerButton.getText().toString().equals(
					getResources().getString(R.string.start_the_answer)))
			{
				it = new Intent(this,DoingPlanActivity.class);
				it.putExtra("ID", mPlanID);
				startActivity(it);
			}else{//�������
				it = new Intent(this,DonePlanTestActivity.class);
				it.putExtra("ID", mPlanID);
				startActivity(it);
			}
			break;
		case R.id.sync: 
			
			if(CheckNetwork.isConnectingToInternet(this)){
				showSyncPlanDialog();
			}else{
				Toast.makeText(PlanDetailActivity.this, R.string.tip_network_unavailable
						, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		
	}
	
	@Override
	protected void onRestart() {
		loadUIData();
		super.onRestart();
	}
	
	//----------------------�Զ��巽��-----------------------
	
	private void showSyncPlanDialog(){
		new AlertDialog.Builder(this)
		.setItems(R.array.plan_sync, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if(which == 0){//�ӿͻ���ͬ����web��
					List<Plan> planList = mDb.getPlanListForWebSync(mApplication.getUserID(),mPlanID);
					List<PlanTestArrange> testArrangeList = mDb.getPlanTestArrange(mPlanID);
					List<PlanChoice> pc = mDb.getPlanChoiceForWebSync(mPlanID);
					
					new SyncPlanAsyncTask.SyncFromLocalToWebThread(planList,testArrangeList,pc,
							new SyncPlanAsyncTask.OnProcessChangeListener() {
								
								@Override
								public void onTaskStart() {
									
									mProgressBar.setVisibility(View.VISIBLE);
									
								}
								
								@Override
								public void onTaskFinished() {
									
									mProgressBar.setVisibility(View.GONE);
									loadUIData();
									Toast.makeText(PlanDetailActivity.this, R.string.tip_update_success
											, Toast.LENGTH_SHORT).show();
									
								}
								
								@Override
								public void onTaskFailed() {
									
									mProgressBar.setVisibility(View.GONE);
									Toast.makeText(PlanDetailActivity.this, R.string.tip_connection_timeout
											, Toast.LENGTH_SHORT).show();
									
								}
							},mApplication.getUserID(),1).execute();
				}else {//��web��ͬ�����ͻ���
					new SyncPlanAsyncTask.SyncFromWebToLocalThread(mDb, mApplication.getUserID(),mPlanID,
							new SyncPlanAsyncTask.OnProcessChangeListener() {
								
								@Override
								public void onTaskStart() {
									
									mProgressBar.setVisibility(View.VISIBLE);
									
								}
								
								@Override
								public void onTaskFinished() {
									
									mProgressBar.setVisibility(View.GONE);
									loadUIData();
									Toast.makeText(PlanDetailActivity.this, R.string.tip_update_success
											, Toast.LENGTH_SHORT).show();
									
								}
								
								@Override
								public void onTaskFailed() {
									
									mProgressBar.setVisibility(View.GONE);
									Toast.makeText(PlanDetailActivity.this, R.string.tip_connection_timeout
											, Toast.LENGTH_SHORT).show();
									
								}
							}).execute();
				}
				
			}
		})
		.create()
		.show();
	}

	private void findView(){
		
		mReturnView = (TextView) findViewById(R.id.returnView);
		mEditView = (TextView) findViewById(R.id.edit);
		mTitleView = (TextView) findViewById(R.id.title);
		mStartDateView = (TextView) findViewById(R.id.sDate);
		mEndDateView = (TextView) findViewById(R.id.eDate);
		mRecentTimeView = (TextView) findViewById(R.id.recentTime);
		mSumCountView = (TextView) findViewById(R.id.sumTestCount);
		mFinishedCountView = (TextView) findViewById(R.id.finishedTestCount);
		mAddTestButton = (ImageView) findViewById(R.id.addTest);
		mStartAnswerButton = (Button) findViewById(R.id.startButton);
		mSyncButton = (ImageView) findViewById(R.id.sync);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		
	}
	
	private void loadData(){
		
		mDb = new DBManager(MySQLiteOpenHelper.getInstance(this).getConnection());
		mPlanID = getIntent().getIntExtra("planID", -1);
		mApplication = (MyApplication) getApplication();
		
		loadUIData();
		
	}
	
	private void setListener(){
		
		mReturnView.setOnClickListener(this);
		mEditView.setOnClickListener(this);
		mAddTestButton.setOnClickListener(this);
		mStartAnswerButton.setOnClickListener(this);
		mSyncButton.setOnClickListener(this);
		
	}
	
	private void loadUIData(){
		HashMap<String, String> plan = mDb.getPlan(mPlanID);
		
		if(plan == null){
			finish();
		}else{
			mTitleView.setText(plan.get("title"));
			mStartDateView.setText(plan.get("startDate"));
			
			String endDate = plan.get("endDate");
			boolean isDeadline = DeadlineCheck.check(endDate);
			
			if(isDeadline){
				mEndDateView.setTextColor(getResources().getColor(R.color.my_red));
			}else{
				mEndDateView.setTextColor(getResources().getColor(android.R.color.black));
			}
			
			//��ֹ��plan�Ͳ����������ҵ��
			if(isDeadline){
				mAddTestButton.setVisibility(View.GONE);
				mStartAnswerButton.setText(R.string.answers);
			}else{
				mAddTestButton.setVisibility(View.VISIBLE);
				mStartAnswerButton.setText(R.string.start_the_answer);
			}
			
			mEndDateView.setText(endDate);
			mSumCountView.setText(plan.get("max"));
			
			//���������Ĳ���ʾ���ⰴť
			if(plan.get("max").equals("0")){
				mStartAnswerButton.setVisibility(View.GONE);
			}else{
				mStartAnswerButton.setVisibility(View.VISIBLE);
			}
			
			mFinishedCountView.setText(plan.get("process"));
			mRecentTimeView.setText(mDb.getRencentPlanChoiceTime(
					Integer.parseInt(plan.get("planID"))));
		}
		
	}
	
	@SuppressLint("InflateParams")
	private void showEditDialog(){
		
		View contentView = LayoutInflater.from(this).inflate(R.layout.layout_add_plan, null);
		final EditText titleView = (EditText) contentView.findViewById(R.id.title);
		final TextView sDateView = (TextView) contentView.findViewById(R.id.startDate);
		final TextView eDateView = (TextView) contentView.findViewById(R.id.endDate);
		
		HashMap<String, String> plan = mDb.getPlan(mPlanID);
		
		titleView.setText(plan.get("title"));
		sDateView.setText(plan.get("startDate"));
		eDateView.setText(plan.get("endDate"));
		
		sDateView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
						PlanDetailActivity.this, "");
				dateTimePicKDialog.dateTimePicKDialog(sDateView);
				
			}
		});
		
		eDateView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				DateTimePickDialogUtil dateTimePicKDialog = new DateTimePickDialogUtil(
						PlanDetailActivity.this, "");
				dateTimePicKDialog.dateTimePicKDialog(eDateView);
				
			}
		});
		
		new AlertDialog.Builder(this).setView(contentView)
		.setPositiveButton(R.string.save,new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				String title = titleView.getText().toString().trim();
				String sDate = sDateView.getText().toString();
				String eDate = eDateView.getText().toString();
				
				if(TextUtils.isEmpty(title) || TextUtils.isEmpty(sDate)
						|| TextUtils.isEmpty(eDate)){
					Toast.makeText(PlanDetailActivity.this,
							R.string.empty_input_error,Toast.LENGTH_SHORT).show();
				}else if(eDate.compareTo(sDate) != 1){
					Toast.makeText(PlanDetailActivity.this,
							R.string.tip_enddate_earlier_than_startdate,Toast.LENGTH_SHORT).show();
				}else{
					
					mDb.updatePlan(mPlanID, title, sDate, eDate);
					loadUIData();
					
					Toast.makeText(PlanDetailActivity.this,
							R.string.save_success,Toast.LENGTH_SHORT).show();
					
				}
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create()
		.show();
	}
	
	//---------------------�ڲ���----------------------
		

}
