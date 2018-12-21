package com.cqupt.util;

import java.util.List;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class  CommonListViewAdapter<T> extends BaseAdapter {
	
	private List<T> mData;
	private LayoutInflater mInflater;
	private int mLayoutResId;
	
	protected abstract void inflate(ViewHolder viewHolder,T item,int position);
	
	public CommonListViewAdapter(List<T> data,Context context,int layoutResId){
		mData = data;
		mInflater = LayoutInflater.from(context);
		mLayoutResId = layoutResId;
	}
	
	public List<T> getData(){
		return mData;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder = null;
		
		if(convertView == null){		
			convertView = mInflater.inflate(mLayoutResId, parent,false);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		inflate(viewHolder, mData.get(position),position);
		
		return convertView;
	}
	
	
	public class ViewHolder{
		
		private SparseArray<View> mViews = new SparseArray<View>();
		private View mConvertView;
		
		public ViewHolder(View convertView){
			mConvertView = convertView;
		}
		
		
		public View getView(int resId){
			
			View view = mViews.get(resId);
			
			if(view == null){
				view = mConvertView.findViewById(resId);
				mViews.put(resId, view);
			}
			
			return view;
			
		}
			
	}

}
