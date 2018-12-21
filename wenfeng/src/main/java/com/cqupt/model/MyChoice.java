package com.cqupt.model;

public class MyChoice {
	private String userID;
	private int testItemID;
	private int taskID;
	private String answer;
	private String submitTime;
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public int getTaskID() {
		return taskID;
	}
	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public int getTestItemID() {
		return testItemID;
	}
	public void setTestItemID(int testItemID) {
		this.testItemID = testItemID;
	}
	public String getSubmitTime() {
		return submitTime;
	}
	public void setSubmitTime(String submitTime) {
		this.submitTime = submitTime;
	}
	
	
}
