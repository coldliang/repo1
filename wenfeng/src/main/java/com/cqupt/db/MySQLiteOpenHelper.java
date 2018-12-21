package com.cqupt.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

	private static final String DATABASENAME = "wenfeng.db";
	private static final int DATABASEVERSION = 3;
	
	private final static String CREATE_TABLE_PLAN = "CREATE TABLE plan " + "("
			+" planID integer  primary key not null, "
			+" title  text , "
			+" createUserID varchar(50) ," 
			+" beginDate varchar(50) ," 
			+" endDate varchar(50) ," 
			+" clickTime  varchar(50) , "
			+" createTime varchar(50) )";
	
	private static final String CREATE_TABLE_FEEDBACK = "CREATE TABLE feedBack " + "("
			+ "taskID int  , "
			+ "userID  text , "
			+ "feedBackContent 	    text, "
			+" feedBackTime varchar(50) ," 
			+" score  varchar(5) ,"
			+" scoreTime  varchar(50) ,"
			+" openFraction varchar(5) , "
			+" constraint pk_feedBack primary key (taskID,userID) )";
	
	//��Ŀ��������
	private static final String CREATE_TABLE_TESTTYPE = "create table testType ("
			+" testTypeID integer primary key not null,"
			+" testTypeName varchar(50) ,"
			+" testCount int ,"
			+" testTopTypeID int )";
	
	private SQLiteDatabase db;
	private static Context context;
	
	private  MySQLiteOpenHelper(){
		
		super(context,DATABASENAME,null,DATABASEVERSION);
		
	}
	
	private static class innerClass{
		
		private static MySQLiteOpenHelper helper = new MySQLiteOpenHelper();
		
	}
	
	public static final MySQLiteOpenHelper getInstance(Context context){
		
		MySQLiteOpenHelper.context = context;
		return innerClass.helper;
		
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String sql = "CREATE TABLE task " + "("
				+ "taskID 	    integer primary key , "
				+ "createUser         varchar(50)          , "
				+ "title         varchar(50)          , "
				+ "remark text , "
				+ "startDate  varchar(50)   , "
				+ "endDate  varchar(50)  , "
				+ "isDiscuss varchar(10) ,"
				+" content text )";     
		db.execSQL(sql);
		
		sql = "CREATE TABLE testLibrary " + "("
				+ "testID 	    integer 	PRIMARY KEY ,"
				+ "createTime    varchar(100)  ,  "
				+ "createUser    varchar(100)  ,  "
				+ "testTypeID    int  ,  "
				+ "testTypeName    varchar(100)  ,  "
				+ "testTitle    varchar(100) ,  "
				+ "testContent   text )";
		db.execSQL(sql);
		
		//��Ŀһ������
		sql = "create table testTopType ("
				+ " testTopTypeID integer primary key not null,"
				+ " testTopTypeName varchar(50) )";
		db.execSQL(sql);
		
		//��Ŀ��������
		db.execSQL(CREATE_TABLE_TESTTYPE);
		
		sql = "CREATE TABLE taskUserArrange " + "("
				+ "userID 	    varchar(50) 	not null , "
				+ "taskID    integer         not null , "
				+ "time varchar(50)  ,"
				+ "answerUploadTime varchar(50)  ,"
				+ "constraint pk_taskUserArrange primary key (userID,taskID))";
		db.execSQL(sql);
		
		sql = "CREATE TABLE taskTestArrange " + "("
				+ "testID         integer         not null , "
				+ "taskID         integer         not null , "
				+ "constraint pk_taskTestArrange primary key (testID,taskID))";
		db.execSQL(sql);
		
		sql = "CREATE TABLE testItem " + "("
				+ "testItemID 	    integer 	PRIMARY KEY , "
				+ "testID          integer         not null , "
				+ "testItemType          integer         not null , "//0��ѡ 1��ѡ 2������
				+ "testItemContent   text 	    not	null )";
		db.execSQL(sql);
		
		sql = "CREATE TABLE testItemsOption " + "("
				+ "testItemOptionID 	    integer 	PRIMARY KEY , "
				+ "testItemID         integer         not null , "
				+ "testItemOptionContent   text         not null , "
				+ "testItemOptionIsAnswer    integer         not null , "
				+ "testItemOptionRemark   text )";
		db.execSQL(sql);
		
		sql = "CREATE TABLE myChoice " + "("
				+ "userID 	    varchar(50) 	not null , "
				+ "testItemID         integer         not null , "
				+ "taskID         integer         not null , "
				+ "answer         varchar(500)         , "
				+ "submitTime     string         , "
				+ "constraint pk_myChoice primary key (testItemID,userID,taskID))";
		db.execSQL(sql);
		
		sql = "CREATE TABLE user " + "("
				+ "userID 	    varchar(50) primary key , "
				+ "userName         varchar(50)          , "
				+ "email         varchar(50)          , "
				+ "school varchar(50) , "
				+ "department varchar(50) , "
				+ "academy varchar(50) , "
				+ "grade varchar(50) , "
				+ "major varchar(50) ,"
				+ "class varchar(50) , "
				+ "exerciseDownloadTime varchar(50) ,"
				+ "login integer , "
				+ "userType varchar(10) )";			
		db.execSQL(sql);
		
		sql = "CREATE TABLE attachment " + "("
				+ "id 	    integer, "
				+" originName varchar(500) ," 
				+" newName  varchar(500) ,"
				+" downloadTime  varchar(50) ,"
				+" userID varchar(50) ,"
				+" constraint pk_attachment primary key (id,newName))";		
		db.execSQL(sql);
		
		db.execSQL(CREATE_TABLE_FEEDBACK);
		db.execSQL(CREATE_TABLE_PLAN);
		
		sql = "CREATE TABLE uploadFiles " + "("
				+ "uploadID int  primary key , "
				+ "taskID int  , "
				+ "userID  text , "
				+ "filePath text ," 
				+" fileUploadTime varchar(50) ," 
				+" fileNewName varchar(50) )";	
		db.execSQL(sql);
		
		sql = "CREATE TABLE planTestArrange " + "("
				+ "planID int not null , "
				+ "testID  int not null , "
				+" constraint pk_planTestArrange primary key (planID,testID) )";	
		db.execSQL(sql);
		
		sql = "CREATE TABLE planChoice " + "("
				+ "planID int not null , "
				+ "testItemID  int not null , "
				+ "answer text , "
				+ "submitTime varchar(50) , " 
				+ "constraint pk_planChoice primary key (planID,testItemID))";	
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		
		if(oldVersion == 1 && newVersion == 2){
			db.execSQL("drop table if exists feedBack");
			db.execSQL(CREATE_TABLE_FEEDBACK);
		}
		
		if(oldVersion == 2 && newVersion == 3){
			db.execSQL("drop table if exists testType");
			db.execSQL(CREATE_TABLE_TESTTYPE);
		}
		
		if(oldVersion == 1 && newVersion == 3){
			db.execSQL("drop table if exists feedBack");
			db.execSQL(CREATE_TABLE_FEEDBACK);
			
			db.execSQL("drop table if exists testType");
			db.execSQL(CREATE_TABLE_TESTTYPE);
		}
		
	}
	
	public  SQLiteDatabase getConnection(){
		
		if(db==null){
			db=getWritableDatabase();
		}
	return db;
	
	}
	
	public void close(){
		
		if(db != null){
			db.close();
		}
		
	}
}
