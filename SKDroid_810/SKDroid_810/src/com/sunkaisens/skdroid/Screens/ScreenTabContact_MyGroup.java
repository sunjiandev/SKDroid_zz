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
import org.doubango.utils.MyLog;

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

public class ScreenTabContact_MyGroup extends BaseScreen {
	private static String TAG = ScreenTabContact_MyGroup.class
			.getCanonicalName();

	private List<ModelContact> contactListOrg = new ArrayList<ModelContact>();
	private List<ModelContact> contactListNet = new ArrayList<ModelContact>(); // 业务
	private List<ModelContact> contactListDisp = new ArrayList<ModelContact>();

	public static boolean isable = true;

	public ScreenTabContact_MyGroup() {
		super(SCREEN_TYPE.TAB_INFO_T, TAG);
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onRestart() {
		super.onRestart();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onResume() {
		super.onResume();

		refresh();
		MyLog.d(TAG, TAG + " OnResume");
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	public boolean refresh() {

		MyLog.d(TAG, "ScreenTabContactMyGroup refresh");
		contactListOrg.clear();
		contactListNet.clear();
		contactListDisp.clear();

		if (GlobalVar.bADHocMode == false) {
			contactListOrg.addAll(SystemVarTools.getContactOrg());
			contactListOrg = SystemVarTools.sortContactsByABC(contactListOrg);

			contactListNet.addAll(SystemVarTools.getContactListBusinessOrg());
			contactListNet = SystemVarTools.sortContactsByABC(contactListNet);

			contactListDisp.addAll(SystemVarTools
					.getContactListGlobalGroupOrg());
			contactListDisp = SystemVarTools.sortContactsByABC(contactListDisp);

		}

		TextView t3 = (TextView) findViewById(R.id.screen_mygroup_textView_org);
		TextView t4 = (TextView) findViewById(R.id.screen_mygroup_textView_bussiness);
		TextView t5 = (TextView) findViewById(R.id.screen_mygroup_textView_publicgroup);

		TextView t3null = (TextView) findViewById(R.id.screen_mygroup_listview_org_null);
		TextView t4null = (TextView) findViewById(R.id.screen_mygroup_listview_bussiness_null);
		TextView t5null = (TextView) findViewById(R.id.screen_mygroup_listview_publicgroup_null);

		MyListView g3 = (MyListView) findViewById(R.id.screen_mygroup_listview_org);
		MyListView g4 = (MyListView) findViewById(R.id.screen_mygroup_listview_bussiness);
		MyListView g5 = (MyListView) findViewById(R.id.screen_mygroup_listview_publicgroup);

		if (contactListOrg == null || contactListOrg.size() == 0) {
			g3.setVisibility(View.GONE);
			t3null.setVisibility(View.VISIBLE);

		} else {
			g3.setVisibility(View.VISIBLE);
			t3null.setVisibility(View.GONE);

			g3.setAdapter(new ContactAdapter(this, contactListOrg));
			g3.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					if (isable) {
						ScreenOrgInfo.isable = true;
					} else {
						ScreenOrgInfo.isable = false;
					}

					MyLog.d(TAG, "" + ScreenOrgInfo.isable);

					if (contactListOrg.size() > arg2) {
						ModelContact item = contactListOrg.get(arg2);

						if (mScreenService.show(ScreenOrgInfo.class,
								item.mobileNo)) {
						}
					}

					// finish();
				}

			});
		}

		if (contactListNet == null || contactListNet.size() == 0) {
			g4.setVisibility(View.GONE);
			t4null.setVisibility(View.VISIBLE);

		} else {
			g4.setVisibility(View.VISIBLE);
			t4null.setVisibility(View.GONE);

			g4.setAdapter(new ContactAdapter(this, contactListNet));
			g4.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					ScreenOrgInfo.isable = true;
					MyLog.d(TAG, "创建组织信息grdtgrt" + ScreenOrgInfo.isable);
					if (contactListNet.size() > arg2) {
						ModelContact item = contactListNet.get(arg2);
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

					// finish();
				}

			});

		}

		if (contactListDisp == null || contactListDisp.size() == 0) {
			g5.setVisibility(View.GONE);
			t5null.setVisibility(View.VISIBLE);

		} else {
			g5.setVisibility(View.VISIBLE);

			t5null.setVisibility(View.GONE);

			g5.setAdapter(new ContactAdapter(this, contactListDisp));
			g5.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					ScreenOrgInfo.isable = true;
					MyLog.d(TAG, "" + ScreenOrgInfo.isable);
					if (contactListDisp.size() > arg2) {
						Log.e("click!!!!!click!!!!!", arg2 + "");
						ModelContact item = contactListDisp.get(arg2);
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
		}

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.screen_mygroup);

		TextView t3null = (TextView) findViewById(R.id.screen_mygroup_listview_org_null);
		TextView t4null = (TextView) findViewById(R.id.screen_mygroup_listview_bussiness_null);
		TextView t5null = (TextView) findViewById(R.id.screen_mygroup_listview_publicgroup_null);

		ImageView back = (ImageView) findViewById(R.id.screen_mygroup_back);
		back.setOnClickListener(new OnClickListener() {

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

		contactListOrg.clear();

		contactListOrg.addAll(SystemVarTools.getContactOrg());

		contactListOrg = SystemVarTools.sortContactsByABC(contactListOrg);

		MyListView g3 = (MyListView) findViewById(R.id.screen_mygroup_listview_org);

		if (contactListOrg == null || contactListOrg.size() == 0) {
			g3.setVisibility(View.GONE);
			t3null.setVisibility(View.VISIBLE);
			if(contactListOrg == null){
				contactListOrg = new ArrayList<ModelContact>();
			}

		} else {
			g3.setVisibility(View.VISIBLE);
			t3null.setVisibility(View.GONE);

			g3.setAdapter(new ContactAdapter(this, contactListOrg));
			g3.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					if (isable) {
						ScreenOrgInfo.isable = true;
					} else {
						ScreenOrgInfo.isable = false;
					}

					MyLog.d(TAG, "" + ScreenOrgInfo.isable);

					if (contactListOrg.size() > arg2) {
						ModelContact item = contactListOrg.get(arg2);

						if (mScreenService.show(ScreenOrgInfo.class,
								item.mobileNo)) {
						}
					}

					// finish();
				}

			});
		}

		contactListNet.clear();
		contactListNet.addAll(SystemVarTools.getContactListBusinessOrg());

		contactListNet = SystemVarTools.sortContactsByABC(contactListNet);

		contactListDisp.clear();

		contactListDisp.addAll(SystemVarTools.getContactListGlobalGroupOrg());

		contactListDisp = SystemVarTools.sortContactsByABC(contactListDisp);

		MyListView g4 = (MyListView) findViewById(R.id.screen_mygroup_listview_bussiness);

		if (contactListNet == null || contactListNet.size() == 0) {
			g4.setVisibility(View.GONE);
			t4null.setVisibility(View.VISIBLE);
			if(contactListNet == null){
				contactListNet = new ArrayList<ModelContact>();
			}

		} else {
			g4.setVisibility(View.VISIBLE);
			t4null.setVisibility(View.GONE);

			g4.setAdapter(new ContactAdapter(this, contactListNet));
			g4.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					ScreenOrgInfo.isable = true;
					MyLog.d(TAG, "" + ScreenOrgInfo.isable);
					if (contactListNet.size() > arg2) {
						ModelContact item = contactListNet.get(arg2);
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

					// finish();
				}

			});

		}

		MyListView g5 = (MyListView) findViewById(R.id.screen_mygroup_listview_publicgroup);

		if (contactListDisp == null || contactListDisp.size() == 0) {
			g5.setVisibility(View.GONE);
			t5null.setVisibility(View.VISIBLE);
			if(contactListDisp == null){
				contactListDisp = new ArrayList<ModelContact>();
			}

		} else {
			g5.setVisibility(View.VISIBLE);

			t5null.setVisibility(View.GONE);
			;
			g5.setAdapter(new ContactAdapter(this, contactListDisp));
			g5.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {

					ScreenOrgInfo.isable = true;
					MyLog.d(TAG, "" + ScreenOrgInfo.isable);
					if (contactListDisp.size() > arg2) {
						MyLog.d(TAG, arg2 + "");
						ModelContact item = contactListDisp.get(arg2);
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
					ScreenTabContact_MyGroup.this.refresh();
				}
			}
		};
		//
		registerReceiver(bcreceiver, new IntentFilter(
				ServiceContact.CONTACT_REFRASH_MSG));

	}

}
