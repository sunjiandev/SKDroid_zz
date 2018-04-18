/* Copyright (C) 2010-2011, Mamadou Diop.
 *  Copyright (C) 2011, Doubango Telecom.
 *  Copyright (C) 2011, Philippe Verney <verney(dot)philippe(AT)gmail(dot)com>
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

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnCameraProducer;
import org.doubango.ngn.media.NgnCameraProducer_surface;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.tinyWRAP.tmedia_pref_video_size_t;
import org.doubango.tinyWRAP.tmedia_profile_t;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.Tools_data;

public class ScreenGeneral extends BaseScreen {
	private final static String TAG = ScreenGeneral.class.getCanonicalName();

	private Spinner mSpProfile;
	private Spinner mSpAudioPlaybackLevel;
	// private CheckBox mCbFullScreenVideo;
	private ToggleButton mCbFullScreenVideo;

	private CheckBox mCbFFC;

	// private CheckBox mCbAutoStart;
	private ToggleButton mCbAutoStart;

	private CheckBox mCbInterceptOutgoingCalls;
	private EditText mEtEnumDomain;
	private CheckBox mCbAEC;
	private CheckBox mCbVAD;
	private CheckBox mCbNR;
	private ImageView mback;
	private RelativeLayout mVideofblchoose;
	private TextView videofblchoose_choosed;

	private CheckBox mCbFEC;
	private String QOS_PREF_VIDEO_SIZE;
	
	private EditText mEtNetworkQos;

	private String[] videoSize = new String[] {
			NgnApplication.getContext().getString(R.string.video_fbl_lc),
			NgnApplication.getContext().getString(R.string.video_fbl_bq) };

	// gzc 20141018
	private CheckBox mCheck_autologin;
	public static HashMap<String, Object> mMessageReportHashMap;

	private final INgnConfigurationService mConfigurationService;

	private final static AudioPlayBackLevel[] sAudioPlaybackLevels = new AudioPlayBackLevel[] {
			new AudioPlayBackLevel(0.25f, "Low"),
			new AudioPlayBackLevel(0.50f, "Medium"),
			new AudioPlayBackLevel(0.75f, "High"),
			new AudioPlayBackLevel(1.0f, "Maximum"), };
	private final static Profile[] sProfiles = new Profile[] {
			new Profile(tmedia_profile_t.tmedia_profile_default,
					"Default (User Defined)"),
			new Profile(tmedia_profile_t.tmedia_profile_rtcweb,
					"RTCWeb (Override)") };

	public ScreenGeneral() {
		super(SCREEN_TYPE.GENERAL_T, TAG);

		mConfigurationService = getEngine().getConfigurationService();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_general);

		// mCbFullScreenVideo = (CheckBox)
		// findViewById(R.id.screen_general_checkBox_fullscreen);
		mCbFullScreenVideo = (ToggleButton) findViewById(R.id.screen_general_setting_video_fullscreen);

		mCbInterceptOutgoingCalls = (CheckBox) findViewById(R.id.screen_general_checkBox_interceptCall);
		mCbFFC = (CheckBox) findViewById(R.id.screen_general_checkBox_ffc);

		// mCbAutoStart = (CheckBox)
		// findViewById(R.id.screen_general_checkBox_autoStart);
		mCbAutoStart = (ToggleButton) findViewById(R.id.screen_general_setting_self_run);

		mSpAudioPlaybackLevel = (Spinner) findViewById(R.id.screen_general_spinner_playback_level);
		mSpProfile = (Spinner) findViewById(R.id.screen_general_spinner_media_profile);
		mEtEnumDomain = (EditText) findViewById(R.id.screen_general_editText_enum_domain);
		mCbAEC = (CheckBox) this.findViewById(R.id.screen_general_checkBox_AEC);
		mCbVAD = (CheckBox) this.findViewById(R.id.screen_general_checkBox_VAD);
		mCbNR = (CheckBox) this.findViewById(R.id.screen_general_checkBox_NR);

		mCbFEC = (CheckBox) this.findViewById(R.id.screen_general_checkBox_FEC);
		// ToggleButton
		// toggleButton=(ToggleButton)findViewById(R.id.screen_general_setting_self_run);

		mback = (ImageView) findViewById(R.id.screen_general_back);
		mback.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

		mVideofblchoose = (RelativeLayout) findViewById(R.id.screen_general_setting_videofblchoose_layout);

		mVideofblchoose.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ScreenGeneral.this)
						.setTitle(
								ScreenGeneral.this
										.getString(R.string.set_video))
						.setItems(videoSize,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										switch (which) {
										case 0:
											QOS_PREF_VIDEO_SIZE = tmedia_pref_video_size_t.tmedia_pref_video_size_cif
													.toString();
											break;
										case 1:
											QOS_PREF_VIDEO_SIZE = tmedia_pref_video_size_t.tmedia_pref_video_size_vga
													.toString();
											break;

										default:
											break;
										}
										videofblchoose_choosed
												.setText(videoSize[which]);
									}
								})
						.setNegativeButton(
								ScreenGeneral.this.getString(R.string.cancel),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});
				builder.create().show();
			}
		});

		videofblchoose_choosed = (TextView) findViewById(R.id.screen_general_setting_videofblchoose_choosed);

		String defaultVideoSize = mConfigurationService.getString(
				NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
				tmedia_pref_video_size_t.tmedia_pref_video_size_cif.toString());

		if (defaultVideoSize != null) {
			if (defaultVideoSize
					.equals(tmedia_pref_video_size_t.tmedia_pref_video_size_cif
							.toString())) {
				videofblchoose_choosed.setText(videoSize[0]);
			} else if (defaultVideoSize
					.equals(tmedia_pref_video_size_t.tmedia_pref_video_size_vga
							.toString())) {
				videofblchoose_choosed.setText(videoSize[1]);
			}
		}

		// gzc 20141018
		mCheck_autologin = (CheckBox) findViewById(R.id.screen_general_checkBox_autoLogin);
		mMessageReportHashMap = Tools_data.readData();
		String account = mConfigurationService.getString(
				NgnConfigurationEntry.IDENTITY_IMPI,
				NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);
		Log.d(TAG, "account1:" + account);
		String _login_chk = (String) mMessageReportHashMap.get(account
				+ "_login_chk");
		if (_login_chk != null && _login_chk.equals("true")) {
			mCheck_autologin.setChecked(true);
		} else {
			mCheck_autologin.setChecked(false);
		}
		
		mEtNetworkQos = (EditText)findViewById(R.id.screen_qos_editText_networklosepackets);

		// Audio Playback levels
		ArrayAdapter<AudioPlayBackLevel> adapter = new ArrayAdapter<AudioPlayBackLevel>(
				this, android.R.layout.simple_spinner_item,
				ScreenGeneral.sAudioPlaybackLevels);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpAudioPlaybackLevel.setAdapter(adapter);
		// Media Profile
		ArrayAdapter<Profile> adapterProfile = new ArrayAdapter<Profile>(this,
				android.R.layout.simple_spinner_item, ScreenGeneral.sProfiles);
		adapterProfile
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpProfile.setAdapter(adapterProfile);

		mCbFullScreenVideo.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO,
				NgnConfigurationEntry.DEFAULT_GENERAL_FULL_SCREEN_VIDEO));
		mCbInterceptOutgoingCalls
				.setChecked(mConfigurationService
						.getBoolean(
								NgnConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS,
								NgnConfigurationEntry.DEFAULT_GENERAL_INTERCEPT_OUTGOING_CALLS));
		mCbFFC.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.GENERAL_USE_FFC,
				NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC));
		mCbAutoStart.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.GENERAL_AUTOSTART,
				NgnConfigurationEntry.DEFAULT_GENERAL_AUTOSTART));
		mCbAEC.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.GENERAL_AEC,
				NgnConfigurationEntry.DEFAULT_GENERAL_AEC));
		mCbVAD.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.GENERAL_VAD,
				NgnConfigurationEntry.DEFAULT_GENERAL_VAD));
		mCbNR.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.GENERAL_NR,
				NgnConfigurationEntry.DEFAULT_GENERAL_NR));
		mCbFEC.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.GENERAL_USE_FEC,
				NgnConfigurationEntry.DEFAULT_GENERAL_USE_FEC));

		mSpProfile.setSelection(Profile.getSpinnerIndex(tmedia_profile_t
				.valueOf(mConfigurationService.getString(
						NgnConfigurationEntry.MEDIA_PROFILE,
						NgnConfigurationEntry.DEFAULT_MEDIA_PROFILE))));
		mSpAudioPlaybackLevel
				.setSelection(AudioPlayBackLevel.getSpinnerIndex(mConfigurationService
						.getFloat(
								NgnConfigurationEntry.GENERAL_AUDIO_PLAY_LEVEL,
								NgnConfigurationEntry.DEFAULT_GENERAL_AUDIO_PLAY_LEVEL)));
		mEtEnumDomain.setText(mConfigurationService.getString(
				NgnConfigurationEntry.GENERAL_ENUM_DOMAIN,
				NgnConfigurationEntry.DEFAULT_GENERAL_ENUM_DOMAIN));
		mEtNetworkQos.setText(Integer.toString(mConfigurationService.getInt(NgnConfigurationEntry.NETWORK_QOS_LOSEPACKETS,
				NgnConfigurationEntry.DEFAULT_NETWORK_QOS_LOSEPACKETS)));

		super.addConfigurationListener(mCbFullScreenVideo);
		super.addConfigurationListener(mCbInterceptOutgoingCalls);
		super.addConfigurationListener(mCbFFC);
		super.addConfigurationListener(mCbAutoStart);
		super.addConfigurationListener(mEtEnumDomain);
		super.addConfigurationListener(mSpAudioPlaybackLevel);
		super.addConfigurationListener(mSpProfile);
		super.addConfigurationListener(mCbAEC);
		super.addConfigurationListener(mCbVAD);
		super.addConfigurationListener(mCbNR);
		super.addConfigurationListener(videofblchoose_choosed);
		super.addConfigurationListener(mCbFEC);
		super.addConfigurationListener(mEtNetworkQos);

	}

	protected void onPause() {
		if (super.mComputeConfiguration) {

			// 修改视频清晰度
			mConfigurationService.putString(
					NgnConfigurationEntry.QOS_PREF_VIDEO_SIZE,
					QOS_PREF_VIDEO_SIZE);

			mConfigurationService.putBoolean(
					NgnConfigurationEntry.GENERAL_AUTOSTART,
					mCbAutoStart.isChecked());
			mConfigurationService.putBoolean(
					NgnConfigurationEntry.GENERAL_FULL_SCREEN_VIDEO,
					mCbFullScreenVideo.isChecked());
			mConfigurationService.putBoolean(
					NgnConfigurationEntry.GENERAL_INTERCEPT_OUTGOING_CALLS,
					mCbInterceptOutgoingCalls.isChecked());
			mConfigurationService.putBoolean(
					NgnConfigurationEntry.GENERAL_USE_FFC, mCbFFC.isChecked());
			mConfigurationService.putFloat(
					NgnConfigurationEntry.GENERAL_AUDIO_PLAY_LEVEL,
					((AudioPlayBackLevel) mSpAudioPlaybackLevel
							.getSelectedItem()).mValue);
			mConfigurationService.putString(
					NgnConfigurationEntry.GENERAL_ENUM_DOMAIN, mEtEnumDomain
							.getText().toString());
			mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_AEC,
					mCbAEC.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_VAD,
					mCbVAD.isChecked());
			mConfigurationService.putBoolean(NgnConfigurationEntry.GENERAL_NR,
					mCbNR.isChecked());
			mConfigurationService.putBoolean(
					NgnConfigurationEntry.GENERAL_USE_FEC, mCbFEC.isChecked());
			// profile should be moved to another screen (e.g. Media)
			mConfigurationService.putString(
					NgnConfigurationEntry.MEDIA_PROFILE, sProfiles[mSpProfile
							.getSelectedItemPosition()].mValue.toString());
			mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_QOS_LOSEPACKETS, 
					NgnStringUtils.parseInt(mEtNetworkQos.getText().toString().trim(),
							NgnConfigurationEntry.DEFAULT_NETWORK_QOS_LOSEPACKETS));

			String account = mConfigurationService.getString(
					NgnConfigurationEntry.IDENTITY_IMPI,
					NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);
			Log.d(TAG, "account2:" + account);
			if (mCheck_autologin.isChecked()) { // 保存自动登录设置
				mMessageReportHashMap.remove(account + "_login_chk");
				mMessageReportHashMap.put(account + "_login_chk", "true");
				try {
					Tools_data.writeData(mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else { // 取消自动登录设置
				mMessageReportHashMap.remove(account + "_login_chk");
				try {
					Tools_data.writeData(mMessageReportHashMap);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			// Compute
			if (!mConfigurationService.commit()) {
				Log.e(TAG, "Failed to commit() configuration");
			} else {
				// codecs, AEC, NoiseSuppression, Echo cancellation, ....
				boolean aec = mCbAEC.isChecked();
				boolean vad = mCbVAD.isChecked();
				boolean nr = mCbNR.isChecked();
				boolean fec = mCbFEC.isChecked();
				int echo_tail = mConfigurationService.getInt(
						NgnConfigurationEntry.GENERAL_ECHO_TAIL,
						NgnConfigurationEntry.DEFAULT_GENERAL_ECHO_TAIL);
				Log.d(TAG, "Configure AEC[" + aec + "/" + echo_tail
						+ "] NoiseSuppression[" + nr
						+ "], Voice activity detection[" + vad + "]");
				if (aec) {
					MediaSessionMgr.defaultsSetEchoSuppEnabled(true);
					MediaSessionMgr.defaultsSetEchoTail(echo_tail); // 2s == 100
																	// packets
																	// of 20 ms
				} else {
					MediaSessionMgr.defaultsSetEchoSuppEnabled(false);
					MediaSessionMgr.defaultsSetEchoTail(0);
				}

				MediaSessionMgr.defaultsSetVideoFecEnabled(fec);
				MediaSessionMgr.defaultsSetVadEnabled(vad);
				MediaSessionMgr.defaultsSetNoiseSuppEnabled(nr);
				MediaSessionMgr.defaultsSetProfile(sProfiles[mSpProfile
						.getSelectedItemPosition()].mValue);
				
				
				int losePackets = mConfigurationService.getInt(NgnConfigurationEntry.NETWORK_QOS_LOSEPACKETS, 
						NgnConfigurationEntry.DEFAULT_NETWORK_QOS_LOSEPACKETS);
				Log.d(TAG,String.format("set losePackets=%d",losePackets));
				MediaSessionMgr.defaultsSetVideoFps(mConfigurationService.getInt(NgnConfigurationEntry.GENERAL_VIDDO_FPS,
						NgnConfigurationEntry.DEFAULT_GENERAL_VIDDO_FPS));
				MediaSessionMgr.defaultsSetNetworkBearPacketsLose(mConfigurationService.getInt(NgnConfigurationEntry.NETWORK_QOS_LOSEPACKETS, 
						NgnConfigurationEntry.DEFAULT_NETWORK_QOS_LOSEPACKETS));
			}

			NgnCameraProducer.useFrontFacingCamera = NgnEngine
					.getInstance()
					.getConfigurationService()
					.getBoolean(NgnConfigurationEntry.GENERAL_USE_FFC,
							NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC);

			NgnCameraProducer_surface.useFrontFacingCamera = NgnEngine
					.getInstance()
					.getConfigurationService()
					.getBoolean(NgnConfigurationEntry.GENERAL_USE_FFC,
							NgnConfigurationEntry.DEFAULT_GENERAL_USE_FFC);

			super.mComputeConfiguration = false;
		}
		super.onPause();
	}

	static class Profile {
		tmedia_profile_t mValue;
		String mDescription;

		Profile(tmedia_profile_t value, String description) {
			mValue = value;
			mDescription = description;
		}

		@Override
		public String toString() {
			return mDescription;
		}

		static int getSpinnerIndex(tmedia_profile_t value) {
			for (int i = 0; i < sProfiles.length; i++) {
				if (sProfiles[i].mValue == value) {
					return i;
				}
			}
			return 0;
		}
	}

	static class AudioPlayBackLevel {
		float mValue;
		String mDescription;

		AudioPlayBackLevel(float value, String description) {
			mValue = value;
			mDescription = description;
		}

		@Override
		public String toString() {
			return mDescription;
		}

		static int getSpinnerIndex(float value) {
			for (int i = 0; i < sAudioPlaybackLevels.length; i++) {
				if (sAudioPlaybackLevels[i].mValue == value) {
					return i;
				}
			}
			return 0;
		}
	}
}
