package com.cqupt.db;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cqupt.model.Attachment;
import com.cqupt.model.FeedBack;
import com.cqupt.model.MyChoice;
import com.cqupt.model.Plan;
import com.cqupt.model.PlanArrangeTableData;
import com.cqupt.model.PlanChoice;
import com.cqupt.model.PlanTestArrange;
import com.cqupt.model.Task;
import com.cqupt.model.Test;
import com.cqupt.model.TestItem;
import com.cqupt.model.TestItemOption;
import com.cqupt.model.TestSubType;
import com.cqupt.model.TestType;
import com.cqupt.model.UploadFiles;
import com.cqupt.model.UserInfo;


public class DBManager {
	
	SQLiteDatabase db = null;
	
	public DBManager(SQLiteDatabase db){
		this.db = db;
	}
	public ArrayList<Test> getTestListByTaskID(int taskID){
		ArrayList<Test> list = new ArrayList<Test>();
		String sql = "select * from testLibrary where testID in (select testID from taskTestArrange"
				+ " where taskID = ?)";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(taskID)});
		while(cr.moveToNext()){
			Test test = new Test();
			test.setTestID(cr.getInt(0));
			test.setCreateTime(cr.getString(1));
			test.setCreateUser(cr.getString(2));
			test.setTestTypeID(cr.getInt(3));
			test.setTestTypeName(cr.getString(4));
			test.setTestTitle(cr.getString(5));
			test.setTestContent(cr.getString(6));
			list.add(test);
		}
		cr.close();
		return list;
	}
	
	//��üƻ���Ӧ������б�
	public ArrayList<Test> getTestListByPlanID(int planID){
		ArrayList<Test> list = new ArrayList<Test>();
		String sql = "select * from testLibrary where testID in (" +
				"select testID from planTestArrange where planID = ?)";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(planID)});
		while(cr.moveToNext()){
			Test test = new Test();
			test.setTestID(cr.getInt(0));
			test.setCreateTime(cr.getString(1));
			test.setCreateUser(cr.getString(2));
			test.setTestTypeID(cr.getInt(3));
			test.setTestTypeName(cr.getString(4));
			test.setTestTitle(cr.getString(5));
			test.setTestContent(cr.getString(6));
			list.add(test);
		}
		cr.close();
		return list;
	}
	
	//������ѯ�����б�
	public ArrayList<HashMap<String,String>> getTaskListWithConditon(String userID
			,String condition){
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		HashMap<String,String> map = null;
		String sql = "select taskID,title,startDate,endDate,createUser from task where " +
				"taskID in (select taskID from taskUserArrange where userID = ?) "
				+" and title like ? or createUser like ? order by endDate desc";
		Cursor cr = db.rawQuery(sql,new String[]{userID,"%"+condition+"%","%"+condition+"%"});
		
		while(cr.moveToNext()){
			map = new HashMap<String,String>();
			map.put("taskID", cr.getString(0));
			map.put("title", cr.getString(1));
			map.put("startDate", cr.getString(2));
			map.put("endDate", cr.getString(3));
			map.put("teacherName", cr.getString(4));
			map.put("process",String.valueOf(getFinishedTestCount(userID,cr.getInt(0))));
			map.put("max",String.valueOf(getTestsCount(cr.getInt(0))));
			map.put("condition",condition);
			list.add(map);
		}
		
		cr.close();
		
		return list;
	}
	
	//������ѯ�ƻ��б�
	public ArrayList<HashMap<String,String>> getPlanListWithCondition(String userID,String keyword){
		
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		
		String sql = "select planID,title,beginDate,endDate from plan where createUserID = ?" +
				" and title like ? order by endDate desc";
		
		Cursor cr = db.rawQuery(sql, new String[]{userID,"%" + keyword + "%"});
		
		while(cr.moveToNext()){
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("planID", cr.getString(0));
			map.put("title", cr.getString(1));
			map.put("startDate", cr.getString(2));
			map.put("endDate", cr.getString(3));
			map.put("process", String.valueOf(getPlanFinishedTestCount(cr.getInt(0))));
			map.put("max",String.valueOf(getPlanTestListIDs(cr.getInt(0)).size()));
			map.put("condition",keyword);
			list.add(map);
		}
		
		cr.close();
				
		return list;
		
	}
	
	/**
	 * ���user�������б�
	 * @param type 0:ȫ������ҵ 1:δ���� 2:������ 3:δ��ֹ��ҵ 4:�ѽ�ֹ����ҵ
	 */
	public ArrayList<HashMap<String,String>> getTaskList(String userID,int type){
		
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		ArrayList<Integer> IDList = getTaskListIDs(userID, type);
		
		String sql = "select taskID,title,startDate,endDate,createUser from task where taskID = ?";
		Cursor cr = null; 
		
		for(int i = 0 ;i < IDList.size();i++){
			cr = db.rawQuery(sql, new String[]{String.valueOf(IDList.get(i))});
			if(cr.moveToNext()){
				HashMap<String,String> map = new HashMap<String,String>();
				map.put("taskID", cr.getString(0));
				map.put("title", cr.getString(1));
				map.put("startDate", cr.getString(2));
				map.put("endDate", cr.getString(3));
				map.put("teacherName", cr.getString(4));
				map.put("process",String.valueOf(getFinishedTestCount(userID,cr.getInt(0))));
				map.put("max",String.valueOf(getTestsCount(cr.getInt(0))));
				list.add(map);
			}
			cr.close();
		}
		//���ս�ֹ���ڵ�������
		Collections.sort(list, new Comparator<HashMap<String,String>>(){
			@Override
			public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
				// TODO Auto-generated method stub
				return rhs.get("endDate").compareTo(lhs.get("endDate"));
			}
		});
		
		return list;
		
	}
	/**
	 * ȡ�����������ǰ5������
	 */
	public ArrayList<HashMap<String,String>> getRecentTask(String userID){
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		String sql = "select taskID from taskUserArrange where userID = ? and time <> ? "
				+ "order by time desc limit 5";
		ArrayList<Integer> IDList = new ArrayList<Integer>();
		Cursor cr= db.rawQuery(sql,new String[]{userID,"0"});
		for(cr.moveToFirst();!cr.isAfterLast();cr.moveToNext()){
			IDList.add(cr.getInt(0));
			}
		cr.close();
		sql = "select taskID,title,startDate,endDate,createUser from task where taskID = ?";
		for(int i = 0;i < IDList.size();i++){
			HashMap<String,String> map = null;
			cr= db.rawQuery(sql,new String[]{String.valueOf(IDList.get(i))});
			for(cr.moveToFirst();!cr.isAfterLast();cr.moveToNext()){
				map = new HashMap<String,String>();
				map.put("taskID", cr.getString(0));
				map.put("title", cr.getString(1));
				map.put("startDate", cr.getString(2));
				map.put("endDate", cr.getString(3));
				map.put("teacherName", cr.getString(4));
				map.put("process",String.valueOf(getFinishedTestCount(userID,cr.getInt(0))));
				map.put("max",String.valueOf(getTestsCount(cr.getInt(0))));
				list.add(map);
				}
			cr.close();
		}
		
		return list;
	}
	
	/**
	 * ȡ�����������ǰ5���ƻ�
	 */
	public ArrayList<HashMap<String,String>> getRecentPlan(String userID){
		
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		String sql = "select * from plan where createUserID = ? and clickTime <> ? order by " +
				"clickTime desc limit 5";
		
		Cursor cr = db.rawQuery(sql, new String[]{userID,""});
		
		while(cr.moveToNext()){
			HashMap<String, String> map = new HashMap<String, String>();
			map.put("planID", cr.getString(0));
			map.put("title", cr.getString(1));
			map.put("startDate", cr.getString(3));
			map.put("endDate", cr.getString(4));
			map.put("process", String.valueOf(getPlanFinishedTestCount(cr.getInt(0))));
			map.put("max",String.valueOf(getPlanTestListIDs(cr.getInt(0)).size()));
			list.add(map);
		}
		
		return list;
		
	}
	
	/**
	 * ��������ID
	 * @param type 0:��������ID 1:δ��������ID 2:�Ѿ���������ID 3:δ��ֹ 4:�ѽ�ֹ
	 */
	public ArrayList<Integer> getTaskListIDs(String userID,int type){
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		String sql = null;
		
		Date date = new Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String now = format.format(date);
		Cursor cr = null;
		
		if(type == 3){
			sql = "select taskID from task where taskID in " +
					"(select taskID from taskUserArrange where userID = ?)" +
					" and endDate >= ?";
			cr = db.rawQuery(sql, new String[]{userID,now});
			
			while(cr.moveToNext()){
				list.add(cr.getInt(0));
			}
			
		}else{
			
			if(type == 4){
				sql = "select taskID from task where taskID in " +
						"(select taskID from taskUserArrange where userID = ?)" +
						" and endDate < ?";
				cr = db.rawQuery(sql, new String[]{userID,now});
				
				while(cr.moveToNext()){
					list.add(cr.getInt(0));
				}
			}else{
				sql = "select taskID from taskUserArrange where userID = ?";
				cr = db.rawQuery(sql, new String[]{userID});
				
				if(type == 0){
					while(cr.moveToNext()){
						list.add(cr.getInt(0));
					}
				}else{
					for(cr.moveToFirst();!cr.isAfterLast();cr.moveToNext()){
						int taskID = cr.getInt(0);
						//δ���ֵ���ҵ
						if(type == 1){
							if(!checkTaskScored(userID,taskID)){
								list.add(taskID);
							}
						//�Ѿ����ֵ���ҵ
						}else{
							if(checkTaskScored(userID,taskID)){
								list.add(taskID);
							}
						}	
					}
				}
			}
		}
		
		if(cr != null){
			cr.close();
		}
		
		return list;
	}
	//�������-�û���Ӧ��ϵ��
	public void addTaskUserArrangeList(String userID,ArrayList<Task> taskList){
		if(taskList != null ){
			String sql = "insert into taskUserArrange(userID,taskID,time) values(?,?,?)";
			for(int i = 0; i < taskList.size();i ++){
				int taskID = taskList.get(i).getTaskID();
				if(!checkTask_UserID(taskID,userID))
					db.execSQL(sql, new Object[]{userID,taskID,0});
				}
		}
	}
	
	//�������-�û���Ӧ��ϵ��
	public void addTaskUserArrangeList(String userID, int taskID) {
		
		String sql = "insert into taskUserArrange(userID,taskID,time) values(?,?,?)";

		if (!checkTask_UserID(taskID, userID)){
			db.execSQL(sql, new Object[] { userID, taskID, 0 });
		}	

	}
	//�������
	public void addTestLibrary(List<Test> list){
		if(list != null){
		String sql1 = "insert into testLibrary values(?,?,?,?,?,?,?)";
		String sql2 = "update testLibrary set testTypeID = ? , testTypeName = ? , testTitle = ? "
				+ ", testContent = ? where testID = ?";
		for(int i = 0;i < list.size();i++){
			int testID = list.get(i).getTestID();
			String createTime = list.get(i).getCreateTime();
			String createUser = list.get(i).getCreateUser();
			int typeID = list.get(i).getTestTypeID();
			String typeName = list.get(i).getTestTypeName();
			String title = list.get(i).getTestTitle();
			String content = list.get(i).getTestContent();
			if(!checkTestID(testID)){
				db.execSQL(sql1,new Object[]{testID,createTime,createUser,typeID,
						typeName,title,content});
			}else{
				db.execSQL(sql2, new Object[]{typeID,typeName,title,content,testID});
			}
		}
		}
		
	}
	//������Ŀ
	public void addTestItem(ArrayList<TestItem> list){
		String sql1 = "insert into testItem values(?,?,?,?)";
		String sql2 = "update testItem set testItemType = ? , testItemContent = ? where testItemID = ?";
		for(int i = 0;i < list.size();i++){
			int testItemID = list.get(i).getTestItemID();
			int testID = list.get(i).getTestID();
			int testItemType = list.get(i).getTestItemType();
			String testItemContent = list.get(i).getTestItemContent();
			if(!checkTestItemID(testItemID)){
				db.execSQL(sql1,new Object[]{testItemID,testID,testItemType,testItemContent});
			}else{
				db.execSQL(sql2,new Object[]{testItemType,testItemContent,testItemID});
			}
		}
	}
	//����ѡ��
	public void addTestOption(ArrayList<TestItemOption> list){
		String sql1 = "insert into testItemsOption values(?,?,?,?,?)";
		String sql2 = "update testItemsOption set testItemOptionContent = ? , testItemOptionIsAnswer = ? "
				+ ", testItemOptionRemark = ? where testItemOptionID = ?";
		for(int i = 0;i < list.size();i++){
			int testItemOptionID = list.get(i).getTestItemOptionID();
			int testItemID = list.get(i).getTestItemID();
			String testItemOptionContent = list.get(i).getTestItemOptionContent();
			int testItemOptionIsAnswer = list.get(i).getTestItemOptionIsAnswer();
			String remark = list.get(i).getTestItemOptionRemark();
			if(!checkOptionID(testItemOptionID)){
				db.execSQL(sql1,new Object[]{testItemOptionID,testItemID,testItemOptionContent
						,testItemOptionIsAnswer,remark});
			}else{
				db.execSQL(sql2, new Object[]{testItemOptionContent,testItemOptionIsAnswer
						,remark,testItemOptionID});
			}
		}
	}
	//������񸽼�
	public void addAttachment(ArrayList<Attachment> list){
		String sql = "insert into attachment values(?,?,?,?,?)";
		if(list != null){
			for(Attachment a:list){
				int id = a.getId();
				String oName = a.getOriginName();
				String nName = a.getNewName();
				String userID = a.getUserID();
				if(!checkPk_attachment(id, nName)){
					db.execSQL(sql,new Object[]{id,oName,nName,"",userID});
				}	
			}
		}
	}
	
	public void addAttachment(Attachment a){
		
		String sql = "insert into attachment values(?,?,?,?,?)";
		int id = a.getId();
		String oName = a.getOriginName();
		String nName = a.getNewName();
		String userID = a.getUserID();
		if(!checkPk_attachment(id, nName)){
			db.execSQL(sql,new Object[]{id,oName,nName,"",userID});
		}	

	}
	//����ҵĴ�
	public void addMyChoice(MyChoice choice){
		
		String sql = "insert into myChoice values(?,?,?,?,?)";
		String userID = choice.getUserID();
		int itemID = choice.getTestItemID();
		int taskID = choice.getTaskID();
		String answer = choice.getAnswer();
		String submitTime = choice.getSubmitTime();
		//�𰸲����ھ���� insert
		if(!checkUser_testItem_taskID(userID, itemID,taskID)){
			db.execSQL(sql,new Object[]{userID,itemID,taskID,answer,submitTime});
		}
		//�Ѿ��еĴ�ֻ����� update
		else{
			updateMyChoice(answer, submitTime, userID, itemID, taskID);
		}
	}
	public boolean checkUser_testItem_taskID(String userID,int testItemID,int taskID){
		boolean flag = true;
		String sql = "select count(*) from myChoice where userID = ? and testItemID = "
		+ testItemID+" and taskID = "+taskID;
		Cursor cr = db.rawQuery(sql,new String[]{userID});
		if(cr.moveToNext()){
			if(cr.getInt(0) == 0){
				flag = false;
			};
		}
		cr.close();
		return flag;
	}
	public boolean checkTestItemID(int id){
		boolean flag = true;
		String sql = "select count(*) from testItem where testItemID = "+id;
		Cursor cr = db.rawQuery(sql,null);
		if(cr.moveToNext()){
			if(cr.getInt(0) == 0){
				flag = false;
			};
		}
		cr.close();
		return flag;
	}
	public boolean checkOptionID(int id){
		boolean flag = true;
		String sql = "select count(*) from testItemsOption where testItemOptionID = "+id;
		Cursor cr = db.rawQuery(sql,null);
		if(cr.moveToNext()){
			if(cr.getInt(0) == 0){
				flag = false;
			};
		}
		cr.close();
		return flag;
	}
	public boolean checkTestID(int testID){
		boolean flag = true;
		String sql = "select count(*) from testLibrary where testID = "+testID;
		Cursor cr = db.rawQuery(sql,null);
		if(cr.moveToNext()){
			if(cr.getInt(0) == 0){
				flag = false;
			};
		}
		cr.close();
		return flag;
	}
	//��������б�
	public void addTaskList(ArrayList<Task> taskList){
		if(taskList != null){
			String sql1 = "insert into task values(?,?,?,?,?,?,?,?)";
			String sql2 = "update task set  title = ? , remark = ? , endDate = ? ,isDiscuss = ? "
					+ ", content = ? where taskID = ?";
			for(Task task:taskList){
				String createUser = task.getCreateUser();
				String title = task.getTitle();
				String remark = task.getRemark();
				String sDate = task.getStartDate();
				String eDate = task.getEndDate();
				String isDiscuss = task.getIsDiscuss();
				String content = task.getContent();
				int taskID = task.getTaskID();
				if(!checkTaskID(taskID)){
					db.execSQL(sql1,new Object[]{taskID,createUser,title,remark,sDate,eDate,isDiscuss,content});
				}else{
					db.execSQL(sql2, new Object[]{title,remark,eDate,isDiscuss,content,taskID});
				}	
			}
		}
	}
	//���һ������
	public void addTask(Task task){
		if(task != null){
			String sql1 = "insert into task values(?,?,?,?,?,?,?,?)";
			String sql2 = "update task set  title = ? , remark = ? , endDate = ? ,isDiscuss = ? "
					+ ", content = ? where taskID = ?";
			String createUser = task.getCreateUser();
			String title = task.getTitle();
			String remark = task.getRemark();
			String sDate = task.getStartDate();
			String eDate = task.getEndDate();
			String isDiscuss = task.getIsDiscuss();
			String content = task.getContent();
			int taskID = task.getTaskID();
			if(!checkTaskID(taskID)){
				db.execSQL(sql1,new Object[]{taskID,createUser,title,remark,sDate,eDate,isDiscuss,content});
			}else{
				db.execSQL(sql2, new Object[]{title,remark,eDate,isDiscuss,content,taskID});
			}	
		}
	}

	public boolean checkTask_TestID(int taskID,int testID){
		boolean flag = false;
		String sql = "select count(*) from taskTestArrange where taskID = "+taskID+" and testID = "
				+ testID;
		Cursor cr = db.rawQuery(sql,null);
		while(cr.moveToNext()){
			if(cr.getInt(0)!=0){
				flag = true;
			}
		}
		cr.close();
		return flag;
	}

	//�����ҵ-��Ŀ��Ӧ��
	public void addTaskTestArrange(int taskID,int testID){
		
		String sql = "insert into taskTestArrange(testID,taskID) values(?,?)";
		if(!checkTask_TestID(taskID, testID)){
			db.execSQL(sql, new Object[]{testID,taskID});
		}
	}
	
	public void addPlanTestArrange(int planID,int testID){
		
		String sql = "insert into planTestArrange values(?,?)";
		
		if(checkPk_planTestArrange(planID, testID)){
			db.execSQL(sql, new Object[]{planID,testID});
		}
		
	}

	
	public boolean checkPk_planTestArrange(int planID,int testID){
		
		boolean flag = false;
		String sql = "select count(planID) from planTestArrange where planID = " + planID
				+ " and testID = " + testID;
		
		Cursor cr = db.rawQuery(sql,null);
		
		if(cr.moveToNext()){
			flag = cr.getInt(0) == 1 ? false : true;
		}
		
		cr.close();
		
		return flag;
	}
	
	public boolean checkTaskID(int id){
		boolean flag = false;
		String sql = "select * from task where taskID = " + id;
		Cursor cr = db.rawQuery(sql,null);
		if(cr.moveToNext()){
			flag = true;
		}
		cr.close();
		return flag;
	}
	public boolean checkTask_UserID(int taskID,String userID){
		boolean flag = false;
		String sql = "select * from taskUserArrange where taskID = " + taskID +" and userID = ?";
		Cursor cr = db.rawQuery(sql,new String[]{userID});
		if(cr.moveToNext()){
			flag = true;
		}
		cr.close();
		return flag;
	}
	public int getFinishedTestCount(String userID,int taskID){
		
		int finishedTests = 0;
		ArrayList<Integer> testIDs = getTestListIDs(taskID);
		for(Integer testID : testIDs){
			if(checkTestFinished(taskID, testID, userID)){
				finishedTests++;
			}
		}
		return finishedTests;
	}
	
	public int getPlanFinishedTestCount(int planID){
		
		int finishedTests = 0;
		ArrayList<Integer> testIDs = getPlanTestListIDs(planID);
		for(Integer testID : testIDs){
			if(checkPlanTestFinished(planID, testID)){
				finishedTests++;
			}
		}
		return finishedTests;
	}
	//���������ɶ�
	public String getProcessPercent(int taskID,String userID){
		int allTests = getTestsCount(taskID);
		int finishedTests = getFinishedTestCount(userID,taskID);
		DecimalFormat df = new DecimalFormat("0");
		String percent = df.format(((float)finishedTests/allTests)*100)+"%";
		if(allTests == 0) 
			return null;
		else
			return percent;
		
		}
	//�������������Ŀ����
	public int getTestsCount(int taskID){
		int allTests = 0;
		String sql = "select count(*) from taskTestArrange where taskID = "+ taskID;
		Cursor cr = db.rawQuery(sql,null);
		if(cr.moveToNext()){
			allTests = cr.getInt(0);
		}
		cr.close();
		return allTests;
	}
	
	public ArrayList<TestItem> getTestItems(int testID){
		ArrayList<TestItem> list = new ArrayList<TestItem>();
		String sql = "select * from testItem where testID = "+testID;
		Cursor cr = db.rawQuery(sql,null); 
		while(cr.moveToNext()){
			TestItem testItem = new TestItem();
			testItem.setTestItemID(cr.getInt(0));
			testItem.setTestID(cr.getInt(1));
			testItem.setTestItemType(cr.getInt(2));
			testItem.setTestItemContent(cr.getString(3));
			list.add(testItem);
		}
		cr.close();
		return list;
	}
	public int getItemType(int itemID){
		int type = -1;
		String sql = "select testItemType from testItem where testItemID = "+ itemID;
		Cursor cr = db.rawQuery(sql,null);
		if(cr.moveToNext()){
			type = cr.getInt(0);
		}
		cr.close();
		return type;
	}
	public int getAllItemCount(int testID){
		int count = 0;
		String sql = "select count(*) from testItem where testID = "+testID;
		Cursor cr = db.rawQuery(sql,null);
		if(cr.moveToNext()){
			count = cr.getInt(0);
		}
		cr.close();
		return count;
	}
	public ArrayList<TestItemOption> getTestItemOption(int testItemID){
		ArrayList<TestItemOption> list = new ArrayList<TestItemOption>();
		String sql = "select * from testItemsOption where testItemID = "+testItemID;
		Cursor cr = db.rawQuery(sql,null); 
		while(cr.moveToNext()){
			TestItemOption t = new TestItemOption();
			t.setTestItemOptionID(cr.getInt(0));
			t.setTestItemID(cr.getInt(1));
			t.setTestItemOptionContent(cr.getString(2));
			t.setTestItemOptionIsAnswer(cr.getInt(3));
			list.add(t);
		}
		cr.close();
		return list;
	}
	//��װѡ��ʹ�
	public ArrayList<HashMap<String,String>> getQuestions(int testID){
		ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
		ArrayList<TestItem> itemList = getTestItems(testID);
		for(int i = 0;i < itemList.size();i ++){
			HashMap<String,String> map = new HashMap<String,String>();
			map.put("title", itemList.get(i).getTestItemContent());
			map.put("itemID", String.valueOf(itemList.get(i).getTestItemID()));
			ArrayList<TestItemOption> optionList = getTestItemOption(itemList.get(i).getTestItemID());
			for(int j = 0; j < optionList.size();j++){
				map.put("c"+j+"ID", String.valueOf(optionList.get(j).getTestItemOptionID()));
				map.put("c"+j, optionList.get(j).getTestItemOptionContent());
			}
			list.add(map);
			}
		return list;
	}
	//��ô𰸸���
	public int getOptionCount(int itemID){
		int count = -1;
		String sql = "select count(*) from testItemsOption where testItemID = "+itemID;
		Cursor cr = db.rawQuery(sql,null);
		if(cr.moveToNext()){
			count = cr.getInt(0);
		}
		cr.close();
		return count;
	}
	//����û��Ĵ�
	public String getAnswer(String userID ,int itemID,int taskID){
		String answer = "";
		String sql = "select answer from myChoice where testItemID = "+ itemID +" and userID = ?"
				+ " and taskID = "+taskID;
		Cursor cr = db.rawQuery(sql, new String[]{userID});
		if(cr.moveToNext()){
			answer = cr.getString(0);
		}
		cr.close();
		return answer;
	}
	
	//����û��ƻ��Ĵ�
	public String getPlanAnswer(int itemID,int planID){
		String answer = "";
		String sql = "select answer from planChoice where testItemID = ? and planID = ?";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(itemID)
				,String.valueOf(planID)});
		if(cr.moveToNext()){
			answer = cr.getString(0);
		}
		cr.close();
		return answer;
	}
	
	public ArrayList<String> getAnswer(String userID,int taskID){
		
		ArrayList<String> list = new ArrayList<String>();
		
		String sql = "select answer from myChoice where userID = ? and taskID = " + taskID;
		Cursor cr = db.rawQuery(sql, new String[]{userID});
		while(cr.moveToNext()){
			list.add(cr.getString(0));
		}
		cr.close();
		
		return list;
		
	}
	
	//����û��Ƿ��¼
	public boolean login(){
		
		boolean flag = false;
		String sql = "select login from user where login = 1";
		Cursor cr = db.rawQuery(sql,null);
		if(cr.moveToNext()){
			flag = true;
		}
		cr.close();
		return flag;
		
	}
	//�ж��Ѿ���¼�û�������
	public String getLoginUserType(){
		
		String userType = null;
		String sql = "select userType from user where login = 1";
		Cursor cr = db.rawQuery(sql, null);
		if(cr.moveToNext()){
			userType = cr.getString(0);
		}
		
		cr.close();
		
		return userType;
		
	}
	//�޸��û���¼״̬
	public void setLogin(String id,int state){
		if(!checkUserID(id)){
			String sql = "insert into user ( userID , login) values (?,?)";
			db.execSQL(sql,new String[]{id,String.valueOf(state)});
		}else{
			String sql = "update user set login = ? where userID = ?";
			db.execSQL(sql,new Object[]{state,id});
		}
	}
	
	public void addUserInfo(UserInfo userInfo){
		
		String userType = userInfo.getUserType();
		String userID = userInfo.getUserID();
		String userName = userInfo.getUserName();
		String userEmail = userInfo.getUserEmail();
		String userSchool = userInfo.getUserSchool();
		String userMajor = userInfo.getUserMajor();
		String dept = userInfo.getUserDepartment();
		String academy = userInfo.getUserAcademy();
		String grade = userInfo.getUserGrade();
		String userClass = userInfo.getUserClass();
		
		String sql = "update user set userType = ? ,userName = ? , email = ?" +
				" , school = ? , major = ? ,department = ? ,academy = ?," +
				" grade = ? ,class = ? where userID = ?";
		db.execSQL(sql, new Object[]{userType,userName,userEmail,
				userSchool,userMajor,dept,academy,grade,userClass,userID});
		
	}
	//����û���Ϣ
	public UserInfo getUserInfo(){
		UserInfo user = new UserInfo();
		String sql = "select * from user where login = 1";
		Cursor cr = db.rawQuery(sql,null);
		if(cr.moveToNext()){
			user.setUserID(cr.getString(0));
			user.setUserName(cr.getString(1));
			user.setUserEmail(cr.getString(2));
			user.setUserSchool(cr.getString(3));
			user.setUserDepartment(cr.getString(4));
			user.setUserAcademy(cr.getString(5));
			user.setUserGrade(cr.getString(6));
			user.setUserMajor(cr.getString(7));
			user.setUserClass(cr.getString(8));
			user.setUserType(cr.getString(11));
		}
		cr.close();
		return user;
	}
	
	public String getAttachmentDownloadTime(int id,String name){
		String time = null;
		String sql = "select downloadTime from attachment where id = "+id+" and newName = ?";
		Cursor cr = db.rawQuery(sql,new String[]{name});
		if(cr.moveToNext()){
			time = cr.getString(0);
		}
		cr.close();
		return time;
	}
	public String getExerciseDownloadTime(String userID){
		String time = "";
		String sql = "select exerciseDownloadTime from user where userID = ?";
		Cursor cr = db.rawQuery(sql,new String[]{userID});
		if(cr.moveToNext()){
			time = cr.getString(0);
		}
		if(time == null){
			time = "";
		}
		cr.close();
		return time;
	}
	public void setExerciseDownloadTime(String userID,String time){
		String sql = "update user set exerciseDownloadTime = ? where userID = ? ";
		db.execSQL(sql,new String[]{time,userID});
	}
	public boolean checkUserID(String id){
		boolean flag = false;
		String sql = "select * from user where userID = ?";
		Cursor cr = db.rawQuery(sql,new String[]{id});
		if(cr.moveToNext()){
			flag = true;
		}
		cr.close();
		return flag;
	}
	public boolean checkTestFinished(int taskID,int testID,String userID){
		
		boolean flag = true;
		ArrayList<Integer> itemIDs = getItemListIDs(testID);
		
		for(Integer itemID : itemIDs){
			if(getPlanAnswer(itemID, taskID).equals("")){
				flag = false;
				break;
			}
		}
		
		return flag;
	}
	
    public boolean checkPlanTestFinished(int planID,int testID){
		
		boolean flag = true;
		ArrayList<Integer> itemIDs = getItemListIDs(testID);
		
		for(Integer itemID : itemIDs){
			if(getPlanAnswer(itemID, planID).equals("")){
				flag = false;
				break;
			}
		}
		
		return flag;
	}
	
	public void setAttachmentDownloadTime(int id,String name,String time){
		String sql = "update attachment set downloadTime = ? where id = ? and newName = ?";
		db.execSQL(sql,new Object[]{time,id,name});
	}
	public void setSubmitTime(String userID ,int testItemID,int taskID,String submitTime){
		String sql = "update myChoice set submitTime = ? where testItemID = ? and " +
				"taskID = ? and userID = ?";
		db.execSQL(sql,new Object[]{submitTime,testItemID,taskID,userID});
	}
	//ͬ��web��
	public void updateMyChoice(String answer,String submitTime,String userID,int testItemID,int taskID){
		String sql = "update myChoice set answer = ? ,submitTime = ? where userID = ? "
				+ "and testItemID = ? and taskID = ? ";
		db.execSQL(sql,new Object[]{answer,submitTime,userID,testItemID,taskID});
	}
	public Task getTaskDetail(int taskID){
		Task task = null;
		String sql = "select * from task where taskID = "+ taskID;
		Cursor cr = db.rawQuery(sql,null);
		if(cr.moveToNext()){
			task = new Task();
			task.setTaskID(cr.getInt(0));
			task.setCreateUser(cr.getString(1));
			task.setTitle(cr.getString(2));
			task.setRemark(cr.getString(3));
			task.setStartDate(cr.getString(4));
			task.setEndDate(cr.getString(5));
			task.setIsDiscuss(cr.getString(6));
			task.setContent(cr.getString(7));
		}
		cr.close();
		return task;
	}
	public String getSubmitTime(String userID,int taskID,int itemID){
		String time = "";
		String sql = "select submitTime from myChoice where taskID = "+taskID+" and testItemID = "
		+itemID+" and userID = ?";
		Cursor cr = db.rawQuery(sql,new String[]{userID});
		if(cr.moveToNext()){
			time = cr.getString(0);
		}
		cr.close();
		return time;
	}
	
	public ArrayList<Attachment> getAttachment(int id){
		
		ArrayList<Attachment> list = new ArrayList<Attachment>();
		String sql = "select * from attachment where id = "+ id + " and userID = ''";
		Cursor cr = db.rawQuery(sql,null);
		while(cr.moveToNext()){
			Attachment a = new Attachment();
			a.setId(cr.getInt(0));
			a.setOriginName(cr.getString(1));
			a.setNewName(cr.getString(2));
			a.setDownloadTime(cr.getString(3));
			list.add(a);
		}
		cr.close();
		return list;
		
	}
	
	//���ѧ����web���Ѿ��ϴ��ĸ���
	public ArrayList<Attachment> getAttachment(int taskID,String userID){
		
		ArrayList<Attachment> list = new ArrayList<Attachment>();
		String sql = "select * from attachment where id = ? and userID = ?";
		
		Cursor cr = db.rawQuery(sql,new String[]{String.valueOf(taskID),userID});
		
		while(cr.moveToNext()){
			Attachment a = new Attachment();
			a.setId(cr.getInt(0));
			a.setOriginName(cr.getString(1));
			a.setNewName(cr.getString(2));
			a.setDownloadTime(cr.getString(3));
			a.setUserID(cr.getString(4));
			list.add(a);
		}
		cr.close();
		return list;
		
	}
	
	public void addFeedBack(String userID,FeedBack f){
		
		String sql = null;
		int taskID = f.getTaskID();
		String content = f.getFeedBackContent();
		String fTime = f.getFeedBackTime();
		String score = f.getScore();
		String sTime = f.getScoreTime();
		String openfraction = f.getOpenFraction();
		
		if(checkPK_Feedback(taskID, userID)){
			sql = "insert into feedBack values(?,?,?,?,?,?,?)";
			db.execSQL(sql,new Object[]{taskID,userID,content,fTime,score,sTime,openfraction});
		}else{
			sql ="update feedBack set feedBackContent = ? ,feedBackTime = ?,score = ?"
                 +" ,scoreTime = ? , openFraction = ? where userID = ? and taskID = ?";
			db.execSQL(sql, new Object[]{content,fTime,score,sTime,openfraction,userID,taskID});
		}
		
	}
	
	public boolean checkPK_Feedback(int taskID,String userID){
		
		boolean flag = true;
		String sql = "select count(taskID) from feedBack where taskID = ? and userID = ?";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(taskID),userID});
		
		if(cr.moveToNext()){
			flag = cr.getInt(0) == 1 ? false : true;
		}
		
		cr.close();
		
		return flag;
	}
	public FeedBack getFeedBack(String userID,int taskID){
		FeedBack f = new FeedBack();
		String sql = "select feedBackContent,feedBackTime,score,scoreTime ,openFraction from feedBack "
				+ " where taskID = ? and userID = ?";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(taskID),userID});
		if(cr.moveToNext()){
			f.setTaskID(taskID);
			f.setFeedBackContent(cr.getString(0));
			f.setFeedBackTime(cr.getString(1));
			f.setScore(cr.getString(2));
			f.setScoreTime(cr.getString(3));
			f.setOpenFraction(cr.getString(4));
		}
		
		cr.close();
		
		return f;
	}
	public boolean checkPk_attachment(int id,String newName){
		boolean flag = false;
		String sql = "select count(*) from attachment where id = ? and newName = ?";
		Cursor cr = db.rawQuery(sql,new String[]{String.valueOf(id),newName});
		if(cr.moveToNext()){
			if(cr.getInt(0) == 1){
				flag = true;
			}
		}
		cr.close();
		return flag;
	}
	
	/**
	 * ��¼������������ʱ��
	 */
	public void setRecentTask(String userID,int taskID,String time){
		String sql = "update taskUserArrange set time = ? where taskID = ? and userID = ?";
		db.execSQL(sql, new Object[]{time,taskID,userID});	
	}
	
	/**
	 * ��¼��������ƻ���ʱ��
	 */
	public void setRecentPlan(int planID,String time){
		String sql = "update plan set clickTime = ? where planID = ?";
		db.execSQL(sql,new Object[]{time,String.valueOf(planID)});	
	}
	
	/**
	 * �����ҵ�Ƿ��÷���
	 */
	public boolean checkTaskScored(String userID ,int taskID){
		boolean flag = false;//Ĭ��Ϊû�д��
		FeedBack f = getFeedBack(userID,taskID);
		if(!(f.getScore() == null || f.getScore().equals(""))){
			flag = true;
		}
		return flag;
	}
	
	public ArrayList<Integer> getTestListIDs(int taskID){
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		String sql = "select testID from taskTestArrange where taskID = " + taskID;
		Cursor cr = db.rawQuery(sql,null);
		while(cr.moveToNext()){
			list.add(cr.getInt(0));
		}
		cr.close();
		return list;
		
	}
	
	public ArrayList<Integer> getPlanTestListIDs(int planID){
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		String sql = "select testID from planTestArrange where planID = " + planID;
		Cursor cr = db.rawQuery(sql,null);
		while(cr.moveToNext()){
			list.add(cr.getInt(0));
		}
		cr.close();
		return list;
		
	}
	
	public ArrayList<Integer> getItemListIDs(int testID){
		ArrayList<Integer> list = new ArrayList<Integer>();
		String sql = "select testItemID from testItem where testID = " + testID;
		Cursor cr = db.rawQuery(sql,null);
		while(cr.moveToNext()){
			list.add(cr.getInt(0));
		}
		cr.close();
		return list;
	}
	
	public ArrayList<Integer> getOptionListIDs(int itemID){
		ArrayList<Integer> list = new ArrayList<Integer>();
		String sql = "select testItemOptionID from testItemsOption where testItemID = " + itemID;
		Cursor cr = db.rawQuery(sql,null);
		while(cr.moveToNext()){
			list.add(cr.getInt(0));
		}
		cr.close();
		return list;
	}
	
	public void deleteTask(int taskID){
		String sql = "delete from task where taskID = " + taskID;
		db.execSQL(sql);
	}
	
	public void deleteTaskUserArrange(String userID,int taskID){
		String sql = "delete from taskUserArrange where taskID = " + taskID +" and userID = ?";
		db.execSQL(sql,new Object[]{userID});
	}
	
	public void deleteTaskTestArrange(String userID,int taskID){
		String	sql = "delete from taskTestArrange where taskID = " + taskID;
		db.execSQL(sql,new Object[]{userID});
	}
	
	public void deleteTaskTestArrange(int taskID,int testID){
		String	sql = "delete from taskTestArrange where taskID = " + taskID +" and testID = " + testID;
		db.execSQL(sql);
	}
	
	public void deleteMyChoice(String userID,int taskID){
		
		String sql = "delete from myChoice where taskID = " + taskID +" and userID = ?";
		db.execSQL(sql,new Object[]{userID});
	}
	
	public void deleteMyChoice(String userID,int taskID,int itemID){
		
		String sql = "delete from myChoice where testItemID = " + itemID +" and userID = ? and taskID = "+ taskID;
		db.execSQL(sql,new Object[]{userID});
	}
	
	public void deleteAttachment(int id){
		String sql = "delete from attachment where id = " + id;
		db.execSQL(sql);
	}
	
	public void deleteAttachment(int id,String newName){
		String sql = "delete from attachment where id = " + id + " and newName = ?";
		db.execSQL(sql,new String[]{newName});
	}
	
	public void deleteAttachment(int taskID,String userID,String newName){
		String sql = "delete from attachment where id = ? and userID = ? and newName = ?";
		db.execSQL(sql,new String[]{String.valueOf(taskID),userID,newName});
	}
	
	public void deleteAttachment(String userID,int id){
		String sql = "delete from attachment where userID = ? and id = ?";
		db.execSQL(sql, new Object[]{userID,id});
	}
	
	public void deleteFeedBack(String userID,int taskID){
		String sql = "delete from feedBack where taskID = " + taskID +" and userID = ?";
		db.execSQL(sql,new Object[]{userID});
	}
	
	public void deleteItem(int testID,int itemID){
		String sql = "delete from testItem where testID = " + testID + " and testItemID = " + itemID;
		db.execSQL(sql);
	}
	
	public void deleteOption(int itemID,int optionID){
		String sql = "delete from testItemsOption where testItemID = " + itemID + 
				" and testItemOptionID = "+ optionID;
		db.execSQL(sql);
	}
	
	public void addUploadFiles(String userID,int taskID,String filePath
			,String uploadTime,String fileNewName){
		String sql = "insert into uploadFiles(taskID,userID,filePath,fileUploadTime,fileNewName)"
			+"values(?,?,?,?,?)";
		db.execSQL(sql, new Object[]{taskID,userID,filePath,uploadTime,fileNewName});
	}
	
	public ArrayList<String> getUploadFilePathList(String userID,int taskID){
		ArrayList<String> list = new ArrayList<String>();
		String sql = "select filePath from uploadFiles where userID = " + userID +
				     " and taskID = " + taskID;
		Cursor cr = db.rawQuery(sql,null);
		while(cr.moveToNext()){
			list.add(cr.getString(0));
		}
		cr.close();
		return list;
	}
	
	public ArrayList<UploadFiles> getUploadFile(String userID,int taskID){
		
		ArrayList<UploadFiles> list = new ArrayList<UploadFiles>();
		String sql = "select * from uploadFiles where userID = ? and taskID = ?";
		Cursor cr = db.rawQuery(sql,new String[]{userID,String.valueOf(taskID)});

		
		while(cr.moveToNext()){
			UploadFiles file = new UploadFiles();
			file.setTaskID(cr.getInt(1));
			file.setUserID(cr.getString(2));
			file.setFilePath(cr.getString(3));
			file.setFileUploadTime(cr.getString(4));
			file.setFileNewName(cr.getString(5));
			list.add(file);
		}
		
		cr.close();
		
		return list;
	}
	
	public String getUploadFileTime(String filePath){
		String time = "";
		String sql = "select fileUploadTime from uploadFiles where filePath = ?";
		Cursor cr = db.rawQuery(sql,new String[]{filePath});
		if(cr.moveToNext()){
			time = cr.getString(0);
		}
		cr.close();
		return time;
	}
	
	public void deleteUploadFiles(String fileNewName){
		
		String sql = "delete from uploadFiles where fileNewName = ?";
		db.execSQL(sql,new Object[]{fileNewName});
	}
	
	public void deleteUploadFilesFromFilePath(String filePath){
		
		String sql = "delete from uploadFiles where filePath = ?";
		db.execSQL(sql,new Object[]{filePath});
	}
	
	public void deleteUploadFiles(String userID,int taskID){
		
		String sql = "delete from uploadFiles where taskID = " + taskID + " and userID = ? ";
		db.execSQL(sql,new Object[]{userID});
	}
	
	public void setAnswersUploadTime(String userID,int taskID,String uploadTime){
		
		String sql = "update taskUserArrange set answerUploadTime = ? where taskID = ? " +
				"and userID = ?";
		db.execSQL(sql,new Object[]{uploadTime,taskID,userID});
	}
	
	public String getAnswersUploadTime(String userID,int taskID){
		
		String uploadTime = null;
		String sql = "select answerUploadTime from taskUserArrange where taskID = ? " +
				"and userID = ?";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(taskID),userID});
		if(cr.moveToNext()){
			uploadTime = cr.getString(0);
		}
		cr.close();
		return uploadTime;
		
	}
	
	public boolean isFilePathExisted(String filePath){
		
		boolean flag = true;
		String sql = "select count(filePath) from uploadFiles where filePath = ?";
		Cursor cr = db.rawQuery(sql, new String[]{filePath});
		
		if(cr.moveToNext()){
			flag = cr.getInt(0) == 1 ? true : false;
		}
		
		cr.close();
		
		return flag;
		
	}
	
	/**
	 * ��Ӽƻ�
	 * @return �����ƻ���ID,���ʧ���򷵻�-1
	 */
	public int addPlan(String title,String userID,String startDate,String endDate){
		
		int id = -1;
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		String createTime = format.format(new Date());
		
		String sql = "insert into plan(title,createUserID,beginDate,endDate,clickTime" +
				",createTime) values (?,?,?,?,?,?)";
		
		db.execSQL(sql,new Object[]{title,userID,startDate,endDate,"",createTime});
		
		sql = "select last_insert_rowid()";
		
		Cursor cr = db.rawQuery(sql,null);
		
		if(cr.moveToNext()){
			id = cr.getInt(0);
		}
		
		cr.close();
		
		return id;
		
	}
	
	public void updatePlan(int planID,String title,String sDate,String eDate ){
		String sql = "update plan set title = ? , beginDate = ?,endDate = ? where planID = ?";
		db.execSQL(sql, new Object[]{title,sDate,eDate,planID});
	}
	
	public void updatePlan(List<Plan> planList){
		
		String sql1 = "update plan set title = ? , beginDate = ?,endDate = ? " +
				"where createTime = ?";
		String sql2 = "insert into plan(title,createUserID,beginDate,endDate,clickTime" +
				",createTime) values (?,?,?,?,?,?)";
		
		for(Plan plan : planList){
			
			String title = plan.getTitle();
			String sDate = plan.getsDate();
			String eDate = plan.geteDate();
			String createTime = plan.getCreateTime();
			
			if(checkPlanCreateTime(createTime)){
				db.execSQL(sql2, new Object[]{title,plan.getUserID(),sDate,eDate,
						"",createTime});
			}else{
				db.execSQL(sql1, new Object[]{title,sDate,eDate,createTime});
			}
			
		}	
		
	}
	
	public boolean checkPlanCreateTime(String createTime){
		
		boolean flag = false;
		String sql = "select count(planID) from plan where createTime = ?";
		Cursor cr = db.rawQuery(sql, new String[]{createTime});
		
		if(cr.moveToNext()){
			flag = cr.getInt(0) == 1 ? false : true;
		}
		
		cr.close();
		
		return flag;
		
	}
	
	public HashMap<String, String> getPlan(int planID){
		HashMap<String, String> plan = null;
		
		String sql = "select * from plan where planID = ?";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(planID)});
		
		if(cr.moveToNext()){
			plan = new HashMap<String, String>();
			plan.put("planID", cr.getString(0));
			plan.put("title", cr.getString(1));
			plan.put("startDate", cr.getString(3));
			plan.put("endDate", cr.getString(4));
			plan.put("process",String.valueOf(getPlanFinishedTestCount(cr.getInt(0))));
			plan.put("max",String.valueOf(getPlanTestListIDs(cr.getInt(0)).size()));
		}
		
		cr.close();
		
		return plan;
	}
	
	//�������ͬ��web�˵�plan model
	public List<Plan> getPlanListForWebSync(String userID){
		
		List<Plan> planList = new ArrayList<Plan>();
		
		String sql = "select * from plan where createUserID = ? and endDate >= ?";
		String deadLine = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
		
		Cursor cr = db.rawQuery(sql, new String[]{userID,deadLine});
		
		while(cr.moveToNext()){
			Plan plan = new Plan();
			plan.setPlanID(cr.getInt(0));
			plan.setTitle(cr.getString(1));
			plan.setUserID(userID);
			plan.setsDate(cr.getString(3));
			plan.seteDate(cr.getString(4));
			plan.setCreateTime(cr.getString(6));
			planList.add(plan);
		}
		
		cr.close();
		
		return planList;
	}
	
	//�������ͬ��web�˵�plan model
	public List<Plan> getPlanListForWebSync(String userID,int planID){
		
		List<Plan> planList = new ArrayList<Plan>();
		
		String sql = "select * from plan where planID = " + planID;
		Cursor cr = db.rawQuery(sql, null);
		
		while(cr.moveToNext()){
			Plan plan = new Plan();	
			plan.setPlanID(planID);
			plan.setTitle(cr.getString(1));
			plan.setUserID(userID);
			plan.setsDate(cr.getString(3));
			plan.seteDate(cr.getString(4));
			plan.setCreateTime(cr.getString(6));
			planList.add(plan);
		}
		
		cr.close();
		
		return planList;
	}
	
	public List<PlanTestArrange> getPlanTestArrange(String userID){
		
		List<PlanTestArrange> list = new ArrayList<PlanTestArrange>();
		String sql = "select plan.createTime ,planTestArrange.testID from plan inner join " +
				" planTestArrange on plan.planID = planTestArrange.planID where " +
				" plan.createUserID = ? and plan.endDate >= ?";
		
		String deadLine = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
		
		Cursor cr = db.rawQuery(sql, new String[]{userID,deadLine});
		
		while(cr.moveToNext()){
			PlanTestArrange pa = new PlanTestArrange();
			pa.setCreateTime(cr.getString(0));
			pa.setTestID(cr.getInt(1));
			list.add(pa);
		}
		
		cr.close();
		
		return list;
		
	}
	
	public List<PlanTestArrange> getPlanTestArrange(int planID){
		
		List<PlanTestArrange> list = new ArrayList<PlanTestArrange>();
		String sql = "select plan.createTime ,planTestArrange.testID from plan inner join " +
				" planTestArrange on plan.planID = planTestArrange.planID where plan.planID = ?";
		
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(planID)});
		
		while(cr.moveToNext()){
			PlanTestArrange pa = new PlanTestArrange();
			pa.setCreateTime(cr.getString(0));
			pa.setTestID(cr.getInt(1));
			list.add(pa);
		}
		
		cr.close();
		
		return list;
		
	}
	
	/**
	 * ����û��ļƻ��б�
	 * @param type 0:ȫ���ƻ� 1��δ��ֹ�ƻ� 2���ѽ�ֹ�ƻ�
	 */
	public ArrayList<HashMap<String, String>> getPlanList(String userID,int type){
		
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String,String>>();
		
		String sql = null;
		Cursor cr = null;
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String now = format.format(new Date());
		
		switch (type) {
		case 0:
			sql = "select * from plan where createUserID = ?";
			cr = db.rawQuery(sql, new String[]{userID});		
			break;

		case 1:
			sql = "select * from plan where createUserID = ? and endDate >= ? order by " +
					" endDate desc";
			cr = db.rawQuery(sql, new String[]{userID,now});
			break;
		case 2:
			sql = "select * from plan where createUserID = ? and endDate < ? order by " +
					" endDate desc";
			cr = db.rawQuery(sql, new String[]{userID,now});

		}
		
		if(cr != null){
			while(cr.moveToNext()){
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("planID", cr.getString(0));
				map.put("title", cr.getString(1));
				map.put("startDate", cr.getString(3));
				map.put("endDate", cr.getString(4));
				map.put("process", String.valueOf(getPlanFinishedTestCount(cr.getInt(0))));
				map.put("max",String.valueOf(getPlanTestListIDs(cr.getInt(0)).size()));
				list.add(map);
			}
			cr.close();
		}
				
		return list;
		
	}
	
	public void deletePlans(List<Integer> planIDs){
		
		String sql = "delete from plan where planID = ?";
		
		for(int ids : planIDs){
			db.execSQL(sql, new Object[]{ids});
		}
		
	}
	
	public void deletePlan(int planID){
		
		String sql = "delete from plan where planID = ?";
		
		db.execSQL(sql, new Object[]{planID});
		
	}
	
	public void deletePlanTestArrange(int planID){
		String sql = "delete from planTestArrange where planID = " + planID;
		db.execSQL(sql);
	}
	
	public void deletePlanChoice(int planID){
		String sql = "delete from planChoice where planID = " + planID;
		db.execSQL(sql);
	}
	
	public void addTestTopType(TestType tp){
		
		String sql = null;
				
		if(checkTestTopTypeID(tp.getTestTypeID())){
			sql = "insert into testTopType values(?,?)";
			db.execSQL(sql, new Object[]{tp.getTestTypeID(),tp.getTestTypeName()});
		}else{
			sql = "update testTopType set testTopTypeName = ? where testTopTypeID = ?";
			db.execSQL(sql,new Object[]{tp.getTestTypeName(),tp.getTestTypeID()});
		}
	}
	
	public boolean checkTestTopTypeID(int typeID){
	
		boolean flag = false;
		String sql = "select count(testTopTypeID) from testTopType where testTopTypeID = ?";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(typeID)});
		
		if(cr.moveToNext()){
			if(cr.getInt(0) == 0){
				flag = true;
			}
		}
		
		cr.close();
		
		return flag;
	}
	
	//���ݿ��testType��Ӧmodel��testSubtype
	//���ݿ��testTopType��Ӧmodel��testType
	public void addTestType(TestSubType tst){
		String sql = null;
		
		if(checkTestTypeID(tst.getTestSubTypeID())){
			sql = "insert into testType values(?,?,?,?)";
			db.execSQL(sql, new Object[]{tst.getTestSubTypeID(),tst.getTestSubTypeName(),tst.getCount()
					,tst.getTestTypeID()});
		}else{
			sql = "update testType set testTypeName = ? ,testTopTypeID = ? ,testCount = ?" +
					"where testTypeID = ?";
			db.execSQL(sql,new Object[]{tst.getTestSubTypeName(),tst.getTestTypeID(),tst.getCount()
					,tst.getTestSubTypeID()});
		}
	}
	
	public boolean checkTestTypeID(int typeID){
		
		boolean flag = false;
		String sql = "select count(testTypeID) from testType where testTypeID = ?";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(typeID)});
		
		if(cr.moveToNext()){
			if(cr.getInt(0) == 0){
				flag = true;
			}
		}
		
		cr.close();
		
		return flag;
	}
	
	public PlanChoice getPlanChoice(int itemID,int planID){
		
		PlanChoice pc = new PlanChoice();
		String sql = "select * from planChoice where planID = ? and testItemID = ?";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(planID)
				,String.valueOf(itemID)});
		
		if(cr.moveToNext()){
			pc.setPlanID(cr.getInt(0));
			pc.setItemID(cr.getInt(1));
			pc.setAnswer(cr.getString(2));
			pc.setSubmitTime(cr.getString(3));
		}
		
		cr.close();
		
		return pc;
		
	}
	
	public List<PlanChoice> getPlanChoice(int planID){
		
		List<PlanChoice> list = new ArrayList<PlanChoice>();
		String sql = "select * from planChoice where planID = ?";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(planID)});
		
		while(cr.moveToNext()){
			PlanChoice pc = new PlanChoice();
			pc.setPlanID(cr.getInt(0));
			pc.setItemID(cr.getInt(1));
			pc.setAnswer(cr.getString(2));
			pc.setSubmitTime(cr.getString(3));
			list.add(pc);
		}
		
		cr.close();
		
		return list;
		
	}
	
	public List<PlanChoice> getPlanChoiceForWebSync(String userID){
		
		List<PlanChoice> list = new ArrayList<PlanChoice>();
		String sql = "select plan.createTime,planChoice.testItemID,planChoice.answer," +
				" planChoice.submitTime from plan inner join planChoice on " +
				" plan.planID = planChoice.planID where plan.createUserID = ? and " +
				" plan.endDate >= ?";
		
		String deadLine = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
		
		Cursor cr = db.rawQuery(sql, new String[]{userID,deadLine});
		
		while(cr.moveToNext()){
			PlanChoice pc = new PlanChoice();
			pc.setCreateTime(cr.getString(0));
			pc.setItemID(cr.getInt(1));
			pc.setAnswer(cr.getString(2));
			pc.setSubmitTime(cr.getString(3));
			list.add(pc);
		}
		
		cr.close();
		
		return list;
	}
	
	public List<PlanChoice> getPlanChoiceForWebSync(int planID){
		
		List<PlanChoice> list = new ArrayList<PlanChoice>();
		String sql = "select plan.createTime,planChoice.testItemID,planChoice.answer," +
				" planChoice.submitTime from plan inner join planChoice on " +
				" plan.planID = planChoice.planID where plan.planID = ?";
			
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(planID)});
		
		while(cr.moveToNext()){
			PlanChoice pc = new PlanChoice();
			pc.setCreateTime(cr.getString(0));
			pc.setItemID(cr.getInt(1));
			pc.setAnswer(cr.getString(2));
			pc.setSubmitTime(cr.getString(3));
			list.add(pc);
		}
		
		cr.close();
		
		return list;
	}
	
	public String getRencentPlanChoiceTime(int planID){
		String time = "";
		String sql = "select submitTime from planChoice where planID = ? order by submitTime" +
				" desc limit 1";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(planID)});
		
		if(cr.moveToNext()){
			time = cr.getString(0);
		}
		
		cr.close();
				
		return time;
	}
	
	//�ƻ�������Ŀʱ��table��������
	public List<PlanArrangeTableData> getPlanArrangeTableData(List<TestSubType> tsts,int planID){
		
		List<PlanArrangeTableData> list = new ArrayList<PlanArrangeTableData>();
		
		for(TestSubType tst : tsts){
			PlanArrangeTableData data = new PlanArrangeTableData();
			data.setId(tst.getTestSubTypeID());
			data.setLevel(tst.getTestSubTypeName());
			int addedTestCount = getArrangedPlanTestCount(tst.getTestSubTypeID(),planID);
			data.setAddedTest(String.valueOf(addedTestCount));
			int sumCount = tst.getCount();
			
			if(sumCount == 0){
				data.setAvailableTest("0");
			}else{
				data.setAvailableTest(String.valueOf(sumCount-addedTestCount));
			}
			
			list.add(data);
		}
		
		return list;
		
	}
	
	//������ϰʱ ���table�б���Ϣ
	public List<PlanArrangeTableData> getPlanArrangeTableData(List<TestSubType> tsts){
		
		List<PlanArrangeTableData> list = new ArrayList<PlanArrangeTableData>();
		
		for(TestSubType tst : tsts){
			PlanArrangeTableData data = new PlanArrangeTableData();
			data.setId(tst.getTestSubTypeID());
			data.setLevel(tst.getTestSubTypeName());
			int sumCount = tst.getCount();
			
			if(sumCount == 0){
				data.setAvailableTest("0");
			}else{
				data.setAvailableTest(String.valueOf(sumCount));
			}
			
			list.add(data);
		}
		
		return list;
		
	}
	
	//��üƻ�������������Ŀ��(�����Ѷȵȼ�����)
	public int getArrangedPlanTestCount(int testSubTypeID,int planID){
		int count = 0;
		
		String sql = "select count(testLibrary.testID) from testLibrary inner join " +
				" planTestArrange on testLibrary.testID = planTestArrange.testID " +
				" where testLibrary.testTypeID = ? and planID = ?";
		Cursor cr = db.rawQuery(sql, new String[]{String.valueOf(testSubTypeID)
				,String.valueOf(planID)});
		
		if(cr.moveToNext()){
			count = cr.getInt(0);
		}
		
		cr.close();
		
		return count;
	}
	
	public void removePlanTests(int planID){
		String sql = "delete from planTestArrange where planID = " + planID;
		db.execSQL(sql);
	}
	
	public void addPlanChoice(PlanChoice pc){
		String sql = null;
		
		int planID = pc.getPlanID();
		int testItemID = pc.getItemID();
		String answer = pc.getAnswer();
		String submitTime = pc.getSubmitTime();
		
		if(checkPK_PlanChoice(planID, testItemID)){
			sql = "insert into planChoice values(?,?,?,?)";
			db.execSQL(sql,new Object[]{planID,testItemID,answer,submitTime});
		}else{
			sql = "update planChoice set answer = ? , submitTime = ? where  " +
					" planID = ? and testItemID = ?";
			db.execSQL(sql, new Object[]{answer,submitTime,planID,testItemID});
		}
	}
	
	public boolean checkPK_PlanChoice(int planID,int testItemID){
		boolean flag = false;
		String sql = "select count(planID) from planChoice where planID = " + planID
				+ " and testItemID = " + testItemID;
		Cursor cr = db.rawQuery(sql, null);
		
		if(cr.moveToNext()){
			flag = cr.getInt(0) == 1 ? false : true;
		}
		
		cr.close();
		
		return flag;
	}
	
	public int getPlanID(String createTime){
			
		String sql = "select planID from plan where createTime = ?";
		Cursor cr = db.rawQuery(sql, new String[]{createTime});
		
		if(cr.moveToNext()){
			return cr.getInt(0);
		}
		
		cr.close();
		
		return -1;
		
	}
	
	/**
	 * 
	 * @param testTopTypeID = -1 ��ʾȫ��
	 * @return
	 */
	public List<TestType> getTestTopType(int testTopTypeID){
		
		String sql = null;
		List<TestType> list = new ArrayList<TestType>();
		
		if(testTopTypeID == -1){
			sql = "select * from testTopType";
		}else{
			sql = "select * from testTopType where testTopTypeID = " + testTopTypeID;
		}
		
		Cursor cr = db.rawQuery(sql, null);
		
		while(cr.moveToNext()){
			TestType tp = new TestType();
			tp.setTestTypeID(cr.getInt(0));
			tp.setTestTypeName(cr.getString(1));
			list.add(tp);
		}
		
		cr.close();
		
		return list;
		
	}
	
	/**
	 * 
	 * @param testTypeID = -1 ��ʾȡ��ȫ������
	 * @return
	 */
	public List<TestSubType> getTestType(int testTopTypeID){
		
		List<TestSubType> list = new ArrayList<TestSubType>();
		String sql = null;
		
		if(testTopTypeID == -1){
			sql = "select * from testType";
		}else{
			sql = "select * from testType where testTopTypeID = " + testTopTypeID;
		}
		
		Cursor cr = db.rawQuery(sql, null);
		
		while(cr.moveToNext()){
			TestSubType tst = new TestSubType();
			tst.setTestSubTypeID(cr.getInt(0));
			tst.setTestSubTypeName(cr.getString(1));
			tst.setCount(cr.getInt(2));
			tst.setTestTypeID(cr.getInt(3));
			list.add(tst);
		}
		
		cr.close();
		
		return list;
		
	}
	
	/**
	 * ��Ŀ��Դ���͵�������Ҫ��testLibrary��testType���ű߼������,����ֱ��ʹ��testCount�ֶ�
	 * @param testTypeID = -1 ��ʾȡ��ȫ������
	 * @return
	 */
	public List<TestSubType> getTestTypeWithoutCount(int testTopTypeID){
		
		List<TestSubType> list = new ArrayList<TestSubType>();
		String sql = null;
		
		if(testTopTypeID == -1){
			sql = "select * from testType";
		}else{
			sql = "select * from testType where testTopTypeID = " + testTopTypeID;
		}
		
		Cursor cr = db.rawQuery(sql, null);
		
		while(cr.moveToNext()){
			TestSubType tst = new TestSubType();
			tst.setTestSubTypeID(cr.getInt(0));
			tst.setTestSubTypeName(cr.getString(1));
			tst.setCount(getTestTypeCount(cr.getInt(0)));
			tst.setTestTypeID(cr.getInt(3));
			list.add(tst);
		}
		
		cr.close();
		
		return list;
		
	}
	
	/**
	 * ���ĳ�������͵���Ŀ����
	 */
	public int getTestTypeCount(int testTypeID){
		
		String sql = "select count(testTypeID) from testLibrary where testTypeID = " + testTypeID;
		
		Cursor cr = db.rawQuery(sql, null);
		
		if(cr.moveToNext()){
			return cr.getInt(0);
		}
		
		cr.close();
		
		return 0;
		
	}
	
	public boolean isLocalResourceAvailable(){
		
		String sql = "select count(*) from testTopType";
		
		Cursor cr = db.rawQuery(sql, null);
		
		if(cr.moveToNext()){
			return true;
		}
		
		cr.close();
		
		return false;
		
	}
	
	/**
	 * 
	 * @param range  (1,2,3)
	 * @return
	 */
	public List<Test> getTestListByTestIDs(String range){
		
		List<Test> list = new ArrayList<Test>();
		String sql = "select * from testLibrary where testID in " + range;
		
		Cursor cr = db.rawQuery(sql, null);
		
		while(cr.moveToNext()){
			Test test = new Test();
			test.setTestID(cr.getInt(0));
			test.setCreateTime(cr.getString(1));
			test.setCreateUser(cr.getString(2));
			test.setTestTypeID(cr.getInt(3));
			test.setTestTypeName(cr.getString(4));
			test.setTestTitle(cr.getString(5));
			test.setTestContent(cr.getString(6));
			list.add(test);
		}
		
		cr.close();
		
		return list;
		
	}
	
	public List<Integer> getTestIDsAtRandom(String ids,String counts){
		
		List<Integer> list = new ArrayList<Integer>();
		
		String[] idList = ids.split(",");
		String[] countList = counts.split(",");
 		String sql = "select testID from testLibrary where testTypeID = ? order by RANDOM() limit ? ";
		
		for(int i = 0; i < idList.length; i++){
			
			Cursor cr = db.rawQuery(sql, new String[]{idList[i],countList[i]});
			
			while(cr.moveToNext()){
				list.add(cr.getInt(0));
			}
			
			cr.close();
			
		}
		
		return list;
		
	}
	
}