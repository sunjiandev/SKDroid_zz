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

import org.doubango.ngn.utils.NgnGraphicsUtils;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.adapter.AbcListAdapter;
import com.sunkaisens.skdroid.adapter.CallAdapter;
import com.sunkaisens.skdroid.adapter.ContactAdapter;
import com.sunkaisens.skdroid.component.MyListView;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.util.GlobalVar;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class ScreenTabContact_MySubscribe extends BaseScreen {
	private static String TAG = ScreenTabContact_MySubscribe.class
			.getCanonicalName();

	private List<ModelContact> contactListOrg = new ArrayList<ModelContact>();

	public ScreenTabContact_MySubscribe() {
		super(SCREEN_TYPE.TAB_INFO_T, TAG);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.e("ScreenTabContact_MySubscribe",
				"ScreenTabContact_MySubscribe ONSTART");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.e("ScreenTabContact_MySubscribe",
				"ScreenTabContact_MySubscribe ONReSTART");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e("ScreenTabContact_MySubscribe",
				"ScreenTabContact_MySubscribe OnDestroy");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e("ScreenTabContact_MySubscribe", "ScreenTabContact OnPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		SystemVarTools.updateContactRecent();
		Log.e("ScreenTabContact_MySubscribe",
				"ScreenTabContact_MySubscribe OnResume");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.e("ScreenTabContact_MySubscribe",
				"ScreenTabContact_MySubscribe OnStop");
	}

	@Override
	public boolean refresh() {

		Log.e("ScreenTabContact_MySubscribe refresh",
				"ScreenTabContact_MySubscribe refresh");
		if(contactListOrg == null){
			contactListOrg = new ArrayList<ModelContact>();
		}
		contactListOrg.clear();

		if (GlobalVar.bADHocMode == false) {
			contactListOrg.addAll(SystemVarTools
					.getContactListSubscribeGroupOrg());
		}

		ListView g3 = (ListView) findViewById(R.id.screen_mysubscribe_listview);

		if (contactListOrg == null || contactListOrg.size() == 0) {
			g3.setVisibility(View.GONE);

		} else {
			g3.setVisibility(View.VISIBLE);

			g3.setAdapter(new ContactAdapter(this, contactListOrg));
			g3.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					if (contactListOrg.size() > arg2) {
						ModelContact item = contactListOrg.get(arg2);

						if (mScreenService.show(ScreenPersonInfo.class,
								item.mobileNo)) {
						}
					}

					// finish();
				}

			});
		}

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.screen_mysubscribegroup);

		ImageView back = (ImageView) findViewById(R.id.screen_mysubscribegroup_back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

		contactListOrg.clear();

		contactListOrg.addAll(SystemVarTools.getContactListSubscribeGroupOrg());
		ListView g3 = (ListView) findViewById(R.id.screen_mysubscribe_listview);

		if (contactListOrg == null || contactListOrg.size() == 0) {
			g3.setVisibility(View.GONE);

		} else {
			g3.setVisibility(View.VISIBLE);

			g3.setAdapter(new ContactAdapter(this, contactListOrg));
			g3.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					if (contactListOrg.size() > arg2) {
						ModelContact item = contactListOrg.get(arg2);

						if (mScreenService.show(ScreenPersonInfo.class,
								item.mobileNo)) {
						}
					}

					// finish();
				}

			});
		}

		// // 更新显示数据
		SystemVarTools.updateContactRecent();// 更新最近通讯录
		refresh();

		BroadcastReceiver bcreceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				// Registration Event 服务器（实为本地代理CSCF-Proxy）对客户端注册动作的响应反馈：
				if (ServiceContact.CONTACT_REFRASH_MSG.equals(action)) {
					ScreenTabContact_MySubscribe.this.refresh();
				}
			}
		};
		//
		registerReceiver(bcreceiver, new IntentFilter(
				ServiceContact.CONTACT_REFRASH_MSG));

	}

}
