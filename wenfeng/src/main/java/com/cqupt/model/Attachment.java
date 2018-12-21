package com.cqupt.model;

public class Attachment {
	
	private int id;
	private String originName;
	private String newName;
	private String downloadTime;
	private String userID = "";
	private String uploadTime = "";
	private String fileSize = "";
	private boolean isFileRead;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOriginName() {
		return originName;
	}
	public void setOriginName(String originName) {
		this.originName = originName;
	}
	public String getNewName() {
		return newName;
	}
	public void setNewName(String newName) {
		this.newName = newName;
	}
	public String getDownloadTime() {
		return downloadTime;
	}
	public void setDownloadTime(String downloadTime) {
		this.downloadTime = downloadTime;
	}
	public String getUserID() {
		return userID;
	}
	public void setIsFileRead(boolean isFileRead){
		this.isFileRead = isFileRead;
	}
	public boolean getIsFileRead(){
		return isFileRead;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(String uploadTime) {
		this.uploadTime = uploadTime;
	}
	public String getFileSize() {
		return fileSize;
	}
	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
	@Override
	public boolean equals(Object o) {
		
		if(o == null || ! (o instanceof Attachment)){
			return false;
		}
		
		if(this == o){
			return true;
		}
		
		Attachment a = (Attachment)o;
		return a.getNewName().equals(this.getNewName());
	}
		
}
