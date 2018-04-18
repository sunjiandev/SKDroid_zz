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

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.utils.MyLog;

import android.app.LocalActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Services.ServiceRegiste;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.adapter.ContactAdapter;
import com.sunkaisens.skdroid.component.RefreshableView;
import com.sunkaisens.skdroid.component.RefreshableView.PullToRefreshListener;
import com.sunkaisens.skdroid.model.ModelContact;

public class ScreenTabContact extends BaseScreen {
	private static String TAG = ScreenTabContact.class.getCanonicalName();

	// ywh 下拉刷新控件
	public static RefreshableView refreshableView;

	private int viewheight = 12;
	private List<ModelContact> contactListAll = new ArrayList<ModelContact>();

	private ImageButton btnserch;

	public static int groupradioheight = 0;

	private boolean isShowOnline = false;
	private boolean isShowMe = false;

	LocalActivityManager lam;

	private String[] abcs = new String[] { "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z" };

	private ListView contactListView;

	private ContactAdapter contacAdapter;

	private View haederView;

	public static boolean isable = true;

	private static LinearLayout prossgressShow = null;

	public static Handler prossgressShowHandler = new Handler() {
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case Main.PROGRESS_GONE:
					if (prossgressShow != null)
						prossgressShow.setVisibility(View.GONE);
					break;
				default:
					break;

				}
			} catch (Exception e) {
				MyLog.d(TAG, e.getMessage());
			}
		}
	};

	public ScreenTabContact() {
		super(SCREEN_TYPE.TAB_INFO_T, TAG);

	}

	@Override
	protected void onStart() {
		super.onStart();
		MyLog.d(TAG, "ScreenTabContact onStart");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		MyLog.d(TAG, "ScreenTabContact onRestart");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MyLog.d(TAG, "ScreenTabContact OnDestroy");
	}

	@Override
	protected void onPause() {
		super.onPause();
		MyLog.d(TAG, "ScreenTabContact OnPause");
	}

	@Override
	protected void onResume() {
		super.onResume();
		SystemVarTools.updateContactRecent();
		MyLog.d(TAG, "ScreenTabContact OnResume");

		if (SystemVarTools.isDownContactFinished) {
			if (prossgressShow != null) {
				prossgressShow.setVisibility(View.GONE);

			}
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		MyLog.d(TAG, "ScreenTabContact OnStop");
	}

	@Override
	public boolean refresh() {

		MyLog.e(TAG, "ScreenTabContact refresh");

		if (!isShowOnline) {
			contactListAll.clear();
			contactListAll.addAll(SystemVarTools.getContactAll());

			contactListAll = SystemVarTools.sortContactsByABC(contactListAll);

			if (contacAdapter == null) {
				contacAdapter = new ContactAdapter(this, contactListAll);
			} else {
				contacAdapter.setList(contactListAll);
			}
			contactListView.setAdapter(contacAdapter);

		} else {
			contactListAll.clear();
			contactListAll.addAll(SystemVarTools.getContactAll());
			List<ModelContact> tempcontactListAll = new ArrayList<ModelContact>();
			for (int i = 0; i < contactListAll.size(); i++) {
				// Log.e(""+i, contactListAll.get(i).name);
				if (contactListAll.get(i).isOnline) { // 这块可以不用去Service.mContactAll查询
					tempcontactListAll.add(contactListAll.get(i));
				}
			}
			contactListAll.clear();
			contactListAll.addAll(tempcontactListAll);
			contactListAll = SystemVarTools.sortContactsByABC(contactListAll);

			if (contacAdapter == null) {
				contacAdapter = new ContactAdapter(this, contactListAll);
			} else {
				contacAdapter.setList(contactListAll);
			}
			contactListView.setAdapter(contacAdapter);
		}

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.screen_tab_contact);
		//
		MyLog.d(TAG, "ScreenTabContact oncreate");

		if (isable) {
			ScreenOrgInfo.isable = true;
		} else {
			ScreenOrgInfo.isable = false;
		}

		WindowManager wm = this.getWindowManager();
		int height = wm.getDefaultDisplay().getHeight();
		int heightdp = SystemVarTools.px2dip(getApplicationContext(), height);

		int Viewheightdp = (int) ((heightdp - 80 - 100) / 26);

		prossgressShow = (LinearLayout) findViewById(R.id.screen_tab_contact_prossgress_show);

		final TextView A = (TextView) findViewById(R.id.a);
		TextView B = (TextView) findViewById(R.id.b);
		TextView C = (TextView) findViewById(R.id.c);
		TextView D = (TextView) findViewById(R.id.d);
		TextView E = (TextView) findViewById(R.id.e);
		TextView F = (TextView) findViewById(R.id.f);
		TextView G = (TextView) findViewById(R.id.g);
		TextView H = (TextView) findViewById(R.id.h);
		TextView I = (TextView) findViewById(R.id.i);
		TextView J = (TextView) findViewById(R.id.j);
		TextView K = (TextView) findViewById(R.id.k);
		TextView L = (TextView) findViewById(R.id.l);
		TextView M = (TextView) findViewById(R.id.m);
		TextView N = (TextView) findViewById(R.id.n);
		TextView O = (TextView) findViewById(R.id.o);
		TextView P = (TextView) findViewById(R.id.p);
		TextView Q = (TextView) findViewById(R.id.q);
		TextView Rr = (TextView) findViewById(R.id.r);
		TextView S = (TextView) findViewById(R.id.s);
		TextView T = (TextView) findViewById(R.id.t);
		TextView U = (TextView) findViewById(R.id.u);
		TextView V = (TextView) findViewById(R.id.v);
		TextView W = (TextView) findViewById(R.id.w);
		TextView X = (TextView) findViewById(R.id.x);
		TextView Y = (TextView) findViewById(R.id.y);
		TextView Z = (TextView) findViewById(R.id.z);

		final ImageView abc_myself = (ImageView) findViewById(R.id.abc_myself);

		final ImageView abc_isonline = (ImageView) findViewById(R.id.abc_isonline);

		ViewTreeObserver vto = A.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				A.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				viewheight = A.getHeight();

			}
		});

		contactListView = (ListView) findViewById(R.id.screen_tab_contact_myGrid2);

		LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
		haederView = inflater.inflate(R.layout.screen_tab_contact_header, null);
		contactListView.addHeaderView(haederView);

		contactListView.setAdapter(new ContactAdapter(this, contactListAll));

		contactListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.i("ywh", "contactListView.setOnItemClickListener");
				// view.setBackgroundResource(R.color.color_contact_list_item);
				if (isable) {
					ScreenOrgInfo.isable = true;
				} else {
					ScreenOrgInfo.isable = false;
				}

				if (contactListAll.size() > position - 1) {
					ModelContact item = contactListAll.get(position - 1); // 还有一个为HeaderView

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

		LinearLayout myGroup = (LinearLayout) haederView
				.findViewById(R.id.screen_tab_contact_mygroup_layout_header);

		myGroup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Intent intent =new Intent(getApplicationContext(),
				// ScreenTabContact_MyGroup.class);
				// startActivity(intent);

				mScreenService.show(ScreenTabContact_MyGroup.class);

			}
		});

		LinearLayout mySubscribe = (LinearLayout) haederView
				.findViewById(R.id.screen_tab_contact_mysubscribe_layout_header);
		// LinearLayout
		// mypublicgroup=(LinearLayout)findViewById(R.id.screen_tab_contact_mypublicgroup_layout);
		mySubscribe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Intent intent =new Intent(getApplicationContext(),
				// ScreenTabContact_MyGroup.class);
				// startActivity(intent);

				mScreenService.show(ScreenTabContact_MySubscribe.class);

			}
		});

		A.setTextSize(viewheight);
		B.setTextSize(viewheight);
		C.setTextSize(viewheight);
		D.setTextSize(viewheight);
		E.setTextSize(viewheight);
		F.setTextSize(viewheight);
		G.setTextSize(viewheight);
		H.setTextSize(viewheight);
		I.setTextSize(viewheight);
		J.setTextSize(viewheight);
		K.setTextSize(viewheight);
		L.setTextSize(viewheight);
		M.setTextSize(viewheight);
		N.setTextSize(viewheight);
		O.setTextSize(viewheight);
		P.setTextSize(viewheight);
		Q.setTextSize(viewheight);
		Rr.setTextSize(viewheight);
		S.setTextSize(viewheight);
		T.setTextSize(viewheight);
		U.setTextSize(viewheight);
		V.setTextSize(viewheight);
		W.setTextSize(viewheight);
		X.setTextSize(viewheight);
		Y.setTextSize(viewheight);
		Z.setTextSize(viewheight);

		abc_myself.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!isShowMe) {
					isShowMe = true;
					abc_myself.setImageResource(R.drawable.abc_myself2);

					final INgnConfigurationService mConfigurationService = getEngine()
							.getConfigurationService();
					String accountString = mConfigurationService.getString(
							NgnConfigurationEntry.IDENTITY_IMPI,
							NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

					int index = 0;
					for (int i = 0; i < contactListAll.size(); i++) {
						// Log.e(""+i, contactListAll.get(i).name);
						if (contactListAll.get(i).mobileNo.equals(accountString
								.trim())) {
							index = i;
							break;
						}

					}

					if (contactListAll.size() <= index) {
						return;
					}
					contactListView.setSelection(index);
					contactListView.setSelected(true);

				} else {
					isShowMe = false;
					abc_myself.setImageResource(R.drawable.abc_myself);
					contactListView.setSelection(0);
					contactListView.setSelected(true);
				}

			}
		});

		abc_isonline.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				abc_myself.setImageResource(R.drawable.abc_myself);
				isShowMe = false;

				if (isShowOnline) {
					isShowOnline = false;
					abc_isonline.setImageResource(R.drawable.abc_isonlineno);
				} else {
					isShowOnline = true;
					abc_isonline.setImageResource(R.drawable.abc_isonlineyes);
					List<ModelContact> tempcontactListAll = new ArrayList<ModelContact>();
					for (int i = 0; i < contactListAll.size(); i++) {
						// Log.e(""+i, contactListAll.get(i).name);
						if (contactListAll.get(i).isOnline) { // 这块可以不用去Service.mContactAll查询
							tempcontactListAll.add(contactListAll.get(i));

						}

					}

					contactListAll.clear();
					contactListAll.addAll(tempcontactListAll);

				}

				refresh();
			}
		});

		A.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// showABC("A");

				String abc = abcs[0];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});

		B.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("B");
				String abc = abcs[1];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		C.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("C");
				String abc = abcs[2];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		D.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("D");
				String abc = abcs[3];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		E.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("E");
				String abc = abcs[4];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		F.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("F");
				String abc = abcs[5];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		G.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("G");
				String abc = abcs[6];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		H.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("H");
				String abc = abcs[7];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		I.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("I");
				String abc = abcs[8];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		J.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("J");
				String abc = abcs[9];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		K.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("K");
				String abc = abcs[10];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		L.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("L");
				String abc = abcs[11];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		M.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("M");
				String abc = abcs[12];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		N.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("N");
				String abc = abcs[13];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		O.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("O");
				String abc = abcs[14];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		P.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("P");
				String abc = abcs[15];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		Q.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("Q");
				String abc = abcs[16];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		Rr.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("R");
				String abc = abcs[17];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		S.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("S");
				String abc = abcs[18];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		T.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("T");
				String abc = abcs[19];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		U.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("U");
				String abc = abcs[20];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		V.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("V");
				String abc = abcs[21];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		W.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("W");
				String abc = abcs[22];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		X.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("X");
				String abc = abcs[23];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		Y.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("Y");
				String abc = abcs[24];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});
		Z.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showABC("Z");
				String abc = abcs[25];
				int index = SystemVarTools.getListItemPosByAbc(abc,
						contactListAll);
				if (contactListAll.size() <= index) {
					return;
				}
				contactListView.setSelection(index);
				contactListView.setSelected(true);
			}
		});

		BroadcastReceiver bcreceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				// Registration Event 服务器（实为本地代理CSCF-Proxy）对客户端注册动作的响应反馈：
				if (ServiceContact.CONTACT_REFRASH_MSG.equals(action)) {
					ScreenTabContact.this.refresh();
				}
			}
		};

		registerReceiver(bcreceiver, new IntentFilter(
				ServiceContact.CONTACT_REFRASH_MSG));

		btnserch = (ImageButton) findViewById(R.id.screen_tab_contact_search_bt);

		btnserch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				mScreenService.show(ScreenSearch.class);
			}
		});

		// ywh 找到RefreshableView控件
		refreshableView = (RefreshableView) findViewById(R.id.refreshable_view);
		// 为RefreshableView注册监听
		refreshableView.setOnRefreshListener(new PullToRefreshListener() {

			@Override
			public void onRefresh() {

				// contactListView.setPressed(false);
				// contactListView.setFocusable(false);
				// contactListView.setFocusableInTouchMode(false);

				Intent intent = new Intent();
				intent.setAction(MessageTypes.MSG_CONTACT_EVENT);
				intent.putExtra(MessageTypes.MSG_CONTACT_EVENT,
						ServiceRegiste.NET_DOWNLOAD_CONTACTS);
				ScreenTabContact.this.sendBroadcast(intent);

				// try {
				// Thread.sleep(3000);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				// // 收到刷新成功后通知RefreshableView刷新结束
				// refreshableView.finishRefreshing();
			}
		}, 0);

	}

}
