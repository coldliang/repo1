package com.cqupt.fragment;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.cqupt.R;


public class VideoViewFragment extends Fragment {
	
	private static final String TAG_URL = "url";
	
	private VideoView mVideoView;
	private ProgressBar mProgressBar;
	private View mRootView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if(mRootView == null){
			mRootView = inflater.inflate(R.layout.fragment_videoview, container,false);
			mVideoView = (VideoView) mRootView.findViewById(R.id.videoView);
			mProgressBar = (ProgressBar) mRootView.findViewById(R.id.progressBar);

			String url = getArguments().getString(TAG_URL);

			final MediaController controller = new MediaController(getActivity());
			mVideoView.setMediaController(controller);
			controller.setMediaPlayer(mVideoView);
			mVideoView.setVideoPath(url);

			mVideoView.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer mp) {

					//mVideoView.seekTo(1);//this will cause the control bar to appear immediately
					mProgressBar.setVisibility(View.GONE);

				}
			});

            mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mProgressBar.setVisibility(View.GONE);
                    return false;
                }
            });
		}

		return mRootView;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopPlay();
		
	}

	@Override
	public void onDetach() {
		super.onDetach();
		stopPlay();
	}

	@Override
	public void onPause() {
		super.onPause();
		mVideoView.pause();
	}

	public static VideoViewFragment getInstance(String url){
		
		VideoViewFragment fragment = new VideoViewFragment();
		Bundle bundle = new Bundle();
		bundle.putString(TAG_URL, url);
		fragment.setArguments(bundle);
		
		return fragment;
		
	}
	
	public void stopPlay(){
		
		if(mVideoView != null && mVideoView.isPlaying()){
			mVideoView.stopPlayback();
		}
		
		mProgressBar.setVisibility(View.GONE);
	}
	
	public void pause(){
		
		if(mVideoView != null && mVideoView.isPlaying()){
			mVideoView.pause();
		}
		
		mProgressBar.setVisibility(View.GONE);
	}

    public void setUri(String uri){

        mVideoView.setVideoURI(Uri.parse(uri));

    }
	
	

}
