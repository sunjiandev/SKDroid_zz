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

import java.util.Properties;

import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnStringUtils;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.tinyWRAP.MediaSessionMgr;
import org.doubango.utils.MyLog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.MyProp;
import com.sunkaisens.skdroid.Utils.SystemVarTools;
import com.sunkaisens.skdroid.util.GlobalVar;

public class ScreenLoginSetting extends BaseScreen {
	private final static String TAG = ScreenLoginSetting.class
			.getCanonicalName();
	private final INgnConfigurationService mConfigurationService;

	// private EditText mEtDisplayName;
	// private EditText mEtIMPU;
	// private EditText mEtIMPI;
	// private EditText mEtPassword;
	/** 文件服务器url(ip:port) */
	private EditText mEtFileServer;

	/* 地图服务器地址(ip:port/realm) */
	private EditText mEtMapServer;

	/** 呈现服务器域名 */
	private EditText mEtRealm;

	/** 群组服务器域名 */
	private EditText mEtGroupRealm;
	private CheckBox mCbEarlyIMS;

	private EditText mEtProxyHost;
	private EditText mEtProxyPort;
	private Spinner mSpTransport;
	private Spinner mSpProxyDiscovery;
	private CheckBox mCbSigComp;
	private CheckBox mCbWiFi;
	private CheckBox mCb3G;
	private RadioButton mRbIPv4;
	private RadioButton mRbIPv6;

	private EditText mEtReflashTime;
	private CheckBox mCbEnableStun;
	private RelativeLayout mRlStunServer;
	private EditText mEtStunServer;
	private EditText mEtStunPort;

	private CheckBox mEtadvancedOpt;
	private LinearLayout mEtscreen_advanced_option_block;

	private CheckBox mWtiteLogsTofile;// 是否将日志写入到文件中开关

	private CheckBox mWtiteSysLogsTofile;// 是否将日志写入到文件中开关

	private final static String[] sSpinnerTransportItems = new String[] {
			NgnConfigurationEntry.DEFAULT_NETWORK_TRANSPORT.toUpperCase(),
			"TCP", "TLS"/* , "SCTP" */};
	private final static String[] sSpinnerProxydiscoveryItems = new String[] {
			NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_DISCOVERY,
			NgnConfigurationEntry.PCSCF_DISCOVERY_DNS_SRV /* , "DHCPv4/v6", "Both" */};

	public ScreenLoginSetting() {
		super(SCREEN_TYPE.IDENTITY_T, TAG);

		mConfigurationService = getEngine().getConfigurationService();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_login_setting);

		// mEtIMPU =
		// (EditText)findViewById(R.id.screen_identity_editText_impu);//duhaitao
		// 舍弃
		// mEtIMPI =
		// (EditText)findViewById(R.id.screen_identity_editText_impi);//duhaitao
		// 舍弃
		mEtRealm = (EditText) findViewById(R.id.screen_identity_editText_realm); // duhaitao
																					// 添加
		mEtGroupRealm = (EditText) findViewById(R.id.screen_identity_editText_proxy_realm);
		mEtFileServer = (EditText) findViewById(R.id.screen_identity_editText_fileurl); // duhaitao
																						// 添加
		mCbEarlyIMS = (CheckBox) findViewById(R.id.screen_identity_checkBox_earlyIMS);
		mEtMapServer = (EditText) findViewById(R.id.screen_identity_editText_mapurl);//

		mEtProxyHost = (EditText) findViewById(R.id.screen_network_editText_pcscf_host);
		mEtProxyPort = (EditText) findViewById(R.id.screen_network_editText_pcscf_port);
		mSpTransport = (Spinner) findViewById(R.id.screen_network_spinner_transport);
		mSpProxyDiscovery = (Spinner) findViewById(R.id.screen_network_spinner_pcscf_discovery);
		mCbSigComp = (CheckBox) findViewById(R.id.screen_network_checkBox_sigcomp);
		mCbWiFi = (CheckBox) findViewById(R.id.screen_network_checkBox_wifi);
		mCb3G = (CheckBox) findViewById(R.id.screen_network_checkBox_3g);
		mRbIPv4 = (RadioButton) findViewById(R.id.screen_network_radioButton_ipv4);
		mRbIPv6 = (RadioButton) findViewById(R.id.screen_network_radioButton_ipv6);

