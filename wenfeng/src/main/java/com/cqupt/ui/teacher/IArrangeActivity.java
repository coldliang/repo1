package com.cqupt.ui.teacher;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.db.DBManager;
import com.cqupt.db.MySQLiteOpenHelper;
import com.cqupt.net.WebService;
import com.cqupt.util.pageModel.PageModel;
import com.readystatesoftware.viewbadger.BadgeView;

import java.util.ArrayList;
import java.util.List;

public abstract class IArrangeActivity<T> extends Activity implements OnClickListener{
	
	protected final static int FAIL = -1;
	protected final static int SUCCESS = 1;

	private TextView returnView;
	private TextView tabUnselectedText;
	private TextView tabSelectedText;
	private LinearLayout tabUnselected;
	private LinearLayout tabSelected;
	private ImageView tabUnselectedImage;
	private ImageView tabSelectedImage;
	protected ProgressBar progressBar;

	// contentView
	private RelativeLayout contentView;
	protected LinearLayout contentViewTop;
	protected ImageView imageView;
	private ListView listView;
	protected Button confirmButton;
	protected BaseAdapter listViewAdapter;
	protected PageModel<T> pageModel;
	protected BadgeView selectedItemBadgeView;
	
	protected List<T> listViewDataList;
	protected List<T> selectedDataList;//ѡ����Ҫ���ŵ�����(��ʱ��δ��ӵ�������)
	protected List<T> arrangedDataList;//�Ѿ����Ź�������(�ӷ��������)
	protected boolean isPopupwindowShow = false;
	protected boolean isAllSelected;//��¼�Ƿ���ȫѡ
	protected int currentTabIndex = 2;// ��¼��ǰѡ�е�tab 1-2
	protected int sumPages;//��������ѧ���ܸ���
	protected int taskID;
	protected WebService web = new WebService();
	protected DBManager db;
	protected MyApplication application;
	
	abstract protected void showPopupWindow();
	abstract protected void performSearch();
	abstract protected void clickConfirmButton();
	abstract protected BaseAdapter getListViewAdapter();
	abstract protected void getArrangedData();
	abstract protected void deleteData(int position);
	abstract protected View getListViewHeaderView();

