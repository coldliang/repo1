package com.cqupt.model;

public class UserInfo {
	
	private String userID;
	private String userName;
	private String userType;
	private String userEmail;
	private String userSchool;
	private String userMajor;
	private String userDepartment;

	private String userAcademy;
	/**
	 * �û������꼶
	 */
	private String userGrade;
	/**
	 * �û����ڰ༶
	 */
	private String userClass;
	

	
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getUserSchool() {
		return userSchool;
	}
	public void setUserSchool(String userSchool) {
		this.userSchool = userSchool;
	}
	public String getUserMajor() {
		return userMajor;
	}
	public void setUserMajor(String userMajor) {
		this.userMajor = userMajor;
	}
	public String getUserType() {
		return userType;
	}
	public void setUserType(String userType) {
		this.userType = userType;
	}
	public String getUserDepartment() {
		return userDepartment;
	}
	public void setUserDepartment(String userDepartment) {
		this.userDepartment = userDepartment;
	}
	public String getUserAcademy() {
		return userAcademy;
	}
	public void setUserAcademy(String userAcademy) {
		this.userAcademy = userAcademy;
	}
	public String getUserGrade() {
		return userGrade;
	}
	public void setUserGrade(String userGrade) {
		this.userGrade = userGrade;
	}
	public String getUserClass() {
		return userClass;
	}
	public void setUserClass(String userClass) {
		this.userClass = userClass;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o == null){
			return false;
		}
		
		if(! (o instanceof UserInfo)){
			return false;
		}
		
		if(this == o){
			return true;
		}
		
		UserInfo userInfo = (UserInfo)o;
		
		return userInfo.getUserID().equals(getUserID());
		
	}
}
