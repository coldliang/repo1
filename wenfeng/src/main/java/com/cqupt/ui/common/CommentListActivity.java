package com.cqupt.ui.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.Comment;
import com.cqupt.net.WebService;
import com.cqupt.util.CheckNetwork;
import com.cqupt.util.pageModel.CommentListPageModel;

import org.ksoap2.serialization.SoapObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class CommentListActivity extends Activity {
	
	private MyApplication application;
	
	private ArrayList<Comment> commentList = new ArrayList<Comment>();//�����б����ݼ�
	private CommentListAdapter commentListAdapter = new CommentListAdapter();
	private RelativeLayout top2;
    private TextView returnView;
    private ListView commentlistView;
    private EditText commentView;
    private TextView emptyView;
    private Button reloadButton;
    private Button sendButton;
    private ProgressBar progress;
    private int ID;
    private int type;
    private int sumPages = 0;//��ҳ��
    private CommentListPageModel pageModel;
    
    //-------------��д����-----------------------------
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_comment);
		findView();
		loadData();
		setListener();
		
	}
	
	//-------------------�Զ��巽��-----------------------------
	
	private void findView(){
		
		returnView = (TextView)findViewById(R.id.returnView);
		commentlistView = (ListView)findViewById(R.id.commentList);
		commentView = (EditText)findViewById(R.id.comment);
		emptyView = (TextView)findViewById(R.id.emptyView);
		top2 = (RelativeLayout)findViewById(R.id.top2);
		reloadButton = (Button)findViewById(R.id.reloadButton);
		sendButton = (Button)findViewById(R.id.sendButton);
		progress = (ProgressBar)findViewById(R.id.progressBar1);
	}
	
	private void loadData(){
		
		application = (MyApplication)getApplication();
		
		ID = getIntent().getIntExtra("ID",0);//ȡ��ǰһ��ҳ�洫����ID
		type = getIntent().getIntExtra("type",0);//ȡ����������,0��Ŀ����,1��������
		commentlistView.setEmptyView(emptyView);
		pageModel = new CommentListPageModel(type, ID, 1, 10);
		if(!CheckNetwork.isConnectingToInternet(this)){
			top2.setVisibility(View.VISIBLE);
		}else{
			progress.setVisibility(View.VISIBLE);
			new LoadDataThread().execute();//���������б�
		}
		
	}
	
	private void setListener(){
		returnView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		commentlistView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(pageModel.getLastItemIndex() == commentList.size()
						&&pageModel.getCurrentPage()<sumPages
						&&scrollState == OnScrollListener.SCROLL_STATE_IDLE){
						//������һҳ����
					if(CheckNetwork.isConnectingToInternet(CommentListActivity.this)){
						new AppendDataThread().execute(0);	
					}
						
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				pageModel.setLastItemIndex(firstVisibleItem + visibleItemCount);
				
			}
		});
		reloadButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(CheckNetwork.isConnectingToInternet(CommentListActivity.this)){
					top2.setVisibility(View.INVISIBLE);
					progress.setVisibility(View.VISIBLE);
					new LoadDataThread().execute();
				}
				
			}
		});
		
		sendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(!CheckNetwork.isConnectingToInternet(CommentListActivity.this)){
					Toast.makeText(CommentListActivity.this,
							R.string.tip_network_unavailable,Toast.LENGTH_SHORT).show();
				}else{
					
					//δ��¼�û����ܷ�������
					if(application.getUserID() == null){
						showLoginCheckDialog();
					}else{
						if("".equals(commentView.getText().toString().trim())){
							Toast.makeText(CommentListActivity.this,
									R.string.empty_comment_content,Toast.LENGTH_LONG).show();
						}else{
							new MakeCommentThread().execute();
						}
					}
					
					
				}
				
			}
		});
		
	}
	
	//����δ��¼���û����ڷ�������ʱ��ʾ�Ƿ���Ҫǰ����¼ҳ��
	private void showLoginCheckDialog(){
		
		new AlertDialog.Builder(this)
		.setMessage(R.string.tip_login_user_can_make_comment)
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				Intent intent = new Intent(CommentListActivity.this,LoginActivity.class);
				intent.putExtra("type", LoginActivity.ACTIVITY_TYPE_COMMENT);
				startActivity(intent);
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create()
		.show();
		
	}
	
	//---------------�ڲ���-----------------------
	
	
	/**
	 * �����б�������
	 */
	class CommentListAdapter extends BaseAdapter{
		
		private class ViewHolder {
			public TextView userView;
			public TextView contentView;
			public TextView timeView;
			//public ImageView imageView;
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return commentList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return commentList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = LayoutInflater.from(CommentListActivity.this)
						.inflate(R.layout.layout_comment,parent,false);
				holder.userView = (TextView)convertView.findViewById(R.id.user);
				holder.contentView = (TextView)convertView.findViewById(R.id.content);
				holder.timeView = (TextView)convertView.findViewById(R.id.time);
				//holder.imageView = (ImageView)convertView.findViewById(R.id.imageView1);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
			}
			holder.userView.setText(commentList.get(position).getUserName());
			holder.contentView.setText(commentList.get(position).getContent());
			holder.timeView.setText(commentList.get(position).getTime());
			return convertView;
		}
		
	}
	
	/**
	 * ���μ������ݵĺ�̨�߳�
	 */
	class LoadDataThread extends AsyncTask<Void, String, Integer>{
		
		@Override
		protected Integer doInBackground(Void... params) {
			sumPages = pageModel.getSumPages();
			if(sumPages > 0){
				commentList = pageModel.getCommentList();
			}
			return sumPages;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			progress.setVisibility(View.GONE);
			if(sumPages == CommentListPageModel.CONNECTION_FAIL){
				Toast.makeText(CommentListActivity.this,
						R.string.tip_connection_timeout, Toast.LENGTH_SHORT).show();
			}else{
				commentlistView.setAdapter(commentListAdapter);
			}
			
		}
		
	} 
	
	/**
	 * ��̬������ݵĺ�̨�߳�
	 */
	class AppendDataThread extends AsyncTask<Integer, String, String>{


		@Override
		protected String doInBackground(Integer... params) {
			
			commentList.addAll(pageModel.nextPage());
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			
			commentListAdapter.notifyDataSetChanged();
			commentlistView.setSelection(pageModel.getLastItemIndex());
		}
		
		
	}
	
	/**
	 * �������ۺ�̨�߳�
	 */
	class MakeCommentThread extends AsyncTask<Void,String,Integer>{
		
		private final int CONNECTION_FAIL = -1;
		private final int SUCCESS = 1;

		@Override
		protected Integer doInBackground(Void... params) {
			
			WebService web = new WebService();
			String content = commentView.getText().toString();//ȡ�÷�������
			//��ʽ������ʱ��
			SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			String time = format.format(date);
			
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("ID", String.valueOf(ID));
			p.put("type", String.valueOf(type));
			p.put("content", content);
			p.put("userID",application.getUserID());
			p.put("discussTime", time);
			SoapObject result = web.CallWebService("addComment", p);
			if(result == null){
				return CONNECTION_FAIL;
			}
			//���·��ص�һҳ,�����ֲŷ��������λ���б���λ
			DBManager db = new DBManager(MySQLiteOpenHelper.getInstance(CommentListActivity.this)
					.getConnection());
			Comment comment = new Comment();
			comment.setContent(content);
			comment.setTime(time);
			comment.setUserID(application.getUserID());
			comment.setUserName(db.getUserInfo().getUserName());
			commentList.add(0, comment);
			
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == null){
				Toast.makeText(CommentListActivity.this, 
						R.string.tip_connection_timeout,Toast.LENGTH_SHORT).show();
			}else{
				commentView.setText("");
				//���������
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(commentView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
				commentListAdapter.notifyDataSetChanged();
				commentlistView.setSelection(0);//���ص�һ����¼(���ŷ��������)
			}
		}
	}
	
}
