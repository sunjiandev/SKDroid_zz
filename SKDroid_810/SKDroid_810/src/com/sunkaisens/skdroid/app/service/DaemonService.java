package com.sunkaisens.skdroid.app.service;

import java.util.List;
import java.util.TimerTask;

import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnTimer;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class DaemonService extends Service {

	private String TAG = DaemonService.class.getCanonicalName();
	// 用于判断进程是否运行
	private String Process_Name = "com.sunkaisens.skdroid";
	private boolean runningAlways = false;
	private NgnTimer mCheckIsRunningTimer = null;
	/**
	 * 启动Service2
	 */
	private DaemonServiceProcess startS = new DaemonServiceProcess.Stub() {
		@Override
		public void stopService() throws RemoteException {
			Log.d(TAG, "stopService");
			Intent i = new Intent(getBaseContext(), NativeService.class);
			getBaseContext().stopService(i);
		}

		@Override
		public void startService() throws RemoteException {
			Log.d(TAG, "startService");
			Intent i = new Intent(getBaseContext(), NativeService.class);
			getBaseContext().startService(i);
		}
	};

	@Override
	public void onCreate() {
		Log.d(TAG, "DaemonService onCreate()...");
		// Toast.makeText(DaemonService.this, "DaemonService onCreate...",
		// Toast.LENGTH_SHORT).show();
		// runningAlways = true;
		// checkProcessIsRunning.start();
	}

	public Thread checkProcessIsRunning = new Thread("DaemonCheckProcess") {
		@Override
		public void run() {
			boolean isRunning = false;
			while (runningAlways) {
				ActivityManager am = (ActivityManager) DaemonService.this
						.getSystemService(Context.ACTIVITY_SERVICE);
				// keepService();
				// Log.d("DaemonService", "Running: 0");

				List<RunningAppProcessInfo> lists = am.getRunningAppProcesses();
				// Log.d("DaemonService", "Running: 1");
				for (RunningAppProcessInfo info : lists) {
					// Log.d("DaemonService", "Running: 2");
					if (info.processName.equals(Process_Name)) {
						// Log.d("DaemonService", "Running: 3 :" +
						// info.processName);
						isRunning = true;
						break;
					}
				}
				// Log.d("DaemonService", "Running: 4");
				if (isRunning == false) {
					try {
						// Toast.makeText(getBaseContext(),
						// "reStart Process: com.sunkaisens.skdroid...",
						// Toast.LENGTH_SHORT).show();
						Log.d(TAG, "reStart Process: com.sunkaisens.skdroid...");
						startS.startService();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				// Log.d("DaemonService", "Running: 5");
			}
		}
	};

	/**
	 * 判断com.sunkaisens.skdroid是否还在运行，如果不是则启动
	 */

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand!");
		// Toast.makeText(DaemonService.this, "DaemonService onStartCommand...",
		// Toast.LENGTH_SHORT).show();
		mCheckIsRunningTimer = new NgnTimer();
		mCheckIsRunningTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stu
				boolean isRunning = false;
				ActivityManager am = (ActivityManager) DaemonService.this
						.getSystemService(Context.ACTIVITY_SERVICE);

				List<RunningAppProcessInfo> lists = am.getRunningAppProcesses();
				// Log.d("DaemonService", "Running: 1");
				for (RunningAppProcessInfo info : lists) {
					// Log.d("DaemonService", "Running: 2");
					if (info.processName.equals(Process_Name)) {
						// Log.d("DaemonService", "Running: 3 :" +
						// info.processName);
						isRunning = true;
						break;
					}
				}
				// Log.d("DaemonService", "Running: 4");
				if (isRunning == false) {
					try {
						// Toast.makeText(getBaseContext(),
						// "reStart Process: com.sunkaisens.skdroid...",
						// Toast.LENGTH_SHORT).show();
						Log.d(TAG, "reStart Process: com.sunkaisens.skdroid...");
						startS.startService();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}, 0, NgnConfigurationEntry.CHECK_THREAD_DELT);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
		// return (IBinder) startS;
	}

	public void onDestroy() {
		Log.d(TAG, "DaemonService onDestroy!");
		// Toast.makeText(DaemonService.this, "DaemonService onDestroy...",
		// Toast.LENGTH_SHORT).show();
		runningAlways = false;
		if (checkProcessIsRunning != null) {
			checkProcessIsRunning.interrupt();
			checkProcessIsRunning = null;
		}
		if (mCheckIsRunningTimer != null) {
			mCheckIsRunningTimer.cancel();
			mCheckIsRunningTimer.purge();
			mCheckIsRunningTimer = null;
		}
		super.onDestroy();
		System.exit(0);

	}

	public static boolean isProcessRunning(Context context, String proessName) {

		Log.d("DaemonService", "check isProcessRunning()...");
		boolean isRunning = false;
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		List<RunningAppProcessInfo> lists = am.getRunningAppProcesses();
		for (RunningAppProcessInfo info : lists) {
			if (info.processName.equals(proessName)) {
				Log.d("DaemonService", "Running: " + info.processName);
				isRunning = true;
			}
		}

		return isRunning;
	}
}
