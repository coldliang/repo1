package com.cqupt.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.cqupt.R;

import java.util.Formatter;
import java.util.Locale;


public class MyAudioPlayer extends FrameLayout {

	private Context mContext;
	private View mRoot;
	private ImageButton mPauseButton;
	private ImageButton mFfwdButton;
	private ImageButton mRewButton;
	private SeekBar mSeekBar;
	private TextView mEndTime, mCurrentTime;
	private MediaPlayer mPlayer;
	private StringBuilder mFormatBuilder;
	private Formatter mFormatter;
	private OnDragListener mListener;
	private OnCompletionListener mOnCompletionListener;
	
	private boolean mIsDragging = false;
	
	public interface OnDragListener{
		public void onDragStart();
		public void onDragStop();
	}

	public MyAudioPlayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mContext = context;

		FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		View v = makeControllerView();
		addView(v, frameParams);
	}

	public MyAudioPlayer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MyAudioPlayer(Context context) {
		this(context, null);
	}
	
	public void setOnDragListener(OnDragListener listener){
		mListener = listener;
	}
	
	public boolean isPlaying(){
		
		if(mPlayer != null){
			return mPlayer.isPlaying();
		}
		
		return false;
		
	}
	
	public void setLocalFile(String path) throws Exception{
		
		initMediaPlayer();
		
		try{
			mPlayer.stop();
			mPlayer.reset();
			
			mPlayer.setDataSource(path);
			mPlayer.prepare();
			setProgress();
		}catch(Exception e){
			e.printStackTrace();
			disableButtons();//���ܲ�����Ƶ ����ð�ť
			throw e;//������¶��������
		}
		
	}
	
	public void setUri(Context context,Uri uri) throws Exception{
		
		initMediaPlayer();
		
		try{
			mPlayer.stop();
			mPlayer.reset();
			
			mPlayer.setDataSource(context, uri);
			//mPlayer.prepare();
			mPlayer.prepareAsync();
			setProgress();
		}catch(Exception e){
			e.printStackTrace();
			disableButtons();//���ܲ�����Ƶ ����ð�ť
			throw e;//������¶��������
		}
		
		
	}
	
	private void initMediaPlayer(){
		
		if(mPlayer == null){
			mPlayer = new MediaPlayer();
			mPlayer.setOnPreparedListener(new OnPreparedListener() {
				
				@Override
				public void onPrepared(MediaPlayer mp) {
					
					enableButtons();
					
				}
			});
		}
	}
	
	
	public void stop(){
		if(mPlayer != null){
			mPlayer.stop();
			setProgress();
			mPauseButton.setImageResource(R.drawable.ic_media_play);
			mPlayer.reset();
		}
	}
	
	public void pause(){
		
		if(mPlayer != null && mPlayer.isPlaying()){
			doPauseResume();
		}
			
		
	}
	
	 private void disableButtons() {

        if (mPauseButton != null) {
            mPauseButton.setEnabled(false);
        }
        
        if (mRewButton != null) {
            mRewButton.setEnabled(false);
        }
        
        if (mFfwdButton != null) {
            mFfwdButton.setEnabled(false);
        }
        
        if(mSeekBar != null){
        	mSeekBar.setEnabled(false);
        }

	 }
	 
	 private void enableButtons(){
		 
		if (mPauseButton != null) {
			mPauseButton.setEnabled(true);
			mPauseButton.setClickable(true);
		}
		
		if (mRewButton != null) {
			mRewButton.setEnabled(true);
			mPauseButton.setClickable(true);
		}
		
		if (mFfwdButton != null) {
			mFfwdButton.setEnabled(true);
			mPauseButton.setClickable(true);
		}
		
		if(mSeekBar != null){
        	mSeekBar.setEnabled(true);
        	mSeekBar.setClickable(true);
        }
		 
	 }

	@SuppressLint("InflateParams")
	private View makeControllerView() {

		LayoutInflater inflate = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mRoot = inflate.inflate(R.layout.layout_media_controller, null);

		initControllerView(mRoot);

		return mRoot;
	}

	private void initControllerView(View v) {
		mPauseButton = (ImageButton) v.findViewById(R.id.pause);
		if (mPauseButton != null) {
			mPauseButton.setOnClickListener(mPauseListener);
		}

		mFfwdButton = (ImageButton) v.findViewById(R.id.ffwd);
		if (mFfwdButton != null) {
			mFfwdButton.setOnClickListener(mFfwdListener);
		}

		mRewButton = (ImageButton) v.findViewById(R.id.rew);
		if (mRewButton != null) {
			mRewButton.setOnClickListener(mRewListener);
		}

		mSeekBar = (SeekBar) v.findViewById(R.id.mediacontroller_progress);
		if (mSeekBar != null) {
			mSeekBar.setOnSeekBarChangeListener(mSeekListener);
			mSeekBar.setMax(1000);
		}

		mEndTime = (TextView) v.findViewById(R.id.time);
		mCurrentTime = (TextView) v.findViewById(R.id.time_current);

		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
		
		disableButtons();

	}

	private OnClickListener mPauseListener = new OnClickListener() {
		public void onClick(View v) {
			doPauseResume();
		}
	};

	private OnClickListener mFfwdListener = new OnClickListener() {
		public void onClick(View v) {
			
			int pos = mPlayer.getCurrentPosition();
			pos += 15000; // milliseconds
			int duration = mPlayer.getDuration();
			mPlayer.seekTo(pos > duration ? duration : pos);
			setProgress();
		}
	};

	private OnClickListener mRewListener = new OnClickListener() {
		public void onClick(View v) {
			int pos = mPlayer.getCurrentPosition();
			pos -= 5000; // milliseconds
			mPlayer.seekTo(pos < 0 ? 0 : pos);
			setProgress();
		}
	};

	private void doPauseResume() {
		if (mPlayer.isPlaying()) {
			mPlayer.pause();
		} else {
			mPlayer.start();
			
			//setOnCompletionListener����start()֮�����Ч����֪��Ϊʲô
			if(mOnCompletionListener == null){
				mOnCompletionListener = new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						
						mPauseButton.setImageResource(R.drawable.ic_media_play);
						mCurrentTime.setText("");
						mEndTime.setText("");
						
					}
				};
				mPlayer.setOnCompletionListener(mOnCompletionListener);
			}
			
			new UpdateProcessThread().execute();
		}
		updatePausePlay();
	}

	private void updatePausePlay() {
		if (mRoot == null || mPauseButton == null)
			return;

		if (mPlayer.isPlaying()) {
			mPauseButton.setImageResource(R.drawable.ic_media_pause);
		} else {
			mPauseButton.setImageResource(R.drawable.ic_media_play);
		}
	}

	synchronized private int setProgress() {
		if (mPlayer == null || mIsDragging) {
			return 0;
		}
		int position = mPlayer.getCurrentPosition();
		int duration = mPlayer.getDuration();
		
		if (mSeekBar != null) {
			if (duration > 0) {
				// use long to avoid overflow
				long pos = 1000L * position / duration;
				mSeekBar.setProgress((int) pos);
			}

		}

		if (mEndTime != null)
			mEndTime.setText(stringForTime(duration));
		if (mCurrentTime != null)
			mCurrentTime.setText(stringForTime(position));
		
		return position;
	}

	private String stringForTime(int timeMs) {
		int totalSeconds = timeMs / 1000;

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		mFormatBuilder.setLength(0);
		if (hours > 0) {
			return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds)
					.toString();
		} else {
			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}

	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar bar) {
			
			mIsDragging = true;
			
			if(mListener != null){
				mListener.onDragStart();
			}

		}

		public void onProgressChanged(SeekBar bar, int progress,
				boolean fromuser) {
			if (!fromuser) {
				// We're not interested in programmatically generated changes to
				// the progress bar's position.
				return;
			}

			long duration = mPlayer.getDuration();
			long newposition = (duration * progress) / 1000L;
			mPlayer.seekTo((int) newposition);
			if (mCurrentTime != null)
				mCurrentTime.setText(stringForTime((int) newposition));
		}

		public void onStopTrackingTouch(SeekBar bar) {

			mIsDragging = false;
			
			if(mListener != null){
				mListener.onDragStop();
			}
			//setProgress();
			//updatePausePlay();

		}
	};
	
	class UpdateProcessThread extends AsyncTask<Void, Void, Void>{
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			setProgress();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			
			while(mPlayer != null && mPlayer.isPlaying()){//��Ƶ�ڲ��žͲ�ͣ���½���
				if(!mIsDragging){//���ȵ�δ�����϶�״̬�Ž��н���ˢ��
					publishProgress();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			return null;
		}
	}

}
