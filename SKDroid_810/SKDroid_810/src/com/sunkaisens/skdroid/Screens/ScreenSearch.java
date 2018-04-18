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

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Services.ServiceScreen;
import com.sunkaisens.skdroid.Utils.ChkVer;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.adapter.ContactAdapter;
import com.sunkaisens.skdroid.model.ModelContact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ScreenSearch extends BaseScreen {
	private static final String TAG = ScreenSearch.class.getCanonicalName();

	private List<ModelContact> contactListAll = new ArrayList<ModelContact>();

	private String searchContent = "";
	private EditText searchedit;

	public ScreenSearch() {
		super(SCREEN_TYPE.ABOUT_T, TAG);
	}

	public static boolean isable = true;

	@Override
	public boolean refresh() {
		Log.e("ScreenTabContact refresh", "ScreenTabContact refresh");
		contactListAll.clear();

		contactListAll.addAll(SystemVarTools.getContactAll());

		if (searchContent != null && !searchContent.isEmpty()) {

			for (int i = 0; i < contactListAll.size(); i++) {
				if (contactListAll.get(i).name.indexOf(searchContent) < 0
						&& contactListAll.get(i).mobileNo
								.indexOf(searchContent) < 0) {
					contactListAll.remove(i);
					i--;
				}
			}
		}

		ListView g2 = (ListView) findViewById(R.id.screen_search_list);
		contactListAll = SystemVarTools.sortContactsByABC(contactListAll);
		((ContactAdapter) g2.getAdapter()).setList(contactListAll);

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_search);

		ImageView back = (ImageView) findViewById(R.id.screen_search_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

		if (isable) {
			ScreenOrgInfo.isable = true;
		} else {
			ScreenOrgInfo.isable = false;
		}

		final ListView g2 = (ListView) findViewById(R.id.screen_search_list);
		g2.setAdapter(new ContactAdapter(this, contactListAll));

		g2.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				if (isable) {
					ScreenOrgInfo.isable = true;
				} else {
					ScreenOrgInfo.isable = false;
				}

				if (contactListAll.size() > arg2) {
					ModelContact item = contactListAll.get(arg2);

					if (item.isgroup == false) {
						if (mScreenService.showPersonOrOrgInfo(
								ScreenPersonInfo.class, item.mobileNo)) {

						}
					} else {

						if (mScreenService.show(ScreenOrgInfo.class,
								item.mobileNo)) {
						}
					}
				}
			}

		});

		refresh();

		BroadcastReceiver bcreceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				// Registration Event 服务器（实为本地代理CSCF-Proxy）对客户端注册动作的响应反馈：
				if (ServiceContact.CONTACT_REFRASH_MSG.equals(action)) {
					ScreenSearch.this.refresh();
				}
			}
		};

		registerReceiver(bcreceiver, new IntentFilter(
				ServiceContact.CONTACT_REFRASH_MSG));

		searchedit = (EditText) findViewById(R.id.screen_searchedit);

		searchedit.requestFocus();
		searchedit.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				EditText t = (EditText) findViewById(R.id.screen_searchedit);
				searchContent = t.getText().toString().trim();
				ScreenSearch.this.refresh();
			}

		});

	}
}
