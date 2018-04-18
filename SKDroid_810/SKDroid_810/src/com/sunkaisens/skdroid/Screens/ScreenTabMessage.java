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

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.model.NgnHistoryPushEvent.HistoryEventPushFilter;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.model.NgnHistorySMSEvent.HistoryEventSMSFilter;
import org.doubango.ngn.model.NgnHistorySMSEvent.HistoryEventSMSIntelligentFilter;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnContactService;
import org.doubango.ngn.services.INgnHistoryService;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.utils.MyLog;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Utils.DateTimeUtils;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.model.ModelFileTransport;

public class ScreenTabMessage extends BaseScreen {
	private static String TAG = ScreenTabMessage.class.getCanonicalName();

	private static final int MENU_CLEAR_ALL_MESSSAGES = 1;
	private static final int MENU_CLEAR_CURRENT_MESSSAGES = 2;

	private final INgnHistoryService mHistoryService;
	private final INgnContactService mContactService;
	private static INgnConfigurationService mConfigurationService;

	private ImageButton mBNewSMS;
	private ListView mListView;
	private ScreenTabMessagesAdapter mAdapter;
	private ImageButton btnserch;

	// public static HashMap<String, String>mMessageDraftHashMap = new
	// HashMap<String, String>();//短消息草稿Hash表

	private int position = 0;
	private String mDelRemote = null;

	public ScreenTabMessage() {
		super(SCREEN_TYPE.TAB_MESSAGES_T, TAG);
		mConfigurationService = getEngine().getConfigurationService();
		mHistoryService = (INgnHistoryService) getEngine().getHistoryService();
		mContactService = (INgnContactService) getEngine().getContactService();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_tab_messages);

		MyLog.d(TAG, "onCreate()");

