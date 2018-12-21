package com.cqupt.ui.common;

import java.util.ArrayList;
import java.util.List;

import com.cqupt.model.Test;
import com.cqupt.model.TestItemOption;

public class DonePlanTestActivity extends IDoneTestActivity {
	
	@Override
	protected boolean isTestFinished(int id, int testID, String userID) {
		// TODO Auto-generated method stub
		return mDb.checkPlanTestFinished(id, testID);
	}

	@Override
	protected String getAnswer(int itemID) {
		return mDb.getPlanAnswer(itemID,ID);
	}

	@Override
	protected ArrayList<Test> getTestList() {
		return mDb.getTestListByPlanID(ID);
	}

	@Override
	protected int getItemType(int itemID) {
		// TODO Auto-generated method stub
		return mDb.getItemType(itemID);
	}

	@Override
	protected List<TestItemOption> getTestItemOptions(int itemID) {
		// TODO Auto-generated method stub
		return mDb.getTestItemOption(itemID);
	}
}
