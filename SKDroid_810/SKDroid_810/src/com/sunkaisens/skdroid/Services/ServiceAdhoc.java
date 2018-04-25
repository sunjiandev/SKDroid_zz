package com.sunkaisens.skdroid.Services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnNativeService;
import org.doubango.ngn.events.AdhocSessionEventArgs;
import org.doubango.ngn.events.AdhocSessionEventTypes;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.sip.NgnMediaSession;
import org.doubango.ngn.sip.NgnMediaSession.NgnMediaSessionState;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.utils.MyLog;

import com.sks.adhoc.service.CommandType;
import com.sks.adhoc.service.SKSPerson;
import com.sks.net.socket.message.BaseSocketMessage;
import com.sks.net.socket.server.ServerMsgReceiver;
import com.sks.net.socket.server.SocketEventCallback;
import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Screens.ScreenAV;
import com.sunkaisens.skdroid.Screens.ScreenMediaAV;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.component.NodeResource;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall.PTTState;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

public class ServiceAdhoc {
	private static final String TAG = ServiceAdhoc.class.getCanonicalName();
	public ConcurrentHashMap<String, SKSPerson> childrenMap = new ConcurrentHashMap<String, SKSPerson>();// ��ǰ�����û�

	private List<Map> backUpContact = new ArrayList<Map>();// ��������ϵ�˱���

	private String localIp = null;
	private static SKSPerson me = null;// ������������������Ϣ
	private AdhocCommunication adhocCom = null;// ͨѶ��Э�����ģ��
	private UpdateMe mUpdateMe = null;
	private CheckUserOnline mCheckUserOnline = null;
	private List<NodeResource> list;
	boolean isStopUpdateMe = false;
	private Handler mAdhocHandler = null;
	private static ServiceAdhoc instance = null;
	private BroadcastReceiver broadcastReceiver;
	private boolean isStartOK = false;

	public SocketEventCallback mSocketCallback;