		mBNewSMS = (ImageButton) findViewById(R.id.screen_tab_message_add_bt);
		mBNewSMS.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				mScreenService.show(ScreenNewSMS.class);
			}
		});

		mAdapter = new ScreenTabMessagesAdapter(this);
		mListView = (ListView) findViewById(R.id.screen_tab_messages_listView);
		mListView.setAdapter(mAdapter);
		// mListView.setOnItemClickListener(mOnItemListViewClickListener);

		btnserch = (ImageButton) findViewById(R.id.screen_tab_message_search_bt);
		// searchedit = (EditText)
		// findViewById(R.id.screen_tab_contact_searchedit);

		btnserch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// if(searchedit.getVisibility()==View.GONE){
				// searchedit.setVisibility(View.VISIBLE);
				// }else {
				// searchedit.setVisibility(View.GONE);
				// }

				mScreenService.show(ScreenSearch.class);
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
					ScreenTabMessage.this.refresh();
				}
			}
		};

		registerReceiver(bcreceiver, new IntentFilter(
				ServiceContact.CONTACT_REFRASH_MSG));
	}

	@Override
	protected void onResume() {
		super.onResume();
		MyLog.d(TAG, "onResume()");

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
		mListView = (ListView) findViewById(R.id.screen_tab_messages_listView);
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

		if (remoteParty.endsWith("Subscribe")) {

			List<NgnHistoryEvent> events = mHistoryService
					.getObservableEvents().filter(new HistoryEventSMSFilter());

			int i = 0;
			while (i < events.size()) {
				NgnHistoryEvent event = events.get(i);
				if (event.getRemoteParty().equals(remoteParty)) {
					mHistoryService.deleteEvent(event);
				}
				i++;
			}

			mHistoryService.deleteEvents(new HistoryEventPushFilter());

		} else {

			List<NgnHistoryEvent> events = mHistoryService
					.getObservableEvents().filter(new HistoryEventSMSFilter());
			int i = 0;
			while (i < events.size()) {
				NgnHistoryEvent event = events.get(i);
				if (event.getRemoteParty().equals(remoteParty)) {
					mHistoryService.deleteEvent(event);
				}
				i++;
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		MyLog.d(TAG, "onContextItemSelected");

		if (mDelRemote != null) {
			switch (item.getItemId()) {
			case MENU_CLEAR_ALL_MESSSAGES:

				AlertDialog.Builder builder = new Builder(
						(Engine.getInstance()).getMainActivity());
				builder.setMessage(getString(R.string.delete_all_sessions));
				builder.setTitle(getString(R.string.tips));
				builder.setPositiveButton(getString(R.string.ok),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								mHistoryService
										.deleteEvents(new HistoryEventSMSFilter());
								mHistoryService
										.deleteEvents(new HistoryEventPushFilter()); // 删除全部时也删掉订阅号

								MyLog.d(TAG, "删除全部会话");

								ServiceContact.sendContactFrashMsg();

							}
						});
				builder.setNegativeButton(getString(R.string.cancel),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();

				break;
			case MENU_CLEAR_CURRENT_MESSSAGES:

				final String mDelRemoteString = mDelRemote;

				AlertDialog.Builder builder2 = new Builder(
						(Engine.getInstance()).getMainActivity());
				builder2.setMessage(getString(R.string.delete_current_session));
				builder2.setTitle(getString(R.string.tips));
				builder2.setPositiveButton(getString(R.string.ok),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								doDelete(mDelRemoteString);

								ServiceContact.sendContactFrashMsg();

								MyLog.d(TAG, "删除当前会话:" + mDelRemote);

							}
						});
				builder2.setNegativeButton(getString(R.string.cancel),
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder2.create().show();

				break;
			}
		}
		mDelRemote = null;
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (v != null && v.getTag() != null) {
			mDelRemote = (String) v.getTag();
			if (mDelRemote != null) {

				ModelContact userinfo = SystemVarTools
						.createContactFromRemoteParty(mDelRemote);

				if (mDelRemote.endsWith("Subscribe")) {
					menu.setHeaderTitle(getString(R.string.string_Contact_allContactSubscribe));
				} else {
					menu.setHeaderTitle(userinfo.name);
				}

				menu.addSubMenu(0, MENU_CLEAR_CURRENT_MESSSAGES, Menu.NONE,
						getString(R.string.delete_current));
				menu.addSubMenu(0, MENU_CLEAR_ALL_MESSSAGES, Menu.NONE,
						getString(R.string.delete_all));
				MyLog.e(TAG, "删除短信  NO=" + userinfo.mobileNo);

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
		private final ScreenTabMessage mBaseScreen;
		private final MyHistoryEventSMSIntelligentFilter mFilter;

		// private String draftString;

		ScreenTabMessagesAdapter(ScreenTabMessage baseSceen) {
			mBaseScreen = baseSceen;
			mHandler = new Handler();
			mInflater = LayoutInflater.from(mBaseScreen);
			mFilter = new MyHistoryEventSMSIntelligentFilter();
			mEvents = mBaseScreen.mHistoryService.getObservableEvents().filter(
					mFilter);
			MyLog.d(TAG,
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
			MyLog.d(TAG, "ScreenTabMessage adapter refresh()");
			mFilter.reset();
			mEvents = mBaseScreen.mHistoryService.getObservableEvents().filter(
					mFilter);
			MyLog.d(TAG, "observableEvents size:"
					+ mBaseScreen.mHistoryService.getObservableEvents()
							.countObservers());
			MyLog.d(TAG, "mEvents size:" + mEvents.size());

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
			// Log.d(TAG, "ScreenTabMessage adapter getView()");
			final NgnHistoryEvent event = (NgnHistoryEvent) getItem(position);
			if (event == null) {
				return null;
			}
			// if (view == null) {
			switch (event.getMediaType()) {
			case Audio:
			case AudioVideo:
			case FileTransfer:
			default:
				Log.e(TAG, "Invalid media type");
				return null;
			case SMS:
				view = mInflater.inflate(R.layout.screen_tab_messages_item,
						null);
				String remoteParty = event.getRemoteParty();

				if (remoteParty != null && remoteParty.equals("Subscribe")) {

					view.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							mBaseScreen.mScreenService
									.show(ScreenSubscribeMessage_list.class);
						}
					});
				} else {

					view.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							String remote = (String) v.getTag();
							if (remote != null) {
								ScreenChat.startChat(remote, true);

								((Engine) Engine.getInstance())
										.cancelSMSNotif(); // 消掉短消息通知
							}
						}
					});

				}
				break;

			}

			String remoteParty = event.getRemoteParty();

			view.setTag(remoteParty);

			// 取出保存的用户消息信息，根据flag 判断是否有未读消息，并改变item的背景颜色，完成未读信息标识功能
			boolean flag = mConfigurationService.getBoolean(remoteParty, false);

			if (flag) {
				view.setBackgroundColor(Color.GRAY);
			} else {
				view.setBackgroundColor(mBaseScreen.getResources().getColor(
						R.color.color_mainbg));
			}

			ModelContact mc = SystemVarTools
					.createContactFromRemoteParty(remoteParty);

			// draftString = mMessageDraftHashMap.get(mc.mobileNo);

			remoteParty = mc.name;

			// 头像和信息响应
			final ImageView imageicon = (ImageView) view
					.findViewById(R.id.icon);
			//

			// 联系人名称
			final TextView tvRemote = (TextView) view
					.findViewById(R.id.screen_tab_messages_item_textView_remote);

			// 接收短信的日期
			final TextView tvDate = (TextView) view
					.findViewById(R.id.screen_tab_messages_item_textView_date);

			// 短信的内容
			final TextView tvContent = (TextView) view
					.findViewById(R.id.screen_tab_messages_item_textView_content);

			final TextView tvCeneterRemote = (TextView) view
					.findViewById(R.id.screen_tab_messages_item_textView_center_remote);

			//
			final TextView tvUnSeen = (TextView) view
					.findViewById(R.id.screen_tab_messages_item_textView_unseen);

			final NgnHistorySMSEvent SMSEvent = (NgnHistorySMSEvent) event;

			if (remoteParty != null && remoteParty.equals("Subscribe")) {

				tvCeneterRemote.setText(mBaseScreen.getBaseContext().getString(
						R.string.string_Contact_allContactSubscribe));
				tvCeneterRemote.setVisibility(View.VISIBLE);

				// tvRemote.setText(mBaseScreen.getBaseContext().getString(
				// R.string.string_Contact_allContactSubscribe));
				tvRemote.setVisibility(View.GONE);
				tvRemote.setSelected(true);
				tvDate.setText(DateTimeUtils.getFriendlyDateString(new Date(
						event.getEndTime())));
				tvDate.setVisibility(View.GONE);
				imageicon.setImageResource(R.drawable.my_publicgroup);

			} else {
				tvCeneterRemote.setVisibility(View.GONE);

				tvRemote.setText(remoteParty);
				tvRemote.setVisibility(View.VISIBLE);
				tvRemote.setSelected(true);
				tvDate.setText(DateTimeUtils.getFriendlyDateString(new Date(
						event.getEndTime())));
				tvDate.setVisibility(View.VISIBLE);
				// imageicon.setImageResource(SystemVarTools
				// .getThumbID(mc.imageid));

				SystemVarTools.showicon(imageicon, mc,
						mBaseScreen.getApplicationContext());

			}
			final String SMSContent = SMSEvent.getContent();
			// wangds modify 2014.7.12,filetransfer show.

			ModelContact lastUser = null;

			if (SMSContent != null && SMSContent.startsWith("type:file")) {
				ModelFileTransport fileModel = new ModelFileTransport();
				fileModel.parseFileContent(SMSContent);
				String type = "";
				if (fileModel.name != null) {
					if (fileModel.name.endsWith(".jpg")
							|| fileModel.name.endsWith(".jpeg")
							|| fileModel.name.endsWith(".png")
							|| fileModel.name.endsWith(".bmp")
							|| fileModel.name.endsWith(".gif")) {
						type = ""
								+ mBaseScreen.getText(R.string.msg_file_image);
					} else if (fileModel.name.contains("tempaudio.amr")) {
						type = ""
								+ mBaseScreen.getText(R.string.msg_file_audio);
						;
					} else if (fileModel.name.contains("VideoRecorder.mp4")) {
						type = ""
								+ mBaseScreen.getText(R.string.msg_file_video);
						;
					} else {
						type = "" + mBaseScreen.getText(R.string.msg_file_file);
					}
				} else {
					type = "" + mBaseScreen.getText(R.string.msg_file_file);
				}
				String lastSpeaker = "";
				if (mc.isgroup) {
					if (SMSEvent.getGMMember() == null
							|| SMSEvent.getGMMember().equals("")) {
						lastUser = SystemVarTools
								.createContactFromRemoteParty(SMSEvent
										.getmLocalParty());
					} else {
						lastUser = SystemVarTools
								.createContactFromRemoteParty(SMSEvent
										.getGMMember());
					}
					lastSpeaker = lastUser.name;
					tvContent.setText(lastSpeaker + ": " + "[" + type + "]");
				} else {
					tvContent.setText("[" + type + "]");
				}
			} else {

				// if(draftString!=null && !draftString.equals("")){
				// tvContent.setText("[草稿]"+draftString);
				// }else{
				// tvContent.setText(NgnStringUtils.isNullOrEmpty(SMSContent) ?
				// NgnStringUtils.emptyValue() : SMSContent);
				// }

				if (SMSEvent.getIsDraft().equals("true")) {
					String lastSpeaker = "";
					if (mc.isgroup) {
						if (SMSEvent.getGMMember() == null
								|| SMSEvent.getGMMember().equals("")) {
							lastUser = SystemVarTools
									.createContactFromRemoteParty(SMSEvent
											.getmLocalParty());
						} else {
							lastUser = SystemVarTools
									.createContactFromRemoteParty(SMSEvent
											.getGMMember());
						}
						lastSpeaker = lastUser.name;
						tvContent.setText(NgnApplication.getContext()
								.getString(R.string.draft_with_bracket)
								+ lastSpeaker
								+ ": "
								+ SMSEvent.getDraftString());
					} else {
						tvContent.setText(NgnApplication.getContext()
								.getString(R.string.draft_with_bracket)
								+ SMSEvent.getDraftString());
					}

				} else {
					String lastSpeaker = "";
					String content = NgnStringUtils.isNullOrEmpty(SMSContent) ? NgnStringUtils
							.emptyValue() : SMSContent;
					if (mc.isgroup) {
						if (SMSEvent.getGMMember() == null
								|| SMSEvent.getGMMember().equals("")) {
							lastUser = SystemVarTools
									.createContactFromRemoteParty(SMSEvent
											.getmLocalParty());
						} else {
							lastUser = SystemVarTools
									.createContactFromRemoteParty(SMSEvent
											.getGMMember());
						}
						lastSpeaker = lastUser.name;
						tvContent.setText(lastSpeaker + ":" + content);
					} else {
						tvContent.setText(content);
					}
				}

			}

			List<NgnHistoryEvent> mEventUnSeen = mBaseScreen.mHistoryService
					.getObservableEvents().filter(
							new HistoryEventChatUnSeenFilter(mc.mobileNo));

			int unseenSize = mEventUnSeen.size();

			tvUnSeen.setText("" + unseenSize);

			if (unseenSize == 0) {
				tvUnSeen.setVisibility(View.GONE);
			} else if (unseenSize <= 99) {
				tvUnSeen.setVisibility(View.VISIBLE);
			} else {
				tvUnSeen.setVisibility(View.VISIBLE);
				tvUnSeen.setText("..");
			}

			mBaseScreen.registerForContextMenu(view);

			return view;
		}

		@Override
		public void update(Observable observable, Object data) {

			MyLog.d(TAG, "update()");

			refresh();
		}
	}

	//
	// MyHistoryEventSMSIntelligentFilter
	//
	static class MyHistoryEventSMSIntelligentFilter extends
			HistoryEventSMSIntelligentFilter {
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

	// 筛选未读短信
	public static class HistoryEventChatUnSeenFilter implements
			NgnPredicate<NgnHistoryEvent> {

		private String sRemoteParty = "";

		public HistoryEventChatUnSeenFilter(String remote) {
			this.sRemoteParty = remote;
		}

		@Override
		public boolean apply(NgnHistoryEvent event) {

			if (event != null
					&& event.getmLocalParty().equals(
							SystemVarTools.getmIdentity())
					&& (event.getMediaType() == NgnMediaType.SMS)) {

				NgnHistorySMSEvent SMSEvent = (NgnHistorySMSEvent) event;
				if (!SMSEvent.getIsDraft().equals("true")
						&& SMSEvent.getIsSeen().equals("false")) {
					return NgnStringUtils.equals(sRemoteParty,
							event.getRemoteParty(), false);
				}
			}
			return false;
		}
	}

}
