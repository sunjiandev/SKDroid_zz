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

import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryPushEvent;
import org.doubango.ngn.model.NgnHistoryPushEvent.HistoryEventPushFilter;
import org.doubango.ngn.model.NgnHistoryPushEvent.HistoryEventPushIntelligentFilter;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnContactService;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.utils.NgnStringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Utils.DateTimeUtils;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.ModelContact;

public class ScreenSubscribeMessage_list extends BaseScreen {
	private static String TAG = ScreenSubscribeMessage_list.class
			.getCanonicalName();

	private static final int MENU_CLEAR_ALL_MESSSAGES = 1;
	private static final int MENU_CLEAR_CURRENT_MESSSAGES = 2;

	private final INgnHistoryService mHistoryService;
	private final INgnContactService mContactService;
	private static INgnConfigurationService mConfigurationService;

	private ListView mListView;
	private ScreenTabMessagesAdapter mAdapter;

	// public static HashMap<String, String>mMessageDraftHashMap = new
	// HashMap<String, String>();//短消息草稿Hash表

	private int position = 0;

	public ScreenSubscribeMessage_list() {
		super(SCREEN_TYPE.TAB_MESSAGES_T, TAG);
		mConfigurationService = getEngine().getConfigurationService();
		mHistoryService = (INgnHistoryService) getEngine().getHistoryService();
		mContactService = (INgnContactService) getEngine().getContactService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_subscribe_list);
		Log.d(TAG, "onCreate()");

		mAdapter = new ScreenTabMessagesAdapter(this);
		mListView = (ListView) findViewById(R.id.screen_subscribe_list_listView);
		mListView.setAdapter(mAdapter);
		// mListView.setOnItemClickListener(mOnItemListViewClickListener);

