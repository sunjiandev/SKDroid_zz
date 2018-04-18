package com.sunkaisens.skdroid.Utils;  
  
import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.util.GlobalSession;

import android.content.BroadcastReceiver;  
import android.content.Context;  
import android.content.Intent;  
import android.util.Log;
  
/**
 * 程序可以在开机时自动运行
 * @author zhaohua
 *
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
	private final static String TAG = BootBroadcastReceiver.class.getCanonicalName();	
	
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "BootBroadcastReceiver onReceive()");

		String action = intent.getAction();
		if (action.equals(ACTION)) {
			boolean autoStart = Engine.getInstance().getConfigurationService().getBoolean(
					NgnConfigurationEntry.GENERAL_AUTOSTART.toString(),
					NgnConfigurationEntry.DEFAULT_GENERAL_AUTOSTART);
//			if (autoStart) {
			if (autoStart && !GlobalSession.bSocketService) { //非socket服务模式时开机自启动 瀚讯大终端
//			if (autoStart && !NgnApplication.isHxSabresd()) { //非瀚讯大终端(socket服务模式时)开机自启动
				Intent mainIntent = new Intent(context, Main.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				context.startActivity(mainIntent);
			}
		}
	}

}
