package com.cqupt.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class MyPagerAdapter extends PagerAdapter{
	
	private ArrayList<View> pagerList;
	
	public MyPagerAdapter(ArrayList<View> pagerList){
		
		this.pagerList = pagerList;
		
	}

	@Override
	public int getCount() {
		return pagerList.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	 @Override
     public Object instantiateItem(ViewGroup container, int position) {
         container.addView(pagerList.get(position), 0);
         return pagerList.get(position);
     }
	 
	 @Override
     public void destroyItem(ViewGroup container, int position, Object object) {
         container.removeView(pagerList.get(position));
     }
}
