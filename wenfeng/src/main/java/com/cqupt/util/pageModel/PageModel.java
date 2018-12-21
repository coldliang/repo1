package com.cqupt.util.pageModel;

import java.util.List;

import com.cqupt.net.WebService;

public abstract class PageModel<T> {
	
	public static final int CONNECTION_FAIL = -1;
	
	protected int currentPage = 1;
	protected int lineSize = 10;
	protected int lastItemIndex = 0;// the index of last item
	protected int sumCount = 0;
	protected WebService web = new WebService();
	
	public abstract List<T> getDataList();
	protected abstract int getDataCount();
	
	public List<T> nextPage(){
		currentPage++;
		return getDataList();
	}
	
	public int getSumPages(){
		
		if(getDataCount() == CONNECTION_FAIL){
			return CONNECTION_FAIL;
		}else{
			return (getDataCount()+lineSize-1)/lineSize;
		}
		
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	
	public int getLastItemIndex() {
		return lastItemIndex;
	}
	public void setLastItemIndex(int lastItemIndex) {
		this.lastItemIndex = lastItemIndex;
	}
	
	public int getSumCount(){
		return sumCount;
	}

}
