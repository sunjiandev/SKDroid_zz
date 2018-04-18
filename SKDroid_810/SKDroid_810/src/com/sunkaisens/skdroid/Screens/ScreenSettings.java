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
import java.io.IOException;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.utils.MyLog;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Services.ServiceAdhoc;
import com.sunkaisens.skdroid.Services.ServiceGPSReport;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.app.service.DaemonService;
import com.sunkaisens.skdroid.model.VERSION;
import com.sunkaisens.skdroid.update.SKDroidUpdate;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ScreenSettings extends BaseScreen {
	private final static String TAG = ScreenSettings.class.getCanonicalName();
	private final INgnConfigurationService mConfigurationService;

	private EditText mEtDisplayName;
	private EditText mEtIMPU;
	private EditText mEtIMPI;
	private EditText mEtPassword;
	private EditText mEtRealm;
	private CheckBox mCbEarlyIMS;
	private TextView screen_setting_exit;
	private TextView screen_setting_change_identity;
	private TextView screen_setting_abouTextView;
	private TextView feedback;

	public ScreenSettings() {
		super(SCREEN_TYPE.IDENTITY_T, TAG);

		mConfigurationService = getEngine().getConfigurationService();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_settings);

		ImageView back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

		TextView tv_general_setting = (TextView) findViewById(R.id.screen_settings_textView_general_setting);
		tv_general_setting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenGeneral.class);
			}
		});

		TextView tv_network_48 = (TextView) findViewById(R.id.screen_settings_textView_network_48);
		tv_network_48.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenNetwork.class);
			}
		});

		TextView tv_lock_48 = (TextView) findViewById(R.id.screen_settings_textView_lock_48);
		tv_lock_48.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenSecurity.class);
			}
		});

		// 取消编解码可选
		TextView tv_codecs_48 = (TextView) findViewById(R.id.screen_settings_textView_codecs_48);
		tv_codecs_48.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenCodecs.class);
			}
		});

		TextView tv_qos_qoe_48 = (TextView) findViewById(R.id.screen_settings_textView_qos_qoe_48);
		tv_qos_qoe_48.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenQoS.class);
			}
		});

		TextView tv_natt_48 = (TextView) findViewById(R.id.screen_settings_textView_natt_48);
		tv_natt_48.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenNatt.class);
			}
		});

		// 设置界面新
		screen_setting_abouTextView = (TextView) findViewById(R.id.screen_setting_textView_about);

		screen_setting_abouTextView
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mScreenService.show(ScreenAbout.class);
					}
				});

		feedback = (TextView) findViewById(R.id.screen_setting_feedback_but);
		feedback.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startActivity(new Intent((Main) Engine.getInstance()
						.getMainActivity(), ScreenFeedback.class));
			}
		});

		if (!SystemVarTools.useFeedback) {
			feedback.setVisibility(View.GONE);
		}

		TextView tv_subscribe_update = (TextView) findViewById(R.id.screen_more_textView_update);
		tv_subscribe_update.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mScreenService.show(ScreenPushInfos.class);
				// 检测是否有需要进行软件更新版本
				SKDroidUpdate.getSkDroidUpdate().doUpdate();
			}
		});

		// if(SKDroid.sks_version == VERSION.NORMAL){
		// tv_subscribe_update.setVisibility(View.GONE);
		// }

		screen_setting_change_identity = (TextView) findViewById(R.id.screen_setting_change_identity);
		screen_setting_change_identity
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.e("zhangjie:ScreenMyHome-processBackKeyDown()",
								"切换帐号！");
						// ((Engine)Engine.getInstance()).getSipService().unRegister();
						// ((Main)(((Engine)Engine.getInstance()).getMainActivity())).exit();
						AlertDialog.Builder builder = new Builder(
								ScreenSettings.this);
						builder.setMessage(getText(R.string.change_identy_tip));
						builder.setTitle(getText(R.string.tips));
						builder.setPositiveButton(
								getText(R.string.ok),
								new android.content.DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {

										// 切换账号时，删除前一账号头像
										if (SystemVarTools
												.fileExists(SystemVarTools.downloadIconPath
														+ "myicon.jpg")) {
											File icon = new File(
													SystemVarTools.downloadIconPath
															+ "myicon.jpg");
											icon.delete();
										}

										dialog.dismiss();
										ScreenLoginAccount.btLogin_isClicked = false;
										SystemVarTools.setContactOK(false);

										try {
											Main.mMessageReportHashMap
													.remove("recent_user");
											Tools_data
													.writeData(Main.mMessageReportHashMap);
										} catch (IOException e) {
											e.printStackTrace();
										}

										if (GlobalVar.bADHocMode) {
											ServiceAdhoc.getInstance()
													.StopAdhoc();
											ServiceLoginAccount.getInstance()
													.adhoc_Logout();
										} else {
											Intent stopDaemonService = new Intent(
													getApplicationContext(),
													DaemonService.class);
											stopService(stopDaemonService);
											ScreenDownloadConcacts
													.setUnSubscribeOK();
											((Engine) Engine.getInstance())
													.getSipService()
													.unRegister();
											GlobalVar.mLogout = true;// gzc
																		// 标记注销操作
											if (ServiceGPSReport.getInstance()
													.getGPSServiceReprotStart())
												ServiceGPSReport.getInstance()
														.StopGPSReport();
										}

										// 退出程序
										Intent intent = new Intent(
												getApplicationContext(),
												Main.class);
										PendingIntent restartIntent = PendingIntent
												.getActivity(
														getApplicationContext(),
														0,
														intent,
														Intent.FLAG_ACTIVITY_NEW_TASK);
										AlarmManager mgr = (AlarmManager) SKDroid
												.getContext().getSystemService(
														Context.ALARM_SERVICE);
										mgr.set(AlarmManager.RTC,
												System.currentTimeMillis() + 1000,
												restartIntent); // 1秒钟后重启应用
										((Main) (((Engine) Engine.getInstance())
												.getMainActivity())).exit();

										Main.isFirstPTT_onKeyDown = true;
										Main.isFirstPTT_onKeyLongPress = true;
									}
								});
						builder.setNegativeButton(
								getText(R.string.cancel),
								new android.content.DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
						builder.create().show();
					}
				});

		screen_setting_exit = (TextView) findViewById(R.id.screen_setting_textView_exit);
		screen_setting_exit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Log.d("zhangjie:ScreenMyHome-processBackKeyDown()","退出应用！");
				// ((Engine)Engine.getInstance()).getSipService().unRegister();
				// mScreenService.show(ScreenLoginAccount.class);
				// if (flag) {
				// Toast mBackTipToast = Toast.makeText((Main) ( ((Engine)
				// Engine.getInstance())
				// .getMainActivity()),
				// (CharSequence) ( ((Engine)
				// Engine.getInstance()).getMainActivity())
				// .getResources().getString(R.string.quit_tip),
				// 2000);
				// mBackTipToast.show();
				// flag = false;
				// } else {
				// Log.d("zhangjie:ScreenMyHome-processBackKeyDown()","退出应用！");
				// ((Engine)Engine.getInstance()).getSipService().unRegister();
				// mScreenService.show(ScreenLoginAccount.class);
				// }
				// AlertDialog.Builder builder = new
				// Builder((Context)mScreenService.getCurrentScreen());
				// android.view.WindowManager$BadTokenException: Unable to add
				// window -- token android.os.BinderProxy@4144bf18 is not valid;
				// is your activity running?
				// AlertDialog.Builder builder = new
				// Builder(ScreenTabMore.this); //必然出错
				// AlertDialog.Builder builder = new
				// Builder((Activity)mScreenService.getCurrentScreen());
				// //com.sunkaisens.skdroid.Screens.ScreenTabHome
				AlertDialog.Builder builder = new Builder(
						(Engine.getInstance()).getMainActivity()); // com.sunkaisens.skdroid.Main
				builder.setMessage(ScreenSettings.this
						.getString(R.string.make_sure_to_quit));
				builder.setTitle(ScreenSettings.this.getString(R.string.tips));
				builder.setPositiveButton(
						ScreenSettings.this.getString(R.string.ok),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								try {
									Tools_data
											.writeData(Main.mMessageReportHashMap);
								} catch (IOException e) {
									e.printStackTrace();
								}
								dialog.dismiss();
								ScreenLoginAccount.btLogin_isClicked = false;
								if (GlobalVar.bADHocMode) {
									ServiceAdhoc.getInstance().StopAdhoc();
									ServiceLoginAccount.getInstance()
											.adhoc_Logout();
								} else {
									Intent stopDaemonService = new Intent(
											getApplicationContext(),
											DaemonService.class);
									stopService(stopDaemonService);
									ScreenDownloadConcacts.setUnSubscribeOK();
									((Engine) Engine.getInstance())
											.getSipService().unRegister();
									GlobalVar.mLogout = true;// gzc 标记注销操作
									if (ServiceGPSReport.getInstance()
											.getGPSServiceReprotStart())
										ServiceGPSReport.getInstance()
												.StopGPSReport();
								}

								// 退出程序
								Intent intent = new Intent(
										getApplicationContext(), Main.class);
								PendingIntent restartIntent = PendingIntent
										.getActivity(getApplicationContext(),
												0, intent,
												Intent.FLAG_ACTIVITY_NEW_TASK);
								AlarmManager mgr = (AlarmManager) SKDroid
										.getContext().getSystemService(
												Context.ALARM_SERVICE);
								mgr.set(AlarmManager.RTC,
										System.currentTimeMillis() + 1000,
										restartIntent); // 1秒钟后重启应用
								Main main = ((Main) (((Engine) Engine
										.getInstance()).getMainActivity()));
								if (main != null) {
									main.exit();
								}

								Main.isFirstPTT_onKeyDown = true;
								Main.isFirstPTT_onKeyLongPress = true;
							}
						});
				builder.setNegativeButton(getText(R.string.cancel),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
			}
		});

		screen_setting_exit = (TextView) findViewById(R.id.screen_setting_textView_exit);
		screen_setting_exit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new Builder(
						(Engine.getInstance()).getMainActivity()); // com.sunkaisens.skdroid.Main
				builder.setMessage(ScreenSettings.this
						.getString(R.string.make_sure_to_quit));
				builder.setTitle(ScreenSettings.this.getString(R.string.tips));
				builder.setPositiveButton(
						ScreenSettings.this.getString(R.string.ok),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								try {
									Tools_data
											.writeData(Main.mMessageReportHashMap);
								} catch (IOException e) {
									e.printStackTrace();
								}
								dialog.dismiss();
								ScreenLoginAccount.btLogin_isClicked = false;
								if (GlobalVar.bADHocMode) {
									ServiceAdhoc.getInstance().StopAdhoc();
									ServiceLoginAccount.getInstance()
											.adhoc_Logout();
								} else {
									Intent stopDaemonService = new Intent(
											getApplicationContext(),
											DaemonService.class);
									stopService(stopDaemonService);
									ScreenDownloadConcacts.setUnSubscribeOK();
									((Engine) Engine.getInstance())
											.getSipService().unRegister();
									GlobalVar.mLogout = true;// gzc 标记注销操作
									if (ServiceGPSReport.getInstance()
											.getGPSServiceReprotStart())
										ServiceGPSReport.getInstance()
												.StopGPSReport();
								}
								Main main = ((Main) (((Engine) Engine
										.getInstance()).getMainActivity()));
								if (main != null) {
									main.exit();
								}
							}
						});
				builder.setNegativeButton(
						ScreenSettings.this.getString(R.string.cancel),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (!SystemVarTools.bLogin && SKDroid.sks_version != VERSION.ADHOC) {
			mScreenService.show(Main.class);
			return;
		}

		Intent intent = getIntent();
		if (intent != null) {
			Bundle data = intent.getExtras();
			if (data != null) {
				String action = data.getString("action");
				if (action != null) {
					if (SKDroidUpdate.progress == 100) {
						SKDroidUpdate.getSkDroidUpdate().update();
					} else {
						SystemVarTools.showToast(ScreenSettings.this
								.getString(R.string.is_updateing));
					}
				} else {
					MyLog.d(TAG, "UPDATE_ACTION is null.");
				}
			}
		}
	}

	protected void onPause() {
		// if(super.mComputeConfiguration){
		// mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
		// mEtDisplayName.getText().toString().trim());
		// mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU,
		// mEtIMPU.getText().toString().trim());
		// mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI,
		// mEtIMPI.getText().toString().trim());
		// mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD,
		// mEtPassword.getText().toString().trim());
		// mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM,
		// mEtRealm.getText().toString().trim());
		// mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
		// mCbEarlyIMS.isChecked());
		//
		// // Compute
		// if(!mConfigurationService.commit()){
		// Log.e(TAG, "Failed to Commit() configuration");
		// }
		//
		// super.mComputeConfiguration = false;
		// }
		super.onPause();
	}

}
