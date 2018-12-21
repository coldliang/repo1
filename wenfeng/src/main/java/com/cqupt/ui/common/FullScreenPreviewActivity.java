package com.cqupt.ui.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.cqupt.R;
import com.cqupt.application.MyApplication;
import com.cqupt.fragment.ImageViewFragment;
import com.cqupt.fragment.VideoViewFragment;
import com.cqupt.util.FileDownloadAndroidManager;
import com.cqupt.util.FileUtil;
import com.cqupt.view.ViewPagerForPhotoView;

import java.io.File;
import java.util.ArrayList;

public class FullScreenPreviewActivity extends FragmentActivity {
	
	public static final String INTENT_URLS = "urls";
	public static final String INTENT_ORIGIN_NAMES = "originNames";
	public static final String INTENT_FRAGMENT_TYPE = "type";
	public static final String INTENT_ITEM_POSITION = "position";
	
	private ViewPagerForPhotoView mViewPager;
	private TextView mNumberView;
	private ImageView mDownloadView;
	
	private ArrayList<String> mUrls;
	private ArrayList<String> mOriginNames;
	private int mFragmentType;
	private MyApplication mApplication;
	private OnPageChangeListener mOnPageChangeListener;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_full_screen_preview);
		
		findView();
		loadData();
		setListener();
		
	}
	
	private void findView(){
		mViewPager = (ViewPagerForPhotoView) findViewById(R.id.viewPager);
		mNumberView = (TextView) findViewById(R.id.number);
		mDownloadView = (ImageView) findViewById(R.id.downloadView);
	}
	
	private void loadData(){
		
		Intent intent = getIntent();
		mUrls = intent.getStringArrayListExtra(INTENT_URLS);
		mOriginNames = intent.getStringArrayListExtra(INTENT_ORIGIN_NAMES);
		mFragmentType = intent.getIntExtra(INTENT_FRAGMENT_TYPE, FileUtil.TYPE_IMAGE);
		int itemPosition = intent.getIntExtra(INTENT_ITEM_POSITION, 0);
		mApplication = (MyApplication) getApplication();
		
		mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
			
			@Override
			public int getCount() {
				return mUrls == null ? 0 : mUrls.size();
			}
			
			@Override
			public Fragment getItem(int position) {

				if(mFragmentType == FileUtil.TYPE_IMAGE){
					return ImageViewFragment.getInstance(mUrls.get(position));
				}else{
					return VideoViewFragment.getInstance(mUrls.get(position));
				}
				
			}

			
		});
		
		
		mViewPager.setCurrentItem(itemPosition, false);
		mNumberView.setText((itemPosition + 1) + "/" + mUrls.size());
		
		String url = mUrls.get(itemPosition);
		
		File file = new File(url);
		
		if(file.exists()){
			mDownloadView.setVisibility(View.GONE);
		}

	}
	
	private void setListener(){
		
		mOnPageChangeListener = new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				
				mNumberView.setText((position + 1) + "/" + mUrls.size());
				
				for(Fragment f : getSupportFragmentManager().getFragments()){

					if(f instanceof VideoViewFragment){
						((VideoViewFragment) f).pause();
					}
					
				}
				
				String url = mUrls.get(position);
				
				File file = new File(url);
				
				if(file.exists()){
					mDownloadView.setVisibility(View.GONE);
				}else{
					mDownloadView.setVisibility(View.VISIBLE);
				}
				
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				
			}
		};
		
		//mViewPager.addOnPageChangeListener(mOnPageChangeListener);
		mViewPager.setOnPageChangeListener(mOnPageChangeListener);
		
		mDownloadView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				String url = mUrls.get(mViewPager.getCurrentItem());
				String fileName = mOriginNames.get(mViewPager.getCurrentItem());
				
				if(url.startsWith("http")){
					FileDownloadAndroidManager.download(FullScreenPreviewActivity.this, Uri.parse(url)
							,"/wenfeng/" + mApplication.getUserID() 
							+ "/download/", fileName);
				}
				
				
			}
		});
	}
	
	/*@Override
	protected void onDestroy() {
		super.onDestroy();
		mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
	}*/
	
}
