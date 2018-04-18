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

import java.util.List;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnUriUtils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.session.SessionType;

public class ScreenNetInfo extends BaseScreen {
	public static String TAG = ScreenNetInfo.class.getCanonicalName();

	private ModelContact info = null;
	private Button audiocall = null;
	private Button sms = null;
	private Button videocall = null;
	private Button members = null;
	private Button setcurrentgroup = null;

	private SharedPreferences mSetCurrentGroup;
	private SharedPreferences.Editor mSetCurrentGroupEditor;

	static enum PhoneInputType {
		Numbers, Text
	}

	//
	// private final INgnSipService mSipService;

	public ScreenNetInfo() {
		super(SCREEN_TYPE.SKS_Screen_OrgInfo, TAG);

		// mSipService = getEngine().getSipService();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_orginfo);

		super.mId = getIntent().getStringExtra("id");
		updateInfo(super.mId);
		// list数据填充
		// /ListView list = (ListView) findViewById(R.id.messagerlist);

		//
		ImageView back = (ImageView) findViewById(R.id.back);
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
					// String g_v_dial_Uri =
					// "sip:"+info.mobileNo+"@sunkaisens.com";
					String realm = NgnEngine
							.getInstance()
							.getConfigurationService()
							.getString(NgnConfigurationEntry.NETWORK_REALM,
									NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
					String g_v_dial_Uri = "sip:" + info.mobileNo + "@" + realm;
					if (NgnUriUtils.isValidSipUri(g_v_dial_Uri)) {
						ServiceAV.makeCall(g_v_dial_Uri, NgnMediaType.Audio,
								SessionType.GroupAudioCall);

						if (NgnApplication.isBh03()) { // 手持台 ptt
							if (Main.isFirstPTT_onKeyDown) {
								Main.isFirstPTT_onKeyDown = false;
							}
						}
						if (NgnApplication.isBh03()) { // 手持台 ptt
							if (Main.isFirstPTT_onKeyLongPress) {
								Main.isFirstPTT_onKeyLongPress = false;
							}
						}
						Log.d(TAG, "GroupAudioCall - onClick()");
					}
				}
			}
		});

		Button sms = (Button) findViewById(R.id.sms);
		sms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Toast.makeText(getApplicationContext(),
				// "暂不支持短信群发!",Toast.LENGTH_SHORT).show();
				if (info != null) {
					ScreenChat.startChat(info.mobileNo, true);
				}
			}
		});

		Button videocall = (Button) findViewById(R.id.videocall);
		videocall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (info != null) {
					// String g_v_dial_Uri =
					// "sip:"+info.mobileNo+"@sunkaisens.com";
					String realm = NgnEngine
							.getInstance()
							.getConfigurationService()
							.getString(NgnConfigurationEntry.NETWORK_REALM,
									NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
					String g_v_dial_Uri = "sip:" + info.mobileNo + "@" + realm;
					if (NgnUriUtils.isValidSipUri(g_v_dial_Uri)) {
						ServiceAV.makeCall(g_v_dial_Uri,
								NgnMediaType.AudioVideo,
								SessionType.GroupVideoCall);
					}
				}
			}
		});

		Button members = (Button) findViewById(R.id.members);
		members.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				List<ModelContact> list1 = SystemVarTools.getOrgChildContact(
						info, 0);
				List<ModelContact> list2 = SystemVarTools.getOrgChildContact(
						info, 1);
				if ((list1 == null || list1.isEmpty())
						&& (list2 == null || list2.isEmpty())) {
					Toast.makeText(
							getApplicationContext(),
							ScreenNetInfo.this
									.getString(R.string.group_is_null),
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (mScreenService.show(ScreenContactChild.class,
						"ScreenContactChild:" + info.mobileNo)) {

				}
			}
		});

		Button setcurrentgroup = (Button) findViewById(R.id.setcurrentgroup);
		setcurrentgroup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				// mSetCurrentGroup =
				// NgnApplication.getContext().getSharedPreferences(BaseSocketMessage.SHARED_PREF_GROUP,
				// Activity.MODE_PRIVATE);
				// mSetCurrentGroupEditor = mSetCurrentGroup.edit();
				// mSetCurrentGroupEditor.putString(BaseSocketMessage.DEFAULT_CURRENT_GROUP,
				// info.mobileNo);
				// mSetCurrentGroupEditor.commit();
				SystemVarTools.setCurrentGroup(info.mobileNo);
				Toast.makeText(
						getApplicationContext(),
						ScreenNetInfo.this.getString(R.string.allready)
								+ info.mobileNo
								+ ScreenNetInfo.this
										.getString(R.string.set_default_group),
						Toast.LENGTH_SHORT).show();
			}
		});
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

	//
	public void updateInfo(String remoteParty) {
		info = SystemVarTools.createContactFromRemoteParty(remoteParty);
		TextView name = (TextView) findViewById(R.id.name);
		TextView number = (TextView) findViewById(R.id.number);
		TextView brief = (TextView) findViewById(R.id.brief);
		ImageView image = (ImageView) findViewById(R.id.icon);
		name.setText(info.name);
		number.setText(info.mobileNo);
		brief.setText(info.brief);
		image.setImageResource(SystemVarTools.getThumbID(info.imageid));
	}
}
