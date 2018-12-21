package com.cqupt.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.widget.TextView;

/**
 * use <image> get the class
 */
public class MyImageGetter implements ImageGetter {
	
	private TextView textView;
	private String content;
	private Context context;
	
	public MyImageGetter(String content,TextView textView,Context context){
		this.content = content;
		this.textView = textView;
		this.context = context;
	}

	@Override
	public Drawable getDrawable(String source) {
		
		Drawable drawable = null;
		String fileName = source.substring(source.lastIndexOf("/") + 1);
			
		String path = Environment.getExternalStorageDirectory().getAbsolutePath()
				+"/wenfeng/cache/";
		
		final File file = new File(path + fileName);
		// get the local file if have
		if(file.exists()){
			drawable = Drawable.createFromPath(file.getPath());
			
			if(drawable == null){new Thread();
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						FileUtil.deleteFile(file.getPath());
						
					}
				}).start();
				
				return drawable;
			}
			
			int w = drawable.getIntrinsicWidth();
			int h = drawable.getIntrinsicHeight();
			int windowWidth = DensityUtil.getWindowWidth(context);

			drawable.setBounds(0,0,windowWidth,windowWidth/w*h);
		}else{// if not have, load from web
			new LoadDrawableThread().execute("http://202.202.43.245" + source,path,fileName);
		}
	
		return drawable;
	}
	
	private class LoadDrawableThread extends AsyncTask<String,Void,Integer>{
		
		private static final int SUCCESS = 0;
		private static final int FAIL = -1;

		@Override
		protected Integer doInBackground(String... params) {
			
			String downloadPath = params[0];
			String fileSavePath = params[1];
			String fileName = params[2];
			
			InputStream in = null;
			FileOutputStream out = null;
			HttpURLConnection con = null;
			
			try {
				URL url = new URL(downloadPath);
				con = (HttpURLConnection) url.openConnection();
				con.setConnectTimeout(3000);
				con.setRequestMethod("GET");
				
				if(con.getResponseCode() == 200){
					in = con.getInputStream();
					byte[] buffer = new byte[1024];
					File filePath = new File(fileSavePath);
					
					if(!filePath.exists()){
						filePath.mkdirs();
					}
					
					File file = new File(fileSavePath + fileName);
					
					if(!file.exists()){
						file.createNewFile();
					}
					
					out = new FileOutputStream(file);
					int size = -1;
					
					while((size = in.read(buffer)) != -1){
						out.write(buffer,0,size);
					}
				}else{
					return FAIL;
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return FAIL;
			} catch (IOException e) {
				e.printStackTrace();
				return FAIL;
			}finally{
				
				if(con != null){
					con.disconnect();
				}
				
				if(in != null){
					try {
						in.close();
					} catch (IOException e) {			
						e.printStackTrace();
						return FAIL;
					}
				}
				
				if(out != null){
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
						return FAIL;
					}
				}
			}
				
			return SUCCESS;
		}

		@Override
		protected void onPostExecute(Integer result) {
			
			if(result == SUCCESS && textView != null){
				textView.setText(Html.fromHtml(content,MyImageGetter.this, null));
			}
		}
		
	}
	
}
