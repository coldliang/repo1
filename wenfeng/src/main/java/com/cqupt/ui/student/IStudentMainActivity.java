package com.cqupt.ui.student;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class IStudentMainActivity extends Activity implements OnClickListener{
	
	
	/**
	 * ���ݿ����
	 */
	protected MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(this);

	/**
	 * ��ʾ�û�ID����ͼ
	 */
	protected TextView userIdView;
	/**
	 * �����б��Զ���������
	 */ 
	protected BaseAdapter adapter;
	/**
	 * �����б�Ϊ��ʱ��Ĭ����ʾ��ͼ
	 */
	private TextView emptyView;
	
	/**
	 * ��Ӧ�����б�
	 */
	protected ListView listView;
	
	
	/**
	 * �л������ҵ��tab��
	 */
	private LinearLayout recentTab;
	/**
	 * �л����ڽ�������ҵ��tab��
	 */
	private LinearLayout doingTab;
	/**
	 * �л��Ѿ������ҵ��tab��
	 */
	private LinearLayout doneTab;
	/**
	 * ������
	 */
	private TextView searchView;
	/**
	 * ���Ƽ�������
	 */
	private GestureDetector gesDet;
	
	/**
	 * ��ǰѡ��tab����±�(1-3)
	 */
	protected int selectedTabIndex = 2;
	/**
	 * ��������б�����ݼ�
	 */
	protected ArrayList<HashMap<String,String>> data;
	
	private TextView recentTabText;
	private TextView doingTabText;
	private TextView doneTabText;
	private ImageView recentTabImage;
	private ImageView doingTabImage;
	private ImageView doneTabImage;
	
	protected MyApplication application;
	protected SharedPreferences sp;
	private boolean canTabMove = true;
	
	//------------------------���󷽷�-------------------
	
	protected abstract ArrayList<HashMap<String,String>> getRecentTabData();
	protected abstract ArrayList<HashMap<String,String>> getDoingTabData();
	protected abstract ArrayList<HashMap<String,String>> getDoneTabData();
	protected abstract BaseAdapter getAdapter();
	protected abstract void onSearchBarClick();
	
	//--------------------------------------��д����---------------------------------
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//ȡ��������

    }
	
	@Override
	protected void onRestart(){
			
		super.onRestart();
		//��Ҫ�Ǹ��������������ͻ�ԭ��һ���û��뿪�ý���ʱ��ͣ����tab��
		updateDateForTab();			
   
	}

	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if(canTabMove){
			return gesDet.onTouchEvent(event);
		}else{
			return false;
		}
		
	}
	
	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.tab_recent :
			
			if(canTabMove){
				setTabMoveAnimation(selectedTabIndex, 1);
			}
			
			break;
		case R.id.tab_doing : 
			
			if(canTabMove){
				setTabMoveAnimation(selectedTabIndex, 2);
			}
			
			break;
		case R.id.tab_done : 
			
			if(canTabMove){
				setTabMoveAnimation(selectedTabIndex, 3);
			}
			
			break;
		case R.id.includeSearch : onSearchBarClick();break;
		}
	}
	
	//--------------------------------------�Զ��巽��----------------------------------------
	
	
	protected void setTabMoveable(boolean moveable){
		canTabMove = moveable;
	}
	
	protected void setListener(){
		
		recentTab.setOnClickListener(this);
		doingTab.setOnClickListener(this);
		doneTab.setOnClickListener(this);
		searchView.setOnClickListener(this);
		
		listView.setOnTouchListener(new OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				if(canTabMove){
					return gesDet.onTouchEvent(event);
				}
				
				return false;
				
			}
		});

		gesDet = new GestureDetector(IStudentMainActivity.this,new SimpleOnGestureListener(){
			
			@Override
	        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX
	        		, float velocityY){
	            final int FLING_MIN_DISTANCE=300;//X����y�����ƶ��ľ���(����)
	            final int FLING_MIN_VELOCITY=200;//x����y���ϵ��ƶ��ٶ�(����/��)
	            //���󻬶�
	            if((e1.getX()-e2.getX())>FLING_MIN_DISTANCE && 
	            		Math.abs(velocityX)>FLING_MIN_VELOCITY){
	            	//��ǰ�������м��tab,��Ӧ���л������ұߵ�tab
	            	if(selectedTabIndex == 2){
	            		setTabMoveAnimation(selectedTabIndex, 3);
	            	}else{
	            		//��ǰ��������ߵ�tab,��Ӧ���л������м��tab
	            		if(selectedTabIndex == 1){
	            			setTabMoveAnimation(selectedTabIndex, 2);
		            	}
	            	}
	            //���һ���
	            }else {
	            	if((e2.getX()-e1.getX())>FLING_MIN_DISTANCE && 
	            			Math.abs(velocityX)>FLING_MIN_VELOCITY){
	            		//��ǰ�������м��tab,��Ӧ���л�������ߵ�tab
		            	if(selectedTabIndex == 2){
		            		setTabMoveAnimation(selectedTabIndex, 1);
		            	}else{
		            		//��ǰ�������ұߵ�tab,��Ӧ���л������м��tab
		            		if(selectedTabIndex == 3){
		            			setTabMoveAnimation(selectedTabIndex, 2);
			            	}
	            	    }
	                }
	            }	 
	            return false;
	        }
		});
	}

	/**
	 * ��ȡXML��ͼ
	 */
    protected void findView(){
    	
		userIdView = (TextView)findViewById(R.id.userIdView);
		listView = (ListView) findViewById(R.id.list);
		emptyView = (TextView)findViewById(R.id.emptyView);
		recentTab = (LinearLayout)findViewById(R.id.tab_recent);
		doingTab = (LinearLayout)findViewById(R.id.tab_doing);
		doneTab = (LinearLayout)findViewById(R.id.tab_done);
		recentTabText = (TextView)findViewById(R.id.tab_recent_text);
		doingTabText = (TextView)findViewById(R.id.tab_doing_text);
		doneTabText = (TextView)findViewById(R.id.tab_done_text);
		recentTabImage = (ImageView)findViewById(R.id.tab_recent_image);
		doingTabImage = (ImageView)findViewById(R.id.tab_doing_image);
		doneTabImage = (ImageView)findViewById(R.id.tab_done_image);
		searchView = (TextView)findViewById(R.id.includeSearch);
	}

	protected void loadData(){
		
		application = (MyApplication)getApplication();
		
		DBManager db = new DBManager(helper.getConnection());
	    sp = getSharedPreferences("my_prefer",Activity.MODE_PRIVATE);
		
		userIdView.setText(db.getUserInfo().getUserName());
		data = getDoingTabData();
	
		adapter = getAdapter();
		listView.setEmptyView(emptyView);
		listView.setAdapter(adapter);
					
			
	}
	
	/**
	 * ����ѡ�е�tab�����ز�ͬ������
	 */
	protected void updateDateForTab(){
		data.removeAll(data);
		if(selectedTabIndex == 2){
			data.addAll(getDoingTabData());
		}else{
			if(selectedTabIndex == 3){
				data.addAll(getDoneTabData());
			}else{
				data.addAll(getRecentTabData());
			}
		}
		adapter.notifyDataSetChanged();

	}
	
	
	
	/**
	 * ����tab���л���"����"Ч��
	 * @param from ��ʼ�ƶ���tab��
	 * @param to �ƶ����tab��
	 */
	private void setTabMoveAnimation(int from,int to){	
		
		if(from != to){
			data.removeAll(data);
			//�ı�tab��ѡ����ɫ
			//��ʼ��tab��ͼ��䰵
			switch(from){
			case 1 :
				recentTabText.setTextColor(Color.BLACK);
				recentTabImage.setVisibility(View.INVISIBLE);
				break;
			case 2 :
				doingTabText.setTextColor(Color.BLACK);
				doingTabImage.setVisibility(View.INVISIBLE);
				break;
			case 3 :
				doneTabText.setTextColor(Color.BLACK);
				doneTabImage.setVisibility(View.INVISIBLE);
			}
			//�ƶ����tab��ͼ�����
			switch(to){
			case 1 :
				recentTabText.setTextColor(getResources().getColor(R.color.background_lightgreen));
				recentTabImage.setVisibility(View.VISIBLE);
				data.addAll(getRecentTabData());
				break;
			case 2 :
				doingTabText.setTextColor(getResources().getColor(R.color.background_lightgreen));
				doingTabImage.setVisibility(View.VISIBLE);
				data.addAll(getDoingTabData());//���»��listView����
				break;
			case 3 :
				doneTabText.setTextColor(getResources().getColor(R.color.background_lightgreen));
				doneTabImage.setVisibility(View.VISIBLE);
				data.addAll(getDoneTabData());//���»��listView����
			}
			
			selectedTabIndex = to;
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
		}
	}

}
