package com.cqupt.ui.teacher;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.cqupt.R;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.model.UserInfo;
import com.cqupt.net.WebService;
import com.cqupt.util.ArrangeStudent;
import com.cqupt.util.XMLParser;
import com.cqupt.util.pageModel.StuListPageModel;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("InflateParams")
public class ArrangeStudentActivity extends IArrangeActivity<UserInfo>{
	
	//contentView popupWindow
	private PopupWindow popupWindow;
	private Spinner deptSpinner;
	private Spinner myListSpinner;
	private ImageButton searchButton;
	private EditText keywordEditText;
	
	private ArrayList<String> deptSpinnerDataList;
	private ArrayList<String> myListSpinnerDataList;

	//---------------------��д����--------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState){
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_arrange_student);
		
		findView();
		loadData();
		setListener();
	}
	
	@Override
	protected void loadData() {
		
		super.loadData();
		deptSpinnerDataList = new ArrayList<String>();
		myListSpinnerDataList = new ArrayList<String>();

		//���Ĭ�ϵ�"ȫѡ"��ѡ��
		deptSpinnerDataList.add(this.getResources().getString(R.string.selectAll));		
	}
	
	@Override
	protected void performSearch() {
		
		String keyword = keywordEditText.getText().toString();
		String dept = deptSpinner.getSelectedItem().toString();
		String myList = myListSpinner.getSelectedItem() == null ?
				"" : myListSpinner.getSelectedItem().toString();
		
		if(myList.equals("����") || myList.equalsIgnoreCase("none")){
			myList = "";
		}
		if(dept.equalsIgnoreCase("all")){
			dept = "ȫѡ";
		}
		if(myList.equalsIgnoreCase("all")){
			myList = "ȫѡ";
		}
		
		popupWindow.dismiss();
		
		if(currentTabIndex == 1){
			new GetStudentsList().execute(keyword,dept,myList);
		}else{
			new GetArrangedStudentListThread().execute(keyword,dept,myList);
		}
		
	}
	
	@Override
	protected void clickConfirmButton() {
		
		new AddStudentThread().execute();
		
	}

	@Override
	protected BaseAdapter getListViewAdapter() {
		
		return new ListViewAdapter();
	}

	@Override
	protected void getArrangedData() {
		
		new GetArrangedStudentListThread().execute("","ȫѡ","");
		
	}
	
	@Override
	protected void deleteData(int position) {
		
		showDeleteStudentDialog(position);
		

	}
	
	@Override
	protected View getListViewHeaderView() {
		
		return LayoutInflater.from(this).inflate(R.layout.layout_arrange_student_table,null);
	}
	
	@Override
	protected void showPopupWindow(){
		
		if(popupWindow == null){
			View contentView = LayoutInflater.from(this)
					.inflate(R.layout.layout_student_retrieve,null);
			
			deptSpinner = (Spinner)contentView.findViewById(R.id.DeptSpinner);
			deptSpinner.setPromptId(R.string.hint_select_depatment);
			new LoadDepartmentListThread().execute();
			
			myListSpinner = (Spinner)contentView.findViewById(R.id.myListSpinner);
			myListSpinner.setPromptId(R.string.my_list);
			new LoadMyListThread().execute();
			
			searchButton = (ImageButton)contentView.findViewById(R.id.searchView);
			searchButton.setOnClickListener(this);
			
			keywordEditText = (EditText)contentView.findViewById(R.id.keyword);
			
			popupWindow = new PopupWindow(this);
			popupWindow.setContentView(contentView);
			popupWindow.setFocusable(true);
			popupWindow.setWidth(LayoutParams.MATCH_PARENT);
			popupWindow.setHeight(LayoutParams.WRAP_CONTENT);
			
			popupWindow.setOnDismissListener(new OnDismissListener() {
				
				@Override
				public void onDismiss() {
					imageView.setBackgroundResource(R.drawable.show);
					isPopupwindowShow = false;
				}
			});
		}
		
		popupWindow.showAsDropDown(contentViewTop);
		
	}
	
//------------------------------�Զ��巽��-----------------------------------
	
	private void showDeleteStudentDialog(final int position){
		
		new AlertDialog.Builder(this)
		.setTitle(R.string.delete_confirm)
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				new DeleteStudentThread().execute(position);
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create()
		.show();
		
	}
	
	

