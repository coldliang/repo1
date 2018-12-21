package com.cqupt.ui.teacher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.model.FeedBack;
import com.cqupt.model.ScoreTestItem;
import com.cqupt.net.WebService;
import com.cqupt.ui.common.CommentListActivity;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.ConfigUtils;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.util.XMLParser;
import com.readystatesoftware.viewbadger.BadgeView;

import org.ksoap2.serialization.SoapObject;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ScoreTestListActivity extends Activity implements OnClickListener{
	
	private MyApplication application;
	
	private TextView returnView;
	private TextView userNameView;
	private TextView feedBackView;
	private TextView objectiveCountView;
	private TextView rightCountView;
	private TextView accuracyView;
	private ListView listView;
	private LinearLayout downloadView;
	private EditText scoreInputView;
	private Button scoreButton;
	private ImageButton commentView;
	private BadgeView downloadBadgeView;
	private GestureDetector gestureDetector;
	
	private int taskID;
	private List<String> userNames;//����ѧ����ID
	private int currentPage;//��ǰҳ��
	private boolean scoreChanged = false;//�Ƿ�ı��˷���
	private FeedBack feedBack;
	private List<ScoreTestItem> listViewData;
	private ListViewAdapter listViewAdapter;
	
	
	protected void onCreate(Bundle savedInstanceState){
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_score_testlist);
		
		findView();
		loadData();
		setListener();
		
	}

	private void findView() {
		
		returnView = (TextView) findViewById(R.id.returnView);
		userNameView = (TextView) findViewById(R.id.userName);
		feedBackView = (TextView) findViewById(R.id.feedBackView);
		objectiveCountView = (TextView) findViewById(R.id.objectiveQuestionCount);
		rightCountView = (TextView) findViewById(R.id.rightAnswerCount);
		accuracyView = (TextView) findViewById(R.id.accuracy);
		listView = (ListView) findViewById(R.id.listView);
		downloadView = (LinearLayout) findViewById(R.id.downloadView);
		scoreInputView = (EditText) findViewById(R.id.scoreInput);
		scoreButton = (Button) findViewById(R.id.saveScore);
		commentView = (ImageButton) findViewById(R.id.commentButton);
		
	}
	
	private void loadData(){
		
		Intent it = getIntent();
		taskID = it.getIntExtra("taskID", 0);
		String names = it.getStringExtra("userNames");
		String[] s = names.split(",");
		userNames = Arrays.asList(s);
		
		currentPage = it.getIntExtra("currentPage", 1);
		
		application = (MyApplication)getApplication();
		downloadBadgeView = new BadgeView(this, downloadView);
		
		gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				
				float startX = e1.getX();
				float endX = e2.getX();
				
				//���󻬶�����һҳ
				if(startX - endX > ConfigUtils.FLING_MIN_DISTANCE && Math.abs(velocityX) > ConfigUtils.FLING_MIN_VELOCITY
						&& currentPage < userNames.size()){
					pageMove(currentPage,currentPage + 1);					
				}else if(endX - startX > 200 && velocityX > ConfigUtils.FLING_MIN_VELOCITY
						&& currentPage > 1){//���һ�������һҳ
					pageMove(currentPage,currentPage - 1);					
				}
				
				return super.onFling(e1, e2, velocityX, velocityY);
			}
			
			
			
		});

		
		new GetDataThread().execute();
		new GetListViewDataThread().execute();
		
	}
	
	private void pageMove(int from , int to){
		
		currentPage = to;
		new GetDataThread().execute();
		new GetListViewDataThread().execute();
		
	}
	
	private void setListener(){
		
		returnView.setOnClickListener(this);
		feedBackView.setOnClickListener(this);
		downloadView.setOnClickListener(this);
		scoreButton.setOnClickListener(this);
		commentView.setOnClickListener(this);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent intent = new Intent(ScoreTestListActivity.this
						,TeacherDoneTestActivity.class);
				intent.putExtra("ID", taskID);
				intent.putExtra("studentUserID", userNames.get(currentPage - 1).split("\\|")[0]);
				startActivity(intent);
				
			}
		});
		
		listView.setOnTouchListener(new OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
		
		
	}
	
	@Override
	public void onBackPressed() {
		
		Intent it = new Intent();
		it.putExtra("scoreChanged", scoreChanged);
		setResult(Activity.RESULT_OK,it);
		super.onBackPressed();
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		case R.id.returnView : onBackPressed();break;
		case R.id.feedBackView : 
			
			if(CheckNetwork.isConnectingToInternet(this)){
				feedBackView.setEnabled(false);
				new GetFeedbackThread().execute();			
			}else{
				ShowToastUtil.showNetworkUnavailableToast(this);
			}
			
			break;
		case R.id.downloadView : 
			Intent intent = new Intent(this,StudentUploadedFilesActivity.class);
			intent.putExtra("taskID", taskID);
			intent.putExtra("userID", userNames.get(currentPage - 1).split("\\|")[0]);
			startActivity(intent);
			break;
		case R.id.saveScore : 
			
			if(CheckNetwork.isConnectingToInternet(this)){
				
				if(isScoreInputIlligal(scoreInputView.getText().toString().trim())){
					new ScoreThread().execute();
				}else{
					Toast.makeText(this, R.string.score_range, Toast.LENGTH_SHORT).show();
				}
				
			}else{
				Toast.makeText(this, R.string.tip_network_unavailable, Toast.LENGTH_SHORT).show();
			}
			
			
			break;
			
		case R.id.commentButton :
			Intent it = new Intent(this,CommentListActivity.class);
			it.putExtra("ID",taskID);
			it.putExtra("type", 1);
			startActivity(it);
			break;
		}
		
	};
	
	private boolean isScoreInputIlligal(String input){
		
		Integer score;
		
		try{
			score = Integer.parseInt(input.trim());
		}catch(NumberFormatException e){
			return false;
		}
		
		return (score >= 0 && score <= 100);
		
	}
	
	@SuppressLint("InflateParams")
	private void showFeedbackDialog(){
		
		View contentView = LayoutInflater.from(this)
				.inflate(R.layout.layout_feedback_edittext, null);
		final EditText editText = (EditText) contentView.findViewById(R.id.editText);
		
		editText.setText(feedBack.getFeedBackContent());
		new AlertDialog.Builder(this)
		.setView(contentView)
		.setTitle(feedBack.getFeedBackTime())
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				String feedback = editText.getText().toString().trim();
				
				if(TextUtils.isEmpty(feedback)){
					ShowToastUtil.showEmptyInputToast(ScoreTestListActivity.this);
				}else{
					new MakeFeedbackThread().execute(feedback);
				}
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.setCancelable(false)
		.create()
		.show();
		
	}
	
	//---------------------�Զ�����-----------------------------
	
	/**
	 * ȡ��listview���ݵĺ�̨�߳�
	 */
	private class GetListViewDataThread extends AsyncTask<Void,Void,Integer>{
		
		private static final int FAIL = -1;
		private static final int SUCCESS = 0;

		@Override
		protected Integer doInBackground(Void... params) {
			
			WebService web = new WebService();
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("taskID",String.valueOf(taskID));
			p.put("userID", userNames.get(currentPage - 1).split("\\|")[0]);
			
			SoapObject result = web.CallWebService("getScoreTestList",p);
			
			if(result == null){
				return FAIL;
			}
			
			listViewData = XMLParser.parseScoreTestItem(result);
			
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == SUCCESS){
				if(listViewAdapter == null){
					listViewAdapter = new ListViewAdapter();
					listView.setAdapter(listViewAdapter);
				}else{
					listViewAdapter.notifyDataSetChanged();
				}
			}
			
		}
		
	}
	
	//�������ҳ�����ݵĺ�̨�߳�
	class GetDataThread extends AsyncTask<Void, Void, Void>{
		
		private int sumCount;
		private int rightCount;
		private int attachmentCount;
		private String score;

		@Override
		protected Void doInBackground(Void... params) {
			WebService web = new WebService();
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("exerciseID", String.valueOf(taskID));
			p.put("type", "2");
			
			SoapObject result = web.CallWebService("getItemCountByExerciseID", p);
			
			if(result != null){
				sumCount = XMLParser.parseInt(result);
			}else{
				return null;
			}
			
			
			p.clear();
			p.put("exerciseID", String.valueOf(taskID));
			p.put("userID",userNames.get(currentPage - 1).split("\\|")[0]);
			
			result = web.CallWebService("getRightObjectAnswerCount", p);
			
			if(result != null){
				rightCount = XMLParser.parseInt(result);
			}else{
				return null;
			}
			
			p.clear();
			p.put("taskID", String.valueOf(taskID));
			
			
			result = web.CallWebService("getFeedback", p);
			
			if(result != null){
				score = XMLParser.parseFeedBack(result).getScore();
			}
			
			p.clear();
			p.put("exerciseID", String.valueOf(taskID));
			p.put("userID", userNames.get(currentPage - 1).split("\\|")[0]);
			p.put("type", "3");
			p.put("isRead", "0");//0��ʾȫ���ĸ���
			
			result = web.CallWebService("getAttachmentCount", p);
			
			if(result != null){
				attachmentCount = XMLParser.parseInt(result);
			}
			
			return null;
			
		}

		@Override
		protected void onPostExecute(Void result) {
			userNameView.setText(userNames.get(currentPage - 1));
			objectiveCountView.setText(String.valueOf(sumCount));
			rightCountView.setText(String.valueOf(rightCount));
			
			double accuracy = sumCount == 0 ? 0 : rightCount*1.0f/sumCount;
			accuracyView.setText(new DecimalFormat("#.##%").format(accuracy));
			
			scoreInputView.setText(score);
			
			if(attachmentCount != 0){
				downloadBadgeView.setText(String.valueOf(attachmentCount));
				downloadBadgeView.show();
			}else{
				downloadBadgeView.hide();
			}
			
		}
		
	}
	
	class ListViewAdapter extends BaseAdapter{
		
		public final class ViewHolder{
			
			public TextView titleView;
			public TextView categoryView;
			public TextView levelView;
			public TextView objectCountView;
			public TextView subjectCountView;
			
		}

		@Override
		public int getCount() {
			
			return listViewData.size();
		}

		@Override
		public Object getItem(int position) {
			
			return null;
		}

		@Override
		public long getItemId(int position) {
			
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			ViewHolder holder = null;
			
			if(convertView == null){
				
				convertView = LayoutInflater.from(ScoreTestListActivity.this)
						.inflate(R.layout.layout_score_test_item, parent,false);
				
				holder = new ViewHolder();
				holder.titleView = (TextView) convertView.findViewById(R.id.title);
				holder.categoryView = (TextView) convertView.findViewById(R.id.category);
				holder.levelView = (TextView) convertView.findViewById(R.id.level);
				holder.objectCountView = (TextView) convertView.findViewById(
						R.id.objectQuestionCount);
				holder.subjectCountView = (TextView) convertView.findViewById(
						R.id.subjectiveQuestionCount);
				
				convertView.setTag(holder);
				
			}else{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.titleView.setText(listViewData.get(position).getTitle());
			holder.categoryView.setText(listViewData.get(position).getCategory());
			holder.levelView.setText(listViewData.get(position).getLevel());
			holder.objectCountView.setText(listViewData.get(position).getObjectCount());
			holder.subjectCountView.setText(listViewData.get(position).getSubjectCount());
			
			return convertView;
			
		}
		
	}
	
	//���ֵĺ�̨�߳�
	class ScoreThread extends AsyncTask<Void, Void, Integer>{
		
		private final static int FAIL = -1;
		private final static int SUCCESS = 1;
		
		@Override
		protected Integer doInBackground(Void... params) {
			WebService web = new WebService();
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("taskID", String.valueOf(taskID));
			p.put("userID", userNames.get(currentPage - 1).split("\\|")[0]);
			p.put("score", scoreInputView.getText().toString());
			
			SoapObject result = web.CallWebService("manualScore", p);
			
			if(result == null){
				return FAIL;
			}
			
			return XMLParser.parseBoolean(result).equalsIgnoreCase("true") ? SUCCESS : FAIL;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if(result == FAIL){
				Toast.makeText(ScoreTestListActivity.this, R.string.tip_connection_timeout
						, Toast.LENGTH_SHORT).show();
			}else{
				scoreChanged = true;
				Toast.makeText(ScoreTestListActivity.this, R.string.save_success
						,Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	class MakeFeedbackThread extends AsyncTask<String, Void, Integer>{
		
		private static final int SUCCESS = 1;
		private static final int FAIL = -1;

		@Override
		protected Integer doInBackground(String... params) {

			String feedback = params[0];
			
			WebService web = new WebService();
			HashMap<String, String> p = new HashMap<String, String>();
			
			p.put("content", feedback);
			p.put("fromUserID", application.getUserID());
			p.put("toUserID", userNames.get(currentPage - 1).split("\\|")[0]);
			p.put("exerciseID", String.valueOf(taskID));
			
			SoapObject result = web.CallWebService("makeFeedback", p);
			
			if(result == null){
				return FAIL;
			}
			
			return XMLParser.parseBoolean(result).equalsIgnoreCase("true") ? SUCCESS : FAIL;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == FAIL){
				ShowToastUtil.showConnectionTimeOutToast(ScoreTestListActivity.this);
			}else{
				Toast.makeText(ScoreTestListActivity.this
						, R.string.make_feedback_success, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	class GetFeedbackThread extends AsyncTask<Void, Void, Integer>{
		
		private static final int SUCCESS = 1;
		private static final int FAIL = -1;

		@Override
		protected Integer doInBackground(Void... params) {
			WebService web = new WebService();
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("taskID", String.valueOf(taskID));
			p.put("userID", userNames.get(currentPage - 1).split("\\|")[0]);
			
			SoapObject result = web.CallWebService("getFeedback", p);
			
			if(result == null){
				return FAIL;
			}
			
			feedBack = XMLParser.parseFeedBack(result);
			
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			feedBackView.setEnabled(true);
			if(result == SUCCESS){
				showFeedbackDialog();
			}else{
				ShowToastUtil.showConnectionTimeOutToast(ScoreTestListActivity.this);
			}
			
		}
		
	}
	
	
}
