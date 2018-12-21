package com.cqupt.ui.teacher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.model.Task;
import com.cqupt.net.WebService;
import com.cqupt.util.DateTimePickDialogUtil;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.util.HashMap;
import java.util.TreeSet;

public class EditTaskActivity extends Activity implements OnClickListener{

	private MyApplication application;
	
	private ProgressBar progressBar;
	private CheckBox checkBox;
	private EditText titleView;
	private EditText contentView;
	private EditText remarkView;
	private TextView stuCountView;
	private TextView examsCountView;
	private TextView sDateView;
	private TextView eDateView;
	private TextView returnView;
	private TextView hintTitleView;
	private TextView hintStartDateView;
	private Button saveButton;
	
	private int taskID;
	private String originTitle;//��¼ԭʼ�ı���,��������༭�����еı����ظ�����
	
	/**
	 * ��¼�������벻�Ϸ�����
	 */
	private TreeSet<Integer> errorLines;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_edit_task);
		
		findView();
		loadData();
		setListener();
		
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()){	
		case R.id.startDate : 
			new DateTimePickDialogUtil(this, "").dateTimePicKDialog(sDateView);
		    break;
		case R.id.endDate : 
			new DateTimePickDialogUtil(this, "").dateTimePicKDialog(eDateView);
			break;
		case R.id.saveButton :
			new SaveExerciseThread().execute();break;
		case R.id.returnView : onBackPressed();break;
			
		}		
	}

	/*@Override
	public void onBackPressed() {
		showReturnConfirmDialog();
	}*/
	
	private void loadData(){
		
		application = (MyApplication)getApplication();
		errorLines = new TreeSet<Integer>();
		
		Intent it = getIntent();
		taskID = it.getIntExtra("taskID", -1);
		
		//��ʾ�Ѿ��е���ҵ����
		if(taskID != -1){
			new GetTaskInfoThread().execute();
		}	
	}
	
	@Override
	protected void onRestart() {
		new GetTaskInfoThread().execute();
		super.onRestart();
	}
	
	//-----------------------------�Զ��巽��-----------------------------
	

	private void findView(){
		
		progressBar = (ProgressBar)findViewById(R.id.progressBar);
		checkBox = (CheckBox)findViewById(R.id.isDiscuss);
		titleView = (EditText)findViewById(R.id.title);
		contentView = (EditText)findViewById(R.id.taskContent);
		remarkView = (EditText)findViewById(R.id.taskRemark);
		stuCountView = (TextView)findViewById(R.id.studentCount);
		examsCountView = (TextView)findViewById(R.id.examCount);
		sDateView = (TextView)findViewById(R.id.startDate);
		eDateView = (TextView)findViewById(R.id.endDate);
		returnView = (TextView)findViewById(R.id.returnView);
		hintTitleView = (TextView)findViewById(R.id.hintTitle);
		hintStartDateView = (TextView)findViewById(R.id.hintStartDate);		
		saveButton = (Button)findViewById(R.id.saveButton);
		
	}
	
	private void setListener(){
		
		sDateView.setOnClickListener(this);
		eDateView.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		returnView.setOnClickListener(this);
		
		titleView.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				
				if(!hasFocus){
					String title = ((EditText)v).getText().toString().trim();
					
					if(TextUtils.isEmpty(title) || !title.equals(originTitle)){
						//������ⲻΪ��,���ύ������������Ƿ�����
						if(!TextUtils.isEmpty(title)){
							new CheckTitleThread().execute();
						}else{
							Toast.makeText(EditTaskActivity.this, 
									R.string.empty_task_title, Toast.LENGTH_SHORT).show();
							
							hintTitleView.setTextColor(EditTaskActivity.this
									.getResources().getColor(R.color.my_red));
							errorLines.add(1);
						}
					}					
					
				}
			}
		});
		
		sDateView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				String eDate = eDateView.getText().toString();
				
				if(!eDate.equals("") && (s.toString().compareTo(eDate) > 0)){
					hintStartDateView.setTextColor(EditTaskActivity.this
							.getResources().getColor(R.color.my_red));
					errorLines.add(2);
				}else{
					if(!eDate.equals("") && (s.toString().compareTo(eDate) < 0)){
						hintStartDateView.setTextColor(EditTaskActivity.this
								.getResources().getColor(android.R.color.black));
						errorLines.remove((Object)2);
					}
				}
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		
		eDateView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				String sDate = sDateView.getText().toString();
				
				if(!sDate.equals("") && (s.toString().compareTo(sDate) < 0)){
					hintStartDateView.setTextColor(EditTaskActivity.this
							.getResources().getColor(R.color.my_red));
					errorLines.add(2);
				}else{
					if(!sDate.equals("") && (s.toString().compareTo(sDate) > 0)){
						hintStartDateView.setTextColor(EditTaskActivity.this
								.getResources().getColor(android.R.color.black));
						errorLines.remove((Object)2);
					}
				}
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	/**
	 * ��ʾ�˳���ʾ�Ի���
	 */
	/*private void showReturnConfirmDialog(){
		
		AlertDialog log = new AlertDialog.Builder(this)
		                      .setMessage(R.string.return_confirm)
		                      .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent it = new Intent();
									it.putExtra("isTaskChanged",isTaskChanged);
									setResult(Activity.RESULT_OK,it);
									finish();	
								}
							})
							.setNegativeButton(R.string.cancel,null)
							.create();
		log.show();
		
	}*/
	
	
	
	/**
	 * �������Ƿ��ظ��ĺ�̨�߳�
	 */
	class CheckTitleThread extends AsyncTask<Void,Void,Integer>{

		private final int DUPLICATED_NAME = -2;
		private final int CONNECTION_FAIL = -1;
		private final int SUCCESS = 1;
		
		private WebService web = new WebService();
		
		@Override
		protected Integer doInBackground(Void... params) {
			
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("title",titleView.getText().toString().trim());
			p.put("userID",application.getUserID());
			
			SoapObject result = web.CallWebService("checkTaskTitle", p);
			
			if(result == null){
				return CONNECTION_FAIL;
			}
			
			if(XMLParser.parseBoolean(result).equalsIgnoreCase("true")){
				return SUCCESS;
			}else{
				return DUPLICATED_NAME;
			}
			
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			switch(result){
			case DUPLICATED_NAME :
				Toast.makeText(EditTaskActivity.this,
						R.string.duplicated_task_title, Toast.LENGTH_LONG).show();
				
				hintTitleView.setTextColor(EditTaskActivity.this
						.getResources().getColor(R.color.my_red));
				errorLines.add(1);
				break;
			case CONNECTION_FAIL : break;
			case SUCCESS : 
				hintTitleView.setTextColor(Color.BLACK);
				errorLines.remove((Object)1);
				break;					
			}	
		}
	} 
	
	/**
	 * �����ҵ����ĺ�̨�߳�
	 */
	class GetTaskInfoThread extends AsyncTask<Void,Void,Integer>{
		
		private final int CONNECTION_FAIL = -1;
		private final int SUCCESS = 1;
		
		private WebService web = new WebService();
		private Task task;

		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(Void... params) {
			
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("taskID",String.valueOf(taskID));
			
			SoapObject result = web.CallWebService("getTeacherTaskDetail", p);
			
			if(result == null){
				return CONNECTION_FAIL;
			}
			
			task = XMLParser.parseTeacherTaskDetail(result);
			
			return SUCCESS;
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			
			if(result == CONNECTION_FAIL){
				Toast.makeText(EditTaskActivity.this,
						R.string.tip_connection_timeout, Toast.LENGTH_LONG).show();
			}else{
				titleView.setText(task.getTitle());
				originTitle = task.getTitle();
				stuCountView.setText(String.valueOf(task.getStudents()));
				examsCountView.setText(String.valueOf(task.getExams()));
				
				if(task.getIsDiscuss().equalsIgnoreCase("false")){
					checkBox.setChecked(false);
				}else{
					checkBox.setChecked(true);
				}
				
				sDateView.setText(task.getStartDate());
				eDateView.setText(task.getEndDate());
				contentView.setText(task.getContent().equals("null") ? "" : 
					Html.fromHtml(task.getContent()));
				remarkView.setText(task.getRemark().equals("null") ? "" : 
					Html.fromHtml(task.getRemark()));
			}	
		}	
	}
	
	/**
	 * ������ҵ�ĺ�̨�߳�
	 */
	class SaveExerciseThread extends AsyncTask<Void,Void,Integer>{

		private final static int ERROR_INPUT = -1;
		private final static int CONNECTION_FAIL = -2;
		private final static int ERROR = -3;
		private final static int EMPTY_DATETIME = -4;

		private final static int SUCCESS = 0;
		
		WebService web = new WebService();
		
		@Override
		protected void onPreExecute() {
			
			progressBar.setVisibility(View.VISIBLE);

		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			
			if(errorLines.size() != 0){
				return ERROR_INPUT;
			}
			
			if(sDateView.getText().toString().equals("") 
					|| eDateView.getText().toString().equals("")){
				return EMPTY_DATETIME;
			}
			
			HashMap<String,String> p = new HashMap<String, String>();
			
			p.put("title",titleView.getText().toString());
			p.put("isDiscuss",checkBox.isChecked() ? "1" : "0");
			p.put("sDate",sDateView.getText().toString());
			p.put("eDate", eDateView.getText().toString());
			p.put("content", contentView.getText().toString());
			p.put("remark", remarkView.getText().toString());
			
			SoapObject result;
			//������ҵ
			if(taskID == -1){
				p.put("userID",application.getUserID());
				result = web.CallWebService("publishExercise",p);
			}else{//�޸���ҵ����
				p.put("taskID",String.valueOf(taskID));
				result = web.CallWebService("updateExercise",p);
			}
			
			if(result == null){
				return CONNECTION_FAIL;
			}
			
			if(XMLParser.parseInt(result) == 0){
				return ERROR;
			}
			
			taskID = XMLParser.parseInt(result);
			
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			switch(result){
			case ERROR_INPUT :
				progressBar.setVisibility(View.GONE);
				Toast.makeText(EditTaskActivity.this, R.string.error_input
						, Toast.LENGTH_SHORT).show();				
				break;
			case CONNECTION_FAIL :
				progressBar.setVisibility(View.GONE);
				Toast.makeText(EditTaskActivity.this,R.string.tip_connection_timeout
						,Toast.LENGTH_SHORT).show();
				break;
			case ERROR : 
				progressBar.setVisibility(View.GONE);
				Toast.makeText(EditTaskActivity.this,R.string.error
						,Toast.LENGTH_SHORT).show();
				break;
			case EMPTY_DATETIME :
				progressBar.setVisibility(View.GONE);
				Toast.makeText(EditTaskActivity.this,
						R.string.tip_empty_sDate_eDate_is_not_allowed
						,Toast.LENGTH_SHORT).show();
				break;
			case SUCCESS :			
				progressBar.setVisibility(View.GONE);
				Intent it = new Intent(EditTaskActivity.this,UploadAttachmentActivity.class);
				it.putExtra("taskID", taskID);
				startActivity(it);
				break;
				
			}
		}
		
	}
    
}
