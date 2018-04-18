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
package com.sunkaisens.skdroid.Services;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.doubango.ngn.services.impl.NgnBaseService;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.IBaseScreen;
import com.sunkaisens.skdroid.Screens.ScreenAV;
import com.sunkaisens.skdroid.Screens.ScreenMediaAV;
import com.sunkaisens.skdroid.Screens.ScreenPersonInfo;
import com.sunkaisens.skdroid.Screens.ScreenTabHome;
import com.sunkaisens.skdroid.model.ModelContact;

import android.R.integer;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.LinearLayout;

public class ServiceScreen extends NgnBaseService implements IServiceScreen {
	private final static String TAG = ServiceScreen.class.getCanonicalName();
	private int mLastScreensIndex = -1; // ring cursor
	private String[] mLastScreens = new String[] { // ring
	null, null, null, null };	

	private List<String> mLastScreensN = new ArrayList<String>();
	
	private boolean mIsScreenAV = false;
	
	@Override
	public boolean start() {
		Log.d(TAG, "starting...");
		return true;
	}

	@Override
	public boolean stop() {
		Log.d(TAG, "stopping...");
		return true;
	}

	@Override
	public boolean back() {
		String screen;
		try {

//			Log.d(TAG, "mLastScreens:"+mLastScreens+"\n|mLastScreensIndex:"+mLastScreensIndex);
			
			// no screen in the stack
			if (mLastScreensIndex < 0) {
				return true;
			}

			// zero is special case
			if (mLastScreensIndex == 0) {
				if ((screen = mLastScreens[mLastScreens.length - 1]) == null) {
					// goto home
					return show(ScreenTabHome.class);
				} else {
					Main main = ((Main) Engine.getInstance().getMainActivity());
					if(main == null){
						MyLog.e(TAG, "main instance is null.");
						return show(ScreenTabHome.class);
					}
					final Activity screena = (Activity) main
							.getLocalActivityManager().getActivity(screen);
					if (screena == null
							|| screena.getClass() == ScreenTabHome.class) {
						return show(ScreenTabHome.class);
					} else
						return show(screena.getClass(), screen);
					// return this.show(screen);
				}
			}
			// all other cases	
			if(mIsScreenAV){
				screen = mLastScreens[mLastScreensIndex];
				mLastScreens[mLastScreensIndex] = null;
				mIsScreenAV = false;
			}else {
				screen = mLastScreens[mLastScreensIndex - 1];
				mLastScreens[mLastScreensIndex - 1] = null;
			}
			mLastScreensIndex--;
			
			Log.d(TAG, "mLastScreens:"+mLastScreens);
			Log.d(TAG, "mLastScreensIndex:"+mLastScreensIndex);
			
			if (screen == null || screen.isEmpty() || !show(screen)) {
				return show(ScreenTabHome.class);
			}

		} catch (Exception e) {
			Log.d(TAG, "Exception: " + e.getMessage());
		}

		return true;
	}

	@Override
	public boolean bringToFront(int action, String[]... args) {

		Intent intent = new Intent(SKDroid.getContext(), Main.class);
		try {
			intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra("action", action);
			if(args != null){
				for (String[] arg : args) {
					if (arg.length != 2) {
						continue;
					}
					intent.putExtra(arg[0], arg[1]);
					//Log.d(TAG,"Receive putExtra "+arg[0]+"="+arg[1]);
				}
			}
			SKDroid.getContext().startActivity(intent);
			return true;
		} catch (Exception e) {
			Log.d(TAG, "Exception: " + e.getMessage());
			return false;
		}
	}

	@Override
	public boolean bringToFront(String[]... args) {
		return this.bringToFront(Main.ACTION_NONE);
	}

