package com.cqupt.fragment;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cqupt.R;
import com.cqupt.adapter.ItemsShowingAdapter;
import com.cqupt.application.MyApplication;
import com.cqupt.model.FeedBack;
import com.cqupt.model.ItemsShowingModel;
import com.cqupt.net.WebServiceOperation;
import com.cqupt.ui.teacher.ScoreActivity;
import com.cqupt.util.ShowToastUtil;
import com.cqupt.view.FlowLayout;

import java.util.ArrayList;

public class FeedbackFragment extends Fragment implements OnClickListener{
	
	private static final String TAG_EXTRA_DATA = "data";
	private static final String TAG_USER_ID = "userID";
	private static final String TAG_EXERCISE_ID = "exerciseID";
	
	private TextView mReturnView;
	private FlowLayout mFlowLayout;
	private EditText mScoreEditText;
	private EditText mFeedbackEditText;
	private Button mSaveButton;
	private TextView mTimeView;

	private MyApplication mApplication;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_feedback, container, false);
		mReturnView = (TextView) view.findViewById(R.id.returnView);
		mFlowLayout = (FlowLayout) view.findViewById(R.id.myHorizontalView);
		mScoreEditText = (EditText) view.findViewById(R.id.scoreInput);
		mFeedbackEditText = (EditText) view.findViewById(R.id.feedBackInput);
		mSaveButton = (Button) view.findViewById(R.id.saveButton);
		mTimeView = (TextView) view.findViewById(R.id.timeView);
		
		loadData();
		setListener();
		
		return view;
	}

	@Override
	public void onClick(View v) {
		
	switch (v.getId()) {
	case R.id.returnView:
		getFragmentManager().beginTransaction().hide(this).commit();
		break;

	case R.id.saveButton:
		new MakeFeedBackThread().execute();
		break;
	}	
		
	}
	
	public static FeedbackFragment getInstance(ArrayList<ItemsShowingModel> data,String userID,int exerciseID){
		
		FeedbackFragment fragment = new FeedbackFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable(TAG_EXTRA_DATA, data);
		bundle.putString(TAG_USER_ID, userID);
		bundle.putInt(TAG_EXERCISE_ID, exerciseID);
		fragment.setArguments(bundle);
		
		return fragment;
	}
	
	private void setListener(){
		
		mReturnView.setOnClickListener(this);
		mSaveButton.setOnClickListener(this);
		mFlowLayout.setOnItemViewClickListener(new ScoreActivity.SimpleOnItemViewClickListener(getActivity()));
		
	}
	
	private void loadData(){
		
		mApplication = (MyApplication) getActivity().getApplication();
		Bundle bundle = getArguments();
		@SuppressWarnings("unchecked")
		ArrayList<ItemsShowingModel> data = (ArrayList<ItemsShowingModel>)bundle.getSerializable(TAG_EXTRA_DATA);
		bundle.remove(TAG_EXTRA_DATA);//������ ������ô˷��� ��Ȼapp ����...
		mFlowLayout.setAdapter(new ItemsShowingAdapter(getActivity(),data));
		new GetFeedbackThread().execute();
		
	}
	
	class GetFeedbackThread extends AsyncTask<Void, Void, FeedBack>{

		@Override
		protected FeedBack doInBackground(Void... params) {
			
			Bundle bundle = getArguments();
			
			return WebServiceOperation.getFeedBack(bundle.getString(TAG_USER_ID), bundle.getInt(TAG_EXERCISE_ID));
			
		}

		@Override
		protected void onPostExecute(FeedBack result) {

			if(result == null){
				ShowToastUtil.showConnectionTimeOutToast(getActivity());
			}else{
				
				mScoreEditText.setText(result.getScore());
				mFeedbackEditText.setText(result.getFeedBackContent());
				mTimeView.setText(result.getFeedBackTime());

			}
			
		}
		
	}
	
	class MakeFeedBackThread extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			return WebServiceOperation.makeFeedback(mApplication.getUserID()
					, getArguments().getString(TAG_USER_ID), getArguments().getInt(TAG_EXERCISE_ID)
					, mScoreEditText.getText().toString(), mFeedbackEditText.getText().toString());
		}

		@Override
		protected void onPostExecute(Boolean result) {
			
			if(result){
				ShowToastUtil.ShowSaveSuccessToast(getActivity());
			}else{
				ShowToastUtil.showConnectionTimeOutToast(getActivity());
			}
			
		}
		
	}
	
}
