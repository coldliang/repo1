package com.cqupt.model;

public class Plan {
	
	private int planID;
	private String title;
	private String userID;
	private String sDate;
	private String eDate;
	private String createTime;
	public int getPlanID() {
		return planID;
	}
	public void setPlanID(int planID) {
		this.planID = planID;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getsDate() {
		return sDate;
	}
	public void setsDate(String sDate) {
		this.sDate = sDate;
	}
	public String geteDate() {
		return eDate;
	}
	public void seteDate(String eDate) {
		this.eDate = eDate;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	

	@Override
	public boolean equals(Object o) {
		
		if(o == null || !(o instanceof Plan)){
			return false;
		}
		
		if(o == this){
			return true;
		}
		
		Plan plan = (Plan)o;
		return this.createTime.equals(plan.getCreateTime());
	}


}