		ImageView back = (ImageView) findViewById(R.id.screen_subscribe_list_linearLayout_top_back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

		// registerForContextMenu(mListView);//gzc 20141025
		//
		BroadcastReceiver bcreceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				// Registration Event 服务器（实为本地代理CSCF-Proxy）对客户端注册动作的响应反馈：
				if (ServiceContact.CONTACT_REFRASH_MSG.equals(action)) {
					ScreenSubscribeMessage_list.this.refresh();
				}
			}
		};

		registerReceiver(bcreceiver, new IntentFilter(
				ServiceContact.CONTACT_REFRASH_MSG));
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");
		refresh();
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
	public boolean refresh() {
		mListView = (ListView) findViewById(R.id.screen_subscribe_list_listView);
		if (mListView == null)
			return false;
		((ScreenTabMessagesAdapter) mListView.getAdapter())
				.notifyDataSetChanged();
		return true;
	}

	@Override
	public boolean hasMenu() {
		return true;
	}

	private void doDelete(String remoteParty) {
		List<NgnHistoryEvent> events = mHistoryService.getObservableEvents()
				.filter(new HistoryEventPushFilter());
		int i = 0;
		while (i < events.size()) {
			NgnHistoryEvent event = events.get(i);
			if (event.getRemoteParty().equals(remoteParty)) {
				mHistoryService.deleteEvent(event);
			}
			i++;
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		List<NgnHistoryEvent> events = mHistoryService.getObservableEvents()
				.filter(new HistoryEventPushFilter());
		Log.e(TAG, "events.size:" + events.size() + "|pos:" + position);
		NgnHistoryPushEvent event = (NgnHistoryPushEvent) events.get(position);
		if (event != null) {
			switch (item.getItemId()) {
			case MENU_CLEAR_ALL_MESSSAGES:
				mHistoryService.deleteEvents(new HistoryEventPushFilter());
				this.refresh();
				Log.d(TAG, "删除全部会话");
				break;
			case MENU_CLEAR_CURRENT_MESSSAGES:
				doDelete(event.getRemoteParty());
				this.refresh();
				Log.d(TAG, "删除当前会话");
				break;
			}
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v != null && v.getTag() != null) {
			int pos = (Integer) v.getTag();

			if (pos >= 0) {
				List<NgnHistoryEvent> events = mHistoryService
						.getObservableEvents().filter(
								new MyHistoryEventPushIntelligentFilter());
				if (events.size() > pos) {
					NgnHistoryEvent event = events.get(pos);
					if (event != null) {
						position = pos;

						ModelContact userinfo = SystemVarTools
								.createContactFromRemoteParty(event
										.getRemoteParty());
						menu.setHeaderTitle(userinfo.mobileNo);
						menu.addSubMenu(0, MENU_CLEAR_CURRENT_MESSSAGES,
								Menu.NONE, ScreenSubscribeMessage_list.this
										.getString(R.string.delete_current));
						menu.addSubMenu(0, MENU_CLEAR_ALL_MESSSAGES, Menu.NONE,
								ScreenSubscribeMessage_list.this
										.getString(R.string.delete_all));
						Log.e(TAG, "删除短信  NO=" + userinfo.mobileNo);
					}
				}
			}
		}

	}

	// private final OnItemClickListener mOnItemListViewClickListener = new
	// OnItemClickListener() {
	// public void onItemClick(AdapterView<?> parent, View view, int position,
	// long id) {
	// final NgnHistoryEvent event = (NgnHistoryEvent) parent
	// .getItemAtPosition(position);
	// if (event != null) {
	// ScreenChat.startChat(event.getRemoteParty(), true);
	// }
	// }
	// };

	/**
	 * ScreenTabMessagesAdapter
	 */
	static class ScreenTabMessagesAdapter extends BaseAdapter implements
			Observer {
		private List<NgnHistoryEvent> mEvents;
		private final LayoutInflater mInflater;
		private final Handler mHandler;
		private final ScreenSubscribeMessage_list mBaseScreen;
		private final MyHistoryEventPushIntelligentFilter mFilter;

		// private String draftString;

		ScreenTabMessagesAdapter(ScreenSubscribeMessage_list baseSceen) {
			mBaseScreen = baseSceen;
			mHandler = new Handler();
			mInflater = LayoutInflater.from(mBaseScreen);
			mFilter = new MyHistoryEventPushIntelligentFilter();
			mEvents = mBaseScreen.mHistoryService.getObservableEvents().filter(
					mFilter);
			Log.d(TAG,
					"ScreenTabMessagesAdapter mEcents size:" + mEvents.size());
			// add by gle
			/*
			 * for(int i=0;i<mEvents.size();i++) { String localNum =
			 * mEvents.get(i).getLocalParty(); String myLocalNum =
			 * SystemVarTools.getLocalParty(); if(!localNum.equals(myLocalNum))
			 * { mEvents.remove(i); i--; } }
			 */

			mBaseScreen.mHistoryService.getObservableEvents().addObserver(this);
		}

		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			mBaseScreen.mHistoryService.getObservableEvents().deleteObserver(
					this);
		}

		void refresh() {
			Log.d(TAG, "ScreenTabMessage adapter refresh()");
			mFilter.reset();
			mEvents = mBaseScreen.mHistoryService.getObservableEvents().filter(
					mFilter);
			Log.d(TAG, "observableEvents size:"
					+ mBaseScreen.mHistoryService.getObservableEvents()
							.countObservers());
			Log.d(TAG, "mEvents size:" + mEvents.size());
			// add by gle
			/*
			 * for(int i=0;i<mEvents.size();i++) { String localNum =
			 * mEvents.get(i).getLocalParty(); String myLocalNum =
			 * SystemVarTools.getLocalParty(); if(!localNum.equals(myLocalNum))
			 * { mEvents.remove(i); i--; } }
			 */

			if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
				notifyDataSetChanged();
			} else {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		}

		@Override
		public int getCount() {
			return mEvents.size();
		}

		@Override
		public Object getItem(int position) {
			return mEvents.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			Log.d(TAG, "ScreenTabMessage adapter getView()");
			final NgnHistoryEvent event = (NgnHistoryEvent) getItem(position);
			if (event == null) {
				return null;
			}
			switch (event.getMediaType()) {
			case Audio:
			case AudioVideo:
			case FileTransfer:

			case Push:
				view = mInflater.inflate(R.layout.screen_pushinfos_item, null);

				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = (Integer) v.getTag();
						final NgnHistoryEvent event = (NgnHistoryEvent) getItem(pos);
						if (event != null) {
							ScreenPushInfo.startPushInfo(
									event.getRemoteParty(), true);

							((Engine) Engine.getInstance()).cancelPushNotif(); // 消掉推送消息通知
						}
					}
				});
				view.setTag(position);

				String remoteParty = event.getRemoteParty();

				// 取出保存的用户消息信息，根据flag 判断是否有未读消息，并改变item的背景颜色，完成未读信息标识功能
				boolean flag = mConfigurationService.getBoolean(remoteParty,
						false);

				if (flag) {
					view.setBackgroundColor(Color.GRAY);
				} else {
					view.setBackgroundColor(mBaseScreen.getResources()
							.getColor(R.color.color_mainbg));
				}

				ModelContact mc = SystemVarTools
						.createContactFromRemoteParty(remoteParty);

				remoteParty = mc.name;

				// 头像和信息响应
				final ImageView imageicon = (ImageView) view
						.findViewById(R.id.screen_pushinfos_item_image);

				imageicon.setImageResource(SystemVarTools
						.getThumbID(mc.imageid));
				// 联系人名称
				final TextView tvRemote = (TextView) view
						.findViewById(R.id.screen_pushinfos_item_name);

				// 接收消息的时间
				final TextView tvDate = (TextView) view
						.findViewById(R.id.screen_pushinfos_item_time);

				// 消息的内容/摘要
				final TextView tvContent = (TextView) view
						.findViewById(R.id.screen_pushinfos_item_content);

				final NgnHistoryPushEvent pushEvent = (NgnHistoryPushEvent) event;

				tvRemote.setText(remoteParty);
				tvDate.setText(DateTimeUtils.getFriendlyDateString(new Date(
						event.getStartTime())));
				String pushContent = pushEvent.getTitle();
				if (pushContent == null || pushContent.equals("")) {
					pushContent = mBaseScreen.getApplicationContext()
							.getString(R.string.notitle);
				}

				tvContent
						.setText(NgnStringUtils.isNullOrEmpty(pushContent) ? NgnStringUtils
								.emptyValue() : pushContent);

				mBaseScreen.registerForContextMenu(view);

				return view;

			default:
				Log.e(TAG, "Invalid media type");
				return null;

			}

		}

		@Override
		public void update(Observable observable, Object data) {
			Log.d(TAG, "update()");
			refresh();
		}
	}

	static class MyHistoryEventPushIntelligentFilter extends
			HistoryEventPushIntelligentFilter {
		private int mUnSeen;

		int getUnSeen() {
			return mUnSeen;
		}

		@Override
		protected void reset() {
			super.reset();
			mUnSeen = 0;
		}

		@Override
		public boolean apply(NgnHistoryEvent event) {
			if (super.apply(event)) {
				mUnSeen += event.isSeen() ? 0 : 1;
				return true;
			}
			return false;
		}
	}
}
