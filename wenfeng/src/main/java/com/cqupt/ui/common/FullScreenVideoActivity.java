package com.cqupt.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.widget.MediaController;
import android.widget.VideoView;

import com.cqupt.R;


public class FullScreenVideoActivity extends Activity {
	
	private VideoView videoView;
	
	private String videoUri;
	//video�Ĳ���״̬
	private boolean isVideoPlaying = false;
	private int currentVideoPosition = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_videoview_full_screen);
		
		Intent it = getIntent();
		isVideoPlaying = it.getBooleanExtra("isVideoPlaying", false);
		currentVideoPosition = it.getIntExtra("currentVideoPosition", 1);
		videoUri = it.getStringExtra("videoUri");
		
		videoView = (VideoView) findViewById(R.id.videoView);
		
		MediaController controller = new MediaController(this);
		videoView.setMediaController(controller);
		videoView.setVideoURI(Uri.parse(videoUri));
		videoView.seekTo(currentVideoPosition);
		
		if(isVideoPlaying){
			videoView.start();	
		}
		
	}

	@Override
	public void onBackPressed() {
		
		Intent it = new Intent();
		it.putExtra("isVideoPlaying", videoView.isPlaying());
		it.putExtra("currentVideoPosition", videoView.getCurrentPosition());
		setResult(Activity.RESULT_OK,it);
		
		super.onBackPressed();
	}
	
	

}
