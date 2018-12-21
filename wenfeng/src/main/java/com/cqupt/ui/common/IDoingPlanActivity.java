package com.cqupt.ui.common;

import android.graphics.Color;
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
import com.cqupt.model.TestItemOption;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.util.HashMap;
import java.util.List;

public abstract class IDoingPlanActivity extends IDoingTestActivity {
	
	protected Button mShowAnswersButton;
	protected Button mBottomRightButton;
	private boolean isAnswerShown;//��¼�Ƿ���ʾ�˴�
	
	abstract protected void onBottomRightButtonClick();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_doing_plan);
		
		findView();
		loadData();
		setListener();
	}
	
	@Override
	protected void pageMove(int from, int to) {
		mShowAnswersButton.setEnabled(false);
		mBottomRightButton.setEnabled(false);
		super.pageMove(from, to);
		isAnswerShown = false;
		mShowAnswersButton.setText(R.string.show_answers);
	}
	
	@Override
	protected void findView() {
		super.findView();
		
		mShowAnswersButton = (Button) findViewById(R.id.bottomLeftButton);
		mBottomRightButton = (Button) findViewById(R.id.bottomRightButton);
		
	}
	
	//������questionListview��֤��questionList�Ѿ��������
	@Override
	protected void makeQuestionListView() {
		super.makeQuestionListView();
		
		mShowAnswersButton.setEnabled(true);
		mBottomRightButton.setEnabled(true);
	
	}
	
	@Override
	protected void setListener() {
		
		super.setListener();
		
		mShowAnswersButton.setOnClickListener(this);
		mBottomRightButton.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		switch (v.getId()) {
		case R.id.bottomLeftButton:
			
			if(questionList == null || questionList.size() == 0){
				Toast.makeText(this, R.string.no_answers, Toast.LENGTH_SHORT).show();
			}else{
				toggleAnswerViews();
			}
			
			break;

		case R.id.bottomRightButton:
			onBottomRightButtonClick();
			break;
		}
	}

	private List<TestItemOption> getTestItemOptions(int itemID) {
		
		List<TestItemOption> list =  mDb.getTestItemOption(itemID);
		
		if(list.size() == 0){
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("testItemID", String.valueOf(itemID));
			
			SoapObject result = mWeb.CallWebService("getTestItemOption", p);
			
			if(result != null){
				list = XMLParser.parseTestItemOption(result);
			}
			
			return list;
		}
		
		return list;
		
	}

	/**
	 * ��ʾ�����ش�
	 */
	private void toggleAnswerViews(){
		
		//�����ݲ��ֽ������»���
		for(int i = 1; i < svCenter.getChildCount(); i++){
			
			LinearLayout item = (LinearLayout)svCenter.getChildAt(i);
			int itemID = Integer.parseInt(questionList.get(i - 1).get("itemID"));
			int type = getItemType(itemID);
			//��ø��������options
			List<TestItemOption> options = getTestItemOptions(itemID);
			
			switch(type){
			//��ѡ
			case 0:
				
				RadioGroup radioGroup = (RadioGroup) item.getChildAt(1);
				for(int j = 0 ; j < radioGroup.getChildCount() - 1; j ++){
					RadioButton radioButton = (RadioButton)radioGroup.getChildAt(j);
					if(!isAnswerShown){
						
						if(options.get(j).getTestItemOptionIsAnswer() == 1){							
							radioButton.setTextColor(getResources().getColor(
									R.color.background_lightgreen));
							break;
						}
						
					}else{
						radioButton.setTextColor(Color.BLACK);
					}
					
					
				}
		
				break;
			//��ѡ
			case 1:

				for(int j = 1 ; j < item.getChildCount() - 1 ; j ++){
					
					CheckBox box = (CheckBox) item.getChildAt(j);
					if(!isAnswerShown){
						//��ȷ������ɫ��ʾ
						if(options.get(j - 1).getTestItemOptionIsAnswer() == 1){
							box.setTextColor(getResources().getColor(R.color.background_lightgreen));
						}
						
					}else{
						box.setTextColor(Color.BLACK);
					}
					
				}
		
				break;
			//������
			case 2:
				
				TextView textView = (TextView) item.getChildAt(2);
								
				if(!isAnswerShown){
					
					textView.setVisibility(View.VISIBLE);
					
					//û�вο���
					if(options == null || options.size() == 0){
						textView.setText(getResources().getString(R.string.ref_answer));
					}else{
						
						StringBuilder sb = new StringBuilder();
						for(int j = 0; j < options.size(); j ++){
							sb.append(options.get(j).getTestItemOptionContent() + "\n");
							
						}
						
						textView.setText(getResources().getString(R.string.ref_answer) + "\n" 
								+ sb.toString());
						
					}
					
					textView.setTextColor(getResources().getColor(R.color.background_lightgreen));
					
				}else{
					textView.setVisibility(View.GONE);
				}
				
				break;
				
			}
		}
		
		isAnswerShown = !isAnswerShown;
		
		if(!isAnswerShown){
			mShowAnswersButton.setText(R.string.show_answers);
		}else{
			mShowAnswersButton.setText(R.string.hide_answers);
		}
		
		
	}

}
