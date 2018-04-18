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

import java.io.IOException;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Services.ServiceAdhoc;
import com.sunkaisens.skdroid.Services.ServiceGPSReport;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.Utils.Tools_data;
import com.sunkaisens.skdroid.util.GlobalVar;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ScreenIdentity extends BaseScreen {
	private final static String TAG = ScreenIdentity.class.getCanonicalName();
	private final INgnConfigurationService mConfigurationService;
	private final String ACTION_CHANGACCOUNT_EVENT = "com.sunkaisens.changeaccount";
	
	private TextView mEtDisplayName;
	private TextView mEtIMPU;
	private TextView mEtIMPI;
	private TextView mEtPassword;
	private TextView mEtRealm;
	private CheckBox mCbEarlyIMS;
	
	public ScreenIdentity() {
		super(SCREEN_TYPE.IDENTITY_T, TAG);
		
		mConfigurationService = getEngine().getConfigurationService();
	}

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_identity);

		ImageView back = (ImageView)findViewById(R.id.back);
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mScreenService.back();
			}
		});

	
        
        mEtDisplayName = (TextView)findViewById(R.id.screen_identity_editText_displayname);
        mEtIMPU = (TextView)findViewById(R.id.screen_identity_editText_impu);
        mEtIMPI = (TextView)findViewById(R.id.screen_identity_editText_impi);
        mEtPassword = (TextView)findViewById(R.id.screen_identity_editText_password);
        mEtRealm = (TextView)findViewById(R.id.screen_identity_editText_realm);
        mCbEarlyIMS = (CheckBox)findViewById(R.id.screen_identity_checkBox_earlyIMS);
        
        mEtDisplayName.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME));
        mEtIMPU.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPU, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPU));
        mEtIMPI.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI));
        mEtPassword.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_PASSWORD, NgnStringUtils.emptyValue()));
        mEtRealm.setText(mConfigurationService.getString(NgnConfigurationEntry.NETWORK_REALM, NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
        mCbEarlyIMS.setChecked(mConfigurationService.getBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS, NgnConfigurationEntry.DEFAULT_NETWORK_USE_EARLY_IMS));

        ImageView icon = (ImageView)findViewById(R.id.icon);
        icon.setImageResource(SystemVarTools.getThumbID(SystemVarTools.getImageIDFromNumber(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI, NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI))));

      //  super.addConfigurationListener(mEtDisplayName);
       // super.addConfigurationListener(mEtIMPU);
      //  super.addConfigurationListener(mEtIMPI);
      //  super.addConfigurationListener(mEtPassword);
      //  super.addConfigurationListener(mEtRealm);
      //  super.addConfigurationListener(mCbEarlyIMS);
        
        
    	Button screen_identity_button_exit = (Button)findViewById(R.id.screen_identity_button_exit);
		screen_identity_button_exit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
					Log.d("zhangjie:ScreenMyHome-processBackKeyDown()","切换帐号！");
//					((Engine)Engine.getInstance()).getSipService().unRegister();
//					((Main)(((Engine)Engine.getInstance()).getMainActivity())).exit();
					AlertDialog.Builder builder = new Builder(ScreenIdentity.this); 
			          builder.setMessage(getText(R.string.change_identy_tip)); 
			          builder.setTitle(getText(R.string.tips)); 
			          builder.setPositiveButton(getText(R.string.ok), 
			          new android.content.DialogInterface.OnClickListener() { 
			              @Override 
			              public void onClick(DialogInterface dialog, int which) {
//			  				try {
//								Tools_data.writeData(Main.mMessageReportHashMap);
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
			                  dialog.dismiss();
			                  ScreenLoginAccount.btLogin_isClicked = false;
			                  SystemVarTools.setContactOK(false);
//								Engine.getInstance().getSipService().unRegister();
//								broadcastChangeAccountEvent();
			                  
			                  	//清除自动登录设置
			            //      	Main.mMessageReportHashMap.put(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME) + "_login_chk","fasle");
			                 
			                  try {
			                	  Main.mMessageReportHashMap.remove(mEtIMPI.getText().toString().trim() + "_login_chk");
									Tools_data.writeData(Main.mMessageReportHashMap);
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								if (GlobalVar.bADHocMode) {
									ServiceAdhoc.getInstance().StopAdhoc();
			                  		ServiceLoginAccount.getInstance().adhoc_Logout();
			                  	}
			                  	else {
			                  		ScreenDownloadConcacts.getInstance().setUnSubscribeOK();
			                  		((Engine)Engine.getInstance()).getSipService().unRegister();
			                  		GlobalVar.mLogout = true;//gzc 标记注销操作
			                  		if(ServiceGPSReport.getInstance().getGPSServiceReprotStart())
			                  			ServiceGPSReport.getInstance().StopGPSReport();
			                  	}
								
					            //退出程序
								Intent intent = new Intent(getApplicationContext(), Main.class);
					            PendingIntent restartIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
					            AlarmManager mgr = (AlarmManager)SKDroid.getContext().getSystemService(Context.ALARM_SERVICE);
					            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); //1秒钟后重启应用
					            Main main = ((Main)(((Engine)Engine.getInstance()).getMainActivity()));
					            if(main != null){
					            	main.exit();
					            };

								Main.isFirstPTT_onKeyDown = true;
								Main.isFirstPTT_onKeyLongPress = true;
			              } 
			          }); 
			          builder.setNegativeButton(getText(R.string.cancel), new android.content.DialogInterface.OnClickListener() { 
			              @Override 
			              public void onClick(DialogInterface dialog, int which) { 
			                  dialog.dismiss(); 
			              } 
			          }); 
			          builder.create().show();
			}
		});
        
        
	}	

	protected void onPause() {
		if(super.mComputeConfiguration){
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME, 
					mEtDisplayName.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU, 
					mEtIMPU.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI, 
					mEtIMPI.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD, 
					mEtPassword.getText().toString().trim());
			mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM, 
					mEtRealm.getText().toString().trim());
			mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS, 
					mCbEarlyIMS.isChecked());
			
			// Compute
			if(!mConfigurationService.commit()){
				Log.e(TAG, "Failed to Commit() configuration");
			}
			
			super.mComputeConfiguration = false;
		}
		super.onPause();
	}
	private void broadcastChangeAccountEvent(){
		final Intent intent = new Intent(ACTION_CHANGACCOUNT_EVENT);
		NgnApplication.getContext().sendBroadcast(intent);
	}
}