	@Override
	public boolean show(Class<? extends Activity> cls, String id) { // 显示/启动各个screen的Activity
																	// id标示屏
		
		MyLog.d(TAG, "show("+cls.getSimpleName()+", "+id+")");
		
		try {

			final Main mainActivity = (Main) Engine.getInstance()
					.getMainActivity();
			if(mainActivity == null){
				MyLog.e(TAG, "main instance is null.");
				return show(ScreenTabHome.class);
			}
			String screen_id = (id == null) ? cls.getCanonicalName() : id;
//			MyLog.d(TAG, "screen_id==" + screen_id+"|mLastScreensIndex:"+mLastScreensIndex);
			Intent intent = new Intent(mainActivity, cls); // 源——mainActivity,目的——cls
			intent.putExtra("id", screen_id); // 屏ID
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//

			// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//
			// FLAG_ACTIVITY_CLEAR_TOP标志：原栈：A,B,C,D.
			// 栈顶D通过Intent跳转到B，并设置此标志后，栈会变为：A,B!!而不是A,B,C,D,B!!
//			final Window window = mainActivity.getLocalActivityManager()
//					.startActivity(screen_id, intent);// 通过ID启动一个Activity，ID用于跟踪此Activity状态。
			
			LocalActivityManager lam = mainActivity.getLocalActivityManager();
			final Window window = lam.startActivity(screen_id, intent);// 通过ID启动一个Activity，ID用于跟踪此Activity状态。
//			Log.d(TAG, "im in the ScreenService show() mainActivity==window"
//					+ window);		

			if (window != null) {
				View view = window.getDecorView();// mainActivity.getLocalActivityManager().startActivity(screen_id,
													// intent).getDecorView();												
				
				LinearLayout layout = (LinearLayout) mainActivity
						.findViewById(R.id.main_linearLayout_principal);
				
				layout.removeAllViews();
				layout.addView(view, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));

				// add to stack
			
//				Log.d(TAG, "cls.getCanonicalName():"+cls.getCanonicalName());
				
				if(!cls.getCanonicalName().equals(ScreenAV.class.getCanonicalName())
						&& !cls.getCanonicalName().equals(ScreenMediaAV.class.getCanonicalName())){
					this.mLastScreens[(++this.mLastScreensIndex % this.mLastScreens.length)] = screen_id;
					this.mLastScreensIndex %= this.mLastScreens.length;
				}else {
					mIsScreenAV = true;
				}
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean showPersonOrOrgInfo(Class<? extends Activity> cls, String id) { // 显示/启动各个screen的Activity
											
		MyLog.d(TAG, "showPO("+cls.getSimpleName()+", "+id+")");
		// id标示屏
		try {

			final Main mainActivity = (Main) Engine.getInstance()
					.getMainActivity();	
			if(mainActivity == null){
				MyLog.e(TAG, "main instance is null.");
				return show(ScreenTabHome.class);
			}
			String screen_id = cls.getCanonicalName();
			Intent intent = new Intent(mainActivity, cls); // 源——mainActivity,目的——cls			
			intent.putExtra("id", id); 
			
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//

			// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//
			// FLAG_ACTIVITY_CLEAR_TOP标志：原栈：A,B,C,D.
			// 栈顶D通过Intent跳转到B，并设置此标志后，栈会变为：A,B!!而不是A,B,C,D,B!!
//			Log.d(TAG, "im in the ScreenService show() mainActivity==intent"
//					+ intent);			
//			final Window window = mainActivity.getLocalActivityManager()
//					.startActivity(screen_id, intent);// 通过ID启动一个Activity，ID用于跟踪此Activity状态。
			
			LocalActivityManager lam = mainActivity.getLocalActivityManager();
			final Window window = lam.startActivity(screen_id, intent);// 通过ID启动一个Activity，ID用于跟踪此Activity状态。

			if (window != null) {
				View view = window.getDecorView();// mainActivity.getLocalActivityManager().startActivity(screen_id,
													// intent).getDecorView();												
				
				LinearLayout layout = (LinearLayout) mainActivity
						.findViewById(R.id.main_linearLayout_principal);
				
				layout.removeAllViews();
				layout.addView(view, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));

				// add to stack
			
				this.mLastScreens[(++this.mLastScreensIndex % this.mLastScreens.length)] = screen_id;
				this.mLastScreensIndex %= this.mLastScreens.length;
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean show(Class<? extends Activity> cls) { // 显示/启动各个screen的Activity
																	// id标示屏
		
		MyLog.d(TAG, "show("+cls.getSimpleName()+", null)");
		
		try {

			final Main mainActivity = (Main) Engine.getInstance()
					.getMainActivity();
			if(mainActivity == null){
				MyLog.e(TAG, "main instance is null.");
				return show(ScreenTabHome.class);
			}
			String screen_id = cls.getCanonicalName();
//			MyLog.d(TAG, "screen_id==" + screen_id+"|mLastScreensIndex:"+mLastScreensIndex);
			Intent intent = new Intent(mainActivity, cls); // 源——mainActivity,目的——cls
			intent.putExtra("id", screen_id); // 屏ID
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			// intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);//

			// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//
			// FLAG_ACTIVITY_CLEAR_TOP标志：原栈：A,B,C,D.
			// 栈顶D通过Intent跳转到B，并设置此标志后，栈会变为：A,B!!而不是A,B,C,D,B!!
//			final Window window = mainActivity.getLocalActivityManager()
//					.startActivity(screen_id, intent);// 通过ID启动一个Activity，ID用于跟踪此Activity状态。
			
			LocalActivityManager lam = mainActivity.getLocalActivityManager();
			final Window window = lam.startActivity(screen_id, intent);// 通过ID启动一个Activity，ID用于跟踪此Activity状态。
//			Log.d(TAG, "im in the ScreenService show() mainActivity==window"
//					+ window);		

			if (window != null) {
				View view = window.getDecorView();// mainActivity.getLocalActivityManager().startActivity(screen_id,
													// intent).getDecorView();												
				
				LinearLayout layout = (LinearLayout) mainActivity
						.findViewById(R.id.main_linearLayout_principal);
				
				layout.removeAllViews();
				layout.addView(view, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));

				// add to stack
			
//				Log.d(TAG, "cls.getCanonicalName():"+cls.getCanonicalName());
				
				if(!cls.getCanonicalName().equals(ScreenAV.class.getCanonicalName())
						&& !cls.getCanonicalName().equals(ScreenMediaAV.class.getCanonicalName())){
					this.mLastScreens[(++this.mLastScreensIndex % this.mLastScreens.length)] = screen_id;
					this.mLastScreensIndex %= this.mLastScreens.length;
				}else {
					mIsScreenAV = true;
				}
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean show(String id) {

		try {

			Main main = ((Main) Engine.getInstance().getMainActivity());
			if(main == null){
				MyLog.e(TAG, "main instance is null.");
				return show(ScreenTabHome.class);
			}
			final Activity screen = (Activity) main.getLocalActivityManager().getActivity(
					id);
			if (screen == null) {
				Log.e(TAG, String.format(
						"Failed to retrieve the Screen with id=%s", id));
				return false;
			} else {
				return this.show(screen.getClass(), id);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void runOnUiThread(Runnable r) {
		Activity main = Engine.getInstance().getMainActivity(); 
		if (main != null) {
			main.runOnUiThread(r);
		} else {
			Log.e(this.getClass().getCanonicalName(), "No Main activity");
		}
	}
	
	public void clearScreenList(){
		mLastScreens = new String[]{
				null,null,null,null
		};
	}

	@Override
	public boolean destroy(String id) {

		try {

			Main main = (((Main) Engine.getInstance().getMainActivity()));
			if(main == null){
				MyLog.e(TAG, "main instance is null.");
				return show(ScreenTabHome.class);
			}
			
			final LocalActivityManager activityManager = main
					.getLocalActivityManager();
			if (activityManager != null) {
				activityManager.destroyActivity(id, true);

				// http://code.google.com/p/android/issues/detail?id=12359
				// http://www.netmite.com/android/mydroid/frameworks/base/core/java/android/app/LocalActivityManager.java
				try {
					final Field mActivitiesField = LocalActivityManager.class
							.getDeclaredField("mActivities");
					if (mActivitiesField != null) {
						mActivitiesField.setAccessible(true);
						@SuppressWarnings("unchecked")
						final Map<String, Object> mActivities = (Map<String, Object>) mActivitiesField
								.get(activityManager);
						if (mActivities != null) {
							mActivities.remove(id);
						}
						final Field mActivityArrayField = LocalActivityManager.class
								.getDeclaredField("mActivityArray");
						if (mActivityArrayField != null) {
							mActivityArrayField.setAccessible(true);
							@SuppressWarnings("unchecked")
							final ArrayList<Object> mActivityArray = (ArrayList<Object>) mActivityArrayField
									.get(activityManager);
							if (mActivityArray != null) {
								for (Object record : mActivityArray) {
									final Field idField = record.getClass()
											.getDeclaredField("id");
									if (idField != null) {
										idField.setAccessible(true);
										final String _id = (String) idField
												.get(record);
										if (id.equals(_id)) {
											mActivityArray.remove(record);
											break;
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}

		} catch (Exception e) {
			Log.d(TAG, "Exception: " + e.getMessage());
		}

		return false;
	}

	@Override
	public void setProgressInfoText(String text) {
	}

	@Override
	public IBaseScreen getCurrentScreen() {
		Main main = ((Main) Engine.getInstance().getMainActivity());
		if(main == null){
			MyLog.e(TAG, "main instance is null.");
			return null;
		}
		return (IBaseScreen)main.getLocalActivityManager().getCurrentActivity();
	}

	@Override
	public IBaseScreen getScreen(String id) {
		Main main = ((Main) Engine.getInstance().getMainActivity());
		if(main == null){
			MyLog.e(TAG, "main instance is null.");
			return null;
		}
		return (IBaseScreen) main.getLocalActivityManager().getActivity(id);
	}

	@Override
	public boolean show(Class<? extends Activity> cls, Bundle params) {

		try {

			final Main mainActivity = (Main) Engine.getInstance()
					.getMainActivity();
			if(mainActivity == null){
				return false;
			}
			String screen_id = cls.getCanonicalName();
			Intent intent = new Intent(mainActivity, cls); // 源——mainActivity,目的——cls
			intent.putExtra("id", screen_id); // 屏ID
			//
			intent.putExtras(params);
			//
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//
			// FLAG_ACTIVITY_CLEAR_TOP标志：原栈：A,B,C,D.
			// 栈顶D通过Intent跳转到B，并设置此标志后，栈会变为：A,B!!而不是A,B,C,D,B!!
			Log.d(TAG, "im in the ScreenService show() mainActivity==intent"
					+ intent);
			final Window window = mainActivity.getLocalActivityManager()
					.startActivity(screen_id, intent);// 通过ID启动一个Activity，ID用于跟踪此Activity状态。
			Log.d(TAG, "im in the ScreenService show() mainActivity==window"
					+ window);

			if (window != null) {
				Log.d(TAG, "2 window.getDecorView start");
				View view = window.getDecorView();// mainActivity.getLocalActivityManager().startActivity(screen_id,
													// intent).getDecorView();

				LinearLayout layout = (LinearLayout) mainActivity
						.findViewById(R.id.main_linearLayout_principal);
				layout.removeAllViews();
				layout.addView(view, new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));
				if(!cls.getCanonicalName().equals(ScreenAV.class.getCanonicalName())
						|| !cls.getCanonicalName().equals(ScreenMediaAV.class.getCanonicalName())){
				// add to stack
					this.mLastScreens[(++this.mLastScreensIndex % this.mLastScreens.length)] = screen_id;
					this.mLastScreensIndex %= this.mLastScreens.length;
				}else {
					mIsScreenAV = true;
				}
				
				Log.d(TAG, "mLastScreens:"+mLastScreens);
				Log.d(TAG, "mLastScreensIndex:"+mLastScreensIndex);

				return true;
			}

		} catch (Exception e) {
			Log.d(TAG, "Exception: " + e.getMessage());
		}

		return false;
	}

}
