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

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;

import com.sunkaisens.skdroid.R;

import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.CustomDialog;
import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.Services.IServiceScreen;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public abstract class BaseScreen extends Activity implements IBaseScreen {
	private static boolean isFirstback = true;
	private static final String TAG = BaseScreen.class.getCanonicalName();

	public static enum SCREEN_TYPE {
		// Well-Known
		MAIN_T, ABOUT_T, AV_QUEUE_T, CHAT_T, CHAT_QUEUE_T, CODECS_T, CONTACTS_T, DIALER_T, FILETRANSFER_QUEUE_T, FILETRANSFER_VIEW_T, HOME_T, IDENTITY_T, INTERCEPT_CALL_T, GENERAL_T, MESSAGING_T, NATT_T, NETWORK_T, PRESENCE_T, QOS_T, SETTINGS_T, SECURITY_T, SPLASH_T,

		TAB_CONTACTS, TAB_HISTORY_T, TAB_INFO_T, TAB_ONLINE, TAB_MESSAGES_T,TAB_HOME,
		ABOUT_EXPANDED_T,

		// All others
		AV_T,
		
		NEW_SMS_T,

		// For GPSScreen by rockman 2012/7/24
		GPSScreen_T,
		// For GroupContactsScreen by rockman 2012/9/7
		GroupContactsScreen_T,
		// For SendMessage by rockman 2012/11/7
		ScreenSendMessage_T, GROUPCONTACTITEM_T, CONFIGURE_T, SCREENLOGIN_T, LOGIN_T,
		//
		SKS_ScreenNewMessage_T,SKS_Screen_PersionInfo,SKS_Screen_OrgInfo,
		CONTACT_CHILD, TAB_HOME_ADHOC, LOGIN_ADHOC,
		MEDIA_AV_T
	}

	protected String mId;
	protected final SCREEN_TYPE mType;
	protected boolean mComputeConfiguration; // 标志各个Screen子类中的各个控件状态（文字改变、selected等等）是否有变化。true：有变化-----则向Preferences中写入所有控件代表的那些配置信息的Kye-Value，以达到保存对配置信息更改的目的！
	protected ProgressDialog mProgressDialog;
	protected Handler mHanler;

	protected final IServiceScreen mScreenService;

	protected BaseScreen(SCREEN_TYPE type, String id) {
		super();
		mType = type;
		mId = id;
		mScreenService = ((Engine) Engine.getInstance()).getScreenService();
	}

	protected Engine getEngine() {
		return (Engine) Engine.getInstance();
	}

	//do refresh
	@Override
	public boolean refresh()
	{
		return false;
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHanler = new Handler();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!processKeyDown(keyCode, event)) {
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public SCREEN_TYPE getType() {
		return mType;
	}

	@Override
	public boolean hasMenu() {
		return false;
	}

	@Override
	public boolean hasBack() {
		return false;
	}

	@Override
	public boolean back() {
		return mScreenService.back();
	}

	@Override
	public boolean createOptionsMenu(Menu menu) {
		return false;
	}

	protected void addConfigurationListener(RadioButton radioButton) { // 对某个RadioButton控件添加“状态修改监听者”，监视修改动作。
		radioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mComputeConfiguration = true;
			}
		});
	}

	protected void addConfigurationListener(EditText editText) { // 对某个EditText控件添加“状态修改监听者”，监视修改动作。
		editText.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mComputeConfiguration = true;
			}
		});
	}

	protected void addConfigurationListener(CheckBox checkBox) { // 对某个CheckBox控件添加“状态修改监听者”，监视修改动作。
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mComputeConfiguration = true;
			}
		});
	}
	
	
	protected void addConfigurationListener(ToggleButton checkBox) { // 对某个CheckBox控件添加“状态修改监听者”，监视修改动作。
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				mComputeConfiguration = true;
			}
		});
	}
	

	protected void addConfigurationListener(TextView textView) { // 对某个CheckBox控件添加“状态修改监听者”，监视修改动作。
		textView.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				mComputeConfiguration = true;
			}
		});
	}

	protected void addConfigurationListener(Spinner spinner) { // 对某个Spinner控件添加“状态修改监听者”，监视修改动作。
		// setOnItemClickListener not supported by Spinners
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mComputeConfiguration = true;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	protected int getSpinnerIndex(String value, String[] values) {
		for (int i = 0; i < values.length; i++) {
			if (NgnStringUtils.equals(value, values[i], true)) {
				return i;
			}
		}
		return 0;
	}

	protected int getSpinnerIndex(int value, int[] values) {
		for (int i = 0; i < values.length; i++) {
			if (value == values[i]) {
				return i;
			}
		}
		return 0;
	}

	protected void showInProgress(String text, boolean bIndeterminate,
			boolean bCancelable) {
		synchronized (this) {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(this);
				mProgressDialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						mProgressDialog = null;
					}
				});
				mProgressDialog.setMessage(text);
				mProgressDialog.setIndeterminate(bIndeterminate);
				mProgressDialog.setCancelable(bCancelable);
				mProgressDialog.show();
			}
		}
	}

	protected void cancelInProgress() {
		synchronized (this) {
			if (mProgressDialog != null) {
				mProgressDialog.cancel();
				mProgressDialog = null;
			}
		}
	}

	protected void cancelInProgressOnUiThread() {
		mHanler.post(new Runnable() {
			@Override
			public void run() {
				cancelInProgress();
			}
		});
	}

	protected void showInProgressOnUiThread(final String text,
			final boolean bIndeterminate, final boolean bCancelable) {
		mHanler.post(new Runnable() {
			@Override
			public void run() {
				showInProgress(text, bIndeterminate, bCancelable);
			}
		});
	}

	protected void showMsgBox(String title, String message) {
		CustomDialog.show(this, R.drawable.icon, title, message, "OK",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}, null, null);
	}

	protected void showMsgBoxOnUiThread(final String title, final String message) {
		mHanler.post(new Runnable() {
			@Override
			public void run() {
				showMsgBox(title, message);
			}
		});
	}

