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
package com.sunkaisens.skdroid.Screens;

import java.io.IOException;
import java.util.HashMap;
import java.util.TimerTask;

import com.sks.net.socket.server.ServerMsgReceiver;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;

import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.Utils.ChkVer;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.app.service.NativeService;
import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.util.GlobalSession;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ScreenSplash extends BaseScreen {
	private static String TAG = ScreenSplash.class.getCanonicalName();

	private BroadcastReceiver mBroadCastRecv;

	public ScreenSplash() {
		super(SCREEN_TYPE.SPLASH_T, TAG);
	}

	private String mUserName;
	private String pwd;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1000:
				mScreenService.show(ScreenTabHome.class);

				break;

			case 2000:

				Toast.makeText(
						getApplicationContext(),
						""
								+ getText(R.string.tip_net_login_failed_check_name_pass),
						Toast.LENGTH_LONG).show(); // 更新UI
				Main.mMessageReportHashMap.put(mUserName + "_login_chk",
						"false");
				try {
					Tools_data.writeData(Main.mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(!SystemVarTools.bLogin){
					mScreenService.show(ScreenLoginAccount.class);
				}
				break;

			case 3000:
				Toast.makeText(getApplicationContext(),
						"" + getText(R.string.tip_net_login_failed_try_later),
						Toast.LENGTH_LONG).show(); // 更新UI
				Main.mMessageReportHashMap.put(mUserName + "_login_chk",
						"false");
				try {
					Tools_data.writeData(Main.mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(!SystemVarTools.bLogin){
					mScreenService.show(ScreenLoginAccount.class);
				}
				break;
			case 403:
				Toast.makeText(getApplicationContext(),
						"" + getText(R.string.login_forbidden),
						Toast.LENGTH_LONG).show(); 
				Main.mMessageReportHashMap.put(mUserName + "_login_chk",
						"false");
				try {
					Tools_data.writeData(Main.mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(!SystemVarTools.bLogin){
					mScreenService.show(ScreenLoginAccount.class);
				}
				break;
			case 404:
				Toast.makeText(getApplicationContext(),
						"" + getText(R.string.login_notexist_user),
						Toast.LENGTH_LONG).show(); 
				Main.mMessageReportHashMap.put(mUserName + "_login_chk",
						"false");
				try {
					Tools_data.writeData(Main.mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(!SystemVarTools.bLogin){
					mScreenService.show(ScreenLoginAccount.class);
				}
				break;
			case 4000:
				// //更新UI
				Toast.makeText(
						getApplicationContext(),
						""
								+ getText(R.string.tip_net_login_failed_check_config),
						Toast.LENGTH_LONG).show(); // 更新UI
				Main.mMessageReportHashMap.put(mUserName + "_login_chk",
						"false");
				try {
					Tools_data.writeData(Main.mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(!SystemVarTools.bLogin){
					mScreenService.show(ScreenLoginAccount.class);
				}
				break;
			case 5000:
				String appName = ChkVer.getAppName(getApplicationContext());
				SystemVarTools.showToast(appName + " " + getText(R.string.tips)
						+ "：\n"
						+ getText(R.string.tip_net_server_connect_failed));
				Main.mMessageReportHashMap.put(mUserName + "_login_chk",
						"false");
				try {
					Tools_data.writeData(Main.mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(!SystemVarTools.bLogin){
					mScreenService.show(ScreenLoginAccount.class);
				}
				break;
			case 6000:
				appName = ChkVer.getAppName(getApplicationContext());
				SystemVarTools.showToast(appName + " " + getText(R.string.tips)
						+ "：\n"
						+ getText(R.string.tip_net_login_failed_timeout));
				Main.mMessageReportHashMap.put(mUserName + "_login_chk",
						"false");
				try {
					Tools_data.writeData(Main.mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				mScreenService.show(ScreenLoginAccount.class);
				break;
			default:
				break;

			}
//			super.handleMessage(msg);
		}
	};
	
	private NgnTimer timeoutTimer = new NgnTimer();

	// duhaitao 修改
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ServiceLoginAccount.setHandler(mHandler);

		setContentView(R.layout.login);

		LinearLayout imageView = (LinearLayout) this
				.findViewById(R.id.login_splash);

		/**
		 * 非Socket服务方式
		 */
		Log.d(TAG, "ScreenSplash GlobalSession.bSocketService:"
				+ GlobalSession.bSocketService);
		if (!GlobalSession.bSocketService) {
			AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
			aa.setDuration(20000);
			imageView.startAnimation(aa);
			aa.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationEnd(Animation arg0) {
//					loginDefault();
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationStart(Animation animation) {
					timeoutTimer.schedule(new TimerTask() {
						
						@Override
						public void run() {
							mHandler.sendEmptyMessage(6000);
							timeoutTimer = null;
							MyLog.e(TAG, "登陆超时");
						}
					}, 43000);
					loginDefault();
				}
			});

		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyLog.d(TAG, "onPause()");
		if(timeoutTimer != null){
			timeoutTimer.cancel();
			timeoutTimer.purge();
			MyLog.d(TAG, "取消登陆超时定时器.");
		}
	}
	@Override
	protected void onStop() {
		super.onStop();
		MyLog.d(TAG, "onStop()");
	}
	@Override
	protected void onDestroy() {
		MyLog.d(TAG, "onDestroy()");
		if (mBroadCastRecv != null) {
			unregisterReceiver(mBroadCastRecv);
		}
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		final Engine engine = getEngine();

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

		/**
		 * Socket服务方式
		 */
		if (GlobalSession.bSocketService) {
			finish();
		}
	}

	private void loginDefault() {
		if (Main.mMessageReportHashMap != null) {
//			mUserName = (String) Main.mMessageReportHashMap.get("recent_user");
			mUserName = SystemVarTools.getSimCardMdn();
			if(mUserName == null){
				mUserName = (String) Main.mMessageReportHashMap.get("recent_user");
			}
			
			pwd = (String) Main.mMessageReportHashMap.get(mUserName + "_pwd");
			MyLog.i(TAG, "UserName = " + mUserName+", pwd = "+ pwd);
			if (!NgnStringUtils.isNullOrEmpty(mUserName) && !NgnStringUtils.isNullOrEmpty(pwd)) {

				new Thread(new Runnable() {
					public void run() {
						boolean a = CrashHandler.isNetworkAvailable();
						if(!a){
							Engine.getInstance().getNetworkService().setNetworkEnable(false);							
							mHandler.sendEmptyMessage(5000);
							Log.d(TAG, "登录网络检测失败，网络已断开");
							return;
						}

						boolean b = ServiceLoginAccount.getInstance().login(
								mUserName.trim(), pwd.trim());
						if(!b){
							mHandler.sendEmptyMessage(4000);
							Log.d(TAG, "auto login failed!");
						}
					}
				}).start();

				Main.mMessageReportHashMap.put("recent_user", mUserName.trim());
				Main.mMessageReportHashMap.put(mUserName.trim() + "_pwd",
						pwd.trim());
				Main.mMessageReportHashMap.put(mUserName.trim() + "_pwd_chk",
						"true");
				Main.mMessageReportHashMap.put(mUserName.trim() + "_login_chk",
						"true");

				try {
					Tools_data.writeData(Main.mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				MyLog.e(TAG, "Map不为空，非自动登录");
				mScreenService.show(ScreenLoginAccount.class);
			}

		} else {
			MyLog.e(TAG, "Map为空");
			mScreenService.show(ScreenLoginAccount.class);
		}
	}

}