package com.hanvon.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hanvon.bean.FileInfo;
import com.hanvon.bean.Label;
import com.hanvon.bean.TransInfo;
import com.hanvon.bean.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBManager {
	private DBHelper helper;
	private SQLiteDatabase db;
	
	private final static String TABLE_USER = "user";
	private final static String USER_ID = "user_id";
	private final static String USER_PWD = "password";
	private final static String USER_TOKEN = "token";
	private final static String USER_STATUS = "status";
	
	private final static String TABLE_FILE = "file";
	private final static String FILE_ID = "fuuid";
	private final static String FILE_USERID = "user_id";
	private final static String TYPE = "type";
	private final static String TITLE = "title";
	private final static String SUMMARY = "summary";
	private final static String LENGTH = "length";
	private final static String CREATETIME = "create_time";
	private final static String MODIFYTIME = "modify_time";
	private final static String ACCESSTIME = "access_time";
	private final static String SYNC = "sync";
	private final static String PATH = "path";
	private final static String SER_VER = "ser_ver";
	private final static String CONTENT = "content";
	
	private final static String TABLE_TRANS = "translate";
	private final static String TRANS_ID = "uuid";
	private final static String TRANS_USERID = "user_id";
	private final static String TRANS_TYPE = "type";
	private final static String TRANS_WORD = "word";
	private final static String TRANS_TRANS = "trans";
	private final static String TRANS_DATE = "date";
	private final static String TRANS_COUNT = "count";
	private final static String TRANS_ISMASTER = "is_master";
	
	private final static String TABLE_LABEL = "label";
	private final static String LABEL_ID = "id";
	private final static String LABEL_LABEL = "label";
	private final static String LABEL_POSI = "position";
	private final static String LABEL_RECOG = "recognition";
	private final static String LABEL_FILEID = "file_id";
	
	private static final String TAG = "DBManager";
	
	public DBManager(Context context) {
		helper = new DBHelper(context);
		db = helper.getWritableDatabase();//getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
		//所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里【打开数据库】
	}
	//关闭数据库
	public void closeDB() {
		db.close();
	}
	
	public boolean user_add(User user) {
		db.beginTransaction(); //开始事务
		try {
			if (user_exists(user.getUserId())) {
				user_delete(user.getUserId());
			}
			ContentValues values = new ContentValues();
			values.put(USER_ID, user.getUserId());
			values.put(USER_PWD, user.getPassword());
			values.put(USER_TOKEN, user.getToken());
			values.put(USER_STATUS, user.getStatus());
			db.insert(TABLE_USER, null, values);
			
			db.setTransactionSuccessful();//设置事务完成
		} catch (Exception e) {
			db.endTransaction();
			e.printStackTrace();
		}
		db.endTransaction(); //结束事务
		return true;
	}
	
	/**
	 * 获取当前user
	 * @return
	 */
	public User user_lastUser() {
		User user = new User();
		Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USER +" ORDER BY " + USER_ID + " DESC LIMIT 1", null);
		while (c.moveToNext()) {
			user.setUserId(c.getString(c.getColumnIndex(USER_ID)));
			user.setPassword(c.getString(c.getColumnIndex(USER_PWD)));
			user.setToken(c.getString(c.getColumnIndex(USER_TOKEN)));
			user.setStatus(c.getString(c.getColumnIndex(USER_STATUS)));
		}
		c.close();
		return user;
    }
	
	public User user_queryUser(String userId) {
		User user = new User();
		Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USER +" WHERE " + USER_ID + " = " + "'" + userId + "'", null);
		while (c.moveToNext()) {
			user.setId(c.getInt(c.getColumnIndex(USER_ID)));
			user.setUserId(userId);
			user.setPassword(c.getString(c.getColumnIndex(USER_PWD)));
			user.setToken(c.getString(c.getColumnIndex(USER_TOKEN)));
			user.setStatus(c.getString(c.getColumnIndex(USER_STATUS)));
		}
		c.close();
		return user;
	}
	
	public List<User> user_queryAll() {
		Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_USER +" ORDER BY " + USER_ID + " DESC", null);
		List<User> users = new ArrayList<User>();
		while (c.moveToNext()) {
			User user = new User();
			user.setUserId(c.getString(c.getColumnIndex(USER_ID)));
			user.setPassword(c.getString(c.getColumnIndex(USER_PWD)));
			user.setToken(c.getString(c.getColumnIndex(USER_TOKEN)));
			user.setStatus(c.getString(c.getColumnIndex(USER_STATUS)));
			users.add(user);
		}
		c.close();
		return users;
	}
	
	public boolean user_exists(String userId) {
		boolean flag = false;
		Cursor cursor = db.query(TABLE_USER, null, USER_ID + " = ?", new String[]{userId}, null, null, null);
		flag = cursor.moveToFirst();
		cursor.close();
		return flag;
	}
	
	public boolean user_delete(String userId) {
		int flag = 0;
		flag = db.delete(TABLE_USER, USER_ID + " = ?", new String[]{userId});
		return flag > 0;
	}
	
	public boolean user_update(User user) {
		int flag = 0;
		ContentValues values = new ContentValues();
		if (null != user.getPassword()) {
			values.put(USER_PWD, user.getPassword());
		}
		if (null != user.getToken()) {
			values.put(USER_TOKEN, user.getToken());
		}
		if (null != user.getStatus()) {
			values.put(USER_STATUS, user.getStatus());
		}
		flag = db.update(TABLE_USER, values, USER_ID + "= '" + user.getUserId() + "'", null);
		return flag > 0;
	}
	
	public boolean file_add(FileInfo file) {
		db.beginTransaction(); //开始事务
		try {
			if (file_exists(file.getFuuid())) {
				file_delete(file.getFuuid());
			}
			ContentValues values = new ContentValues();
			values.put(FILE_ID, file.getFuuid());
			values.put(FILE_USERID, file.getUserId());
			values.put(TYPE, file.getType());
			values.put(TITLE, file.getTitle());
			values.put(SUMMARY, file.getSummary());
			values.put(LENGTH, file.getLength());
			values.put(CREATETIME, file.getCreateTime());
			values.put(MODIFYTIME, file.getModifyTime());
			values.put(ACCESSTIME, file.getAccessTime());
			values.put(SYNC, file.getSyn());
			values.put(PATH, file.getPath());
			values.put(SER_VER, file.getSerVer());
			values.put(CONTENT, file.getContent());
			db.insert(TABLE_FILE, null, values);
			
			db.setTransactionSuccessful();//设置事务完成
		} catch (Exception e) {
			db.endTransaction();
			e.printStackTrace();
		}
		db.endTransaction(); //结束事务
		return true;
	}
	
	public boolean files_add(List<FileInfo> files) {
		db.beginTransaction(); //开始事务
		try {
			for (FileInfo file : files) {
				if (file_exists(file.getFuuid())) {
					file_delete(file.getFuuid());
				}
				ContentValues values = new ContentValues();
				values.put(FILE_ID, file.getFuuid());
				values.put(FILE_USERID, file.getUserId());
				values.put(TYPE, file.getType());
				values.put(TITLE, file.getTitle());
				values.put(SUMMARY, file.getSummary());
				values.put(LENGTH, file.getLength());
				values.put(CREATETIME, file.getCreateTime());
				values.put(MODIFYTIME, file.getModifyTime());
				values.put(ACCESSTIME, file.getAccessTime());
				values.put(SYNC, file.getSyn());
				values.put(PATH, file.getPath());
				values.put(SER_VER, file.getSerVer());
				values.put(CONTENT, file.getContent());
				db.insert(TABLE_FILE, null, values);
			}
			db.setTransactionSuccessful();//设置事务完成
		} catch (Exception e) {
			db.endTransaction();
			e.printStackTrace();
		}
		db.endTransaction(); //结束事务
		return true;
	}
	public boolean file_exists(String fuuid) {
		boolean flag = false;
		Cursor cursor = db.query(TABLE_FILE, null, FILE_ID + " = ?", new String[]{fuuid}, null, null, null);
		flag = cursor.moveToFirst();
		cursor.close();
		return flag;
	}
	
	public boolean file_delete(String fuuid) {
		int flag = 0;
		flag = db.delete(TABLE_FILE, FILE_ID + " = ?", new String[]{fuuid});
		return flag > 0;
	}
	
	public List<FileInfo> file_queryAll() {
		Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_FILE +" ORDER BY " + FILE_ID + " DESC", null);
		List<FileInfo> files = new ArrayList<FileInfo>();
		while (c.moveToNext()) {
			FileInfo file = new FileInfo();
			file.setFuuid(c.getString(c.getColumnIndex(FILE_ID)));
			file.setUserId(c.getString(c.getColumnIndex(FILE_USERID)));
			file.setTitle(c.getString(c.getColumnIndex(TITLE)));
			file.setType(c.getString(c.getColumnIndex(TYPE)));
			file.setSummary(c.getString(c.getColumnIndex(SUMMARY)));
			file.setLength(c.getString(c.getColumnIndex(LENGTH)));
			file.setCreateTime(c.getString(c.getColumnIndex(CREATETIME)));
			file.setModifyTime(c.getString(c.getColumnIndex(MODIFYTIME)));
			file.setAccessTime(c.getString(c.getColumnIndex(ACCESSTIME)));
			file.setSyn(c.getString(c.getColumnIndex(SYNC)));
			file.setPath(c.getString(c.getColumnIndex(PATH)));
			file.setSerVer(c.getString(c.getColumnIndex(SER_VER)));
			file.setContent(c.getString(c.getColumnIndex(CONTENT)));
			files.add(file);
		}
		c.close();
		return files;
	}
	
	public FileInfo file_queryById(String fuuid) {
		FileInfo file = new FileInfo();
		Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_FILE +" WHERE " + FILE_ID + " = " + "'" + fuuid + "'", null);
		while (c.moveToNext()) {
			file.setFuuid(c.getString(c.getColumnIndex(FILE_ID)));
			file.setUserId(c.getString(c.getColumnIndex(FILE_USERID)));
			file.setTitle(c.getString(c.getColumnIndex(TITLE)));
			file.setType(c.getString(c.getColumnIndex(TYPE)));
			file.setSummary(c.getString(c.getColumnIndex(SUMMARY)));
			file.setLength(c.getString(c.getColumnIndex(LENGTH)));
			file.setCreateTime(c.getString(c.getColumnIndex(CREATETIME)));
			file.setModifyTime(c.getString(c.getColumnIndex(MODIFYTIME)));
			file.setAccessTime(c.getString(c.getColumnIndex(ACCESSTIME)));
			file.setSyn(c.getString(c.getColumnIndex(SYNC)));
			file.setPath(c.getString(c.getColumnIndex(PATH)));
			file.setSerVer(c.getString(c.getColumnIndex(SER_VER)));
			file.setContent(c.getString(c.getColumnIndex(CONTENT)));
		}
		c.close();
		return file;
	}
	
	public List<FileInfo> file_queryByUserAndType(String userId, String type) {
		try {
			Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_FILE +" WHERE " + FILE_USERID + " = " + "'" + userId + "'" +
					" AND " + TYPE + " = " + "'" + type + "' ORDER BY " + MODIFYTIME + " DESC", null);
			List<FileInfo> files = new ArrayList<FileInfo>();
			while (c.moveToNext()) {
				FileInfo file = new FileInfo();
				file.setFuuid(c.getString(c.getColumnIndex(FILE_ID)));
				file.setUserId(c.getString(c.getColumnIndex(FILE_USERID)));
				file.setTitle(c.getString(c.getColumnIndex(TITLE)));
				file.setType(c.getString(c.getColumnIndex(TYPE)));
				file.setSummary(c.getString(c.getColumnIndex(SUMMARY)));
				file.setLength(c.getString(c.getColumnIndex(LENGTH)));
				file.setCreateTime(c.getString(c.getColumnIndex(CREATETIME)));
				file.setModifyTime(c.getString(c.getColumnIndex(MODIFYTIME)));
				file.setAccessTime(c.getString(c.getColumnIndex(ACCESSTIME)));
				file.setSyn(c.getString(c.getColumnIndex(SYNC)));
				file.setPath(c.getString(c.getColumnIndex(PATH)));
				file.setSerVer(c.getString(c.getColumnIndex(SER_VER)));
				file.setContent(c.getString(c.getColumnIndex(CONTENT)));
				files.add(file);
			}
			c.close();
			return files;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public List<FileInfo> file_queryByStatus(String userId, String type, String syn) {
		try {
//			Cursor c = db.rawQuery("SELECT * FROM " + TABLE_FILE + " WHERE " + FILE_USERID + " = '" + userId + "' AND " + TYPE + " = '" + type  + "' ORDER BY " + MODIFYTIME + " DESC", null);
			Cursor c = db.rawQuery("SELECT * FROM " + TABLE_FILE + " WHERE " + FILE_USERID + " = '" + userId + "' AND " + TYPE + " = '" + type + "' AND " + SYNC + " = '" + syn + "' ORDER BY " + MODIFYTIME + " DESC", null);
			List<FileInfo> files = new ArrayList<FileInfo>();
			while (c.moveToNext()) {
				FileInfo file = new FileInfo();
				file.setFuuid(c.getString(c.getColumnIndex(FILE_ID)));
				file.setUserId(c.getString(c.getColumnIndex(FILE_USERID)));
				file.setTitle(c.getString(c.getColumnIndex(TITLE)));
				file.setType(c.getString(c.getColumnIndex(TYPE)));
				file.setSummary(c.getString(c.getColumnIndex(SUMMARY)));
				file.setLength(c.getString(c.getColumnIndex(LENGTH)));
				file.setCreateTime(c.getString(c.getColumnIndex(CREATETIME)));
				file.setModifyTime(c.getString(c.getColumnIndex(MODIFYTIME)));
				file.setAccessTime(c.getString(c.getColumnIndex(ACCESSTIME)));
				file.setSyn(c.getString(c.getColumnIndex(SYNC)));
				file.setPath(c.getString(c.getColumnIndex(PATH)));
				file.setSerVer(c.getString(c.getColumnIndex(SER_VER)));
				file.setContent(c.getString(c.getColumnIndex(CONTENT)));
				files.add(file);
			}
			c.close();
			return files;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * 按条件查询 map{}
	 * @param map
	 * @return
	 */
	public List<FileInfo> file_queryByCondition(Map<String, String> map) {
		try {
			String conStr = " WHERE 1=1";
			if (map.get("userId") != null && !map.get("userId").equals("")) {
				conStr += " AND " + FILE_USERID + " = " + map.get("userId");
			}
			if (map.get("type") != null && !map.get("type").equals("")) {
				conStr += " AND " + TYPE + " = " + map.get("type");
			}
			Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_FILE + conStr + " ORDER BY " + FILE_ID + " DESC", null);
			List<FileInfo> files = new ArrayList<FileInfo>();
			while (c.moveToNext()) {
				FileInfo file = new FileInfo();
				file.setFuuid(c.getString(c.getColumnIndex(FILE_ID)));
				file.setUserId(c.getString(c.getColumnIndex(FILE_USERID)));
				file.setTitle(c.getString(c.getColumnIndex(TITLE)));
				file.setType(c.getString(c.getColumnIndex(TYPE)));
				file.setSummary(c.getString(c.getColumnIndex(SUMMARY)));
				file.setLength(c.getString(c.getColumnIndex(LENGTH)));
				file.setCreateTime(c.getString(c.getColumnIndex(CREATETIME)));
				file.setModifyTime(c.getString(c.getColumnIndex(MODIFYTIME)));
				file.setAccessTime(c.getString(c.getColumnIndex(ACCESSTIME)));
				file.setSyn(c.getString(c.getColumnIndex(SYNC)));
				file.setPath(c.getString(c.getColumnIndex(PATH)));
				file.setSerVer(c.getString(c.getColumnIndex(SER_VER)));
				file.setContent(c.getString(c.getColumnIndex(CONTENT)));
				files.add(file);
			}
			c.close();
			return files;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public boolean file_update(FileInfo file) {
		int flag = 0;
		ContentValues values = new ContentValues();
		if (null != file.getPath()) {
			values.put(PATH, file.getPath());
		}
		if (null != file.getSerVer()) {
			values.put(SER_VER, file.getSerVer());
		}
		flag = db.update(TABLE_FILE, values, FILE_ID + "=" + "'" + file.getFuuid() + "'", null);
		return flag > 0;
	}
	
	public boolean trans_add(TransInfo trans) {
		db.beginTransaction(); //开始事务
		try {
			ContentValues values = new ContentValues();
			values.put(TRANS_ID, trans.getUuid());
			values.put(TRANS_USERID, trans.getUserId());
			values.put(TRANS_TYPE, trans.getType());
			values.put(TRANS_WORD, trans.getWord());
			values.put(TRANS_TRANS, trans.getTrans());
			if (null != trans.getDate()) {
				values.put(TRANS_DATE, trans.getDate());
			}
			if (null != trans.getCount()) {
				values.put(TRANS_COUNT, trans.getCount());
			}
			if (null != trans.getIsMaster()) {
				values.put(TRANS_ISMASTER, trans.getIsMaster());
			}
			db.insert(TABLE_TRANS, null, values);
			db.setTransactionSuccessful();//设置事务完成
		} catch (Exception e) {
			db.endTransaction();
			e.printStackTrace();
		}
		db.endTransaction(); //结束事务
		return true;
	}
	
	public boolean transList_add(List<TransInfo> transList) {
		db.beginTransaction(); //开始事务
		try {
			for (TransInfo trans : transList) {
				ContentValues values = new ContentValues();
				values.put(TRANS_ID, trans.getUuid());
				values.put(TRANS_USERID, trans.getUserId());
				values.put(TRANS_TYPE, trans.getType());
				values.put(TRANS_WORD, trans.getWord());
				values.put(TRANS_TRANS, trans.getTrans());
				if (null != trans.getDate()) {
					values.put(TRANS_DATE, trans.getDate());
				}
				if (null != trans.getCount()) {
					values.put(TRANS_COUNT, trans.getCount());
				}
				if (null != trans.getIsMaster()) {
					values.put(TRANS_ISMASTER, trans.getIsMaster());
				}
				db.insert(TABLE_TRANS, null, values);
			}
			db.setTransactionSuccessful();//设置事务完成
		} catch (Exception e) {
			db.endTransaction();
			e.printStackTrace();
		}
		db.endTransaction(); //结束事务
		return true;
	}
	
	public boolean trans_delete(String uuid) {
		int flag = 0;
		flag = db.delete(TABLE_TRANS, TRANS_ID + " = ?", new String[]{uuid});
		return flag > 0;
	}
	
	public boolean trans_deleteByUserAndType(String userId, String type) {
		int flag = 0;
		flag = db.delete(TABLE_TRANS, TRANS_USERID + " = ?" + TRANS_TYPE + " = ?", new String[]{userId, type});
		return flag > 0;
	}
	
	public List<TransInfo> trans_queryAll() {
		Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_TRANS, null);
		List<TransInfo> transList = new ArrayList<TransInfo>();
		while (c.moveToNext()) {
			TransInfo trans = new TransInfo();
			trans.setUuid(c.getString(c.getColumnIndex(TRANS_ID)));
			trans.setUserId(c.getString(c.getColumnIndex(TRANS_USERID)));
			trans.setType(c.getString(c.getColumnIndex(TRANS_TYPE)));
			trans.setWord(c.getString(c.getColumnIndex(TRANS_WORD)));
			trans.setTrans(c.getString(c.getColumnIndex(TRANS_TRANS)));
			trans.setDate(c.getString(c.getColumnIndex(TRANS_DATE)));
			trans.setCount(c.getString(c.getColumnIndex(TRANS_COUNT)));
			trans.setIsMaster(c.getString(c.getColumnIndex(TRANS_ISMASTER)));
			transList.add(trans);
		}
		c.close();
		return transList;
	}
	
	public List<TransInfo> trans_queryByUserAndType(String userId, String type) {
		List<TransInfo> transList = null;
		try {
			String queryStr = " WHERE 1=1";
			if (type == null || userId.equals("") || userId == null || userId.equals("")) {
				Log.e(TAG, "trans_queryByUserAndType params is error!");
				return null;
			}
			queryStr += " AND " + TRANS_USERID + " = '" + userId + "' AND " + TRANS_TYPE + " = '" + type + "'";
			Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_TRANS + queryStr + " ORDER BY " + TRANS_ID + " DESC", null);
			transList = new ArrayList<TransInfo>();
			while (c.moveToNext()) {
				TransInfo trans = new TransInfo();
				trans.setUuid(c.getString(c.getColumnIndex(TRANS_ID)));
				trans.setUserId(c.getString(c.getColumnIndex(TRANS_USERID)));
				trans.setType(c.getString(c.getColumnIndex(TRANS_TYPE)));
				trans.setWord(c.getString(c.getColumnIndex(TRANS_WORD)));
				trans.setTrans(c.getString(c.getColumnIndex(TRANS_TRANS)));
				trans.setDate(c.getString(c.getColumnIndex(TRANS_DATE)));
				trans.setCount(c.getString(c.getColumnIndex(TRANS_COUNT)));
				trans.setIsMaster(c.getString(c.getColumnIndex(TRANS_ISMASTER)));
				transList.add(trans);
			}
			c.close();
			return transList;
		} catch (Exception e) {
			e.printStackTrace();
			return transList;
		}
	}
	
	public boolean label_delete(String fileId) {
		int flag = 0;
		flag = db.delete(TABLE_LABEL, LABEL_FILEID + " = ?", new String[]{fileId});
		return flag > 0;
	}
	
	public boolean labels_add(List<Label> labels) {
		db.beginTransaction(); //开始事务
		try {
			for (Label label : labels) {
				ContentValues values = new ContentValues();
				values.put(LABEL_ID, label.getId());
				values.put(LABEL_LABEL, label.getLabel());
				values.put(LABEL_POSI, label.getPosition());
				values.put(LABEL_RECOG, label.getRecognition());
				values.put(LABEL_FILEID, label.getFileId());
				db.insert(TABLE_LABEL, null, values);
			}
			db.setTransactionSuccessful();//设置事务完成
		} catch (Exception e) {
			db.endTransaction();
			e.printStackTrace();
		}
		db.endTransaction(); //结束事务
		return true;
	}
	
	public List<Label> label_queryAll() {
		Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_LABEL, null);
		List<Label> labels = new ArrayList<Label>();
		while (c.moveToNext()) {
			Label lab = new Label();
			lab.setId(c.getString(c.getColumnIndex(LABEL_ID)));
			lab.setLabel(c.getString(c.getColumnIndex(LABEL_LABEL)));
			lab.setPosition(c.getString(c.getColumnIndex(LABEL_POSI)));
			lab.setRecognition(c.getString(c.getColumnIndex(LABEL_RECOG)));
			lab.setFileId(c.getString(c.getColumnIndex(LABEL_FILEID)));
			labels.add(lab);
		}
		c.close();
		return labels;
	}
	
	public List<Label> label_queryByFileId(String fileId) {
		List<Label> labels = null;
		try {
			String queryStr = " WHERE 1=1";
			if (fileId == null || fileId.equals("")) {
				Log.e(TAG, "trans_queryByUserAndType params is error!");
				return null;
			}
			queryStr += " AND " + LABEL_FILEID + " = '" + fileId + "'";
			Cursor c = db.rawQuery("SELECT * FROM "+ TABLE_LABEL + queryStr + " ORDER BY " + LABEL_ID + " ASC", null);
			labels = new ArrayList<Label>();
			while (c.moveToNext()) {
				Label lab = new Label();
				lab.setId(c.getString(c.getColumnIndex(LABEL_ID)));
				lab.setLabel(c.getString(c.getColumnIndex(LABEL_LABEL)));
				lab.setPosition(c.getString(c.getColumnIndex(LABEL_POSI)));
				lab.setRecognition(c.getString(c.getColumnIndex(LABEL_RECOG)));
				lab.setFileId(c.getString(c.getColumnIndex(LABEL_FILEID)));
				labels.add(lab);
			}
			c.close();
			return labels;
		} catch (Exception e) {
			e.printStackTrace();
			return labels;
		}
	}
	
	/**
	 * 判断某张表是否存在
	 * @param tableName 表名
	 * @return
	 */
	public boolean tableExist(String tableName) {
		boolean result = false;
		if (tableName == null) {
			return false;
		}
		Cursor cursor = null;
		try {
			String sql = "select count(*) as c from Sqlite_master  where type ='table' and name ='"
					+ tableName.trim() + "' ";
			cursor = db.rawQuery(sql, null);
			if (cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					result = true;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 删除表
	 * @param tableName
	 * @return
	 */
	public boolean tableDelete(String tableName){
		try {
			db.execSQL("DROP TABLE IF EXISTS " + tableName);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 清空表
	 * @param tableName
	 * @return
	 */
	public boolean tableClear(String tableName){
		try {
			db.execSQL("DELETE FROM " + tableName);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
