package com.sunkaisens.skdroid.Services;

import java.util.HashMap;

import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.tinyWRAP.SipMessage;
import org.doubango.utils.MyLog;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.ScreenLoginAccount;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.app.service.NativeService;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;
// author  duhaitao
public class ServiceLoginAccount {

	private static final String TAG = ServiceLoginAccount.class.getCanonicalName();;
	
	//
	private static ServiceLoginAccount instance = null;
	private final INgnSipService mSipService;
	private final INgnConfigurationService mConfigurationService;

	public static HashMap<String, Object> mMessageREPORTHashMap;
	public static HashMap<String, Object> mMessageIDHashMap = new HashMap<String, Object>();;
	
	private static Handler mHandler = null;

	private ServiceLoginAccount() {
		
		SystemVarTools.clear();//clear history data
		mSipService = Engine.getInstance().getSipService();
		mConfigurationService = Engine.getInstance().getConfigurationService();
		
		mMessageIDHashMap = Tools_data.readIDHashMap();
		if(null == mMessageIDHashMap)
			mMessageIDHashMap = new HashMap<String, Object>();
		
		if (GlobalSession.bSocketService) { //Socket��ʽ
			if (!SystemVarTools.setDefaultSetting_socket()) { //Socket�����ļ�������
				SystemVarTools.setDefaultSetting();
			}
		}
		else {
			SystemVarTools.setDefaultSetting();
		}
	}
	
	public static ServiceLoginAccount getInstance()
	{
		if(instance == null)
		{
			instance = new ServiceLoginAccount();
		}
		//
		return instance;
	}
	
	public void setOwnerScreen(ScreenLoginAccount owner)
	{
	}

	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		// Registration Event
		if (GlobalVar.bADHocMode == false && NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT
				.equals(action)) { // ACTION_REGISTRATION_EVENT
			NgnRegistrationEventArgs args = intent
					.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED); // 
			
			if (args == null) {
				Log.e(TAG, "Invalid event args");
				return;
			}
			switch (args.getEventType()) {
			case REGISTRATION_NOK:
				if(GlobalSession.bSocketService == false && mHandler != null)
				{ //��mHandler����Ϣ   
					int msg = args.getSipCode();
					if(msg != 403 && msg != 404){
						msg = 3000;
					}
					MyLog.d(TAG, "REGISTRATION_NOK msg = " + msg);
					mHandler.sendEmptyMessage(msg);
					mHandler.sendEmptyMessage(4000);
//					Toast.makeText(NgnApplication.getContext(), "��¼ʧ�ܣ����Ժ����ԣ�"+args.getPhrase() + ","+args.getSipCode(),Toast.LENGTH_SHORT).show();
				}

				break;
			case UNREGISTRATION_INPROGRESS:
				break;
			case UNREGISTRATION_OK:
//				if(GlobalSession.bSocketService == false && SystemVarTools.bLogin == false && mSipService.isRegistered() == false && ownerScreen.btLogin_isClicked)
				if(GlobalSession.bSocketService == false 
					&& SystemVarTools.bLogin == false 
					&& mSipService.isRegisteSessionConnected() == false
					&& !SystemVarTools.isNetChecking && mHandler != null)
				{ //��mHandler����Ϣ   
					mHandler.sendEmptyMessage(2000);
//					Toast.makeText(NgnApplication.getContext(), "��¼ʧ�ܣ������ʻ����������ȷ�ԣ�",Toast.LENGTH_SHORT).show();
				}
//				nativeService.stopService(new Intent(nativeService, GPSDataService.class));
//				nativeService.stopService(new Intent(NgnApplication.getContext(), GPSDataService.class));
				
				break; // �ؼ�

			case REGISTRATION_INPROGRESS:
				break;
			case REGISTRATION_OK:
				Log.e(TAG, "i am in the REGISTRATION_OK");
				if(GlobalSession.bSocketService == false && mHandler != null)
				{ //��mHandler����Ϣ   
					mHandler.sendEmptyMessage(1000);
//					((Engine) Engine.getInstance()).getScreenService().show(ScreenTabHome.class);
				}
				else
				{//
						
				}
				
//				nativeService.startService(new Intent(nativeService, GPSDataService.class));
//				nativeService.startService(new Intent(NgnApplication.getContext(), GPSDataService.class));
				
				break;

			case UNREGISTRATION_NOK:

				break;
			default:
				if(GlobalSession.bSocketService == false && mHandler != null)
				{ //��mHandler����Ϣ   
					mHandler.sendEmptyMessage(3000);
//					Toast.makeText(NgnApplication.getContext(), "��¼ʧ�ܣ����Ժ����ԣ�"+args.getPhrase() + ","+args.getSipCode(),Toast.LENGTH_SHORT).show();
				}

				break;
			}
		}
	}
	
	//
	public boolean adhoc_Login(String displayname,String account)
	{
		Log.e(TAG, "������ע��");
		GlobalVar.bADHocMode = true;
		ServiceAdhoc.getInstance().childrenMap.clear();
		/*if(account.trim().length() < 5){
		int _mLen = 5-account.trim().length();
			switch(_mLen ){
			case 1:
				account = "0" + account;
				break;
			case 2:
				account = "00" + account;
				break;
			case 3:
				account = "000" + account;
				break;
			case 4:
				account = "0000" + account;
				break;
			}	
		}*/
		GlobalVar.displayname = displayname;
		GlobalVar.account = account;
		
		mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, displayname.trim());
		mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, "sip:" + account.trim() + "@"
				+ mConfigurationService.getString(NgnConfigurationEntry.NETWORK_REALM, NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
		mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, account.trim());
		if (!mConfigurationService.commit()) {
			Log.e(TAG, "Failed to Commit() configuration");
		}
		if(!mSipService.ADHOC_Start(SKDroid.getContext()))
		{
			ServiceLoginAccount.getInstance().adhoc_Logout();
			Main main = ((Main)(((Engine)Engine.getInstance()).getMainActivity())); 
			if(main != null){
				main.exit();
			};
			return false;
		}
		return true;
	}
	
	//
	public void adhoc_Logout()
	{
		Log.d(TAG, "������ȥע��");
		GlobalVar.bADHocMode = false;
		mSipService.ADHOC_Stop();
	}
	
	//
//	public void login(String username, String pwd) {
	public boolean login(String username, String pwd) {
		Log.d(TAG,"����ע��");
		GlobalVar.bADHocMode = false;
		mConfigurationService.putString(
				NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
				username.trim());
		mConfigurationService.putString(
				NgnConfigurationEntry.IDENTITY_PASSWORD,
				pwd.trim());
		mConfigurationService.putString(
				NgnConfigurationEntry.IDENTITY_IMPU, "sip:"
						+ username.trim()
						+ "@" + mConfigurationService.getString(NgnConfigurationEntry.NETWORK_REALM,
								NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
		mConfigurationService.putString(
				NgnConfigurationEntry.IDENTITY_IMPI, username.trim());
		if (!mConfigurationService.commit()) {
			Log.e(TAG, "Failed to Commit() configuration");
		}
//		mSipService.register(nativeService);
		if(!GlobalVar.isSecuriteCardExist)
			GlobalVar.isSecuriteCardExist = Tools_data.getSecurityCard();
		return mSipService.register(SKDroid.getContext());
	}
			
	
	public static void setHandler(Handler handler) {
		mHandler = handler;
	}

}