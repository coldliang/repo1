package com.cqupt.ui.common;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cqupt.R;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.util.HashMap;
import java.util.Map;

@SuppressLint("InflateParams")
public abstract class IDoingTestActivity extends ITestHandlingActivity {
	
	
	//-----------------���󷽷�-------------------
	
	abstract protected String getAnswer(int testItemID);
	
	//------------------------------��д����--------------------------	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		//�������̷߳�������	
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.permitNetwork().build());
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	protected void makeQuestionListView(){
		
		svCenter.removeViews(1,svCenter.getChildCount()-1);
		
		for(int i = 0;i < questionList.size();i++){
			int itemID = Integer.parseInt(questionList.get(i).get("itemID"));
			int type = getItemType(itemID);
			String answer = getAnswer(itemID);
			LinearLayout layout = new LinearLayout(IDoingTestActivity.this);
			layout.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT
					,LinearLayout.LayoutParams.WRAP_CONTENT));
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setTag(type);//��¼����,���㱣���ʱ�����ֱ��淽ʽ
			int optionCounts = getOptionCount(itemID);//ѡ�����
			//���Ʒָ���
			LinearLayout divider = new LinearLayout(IDoingTestActivity.this);
			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,2);
			p.setMargins(0,2,0,0);
			divider.setLayoutParams(p);
			divider.setBackgroundResource(R.color.divider);
			//��̬����ѡ��
			switch(type){
			//��ѡ
			case 0:
				//���
				TextView titleView = new TextView(this);
				String title = questionList.get(i).get("title");
				titleView.setText(Html.fromHtml(title));
				titleView.setTextSize(textSize);
				layout.addView(titleView);
				RadioGroup group = new RadioGroup(IDoingTestActivity.this);
				for(int j = 0;j < optionCounts; j++){
					RadioButton radio = new RadioButton(this);
					group.addView(radio);
					radio.setTag(questionList.get(i).get("c"+j+"ID"));
					//���ֱ������ݿ����û��Ѿ�ѡ��Ĵ�
					if(questionList.get(i).get("c"+j+"ID").equals(answer)){
						radio.setChecked(true);
					}
					radio.setText(questionList.get(i).get("c"+j));
					radio.setTextSize(textSize);
					}
				layout.addView(group);
				break;
			//��ѡ
			case 1:
				//���
				TextView titleView1 = new TextView(this);
				String title1 = questionList.get(i).get("title");
				titleView1.setText(Html.fromHtml(title1));
				titleView1.setTextSize(textSize);
				layout.addView(titleView1);
				String[] answers = answer.split(",");//��ֶ�ѡ��,�Ա���ƥ��
				for(int j = 0;j < optionCounts; j++){
					CheckBox box = new CheckBox(this);
					box.setTag(questionList.get(i).get("c"+j+"ID"));
					box.setText(questionList.get(i).get("c"+j));
					box.setTextSize(textSize);
					//���ֱ������ݿ����û��Ѿ�ѡ��Ĵ�
					for(int k = 0 ;k < answers.length;k++){
						if(answers[k].equals(questionList.get(i).get("c"+j+"ID"))){
							box.setChecked(true);
						}
					}
					layout.addView(box);
				}
							
				break;
			//������
			case 2:
				layout = (LinearLayout)LayoutInflater.from(IDoingTestActivity.this)
				         .inflate(R.layout.layout_subject_question, null);
				layout.setTag(2);
				
				TextView text = (TextView)layout.findViewById(R.id.title);
				text.setText(Html.fromHtml(questionList.get(i).get("title")));
				text.setTextSize(textSize);
				
				EditText answerView= (EditText)layout.findViewById(R.id.answer);
		        answerView.setText(answer);//�����û���
		        answerView.setTextSize(textSize);
		        answerView.setHint(R.string.subject_question_eidt_hint);
		        
		        TextView referAnswerView = (TextView) layout.findViewById(R.id.referAnswer);
		        referAnswerView.setTextSize(textSize);
			}
			
			layout.addView(divider);
			svCenter.addView(layout);
			zoomView.setEnabled(true);
			zoomView.setClickable(true);
		}

	}
	
	//----------------�Զ��巽��-------------------
	
	private int getOptionCount(int itemID) {
		
		int optionCount = mDb.getOptionCount(itemID);
		
		if(optionCount == 0){
			
			Map<String,String> p = new HashMap<String, String>();
			p.put("itemID", String.valueOf(itemID));
			
			SoapObject result = mWeb.CallWebService("getOptionCountByItemID", p);
			
			if(result == null){
				return 0;
			}
			
			return XMLParser.parseInt(result);
			
		}
		
		return optionCount;
	}
	
	protected int getItemType(int itemID) {
		
		int itemType = mDb.getItemType(itemID);
		
		//����û������,���������
		if(itemType == -1){
			
			Map<String,String> p = new HashMap<String, String>();
			p.put("itemID", String.valueOf(itemID));
			
			SoapObject result = mWeb.CallWebService("getTestItemType", p);
			
			if(result == null){
				return -1;
			}
			
			return XMLParser.parseInt(result);
		
		}
		
		return itemType;
		
	}


}
