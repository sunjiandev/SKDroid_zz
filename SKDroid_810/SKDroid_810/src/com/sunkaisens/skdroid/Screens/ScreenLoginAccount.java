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

//author  duhaitao
package com.sunkaisens.skdroid.Screens;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Properties;

import android.widget.TextView;

import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.Utils.ChkVer;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.animation.CustomAnimation;
//import com.sunkaisens.skdroid.animation.CustomAnimation;
import com.sunkaisens.skdroid.app.service.NativeService;
import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.model.VERSION;
import com.sunkaisens.skdroid.util.GlobalSession;

import android.R.integer;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

// author  duhaitao
public class ScreenLoginAccount extends BaseScreen {
	private static String TAG = ScreenLoginAccount.class.getCanonicalName();
	public static ScreenLoginAccount mLoginAccount = null;

	private EditText mEditAccount;
	private EditText mEditAccountPassword;
	private Button mBtLogin;

	private CustomAnimation mCustomAnimation = new CustomAnimation();

	private ImageView mBtConfigure;

	private ImageView mEditAccountDelete;
	private ImageView mEditAccountPasswordDelete;

	private TextView textTitle;

	private CheckBox mCheck_remember;

	private CheckBox mCheck_autologin;

	private ProgressDialog mProgressDialog = null;

	private String mUserName = null;

	public static boolean btLogin_isClicked;

	private int loginCount = 0;

	private RelativeLayout reloginLayout = null;

	private TextView reloginTimer = null;

	private CountDownTimer mTimer = new CountDownTimer(30000, 1000) {

		@Override
		public void onTick(long millisUntilFinished) {
			reloginTimer.setText("" + millisUntilFinished / 1000);
		}

		@Override
		public void onFinish() {

			mBtLogin.setEnabled(true);
			mEditAccountPassword.setEnabled(true);
			mEditAccount.setEnabled(true);
			mEditAccountDelete.setEnabled(true);
			mEditAccountPasswordDelete.setEnabled(true);
			mBtConfigure.setEnabled(true);
			mCheck_remember.setEnabled(true);

			reloginTimer.setText("30");
			reloginLayout.setVisibility(View.GONE);

			loginCount = 0;

		}

	};

	public ScreenLoginAccount() {
		super(SCREEN_TYPE.LOGIN_T, TAG);

		btLogin_isClicked = false;

	}

