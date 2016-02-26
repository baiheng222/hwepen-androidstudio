package com.hanvon.application;

import android.app.Activity;

import java.util.LinkedList;
import java.util.List;

/**
 * 应用退出----activity容器
 */
public class AppManage {
	private List<Activity> activityList = new LinkedList<Activity>();
	private static AppManage instance;

	private AppManage() {

	}

	// 单例模式中获取唯一的AppManage实例
	public static AppManage getInstance() {
		if (null == instance) {
			instance = new AppManage();
		}
		return instance;
	}

	// 添加Activity到容器中
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}
	
	// 遍历所有Activity并finish exit
	public void exit() {
		for (Activity activity : activityList) {
			activity.finish();
		}
		System.exit(0);
	}
	
	// 遍历所有Activity并finish 但不退出
	public void finishPreActivities() {
		for (Activity activity : activityList) {
			activity.finish();
		}
		activityList.clear();
	}
	
	// 当前容器中Activity的数量
	public int getActivityCount() {
		return activityList.size();
	}
}
