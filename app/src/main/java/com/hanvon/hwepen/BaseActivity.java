package com.hanvon.hwepen;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.hanvon.application.AppManage;
import com.hanvon.db.DBManager;

public class BaseActivity extends Activity
{
	
	private DBManager dbManager;
	
	//create menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
//		menu.add(0, 0, 0, "关于");
//		menu.add(0, 1, 1, "注销账号");
//		menu.add(0, 2, 2, "退出");
		return super.onCreateOptionsMenu(menu);
	}
	
	//menu response
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		try {
			String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			switch (item.getItemId()) {
			case 0:
				Toast.makeText(this, "汉王科技股份有限公司" + version, Toast.LENGTH_SHORT).show();
				break;
			case 1:
				AppManage.getInstance().finishPreActivities();
				break;
			case 2:
				AppManage.getInstance().exit();
				break;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//按两下返回键退出
//	private long exitTime = 0;
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//	    if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
//		    if((System.currentTimeMillis()-exitTime) > 2000){//System.currentTimeMillis()无论何时调用，肯定大于2000  
//		        Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
//		        exitTime = System.currentTimeMillis();
//		    } else {
//		        AppManage.getInstance().exit();
//		    }
//	            return true;
//	    }
//	    return super.onKeyDown(keyCode, event);
//	}
}
