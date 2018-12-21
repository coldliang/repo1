package com.cqupt.ui.common;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.net.WebService;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.util.HashMap;

public class FeedbackActivity extends Activity implements OnClickListener{
	
	private TextView mReturnView;
	private EditText mContentView;
	private EditText mContactInfoView;
	private Button mSubmitButton;
	private ProgressBar mProgressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_feedback);
		
		findView();
		setListener();
		
	}
	
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.returnView : onBackPressed();break;
		case R.id.submitButton :
			
			if(CheckNetwork.isConnectingToInternet(this)){
				
				mSubmitButton.setEnabled(false);
				String content = mContentView.getText().toString();
				String contactInfo = mContactInfoView.getText().toString();
				String mobel = Build.MODEL;
				String code = Build.VERSION.RELEASE;
				
				StringBuilder sb = new StringBuilder();
				sb.append(content);
				
				if(!contactInfo.equals("")){
					sb.append("  {" + contactInfo + "}  ");
				}
				
				sb.append("  {" + mobel + code +"}  ");
				
				if(TextUtils.isEmpty(content)){
					ShowToastUtil.showEmptyInputToast(this);
					mSubmitButton.setEnabled(true);
				}
				
				new MakeFeedbakTask().execute(sb.toString());
				
			}else{
				ShowToastUtil.showNetworkUnavailableToast(this);
			}
			
			break;
			
		}
		
	}
	
	private void findView(){
		
		mReturnView = (TextView) findViewById(R.id.returnView);
		mContentView = (EditText) findViewById(R.id.content);
		mContactInfoView = (EditText) findViewById(R.id.contactInfo);
		mSubmitButton = (Button) findViewById(R.id.submitButton);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		
	}
	
	private void setListener(){
		
		mReturnView.setOnClickListener(this);
		mSubmitButton.setOnClickListener(this);
		
	}

	class MakeFeedbakTask extends AsyncTask<String, Void, Integer>{
		
		private static final int FAIL = -1;
		private static final int SUCCESS = 1;
		private WebService web;

		@Override
		protected void onPreExecute() {
			mProgressBar.setVisibility(View.VISIBLE);
		}

		
		@Override
		protected Integer doInBackground(String... params) {
			
			String content = params[0];
			String userID = ((MyApplication)getApplication()).getUserID();
			
			web = new WebService();
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("content", content);
			p.put("userID", userID == null ? "" : userID);
			
			SoapObject result = web.CallWebService("makeAppFeedback", p);
			
			if(result == null){
				return FAIL;
			}
			
			return XMLParser.parseBoolean(result).equalsIgnoreCase("true") ? SUCCESS : FAIL;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			
			mProgressBar.setVisibility(View.GONE);
			mSubmitButton.setEnabled(true);
			
			if(result == FAIL){
				ShowToastUtil.showConnectionTimeOutToast(FeedbackActivity.this);
			}else{
				mContentView.setText("");
				mContactInfoView.setText("");
				Toast.makeText(FeedbackActivity.this, R.string.make_feedback_success, Toast.LENGTH_SHORT)
				.show();
			}
			
		}
		
	}

}
