package com.cqupt.util;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtil {
	
	public static final int TYPE_AUDIO = 0;
	public static final int TYPE_IMAGE = 1;
	public static final int TYPE_DOCUMENT = 2;
	public static final int TYPE_VIDEO = 3;
	
	/**
	 * check the type of files
	 * @param type file type mp3 or ...
	 */
	public static boolean checkFileType(File file,String type){
		
		return checkFileType(file.getName(), type);
		
	}
	
	public static boolean checkFileType(String fileName,String type){
		
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
		
		return extension.equalsIgnoreCase(type);
		
	}
	
	/**
	 * judge the file type:word mp3 img
	 */
	public static int getFileType(File file){
		return getFileType(file.getName());
	}
	
	/**
	 * judge the file type:word mp3 img
	 */
	public static int getFileType(String path){
		//String[] strs = path.split("\\.");
		//String extension = strs[strs.length - 1];
		String extension = path.substring(path.lastIndexOf(".") + 1);
		
		if( "mp3".equalsIgnoreCase(extension) || "amr".equalsIgnoreCase(extension) 
		    || "wma".equalsIgnoreCase(extension) ||"wav".equalsIgnoreCase(extension)
		    || "au".equalsIgnoreCase(extension) || "RealAudio".equalsIgnoreCase(extension) 
		    || "midi".equalsIgnoreCase(extension) || "vqf".equalsIgnoreCase(extension)
		  )
		{
			return TYPE_AUDIO;
		}
		
		if( "bmp".equalsIgnoreCase(extension) || "png".equalsIgnoreCase(extension) 
			|| extension.contains("jpeg") || "jpg".equalsIgnoreCase(extension))
		{
			return TYPE_IMAGE;
		}
		
		if("AVI".equalsIgnoreCase(extension) || "wma".equalsIgnoreCase(extension) 
				||"rmvb".equalsIgnoreCase(extension) || "rm".equalsIgnoreCase(extension)
				|| "flash".equalsIgnoreCase(extension) || "mp4".equalsIgnoreCase(extension)
				||"mid".equalsIgnoreCase(extension) || "3GP".equalsIgnoreCase(extension)
				|| "flv".equalsIgnoreCase(extension) || "wmv".equalsIgnoreCase(extension))
		{
			return TYPE_VIDEO;
		}
		
		return TYPE_DOCUMENT;
	}
		
	/**
	 * return the str, which denotes the file size with unit
     */
	public static String getFileSize(long fileSize){
		// control the percision of digital
		DecimalFormat dformat = new DecimalFormat("####.##");
		
		if(fileSize >= 1024*1024){
			return dformat.format((float)fileSize/(1024*1024))+"MB";
		}
		return dformat.format((float)fileSize/1024)+"KB";
	}
	
	/**
	 * return the str, which denotes the file size with unit
     */
	public static String getFileSize(String fileSize) throws NumberFormatException{
		return getFileSize(Long.parseLong(fileSize));	
	}
	
	/**
	 * return the time in str as the file name
	 */
	public static String getStringFromTimestamp(String f,String fileName){
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat(f);
		return format.format(date) + "." +fileName.split("\\.")[1];
	}
	
	public static boolean deleteFile(String filePath){
		
		File file = new File(filePath);
		
		if(file.exists()){
			return file.delete();
		}
		
		return false;
		
	}

}
