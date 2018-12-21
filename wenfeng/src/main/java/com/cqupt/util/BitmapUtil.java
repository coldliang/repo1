package com.cqupt.util;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class BitmapUtil {
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {  
		// Raw height and width of image  
       final int height = options.outHeight;  
       final int width = options.outWidth;  
       int inSampleSize = 1;  
   
       if (height > reqHeight || width > reqWidth) {  
    	   // Calculate ratios of height and width to requested height and width  
           final int heightRatio = Math.round((float) height / (float) reqHeight);  
           final int widthRatio = Math.round((float) width / (float) reqWidth);  
   
           // Choose the smallest ratio as inSampleSize value, this will guarantee  
           // a final image with both dimensions larger than or equal to the  
           // requested height and width.  
           inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;  
       }  
       return inSampleSize;  
    }  
	
	public static Bitmap decodeSampledBitmapFromFile(String pathName,  int reqWidth, int reqHeight) {  
	   
	    // First decode with inJustDecodeBounds=true to check dimensions  
	    final BitmapFactory.Options options = new BitmapFactory.Options();  
	    options.inJustDecodeBounds = true;  
	    BitmapFactory.decodeFile(pathName, options);
	   
	    // Calculate inSampleSize  
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);  
	   
	    // Decode bitmap with inSampleSize set  
	    options.inJustDecodeBounds = false;  
	    Bitmap bitmap = BitmapFactory.decodeFile(pathName, options);
	    Bitmap newBitmap = null;
	    
	    if(bitmap != null){
	    	newBitmap =  Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
	    	bitmap.recycle();
	    }
	    
	    return newBitmap;
	} 
	
	public static ByteArrayOutputStream compressImage(Bitmap image) {  
		  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int options = 100;  
        while ( baos.toByteArray().length / 1024>100) {  //ѭ���ж����ѹ����ͼƬ�Ƿ����100kb,���ڼ���ѹ��         
            baos.reset();//����baos�����baos
            //����ѹ��options%����ѹ��������ݴ�ŵ�baos��  
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);
            options -= 10;//ÿ�ζ�����10  
        }
        return baos;
    }
	
	/**
	 * ����bitmap���첽��
	 */
	public static class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {  
	    private final WeakReference<ImageView> imageViewReference;    
	  
	    public BitmapWorkerTask(ImageView imageView) {  
	        // Use a WeakReference to ensure the ImageView can be garbage collected  
	        imageViewReference = new WeakReference<ImageView>(imageView);  
	    }  
	  
	    // Decode image in background.  
	    @Override  
	    protected Bitmap doInBackground(String... params) {   
	        return BitmapUtil.decodeSampledBitmapFromFile(params[0],200,200);  
	    }  
	  
	    // Once complete, see if ImageView is still around and set bitmap.  
	    @Override  
	    protected void onPostExecute(Bitmap bitmap) {  
	        if (imageViewReference != null && bitmap != null) {  
	            final ImageView imageView = imageViewReference.get();  
	            if (imageView != null) {  
	                imageView.setImageBitmap(bitmap);  
	            }  
	        }  
	    }  
	}

}
