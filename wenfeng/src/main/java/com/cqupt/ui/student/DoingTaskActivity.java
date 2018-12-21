package com.cqupt.ui.student;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.db.DBManager;
import com.cqupt.model.MyChoice;
import com.cqupt.model.Test;
import com.cqupt.ui.common.IDoingTestActivity;
import com.cqupt.util.SoftInputManagerUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class DoingTaskActivity extends IDoingTestActivity{
	
	private DBManager mDb = new DBManager(helper.getConnection());
	private Button finishButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doing_task);

		findView();
		loadData();
		setListener();
	}
	
	@Override
	protected void findView(){
	
		super.findView();
		finishButton = (Button)findViewById(R.id.finishButton);
	    
	}
	
	@Override
	protected void setListener(){
		super.setListener();
		finishButton.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		
		super.onClick(v);
		switch(v.getId()){
		case R.id.finishButton :saveChoice();break;
		}
	}	
	
	@Override
	protected boolean isTestFinished(int id, int testID, String userID) {
		// TODO Auto-generated method stub
		return mDb.checkTestFinished(id, testID, userID);
	}
	
	private void saveChoice() {
		
		progressBar.setVisibility(View.VISIBLE);//��ʼ��ʾ������ ��ʾ�û����ڱ�����ҵ
		DBManager db = new DBManager(helper.getConnection());
		int type;
		int savedCount = 0;
		
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
						MyChoice myChoice = new MyChoice();
						myChoice.setAnswer((String)radio.getTag());
						myChoice.setTestItemID(Integer.parseInt
								(questionList.get(i-1).get("itemID")));
						myChoice.setUserID(application.getUserID());
						myChoice.setSubmitTime(time);
						myChoice.setTaskID(ID);
						db.addMyChoice(myChoice);
						
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
					MyChoice myChoice = new MyChoice();
					myChoice.setAnswer(buffer.toString());
					myChoice.setTestItemID(Integer.parseInt
							(questionList.get(i-1).get("itemID")));
					myChoice.setUserID(application.getUserID());
					myChoice.setSubmitTime(time);
					myChoice.setTaskID(ID);
					db.addMyChoice(myChoice);
					
					savedCount++;
				}
				break;
			case 2:
				TextView textView = (TextView)layout.getChildAt(1);
				String answer = textView.getText().toString();
				if(!answer.trim().equals("")){
					MyChoice myChoice = new MyChoice();
					myChoice.setAnswer(answer);
					myChoice.setTestItemID(Integer.parseInt
							(questionList.get(i-1).get("itemID")));
					myChoice.setUserID(application.getUserID());
					myChoice.setSubmitTime(time);
					myChoice.setTaskID(ID);
					db.addMyChoice(myChoice);
					
					savedCount++;
				}
			}
		}
		progressBar.setVisibility(View.INVISIBLE);

		SoftInputManagerUtil.hideSoftInput(this);
		
		Toast.makeText(DoingTaskActivity.this,
				getResources().getString(R.string.save_success) 
				+ "(" + savedCount + "|"+ (svCenter.getChildCount()-1) + ")"
				,Toast.LENGTH_LONG).show();
		
		
	}

	@Override
	protected ArrayList<Test> getTestList() {
		return mDb.getTestListByTaskID(ID);
	}

	@Override
	protected String getAnswer(int testItemID) {
		return mDb.getAnswer(application.getUserID(), testItemID, ID);
	}

}
