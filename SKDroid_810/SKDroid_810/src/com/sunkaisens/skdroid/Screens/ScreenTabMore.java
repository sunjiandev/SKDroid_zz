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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.utils.MyLog;
import org.json.JSONException;
import org.json.JSONObject;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Services.ServiceAdhoc;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Services.ServiceGPSReport;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.Utils.CropOption;
import com.sunkaisens.skdroid.Utils.CropOptionAdapter;
import com.sunkaisens.skdroid.Utils.MyiconFileHttpDownLoadClient;
import com.sunkaisens.skdroid.Utils.MyiconHttpUpLoadClient;
import com.sunkaisens.skdroid.Utils.RoundProgressBar;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.adapter.ImageLoader;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.model.VERSION;
import com.sunkaisens.skdroid.update.ChkVer;
import com.sunkaisens.skdroid.update.SKDroidUpdate;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenTabMore extends BaseScreen {
	private static String TAG = ScreenTabMore.class.getCanonicalName();

	private Boolean flag = true;

	private final INgnConfigurationService mConfigurationService = getEngine()
			.getConfigurationService();

	private boolean updatingFlag = false;

	private List<ModelContact> contactListAll = new ArrayList<ModelContact>();

	private ImageButton btnserch;

	public static final int SELECT_PICTURE = 8;
	public static final int SELECT_CAMERA = 9;
	public static final int CROP_FROM_CAMERA = 10;

	private ImageView icon = null;
	private ModelContact myself = null;
	private String iconFileName = null; // 通讯录中的带的头像名称
	private String fileSavePath = null; // 通讯录中带的头像名称+保存路径
	private String selectedPicturePath = null; // 头像上传选中图片的路径

	private LinearLayout userInfoLayout = null;

	private static RoundProgressBar rounProgress = null;

	public static Handler progressHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case Main.FILEUPLOADPROGRESS:

					if (rounProgress != null) {

						int progress = msg.getData().getInt(
								"fileTransferProgress");

						if ((progress % 20) == 0) {
							Log.d(TAG, "upload progress=" + progress);
						}

						if (progress < 100) {
							rounProgress.setVisibility(View.VISIBLE);
							rounProgress.setProgress(progress);

						} else {
							rounProgress.setVisibility(View.GONE);
						}

					}
					break;

				case Main.FILEUPLOAD_SUCCESS:

					File tempPicture = new File(SystemVarTools.downloadIconPath
							+ "myicon_temp.jpg");

					// File myicon = new File(SystemVarTools.downloadIconPath
					// + "myicon.jpg");
					//
					// if (myicon.exists()) {
					// myicon.delete();
					// }
					//
					// File myBigIcon = new File(SystemVarTools.downloadIconPath
					// + "myicon_big.jpg");
					//
					// if (myBigIcon.exists()) {
					// myBigIcon.delete();
					// }

					// tempPicture.renameTo(myicon);

					if (tempPicture.exists()) {
						tempPicture.delete();
					}

					SystemVarTools.mImageLoader.clearCache();
					if (rounProgress != null) {
						rounProgress.setVisibility(View.GONE);
					}

					SystemVarTools.showToast(NgnApplication.getContext()
							.getString(R.string.icon_update_success));

					break;

				case Main.FILEUPLOAD_FAILED:

					File temppicture = new File(SystemVarTools.downloadIconPath
							+ "myicon_temp.jpg");
					if (temppicture.exists()) {
						temppicture.delete();
					}

					if (rounProgress != null) {
						rounProgress.setVisibility(View.GONE);
					}
					SystemVarTools.showToast(NgnApplication.getContext()
							.getString(R.string.icon_update_failed));
					break;

				default:
					break;

				}
			} catch (Exception e) {
				MyLog.d(TAG, "progressHandler  handleMessage " + e.getMessage());

			}
		}
	};

	static enum PhoneInputType {
		Numbers, Text
	}

	public ScreenTabMore() {
		super(SCREEN_TYPE.DIALER_T, TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_more);

		TextView tv_persondetail = (TextView) findViewById(R.id.screen_more_textView_persondetail);
		tv_persondetail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenIdentity.class);
			}
		});

		// 显示用户名和账号 jgc
		TextView tv_username = (TextView) findViewById(R.id.screen_tab_more_displayname);
		TextView tv_useraccount = (TextView) findViewById(R.id.screen_tab_more_account);
		String accountString = mConfigurationService.getString(
				NgnConfigurationEntry.IDENTITY_IMPI,
				NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

		String usernameString = "";

		rounProgress = (RoundProgressBar) findViewById(R.id.screen_tab_more_icon_progress);

		contactListAll.clear();
		contactListAll.addAll(SystemVarTools.getContactAll());

		for (int i = 0; i < contactListAll.size(); i++) {

			if (contactListAll.get(i).mobileNo.equals(accountString.trim())) {
				usernameString = contactListAll.get(i).name;
				break;
			}

		}

		if (usernameString.equals("")) {
			usernameString = accountString;
		}

		myself = SystemVarTools.createContactFromNumberorName(accountString);

		tv_useraccount.setText(NgnApplication.getContext().getString(
				R.string.account_with_colon)
				+ accountString);
		tv_username.setText(usernameString);

		icon = (ImageView) findViewById(R.id.screen_tab_more_icon);
		icon.setImageResource(SystemVarTools.getThumbID(SystemVarTools
				.getImageIDFromNumber(mConfigurationService.getString(
						NgnConfigurationEntry.IDENTITY_IMPI,
						NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI))));

		icon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ScreenShowIcon.showContact = myself;
				Intent intent = new Intent(getApplicationContext(),
						ScreenShowIcon.class);
				startActivity(intent);
			}
		});

		userInfoLayout = (LinearLayout) findViewById(R.id.screen_tab_more_userinfo_layout);
		userInfoLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (SKDroid.sks_version == VERSION.ONLINE) {
					getimage(ScreenTabMore.this.getParent());
				}
			}
		});

		ImageButton ib_persondetail = (ImageButton) findViewById(R.id.screen_more_imageButton_persondetail);
		ib_persondetail.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenIdentity.class);
			}
		});

		TextView tv_about = (TextView) findViewById(R.id.screen_more_textView_about);
		tv_about.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenAbout.class);
			}
		});

		ImageButton ib_about = (ImageButton) findViewById(R.id.screen_more_imageButton_about);
		ib_about.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenAbout.class);
			}
		});

		TextView tv_setting = (TextView) findViewById(R.id.screen_more_textView_setting);
		tv_setting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mScreenService.show(ScreenSimpleSettings.class);
				mScreenService.show(ScreenSettings.class);
			}
		});

		ImageButton ib_setting = (ImageButton) findViewById(R.id.screen_more_imageButton_setting);
		ib_setting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mScreenService.show(ScreenSimpleSettings.class);
				mScreenService.show(ScreenSettings.class);
			}
		});

		TextView tv_map = (TextView) findViewById(R.id.screen_more_textView_map);
		tv_map.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenMap.class);
			}
		});

		ImageButton ib_map = (ImageButton) findViewById(R.id.screen_more_imageButton_map);
		ib_map.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.show(ScreenMap.class);
			}
		});

		if (!SystemVarTools.useGisMap) {
			tv_map.setVisibility(View.GONE);
			ib_map.setVisibility(View.GONE);
			findViewById(R.id.screen_tab_more_line4).setVisibility(View.GONE);
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

		ImageButton ib_subscribe_update = (ImageButton) findViewById(R.id.screen_more_imageButton_update);
		ib_subscribe_update.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// mScreenService.show(ScreenPushInfos.class);
				// 检测是否有需要进行软件更新版本
				SKDroidUpdate.getSkDroidUpdate().doUpdate();
			}
		});

		TextView tv_exit = (TextView) findViewById(R.id.screen_more_textView_exit);
		tv_exit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				AlertDialog.Builder builder = new Builder(
						(Engine.getInstance()).getMainActivity()); // com.sunkaisens.skdroid.Main
				builder.setMessage(NgnApplication.getContext().getString(
						R.string.make_sure_to_quit));
				builder.setTitle(NgnApplication.getContext().getString(
						R.string.tips));
				builder.setPositiveButton(NgnApplication.getContext()
						.getString(R.string.ok),
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
									ScreenDownloadConcacts.setUnSubscribeOK();
									((Engine) Engine.getInstance())
											.getSipService().unRegister();
									GlobalVar.mLogout = true;// gzc 标记注销操作
									if (ServiceGPSReport.getInstance()
											.getGPSServiceReprotStart())
										ServiceGPSReport.getInstance()
												.StopGPSReport();
								}
								new NgnTimer().schedule(new TimerTask() {

									@Override
									public void run() {
										MyLog.d(TAG, "SKDroid 程序退出.");
										Main main = ((Main) (((Engine) Engine
												.getInstance())
												.getMainActivity()));
										if (main != null) {
											main.exit();
										} else {
											MyLog.e(TAG,
													"main instance is null.");
										}
									}
								}, 500);

							}
						});
				builder.setNegativeButton(NgnApplication.getContext()
						.getString(R.string.cancel),
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

		btnserch = (ImageButton) findViewById(R.id.screen_tab_more_search_bt);
		// searchedit = (EditText)
		// findViewById(R.id.screen_tab_contact_searchedit);

		btnserch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// if(searchedit.getVisibility()==View.GONE){
				// searchedit.setVisibility(View.VISIBLE);
				// }else {
				// searchedit.setVisibility(View.GONE);
				// }
				Log.e("我被点击了", "我被点击了");
				mScreenService.show(ScreenSearch.class);
			}
		});

		BroadcastReceiver bcreceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				// Registration Event 服务器（实为本地代理CSCF-Proxy）对客户端注册动作的响应反馈：
				if (ServiceContact.CONTACT_REFRASH_MSG.equals(action)) {
					ScreenTabMore.this.refresh();
				}
			}
		};

		registerReceiver(bcreceiver, new IntentFilter(
				ServiceContact.CONTACT_REFRASH_MSG));

		SystemVarTools.showicon(icon, myself, getApplicationContext());

	}

	public String getIconFileName(String url) {
		if (url != null) {
			String[] temp = url.split("/");
			if (temp[temp.length - 1] != null) {
				return temp[temp.length - 1];

			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public boolean fileExists(String path) {
		try {
			File f = new File(path);

			if (!f.exists()) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			return false;
		}

	}

	@Override
	public boolean refresh() {
		// return super.refresh();
		TextView tv_username = (TextView) findViewById(R.id.screen_tab_more_displayname);
		// tv_username.setText(mConfigurationService.getString(
		// NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
		// NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME));

		TextView tv_useraccount = (TextView) findViewById(R.id.screen_tab_more_account);
		String accountString = mConfigurationService.getString(
				NgnConfigurationEntry.IDENTITY_IMPI,
				NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

		String usernameString = "";

		contactListAll.clear();
		contactListAll.addAll(SystemVarTools.getContactAll());

		for (int i = 0; i < contactListAll.size(); i++) {

			// Log.e(""+i, contactListAll.get(i).name);
			if (contactListAll.get(i).mobileNo.equals(accountString.trim())) {
				usernameString = contactListAll.get(i).name;
				break;
			}

		}

		if (usernameString.equals("")) {
			usernameString = accountString;

		}

		myself = SystemVarTools.createContactFromNumberorName(accountString);

		tv_useraccount.setText(accountString);
		tv_username.setText(usernameString);

		SystemVarTools.showicon(icon, myself, getApplicationContext());

		return true;

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean hasBack() {
		return true;
	}

	@Override
	public boolean back() {
		return super.back();
	}

	public void getimage(Context mContext) {
		// 在父Activity中处理返回结果
		new AlertDialog.Builder(mContext)
				.setTitle(R.string.dialog_title)
				.setPositiveButton(R.string.dialog_p_btn,
						new android.content.DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent();

								intent.setType("image/*");
								intent.setAction(Intent.ACTION_GET_CONTENT);
								getParent().startActivityForResult(
										Intent.createChooser(intent,
												"Complete action using"),
										SELECT_PICTURE);

							}
						})
				.setNegativeButton(R.string.dialog_n_btn,
						new android.content.DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								Intent intent = new Intent(
										MediaStore.ACTION_IMAGE_CAPTURE);

								File temp_myicon = new File(
										SystemVarTools.downloadIconPath
												+ "temp_myicon.jpg");

								ScreenTabHome.mImageCaptureUri = Uri
										.fromFile(temp_myicon);

								intent.putExtra(
										android.provider.MediaStore.EXTRA_OUTPUT,
										ScreenTabHome.mImageCaptureUri);

								try {
									intent.putExtra("return-data", true);

									getParent().startActivityForResult(intent,
											SELECT_CAMERA);
								} catch (ActivityNotFoundException e) {
									e.printStackTrace();
								}

							}
						}).create().show();

	}

	@Override
	protected void onResume() {
		super.onResume();

	}

}
