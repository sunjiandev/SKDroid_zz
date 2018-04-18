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
 * ��������ڿ���ʱ�Զ�����
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
			if (autoStart && !GlobalSession.bSocketService) { //��socket����ģʽʱ���������� �Ѷ���ն�
//			if (autoStart && !NgnApplication.isHxSabresd()) { //���Ѷ���ն�(socket����ģʽʱ)����������
				Intent mainIntent = new Intent(context, Main.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				context.startActivity(mainIntent);
			}
		}
	}

}
