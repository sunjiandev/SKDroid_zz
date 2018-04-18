package com.sunkaisens.skdroid.Utils;  
  
import org.doubango.ngn.utils.NgnConfigurationEntry;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.app.service.MainService;

import android.content.BroadcastReceiver;  
import android.content.Context;  
import android.content.Intent;  
import android.util.Log;
  
/**
 * 程序可以在开机时自动运行
 * @author boostor
 *
 */
public class BootServiceBroadcastReceiver extends BroadcastReceiver {
	private final static String TAG = BootServiceBroadcastReceiver.class.getCanonicalName();

	static final String ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "BootServiceBroadcastReceiver onReceive()");

		String action = intent.getAction();
		if (action.equals(ACTION)) {
			Intent mainServiceIntent = new Intent(Intent.ACTION_RUN);
			mainServiceIntent.setClass(context, MainService.class);

			context.startService(mainServiceIntent);
		}
	}

}
