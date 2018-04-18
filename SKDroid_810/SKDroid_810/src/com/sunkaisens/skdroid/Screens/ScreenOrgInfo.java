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

import java.util.ArrayList;
import java.util.List;

import com.sks.net.socket.message.BaseSocketMessage;
import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnNetworkService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.StaticLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sunkaisens.skdroid.Services.ServiceAV;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.crash.CrashHandler;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.session.SessionType;

public class ScreenOrgInfo extends BaseScreen {
	public static String TAG = ScreenOrgInfo.class.getCanonicalName();

	private ModelContact orgShowContact = null;
	private INgnNetworkService networkService;

	public static boolean isable = true;

	static enum PhoneInputType {
		Numbers, Text
	}

	//
	// private final INgnSipService mSipService;

	public ScreenOrgInfo() {
		super(SCREEN_TYPE.SKS_Screen_OrgInfo, TAG);
		networkService = Engine.getInstance().getNetworkService();
		// mSipService = getEngine().getSipService();

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_orginfo);

		MyLog.d(TAG, "onCreate");

		super.mId = getIntent().getStringExtra("id");
		updateInfo(super.mId);
		// list数据填充
		// /ListView list = (ListView) findViewById(R.id.messagerlist);

		//
		ImageView back = (ImageView) findViewById(R.id.screen_org_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

		TextView orginfo = (TextView) findViewById(R.id.org);

		TextView briefInfo = (TextView) findViewById(R.id.brief);

		final ScreenOrgInfo so = this;

		Button audiocall = (Button) findViewById(R.id.audiocall);

		if (isable == false) {
			// audiocall.setVisibility(View.GONE);
			audiocall.setEnabled(false);
			audiocall.setAlpha((float) 0.3);
		} else {
			audiocall.setVisibility(View.VISIBLE);
		}

		audiocall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (orgShowContact != null) {
					if (CrashHandler.isNetworkAvailable2()) {
						// String g_v_dial_Uri =
						// "sip:"+info.mobileNo+"@sunkaisens.com";
						String realm = NgnEngine
								.getInstance()
								.getConfigurationService()
								.getString(
										NgnConfigurationEntry.NETWORK_REALM,
										NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
						String g_v_dial_Uri = "sip:" + orgShowContact.mobileNo
								+ "@" + realm;
						if (NgnUriUtils.isValidSipUri(g_v_dial_Uri)) {
							ServiceAV.makeCall(g_v_dial_Uri,
									NgnMediaType.Audio,
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
							MyLog.d(TAG, "GroupAudioCall - onClick()");
						}
					} else {
						SystemVarTools.showNotifyDialog(""
								+ getText(R.string.tip_net_no_conn_error), so);
						// Intent refresh = new
						// Intent(NgnRegistrationEventArgs.ACTION_REFRESHREGISTRATION_EVENT);
						// refresh.putExtra(NgnRegistrationEventArgs.REFRESHREGISTRATION,
						// "NOK");
						// NgnApplication.getContext().sendBroadcast(refresh);
					}
				}
			}
		});

