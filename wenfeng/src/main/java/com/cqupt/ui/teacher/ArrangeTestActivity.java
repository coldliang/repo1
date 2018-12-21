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
import com.cqupt.model.TestDetailForArrange;
import com.cqupt.model.TestType;
import com.cqupt.net.WebService;
import com.cqupt.util.ArrangeTest;
import com.cqupt.util.TestListPageModel;
import com.cqupt.util.XMLParser;
import com.cqupt.util.pageModel.PageModel;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("InflateParams")
public class ArrangeTestActivity extends IArrangeActivity<TestDetailForArrange>{
	
	//popupWindow
	private PopupWindow popupWindow;
	private Spinner testTypeSpinner;
	private Spinner myListSpinner;
	private ImageButton searchButton;
	private EditText titleEditText;
	
	private ArrayList<String> testTypeSpinnerDataList;
	private ArrayList<String> myListSpinnerDataList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_arrange_test);
		
		findView();
		loadData();
		setListener();
	}
	
	@Override
	protected void loadData() {
		
		super.loadData();
		testTypeSpinnerDataList = new ArrayList<String>();
		myListSpinnerDataList = new ArrayList<String>();
		//���Ĭ�ϵ�"ȫѡ"��ѡ��
		testTypeSpinnerDataList.add(this.getResources().getString(R.string.selectAll));
		confirmButton.setText(R.string.finish);
	}

	@Override
	protected void showPopupWindow() {
		
		if(popupWindow == null){
			View contentView = LayoutInflater.from(this)
					.inflate(R.layout.layout_test_retrieve,null);
			
			testTypeSpinner = (Spinner)contentView.findViewById(R.id.testTypeSpinner);
			testTypeSpinner.setPromptId(R.string.hint_select_testType);
			new LoadTestTypeListThread().execute();
			
			myListSpinner = (Spinner)contentView.findViewById(R.id.myListSpinner);
			myListSpinner.setPromptId(R.string.my_list);
			new LoadMyListThread().execute();
			
			searchButton = (ImageButton)contentView.findViewById(R.id.searchView);
			searchButton.setOnClickListener(this);
			
			titleEditText = (EditText)contentView.findViewById(R.id.keyword);
			
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

	@Override
	protected void performSearch() {
		
		String title = titleEditText.getText().toString();
		String testType = testTypeSpinner.getSelectedItem().toString();
		String myList = myListSpinner.getSelectedItem() == null ?
				"" : myListSpinner.getSelectedItem().toString();
		
		if(myList.equals("����") || myList.equals("none")){
			myList = "";
		}
		if(testType.equalsIgnoreCase("all")){
			testType = "ȫѡ";
		}
		if(myList.equalsIgnoreCase("all")){
			myList = "ȫѡ";
		}
		
		popupWindow.dismiss();
		
		if(currentTabIndex == 1){
			new GetTestList().execute(title,testType,myList);
		}else{
			new GetArrangedTestListThread().execute(title,testType,myList);
		}
		
	}

	@Override
	protected void clickConfirmButton() {
		new AddTestThread().execute();
	}

	@Override
	protected BaseAdapter getListViewAdapter() {
		
		return new ListViewAdapter();
	}

	@Override
	protected void getArrangedData() {
		
		new GetArrangedTestListThread().execute("","ȫѡ","");
		
	}

	@Override
	protected void deleteData(int position) {
		
		showDeleteTestDialog(position);
		
	}
	
	@Override
	protected View getListViewHeaderView() {
		
		return LayoutInflater.from(this).inflate(R.layout.layout_arrange_test_table,null);
	}
	
	//-----------------------�Զ��巽��-------------------
	
	private void showDeleteTestDialog(final int position){
		
		new AlertDialog.Builder(this)
		.setTitle(R.string.delete_confirm)
		.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				new DeleteTestThread().execute(position);
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create()
		.show();
		
	}
	
	//--------------�ڲ���-------------------
	
	class ListViewAdapter extends BaseAdapter{
		
		public class ViewHolder{
			
			public TextView title;
			public TextView testType;
			public TextView level;
			public TextView domain;
			public TextView createUser;
			
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
				convertView = LayoutInflater.from(ArrangeTestActivity.this)
						.inflate(R.layout.layout_arrange_test_table,parent,false);
				
				holder = new ViewHolder();
				holder.title = (TextView)convertView.findViewById(R.id.title);
				holder.testType = (TextView)convertView.findViewById(R.id.testType);
				holder.level = (TextView)convertView.findViewById(R.id.level);
				holder.domain = (TextView)convertView.findViewById(R.id.domain);
				holder.createUser = (TextView) convertView.findViewById(R.id.createUser);
				
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
			}			
			
			holder.title.setText(listViewDataList.get(position).getTitle());
			
			if(currentTabIndex == 2){
				
				if(selectedDataList.contains(listViewDataList.get(position))){
					holder.title.setTextColor(getResources().getColor(R.color.my_red));
				}else{
					holder.title.setTextColor(Color.BLACK);
				}
				
			}else{
				holder.title.setTextColor(Color.BLACK);
			}
			
			holder.testType.setText(listViewDataList.get(position).getTestType());
			holder.level.setText(listViewDataList.get(position).getLevel());
			holder.domain.setText(listViewDataList.get(position).getDomain());
			holder.createUser.setText(listViewDataList.get(position).getCreateUser());
			
			return convertView;
		}
		
	}
	
	/**
	 * ����ѧԺ��Ϣ�ĺ�̨�߳�
	 */
	class LoadTestTypeListThread extends AsyncTask<Void,Void, Integer>{

		@Override
		protected Integer doInBackground(Void... params) {	
			
			WebService web = new WebService();
			
			SoapObject result = web.CallWebService("getTestType", null);
			
			if(result == null){
				return FAIL;
			}
			
			List<TestType> list = XMLParser.parseTestType(result);
			for(int i = 0;i < list.size(); i ++){
				testTypeSpinnerDataList.add(list.get(i).getTestTypeName());
			}

			return SUCCESS;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == SUCCESS){
				testTypeSpinner.setAdapter(new ArrayAdapter<String>(
						ArrangeTestActivity.this
						,android.R.layout.simple_spinner_dropdown_item
						,testTypeSpinnerDataList));
			}
			
		}
			
	}
	
	/**
	 * �����Ѿ��ղص�����б�ĺ�̨�߳�
	 */
	class LoadMyListThread extends AsyncTask<Void,Void,Integer>{
		
		@Override
		protected Integer doInBackground(Void... params) {
			
			HashMap<String,String> p = new HashMap<String, String>();
			
			p.put("userID",application.getUserID());
			
			WebService web = new WebService();
			SoapObject result = web.CallWebService("getMyTestList",p);
			
			if(result == null){
				return FAIL;
			}
			
			myListSpinnerDataList.addAll(XMLParser.parseMyListInfo(result));
			
			if(myListSpinnerDataList.size() != 0){
				
				myListSpinnerDataList.add(0,ArrangeTestActivity.
						this.getResources().getString(R.string.none));
				myListSpinnerDataList.add(1,ArrangeTestActivity.
						this.getResources().getString(R.string.selectAll));
			}
			
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == SUCCESS){
				myListSpinner.setAdapter(new ArrayAdapter<String>(
							ArrangeTestActivity.this
							,android.R.layout.simple_spinner_dropdown_item
							,myListSpinnerDataList));
			}
			
		}	
	}
	
	/**
	 * ��ü�������ѧ���б�ĺ�̨�߳�
	 */
	class GetTestList extends AsyncTask<String,Void,Integer>{

		@Override
		protected Integer doInBackground(String... params) {
			
			pageModel = new TestListPageModel(application.getUserID(),params[0]
					,params[1], params[2], 1, 20, String.valueOf(taskID));
			
			sumPages = pageModel.getSumPages();
			
			if(sumPages == PageModel.CONNECTION_FAIL){
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
				Toast.makeText(ArrangeTestActivity.this, 
						R.string.tip_connection_timeout,Toast.LENGTH_SHORT).show();
			}else{
				
				listViewAdapter.notifyDataSetChanged();
				
			}
		}
		
	}
	
	class GetArrangedTestListThread extends AsyncTask<String, Void, Integer>{
		
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected Integer doInBackground(String... params) {
			
			HashMap<String, String> p = new HashMap<String, String>();
			p.put("title", params[0]);
			p.put("testType", params[1]);
			p.put("myList", params[2]);
			p.put("userID", application.getUserID());
			p.put("exerciseID", String.valueOf(taskID));
			
			SoapObject result = web.CallWebService("getArrangedTestList", p);
			
			if(result == null){
				return FAIL;
			}
			
			arrangedDataList.clear();
			arrangedDataList.addAll(XMLParser.parseTestDetailForArrange(result));
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
				
				if(listViewDataList.size() == 0){
					selectedItemBadgeView.setText("");
					selectedItemBadgeView.hide();
				}else{
					selectedItemBadgeView.setText("" + listViewDataList.size());
					selectedItemBadgeView.show();
				}	
				
				listViewAdapter.notifyDataSetChanged();
			}else{
				Toast.makeText(ArrangeTestActivity.this, 
						R.string.tip_connection_timeout,Toast.LENGTH_SHORT).show();
			}
			
		}
			
	}
	
	//���ѧ���ĺ�̨�߳�
	class AddTestThread extends AsyncTask<Void, Void, Integer>{

		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Integer doInBackground(Void... params) {
			
			if(selectedDataList.size() == 0){
				return ArrangeTest.SUCCESS;
			}
			
			List<Integer> testIDs = new ArrayList<Integer>();
			
			for(TestDetailForArrange t : selectedDataList){
				testIDs.add(t.getTestID());
			}
			
			int flag = ArrangeTest.add(taskID, testIDs);
			
			return flag;
		}	

		@Override
		protected void onPostExecute(Integer result) {
			progressBar.setVisibility(View.INVISIBLE);
			
			if(result == ArrangeTest.SUCCESS){

				Intent it = new Intent(ArrangeTestActivity.this,TeacherMainActivity.class);
				startActivity(it);
				finish();
				
			}else{				
				Toast.makeText(ArrangeTestActivity.this,R.string.arrange_test_fail
						,Toast.LENGTH_SHORT).show();
			}
		}
		
	}
		
	//�Ӱ��ű���ɾ����Ŀ�ĺ�̨�߳�
	
	class DeleteTestThread extends AsyncTask<Integer, Void, Integer>{
		
		private int position;
	
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
		}
		
		@Override
		protected Integer doInBackground(Integer... params) {
			
			
			String testID = null;
			if(isAllSelected){
				StringBuilder sb = new StringBuilder();
				
				//����Ҫ�ӷ�����ɾ������
				if(arrangedDataList.size() == 0){
					return SUCCESS;
				}
				
				for(TestDetailForArrange t : arrangedDataList){
					sb.append(t.getTestID() + ",");
				}
				
				testID = sb.deleteCharAt(sb.length() - 1).toString();
			}else{
				position = params[0];
				testID = String.valueOf(listViewDataList.get(position).getTestID());
			}

			HashMap<String, String> p = new HashMap<String, String>();
			p.put("exerciseID", String.valueOf(taskID));
			p.put("testID", testID);
			
			SoapObject result = web.CallWebService("deleteTestFromExerciseArrange", p);
			
			if(result == null){
				return FAIL;
			}
			
			int flag = XMLParser.parseBoolean(result).equalsIgnoreCase("true") ? SUCCESS : FAIL;
			
			return flag;
		}

		@Override
		protected void onPostExecute(Integer result) {
			progressBar.setVisibility(View.GONE);
			
			if(result == FAIL){
				Toast.makeText(ArrangeTestActivity.this
						, R.string.delete_task_fail, Toast.LENGTH_SHORT).show();
			}else{
				
				if(isAllSelected){
					listViewDataList.clear();
					arrangedDataList.clear();
					selectedDataList.clear();
				}else{
					TestDetailForArrange t = listViewDataList.get(position);
					arrangedDataList.remove(t);
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
				
				Toast.makeText(ArrangeTestActivity.this
						, R.string.delete_task_success, Toast.LENGTH_SHORT).show();
			}
			
		}
		
	}
	
	
}
