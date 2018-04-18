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
package com.sunkaisens.skdroid;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.NgnNativeService;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnMsrpSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnPredicate;
import org.doubango.tinyWRAP.tmedia_pref_video_size_t;
import org.doubango.utils.MyLog;

import com.sunkaisens.skdroid.Services.IServiceScreen;
import com.sunkaisens.skdroid.Services.ServiceScreen;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.app.service.NativeService;
import com.sunkaisens.skdroid.model.VERSION;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class Engine extends NgnEngine {
	private final static String TAG = Engine.class.getCanonicalName();

	private static final String CONTENT_TITLE = "SKDroid";

	private static final int NOTIF_AVCALL_ID = 19833892;
	private static final int NOTIF_SMS_ID = 19833893;
	private static final int NOTIF_APP_ID = 19833894;
	private static final int NOTIF_CONTSHARE_ID = 19833895;
	private static final int NOTIF_CHAT_ID = 19833896;
	private static final int NOTIF_PUSH_ID = 19833897;
	private static final int NOTIF_PTT_ID = 19833898;
	private static final int NOTIF_AVCALL_NOT_ID = 19833899;
	private static final int NOTIF_MAP_ID = 19833900;

	/**
	 * 通知栏GIS提示ID
	 */
	private static final int NOTIF_GIS_ID = 19833901;
	private static final String notifPTTTone = String.format(
			"android.resource://%s/", NgnApplication.getContext()
					.getPackageName());
	private IServiceScreen mScreenService;

	// 是否有短信通知
	private boolean hasSMSNotif = false;

	// 是否有推送通知
	private boolean hasPushNotif = false;

	public static NgnEngine getInstance() {
		if (sInstance == null) {
			sInstance = new Engine();
		}
		return sInstance;
	}

	public Engine() {
		super();
	}

	@Override
	public boolean start() {
		// 根据不同版本设定不同默认分辨率
		// 除ONLINE版本外,其它均为CIF
		// if (SKDroid.sks_version == VERSION.ONLINE) {
		// MyLog.d(TAG, "DEFAULT_QOS_PREF_VIDEO_SIZE : skd");
		// NgnConfigurationEntry.DEFAULT_QOS_PREF_VIDEO_SIZE =
		// tmedia_pref_video_size_t.tmedia_pref_video_size_skd.toString();
		// }
		// else{
		// MyLog.d(TAG, "DEFAULT_QOS_PREF_VIDEO_SIZE : cif");
		// NgnConfigurationEntry.DEFAULT_QOS_PREF_VIDEO_SIZE =
		// tmedia_pref_video_size_t.tmedia_pref_video_size_cif.toString();
		// }
		return super.start();

	}

	@Override
	public boolean stop() {
		return super.stop();
	}

	private void showNotification(int notifId, int drawableId, String tickerText) {
		if (!mStarted) {
			return;
		}
		// Set the icon, scrolling text and timestamp
		final Notification notification = new Notification(drawableId, "",
				System.currentTimeMillis());

		Intent intent = new Intent(SKDroid.getContext(), Main.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);

		switch (notifId) {
		case NOTIF_APP_ID:
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			intent.putExtra("notif-type", "reg");
			break;

		case NOTIF_CONTSHARE_ID:
			intent.putExtra("action", Main.ACTION_SHOW_CONTSHARE_SCREEN);
			notification.defaults |= Notification.DEFAULT_SOUND;
			break;

		case NOTIF_SMS_ID:
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.tickerText = tickerText;
			intent.putExtra("action", Main.ACTION_SHOW_SMS);
			break;

		case NOTIF_AVCALL_ID:
			tickerText = String.format("%s (%d)", tickerText,
					NgnAVSession.getSize());
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			intent.putExtra("action", Main.ACTION_SHOW_AVSCREEN);
			break;

		case NOTIF_CHAT_ID:
			notification.defaults |= Notification.DEFAULT_SOUND;
			tickerText = String.format("%s (%d)", tickerText,
					NgnMsrpSession.getSize(new NgnPredicate<NgnMsrpSession>() {
						@Override
						public boolean apply(NgnMsrpSession session) {
							return session != null
									&& NgnMediaType.isChat(session
											.getMediaType());
						}
					}));
			intent.putExtra("action", Main.ACTION_SHOW_CHAT_SCREEN);
			break;

		case NOTIF_PUSH_ID:
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.tickerText = tickerText;
			intent.putExtra("action", Main.ACTION_SHOW_PUSH);
			break;

		case NOTIF_AVCALL_NOT_ID:
			tickerText = String.format("%s (%d)", tickerText,
					SystemVarTools.avCallNotNumber);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			intent.putExtra("action", Main.ACTION_SHOW_CALLSCREEN);
			break;
		case NOTIF_MAP_ID:
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.tickerText = tickerText;
			intent.putExtra("action", Main.ACTION_SHOW_MAP);
			break;
		case NOTIF_GIS_ID:
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			break;
		default:

			break;
		}

		PendingIntent contentIntent = PendingIntent.getActivity(
				SKDroid.getContext(), notifId/* requestCode */, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(SKDroid.getContext(), CONTENT_TITLE,
				tickerText, contentIntent);

		// Send the notification.
		// We use a layout id because it is a unique number. We use it later to
		// cancel.
		mNotifManager.notify(notifId, notification);
	}

	public void showAppNotif(int drawableId, String tickerText) {
		Log.d(TAG, "showAppNotif");
		showNotification(NOTIF_APP_ID, drawableId, tickerText);
	}

	public void showAVCallNotif(int drawableId, String tickerText) {
		showNotification(NOTIF_AVCALL_ID, drawableId, tickerText);
	}

	public void cancelAVCallNotif() {
		if (!NgnAVSession.hasActiveSession()) {
			mNotifManager.cancel(NOTIF_AVCALL_ID);
		}
	}

	public void showAVCallNotNotif(int drawableId) {
		showNotification(NOTIF_AVCALL_NOT_ID, drawableId, getMainActivity()
				.getString(R.string.calling_not_response));
	}

	// gzc 20141015
	public void cancelAVCallNotNotif() {
		mNotifManager.cancel(NOTIF_AVCALL_NOT_ID);
		SystemVarTools.avCallNotNumber = 0;
		SystemVarTools.avCallNotNumberLast = 0;
	}

	public void refreshAVCallNotif(int drawableId) {
		if (!NgnAVSession.hasActiveSession()) {
			mNotifManager.cancel(NOTIF_AVCALL_ID);
			// mNotifManager.cancel(NOTIF_AVCALL_NOT_ID);
		}
	}

	public void showContentShareNotif(int drawableId, String tickerText) {
		showNotification(NOTIF_CONTSHARE_ID, drawableId, tickerText);
	}

	public void cancelContentShareNotif() {
		if (!NgnMsrpSession
				.hasActiveSession(new NgnPredicate<NgnMsrpSession>() {
					@Override
					public boolean apply(NgnMsrpSession session) {
						return session != null
								&& NgnMediaType.isFileTransfer(session
										.getMediaType());
					}
				})) {
			mNotifManager.cancel(NOTIF_CONTSHARE_ID);
		}
	}

	public void refreshContentShareNotif(int drawableId) {
		if (!NgnMsrpSession
				.hasActiveSession(new NgnPredicate<NgnMsrpSession>() {
					@Override
					public boolean apply(NgnMsrpSession session) {
						return session != null
								&& NgnMediaType.isFileTransfer(session
										.getMediaType());
					}
				})) {
			mNotifManager.cancel(NOTIF_CONTSHARE_ID);
		} else {
			showNotification(NOTIF_CONTSHARE_ID, drawableId, getMainActivity()
					.getString(R.string.content_shared));
		}
	}

	public void showContentChatNotif(int drawableId, String tickerText) {
		showNotification(NOTIF_CHAT_ID, drawableId, tickerText);
	}

	public void cancelChatNotif() {
		if (!NgnMsrpSession
				.hasActiveSession(new NgnPredicate<NgnMsrpSession>() {
					@Override
					public boolean apply(NgnMsrpSession session) {
						return session != null
								&& NgnMediaType.isChat(session.getMediaType());
					}
				})) {
			mNotifManager.cancel(NOTIF_CHAT_ID);
		}
	}

	public void refreshChatNotif(int drawableId) {
		if (!NgnMsrpSession
				.hasActiveSession(new NgnPredicate<NgnMsrpSession>() {
					@Override
					public boolean apply(NgnMsrpSession session) {
						return session != null
								&& NgnMediaType.isChat(session.getMediaType());
					}
				})) {
			mNotifManager.cancel(NOTIF_CHAT_ID);
		} else {
			showNotification(NOTIF_CHAT_ID, drawableId, "Chat");
		}
	}

	public void playNotificationTone()//
	{
		final Notification notification = new Notification();
		notification.sound = Uri.parse(notifPTTTone + R.raw.ptt_starting);
		mNotifManager.notify(NOTIF_PTT_ID, notification);
		Log.d("", "showPTTNotification: play PTT TONE ...");
	}

	public void playNotificationTone2()//
	{
		final Notification notification = new Notification();
		notification.sound = Uri.parse(notifPTTTone + R.raw.talkroom_begin);
		mNotifManager.notify(NOTIF_PTT_ID, notification);
		Log.d("", "showPTTNotification: play super PTT TONE ...");
	}

	public void showSMSNotif(int drawableId, String tickerText) {
		showNotification(NOTIF_SMS_ID, drawableId, tickerText);
		hasSMSNotif = true;
	}

	public void cancelSMSNotif() {
		// 如果有短消息通知则消除该通知
		if (hasSMSNotif == true) {
			mNotifManager.cancel(NOTIF_SMS_ID);
			hasSMSNotif = false;
		}
	}

	public void cancelUpdateNotify() {
		mNotifManager.cancel(R.layout.update_notify);
	}

	public IServiceScreen getScreenService() {
		if (mScreenService == null) {
			mScreenService = new ServiceScreen();
		}
		return mScreenService;
	}

	@Override
	public Class<? extends NgnNativeService> getNativeServiceClass() {
		return NativeService.class;
	}

	public void showPushNotif(int drawableId, String tickerText) {
		showNotification(NOTIF_PUSH_ID, drawableId, tickerText);
		hasPushNotif = true;
	}

	public void cancelPushNotif() {
		// 如果有推送消息通知则消除该通知
		if (hasPushNotif == true) {
			mNotifManager.cancel(NOTIF_PUSH_ID);
			hasPushNotif = false;
		}
	}

	public void showMapNotif(int drawableId, String tickerText) {
		showNotification(NOTIF_MAP_ID, drawableId, tickerText);
	}

	public void cancelMapNotif() {
		// //如果有推送消息通知则消除该通知
		mNotifManager.cancel(NOTIF_MAP_ID);
	}

	/**
	 * 显示GIS上报通知栏提示
	 * 
	 * @param drawableId
	 * @param tickerText
	 */
	public void showGISReportNotif(int drawableId, String tickerText) {
		showNotification(NOTIF_GIS_ID, drawableId, tickerText);
	}

	/**
	 * 取消GIS上报通知栏提示
	 */
	public void cancelGISReportNotif() {
		if (mNotifManager != null) {
			mNotifManager.cancel(NOTIF_GIS_ID);
		}
	}
}
