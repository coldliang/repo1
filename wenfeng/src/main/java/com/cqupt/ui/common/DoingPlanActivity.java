package com.cqupt.ui.common;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.db.DBManager;
import com.cqupt.model.PlanChoice;
import com.cqupt.model.Test;
import com.cqupt.util.SoftInputManagerUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DoingPlanActivity extends IDoingPlanActivity {
	
	
	
	@Override
	protected void loadData() {
		// TODO Auto-generated method stub
		super.loadData();
		mBottomRightButton.setText(R.string.save_answers);
	}
	
	@Override
	protected void makeQuestionListView() {
		super.makeQuestionListView();
		
		if(questionList != null && questionList.size() == 0){		
			mBottomRightButton.setVisibility(View.GONE);
		}else{			
			mBottomRightButton.setVisibility(View.VISIBLE);
		}
		
	}
	
	@Override
	protected boolean isTestFinished(int id, int testID, String userID) {
		return mDb.checkPlanTestFinished(id, testID);
	}

	@Override
	protected ArrayList<Test> getTestList() {
		return mDb.getTestListByPlanID(ID);
	}

	@Override
	protected String getAnswer(int testItemID) {
		return mDb.getPlanAnswer(testItemID, ID);
	}
	
	
	
	

	@Override
	protected void onBottomRightButtonClick() {
		
		progressBar.setVisibility(View.VISIBLE);//��ʼ��ʾ������ ��ʾ�û����ڱ�����ҵ
		DBManager db = new DBManager(helper.getConnection());
		int type = 0;//��¼���͵ı���,����ȷ������𰸵ķ�ʽ
		int savedCount = 0;//��¼�Ѿ��������Ŀ����
		
		//��ʼ���ύ��ʱ��
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String time = format.format(date);
		
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
						PlanChoice pc = new PlanChoice();
						pc.setAnswer((String)radio.getTag());
						pc.setItemID(Integer.parseInt
								(questionList.get(i-1).get("itemID")));
						pc.setSubmitTime(time);
						pc.setPlanID(ID);
						db.addPlanChoice(pc);
						
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
			
					PlanChoice pc = new PlanChoice();
					pc.setAnswer(buffer.toString());
					pc.setItemID(Integer.parseInt
							(questionList.get(i-1).get("itemID")));
					pc.setSubmitTime(time);
					pc.setPlanID(ID);
					db.addPlanChoice(pc);
					
					savedCount++;
				}
				break;
			case 2:
				TextView textView = (TextView)layout.getChildAt(1);
				String answer = textView.getText().toString();
				if(!answer.trim().equals("")){
		
					PlanChoice pc = new PlanChoice();
					pc.setAnswer(answer);
					pc.setItemID(Integer.parseInt
							(questionList.get(i-1).get("itemID")));
					pc.setSubmitTime(time);
					pc.setPlanID(ID);
					db.addPlanChoice(pc);
					
					savedCount++;
				}
			}
		}
		progressBar.setVisibility(View.INVISIBLE);
		
		//��������Ŀ����Ҫ���������
		SoftInputManagerUtil.hideSoftInput(this);
		
		Toast.makeText(DoingPlanActivity.this,
				getResources().getString(R.string.save_success) 
				+ "(" + savedCount + "|"+ (svCenter.getChildCount()-1) + ")"
				,Toast.LENGTH_LONG).show();
		
	}

	

}