		// add by gle
		mEtReflashTime = (EditText) findViewById(R.id.screen_reflash_editText_loginTime);
		mCbEnableStun = (CheckBox) findViewById(R.id.screen_natt_checkBox_stun);
		mRlStunServer = (RelativeLayout) findViewById(R.id.screen_natt_relativeLayout_stun_server);
		mEtStunServer = (EditText) findViewById(R.id.screen_natt_editText_stun_server);
		mEtStunPort = (EditText) findViewById(R.id.screen_natt_editText_stun_port);

		mWtiteLogsTofile = (CheckBox) findViewById(R.id.screen_open_logs);
		mWtiteSysLogsTofile = (CheckBox) findViewById(R.id.screen_open_syslogs);

		mEtadvancedOpt = (CheckBox) findViewById(R.id.screen_advanced_option);
		mEtscreen_advanced_option_block = (LinearLayout) findViewById(R.id.screen_advanced_option_block);
		mEtadvancedOpt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isChecked = ((CheckBox) v).isChecked();
				if (isChecked) {
					mEtscreen_advanced_option_block.setVisibility(View.VISIBLE);
				} else {
					mEtscreen_advanced_option_block.setVisibility(View.GONE);
				}
			}
		});

		mEtRealm.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				if (!hasFocus) {
					String domain = ((EditText) arg0).getText().toString()
							.trim();
					// 检测域名格式是否合法
					if (!NgnUriUtils.checkRealm(domain)) {
						SystemVarTools.showToast(ScreenLoginSetting.this
								.getString(R.string.com_error));
					} else {
						// 群组服务器、文件传输服务器、地图服务器、cscf配置联动
						mEtFileServer
								.setText(SystemVarTools.FileServerDomaimPrefix
										+ "." + domain + ":"
										+ SystemVarTools.FileServerPort);
						mEtMapServer
								.setText(SystemVarTools.MapServerDomaimPrefix
										+ "." + domain
										+ SystemVarTools.MapServerDomaiSuffix);
						mEtProxyHost.setText(SystemVarTools.CscfDomaimPrefix
								+ "." + domain);
						String GroupServerPort = mConfigurationService
								.getString(
										NgnConfigurationEntry.NETWORK_GROUP_PORT,
										NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT);
						mEtGroupRealm
								.setText(SystemVarTools.GroupServerDomaimPrefix
										+ "." + domain + ":" + GroupServerPort);
					}
				}
			}
		});

		mEtRealm.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				String domain = s.toString().trim();
				mEtFileServer.setText(SystemVarTools.FileServerDomaimPrefix
						+ "." + domain + ":" + SystemVarTools.FileServerPort);
				mEtMapServer.setText(SystemVarTools.MapServerDomaimPrefix + "."
						+ domain + SystemVarTools.MapServerDomaiSuffix);
				mEtProxyHost.setText(SystemVarTools.CscfDomaimPrefix + "."
						+ domain);
				String GroupServerPort = mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_GROUP_PORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT);
				mEtGroupRealm.setText(SystemVarTools.GroupServerDomaimPrefix
						+ "." + domain + ":" + GroupServerPort);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				MyLog.d(TAG, "afterTextChanged");
			}
		});

		boolean logs_checked = mConfigurationService.getBoolean(
				NgnConfigurationEntry.LOGS_WRITE_TO_FILE_OPEN, false);
		mWtiteLogsTofile.setChecked(logs_checked);
		MyLog.setMYLOG_WRITE_TO_FILE(mWtiteLogsTofile.isChecked());

		boolean syslogs_checked = mConfigurationService.getBoolean(
				NgnConfigurationEntry.LOGS_WRITE_TO_FILE_SYS_OPEN, false);
		mWtiteSysLogsTofile.setChecked(syslogs_checked);
		MyLog.setMYLOG_WRITE_TO_FILE_SYS(mWtiteSysLogsTofile.isChecked());

		// mEtIMPU.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPU,//duhaitao
		// 舍弃
		// NgnConfigurationEntry.DEFAULT_IDENTITY_IMPU));
		// mEtIMPI.setText(mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI,//duhaitao
		// 舍弃
		// NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI));

		mEtRealm.setText(mConfigurationService.getString(
				NgnConfigurationEntry.NETWORK_REALM,
				NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
		mEtFileServer.setText(mConfigurationService.getString(
				NgnConfigurationEntry.FILE_SERVER_URL,
				NgnConfigurationEntry.DEFAULT_FILE_SERVER_URL));
		mEtMapServer.setText(mConfigurationService.getString(
				NgnConfigurationEntry.MAP_SERVER_URL,
				NgnConfigurationEntry.DEFAULT_MAP_SERVER_URL));

		String groupServerUrl = mConfigurationService.getString(
				NgnConfigurationEntry.NETWORK_GROUP_REALM,
				NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM)
				+ ":"
				+ mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_GROUP_PORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT);

		mEtGroupRealm.setText(groupServerUrl);

		Log.d(TAG,"NETWORK_GROUP_REALM: "
						+ mConfigurationService
								.getString(
										NgnConfigurationEntry.NETWORK_GROUP_REALM,
										NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM));

		Log.d(TAG,"NETWORK_GROUP_PORT: "
						+ mConfigurationService
								.getString(
										NgnConfigurationEntry.NETWORK_GROUP_PORT,
										NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT));

		mCbEarlyIMS.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
				NgnConfigurationEntry.DEFAULT_NETWORK_USE_EARLY_IMS));

		// spinners
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, sSpinnerTransportItems);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpTransport.setAdapter(adapter);
		adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item,
				sSpinnerProxydiscoveryItems);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpProxyDiscovery.setAdapter(adapter);

		mEtProxyHost.setText(mConfigurationService.getString(
				NgnConfigurationEntry.NETWORK_PCSCF_HOST,
				NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST));
		mEtProxyPort.setText(Integer.toString(mConfigurationService.getInt(
				NgnConfigurationEntry.NETWORK_PCSCF_PORT,
				NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT)));
		mSpTransport.setSelection(super.getSpinnerIndex(mConfigurationService
				.getString(NgnConfigurationEntry.NETWORK_TRANSPORT,
						sSpinnerTransportItems[0]), sSpinnerTransportItems));

		// add by gle
		mEtReflashTime.setText(Integer.toString(mConfigurationService.getInt(
				NgnConfigurationEntry.NETWORK_REGISTRATION_TIMEOUT,
				NgnConfigurationEntry.DEFAULT_NETWORK_REGISTRATION_TIMEOUT)));
		mCbEnableStun.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.NATT_USE_STUN,
				NgnConfigurationEntry.DEFAULT_NATT_USE_STUN));
		// mRlStunServer.setVisibility(mCbEnableStun.isChecked() ? View.VISIBLE
		// : View.INVISIBLE);

		mEtStunServer.setText(mConfigurationService.getString(
				NgnConfigurationEntry.NATT_STUN_SERVER,
				NgnConfigurationEntry.DEFAULT_NATT_STUN_SERVER));
		mEtStunPort
				.setText(mConfigurationService.getString(
						NgnConfigurationEntry.NATT_STUN_PORT,
						Integer.toString(NgnConfigurationEntry.DEFAULT_NATT_STUN_PORT)));

		mSpProxyDiscovery.setSelection(super.getSpinnerIndex(
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY,
						sSpinnerProxydiscoveryItems[0]),
				sSpinnerProxydiscoveryItems));
		mCbSigComp.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.NETWORK_USE_SIGCOMP,
				NgnConfigurationEntry.DEFAULT_NETWORK_USE_SIGCOMP));

		mCbWiFi.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.NETWORK_USE_WIFI,
				NgnConfigurationEntry.DEFAULT_NETWORK_USE_WIFI));
		mCb3G.setChecked(mConfigurationService.getBoolean(
				NgnConfigurationEntry.NETWORK_USE_3G,
				NgnConfigurationEntry.DEFAULT_NETWORK_USE_3G));
		mRbIPv4.setChecked(mConfigurationService.getString(
				NgnConfigurationEntry.NETWORK_IP_VERSION,
				NgnConfigurationEntry.DEFAULT_NETWORK_IP_VERSION)
				.equalsIgnoreCase("ipv4"));
		mRbIPv6.setChecked(!mRbIPv4.isChecked());

		// super.addConfigurationListener(mEtDisplayName);
		// super.addConfigurationListener(mEtIMPU);
		// super.addConfigurationListener(mEtIMPI);
		// super.addConfigurationListener(mEtPassword);
		super.addConfigurationListener(mEtMapServer);
		super.addConfigurationListener(mEtFileServer);
		super.addConfigurationListener(mEtRealm);
		super.addConfigurationListener(mEtGroupRealm);
		super.addConfigurationListener(mCbEarlyIMS);

		// add listeners (for the configuration)
		super.addConfigurationListener(mEtProxyHost);
		super.addConfigurationListener(mEtProxyPort);
		super.addConfigurationListener(mSpTransport);
		super.addConfigurationListener(mSpProxyDiscovery);
		super.addConfigurationListener(mCbSigComp);
		super.addConfigurationListener(mCbWiFi);
		super.addConfigurationListener(mCb3G);
		super.addConfigurationListener(mRbIPv4);
		super.addConfigurationListener(mRbIPv6);

		super.addConfigurationListener(mWtiteLogsTofile);
		super.addConfigurationListener(mWtiteSysLogsTofile);

		// add by gle
		mCbEnableStun
				.setOnCheckedChangeListener(mCbEnableStun_OnCheckedChangeListener);
		super.addConfigurationListener(mEtStunServer);
		super.addConfigurationListener(mEtStunPort);
		super.addConfigurationListener(mEtReflashTime);

	}

	protected void onPause() {
		if (super.mComputeConfiguration) {
			// mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
			// mEtDisplayName.getText().toString().trim());
			// mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU,
			// mEtIMPU.getText().toString().trim());
			// mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI,
			// mEtIMPI.getText().toString().trim());
			// mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD,
			// mEtPassword.getText().toString().trim());

			mConfigurationService.putBoolean(
					NgnConfigurationEntry.LOGS_WRITE_TO_FILE_OPEN,
					mWtiteLogsTofile.isChecked());
			mConfigurationService.putBoolean(
					NgnConfigurationEntry.LOGS_WRITE_TO_FILE_SYS_OPEN,
					mWtiteSysLogsTofile.isChecked());

			if (NgnUriUtils.checkRealm(mEtRealm.getText().toString().trim())) {
				mConfigurationService.putString(
						NgnConfigurationEntry.NETWORK_REALM, mEtRealm.getText()
								.toString().trim());
			} else {
				SystemVarTools.showToast(ScreenLoginSetting.this
						.getString(R.string.com_error));
			}

			MyLog.setMYLOG_WRITE_TO_FILE(mWtiteLogsTofile.isChecked());

			mConfigurationService.putString(
					NgnConfigurationEntry.FILE_SERVER_URL, mEtFileServer
							.getText().toString().trim());

			mConfigurationService.putString(
					NgnConfigurationEntry.MAP_SERVER_URL, mEtMapServer
							.getText().toString().trim());

			// 打印服务器域名
			String realm = mConfigurationService.getString(
					NgnConfigurationEntry.NETWORK_REALM,
					NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
			Log.e(TAG, "NETWORK_REALM: " + realm);

			String GroupServerurl = mEtGroupRealm.getText().toString().trim();
			String groupRealm = "";
			String groupPort = "";
			if (GroupServerurl.contains(":")) {
				groupRealm = GroupServerurl.split(":")[0];
				groupPort = GroupServerurl.split(":")[1];
			}

			mConfigurationService.putString(
					NgnConfigurationEntry.NETWORK_GROUP_REALM,
					groupRealm.trim());

			// 打印群组服务器域名
			mConfigurationService.getString(
					NgnConfigurationEntry.NETWORK_GROUP_REALM,
					NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);
			Log.e(TAG, "NETWORK_GROUP_REALM: "+ groupRealm);

			mConfigurationService.putString(
					NgnConfigurationEntry.NETWORK_GROUP_PORT, groupPort.trim());

			// 打印port号
			String port = mConfigurationService.getString(
					NgnConfigurationEntry.NETWORK_GROUP_PORT,
					NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT);
			Log.e(TAG, "NETWORK_GROUP_PORT: " + port);

			mConfigurationService.putBoolean(
					NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
					mCbEarlyIMS.isChecked());

			mConfigurationService.putString(
					NgnConfigurationEntry.NETWORK_PCSCF_HOST, mEtProxyHost
							.getText().toString().trim());
			GlobalVar.pcscfIp = mEtProxyHost.getText().toString().trim();
			mConfigurationService.putInt(
					NgnConfigurationEntry.NETWORK_PCSCF_PORT,
					NgnStringUtils.parseInt(mEtProxyPort.getText().toString()
							.trim(),
							NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT));
			mConfigurationService.putString(
					NgnConfigurationEntry.NETWORK_TRANSPORT,
					ScreenLoginSetting.sSpinnerTransportItems[mSpTransport
							.getSelectedItemPosition()]);
			mConfigurationService
					.putString(
							NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY,
							ScreenLoginSetting.sSpinnerProxydiscoveryItems[mSpProxyDiscovery
									.getSelectedItemPosition()]);
			mConfigurationService.putBoolean(
					NgnConfigurationEntry.NETWORK_USE_SIGCOMP,
					mCbSigComp.isChecked());
			mConfigurationService
					.putBoolean(NgnConfigurationEntry.NETWORK_USE_WIFI,
							mCbWiFi.isChecked());
			mConfigurationService.putBoolean(
					NgnConfigurationEntry.NETWORK_USE_3G, mCb3G.isChecked());
			mConfigurationService.putString(
					NgnConfigurationEntry.NETWORK_IP_VERSION,
					mRbIPv4.isChecked() ? "ipv4" : "ipv6");

			// add by gle

			mConfigurationService.putString(
					NgnConfigurationEntry.NATT_STUN_SERVER, mEtStunServer
							.getText().toString());
			mConfigurationService.putString(
					NgnConfigurationEntry.NATT_STUN_PORT, mEtStunPort.getText()
							.toString());

			mConfigurationService
					.putInt(NgnConfigurationEntry.NETWORK_REGISTRATION_TIMEOUT,
							NgnStringUtils
									.parseInt(
											mEtReflashTime.getText().toString()
													.trim(),
											NgnConfigurationEntry.DEFAULT_NETWORK_REGISTRATION_TIMEOUT));
			mConfigurationService.putBoolean(
					NgnConfigurationEntry.NATT_USE_STUN,
					mCbEnableStun.isChecked());

			// Compute
			if (!mConfigurationService.commit()) {
				Log.e(TAG + "Failed", "Failed to Commit() configuration");
			} else {
				MediaSessionMgr.defaultsSetStunEnabled(mConfigurationService
						.getBoolean(NgnConfigurationEntry.NATT_USE_STUN,
								NgnConfigurationEntry.DEFAULT_NATT_USE_STUN));

			}

			// //保存更新后的配置文件
			// saveSetting2Config();

			super.mComputeConfiguration = false;
		}
		super.onPause();
	}

	/**
	 * 回写配置文件
	 * 
	 * @return
	 */
	private boolean saveSetting2Config() {
		Properties properties = MyProp.loadConfig();
		if (properties == null) {
			return false;
		}

		properties.setProperty(NgnConfigurationEntry.NETWORK_REALM.substring(0,
				NgnConfigurationEntry.NETWORK_REALM.indexOf(".")),
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_REALM));

		properties.setProperty(NgnConfigurationEntry.FILE_SERVER_URL.substring(
				0, NgnConfigurationEntry.FILE_SERVER_URL.indexOf(".")),
				mConfigurationService.getString(
						NgnConfigurationEntry.FILE_SERVER_URL,
						"192.168.1.192:13000"));

		properties
				.setProperty(
						NgnConfigurationEntry.NETWORK_GROUP_REALM.substring(0,
								NgnConfigurationEntry.NETWORK_GROUP_REALM
										.indexOf(".")),
						mConfigurationService
								.getString(
										NgnConfigurationEntry.NETWORK_GROUP_REALM,
										NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM));

		properties.setProperty(NgnConfigurationEntry.NETWORK_GROUP_PORT
				.substring(0,
						NgnConfigurationEntry.NETWORK_GROUP_PORT.indexOf(".")),
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_GROUP_PORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT));

		properties.setProperty(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS
				.substring(0, NgnConfigurationEntry.NETWORK_USE_EARLY_IMS
						.indexOf(".")), String.valueOf(mConfigurationService
				.getBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
						NgnConfigurationEntry.DEFAULT_NETWORK_USE_EARLY_IMS)));

		properties.setProperty(NgnConfigurationEntry.NETWORK_PCSCF_HOST
				.substring(0,
						NgnConfigurationEntry.NETWORK_PCSCF_HOST.indexOf(".")),
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_PCSCF_HOST,
						NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST));
		properties.setProperty(NgnConfigurationEntry.NETWORK_PCSCF_PORT
				.substring(0,
						NgnConfigurationEntry.NETWORK_PCSCF_PORT.indexOf(".")),
				Integer.toString(mConfigurationService.getInt(
						NgnConfigurationEntry.NETWORK_PCSCF_PORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT)));
		properties.setProperty(NgnConfigurationEntry.NETWORK_TRANSPORT
				.substring(0,
						NgnConfigurationEntry.NETWORK_TRANSPORT.indexOf(".")),
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_TRANSPORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_TRANSPORT
								.toUpperCase()));
		properties.setProperty(NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY
				.substring(0, NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY
						.indexOf(".")), mConfigurationService.getString(
				NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY,
				NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_DISCOVERY));
		properties
				.setProperty(
						NgnConfigurationEntry.NETWORK_USE_SIGCOMP.substring(0,
								NgnConfigurationEntry.NETWORK_USE_SIGCOMP
										.indexOf(".")),
						String.valueOf(mConfigurationService
								.getBoolean(
										NgnConfigurationEntry.NETWORK_USE_SIGCOMP,
										NgnConfigurationEntry.DEFAULT_NETWORK_USE_SIGCOMP)));

		properties.setProperty(NgnConfigurationEntry.NETWORK_USE_WIFI
				.substring(0,
						NgnConfigurationEntry.NETWORK_USE_WIFI.indexOf(".")),
				String.valueOf(mConfigurationService.getBoolean(
						NgnConfigurationEntry.NETWORK_USE_WIFI,
						NgnConfigurationEntry.DEFAULT_NETWORK_USE_WIFI)));
		properties.setProperty(NgnConfigurationEntry.NETWORK_USE_3G.substring(
				0, NgnConfigurationEntry.NETWORK_USE_3G.indexOf(".")), String
				.valueOf(mConfigurationService.getBoolean(
						NgnConfigurationEntry.NETWORK_USE_3G,
						NgnConfigurationEntry.DEFAULT_NETWORK_USE_3G)));
		properties.setProperty(NgnConfigurationEntry.NETWORK_IP_VERSION
				.substring(0,
						NgnConfigurationEntry.NETWORK_IP_VERSION.indexOf(".")),
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_IP_VERSION,
						NgnConfigurationEntry.DEFAULT_NETWORK_IP_VERSION));

		// 保存更新后的配置文件
		MyProp.saveConfig(properties);

		return true;
	}

	private OnCheckedChangeListener mCbEnableStun_OnCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// mRlStunServer.setVisibility(isChecked ? View.VISIBLE
			// : View.INVISIBLE);
			mComputeConfiguration = true;
		}
	};
}
