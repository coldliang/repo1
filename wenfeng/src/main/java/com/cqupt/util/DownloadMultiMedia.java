package com.cqupt.util;

import android.os.Environment;

import com.cqupt.model.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * download the multi-media files
 */
public class DownloadMultiMedia {
	
	private static String fileSavePath = Environment.getExternalStorageDirectory()
			+ "/wenfeng/cache";
	
	// download the image in mixed image-text
	public static void downloadInnerTextImage(List<Test> testList){
		
		for(Test test : testList){
			List<String> srcList = getSrc(test.getTestContent());
			
			for(String src : srcList){
				String fileName = src.substring(src.lastIndexOf("/") + 1);
				
				// download when the file not exists
				if(!new File(fileSavePath + "/" + fileName).exists()){
					FileDownloadHttp.download("http://202.202.43.245" + src
							, fileName, fileSavePath);
				}			
			}
	    }
	
    }
	
	private static List<String> getSrc(String string){
		
		List<String> srcList = new ArrayList<String>();
		List<String> imgTagList = new ArrayList<String>();
		
		Pattern p = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
		Matcher m = p.matcher(string);
		
		// get all img labels
		while(m.find()){
			imgTagList.add(m.group());
		}
		
		// get the content of src
		for(String s : imgTagList){
			srcList.add(s.substring(s.indexOf("src=") + 5
					,s.indexOf("\"",s.indexOf("src=") + 5)));
		}
		
		return srcList;
		
	}
	
}
