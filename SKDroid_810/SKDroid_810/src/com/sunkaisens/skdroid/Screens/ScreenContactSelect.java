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

import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.adapter.ContactAdapter;
import com.sunkaisens.skdroid.model.ModelContact;

public class ScreenContactSelect extends BaseScreen {
	private static String TAG = ScreenContactSelect.class.getCanonicalName();

	private List<ModelContact> contactListRecent = new ArrayList<ModelContact>();
	private List<ModelContact> contactListAll = new ArrayList<ModelContact>();
	private List<ModelContact> contactListOrg = new ArrayList<ModelContact>();

	private String searchContent = "";
	private TabHost mHost = null;
	LocalActivityManager lam;

	public ScreenContactSelect() {
		super(SCREEN_TYPE.TAB_INFO_T, TAG);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.e("ScreenTabContact", "ScreenTabContact ONSTART");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.e("ScreenTabContact", "ScreenTabContact ONReSTART");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e("ScreenTabContact", "ScreenTabContact OnDestroy");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.e("ScreenTabContact", "ScreenTabContact OnPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e("ScreenTabContact", "ScreenTabContact OnResume");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.e("ScreenTabContact", "ScreenTabContact OnStop");
	}

	@Override
	public boolean refresh() {

		if (SystemVarTools.isContactOK() == false)
			return false;
		contactListRecent.clear();
		contactListAll.clear();
		contactListOrg.clear();
		contactListRecent.addAll(SystemVarTools.getContactRecent());
		contactListAll.addAll(SystemVarTools.getContactAll());
		contactListOrg.addAll(SystemVarTools.getContactOrg());
		if (searchContent != null && !searchContent.isEmpty()) {
			for (int i = 0; i < contactListRecent.size(); i++) {
				if (contactListRecent.get(i).name.indexOf(searchContent) < 0
						&& contactListRecent.get(i).mobileNo
								.indexOf(searchContent) < 0) {
					contactListRecent.remove(i);
					i--;
				}
			}
			for (int i = 0; i < contactListAll.size(); i++) {
				if (contactListAll.get(i).name.indexOf(searchContent) < 0
						&& contactListAll.get(i).mobileNo
								.indexOf(searchContent) < 0) {
					contactListAll.remove(i);
					i--;
				}
			}
			for (int i = 0; i < contactListOrg.size(); i++) {
				if (contactListOrg.get(i).name.indexOf(searchContent) < 0
						&& contactListOrg.get(i).mobileNo
								.indexOf(searchContent) < 0) {
					contactListOrg.remove(i);
					i--;
				}
			}
		}
		GridView g1 = (GridView) findViewById(R.id.myGrid1);
		((ContactAdapter) g1.getAdapter()).notifyDataSetChanged();
		GridView g2 = (GridView) findViewById(R.id.myGrid2);
		((ContactAdapter) g2.getAdapter()).notifyDataSetChanged();
		GridView g3 = (GridView) findViewById(R.id.myGrid3);
		((ContactAdapter) g3.getAdapter()).notifyDataSetChanged();
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.screen_tab_contact);
		//

		TabWidget tw = (TabWidget) findViewById(android.R.id.tabs);
		mHost = (TabHost) findViewById(android.R.id.tabhost);
		lam = new LocalActivityManager(this, false);
		lam.dispatchCreate(savedInstanceState);
		//
		mHost.setup(lam);

		// mHost.add
		//
		TabSpec tab1 = mHost.newTabSpec("TS_RECENT");
		tab1.setIndicator(ScreenContactSelect.this
				.getString(R.string.recontact));
		tab1.setContent(R.id.myGrid1);
		//
		TabSpec tab2 = mHost.newTabSpec("TS_ALL");
		tab2.setIndicator(ScreenContactSelect.this.getString(R.string.all));
		tab2.setContent(R.id.myGrid2);
		//
		TabSpec tab3 = mHost.newTabSpec("TS_ORG");
		tab3.setIndicator(ScreenContactSelect.this.getString(R.string.org));
		tab3.setContent(R.id.myGrid3);

		mHost.addTab(tab1);
		mHost.addTab(tab2);
		mHost.addTab(tab3);
		//
		for (int i = 0; i < tw.getChildCount(); i++) {
			View child = tw.getChildAt(i);
			final TextView tv = (TextView) child
					.findViewById(android.R.id.title);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv
					.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
			params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
			tv.setLayoutParams(params);
			int paddingLR = NgnGraphicsUtils.getSizeInPixel(20);
			int paddingUD = NgnGraphicsUtils.getSizeInPixel(5);
			if (mHost.getCurrentTab() == i) {
				child.setBackgroundColor(mHost.getResources().getColor(
						R.color.color_mainbg));
				tv.setTextColor(mHost.getResources().getColor(
						R.color.color_text2));
				tv.setBackgroundColor(mHost.getResources().getColor(
						R.color.color_main2));
				tv.setPadding(paddingLR, paddingUD, paddingLR, paddingUD);
			} else {
				child.setBackgroundColor(mHost.getResources().getColor(
						R.color.color_mainbg));
				tv.setTextColor(mHost.getResources().getColor(
						R.color.color_main2));
				tv.setBackgroundColor(mHost.getResources().getColor(
						R.color.color_mainbg));
			}
		}
		//
		GridView g1 = (GridView) findViewById(R.id.myGrid1);
		g1.setAdapter(new ContactAdapter(this, contactListRecent));
		g1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (contactListRecent.size() > arg2) {
					ModelContact item = contactListRecent.get(arg2);
					if (mScreenService.show(ScreenPersonInfo.class)) {
						final IBaseScreen screen = mScreenService
								.getScreen(ScreenPersonInfo.TAG);
						if (screen instanceof ScreenPersonInfo) {
							((ScreenPersonInfo) screen)
									.updateInfo(item.mobileNo);
						}
					}
				}
			}

		});
		GridView g2 = (GridView) findViewById(R.id.myGrid2);
		g2.setAdapter(new ContactAdapter(this, contactListAll));
		g2.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (contactListAll.size() > arg2) {
					ModelContact item = contactListAll.get(arg2);
					if (mScreenService.show(ScreenPersonInfo.class,
							item.mobileNo)) {
					}
				}
			}

		});
		GridView g3 = (GridView) findViewById(R.id.myGrid3);
		g3.setAdapter(new ContactAdapter(this, contactListOrg));
		g3.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (contactListOrg.size() > arg2) {
					ModelContact item = contactListOrg.get(arg2);
					if (mScreenService.show(ScreenOrgInfo.class, item.mobileNo)) {
					}
				}
			}

		});
		// 更新显示数据
		refresh();
		//
		BroadcastReceiver bcreceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				// Registration Event 服务器（实为本地代理CSCF-Proxy）对客户端注册动作的响应反馈：
				if (ServiceContact.CONTACT_REFRASH_MSG.equals(action)) {
					ScreenContactSelect.this.refresh();
				}
			}
		};

		registerReceiver(bcreceiver, new IntentFilter(
				ServiceContact.CONTACT_REFRASH_MSG));
		//
		EditText searchedit = (EditText) findViewById(R.id.searchedit);
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
				EditText t = (EditText) findViewById(R.id.searchedit);
				searchContent = t.getText().toString().trim();
				ScreenContactSelect.this.refresh();
			}

		});

		mHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				TabWidget tw = (TabWidget) findViewById(android.R.id.tabs);
				ScreenContactSelect.this.refresh();
				//
				int paddingLR = NgnGraphicsUtils.getSizeInPixel(20);
				int paddingUD = NgnGraphicsUtils.getSizeInPixel(5);

				for (int i = 0; i < tw.getChildCount(); i++) {
					View child = tw.getChildAt(i);
					final TextView tv = (TextView) child
							.findViewById(android.R.id.title);
					if (mHost.getCurrentTab() == i) {
						child.setBackgroundColor(mHost.getResources().getColor(
								R.color.color_mainbg));
						tv.setTextColor(mHost.getResources().getColor(
								R.color.color_text2));
						tv.setBackgroundColor(mHost.getResources().getColor(
								R.color.color_main2));
						tv.setPadding(paddingLR, paddingUD, paddingLR,
								paddingUD);
					} else {
						child.setBackgroundColor(mHost.getResources().getColor(
								R.color.color_mainbg));
						tv.setTextColor(mHost.getResources().getColor(
								R.color.color_main2));
						tv.setBackgroundColor(mHost.getResources().getColor(
								R.color.color_mainbg));
					}

				}
			}

		});
	}

}
