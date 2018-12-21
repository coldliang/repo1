package com.cqupt.util;

import com.cqupt.model.AppInfo;
import com.cqupt.model.Attachment;
import com.cqupt.model.Chapter;
import com.cqupt.model.ChapterGroup;
import com.cqupt.model.Comment;
import com.cqupt.model.FeedBack;
import com.cqupt.model.Lesson;
import com.cqupt.model.Plan;
import com.cqupt.model.PlanChoice;
import com.cqupt.model.PlanTestArrange;
import com.cqupt.model.ResourceType;
import com.cqupt.model.ScoreStudentItem;
import com.cqupt.model.ScoreTestItem;
import com.cqupt.model.StudentUploadedFiles;
import com.cqupt.model.Task;
import com.cqupt.model.Test;
import com.cqupt.model.TestDetailForArrange;
import com.cqupt.model.TestItem;
import com.cqupt.model.TestItemOption;
import com.cqupt.model.TestSubType;
import com.cqupt.model.TestType;
import com.cqupt.model.UserInfo;

import org.ksoap2.serialization.SoapObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XMLParser {
	
	public static int parseInt(SoapObject s){
		
		return Integer.parseInt(s.getProperty(0).toString());
		
	}
	
	public static String parseBoolean(SoapObject soapObject) {
		
		String result = null;
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			result = soapObject.getProperty(0).toString();
		}
		return result;
		
	}
	
	public static Task parseTeacherTaskDetail(SoapObject soapObject){
		
		Task task = new Task();
		
		if (soapObject.getProperty(0).toString().equals("anyType{}")) {
			return task;
		}
		
		String buffer =  soapObject.getProperty(0).toString().substring(8);
		String[] list = buffer.split("eXiT; ");
		
		task.setTitle(list[0].substring(7));
		task.setRemark(list[1].substring(7));
		task.setStartDate(list[2].substring(7));
		task.setEndDate(list[3].substring(7));
		task.setIsDiscuss(list[4].substring(7));
		task.setContent(list[5].substring(7));
		task.setStudents(Integer.parseInt(list[6].substring(7)));
		task.setExams(Integer.parseInt(list[7].substring(7)));
		
		return task;
		
	}
	
	public static ArrayList<Task> parseTeacherTaskList(SoapObject soapObject){
		
		ArrayList<Task> taskList = new ArrayList<Task>();
		
		if (soapObject.getProperty(0).toString().equals("anyType{}")) {
			return taskList;
		}
		
		Task task = new Task();
		String buffer =  soapObject.getProperty(0).toString().substring(8);
		String[] list = buffer.split("; ");
		for(int i = 0;i < list.length-1;i ++){
			switch(i%6){
			case 0 : task.setStartDate(list[i].substring(7));break;
			case 1 : task.setEndDate(list[i].substring(7));break;
			case 2 : task.setTitle((list[i].substring(7)));break;
			case 3 : task.setTaskID(Integer.valueOf(list[i].substring(7)));break;
			case 4 : task.setStudents(Integer.valueOf(list[i].substring(7)));break;
			case 5 : task.setExams(Integer.valueOf(list[i].substring(7)));
			         taskList.add(task);task = new Task();break;
			}
		}
		
		return taskList;
	}
	
	public static ArrayList<HashMap<String, String>> parseTeacherTaskListToHashMap(SoapObject soapObject){
		
		ArrayList<HashMap<String, String>> taskList = new ArrayList<HashMap<String,String>>();
		
		if (soapObject.getProperty(0).toString().equals("anyType{}")) {
			return taskList;
		}
		
		HashMap<String, String> map = new HashMap<String, String>();
		String buffer =  soapObject.getProperty(0).toString().substring(8);
		String[] list = buffer.split("; ");
		for(int i = 0;i < list.length-1;i ++){
			switch(i%6){
			case 0 : map.put("sDate", list[i].substring(7));break;
			case 1 : map.put("eDate", list[i].substring(7));break;
			case 2 : map.put("title", list[i].substring(7));break;
			case 3 : map.put("taskID", list[i].substring(7));break;
			case 4 : map.put("stuCount", list[i].substring(7));break;
			case 5 : map.put("examCount", list[i].substring(7));
			         taskList.add(map);map = new HashMap<String, String>();break;
			}
		}
		
		return taskList;
	}
	
	public static ArrayList<Task> parseTaskList(SoapObject soapObject){
		//û����ҵ
		if (soapObject.getProperty(0).toString().equals("anyType{}")) {
			return null;
		}else{
			ArrayList<Task> taskList = new ArrayList<Task>();
			Task task = new Task();
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");
			for(int i = 0;i < list.length-1;i ++){
				switch(i%8){
				case 0:task.setCreateUser(list[i].substring(7));break;
				case 1:task.setTitle((list[i].substring(7)));break;
				case 2:
					String remark = list[i].substring(7);
					task.setRemark(remark.equalsIgnoreCase("null") ? "" : remark);break;
				case 3:task.setStartDate(list[i].substring(7));break;
				case 4:task.setEndDate(list[i].substring(7));break;
				case 5:task.setIsDiscuss(list[i].substring(7));break;
				case 6:
					String content = list[i].substring(7);
					task.setContent(content.equalsIgnoreCase("null") ? "" : content);break;
				case 7:task.setTaskID(Integer.parseInt(list[i].substring(7)));
					   taskList.add(task);task = new Task();break;
				}
				
			}
			return taskList;
		}
	}
	
	public static ArrayList<Task> parseFinishedTaskList(SoapObject soapObject){
		
		ArrayList<Task> taskList = new ArrayList<Task>();
		
		if (soapObject.getProperty(0).toString().equals("anyType{}")) {
			return taskList;
		}
		
		Task task = new Task();
		String buffer =  soapObject.getProperty(0).toString().substring(8);
		String[] list = buffer.split("; ");
		for(int i = 0;i < list.length-1;i ++){
			switch(i%4){
			case 0:task.setTaskID(Integer.parseInt(list[i].substring(7)));break;
			case 1:task.setTitle((list[i].substring(7)));break;
			case 2:task.setCreateUser(list[i].substring(7));break;
			case 3:task.setEndDate(list[i].substring(7));
			       taskList.add(task);task = new Task();break;
				   
			}
			
		}
		return taskList;
	}
	
	public static ArrayList<Test> parseTestList(SoapObject soapObject){
		ArrayList<Test> testList = null;
		Test test = new Test();
		
		if (soapObject.getProperty(0).toString().equals("anyType{}")) {
			return testList;
		} else {
			testList = new ArrayList<Test>();
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");
			for(int i = 0;i < list.length-1;i ++){
				switch(i%7){
				case 0:test.setCreateUser(list[i].substring(7));break;	
				case 1:test.setTestID(Integer.parseInt(list[i].substring(7)));break;
				case 2:test.setTestTitle(list[i].substring(7));break;
				case 3:test.setTestContent(list[i].substring(7));break;
				case 4:test.setCreateTime(list[i].substring(7));break;
				case 5:test.setTestTypeID(Integer.parseInt(list[i].substring(7)));break;
				case 6:test.setTestTypeName(list[i].substring(7));
				       testList.add(test);test = new Test();break;
				}
			}
			return testList;
			}
	}
	public static ArrayList<TestItem> parseTestItem(SoapObject soapObject){
		ArrayList<TestItem> testItemList = null;
		
		if (!soapObject.getProperty(0).toString().equals("anyType{}")) {
			testItemList = new ArrayList<TestItem>();
			TestItem testItem = new TestItem();
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");
			for(int i = 0;i < list.length-1;i ++){
				switch(i%4){
				case 0: testItem.setTestItemID(Integer.parseInt(list[i].substring(7)));break;	
				case 1: testItem.setTestID(Integer.parseInt(list[i].substring(7)));break;
				case 2: testItem.setTestItemContent((list[i].substring(7)));break;
				case 3: testItem.setTestItemType(Integer.parseInt(list[i].substring(7)));
						testItemList.add(testItem);testItem = new TestItem();break;   
				}
			}
			}
		return testItemList;
			
	}
	public static ArrayList<TestItemOption> parseTestItemOption(SoapObject soapObject){
		ArrayList<TestItemOption> testItemOptionList = new ArrayList<TestItemOption>();
		if (!soapObject.getProperty(0).toString().equals("anyType{}")){
			testItemOptionList = new ArrayList<TestItemOption>();
			TestItemOption testItemOption = new TestItemOption();
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");
			int flag = 0;
			for(int i = 0;i < list.length-1;i ++){
				flag = 0;//Ĭ�ϲ�����ȷѡ��
				switch(i%5){
				case 0: testItemOption.setTestItemOptionID(Integer.parseInt(list[i].substring(7)));break;	
				case 1: testItemOption.setTestItemID(Integer.parseInt(list[i].substring(7)));break;
				case 2: testItemOption.setTestItemOptionContent((list[i].substring(7)));break;
				case 3: if(list[i].substring(7).equals("True")) flag = 1;
						testItemOption.setTestItemOptionIsAnswer(flag);break;
				case 4: testItemOption.setTestItemOptionRemark(list[i].substring(7));
						testItemOptionList.add(testItemOption);testItemOption = new TestItemOption();break;   
				}
			}
		}
		return testItemOptionList;	
	}
	static public HashMap<String,String> parseAnswers(SoapObject soapObject){
		
		HashMap<String,String> map = null;
		//web�˽��й�����
		if (!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");
			map = new HashMap<String,String>();
			map.put("answer",list[0].substring(7));
			map.put("submitTime",list[1].substring(7));
		}
		return map;
	}
	public static ArrayList<Comment> parseCommentList(SoapObject soapObject){
		ArrayList<Comment> commentList = new ArrayList<Comment>();
		Comment comment = new Comment();
		if(soapObject.getProperty(0).toString().equals("anyType{}")){
			return commentList;
		}else{
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");
			for(int i = 0;i < list.length-1;i++){
				switch(i%5){
				case 0:comment.setDiscussID(Integer.parseInt((list[i].substring(7))));break;
				case 1:comment.setContent((list[i].substring(7)));break;
				case 2:comment.setUserID(list[i].substring(7));break;
				case 3:comment.setTime(list[i].substring(7));break;
				case 4:comment.setUserName(list[i].substring(7)); 
					   commentList.add(comment);comment = new Comment();break;
				}
			}
			return commentList;
		}
	
	}
	
	static public int parseCommentCount(SoapObject soapObject){
		
		int count = 0;
		count = Integer.parseInt(soapObject.getProperty(0).toString());
		return count;
	}
	
	static public ArrayList<Attachment> parseAttachment(SoapObject soapObject){
		ArrayList<Attachment> attachmentList = new ArrayList<Attachment>();
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			Attachment attachment = new Attachment();
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");
			for(int i = 0; i<list.length-1;i++){
				switch(i%3){
				case 0: attachment.setOriginName(list[i].substring(7));break;
				case 1: attachment.setNewName(list[i].substring(7));break;
				case 2: attachment.setId(Integer.parseInt(list[i].substring(7)));
						attachmentList.add(attachment);attachment = new Attachment();
				}
			}
		}
		return attachmentList;
		
	}
	
	public static ArrayList<Attachment> parseAttachment2(SoapObject soapObject){
		ArrayList<Attachment> attachmentList = new ArrayList<Attachment>();
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			Attachment attachment = new Attachment();
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");
			for(int i = 0; i<list.length-1;i++){
				switch(i%4){
				case 0: attachment.setOriginName(list[i].substring(7));break;
				case 1: attachment.setNewName(list[i].substring(7));break;
				case 2: attachment.setId(Integer.parseInt(list[i].substring(7)));break;
				case 3: attachment.setIsFileRead(list[i].substring(7).equals("�Ѷ�"));
						attachmentList.add(attachment);attachment = new Attachment();
				}
			}
		}
		return attachmentList;
		
	}
	
	public static FeedBack  parseFeedBack(SoapObject soapObject){
		FeedBack feedBack = new FeedBack();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");
			
			String content = list[0].substring(7);
			String feedbackTime = list[1].substring(7);
			String score = list[2].substring(7);
			String scoreTime = list[3].substring(7);
			String openFraction = list[4].substring(7);
			
			feedBack.setFeedBackContent(content.equalsIgnoreCase("null") ? "" : content);
			feedBack.setFeedBackTime(feedbackTime.equalsIgnoreCase("null") ? "" : feedbackTime);
			feedBack.setScore(score.equalsIgnoreCase("null") ? "" : score);
			feedBack.setScoreTime(scoreTime.equalsIgnoreCase("null") ? "" : scoreTime);
			feedBack.setOpenFraction(openFraction);
		}
		return feedBack;
	}
	
	public static AppInfo parseAppInfo(SoapObject soapObject){
		
		AppInfo info = new AppInfo();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			info.setPublishDate(list[0].substring(7));
			info.setVersionName(list[1].substring(7));
			info.setSize(list[2].substring(7));
			info.setContent(list[3].substring(7));
		}
		
		return info;
	}
	
	public static UserInfo ParseUserInfo(SoapObject soapObject){
		
		UserInfo info = new UserInfo();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			info.setUserName(list[0].substring(7));
			info.setUserEmail(list[1].substring(7));
			info.setUserDepartment(list[2].substring(7));
			info.setUserMajor(list[3].substring(7));
			info.setUserGrade(list[4].substring(7));
			info.setUserClass(list[5].substring(7));
			info.setUserSchool(list[6].substring(7));
			info.setUserAcademy(list[7].substring(7));
		}
		
		return info;
	}
	
	public static ArrayList<UserInfo> ParseUserInfoList(SoapObject soapObject){
		
		ArrayList<UserInfo> infoList = new ArrayList<UserInfo>();
		UserInfo info = new UserInfo();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			for(int i = 0;i < list.length-1;i++){
				switch(i%5){
				case 0 : info.setUserID(list[i].substring(7));break;
				case 1 : info.setUserName(list[i].substring(7));break;
				case 2 : info.setUserClass(list[i].substring(7));break;
				case 3 : info.setUserMajor(list[i].substring(7));break;
				case 4 : info.setUserAcademy(list[i].substring(7));
				         infoList.add(info);info = new UserInfo();break;
				}
			}
		}
		
		return infoList;
	}
	
	public static ArrayList<String> parseDepartmentInfo(SoapObject soapObject){
		
		ArrayList<String> deptInfo = new ArrayList<String>();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			for(int i = 0;i < list.length-1;i++){
				deptInfo.add(list[i].substring(7));
			}
		}
		
		return deptInfo;
		
	}
	
	public static ArrayList<String> parseMyListInfo(SoapObject soapObject){
		
		ArrayList<String> myListInfo = new ArrayList<String>();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			for(int i = 0;i < list.length-1;i++){
				myListInfo.add(list[i].substring(7));
			}
		}
		
		return myListInfo;
	}
	
	public static List<Integer> parseTestListIds(SoapObject soapObject){
		
		List<Integer> testList = new ArrayList<Integer>();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			for(int i = 0;i < list.length-1;i++){
				testList.add(Integer.parseInt(list[i].substring(7)));
			}
		}
		
		return testList;
	}
	
	public static List<ScoreStudentItem> parseScoreStudentItem(SoapObject soapObject){
		
		List<ScoreStudentItem> items = new ArrayList<ScoreStudentItem>();
		ScoreStudentItem item = new ScoreStudentItem();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			for(int i = 0;i < list.length-1;i++){
				switch(i%7){
				case 0 : item.setName(list[i].substring(7));break;
				case 1 :
					String score = list[i].substring(7);
					item.setScore(score.equals("null") ? "" : score);break;
				case 2 : 
					String time = list[i].substring(7);
					item.setSubmitTime(time.equals("null") ? "" : time);break;
				case 3 : item.setMax(Integer.parseInt(list[i].substring(7)));break;
				case 4 : item.setProcess(Integer.parseInt(list[i].substring(7)));break;
				case 5 : item.setReadedAttachmentCount(Integer.parseInt(list[i].substring(7)));break;
				case 6 : item.setNotReadedAttachmentCount(Integer.parseInt(list[i].substring(7)));
				         items.add(item);item = new ScoreStudentItem();break;
				}
			}
		}
		
		return items;
		
	}
	
	public static List<TestType> parseTestType(SoapObject soapObject){
		List<TestType> lists = new ArrayList<TestType>();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			TestType testType = new TestType();

			for(int i = 0;i < list.length-1;i++){
				switch(i%2){
				case 0 : testType.setTestTypeID(Integer.parseInt(list[i].substring(7)));break;
				case 1 : testType.setTestTypeName(list[i].substring(7));
				         lists.add(testType);testType = new TestType();break;
				}
			}
				
		}
		
		return lists;
	}
	
	public static List<TestSubType> parseTestSubType(SoapObject soapObject){
		List<TestSubType> lists = new ArrayList<TestSubType>();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			TestSubType testSubType = new TestSubType();
			
			
			for(int i = 0;i < list.length-1;i++){
				switch(i%4){
				case 0 : testSubType.setTestSubTypeID(Integer.parseInt(list[i].substring(7)));break;	
				case 1 : testSubType.setTestSubTypeName(list[i].substring(7));break;
				case 2 : testSubType.setTestTypeID(Integer.parseInt(list[i].substring(7)));break;
				case 3 : testSubType.setCount(Integer.parseInt(list[i].substring(7)));
				         lists.add(testSubType);testSubType = new TestSubType();
				         break;
				}
			}
				
		}
		
		return lists;
	}
	
	public static List<TestSubType> parseTestSubTypeWithoutTypeId(SoapObject soapObject){
		List<TestSubType> lists = new ArrayList<TestSubType>();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			TestSubType testSubType = new TestSubType();
			
			
			for(int i = 0;i < list.length-1;i++){
				switch(i%3){
				case 0 : testSubType.setTestSubTypeID(Integer.parseInt(list[i].substring(7)));break;	
				case 1 : testSubType.setTestSubTypeName(list[i].substring(7));break;
				case 2 : testSubType.setCount(Integer.parseInt(list[i].substring(7)));
				         lists.add(testSubType);testSubType = new TestSubType();
				         break;
				}
			}
				
		}
		
		return lists;
	}
	
	public static List<Plan> parsePlanList(SoapObject soapObject){
		List<Plan> lists = new ArrayList<Plan>();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			Plan plan = new Plan();
						
			for(int i = 0;i < list.length-1;i++){
				switch(i%6){
				case 0 : plan.setPlanID(Integer.parseInt(list[i].substring(7)));break;
				case 1 : plan.setTitle(list[i].substring(7));break;
				case 2 : plan.setUserID(list[i].substring(7));break;
				case 3 : plan.setsDate(list[i].substring(7));break;
				case 4 : plan.seteDate(list[i].substring(7));break;
				case 5 : plan.setCreateTime(list[i].substring(7));
				         lists.add(plan);plan = new Plan();break;
				}
			}
				
		}
		
		return lists;
			
	}
	
	public static List<PlanTestArrange> parsePlanTestArrange(SoapObject soapObject){
		
		List<PlanTestArrange> lists = new ArrayList<PlanTestArrange>();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			PlanTestArrange planTestArrange = new PlanTestArrange();
						
			for(int i = 0;i < list.length-1;i++){
				switch(i%2){
				case 0 : planTestArrange.setCreateTime(list[i].substring(7));break;
				case 1 : planTestArrange.setTestID(Integer.parseInt(list[i].substring(7)));
				         lists.add(planTestArrange);planTestArrange = new PlanTestArrange();
				         break;
				}
			}
				
		}
		
		return lists;
		
	}
	
	public static List<PlanChoice> parsePlanChoice(SoapObject soapObject){
		
		List<PlanChoice> lists = new ArrayList<PlanChoice>();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");
			
			PlanChoice planChoice = new PlanChoice();
						
			for(int i = 0;i < list.length-1;i++){
				switch(i%4){
				case 0 : planChoice.setCreateTime(list[i].substring(7));break;
				case 1 : planChoice.setItemID(Integer.parseInt(list[i].substring(7)));break;
				case 2 : planChoice.setAnswer(list[i].substring(7));break;
				case 3 : planChoice.setSubmitTime(list[i].substring(7));
				         lists.add(planChoice);planChoice = new PlanChoice();
				         break;
				}
			}
				
		}
		
		return lists;
		
	}
	
	public static List<TestDetailForArrange> parseTestDetailForArrange(SoapObject soapObject){
		List<TestDetailForArrange> lists = new ArrayList<TestDetailForArrange>();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			TestDetailForArrange test = new TestDetailForArrange();
						
			for(int i = 0;i < list.length-1;i++){
				switch(i%6){
				case 0 : test.setTestID(Integer.parseInt(list[i].substring(7)));break;
				case 1 : test.setTitle(list[i].substring(7));break;
				case 2 : test.setTestType(list[i].substring(7));break;
				case 3 : test.setLevel(list[i].substring(7));break;
				case 4 : test.setDomain(list[i].substring(7));break;
				case 5 : test.setCreateUser(list[i].substring(7));
				         lists.add(test);test = new TestDetailForArrange();
				         break;
				}
			}
				
		}
		
		return lists;
	}
	
	public static List<ScoreTestItem> parseScoreTestItem(SoapObject soapObject){
		
		List<ScoreTestItem> items = new ArrayList<ScoreTestItem>();
		ScoreTestItem item = new ScoreTestItem();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			for(int i = 0;i < list.length-1;i++){
				switch(i%6){
				case 0 : item.setTestID(Integer.parseInt(list[i].substring(7)));break;
				case 1 : item.setTitle(list[i].substring(7));break;
				case 2 : item.setCategory(list[i].substring(7));break;
				case 3 : item.setLevel(list[i].substring(7));break;
				case 4 : item.setObjectCount(list[i].substring(7));break;
				case 5 : item.setSubjectCount(list[i].substring(7));
				         items.add(item);item = new ScoreTestItem();break;
				}
			}
		}
		
		return items;
		
	}
	
	public static List<StudentUploadedFiles> parseStudentUploadedFiles(SoapObject soapObject){
		
		List<StudentUploadedFiles> files = new ArrayList<StudentUploadedFiles>();
		StudentUploadedFiles file = new StudentUploadedFiles();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");
			
			for(int i = 0;i < list.length-1;i++){
				switch(i%4){
				case 0 : file.setOriginName(list[i].substring(7));break;
				case 1 : file.setNewName(list[i].substring(7));break;
				case 2 : file.setSize(
						FileUtil.getFileSize(Long.parseLong(list[i].substring(7))));break;
				case 3 : file.setTime(list[i].substring(7));
				         files.add(file);file = new StudentUploadedFiles();break;
				}
			}
		}
		
		return files;
		
	}

	public static List<Lesson> parseLessonList(SoapObject soapObject){

		List<Lesson> lessons = new ArrayList<>();
		Lesson lesson = new Lesson();

		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");

			for(int i = 0;i < list.length-1;i++){
				switch(i%9){
					case 0 : lesson.setId(Integer.parseInt(list[i].substring(7)));break;
					case 1 : lesson.setTitle(list[i].substring(7));break;
					case 2 : lesson.setBackground(list[i].substring(7));break;
					case 3 : lesson.setGoal(list[i].substring(7));break;
					case 4 : lesson.setHours(list[i].substring(7));break;
					case 5 : lesson.setRefBook(list[i].substring(7));break;
					case 6 : lesson.setIntroduction(list[i].substring(7));break;
					case 7 : lesson.setCreateUserId(list[i].substring(7));break;
					case 8 : lesson.setImageUri("http://202.202.43.244/webcollegeTest/UploadFiles/WebLesson/ImagesFile/" + list[i].substring(7));
						lessons.add(lesson);lesson = new Lesson();break;
				}
			}
		}

		return lessons;

	}

	public static ArrayList<ChapterGroup> parseChapterGroup(SoapObject soapObject){

		ArrayList<ChapterGroup> groups = new ArrayList<>();
		List<Chapter> chapters = new ArrayList<>();
		Chapter chapter = new Chapter();

		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("eXiT; ");

			for(int i = 0;i < list.length-1;i++){
				switch(i%6){
					case 0 : chapter.setChapterID(Integer.parseInt(list[i].substring(7)));break;
					case 1 : chapter.setChapterTitle(list[i].substring(7));break;
					case 2 : chapter.setParentChapterID(Integer.parseInt(list[i].substring(7)));break;
					case 3 : chapter.setChapterContent(list[i].substring(7));break;
					case 4 : chapter.setLessonID(Integer.parseInt(list[i].substring(7)));break;
					case 5 : chapter.setAttachmentUri("http://202.202.43.244/webcollegetest/UploadFiles/WebLesson/"+list[i].substring(7));
					         chapters.add(chapter);chapter = new Chapter();break;
				}
			}

			//divide chapter into two group :parent and child
			for (Chapter chapter1 : chapters) {

				ChapterGroup group = new ChapterGroup();

				if(chapter1.getParentChapterID() == 0){//the parent

					group.setChapter(chapter1);
                    groups.add(group);

				}else{//the child

					for (ChapterGroup chapterGroup : groups) {

                        //find its parent
						if(chapterGroup.getChapter().getChapterID() == chapter1.getParentChapterID()){

							if(chapterGroup.getChildren() == null){
								chapterGroup.setChildren(new ArrayList<Chapter>());
							}

                            chapterGroup.getChildren().add(chapter1);

						}

					}

				}

			}
		}

		return groups;

	}
	
	public static StudentUploadedFiles parseFileInfo(SoapObject soapObject){
		
		StudentUploadedFiles file = new StudentUploadedFiles();
		
		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");
			
			for(int i = 0;i < list.length-1;i++){
				switch(i%3){
				case 0 : file.setOriginName(list[i].substring(7));break;
				case 1 : file.setTime(list[i].substring(7));break;
				case 2 : file.setSize(
						FileUtil.getFileSize(Long.parseLong(list[i].substring(7))));break;
				}
			}
		}
		
		return file;
		
	}

	public static ArrayList<ResourceType> parseLessonType(SoapObject soapObject){

		ArrayList<ResourceType> resList = new ArrayList<>();
		ResourceType res = new ResourceType();

		if(!soapObject.getProperty(0).toString().equals("anyType{}")){
			String buffer =  soapObject.getProperty(0).toString().substring(8);
			String[] list = buffer.split("; ");

			for(int i = 0;i < list.length-1;i++){
				switch(i%2){
					case 0 : res.setResType(Integer.parseInt(list[i].substring(7)));break;
					case 1 : res.setResName(list[i].substring(7));
						resList.add(res);res = new ResourceType();break;
				}
			}
		}

		return resList;

	}
	
}
