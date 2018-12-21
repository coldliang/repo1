package com.cqupt.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.readystatesoftware.viewbadger.BadgeView;

public class TagView extends FrameLayout {
	
	private Context mContext;

	
	public TagView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public TagView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TagView(Context context) {
		this(context, null);
	}
	
	@SuppressLint("RtlHardcoded")
	public void setTarget(View view,boolean isBadgeViewShown){
		BadgeView badgeView = new BadgeView(mContext);
		badgeView.setText("!");
		
		if(!isBadgeViewShown){
			badgeView.setVisibility(View.GONE);
		}
		
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lp.gravity = Gravity.RIGHT | Gravity.TOP;
		
		addView(view);
		addView(badgeView, lp);
	}

}
