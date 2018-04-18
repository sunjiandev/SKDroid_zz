package com.sunkaisens.skdroid.Services;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.doubango.ngn.NgnEngine;
import org.doubango.ngn.utils.NgnConfigurationEntry;

import android.util.Log;

import com.sunkaisens.skdroid.Engine;

/**
 * ���������صķ���
 * 
 * @author ZhichengGu
 *
 */
public class ServiceMessage {
	
	/**
	 * ����������Ϣ����
	 */
	public static void getOfflineMsg(){

		Thread httpGetThread = new Thread("REGISTRATIONOKHttpGet") {
			@Override
			public void run() {

				String middleString = "/services/offlineMessage/users/";

				String mIdentity = Engine
						.getInstance()
						.getConfigurationService()
						.getString(NgnConfigurationEntry.IDENTITY_IMPI,
								NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI); // "�Լ�"

				String mNetworkRealm = Engine
						.getInstance()
						.getConfigurationService()
						.getString(NgnConfigurationEntry.NETWORK_REALM,
								NgnConfigurationEntry.DEFAULT_NETWORK_REALM); // "sunkaisens.com"

				String localSipUri = "sip:" + mIdentity + "@"
						+ mNetworkRealm;

				String fileserverAndPort = NgnEngine
						.getInstance()
						.getConfigurationService()
						.getString(
								NgnConfigurationEntry.FILE_SERVER_URL,
								NgnConfigurationEntry.DEFAULT_FILE_SERVER_URL);
				int i = fileserverAndPort.indexOf(":");
				String fileserver = fileserverAndPort.substring(0, i);

				String portString = fileserverAndPort.substring(i + 1);

				if (fileserver != null && portString != null) {

					Log.e("file Server", "file Server��" + fileserver);
					Log.e("file Server  port", "file Server  port��"
							+ portString);
					Log.e("������ϢHttp����", fileserver + middleString
							+ localSipUri);
					
					HttpGet httpGet = new HttpGet("http://"+fileserver + middleString
							+ localSipUri);

					HttpHost targethost = new HttpHost(fileserver,
							Integer.parseInt(portString));

					HttpClient httpClient = new DefaultHttpClient();
					try {

						HttpResponse httpResponse = httpClient.execute(
								targethost, httpGet);

						if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
							Log.e("����������Ϣ", "����������Ϣ�ɹ�");

						} else {
							Log.e("����������Ϣ", "����������Ϣδ�õ���Ӧ~������");
						}

					} catch (ClientProtocolException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}
		};

		httpGetThread.start();
	}

}
