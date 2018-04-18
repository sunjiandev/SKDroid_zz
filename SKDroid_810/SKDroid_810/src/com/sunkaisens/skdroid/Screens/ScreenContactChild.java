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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.adapter.ContactChildAdapter;
import com.sunkaisens.skdroid.component.MyGridView;
import com.sunkaisens.skdroid.model.ModelContact;

public class ScreenContactChild extends BaseScreen {
	public static String TAG = ScreenContactChild.class.getCanonicalName();

	private List<ModelContact> contactListAll = new ArrayList<ModelContact>();

	private TextView toatalNumber;

	private ModelContact org = null;

	private BroadcastReceiver bcreceiver;

	private ImageView back;

	private MyGridView myChildGridView;

	public ScreenContactChild() {
		super(SCREEN_TYPE.CONTACT_CHILD, TAG);
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.screen_child_contact);

		// back
		back = (ImageView) findViewById(R.id.screen_tab_child_contact_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

		toatalNumber = (TextView) findViewById(R.id.Screen_Contact_Child_toatalnumber);

		myChildGridView = (MyGridView) findViewById(R.id.Screen_Contact_Child_myGrid1);
		// 更新数据
		super.mId = getIntent().getStringExtra("id");
		updateInfo(super.mId);
		//

		bcreceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				// Registration Event 服务器（实为本地代理CSCF-Proxy）对客户端注册动作的响应反馈：
				if (ServiceContact.CONTACT_REFRASH_MSG.equals(action)) {
					ScreenContactChild.this.refreshContactListShow();
				}
			}
		};

		registerReceiver(bcreceiver, new IntentFilter(
				ServiceContact.CONTACT_REFRASH_MSG));
	}

	public void updateInfo(String mobileNo) {
		if (mobileNo != null && mobileNo.startsWith("ScreenContactChild:")) {
			mobileNo = mobileNo.replace("ScreenContactChild:", "");
			org = SystemVarTools.createContactFromRemoteParty(mobileNo);
		}
		if (org == null)
			return;
		TextView title = (TextView) findViewById(R.id.title);
		// title.setText(org.name + "成员\n" + org.mobileNo);
		title.setText(ScreenContactChild.this
				.getString(R.string.contact_child_group_mem));
		refreshContactListShow();
	}

	@Override
	protected void onResume() {
		// ScreenContactChild.this.refreshContactListShow();
		Log.e("", "screenContactChild onresume");
		super.mId = getIntent().getStringExtra("id");
		updateInfo(super.mId);
		super.onResume();

	}

	private void refreshContactListShow() {

		Log.e("ScreenContactChild refresh", "refreshContactListShow");

		contactListAll.clear();
		if (org != null) {
			List<ModelContact> list1 = SystemVarTools
					.getOrgChildContact(org, 0);
			List<ModelContact> list2 = SystemVarTools
					.getOrgChildContact(org, 1);
			if (list1 != null)
				contactListAll.addAll(list1);
			if (list2 != null) {
				contactListAll.addAll(list2);
			}
		}

		myChildGridView
				.setAdapter(new ContactChildAdapter(this, contactListAll));
		myChildGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				if (contactListAll.size() > arg2) {
					ModelContact item = contactListAll.get(arg2);

					if (item != null) {
						if (item.isgroup == false) {
							if (mScreenService.show(ScreenPersonInfo.class,
									item.mobileNo)) {
							}
						} else {
							if (mScreenService.show(ScreenOrgInfo.class,
									item.mobileNo)) {
							}
						}
					}
				}
			}

		});

		toatalNumber.setText(contactListAll.size() + "");

	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.e(TAG, "ScreenContact onNewIntent");

		super.mId = intent.getStringExtra("id");
		updateInfo(super.mId);

		super.onNewIntent(intent);
	}

}
