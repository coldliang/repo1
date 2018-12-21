package com.cqupt.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public abstract class ISimpleAdapter<T> {
	
	public interface OnDataSetChangedListener{
		public void onChanged();
	}

	protected List<T> mData;
	protected OnDataSetChangedListener mOndataChangedListener;
	protected Context mContext;
	protected LayoutInflater mInflater;
	
	public abstract View getView(int position);
	
	public int getItemCount(){
		return mData == null ? 0 : mData.size();
	} 
	
	public List<T> getDatas(){
		return mData;
	}
	
	public void notifyDataSetChanged(){
		
		if(mOndataChangedListener != null){
			mOndataChangedListener.onChanged();
		}
		
	}
	
	public void setOnDataSetChangeListener(OnDataSetChangedListener listener){
		mOndataChangedListener = listener;
	}

}
