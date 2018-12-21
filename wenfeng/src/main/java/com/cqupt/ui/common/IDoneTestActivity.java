package com.cqupt.ui.common;

import android.os.Bundle;
import android.text.Html;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cqupt.R;
import com.cqupt.model.TestItemOption;

import java.util.List;

public abstract class IDoneTestActivity extends ITestHandlingActivity {
	
	//---------------------���󷽷�--------------------
	
	protected abstract String getAnswer(int itemID);
	protected abstract int getItemType(int itemID);
	protected abstract List<TestItemOption> getTestItemOptions(int itemID);
		
	//------------------------��д����----------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_done_test);
				
		findView();
		loadData();
		setListener();
	}

	@Override
	protected void makeQuestionListView(){
		
		svCenter.removeViews(1,svCenter.getChildCount()-1);
		for(int i = 0;i < questionList.size();i++){
			int itemID = Integer.parseInt(questionList.get(i).get("itemID"));
			int type = getItemType(itemID);
			
			String answer = getAnswer(itemID);
			LinearLayout layout = new LinearLayout(this);
			layout.setLayoutParams(new LinearLayout
					.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
							,LinearLayout.LayoutParams.WRAP_CONTENT));
			layout.setOrientation(LinearLayout.VERTICAL);
			//��ø��������options
			List<TestItemOption> options = getTestItemOptions(itemID);
			//������ɲ���
			TextView titleView1 = new TextView(this);
			String title1 = questionList.get(i).get("title");
			titleView1.setText(Html.fromHtml(title1));
			titleView1.setTextSize(textSize);
			layout.addView(titleView1);
			//���Ʒָ���
			LinearLayout divider = new LinearLayout(this);
			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,2);
			p.setMargins(0,2,0,0);
			divider.setLayoutParams(p);
			divider.setBackgroundResource(R.color.divider);
			//����ѡ���
			switch(type){
			//��ѡ
			case 0:
				for(int j = 0;j < options.size(); j++){
					RadioButton radioButton = new RadioButton(this);
					radioButton.setClickable(false);
					//�û�ѡ��Ĵ𰸴�
					if(questionList.get(i).get("c"+j+"ID").equals(answer)){
						radioButton.setChecked(true);
					}
					//��ȷ������ɫ��ʾ
					if(options.get(j).getTestItemOptionIsAnswer() == 1){
						radioButton.setTextColor(getResources().getColor(
								R.color.background_lightgreen));
					}
					radioButton.setText(questionList.get(i).get("c"+j));
					radioButton.setTextSize(textSize);
					layout.addView(radioButton);
					}
				break;
			//��ѡ
			case 1:
				String[] answers = answer.split(",");//��ֶ�ѡ��,�Ա���ƥ��
				for(int j = 0;j < options.size(); j++){
					CheckBox box = new CheckBox(this);
					box.setClickable(false);
					box.setText(questionList.get(i).get("c"+j));
					box.setTextSize(textSize);
					//�û���ѡ�𰸴�
					for(int k = 0 ;k < answers.length;k++){
						if(answers[k].equals(questionList.get(i).get("c"+j+"ID"))){
							box.setChecked(true);
						}
					}
					//��ȷ������ɫ��ʾ
					if(options.get(j).getTestItemOptionIsAnswer() == 1){
						box.setTextColor(getResources().getColor(R.color.background_lightgreen));
					}
					layout.addView(box);
				}
				break;
			//������
			case 2:
				TextView myAnswer = new TextView(this);//�û���
				TextView rightAnswer = new TextView(this);//��ȷ��
				//�����û���
				myAnswer.setText(getResources().getString(R.string.my_answer)+"\n"+answer);
				myAnswer.setTextSize(textSize);
				layout.addView(myAnswer);
				//û�вο���
				if(options == null || options.size() == 0){
					rightAnswer.setText(getResources().getString(R.string.ref_answer));
				}else{
					
					StringBuilder sb = new StringBuilder();
					for(int j = 0; j < options.size(); j ++){
						sb.append(options.get(j).getTestItemOptionContent() + "\n");
						
					}
					
					rightAnswer.setText(getResources().getString(R.string.ref_answer) + "\n" 
							+ sb.toString());
					
				}
				rightAnswer.setTextSize(textSize);
				layout.addView(rightAnswer);
			}
			layout.addView(divider);//���Ʒָ���
			svCenter.addView(layout);
			
			zoomView.setClickable(true);
			zoomView.setEnabled(true);
		}

	}

}