	public ServiceAdhoc() {

		list = new ArrayList<NodeResource>();
		childrenMap.clear();
		localIp = Engine.getInstance().getNetworkService().getLocalIP(false);
		Log.d("ServiceAdhoc", "ServiceAdhoc - ServiceAdhoc()");
		Log.d("ServiceAdhoc", "ServiceAdhoc - localIp = " + localIp);

		broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();

				if (action.equals(CommandType.personOnLine)) {
					List<NodeResource> resList = parserContactsTree();
					SystemVarTools.setContactAll(resList);
					ServiceContact.sendContactFrashMsg();
					SystemVarTools.showToast(NgnApplication.getContext()
							.getString(R.string.user_on_line) + resList.size());
					// Toast.makeText(NgnApplication.getContext(),
					// "���û�����!"+resList.size(), Toast.LENGTH_SHORT).show();
					if (GlobalSession.bSocketService) {
						Intent it = new Intent();
						it.setAction(ServerMsgReceiver.MESSAGE_SOCKET_INTENT);
						it.putExtra("type",
								ServerMsgReceiver.MSG_S_ADHOC_REFRESH);
						it.putExtra("pid", GlobalVar.mMyPid);
						// NgnApplication.getContext().sendBroadcast(it);
						if (mSocketCallback != null) {
							mSocketCallback.onSocketEvent(it);
						}
					}
				} else if (action.equals(CommandType.personOffLine)) {
					List<NodeResource> resList = parserContactsTree();
					SystemVarTools.setContactAll(resList);
					ServiceContact.sendContactFrashMsg();
					SystemVarTools
							.showToast(NgnApplication.getContext().getString(
									R.string.user_off_line)
									+ resList.size());
					// Toast.makeText(NgnApplication.getContext(),
					// "���û�����!"+resList.size(), Toast.LENGTH_SHORT).show();
					if (GlobalSession.bSocketService) {
						Intent it = new Intent();
						it.setAction(ServerMsgReceiver.MESSAGE_SOCKET_INTENT);
						it.putExtra("type",
								ServerMsgReceiver.MSG_S_ADHOC_REFRESH);
						it.putExtra("pid", GlobalVar.mMyPid);
						// NgnApplication.getContext().sendBroadcast(it);
						if (mSocketCallback != null) {
							mSocketCallback.onSocketEvent(it);
						}
					}
				}
			}

		};

		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CommandType.personOnLine);
		intentFilter.addAction(CommandType.personOffLine);
		NgnApplication.getContext().registerReceiver(broadcastReceiver,
				intentFilter);

	}

	public boolean isStartOK() {
		return isStartOK;
	}

	public void StartAdhoc() {
		if (isStartOK)
			return;
		getMyInfomation();// ���������Ϣ

		adhocCom = new AdhocCommunication(NgnApplication.getContext()
				.getString(R.string.login_adhoc));// ����socket����
		adhocCom.start();

		mUpdateMe = new UpdateMe();
		mUpdateMe.start();// �����緢������������ע��

		mCheckUserOnline = new CheckUserOnline();
		mCheckUserOnline.start();// ����û��б��Ƿ��г�ʱ�û�
		Log.d("ServiceAdhoc", "GLE---Service started...");
		isStartOK = true;
	}

	public static ServiceAdhoc getInstance() {
		if (instance == null) {
			instance = new ServiceAdhoc();
		}
		//
		return instance;
	}

	public void StopAdhoc() {
		isStartOK = false;

		isStopUpdateMe = true;
		if (childrenMap != null) {
			childrenMap.clear();
		}
		if (adhocCom != null) {
			adhocCom.release();
			adhocCom = null;
		}

		backUpContact.clear();

		me = null;
		instance = null;

		Log.d("ServiceAdhoc", "GLE---Service on destory...");
	}

	// ������ѵ������Ϣ
	private void getMyInfomation() {
		String nickName = GlobalVar.displayname;
		String mobileNo = GlobalVar.account;

		if (nickName == null || nickName.isEmpty() || mobileNo == null
				|| mobileNo.isEmpty()) {
			Log.e("ServiceAdhoc", "nickName=" + nickName + "," + "mobileNo="
					+ mobileNo);
			return;
		}
		long currentTime = System.currentTimeMillis();
		if (null == me) {
			localIp = Engine.getInstance().getNetworkService()
					.getLocalIP(false); // ���ն˰汾��Ҫ�ٴλ�ȡip������Ϊnull��
			Log.d("ServiceAdhoc", "ServiceAdhoc - getMyInfomation()");
			Log.d("ServiceAdhoc", "ServiceAdhoc - localIp = " + localIp);
			// String localUserID = "UE" +
			// java.util.UUID.randomUUID().toString();
			// String personNickeName,String mobile,String ipAddress,long
			// loginTime
			me = new SKSPerson(localIp, nickName, mobileNo, currentTime);
			me.setHeartbeatTime(0);

			sendPersonHasChangedBroadcast(CommandType.personOnLine);
			((Engine) Engine.getInstance()).showAppNotif(
					R.drawable.bullet_ball_glass_green_16, NgnApplication
							.getContext().getString(R.string.login));
		} else {
			Log.d("", "logingTime= " + me.getLoginTime() + "--currentTime="
					+ currentTime);
		}
	}

	// ��������û�����
	private List<NodeResource> parserContactsTree() {
		if (list != null) {
			list.clear();
		} else {
			list = new ArrayList<NodeResource>();
		}
		int mapNum = childrenMap.size();

		// String index, String uri, String displayName, String number,int
		// iconId
		list.add(new NodeResource(me.getIpAddress(), me.getPersonNickeName(),
				"sip:" + me.getMobileNo() + "@" + me.getIpAddress(), me
						.getMobileNo(), me.isGroupLeader()));
		if (backUpContact.size() > 0) {
			@SuppressWarnings("unchecked")
			ConcurrentHashMap<String, SKSPerson> contactMap = (ConcurrentHashMap<String, SKSPerson>) backUpContact
					.get(0);
			Set<String> keys = contactMap.keySet();
			for (String key : keys) {
				SKSPerson person = contactMap.get(key);
				NodeResource nr = new NodeResource(person.getIpAddress(),
						person.getPersonNickeName(), "sip:"
								+ person.getMobileNo() + "@"
								+ person.getIpAddress(), person.getMobileNo(),
						person.isGroupLeader());
				list.add(nr);
			}
		}
		return list;
	}

	private class UpdateMe extends Thread {
		@Override
		public void run() {
			while (!isStopUpdateMe) {
				try {
					adhocCom.joinOrganization();
					if (!backUpContact.contains(childrenMap)) {// 5��ͬ��һ������
						backUpContact.clear();
						backUpContact.add(childrenMap);
						MyLog.d(TAG, "��ʼͬ������");
					} else {
						MyLog.d(TAG, "�����Ѿ�ͬ��");
					}

					sleep(5000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	// ����û��Ƿ����ߣ��������15��˵���û������ߣ�����б���������û�
	private class CheckUserOnline extends Thread {
		@Override
		public void run() {
			super.run();

			while (!isStopUpdateMe) {
				boolean hasChanged = false;
				if (childrenMap.size() > 0) {
					Set<String> keys = childrenMap.keySet();
					for (String key : keys) {
						long heartbeatTime = childrenMap.get(key)
								.getHeartbeatTime();
						// Log.d("","each heartbeatTime ="+heartbeatTime);
						if (heartbeatTime == 0)
							continue;
						if (childrenMap.get(key).getIpAddress()
								.equals(me.getIpAddress()))
							continue;
						long detaTime = System.currentTimeMillis()
								- heartbeatTime;
						// Log.d("","heartbeatTime current ="+System.currentTimeMillis()+"-"+heartbeatTime+"="+detaTime+"personId"+key);
						if (detaTime > CommandType.MAX_DELT) {
							childrenMap.remove(key);
							// Log.d("","heartbeatTime remove!");
							hasChanged = true;
						}
					}
				}
				if (hasChanged) {
					sendPersonHasChangedBroadcast(CommandType.personOffLine);

				}
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// �����û����¹㲥
	private void sendPersonHasChangedBroadcast(String personState) {
		Intent intent = new Intent();
		intent.setAction(personState);
		NgnApplication.getContext().sendBroadcast(intent);
	}

	// ========================Э�������ͨѶģ��=======================================================
	// private DatagramSocket DatagramSocket = null; HandlerThread
	private class AdhocCommunication extends Thread {
		private DatagramSocket socketReceive = null;
		private byte[] recvBuffer = null;

		public AdhocCommunication(String name) {
			super(name);
		}

		// ���鲥�˿ڣ�׼���鲥ͨѶ
		@Override
		public void run() {
			super.run();
			try {
				socketReceive = new DatagramSocket(CommandType.RECV_PORT);
				// socketReceive.setBroadcast(true);
				Log.d("ServiceAdhoc", "GLE---Socket started...");
				while (!socketReceive.isClosed() && null != socketReceive) {
					recvBuffer = new byte[CommandType.bufferSize];
					DatagramPacket rdp = new DatagramPacket(recvBuffer,
							recvBuffer.length);
					socketReceive.receive(rdp);
					Log.d("ServiceAdhoc", "�յ��µ����ݰ�");
					Log.d("adhoc", "adhoc content:" + new String(rdp.getData()));
					Log.d("adhoc", "adhoc host:"
							+ rdp.getAddress().getHostAddress());
					parsePackage(recvBuffer);
				}

			} catch (Exception e) {
				try {
					if (null != socketReceive && !socketReceive.isClosed()) {
						socketReceive.close();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}

		}

		// �������յ������ݰ�
		private void parsePackage(byte[] pkg) throws OptionalDataException,
				ClassNotFoundException, IOException {

			SKSPerson person = new SKSPerson(new String(pkg));
			int cmdType = person.cmdType;
			Log.d("SKSPerson", "cmdType : " + cmdType);
			switch (cmdType) {
			case CommandType.CMD_TYPE1:
				// �������Ϣ�����Լ���������Է����ͻ�Ӧ��,���ѶԷ������û��б�
				if (person.getIpAddress().equals(me.getIpAddress()) == false) {
					addPerson(person);
					// ����Ӧ���
					sendMessage(CommandType.CMD_TYPE2, person.getIpAddress());
				} else {// �����Լ�����������ʵ����ν��������ʱ���ǶԽ��շ���Ч��
					person.setHeartbeatTime(System.currentTimeMillis());
				}
				break;
			case CommandType.CMD_TYPE2:
				addPerson(person);
				break;
			case CommandType.CMD_TYPE3:
				childrenMap.remove(person.getIpAddress());
				sendPersonHasChangedBroadcast(CommandType.personOffLine);
				break;
			case CommandType.CMD_NORMAL_GROUP_AUDIOCALL:// �����յ��������
				Log.d(TAG, "Recv: CMD_NORMAL_GROUP_AUDIOCALL");
				if (person.getIpAddress().equals(me.getIpAddress()) == false) {
					Log.d(TAG,
							String.format("Callee:%d; Caller:%d",
									me.getCalleeGroupNumber(),
									person.getCalleeGroupNumber()));

					if (me.getCalleeGroupNumber() == person
							.getCalleeGroupNumber() && mMediaSession == null) {
						Log.d(TAG,
								String.format("Get audioPort = %d",
										person.getAudioPort()));
						if (mMediaSession != null
								&& me.getLastRecCmd() == CommandType.CMD_NORMAL_GROUP_AUDIOCALL) {
							Log.d(TAG,
									"Already handle: CMD_NORMAL_GROUP_AUDIOCALL ! ");
							return;
						}
						final NgnMediaSession mediaSession = NgnMediaSession
								.takeIncommingSession(me.getIpAddress(),
										person.getIpAddress(),
										NgnMediaType.Audio,
										SessionType.GroupAudioCall,
										person.getAudioPort(),
										person.getVideoPort());
						mediaSession
								.setSessionState(NgnMediaSessionState.CONNECTED);
						mediaSession.startSession();
						mMediaSession = mediaSession;

						Log.d(TAG, "BroadCast: CONNECTED mediaSession id:"
								+ mediaSession.getId() + ", mediaSession"
								+ mediaSession);

						ServiceAV.receiveCall(mediaSession);

						// ����
						sendSessionStateBroadcast(new AdhocSessionEventArgs(
								AdhocSessionEventTypes.INCOMING,
								NgnMediaType.Audio, mediaSession.getId(),
								CommandType.NORMAL_GROUP_AUDIO_CALL,
								PTTState.NONE));
						me.setLastRecCmd(CommandType.CMD_NORMAL_GROUP_AUDIOCALL);

						// sendP2PMessage(CommandType.CMD_NORMAL_AUDIOCALL_ACCEPT,person.getIpAddress(),0,0);
						sendBroadCastMessage(
								CommandType.CMD_NORMAL_AUDIOCALL_ACCEPT, 0, 0);

					}

				} else {
					// sendMessage(CommandType.CMD_AUDIOCALL_REGECT,person.getIpAddress());
				}
				break;
			case CommandType.CMD_NORMAL_AUDIOCALL_ACCEPT:// �����յ��϶��ظ�
				Log.d(TAG, "Recv: CMD_NORMAL_AUDIOCALL_ACCEPT");
				// Log.d(TAG,String.format("Caller:%d; Callee:%d",
				// me.getCalleeGroupNumber(),person.getCalleeGroupNumber()));
				if (person.getIpAddress().equals(me.getIpAddress()) == false
						&& person.getCalleeGroupNumber() == me
								.getCalleeGroupNumber()) {// the same Group

					// Log.d(TAG,String.format("Caller:%d; Callee:%d",
					// me.getCalleeGroupNumber(),person.getCalleeGroupNumber()));

					if (mMediaSession.getSessionState() == NgnMediaSessionState.INPROGRESS) {
						if (me.getLastRecCmd() == CommandType.CMD_NORMAL_AUDIOCALL_ACCEPT) {
							Log.d(TAG,
									"Already handle: CMD_NORMAL_AUDIOCALL_ACCEPT ! ");
							return;
						}
						mMediaSession
								.setSessionState(NgnMediaSessionState.CONNECTED);
						mMediaSession.startSession();
						Log.d(TAG, "BroadCast: CONNECTED mediaSession id:"
								+ mMediaSession.getId() + ", mediaSession"
								+ mMediaSession);
						// ����
						ServiceAV.receiveCall(mMediaSession);

						sendSessionStateBroadcast(new AdhocSessionEventArgs(
								AdhocSessionEventTypes.INCALL,
								NgnMediaType.Audio, mMediaSession.getId(),
								CommandType.NORMAL_GROUP_AUDIO_CALL,
								PTTState.NONE));
						me.setLastRecCmd(CommandType.CMD_NORMAL_AUDIOCALL_ACCEPT);
					}
				}
				break;
			case CommandType.CMD_NORMAL_SESSION_HUNGUP:
				Log.d(TAG, "Recv: CMD_NORMAL_SESSION_HUNGUP");
				if (person.getIpAddress().equals(me.getIpAddress()) == false
						&& person.getCalleeGroupNumber() == me
								.getCalleeGroupNumber()) { // the same Group

					Log.d(TAG,
							String.format("Callee:%d; Caller:%d",
									me.getCalleeGroupNumber(),
									person.getCalleeGroupNumber()));
					if (mMediaSession != null) {
						// �Ҷ�
						if (me.getLastRecCmd() == CommandType.CMD_NORMAL_SESSION_HUNGUP) {
							MyLog.d(TAG,
									"Already handle: CMD_NORMAL_SESSION_HUNGUP ! ");
							return;
						}
						me.setLastRecCmd(CommandType.CMD_NORMAL_SESSION_HUNGUP);
						long sessionid = mMediaSession.getId();
						Log.d(TAG, "BroadCast: TERMINATED mediaSession id:"
								+ mMediaSession.getId() + ", mediaSession"
								+ mMediaSession);
						mMediaSession
								.setSessionState(NgnMediaSessionState.TERMINATED);
						sendSessionStateBroadcast(new AdhocSessionEventArgs(
								AdhocSessionEventTypes.INCOMING,
								NgnMediaType.Audio, sessionid,
								CommandType.NORMAL_GROUP_AUDIO_CALL,
								PTTState.NONE));
						me.clearRTPPort();
						mMediaSession = null;
						isSuperGroupCall = false;

					}

				}
				break;
			case CommandType.CMD_PTT_REQUEST:
				Log.d(TAG, "Recv: CMD_PTT_REQUEST");
				if (mMediaSession == null || !mMediaSession.isConnected()) {
					return;
				}
				if (me.getLastRecCmd() == CommandType.CMD_PTT_REQUEST) {
					Log.d(TAG, "Already handle: CMD_PTT_REQUEST ! ");
					return;
				}
				if (me.getIsSuperGroupCall() && person.isGroupLeader()) // the
																		// SuperGroup
				{
					if (person.getIpAddress().equals(me.getIpAddress())) {
						Log.d(TAG,
								"BroadCast: CMD_PTT_REQUEST GRANTED mediaSession id:"
										+ mMediaSession.getId()
										+ ", mediaSession" + mMediaSession);
						sendSessionStateBroadcast(new AdhocSessionEventArgs(
								AdhocSessionEventTypes.PTT_REQUEST,
								NgnMediaType.Audio, mMediaSession.getId(),
								CommandType.SUPER_GROUP_AUDIO_CALL,
								PTTState.GRANTED));
					} else {
						Log.d(TAG,
								"BroadCast: CMD_PTT_REQUEST REJECTED mediaSession id:"
										+ mMediaSession.getId()
										+ ", mediaSession" + mMediaSession);
						sendSessionStateBroadcast(new AdhocSessionEventArgs(
								AdhocSessionEventTypes.PTT_REQUEST,
								NgnMediaType.Audio, mMediaSession.getId(),
								CommandType.SUPER_GROUP_AUDIO_CALL,
								PTTState.REJECTED));

					}
				} else if ((me.getIsSuperGroupCall() == false && person
						.getCalleeGroupNumber() == me.getCalleeGroupNumber())) { // the
																					// same
																					// Group
					if (person.getIpAddress().equals(me.getIpAddress())) {
						Log.d(TAG,
								"BroadCast: CMD_PTT_REQUEST GRANTED mediaSession id:"
										+ mMediaSession.getId()
										+ ", mediaSession" + mMediaSession);
						sendSessionStateBroadcast(new AdhocSessionEventArgs(
								AdhocSessionEventTypes.PTT_REQUEST,
								NgnMediaType.Audio, mMediaSession.getId(),
								CommandType.NORMAL_GROUP_AUDIO_CALL,
								PTTState.GRANTED));
					} else {
						Log.d(TAG,
								"BroadCast: CMD_PTT_REQUEST REJECTED mediaSession id:"
										+ mMediaSession.getId()
										+ ", mediaSession" + mMediaSession);
						sendSessionStateBroadcast(new AdhocSessionEventArgs(
								AdhocSessionEventTypes.PTT_REQUEST,
								NgnMediaType.Audio, mMediaSession.getId(),
								CommandType.NORMAL_GROUP_AUDIO_CALL,
								PTTState.REJECTED));

					}
				}
				me.setLastRecCmd(CommandType.CMD_PTT_REQUEST);
				break;
			case CommandType.CMD_PTT_RELEASE:
				Log.d(TAG, "Recv: CMD_PTT_RELEASE");
				if (mMediaSession == null || !mMediaSession.isConnected()) {
					Log.d(TAG, "mediaSession in null! ");
					return;
				}
				if (mMediaSession.getSessionState() == NgnMediaSessionState.CONNECTED) {
					if (me.getLastRecCmd() == CommandType.CMD_PTT_RELEASE) {
						Log.d(TAG, "Already handle: CMD_PTT_RELEASE ! ");
						return;
					}
					if ((me.getIsSuperGroupCall() && person.isGroupLeader())
							|| // the SuperGroup
							(me.getIsSuperGroupCall() == false && person
									.getCalleeGroupNumber() == me
									.getCalleeGroupNumber())) {
						Log.d(TAG,
								"BroadCast: CMD_PTT_RELEASE RELEASE_SUCCESS mediaSession id:"
										+ mMediaSession.getId()
										+ ", mediaSession" + mMediaSession);
						sendSessionStateBroadcast(new AdhocSessionEventArgs(
								AdhocSessionEventTypes.PTT_REQUEST,
								NgnMediaType.Audio, mMediaSession.getId(),
								CommandType.GROUP_AUDIO_CALL,
								PTTState.RELEASE_SUCCESS));

					}
					me.setLastRecCmd(CommandType.CMD_PTT_RELEASE);
				}
				break;
			case CommandType.CMD_SUPER_GROUP_AUDIOCALL:// �����յ��������
				Log.d(TAG, "Recv: CMD_SUPER_GROUP_AUDIOCALL");
				if (person.getIpAddress().equals(me.getIpAddress()) == false
						&& me.getIsGroupLeader()) {
					Log.d(TAG,
							String.format("Callee:%d; Caller:%d",
									me.getCalleeGroupNumber(),
									person.getCalleeGroupNumber()));
					if (me.isGroupLeader() && mMediaSession == null) {
						if (me.getLastRecCmd() == CommandType.CMD_SUPER_GROUP_AUDIOCALL) {
							Log.d(TAG,
									"Already handle: CMD_SUPER_GROUP_AUDIOCALL ! ");
							return;
						}
						Log.d(TAG,
								String.format("Get audioPort = %d",
										person.getAudioPort()));
						final NgnMediaSession mediaSession = NgnMediaSession
								.takeIncommingSession(me.getIpAddress(),
										person.getMobileNo(),
										NgnMediaType.Audio,
										SessionType.GroupAudioCall,
										person.getAudioPort(),
										person.getVideoPort());
						mediaSession
								.setSessionState(NgnMediaSessionState.CONNECTED);
						mediaSession.startSession();
						mMediaSession = mediaSession;
						me.setSuperGroupCall(true);

						Log.d(TAG, "BroadCast: CONNECTED mediaSession id:"
								+ mediaSession.getId() + ", mediaSession"
								+ mediaSession);
						// sendMessage(CommandType.CMD_SUPER_AUDIOCALL_ACCEPT,person.getIpAddress());

						ServiceAV.receiveCall(mediaSession);

						// ����
						sendSessionStateBroadcast(new AdhocSessionEventArgs(
								AdhocSessionEventTypes.INCOMING,
								NgnMediaType.Audio, mediaSession.getId(),
								CommandType.SUPER_GROUP_AUDIO_CALL,
								PTTState.NONE));
						me.setLastRecCmd(CommandType.CMD_SUPER_GROUP_AUDIOCALL);
						// sendP2PMessage(CommandType.CMD_SUPER_AUDIOCALL_ACCEPT,person.getIpAddress(),0,0);
						sendBroadCastMessage(
								CommandType.CMD_SUPER_AUDIOCALL_ACCEPT, 0, 0);
					}

				} else {
					// sendMessage(CommandType.CMD_AUDIOCALL_REGECT,person.getIpAddress());
				}
				break;
			case CommandType.CMD_SUPER_AUDIOCALL_ACCEPT:// �����յ��϶��ظ�
				Log.d(TAG, "Recv: CMD_SUPER_AUDIOCALL_ACCEPT");
				if (person.getIpAddress().equals(me.getIpAddress()) == false
						&& me.isGroupLeader()) {// the same Group
					if (mMediaSession.isConnected()
							&& me.getLastRecCmd() == CommandType.CMD_SUPER_AUDIOCALL_ACCEPT) {
						Log.d(TAG,
								"Already handle: CMD_SUPER_AUDIOCALL_ACCEPT ! ");
						return;
					}
					Log.d(TAG,
							String.format("Caller:%d; Callee:%d",
									me.getCalleeGroupNumber(),
									person.getCalleeGroupNumber()));
					if (mMediaSession.getSessionState() == NgnMediaSessionState.INPROGRESS) {

						mMediaSession
								.setSessionState(NgnMediaSessionState.CONNECTED);
						mMediaSession.startSession();
						Log.d(TAG, "BroadCast: CONNECTED mediaSession id:"
								+ mMediaSession.getId() + ", mediaSession"
								+ mMediaSession);
						// ����
						ServiceAV.receiveCall(mMediaSession);

						sendSessionStateBroadcast(new AdhocSessionEventArgs(
								AdhocSessionEventTypes.INCALL,
								NgnMediaType.Audio, mMediaSession.getId(),
								CommandType.SUPER_GROUP_AUDIO_CALL,
								PTTState.NONE));
						me.setLastRecCmd(CommandType.CMD_SUPER_AUDIOCALL_ACCEPT);
					}
				}
				break;
			case CommandType.CMD_SUPER_SESSION_HUNGUP:
				Log.d(TAG, "Recv: CMD_SUPER_SESSION_HUNGUP");
				if (person.getIpAddress().equals(me.getIpAddress()) == false
						&& me.isGroupLeader()) {

					Log.d(TAG,
							String.format("Callee:%d; Caller:%d",
									me.getCalleeGroupNumber(),
									person.getCalleeGroupNumber()));
					if (mMediaSession != null) {

						if (me.getLastRecCmd() == CommandType.CMD_SUPER_SESSION_HUNGUP) {
							MyLog.d(TAG,
									"Already handle: CMD_SUPER_SESSION_HUNGUP ! ");
						}

						// �Ҷ�

						long sessionid = mMediaSession.getId();
						Log.d(TAG, "BroadCast: TERMINATED mediaSession id:"
								+ mMediaSession.getId() + ", mediaSession"
								+ mMediaSession);
						mMediaSession
								.setSessionState(NgnMediaSessionState.TERMINATED);
						sendSessionStateBroadcast(new AdhocSessionEventArgs(
								AdhocSessionEventTypes.INCOMING,
								NgnMediaType.Audio, sessionid,
								CommandType.SUPER_GROUP_AUDIO_CALL,
								PTTState.NONE));
						me.clearRTPPort();
						mMediaSession = null;
						me.setLastRecCmd(CommandType.CMD_SUPER_SESSION_HUNGUP);
					}

				}
				break;
			default:
				break;

			}
		}

		// ���»���û���Ϣ���û��б���
		private void addPerson(SKSPerson person) {
			// Log.d("","Gle---heartbeatTime="+person.heartbeatTime);
			person.setHeartbeatTime(System.currentTimeMillis());
			// Log.d("","GLE---modify heartbeatTime="+person.heartbeatTime);
			if (childrenMap.size() > 0
					&& childrenMap.containsKey(person.getIpAddress())) {
				// childrenMap.remove(person.personId);
				// childrenMap.put(person.personId, person);

				for (Entry<String, SKSPerson> entry : childrenMap.entrySet()) {
					String key = entry.getKey();
					SKSPerson sksPerson = childrenMap.get(key);
					MyLog.d("sunjianyun", "key---value----->" + key + "----"
							+ sksPerson);

					if (sksPerson.getIpAddress().equals(person.getIpAddress())
							&& (!sksPerson.getPersonNickeName().equals(
									person.getPersonNickeName()) || !sksPerson
									.getMobileNo().equals(person.getMobileNo()))) {

						MyLog.d("sunjianyun",
								"user login again,but username is change");
						childrenMap.put(person.getIpAddress(), person);
						sendPersonHasChangedBroadcast(CommandType.personOnLine);
						break;

					} else {
						childrenMap.get(person.getIpAddress())
								.setHeartbeatTime(System.currentTimeMillis());
					}
				}

			} else {
				childrenMap.put(person.getIpAddress(), person);
				sendPersonHasChangedBroadcast(CommandType.personOnLine);
			}
		}

		// �ر�Socket����
		private void release() {
			// sendMessage(CommandType.CMD_TYPE3,CommandType.MULTICAST_IP);
			if (socketReceive != null)
				socketReceive.close();
		}

		// ע���Լ���������
		public void joinOrganization() {
			// getMyInfomation();
			// sendMessage(CommandType.CMD_TYPE1, CommandType.MULTICAST_IP);
			sendMessage(CommandType.CMD_TYPE1, "255.255.255.255"); // ���ն˰汾����java.net.SocketException:
																	// sendto
																	// failed:
																	// ENETUNREACH
																	// (Network
																	// is
																	// unreachable)
		}

		public void sendMessage(int cmdType, String targetIp) {
			Log.d("ServiceAdhoc", "ServiceAdhoc - sendMessage()");
			Log.d("ServiceAdhoc", "ServiceAdhoc - cmdType = " + cmdType);
			Log.d("ServiceAdhoc", "ServiceAdhoc - targetIp = " + targetIp);

			// SetStrictMode();
			// Log.d("ServiceAdhoc", "ServiceAdhoc - SetStrictMode()");
			try {
				DatagramSocket socketSend = new DatagramSocket();
				me.setCmdType(cmdType);

				socketSend.setTrafficClass(0xb4);
				MyLog.d(TAG, "get TOS: " + socketSend.getTrafficClass());
				Log.d("ServiceAdhoc", "ServiceAdhoc - me = " + me.toString());
				byte[] outarray = me.toString().getBytes();// .toByteArray();
				// InetAddress local = InetAddress.getByName("255.255.255.255");
				// DatagramPacket dp = new
				// DatagramPacket(outarray,outarray.length,
				// InetAddress.getByName(targetIp), CommandType.PORT);
				InetAddress targetAddr = InetAddress.getByName(targetIp);
				DatagramPacket dp = new DatagramPacket(outarray,
						outarray.length, targetAddr, CommandType.RECV_PORT);
				socketSend.send(dp);

				socketSend.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// ========================Э�������ͨѶģ�����=======================================================

	// ========================�㲥���д���=======================================
	public NgnTimer mSendSessionCmdTimer = null;
	private NgnMediaSession mMediaSession = null;
	public NgnTimer mStopSendSessionCmdTimer = null;
	public boolean isSuperGroupCall = false;

	public void MakeCall(final int cmdType, final String targetIp,
			long mediaSessionID) {

		// if(mMediaSession!=null){
		// if(mMediaSession.isActive()){
		// mMediaSession.hungUp();
		// mMediaSession = null;
		// }
		// else{
		// MyLog.d(TAG, "Already in GroupAudio Call ... return.");
		// return;
		// }
		// }
		if (cmdType == CommandType.CMD_SUPER_GROUP_AUDIOCALL) {
			if (me.isGroupLeader() == false) {
				MyLog.d(TAG, "youself is not a GroupLeader... so return.");
				return;
			}
			me.setSuperGroupCall(true);
		}
		int[] audioPort = new int[1];
		int[] videoPort = new int[1];
		audioPort[0] = 0;
		videoPort[0] = 0;
		// mediaSession.getMediaSessionMgr().getAudioPort(audioPort);
		// mediaSession.getMediaSessionMgr().getVideoPort(videoPort);

		mMediaSession = NgnMediaSession.getSession(mediaSessionID);
		if (mMediaSession == null) {
			return;
		}
		mMediaSession.getMediaSessionMgr().getAudioPort(audioPort);
		mMediaSession.getMediaSessionMgr().getVideoPort(videoPort);
		final int mAudioPort = audioPort[0];
		final int mVideoPort = videoPort[0];

		mMediaSession.setSessionState(NgnMediaSessionState.INPROGRESS);// ����
		Log.d(TAG, "Send: CMD_XXX_GROUP_AUDIOCALL mediaSession id:"
				+ mMediaSession.getId() + ", mediaSession" + mMediaSession);
		// sendSessionStateBroadcast(
		// new
		// AdhocSessionEventArgs(AdhocSessionEventTypes.INCALL,NgnMediaType.Audio,mMediaSession.getId(),CommandType.GROUP_AUDIO_CALL,PTTState.NONE));
		//
		Log.d(TAG, String.format(
				"Get audioPort = %d; videoPort = %d; mediaSession = %s",
				mAudioPort, mVideoPort, mMediaSession));
		me.setLastRecCmd(-1);

		Log.d(TAG, "Send: CMD_XXX_GROUP_AUDIOCALL:" + cmdType);
		sendBroadCastMessage(cmdType, mAudioPort, mVideoPort);

		// if session can't be setup during 10s
		NgnTimer timer = new NgnTimer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (mMediaSession != null
						&& mMediaSession.isConnected() == false) {
					mMediaSession.hungUp();
					me.setLastRecCmd(-1);
					me.clearRTPPort();
					if (mMediaSession.isOutgoing()) {
						Log.d(TAG,
								"Send: CMD_SESSION_HUNGUP for session can't be setup during 4s");
						if (me.getIsSuperGroupCall())
							sendBroadCastMessage(
									CommandType.CMD_SUPER_SESSION_HUNGUP, 0, 0);
						else {
							Log.d(TAG,
									"Send: CMD_NORMAL_SESSION_HUNGUP for session can't be setup during 4s");
							sendBroadCastMessage(
									CommandType.CMD_NORMAL_SESSION_HUNGUP, 0, 0);
						}
					}
					mMediaSession = null;
				}
			}
		}, 4 * 1000);
	}

	// public NgnMediaSession getMediaSession()
	// {
	// return mMediaSession;
	// }
	public void Hungup() {
		Log.d(TAG, "ServiceAdhoc Hungup()...");
		if (mMediaSession == null) {
			Log.d(TAG, "ServiceAdhoc No Need to Hungup()...");
			return;
		}
		me.setLastRecCmd(-1);
		if (mMediaSession.isOutgoing()) {
			Log.d(TAG, "Caller Hungup()!");

			Log.d(TAG, "Send: CMD_SESSION_HUNGUP");
			if (me.getIsSuperGroupCall()) {
				Log.d(TAG, "Send: CMD_SUPER_SESSION_HUNGUP");
				sendBroadCastMessage(CommandType.CMD_SUPER_SESSION_HUNGUP, 0, 0);
			} else {
				Log.d(TAG, "Send: CMD_NORMAL_SESSION_HUNGUP");
				sendBroadCastMessage(CommandType.CMD_NORMAL_SESSION_HUNGUP, 0,
						0);
			}
		}
		long sessionid = mMediaSession.getId();
		mMediaSession.setSessionState(NgnMediaSessionState.TERMINATED);
		sendSessionStateBroadcast(new AdhocSessionEventArgs(
				AdhocSessionEventTypes.INCALL, NgnMediaType.Audio, sessionid,
				CommandType.GROUP_AUDIO_CALL, PTTState.NONE));
		me.clearRTPPort();
		mMediaSession = null;

	}

	public void sendPTTRequestCMD() {
		if (mMediaSession == null)
			return;
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		Log.d(TAG, "Send: CMD_PTT_REQUEST");
		sendBroadCastMessage(CommandType.CMD_PTT_REQUEST, 0, 0);

		// }
		// }).start();
	}

	public void sendPTTReleaseCMD() {
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		if (mMediaSession == null)
			return;
		if (GlobalSession.bSocketService == true
				&& mMediaSession.isConnected() == false)
			return;
		Log.d(TAG, "Send: CMD_PTT_RELEASE");
		sendBroadCastMessage(CommandType.CMD_PTT_RELEASE, 0, 0);
		// }
		// }).start();
	}

	public void sendP2PMessage(final int cmdType, final String targetIp,
			final int audioPort, final int videoPort) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// for(int i = 0; i < 3; ++i){
				try {
					InetAddress srcAddr = InetAddress.getByName(me
							.getIpAddress());
					DatagramSocket socketSend = new DatagramSocket(
							CommandType.SEND_PORT, srcAddr);
					socketSend.setTrafficClass(0xb4);
					MyLog.d(TAG, "get TOS: " + socketSend.getTrafficClass());
					// DatagramSocket socketSend = new
					// DatagramSocket(CommandType.SEND_PORT);
					me.setCmdType(cmdType);
					me.setAudioPort(audioPort);
					me.setVideoPort(videoPort);
					// me.setCallerGroupNumber(me.getCalleeGroupNumber());
					byte[] outarray = me.toString().getBytes();
					InetAddress targetAddr = InetAddress.getByName(targetIp);
					DatagramPacket dp = new DatagramPacket(outarray,
							outarray.length, targetAddr, CommandType.RECV_PORT);
					socketSend.send(dp);

					socketSend.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// try {
				// Thread.sleep(100);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// }
			}
		}).start();
	}

	public void sendBroadCastMessage(final int cmdType, final int audioPort,
			final int videoPort) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// for(int i = 0; i < 3; ++i){
				try {
					InetAddress srcAddr = InetAddress.getByName(me
							.getIpAddress());
					DatagramSocket socketSend = new DatagramSocket(
							CommandType.SEND_PORT, srcAddr);
					socketSend.setTrafficClass(0xb4);
					MyLog.d(TAG, "get TOS: " + socketSend.getTrafficClass());
					me.setCmdType(cmdType);
					me.setAudioPort(audioPort);
					me.setVideoPort(videoPort);
					// me.setCallerGroupNumber(me.getCalleeGroupNumber());
					byte[] outarray = me.toString().getBytes();
					InetAddress targetAddr = InetAddress
							.getByName(CommandType.BROADCAST_IP);
					DatagramPacket dp = new DatagramPacket(outarray,
							outarray.length, targetAddr, CommandType.RECV_PORT);
					socketSend.send(dp);

					socketSend.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// try {
				// Thread.sleep(100);
				// } catch (InterruptedException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// }
			}
		}).start();
	}

	// �����û����¹㲥
	public static void sendSessionStateBroadcast(AdhocSessionEventArgs args) {
		Intent intent = new Intent(AdhocSessionEventArgs.ADHOC_SESSION_EVENT);
		intent.putExtra(AdhocSessionEventArgs.EXTRA_EMBEDDED, args);
		if (GlobalVar.orderedbroadcastSign) {
			NgnApplication.getContext().sendOrderedBroadcast(intent, null);
		} else {
			NgnApplication.getContext().sendBroadcast(intent);
		}
	}
	// ========================�㲥���д��� End=======================================

}
