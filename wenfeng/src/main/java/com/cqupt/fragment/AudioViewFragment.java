package com.cqupt.fragment;

import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cqupt.view.MyAudioPlayer;

/**
 * Created by Alan on 2016/5/20.
 */
public class AudioViewFragment extends Fragment {

    private static final String ARGUMENT_URI = "uri";
    private MyAudioPlayer mRootView;
    private String mResUri;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if(mRootView == null){
            mRootView = new MyAudioPlayer(getActivity());
            Bundle bundle = getArguments();
            mResUri = bundle.getString(ARGUMENT_URI);

            if(!TextUtils.isEmpty(mResUri)){

                try{
                    mRootView.setUri(getActivity(),Uri.parse(mResUri));
                }catch (Exception e){
                    e.printStackTrace();
                }

            }

        }

        return mRootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRootView.stop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mRootView.pause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mRootView.stop();
    }

    public static AudioViewFragment getInstance(String uri){

        AudioViewFragment f = new AudioViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_URI,uri);
        f.setArguments(bundle);
        return f;

    }

    public void setAudioUri(String uri){

        if(!TextUtils.isEmpty(uri)){
            mResUri = uri;

            try{
                mRootView.setUri(getActivity(),Uri.parse(uri));
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    }
}
