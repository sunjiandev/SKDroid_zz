package com.sunkaisens.skdroid.Services;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.model.NgnHistorySMSEvent;
import org.doubango.ngn.model.NgnHistoryEvent.StatusType;
import org.doubango.ngn.sip.NgnSubscriptionSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnDateTimeUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.R.integer;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.ScreenDownloadConcacts;
import com.sunkaisens.skdroid.Screens.ScreenTabContact;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.component.NodeResource;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.model.ModelContactSubs;
import com.sunkaisens.skdroid.model.ModelNode;
import com.sunkaisens.skdroid.util.GlobalVar;

/**
 * 处理通讯录相关的服务
 * 
 * @author ZhichengGu
 * 
 */
public class ServiceContact {

	private static final String TAG = ServiceContact.class.getCanonicalName();

	public final static String PUBLICGROUP_TYPE = "public_group";
	
	/**
	 * rls订阅的Session id
	 * */
	public static long sessionID = 0;

	/**
	 * rls订阅的AcceptHeader
	 * */
	public static String auidForPresence = "";

	/**
	 * 全网个呼组
	 */
	public final static int PUBLIC_GROUP = 1;

	/**
	 * 业务组
	 */
	public final static int SERVICE_GROUP = 2;

	/**
	 * 公共组（调度台所在的组）
	 */
	public final static int GLOBAL_GROUP = 3;

	/**
	 * 组呼组
	 */
	public final static int COMM_GROUP = 4;

	/**
	 * GIS临时组
	 */
	public final static int GIS_GROUP = 5;

	/**
	 * 通讯录刷新action
	 */
	public final static String CONTACT_REFRASH_MSG = "com.sunkaisens.screen.refresh";
	
	/**
	 * 所有成员列表
	 */
	// public static List<ModelContactSubs> mContactAll = new
	// ArrayList<ModelContactSubs>();

	public static Map<String, ModelContactSubs> mContactAll = new HashMap<String, ModelContactSubs>();

	/**
	 * 全网个呼群组根节点
	 */
	private ModelNode mRootPublic = new ModelNode();

	/**
	 * 业务组根节点
	 */
	private ModelNode mRootService = new ModelNode();

	/**
	 * 公共台群组根节点
	 */
	private ModelNode mRootGlobal = new ModelNode();

	/**
	 * 组呼组根节点
	 */
	private ModelNode mRootComm = new ModelNode();

	private static String mNetworkRealm = Engine
			.getInstance()
			.getConfigurationService()
			.getString(NgnConfigurationEntry.NETWORK_REALM,
					NgnConfigurationEntry.DEFAULT_NETWORK_REALM);

	public ServiceContact() {

	}

