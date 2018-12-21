package com.cqupt.view;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.cqupt.R;


public class MyAudioPlayView extends FrameLayout{
	
	private ImageView mForeGroundImageView;
	private ProgressBar mProgressBar;
	
	private boolean isPlaying;

	public MyAudioPlayView(Context context) {
		this(context, null);
	}
	
	public MyAudioPlayView(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public MyAudioPlayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		View view = LayoutInflater.from(context).inflate(R.layout.layout_audio_play, this,true);
		mForeGroundImageView = (ImageView) view.findViewById(R.id.fImage);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);	
				
		mProgressBar.setProgress(0);
		
	}
	
	public void setMaxProcess(int max){
		mProgressBar.setMax(max < 0 ? 0 : max);
	}
	
	public void play(){
		isPlaying = true;
		mForeGroundImageView.setImageResource(R.drawable.stop_play);
		mProgressBar.setVisibility(View.VISIBLE);
		new UpdateProgressThread().execute();
	}
	
	public void stop(){
		mProgressBar.setProgress(0);
		mProgressBar.setVisibility(View.GONE);
		mForeGroundImageView.setImageResource(R.drawable.play);
		isPlaying = false;
	}
	
	//����ˢ�²��Ž�����
	class UpdateProgressThread extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {

			try {
				
				while(true){
					Thread.sleep(1000);
					
					if(isPlaying){
						mProgressBar.setProgress(mProgressBar.getProgress() + 1);
					}else{
						stop();
						break;
					}
					
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
			
		}
		
	}
	
}