	public static ScreenLoginAccount getInstance() {
		if (mLoginAccount == null) {
			mLoginAccount = new ScreenLoginAccount();
		}
		return mLoginAccount;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		SystemVarTools.clear();

		Log.d(TAG, "ScreenLogin create");

		mCustomAnimation.setDuration(1000);

		ServiceLoginAccount.setHandler(mHandler);

		setContentView(R.layout.login_account);

		mEditAccount = (EditText) findViewById(R.id.login_et);
		mEditAccountPassword = (EditText) findViewById(R.id.password_value);

		// 文本编辑框删除图标
		mEditAccountDelete = (ImageView) findViewById(R.id.login_account_name_delete);
		mEditAccountPasswordDelete = (ImageView) findViewById(R.id.login_account_pwd_delete);

		mEditAccountDelete.setVisibility(View.INVISIBLE);
		mEditAccountPasswordDelete.setVisibility(View.INVISIBLE);

		mBtLogin = (Button) findViewById(R.id.login_bt);
		mBtConfigure = (ImageView) findViewById(R.id.configure_bt);

		mCheck_remember = (CheckBox) findViewById(R.id.check_remember);

		mCheck_autologin = (CheckBox) findViewById(R.id.check_autologin);

		textTitle = (TextView) findViewById(R.id.text_title);

		reloginLayout = (RelativeLayout) findViewById(R.id.login_account_relogin);

		reloginLayout.setVisibility(View.GONE);

		reloginTimer = (TextView) findViewById(R.id.login_account_timer);

		String userMdn = SystemVarTools.getSimCardMdn();

		if (Main.mMessageReportHashMap != null) {
			mUserName = (String) Main.mMessageReportHashMap.get("recent_user");

			if (userMdn != null && !userMdn.equals("")) {
				if (userMdn.startsWith("+")) {
					userMdn = userMdn.substring(3);
				}
				mUserName = userMdn;
				mEditAccount.setEnabled(false);
				mEditAccountDelete.setVisibility(View.GONE);
			}
			MyLog.i(TAG, "UserName = " + mUserName);

			mEditAccount.setText(mUserName);

			String pwd = (String) Main.mMessageReportHashMap.get(mEditAccount
					.getText().toString().trim()
					+ "_pwd");
			if (pwd != null) {

				mEditAccountPassword.setText(pwd);

				mEditAccountPasswordDelete.setVisibility(View.VISIBLE);
				// mEditAccountDelete.setVisibility(View.VISIBLE);

			} else {
				mEditAccountPassword.setText("");

				mEditAccountPasswordDelete.setVisibility(View.INVISIBLE);
				// mEditAccountDelete.setVisibility(View.VISIBLE);
			}
			String _pwd_chk = (String) Main.mMessageReportHashMap
					.get(mEditAccount.getText().toString().trim() + "_pwd_chk");
			if (_pwd_chk != null && _pwd_chk.equals("true")) {
				mCheck_remember.setChecked(true);
			} else {
				mCheck_remember.setChecked(false);
			}
			String _login_chk = (String) Main.mMessageReportHashMap
					.get(mEditAccount.getText().toString().trim()
							+ "_login_chk");
			if (_login_chk != null && _login_chk.equals("true")) {
				mCheck_autologin.setChecked(true);
			} else {
				mCheck_autologin.setChecked(false);
			}

		} else {
			Main.mMessageReportHashMap = new HashMap<String, Object>();
			mUserName = userMdn;
			mEditAccount.setEnabled(false);
			mEditAccountDelete.setVisibility(View.GONE);
		}
		mEditAccountDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEditAccount.setText("");
				mEditAccountDelete.setVisibility(View.INVISIBLE);

			}
		});

		mEditAccountPasswordDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEditAccountPassword.setText("");
				mEditAccountPasswordDelete.setVisibility(View.INVISIBLE);
			}
		});

		mEditAccountPassword.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				mEditAccountPasswordDelete.setVisibility(View.VISIBLE);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (mEditAccountPassword.getText().toString().equals("")) {
					mEditAccountPasswordDelete.setVisibility(View.INVISIBLE);
				} else {
					mEditAccountPasswordDelete.setVisibility(View.VISIBLE);
				}
			}
		});

		mEditAccount.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				// if(mEditAccount.isEnabled()){
				// mEditAccountDelete.setVisibility(View.VISIBLE);
				// }else {
				// mEditAccountDelete.setVisibility(View.GONE);
				// }

				if (!mEditAccount.getText().toString().trim().isEmpty()) { // 取出对应的用户密码
					// Main.mMessageReportHashMap = Tools_data.readData();
					if (Main.mMessageReportHashMap != null) {
						String pwd = (String) Main.mMessageReportHashMap
								.get(mEditAccount.getText().toString().trim()
										+ "_pwd");
						if (pwd != null) {
							mEditAccountPassword.setText(pwd);
						} else {
							mEditAccountPassword.setText("");
						}
						String _pwd_chk = (String) Main.mMessageReportHashMap
								.get(mEditAccount.getText().toString().trim()
										+ "_pwd_chk");
						if (_pwd_chk != null && _pwd_chk.equals("true")) {
							mCheck_remember.setChecked(true);
						} else {
							mCheck_remember.setChecked(false);
						}
						String _login_chk = (String) Main.mMessageReportHashMap
								.get(mEditAccount.getText().toString().trim()
										+ "_login_chk");
						if (_login_chk != null && _login_chk.equals("true")) {
							mCheck_autologin.setChecked(true);
						} else {
							mCheck_autologin.setChecked(false);
						}
					} else {
						mEditAccountPassword.setText("");
						mCheck_remember.setChecked(false);
						mCheck_autologin.setChecked(false);
					}
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!mEditAccount.isEnabled()) {
					return;
				}
				if (mEditAccount.getText().toString().equals("")) {
					mEditAccountDelete.setVisibility(View.INVISIBLE);
				} else {
					mEditAccountDelete.setVisibility(View.VISIBLE);
				}
			}
		});

		mEditAccount.setText(mUserName);

		// 登陆界面自组网按钮
		if (SKDroid.sks_version == VERSION.ADHOC) {
			textTitle.setVisibility(View.GONE);
			((Button) findViewById(R.id.login_bt_adhoc))
					.setVisibility(View.VISIBLE);

			((Button) findViewById(R.id.login_bt_adhoc))
					.setOnClickListener(new OnClickListener() {// gle
						@Override
						public void onClick(View v) {

							mScreenService.show(ScreenAdhocLogin.class);
						}
					});
		}

		mBtLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				btLogin_isClicked = true;
				SystemVarTools.isLoginRefreshFail = false;

				if (((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
						.isActive()
						&& ScreenLoginAccount.this.getCurrentFocus() != null)

					((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(ScreenLoginAccount.this
									.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);

				if (mEditAccount.getText().toString().trim().isEmpty()) {
					// Toast.makeText(NgnApplication.getContext(), "请输入用户名!",
					// Toast.LENGTH_SHORT).show();
					return;
				}

				if (mEditAccountPassword.getText().toString().trim().isEmpty()) {
					// Toast.makeText(NgnApplication.getContext(), "请输入密码",
					// Toast.LENGTH_SHORT).show();
					return;
				}
				Main.mMessageReportHashMap.put("recent_user", mEditAccount
						.getText().toString().trim());
				Main.mMessageReportHashMap.put(mEditAccount.getText()
						.toString().trim()
						+ "_pwd", mEditAccountPassword.getText().toString()
						.trim());
				Main.mMessageReportHashMap.put(mEditAccount.getText()
						.toString().trim()
						+ "_pwd_chk", "true");
				try {
					Tools_data.writeData(Main.mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
				Main.mMessageReportHashMap.put(mEditAccount.getText()
						.toString().trim()
						+ "_login_chk", "true");
				try {
					Tools_data.writeData(Main.mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}

				mProgressDialog = ProgressDialog.show(Engine.getInstance()
						.getMainActivity(), "", getText(R.string.loading)
						+ "                ");
				mProgressDialog.show();

				Engine.getInstance().getNetworkService().setNetworkEnable(true);

				new Thread(new Runnable() {
					public void run() {
						// mSipService.register(ScreenLoginAccount.this);
						boolean a = CrashHandler.isNetworkAvailable();
						if (!a) {
							Engine.getInstance().getNetworkService()
									.setNetworkEnable(false);
							mHandler.sendEmptyMessage(5000);
							Log.d(TAG, "登录网络检测失败，网络已断开");
							return;
						}
						SystemVarTools.mIdentityChk = mEditAccount.getText()
								.toString().trim();

						Log.e("用户名", mEditAccount.getText().toString().trim());
						Log.e("密码", mEditAccountPassword.getText().toString());

						loginCount++;

						boolean b = ServiceLoginAccount.getInstance().login(
								mEditAccount.getText().toString().trim(),
								mEditAccountPassword.getText().toString()
										.trim());
						if (!b) { // 向mHandler发消息
							mHandler.sendEmptyMessage(4000);
						}
					}
				}).start();
				Log.e("ScreenLoginAccount-loginOnClick",
						"mSipService 用户正在登录register ");

			}

		});
		mBtConfigure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenLoginSetting.class);
			}
		});

		if (mCheck_autologin.isChecked()) {
			SystemVarTools.isLoginRefreshFail = false;

			if (((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
					.isActive()
					&& ScreenLoginAccount.this.getCurrentFocus() != null)

				((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(ScreenLoginAccount.this
								.getCurrentFocus().getWindowToken(),
								InputMethodManager.HIDE_NOT_ALWAYS);

			if (mEditAccount.getText().toString().trim().isEmpty()) {
				// Toast.makeText(NgnApplication.getContext(), "请输入用户名!",
				// Toast.LENGTH_SHORT).show();
				return;
			}

			if (mEditAccountPassword.getText().toString().trim().isEmpty()) {
				// Toast.makeText(NgnApplication.getContext(), "请输入密码",
				// Toast.LENGTH_SHORT).show();
				return;
			}

			Main.mMessageReportHashMap.put("recent_user", mEditAccount
					.getText().toString().trim());
			Main.mMessageReportHashMap.put(mEditAccount.getText().toString()
					.trim()
					+ "_pwd", mEditAccountPassword.getText().toString().trim());
			Main.mMessageReportHashMap.put(mEditAccount.getText().toString()
					.trim()
					+ "_pwd_chk", "true");
			try {
				Tools_data.writeData(Main.mMessageReportHashMap);
			} catch (IOException e) {
				e.printStackTrace();
			}

			Main.mMessageReportHashMap.put(mEditAccount.getText().toString()
					.trim()
					+ "_login_chk", "true");
			try {
				Tools_data.writeData(Main.mMessageReportHashMap);
			} catch (IOException e) {
				e.printStackTrace();
			}

			mProgressDialog = ProgressDialog.show(Engine.getInstance()
					.getMainActivity(), "", getText(R.string.loading)
					+ "                ");
			mProgressDialog.show();

			Engine.getInstance().getNetworkService().setNetworkEnable(true);

			new Thread(new Runnable() {
				public void run() {
					// mSipService.register(ScreenLoginAccount.this);
					boolean a = CrashHandler.isNetworkAvailable();
					if (!a) {
						Engine.getInstance().getNetworkService()
								.setNetworkEnable(false);
						mHandler.sendEmptyMessage(5000);
						Log.d(TAG, "登录网络检测失败，网络已断开");
						return;
					}
					SystemVarTools.mIdentityChk = mEditAccount.getText()
							.toString().trim();

					Log.e("用户名", mEditAccount.getText().toString().trim());
					Log.e("密码", mEditAccountPassword.getText().toString());

					boolean b = ServiceLoginAccount.getInstance().login(
							mEditAccount.getText().toString().trim(),
							mEditAccountPassword.getText().toString().trim());
					// mProgressDialog.dismiss(); //关闭ProgressDialog
					if (!b) { // 向mHandler发消息
						mHandler.sendEmptyMessage(4000);
					}
				}
			}).start();
			Log.e("ScreenLoginAccount-loginOnClick",
					"mSipService 用户正在登录register ");
		}
	}

	/**
	 * 用Handler来更新UI
	 */
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (mProgressDialog != null)
				mProgressDialog.dismiss(); // 关闭ProgressDialog
			switch (msg.what) {
			case 1000:

				loginCount = 0;

				mScreenService.show(ScreenTabHome.class);
				// Toast.makeText(getApplicationContext(), "登录成功！",
				// Toast.LENGTH_LONG).show(); //更新UI

				break;

			case 2000:

				Toast.makeText(
						getApplicationContext(),
						""
								+ getText(R.string.tip_net_login_failed_check_name_pass),
						Toast.LENGTH_LONG).show(); // 更新UI

				mBtLogin.startAnimation(mCustomAnimation);

				break;

			case 3000:
				Toast.makeText(getApplicationContext(),
						"" + getText(R.string.tip_net_login_failed_try_later),
						Toast.LENGTH_LONG).show(); // 更新UI
				mBtLogin.startAnimation(mCustomAnimation);
				break;
			case 403:

				if (loginCount < 5) { // 密码错误小于5次时，toast提示
					Toast.makeText(getApplicationContext(),
							"" + getText(R.string.login_forbidden),
							Toast.LENGTH_LONG).show();
					mBtLogin.startAnimation(mCustomAnimation);
				} else { // 密码错误大于5次时，锁屏5秒钟

					mBtLogin.setEnabled(false);
					mEditAccountPassword.setEnabled(false);
					mEditAccount.setEnabled(false);
					mEditAccountDelete.setEnabled(false);
					mEditAccountPasswordDelete.setEnabled(false);
					mBtConfigure.setEnabled(false);
					mCheck_remember.setEnabled(false);

					reloginLayout.setVisibility(View.VISIBLE);
					mTimer.start();

				}

				break;
			case 404:
				Toast.makeText(getApplicationContext(),
						"" + getText(R.string.login_notexist_user),

						Toast.LENGTH_LONG).show();
				mBtLogin.startAnimation(mCustomAnimation);

				break;

			case 4000:
				// Toast.makeText(getApplicationContext(),
				// "Failed to start the SIP stack", Toast.LENGTH_LONG).show();
				// //更新UI
				Toast.makeText(
						getApplicationContext(),
						""
								+ getText(R.string.tip_net_login_failed_check_config),
						Toast.LENGTH_LONG).show(); // 更新UI

				mBtLogin.startAnimation(mCustomAnimation);
				break;
			case 5000:
				String appName = ChkVer.getAppName(getApplicationContext());
				SystemVarTools.showToast(appName + " " + getText(R.string.tips)
						+ "：\n"
						+ getText(R.string.tip_net_server_connect_failed));
				mBtLogin.startAnimation(mCustomAnimation);
				break;
			default:
				break;

			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onResume() { // zhaohua add 20140712
		super.onResume();

		Log.d(TAG, "ScreenLogin resume");
		if (GlobalSession.bSocketService) {
			finish();
		}

		if (SystemVarTools.isLoginRefreshFail) {
			((Engine) Engine.getInstance()).showAppNotif(
					R.drawable.bullet_ball_glass_grey_16,
					getString(R.string.logout));
			SystemVarTools.isLoginRefreshFail = false;
		}

	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "ScreenLogin destroy");
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "ScreenLogin pause");
		super.onPause();
	}

	@Override
	protected void onRestart() {
		Log.d(TAG, "ScreenLogin restart");
		super.onRestart();
	}

	@Override
	protected void onStart() {
		Log.d(TAG, "ScreenLogin start");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "ScreenLogin stop");
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
		}
		super.onStop();
	}

}