	public static void sendSubscribe() {
		try {
			new Thread(new Runnable() {

				@Override
				public void run() {
					SystemVarTools.isSubscribeSended = true;
					MyLog.d(TAG, "sendSubscribe 重新发送订阅");

					SystemVarTools.subscrebeContacts(SystemVarTools
							.getContactAll());
					SystemVarTools.subscrebeContacts(SystemVarTools
							.getContactListBusinessAll());
					SystemVarTools.subscrebeContacts(SystemVarTools
							.getContactListBusinessOrg());
					SystemVarTools.subscrebeContacts(SystemVarTools
							.getContactListCommGroupOrg());
					SystemVarTools.subscrebeContacts(SystemVarTools
							.getContactOrg());
					// SystemVarTools.subscribePublicAndServiceGroup();
					SystemVarTools.subscribePublicGroup();
					SystemVarTools.subscribeServiceGroup();
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// add by jgc 2014.12.12 通讯录下载前先询问服务器
	public static class ContactReqThread extends HandlerThread {

		private Handler mMainHandler;

		public ContactReqThread(String name, Handler handler) {
			super(name);
			mMainHandler = handler;
		}

		@Override
		public void run() {
			MyLog.d(TAG, "ContactReqThread.run()");
			MyLog.i(TAG, "Query the options of the contacts.");

			if (mMainHandler == null) {
				MyLog.i(TAG, "MainHandler is null, i can't notify,i will stop.");
				return;
			}

			if (ScreenDownloadConcacts.getInstance().ContactReq()) {
				if (GlobalVar.bADHocMode == false) {
					Message msg = Message.obtain(mMainHandler,
							MessageTypes.MSG_REQ_CONTACTS_SUCCESS);
					mMainHandler.sendMessage(msg);
				}
				MyLog.i(TAG, "Query success.");
			} else if (GlobalVar.bADHocMode == false) {
				Message msg = Message.obtain(mMainHandler,
						MessageTypes.MSG_REQ_CONTACTS_FAILED);
				mMainHandler.sendMessage(msg);
				MyLog.i(TAG, "Query failed.");
			}

			// super.run();
		}
	}

	// add by jgc 2014.12.12 下载通讯录
	public static class ContactGetThread extends HandlerThread {

		private Handler mMainHandler;

		/**
		 * 全网个呼下载开关
		 */
		private boolean canDownloadPublicGroup = false;

		/**
		 * 业务组下载开关
		 */
		private boolean canDownloadServiceGroup = false;

		/**
		 * 组呼组下载开关
		 */
		private boolean canDownloadCommGroup = false;

		/**
		 * 公共台下载开关
		 */
		private boolean canDownloadGlobalGroup = false;

		/**
		 * 订阅号群组下载开关
		 */
		private boolean canDownloadSubscribeGroup = false;

		public ContactGetThread(String name, Handler handler) {
			super(name);
			mMainHandler = handler;
		}

		@Override
		public void run() {

			MyLog.d(TAG, "ContactGetThread.run()");
			MyLog.i(TAG, "Query all kinds of the contacts.");

			if (mMainHandler == null) {
				MyLog.e(TAG, "MainHandler is null, i can't notify,i will stop.");
				return;
			}

			Looper.prepare();

			if (canDownloadPublicGroup == true) {
				// SystemVarTools.showToast("可以下载全部通讯录！！");

				if (ScreenDownloadConcacts.getInstance().downloadPublicGroup()) {
					// 下载完联系人后给UI线程发送下载完成消息

					List<NodeResource> resList = ScreenDownloadConcacts
							.getInstance().parserContactsTree();
					SystemVarTools.setContactAll(resList);

					SystemVarTools.setContactOK(true);

					// 全网个呼下完之后先发一个刷新，先显示通讯录
					Intent i = new Intent();
					i.setAction("com.sunkaisens.screen.refresh");
					NgnApplication.getContext().sendBroadcast(i);

					Message msg = Message.obtain(
							ScreenTabContact.prossgressShowHandler,
							Main.PROGRESS_GONE);
					ScreenTabContact.prossgressShowHandler.sendMessage(msg);
					SystemVarTools.isDownContactFinished = true;

					MyLog.i(TAG, "Public groups download ok.");
				} else {

					Message msg = Message.obtain(mMainHandler,
							MessageTypes.MSG_DOWNLOAD_CONTACTS_FAILED);
					mMainHandler.sendMessage(msg);

					msg = Message.obtain(
							ScreenTabContact.prossgressShowHandler,
							Main.PROGRESS_GONE);
					ScreenTabContact.prossgressShowHandler.sendMessage(msg);

					MyLog.e(TAG, "Public groups download failed.");
				}
			} else {

				Message msg = Message.obtain(
						ScreenTabContact.prossgressShowHandler,
						Main.PROGRESS_GONE);
				ScreenTabContact.prossgressShowHandler.sendMessage(msg);

				MyLog.e(TAG, "Can not download PublicGroups");
			}

			if (canDownloadServiceGroup == true) {
				// SystemVarTools.showToast("可以下载业务通讯录！！");
				if (ScreenDownloadConcacts.getInstance().downloadServiceGroup()) {

					List<NodeResource> resListNet = ScreenDownloadConcacts
							.getInstance().parserContactsNetTree();
					SystemVarTools.setContactBussiness(resListNet);

					// Message msg = Message.obtain(mMainHandler,
					// MessageTypes.MSG_DOWNLOAD_CONTACTSNET_FINISH);
					// mMainHandler.sendMessage(msg);

					MyLog.i(TAG, "Service groups download ok.");
				} else {
					// Message msg = Message.obtain(mMainHandler,
					// MessageTypes.MSG_DOWNLOAD_CONTACTSNET_FAILED);
					// mMainHandler.sendMessage(msg);
					MyLog.e(TAG, "Service groups download failed.");
				}
			} else {
				// Message msg = Message.obtain(mMainHandler,
				// MessageTypes.MSG_DOWNLOAD_CONTACTSNET_FAILED);
				// mMainHandler.sendMessage(msg);
				MyLog.e(TAG, "Can not download ServiceGroups");
			}

			if (canDownloadCommGroup == true) {
				if (ScreenDownloadConcacts.getInstance().downloadCommGroup()) {

					List<NodeResource> resListCommGroup = ScreenDownloadConcacts
							.getInstance().parserContactsCommGroupTree();
					SystemVarTools.setContactCommGroupOrg(resListCommGroup);

					// Message msg = Message.obtain(mMainHandler,
					// MessageTypes.MSG_DOWNLOAD_CONTACTSCOMMGROUP_FINISH);
					// mMainHandler.sendMessage(msg);

					MyLog.i(TAG, "Comm groups download ok.");
				} else {
					// Message msg = Message.obtain(mMainHandler,
					// MessageTypes.MSG_DOWNLOAD_CONTACTSCOMMGROUP_FAILED);
					// mMainHandler.sendMessage(msg);
					MyLog.e(TAG, "Comm groups download failed.");
				}
			} else {
				// Message msg = Message.obtain(mMainHandler,
				// MessageTypes.MSG_DOWNLOAD_CONTACTSCOMMGROUP_FAILED);
				// mMainHandler.sendMessage(msg);
				MyLog.e(TAG, "Can not download CommGroups.");
			}

			if (canDownloadGlobalGroup == true) {
				if (ScreenDownloadConcacts.getInstance().downloadGlobalGroup()) {

					List<NodeResource> resListGlobalGroup = ScreenDownloadConcacts
							.getInstance().parserContactsGlobalGroupTree();
					SystemVarTools.setContactGlobalGroupOrg(resListGlobalGroup);

					// Message msg = Message
					// .obtain(mMainHandler,
					// MessageTypes.MSG_DOWNLOAD_CONTACTSGLOBALGROUP_FINISH);
					// mMainHandler.sendMessage(msg);

					MyLog.i(TAG, "Global groups download ok.");
				} else {
					Message msg = Message
							.obtain(mMainHandler,
									MessageTypes.MSG_DOWNLOAD_CONTACTSGLOBALGROUP_FAILED);
					mMainHandler.sendMessage(msg);
					MyLog.e(TAG, "Global groups download failed.");
				}
			} else {
				// Message msg = Message.obtain(mMainHandler,
				// MessageTypes.MSG_DOWNLOAD_CONTACTSCOMMGROUP_FAILED);
				// mMainHandler.sendMessage(msg);
				MyLog.e(TAG, "Can not download GlobalGroups.");
			}

			if (canDownloadSubscribeGroup == true) {
				if (ScreenDownloadConcacts.getInstance()
						.downloadSubscribeGroup()) {

					List<NodeResource> resListSubscribeGroup = ScreenDownloadConcacts
							.getInstance().parserContactsSubscribeGroupTree();

					// Log.e("","订阅号组大小" + resListSubscribeGroup.size());

					if (resListSubscribeGroup != null
							&& resListSubscribeGroup.size() > 0) { // 如果有订阅号，则在消息界面显示订阅号标签

						NgnHistorySMSEvent event = new NgnHistorySMSEvent(
								"Subscribe", StatusType.Incoming, "Subscribe"); // 19800005001

						event.setStartTime(NgnDateTimeUtils.parseDate(
								NgnDateTimeUtils.now()).getTime());
						String tmpId = "" + new Date().getTime();
						event.setLocalMsgID(tmpId);

						event.setmLocalParty(SystemVarTools.getmIdentity());

						Engine.getInstance().getHistoryService()
								.addEvent(event);
					}

					SystemVarTools
							.setContactSubscribeGroupOrg(resListSubscribeGroup);

					// Message msg = Message
					// .obtain(mMainHandler,
					// MessageTypes.MSG_DOWNLOAD_CONTACTSSUBSCRIBEGROUP_FINISH);
					// mMainHandler.sendMessage(msg);

					MyLog.i(TAG, "Subscribe groups download ok.");
				} else {
					Message msg = Message
							.obtain(mMainHandler,
									MessageTypes.MSG_DOWNLOAD_CONTACTSSUBSCRIBEGROUP_FAILED);
					mMainHandler.sendMessage(msg);
					MyLog.e(TAG, "Subscribe groups download failed.");
				}
			} else {
				// Message msg = Message.obtain(mMainHandler,
				// MessageTypes.MSG_DOWNLOAD_CONTACTSCOMMGROUP_FAILED);
				// mMainHandler.sendMessage(msg);
				MyLog.e(TAG, "Can not download SubscribeGroups.");
			}

			mMainHandler.sendEmptyMessage(MessageTypes.MSG_DOWNLOAD_FINISH);

			// super.run();
			Looper.loop();
		}

		public void setCanDownloadCommGroup(boolean canDownloadCommGroup) {
			this.canDownloadCommGroup = canDownloadCommGroup;
		}

		public void setCanDownloadGlobalGroup(boolean canDownloadGlobalGroup) {
			this.canDownloadGlobalGroup = canDownloadGlobalGroup;
		}

		public void setCanDownloadPublicGroup(boolean canDownloadPublicGroup) {
			this.canDownloadPublicGroup = canDownloadPublicGroup;
		}

		public void setCanDownloadServiceGroup(boolean canDownloadServiceGroup) {
			this.canDownloadServiceGroup = canDownloadServiceGroup;
		}

		public void setCanDownloadSubscribeGroup(
				boolean canDownloadSubscribeGroup) {
			this.canDownloadSubscribeGroup = canDownloadSubscribeGroup;
		}

	}

	/**
	 * 获取全网个呼组根节点
	 * 
	 * @return
	 */
	public ModelNode getmRootPublic() {
		return mRootPublic;
	}

	/**
	 * 获取业务组根节点
	 * 
	 * @return
	 */
	public ModelNode getmRootService() {
		return mRootService;
	}

	/**
	 * rls订阅，用户登录下载完通讯录之后订阅一次，表示订阅所有用户
	 * 
	 * **/
	public static void subAll(String auidString) {
		MyLog.d(TAG, "subAll begin,session id :" + sessionID);

		MyLog.d(TAG, "subAll begin,AcceptHeader :" + auidString);

		String mIdentity = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_IMPI,
						NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);
		if (SystemVarTools.mIdentityChk != null
				&& !mIdentity.equals(SystemVarTools.mIdentityChk)) {
			mIdentity = SystemVarTools.mIdentityChk;
		}

		String uri = "sip:rls" + "@" + mNetworkRealm;
		String uriId = "sip:" + mIdentity + "@" + mNetworkRealm;
		NgnSubscriptionSession subscriptionSession = NgnSubscriptionSession
				.createOutgoingSession(Engine.getInstance().getSipService()
						.getSipStack(), uriId, uri,
						NgnSubscriptionSession.EventPackageType.Presence,
						auidString);
		subscriptionSession.subscribe();

		sessionID = subscriptionSession.getId();

		MyLog.d(TAG, "subAll session id: " + sessionID);

	}

	/**
	 * 加人,并订阅
	 * 
	 * @param mc
	 */
	public static boolean addContacts(ModelContact mc) {

		boolean result = false;
		if (mc == null) {
			MyLog.d(TAG, "Contact is null.");
			return result;
		}
		if (mc.mobileNo == null) {
			MyLog.d(TAG, "Contact's mobileNo is null.");
			return result;
		}
		if (mContactAll.keySet().contains(mc.mobileNo)) {
			mContactAll.get(mc.mobileNo).refCount++;

			MyLog.d(TAG, "mContactAll contain " + mc.mobileNo);

		} else {
			MyLog.d(TAG, "mContactAll do not contain " + mc.mobileNo);

			ModelContactSubs mcs = new ModelContactSubs();
			mcs.contact = mc;
			mcs.refCount++;

			mcs.subSessionId = sessionID;

			mContactAll.put(mc.mobileNo, mcs);

			// 第一次下载完通讯录之后到还未进行rls订阅之前sessionID为0,
			// rls订阅之后sessionID不为0，这是每增加一个不在mContactAll表中联系人均是通讯录变更产生的
			if (sessionID != 0) {

				MyLog.d(TAG, "新增订阅成员:  " + "user-add/" + mc.mobileNo);

				NgnSubscriptionSession session = NgnSubscriptionSession
						.getSession(sessionID);
				session.addHeader("Accept", "user-add/" + mc.mobileNo);

				if (session != null) {
					try {
						Thread.sleep(10);
						session.subscribe();
						MyLog.e(TAG, "新增订阅成员:  " + "user-add/" + mc.mobileNo
								+ "订阅结束");

					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

			}
		}
		return true;
	}

	/**
	 * 通讯录下载完成之后统一订阅
	 */
	public static void subscribeAllContact() {

		Iterator it = mContactAll.keySet().iterator();

		while (it.hasNext()) {

			String key = (String) it.next();
			// Log.d("", "key值"+key);

			ModelContact mc = mContactAll.get(key).contact;

			String mIdentity = Engine
					.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.IDENTITY_IMPI,
							NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);
			if (SystemVarTools.mIdentityChk != null
					&& !mIdentity.equals(SystemVarTools.mIdentityChk)) {
				mIdentity = SystemVarTools.mIdentityChk;
			}

			String uri = "sip:" + mc.mobileNo + "@" + mNetworkRealm;
			String uriId = "sip:" + mIdentity + "@" + mNetworkRealm;
			NgnSubscriptionSession subscriptionSession = NgnSubscriptionSession
					.createOutgoingSession(Engine.getInstance().getSipService()
							.getSipStack(), uriId, uri,
							NgnSubscriptionSession.EventPackageType.Presence);
			subscriptionSession.subscribe();

			mContactAll.get(key).subSessionId = subscriptionSession.getId();

		}
	}

	/**
	 * 删人，并取消订阅
	 * 
	 * @param mc
	 * @return
	 */
	public static boolean deleteContact(ModelContact mc) {
		boolean result = false;
		if (mc == null) {
			MyLog.d(TAG, "Contact is null.");
			return result;
		}
		if (mc.mobileNo == null) {
			MyLog.d(TAG, "Contact's mobileNo is null.");
			return result;
		}
		if (!mContactAll.keySet().contains(mc.mobileNo)) {
			MyLog.d(TAG, "This contact(" + mc.mobileNo
					+ ") has not been subscribed.");
		} else {
			ModelContactSubs mcs = mContactAll.get(mc.mobileNo);
			mcs.refCount--;
			if (mcs.refCount == 0) {
				mContactAll.remove(mc.mobileNo);

				if (sessionID != 0) {

					MyLog.d(TAG, "删除订阅成员:  " + "user-delete/" + mc.mobileNo);

					NgnSubscriptionSession session = NgnSubscriptionSession
							.getSession(sessionID);

					session.addHeader("Accept", "user-delete/" + mc.mobileNo);

					if (session != null) {
						try {
							Thread.sleep(10);
							session.subscribe();
							MyLog.e(TAG, "删除订阅成员:  " + "user-delete/"
									+ mc.mobileNo + "订阅结束");

						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

				}
			} else {
				MyLog.d(TAG, mc.mobileNo + " refCount is not 0");
			}

		}
		return true;
	}

	/**
	 * 重新下载业务组时，处理业务组所有成员订阅记录
	 */
	public static void clearBusinessGroup() {
		MyLog.d("", "clearBusinessGroup()");

		List<ModelContact> BusAll = SystemVarTools.getContactListBusinessAll();
		for (int j = 0; j < BusAll.size(); ++j) {

			ServiceContact.deleteContact(BusAll.get(j));

		}
	}

	/**
	 * 清空已订阅成员列表
	 * 
	 * @return
	 */
	public static boolean clearContacts() {
		MyLog.d(TAG, "clearContacts()");
		try {
			mContactAll.clear();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 获取公共台群组根节点
	 * 
	 * @return
	 */
	public ModelNode getmRootGlobal() {
		return mRootGlobal;
	}

	/**
	 * 发送通讯录刷新广播
	 */
	public static void sendContactFrashMsg(){
		MyLog.d(TAG, "sendContactFrashMsg()");
		Intent intent = new Intent(CONTACT_REFRASH_MSG);
		SKDroid.getContext().sendBroadcast(intent);
	}
	
}