//	protected String getPath(Uri uri){
//		String filePath = "";
//		
//		String[] projection ={MediaColumns.DATA};
//		Cursor cursor = managedQuery(uri, projection, null, null, null);
//		if (cursor!=null) {
//			int columnIndex = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
//			cursor.moveToFirst();
//			filePath = cursor.getString(columnIndex);
//			if (Integer.parseInt(Build.VERSION.SDK)<14) {
//				cursor.close();
//			}
//		}
//		
//		return filePath;
//	}

	@SuppressLint("NewApi")
	protected String getPath(Uri uri){
		String filePath = "";
		MyLog.d(TAG, "getPath()URI="+uri.toString());
		if(uri.toString().contains("content://com.android.providers.media.documents")
				&&DocumentsContract.isDocumentUri(getApplicationContext(), uri)){
			
			String wholeId = DocumentsContract.getDocumentId(uri);
			Log.d(TAG, "wholeId:"+wholeId);
			String id = wholeId.split(":")[1];
			String type = wholeId.split(":")[0];
			Uri contentUri = null;
			if ("image".equals(type)) {
	            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	        } else if ("video".equals(type)) {
	            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	        } else if ("audio".equals(type)) {
	            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	        }
			
            final String selection = "_id=?";
            final String[] selectionArgs = new String[] {
            		id
            };
            final String[] projection = {
                 "_data"
            };
			Cursor cursor = getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
			if (cursor == null) { //Source is Dropbox or other similar local file
				//path
				filePath = uri.getPath();
			}
			else {
				cursor.moveToFirst();
				String[] colns = cursor.getColumnNames();
				if(colns.length > 0){
					StringBuilder str = new StringBuilder(); 
					for(String col : colns){
						str.append("[");
						str.append(col);
						str.append("] ");
					}
					MyLog.d(TAG, "Columns:"+str.toString());
				}
				try {
					int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
					filePath = cursor.getString(idx);
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
				cursor.close();
			}
			return filePath;
		}else if(uri.toString().contains("content://com.android.externalstorage.documents")
				&&DocumentsContract.isDocumentUri(getApplicationContext(), uri)){
			String wholeId = DocumentsContract.getDocumentId(uri);
			Log.d(TAG, "wholeId:"+wholeId);
			String id = wholeId.split(":")[1];
			String type = wholeId.split(":")[0];
			filePath = SystemVarTools.sdcardRootPath+"/"+id;
			return filePath;
			
		}else{
			Cursor cursor = getContentResolver().query(uri, null, null, null, null);
			if (cursor == null) { //Source is Dropbox or other similar local file
				//path
				filePath = uri.getPath();
			}
			else {
				try {
					cursor.moveToFirst();
					int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
					filePath = cursor.getString(idx);
					cursor.close();
				} catch (Exception e) {
					e.printStackTrace();
					filePath = null;
				}
				
			}
			return filePath;
		}
	}
	
	@SuppressLint("NewApi") 
	protected String getPath2(Uri uri){
		String filePath = null;
		//以content://开头的uri
		if(uri.getScheme().toString().compareTo("content") == 0){
			if(uri.toString().contains("content://com.android.providers.media.documents")
					&&DocumentsContract.isDocumentUri(getApplicationContext(), uri)){
				File file = new File(URI.create(uri.toString()));
				MyLog.d(TAG, "FilePath2="+file.getAbsolutePath());
			}
		}
		else if (uri.getScheme().toString().compareTo("file") == 0) {
			filePath = uri.getPath();
		}
		return filePath;
	}
	
	private static long exitTime = 0;

	public static boolean processDoubleBackExitKey(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast mBackTipToast = Toast.makeText((Main) ( ((Engine) Engine.getInstance()).getMainActivity()),
						(CharSequence) ( ((Engine) Engine.getInstance()).getMainActivity())
								.getResources().getString(R.string.quit_tip), 2000);
				mBackTipToast.show();
				exitTime = System.currentTimeMillis();
				return true;
			} else {
				Log.d("ScreenTabHome-processBackKeyDown()","退出应用！");
				//remarked by zhaohua on 20140317
				//后台服务进程的开发（系统退出后，仍能提供服务）
//				 ((Engine) Engine.getInstance()).getSipService().unRegister();
				Main main = ((Main) ( ((Engine) Engine.getInstance()).getMainActivity()));
				if(main != null){
					main.showTableHome();
				}
				//finish();
				try {
					Tools_data.writeData(Main.mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}
	
	public static boolean processOneBackExitKey(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			 ((Engine) Engine.getInstance()).getSipService().unRegister();
			 Main main = ((Main) ( ((Engine) Engine.getInstance()).getMainActivity()));
			 if(main != null){
				main.exit();
			 }
				return true;
		}
		return false;
	}
	
	public static boolean processKeyDown(int keyCode, KeyEvent event) {
		
		final IServiceScreen screenService = ((Engine) Engine.getInstance())
				.getScreenService();
		final IBaseScreen currentScreen = screenService.getCurrentScreen();
		if (currentScreen != null) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
				if (currentScreen.getType() != SCREEN_TYPE.TAB_HOME
						&& currentScreen.getType() != SCREEN_TYPE.LOGIN_T) {
					if (currentScreen.hasBack()) {
						if (!currentScreen.back()) {
							return false;
						}
					} else {
						screenService.back();
					}
					return true;
				} else if (currentScreen.getType() == SCREEN_TYPE.TAB_HOME) {
//					boolean temp = isFirstback;
//					isFirstback = !temp;
//					return processDoubleBackExitKey(keyCode, event, temp);
					return processDoubleBackExitKey(keyCode, event);
					
				} else if (currentScreen.getType() == SCREEN_TYPE.LOGIN_T) {
					 
					 return processOneBackExitKey(keyCode, event);
				}

			}
			
			else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
					|| keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
				//Log.d(TAG,"currentScreen: " + currentScreen.toString() + ", currentScreenType = " + currentScreen.getType());
				if (currentScreen.getType() == SCREEN_TYPE.AV_T) {
					Log.d(TAG, "intercepting volume changed event");
					if (((ScreenAV) currentScreen)
							.onVolumeChanged((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))) {
						return true;
					}
				}
				else if (currentScreen.getType() == SCREEN_TYPE.MEDIA_AV_T) {
					Log.d(TAG, "intercepting volume changed event");
					if (((ScreenMediaAV) currentScreen)
							.onVolumeChanged((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN))) {
						return true;
					}
				}
			} else if (keyCode == KeyEvent.KEYCODE_MENU
					&& event.getRepeatCount() == 0) {
				if (currentScreen instanceof Activity
						&& currentScreen.hasMenu()) {
					return false;
					// return ((Activity)currentScreen).onKeyDown(keyCode,
					// event);
				}
				
				return true;
			}else if(keyCode == 119){
				MyLog.d(TAG, "processKeyDown() keyCode="+keyCode);
				event.startTracking();
			}
		}
		return false;
	}
}
