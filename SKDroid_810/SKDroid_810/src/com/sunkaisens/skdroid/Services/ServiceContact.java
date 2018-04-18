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
 * ����ͨѶ¼��صķ���
 * 
 * @author ZhichengGu
 * 
 */
public class ServiceContact {

	private static final String TAG = ServiceContact.class.getCanonicalName();

	public final static String PUBLICGROUP_TYPE = "public_group";
	
	/**
	 * rls���ĵ�Session id
	 * */
	public static long sessionID = 0;

	/**
	 * rls���ĵ�AcceptHeader
	 * */
	public static String auidForPresence = "";

	/**
	 * ȫ��������
	 */
	public final static int PUBLIC_GROUP = 1;

	/**
	 * ҵ����
	 */
	public final static int SERVICE_GROUP = 2;

	/**
	 * �����飨����̨���ڵ��飩
	 */
	public final static int GLOBAL_GROUP = 3;

	/**
	 * �����
	 */
	public final static int COMM_GROUP = 4;

	/**
	 * GIS��ʱ��
	 */
	public final static int GIS_GROUP = 5;

	/**
	 * ͨѶ¼ˢ��action
	 */
	public final static String CONTACT_REFRASH_MSG = "com.sunkaisens.screen.refresh";
	
	/**
	 * ���г�Ա�б�
	 */
	// public static List<ModelContactSubs> mContactAll = new
	// ArrayList<ModelContactSubs>();

	public static Map<String, ModelContactSubs> mContactAll = new HashMap<String, ModelContactSubs>();

	/**
	 * ȫ������Ⱥ����ڵ�
	 */
	private ModelNode mRootPublic = new ModelNode();

	/**
	 * ҵ������ڵ�
	 */
	private ModelNode mRootService = new ModelNode();

	/**
	 * ����̨Ⱥ����ڵ�
	 */
	private ModelNode mRootGlobal = new ModelNode();

	/**
	 * �������ڵ�
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
					MyLog.d(TAG, "sendSubscribe ���·��Ͷ���");

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

	// add by jgc 2014.12.12 ͨѶ¼����ǰ��ѯ�ʷ�����
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

	// add by jgc 2014.12.12 ����ͨѶ¼
	public static class ContactGetThread extends HandlerThread {

		private Handler mMainHandler;

		/**
		 * ȫ���������ؿ���
		 */
		private boolean canDownloadPublicGroup = false;

		/**
		 * ҵ�������ؿ���
		 */
		private boolean canDownloadServiceGroup = false;

		/**
		 * ��������ؿ���
		 */
		private boolean canDownloadCommGroup = false;

		/**
		 * ����̨���ؿ���
		 */
		private boolean canDownloadGlobalGroup = false;

		/**
		 * ���ĺ�Ⱥ�����ؿ���
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
				// SystemVarTools.showToast("��������ȫ��ͨѶ¼����");

				if (ScreenDownloadConcacts.getInstance().downloadPublicGroup()) {
					// ��������ϵ�˺��UI�̷߳������������Ϣ

					List<NodeResource> resList = ScreenDownloadConcacts
							.getInstance().parserContactsTree();
					SystemVarTools.setContactAll(resList);

					SystemVarTools.setContactOK(true);

					// ȫ����������֮���ȷ�һ��ˢ�£�����ʾͨѶ¼
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
				// SystemVarTools.showToast("��������ҵ��ͨѶ¼����");
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

					// Log.e("","���ĺ����С" + resListSubscribeGroup.size());

					if (resListSubscribeGroup != null
							&& resListSubscribeGroup.size() > 0) { // ����ж��ĺţ�������Ϣ������ʾ���ĺű�ǩ

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
	 * ��ȡȫ����������ڵ�
	 * 
	 * @return
	 */
	public ModelNode getmRootPublic() {
		return mRootPublic;
	}

	/**
	 * ��ȡҵ������ڵ�
	 * 
	 * @return
	 */
	public ModelNode getmRootService() {
		return mRootService;
	}

	/**
	 * rls���ģ��û���¼������ͨѶ¼֮����һ�Σ���ʾ���������û�
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
	 * ����,������
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

			// ��һ��������ͨѶ¼֮�󵽻�δ����rls����֮ǰsessionIDΪ0,
			// rls����֮��sessionID��Ϊ0������ÿ����һ������mContactAll������ϵ�˾���ͨѶ¼���������
			if (sessionID != 0) {

				MyLog.d(TAG, "�������ĳ�Ա:  " + "user-add/" + mc.mobileNo);

				NgnSubscriptionSession session = NgnSubscriptionSession
						.getSession(sessionID);
				session.addHeader("Accept", "user-add/" + mc.mobileNo);

				if (session != null) {
					try {
						Thread.sleep(10);
						session.subscribe();
						MyLog.e(TAG, "�������ĳ�Ա:  " + "user-add/" + mc.mobileNo
								+ "���Ľ���");

					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

			}
		}
		return true;
	}

	/**
	 * ͨѶ¼�������֮��ͳһ����
	 */
	public static void subscribeAllContact() {

		Iterator it = mContactAll.keySet().iterator();

		while (it.hasNext()) {

			String key = (String) it.next();
			// Log.d("", "keyֵ"+key);

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
	 * ɾ�ˣ���ȡ������
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

					MyLog.d(TAG, "ɾ�����ĳ�Ա:  " + "user-delete/" + mc.mobileNo);

					NgnSubscriptionSession session = NgnSubscriptionSession
							.getSession(sessionID);

					session.addHeader("Accept", "user-delete/" + mc.mobileNo);

					if (session != null) {
						try {
							Thread.sleep(10);
							session.subscribe();
							MyLog.e(TAG, "ɾ�����ĳ�Ա:  " + "user-delete/"
									+ mc.mobileNo + "���Ľ���");

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
	 * ��������ҵ����ʱ������ҵ�������г�Ա���ļ�¼
	 */
	public static void clearBusinessGroup() {
		MyLog.d("", "clearBusinessGroup()");

		List<ModelContact> BusAll = SystemVarTools.getContactListBusinessAll();
		for (int j = 0; j < BusAll.size(); ++j) {

			ServiceContact.deleteContact(BusAll.get(j));

		}
	}

	/**
	 * ����Ѷ��ĳ�Ա�б�
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
	 * ��ȡ����̨Ⱥ����ڵ�
	 * 
	 * @return
	 */
	public ModelNode getmRootGlobal() {
		return mRootGlobal;
	}

	/**
	 * ����ͨѶ¼ˢ�¹㲥
	 */
	public static void sendContactFrashMsg(){
		MyLog.d(TAG, "sendContactFrashMsg()");
		Intent intent = new Intent(CONTACT_REFRASH_MSG);
		SKDroid.getContext().sendBroadcast(intent);
	}
	
}
