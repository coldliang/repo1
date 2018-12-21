package com.cqupt.model;

public class Task {
	private String createUser;
	private String  title;
	private String remark;
	private String startDate;
	private String endDate;
	private String isDiscuss;
	private String content;
	private int taskID;
	private int students;
	private int exams;
	
	public String getCreateUser() {
		return createUser;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getIsDiscuss() {
		return isDiscuss;
	}
	public void setIsDiscuss(String isDiscuss) {
		this.isDiscuss = isDiscuss;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getTaskID() {
		return taskID;
	}
	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}
	public int getStudents() {
		return students;
	}
	public void setStudents(int students) {
		this.students = students;
	}
	public int getExams() {
		return exams;
	}
	public void setExams(int exams) {
		this.exams = exams;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o == null) return false;
		
		if(o == this) return true;
		
		if(o instanceof Task){
			Task other = (Task)o;
			
			return this.taskID == other.taskID;
		}else{
			return false;
		}
		
	}

}
