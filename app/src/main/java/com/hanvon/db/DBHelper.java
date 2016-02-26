package com.hanvon.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{

	private static final String DATABASE_NAME = "note.db";
	private static final Integer DATABASE_VERSION = 3;
	
	public DBHelper(Context context) {
		//CursorFactory设置null， 使用默认值
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	//数据库第一次被创建时onCreate会被调用
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS user" +
				"(id INTEGER PRIMARY KEY, user_id VARCHAR, password VARCHAR, token VARCHAR, status VARCHAR)");
		db.execSQL("CREATE TABLE IF NOT EXISTS file" +
				"(fuuid VARCHAR PRIMARY KEY, user_id VARCHAR, title VARCHAR, type VARCHAR, summary VARCHAR, length VARCHAR, create_time VARCHAR, modify_time VARCHAR, access_time VARCHAR, sync VARCHAR, path VARCHAR, ser_ver VARCHAR, content VARCHAR)");
		db.execSQL("CREATE TABLE IF NOT EXISTS translate" +
				"(uuid VARCHAR PRIMARY KEY, user_id VARCHAR, type VARCHAR, word VARCHAR, trans VARCHAR, date VARCHAR, count VARCHAR, is_master VARCHAR)");
		db.execSQL("CREATE TABLE IF NOT EXISTS label" +
				"(id INTEGER PRIMARY KEY ,label VARCHAR, position VARCHAR, recognition VARCHAR, file_id VARCHAR)");
	}

	//如果DATABASE_VERSION值被改为2，系统发现现有数据库版本不同，即会调用onUpgrade
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF  EXISTS user");
		db.execSQL("DROP TABLE IF  EXISTS file");
		db.execSQL("DROP TABLE IF  EXISTS translate");
		db.execSQL("DROP TABLE IF  EXISTS label");
		onCreate(db);
	}
	
}
