package com.cqupt.ui.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.Plan;
import com.cqupt.model.PlanChoice;
import com.cqupt.model.PlanTestArrange;
import com.cqupt.model.Test;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.DownloadTest;
import com.cqupt.util.DownloadTest.OnProcessChangeListener;
import com.cqupt.util.SoftInputManagerUtil;
import com.cqupt.util.SyncPlanAsyncTask;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FreeStudyDoingPlanActivity extends IDoingPlanActivity {
	
	public static final int FREE_STUDY_SAVE_AS_PLAN = 2;
	
	/**
	 * List.toString():[1,2,3...]
	 */
	private String mTestIds;
	private SparseArray<String> mAnswerList = new SparseArray<String>();//��ʱ��¼�û��Ĵ������
	private Set<Integer> mFinishedTestIds = new TreeSet<Integer>();//��¼�Ѿ�������ϵ�testID
	
	//------------------��д����----------------------------
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//�������̷߳�������	
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.permitNetwork().build());
		super.onCreate(savedInstanceState);
	}
	@Override
	protected void loadData() {
		super.loadData();
		mTestIds = getIntent().getStringExtra("testIds");
	}
	
	@Override
	protected boolean isTestFinished(int id, int testID, String userID) {
		
		return mFinishedTestIds.contains(testID);
		
	}
	
	@Override
	protected void pageMove(int from, int to) {
		super.pageMove(from, to);
		saveMyAnswers(from);
	}
	
	@Override
	protected String getAnswer(int testItemID) {
		
		if(mAnswerList.size() == 0){
			return "";
		}
		
		return mAnswerList.get(testItemID,"");
	}

	@Override
	protected List<Test> getTestList() {
		
		if(mTestIds == null || mTestIds.equals("")){
			return new ArrayList<Test>();
		}
		
		if(CheckNetwork.isConnectingToInternet(this)){
			HashMap<String, String> p = new HashMap<String, String>();
			
			p.put("testIDs",mTestIds.toString().replace("[", "").replace("]", "").replace(" ", "").trim());
			SoapObject result = mWeb.CallWebService("getTestListByTestIDs", p);
			
			if(result != null){
				final List<Test> webTestList = XMLParser.parseTestList(result);
				
				//��̨���±�����⣬��������������ʱʹ��
				new Thread(new Runnable() {
					
					@Override
					public void run() {
					
						new DownloadTest(mDb, null).downloadTest(webTestList);
									
					}
				}).start();
				
				return webTestList;
				
			}
			
			return new ArrayList<Test>();
		}else{
			return mDb.getTestListByTestIDs(mTestIds.replace("[", "(").replace("]", ")"));
		}
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == FREE_STUDY_SAVE_AS_PLAN && resultCode == Activity.RESULT_OK){
			
			//��¼�ɹ�
			if(data.getBooleanExtra("isLogin", false)){
				
				saveAsPlan();
				new DownloadTestThread().execute();
				
			}else{
				Toast.makeText(this, R.string.save_fail, Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	//---------------�Զ��巽��---------------------
	
	//����ǰ�����Ϊ�ҵļƻ��б�
	private void saveAsPlan(){
		
		//��Ϊ�ƻ�֮ǰ�ȱ��浱ǰҳ�Ĵ�
		saveMyAnswers(currentPage);
		
		MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(this);
		final DBManager db = new DBManager(helper.getConnection());
		//��Ӽƻ�
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmm");
		String title = "������ϰ-" + format.format(date);
		
		format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String startDate = format.format(date);
		
		String endDate = "2100/01/01 00:00:00";
		final int planID = db.addPlan(title, ((MyApplication)getApplication()).getUserID(), startDate, endDate);
		
		if(planID != -1){
			//��Ӽƻ���ҵ����
			String[] testIDs = mTestIds.replace("[","").replace("]", "").replace(" ", "").split(",");
			for(int i = 0; i < testIDs.length;i++){
				db.addPlanTestArrange(planID, Integer.parseInt(testIDs[i]));
			}
			
			//��Ӵ�
			for(int i = 0; i < mAnswerList.size(); i++){
				PlanChoice pc = new PlanChoice();
				pc.setPlanID(planID);
				pc.setItemID(mAnswerList.keyAt(i));
				pc.setAnswer(mAnswerList.valueAt(i));
				pc.setSubmitTime(startDate);
				
				db.addPlanChoice(pc);
			}
			
			//�����ؼƻ��б��ϴ�����������
	
			List<Plan> planList = db.getPlanListForWebSync(((MyApplication)getApplication())
					.getUserID(),planID);
			List<PlanTestArrange> testArrangeList = db.getPlanTestArrange(planID);
			List<PlanChoice> pc = db.getPlanChoiceForWebSync(planID);
			
			new SyncPlanAsyncTask.SyncFromLocalToWebThread(planList,testArrangeList,pc,
					new SyncPlanAsyncTask.OnProcessChangeListener() {
						
						@Override
						public void onTaskStart() {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onTaskFinished() {
							// TODO Auto-generated method stub
							
						}
						
						@Override
						public void onTaskFailed() {
							// TODO Auto-generated method stub
							
						}
					},((MyApplication)getApplication()).getUserID(),1).execute();
			
			
		}
		
	}
	
	/**
	 * �洢��ǰ�Ĵ���״��
	 * @param currentPage ��ʾ �ƶ�֮ǰ��ҳ��
	 */
	private void saveMyAnswers(int currentPage){
		
		progressBar.setVisibility(View.VISIBLE);//��ʼ��ʾ������ ��ʾ�û����ڱ�����ҵ

		int type = 0;//��¼���͵ı���,����ȷ������𰸵ķ�ʽ
		int savedCount = 0;//��¼�Ѿ����˵���Ŀ��
		
		//����svCenter�����ӿؼ�(��������Ŀ�б�),��¼�û��Ĵ�
		for(int i = 1;i < svCenter.getChildCount();i++){
			LinearLayout layout = (LinearLayout)svCenter.getChildAt(i);
			type = (Integer)layout.getTag();
			switch(type){
			case 0:
				RadioGroup group = (RadioGroup)layout.getChildAt(1); 
				for(int j = 0;j < group.getChildCount();j++){
					RadioButton radio = (RadioButton)group.getChildAt(j);
					if(radio.isChecked()){
						mAnswerList.append(Integer.parseInt
								(questionList.get(i-1).get("itemID")), (String)radio.getTag());
						savedCount++;
						break;
					}
				}
				break;
			case 1:
				StringBuffer buffer = new StringBuffer();
				for(int k = 1;k < layout.getChildCount()-1;k++){
					CheckBox box = (CheckBox)layout.getChildAt(k);
					if(box.isChecked()){
						buffer.append((String)box.getTag()+",");
					}
				}
				if(!buffer.toString().equals("")){
					mAnswerList.append(Integer.parseInt
							(questionList.get(i-1).get("itemID")), buffer.toString());
					savedCount++;
				}
				break;
			case 2:
				TextView textView = (TextView)layout.getChildAt(1);
				String answer = textView.getText().toString();
				if(!answer.trim().equals("")){
					mAnswerList.append(Integer.parseInt
							(questionList.get(i-1).get("itemID")), answer);	
					savedCount++;
				}
			}
		}
		progressBar.setVisibility(View.INVISIBLE);
		
		//��������Ŀ����Ҫ���������
		SoftInputManagerUtil.hideSoftInput(this);
		
		//��¼�Ѿ�ȫ���������ĿtestID
		if(savedCount == questionList.size()){
			mFinishedTestIds.add(testList.get(currentPage - 1).getTestID());
		}else{
			mFinishedTestIds.remove((Object)testList.get(currentPage - 1).getTestID());
		}
		
	}
	
	//-------------------------�ڲ���-------------------
	
	//����Ŀ��Ϣ��web�˵��뵽�������ݿ�
	class DownloadTestThread extends AsyncTask<Void, Integer, Integer>{
		
		private static final int SUCCESS = 1;
		private static final int FAIL = -1;
		
		Dialog log;
		
		@Override
		protected void onPreExecute() {
			
			log = new AlertDialog.Builder(FreeStudyDoingPlanActivity.this)
			.setTitle(R.string.tip_free_study_save_as_plan)
			.setCancelable(false)
			.setMessage("")
			.create();
			
			log.setCancelable(false);
			log.setCanceledOnTouchOutside(false);
			log.show();
			
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			switch (values[0]) {
			case DownloadTest.IMPORT_TEST:((AlertDialog)log).setMessage(
					getResources().getString(R.string.import_test));break;
			case DownloadTest.DOWNLOAD_TEST_ITEM:((AlertDialog)log).setMessage(
					getResources().getString(R.string.download_test_item));break;
			case DownloadTest.IMPORT_TEST_ITEM:((AlertDialog)log).setMessage(
					getResources().getString(R.string.import_test_item));break;
			case DownloadTest.DOWNLOAD_OPTION:((AlertDialog)log).setMessage(
					getResources().getString(R.string.download_test_item_option));break;
			case DownloadTest.IMPORT_OPTION:((AlertDialog)log).setMessage(
					getResources().getString(R.string.import_test_item_option));break;
			}
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(FreeStudyDoingPlanActivity.this);
			DBManager db = new DBManager(helper.getConnection());
			DownloadTest downloadTest = new DownloadTest(db);
			downloadTest.setListener(new OnProcessChangeListener() {
				
				@Override
				public void onProcessChange(int process) {
					
					publishProgress(process);
					
				}
			});
			
			return downloadTest.downloadTest(testList) == DownloadTest.SUCCESS ? SUCCESS : FAIL;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			
			log.dismiss();
			if(result == SUCCESS){
				Intent it = new Intent(FreeStudyDoingPlanActivity.this,PlanActivity.class);
				startActivity(it);
				finish();
			}else{
				Toast.makeText(FreeStudyDoingPlanActivity.this, R.string.save_fail, Toast.LENGTH_SHORT).show();
			}
			
		}
	}

	@Override
	protected void onBottomRightButtonClick() {
		
		Intent it = null;
		if(((MyApplication)getApplication()).getUserID() != null){//�ѵ�¼
			saveAsPlan();
			new DownloadTestThread().execute();
		}else{
			it = new Intent(FreeStudyDoingPlanActivity.this,LoginActivity.class);
			it.setFlags(LoginActivity.ACTIVITY_TYPE_FREE_STUDY);
			startActivityForResult(it, FREE_STUDY_SAVE_AS_PLAN);
		}
		
	}

}