//-------------------------�ڲ���-------------------------------

	class ListViewAdapter extends BaseAdapter{
		
		public class ViewHolder{
		
			public TextView ID;
			public TextView name;
			public TextView stuClass;
			public TextView major;
			public TextView academy;
			
		}

		@Override
		public int getCount() {
			
			return listViewDataList.size();
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
				convertView = LayoutInflater.from(ArrangeStudentActivity.this)
						.inflate(R.layout.layout_arrange_student_table,parent,false);
				
				holder = new ViewHolder();			
				holder.ID = (TextView)convertView.findViewById(R.id.stuID);
				holder.name = (TextView)convertView.findViewById(R.id.stuName);
				holder.stuClass = (TextView)convertView.findViewById(R.id.stuClass);
				holder.major = (TextView)convertView.findViewById(R.id.stuMajor);
				holder.academy = (TextView)convertView.findViewById(R.id.stuAcademy);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
			}
			
			holder.ID.setText(listViewDataList.get(position).getUserID());
			
			if(currentTabIndex == 2){
				
				if(selectedDataList.contains(listViewDataList.get(position))){
					holder.ID.setTextColor(getResources().getColor(R.color.my_red));
				}else{
					holder.ID.setTextColor(Color.BLACK);
				}
				
			}else{
				holder.ID.setTextColor(Color.BLACK);
			}
			
			holder.name.setText(listViewDataList.get(position).getUserName());
			holder.stuClass.setText(listViewDataList.get(position).getUserClass());
			holder.major.setText(listViewDataList.get(position).getUserMajor());
			holder.academy.setText(listViewDataList.get(position).getUserAcademy());
			
			return convertView;
		}
		
	}
	
	/**
	 * ����ѧԺ��Ϣ�ĺ�̨�߳�
	 */
	class LoadDepartmentListThread extends AsyncTask<Void,Void, Integer>{

		@Override
		protected Integer doInBackground(Void... params) {
			
			MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(
					ArrangeStudentActivity.this);
			DBManager db = new DBManager(helper.getConnection());
			
			String school = db.getUserInfo().getUserSchool();
			
			return loadDeptInfoFromWeb(school);
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == SUCCESS){
				deptSpinner.setAdapter(new ArrayAdapter<String>(
						ArrangeStudentActivity.this
						,android.R.layout.simple_spinner_dropdown_item
						,deptSpinnerDataList));
			}
			
		}
		
		private int loadDeptInfoFromWeb(String schoolName){
			
			WebService web = new WebService();
			HashMap<String,String> p = new HashMap<String, String>();
			p.put("schoolName", schoolName);
			
			SoapObject result = web.CallWebService("getDeptInfo", p);
			
			if(result == null){
				return FAIL;
			}
			
			deptSpinnerDataList.addAll(XMLParser.parseDepartmentInfo(result));

			return SUCCESS;
		}		
	}
	
	/**
	 * �����Ѿ��ղص�ѧ���б�ĺ�̨�߳�
	 */
	class LoadMyListThread extends AsyncTask<Void,Void,Integer>{
		
		@Override
		protected Integer doInBackground(Void... params) {
			
			HashMap<String,String> p = new HashMap<String, String>();
			
			p.put("userID",application.getUserID());
			
			WebService web = new WebService();
			SoapObject result = web.CallWebService("getMyListInfo",p);
			
			if(result == null){
				return FAIL;
			}
			
			myListSpinnerDataList.addAll(XMLParser.parseMyListInfo(result));
			
			if(myListSpinnerDataList.size() != 0){
				
				myListSpinnerDataList.add(0,ArrangeStudentActivity.
						this.getResources().getString(R.string.none));
				myListSpinnerDataList.add(1,ArrangeStudentActivity.
						this.getResources().getString(R.string.selectAll));
			}
			
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == SUCCESS){
				myListSpinner.setAdapter(new ArrayAdapter<String>(
							ArrangeStudentActivity.this
							,android.R.layout.simple_spinner_dropdown_item
							,myListSpinnerDataList));
			}
			
		}	
	}
	
	/**
	 * ��ü�������ѧ���б�ĺ�̨�߳�
	 */
	class GetStudentsList extends AsyncTask<String,Void,Integer>{

		@Override
		protected Integer doInBackground(String... params) {
			
			MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(
					ArrangeStudentActivity.this);
			DBManager db = new DBManager(helper.getConnection());
			
			pageModel = new StuListPageModel(params[0], params[1], params[2]
					,application.getUserID()
					,db.getUserInfo().getUserSchool(),taskID);
			
			sumPages = pageModel.getSumPages();
			
			if(sumPages == StuListPageModel.CONNECTION_FAIL){
				return FAIL;
			}
			
			listViewDataList = pageModel.getDataList();
			listViewDataList.removeAll(selectedDataList);
			
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.GONE);
			
			if(result == FAIL){
				Toast.makeText(ArrangeStudentActivity.this, 
						R.string.tip_connection_timeout,Toast.LENGTH_SHORT).show();
			}else{
				
				listViewAdapter.notifyDataSetChanged();
				
			}
		}
		
	}
	
	class GetArrangedStudentListThread extends AsyncTask<String, Void, Integer>{
		
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(String... params) {

			HashMap<String, String> p = new HashMap<String, String>();
			p.put("keyword", params[0]);
			p.put("dept", params[1]);
			p.put("myList", params[2]);
			p.put("userID", application.getUserID());
			p.put("exerciseID", String.valueOf(taskID));
			
			SoapObject result = web.CallWebService("getArrangedStudentList", p);
			
			if(result == null){
				return FAIL;
			}
			
			arrangedDataList.clear();
			arrangedDataList.addAll(XMLParser.ParseUserInfoList(result));
			listViewDataList.clear();
			listViewDataList.addAll(selectedDataList);
			listViewDataList.addAll(arrangedDataList);
			
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			progressBar.setVisibility(View.INVISIBLE);
			
			if(result == SUCCESS){
				
				if(popupWindow != null){
					popupWindow.dismiss();
				}
							
				listViewAdapter.notifyDataSetChanged();
			    
				if(listViewDataList.size() == 0){
					selectedItemBadgeView.setText("");
					selectedItemBadgeView.hide();
				}else{
					selectedItemBadgeView.setText("" + listViewDataList.size());
					selectedItemBadgeView.show();
				}				
				
			}else{
				Toast.makeText(ArrangeStudentActivity.this, 
						R.string.tip_connection_timeout,Toast.LENGTH_SHORT).show();
			}
			
		}
			
	}
	
	//���ѧ���ĺ�̨�߳�
	class AddStudentThread extends AsyncTask<Void, Void, Integer>{

		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			
			
			if(selectedDataList.size() == 0){
				return ArrangeStudent.SUCCESS;
			}
			
			List<String> studentIDs = new ArrayList<String>();
			for(UserInfo userInfo : selectedDataList){
				studentIDs.add(userInfo.getUserID());
			}
			
			int flag = ArrangeStudent.add(taskID, studentIDs);
			
			return flag;
		}	

		@Override
		protected void onPostExecute(Integer result) {
			progressBar.setVisibility(View.INVISIBLE);
			
			if(result == ArrangeStudent.SUCCESS){
				Intent it = new Intent(ArrangeStudentActivity.this,ArrangeTestActivity.class);
				it.putExtra("taskID", taskID);
				startActivity(it);
			}else{				
				Toast.makeText(ArrangeStudentActivity.this,R.string.arrange_student_fail
						,Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	//�Ӱ��ű���ɾ��ѧ���ĺ�̨�߳�
	
	class DeleteStudentThread extends AsyncTask<Integer, Void, Integer>{
		
		private int position;
		
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Integer doInBackground(Integer... params) {
			
			String userID = null;
			if(isAllSelected){
				StringBuilder sb = new StringBuilder();
				
				if(arrangedDataList.size() == 0){//����Ҫ�ӷ�����ɾ������
					return SUCCESS;
				}
				
				for(UserInfo userInfo : arrangedDataList){
					sb.append(userInfo.getUserID() + ",");
				}
				
				userID = sb.deleteCharAt(sb.length() - 1).toString();
			}else{
				position = params[0];
				userID = listViewDataList.get(position).getUserID();
			}
			
			WebService web = new WebService();
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("exerciseID", String.valueOf(taskID));
			p.put("userID", userID);
			
			SoapObject result = web.CallWebService("deleteStudentFromExerciseArrange", p);
			
			if(result == null){
				return FAIL;
			}
			
			return XMLParser.parseBoolean(result).equalsIgnoreCase("true") ? SUCCESS : FAIL;
			
		}

		@Override
		protected void onPostExecute(Integer result) {
			progressBar.setVisibility(View.GONE);

			if(result == FAIL){
				Toast.makeText(ArrangeStudentActivity.this
						, R.string.delete_task_fail, Toast.LENGTH_SHORT).show();
			}else{
				
				if(isAllSelected){
					listViewDataList.clear();
					arrangedDataList.clear();
					selectedDataList.clear();
				}else{
					UserInfo userInfo = listViewDataList.get(position);
					
					arrangedDataList.remove(userInfo);
					listViewDataList.remove(position);
				}
				
				listViewAdapter.notifyDataSetChanged();
				
				int count = listViewDataList.size();
				
				if(count == 0){
					selectedItemBadgeView.setText("");
					selectedItemBadgeView.hide();
				}else{
					selectedItemBadgeView.setText("" + count);
					selectedItemBadgeView.show();
				}
				
				Toast.makeText(ArrangeStudentActivity.this
						, R.string.delete_task_success, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}

}
