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
package com.sunkaisens.skdroid.app.service;

import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.crash.CrashHandler;

public class MainService extends Service {
	private static String TAG = MainService.class.getCanonicalName();

	public static final int ACTION_NONE = 0;
	public static final int ACTION_RESTORE_LAST_STATE = 1;
	public static final int ACTION_SHOW_AVSCREEN = 2;
	public static final int ACTION_SHOW_CONTSHARE_SCREEN = 3;
	public static final int ACTION_SHOW_SMS = 4;
	public static final int ACTION_SHOW_CHAT_SCREEN = 5;
	public static final int ACTION_SHOW_PUSH = 6;

	private static final int RC_SPLASH = 0;

	private final Engine mEngine;
	// private final IScreenService mScreenService; // ?

	private Handler mTipsHandler = null;
	public static final int MSG_IS_NETWORK_AVAILABLE = 1000;

	public static boolean isFirstPTT_onKeyDown_bh03 = true;
	public static boolean isFirstPTT_onKeyLongPress_bh03 = true;
	public static boolean isFirstPTT_onKeyDown_bh04 = true;
	public static boolean isFirstPTT_onKeyLongPress_bh04 = true;

	private BroadcastReceiver mBroadCastRecv;
	private final INgnSipService mSipService;

	public MainService() {
		super();

		// Sets main activity (should be done before starting services)
		mEngine = (Engine) Engine.getInstance();
		// mScreenService = ((Engine) Engine.getInstance()).getScreenService();

		mSipService = mEngine.getSipService();
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate() {
		super.onCreate();

		// 异常处理，不需要处理时注释掉这两句即可！
		CrashHandler crashHandler = CrashHandler.getInstance();
		// 注册crashHandler
		crashHandler.init(getApplicationContext());

		mTipsHandler = new Handler(getMainLooper()) {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_IS_NETWORK_AVAILABLE:
					SystemVarTools.showToast(getApplicationContext().getString(
							R.string.net_is_unreachable));
					Log.d(TAG, "当前网络不可用，请稍候重试！");
					break;
				default:
					break;
				}
			}
		};

		new Thread("message handler thread") {
			public void run() {
				while (true) {
					if (!CrashHandler.isNetworkAvailable(MainService.this)) {
						// 检测到网络不可用后给UI线程发送检测完成消息
						Message msg = Message.obtain(mTipsHandler,
								MSG_IS_NETWORK_AVAILABLE);
						mTipsHandler.sendMessage(msg);
					}
					try {
						sleep(30000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();

		if (!Engine.getInstance().isStarted()) {
			mBroadCastRecv = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					final String action = intent.getAction();

					if (NativeService.ACTION_STATE_EVENT.equals(action)) {
						if (intent.getBooleanExtra("started", false)) {
							mEngine.getConfigurationService().putBoolean(
									NgnConfigurationEntry.GENERAL_AUTOSTART,
									true);
						}
					}
				}
			};
			final IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(NativeService.ACTION_STATE_EVENT);
			registerReceiver(mBroadCastRecv, intentFilter);
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onDestroy() {
		if (mBroadCastRecv != null) {
			unregisterReceiver(mBroadCastRecv);
		}
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		final Engine engine = (Engine) Engine.getInstance();

		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				if (!engine.isStarted()) {
					Log.d(TAG, "Starts the engine from the splash screen");
					try {
						engine.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.start();
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}