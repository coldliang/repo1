package com.cqupt.util;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;

public class AudioPlayManager {
	
	private MediaPlayer mMediaPlayer;
	
	public AudioPlayManager(){
		
		mMediaPlayer = new MediaPlayer();
		
	}
	
	public void setLocalFile(String path) throws IllegalArgumentException,SecurityException
	,IllegalStateException,IOException{
		
		mMediaPlayer.stop();
		mMediaPlayer.reset();
		
	    mMediaPlayer.setDataSource(path);
		mMediaPlayer.prepare();
		
	}
	
	public void setUri(Context context,Uri uri) throws IllegalArgumentException,IllegalStateException,IOException{
		
	
		mMediaPlayer.stop();
		mMediaPlayer.reset();
		
		mMediaPlayer.setDataSource(context, uri);
		mMediaPlayer.prepareAsync();
			
		
	}
	
	public void play(){
		mMediaPlayer.start();
	}
	
	public void pause(){
		
		if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
			mMediaPlayer.pause();
		}
		
	}
	
	public void stop(){
		
		if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
			mMediaPlayer.stop();
		}
		
	}
	
	public void seekTo(int msec){
		
		if(mMediaPlayer != null){
			mMediaPlayer.seekTo(msec);
		}
		
	}
	
	public boolean isPlaying(){
		
		if(mMediaPlayer != null && mMediaPlayer.isPlaying()){
			return true;
		}else{
			return false;
		}
		
	}
	
	public void setOnPreparedListener(OnPreparedListener l){
		
		if(l != null){
			mMediaPlayer.setOnPreparedListener(l);

		}
		
	}
	
	public void setOnCompletionListener(OnCompletionListener l){
		
		if(l != null){
			mMediaPlayer.setOnCompletionListener(l);
		}
		
	}
	

}
