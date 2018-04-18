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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.adapter.ContactChildAdapter;
import com.sunkaisens.skdroid.component.MyGridView;
import com.sunkaisens.skdroid.model.ModelContact;

public class ScreenContactAdd extends BaseScreen {
	private static String TAG = ScreenContactAdd.class.getCanonicalName();

	private List<ModelContact> contactListAll = new ArrayList<ModelContact>();

	private String searchContent = "";

	public ScreenContactAdd() {
		super(SCREEN_TYPE.TAB_INFO_T, TAG);
	}

	@Override
	public boolean hasBack() {
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent();
			intent.putExtra("result", "");
			setResult(RESULT_CANCELED, intent);
			finish();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	@Override
	public boolean back() {
		Intent intent = new Intent();
		intent.putExtra("result", "");
		setResult(RESULT_CANCELED, intent);
		finish();
		return true;
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
		contactListAll.clear();
		contactListAll.addAll(SystemVarTools.getContactAll());
		contactListAll.addAll(SystemVarTools.getContactOrg());
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
		MyGridView g2 = (MyGridView) findViewById(R.id.Screen_contact_add_myGrid2);
		((ContactChildAdapter) g2.getAdapter()).notifyDataSetChanged();
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.screen_contact_add);
		MyGridView g2 = (MyGridView) findViewById(R.id.Screen_contact_add_myGrid2);
		g2.setAdapter(new ContactChildAdapter(this, contactListAll));
		g2.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (contactListAll.size() > arg2) {
					ModelContact item = contactListAll.get(arg2);
					String result = item.mobileNo;
					Intent intent = new Intent();
					intent.putExtra("result", result);
					/**
					 * 调用setResult方法表示我将Intent对象返回给之前的那个Activity，
					 * 这样就可以在onActivityResult方法中得到Intent对象， 设置结果数据
					 */
					setResult(RESULT_OK, intent);
					/**
					 * 结束当前这个Activity对象的生命 关闭Activity
					 */
					finish();
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
					ScreenContactAdd.this.refresh();
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
				ScreenContactAdd.this.refresh();
			}

		});

		ImageView back = (ImageView) findViewById(R.id.screen_chat_add_back);
		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				back();
			}
		});

	}

}
