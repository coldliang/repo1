package com.cqupt.model;

public class ScoreStudentItem {
	
	private String name;
	private String submitTime;
	private String score;
	private int max;
	private int process;
	private int readedAttachmentCount;
	private int notReadedAttachmentCount;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSubmitTime() {
		return submitTime;
	}
	public void setSubmitTime(String submitTime) {
		this.submitTime = submitTime;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	public int getProcess() {
		return process;
	}
	public void setProcess(int process) {
		this.process = process;
	}
	public int getReadedAttachmentCount() {
		return readedAttachmentCount;
	}
	public void setReadedAttachmentCount(int readedAttachmentCount) {
		this.readedAttachmentCount = readedAttachmentCount;
	}
	public int getNotReadedAttachmentCount() {
		return notReadedAttachmentCount;
	}
	public void setNotReadedAttachmentCount(int notReadedAttachmentCount) {
		this.notReadedAttachmentCount = notReadedAttachmentCount;
	}
	
	
}
