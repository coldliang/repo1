package com.cqupt.model;

public class PlanTestArrange {
	
	private String createTime;
	private int testID;
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public int getTestID() {
		return testID;
	}
	public void setTestID(int testID) {
		this.testID = testID;
	}
	
	@Override
	public boolean equals(Object o) {
		
		if(o == null || !(o instanceof PlanTestArrange)){
			return false;
		}
		
		if(o == this){
			return true;
		}
		
		PlanTestArrange pta = (PlanTestArrange)o;
		
		return (this.createTime.equals(pta.getCreateTime()) && this.testID == pta.getTestID());
	}
	

}
