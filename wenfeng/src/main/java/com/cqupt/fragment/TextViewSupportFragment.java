package com.cqupt.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cqupt.R;

/**
 * Created by Alan on 2016/5/24.
 */
public class TextViewSupportFragment extends Fragment {

    private static final String ARGUMENT_CONTENT = "content";

    private TextView mRootView;
    private String mContent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(mRootView == null){
            mRootView = (TextView) inflater.inflate(R.layout.layout_empty_textview,container,false);
            mRootView.setMovementMethod(new ScrollingMovementMethod());//enable scrollbar

            Bundle bundle = getArguments();
            mContent = bundle.getString(ARGUMENT_CONTENT);

            if(!TextUtils.isEmpty(mContent)){
                mRootView.setText(Html.fromHtml(mContent));
            }
        }

        return mRootView;
    }

    public static TextViewSupportFragment getInstance(String content){

        TextViewSupportFragment f = new TextViewSupportFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_CONTENT,content);
        f.setArguments(bundle);
        return f;

    }

    public void setTextContent(String content){
        mContent = content;
        mRootView.setText(Html.fromHtml(mContent));
        mRootView.scrollTo(0,0);
    }

}
