package com.cqupt.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.cqupt.adapter.ISimpleAdapter.OnDataSetChangedListener;
import com.cqupt.adapter.ItemsShowingAdapter;
import com.cqupt.model.ItemsShowingModel;

public class FlowLayout extends ViewGroup implements OnClickListener{
	
	private final static int DEFAULT_ITEM_WIDTH = 220;
	private final static int DEFAULT_ITEM_HEIGHT = 220;
	
	public interface OnItemViewClickListener{
		public void onClick(View view,List<ItemsShowingModel> data,int position);
	}
	
	private int mItemWidth = DEFAULT_ITEM_WIDTH;
	private int mItemHeight = DEFAULT_ITEM_HEIGHT;
	private OnItemViewClickListener mListener;
    protected List<List<View>> mAllViews = new ArrayList<List<View>>();
    protected List<Integer> mLineHeight = new ArrayList<Integer>();

    private ItemsShowingAdapter mAdapter;
    
    public FlowLayout(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public FlowLayout(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context)
    {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // wrap_content
        int width = 0;
        int height = 0;

        int lineWidth = 0;
        int lineHeight = 0;

        int cCount = getChildCount();

        for (int i = 0; i < cCount; i++)
        {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE)
            {
                if (i == cCount - 1)
                {
                    width = Math.max(lineWidth, width);
                    height += lineHeight;
                }
                continue;
            }
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();

            int childWidth = child.getMeasuredWidth() + lp.leftMargin
                    + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin
                    + lp.bottomMargin;

            if (lineWidth + childWidth > sizeWidth - getPaddingLeft() - getPaddingRight())
            {
                width = Math.max(width, lineWidth);
                lineWidth = childWidth;
                height += lineHeight;
                lineHeight = childHeight;
            } else
            {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            if (i == cCount - 1)
            {
                width = Math.max(lineWidth, width);
                height += lineHeight;
            }
        }
        setMeasuredDimension(
                //
                modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width + getPaddingLeft() + getPaddingRight(),
                modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height + getPaddingTop() + getPaddingBottom()//
        );

    }


    @SuppressLint("DrawAllocation")
	@Override
    protected void onLayout(boolean changed, int l, int t, int r, int b)
    {
    	mAllViews.clear();
        mLineHeight.clear();

        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

        List<View> lineViews = new ArrayList<View>();

        int cCount = getChildCount();

        for (int i = 0; i < cCount; i++)
        {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) continue;
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (childWidth + lineWidth + lp.leftMargin + lp.rightMargin > width - getPaddingLeft() - getPaddingRight())
            {
                mLineHeight.add(lineHeight);
                mAllViews.add(lineViews);

                lineWidth = 0;
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
                lineViews = new ArrayList<View>();
            }
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin
                    + lp.bottomMargin);
            lineViews.add(child);

        }
        mLineHeight.add(lineHeight);
        mAllViews.add(lineViews);


        int left = getPaddingLeft();
        int top = getPaddingTop();

        int lineNum = mAllViews.size();

        for (int i = 0; i < lineNum; i++)
        {
            lineViews = mAllViews.get(i);
            lineHeight = mLineHeight.get(i);

            for (int j = 0; j < lineViews.size(); j++)
            {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE)
                {
                    continue;
                }

                MarginLayoutParams lp = (MarginLayoutParams) child
                        .getLayoutParams();

                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                child.layout(lc, tc, rc, bc);

                left += child.getMeasuredWidth() + lp.leftMargin
                        + lp.rightMargin;
            }
            left = getPaddingLeft();
            top += lineHeight;
        }

    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs)
    {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams()
    {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p)
    {
        return new MarginLayoutParams(p);
    }
    
    @Override
	public void onClick(View v) {
		
    	if(mListener != null){
			mListener.onClick(v, mAdapter.getDatas(), (Integer)v.getTag());
		}
		
	}
    
    public void setAdapter(ItemsShowingAdapter adapter){
    	
    	mAdapter = adapter;
    	
    	if(mAdapter != null){
    		mAdapter.setOnDataSetChangeListener(new OnDataSetChangedListener() {
    			
    			@Override
    			public void onChanged() {
    				makeChildViews();
    			}
    		});
        	makeChildViews();
    	}
    	
    }
    
    private void makeChildViews(){
    	
    	if(mAdapter != null && mAdapter.getItemCount() != 0){
    		
    		removeAllViews();
    		
    		android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout
    				.LayoutParams(mItemWidth,mItemHeight);
    		lp.topMargin = 10;
    		lp.bottomMargin = 10;
    		lp.leftMargin = 5;
    		lp.rightMargin = 5;
    		
    		for(int i = 0; i < mAdapter.getItemCount(); i++){
    			
    			//Nine items are displayed by default
    			if(i > 8){
    				break;
    			}
    			View view = mAdapter.getView(i);
    			view.setOnClickListener(this);
    			addView(view, lp);
    			
    		}	
    	}	
    }
    
    public void setOnItemViewClickListener(OnItemViewClickListener listener){
		mListener = listener;
	}
  
}
