package com.cqupt.model;

public class GroupItem {
	
	private String text;
	private int childrenCount;
	private int startChildIndex;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getChildrenCount() {
		return childrenCount;
	}
	public void setChildrenCount(int childrenCount) {
		this.childrenCount = childrenCount;
	}
	public int getStartChildIndex() {
		return startChildIndex;
	}
	public void setStartChildIndex(int startChildIndex) {
		this.startChildIndex = startChildIndex;
	}
	

}
