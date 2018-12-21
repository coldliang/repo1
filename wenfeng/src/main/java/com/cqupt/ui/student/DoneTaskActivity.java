package com.cqupt.ui.student;

import com.cqupt.db.DBManager;
import com.cqupt.model.Test;
import com.cqupt.model.TestItemOption;
import com.cqupt.ui.common.IDoneTestActivity;

import java.util.ArrayList;
import java.util.List;

public class DoneTaskActivity extends IDoneTestActivity{
	
	private DBManager mDb = new DBManager(helper.getConnection());

	
	@Override
	protected boolean isTestFinished(int id, int testID, String userID) {
		// TODO Auto-generated method stub
		return mDb.checkTestFinished(id, testID, userID);
	}
	
	@Override
	protected ArrayList<Test> getTestList() {
		return mDb.getTestListByTaskID(ID);
	}

	@Override
	protected String getAnswer(int itemID) {
		return mDb.getAnswer(application.getUserID(),itemID,ID);//�û��Ĵ�;
	}
	
	@Override
	protected int getItemType(int itemID) {
		return mDb.getItemType(itemID);
	}

	@Override
	protected List<TestItemOption> getTestItemOptions(int itemID) {
		return mDb.getTestItemOption(itemID);
	}
}