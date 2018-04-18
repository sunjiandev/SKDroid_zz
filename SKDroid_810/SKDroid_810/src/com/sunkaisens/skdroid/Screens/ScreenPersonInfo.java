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

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.utils.MyLog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ScreenPersonInfo extends BaseScreen {
	public static String TAG = ScreenPersonInfo.class.getCanonicalName();

	private ModelContact info = null;
	private Button uamonitor = null;

	static enum PhoneInputType {
		Numbers, Text
	}

	//
	// private final INgnSipService mSipService;

	public ScreenPersonInfo() {
		super(SCREEN_TYPE.SKS_Screen_PersionInfo, TAG);
		Engine.getInstance().getNetworkService();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_personinfo);

		MyLog.d(TAG, "ScreenPersonInfo onCreate");

		final ScreenPersonInfo sp = this;

		super.mId = getIntent().getStringExtra("id");
		updateInfo(super.mId);
		// list数据填充
		// ListView list = (ListView) findViewById(R.id.messagerlist);

		//
		TextView title = (TextView) findViewById(R.id.screen_person_info_title);

		title.setText(getResources().getString(R.string.string_subscribeinfo));

		ImageView back = (ImageView) findViewById(R.id.screen_person_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

		Button audiocall = (Button) findViewById(R.id.audiocall);
		audiocall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (info != null) {
					if (CrashHandler.isNetworkAvailable2()) {
						ServiceAV.makeCall(info.mobileNo, NgnMediaType.Audio,
								SessionType.AudioCall);
					} else {
						SystemVarTools.showNotifyDialog(ScreenPersonInfo.this
								.getString(R.string.tip_net_no_conn_error), sp);
					}
				}
			}
		});

		Button sms = (Button) findViewById(R.id.sms);
		sms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (info != null) {
					if (GlobalVar.bADHocMode) {
						if (SystemVarTools.getIPFromUri(info.uri) == null)
							return;
						MyLog.d(TAG,
								"sms set cscf host:"
										+ SystemVarTools.getIPFromUri(info.uri));
						// set pcscf;
						((Engine) Engine.getInstance()).getSipService()
								.ADHOC_SetPcscfHost(
										SystemVarTools.getIPFromUri(info.uri));
					}
					ScreenChat.startChat(info.mobileNo, true);
				}
			}
		});
		Button videocall = (Button) findViewById(R.id.videocall);
		videocall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (info != null) {
					/*
					 * Engine.getInstance().getConfigurationService()
					 * .putString(NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
					 * tmedia_pref_video_size_t
					 * .tmedia_pref_video_size_720p.toString());
					 * Engine.getInstance().getConfigurationService().commit();
					 */
					if (CrashHandler.isNetworkAvailable2()) {

						ScreenAV.ispeoplePTT = true;

						ServiceAV.makeCall(info.mobileNo,
								NgnMediaType.AudioVideo, SessionType.VideoCall);
					} else {
						SystemVarTools.showNotifyDialog(ScreenPersonInfo.this
								.getString(R.string.tip_net_no_conn_error), sp);
						// Intent refresh = new
						// Intent(NgnRegistrationEventArgs.ACTION_REFRESHREGISTRATION_EVENT);
						// refresh.putExtra(NgnRegistrationEventArgs.REFRESHREGISTRATION,
						// "NOK");
						// NgnApplication.getContext().sendBroadcast(refresh);
					}
				}
			}
		});
		Button audiomonitor = (Button) findViewById(R.id.audiomonitor);
		audiomonitor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (CrashHandler.isNetworkAvailable2()) {
					ServiceAV.makeCall(info.mobileNo, NgnMediaType.Audio,
							SessionType.AudioEvn);
				} else {
					SystemVarTools.showNotifyDialog(ScreenPersonInfo.this
							.getString(R.string.tip_net_no_conn_error), sp);
				}
				// Toast.makeText(getApplicationContext(),
				// "暂不支持音频监控!",Toast.LENGTH_SHORT).show();
			}
		});
		// Button gpsmonitor = (Button) findViewById(R.id.gpsmonitor);
		// gpsmonitor.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Toast.makeText(getApplicationContext(),
		// "暂不支持定位监控!",Toast.LENGTH_SHORT).show();
		// }
		// });
		Button videomonitor = (Button) findViewById(R.id.videomonitor);
		videomonitor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (CrashHandler.isNetworkAvailable2()) {
					ServiceAV.makeCall(info.mobileNo, NgnMediaType.Video,
							SessionType.VideoMonitor);
				} else {
					SystemVarTools.showNotifyDialog(ScreenPersonInfo.this
							.getString(R.string.tip_net_no_conn_error), sp);
					// Intent refresh = new
					// Intent(NgnRegistrationEventArgs.ACTION_REFRESHREGISTRATION_EVENT);
					// refresh.putExtra(NgnRegistrationEventArgs.REFRESHREGISTRATION,
					// "NOK");
					// NgnApplication.getContext().sendBroadcast(refresh);
				}
				// Toast.makeText(getApplicationContext(),
				// "暂不支持视频监控!",Toast.LENGTH_SHORT).show();
			}
		});

		Button uamonitor = (Button) findViewById(R.id.uamonitor);
		if (info.userType != null)
			uamonitor.setVisibility(info.userType.equals("1") ? View.VISIBLE
					: View.GONE);
		// uamonitor.setVisibility(View.GONE);
		uamonitor.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (CrashHandler.isNetworkAvailable2()) {
					ServiceAV.makeCall(GlobalVar.videoMonitorPrefix
							+ info.mobileNo, NgnMediaType.Video,
							SessionType.VideoUaMonitor);// add by gle
				} else {
					SystemVarTools.showNotifyDialog(ScreenPersonInfo.this
							.getString(R.string.tip_net_no_conn_error), sp);
				}
			}
		});

		if (info.businessType != null && info.businessType.equals("subscribe")) {
			title.setText(getResources().getString(
					R.string.string_subscribeinfo));
			RelativeLayout btnLayout = (RelativeLayout) findViewById(R.id.screen_person_button_layout);
			btnLayout.setVisibility(View.GONE);

		} else {
			title.setText(getResources().getString(R.string.string_personinfo));
			RelativeLayout btnLayout = (RelativeLayout) findViewById(R.id.screen_person_button_layout);
			btnLayout.setVisibility(View.VISIBLE);
		}

		if (GlobalVar.bADHocMode == true) {
			Button audioGroupcall = (Button) findViewById(R.id.audioGroup);
			audioGroupcall.setVisibility(View.VISIBLE);
			audioGroupcall.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (info != null) {
						if (CrashHandler.isNetworkAvailable2()) {// SystemVarTools.getIPFromUri(info.uri),
							ServiceAV.makeCall(info.mobileNo,
									NgnMediaType.Audio,
									SessionType.GroupAudioCall, false);
						} else {
							SystemVarTools.showNotifyDialog(
									ScreenPersonInfo.this
											.getString(R.string.tip_net_no_conn_error),
									sp);
						}
					}
				}
			});

			/*
			 * Button videoGroupcall = (Button) findViewById(R.id.videoGroup);
			 * videoGroupcall.setVisibility(View.VISIBLE);
			 * videoGroupcall.setOnClickListener(new View.OnClickListener() {
			 * 
			 * @Override public void onClick(View v) { if(info != null) {
			 * if(CrashHandler
			 * .isNetworkAvailable2()){//SystemVarTools.getIPFromUri(info.uri),
			 * ServiceAV.makeCall(info.mobileNo,
			 * NgnMediaType.AudioVideo,SessionType.GroupVideoCall, false); }
			 * else { SystemVarTools.showNotifyDialog("网络已断开，请检查网络.", sp); } } }
			 * });
			 */
		}

	}

	@Override
	protected void onDestroy() {

		MyLog.d(TAG, "ScreenPersonInfo onDestroy");

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

	//
	public void updateInfo(String remoteParty) {
		info = SystemVarTools.createContactFromRemoteParty(remoteParty);
		TextView name = (TextView) findViewById(R.id.name);
		TextView number = (TextView) findViewById(R.id.number);
		TextView brief = (TextView) findViewById(R.id.brief);
		TextView org = (TextView) findViewById(R.id.org);
		ImageView image = (ImageView) findViewById(R.id.icon);
		name.setText(SystemVarTools.toDBC(info.name));
		number.setText(info.mobileNo);
		brief.setText(info.brief);
		ModelContact parentinfo = SystemVarTools
				.getContactFromIndex(info.parent);
		if (parentinfo != null) {
			org.setText(parentinfo.name + "(" + parentinfo.mobileNo + ")");
		} else {
			org.setText(getResources().getString(R.string.func_desc));
		}
		// image.setImageResource(SystemVarTools.getThumbID(info.imageid));

		SystemVarTools.showicon(image, info, getApplicationContext());

		image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				ScreenShowIcon.showContact = info;
				Intent intent = new Intent(getApplicationContext(),
						ScreenShowIcon.class);
				startActivity(intent);
			}
		});

	}

	@Override
	protected void onNewIntent(Intent intent) {
		MyLog.d(TAG, "ScreenPersonInfo onNewIntent");

		if (!intent.getStringExtra("id").equals(TAG)) {
			super.mId = intent.getStringExtra("id");
		}
		updateInfo(super.mId);
		if (uamonitor == null) {
			uamonitor = (Button) findViewById(R.id.uamonitor);
			uamonitor.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					ServiceAV.makeCall(GlobalVar.videoMonitorPrefix
							+ info.mobileNo, NgnMediaType.Video,
							SessionType.VideoUaMonitor);
				}
			});
		}
		if (info.userType != null) {
			uamonitor.setVisibility(info.userType.equals("1") ? View.VISIBLE
					: View.GONE);
		} else {
			uamonitor.setVisibility(View.GONE);
		}

		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		MyLog.d(TAG, "ScreenPersonInfo onResume");
		super.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		MyLog.d(TAG, "ScreenPersonInfo onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		MyLog.d(TAG, "ScreenPersonInfo onStart");
		super.onStart();
	}

	@Override
	protected void onStop() {
		MyLog.d(TAG, "ScreenPersonInfo onStop");
		super.onStop();
	}
}