	// ---------------------��д����--------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.includeSearch:
			if (!isPopupwindowShow) {
				showPopupWindow();
				imageView.setBackgroundResource(R.drawable.hide);
			}
			break;
		case R.id.searchView:
			progressBar.setVisibility(View.VISIBLE);
			performSearch();
			break;
		case R.id.returnView:
			onBackPressed();
			break;
		case R.id.tab_selected:
			moveTab(currentTabIndex, 2);
			break;
		case R.id.tab_unselected:
			moveTab(currentTabIndex, 1);
			break;
		case R.id.confirmButton:
			clickConfirmButton();
			break;
		}

	}

	// ------------------------------�Զ��巽��-----------------------------------

	protected void findView() {

		returnView = (TextView) findViewById(R.id.returnView);
		tabUnselectedText = (TextView) findViewById(R.id.tab_unselected_text);
		tabSelectedText = (TextView) findViewById(R.id.tab_selected_text);
		tabUnselected = (LinearLayout) findViewById(R.id.tab_unselected);
		tabSelected = (LinearLayout) findViewById(R.id.tab_selected);
		tabUnselectedImage = (ImageView) findViewById(R.id.tab_unselected_image);
		tabSelectedImage = (ImageView) findViewById(R.id.tab_selected_image);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		contentView = (RelativeLayout) findViewById(R.id.content);
		contentViewTop = (LinearLayout) contentView.findViewById(R.id.includeSearch);
		imageView = (ImageView) contentView.findViewById(R.id.image);
		confirmButton = (Button) contentView.findViewById(R.id.confirmButton);
		listView = (ListView) contentView.findViewById(R.id.listView);

	}

	protected void loadData() {

		application = (MyApplication) getApplication();
		taskID = getIntent().getIntExtra("taskID", -1);
		db = new DBManager(MySQLiteOpenHelper.getInstance(this).getConnection());
		// ��ʼ��contentView
		listViewAdapter = getListViewAdapter();
		listViewDataList = new ArrayList<T>();
		selectedDataList = new ArrayList<T>();
		arrangedDataList = new ArrayList<T>();

		View titleView = getListViewHeaderView();
		listView.addHeaderView(titleView);
		listView.setAdapter(listViewAdapter);

		selectedItemBadgeView = new BadgeView(this, tabSelectedText);
		
		getArrangedData();

	}

	protected void setListener() {

		contentViewTop.setOnClickListener(this);
		confirmButton.setOnClickListener(this);
		returnView.setOnClickListener(this);
		tabSelected.setOnClickListener(this);
		tabUnselected.setOnClickListener(this);

		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				
				if(currentTabIndex == 1){
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
							&& pageModel.getLastItemIndex() == listViewDataList
									.size()
							&& pageModel.getCurrentPage() < sumPages) {
						// ������һҳ
						new AppendListViewData().execute();
					}
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				if(currentTabIndex == 1){
					if (pageModel != null) {
						pageModel.setLastItemIndex(firstVisibleItem
								+ visibleItemCount - 1);
					}
				}
				
			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				isAllSelected = view.getTag() == null;//���header��Ϊȫѡ

				//ִ����Ӳ���
				if(currentTabIndex == 1){
					
					if(isAllSelected){
						selectedDataList.addAll(listViewDataList);
					}else{
						selectedDataList.add(listViewDataList.get(position - 1));
					}
					
					if(!selectedItemBadgeView.isShown()){
						selectedItemBadgeView.show();
					}
					
					int count = selectedDataList.size() + arrangedDataList.size();
					selectedItemBadgeView.setText("" + count);					
					
					if(isAllSelected){
						listViewDataList.clear();
					}else{
						listViewDataList.remove(position - 1);
					}
					
					listViewAdapter.notifyDataSetChanged();
					
				}else{//ִ��ɾ������
					
					if(isAllSelected){
						deleteData(0);						
					}else{
						T item = listViewDataList.get(position - 1);
						
						if(selectedDataList.contains(item)){
							selectedDataList.remove(item);
							
							listViewDataList.remove(position - 1);
							listViewAdapter.notifyDataSetChanged();
							
							if(listViewDataList.size() == 0){
								selectedItemBadgeView.setText("");
								selectedItemBadgeView.hide();
							}else{
								selectedItemBadgeView.setText("" + listViewDataList.size());
								selectedItemBadgeView.show();
							}
						}else{
							deleteData(position - 1);
						}				
					}							
										
				}
				
			}
		});
	}

	private void moveTab(int from, int to) {

		if (from == 1 && to == 2) {
			
			tabSelectedText.setTextColor(getResources().getColor(
					R.color.background_lightgreen));
			tabSelectedImage.setVisibility(View.VISIBLE);
			tabUnselectedText.setTextColor(getResources().getColor(
					android.R.color.black));
			tabUnselectedImage.setVisibility(View.INVISIBLE);
		
			getArrangedData();
			
		} else {
			if (from == 2 && to == 1) {
				tabUnselectedText.setTextColor(getResources().getColor(
						R.color.background_lightgreen));
				tabUnselectedImage.setVisibility(View.VISIBLE);
				tabSelectedText.setTextColor(getResources().getColor(
						android.R.color.black));
				tabSelectedImage.setVisibility(View.INVISIBLE);
				
				listViewDataList.clear();
				listViewAdapter.notifyDataSetChanged();
			}
		}

		currentTabIndex = to;

	}

	// -------------------------�ڲ���-------------------------------

	private class AppendListViewData extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {

			List<T> tempList = pageModel.nextPage();

			if (tempList.size() == 0) {
				return FAIL;
			}

			listViewDataList.addAll(tempList);

			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {

			if (result == SUCCESS) {
				listViewAdapter.notifyDataSetChanged();
				listView.setSelection(pageModel.getLastItemIndex());
			}
		}
	}

}
