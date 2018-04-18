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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.doubango.ngn.services.INgnSipService;
import org.doubango.utils.MyLog;

import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Services.ServiceAdhoc;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.Services.ServiceRegiste;
import com.sunkaisens.skdroid.Utils.CropOption;
import com.sunkaisens.skdroid.Utils.CropOptionAdapter;
import com.sunkaisens.skdroid.Utils.MyiconHttpUpLoadClient;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ScreenTabHome extends BaseScreen implements
		OnCheckedChangeListener {
	public static String TAG = ScreenTabHome.class.getCanonicalName();
	private TabHost mHost = null;
	LocalActivityManager lam;
	private RadioGroup mainTab = null;
	private BroadcastReceiver mSipBroadCastRecv;
	private final String ACTION_CHANGACCOUNT_EVENT = "com.sunkaisens.changeaccount";

	private LinearLayout neterror;

	private TextView mErrorInfo;

	private RelativeLayout taball;

	private INgnSipService mSipService;

	// private Handler mContactReq = null; // 向GroupServer请求通讯录之前增加一条服务器能力查询请求

	public static Uri mImageCaptureUri;

	public ScreenTabHome() {
		super(SCREEN_TYPE.TAB_HOME, TAG);
		mSipService = Engine.getInstance().getSipService();
		GlobalVar.mLocalNum = SystemVarTools.getmIdentity();
		Log.d(TAG, "GlobalVar.mLocalNum:" + GlobalVar.mLocalNum);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Log.e("ScreenTabHome", "ScreenTabHome OnCreate");

		// setContentView(R.layout.screen_tab_info);
		setContentView(R.layout.screen_home);

		taball = (RelativeLayout) findViewById(R.id.taball);

		neterror = (LinearLayout) findViewById(R.id.neterror);
		mErrorInfo = (TextView) findViewById(R.id.error_info);
		neterror.setOnClickListener(neterror_clickListener);

		if (!CrashHandler.isNetworkAvailable2()) {
			neterror.setClickable(true);
			neterror.setVisibility(View.VISIBLE);
			mErrorInfo.setText(ScreenTabHome.this
					.getString(R.string.net_broken));
			neterror.setBackgroundColor(getResources().getColor(
					R.color.color_neterror));
		}

		//
		mainTab = (RadioGroup) findViewById(R.id.main_tab);
		mainTab.setOnCheckedChangeListener(this);

		// ScreenTabContact.groupradioheight=mainTab.getHeight();

		mHost = (TabHost) findViewById(android.R.id.tabhost);
		lam = new LocalActivityManager(this, false);
		lam.dispatchCreate(savedInstanceState);
		//
		mHost.setup(lam);
		//
		// tab1.setIndicator()
		TabSpec tab1 = mHost.newTabSpec("TS_CALL");
		tab1.setIndicator(getString(R.string.call),
				getResources().getDrawable(R.drawable.chat_48));
		tab1.setContent(new Intent(this, ScreenTabCall.class));
		//
		TabSpec tab2 = mHost.newTabSpec("TS_CONTACTS");
		tab2.setIndicator(getString(R.string.contacts), getResources()
				.getDrawable(R.drawable.chat_48));
		tab2.setContent(new Intent(this, ScreenTabContact.class));
		//
		TabSpec tab3 = mHost.newTabSpec("TS_MESSAGE");
		tab3.setIndicator(getString(R.string.messages), getResources()
				.getDrawable(R.drawable.chat_48));
		tab3.setContent(new Intent(this, ScreenTabMessage.class));
		//
		TabSpec tab4 = mHost.newTabSpec("TS_MORE");
		tab4.setIndicator(getString(R.string.more),
				getResources().getDrawable(R.drawable.chat_48));
		tab4.setContent(new Intent(this, ScreenTabMore.class));

		mHost.addTab(tab1);
		mHost.addTab(tab2);
		mHost.addTab(tab3);
		mHost.addTab(tab4);

		RadioButton rbButton0 = (RadioButton) this
				.findViewById(R.id.radio_button0);
		RadioButton rbButton1 = (RadioButton) this
				.findViewById(R.id.radio_button1);
		RadioButton rbButton2 = (RadioButton) this
				.findViewById(R.id.radio_button2);
		RadioButton rbButton3 = (RadioButton) this
				.findViewById(R.id.radio_button3);
		RadioButton rbButton4 = (RadioButton) this
				.findViewById(R.id.radio_button4);

		Drawable draw[] = rbButton0.getCompoundDrawables();

		mSipBroadCastRecv = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				// Registration Event
				if (MessageTypes.MSG_REG_EVENT.equals(action)) {
					int args = intent
							.getIntExtra(MessageTypes.MSG_REG_EVENT, 0);

					switch (args) {
					case MessageTypes.MSG_REG_NOK:
						if (SystemVarTools.bLogin && neterror != null) {
							// 登录失败!
							neterror.setClickable(true);
							mErrorInfo.setText(getString(R.string.logfail));
							neterror.setVisibility(View.VISIBLE);
							neterror.setBackgroundColor(getResources()
									.getColor(R.color.color_neterror));
						}
						break;
					case MessageTypes.MSG_REG_OK:
						// 登录成功
						if (neterror != null) {
							neterror.setClickable(true);
							neterror.setVisibility(View.GONE);
						}
						break;
					case MessageTypes.MSG_REG_INPROGRESS:
						// 正在登录
						if (neterror != null) {
							neterror.setClickable(false);
							neterror.setVisibility(View.VISIBLE);
							mErrorInfo.setText(getString(R.string.logining));
							neterror.setBackgroundColor(getResources()
									.getColor(R.color.color_lightgreen));
						}
						break;
					case MessageTypes.MSG_REG_NETWORK_ERROR:
						// 网络已断开
						if (neterror != null) {
							neterror.setClickable(true);
							neterror.setVisibility(View.VISIBLE);
							mErrorInfo.setText(getString(R.string.net_broken));
							neterror.setBackgroundColor(getResources()
									.getColor(R.color.color_neterror));
						}
						break;
					case MessageTypes.MSG_REG_NETWORK_CHECK:
						// 正在检测网络
						neterror.setClickable(false);
						if (neterror != null) {
							neterror.setVisibility(View.VISIBLE);
							mErrorInfo
									.setText(getString(R.string.checking_net));
							neterror.setBackgroundColor(getResources()
									.getColor(R.color.color_lightgreen));
						}
						break;
					default:
						break;
					}
				}
				// change account
				else if (ACTION_CHANGACCOUNT_EVENT.equals(action)) {
					mScreenService.show(ScreenLoginAccount.class);
					if (GlobalVar.bADHocMode) {
						ServiceAdhoc.getInstance().StopAdhoc();
						ServiceLoginAccount.getInstance().adhoc_Logout();
					}
				}
			}
		};
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(MessageTypes.MSG_REG_EVENT);
		intentFilter.addAction(ACTION_CHANGACCOUNT_EVENT);

		registerReceiver(mSipBroadCastRecv, intentFilter);

		Intent intent = this.getIntent();
		int index = intent.getIntExtra("index", 1);
		this.updateData(index);

	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.e("ScreenTabHome", "ScreenTabHome ONSTART");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.e("ScreenTabHome", "ScreenTabHome ONReSTART");
	}

	@Override
	protected void onDestroy() {
		if (GlobalVar.bADHocMode)
			ServiceAdhoc.getInstance().StopAdhoc();
		super.onDestroy();
		Log.e("ScreenTabHome", "ScreenTabHome OnDestroy");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e("ScreenTabHome", "ScreenTabHome OnPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "ScreenTabHome OnResume");
		if (GlobalSession.bSocketService) {
			finish();
		}

		// if (!SKDroidUpdate.isStartUpdateChecked) {
		// SKDroidUpdate.getSkDroidUpdate().doUpdate2();
		// SKDroidUpdate.isStartUpdateChecked = true;
		// }

		if (GlobalVar.bADHocMode) {
			if (!ServiceAdhoc.getInstance().isStartOK())
				ServiceAdhoc.getInstance().StartAdhoc();
			SystemVarTools.updateContactRecent();
		} else {
			SystemVarTools.updateContactRecent();

		}

		ServiceContact.sendContactFrashMsg();
		if (mHost.getCurrentTab() == 0) {
			// 进入通话历史记录界面后取消通知栏未接来电提示
			((Engine) Engine.getInstance()).cancelAVCallNotNotif();
		}
		mHost.setCurrentTab(mHost.getCurrentTab());
		//
		// Intent intent = this.getIntent();
		// int index = intent.getIntExtra("index",0);
		// this.updateData(index);

	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.e("ScreenTabHome", "ScreenTabHome OnStop");
	}

	private OnClickListener neterror_clickListener = new OnClickListener() {

		@Override
		public void onClick(View arg0) {
			Log.d(TAG, "点击刷新注册按钮");
			ServiceRegiste.sendRegStatus(MessageTypes.MSG_REG_NETWORK_CHECK);
			Intent tIntent = new Intent(MessageTypes.MSG_NET_EVENT);
			SKDroid.getContext().sendBroadcast(tIntent);
		}
	};

	public boolean updateData(int index) {
		MyLog.d(TAG, "updateData(" + index + ")");
		switch (index) {
		case 0:
			// this.mHost.setCurrentTabByTag("TS_CALL");
			RadioButton checkbtn0 = (RadioButton) findViewById(R.id.radio_button0);
			// checkbtn0.setTextColor(getResources().getColor(R.color.color_lightgreen));
			checkbtn0.setChecked(true);
			break;
		case 1:
			// this.mHost.setCurrentTabByTag("TS_CONTACTS");
			RadioButton checkbtn1 = (RadioButton) findViewById(R.id.radio_button1);
			checkbtn1.setChecked(true);
			break;
		case 2:
			// this.mHost.setCurrentTabByTag("TS_MESSAGE");
			RadioButton checkbtn2 = (RadioButton) findViewById(R.id.radio_button2);
			checkbtn2.setChecked(true);
			break;
		case 3:
			// this.mHost.setCurrentTabByTag("TS_MORE");
			RadioButton checkbtn3 = (RadioButton) findViewById(R.id.radio_button3);
			checkbtn3.setChecked(true);
		default:
			// this.mHost.setCurrentTabByTag("TS_CALL");
			break;
		}
		return true;
	}

	@Override
	public boolean hasBack() {
		return false;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		// SystemVarTools.put("HOME_CURTAB",checkedId);

		MyLog.d(TAG, "onCheckedChanged(" + checkedId + ")");

		RadioButton rbButton0 = (RadioButton) this
				.findViewById(R.id.radio_button0);
		RadioButton rbButton1 = (RadioButton) this
				.findViewById(R.id.radio_button1);
		RadioButton rbButton2 = (RadioButton) this
				.findViewById(R.id.radio_button2);
		RadioButton rbButton3 = (RadioButton) this
				.findViewById(R.id.radio_button3);
		RadioButton rbButton4 = (RadioButton) this
				.findViewById(R.id.radio_button4);

		if (checkedId == R.id.radio_button0) {
			this.mHost.setCurrentTabByTag("TS_CALL");

			// 进入通话历史记录界面后取消通知栏未接来电提示
			((Engine) Engine.getInstance()).cancelAVCallNotNotif();

			// 选中之后更换背景
			rbButton0.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_call_2, 0, 0);
			rbButton1.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_contact, 0, 0);
			rbButton2.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_message, 0, 0);
			rbButton3.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_me, 0, 0);
			// rbButton4.setCompoundDrawablesWithIntrinsicBounds(0,
			// R.drawable.n_maintab_gis_2, 0, 0);

			rbButton0.setTextColor(getResources().getColor(
					R.color.color_text_blue));
			rbButton1.setTextColor(getResources().getColor(R.color.color_text));
			rbButton2.setTextColor(getResources().getColor(R.color.color_text));
			rbButton3.setTextColor(getResources().getColor(R.color.color_text));
			rbButton4.setTextColor(getResources().getColor(R.color.color_text));
		} else if (checkedId == R.id.radio_button1) {
			this.mHost.setCurrentTabByTag("TS_CONTACTS");

			rbButton0.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_call, 0, 0);
			rbButton1.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_contact_2, 0, 0);
			rbButton2.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_message, 0, 0);
			rbButton3.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_me, 0, 0);

			rbButton0.setTextColor(getResources().getColor(R.color.color_text));
			rbButton1.setTextColor(getResources().getColor(
					R.color.color_text_blue));
			rbButton2.setTextColor(getResources().getColor(R.color.color_text));
			rbButton3.setTextColor(getResources().getColor(R.color.color_text));
			rbButton4.setTextColor(getResources().getColor(R.color.color_text));
		} else if (checkedId == R.id.radio_button2) {
			this.mHost.setCurrentTabByTag("TS_MESSAGE");

			rbButton0.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_call, 0, 0);
			rbButton1.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_contact, 0, 0);
			rbButton2.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_message_2, 0, 0);
			rbButton3.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_me, 0, 0);

			rbButton0.setTextColor(getResources().getColor(R.color.color_text));
			rbButton1.setTextColor(getResources().getColor(R.color.color_text));
			rbButton2.setTextColor(getResources().getColor(
					R.color.color_text_blue));
			rbButton3.setTextColor(getResources().getColor(R.color.color_text));
			rbButton4.setTextColor(getResources().getColor(R.color.color_text));
		} else if (checkedId == R.id.radio_button3) {
			this.mHost.setCurrentTabByTag("TS_MORE");

			rbButton0.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_call, 0, 0);
			rbButton1.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_contact, 0, 0);
			rbButton2.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_message, 0, 0);
			rbButton3.setCompoundDrawablesWithIntrinsicBounds(0,
					R.drawable.n_maintab_me_2, 0, 0);

			rbButton0.setTextColor(getResources().getColor(R.color.color_text));
			rbButton1.setTextColor(getResources().getColor(R.color.color_text));
			rbButton2.setTextColor(getResources().getColor(R.color.color_text));
			rbButton3.setTextColor(getResources().getColor(
					R.color.color_text_blue));
			rbButton4.setTextColor(getResources().getColor(R.color.color_text));
		}

	}

	@Override
	public boolean hasMenu() {
		int index = this.mHost.getCurrentTab();
		if (this.mHost.getTabContentView().getChildAt(index) == null) {
			return false;
		}
		switch (index) {
		case 0:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabCall) {
				ScreenTabCall call = (ScreenTabCall) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return call.hasMenu();
			}
		case 1:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabContact) {
				ScreenTabContact contact = (ScreenTabContact) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return contact.hasMenu();
			}
		case 2:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabMessage) {
				ScreenTabMessage message = (ScreenTabMessage) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return message.hasMenu();
			}
		case 3:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabMore) {
				ScreenTabMore more = (ScreenTabMore) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return more.hasMenu();
			}
		default:
			return false;
		}
	}

	@Override
	public boolean createOptionsMenu(Menu menu) {
		int index = this.mHost.getCurrentTab();
		if (this.mHost.getTabContentView().getChildAt(index) == null) {
			return false;
		}
		switch (index) {
		case 0:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabCall) {
				ScreenTabCall call = (ScreenTabCall) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return call.createOptionsMenu(menu);
			}
		case 1:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabContact) {
				ScreenTabContact contact = (ScreenTabContact) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return contact.createOptionsMenu(menu);
			}
		case 2:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabMessage) {
				ScreenTabMessage message = (ScreenTabMessage) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return message.createOptionsMenu(menu);
			}
		case 3:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabMore) {
				ScreenTabMore more = (ScreenTabMore) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return more.createOptionsMenu(menu);
			}
		default:
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int index = this.mHost.getCurrentTab();
		if (this.mHost.getTabContentView().getChildAt(index) == null) {
			return false;
		}
		switch (index) {
		case 0:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabCall) {
				ScreenTabCall call = (ScreenTabCall) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return call.onOptionsItemSelected(item);
			}
		case 1:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabContact) {
				ScreenTabContact contact = (ScreenTabContact) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return contact.onOptionsItemSelected(item);
			}
		case 2:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabMessage) {
				ScreenTabMessage message = (ScreenTabMessage) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return message.onOptionsItemSelected(item);
			}
		case 3:
			if (this.mHost.getTabContentView().getChildAt(index).getContext() instanceof ScreenTabMore) {
				ScreenTabMore more = (ScreenTabMore) this.mHost
						.getTabContentView().getChildAt(index).getContext();
				return more.onOptionsItemSelected(item);
			}
		default:
			return false;
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("hasDownloadContact", false);
		super.onSaveInstanceState(outState);
	}

	// 处理我界面设置头像返回数据
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.e("", "我执行了");

		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case ScreenTabMore.SELECT_CAMERA:
			doCrop();
			break;
		case ScreenTabMore.SELECT_PICTURE:
			mImageCaptureUri = data.getData();
			doCrop();
			break;
		case ScreenTabMore.CROP_FROM_CAMERA:
			if (null != data) {
				saveCutPic(data);
			}
			break;

		default:
			break;
		}

	}

	private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
				intent, 0);

		int size = list.size();
		// Toast.makeText(this, "Can not find image crop app" + size,
		// Toast.LENGTH_LONG).show();

		if (size == 0) {
			// Toast.makeText(this, "Can not find image crop app",
			// Toast.LENGTH_SHORT).show();

			return;
		} else {
			intent.setData(mImageCaptureUri);

			intent.putExtra("outputX", 200);
			intent.putExtra("outputY", 200);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);

			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName,
						res.activityInfo.name));

				startActivityForResult(i, ScreenTabMore.CROP_FROM_CAMERA);
			} else {
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(
							res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(
							res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent
							.setComponent(new ComponentName(
									res.activityInfo.packageName,
									res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(
						getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose Crop App");
				builder.setAdapter(adapter,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								startActivityForResult(
										cropOptions.get(item).appIntent,
										ScreenTabMore.CROP_FROM_CAMERA);
							}
						});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

						if (mImageCaptureUri != null) {
							getContentResolver().delete(mImageCaptureUri, null,
									null);
							mImageCaptureUri = null;
						}
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}
	}

	private void saveCutPic(Intent picdata) {
		Bundle bundle = picdata.getExtras();

		Bitmap mBitmap = null;
		if (null != bundle) {
			mBitmap = bundle.getParcelable("data");

		} else {
			return;
		}
		File f = new File(mImageCaptureUri.getPath());

		if (f.exists()) {
			f.delete();
		}

		FileOutputStream newfos = null;

		try {

			File newpictureFlie = getOutputMediaFile();

			newfos = new FileOutputStream(newpictureFlie);

			mBitmap.compress(CompressFormat.JPEG, 100, newfos); // 用60不用100，降低存储照片的大小

			MyiconHttpUpLoadClient myiconUpload = new MyiconHttpUpLoadClient();

			myiconUpload
					.setmFileUploadProgressHandler(ScreenTabMore.progressHandler);
			myiconUpload.httpSendFileInThread(newpictureFlie.getPath());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (newfos != null) {
				try {
					newfos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private File getOutputMediaFile() {

		String saveDir = SystemVarTools.downloadIconPath;
		File dir = new File(saveDir);
		if (!dir.exists()) {
			dir.mkdir();
		}
		String fileName = "myicon_temp.jpg";
		File takephoto_tempfile = new File(saveDir, fileName);
		takephoto_tempfile.delete();
		if (!takephoto_tempfile.exists()) {
			try {
				takephoto_tempfile.createNewFile();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return takephoto_tempfile;

	}

}
