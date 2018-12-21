package com.cqupt.util.pageModel;

import java.util.List;

import com.cqupt.model.ScoreStudentItem;
import com.cqupt.net.WebServiceOperation;

public class ScoreStuItemPageModel extends PageModel<ScoreStudentItem> {
	
	private int exerciseID;
	private String scoreType;
	private String fileType;
	
	public ScoreStuItemPageModel(int exerciseID, String scoreType,
			String fileType) {
		this.exerciseID = exerciseID;
		this.scoreType = scoreType;
		this.fileType = fileType;
	}
	
	public ScoreStuItemPageModel(int exerciseID, String scoreType,
			String fileType,int lineSize,int currentPage) {
		this.exerciseID = exerciseID;
		this.scoreType = scoreType;
		this.fileType = fileType;
		this.lineSize = lineSize;
		this.currentPage = currentPage;
	}

	@Override
	public List<ScoreStudentItem> getDataList() {
		return WebServiceOperation.getScoreStuItemByPage(exerciseID, scoreType, fileType,lineSize,currentPage);
	}

	@Override
	protected int getDataCount() {
		int count = WebServiceOperation.getScoreStuItemsCount(exerciseID, fileType, scoreType);
		return count == -1 ? CONNECTION_FAIL : count;
	}

}
