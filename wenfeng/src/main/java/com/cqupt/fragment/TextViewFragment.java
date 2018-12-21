package com.cqupt.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cqupt.R;

/**
 * Created by Alan on 2016/5/20.
 */
public class TextViewFragment extends Fragment {

    private static final String ARGUEMENT_CONTENT = "content";

    private TextView mRootView;
    private String mContent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(mRootView == null){
            mRootView = (TextView) inflater.inflate(R.layout.layout_empty_textview,container);
            mRootView.setMovementMethod(new ScrollingMovementMethod());//enable scrollbar

            Bundle bundle = getArguments();
            mContent = bundle.getString(ARGUEMENT_CONTENT);

            if(!TextUtils.isEmpty(mContent)){
                mRootView.setText(mContent);
            }
        }

        return mRootView;
    }

    public static TextViewFragment getInstance(String content){

        TextViewFragment f = new TextViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUEMENT_CONTENT,content);
        f.setArguments(bundle);
        return f;

    }

    public void setTextContent(String content){
        mContent = content;
    }
}
