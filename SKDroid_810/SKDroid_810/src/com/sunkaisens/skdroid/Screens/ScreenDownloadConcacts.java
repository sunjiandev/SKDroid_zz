package com.sunkaisens.skdroid.Screens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.SystemClock;
import android.util.Log;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.component.NodeResource;

//author  杜海涛
public class ScreenDownloadConcacts {
	private static String TAG = ScreenDownloadConcacts.class.getCanonicalName();

	private static ScreenDownloadConcacts mScreenDownloadConcacts;
	private List<NodeResource> list;
	private Stack<NodeResource> stk = new Stack<NodeResource>();
	private String mIdentity;
	private String mNetworkGroupRealm;
	private String mNetworkRealm;
	private String mNetworkGroupPort;
	private String XMLContact = "";
	private int timeout = 10;// s

	private static boolean isSubscribeServiceGroup = false;
	private static boolean isSubscribePublicGroup = false;

	// add  业务组
	private String XMLContactNet = "";
	private List<NodeResource> listNet;

	// 组呼通讯录
	private String XMLContactCommGroup = "";
	private List<NodeResource> listCommGroup;

	private String ReqContact = "";
	private String auidString = "";

	protected static boolean isShenYang = false;

	private String shenYangString = "/services/org.openmobilealliance.group-usage-list-sy/users/sip:";
	// private String NotshenYangString =
	// "/services/org.openmobilealliance.group-usage-list/users/sip:";
	private String NotshenYangString = "/services/public-group/users/sip:";

	private List<String> contactReqList;

	private String XMLContactGlobalGroup = ""; // 公共台通讯录
	private List<NodeResource> listGlobalGroup;

	private String XMLContactSubscribeGroup = ""; // 订阅号通讯录
	private List<NodeResource> listSubscribeGroup;

	// private Stack<NodeResource> stkNet = new Stack<NodeResource>();

	private ScreenDownloadConcacts() {
		list = new ArrayList<NodeResource>();
		listNet = new ArrayList<NodeResource>();
		listCommGroup = new ArrayList<NodeResource>();
		listGlobalGroup = new ArrayList<NodeResource>();
		listSubscribeGroup = new ArrayList<NodeResource>();
		isSubscribeServiceGroup = false;
		isSubscribePublicGroup = false;

		contactReqList = new ArrayList<String>();
	}

	public void clearXML() {
		XMLContact = null;
		XMLContactNet = null;
		XMLContactCommGroup = null;
		ReqContact = null;
		XMLContactGlobalGroup = null;
		XMLContactSubscribeGroup = null;

	}

	public static ScreenDownloadConcacts getInstance() {
		if (mScreenDownloadConcacts == null) {
			mScreenDownloadConcacts = new ScreenDownloadConcacts();
		}
		return mScreenDownloadConcacts;
	}

	public static void setUnSubscribeOK() {
		isSubscribeServiceGroup = false;
		isSubscribePublicGroup = false;
	}

	/**
	 * 下载全网个呼通讯录
	 * 
	 * @return
	 */
	public boolean downloadPublicGroup() {

		MyLog.d(TAG, "Download public groups.");

		mIdentity = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_IMPI,
						NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI); // "自己"

