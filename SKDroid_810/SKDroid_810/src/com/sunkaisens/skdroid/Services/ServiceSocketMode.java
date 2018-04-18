package com.sunkaisens.skdroid.Services;

//service for socket mode.
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.events.AdhocSessionEventArgs;
import org.doubango.ngn.events.AdhocSessionEventTypes;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnInviteEventArgs;
import org.doubango.ngn.events.NgnInviteEventTypes;
import org.doubango.ngn.events.NgnMessagingEventArgs;
import org.doubango.ngn.events.NgnMessagingEventTypes;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.events.NgnSubscriptionEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.model.NgnAccessPoint;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnInviteSession.InviteState;
import org.doubango.ngn.sip.NgnMediaSession;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnContentType;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnSipCode;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.leadcore.rs485control.Rs485Controller;
import com.sks.adhoc.service.CommandType;
import com.sks.net.socket.message.BCDTools;
import com.sks.net.socket.message.BaseSocketMessage;
import com.sks.net.socket.server.ServerMsgReceiver;
import com.sks.net.socket.server.SocketServer;
import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.MessageTypes;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.ScreenDownloadConcacts;
import com.sunkaisens.skdroid.Utils.ParserSubscribeState;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.component.NodeResource;
import com.sunkaisens.skdroid.cpim.CPIMMessage;
import com.sunkaisens.skdroid.cpim.CPIMParser;
import com.sunkaisens.skdroid.encryptcall.EncryptProcess;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall;
import com.sunkaisens.skdroid.groupcall.GroupPTTCall.PTTState;
import com.sunkaisens.skdroid.groupcall.PTTActionTypes;
import com.sunkaisens.skdroid.groupcall.PTTInfoMsg;
import com.sunkaisens.skdroid.groupcall.PTTResultTypes;
import com.sunkaisens.skdroid.groupcall.PTTTypes;
import com.sunkaisens.skdroid.model.VERSION;
import com.sunkaisens.skdroid.session.SessionType;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ServiceSocketMode {

	private static final String TAG = ServiceSocketMode.class
			.getCanonicalName();;

	private static Rs485Controller controller;
	private static int controllerSpeed = 0;
	//
	private static ServiceSocketMode instance = null;
	private GroupPTTCall mPttCall = null;

	private NgnTimer mGisRequestTimer = null;// the timer of the gis report

	private static NgnTimer mTimerAudioPTTReport; // group audio heartbeat

	public static Handler contactRefreshhandler = null; // 濠㈣泛瀚幃濠囨焻濮樻剚鍞电憸鐗堟礀瑜板鏁?

	public static final int CONTACTREFRESHMESSAGE = 1111; // 闁告帡鏀遍弻濠囨焻濮樻剚鍞电憸鐗堟礃缁夌兘鏁?

	private NgnTimer hungupTimer = null;

	// ywh add 自组网根据状态播放回铃音
	private MediaPlayer mPlayer = null;
	private int hungupStatus = 0;
	private boolean isConnected = false;

	private static TimerTask mTimerTaskAudioPTTReport = new TimerTask() {
		@Override
		public void run() {
			if (GlobalSession.avSession != null
					&& SystemVarTools.mTimerAudioPTTReport != null) {
				sendPTTReportAliveInfoMsg();
			}
		}
	};

	private ServiceSocketMode() {

		contactRefreshhandler = new Handler() {
			public void handleMessage(Message message) {

				if (message.what == CONTACTREFRESHMESSAGE) {
					pushcontacts(false);
				}

			}
		};

	}

	public static ServiceSocketMode getInstance() {
		if (instance == null) {
			instance = new ServiceSocketMode();
		}
		//
		return instance;
	}

	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		MyLog.d(TAG, "FUNC:ServiceSocketMode.onReceive()");

		// Registration Events
		if (NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)) {
			NgnRegistrationEventArgs args = intent
					.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				Log.e(TAG, "Invalid event args");
				return;
			}
			if (!GlobalSession.bSocketService) {
				return;
			}
			String localMobileNo = Engine
					.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
							NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
			switch ((args.getEventType())) {
			case REGISTRATION_INPROGRESS:
				break;

			case REGISTRATION_OK:
				// start to down contacts
				// downloadContacts();

				byte[] dataok = new byte[1];
				dataok[0] = 0; // 0 register OK ?1. invalid password ?2. user
								// not exist ?5. already logged ?6. failed
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_USER_REGRESULT, dataok)); // 闂傚倷鑳堕崕鐢稿磻閹捐绀夌广儱顦弰銉︾箾閹寸們姘跺几閺嶎厽鐓忓┑鐐茬仢閸旀岸鏌ㄩ悢鍓佺煓鐎殿喖鐖奸崺锟犲磼濞戞艾寮虫繝鐢靛仜瀵爼鎮ч弴銏犵叀濠㈣埖鍔曢柋鍥煟閺冨浂鍟囬柨锟

				MyLog.i("ServiceSocketMode: type = "
						+ BaseSocketMessage.MSG_S_USER_REGRESULT,
						"mobileNo = " + localMobileNo + "; message = "
								+ dataok[0] + ", MSG_S_USER_REGRESULT"
								+ ", sipCode = " + args.getSipCode());
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_INIT_STATE, BCDTools.Str2BCD(
								localMobileNo, 10))); // 闂備浇宕垫慨鎶芥倿閿曞倸纾块柟璺哄閸ヮ剦鏁嗗ù锝堛閺鎶芥⒑閺傘儲娅呴柛鐔叉櫊閺佹捇鏁?

				MyLog.i("ServiceSocketMode: type = "
						+ BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = " + ""
						+ "; message = 0 " + "閻€劍鍩涢崣椋庣垳:" + localMobileNo);

				break;

			case REGISTRATION_NOK:

				// failed to registe
				byte[] datanok = new byte[1];
				datanok[0] = 6; // 0 Registe success,1 The passwoord is wrong,2
				if (args.getSipCode() == 403) {
					datanok[0] = 1;
				} else if (args.getSipCode() == 404) {
					datanok[0] = 2;
				} else if (args.getSipCode() == 903) {
					datanok[0] = 6;
				}
				// User is not exist,6 Registe failed
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_USER_REGRESULT, datanok));

				MyLog.i("ServiceSocketMode: type = "
						+ BaseSocketMessage.MSG_S_USER_REGRESULT,
						"mobileNo = " + localMobileNo + "; message = "
								+ datanok[0] + ", MSG_S_USER_REGRESULT"
								+ ", sipCode = " + args.getSipCode());
				break;

			case UNREGISTRATION_INPROGRESS:
				break;

			case UNREGISTRATION_OK:

				byte[] dataunregok = new byte[1];
				dataunregok[0] = 0; // 0:婵炲鍔戦弨銏ゅ箣閹邦剙顫 1:婵炲鍔戦弨銏″緞鏉堫偉袝
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_USER_UNREGRESULT, dataunregok));

				MyLog.i("ServiceSocketMode: type = "
						+ BaseSocketMessage.MSG_S_USER_UNREGRESULT,
						"mobileNo = " + "" + "; message = "
								+ "0: unRegister OK");
				break;

			case UNREGISTRATION_NOK:

				// 濠电偛顦崝鎴﹀绩閵忊崇窞閺夊牜鍋夎
				byte[] dataunregnok = new byte[1];
				dataunregnok[0] = 1; // 0 濠电偛顦崝鎴﹀绩閵忋倕绠ｉ柟閭﹀墮椤拷1
										// 濠电偛顦崝鎴﹀绩閵忊崇窞閺夊牜鍋夎
				SocketServer
						.sendMessage(new BaseSocketMessage(
								BaseSocketMessage.MSG_S_USER_UNREGRESULT,
								dataunregnok));

				MyLog.i("ServiceSocketMode: type = "
						+ BaseSocketMessage.MSG_S_USER_UNREGRESULT,
						"mobileNo = " + "" + "; message = " + "1:婵炲鍔戦弨銏″緞鏉堫偉袝");

				break;

			default:
				break;

			}
		}

		// PagerMode Messaging Events
		if (NgnMessagingEventArgs.ACTION_MESSAGING_EVENT.equals(action)) {
			NgnMessagingEventArgs args = intent
					.getParcelableExtra(NgnMessagingEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				Log.d(TAG, "Invalid event args");
				return;
			}
			NgnMessagingEventTypes eventType = args.getEventType();
			switch (eventType) {
			case INCOMING:
				// if(SKDroid.getVersionName().contains("soc_msg")){
				args.getContentType();
				intent.getStringExtra(NgnMessagingEventArgs.EXTRA_DATE);
				String remoteParty = intent
						.getStringExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY); // sip:19800005003@192.168.1.192:5061
				if (NgnStringUtils.isNullOrEmpty(remoteParty)) {
					remoteParty = NgnStringUtils.nullValue();
				}
				MyLog.d(TAG, "remoteParty=" + remoteParty);
				String remoteArray[] = remoteParty.split(",");
				String remoteNo2 = null;
				if (remoteArray.length > 1) {
					remoteNo2 = remoteArray[0];
					remoteParty = NgnUriUtils.getUserName(remoteArray[1]);
				} else {
					remoteParty = NgnUriUtils.getUserName(remoteParty);
					remoteNo2 = remoteParty;
				}
				// 19800005003
				MyLog.d(TAG, "閸欐垿锟介弬鐟板娇閻拷=" + remoteNo2 + "  閸欓鐖2="
						+ remoteParty);

				CPIMMessage cpimMessage = CPIMParser.parse(new String(args
						.getPayload()));
				String content = (String) cpimMessage.getContent();
				if (SKDroid.sks_version == VERSION.SOCKET && content != null
						&& content.startsWith("type:file")) {
					return;
				}
				String gmmember = "";
				if (remoteArray.length > 1) {
					gmmember = NgnUriUtils.getUserName(remoteArray[1]);

				}
				// String msgID = gmmember + ";" +
				// cpimMessage.getLocalMsgID();//
				// 閻€劌褰傞柅浣规煙閸欓鐖+localMsgID閺夈儲鐖ｇ拠鍡樻Ц閸氾附妲搁柌宥呭絺閻拷
				// Log.d(TAG,String.format("Get a message with msgID = %s",
				// msgID));
				if (cpimMessage.getMsgType() != null
						&& !cpimMessage.getMsgType().equals("REPORT")) {

				} else { // 婵犮垼娉涚�氼噣骞冩繝鍥ㄥ亹閻犱浇顫夌�氬綊鏌熼煬鎻掑姷闁匡拷
					if (content != null && !content.isEmpty()) {
						if (content.indexOf("MsgExt.reportID: ") != -1
								&& content
										.indexOf("MsgExt.reportType: SUCCESSFUL") != -1) {

							int reportIDPos = content
									.indexOf("MsgExt.reportID:");
							content = content.substring(reportIDPos).trim();
							String reportID = content.substring(17, 55);

							ServiceLoginAccount.mMessageIDHashMap.put(reportID,
									System.currentTimeMillis() / 1000);
							try {
								Tools_data
										.writeIDHashMap(ServiceLoginAccount.mMessageIDHashMap);
							} catch (IOException e) {
								e.printStackTrace();
							}
							/**
							 * Socket閻樿埖锟介幎銉ユ啞婢跺嫸鎷�?
							 */
							if (GlobalSession.bSocketService) {
								byte[] data = new byte[1];
								data[0] = 0; // 0 闁瑰瓨鍔曢敓锟�1 濠㈡儼绮鹃敓锟�
												// SocketServer.sendMessage(new
												// BaseSocketMessage(BaseSocketMessage.MSG_C_S_SMS_RESULT,
												// data));

								MyLog.i("ServiceSocketMode: type = "
										+ BaseSocketMessage.MSG_C_S_SMS_RESULT,
										"mobileNo = " + remoteParty
												+ "; message = "
												+ "MSG Send OK!");

								break;

							}

						}
					} else {
						break;
					}
				}

				byte[] mobileNo = BCDTools.Str2BCD(remoteParty, 10);
				byte[] mobileNo2 = null;
				mobileNo2 = BCDTools.Str2BCD(remoteNo2, 10);
				String msg = content;
				byte[] byteMessage = null;
				try {
					byteMessage = msg.getBytes(SystemVarTools.encoding_gb2312);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				byte[] data = new byte[22 + byteMessage.length];
				System.arraycopy(mobileNo2, 0, data, 0, 10);
				System.arraycopy(mobileNo, 0, data, 10, 10);
				data[20] = 1; // 1 娑撴艾濮熸潪顖欐 --> 闁倿鍘ゆ潪顖欐

				// data[11] = (byte) ((int)(byteMessage.length/256)); //
				// 闂佽崵鍋炵粙蹇涘礉鎼淬劌桅婵鍩栭崕妤併亜閹捐泛鏋旈崯鎼佹⒑鐠団�冲季闁搞劋鍗冲畷顒傦拷闂堚晜瀚归柣鐔稿缁夋椽鏌￠崱姘跺摵闁诡垱妫冮弫鍐╂媴閻╂帇鍨介弻锟�56闂傚倷绶￠崰鎾诲礉瀹�鍕瀭?
				// data[12] = (byte) ((int)(byteMessage.length%256)); //
				// 闂佽崵鍋炵粙蹇涘礉鎼淬劌桅婵鍩栭崕妤併亜閹捐泛鏋旈崯鎼佹⒑鐠団�冲季闁搞劋鍗冲畷顒傦拷闂堚晜瀚归柣鐔稿缁夋椽鏌￠崱姘跺摵闁诡垱妫冮弫鍌滄崉閵娧勫枓闂佺儵鍓濈敮鎺楀箠鎼搭煉鎷�?56闂傚倷绶￠崰鎾诲礉瀹�鍕瀭?
				// System.arraycopy(byteMessage, 0, data, 13,
				// byteMessage.length);
				data[21] = (byte) ((int) byteMessage.length); // 閻厺淇婇梹鍨
				// data[11] =intToBytes2(byteMessage.length)[0]; //
				// 閻厺淇婇梹鍨缁楊兛绔存稉顏勭摟閿燂拷//
				// data[12] =intToBytes2(byteMessage.length)[1]; //
				// 闂佽崵鍋炵粙蹇涘礉鎼淬劌桅婵鍩栭崕妤併亜閹捐泛鏋旈崯鎼佹⒑鐠団�冲季闁搞劋鍗冲畷顒傦拷闂堚晜瀚归柣鐔稿缁夋椽鏌￠崱娑楁喚闁跨噦鎷�?

				System.arraycopy(byteMessage, 0, data, 22, byteMessage.length);
				// System.arraycopy(byteMessage, 0, data, 13,
				// byteMessage.length);

				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_C_S_SMS_FORMAT, data)); // 闂備焦妞块崣搴ㄥ储閻ｅ瞼鐭撻柣鎴ｆ缁犳帗銇勯弽銊с�掑瑙勫姍閿燂拷濠电偞鍨堕幐鎼侇敄閸儻鎷�?

				MyLog.i("ServiceSocketMode: type = "
						+ BaseSocketMessage.MSG_C_S_SMS_FORMAT, "mobileNo1 = "
						+ remoteNo2 + " mobileNo2=" + remoteParty
						+ "; message = " + msg);
				Log.d("", "閻厺淇婇梹鍨=" + data[21]);
				// }else {
				// args.getContentType();
				// intent.getStringExtra(NgnMessagingEventArgs.EXTRA_DATE);
				// String remoteParty = intent
				// .getStringExtra(NgnMessagingEventArgs.EXTRA_REMOTE_PARTY); //
				// sip:19800005003@192.168.1.192:5061
				// MyLog.d(TAG, "閸欐垿锟介弬鐟板娇閻拷" + remoteParty);
				// if (NgnStringUtils.isNullOrEmpty(remoteParty)) {
				// remoteParty = NgnStringUtils.nullValue();
				// }
				// String remoteArray[] = remoteParty.split(",");
				// if (remoteArray.length > 1) {
				// remoteParty = NgnUriUtils.makeValidSipUri(remoteArray[1]);
				// }
				// remoteParty = NgnUriUtils.getUserName(remoteParty); //
				// 19800005003
				//
				// CPIMMessage cpimMessage = CPIMParser.parse(new String(args
				// .getPayload()));
				// String content = (String) cpimMessage.getContent();
				//
				// if (cpimMessage.getMsgType() != null
				// && !cpimMessage.getMsgType().equals("REPORT")) {
				// if (ServiceLoginAccount.mMessageIDHashMap != null
				// && ServiceLoginAccount.mMessageIDHashMap
				// .containsKey(cpimMessage.getLocalMsgID())) {
				// } else {
				// ServiceLoginAccount.mMessageIDHashMap.put(
				// cpimMessage.getLocalMsgID(),
				// System.currentTimeMillis() / 1000);
				// try {
				// Tools_data
				// .writeIDHashMap(ServiceLoginAccount.mMessageIDHashMap);
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// }
				// if (content != null && !content.isEmpty()) {
				// if (content.indexOf("MsgExt.reportID: ") != -1
				// && content
				// .indexOf("MsgExt.reportType: SUCCESSFUL") != -1) {
				//
				// int reportIDPos = content
				// .indexOf("MsgExt.reportID:");
				// content = content.substring(reportIDPos).trim();
				// String reportID = content.substring(17, 55);
				//
				// ServiceLoginAccount.mMessageIDHashMap.put(reportID,
				// System.currentTimeMillis() / 1000);
				// try {
				// Tools_data
				// .writeIDHashMap(ServiceLoginAccount.mMessageIDHashMap);
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				//
				// /**
				// // SocketServer.sendMessage(new
				// // BaseSocketMessage(BaseSocketMessage.MSG_C_S_SMS_RESULT,
				// // data));
				//
				// MyLog.i("ServiceSocketMode: type = "
				// + BaseSocketMessage.MSG_C_S_SMS_RESULT,
				// "mobileNo = " + remoteParty
				// + "; message = "
				// + "MSG Send OK!");
				//
				// break;
				//
				// }
				//
				// }
				// } else {
				// break;
				// }
				//
				// }
				//
				// byte[] mobileNo = BCDTools.Str2BCD(remoteParty, 10);
				// String msg = content;
				// byte[] byteMessage = null;
				// try {
				// byteMessage = msg.getBytes(SystemVarTools.encoding_gb2312);
				// } catch (UnsupportedEncodingException e) {
				// e.printStackTrace();
				// }
				// byte[] data = new byte[12 + byteMessage.length];
				// System.arraycopy(mobileNo, 0, data, 0,10);
				//
				// System.arraycopy(byteMessage, 0, data, 12,
				// byteMessage.length);
				// // System.arraycopy(byteMessage, 0, data, 13,
				// // byteMessage.length);
				//
				// SocketServer.sendMessage(new BaseSocketMessage(
				// BaseSocketMessage.MSG_C_S_SMS_FORMAT, data)); //
				// }
				break;

			default:
				break;

			}
		}

		// Invite Events
		else if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)) {
			hungupStatus = 0;
			NgnInviteEventArgs args = intent
					.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				Log.e(TAG, "Invalid event args");
				return;
			}

			NgnInviteEventTypes eventType = args.getEventType(); // CONNECTED
																	// TERMWAIT
																	// /////////////////////////////new
			Log.d(TAG, "eventtype=" + eventType); // ///////////////////////new

			final String phrase = args.getPhrase(); // //////////////////////////////new
													// groupcall/audio
													// cameraerror

			final NgnMediaType mediaType = args.getMediaType();
			int mSessionType = GlobalSession.avSession.getSessionType();

			MyLog.i(TAG, "mediaType=" + mediaType + "   SessionType="
					+ mSessionType);

			// switch (args.getEventType()) {
			switch (eventType) {
			case TERMWAIT:
			case TERMINATED:
				hungupStatus = 1;
				MyLog.d(TAG, "NgnAVSession.Size = " + NgnAVSession.getSize());
				if (NgnAVSession.getSize() > 0) {
					NgnAVSession session2 = NgnAVSession.getSessions().getAt(0);
					if (session2 != null
							&& session2.getConnectionState() == ConnectionState.CONNECTED) {
						MyLog.d(TAG, "閻€劍鍩涘锝呮躬闁俺鐦介敍灞肩瑝娑撳﹥濮ら幐鍌涙焽濞戝牊浼呴妴锟�");
						return;
					} else {

					}
				}
				if (NgnMediaType.isAudioVideoType(mediaType)) {

					if (hungupTimer != null) {
						hungupTimer.cancel();
						hungupTimer.purge();
						hungupTimer = null;
						MyLog.d(TAG, "TERMINATED: set hungupTimer = null.");
					}
					NgnAVSession avSession = GlobalSession.avSession;
					if (avSession != null) {

						// if (NgnMediaType.isAudioType(mediaType) &&
						// (mSessionType == SessionType.AudioCall)) {
						// //闂備浇宕垫慨鏉懨归崒鐐茬煑闁告劦鍠楅崑鍌炴煕椤愶絾绀�缂佺姳鍗抽弻娑氫沪閸撗�濮囬梺璺ㄥ櫐閹凤拷
						if (mSessionType == SessionType.AudioCall) { // 闂備浇宕垫慨鏉懨归崒鐐茬煑闁告劦鍠楅崑鍌炴煕椤愶絾绀�缂佺姳鍗抽弻娑氫沪閸撗�濮囬梺璺ㄥ櫐閹凤拷
							byte[] data = new byte[1];
							if (SystemVarTools.isLocalHangUp) {
								data[0] = 0; // 0:the Session was hungup by
												// yourself
								SystemVarTools.isLocalHangUp = false;
								MyLog.d(TAG, "hungup by yourself");
							} else {
								data[0] = 1; // 1:the session was hungup by the
												// other user
								MyLog.d(TAG, "hungup by remote");
							}
							// 闂傚倷绀侀幉锟犳偡閿曞倸鍨傞柛褎顨嗛弲鎼佹煟閿濆懏婀伴悗姘閵囧嫰骞橀崡鐐典紕闂佸憡鐟╃粻鏍蓟閻旇櫣鐭欓柛鏍ゅ墲鐎氱懓鈹戦悩娆忕У鐎氳绻濋悽闈涗沪闁搞劎鏁婚幃褔鎮滈崶銊ヮ伓?
							// SocketServer.sendMessage(new BaseSocketMessage(
							SocketServer.sendMessage(new BaseSocketMessage(
									BaseSocketMessage.MSG_S_AUDIO_TERMINATED,
									data));

							MyLog.i("ServiceSocketMode: type = "
									+ BaseSocketMessage.MSG_S_AUDIO_TERMINATED,
									"mobileNo = "
											+ ""
											+ "; message = "
											+ "audio session is closed:"
											+ (data[0] == 0 ? "0: local user hungup!"
													: "1:remote user hungup!"));
						}

						// if (NgnMediaType.isVideoType(mediaType)) { //
						// 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐查唶闁哄洨鍋熼鍥⒑缁嬫寧婀扮紒瀣浮閺佹捇鏁�?
						if (mSessionType == SessionType.VideoCall) {
							byte[] data = new byte[1];
							if (SystemVarTools.isLocalHangUp) {
								data[0] = 0; // 0:闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐茬濞达綀娅ｉ敍婊堟⒑鐟欏嫬鍔ら柣掳鍔庢禍鎼佸幢濞戞瑧鍘梺绯曞墲閿氬┑顔肩墦閺佹捇骞嬮幒鎴濈ギ閻庢鍠氶弫濠氬箖濠婂吘鐔兼惞鐟欏嫅锕傛⒒娴ｅ憡鎯堥柣妤�绻掓禍鎼佲�﹂幒鎾愁伓?
								SystemVarTools.isLocalHangUp = false;
							} else {
								data[0] = 1; // 1:闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐茬濞达綀娅ｉ敍婊堟⒑鐟欏嫬鍔ら柣掳鍔庢禍鎼佸幢濞戞瑧鍘梺绯曞墲閿氬┑顔兼处閵囧嫰寮撮妸銉︾亾缂備緡鍠掗弲鐘汇�佸☉妯锋婵☆垰鍚嬬瑧闂傚倷绀侀幖顐︽偋韫囨洑鐒婇柕濞炬櫅缁狅拷
							}
							// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢紒锟藉�块弻锟犲醇閵忊剝姣勯梻浣瑰劤閸婂潡寮婚悢鐑樺皰濠㈣泛锕ら弳娆撴煟閹炬潙鐏撮柡灞剧☉閳诲氦绠涢幘鍓佹晨缂傚倷娴囨ご鍝ユ崲閸曨垰绠熼柟缁㈠枛楠炪垺绻涢幋锝夊摵婵炲牅绮欏娲传閸曨偂绨介梺鍛婎焾閸嬫劙鏁�?
							SocketServer.sendMessage(new BaseSocketMessage(
									BaseSocketMessage.MSG_S_VIDEO_TERMINATED,
									data));

							MyLog.i("ServiceSocketMode: type = "
									+ BaseSocketMessage.MSG_S_VIDEO_TERMINATED,
									"mobileNo = "
											+ ""
											+ "; message = "
											+ "video session is closed:"
											+ (data[0] == 0 ? "0: local user hungup!"
													: "1:remote user hungup!"));
						}
						if (mSessionType == SessionType.VideoSurveilMonitor
								|| mSessionType == SessionType.VideoMonitor) {
							byte[] data = new byte[1];
							if (SystemVarTools.isLocalHangUp) {
								data[0] = 0; // 0:闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐茬濞达綀娅ｉ敍婊堟⒑鐟欏嫬鍔ら柣掳鍔庢禍鎼佸幢濞戞瑧鍘梺绯曞墲閿氬┑顔肩墦閺佹捇骞嬮幒鎴濈ギ閻庢鍠氶弫濠氬箖濠婂吘鐔兼惞鐟欏嫅锕傛⒒娴ｅ憡鎯堥柣妤�绻掓禍鎼佲�﹂幒鎾愁伓?
								SystemVarTools.isLocalHangUp = false;
							} else {
								data[0] = 1; // 1:闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐茬濞达綀娅ｉ敍婊堟⒑鐟欏嫬鍔ら柣掳鍔庢禍鎼佸幢濞戞瑧鍘梺绯曞墲閿氬┑顔兼处閵囧嫰寮撮妸銉︾亾缂備緡鍠掗弲鐘汇�佸☉妯锋婵☆垰鍚嬬瑧闂傚倷绀侀幖顐︽偋韫囨洑鐒婇柕濞炬櫅缁狅拷
							}
							// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢紒锟藉�块弻锟犲醇閵忊剝姣勯梻浣瑰劤閸婂潡寮婚悢鐑樺皰濠㈣泛锕ら弳娆撴煟閹炬潙鐏撮柡灞剧☉閳诲氦绠涢幘鍓佹晨缂傚倷娴囨ご鍝ユ崲閸曨垰绠熼柟缁㈠枛楠炪垺绻涢幋锝夊摵婵炲牅绮欏娲传閸曨偂绨介梺鍛婎焾閸嬫劙鏁�?
							if (controller != null) {
								controller = null;
							}
							SocketServer.sendMessage(new BaseSocketMessage(
									BaseSocketMessage.MSG_S_VIDEO_TERMINATED,
									data));

							MyLog.i("ServiceSocketMode: type = "
									+ BaseSocketMessage.MSG_S_VIDEO_TERMINATED,
									"mobileNo = "
											+ ""
											+ "; message = "
											+ "VideoSurveilMonitor session is closed:"
											+ (data[0] == 0 ? "0: local user hungup!"
													: "1:remote user hungup!"
															+ " SessionType==VideoSurveilMonitor"));
						}
						if (mSessionType == SessionType.GroupAudioCall
								|| mSessionType == SessionType.GroupVideoCall) { // 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺冩嫚閼碱剦鍞堕梻浣侯焾閺堫剛绮欓幒妤佹櫢闁匡拷

							if (SystemVarTools.mStartGroupCalllRepoort
									&& mTimerAudioPTTReport != null) {
								mTimerAudioPTTReport.cancel();
								mTimerAudioPTTReport = null;
							}
							if (SystemVarTools.mStartGroupCalllRepoort
									&& SystemVarTools.mTimerAudioPTTReport != null) {
								SystemVarTools.mTimerAudioPTTReport.cancel();
								SystemVarTools.mTimerAudioPTTReport = null;
								SystemVarTools.mTakeAudioPTTFlag = false;
							}

							// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢�瑰憡绻堥弻锝夊箻閹剁瓔锟介悗鐐瑰�栧浠嬪蓟閻旂儤灏掑璺猴工閺嗘瑩鏌ｉ幘鏉戠伌闁哄本绋戦埢搴ょ疀閹惧墎娉跨紓鍌欐祰妞村摜鎹㈤崒鐑囩稏闁靛繈鍊栭弲鏌ユ煕濞戝崬鏋﹂柛姘煎亰濮婃椽宕ㄦ繝鍕╋拷闂佸憡顭嗛崱鎰睏?闂備浇顕ч柊锝咁焽瑜嶉敃銏ゆ焼瀹ュ孩鏅�?19800005002
							String mobileNo = NgnUriUtils.getUserName(avSession
									.getRemotePartyUri());
							byte[] mobileNoBytes = BCDTools.Str2BCD(mobileNo,
									10);
							byte[] data = new byte[11];
							data[0] = 5; // 5闂傚倷鐒︾�笛呯矙閹烘埈娼╅柕濞у啫鐏婇悗骞垮劚椤︻垰顔忓┑瀣厽闁圭偓濞婇妤冩喐閹殿喖鈻堥柨锟�
							System.arraycopy(mobileNoBytes, 0, data, 1, 10);
							SocketServer.sendMessage(new BaseSocketMessage(
									BaseSocketMessage.MSG_S_GROUP_STATE, data));

							MyLog.i("ServiceSocketMode: type = "
									+ BaseSocketMessage.MSG_S_GROUP_STATE,
									"mobileNo = "
											+ mobileNo
											+ "; message = "
											+ "MSG_S_GROUP_STATE 5: groupcall is closed."); // 闂傚倷娴囧▔鏇㈠窗閹版澘鍑犲┑鐘宠壘缁狀垶鏌ｉ幋锝呅撻柡鍛倐濮婂宕橀埡浣稿Ц闂佽妞挎禍顏堟晸?5闂傚倷娴囧▔鏇㈠窗閹版澘鍑犲┑鐘宠壘缁狀垶鏌ｉ幋锝呅撻柡鍛倐閺岋繝宕掑Ο琛″亾閺嶎偀鍋撳顒傜Ш闁哄被鍔戦幃銏㈢矙鐠恒劌濮烽梻浣筋潐瑜板啴顢栭崱娑欐櫢闁匡拷
						}
					}
				}
				break;

			case INCOMING: {
				if (NgnAVSession.getSize() > 1) {
					Log.d(TAG,
							"INCOMING new call.But i am busy,i will not notify.");
					return;
				}
				final NgnAVSession avSession = NgnAVSession.getSession(args
						.getSessionId());

				if (avSession != null) {
					GlobalSession.avSession = avSession; //
				}

				if (GlobalSession.avSession == null) {
					break;
				}

				if (NgnMediaType.isAudioVideoType(mediaType)
						&& (mSessionType == SessionType.VideoSurveilMonitor || mSessionType == SessionType.VideoMonitor))// 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐查唶闁哄洨鍋熼崐鐐烘⒑缂佹ê濮堝鐟版閺佹捇鏁�?
				{
					GlobalSession.avSession.acceptCall();
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢紒锟藉�块弻锟犲醇閵忊剝姣勯梻浣瑰劤閸婂潡寮婚悢鐑樺皰濠㈣泛锕ら弳娆撴煟閹炬潙鐏撮柡灞剧☉閳诲氦绠涢幘鍓佹晨缂傚倷娴囨ご鍝ユ崲閸繍鍤曢柛顭戝亜缁剁偤骞栭幖顓炴灍缂傚秴锕ユ穱濠囶敃閵堝拋鏆梺鎼炲姂濞佳呭弲闂佸搫绉查崝宀勵敋闁秵鐓熸俊顖涘閻濐亪姊婚崟顐ばч柨锟�
					String mobileNo = NgnUriUtils
							.getUserName(GlobalSession.avSession
									.getRemotePartyUri());
					byte[] mobileNoBytes = BCDTools.Str2BCD(mobileNo, 10);
					byte[] data = new byte[11]; // 11Bytes
					System.arraycopy(mobileNoBytes, 0, data, 0, 10);
					data[10] = 1; // 1闂傚倷鐒︾�笛呯矙閹烘挾鈹嶆繛宸悍缂嶆牠鎮楅敐搴′壕缂佹唻缍侀弻鐔猴拷濡偐鐣洪梺璺ㄥ枑閺嬪鏁�?
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_VIDEO_INCOMING, data)); // MSG_S_VIDEO_SURVEIL_MONITOR
					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_VIDEO_INCOMING,
							"mobileNo = "
									+ mobileNo
									+ "; message = "
									+ "MSG_S_VIDEO_INCOMING: monitor/surveil incoming."); // 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐查唶闁哄洨鍋熸鍥⒑闂堟冻绱￠柛鏇ㄥ墯閻︼拷
																							// 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐查唶闁哄洨鍋熼崐鐐烘⒑缂佹ê濮堝鐟版閺佹捇鏁�?
					break;
				} else if (NgnMediaType.isAudioType(mediaType)
						&& (mSessionType == SessionType.AudioCall)) { // 闂備浇宕垫慨鏉懨归崒鐐茬煑闁告劦鍠楅崑鍌炴煕椤愶絾绀�缂佺嫏鍥ㄧ厪濠㈣鍨扮�氼參鏁�?
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢紒锟藉�块弻锟犲醇閵忊剝姣勯梻浣瑰劤閸婂潡寮婚悢鐑樺皰濠㈣泛锕ら弳娆撴煟閹炬潙鐏撮柡灞剧☉閳诲氦绠涢幘鍓佹晨缂傚倷娴囨ご鍝ユ崲閸繍鍤曢柛顭戝亜缁剁偤鎮楅敐搴′喊婵″墽鍏橀弫鎾绘晸?

					String mobileNo = NgnUriUtils
							.getUserName(GlobalSession.avSession
									.getRemotePartyUri());
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_AUDIO_INCOMING, BCDTools
									.Str2BCD(mobileNo, 10)));
					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_AUDIO_INCOMING,
							"mobileNo = " + mobileNo + "; message = "
									+ "MSG_S_AUDIO_INCOMING");
				}
				//

				// if (NgnMediaType.isAudioType(mediaType) &&
				// !(eventType.equals(NgnInviteEventTypes.GROUP_PTT_INFO))) { //
				// 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺冩嫚閼碱剟鐛撻梻渚�娼ч敍蹇涘礋椤掍胶妲�
				else if (NgnMediaType.isAudioVideoType(mediaType)
						&& (mSessionType == SessionType.GroupAudioCall || mSessionType == SessionType.GroupVideoCall)) { // 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺冩嫚閼碱剟鐛撻梻渚�娼ч敍蹇涘礋椤掍胶妲�

					GlobalSession.avSession.acceptCall();

					if (SystemVarTools.mStartGroupCalllRepoort
							&& mTimerAudioPTTReport == null) {
						mTimerAudioPTTReport = new NgnTimer();
					}
					if (SystemVarTools.mStartGroupCalllRepoort
							&& SystemVarTools.mTakeAudioPTTFlag == false) {
						SystemVarTools.mTimerAudioPTTReport = mTimerAudioPTTReport;
					}
					if (SystemVarTools.mStartGroupCalllRepoort
							&& SystemVarTools.mTakeAudioPTTFlag == false) {
						mTimerAudioPTTReport.schedule(mTimerTaskAudioPTTReport,
								0, 8000); // 闂傚倷娴囧▔鏇㈠窗閹版澘鍑犲┑鐘宠壘缁狀垶鏌ｉ幋锝呅撻柡鍛倐閺岋繝宕掑Ο琛″亾閺嶎偀鍋撳顒傜Ш闁哄被鍔戦幃銏ゅ川婵犲嫪绱曢梻浣哥秺椤ユ捇宕楀锟芥瀬闁规崘顕уΛ姗�鏌曢崼婵囶棞妞ゅ繐鐖煎铏规崉閵娿儲鐎鹃梺鍝勵儏椤兘鐛箛娑欏�婚柤鎭掑劜濞呫垽姊洪崫鍕拷闁稿﹥鐗滈埀顒佽壘缂嶅﹪寮婚妸鈺傚亜闁告稑锕︽导鍕⒑瑜版帩妫戦柛蹇旓耿閺佹捇鏁�?8闂傚倷娴囧▔鏇㈠窗閹版澘鍑犲┑鐘宠壘缁狀垶鏌ｉ幋锝呅撻柡鍛倐閺岋繝宕掑Ο琛″亾閺嶎厽鏅搁柨锟�
						SystemVarTools.mTakeAudioPTTFlag = true;
					}

					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢紒锟藉�块弻锟犲醇閵忊剝姣勯梻浣瑰劤閸婂潡寮婚悢鐑樺皰濠㈣泛锕ら弳娆撴煟閹炬潙鐏撮柡灞剧☉閳诲氦绠涢幘鍓佹晨缂傚倷娴囨ご鍝ユ崲閸繍鍤曢柛顭戝亜缁剁偤鏌ㄩ悢鐑樻珪闁绘挷绶氬娲川婵犲嫧妲堥梺鍛婅壘椤戝懘鈥﹂崶顒佸仺闂傚牃鏅滅�氾拷
					String mobileNo = NgnUriUtils
							.getUserName(GlobalSession.avSession
									.getRemotePartyUri());
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_GROUP_INCOMING, BCDTools
									.Str2BCD(mobileNo, 10)));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_GROUP_INCOMING,
							"mobileNo = "
									+ mobileNo
									+ "; message = "
									+ "MSG_S_GROUP_INCOMING: groupcall/audio incoming."); // 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺冩嫚閼碱剟鐛撻梻渚�娼ч敍蹇涘礋椤掍胶妲�
				}
				if (NgnMediaType.isVideoType(mediaType)
						&& (mSessionType == SessionType.VideoCall)) { // 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐查唶闁哄洨鍋熸鍥⒑闂堟冻绱￠柛鏇ㄥ墯閻︼拷
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢紒锟藉�块弻锟犲醇閵忊剝姣勯梻浣瑰劤閸婂潡寮婚悢鐑樺皰濠㈣泛锕ら弳娆撴煟閹炬潙鐏撮柡灞剧☉閳诲氦绠涢幘鍓佹晨缂傚倷娴囨ご鍝ユ崲閸繍鍤曢柛顭戝亜缁剁偤鎮楅敐搴′喊婵″墽鍏橀弫鎾绘晸?
					String mobileNo = NgnUriUtils
							.getUserName(GlobalSession.avSession
									.getRemotePartyUri());
					byte[] mobileNoBytes = BCDTools.Str2BCD(mobileNo, 10);
					byte[] data = new byte[11]; // 11Bytes
					System.arraycopy(mobileNoBytes, 0, data, 0, 10);
					data[10] = 0; // 0闂傚倷鐒︾�笛呯矙閹烘挾鈹嶆繛宸悍缂嶆牠鎮楅敐搴′壕缂佹唻缍侀弻鐔告綇閹规劦鍚呴梺褰掝棑閸忔﹢骞冭ぐ鎺戠倞闂佸灝顑呴锟�
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_VIDEO_INCOMING, data));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_VIDEO_INCOMING,
							"mobileNo = "
									+ mobileNo
									+ "; message = "
									+ "MSG_S_VIDEO_INCOMING 0: video incoming for videocall."); // 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐查唶闁哄洨鍋熸鍥⒑闂堟冻绱￠柛鏇ㄥ墯閻︼拷
																								// 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐查唶闁哄洨鍠撻崝闈涒攽椤旂煫顏嗗椤撱垺鏅搁柨锟�
				}

			}
				break;

			case INPROGRESS: {
				final NgnAVSession avSession = GlobalSession.avSession;
				if (NgnMediaType.isAudioType(mediaType)
						&& (mSessionType == SessionType.AudioCall)) { // 闂備浇宕垫慨鏉懨归崒鐐茬煑闁告劦鍠楅崑锟藉┑鐘愁問閸犳鎹㈤幇顔肩筏闁芥ê顧�閼版寧銇勮箛鎾跺鐎瑰憡绻堥弻锝夊箻閹剁瓔锟介梺璺ㄥ櫐閹凤拷
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢�瑰憡绻堥弻锝夊箻閹剁瓔锟介悗鐐瑰�栧浠嬪蓟閻旂儤灏掑璺猴工閺嗘瑩鏌ｉ幘鏉戠伌闁哄本绋戦埢搴ょ疀閹惧墎娉跨紓鍌欐祰妞村摜鎹㈤崒鐑囩稏闁靛繈鍊栭弲鏌ユ煕濞戝崬鏋﹂柛姘煎亰濮婃椽宕ㄦ繝鍕╋拷闂佸憡顭嗛崱鎰睏?闂備浇顕ч柊锝咁焽瑜嶉敃銏ゆ焼瀹ュ孩鏅�?19800005002
					String mobileNo = NgnUriUtils.getUserName(avSession
							.getRemotePartyUri());
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_AUDIO_CALLING, BCDTools
									.Str2BCD(mobileNo, 10)));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_AUDIO_CALLING,
							"mobileNo = "
									+ mobileNo
									+ "; message = "
									+ "闂備浇宕垫慨鏉懨归崒鐐茬煑闁告劦鍠楅崑锟藉┑鐘愁問閸犳鎹㈤幇顔肩筏闁芥ê顧�閼版寧銇勮箛鎾跺鐎瑰憡绻堥弻锝夊箻閹剁瓔锟介梺璺ㄥ櫐閹凤拷");
				}

				if (NgnMediaType.isVideoType(mediaType)) { // 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｈ埖瀚�?濠电姵顔栭崰妤冩崲閹邦喖绶ら柦妯侯檧閼版寧銇勮箛鎾跺鐎瑰憡绻堥弻锝夊箻閹剁瓔锟介梺璺ㄥ櫐閹凤拷
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢�瑰憡绻堥弻锝夊箻閹剁瓔锟介悗鐐瑰�栧浠嬪蓟閻旂儤灏掑璺猴工閺嗘瑩鏌ｉ幘鏉戠伌闁哄本绋戦埢搴ょ疀閹惧墎娉跨紓鍌欐祰妞村摜鎹㈤崒鐑囩稏闁靛繈鍊栭弲鏌ユ煕濞戝崬鏋﹂柛姘煎亰濮婃椽宕ㄦ繝鍕╋拷闂佸憡顭嗛崱鎰睏?闂備浇顕ч柊锝咁焽瑜嶉敃銏ゆ焼瀹ュ孩鏅�?19800005002
					String mobileNo = NgnUriUtils.getUserName(avSession
							.getRemotePartyUri());
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_VIDEO_CALLING, BCDTools
									.Str2BCD(mobileNo, 10)));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_VIDEO_CALLING,
							"mobileNo = "
									+ mobileNo
									+ "; message = "
									+ "闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｈ埖瀚�?濠电姵顔栭崰妤冩崲閹邦喖绶ら柦妯侯檧閼版寧銇勮箛鎾跺鐎瑰憡绻堥弻锝夊箻閹剁瓔锟介梺璺ㄥ櫐閹�");
				}

				if (mSessionType == SessionType.AudioCall
						|| mSessionType == SessionType.VideoCall) {
					if (GlobalVar.bADHocMode) {
						hungupTimer = new NgnTimer();
						hungupTimer.schedule(new TimerTask() {

							@Override
							public void run() {
								if (GlobalSession.avSession != null
										&& GlobalSession.avSession
												.isConnected() == false) {
									GlobalSession.avSession.hangUpCall();
									MyLog.i("ServiceSocketMode INPROGRESS: ",
											"hangUpCall");
								}
							}
						}, 20 * 1000);
					}
				}

				if (NgnMediaType.isAudioType(mediaType)
						&& (mSessionType == SessionType.GroupAudioCall)) { // 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺冪磼濡桨姹楅梺鑽ゅС濞村洭锝炴径鎰櫢闁匡拷1闂傚倷鐒︾�笛呯矙閹烘埈娼╅柕濞炬櫅閺嬩線鏌ｉ姀鐘典粵閻庢碍宀搁弻褍顫濋锟藉亾娴犲鏅搁柨锟�
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢�瑰憡绻堥弻锝夊箻閹剁瓔锟介悗鐐瑰�栧浠嬪蓟閻旂儤灏掑璺猴工閺嗘瑩鏌ｉ幘鏉戠伌闁哄本绋戦埢搴ょ疀閹惧墎娉跨紓鍌欐祰妞村摜鎹㈤崒鐑囩稏闁靛繈鍊栭弲鏌ユ煕濞戝崬鏋﹂柛姘煎亰濮婃椽宕ㄦ繝鍕╋拷闂佸憡顭嗛崱鎰睏?闂備浇顕ч柊锝咁焽瑜嶉敃銏ゆ焼瀹ュ孩鏅�?19800005002
					String mobileNo = NgnUriUtils.getUserName(avSession
							.getRemotePartyUri());
					byte[] mobileNoBytes = BCDTools.Str2BCD(mobileNo, 10);
					byte[] data = new byte[11];
					data[0] = 1; // 1闂傚倷鐒︾�笛呯矙閹烘埈娼╅柕濞炬櫅閺嬩線鏌ｉ姀鐘典粵閻庢碍宀搁弻褍顫濋锟藉亾娴犲鏅搁柨锟�
					System.arraycopy(mobileNoBytes, 0, data, 1, 10);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_GROUP_STATE, data));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_GROUP_STATE,
							"mobileNo = " + mobileNo + "; message = "
									+ "MSG_S_GROUP_STATE 1: requesting."); // 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺冪磼濡桨姹楅梺鑽ゅС濞村洭锝炴径鎰櫢闁匡拷1闂傚倷鐒︾�笛呯矙閹烘埈娼╅柕濞炬櫅閺嬩線鏌ｉ姀鐘典粵閻庢碍宀搁弻褍顫濋锟藉亾娴犲鏅搁柨锟�
				}
			}
				break;

			case RINGING:
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_RING_NOTIF));

				MyLog.i("ServiceSocketMode: type = "
						+ BaseSocketMessage.MSG_S_RING_NOTIF,
						"receive 180,send open device notif");

				break;

			case CONNECTED: {
				isConnected = true;
				if (hungupTimer != null) {
					hungupTimer.cancel();
					hungupTimer.purge();
					hungupTimer = null;
					MyLog.d(TAG, "CONNECTED: set hungupTimer = null.");
				}
				NgnAVSession avSession_incall = GlobalSession.avSession;

				GlobalSession.avSession.setSpeakerphoneOn(false);
				if (avSession_incall.isGroupAudioCall()
						|| avSession_incall.isGroupVideoCall()
						|| avSession_incall.isVideoMonitorCall()
						|| avSession_incall.isMicrophoneMute()) {
					avSession_incall.setOnPause(true);
					avSession_incall.setGroupAudioTimerStart(true);
					avSession_incall.setOnResetJB();
					MyLog.d(TAG,
							"Group Call don't send RTP Packets at beginning [CONNECTED]");
				} else {
					avSession_incall.setOnPause(false);
					avSession_incall.setmSendVIdeo(true);
				}

				if (mSessionType == SessionType.GroupVideoCall) {
					avSession_incall.setmSendVIdeo(false);
					avSession_incall.startVideoProducerPreview();
					return;
				}

				if (NgnMediaType.isVideoType(mediaType)
						&& (mSessionType == SessionType.VideoSurveilMonitor || mSessionType == SessionType.VideoMonitor))// 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐查唶闁哄洨鍋熼崐鐐烘⒑缂佹ê濮堝鐟版閺佹捇鏁�?
				{
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢紒锟藉�块弻锟犲醇閵忊剝姣勯梻浣瑰劤閸婂潡寮婚悢鐑樺皰濠㈣泛锕ら弳娆撴煟閹炬潙鐏撮柡灞剧☉閳诲氦绠涢幘鍓佹晨缂傚倷娴囨ご鍝ユ崲閸繍鍤曢柛顭戝亜缁剁偤骞栭幖顓炴灍缂傚秴锕ユ穱濠囶敃閵堝拋鏆梺鎼炲姂濞佳呭弲闂佸搫绉查崝宀勵敋闁秵鐓熸俊顖涘閻濐亪姊婚崟顐ばч柨锟�
					avSession_incall.setmSendVIdeo(true);
					avSession_incall.startVideoProducerPreview();
					String mobileNo = NgnUriUtils
							.getUserName(GlobalSession.avSession
									.getRemotePartyUri());
					byte[] mobileNoBytes = BCDTools.Str2BCD(mobileNo, 10);
					byte[] data = new byte[11]; // 11Bytes
					System.arraycopy(mobileNoBytes, 0, data, 0, 10);
					data[10] = 1; // 0.video session 1閵嗕繄ideoSurveilMonitor
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_VIDEO_INCALL, data)); // MSG_S_VIDEO_SURVEIL_MONITOR
					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_VIDEO_INCOMING,
							"mobileNo = "
									+ mobileNo
									+ "; message = "
									+ "MSG_S_VIDEO_INCOMING: monitor/surveil incoming."); // 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐查唶闁哄洨鍋熸鍥⒑闂堟冻绱￠柛鏇ㄥ墯閻︼拷
																							// 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｃ劉鍋撻崒鐐查唶闁哄洨鍋熼崐鐐烘⒑缂佹ê濮堝鐟版閺佹捇鏁�?
					return;
				}

				if (NgnMediaType.isAudioType(mediaType)
						&& (mSessionType == SessionType.AudioCall)) { // 闂傚倷娴囧▔鏇㈠窗閹版澘鍑犲┑鐘宠壘缁狀垶鏌ｉ幋锝呅撻柡鍛倐閺岋繝宕掑Ο琛″亾閺嶎厽鏅搁柨锟介梻鍌欐祰濞夋洟宕伴幇鏉垮嚑濠电姵鑹剧粻顖滐拷閸曨垰浜版繛鍏碱殜濮婃椽顢曢敐鍛懙闂佽桨鐒﹂幑鍥ь嚕椤掑嫬围闁糕剝顨忔导锟�
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢�瑰憡绻堥弻锝夊箻閹剁瓔锟介悗鐐瑰�栧浠嬪蓟閻旂儤灏掑璺猴工閺嗘瑩鏌ｉ幘鏉戠伌闁哄本绋戦埢搴ょ疀閹惧墎娉跨紓鍌欐祰妞村摜鎹㈤崒鐑嗘晪闁挎繂顦伴弲鍝ョ磼濞戞﹩鍎愮紓宥呯箻濮婄粯绗熼崶褌绨兼繛锝呮处濡炰粙鏁�?闂備浇顕ч柊锝咁焽瑜嶉敃銏ゆ焼瀹ュ孩鏅�?19800005002
					String mobileNo = NgnUriUtils.getUserName(avSession_incall
							.getRemotePartyUri());

					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_AUDIO_INCALL, BCDTools
									.Str2BCD(mobileNo, 10)));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_AUDIO_INCALL,
							"mobileNo = " + mobileNo + "; message = "
									+ "MSG_S_AUDIO_INCALL");
					return;
				}

				if (NgnMediaType.isVideoType(mediaType)) { // 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｈ埖瀚�?闂佽瀛╅鏍闯椤栫偛绠柣鎴炴灮缂嶆牠鏌熼柇锕�鏋熼柛鐘叉閺屾盯鍩ラ崱妤�浼庨梺璺ㄥ櫐閹凤拷

					avSession_incall.startVideoProducerPreview(); // 闂傚倷娴囨竟鍫熴仈缁嬭娑欐媴鐟欏嫬寮块梻渚囧墮缁夊绮堥崒娑栦簻闁硅櫣鍋炵�氬綊鎮楅崹顐ｇ凡闁哥噥鍨伴銉╁礋椤曞懏鞋缂傚倷绶￠崰妤呭箰閸撗冨灊闁冲搫鎷嬮弫宥嗙節閸偄濮堢紓宥咃攻娣囧﹪顢曢妶鍜佹毉闂佹悶鍔庨鏌ュ箯瀹勬壆鏆嗛柛鏇″煐鐎氾拷闂備浇宕甸崑鐐哄礄瑜版帒纾婚柛鏇ㄥ枔娴滃綊鏌曟繛鐐珕闁哄拋鍓熼幃姗�鎮欓弶鎴濆Б闂佹剚浜為。顕�骞忛悜绛嬫晪闁糕剝锚椤忓爼姊洪悷鏉跨骇鐎癸拷顭堥悾鐑藉箻閸︻厾鏉搁梺瑙勫劤瑜板鍒掗幘缁樷拺閻犳亽鍔屽▍鎰版煙閸戙倖瀚�
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢�瑰憡绻堥弻锝夊箻閹剁瓔锟介悗鐐瑰�栧浠嬪蓟閻旂儤灏掑璺猴工閺嗘瑩鏌ｉ幘鏉戠伌闁哄本绋戦埢搴ょ疀閹惧墎娉跨紓鍌欐祰妞村摜鎹㈤崒鐑嗘晪闁挎繂顦伴弲鍝ョ磼濞戞﹩鍎愮紓宥呯箻閺岋綁鎮㈤崫銉﹀櫑濠碘槅鍋呴幐鎶藉极閹剧粯鍋╃�癸拷顑嗙�氬綊姊婚崼鐔衡檨閻犳劧绻濋弫鎾绘晸?19800005002
					String mobileNo = NgnUriUtils.getUserName(avSession_incall
							.getRemotePartyUri());
					byte[] mobileNoBytes = BCDTools.Str2BCD(mobileNo, 10);
					byte[] data = new byte[11]; // 11Bytes
					System.arraycopy(mobileNoBytes, 0, data, 0, 10);
					data[10] = 0; // 0.video session 1閵嗕繄ideoSurveilMonitor
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_VIDEO_INCALL, data));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_VIDEO_INCALL,
							"mobileNo = " + mobileNo + "; message = "
									+ "video in call: Connected");
					return;
				}
				if (NgnMediaType.isAudioType(mediaType)
						&& mSessionType == SessionType.GroupAudioCall) {
					String mobileNo = NgnUriUtils.getUserName(avSession_incall
							.getRemotePartyUri());
					byte[] mobileNoBytes = BCDTools.Str2BCD(mobileNo, 10);
					byte[] data = new byte[11];
					data[0] = 4; // 4 缂佸本妞藉Λ鑺ョ▔?
					System.arraycopy(mobileNoBytes, 0, data, 1, 10);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_GROUP_STATE, data));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_GROUP_STATE,
							"mobileNo = " + mobileNo + "; message = "
									+ "MSG_S_GROUP_STATE 4: released.");
				}
			}

				break;

			case EARLY_MEDIA: //
				GlobalSession.avSession.setSpeakerphoneOn(false);
				GlobalSession.avSession.setOnPause(false);
				break;
			case ENCRYPT_INFO:
				break;

			case SIP_RESPONSE: { // sks add
				// if (NgnMediaType.isAudioType(mediaType)) { //
				// 0: 487 poweroff 1:486 busy 2:404 notfound 3:480 timeout 4:
				// invalid

				short sipCode = intent.getShortExtra(
						NgnInviteEventArgs.EXTRA_SIPCODE, (short) 0);
				if (sipCode != NgnSipCode.sipCode_poweroff
						&& sipCode != NgnSipCode.sipCode_busy
						&& sipCode != NgnSipCode.sipCode_notfound
						&& sipCode != NgnSipCode.sipCode_timeout) {
					return;
				}
				byte[] data = new byte[1];
				data[0] = 1;
				if (sipCode == NgnSipCode.sipCode_poweroff
						|| sipCode == NgnSipCode.sipCode_notfound) {
					data[0] = 0;
				} else if (sipCode == NgnSipCode.sipCode_busy) {
					data[0] = 1; //
				} else if (sipCode == NgnSipCode.sipCode_timeout) {
					data[0] = 3;
				}
				if (NgnMediaType.isAudioType(mediaType)
						&& (mSessionType == SessionType.AudioCall)) { // 闂備浇宕垫慨鏉懨归崒鐐茬煑闁告劦鍠楅崑锟介梻鍌欑閹诧紕绮欓幘璇插偍鐟滄柨鐣烽悽鍓叉晣闁绘棃顥撶粣鐐烘煛婢跺﹦澧遍柛瀣槼椤わ拷
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢�瑰憡绻堥弻锝夊箻閹剁瓔锟介悗鐐瑰�栧浠嬪蓟閻旂儤灏掑璺猴工閺嗘瑩鏌ｉ幘鏉戠伌闁哄本绋戦埢搴ょ疀閹惧墎娉跨紓鍌欐祰妞村摜鎹㈤崒鐑嗘晪闁挎繂顦伴弲鍝ョ磼濞戞﹩鍎愮紓宥呯箻濮婄粯绗熼崶褌绨兼繛锝呮处濡炰粙鏁�?闂備浇顕ч柊锝咁焽瑜嶉敃銏ゆ焼瀹ュ孩鏅�?19800005002

					// data[0] = 1; //
					// 1闂傚倷鐒︾�笛呯矙閹烘鍤屽Δ锝呭枤閺佸銇勯弬鍨挃闁告纰嶉妵鍕冀閵娧�妲堥梺缁樼箖濞茬喖鏁�?
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢紒锟藉�块弻锟犲醇閵忊剝姣勯梻浣瑰劤閸婂潡寮婚悢鐑樺皰濠㈣泛锕ら弳娆撴煟閹炬潙鐏撮柡灞剧☉閳诲氦绠涢幘鍓佹晨缂傚倷娴囨ご鍝ユ崲閸儱钃熼柛娑卞弾濞尖晝锟介弴鐐村闁崇粯鎹囧娲传閸曨偂绨介梺鍛婎焾閸嬫劙鏁�?
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_AUDIO_CALLFAILED, data));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_AUDIO_CALLFAILED,
							"mobileNo = " + "" + "; message = " + data[0]
									+ "REMOTE_REFUSE");
				}

				if (NgnMediaType.isVideoType(mediaType)) { // 闂備浇宕甸崰鎰版偡閵夈儙娑樷攽鐎ｈ埖瀚�?闂傚倷绀侀幉锛勭矙閹捐鍌ㄧ憸鏂跨暦閻㈠壊鏁囬柣鏃堫棑缁愮偤鏌℃径濠勫⒈闁稿顦抽·锟�
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢�瑰憡绻堥弻锝夊箻閹剁瓔锟介悗鐐瑰�栧浠嬪蓟閻旂儤灏掑璺猴工閺嗘瑩鏌ｉ幘鏉戠伌闁哄本绋戦埢搴ょ疀閹惧墎娉跨紓鍌欐祰妞村摜鎹㈤崒鐑嗘晪闁挎繂顦伴弲鍝ョ磼濞戞﹩鍎愮紓宥呯箻閺岋綁鎮㈤崫銉﹀櫑濠碘槅鍋呴幐鎶藉极閹剧粯鍋╃�癸拷顑嗙�氬綊姊婚崼鐔衡檨閻犳劧绻濋弫鎾绘晸?19800005002
					// byte[] data = new byte[1];
					// data[0] = 1; //
					// 1闂傚倷鐒︾�笛呯矙閹烘鍤屽Δ锝呭枤閺佸銇勯弬鍨挃闁告纰嶉妵鍕冀閵娧�妲堥梺缁樼箖濞茬喖鏁�?
					if (phrase.equals("cameraerror")) {
						data[0] = 5; // 5闂傚倷鐒︾�笛呯矙閹烘鍤岄柛鎾楀懐顦柣搴秵閸犳牜绮堥崼鈶╁亾楠炲灝鍔氭俊顐ｇ懇瀹曨垶寮崼鐔哄幈閻庡箍鍎遍幊鎰不娴煎瓨鐓忛柛銉ｅ妽閳锋劗绱掔�ｎ亶妲告い鎾炽偢瀹曘劑顢橀埥鍡楁暭闂佽法鍣﹂幏锟�
					}
					// 闂傚倸鍊风欢锟犲磻閸涱収娼╅柕濞炬櫆閸嬪倿鏌ㄩ悢鍝勑㈢紒锟藉�块弻锟犲醇閵忊剝姣勯梻浣瑰劤閸婂潡寮婚悢鐑樺皰濠㈣泛锕ら弳娆撴煟閹炬潙鐏撮柡灞剧☉閳诲氦绠涢幘鍓佹晨缂傚倷娴囨ご鍝ユ崲閸儱钃熼柛娑卞弾濞尖晝锟介弴鐐村闁崇粯鎹囧娲传閸曨偂绨介梺鍛婎焾閸嬫劙鏁�?
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_VIDEO_CALLFAILED, data));

					String message = "Camera open OK!";
					if (phrase.equals("cameraerror")) {
						message = "Camera open error!";
					}
					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_VIDEO_CALLFAILED,
							"mobileNo = " + "" + "; message = " + message);
				}
			}
				break;

			default:
				break;

			}

			// 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺呭礃閼碱剛鐛梻浣告啞濞诧箓宕㈡禒瀣櫢闁匡拷from ScreenAV
			if (GlobalSession.bSocketService) {
				if (GlobalVar.bADHocMode && hungupStatus != 0 && !isConnected) {
					Log.d("HX-0328", "GlobalVar.bADHocMode = true!!!!!!!!!");

					adhocRingtone(context, intent);

				} else {
					isConnected = false;
					Log.d("HX-0328", "GlobalVar.bADHocMode = false!!!!!!!!!");
					handleSipEvent(intent);
				}

			}
		} else if (NgnSubscriptionEventArgs.ACTION_SUBSCRIBTION_EVENT
				.equals(action)) {
			NgnSubscriptionEventArgs args = intent
					.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				return;
			}
			switch (args.getEventType()) {
			// SUBSCRIPTION_OK, SUBSCRIPTION_NOK, SUBSCRIPTION_INPROGRESS,
			// UNSUBSCRIPTION_OK, UNSUBSCRIPTION_NOK, UNSUBSCRIPTION_INPROGRESS,
			// INCOMING_NOTIFY
			case SUBSCRIPTION_INPROGRESS:
				break;
			case SUBSCRIPTION_OK:
				break;
			case INCOMING_NOTIFY:
				args.getEventPackage();
				if (args.getContent() == null)
					break;
				String notifyContent = new String(args.getContent());
				String contentType = args.getContentType();
				Log.d(TAG, "NOTIFY contentType = " + contentType);
				Log.e(TAG, "NOTIFY body = " + notifyContent);
				// Log.d("INCOMING_NOTIFY",String.format("GLE---type=%s,content=%s",notifyType,notifyContent));
				ParserSubscribeState.getInstance().parserGroupMemberState2(
						contentType, notifyContent);

				// if (contentType.equals("application/xcap-diff+xml")) {
				pushcontacts(false);
				// }
				break;
			case SUBSCRIPTION_NOK:

				break;
			case UNSUBSCRIPTION_OK:
			case UNSUBSCRIPTION_NOK:
			case UNSUBSCRIPTION_INPROGRESS:
			default:
				break;
			}
		} else if (MessageTypes.MSG_GIS_EVENT.equals(action)) {
			MyLog.d(TAG, "Receive message=" + MessageTypes.MSG_GIS_EVENT);
			int msg = intent.getIntExtra(MessageTypes.MSG_GIS_TYPE, 0);
			switch (msg) {
			case MessageTypes.MSG_GIS_REQUEST_START:
				if (mGisRequestTimer == null) {
					MyLog.d(TAG, "=== GIS request timer start===");
					mGisRequestTimer = new NgnTimer();
					mGisRequestTimer.schedule(new TimerTask() {

						@Override
						public void run() {
							SocketServer.sendMessage(new BaseSocketMessage(
									BaseSocketMessage.MSG_C_GIS_REQUEST));
							MyLog.i("ServiceSocketMode: type = "
									+ BaseSocketMessage.MSG_C_GIS_REQUEST,
									"message = " + "MSG_C_GIS_REQUEST");
						}
					}, 0, 1000);
				} else {
					MyLog.d(TAG, "=== GIS request timer has been existed. === ");
				}
				break;
			case MessageTypes.MSG_GIS_REQUEST_STOP:
				if (mGisRequestTimer != null) {
					MyLog.d(TAG, "=== GIS request timer stoped. === ");
					mGisRequestTimer.cancel();
					mGisRequestTimer = null;
				} else {
					MyLog.d(TAG, "=== GIS request timer is null. === ");
				}
				break;
			default:
				break;
			}

		} else if (AdhocSessionEventArgs.ADHOC_SESSION_EVENT.equals(action)) {
			handleAdhocSessionEvent(intent);
		}

	}

	/**
	 * 閻忓繐妫濋埀顒佷亢椤斿棜銇愰弴鐐茬岛闂侇偂绀侀崺宀勬焻閸岀偛甯抽弶鐑嗗灟濞嗭拷濞戞挻鑹炬慨鐔告姜椤栨瑦顐�->闂侇偄鍊块崢銈嗘姜椤栨瑦顐�
	 */

	private void downloadContacts() {
		Thread thread = new Thread("MSG_DOWNLOAD_CONTACTS thread start...") { // 闂傚倷绀侀崥瀣磿閹惰棄搴婇柤鑹扮堪娴滃綊鏌涢妷锝呭闁崇粯姊婚埀顒�绠嶉崕閬嶅箠鎼淬劍鍋傞柛宀�鍋為埛鎴炪亜閹板墎瀵煎ù婧垮灪閵囧嫰寮幐搴％闂侀潧娲﹂崝娆忣嚕閹绢噮鏁傞柛鈩冾殣閹凤拷android.os.NetworkOnMainThreadException
			public void run() {

				Log.d("ServiceSocketMode--onReceive()",
						" MSG_DOWNLOAD_CONTACTS-Startting");
				if (ScreenDownloadConcacts.getInstance().downloadPublicGroup()) {
					// 闂傚倸鍊风欢锟犲磻閸曨垰纾归柛褎顨呴崒銊╂煏閸繃顥炲┑顔界矋閵囧嫰寮介妸褎鍣銈嗗姃缁瑩寮婚妶澶婄畾鐟滃秴危閼姐倖鍠愰柡澶嬪閸犳﹢鏌＄仦鑺ュ櫣妞ゎ偅绻堥獮鍥ㄦ媴閸涘﹥姣勯梺璺ㄥ櫐閹凤拷
					Log.d("ServiceSocketMode--onReceive()",
							" MSG_DOWNLOAD_CONTACTS_FINISH");
					List<NodeResource> resList = ScreenDownloadConcacts
							.getInstance().parserContactsTree();
					SystemVarTools.setContactAll(resList);
				}

				Log.d("ServiceSocketMode--onReceive()",
						" MSG_DOWNLOAD_CONTACTS-Startting");
				if (ScreenDownloadConcacts.getInstance().downloadServiceGroup()) {
					// 闂傚倸鍊风欢锟犲磻閸曨垰纾归柛褎顨呴崒銊╂煏閸繃顥炲┑顔界矋閵囧嫰寮介妸褎鍣銈嗗姃缁瑩寮婚妶澶婄畾鐟滃秴危閼姐倖鍠愰柡澶嬪閸犳﹢鏌＄仦鑺ュ櫣妞ゎ偅绻堥獮鍥ㄦ媴閸涘﹥姣勯梺璺ㄥ櫐閹凤拷婵犵數鍋為崹鍫曞箰婵犳碍鍤岄柣鎰靛墯閸欙拷
					Log.d("ServiceSocketMode--onReceive()",
							" MSG_DOWNLOAD_CONTACTS_FINISH net");
					List<NodeResource> resListNet = ScreenDownloadConcacts
							.getInstance().parserContactsNetTree();
					SystemVarTools.setContactBussiness(resListNet);
				}

				Log.d("ServiceSocketMode--onReceive()",
						" MSG_DOWNLOAD_CONTACTS-Startting");
				if (ScreenDownloadConcacts.getInstance().downloadGlobalGroup()) {
					Log.d("ServiceSocketMode--onReceive()",
							" MSG_DOWNLOAD_CONTACTS_FINISH- globalgroup");

					List<NodeResource> resListNet = ScreenDownloadConcacts
							.getInstance().parserContactsGlobalGroupTree();
					SystemVarTools.setContactGlobalGroupOrg(resListNet);

				}
				// String mIdentity = Engine.getInstance()
				// .getConfigurationService()
				// .getString(NgnConfigurationEntry.IDENTITY_IMPI,
				// NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI); //
				// "闂傚倷鑳堕崢褔銆冩惔銏㈩洸婵犲﹤鎳忓畷锟�
				// String mNetworkRealm = Engine
				// .getInstance()
				// .getConfigurationService()
				// .getString(NgnConfigurationEntry.NETWORK_REALM,
				// NgnConfigurationEntry.DEFAULT_NETWORK_REALM); //
				// "sunkaisens.com"
				//
				// final NgnSubscriptionSession subscriptionSessionService =
				// NgnSubscriptionSession
				// .createOutgoingSession(Engine.getInstance().getSipService()
				// .getSipStack(), "sip:" + mIdentity + "@"
				// + mNetworkRealm, "sip:service-group@" + mNetworkRealm,
				// NgnSubscriptionSession.EventPackageType.Group);
				// subscriptionSessionService.subscribe();
				// MyLog.d(TAG,
				// "闂傚倷绀侀幉锟犳偡閿曞倸鍨傞柛褎顨嗛弲鍝ョ磼閼碱剙鐨焤vice闂備浇宕垫慨鎶芥⒔瀹ュ鍨傞柦妯侯檧閹凤拷);
				//
				// final NgnSubscriptionSession subscriptionSessionPublic =
				// NgnSubscriptionSession
				// .createOutgoingSession(Engine.getInstance().getSipService()
				// .getSipStack(), "sip:" + mIdentity + "@"
				// + mNetworkRealm, "sip:public-group@" + mNetworkRealm,
				// NgnSubscriptionSession.EventPackageType.Group);
				// subscriptionSessionPublic.subscribe();
				// MyLog.d(TAG,
				// "闂傚倷绀侀幉锟犳偡閿曞倸鍨傞柛褎顨嗛弲鍝ョ磼瀹割喗绨沚lic闂備浇宕垫慨鎶芥⒔瀹ュ鍨傞柦妯侯檧閹凤拷);

				SystemVarTools.subscribePublicGroup();

				SystemVarTools.subscribeServiceGroup();

				SystemVarTools.subscribeGlobalGroup();

				pushcontacts(true);

			};
		};

		thread.start();
	}

	/**
	 * 閸氭垵銇囩紒鍫㈩伂娑撴艾濮熼柅鍌炲帳鏉烆垯娆㈤幒銊╋拷闁俺顔嗚ぐ鏇炴嫲瑜版挸澧犻梿鍡欏參缂侊拷
	 */

	public static void pushcontacts(boolean init) {
		byte[] dataContactAllBus = SystemVarTools.getOrgContactAllBus2(0);
		byte[] dataOrgAllBus = SystemVarTools.getOrgContactAllBus2(1);
		// String localMobileNo =
		// Engine.getInstance().getConfigurationService().getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
		// NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
		// if(init){
		// byte[] dataok = new byte[1];
		// dataok[0] = 0; //
		// 0闂傚倷鐒︾�笛呯矙閹烘柨鏋堢�广儱顦弸渚�鏌ｉ幇顒佹儓缂侊拷鍟撮幃姗�鎮欓弶鎴濆Б闂佽绻愬ú顓㈡晸?1闂傚倷鐒︾�笛呯矙閹烘鍤屽Δ锝呭暞閸庡﹪鏌熸潏楣冩闁稿孩锚閵嗘帒顫濋悙顒�顏堕梻浣芥〃缁�渚�鏁冮鍫熸櫢闁匡拷2闂傚倷鐒︾�笛呯矙閹烘埈娼╅柕濞炬櫅閺嬩線鏌曢崼婵愭Ц缂佺媴缍侀弻褍顫濋锟藉亾婵犳碍鍋℃繝闈涱儐閸婂灚绻涢幋鐑嗕痪妞ゅ繐瀚搁懓锟�5闂傚倷鐒︾�笛呯矙閹烘埈娼╅柕濞炬櫅閺嬩線鏌曢崼婵愭Ц缂佺媴绲块埀顒傛嚀鐎氼厼顭垮锟藉惞闁圭儤顨嗛悡鐔烘喐閹达富鏁嬬憸蹇涙晸?6闂傚倷鐒︾�笛呯矙閹烘柨鏋堢�广儱顦弸渚�鏌ｉ幇顒佹儓缂侊拷鍟撮幃姗�鎮欏顔界稐婵炲瓨绮岄妶鎼佹晸?
		// SocketServer.sendMessage(new
		// BaseSocketMessage(BaseSocketMessage.MSG_S_USER_REGRESULT, dataok));
		// //闂傚倷鑳堕崕鐢稿磻閹捐绀夌�广儱顦弰銉︾箾閹寸們姘跺几閺嶎厽鐓忓┑鐐茬仢閸旀岸鏌ㄩ悢鍓佺煓鐎殿喖鐖奸崺锟犲磼濞戞艾寮虫繝鐢靛仜瀵爼鎮ч弴銏犵叀濠㈣埖鍔曢柋鍥煟閺冨浂鍟囬柨锟�
		//
		// MyLog.i("ServiceSocketMode: type = " +
		// BaseSocketMessage.MSG_S_USER_REGRESULT, "mobileNo = " + "" +
		// "; message = " + "");
		// SocketServer.sendMessage(new
		// BaseSocketMessage(BaseSocketMessage.MSG_S_INIT_STATE,
		// BCDTools.Str2BCD(localMobileNo, 10)));
		// //闂備浇宕垫慨鎶芥倿閿曞倸纾块柟璺哄閸ヮ剦鏁嗗ù锝堛��閺�鎶芥⒑閺傘儲娅呴柛鐔叉櫊閺佹捇鏁�?
		//
		// MyLog.i("ServiceSocketMode: type = " +
		// BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = " + "" + "; message = "
		// + "鐟滅増鎸告晶鐘绘偨閵婏箑鐓曢柛娆擃棑閻栵拷" + localMobileNo);
		// }

		if (dataContactAllBus != null) {

			try {
				Log.e(TAG,
						"dataContactAllBus = "
								+ Arrays.toString(dataContactAllBus));

				int personnum = dataContactAllBus.length - 1; // 闂傚倷鑳堕崢褔骞栭锕�纾瑰┑鐘宠壘绾惧鏌ㄥ┑鍡╂Ц缂佺姵鍨甸埞鎴︽偐瀹曞浂鏆″┑顕嗙稻閸旀瑩鏁�?

				int nodenum = (personnum / SystemVarTools.ARR_COLUMN_LEN); // 闂傚倷绀侀幉锛勬暜閹烘嚦娑樷槈濡攱鐏侀梺纭呮彧缁犳垹绮堝畝鍕厱婵炴垵宕弸鐔兼煥閻曞倹瀚�
				MyLog.d(TAG, "nodenum = " + nodenum);
				int baoshu = nodenum / 50;
				MyLog.d(TAG, "AllContactsLength = " + dataContactAllBus.length);

				baoshu++;
				Log.e(TAG, "PacketCounts = " + baoshu);
				List<byte[]> dataContactAllBusList = new ArrayList<byte[]>();

				Set<byte[]> set = new HashSet<byte[]>();

				byte[] temp = null;
				for (int i = 0; i < baoshu; i++) {
					// Log.e("for yunhuan i zhi:", ""+i);
					if (i == baoshu - 1) { // 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄閸涘﹦浼勯梺璺ㄥ櫐閹凤拷
						int length = dataContactAllBus.length - 1 - i * 50
								* SystemVarTools.ARR_COLUMN_LEN; // 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄缂佹﹩妫勯梺浼欑畱閻栧ジ寮诲☉銏犵妞ゆ牗绋掗悵鎺旂磽閸屾氨校闁搞劌鐏濋～蹇氥亹閹烘垹顦遍梺瀹犳閹冲繘鏁�?
						// temp
						// =
						// new
						// byte[length
						// +
						// 1];
						temp = new byte[length + 1];
						// int lastnum = length / SystemVarTools.ARR_COLUMN_LEN;
						// //
						// 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄缂佹﹩妫勯梺浼欑畱閻栧ジ寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃椤掍礁顏�?
						int lastnum = nodenum; // 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄缂佹﹩妫勯梺浼欑畱閻栧ジ寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃椤掍礁顏�?
												// 闂傚倷绀侀幉锟犮�冮崨鏉戝偍闁哄稁鍋呯�氬鏌ｉ弮鍌氬付缂佺姵鍨块弻銈嗘叏閹邦兘鍋撻弽顓熷仼闂侇剙绉甸悡鏇㈡煛瀹擃喖瀚▍銈夋煥閻曞倹瀚�?
						temp[0] = (byte) lastnum;
						System.arraycopy(dataContactAllBus, i * 50
								* SystemVarTools.ARR_COLUMN_LEN + 1, temp, 1,
								length);
						// Log.e("temp shuzu:", Arrays.toString(temp));
					} else {
						temp = new byte[SystemVarTools.ARR_COLUMN_LEN * 50 + 1];
						int lastnum = (i + 1) * 50; // 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄缂佹﹩妫勯梺浼欑畱閻栧ジ寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃椤掍礁顏�?
													// 闂佽崵鍠愮划搴㈡櫠濡ゅ懎绠伴柛娑橈攻濞呯娀鏌ｅΟ娆惧殭缂侊拷鐭傞弻娑樷槈閸楃偟浠梺浼欑秮娴滃爼寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃閿曪拷楠囬梺鍓茬厛閸ｎ喖顭囨潏銊х闁瑰鍋為悡銉︺亜閹存繃鍣洪柡鍛埣閹晝绱掑Ο鍝勫闂備礁鎲″ú锕傚磻閸曨垱鍎婇悹浣筋潐鐎氾拷
						temp[0] = (byte) lastnum;
						System.arraycopy(dataContactAllBus, i * 50
								* SystemVarTools.ARR_COLUMN_LEN + 1, temp, 1,
								SystemVarTools.ARR_COLUMN_LEN * 50);
						// Log.e("temp shuzu:", Arrays.toString(temp));
					}

					dataContactAllBusList.add(temp);

				}

				for (int j = 0; j < baoshu; j++) {
					set.add(dataContactAllBusList.get(j));
					for (byte[] data : set) {
						MyLog.d(TAG, "send contact list data");
						SocketServer.sendMessage(new BaseSocketMessage(
								BaseSocketMessage.MSG_S_INIT_CONTACT, data));
					}

					// 婵犵數鍋為崹鍫曞箰閹间焦鏅濋柕澶涢檮閸欏繘鏌曢崼婵愭Ч闁搞倕鍊块弻锟犲礃閵娿儮鍋撻崸妤�绀傞柛銉亹閻熼偊鐓ラ柛鈩兦滈埀顒�鍟彁闁搞儜宥堝惈闂佽法鍣﹂幏锟�

					MyLog.d(TAG,
							"packet["
									+ j
									+ "] = "
									+ Arrays.toString(dataContactAllBusList
											.get(j)));

				}

			} catch (Exception e) {
				e.printStackTrace();
				Log.e("try yichang", "try yichang:");
			}

			// SocketServer.sendMessage(new
			// BaseSocketMessage(BaseSocketMessage.MSG_S_INIT_CONTACT,
			// dataContactAllBus));
			// //闂傚倷娴囨竟鍫熴仈缁嬫娼栧┑鐘崇閻掗箖鎮楀☉娆樼劷妞も晜鐓￠弻娑樷攽閸℃浠奸梺璇茬箲閻熲晠鏁�?
		} else {

			byte[] temp = new byte[SystemVarTools.ARR_COLUMN_LEN * 50 + 1];
			int lastnum = 0; // 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄缂佹﹩妫勯梺浼欑畱閻栧ジ寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃椤掍礁顏�?
								// 闂佽崵鍠愮划搴㈡櫠濡ゅ懎绠伴柛娑橈攻濞呯娀鏌ｅΟ娆惧殭缂侊拷鐭傞弻娑樷槈閸楃偟浠梺浼欑秮娴滃爼寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃閿曪拷楠囬梺鍓茬厛閸ｎ喖顭囨潏銊х闁瑰鍋為悡銉︺亜閹存繃鍣洪柡鍛埣閹晝绱掑Ο鍝勫闂備礁鎲″ú锕傚磻閸曨垱鍎婇悹浣筋潐鐎氾拷
			temp[0] = (byte) lastnum;
			System.arraycopy("0".getBytes(), 0, temp, 1, "0".getBytes().length);

			SocketServer.sendMessage(new BaseSocketMessage(
					BaseSocketMessage.MSG_S_INIT_CONTACT, temp)); // 婵犵數鍋為崹鍫曞箰閹间焦鏅濋柕澶涢檮閸欏繘鏌曢崼婵愭Ч闁搞倕鍊块弻锟犲礃閵娿儮鍋撻崸妤�绀傞柛銉亹閻熼偊鐓ラ柛鈩兦滈埀顒�鍟彁闁搞儜宥堝惈闂佽法鍣﹂幏锟�
			Log.e("", "MSG_S_INIT_CONTACT null");
		}
		if (dataOrgAllBus != null) {

			try {
				MyLog.d(TAG, "OrgAllBus = " + Arrays.toString(dataOrgAllBus));
				int personnum = dataOrgAllBus.length - 1; // 闂傚倷鑳堕崢褔骞栭锕�纾瑰┑鐘宠壘绾惧鏌ㄥ┑鍡╂Ц缂佺姵鍨甸埞鎴︽偐瀹曞浂鏆″┑顕嗙稻閸旀瑩鏁�?

				int nodenum = (personnum / SystemVarTools.ARR_COLUMN_ORG); // 闂傚倷绀侀幉锛勬暜閹烘嚦娑樷槈濡攱鐏侀梺纭呮彧缁犳垹绮堝畝鍕厱婵炴垵宕弸鐔兼煥閻曞倹瀚�
				Log.e(TAG, "nodenum = " + nodenum);
				int baoshu = nodenum / 50;
				Log.e(TAG, "dataOrgAllBus length = " + dataOrgAllBus.length);

				baoshu++;
				Log.e(TAG, "PacketCounts = " + baoshu);
				List<byte[]> dataContactAllBusList = new ArrayList<byte[]>();
				byte[] temp = null;
				for (int i = 0; i < baoshu; i++) {
					// Log.e("for yunhuan i zhi:", ""+i);
					if (i == baoshu - 1) { // 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄閸涘﹦浼勯梺璺ㄥ櫐閹凤拷
						int length = dataOrgAllBus.length - 1 - i * 50
								* SystemVarTools.ARR_COLUMN_ORG; // 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄缂佹﹩妫勯梺浼欑畱閻栧ジ寮诲☉銏犵妞ゆ牗绋掗悵鎺旂磽閸屾氨校闁搞劌鐏濋～蹇氥亹閹烘垹顦遍梺瀹犳閹冲繘鏁�?
						// temp
						// =
						// new
						// byte[length
						// +
						// 1];
						temp = new byte[length + 1];
						// int lastnum = length / SystemVarTools.ARR_COLUMN_LEN;
						// //
						// 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄缂佹﹩妫勯梺浼欑畱閻栧ジ寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃椤掍礁顏�?
						int lastnum = nodenum; // 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄缂佹﹩妫勯梺浼欑畱閻栧ジ寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃椤掍礁顏�?
												// 闂傚倷绀侀幉锟犮�冮崨鏉戝偍闁哄稁鍋呯�氬鏌ｉ弮鍌氬付缂佺姵鍨块弻銈嗘叏閹邦兘鍋撻弽顓熷仼闂侇剙绉甸悡鏇㈡煛瀹擃喖瀚▍銈夋煥閻曞倹瀚�?
						temp[0] = (byte) lastnum;
						System.arraycopy(dataOrgAllBus, i * 50
								* SystemVarTools.ARR_COLUMN_ORG + 1, temp, 1,
								length);
						// Log.e("temp shuzu:", Arrays.toString(temp));
					} else {
						temp = new byte[SystemVarTools.ARR_COLUMN_ORG * 50 + 1];
						int lastnum = (i + 1) * 50; // 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄缂佹﹩妫勯梺浼欑畱閻栧ジ寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃椤掍礁顏�?
													// 闂佽崵鍠愮划搴㈡櫠濡ゅ懎绠伴柛娑橈攻濞呯娀鏌ｅΟ娆惧殭缂侊拷鐭傞弻娑樷槈閸楃偟浠梺浼欑秮娴滃爼寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃閿曪拷楠囬梺鍓茬厛閸ｎ喖顭囨潏銊х闁瑰鍋為悡銉︺亜閹存繃鍣洪柡鍛埣閹晝绱掑Ο鍝勫闂備礁鎲″ú锕傚磻閸曨垱鍎婇悹浣筋潐鐎氾拷
						temp[0] = (byte) lastnum;
						System.arraycopy(dataOrgAllBus, i * 50
								* SystemVarTools.ARR_COLUMN_ORG + 1, temp, 1,
								SystemVarTools.ARR_COLUMN_ORG * 50);
						// Log.e("temp shuzu:", Arrays.toString(temp));
					}

					dataContactAllBusList.add(temp);

				}

				for (int j = 0; j < baoshu; j++) {
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_INIT_GROUP,
							dataContactAllBusList.get(j))); // 婵犵數鍋為崹鍫曞箰閹间焦鏅濋柕澶涢檮閸欏繘鏌曢崼婵囧窛缁炬儳銈搁弻娑㈠即閵娿儱绠婚梺娲诲幖缁绘﹢寮婚妶鍡樼秶闁靛鑵归幏濠氭⒒娓氬洤鏋涢柣掳鍔庣划瀣吋閸℃ê顫℃俊顐︻暒閼冲墎绮诲鑸垫櫢闁匡拷

					// String contactliString=
					// MessageTools.bytes2HexString(dataContactAllBusList.get(j));
					Log.e(TAG,
							"packet["
									+ j
									+ "] = "
									+ Arrays.toString(dataContactAllBusList
											.get(j)));

				}

			} catch (Exception e) {
				e.printStackTrace();
				Log.e("try yichang", "try yichang:");
			}

			// SocketServer.sendMessage(new
			// BaseSocketMessage(BaseSocketMessage.MSG_S_INIT_GROUP,
			// dataOrgAllBus));
			// //缂傚倸鍊风粈渚�藝闁秴绐楅柟鐗堟緲閺勩儲绻涢幋娆忕仼缂侊拷妫欐穱濠囧Χ閸涱収浠撮梺璺ㄥ櫐閹凤拷
		} else {
			byte[] temp = new byte[SystemVarTools.ARR_COLUMN_ORG * 50 + 1];
			int lastnum = 0; // 闂傚倷绀侀幖顐︽偋閻愬搫绠柣鎴ｆ缁犳牗淇婇妶鍕厡闁崇粯妫冮弻銊╂偄缂佹﹩妫勯梺浼欑畱閻栧ジ寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃椤掍礁顏�?
								// 闂佽崵鍠愮划搴㈡櫠濡ゅ懎绠伴柛娑橈攻濞呯娀鏌ｅΟ娆惧殭缂侊拷鐭傞弻娑樷槈閸楃偟浠梺浼欑秮娴滃爼寮婚妶澶婁紶闁告洝鍩栫�氬綊姊虹粙娆惧劀缂傚秳绀侀锝夘敃閿曪拷楠囬梺鍓茬厛閸ｎ喖顭囨潏銊х闁瑰鍋為悡銉︺亜閹存繃鍣洪柡鍛埣閹晝绱掑Ο鍝勫闂備礁鎲″ú锕傚磻閸曨垱鍎婇悹浣筋潐鐎氾拷
			temp[0] = (byte) lastnum;
			System.arraycopy("0".getBytes(), 0, temp, 1, "0".getBytes().length);
			SocketServer.sendMessage(new BaseSocketMessage(
					BaseSocketMessage.MSG_S_INIT_GROUP, temp)); // 婵犵數鍋為崹鍫曞箰閹间焦鏅濋柕澶涢檮閸欏繘鏌曢崼婵愭Ч闁搞倕鍊块弻锟犲礃閵娿儮鍋撻崸妤�绀傞柛銉亹閻熼偊鐓ラ柛鈩兦滈埀顒�鍟彁闁搞儜宥堝惈闂佽法鍣﹂幏锟�

			Log.e("", "MSG_S_INIT_GROUP 0 ");
		}

		// 娑撳﹥濮ら梿鍡欏參缂侊拷
		String groupNo = ServerMsgReceiver.getDefaultGroupNoNull(); // 閼惧嘲褰囪ぐ鎾冲闂嗗棛鍏㈢紒锟�
		if (groupNo.equals("")) {
			MyLog.e(TAG, "缁楊兛绔村▎鈥崇暔鐟佸拑绱濋柊宥囩枂閺傚洣娆㈡稉顓熺梾閺堝绮拋銈夋肠缂囥倗绮嶆稉锟姐��");

			if (SystemVarTools.getAllOrg().size() == 1) {

				String newGroupNo = SystemVarTools.getAllOrg().get(0).mobileNo;

				SystemVarTools.setCurrentGroup(newGroupNo);

				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_CURRENTGROUP, BCDTools
								.Str2BCD(newGroupNo, 10))); // 娑撳﹥濮よぐ鎾冲闂嗗棛鍏㈢紒锟�

				// ServiceAV.makeCall(SystemVarTools.getAllOrg().get(0).mobileNo,
				// NgnMediaType.Audio, SessionType.GroupAudioCall);
			} else {
				MyLog.d(TAG, "闂嗗棛鍏㈢紒鍕瑝濮濐澀绔存稉顏庣礉娑撳秷顔曠純顕�绮拋銈夋肠缂囥倗绮�");
			}

		} else {

			if (SystemVarTools.isGroupInContact(groupNo)) { // 閼惧嘲褰囬惃鍕肠缂囥倗绮嶉崷銊╋拷鐠侇垰缍嶆稉锟�
				MyLog.d(TAG, "groupNo is in contact");
				SocketServer.sendMessage(new BaseSocketMessage(
						BaseSocketMessage.MSG_S_UPDATE_CURRENTGROUP, BCDTools
								.Str2BCD(groupNo, 10))); // 娑撳﹥濮よぐ鎾冲闂嗗棛鍏㈢紒锟�
			} else { // 閸氾箑鍨敍宀勬娓氬じ绗傞幎銉╋拷鐠侇垰缍嶆稉顓犳畱缁楊兛绔存稉顏嗙矋
				MyLog.d(TAG, "groupNo is not in contact");

				String newGroupNo = SystemVarTools.getFirstOrg();
				if (newGroupNo != null) {
					SystemVarTools.setCurrentGroup(newGroupNo);
					MyLog.d(TAG, "newgroupNo is " + newGroupNo);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_UPDATE_CURRENTGROUP,
							BCDTools.Str2BCD(newGroupNo, 10))); // 娑撳﹥濮よぐ鎾冲闂嗗棛鍏㈢紒锟�
				} else {
					MyLog.d(TAG, "newgroupNo is null ");
					SystemVarTools.setCurrentGroup("");
				}
			}

		}

		try {
			if (dataContactAllBus != null) {
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = "
						+ ""
						+ "; message = "
						+ new String(dataContactAllBus,
								SystemVarTools.encoding_gb2312));
			}
			if (dataOrgAllBus != null) {
				MyLog.i("ServerMsgReceiver: type = "
						+ BaseSocketMessage.MSG_C_INIT_OK, "mobileNo = "
						+ ""
						+ "; message = "
						+ new String(dataOrgAllBus,
								SystemVarTools.encoding_gb2312));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void handleSipEvent(Intent intent) {

		MyLog.d(TAG, "ServiceSocketMode.handleSipEvent()");

		InviteState state;

		final String action = intent.getAction();
		Log.d(TAG, "action=" + action);
		if (NgnInviteEventArgs.ACTION_INVITE_EVENT.equals(action)) {
			NgnInviteEventArgs args = intent
					.getParcelableExtra(NgnInviteEventArgs.EXTRA_EMBEDDED);
			if (args == null) {
				Log.e(TAG, "Invalid event args");
				return;
			}

			NgnAVSession mAVSession = GlobalSession.avSession;

			if (mAVSession == null) {
				Log.e(TAG, "Invalid session object");
				return;
			}

			if (args.getSessionId() != mAVSession.getId()) {
				Log.d(TAG, "Receive a call,handling.1111");
				return;
			}

			NgnInviteEventTypes eventType = args.getEventType(); // CONNECTED
																	// TERMWAIT
			Log.d(TAG, "Receive a call,handling...eventtype=" + eventType);
			if (eventType.equals(NgnInviteEventTypes.ENCRYPT_INFO)) {
				Log.d(TAG, "Receive a call,encryptCall and eventtype="
						+ eventType);
				switch ((state = mAVSession.getState())) {
				case NONE:
				default:
					// Log.d(TAG, "Receive a call!!!!!!11111!!!!!");
					break;

				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
					Log.d("zhangjie:handleSipEvent",
							"state = INCOMING or INPROGRESS or REMOTE_RINGING ");
					// Log.d(TAG,
					// "receive callstate is "+mAVSession.getState());
					// loadTryingView();
					handleEncryptInfoMsg(mAVSession,
							mAVSession.getmInfoContent());// 闂備浇宕甸崑鐐哄礄瑜版帒纾婚柛鏇ㄥ枔娴滃綊鏌曟繛鍨壄閻熸瑥瀚刊鎾偠濞戞帒澧查柨锟�
					break;
				case EARLY_MEDIA:

					handleEncryptInfoMsg(mAVSession,
							mAVSession.getmInfoContent());// 婵犵數鍋為崹鍫曞箲娓氾拷鏁嬬憸鎴﹀Φ閹版澘惟闁靛鍟抽～澶愬箯閸涱垱鍋橀柍鈺佸暟閸橈拷
				}
			} else if (eventType.equals(NgnInviteEventTypes.PTT_INFO_REQUEST)) { // 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺呭礃閼碱剛鐛梻浣告啞濞诧箓宕㈡禒瀣櫢闁匡拷
				Log.d(TAG,
						">>>>>>>>>>>>>>>>>>>>>>>> Receive a call,group Call info message and eventtype="
								+ eventType);
				state = mAVSession.getState();
				Log.d(TAG,
						"======================== Receive a call,group Call info message and state="
								+ state);
				switch (state) {
				case NONE:
				default:
					// Log.d(TAG, "Receive a call!!!!!!11111!!!!!");
					break;

				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
				case EARLY_MEDIA:

					break;

				case INCALL: // 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺冪磼濡厧濮锋繝娈垮枟閿氱痪缁㈠幖铻ｉ柛顐ｇ箥閻斿棝鎮归崫鍕儓妞ゅ浚鍋婇弫鎾绘晸?
					// handleGroupPTTInfoMsg(mAVSession,
					// mAVSession.getmInfoContent());
					handleRequestPTTInfoMsg(mAVSession, args.getmInfoContent()); // 闂傚倷绀侀幉鈥愁啅婵犳艾鐤炬繝闈涱儏閺嬩線鏌曢崼婵囶棤妞も晜鐓￠幃妤呮晲閸屾稒鐝曞銈嗗姃缁瑩骞冨Δ鍛仺婵炲牊瀵ч弫顖炴⒑閼恒儳澧悗姘煎幘閳ь剟娼ч妶鎼佸箖閳哄倸顕辨繛鍡樺姇椤忓爼姊绘担鑺ャ�冪紒锟界劍閹柨顭ㄩ崼鐔告?
				}
			} else if ((eventType
					.equals(NgnInviteEventTypes.GROUP_VIDEO_MONITORING))) {
				Log.d(TAG,
						"Receive a call, group video monitoring and eventtype = "
								+ eventType);
				switch ((state = mAVSession.getState())) {
				case NONE:
				default:
					// Log.d(TAG, "Receive a call!!!!!!11111!!!!!");
					break;

				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
				case EARLY_MEDIA:
				case INCALL:
					break;

				}

			} else if ((eventType.equals(NgnInviteEventTypes.VIEDO_TRANSMINT))) {
				Log.d(TAG, "Receive a call, video transmint and eventtype = "
						+ eventType);
				switch ((state = mAVSession.getState())) {
				case NONE:
				default:
					// Log.d(TAG, "Receive a call!!!!!!11111!!!!!");
					break;

				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
				case EARLY_MEDIA:
					break;
				case INCALL:
					break;
				}
			} else {
				switch ((state = mAVSession.getState())) { // TERMINATED
				case NONE:
				default:
					break;

				case INCOMING:
				case INPROGRESS:
				case REMOTE_RINGING:
					// GlobalSession.avSession.setSpeakerphoneOn(false);
					// GlobalSession.avSession.setOnPause(false);
					break;

				case EARLY_MEDIA:
				case INCALL:

					if (mAVSession.isGroupAudioCall()
							|| mAVSession.isGroupVideoCall()
							|| mAVSession.isMicrophoneMute()) {
						mAVSession.setOnPause(true);
						mAVSession.setGroupAudioTimerStart(true);
						mAVSession.setOnResetJB();
						MyLog.d(TAG,
								"Group Call don't send RTP Packets at beginning [INCALL]");
					} else {
						mAVSession.setOnPause(false);
						mAVSession.setmSendVIdeo(true);
					}
					Engine.getInstance().getSoundService().stopRingTone();
					// mAVSession.setSpeakerphoneOn(false);
					// mAVSession.setOnPause(false);
					break;
				}
			}
		}
	}

	/**
	 * 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺冪磼濡厧濮锋繝娈垮枟閿氱痪缁㈠幖铻ｉ柛顐ｇ箥閻斿棝鎮归崫鍕儓妞ゅ浚鍋婇弫鎾绘晸?socket
	 * service
	 * 
	 * @param mAVSession
	 * @param infoContent
	 */
	private void handleRequestPTTInfoMsg(NgnAVSession mAVSession,
			byte[] infoContent) {

		/**
		 * PTT.Type:Report PTT.PhoneNumber:19800005003 19800005001
		 * PTT.IncludeNumber:110 PTT.VideoIP: PTT.VideoPort: PTT.Action:Online
		 * PTT.Result:OK
		 */
		/**
		 * PTT.Type:Reject PTT.PhoneNumber:19800005002 PTT.IncludeNumber:
		 * PTT.VideoIP: PTT.VideoPort: PTT.Action:Unknown PTT.Result:OK
		 */
		// String mInfoContent = new String(mAVSession.getmInfoContent()); //for
		// test java.lang.NullPointerException

		if (mPttCall == null)
			mPttCall = new GroupPTTCall();
		/**
		 * PTT.Type:Report PTT.PhoneNumber:19800005002 PTT.IncludeNumber:110
		 * PTT.VideoIP: PTT.VideoPort: PTT.Action:Online PTT.Result:OK
		 */
		PTTInfoMsg msg = new PTTInfoMsg(infoContent);
		mPttCall.handlePTTInfoMsg(msg);
		GroupPTTCall.PTTState state = mPttCall.getState();
		String pttAction, pttResult;
		Log.d(TAG, "mPttCall.getState() = " + mPttCall.getState());
		Log.d(TAG,
				"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! handleGroupPTTInfoMsg() - mPttCall.getState() = "
						+ state);

		if (mPttCall.getState() == PTTState.REJECTED) {
			GlobalSession.avSession.setConsumerOnPause(false);
			GlobalSession.avSession.setProducerOnPause(true);

		} else if (mPttCall.getState() == PTTState.GRANTED) {
			GlobalSession.avSession.setConsumerOnPause(true);
			GlobalSession.avSession.setProducerOnPause(false);
		} else if (mPttCall.getState() == PTTState.RELEASE_SUCCESS
				|| mPttCall.getState() == PTTState.RELEASED) {
			GlobalSession.avSession.setConsumerOnPause(true);
			GlobalSession.avSession.setProducerOnPause(true);
		}

		if ("Flash Number".equals(msg.getPTTType())) {

			MyLog.d(TAG, "PTTType=" + msg.getPTTType());

			String newRemoteNO = msg.getPTTPhoneNumber();

			SocketServer.sendMessage(new BaseSocketMessage(
					BaseSocketMessage.MSG_S_AUDIO_INCALL, BCDTools.Str2BCD(
							newRemoteNO, 10)));

			GlobalSession.avSession.setRemotePartyUri(NgnUriUtils
					.makeValidSipUri(newRemoteNO));
			return;

		}

		String mobileNo = NgnUriUtils.getUserName(GlobalSession.avSession
				.getRemotePartyUri());
		byte[] mobileNoBytes = BCDTools.Str2BCD(mobileNo, 10);
		byte[] data = new byte[11];

		switch (state) {
		case NONE:
		case REQUESTING:
			data[0] = 1; // 1 婵繐绲藉﹢顏堟偨鐎圭媭鍤�
			System.arraycopy(mobileNoBytes, 0, data, 1, 10);
			SocketServer.sendMessage(new BaseSocketMessage(
					BaseSocketMessage.MSG_S_GROUP_STATE, data));

			MyLog.i("ServiceSocketMode: type = "
					+ BaseSocketMessage.MSG_S_GROUP_STATE, "mobileNo = "
					+ mobileNo + "; message = "
					+ "MSG_S_GROUP_STATE 1: REQUESTING.");
			break;
		case GRANTED: // PTT闂傚倷鑳堕、濠勭礄娴兼潙鍨傞柣鐔稿閺嗭箑霉閸忓吋缍戠紒鐙呯秮閺岀喖鏌囬敃锟筋潻闂佽法鍣﹂幏锟�
			((Engine) Engine.getInstance()).playNotificationTone();

			ServiceAV.isPTTRejected = false;

			Log.d("zhangjie:handleGroupPTTInfoMsg()",
					"mPttCall.getState() = GRANTED");
			mobileNo = msg.getPTTPhoneNumber();
			mobileNoBytes = BCDTools.Str2BCD(mobileNo, 10);
			data[0] = 3; // 3闂傚倷鐒︾�笛呯矙閹烘埈娼╅柕濞炬櫅閺嬩線鏌ｉ姀鐘典粵閻庢碍宀稿娲垂椤曞懎鍓伴梺璇茬箰濞差參寮诲☉銏犵睄闁稿本纰嶉悵銏㈢磽娴ｄ粙鍝虹紒璇插楠炲繘鎮╃紒妯轰痪濡炪倖鐗徊浠嬫晸?
			System.arraycopy(mobileNoBytes, 0, data, 1, 10);
			SocketServer.sendMessage(new BaseSocketMessage(
					BaseSocketMessage.MSG_S_GROUP_STATE, data));

			MyLog.i("ServiceSocketMode: type = "
					+ BaseSocketMessage.MSG_S_GROUP_STATE, "mobileNo = "
					+ mobileNo + "; message = "
					+ "MSG_S_GROUP_STATE 3: request success, please speak."); // 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺冪磼濡桨姹楅梺鑽ゅС濞村洭锝炴径鎰櫢闁匡拷3闂傚倷鐒︾�笛呯矙閹烘埈娼╅柕濞炬櫅閺嬩線鏌ｉ姀鐘典粵閻庢碍宀稿娲垂椤曞懎鍓伴梺璇茬箰濞差參寮诲☉銏犵睄闁稿本纰嶉悵銏㈢磽娴ｄ粙鍝虹紒璇插楠炲繘鎮╃紒妯轰痪濡炪倖鐗徊浠嬫晸?

			break;
		case RELEASE_SUCCESS: // PTT闂傚倸鍊烽悞锕併亹閸愵喗鏅濋柕澶嗘櫅濡﹢鏌ㄩ悢缁橆棄缂佺媴缍侀弻鐔烘暜椤旇棄顏堕梺璺ㄥ櫐閹凤拷
			Log.d("zhangjie:handleGroupPTTInfoMsg()",
					"mPttCall.getState() = RELEASE_SUCCESS");

			ServiceAV.isPTTRejected = false;
			String mobileNo4 = NgnUriUtils.getUserName(mAVSession
					.getRemotePartyUri());
			byte[] mobileNoBytes4 = BCDTools.Str2BCD(mobileNo4, 10);
			byte[] data4 = new byte[11];
			data4[0] = 4; // 4闂傚倷鐒︾�笛呯矙閹烘柨鏋堢�广儱鎲樺☉銏犖ч柛鈩冪懐濞插憡淇婇妶蹇涙妞ゆ垶鐟╁畷顒冦亹閹烘挾鍙�?
			System.arraycopy(mobileNoBytes4, 0, data4, 1, 10);
			SocketServer.sendMessage(new BaseSocketMessage(
					BaseSocketMessage.MSG_S_GROUP_STATE, data4));

			MyLog.i("ServiceSocketMode: type = "
					+ BaseSocketMessage.MSG_S_GROUP_STATE, "mobileNo = "
					+ mobileNo4 + "; message = "
					+ "MSG_S_GROUP_STATE 4: no speak."); // 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺冪磼濡桨姹楅梺鑽ゅС濞村洭锝炴径鎰櫢闁匡拷4闂傚倷鐒︾�笛呯矙閹烘柨鏋堢�广儱鎲樺☉銏犖ч柛鈩冪懐濞插憡淇婇妶蹇涙妞ゆ垶鐟╁畷顒冦亹閹烘挾鍙�?

			break;
		case RELEASED:
			Log.d("zhangjie:handleGroupPTTInfoMsg()",
					"mPttCall.getState() = RELEASED");

			ServiceAV.isPTTRejected = false;
			String mobileNo4_ = NgnUriUtils.getUserName(mAVSession
					.getRemotePartyUri());
			byte[] mobileNoBytes4_ = BCDTools.Str2BCD(mobileNo4_, 10);
			byte[] data4_ = new byte[11];
			data4_[0] = 4; // 4闂傚倷鐒︾�笛呯矙閹烘柨鏋堢�广儱鎲樺☉銏犖ч柛鈩冪懐濞插憡淇婇妶蹇涙妞ゆ垶鐟╁畷顒冦亹閹烘挾鍙�?
			System.arraycopy(mobileNoBytes4_, 0, data4_, 1, 10);
			SocketServer.sendMessage(new BaseSocketMessage(
					BaseSocketMessage.MSG_S_GROUP_STATE, data4_));

			MyLog.i("ServiceSocketMode: type = "
					+ BaseSocketMessage.MSG_S_GROUP_STATE, "mobileNo = "
					+ mobileNo4_ + "; message = "
					+ "MSG_S_GROUP_STATE 4: no speak."); // 缂傚倸鍊搁崐椋庣矆娴ｈ　鍋撳鐓庡⒋妤犵偛锕幃鈺冪磼濡桨姹楅梺鑽ゅС濞村洭锝炴径鎰櫢闁匡拷4闂傚倷鐒︾�笛呯矙閹烘柨鏋堢�广儱鎲樺☉銏犖ч柛鈩冪懐濞插憡淇婇妶蹇涙妞ゆ垶鐟╁畷顒冦亹閹烘挾鍙�?
			break;
		case REJECTED:
			Log.d("zhangjie:handleGroupPTTInfoMsg()",
					"mPttCall.getState() = REGECTED");

			ServiceAV.isPTTRejected = true;
			String mobileNo2 = msg.getPTTPhoneNumber();
			byte[] mobileNoBytes2 = BCDTools.Str2BCD(mobileNo2, 10);
			byte[] data2 = new byte[11];
			data2[0] = 2; // 2闂傚倷鐒︾�笛呯矙閹烘鍎楁い鏃�鍎抽崹婵囥亜閹哄秷鍏屾い鈺傚絻铻栭柨鏃傜摂閸庛儵鏌涢悙鑼Ш闁诡喖缍婂畷鍫曞Ω瑜忛悾閬嶆煥閻曞倹瀚�?
			System.arraycopy(mobileNoBytes2, 0, data2, 1, 10);
			SocketServer.sendMessage(new BaseSocketMessage(
					BaseSocketMessage.MSG_S_GROUP_STATE, data2));

			MyLog.i("ServiceSocketMode: type = "
					+ BaseSocketMessage.MSG_S_GROUP_STATE, "mobileNo = "
					+ mobileNo2 + "; message = "
					+ "MSG_S_GROUP_STATE 2: someone is speaking."); // 闂傚倷娴囧▔鏇㈠窗閹版澘鍑犲┑鐘宠壘缁狀垶鏌ｉ幋锝呅撻柡鍛倐濮婂宕橀埡浣稿Ц闂佽妞挎禍顏堟晸?2闂傚倷娴囧▔鏇㈠窗閹版澘鍑犲┑鐘宠壘缁狀垶鏌ｉ幋锝呅撻柡鍛倐閺岋繝宕掑Ο琛″亾閺嶎偀鍋撳顒傜Ш闁哄被鍔戦幃銏犵暋闁附锛侀梻浣告惈閸婂爼宕愰弽顐熷亾濮橆剛绉洪柡灞诲姂閹垽宕ㄦ繝鍕磿闂備礁缍婇ˉ鎾诲礂濮楋拷鏋侀柟鎹愵嚙濡﹢鏌曢崼婵囶棞闁匡拷

			break;
		case ALAVE:
			break;
		case SUBSCRIBE_SUCCESS:
			break;
		case SUBSCRIBE_FAILED:
			break;
		case ONLINE:
			break;
		case VIDEOSUB_TURNON:
			Log.d(TAG, "閻熸瑥妫濋。鍓佺磼閸曨偅鍤�   VIDEOSUB_TURNON");

			sendPTTSubscribeAckInfoMsg();
			// startVideo(true,true);
			if (mPttCall.isStateChanged()) {
				GlobalSession.avSession.setmSendVIdeo(true);
			}
			break;

		case VIDEOSUB_TURNOFF:

			Log.d(TAG, "閻熸瑥妫濋。鍓佺磼閸曨偅鍤�   VIDEOSUB_TURNOFF");
			sendPTTSubscribeAckInfoMsg();
			if (mPttCall.isStateChanged()) {
				GlobalSession.avSession.setmSendVIdeo(false);
			}
		case CALSUB:
			break;
		case CANCEL_SUCCESS:
			break;

		case CONTROL: // 婵犵數鍋涢悺銊у垝瀹ュ鍨傚ù锝呭暔娴滃綊鏌熺紒銏犳灈缂佺姴纾幉绋款吋婢跺﹥妲�?
			if (GlobalSession.bSocketService == false
					|| !NgnApplication.isl8848a_l1860())
				break;
			if (controller == null)
				controller = new Rs485Controller();

			// set speed [0,63]
			controllerSpeed = Integer.parseInt(msg.getPTTIncludeNumber()
					.toString());
			controllerSpeed = controllerSpeed < 0 ? 0
					: (controllerSpeed > 63 ? 63 : controllerSpeed);

			// set action
			pttAction = msg.getPTTAction();
			Log.d(TAG, String.format("PTTAction: %s. controllerSpeed=%d)",
					pttAction, controllerSpeed));
			if (pttAction.equals(PTTActionTypes.PTT_ACT_UP)) {
				Log.d(TAG, "PTTAction: PTTActionTypes.PTT_ACT_UP");
				controller.tiltUp(controllerSpeed);
			} else if (pttAction.equals(PTTActionTypes.PTT_ACT_DOWN)) {
				Log.d(TAG, "PTTAction: PTTActionTypes.PTT_ACT_DOWN");
				controller.tiltDown(controllerSpeed);
			} else if (pttAction.equals(PTTActionTypes.PTT_ACT_LEFT)) {
				Log.d(TAG, "PTTAction: PTTActionTypes.PTT_ACT_LEFT");
				controller.panLeft(controllerSpeed);
			} else if (pttAction.equals(PTTActionTypes.PTT_ACT_RIGHT)) {
				Log.d(TAG, "PTTAction: PTTActionTypes.PTT_ACT_RIGHT");
				controller.panRight(controllerSpeed);
			} else if (pttAction.equals(PTTActionTypes.PTT_ACT_OPEN)) {
				Log.d(TAG, "PTTAction: PTTActionTypes.PTT_ACT_OPEN");
				// set Protocol and then open the Controller
				pttResult = msg.getPTTResult();
				if (pttResult
						.equalsIgnoreCase(PTTResultTypes.PTT_RLT_PROTOCOL_P)) {
					Log.d(TAG, "PTTResult: PTTResultTypes.PTT_RLT_PROTOCOL_P");
					controller
							.yuntaiControllerOpen(Rs485Controller.PELCO_TYPE_P);
				} else if (pttResult
						.equalsIgnoreCase(PTTResultTypes.PTT_RLT_PROTOCOL_D)) {
					Log.d(TAG, "PTTResult: PTTResultTypes.PTT_RLT_PROTOCOL_D");
					controller
							.yuntaiControllerOpen(Rs485Controller.PELCO_TYPE_D);
				}
			} else if (pttAction.equals(PTTActionTypes.PTT_ACT_CLOSE)) {
				Log.d(TAG, "PTTAction: PTTActionTypes.PTT_ACT_CLOSE");
				controller.yuntaiControllerClose();
			} else if (pttAction.equals(PTTActionTypes.PTT_ACT_STOP)) {
				Log.d(TAG, "PTTAction: PTTActionTypes.PTT_ACT_STOP");
				controller.actionStop();
			}

			break;
		case GET_AUDIO: // 婵犵數鍋炲娆撳触鐎ｎ喖绠伴柟鎯板Г閸嬪倿鏌涢锝嗙闁绘劕锕弻锝夊箛椤撶儐妫嗛梺璺ㄥ櫐閹凤拷
			pttAction = msg.getPTTAction();
			final AudioManager audiomanager = NgnApplication.getAudioManager();
			audiomanager.setMode(AudioManager.MODE_IN_COMMUNICATION);
			Log.d(TAG,
					String.format(
							"PTTAction: %s. SetMode(AudioManager.MODE_IN_COMMUNICATION)",
							pttAction));
			if (pttAction.equals(PTTActionTypes.PTT_ACT_AUDIO_DISABLE)) {
				Log.d(TAG,
						"PTTAction: PTTActionTypes.PTT_ACT_AUDIO_DISABLE && lc_audio_record=0 && onPause=true");
				audiomanager.setParameters("lc_audio_record=0");
				mAVSession.setOnPause(true);
			} else if (pttAction.equals(PTTActionTypes.PTT_ACT_AUDIO_ANALOG)) {
				Log.d(TAG,
						"PTTAction: PTTActionTypes.PTT_ACT_AUDIO_ANALOG && lc_audio_record=1 && onPause=false");
				audiomanager.setParameters("lc_audio_record=1");
				mAVSession.setOnPause(false);
			} else if (pttAction.equals(PTTActionTypes.PTT_ACT_AUDIO_HD)) {
				Log.d(TAG,
						"PTTAction: PTTActionTypes.PTT_ACT_AUDIO_HD && lc_audio_record=2 && onPause=false");
				audiomanager.setParameters("lc_audio_record=2");
				mAVSession.setOnPause(false);
			}

			break;
		default:
			break;

		}
	}

	private void handleEncryptInfoMsg(NgnAVSession mAVSession,
			byte[] infoContent) {
		Log.d("zhangjie:handleEncryptInfoMsg()", "handleEncryptInfoMsg");
		Log.d(TAG, "receive a encrypt_info!!!!");
		EncryptProcess proc = new EncryptProcess();
		ByteBuffer retBytes = proc.process(infoContent);
		if (EncryptProcess.ENCRYPT_BEGIN.equals(proc.getEncryptState())) {
			Log.d(TAG, "receive a encrypt_info:ENCRYPT_BEGIN!!!!");
			// mTvInfo.setText(getString(R.string.string_call_keybegining));
			sendInfo(mAVSession, retBytes, NgnContentType.USSD_INFO);
			// mTvInfo.setText(getString(R.string.string_call_keyrequesting));
		} else if (EncryptProcess.ENCRYPT_KEY_DIS
				.equals(proc.getEncryptState())) {
			Log.d(TAG, "receive a encrypt_info:ENCRYPT_KEY_DIS!!!!");
			// mTvInfo.setText(getString(R.string.string_call_keydis));
			sendInfo(mAVSession, retBytes, NgnContentType.USSD_INFO);
			// mTvInfo.setText(getString(R.string.string_call_keyresq));
			if (!proc.isCaller())
				Engine.getInstance().getSoundService().startRingTone();
		}

	}

	// int闂備礁鎼ˇ閬嶅磻閻旂厧绠栫紒铏诡棟e闂傚倷娴囧銊╂倿閿旂晫鐝堕柛鈩冪懃閸ㄦ繄锟介妷銉Ч闁哄拋鍓熼弫鎾绘寠婢跺浼�閻熸粓鍋婇崹浼存箒闂佹寧绻傞幊鎰摥婵犵數濯撮幏閿嬨亜韫囨挾澧涢柨锟�
	public static byte[] intToBytes2(int a) {
		byte[] b = new byte[2];
		b[0] = (byte) ((a >> 8) & 0XFF);
		b[1] = (byte) (a & 0XFF);
		return b;
	}

	private boolean sendInfo(NgnAVSession mAVSession,
			java.nio.ByteBuffer payload, String contentType) {// xunzy+
		if (mAVSession != null) {
			return mAVSession.sendInfo(payload, contentType);
		}
		return false;
	}

	private static boolean sendInfo(String content, String contentType) {
		Log.d(TAG, "sendInfo()");
		NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
				.getSessions();
		for (int i = 0; i < mAVSessions.size(); i++) {
			if (mAVSessions.getAt(i) != null) {
				return mAVSessions.getAt(i).sendInfo(content, contentType);
			}
		}
		return false;
	}

	private static boolean sendPTTReportAliveInfoMsg() {
		Log.d(TAG, "sendPTTReportAliveInfoMsg()");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_REPORT);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTResult(PTTResultTypes.PTT_RLT_OK);
		msg.setPTTAction(PTTActionTypes.PTT_ACT_ALIVE);
		Log.d(TAG, "send ptt info msg: " + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	private void handleAdhocSessionEvent(Intent intent) {
		Log.d(TAG, "InviteEvent ServiceSocket handleAdhocSessionEvent");
		final String action = intent.getAction();
		Log.d(TAG, "action=" + action);
		if (AdhocSessionEventArgs.ADHOC_SESSION_EVENT.equals(action)) {
			NgnMediaSession mMediaSession = GlobalSession.mediaSession;
			if (mMediaSession == null) {
				Log.e(TAG, "Invalid session object");
				return;
			}

			AdhocSessionEventArgs args = intent
					.getParcelableExtra(AdhocSessionEventArgs.EXTRA_EMBEDDED);

			AdhocSessionEventTypes eventType = args.getEventType(); // INCALL
																	// INCOMMING
			MyLog.d(TAG, String.format("mediaSession = %s", mMediaSession));
			MyLog.d(TAG, "eventType = " + eventType + ", sessionState = "
					+ mMediaSession.getSessionState());
			if (args == null) {
				MyLog.e(TAG, "Invalid event args(args is null).");
				return;
			}

			if (mMediaSession.getId() != args.getSessionId()) {
				MyLog.d(TAG, "Receive a call,handling.1111");
				return;
			}
			if (eventType.equals(AdhocSessionEventTypes.INCALL)) {// 濠电偞鍨堕幑渚�顢氳閵囨劙濡搁妷鍐潐閹峰懐鎮伴埄鍐幘
				MyLog.d(TAG, "state =" + mMediaSession.getSessionState());
				switch ((mMediaSession.getSessionState())) { // TERMINATED
				default:
					break;

				case INPROGRESS:
					// loadTryingView();
					break;

				case CONNECTED:
					Log.d(TAG, "SendPTTRequestCMD() 1...");
					ServiceAdhoc.getInstance().sendPTTRequestCMD();
					String mobileNo2 = Engine.getInstance().getNetworkService()
							.getLocalIP(false);// msg.getPTTPhoneNumber();
					// byte[] mobileNoBytes2 = BCDTools.Str2BCD(mobileNo2, 10);
					byte[] data2 = new byte[11];
					data2[0] = 1; // 1 : Requesting PTT...
					// System.arraycopy(mobileNoBytes2, 0, data2, 1, 10);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_GROUP_STATE, data2));
					mMediaSession.setOnPause(true);
					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_GROUP_STATE,
							"mobileNo = " + mobileNo2 + "; message = "
									+ "MSG_S_GROUP_STATE 1: request ptt..."); // 闂備浇娉曢崰鎰板几婵犳艾绠柣鎴ｅГ閺呮悂姊婚崘鈺佸姸闁规椿浜弫鎾绘晸?2闂備浇娉曢崰鎰板几婵犳艾绠柣鎴ｅГ閺呮悂鏌￠崒妯猴拷閻庢艾缍婇弻銊╂偄瀹勯偊妫￠梺鍝勫�堕崐鏍拷瑜版帗鐓ラ柣鏂挎啞閻忣噣鏌熸搴″幋闁轰焦鎹囧顒勫Χ閸℃浼�
					/*
					 * MyLog.d(TAG, "GRANT. play tone...");
					 * mMediaSession.setSpeakerphoneOn(true);
					 * if(args.getGroupCallType() ==
					 * CommandType.SUPER_GROUP_AUDIO_CALL){
					 * ((Engine)Engine.getInstance()).playNotificationTone2(); }
					 * else{
					 * ((Engine)Engine.getInstance()).playNotificationTone(); }
					 * 
					 * mMediaSession.setConsumerOnPause(true);
					 * mMediaSession.setProducerOnPause(false);
					 */

					break;
				case TERMINATED: // Call Terminated
					// PTT闂備礁婀遍…鍫ニ囬悽绋跨闁惧浚鍋嗛埢鏃堟煣韫囨洦鏀伴梻鍡楃秺閺屻倗娑甸崨顓℃暱闂侀潧妫楅崯鎵垝閸儲鎯為悷娆忓閻涖儵姊洪崨濠冨碍缂佸鏁婚幆锟藉川椤旂虎娲搁梺缁樺灦閿氭い顐犲劦閺屾稑螖閸愩劍顔員T
					mMediaSession.hungUp();
					// String mobileNo4 =
					// Engine.getInstance().getNetworkService().getLocalIP(false);//NgnUriUtils.getUserName(mAVSession.getRemotePartyUri());
					// byte[] data4 = new byte[11];
					// data4[0] = 4; // 4闂備焦瀵х粙鎺斿枈瀹ュ憘娑㈠Χ閸℃瑯娲告俊銈忛檮椤戞瑩宕ぐ鎺撴櫢闁匡拷
					// //System.arraycopy(mobileNoBytes4, 0, data4, 1, 10);
					// SocketServer.sendMessage(new
					// BaseSocketMessage(BaseSocketMessage.MSG_S_GROUP_STATE,
					// data4));
					//
					// MyLog.i("ServiceSocketMode: type = " +
					// BaseSocketMessage.MSG_S_GROUP_STATE, "mobileNo = " +
					// mobileNo4 + "; message = " +
					// "MSG_S_GROUP_STATE 4: no speak.");
					// //缂傚倸鍊风粈浣猴拷椤掑嫬绠犻柣妯肩帛閸嬫劙鎮规担绛嬫綈闁匡拷4闂備焦瀵х粙鎺斿枈瀹ュ憘娑㈠Χ閸℃瑯娲告俊銈忛檮椤戞瑩宕ぐ鎺撴櫢闁匡拷
					MyLog.d(TAG, "Closed the group call. play tone...");
					if (args.getGroupCallType() == CommandType.SUPER_GROUP_AUDIO_CALL) {
						((Engine) Engine.getInstance()).playNotificationTone2();
					} else {
						((Engine) Engine.getInstance()).playNotificationTone();
					}

					String mobileNo4 = Engine.getInstance().getNetworkService()
							.getLocalIP(false);// NgnUriUtils.getUserName(mAVSession.getRemotePartyUri());

					// byte[] mobileNoBytes4 = BCDTools.Str2BCD(mobileNo4, 10);
					byte[] data4 = new byte[11];
					data4[0] = 5; // 5 : Closed the group call ?
					// System.arraycopy(mobileNoBytes4, 0, data4, 1, 10);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_GROUP_STATE, data4));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_GROUP_STATE,
							"mobileNo = "
									+ mobileNo4
									+ "; message = "
									+ "MSG_S_GROUP_STATE(out) 5: caller hungup."); // 缂傚倸鍊风粈浣猴拷椤掑嫬绠犻柣妯肩帛閸嬫劙鎮规担绛嬫綈闁匡拷4闂備焦瀵х粙鎺斿枈瀹ュ憘娑㈠Χ閸℃瑯娲告俊銈忛檮椤戞瑩宕ぐ鎺撴櫢闁匡拷

					break;
				}
			} else if (eventType.equals(AdhocSessionEventTypes.INCOMING)) {// 闂佽崵鍋為崙褰掑磻閸曨喓浜归柕濞垮剭鐟欏嫭濯撮悶娑掑墲閻擄拷
				MyLog.d(TAG, "eventType =" + mMediaSession.getSessionState());
				switch ((mMediaSession.getSessionState())) { // TERMINATED
				default:
					break;

				case INPROGRESS:
					// loadTryingView();
					break;

				case CONNECTED: {

					String mobileNo = NgnUriUtils
							.getUserName(GlobalSession.mediaSession
									.getRemotePartyUri());
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_GROUP_INCOMING, BCDTools
									.Str2BCD(mobileNo, 10)));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_GROUP_INCOMING,
							"mobileNo = "
									+ mobileNo
									+ "; message = "
									+ "MSG_S_PTT_GROUP_INCOMING: groupcall/audio incoming."); // 缂傚倸鍊风粈浣猴拷椤掑嫬绠犻柣妯款嚙缁狙囨煏婢诡垰瀚В锟�
					mMediaSession.setOnPause(true);
					/*
					 * if(GlobalSession.bSocketService == true){ MyLog.d(TAG,
					 * "REJECTED play tone...");
					 * mMediaSession.setSpeakerphoneOn(true);
					 * if(args.getGroupCallType() ==
					 * CommandType.SUPER_GROUP_AUDIO_CALL){
					 * ((Engine)Engine.getInstance()).playNotificationTone2(); }
					 * else{
					 * ((Engine)Engine.getInstance()).playNotificationTone(); }
					 * mMediaSession.setOnResetJB();
					 * mMediaSession.setConsumerOnPause(false);
					 * mMediaSession.setProducerOnPause(true);
					 * 
					 * String mobileNo2 =
					 * Engine.getInstance().getNetworkService(
					 * ).getLocalIP(false);//msg.getPTTPhoneNumber(); //byte[]
					 * mobileNoBytes2 = BCDTools.Str2BCD(mobileNo2, 10); byte[]
					 * data2 = new byte[11]; data2[0] = 2; //
					 * 2闂備焦瀵х粙鎺楁儗椤旀儳鍨濇い鎺嶈兌椤
					 * ╂彃螖閿旂瓔鍎ラ柛鐐茬秺閹綊宕堕妸褏鐣遍梺璺ㄥ櫐閹凤拷//System.arraycopy
					 * (mobileNoBytes2, 0, data2, 1, 10);
					 * SocketServer.sendMessage(new
					 * BaseSocketMessage(BaseSocketMessage.MSG_S_GROUP_STATE,
					 * data2));
					 * 
					 * MyLog.i("ServiceSocketMode: type = " +
					 * BaseSocketMessage.MSG_S_GROUP_STATE, "mobileNo = " +
					 * mobileNo2 + "; message = " +
					 * "MSG_S_GROUP_STATE 2: someone is speaking.");
					 * //闂備浇娉曢崰鎰板几婵犳艾绠
					 * 柣鎴ｅГ閺呮悂姊婚崘鈺佸姸闁规椿浜弫鎾绘晸?2闂備浇娉曢崰鎰板几婵犳艾绠柣鎴ｅГ閺呮悂鏌￠崒妯猴拷閻庢艾缍婇弻銊
					 * ╂偄瀹勯偊妫￠梺鍝勫�堕崐鏍拷瑜版帗鐓ラ柣鏂挎啞閻忣噣鏌熸搴″幋闁轰焦鎹囧顒勫Χ閸℃浼�
					 * 
					 * }
					 */
				}
					break;
				case TERMINATED: // Call Terminated
					if (GlobalSession.bSocketService) {
						/*
						 * MyLog.d("TAG", "RELEASE_SUCCESS play tone..."); //
						 * PTT闂備礁婀遍
						 * …鍫ニ囬悽绋跨闁惧浚鍋嗛埢鏃堟煣韫囨洦鏀伴梻鍡楃秺閺屻倗娑甸崨顓℃暱闂侀潧妫楅崯鎵垝閸
						 * 儲鎯為悷娆忓閻涖儵姊洪崨濠冨碍缂佸鏁婚幆锟藉川椤旂虎娲搁梺缁樺灦閿氭い顐犲劦閺屾稑螖閸愩劍顔員T
						 * if(args.getGroupCallType() ==
						 * CommandType.SUPER_GROUP_AUDIO_CALL){
						 * ((Engine)Engine.getInstance
						 * ()).playNotificationTone2(); } else{
						 * ((Engine)Engine.getInstance
						 * ()).playNotificationTone(); }
						 */
						// String mobileNo4 =
						// Engine.getInstance().getNetworkService().getLocalIP(false);//NgnUriUtils.getUserName(mAVSession.getRemotePartyUri());
						// // //byte[] mobileNoBytes4 =
						// BCDTools.Str2BCD(mobileNo4, 10);
						// byte[] data4 = new byte[11];
						// data4[0] = 4; //
						// 4闂備焦瀵х粙鎺斿枈瀹ュ憘娑㈠Χ閸℃瑯娲告俊銈忛檮椤戞瑩宕ぐ鎺撴櫢闁匡拷
						// //System.arraycopy(mobileNoBytes4, 0, data4, 1, 10);
						// SocketServer.sendMessage(new
						// BaseSocketMessage(BaseSocketMessage.MSG_S_GROUP_STATE,
						// data4));
						//
						// MyLog.i("ServiceSocketMode: type = " +
						// BaseSocketMessage.MSG_S_GROUP_STATE, "mobileNo = " +
						// mobileNo4 + "; message = " +
						// "MSG_S_GROUP_STATE 4: no speak.");
						// //缂傚倸鍊风粈浣猴拷椤掑嫬绠犻柣妯肩帛閸嬫劙鎮规担绛嬫綈闁匡拷4闂備焦瀵х粙鎺斿枈瀹ュ憘娑㈠Χ閸℃瑯娲告俊銈忛檮椤戞瑩宕ぐ鎺撴櫢闁匡拷
						//
					}
					mMediaSession.hungUp();
					String mobileNo4 = Engine.getInstance().getNetworkService()
							.getLocalIP(false);// NgnUriUtils.getUserName(mAVSession.getRemotePartyUri());
					// byte[] mobileNoBytes4 = BCDTools.Str2BCD(mobileNo4, 10);
					byte[] data4 = new byte[11];
					data4[0] = 5; // 5: Closed the group call
					// System.arraycopy(mobileNoBytes4, 0, data4, 1, 10);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_GROUP_STATE, data4));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_GROUP_STATE,
							"mobileNo = " + mobileNo4 + "; message = "
									+ "MSG_S_GROUP_STATE(in) 5: callee hungup."); // 缂傚倸鍊风粈浣猴拷椤掑嫬绠犻柣妯肩帛閸嬫劙鎮规担绛嬫綈闁匡拷4闂備焦瀵х粙鎺斿枈瀹ュ憘娑㈠Χ閸℃瑯娲告俊銈忛檮椤戞瑩宕ぐ鎺撴櫢闁匡拷

					break;
				}

			} else if (eventType.equals(AdhocSessionEventTypes.PTT_REQUEST)) {// PTT
				PTTState mState = args.getmPTTState();
				switch (mState) {
				case GRANTED: {
					Log.d("TAG", "mPttCall.getState() = GRANTED Ring...");
					if (args.getGroupCallType() == CommandType.SUPER_GROUP_AUDIO_CALL) {
						((Engine) Engine.getInstance()).playNotificationTone2();
					} else {
						((Engine) Engine.getInstance()).playNotificationTone();
					}

					// 闁荤喐绮庢晶妤呭箰閸涘﹥娅犻柣妯肩帛閸嬨劑鏌曟繝蹇曠暠闁绘挻妲圱T闂備胶顢婄紙浼村垂閻熸壆鏆﹀ù鍏兼綑缁狅綁鏌熼柇锕�澧柨锟�
					MyLog.d(TAG, "GRANTED, please speaking...");
					String mobileNo2 = Engine.getInstance().getNetworkService()
							.getLocalIP(false);// msg.getPTTPhoneNumber();
					// byte[] mobileNoBytes2 = BCDTools.Str2BCD(mobileNo2, 10);
					byte[] data2 = new byte[11];
					data2[0] = 3; // 3: Granted,please speaking!
					// System.arraycopy(mobileNoBytes2, 0, data2, 1, 10);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_GROUP_STATE, data2));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_GROUP_STATE,
							"mobileNo = "
									+ mobileNo2
									+ "; message = "
									+ "MSG_S_GROUP_STATE 3:  request success, please speak."); // 闂備浇娉曢崰鎰板几婵犳艾绠柣鎴ｅГ閺呮悂姊婚崘鈺佸姸闁规椿浜弫鎾绘晸?2闂備浇娉曢崰鎰板几婵犳艾绠柣鎴ｅГ閺呮悂鏌￠崒妯猴拷閻庢艾缍婇弻銊╂偄瀹勯偊妫￠梺鍝勫�堕崐鏍拷瑜版帗鐓ラ柣鏂挎啞閻忣噣鏌熸搴″幋闁轰焦鎹囧顒勫Χ閸℃浼�

					// PTT闂備礁婀遍…鍫ニ囬悽绋跨闁惧浚鍋嗛埢鏃堟煣韫囨洘鍤�妞ゆ帇鍨介弻銈囨兜閸涱叀鏁块梺闈涙閸熸壆鍒掗崼銏犲К婵烇絿鎸嗛梻浣侯攰缂堜即宕归悷鎵殾濞村吋娼欑粻锝夋煙闁箑澧柨锟�

					mMediaSession.setConsumerOnPause(true);
					mMediaSession.setProducerOnPause(false);
					mMediaSession.setSpeakerphoneOn(false);

				}
					break;
				case REJECTED: {
					Log.d(TAG, "mPttCall.getState() = REJECTED Ring...");
					if (args.getGroupCallType() == CommandType.SUPER_GROUP_AUDIO_CALL) {
						((Engine) Engine.getInstance()).playNotificationTone2();
					} else {
						((Engine) Engine.getInstance()).playNotificationTone();
					}
					mMediaSession.setOnResetJB();
					mMediaSession.setConsumerOnPause(false);
					mMediaSession.setProducerOnPause(true);
					mMediaSession.setSpeakerphoneOn(true);
					MyLog.d(TAG, "REJECTED");

					String mobileNo2 = Engine.getInstance().getNetworkService()
							.getLocalIP(false);// msg.getPTTPhoneNumber();
					// byte[] mobileNoBytes2 = BCDTools.Str2BCD(mobileNo2, 10);
					byte[] data2 = new byte[11];
					data2[0] = 2; // 2: someone else is speaking
					// System.arraycopy(mobileNoBytes2, 0, data2, 1, 10);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_GROUP_STATE, data2));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_GROUP_STATE,
							"mobileNo = "
									+ mobileNo2
									+ "; message = "
									+ "MSG_S_GROUP_STATE 2: someone else is speaking."); // 闂備浇娉曢崰鎰板几婵犳艾绠柣鎴ｅГ閺呮悂姊婚崘鈺佸姸闁规椿浜弫鎾绘晸?2闂備浇娉曢崰鎰板几婵犳艾绠柣鎴ｅГ閺呮悂鏌￠崒妯猴拷閻庢艾缍婇弻銊╂偄瀹勯偊妫￠梺鍝勫�堕崐鏍拷瑜版帗鐓ラ柣鏂挎啞閻忣噣鏌熸搴″幋闁轰焦鎹囧顒勫Χ閸℃浼�

					// PTT闂備礁婀遍…鍫ニ囬悽绋跨闁惧浚鍋嗛埢鏃堟煣韫囨凹娼愭い蹇嬪�濋弻銈囨兜閸涱叀鏁块梺闈涙閸熸壆鍒掗崼銏犲К婵烇絿鎸嗛柣搴ゎ潐閻℃洜浜稿▎鎴濆灊闁挎稑瀚崑鏍ㄣ亜閹板墎鍒版い顐犲劦閺屾稑螖閸愵煈锟界紓浣介哺缁诲牓骞嗛崟顖ｆ晩闁绘挸瀵掗弸锟芥⒑閸濆嫬鏆熷┑顔炬暩濞戠敻宕奸弴鐐殿唴闂侀潧绻掓慨鎾晸?

				}
					break;

				case RELEASE_SUCCESS:
					MyLog.d(TAG,
							"mPttCall.getState() = RELEASE_SUCCESS Ring...");
					// PTT闂備礁婀遍…鍫ニ囬悽绋跨闁惧浚鍋嗛埢鏃堟煣韫囨洦鏀伴梻鍡楃秺閺屻倗娑甸崨顓℃暱闂侀潧妫楅崯鎵垝閸儲鎯為悷娆忓閻涖儵姊洪崨濠冨碍缂佸鏁婚幆锟藉川椤旂虎娲搁梺缁樺灦閿氭い顐犲劦閺屾稑螖閸愩劍顔員T

					mMediaSession.setConsumerOnPause(true);
					mMediaSession.setProducerOnPause(true);
					mMediaSession.setSpeakerphoneOn(false);

					String mobileNo4 = Engine.getInstance().getNetworkService()
							.getLocalIP(false);// NgnUriUtils.getUserName(mAVSession.getRemotePartyUri());
					// byte[] mobileNoBytes4 = BCDTools.Str2BCD(mobileNo4, 10);
					byte[] data4 = new byte[11];
					data4[0] = 4; // 4: nobody
					// System.arraycopy(mobileNoBytes4, 0, data4, 1, 10);
					SocketServer.sendMessage(new BaseSocketMessage(
							BaseSocketMessage.MSG_S_GROUP_STATE, data4));

					MyLog.i("ServiceSocketMode: type = "
							+ BaseSocketMessage.MSG_S_GROUP_STATE,
							"mobileNo = " + mobileNo4 + "; message = "
									+ "MSG_S_GROUP_STATE 4: no speaker."); // 缂傚倸鍊风粈浣猴拷椤掑嫬绠犻柣妯肩帛閸嬫劙鎮规担绛嬫綈闁匡拷4闂備焦瀵х粙鎺斿枈瀹ュ憘娑㈠Χ閸℃瑯娲告俊銈忛檮椤戞瑩宕ぐ鎺撴櫢闁匡拷
					if (args.getGroupCallType() == CommandType.SUPER_GROUP_AUDIO_CALL) {
						((Engine) Engine.getInstance()).playNotificationTone2();
					} else {
						((Engine) Engine.getInstance()).playNotificationTone();
					}
					if (mMediaSession.isOutgoing()) {
						ServiceAdhoc.getInstance().Hungup();
					}
					break;
				default:
					break;
				}
			}

		}
	}

	private boolean sendPTTSubscribeAckInfoMsg() {
		Log.d("zhangjie:sendPTTSubscribeAckInfoMsg()",
				"sendPTTSubscribeAckInfoMsg");
		PTTInfoMsg msg = new PTTInfoMsg(PTTTypes.PTT_TYPE_SUBSCRIBE_ACK);
		msg.setPTTPhoneNumber(NgnEngine.getInstance().getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, ""));
		msg.setPTTResult(PTTResultTypes.PTT_RLT_OK);
		Log.d(TAG, "send ptt info msg:" + msg.toString());
		return sendInfo(msg.toString(), "sunkaisens/PTT");
	}

	private void adhocRingtone(Context context, final Intent intent) {
		Log.d("HX-0328", "------start ringtone-----");

		if (!SystemVarTools.isLocalHangUp) {
			Log.d("HX-0328", "------!isLocalHangUp-----");
			if (mPlayer == null) {
				mPlayer = MediaPlayer.create(context, R.raw.user_busy);
			}
			mPlayer.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer arg0) {
					Log.d("HX-0328", "------ringtone end-----");
					handleSipEvent(intent);
					if (mPlayer != null) {
						mPlayer.stop();
						mPlayer.release();
						mPlayer = null;
					}
				}
			});

			mPlayer.start();

		} else {
			Log.d("HX-0328", "------isLocalHangUp-----");
			handleSipEvent(intent);
		}

	}

}