		Button sms = (Button) findViewById(R.id.sms);
		if (isable == false) {
			sms.setEnabled(false);
			sms.setAlpha((float) 0.3);
		} else {
			sms.setVisibility(View.VISIBLE);
		}
		MyLog.d(TAG, "创建组织信息grdtgrt" + isable);
		sms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Toast.makeText(getApplicationContext(),
				// "暂不支持短信群发!",Toast.LENGTH_SHORT).show();
				if (orgShowContact != null) {
					ScreenChat.startChat(orgShowContact.mobileNo, true);
				}
			}
		});

		Button videocall = (Button) findViewById(R.id.videocall);
		if (isable == false) {
			videocall.setEnabled(false);
			videocall.setAlpha((float) 0.3);
		} else {
			videocall.setVisibility(View.VISIBLE);
		}

		videocall.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				ScreenAV.ispeoplePTT = false;
				MyLog.d(TAG, "ispeoplePTT change to false");

				if (orgShowContact != null) {
					if (CrashHandler.isNetworkAvailable2()) {
						// String g_v_dial_Uri =
						// "sip:"+info.mobileNo+"@sunkaisens.com";
						String realm = NgnEngine
								.getInstance()
								.getConfigurationService()
								.getString(
										NgnConfigurationEntry.NETWORK_REALM,
										NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
						String g_v_dial_Uri = "sip:" + orgShowContact.mobileNo
								+ "@" + realm;
						if (NgnUriUtils.isValidSipUri(g_v_dial_Uri)) {
							ServiceAV.makeCall(g_v_dial_Uri,
									NgnMediaType.AudioVideo,
									SessionType.GroupVideoCall);
						}
					} else {
						SystemVarTools.showNotifyDialog(""
								+ getText(R.string.tip_net_no_conn_error), so);
						// Intent refresh = new
						// Intent(NgnRegistrationEventArgs.ACTION_REFRESHREGISTRATION_EVENT);
						// refresh.putExtra(NgnRegistrationEventArgs.REFRESHREGISTRATION,
						// "NOK");
						// NgnApplication.getContext().sendBroadcast(refresh);
					}
				}
			}
		});

		Button members = (Button) findViewById(R.id.members);
		members.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				List<ModelContact> list1 = SystemVarTools.getOrgChildContact(
						orgShowContact, 0);
				List<ModelContact> list2 = SystemVarTools.getOrgChildContact(
						orgShowContact, 1);

				// Log.e("成员信息！！！ List1", list1.size()+"");
				// Log.e("成员信息！！！ List2", list2.size()+"");

				if ((list1 == null || list1.isEmpty())
						&& (list2 == null || list2.isEmpty())) {
					Toast.makeText(getApplicationContext(),
							getText(R.string.tip_group_no_members),
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (mScreenService.show(ScreenContactChild.class,
						"ScreenContactChild:" + orgShowContact.mobileNo)) {

				}
			}
		});

		Button setcurrentgroup = (Button) findViewById(R.id.setcurrentgroup);

		// if(isable==false){
		// setcurrentgroup.setEnabled(false);
		// setcurrentgroup.setAlpha((float) 0.3);
		// }else{
		// setcurrentgroup.setVisibility(View.VISIBLE);
		// }

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
				SystemVarTools.setCurrentGroup(orgShowContact.mobileNo);
				Toast.makeText(
						getApplicationContext(),
						"" + getText(R.string.tip_group_default_group_1)
								+ orgShowContact.mobileNo + " "
								+ getText(R.string.tip_group_default_group_2),
						Toast.LENGTH_SHORT).show();
			}
		});

		ModelContact myself = SystemVarTools
				.createContactFromPhoneNumber(SystemVarTools.getmIdentity());

		// if(info.businessType != null &&
		// info.businessType.toLowerCase().equals("disp")){
		if ((orgShowContact.businessType != null && (!orgShowContact.businessType
				.equals("normal")
				&& !orgShowContact.businessType.equals("trunk") && !orgShowContact.businessType
					.equals(ServiceContact.PUBLICGROUP_TYPE)))
				|| orgShowContact.businessType == null) {
			if (orgShowContact != null && myself != null
					&& !myself.parent.equals(orgShowContact.index)) {
				audiocall.setVisibility(View.GONE);
				videocall.setVisibility(View.GONE);
				sms.setVisibility(View.GONE);
				setcurrentgroup.setVisibility(View.GONE);
				members.setBackgroundResource(R.drawable.sel_textbutton_normal);
			}
		}
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

		// Log.e("dfbtgfbtrgf!!!!!!@@@@@@@","kjrvw,ne" );

		orgShowContact = SystemVarTools
				.createContactFromRemoteParty(remoteParty);
		TextView name = (TextView) findViewById(R.id.name);
		TextView number = (TextView) findViewById(R.id.number);
		TextView brief = (TextView) findViewById(R.id.brief);
		ImageView image = (ImageView) findViewById(R.id.icon);
		name.setText(orgShowContact.name);
		number.setText(orgShowContact.mobileNo);
		brief.setText(orgShowContact.brief);
		image.setImageResource(SystemVarTools
				.getThumbID(orgShowContact.imageid));
	}

	@Override
	protected void onNewIntent(Intent intent) {
		MyLog.d(TAG, "ScreenOrgInfo onNewIntent");
		if (!intent.getStringExtra("id").equals(TAG)) {
			super.mId = intent.getStringExtra("id");
		}
		updateInfo(super.mId);

		super.onNewIntent(intent);
	}
}
