package com.cqupt.ui.common;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.control.Controller;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.UserInfo;
import com.cqupt.net.WebService;
import com.cqupt.net.WebServiceThread;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.util.XMLParser;

import org.ksoap2.serialization.SoapObject;

import java.util.HashMap;

public class LoginActivity extends Activity {
	
	public static final int ACTIVITY_TYPE_FREE_STUDY = 1;
	public static final int ACTIVITY_TYPE_COMMENT = 2;
	
	private EditText idEditText;
	private EditText passwordEditText;
	private Button loginButton;
	
	private boolean connectionOut = false;
	private boolean dataLoadFinished = false;
	
	@SuppressLint("HandlerLeak")
	private Handler  handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			final String userType = (String)msg.obj;
			
			if(userType.equals(WebServiceThread.CONNECTION_FAIL)){
				Toast.makeText(LoginActivity.this,
						R.string.tip_connection_timeout, Toast.LENGTH_LONG).show();
				
				enableViews();
			}else{
				//��¼ʧ��
				if(userType.equals("false")){
					Toast.makeText(LoginActivity.this,
							R.string.tip_pass_account_no_match, Toast.LENGTH_LONG).show();
					idEditText.setText("");
					passwordEditText.setText("");
					
					enableViews();
				//��¼�ɹ�
				}else{
					final String id = idEditText.getText().toString();
					MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(LoginActivity.this);
					final DBManager db = new DBManager(helper.getConnection());
					
					db.setLogin(id,1);
					((MyApplication)getApplication()).setUserID(id);
					
					//ȡ���û�������Ϣ
					new Thread(new Runnable(){
						public void run(){
							HashMap<String,String> p = new HashMap<String,String>();
							p.put("userID",id);
							WebService web = new WebService();
							SoapObject result = web.CallWebService("getUserInfo",p);
								
							if(result != null){
								UserInfo userInfo = XMLParser.ParseUserInfo(result);
								userInfo.setUserType(userType);
								userInfo.setUserID(id);	
								db.addUserInfo(userInfo);
								
								dataLoadFinished = true;
							}else{
								connectionOut = true;
							}
						}
					}).start();
					
					//�ȴ����ݼ�����ɲ���ת
					while(true){
						if(connectionOut){
							Toast.makeText(LoginActivity.this,
									R.string.tip_connection_timeout, Toast.LENGTH_SHORT).show();
							
							enableViews();
							break;
						}
						
						if(dataLoadFinished){
							break;
						}
					}
					
					Intent intent = null;
					//������ϰ������ת����¼ҳ��ʱ ��¼�ɹ����践��������ϰҳ��
					if(getIntent().getFlags() == ACTIVITY_TYPE_FREE_STUDY){
						
						intent = new Intent();
						intent.putExtra("isLogin", true);
						setResult(Activity.RESULT_OK,intent);
						
					}else{//���۽�����ת��������Ҫ��������ҳ�棬ֱ��finish()
						int type = getIntent().getIntExtra("type", -1);
						if(type != ACTIVITY_TYPE_COMMENT){
							
							intent = new Intent(LoginActivity.this,ChooseMainActivity.class);
							startActivity(intent);
							
						}
					}
					
					finish();
					
				}
			}
			
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//ȡ��������
		setContentView(R.layout.activity_login);
		if(!CheckNetwork.isConnectingToInternet(this)){
			Toast.makeText(this,R.string.tip_network_unavailable, Toast.LENGTH_SHORT).show();
		}
		idEditText = (EditText)findViewById(R.id.editText2);
		passwordEditText = (EditText)findViewById(R.id.editText1);
		loginButton = (Button)findViewById(R.id.imageButton1);
		loginButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				disableViews();
				
				if(!CheckNetwork.isConnectingToInternet(LoginActivity.this)){
					
					ShowToastUtil.showNetworkUnavailableToast(LoginActivity.this);
					enableViews();
					
				}else{
					String id = idEditText.getText().toString();
					String password = passwordEditText.getText().toString();
					
					if(id == null || "".equals(id.trim()) || 
							password == null || "".equals(password.trim())){
						Toast.makeText(LoginActivity.this,
								R.string.tip_pass_account_is_null,Toast.LENGTH_SHORT).show();
						
						enableViews();
					}else{
						new Controller(handler).loginCheck(id, password);
					}
				}		
			}
		});
	} 
	
	private void disableViews(){
		loginButton.setEnabled(false);
		idEditText.setEnabled(false);
		passwordEditText.setEnabled(false);
	}
	
	private void enableViews(){
		loginButton.setEnabled(true);
		idEditText.setEnabled(true);
		passwordEditText.setEnabled(true);
	}
	
}
