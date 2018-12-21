package com.cqupt.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.cqupt.R;
import com.cqupt.adapter.ItemsShowingAdapter;

import java.util.HashMap;
import java.util.List;

public class ItemsShowingHorizontalView extends HorizontalScrollView implements OnClickListener{
	
	private final static int DEFAULT_ITEM_WIDTH = 220;
	private final static int DEFAULT_ITEM_HEIGHT = 220;
	
	public interface OnItemViewClickListener{
		public void onClick(List<String> urls,int position);
	}
	
	private int mItemWidth = DEFAULT_ITEM_WIDTH;
	private int mItemHeight = DEFAULT_ITEM_HEIGHT;
	private OnItemViewClickListener mListener;
	
	private LinearLayout mLinearLayout;
	private ItemsShowingAdapter mAdapter;

	public ItemsShowingHorizontalView(Context context, AttributeSet attrs,int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ItemShowingHorizontalView);
		mItemWidth = ta.getDimensionPixelSize(R.styleable.ItemShowingHorizontalView_itemWidth, DEFAULT_ITEM_WIDTH);
		
		if(mItemWidth <= 0 ){
			mItemWidth = DEFAULT_ITEM_WIDTH;
		}
		
		mItemHeight = ta.getDimensionPixelSize(R.styleable.ItemShowingHorizontalView_itemHeight, DEFAULT_ITEM_HEIGHT);
		
		if(mItemHeight <= 0){
			mItemHeight = DEFAULT_ITEM_HEIGHT;
		}
		
		ta.recycle();
		
		mLinearLayout = new LinearLayout(context, attrs, defStyle);
		mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		
		addView(mLinearLayout, lp);
				
	}

	public ItemsShowingHorizontalView(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public ItemsShowingHorizontalView(Context context) {
		this(context, null);
	
	}
	
	@Override
	public void onClick(View v) {
		
		if(mListener != null){
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>)v.getTag();
			@SuppressWarnings("unchecked")
			List<String> urls = (List<String>) map.get(ItemsShowingAdapter.TAG_URLS);
			int position = (Integer) map.get(ItemsShowingAdapter.TAG_POSITION);
			
			mListener.onClick(urls,position);
		}
		
	}
	
	public void setAdapter(ItemsShowingAdapter adapter){
		mAdapter = adapter;
		makeChildView();
	}
	
	public void setListener(OnItemViewClickListener listener){
		mListener = listener;
	}
	
	private void makeChildView(){
		
		mLinearLayout.removeAllViews();
		
		android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout
				.LayoutParams(mItemWidth,mItemHeight);
		lp.topMargin = 10;
		lp.bottomMargin = 10;
		lp.leftMargin = 5;
		lp.rightMargin = 5;
		
		for(int i = 0; i < mAdapter.getItemCount(); i++){
			
			View view = mAdapter.getView(i);
			view.setOnClickListener(this);
			mLinearLayout.addView(view, lp);
			
		}
		
		
	}

}
