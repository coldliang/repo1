package com.cqupt.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.ui.student.TaskActivity;
import com.cqupt.ui.teacher.TeacherMainActivity;
import com.cqupt.util.LanguageSetting;
import com.readystatesoftware.viewbadger.BadgeView;

public class ChooseMainActivity extends Activity implements OnClickListener{
	
	private final static int REQUEST_SET = 1;
	
	private ImageButton mSelfLearningButton;
	private ImageButton mInteractiveButton;
	private ImageButton mSetButton;
    private ImageButton mOnlineLearningButton;
	private BadgeView mSetViewBadge;
	
	private long mFirstTime = 0;
	private LanguageSetting mLs;
	private SharedPreferences mSp;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_choose_main);

		findView();
		loadData();
		setListener();
	}
	
	@Override
	public void onClick(View v) {

        Intent it;
        switch (v.getId()) {
            case R.id.self_button:
			it = new Intent(this,ChooseSecondLevelActivity.class);
			startActivity(it);
			break;
            case R.id.interactive_button:
			
			if(isLogin()){
				
				if(getUserType().equalsIgnoreCase("???")){
					it = new Intent(this,TaskActivity.class);
				}else{
					it = new Intent(this,TeacherMainActivity.class);
				}
				
			}else{
				it = new Intent(this,LoginActivity.class);
			}
			
			startActivity(it);
			break;
			case R.id.online_learning:
				it = new Intent(this,OnlineLearningActivity.class);
                startActivity(it);
                break;
            case R.id.set:
			it = new Intent(this,SetActivity.class);
			startActivityForResult(it, REQUEST_SET);
			break;
		}
		
		
	}
	
	@Override
	public void onBackPressed() {

    	long secondTime = System.currentTimeMillis();
        if (secondTime - mFirstTime > 2000) {
       	 Toast.makeText(this,R.string.tip_exit, Toast.LENGTH_SHORT).show(); 
            mFirstTime = secondTime;
        }else{
       	 MySQLiteOpenHelper.getInstance(this).close();
       	 System.exit(0);
         }
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode == REQUEST_SET && resultCode == Activity.RESULT_OK){
			int type = data.getIntExtra("type",SetActivity.DEFAULT);
			
			switch(type){
			case SetActivity.LOG_OUT : logout();break;
			case SetActivity.LANGUAGE :
				String language = data.getStringExtra("language");
				changeLanguage(language);break;
			}
		}
		
	}
	
	@Override
	protected void onRestart(){
			
		if(!mSp.getBoolean("appUpdate",false)){
			mSetViewBadge.hide();
		}else{
			mSetViewBadge.show();
		}	
		
		super.onRestart();
   
	}


	private boolean isLogin(){
		
		MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(ChooseMainActivity.this);
		DBManager db = new DBManager(helper.getConnection());
		return db.login();
	}
	
	private String getUserType(){
		
		MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(ChooseMainActivity.this);
		DBManager db = new DBManager(helper.getConnection());;
		   
		return db.getUserInfo().getUserType();
	}
	
	private void setUserID(){
		
		MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(ChooseMainActivity.this);
		DBManager db = new DBManager(helper.getConnection());
		
		String userID = db.getUserInfo().getUserID();
		
		if(userID != null){
			((MyApplication)getApplication()).setUserID(userID);
		}
		
	}
	
	private void loadData(){
		
		mLs = LanguageSetting.getInstance();
		mSp = getSharedPreferences("my_prefer",Activity.MODE_PRIVATE);
		setUserID();
		
		mSetViewBadge = new BadgeView(this, mSetButton);
		mSetViewBadge.setText("!");
		
		if(mSp.getBoolean("appUpdate",false)){
			mSetViewBadge.show();
		}
		
	}
	
	private void findView() {
		
		mSelfLearningButton = (ImageButton) findViewById(R.id.self_button);
		mInteractiveButton = (ImageButton) findViewById(R.id.interactive_button);
		mSetButton = (ImageButton) findViewById(R.id.set);
        mOnlineLearningButton = (ImageButton) findViewById(R.id.online_learning);
		
	}
	
	private void setListener(){
		
		mInteractiveButton.setOnClickListener(this);
		mSelfLearningButton.setOnClickListener(this);
		mSetButton.setOnClickListener(this);
        mOnlineLearningButton.setOnClickListener(this);
		
	}
	
	private void logout(){
		DBManager db = new DBManager(MySQLiteOpenHelper.getInstance(this).getConnection());
		MyApplication application = (MyApplication)getApplication();
		db.setLogin(application.getUserID(), 0);
		application.setUserID(null);
		
		Intent it = new Intent(this,LoginActivity.class);
		startActivity(it);
		finish();
	}
	
	private void changeLanguage(String language){
		if(language.equals("English")){
			mLs.setCurrentLanguage(this,"en");
		}else{
			mLs.setCurrentLanguage(this,"zh");
		}
		recreate();
	}

}