		mNetworkGroupRealm = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_GROUP_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM); // "192.168.4.10"

		mNetworkRealm = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_REALM); // "sunkaisens.com"

		mNetworkGroupPort = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_GROUP_PORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT);
		String pwd = (String) Main.mMessageReportHashMap
				.get(mIdentity + "_pwd");

		MyLog.d(TAG, "downloadPublicGroup 群组服务器域名 = " + mNetworkGroupRealm);
		MyLog.d(TAG, "downloadPublicGroup 群组服务器端口号 = " + mNetworkGroupPort);
		MyLog.d(TAG, "downloadPublicGroup 服务器域名 = " + mNetworkRealm);
		MyLog.d(TAG, "downloadPublicGroup 用户名/密码= " + mIdentity + "/" + pwd);

		String uriSub = "";
		if (isShenYang) {
			// Log.e("沈阳项目", "沈阳项目");
			uriSub = shenYangString;
		} else {
			// Log.e("不是沈阳项目", "不是沈阳项目");
			uriSub = NotshenYangString;
		}
		uriSub = uriSub + mIdentity + "@" + mNetworkRealm + "/index";

		// if (isSubscribePublicGroup == false) {
		// final NgnSubscriptionSession subscriptionSessionPublic =
		// NgnSubscriptionSession
		// .createOutgoingSession(Engine.getInstance().getSipService()
		// .getSipStack(), "sip:" + mIdentity + "@"
		// + mNetworkRealm, "sip:public@" + mNetworkRealm,
		// NgnSubscriptionSession.EventPackageType.Group);
		// subscriptionSessionPublic.subscribe();
		// isSubscribePublicGroup = true;
		// }

		// while (true) {
		for (int r = 0; r < 5; r++) {
			try {
				if (mNetworkGroupPort.equals("8955")) {
					MyLog.d(TAG, "downloadPublicGroup SSL方式下载，端口号:"
							+ mNetworkGroupPort);

					XMLContact = initSSLALL(mNetworkGroupRealm,
							Integer.parseInt(mNetworkGroupPort), uriSub,
							mNetworkRealm, mIdentity, pwd);
					
				} else {
					MyLog.d(TAG, "downloadPublicGroup 普通方式下载，端口号:"
							+ mNetworkGroupPort);
					XMLContact = normalDowanload(uriSub);
				}
				break;
			} catch (Exception e) {
				e.printStackTrace();
				// return false;
				MyLog.e(TAG, "downloadPublicGroup 下载全部通讯录失败");

				XMLContact = "failed";
				SystemClock.sleep(3000);
				continue;
			}
		}

		if (!XMLContact.equals("failed")) {
			 MyLog.d(TAG, "downloadPublicGroup 下载全部通讯录源文件:" + XMLContact);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * HttpUrlConnection支持所有Https免验证，不建议使用
	 * 
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public String initSSLALL(String serverIp, int port, String uriSub,
			String domain, String name, String pwd) throws Exception {

		String method = "";
		String realm = "";
		String qop = "";
		String nonce = "";
		String opaque = "";
		String result = "";
		int responseCode;

		String uri = "https://" + serverIp + ":" + port + uriSub;

		MyLog.e(TAG, "initSSLALL   uri:" + uri);

		String localSipUri = "sip:" + name + "@test.com";
		String xcapHeader = "X-XCAP-Asserted-Identity";

		URL url = new URL(uri);
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new TrustManager[] { new TrustAllManager() }, null);
		HttpsURLConnection.setDefaultSSLSocketFactory(context
				.getSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		});
		HttpsURLConnection connection = (HttpsURLConnection) url
				.openConnection();
		connection.setDoInput(true);
		connection.setDoOutput(false);
		connection.setRequestMethod("GET");
		connection.connect();

		responseCode = connection.getResponseCode();

		if (responseCode == 401) {
			String headerValue = connection.getHeaderField("WWW-Authenticate");

			String[] valus = headerValue.split(",");
			for (int i = 0; i < valus.length; i++) {
				if (valus[i].contains("realm")) {
					method = valus[i].substring(0, 6);
					realm = valus[i].substring(14, valus[i].length() - 1);
				} else if (valus[i].contains("nonce")) {
					nonce = valus[i].substring(7, valus[i].length() - 1);
				} else if (valus[i].contains("opaque")) {
					opaque = valus[i].substring(8, valus[i].length() - 1);
				} else if (valus[i].contains("qop")) {
					qop = valus[i].substring(5, valus[i].length() - 1);
				}
			}
			connection.disconnect();
			connection = (HttpsURLConnection) url.openConnection();

			MyLog.d(TAG, "initSSLALL realm:" + realm + " qop:" + qop);

			MyLog.d(TAG, "initSSLALL nonce:" + nonce + "   opaque:" + opaque);

			String ha1 = encodeByMD5(name + ":" + realm + ":" + pwd);
			String ha2 = encodeByMD5("GET:" + uriSub);
			String cnonce = DigestScheme.createCnonce();
			String nc = "00000001";
			String response = encodeByMD5(ha1 + ":" + nonce + ":" + nc + ":"
					+ cnonce + ":" + qop + ":" + ha2);

			MyLog.d(TAG, "initSSLALL  ha1:" + ha1);
			MyLog.d(TAG, "initSSLALL  ha2:" + ha2);
			MyLog.d(TAG, "initSSLALL  response:" + response);

			String Authorization = method + " realm=" + realm + ",qop=" + qop
					+ ",nonce=" + nonce + ",opaque=" + opaque + ",username=\""
					+ name + "\",uri=\"" + uriSub + "\",cnonce=\"" + cnonce
					+ "\",nc=\"" + nc + "\"," + "response=\""
					+ response.toUpperCase() + "\"";

			connection.addRequestProperty("Authorization", Authorization);
			connection.addRequestProperty(xcapHeader, localSipUri);

			connection.setDoInput(true);
			connection.setDoOutput(false);
			connection.setRequestMethod("GET");
			connection.connect();

			responseCode = connection.getResponseCode();

			InputStream in = null;
			try {

				if (responseCode == 200) {
					in = connection.getInputStream();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(in));
					String line = "";
					StringBuffer resultBuf = new StringBuffer();
					while ((line = reader.readLine()) != null) {
						resultBuf.append(line);
					}
					result = resultBuf.toString();
				}
			} catch (Exception e) {
				throw e;
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		}

		MyLog.d(TAG, "initSSLALL Result:" + result);

		return result;
	}

	/**
	 * MD5加密
	 * 
	 * @param text
	 * @return
	 */
	public static String encodeByMD5(String str) {
		if (str != null) {
			try {
				MessageDigest sMD5Digest = null;
				final BigInteger bigInt;
				sMD5Digest = MessageDigest.getInstance("MD5");
				synchronized (sMD5Digest) {
					sMD5Digest.reset();
					bigInt = new BigInteger(1, sMD5Digest.digest(str
							.getBytes("UTF-8")));
				}
				String hash = bigInt.toString(16);
				while (hash.length() < 32) {
					hash = "0" + hash;
				}
				return hash.toUpperCase();
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		}

		return "";
	}

	public String normalDowanload(String uriSub) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpRequest httpRequest = null;

		String localSipUri = "sip:" + mIdentity + "@" + mNetworkRealm;
		String xcapHeader = "X-XCAP-Asserted-Identity";
		String result = "";

		try {
			if (isShenYang) {
				MyLog.d(TAG, "normalDowanload 沈阳项目");

				httpRequest = new HttpGet("http://" + mNetworkRealm + ":"
						+ mNetworkGroupPort + uriSub);

			} else {
				MyLog.d(TAG, "normalDowanload 不是沈阳项目");

				httpRequest = new HttpGet("http://" + mNetworkRealm + ":"
						+ mNetworkGroupPort + uriSub);
			}
			httpRequest.addHeader(xcapHeader, localSipUri);
			HttpHost targethost = null;
			try {
				targethost = new HttpHost(mNetworkGroupRealm,
						Integer.valueOf(mNetworkGroupPort));
			} catch (NumberFormatException e) {
				// TODO: handle exception

				MyLog.d(TAG, "normalDowanload  群组服务器端口出现异常，改为默认端口: " + mNetworkGroupPort);

				if (mNetworkGroupRealm == null) {
					targethost = new HttpHost(
							NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM,
							Integer.valueOf(mNetworkGroupPort));

					MyLog.d(TAG, "normalDowanload  群组服务器地址出现异常，改为默认地址："
							+ NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);
				} else {
					targethost = new HttpHost(mNetworkGroupRealm, Integer.valueOf(mNetworkGroupPort));
				}
				e.printStackTrace();
			}

			HttpConnectionParams.setConnectionTimeout(httpRequest.getParams(),
					timeout * 1000);
			HttpConnectionParams.setSoTimeout(httpRequest.getParams(),
					timeout * 1000);
			HttpResponse httpResponse = httpClient.execute(targethost,
					httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				MyLog.d(TAG, "normalDowanload 状态OK！");

				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(entity.getContent()));
					String line = null;
					while ((line = br.readLine()) != null) {
						result += line;
					}
					// MyLog.d(TAG,"联系人列表： " + XMLContact);

				}
			} else {

				MyLog.d(TAG, "normalDowanload 请求未得到响应~！！！");

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	public String normalDownloadReq(int normalPort) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpRequest httpRequest = null;

		String localSipUri = "sip:" + mIdentity + "@" + mNetworkRealm; // 默认情况为：
																		// sip:10658811102@sunkaisens.com
		String xcapHeader = "X-XCAP-Asserted-Identity"; // ???????????

		String result = "";

		try {
			httpRequest = new HttpGet(
					"http://xcap.example.com/services/xcap-caps/users/global/index");

			httpRequest.addHeader(xcapHeader, localSipUri);

			HttpHost targethost = null;
			try {
				targethost = new HttpHost(mNetworkGroupRealm,
						Integer.valueOf(mNetworkGroupPort));
			} catch (NumberFormatException e) {

				MyLog.d(TAG, "normalDownloadReq  default port:" + normalPort);

				if (mNetworkGroupRealm == null) {
					targethost = new HttpHost(
							NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM,
							normalPort);

					MyLog.d(TAG, "normalDownloadReq  default port:"

					+ NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);
				} else {
					targethost = new HttpHost(mNetworkGroupRealm, normalPort);
				}
				e.printStackTrace();
			}

			HttpConnectionParams.setConnectionTimeout(httpRequest.getParams(),
					timeout * 1000);
			HttpConnectionParams.setSoTimeout(httpRequest.getParams(),
					timeout * 1000);
			HttpResponse httpResponse = httpClient.execute(targethost,
					httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) { // 200
																					// OK
				MyLog.d(TAG, "normalDownloadReq Success!");

				HttpEntity entity = httpResponse.getEntity();
				if (entity != null) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(entity.getContent()));
					String line = null;
					while ((line = br.readLine()) != null) {
						result += line;
					}

					MyLog.d(TAG, "normalDownloadReq  body:  " + result);
				} else {
					result = "failed";
					MyLog.d(TAG, "normalDownloadReq 响应异常:"
							+ httpResponse.getStatusLine().getStatusCode());
				}
			} else {
				result = "failed";
				MyLog.d(TAG, "normalDownloadReq 请求下载通讯录失败！");
			}

		} catch (Exception e) {
			result = "failed";
			e.printStackTrace();
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return result;
	}

	public class TrustAllManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
			MyLog.d(TAG, "TrustAllManager checkClientTrusted()|arg0:"
					+ arg0.length + "   arg1:" + arg1);
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
			MyLog.d(TAG, "TrustAllManager checkServerTrusted()|arg0:"
					+ arg0.length + "   arg1:" + arg1);
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			MyLog.d(TAG, "TrustAllManager getAcceptedIssuers()");
			return null;
		}
	}

	/**
	 * 解析全网个呼通讯录
	 * 
	 * @return
	 */
	public List<NodeResource> parserContactsTree() {
		MyLog.d(TAG, "parserContactsTree()");
		if (list != null) {
			list.clear();
		} else {
			list = new ArrayList<NodeResource>();
		}

		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();

			parser.setInput(new StringReader(XMLContact));
			String org = null;
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					// if (tagName.equals("resource-lists"))
					// ;
					if (tagName.equals("list")) {
						NodeResource temp = new NodeResource(
								parser.getAttributeValue(null, "listIdx"),
								parser.getAttributeValue(null, "superIdx"),
								parser.getAttributeValue(null, "name"), "", "",
								parser.getAttributeValue(null, "name"), true,
								R.drawable.group_icn, false, "group", "");
						temp.setBussinessType(ServiceContact.PUBLICGROUP_TYPE);
						stk.push(temp);
						org = temp.getName();
					}
					if (tagName.equals("entry")) {
						String uri = parser.getAttributeValue(null, "uri");
						String number = NgnUriUtils.getUserName(uri);
						String userType = parser.getAttributeValue(null,
								"userType");
						if (null != userType) {
							NodeResource temp = new NodeResource(number, org,
									"", parser.getAttributeValue(null, "uri"),
									"", "", false, R.drawable.user_offline,
									false, userType, ""); // add userType
							stk.push(temp);
						} else {
							NodeResource temp = new NodeResource(number, org,
									"", parser.getAttributeValue(null, "uri"),
									"", "", false, R.drawable.user_offline,
									false, "", "");
							stk.push(temp);
						}

					}

					if (tagName.equals("display-name")) {
						stk.peek().setDisplayName(parser.nextText()); //
						list.add(stk.pop());
					}

					if (tagName.equals("portraitUrl")) {
						String tempString = parser.nextText();

						if (tempString != null) {
							String[] tempStrings = tempString.split(";");
							if (tempStrings[0] != null) {
								MyLog.e(TAG, "有头像" + tempStrings[0]);
								list.get(list.size() - 1).setIcon(
										tempStrings[0]); //

							}

							if (tempStrings[2] != null) {

								MyLog.e(TAG, "有大头像" + tempStrings[0]);
								list.get(list.size() - 1).setBigIcon(
										tempStrings[2]); //

							}
						}

					}

				}
				parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return list;

	}

	/**
	 * 下载业务组
	 * 
	 * @return
	 */
	public boolean downloadServiceGroup() {

		MyLog.d(TAG, "downloadServiceGroup()");

		mIdentity = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_IMPI,
						NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

		mNetworkGroupRealm = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_GROUP_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);

		mNetworkRealm = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_REALM);

		mNetworkGroupPort = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_GROUP_PORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT);
		// if (isSubscribeServiceGroup == false) {
		// final NgnSubscriptionSession subscriptionSessionService =
		// NgnSubscriptionSession
		// .createOutgoingSession(Engine.getInstance().getSipService()
		// .getSipStack(), "sip:" + mIdentity + "@"
		// + mNetworkRealm, "sip:service@" + mNetworkRealm,
		// NgnSubscriptionSession.EventPackageType.Group);
		// subscriptionSessionService.subscribe();
		// isSubscribeServiceGroup = true;
		// }

		String uriSub = "/services/service-group/users/sip:" + mIdentity + "@"
				+ mNetworkRealm + "/index";

		String pwd = (String) Main.mMessageReportHashMap
				.get(mIdentity + "_pwd");

		// while (true) {
		for (int r = 0; r < 5; r++) {
			try {
				if (mNetworkGroupPort.equals("8955")) {
					MyLog.d(TAG, "downloadServiceGroup SSL方式下载，端口号:"
							+ mNetworkGroupPort);

					XMLContactNet = initSSLALL(mNetworkGroupRealm,
							Integer.parseInt(mNetworkGroupPort), uriSub,
							mNetworkRealm, mIdentity, pwd);
				} else {
					MyLog.d(TAG, "downloadServiceGroup 普通方式下载，端口号:"
							+ mNetworkGroupPort);
					XMLContactNet = normalDowanload(uriSub);
					
				}

				break;

			} catch (NumberFormatException e) {
				e.printStackTrace();
				MyLog.d(TAG, "downloadServiceGroup 下载业务通讯录失败");
				XMLContactNet = "failed";
				SystemClock.sleep(3000);
				continue;
				// return false;
			} catch (Exception e) {
				e.printStackTrace();
				MyLog.d(TAG, "downloadServiceGroup 下载业务通讯录失败");
				XMLContactNet = "failed";
				SystemClock.sleep(3000);
				continue;
				// return false;
			}
		}
		// Log.e("ScreenDownloadContacts-downloadContactsNet()",
		// "业务组源文件"+XMLContactNet);
		if (!XMLContactNet.equals("failed")) {
			MyLog.d(TAG, "downloadServiceGroup 下载业务通讯录源文件:" + XMLContactNet);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * 解析业务组
	 * 
	 * @return
	 */
	public List<NodeResource> parserContactsNetTree() {
		MyLog.d(TAG, "parserContactsNetTree()");
		if (listNet != null) {
			listNet.clear();
		} else {
			listNet = new ArrayList<NodeResource>();
		}
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();

			parser.setInput(new StringReader(XMLContactNet));
			String groupIndexName = null;
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					// if (tagName.equals("service-group"));
					// String groupIndex,String uri,String name,String
					// displayName,String bussinessType

					if (tagName.equals("list")) {
						groupIndexName = parser.getAttributeValue(null, "name");
						NodeResource temp = new NodeResource(
								groupIndexName,// groupIndex
								parser.getAttributeValue(null, "uri"),// uri
								groupIndexName,// name
								parser.getAttributeValue(null, "displayName"),
								parser.getAttributeValue(null, "serviceType"),
								R.drawable.group_icn, true, null, "group", "");
						listNet.add(temp);
					}
					if (tagName.equals("entry")) {
						String userType = parser.getAttributeValue(null,
								"userType");
						String uri = parser.getAttributeValue(null, "uri");
						String nember = NgnUriUtils.getUserName(uri);

						String url = parser.getAttributeValue(null,
								"portraitUrl");
						String[] tempStrings = null;
						NodeResource temp = null;

						if (url != null) {
							tempStrings = url.split(";");
							if (tempStrings[0] != null) {
								MyLog.d(TAG, "parserContactsNetTree has icon; "+ tempStrings.length
										+ tempStrings[0]);
								temp = new NodeResource(
										groupIndexName,// groupIndex
										uri,// uri
										nember,// name
										parser.getAttributeValue(null,
												"displayName"),
										parser.getAttributeValue(null,
												"deviceType"),
										R.drawable.user_offline, false,
										userType, tempStrings[0], ""); // add

							} else {
								temp = new NodeResource(
										groupIndexName,// groupIndex
										uri,// uri
										nember,// name
										parser.getAttributeValue(null,
												"displayName"),
										parser.getAttributeValue(null,
												"deviceType"),
										R.drawable.user_offline, false,
										userType, "", ""); // add
							}

							if (tempStrings.length > 2 && tempStrings[2] != null) {

								MyLog.d(TAG, "parserContactsNetTree  has big icon."
										+ tempStrings[2]);

								temp.setBigIcon(tempStrings[2]);
							}

						} else {
							temp = new NodeResource(
									groupIndexName,// groupIndex
									uri,// uri
									nember,// name
									parser.getAttributeValue(null,
											"displayName"),
									parser.getAttributeValue(null, "deviceType"),
									R.drawable.user_offline, false, userType,
									"", ""); // add
						}

						// userType
						listNet.add(temp);
					}

				}
				parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Log.e("ScreenDownloadContacts-parserContactsNetTree()",
		// "业务组个数"+listNet.size());

		// for (int i = 0; i < listNet.size(); i++) {
		// Log.e("ScreenDownloadContacts-parserContactsNetTree()",
		// "业务组"+i+"name:"+listNet.get(i).getName());
		// Log.e("ScreenDownloadContacts-parserContactsNetTree()",
		// "业务组"+i+"type:"+listNet.get(i).getBussinessType());
		// }

		return listNet;

	}

	// add by jgc 下载公共台通讯录
	public boolean downloadGlobalGroup() {

		MyLog.d(TAG, "downloadGlobalGroup()");

		mIdentity = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_IMPI,
						NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

		mNetworkGroupRealm = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_GROUP_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);

		mNetworkRealm = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_REALM);

		mNetworkGroupPort = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_GROUP_PORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT);
		// if (isSubscribeServiceGroup == false) {
		// final NgnSubscriptionSession subscriptionSessionService =
		// NgnSubscriptionSession
		// .createOutgoingSession(Engine.getInstance().getSipService()
		// .getSipStack(), "sip:" + mIdentity + "@"
		// + mNetworkRealm, "sip:service@" + mNetworkRealm,
		// NgnSubscriptionSession.EventPackageType.Group);
		// subscriptionSessionService.subscribe();
		// isSubscribeServiceGroup = true;
		// }

		String uriSub = "/services/global-group/users/sip:" + mIdentity + "@"
				+ mNetworkRealm + "/index";

		String pwd = (String) Main.mMessageReportHashMap
				.get(mIdentity + "_pwd");

		// while (true) {
		for (int r = 0; r < 5; r++) {
			try {
				if (mNetworkGroupPort.equals("8955")) {
					MyLog.d(TAG, "downloadGlobalGroup SSL方式下载，端口号:"
							+ mNetworkGroupPort);

					XMLContactGlobalGroup = initSSLALL(mNetworkGroupRealm,
							Integer.parseInt(mNetworkGroupPort), uriSub,
							mNetworkRealm, mIdentity, pwd);
				} else {
					MyLog.d(TAG, "downloadGlobalGroup 普通方式下载，端口号:"
							+ mNetworkGroupPort);
					XMLContactGlobalGroup = normalDowanload(uriSub);
					
				}
				break;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				MyLog.d(TAG, "downloadGlobalGroup 下载公共台通讯录失败");

				XMLContactGlobalGroup = "failed";
				SystemClock.sleep(3000);
				continue;
				// return false;
			} catch (Exception e) {
				e.printStackTrace();
				MyLog.d(TAG, "downloadGlobalGroup 下载公共台通讯录失败");

				XMLContactGlobalGroup = "failed";
				SystemClock.sleep(3000);
				continue;
				// return false;
			}
		}
		if (!XMLContactGlobalGroup.equals("failed")) {
			 MyLog.d(TAG, "downloadGlobalGroup 下载公共台通讯录源文件:" + XMLContactGlobalGroup);
			return true;
		} else {
			return false;
		}

	}

	// add by jgc 下载组呼通讯录
	public boolean downloadCommGroup() {

		MyLog.d(TAG, "downloadCommGroup()");

		mIdentity = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_IMPI,
						NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

		mNetworkGroupRealm = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_GROUP_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);

		mNetworkRealm = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_REALM);

		mNetworkGroupPort = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_GROUP_PORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT);
		// if (isSubscribeServiceGroup == false) {
		// final NgnSubscriptionSession subscriptionSessionService =
		// NgnSubscriptionSession
		// .createOutgoingSession(Engine.getInstance().getSipService()
		// .getSipStack(), "sip:" + mIdentity + "@"
		// + mNetworkRealm, "sip:service@" + mNetworkRealm,
		// NgnSubscriptionSession.EventPackageType.Group);
		// subscriptionSessionService.subscribe();
		// isSubscribeServiceGroup = true;
		// }

		String uriSub = "/services/comm-group/users/sip:" + mIdentity + "@"
				+ mNetworkRealm + "/index";

		String pwd = (String) Main.mMessageReportHashMap
				.get(mIdentity + "_pwd");

		// while (true) {
		for (int r = 0; r < 5; r++) {
			try {
				if (mNetworkGroupPort.equals("8955")) {
					MyLog.d(TAG, "downloadCommGroup SSL方式下载，端口号:"
							+ mNetworkGroupPort);

					XMLContactCommGroup = initSSLALL(mNetworkGroupRealm,
							Integer.parseInt(mNetworkGroupPort), uriSub,
							mNetworkRealm, mIdentity, pwd);
				} else {
					MyLog.d(TAG, "downloadCommGroup 普通方式下载，端口号:"
							+ mNetworkGroupPort);
					XMLContactCommGroup = normalDowanload(uriSub);
					
				}
				break;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				MyLog.d(TAG, "downloadCommGroup 下载组呼通讯录失败");

				XMLContactCommGroup = "failed";
				SystemClock.sleep(3000);
				continue;
				// return false;
			} catch (Exception e) {
				e.printStackTrace();

				MyLog.e(TAG, "downloadCommGroup 下载组呼通讯录失败");

				XMLContactCommGroup = "failed";
				SystemClock.sleep(3000);
				continue;
				// return false;
			}
		}
		if (!XMLContactCommGroup.equals("failed")) {
			MyLog.d(TAG, "downloadCommGroup 下载组呼通讯录源文件:" + XMLContactCommGroup);
			return true;
		} else {
			return false;
		}

	}

	public List<NodeResource> parserContactsCommGroupTree() {

		MyLog.e(TAG, "parserContactsCommGroupTree()");

		if (listCommGroup != null) {
			listCommGroup.clear();
		} else {
			listCommGroup = new ArrayList<NodeResource>();
		}
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();

			parser.setInput(new StringReader(XMLContactCommGroup));
			String groupIndexName = null;
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					// if (tagName.equals("comm-group"))
					// ;// String groupIndex,String uri,String name,String
					// displayName,String bussinessType

					if (tagName.equals("list")) {
						groupIndexName = parser.getAttributeValue(null, "name");
						NodeResource temp = new NodeResource(
								groupIndexName,// groupIndex
								parser.getAttributeValue(null, "uri"),// uri
								groupIndexName,// name
								parser.getAttributeValue(null, "displayName"),
								parser.getAttributeValue(null, "serviceType"),
								R.drawable.group_icn, true, null, "group", "");
						listCommGroup.add(temp);
					}
					if (tagName.equals("entry")) {
						String userType = parser.getAttributeValue(null,
								"userType");
						NodeResource temp = new NodeResource(
								groupIndexName,// groupIndex
								parser.getAttributeValue(null, "uri"),// uri
								parser.getAttributeValue(null, "name"),// name
								parser.getAttributeValue(null, "displayName"),
								parser.getAttributeValue(null, "deviceType"),
								R.drawable.user_offline, false, userType, "",
								""); // add
										// userType
						listCommGroup.add(temp);
					}

				}
				parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listCommGroup;

	}

	public List<NodeResource> parserContactsGlobalGroupTree() {
		MyLog.e(TAG, "parserContactsGlobalGroupTree()");

		if (listGlobalGroup != null) {
			listGlobalGroup.clear();
		} else {
			listGlobalGroup = new ArrayList<NodeResource>();
		}
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();

			parser.setInput(new StringReader(XMLContactGlobalGroup));
			String groupIndexName = null;
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					// if (tagName.equals("global-group"))
					// ;// String groupIndex,String uri,String name,String
					// displayName,String bussinessType

					if (tagName.equals("list")) {
						groupIndexName = parser.getAttributeValue(null, "name");
						NodeResource temp = new NodeResource(
								groupIndexName,// groupIndex
								parser.getAttributeValue(null, "uri"),// uri
								groupIndexName,// name
								parser.getAttributeValue(null, "displayName"),
								"disp", R.drawable.group_icn, true, null,
								"group", "");
						listGlobalGroup.add(temp);
					}
					if (tagName.equals("entry")) {
						String userType = "1";
						String uri = parser.getAttributeValue(null, "uri");
						String nember = NgnUriUtils.getUserName(uri);

						String url = parser.getAttributeValue(null,
								"portraitUrl");
						String[] tempStrings = null;
						NodeResource temp = null;

						if (url != null) {
							tempStrings = url.split(";");
							if (tempStrings[0] != null) {

								temp = new NodeResource(
										groupIndexName,// groupIndex
										uri,// uri
										nember,// name
										parser.getAttributeValue(null,
												"displayName"),
										parser.getAttributeValue(null,
												"deviceType"),
										R.drawable.user_offline, false,
										userType, tempStrings[0], ""); // add

							} else {
								temp = new NodeResource(
										groupIndexName,// groupIndex
										uri,// uri
										nember,// name
										parser.getAttributeValue(null,
												"displayName"),
										parser.getAttributeValue(null,
												"deviceType"),
										R.drawable.user_offline, false,
										userType, "", ""); // add
							}

							if (tempStrings.length > 2 && tempStrings[2] != null) {
								temp.setBigIcon(tempStrings[2]);

							}

						} else {

							temp = new NodeResource(
									groupIndexName,// groupIndex
									uri,// uri
									nember,// name
									parser.getAttributeValue(null,
											"displayName"),
									parser.getAttributeValue(null, "deviceType"),
									R.drawable.user_offline, false, userType,
									"", ""); // add

						}

						// userType
						listGlobalGroup.add(temp);
					}

				}
				parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listGlobalGroup;

	}

	/**
	 * 记录通讯录能力查询失败时重新查询的次数
	 */
	private static int mContactQueryTimes = 0;

	// add by jgc 下载通讯录服务器请求查询
	public boolean ContactReq() {

		while (true) {
			mIdentity = Engine
					.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.IDENTITY_IMPI,
							NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI); // "自己"
																			// 默认为10658811102

			mNetworkGroupRealm = Engine
					.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.NETWORK_GROUP_REALM,
							NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM); // "192.168.4.10"?????
																				// 默认为192.168.1.192

			mNetworkRealm = Engine
					.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.NETWORK_REALM,
							NgnConfigurationEntry.DEFAULT_NETWORK_REALM); // 默认为"sunkaisens.com"

			mNetworkGroupPort = Engine
					.getInstance()
					.getConfigurationService()
					.getString(NgnConfigurationEntry.NETWORK_GROUP_PORT,
							NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT); // 默认为1000

			MyLog.d(TAG, "ContactReq GroupServer Realm = " + mNetworkGroupRealm);
			MyLog.d(TAG, "ContactReq Realm = " + mNetworkRealm);
			MyLog.d(TAG, "ContactReq Port = " + mNetworkGroupPort);

			String pwd = (String) Main.mMessageReportHashMap.get(mIdentity
					+ "_pwd");

			if (mNetworkGroupPort != null && !mNetworkGroupPort.equals("")
					&& mNetworkGroupRealm != null
					&& !mNetworkGroupRealm.equals("") && mNetworkRealm != null
					&& !mNetworkRealm.equals("")) {
			if (mNetworkGroupPort.equals("8955")) {
				ReqContact = initSSLALLReq(mNetworkGroupRealm,
						Integer.parseInt(mNetworkGroupPort), mNetworkRealm,
						mIdentity, pwd);
			} else {
				MyLog.d(TAG, "ContactReq normalDownloadReq:" + mNetworkGroupPort);
				ReqContact = normalDownloadReq(Integer.parseInt(mNetworkGroupPort));
				}
			} else {
				ReqContact = "failed";
			}
			// Log.e("通讯录能力源文件！！", ReqContact);

			if (!ReqContact.equals("failed")) {
				return true;
			} else {
				if (mContactQueryTimes >= 4) {
					return false;
				} else {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mContactQueryTimes++;
				}
			}
		}

	}

	// add by jgc 下载通讯录服务器请求查询权限解析
	public String parserContactReq() {
		MyLog.e(TAG, "parserContactReq()");

		if (ReqContact == null) {
			auidString = "failed";
			return auidString;
		}

		contactReqList.clear();

		// if (auidString != null) {
		auidString = "";
		// }

		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();

			parser.setInput(new StringReader(ReqContact));
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					if (tagName.equals("auid")) {

						// if(parser.nextText()!=null)
						String tempString = parser.nextText();
						if (tempString != null) {
							auidString = auidString + tempString + "#"; // 通讯录访问格式为XXXX#XXXXXXX#
							contactReqList.add(tempString);
						}

					}
				}
				parser.next();
			}
			MyLog.d(TAG, "parserContactReq  auid: "

			+ (auidString == null ? "NULL" : auidString));

		} catch (XmlPullParserException e) {
			e.printStackTrace();

			MyLog.d(TAG, "parserContactReq 解析服务器通讯录能力失败");
			auidString = "failed";
		} catch (IOException e) {
			e.printStackTrace();
			MyLog.e(TAG, "parserContactReq 解析服务器通讯录能力失败");

			auidString = "failed";
		}

		return auidString;

	};

	// add by jgc 下载订阅号通讯录
	public boolean downloadSubscribeGroup() {

		MyLog.d(TAG, "downloadSubscribeGroup()");

		mIdentity = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_IMPI,
						NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

		mNetworkGroupRealm = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_GROUP_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);

		mNetworkRealm = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_REALM);

		mNetworkGroupPort = Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.NETWORK_GROUP_PORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT);
		// if (isSubscribeServiceGroup == false) {
		// final NgnSubscriptionSession subscriptionSessionService =
		// NgnSubscriptionSession
		// .createOutgoingSession(Engine.getInstance().getSipService()
		// .getSipStack(), "sip:" + mIdentity + "@"
		// + mNetworkRealm, "sip:service@" + mNetworkRealm,
		// NgnSubscriptionSession.EventPackageType.Group);
		// subscriptionSessionService.subscribe();
		// isSubscribeServiceGroup = true;
		// }

		String uriSub = "/services/subscribe-group/users/sip:" + mIdentity
				+ "@" + mNetworkRealm + "/index";

		String pwd = (String) Main.mMessageReportHashMap
				.get(mIdentity + "_pwd");

		// while (true) {
		for (int r = 0; r < 5; r++) {
			try {
				if (mNetworkGroupPort.equals("8955")) {
					MyLog.d(TAG, "downloadSubscribeGroup SSL方式下载，端口号:"
							+ mNetworkGroupPort);

					XMLContactSubscribeGroup = initSSLALL(mNetworkGroupRealm,
							Integer.parseInt(mNetworkGroupPort), uriSub,
							mNetworkRealm, mIdentity, pwd);
				} else {
					MyLog.d(TAG, "downloadSubscribeGroup 普通方式下载，端口号:"
							+ mNetworkGroupPort);
					XMLContactSubscribeGroup = normalDowanload(uriSub);
					
				}
				
				break;
			} catch (NumberFormatException e) {
				e.printStackTrace();
				MyLog.d(TAG, "downloadSubscribeGroup 下载订阅号通讯录失败");

				XMLContactSubscribeGroup = "failed";
				SystemClock.sleep(3000);
				continue;
				// return false;
			} catch (Exception e) {
				e.printStackTrace();

				MyLog.d(TAG, "downloadSubscribeGroup 下载订阅号通讯录失败");

				XMLContactSubscribeGroup = "failed";
				SystemClock.sleep(3000);
				continue;
				// return false;
			}
		}
		if (!XMLContactSubscribeGroup.equals("failed")) {
			 MyLog.d(TAG, "downloadSubscribeGroup 下载订阅号通讯录源文件" + XMLContactSubscribeGroup);
			return true;
		} else {
			return false;
		}

	}

	public List<NodeResource> parserContactsSubscribeGroupTree() {

		MyLog.d(TAG, "parserContactsSubscribeGroupTree()");

		if (listSubscribeGroup != null) {
			listSubscribeGroup.clear();
		} else {
			listSubscribeGroup = new ArrayList<NodeResource>();
		}
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();

			parser.setInput(new StringReader(XMLContactSubscribeGroup));
			String groupIndexName = null;
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					// if (tagName.equals("subscribe-group"))
					// ;// String groupIndex,String uri,String name,String
					// displayName,String bussinessType

					if (tagName.equals("list")) {
						groupIndexName = parser.getAttributeValue(null, "name");
						NodeResource temp = new NodeResource(
								groupIndexName,// groupIndex
								parser.getAttributeValue(null, "uri"),// uri
								groupIndexName,// name
								parser.getAttributeValue(null, "displayName"),
								"subscribe", R.drawable.group_icn, true, null,
								"group", "");
						listSubscribeGroup.add(temp);
					}
					// if (tagName.equals("entry")) {
					// String userType = "1";
					// String uri = parser.getAttributeValue(null, "uri");
					// String nember = NgnUriUtils.getUserName(uri);
					//
					// NodeResource temp = new NodeResource(
					// groupIndexName,// groupIndex
					// uri,// uri
					// nember,// name
					// parser.getAttributeValue(null, "displayName"),
					// parser.getAttributeValue(null, "deviceType"),
					// R.drawable.user_offline, false, userType); // add
					// // userType
					// listGlobalGroup.add(temp);
					// }

				}
				parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return listSubscribeGroup;

	}

	/**
	 * HttpUrlConnection支持所有Https免验证，不建议使用
	 * 
	 * @throws KeyManagementException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public String initSSLALLReq(String serverIp, int port, String domain,
			String name, String pwd) {

		Log.d(TAG, "initSSLALLReq()");

		String method = "";
		String realm = "";
		String qop = "";
		String nonce = "";
		String opaque = "";
		String result = "";
		int responseCode;

		// while (true) {
		for (int r = 0; r < 5; r++) {
			try {
				Log.d(TAG, "QueryTimes = " + r);
				String uri = "https://" + serverIp + ":" + port
						+ "/services/xcap-caps/users/global/index";
				String uriSub = "/services/xcap-caps/users/global/index";

				MyLog.d(TAG, "initSSLALLReq initSSLALLReq uri:" + uri);

				String localSipUri = "sip:" + name + "@test.com";
				String xcapHeader = "X-XCAP-Asserted-Identity";

				URL url = new URL(uri);
				SSLContext context = SSLContext.getInstance("TLS");
				context.init(null,
						new TrustManager[] { new TrustAllManager() }, null);
				HttpsURLConnection.setDefaultSSLSocketFactory(context
						.getSocketFactory());
				HttpsURLConnection
						.setDefaultHostnameVerifier(new HostnameVerifier() {

							@Override
							public boolean verify(String arg0, SSLSession arg1) {
								return true;
							}
						});
				HttpsURLConnection connection = (HttpsURLConnection) url
						.openConnection();
				connection.setDoInput(true);
				connection.setDoOutput(false);
				connection.setRequestMethod("GET");
				connection.connect();

				responseCode = connection.getResponseCode();

				if (responseCode == 401) {
					String headerValue = connection
							.getHeaderField("WWW-Authenticate");

					String[] valus = headerValue.split(",");
					for (int i = 0; i < valus.length; i++) {
						if (valus[i].contains("realm")) {
							method = valus[i].substring(0, 6);
							realm = valus[i].substring(14,
									valus[i].length() - 1);
						} else if (valus[i].contains("nonce")) {
							nonce = valus[i]
									.substring(7, valus[i].length() - 1);
						} else if (valus[i].contains("opaque")) {
							opaque = valus[i].substring(8,
									valus[i].length() - 1);
						} else if (valus[i].contains("qop")) {
							qop = valus[i].substring(5, valus[i].length() - 1);
						}
					}
					connection.disconnect();
					connection = (HttpsURLConnection) url.openConnection();

					MyLog.d(TAG, "initSSLALLReq  realm:" + realm + " qop:"
							+ qop);

					MyLog.d(TAG, "initSSLALLReq  nonce:" + nonce + "   opaque:"

					+ opaque);

					String ha1 = encodeByMD5(name + ":" + realm + ":" + pwd);
					String ha2 = encodeByMD5("GET:" + uriSub);
					String cnonce = DigestScheme.createCnonce();
					String nc = "00000001";
					String response = encodeByMD5(ha1 + ":" + nonce + ":" + nc
							+ ":" + cnonce + ":" + qop + ":" + ha2);

					MyLog.d(TAG, "initSSLALLReq  ha1:" + ha1);
					MyLog.d(TAG, "initSSLALLReq  ha2:" + ha2);
					MyLog.d(TAG, "initSSLALLReq  response:" + response);

					String Authorization = method + " realm=" + realm + ",qop="
							+ qop + ",nonce=" + nonce + ",opaque=" + opaque
							+ ",username=\"" + name + "\",uri=\"" + uriSub
							+ "\",cnonce=\"" + cnonce + "\",nc=\"" + nc + "\","
							+ "response=\"" + response.toUpperCase() + "\"";

					connection.addRequestProperty("Authorization",
							Authorization);
					connection.addRequestProperty(xcapHeader, localSipUri);

					connection.setDoInput(true);
					connection.setDoOutput(false);
					connection.setRequestMethod("GET");
					connection.connect();

					responseCode = connection.getResponseCode();

					if (responseCode == 200) {
						InputStream in = null;
						BufferedReader reader = null;
						try {
							in = connection.getInputStream();
							reader = new BufferedReader(new InputStreamReader(
									in));
							String line = "";
							StringBuffer resultBuf = new StringBuffer();
							while ((line = reader.readLine()) != null) {
								resultBuf.append(line);
							}
							result = resultBuf.toString();
						} catch (Exception e) {
						} finally {
							if (in != null) {
								try {
									in.close();
								} catch (Exception e2) {
								}
							}
							if (reader != null) {
								try {
									reader.close();
								} catch (Exception e2) {
								}
							}
						}
					}
				}

				break;
				// Log.e("TTTT", "DownloadContacts Result:" + result);
				// return result;
			} catch (Exception e) {
				e.printStackTrace();
				// return result;
				MyLog.d(TAG, "initSSLALLReq 通讯录请求失败");

				result = "failed";
				SystemClock.sleep(3000);

				continue;
			}
		}

		return result;
	}

}
