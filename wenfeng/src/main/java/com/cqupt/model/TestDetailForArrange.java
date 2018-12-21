package com.cqupt.model;


public class TestDetailForArrange {
	
	private int testID;
	private String title;
	private String testType;
	private String level;
	private String domain;
	private String createUser;
	public int getTestID() {
		return testID;
	}
	public void setTestID(int testID) {
		this.testID = testID;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTestType() {
		return testType;
	}
	public void setTestType(String testType) {
		this.testType = testType;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getCreateUser() {
		return createUser;
	}
	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o == null || ! (o instanceof TestDetailForArrange)){
			return false;
		}
		
		if(this == o){
			return true;
		}
		
		TestDetailForArrange userInfo = (TestDetailForArrange)o;
		
		return userInfo.getTestID() == getTestID();
	}
	
}
