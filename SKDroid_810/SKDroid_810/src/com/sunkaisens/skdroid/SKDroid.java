/* Copyright (C) 2010-2011, Mamadou Diop.
 *  Copyright (C) 2011, Doubango Telecom.
 *
 * Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
 *	
 * This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
 *
 * imsdroid is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *	
 * imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *	
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.sunkaisens.skdroid;

import org.doubango.ngn.NgnApplication;
import org.doubango.utils.MyLog;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import com.sunkaisens.skdroid.model.VERSION;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

//import com.tencent.bugly.crashreport.CrashReport;

public class SKDroid extends NgnApplication {
	private final static String TAG = SKDroid.class.getCanonicalName();
	/**
	 * 标记当前版本类型
	 */
	public final static VERSION sks_version = VERSION.NORMAL;

	public SKDroid() {
		MyLog.d(TAG, "SKDroid()");
		GlobalVar.mMyPid = Process.myPid();
		MyLog.d(TAG, "当前进程id:" + GlobalVar.mMyPid);
		
		if (sks_version == VERSION.SOCKET) {
			GlobalSession.bSocketService = true;
			GlobalSession.isSocketServicePath = true;
		} else if (sks_version == VERSION.SOCKET_TEST) {
			GlobalSession.bSocketService = true;
			GlobalSession.isSocketServicePath = false;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		MyLog.d(TAG, "SKDroid onCreate");
		
		
		  Runtime rt = Runtime.getRuntime();
	        long maxMemory = rt.maxMemory();
	        MyLog.e("MaxMemory:", Long.toString(maxMemory/(1024*1024)));
	        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	        
	        MyLog.e("MemoryClass:", Long.toString(activityManager.getMemoryClass()));
	        MyLog.e("LargeMemoryClass:", Long.toString(activityManager.getLargeMemoryClass()));
		
		// 微信crash bug管理(bugly)
		// CrashReport.initCrashReport(this, "900019236", false);

		// StrictMode.setThreadPolicy(new ThreadPolicy.Builder()
		// .detectAll()
		// .penaltyDialog()
		// .penaltyDropBox()
		// .penaltyLog()
		// .penaltyFlashScreen()
		// .build());

		// ActivityManager am = (ActivityManager)
		// getSystemService(Context.ACTIVITY_SERVICE);
		// List<RunningAppProcessInfo> lists = am.getRunningAppProcesses();
		// for (RunningAppProcessInfo info : lists) {
		// if (info.processName.equals(Main.processName)) {
		// SystemVarTools.isDaemonStartMe = true;
		// }
		// }
		//

		Intent mainIntent = new Intent(NgnApplication.getContext(), Main.class);
		mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		NgnApplication.getContext().startActivity(mainIntent);
	}
}
