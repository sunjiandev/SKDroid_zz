package com.sunkaisens.skdroid.Utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Properties;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.model.NgnHistoryEvent;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.impl.NgnNetworkService;
import org.doubango.ngn.sip.NgnAVSession;
import org.doubango.ngn.sip.NgnSubscriptionSession;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnObservableHashMap;
import org.doubango.ngn.utils.NgnTimer;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.R.id;
import android.R.integer;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.Toast;

import com.sks.net.socket.message.BCDTools;
import com.sks.net.socket.message.BaseSocketMessage;
import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.Main;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.SKDroid;
import com.sunkaisens.skdroid.Screens.ScreenDownloadConcacts;
import com.sunkaisens.skdroid.Screens.ScreenLoginAccount;
import com.sunkaisens.skdroid.Services.ServiceAdhoc;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Services.ServiceGPSReport;
import com.sunkaisens.skdroid.Services.ServiceLoginAccount;
import com.sunkaisens.skdroid.adapter.ImageLoader;
import com.sunkaisens.skdroid.component.NodeResource;
import com.sunkaisens.skdroid.model.ModelCall;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.model.ModelContactSubs;
import com.sunkaisens.skdroid.model.VERSION;
import com.sunkaisens.skdroid.util.GlobalSession;
import com.sunkaisens.skdroid.util.GlobalVar;

public class SystemVarTools {
	// private String DELETE_MEMBER_ITSELF = "terminated;reason=rejected";
	// private String DELETE_WHOLE_GROUP = "terminated;reason=noresource";

	private static String TAG = SystemVarTools.class.getCanonicalName();

	public static boolean DEBUG_MODE = false; // true/false
	public static String mIdentity = Engine
			.getInstance()
			.getConfigurationService()
			.getString(NgnConfigurationEntry.IDENTITY_IMPI,
					NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

	public static boolean SUBSCRIBEOK = false;

	public static int nativeServiceNum = 0;

	public static String mIdentityChk = null;// gzc 20141020
												// 濞ｅ洦绻嗛惁澶愭偨閵婏箑鐓旾dentity婵繐绲块垾锟�

	// CS闁糕晝鍠曢銏ゆ閻愭枻鎷锋担铏瑰彋濞ｅ洠锟芥慨娑㈡嚄閽樺纾婚柛蹇ユ嫹
	public static boolean useCdmaNetwork = false;

	// 闂傚偆鍣ｉ。浠嬪矗瀹ュ娲柛鏃傚枙閸忔ê顕ｉ敓浠嬪礂閿燂拷
	public static boolean useFeedback = false;

	// GIS闁革附婢樺ù姗�宕濋悢璇插幋鐎殿噯鎷烽柛蹇ユ嫹
	public static boolean useGisMap = false;

	public static String sdcardRootPath = "";

	public static String sdcardPath = "";

	public static String downloadPath = "";
	public static String downloadIconPath = "";

	public static String crashPath = "";

	public static boolean isNetChecking = false;

	public static boolean isDaemonStartMe = false;

	public static int ARR_COLUMN_LEN = 29;
	public static int ARR_COLUMN_ORG = 28;

	public static boolean isDownContactFinished = false; // 闂侇偅淇洪鍡氥亹閺囨氨鐟撻弶鐐跺Г閸ㄦ岸宕濋悢鍝ュ灱闊洦顨愮槐婵堟喆閿濆懎鏋�濠碘�冲�归悘澶愭焻濮樻剚鍞电憸鐗堟礀濠�顏堝触鎼粹�抽叡闁挎稑鏈婵嬪籍閸洩鎷峰鎰佸數鐟滅増娲戠粭鍛姜閼恒儱鐏囬柛鏃傚櫐缁辨繃娼诲☉妯侯唺闁哄锟界粭澶娾槈閸縿浜奸梻鍌ゅ櫍椤ｏ拷

	private static HashMap<String, Object> map = new HashMap<String, Object>();
	private static Integer[] mThumbIds = { R.drawable.n_image_icon1,
			R.drawable.n_image_icon1, R.drawable.n_image_icon1,
			R.drawable.n_image_icon1, R.drawable.n_image_icon1,
			R.drawable.n_image_icon1, R.drawable.n_image_icon1,
			R.drawable.n_image_icon1, R.drawable.n_image_icon1,
			R.drawable.n_image_icon1, R.drawable.n_image_icon1,
			R.drawable.n_image_icon1, R.drawable.n_image_icon1,
			R.drawable.n_image_icon1, R.drawable.org_icon1,
			R.drawable.org_icon2, };

	private static List<ModelContact> contactListRecent = new ArrayList<ModelContact>(); // 闁哄牞鎷烽弶鈺傚灱娴犲牏鍖栫拋铏溄
	private static List<ModelContact> contactListAll = new ArrayList<ModelContact>(); // 闁稿繈鍔戦崕鎾嚂閺冨倿鍏囧ù婊愭嫹
	private static List<ModelContact> contactListOrg = new ArrayList<ModelContact>(); // 缂備礁瀚划鎰版嚂閺冨倿鍏囧ù婊愭嫹
	private static List<ModelContact> contactListBusinessAll = new ArrayList<ModelContact>(); // 濞戞挻鑹炬慨鐔虹磼閸曨垰鍔ラ柛鎺戞濞堟垿骞嶉敓浠嬪嫉婢跺鐏囬柛娑欙公缁辨繃绋夊鍛樁闁瑰鍓涚划锟�
	private static List<ModelContact> contactListBusinessOrg = new ArrayList<ModelContact>(); // 濞戞挻鑹炬慨鐔兼嚂閺冨倿鍏囧ù婊愭嫹,闁告瑯浜濆Σ鍝ョ磼閿燂拷

	private static List<ModelContact> contactListAllBus = new ArrayList<ModelContact>(); // 濠㈠爢鍛煉缂佹棏鍨跺〒鍫曟偨閿燂拷
	private static List<ModelContact> contactListOrgBus = new ArrayList<ModelContact>(); // 濠㈠爢鍛煉缂佹棏鍨跺〒鍫曟偨閿燂拷

	private static List<ModelContact> contactListAllBus_containGlobalGroup = new ArrayList<ModelContact>(); // 濠㈠爢鍛煉缂佹棏鍨跺〒鍫曟偨閿燂拷
																											// 闁告牕鎳庨幆鍫㈡嫬閸愩劌顔婇柛娆欐嫹
	private static List<ModelContact> contactListOrgBus_containGlobalGroup = new ArrayList<ModelContact>(); // 濠㈠爢鍛煉缂佹棏鍨跺〒鍫曟偨閿燂拷
																											// 闁告牕鎳庨幆鍫㈡嫬閸愩劌顔婇柛娆欐嫹

	private static List<ModelContact> contactListAllOnLine = new ArrayList<ModelContact>();

	private static List<ModelContact> contactListCommGroupOrg = new ArrayList<ModelContact>(); // 缂備礁瀚幊鐘绘嚂閺冨倿鍏囧ù婊愭嫹,闁告瑯浜濆Σ鍝ョ磼閿燂拷
	private static List<ModelContact> contactListCommGroupAll = new ArrayList<ModelContact>(); // 缂備礁瀚幊鐘电磼閸曨垰鍔ラ柛鎺戞濞堟垿骞嶉敓浠嬪嫉婢跺鐏囬柛娑欙公缁辨繃绋夊鍛樁闁瑰鍓涚划锟�

	private static List<ModelContact> contactListGlobalGroupOrg = new ArrayList<ModelContact>(); // 闁稿浚鍓欓崣锟犲矗閹峰奔绮撶紒顖濐唺濮癸拷闁告瑯浜濆Σ鍝ョ磼閿燂拷
	private static List<ModelContact> contactListGlobalGroupAll = new ArrayList<ModelContact>(); // 闁稿浚鍓欓崣锟犲矗娴兼潙鍔ラ柛鎺戞濞堟垿骞嶉敓浠嬪嫉婢跺鐏囬柛娑欙公缁辨繃绋夊鍛樁闁瑰鍓涚划锟�

	private static List<ModelContact> contactListSubscribeGroupOrg = new ArrayList<ModelContact>(); // 閻犱降鍨藉Σ鍕矗閻ゎ垯绮撶紒顖濐唺濮癸拷闁告瑯浜濆Σ鍝ョ磼閿燂拷
	// private static List<ModelContact> contactListSubscribeGroupAll = new
	// ArrayList<ModelContact>(); //
	// 閻犱降鍨藉Σ鍕矗閻戣棄鍔ラ柛鎺戞濞堟垿骞嶉敓浠嬪嫉婢跺鐏囬柛娑欙公缁辨繃绋夊鍛樁闁瑰鍓涚划锟�

	private static int contactListBusinessAllNumber = 0;
	private static boolean bContactOK = false;

	private static String mNetworkRealm = Engine
			.getInstance()
			.getConfigurationService()
			.getString(NgnConfigurationEntry.NETWORK_REALM,
					NgnConfigurationEntry.DEFAULT_NETWORK_REALM);

	/**
	 * 闁哄秴娲╅鍥偨閵婏箑鐓曢柣褑顕х紞宥夋偐閼哥鎷烽敓锟介柣鈧妽閸╂盯寮伴姘剨鐎圭寮堕弫鐐哄礃閿燂拷闁绘鍩栭敓鎴掕兌缁
	 * 儤绋夐敓浠嬫偨閻╊晣tiveService闁硅矇鍐ㄧ厬
	 */
	public static boolean bLogin = false;

	public static boolean mStartGroupCalllRepoort = false;// 閻犲浂鍙冮悡鍫曞Υ娴ｇ瓔娼掑Λ鐗堝灟閿熸垝鑳跺ú鍐箳瑜忕划宥夊川閻撳海濡囬悹鍝勫暱缁辨垿宕楅敓锟�

	//
	// public static boolean bSocketService = false; //true/false
	// 闂侇偄鍊块崢銈嗘姜椤栨瑦顐藉☉鎾崇凹缁楃喖宕濋檱閽傚绂掗懜闈涘闁告瑱绲惧﹢鍥礉閳ュ磭纾婚柛蹇ユ嫹
	//
	public static boolean ScreenChat_Is_Top = false; // true/false
														// ScreenChat婵炴垵鐗婃导鍛玻濡わ拷缍撻柡鍕靛灠閹礁顔忛懠顒傜梾闁瑰灚鎸哥槐锟�

	private static SharedPreferences mSetCurrentGroup;
	private static SharedPreferences.Editor mSetCurrentGroupEditor;
	//
	public static boolean isLocalHangUp = false; // true/false
	public static boolean isLocalHangUp1 = false;				//此标记控制自组网回铃音，上面这个标记在响铃之前已经被反初始化，引发bug
	//
	public static boolean isLocalUnReg = false; // true/false
												// 闁汇垹褰夌粭鐔煎礉闄囬拏瀣鐠哄搫绲洪悹褔顥撳▓鎴︽偨閵婏箑鐓曟繛澶堝姂閺�锟�
	//
	public static boolean isLoginRefreshFail = false; // true/false
														// 婵炲鍔岄崬浠嬪礆闁垮鐓�濠㈡儼绮剧憴锕傚冀閸ヮ亞妲�

	public static boolean isSpeakerOn = false; // 闁猴拷绠栭悡璺何熼垾宕囩闁哄秴娲╅锟�

	public static NgnTimer mTimerVideoMonitorReport; // 濞ｅ洦绻傞悺銊ф喆閸℃侗鏆ラ柣鈺傚灦鐢墎锟藉顓燁槯闁革綇鎷�
	public static boolean mTakeVideoMonitorFlag = false; // 閻熸瑥妫濋。鍫曟儎閹寸偛浠橀煫鍥у暢閻戯箓宕ラ姘楅柡宥呮搐缁伙拷
	public static NgnTimer mTimerVideoPTTReport; // 濞ｅ洦绻傞悺銊ф喆閸℃侗鏆ョ紓浣稿閹崇娀骞掕閻ｉ箖寮捄鐑樼彜
	public static boolean mTakeVideoPTTFlag = false; // 閻熸瑥妫濋。鍓佺磼閸曨偅鍤戦煫鍥у暢閻戯箓宕ラ姘楅柡宥呮搐缁伙拷
	public static NgnTimer mTimerAudioPTTReport; // 濞ｅ洦绻傞悺銊ф嫚椤撱垻鍙剧紓浣稿閹崇姷锟藉顓燁槯闁革綇鎷�
	public static boolean mTakeAudioPTTFlag = false; // 閻犲浂鍙冮悡鍓佺磼閸曨偅鍤戦煫鍥у暢閻戯箓宕ラ姘楅柡宥呮搐缁伙拷

	public final static String encoding_utf8 = "UTF-8"; // utf-8

	public final static String encoding_gb2312 = "GB2312"; // GB2312

	public static int avCallNotNumber = 0;
	public static int avCallNotNumberLast = avCallNotNumber;

	public static boolean isSubscribeSended = true;

	public static String GroupServerDomaimPrefix = "appserver";
	public static String FileServerDomaimPrefix = "appserver";
	public static String MapServerDomaimPrefix = "appserver";
	public static String CscfDomaimPrefix = "pcscf";

	public static String MapServerDomaiSuffix = ":8080/GIS_lhr";
	public static String FileServerPort = "8010";
	public static String GroupServerPort = "8955";

	public static ImageLoader mImageLoader = null;

	/**
	 * 闁告帗绻傞～鎰板礌閺嶏妇鐟撻弶鐐舵閹风櫛rash闁哄倸娲ｅ▎銏ゆ儎椤旇偐绉�
	 * 
	 * @param dir
	 */
	public static void initFiles(String dir) {
		SystemVarTools.sdcardPath = dir;
		SystemVarTools.downloadPath = dir + "/download/";

		SystemVarTools.downloadIconPath = SystemVarTools.downloadPath
				+ "icons/";

		String downloadIconPathDir = SystemVarTools.downloadIconPath;
		File dirIcon = new File(downloadIconPathDir);
		if (!dirIcon.exists()) {
			dirIcon.mkdir();
		}

		SystemVarTools.crashPath = dir + "/crash/";
		MyLog.i("FILE", "sdcardPath=" + dir);
	}

	public static String getLocalParty() {
		return Engine
				.getInstance()
				.getConfigurationService()
				.getString(NgnConfigurationEntry.IDENTITY_IMPI,
						NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI)
				+ "_"
				+ Engine.getInstance()
						.getConfigurationService()
						.getString(
								NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
								NgnConfigurationEntry.DEFAULT_IDENTITY_DISPLAY_NAME);
	}

	public static void put(String key, Object o) {
		map.put(key, o);
	}

	public static Object get(String key) {
		return map.get(key);
	}

	public static Object getWithDefault(String key, Object dfvalue) {
		if (null == map.get(key))
			return dfvalue;
		else
			return map.get(key);
	}

	public static Integer getThumbID(int index) {
		if (index > 0 && index < mThumbIds.length)
			return mThumbIds[index];
		return mThumbIds[0];
	}

	public static Integer getImageIDFromNumber(String number) {
		if (bContactOK == false)
			return 0;
		for (int i = 0; i < contactListAll.size(); i++) {
			if (contactListAll.get(i).mobileNo.equalsIgnoreCase(number)) {
				return contactListAll.get(i).imageid;
			}
		}
		return 0;
	}

	public static int getContactOrgSize(String org) {
		int nSize = 0;
		for (int i = 0; i < contactListAll.size(); i++) {
			if (contactListAll.get(i).org.equalsIgnoreCase(org)) {
				nSize++;
			}
		}
		return nSize;
	}

	public static List<ModelContact> getContactAll() {
		// for(ModelContact model : contactListAll){
		// MyLog.d("", "ModelCLSSS闁靛棴鎷�"+model.mobileNo+","+model.name+"闁靛棴鎷�");
		// }
		return contactListAll;
	}

	//
	public static List<ModelContact> getContactOrg() {
		return contactListOrg;
	}

	public static List<ModelContact> getContactListBusinessAll() {
		return contactListBusinessAll;
	}

	public static List<ModelContact> getContactListBusinessOrg() {
		return contactListBusinessOrg;
	}

	// add by jgc
	public static List<ModelContact> getContactListCommGroupOrg() {
		return contactListCommGroupOrg;
	}

	public static List<ModelContact> getContactListGlobalGroupOrg() {
		return contactListGlobalGroupOrg;
	}

	public static List<ModelContact> getContactListSubscribeGroupOrg() {
		return contactListSubscribeGroupOrg;
	}

	public static List<ModelContact> getContactListGlobalGroupAll() {
		return contactListGlobalGroupAll;
	}

	public static List<ModelContact> getContactListCommGroupAll() {
		return contactListCommGroupAll;
	}

	//
	public static List<ModelContact> getContactRecent() {
		return contactListRecent;
	}

	public static List<ModelContact> getContactAllOnLine() {
		return contactListAllOnLine;
	}

	//
	public static String getIPFromUri(String uri) {
		if (uri == null)
			return "";
		String[] items = uri.split("\\@");
		if (items.length > 1)
			return items[1];
		else
			return "";
	}

	//
	public static String contactToString(List<ModelContact> inlist) {
		String value = "";
		for (int i = 0; i < inlist.size(); i++) {
			value += inlist.get(i).toString();
			value += "|";
		}
		return value;
	}

	public static boolean contactFromString(List<ModelContact> inlist,
			String fromvalue) {
		if (fromvalue == null)
			return false;
		String[] items = fromvalue.split("\\|");
		if (items == null)
			return false;
		inlist.clear();
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null && !items[i].isEmpty()) {
				inlist.add(new ModelContact(items[i]));
			}
		}
		return true;
	}

	public static boolean isContactOK() {
		return bContactOK;
	}

	public static void setContactOK(boolean flag) {
		bContactOK = flag;
	}

	public static void clear() {
		contactListAll.clear();
		contactListOrg.clear();
		contactListBusinessAll.clear();
		contactListBusinessOrg.clear();
		contactListGlobalGroupAll.clear();
		contactListGlobalGroupOrg.clear();
		contactListCommGroupAll.clear();
		contactListCommGroupOrg.clear();
		contactListSubscribeGroupOrg.clear();
		bContactOK = false;
		map.clear();
	}

	public static void initTestContactList() {
		bContactOK = true;
		for (int i = 0; i < 15; i++) {
			ModelContact model = new ModelContact();
			model.name = "闁哄牞鎷烽弶鈺傚灣濮瑰鎮ч敓锟�123434234023)" + (i + 1);
			model.imageid = (i + 5) % 14;
			model.mobileNo = "40000" + i;
			model.brief = "闁瑰瓨鍨跺Σ鎼佸嫉閿熻姤娼婚幋婊勭溄闁绘せ鏅濆▓鎴炵▔閿熶粙宕ㄩ敓";
			contactListRecent.add(model);
		}

		for (int i = 0; i < 40; i++) {
			ModelContact model = new ModelContact();
			model.name = "闁稿繈鍔戦崕瀛樼閾忕懓鈷�" + (i + 1);
			model.imageid = i % 14;
			model.mobileNo = "40000" + i;
			model.brief = "闁瑰瓨鍨跺Σ鎼佸礂閵娾晛鍔ュù婊嗘婢у潡鎯冮崟顏嗩伇闁告冻鎷�";
			model.isgroup = false;
			model.parent = "101";
			contactListAll.add(model);

		}
		for (int i = 0; i < 10; i++) {
			ModelContact model = new ModelContact();
			model.name = "闁哄本鍔楃划宥囩磼閿�" + (i + 1);
			model.imageid = 14 + i % 2;
			model.mobileNo = "800" + i;
			model.brief = "闁瑰瓨鍨跺Σ绋款潰閿濆牆鍘撮梺鎻掔箳缁秶绱掗敓";
			model.isgroup = true;
			model.index = "" + (100 + i);
			contactListOrg.add(model);
		}
	}

	public static boolean initContactListFromFile(Context context) {
		// 闁告帗绻傞～鎰板礌閺嶎叏鎷峰鎰佸數鐟滅増娲橀弳鐔煎箲閿燂拷闁哄牜鍓欏﹢锟�
		byte[] contactall = FileTools.readFileData(context, "contactall");
		if (contactall != null) {
			SystemVarTools.contactFromString(SystemVarTools.getContactAll(),
					new String(contactall));
			if (SystemVarTools.getContactAll().size() > 0) {
				Log.e("ScreenTabHome", "READ contact all ok!");
			}
		}
		byte[] contactorg = FileTools.readFileData(context, "contactorg");
		if (contactorg != null)
			SystemVarTools.contactFromString(SystemVarTools.getContactOrg(),
					new String(contactorg));
		byte[] contactrecent = FileTools.readFileData(context, "contactrecent");
		if (contactrecent != null)
			SystemVarTools.contactFromString(SystemVarTools.getContactRecent(),
					new String(contactrecent));
		if (contactall != null) {
			bContactOK = true;
			return true;
		} else
			return false;
	}

	public static int getContactListBusinessAllNumber() {
		return contactListBusinessAllNumber;
	}

	public static void incContactListBusinessAllNumber() {
		contactListBusinessAllNumber++;
	}

	public static void decContactListBusinessAllNumber() {
		contactListBusinessAllNumber--;
	}

	/**
	 * 閻犱礁澧介悿鍡樼▔濮橆剙顫ょ紓浣稿瀵绱掗崟顒�鐏囬柛娑虫嫹
	 * 
	 * @param resList
	 *            濞戞挻鑹炬慨鐔虹磼閸曨喖螡闁绘劗鎳撻崹顏嗘偘閿燂拷
	 */
	public static synchronized void setContactBussiness(
			List<NodeResource> resList) {
		// gzc 濞ｅ洦绻嗛惁澶愭偨閵婏箑鐓曢柛姘У椤掓粎娑甸敓锟�
		// Log.d("com.sunkaisens.skdroid", "setContactBussiness 闁烩偓鍔嶉崺娑㈠触閿燂拷" +
		// mIdentity
		// + "|闁烩偓鍔嶉崺娑㈠触閿燂拷chk):" + mIdentityChk);
		if (mIdentityChk != null && !mIdentity.equals(mIdentityChk)) {
			mIdentity = mIdentityChk;
		}

		if (resList == null)
			return;
		contactListBusinessAll.clear();
		contactListBusinessOrg.clear();
		for (int i = 0; i < resList.size(); i++) {
			NodeResource nr = resList.get(i);
			if (nr != null) {
				// /Log.e("SystemVarTools setContactBussiness",
				// "name"+nr.getName());
				// Log.e("SystemVarTools setContactBussiness",
				// "type"+nr.getUserType());
				// Log.e("SystemVarTools setContactBussiness",
				// "isgroup"+nr.getIsGroup());
				ModelContact mc = new ModelContact();
				if (nr.getDisplayName() != null) {
					mc.name = nr.getDisplayName();
				} else {
					mc.name = nr.getName();
				}

				mc.isgroup = nr.getIsGroup();
				mc.mobileNo = nr.getNumber();
				mc.title = nr.getUri();
				mc.org = nr.getSuperIndex();
				mc.businessType = nr.getBussinessType();

				mc.icon = nr.getIcon();
				mc.bigIcon = nr.getBigIcon();

				int currentBusinessNumber = getContactListBusinessAllNumber();
				if (mc.isgroup == false) {
					mc.imageid = currentBusinessNumber % 14;
					mc.userType = nr.getUserType();
				}

				else
					mc.imageid = 14 + currentBusinessNumber % 2;
				mc.uri = nr.getUri();

				mc.parent = nr.getSuperIndex();
				mc.index = nr.getName();
				/*
				 * if(mc.mobileNo == null ||mc.mobileNo.isEmpty()) mc.mobileNo =
				 * nr.getName(); if(mc.mobileNo == null ||
				 * mc.mobileNo.isEmpty()) mc.mobileNo =
				 * NgnUriUtils.getUserName(nr.getUri());
				 */
				if (nr.getIsGroup()) {
					addContactBusinessOrg(mc);
				} else {
					if (addContactBusinessAll(mc)) {
						// if(GlobalVar.bADHocMode == false){
						// //
						// 閻庣敻锟界粭鍛姜閽樺鐓傞柣銊ュ閿熻姤鐭穱濠呫亹閺囨俺鍘柟纰夋嫹闁哄牆顦伴崹姘跺川濮楋拷鍘撮柛娆愬敹ubscribe婵炴垵鐗婃导鍛存晬瀹�鍐閻炴稑鐭侀褰掓⒓閸涱偓鎷烽敓锟�
						// ServiceContact.addContacts(mc);
						// }
					}
				}
			}
		}

		// for (int i = 0; i < contactListBusinessOrg.size(); i++) {
		// Log.e("SystemVarTools setContactBussiness over ",
		// "contactListBusinessOrg: "+i+" name:"+contactListBusinessOrg.get(i).name);
		// }

		return;
	}

	// add by jgc
	public static void setContactCommGroupOrg(List<NodeResource> resList) {

		// Log.d("com.sunkaisens.skdroid", "setContactBussiness 闁烩偓鍔嶉崺娑㈠触閿燂拷" +
		// mIdentity
		// + "|闁烩偓鍔嶉崺娑㈠触閿燂拷chk):" + mIdentityChk);
		if (mIdentityChk != null && !mIdentity.equals(mIdentityChk)) {
			mIdentity = mIdentityChk;
		}

		if (resList == null)
			return;

		contactListCommGroupAll.clear();
		contactListCommGroupOrg.clear();

		for (int i = 0; i < resList.size(); i++) {
			NodeResource nr = resList.get(i);
			if (nr != null) {
				ModelContact mc = new ModelContact();
				mc.name = nr.getDisplayName();
				mc.isgroup = nr.getIsGroup();
				mc.mobileNo = nr.getNumber();
				mc.title = nr.getUri();
				mc.org = nr.getSuperIndex();
				mc.businessType = nr.getBussinessType();
				int currentBusinessNumber = getContactListBusinessAllNumber();
				if (mc.isgroup == false) {
					mc.imageid = currentBusinessNumber % 14;
					mc.userType = nr.getUserType();
				}

				else
					mc.imageid = 14 + currentBusinessNumber % 2;
				mc.uri = nr.getUri();

				mc.parent = nr.getSuperIndex();
				mc.index = nr.getName();
				/*
				 * if(mc.mobileNo == null ||mc.mobileNo.isEmpty()) mc.mobileNo =
				 * nr.getName(); if(mc.mobileNo == null ||
				 * mc.mobileNo.isEmpty()) mc.mobileNo =
				 * NgnUriUtils.getUserName(nr.getUri());
				 */
				if (nr.getIsGroup()) {
					addContactCommGroupOrg(mc);
				} else {
					if (addContactCommGroupAll(mc)) {
						// 閻庣敻锟界粭鍛姜閽樺鐓傞柣銊ュ閿熻姤鐭穱濠呫亹閺囨俺鍘柟纰夋嫹闁哄牆顦伴崹姘跺川濮楋拷鍘撮柛娆愬敹ubscribe婵炴垵鐗婃导鍛存晬瀹�鍐閻炴稑鐭侀褰掓⒓閸涱偓鎷烽敓锟�
						//
					}
				}
			}
		}
		return;
	}

	// add by jgc
	public static synchronized void setContactGlobalGroupOrg(
			List<NodeResource> resList) {

		// Log.d("com.sunkaisens.skdroid", "setContactBussiness 闁烩偓鍔嶉崺娑㈠触閿燂拷" +
		// mIdentity
		// + "|闁烩偓鍔嶉崺娑㈠触閿燂拷chk):" + mIdentityChk);
		if (mIdentityChk != null && !mIdentity.equals(mIdentityChk)) {
			mIdentity = mIdentityChk;
		}

		if (resList == null)
			return;

		contactListGlobalGroupAll.clear();
		contactListGlobalGroupOrg.clear();

		for (int i = 0; i < resList.size(); i++) {
			NodeResource nr = resList.get(i);
			if (nr != null) {
				ModelContact mc = new ModelContact();
				mc.name = nr.getDisplayName();
				mc.isgroup = nr.getIsGroup();
				mc.mobileNo = nr.getNumber();
				mc.title = nr.getUri();
				mc.org = nr.getSuperIndex();
				mc.businessType = nr.getBussinessType();
				int currentBusinessNumber = getContactListBusinessAllNumber();
				if (mc.isgroup == false) {
					mc.imageid = currentBusinessNumber % 14;
					mc.userType = nr.getUserType();
				}

				else
					mc.imageid = 14 + currentBusinessNumber % 2;
				mc.uri = nr.getUri();

				mc.parent = nr.getSuperIndex();
				mc.index = nr.getName();
				/*
				 * if(mc.mobileNo == null ||mc.mobileNo.isEmpty()) mc.mobileNo =
				 * nr.getName(); if(mc.mobileNo == null ||
				 * mc.mobileNo.isEmpty()) mc.mobileNo =
				 * NgnUriUtils.getUserName(nr.getUri());
				 */
				if (nr.getIsGroup()) {
					addContactGlobalGroupOrg(mc);
				} else {
					if (addContactGlobalGroupAll(mc)) {
						// 閻庣敻锟界粭鍛姜閽樺鐓傞柣銊ュ閿熻姤鐭穱濠呫亹閺囨俺鍘柟纰夋嫹闁哄牆顦伴崹姘跺川濮楋拷鍘撮柛娆愬敹ubscribe婵炴垵鐗婃导鍛存晬瀹�鍐閻炴稑鐭侀褰掓⒓閸涱偓鎷烽敓锟�
						// ServiceContact.addContacts(mc);
					}
				}
			}
		}
		return;
	}

	public static synchronized void setContactSubscribeGroupOrg(
			List<NodeResource> resList) {

		// Log.d("com.sunkaisens.skdroid", "setContactBussiness 闁烩偓鍔嶉崺娑㈠触閿燂拷" +
		// mIdentity
		// + "|闁烩偓鍔嶉崺娑㈠触閿燂拷chk):" + mIdentityChk);
		if (mIdentityChk != null && !mIdentity.equals(mIdentityChk)) {
			mIdentity = mIdentityChk;
		}

		if (resList == null)
			return;

		contactListSubscribeGroupOrg.clear();
		// contactListSubscribeGroupAll.clear();

		for (int i = 0; i < resList.size(); i++) {
			NodeResource nr = resList.get(i);
			if (nr != null) {
				ModelContact mc = new ModelContact();
				mc.name = nr.getDisplayName();
				mc.isgroup = nr.getIsGroup();
				mc.mobileNo = nr.getNumber();
				mc.title = nr.getUri();
				mc.org = nr.getSuperIndex();
				mc.businessType = nr.getBussinessType();
				int currentBusinessNumber = getContactListBusinessAllNumber();
				if (mc.isgroup == false) {
					mc.imageid = currentBusinessNumber % 14;
					mc.userType = nr.getUserType();
				}

				else
					mc.imageid = 14 + currentBusinessNumber % 2;
				mc.uri = nr.getUri();

				mc.parent = nr.getSuperIndex();
				mc.index = nr.getName();
				/*
				 * if(mc.mobileNo == null ||mc.mobileNo.isEmpty()) mc.mobileNo =
				 * nr.getName(); if(mc.mobileNo == null ||
				 * mc.mobileNo.isEmpty()) mc.mobileNo =
				 * NgnUriUtils.getUserName(nr.getUri());
				 */
				if (nr.getIsGroup()) {
					addContactSubscribeGroupOrg(mc);
				} else {
				}
			}
		}
		return;
	}

	public static String getmIdentity() {

		// Log.d("com.sunkaisens.skdroid", "getmIdentity 闁烩偓鍔嶉崺娑㈠触閿燂拷" +
		// mIdentity
		// + "|闁烩偓鍔嶉崺娑㈠触閿燂拷chk):" + mIdentityChk);
		if (mIdentityChk != null && !mIdentity.equals(mIdentityChk)) {
			mIdentity = mIdentityChk;
		}

		return mIdentity;
	}

	/**
	 * 濞戞挸顑堝ù鍥儍閸曨垽鎷峰鎰佸數鐟滃府鎷�
	 */
	public static synchronized void setContactAll(List<NodeResource> resList) {

		// gzc 濞ｅ洦绻嗛惁澶愭偨閵婏箑鐓曢柛姘У椤掓粎娑甸敓锟�
		Log.d("com.sunkaisens.skdroid", "setContactAll 闁烩偓鍔嶉崺娑㈠触閿燂拷"
				+ mIdentity + "|闁烩偓鍔嶉崺娑㈠触閿燂拷chk):" + mIdentityChk);
		if (mIdentityChk != null && !mIdentity.equals(mIdentityChk)) {
			mIdentity = mIdentityChk;
		}

		if (resList == null)
			return;
		boolean bSet = false;
		contactListAll.clear();
		contactListRecent.clear();
		contactListOrg.clear();
		for (int i = 0; i < resList.size(); i++) {
			NodeResource nr = resList.get(i);
			if (nr != null) {

				ModelContact mc = new ModelContact(nr.getIndex(),
						nr.getDisplayName(), nr.getNumber(), nr.getUri(),
						nr.getSuperIndex(), null, nr.getIsGroup(), "");

				mc.icon = nr.getIcon();
				mc.bigIcon = nr.getBigIcon();
				mc.org = nr.getSuperIndex();

				if (mc.isgroup == false) {
					mc.imageid = i % 14;
					mc.userType = nr.getUserType();
				} else
					mc.imageid = 14 + i % 2;
				mc.uri = nr.getUri();
				mc.index = nr.getIndex();
				mc.parent = nr.getSuperIndex();
				if (mc.mobileNo == null || mc.mobileNo.isEmpty())
					mc.mobileNo = nr.getName();
				if (mc.mobileNo == null || mc.mobileNo.isEmpty())
					mc.mobileNo = NgnUriUtils.getUserName(nr.getUri());

				MyLog.d("", "ModelsetConMC闁靛棴鎷�" + mc.mobileNo + "," + mc.name
						+ "闁靛棴鎷�");

				if (nr.getIsGroup()) {
					int j = 0;
					mc.businessType = nr.getBussinessType();
					for (j = 0; j < contactListOrg.size(); j++) {
						if (nr != null
								&& nr.getIndex() != null
								&& nr.getIndex().equalsIgnoreCase(
										contactListOrg.get(j).index))
							break;
					}
					if (j >= contactListOrg.size())
						contactListOrg.add(mc);

				} else {
					int j = 0;
					for (j = 0; j < contactListAll.size(); j++) {
						if (nr != null
								&& nr.getIndex() != null
								&& nr.getIndex().equalsIgnoreCase(
										contactListAll.get(j).index))
							break;
					}
					if (j >= contactListAll.size()) {
						contactListAll.add(mc);
						// // 閻犱降鍨藉Σ锟�
						if (GlobalVar.bADHocMode == false) {
							ServiceContact.addContacts(mc);
						}
					}

				}

			}
		}
		updateContactRecent();
		if (resList.size() > 0) {
			bContactOK = true;
		}
	}

	/**
	 * type :0 闁瑰瓨鍔曢幉锟絫ype :1缂備礁瀚划锟�
	 */
	public static List<ModelContact> getOrgChildContact(ModelContact org,
			int type) {

		if (org == null) {
			MyLog.d(TAG, "org is null.");
			return null;
		}
		if (org.mobileNo == null || org.mobileNo.isEmpty()) {
			MyLog.d(TAG, "org.index=" + org.index);
		}

		List<ModelContact> orgallitems = new ArrayList<ModelContact>();

		if (type == 0) {
			for (int i = 0; i < contactListAll.size(); i++) {
				// if(contactListAll.get(i).parent!=null &&
				// contactListAll.get(i).parent.equalsIgnoreCase(org.index))
				if (contactListAll.get(i).parent != null
						&& contactListAll.get(i).parent
								.equalsIgnoreCase(org.mobileNo)) {
					if (contactListAll.get(i).icon != null)
						MyLog.e("", "SystemVarTools  contactListAll "
								+ contactListAll.get(i).icon);

					orgallitems.add(contactListAll.get(i));
				}
			}
			for (int i = 0; i < contactListBusinessAll.size(); i++) {
				if (contactListBusinessAll.get(i).parent != null
						&& contactListBusinessAll.get(i).parent
								.equalsIgnoreCase(org.mobileNo)
						&& contactListBusinessAll.get(i).index != org.mobileNo) {

					if (contactListBusinessAll.get(i).icon != null)
						MyLog.e("", "SystemVarTools  contactListBusinessAll "
								+ contactListBusinessAll.get(i).icon);

					orgallitems.add(contactListBusinessAll.get(i));

				}
			}

			for (int i = 0; i < contactListCommGroupAll.size(); i++) {
				if (contactListCommGroupAll.get(i).parent != null
						&& contactListCommGroupAll.get(i).parent
								.equalsIgnoreCase(org.mobileNo)
						&& contactListCommGroupAll.get(i).index != org.mobileNo) {

					if (contactListCommGroupAll.get(i).icon != null)
						MyLog.e("", "SystemVarTools  contactListCommGroupAll "
								+ contactListCommGroupAll.get(i).icon);

					orgallitems.add(contactListCommGroupAll.get(i));

				}
			}

			for (int i = 0; i < contactListGlobalGroupAll.size(); i++) {
				if (contactListGlobalGroupAll.get(i).parent != null
						&& contactListGlobalGroupAll.get(i).parent
								.equalsIgnoreCase(org.mobileNo)
						&& contactListGlobalGroupAll.get(i).index != org.mobileNo) {

					if (contactListGlobalGroupAll.get(i).icon != null)
						MyLog.e("",
								"SystemVarTools  contactListGlobalGroupAll "
										+ contactListGlobalGroupAll.get(i).icon);

					orgallitems.add(contactListGlobalGroupAll.get(i));

				}
			}

		} else if (type == 1) {
			for (int i = 0; i < contactListOrg.size(); i++) {
				if (contactListOrg.get(i).parent != null
						&& contactListOrg.get(i).parent
								.equalsIgnoreCase(org.mobileNo)) {
					orgallitems.add(contactListOrg.get(i));
					List<ModelContact> itemarray = getOrgChildContact(
							contactListOrg.get(i), type);
					if (itemarray != null)
						orgallitems.addAll(itemarray);
				}
			}
			for (int i = 0; i < contactListBusinessOrg.size(); i++) {
				if (contactListBusinessOrg.get(i).parent != null
						&& contactListBusinessOrg.get(i).parent
								.equalsIgnoreCase(org.mobileNo)
						&& contactListBusinessOrg.get(i).index != org.mobileNo) {
					List<ModelContact> itemarray = getOrgChildContact(
							contactListBusinessOrg.get(i), type);
					if (itemarray != null)
						orgallitems.addAll(itemarray);
				}
			}

			for (int i = 0; i < contactListCommGroupOrg.size(); i++) {
				if (contactListCommGroupOrg.get(i).parent != null
						&& contactListCommGroupOrg.get(i).parent
								.equalsIgnoreCase(org.mobileNo)
						&& contactListCommGroupOrg.get(i).index != org.mobileNo) {
					List<ModelContact> itemarray = getOrgChildContact(
							contactListCommGroupOrg.get(i), type);
					if (itemarray != null)
						orgallitems.addAll(itemarray);
				}
			}

			for (int i = 0; i < contactListGlobalGroupOrg.size(); i++) {
				if (contactListGlobalGroupOrg.get(i).parent != null
						&& contactListGlobalGroupOrg.get(i).parent
								.equalsIgnoreCase(org.mobileNo)
						&& contactListGlobalGroupOrg.get(i).index != org.mobileNo) {
					List<ModelContact> itemarray = getOrgChildContact(
							contactListGlobalGroupOrg.get(i), type);
					if (itemarray != null)
						orgallitems.addAll(itemarray);
				}
			}

		}

		return orgallitems;
	}

	public static ModelContact createContactFromRemoteParty(String remoteParty) {
		String number = remoteParty;
		if (NgnUriUtils.isValidSipUri(remoteParty)) // # sip:#@test.com *
			number = NgnUriUtils.getUserName(remoteParty);
		if (number.startsWith(GlobalVar.videoMonitorPrefix))
			number = number.substring(3);

		for (int i = 0; i < contactListGlobalGroupOrg.size(); i++)//
		{
			if (contactListGlobalGroupOrg.get(i).mobileNo
					.equalsIgnoreCase(number)) {
				return contactListGlobalGroupOrg.get(i);
			}
		}
		for (int i = 0; i < contactListGlobalGroupAll.size(); i++)//
		{
			if (contactListGlobalGroupAll.get(i).mobileNo
					.equalsIgnoreCase(number)) {
				return contactListGlobalGroupAll.get(i);
			}
		}

		for (int i = 0; i < contactListAll.size(); i++) {
			if (contactListAll.get(i).mobileNo.equalsIgnoreCase(number)) {
				return contactListAll.get(i);
			}
		}
		for (int i = 0; i < contactListOrg.size(); i++) {
			if (contactListOrg.get(i).mobileNo.equalsIgnoreCase(number)) {
				return contactListOrg.get(i);
			}
		}
		for (int i = 0; i < contactListBusinessOrg.size(); i++)//
		{
			if (contactListBusinessOrg.get(i).mobileNo.equalsIgnoreCase(number)) {
				return contactListBusinessOrg.get(i);
			}
		}
		for (int i = 0; i < contactListBusinessAll.size(); i++)//
		{
			if (contactListBusinessAll.get(i).mobileNo.equalsIgnoreCase(number)) {
				return contactListBusinessAll.get(i);
			}
		}

		for (int i = 0; i < contactListCommGroupOrg.size(); i++)//
		{
			if (contactListCommGroupOrg.get(i).mobileNo
					.equalsIgnoreCase(number)) {
				return contactListCommGroupOrg.get(i);
			}
		}
		for (int i = 0; i < contactListCommGroupAll.size(); i++)//
		{
			if (contactListCommGroupAll.get(i).mobileNo
					.equalsIgnoreCase(number)) {
				return contactListCommGroupAll.get(i);
			}
		}

		for (int i = 0; i < contactListSubscribeGroupOrg.size(); i++)//
		{
			if (contactListSubscribeGroupOrg.get(i).mobileNo
					.equalsIgnoreCase(number)) {
				return contactListSubscribeGroupOrg.get(i);
			}
		}

		ModelContact mc = new ModelContact();
		mc.imageid = 0;
		mc.isgroup = false;
		mc.mobileNo = number;

		String sipRemoteParty = NgnUriUtils.makeValidSipUri(number);

		mc.name = NgnUriUtils.getDisplayName(sipRemoteParty);
		if (mc.name == null)
			mc.mobileNo = number;
		return mc;
	}

	public static ModelContact getContactFromIndex(String index) {
		for (int i = 0; i < contactListAll.size(); i++) {
			if (contactListAll.get(i) != null
					&& contactListAll.get(i).index != null
					&& contactListAll.get(i).index.equalsIgnoreCase(index)) {
				return contactListAll.get(i);
			}
		}
		for (int i = 0; i < contactListOrg.size(); i++) {
			if (contactListOrg.get(i) != null
					&& contactListOrg.get(i).index != null
					&& contactListOrg.get(i).index.equalsIgnoreCase(index)) {
				return contactListOrg.get(i);
			}
		}
		return null;
	}

	public static ModelContact createContactFromNumberorName(String numberorName) {
		String number = numberorName;
		if (number.startsWith(GlobalVar.videoMonitorPrefix))
			number = number.substring(3);
		for (int i = 0; i < contactListAll.size(); i++) {
			if (contactListAll.get(i).mobileNo.equalsIgnoreCase(number)
					|| contactListAll.get(i).name.equals(number)) {
				return contactListAll.get(i);
			}
		}
		ModelContact mc = new ModelContact();
		mc.imageid = 0;
		mc.isgroup = false;
		mc.mobileNo = number;
		return mc;
	}

	public static ModelContact getContactFromPhoneNumber(String phoneNumer) {
		for (int i = 0; i < contactListAll.size(); i++) {
			if (contactListAll.get(i) != null
					&& contactListAll.get(i).mobileNo != null
					&& contactListAll.get(i).mobileNo
							.equalsIgnoreCase(phoneNumer)) {
				return contactListAll.get(i);
			}
		}
		for (int i = 0; i < contactListOrg.size(); i++) {
			if (contactListOrg.get(i) != null
					&& contactListOrg.get(i).mobileNo != null
					&& contactListOrg.get(i).mobileNo
							.equalsIgnoreCase(phoneNumer)) {
				return contactListOrg.get(i);
			}
		}
		for (int i = 0; i < contactListBusinessOrg.size(); i++) {
			if (contactListBusinessOrg.get(i) != null
					&& contactListBusinessOrg.get(i).mobileNo != null
					&& contactListBusinessOrg.get(i).mobileNo
							.equalsIgnoreCase(phoneNumer)) {
				return contactListBusinessOrg.get(i);
			}
		}

		for (int i = 0; i < contactListCommGroupOrg.size(); i++) {
			if (contactListCommGroupOrg.get(i) != null
					&& contactListCommGroupOrg.get(i).mobileNo != null
					&& contactListCommGroupOrg.get(i).mobileNo
							.equalsIgnoreCase(phoneNumer)) {
				return contactListCommGroupOrg.get(i);
			}
		}

		ModelContact mc = new ModelContact();
		mc.mobileNo = phoneNumer;
		mc.name = phoneNumer;

		return mc;
	}

	public static String getDisplayNameFromPhoneNumber(String phoneNumber) {
		for (int i = 0; i < contactListAll.size(); i++) {
			if (contactListAll.get(i) != null
					&& contactListAll.get(i).mobileNo != null
					&& contactListAll.get(i).mobileNo
							.equalsIgnoreCase(phoneNumber)) {
				return contactListAll.get(i).name;
			}
		}
		for (int i = 0; i < contactListOrg.size(); i++) {
			if (contactListOrg.get(i) != null
					&& contactListOrg.get(i).mobileNo != null
					&& contactListOrg.get(i).mobileNo
							.equalsIgnoreCase(phoneNumber)) {
				return contactListOrg.get(i).name;
			}
		}
		return phoneNumber;
	}

	public static ModelContact createContactFromPhoneNumber(String phoneNumer) {
		for (int i = 0; i < contactListAll.size(); i++) {
			if (contactListAll.get(i) != null
					&& contactListAll.get(i).mobileNo != null
					&& contactListAll.get(i).mobileNo
							.equalsIgnoreCase(phoneNumer)) {
				return contactListAll.get(i);
			}
		}
		for (int i = 0; i < contactListOrg.size(); i++) {
			if (contactListOrg.get(i) != null
					&& contactListOrg.get(i).mobileNo != null
					&& contactListOrg.get(i).mobileNo
							.equalsIgnoreCase(phoneNumer)) {
				return contactListOrg.get(i);
			}
		}

		for (int i = 0; i < contactListBusinessAll.size(); i++) {
			if (contactListBusinessAll.get(i) != null
					&& contactListBusinessAll.get(i).mobileNo != null
					&& contactListBusinessAll.get(i).mobileNo
							.equalsIgnoreCase(phoneNumer)) {
				return contactListBusinessAll.get(i);
			}
		}

		for (int i = 0; i < contactListBusinessOrg.size(); i++) {
			if (contactListBusinessOrg.get(i) != null
					&& contactListBusinessOrg.get(i).mobileNo != null
					&& contactListBusinessOrg.get(i).mobileNo
							.equalsIgnoreCase(phoneNumer)) {
				return contactListBusinessOrg.get(i);
			}
		}

		for (int i = 0; i < contactListCommGroupOrg.size(); i++) {
			if (contactListCommGroupOrg.get(i) != null
					&& contactListCommGroupOrg.get(i).mobileNo != null
					&& contactListCommGroupOrg.get(i).mobileNo
							.equalsIgnoreCase(phoneNumer)) {
				return contactListCommGroupOrg.get(i);
			}
		}

		ModelContact mc = new ModelContact();
		mc.imageid = 0;
		mc.isgroup = false;
		mc.mobileNo = phoneNumer;
		mc.name = phoneNumer;
		mc.parent = "";
		return mc;
	}

	public static void updateContactRecent() {
		List<NgnHistoryEvent> events = Engine.getInstance().getHistoryService()
				.getEvents();
		//
		for (int i = 0; i < events.size(); i++) {
			if (events.get(i) == null)
				continue;
			if (events.get(i).getRemoteParty() == null
					|| events.get(i).getRemoteParty().isEmpty())
				continue;
			// ModelContact mc =
			// createContactFromPhoneNumber(NgnUriUtils.getUserName(events.get(i).getRemoteParty()));

			String phoneNum = NgnUriUtils.getUserName(events.get(i)
					.getRemoteParty());
			if (phoneNum == null)
				phoneNum = events.get(i).getRemoteParty();
			// Log.d("GLE---getRemoteParty()",events.get(i).getRemoteParty());
			ModelContact mc = createContactFromPhoneNumber(phoneNum);

			if (mc.mobileNo == null || mc.mobileNo.isEmpty()
					|| mc.mobileNo.equals("1000"))
				continue;
			int j = 0;
			for (j = 0; j < contactListRecent.size(); j++) {
				if (contactListRecent.get(j) != null
						&& contactListRecent.get(j).mobileNo != null
						&& contactListRecent.get(j).mobileNo
								.equalsIgnoreCase(mc.mobileNo)) {
					break;
				}
			}
			if (j >= contactListRecent.size()) {
				contactListRecent.add(mc);
			}
		}
	}

	public static void reSetContactList() {
		resetCurrentList(getContactAll());
		resetCurrentList(getContactRecent());
		resetCurrentList(getContactListBusinessAll());
	}

	private static void resetCurrentList(List<ModelContact> currentModelList) {

		ModelContact tempS = new ModelContact();
		ModelContact tempE = new ModelContact();

		int start = 0;
		int end = currentModelList.size() - 1;
		while (start < end) {
			int s = start;
			while (currentModelList.get(s).isOnline == true && s < end)
				++s;
			int e = end;
			while (currentModelList.get(e).isOnline == false && e > s)
				--e;
			if (s < e)// swap
			{
				tempS = currentModelList.get(s);
				tempE = currentModelList.get(e);
				currentModelList.remove(s);
				currentModelList.add(s, tempE);
				currentModelList.remove(e);
				currentModelList.add(e, tempS);
			}
			start = s + 1;
			end = e - 1;
		}
	}

	//
	public static ModelContact getContactFromRemoteParty(String remoteParty) {
		String number = remoteParty;
		if (NgnUriUtils.isValidSipUri(remoteParty))
			number = NgnUriUtils.getUserName(remoteParty);
		// 闂傚牏鍋熺划宥嗙閸濆嫭鍠呭ǎ鍥ｅ墲娴煎懘鎳㈠畡鏉跨悼闁稿繈鍔戦崕鎾焻濮樺磭绠杕ContactAll閻炴稏鍔忛獮蹇涘矗閿燂拷
		for (String key : ServiceContact.mContactAll.keySet()) {
			ModelContactSubs mc = ServiceContact.mContactAll.get(key);

			if (mc.contact.mobileNo.equalsIgnoreCase(number)) {
				return mc.contact;
			}
		}
		for (int i = 0; i < contactListOrg.size(); i++) {
			if (contactListOrg.get(i).mobileNo.equalsIgnoreCase(number)) {
				return contactListOrg.get(i);
			}
		}
		for (int i = 0; i < contactListBusinessOrg.size(); i++) {
			if (contactListBusinessOrg.get(i).mobileNo.equalsIgnoreCase(number)) {
				return contactListBusinessOrg.get(i);
			}
		}
		return null;
	}

	// 闁告帇鍊栭弻鍥及椤栨碍鍎婇柛锔哄姀缁绘鎮板畝鍐惧殧闂傚﹨娅曢崹銊╂嚀閸涢偊娼掑Λ鐗堝灴閿熻姤淇洪惁锟�
	public static boolean isAVCalling() {
		int count = NgnAVSession.getSize();
		Log.e("isAVCalling() ", "count = " + count);
		if (count >= 1) // for test
			// if(count >= 2)
			return true;
		else
			return false;
	}

	public static void hangUpCallAll() {
		Log.d(TAG, "hangUpCallAll()");
		NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
				.getSessions();
		if (mAVSessions == null) {
			Log.e(TAG, "mAVSessions is null.");
			return;
		}
		int count = mAVSessions.size();
		Log.e("hangUpCallAll() ", "count = " + count);
		// for (int i = 0; i < count; i++) {
		// mAVSessions.getAt(i).hangUpCall();
		// }
		for (int i = count - 1; i >= 0; i--) {
			NgnAVSession session = mAVSessions.getAt(i);
			if (session != null)
				session.hangUpCall();
		}
	}

	public static void releaseSessionAll() {
		NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
				.getSessions();
		int count = mAVSessions.size();
		Log.e("releaseSessionAll() ", "count = " + count);
		for (int i = count - 1; i >= 0; i--) {
			// mAVSessions.getAt(i).hangUpCall();
			NgnAVSession.releaseSession(mAVSessions.getAt(i));
		}
	}

	public static void showToast(String tips) {

		if (GlobalSession.bSocketService) {
			return;
		}

		Toast welcomeToast = Toast.makeText(NgnApplication.getContext(), tips,
				Toast.LENGTH_SHORT);
		welcomeToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		welcomeToast.show();
	}

	public static void showToast(String tips, boolean isLong) {

		// Socket婵☆垪锟界槐鈩冪▔鐎ｎ剦娲ｉ柣鈶╂櫜oast闁圭粯鍔楅妵锟�
		if (SKDroid.sks_version == VERSION.SOCKET) {
			return;
		}

		Toast welcomeToast = Toast.makeText(NgnApplication.getContext(), tips,
				isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
		welcomeToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		welcomeToast.show();
	}

	public static void showNotifyDialog(String notifyBody, Activity activity) {
		try {
			AlertDialog.Builder builder = new Builder(activity); // com.sunkaisens.skdroid.Main
			builder.setMessage(notifyBody);
			builder.setTitle("闁圭粯鍔楅妵");
			builder.setPositiveButton("缁绢収鍠涢",
					new android.content.DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * type :0 闁瑰瓨鍔曢幉锟絫ype :1缂備礁瀚划锟藉褏鍋為崸濠冪▔濮橆剙顫ょ紓浣稿閸炲锟介敓锟�
	 */
	public static byte[] getOrgContactAllBus(int type) {

		int count = 0;
		byte[] data = null;

		if (type == 0) {
			contactListAllBus = mergeBothList(contactListAll,
					contactListBusinessAll);
			count = contactListAllBus.size();
			if (count == 0) {
				return null;
			}
			data = new byte[1 + ARR_COLUMN_LEN * count];
			data[0] = (byte) count;
			for (int i = 0; i < count; i++) {
				ModelContact mc = contactListAllBus.get(i);
				data[1 + i * ARR_COLUMN_LEN] = (byte) i; // Byte.parseByte(mc.index)
				data[2 + i * ARR_COLUMN_LEN] = 0; // 0 闁跨喓鏄� 闁哄拋鍣ｉ敓鑺ユ皑閺併倝骞嬮敓锟�1
													// 闁跨喓鏄� 閻犲鍟�规娊宕ｉ敓锟�
				if (mc != null && mc.userType != null
						&& mc.userType.equals("3")) {
					data[2 + i * ARR_COLUMN_LEN] = 0;
				}
				if (mc != null && mc.userType != null
						&& mc.userType.equals("1")) {
					data[2 + i * ARR_COLUMN_LEN] = 1;
				}
				byte[] mobileNo = BCDTools.Str2BCD(mc.mobileNo, 10); // 19800005001
				System.arraycopy(mobileNo, 0, data, 3 + i * ARR_COLUMN_LEN, 10);
				String name = mc.name; // (19800005001)
				byte[] nameBytes = null;
				try {
					if (name != null && name.length() < 16) {
						name = name + getExtStr(16 - name.length());
					}
					nameBytes = name.getBytes(encoding_gb2312);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				System.arraycopy(nameBytes, 0, data, 13 + i * ARR_COLUMN_LEN,
						16);

				// online
				data[29 + i * ARR_COLUMN_LEN] = 0;
				if (mc.isOnline) {
					data[29 + i * ARR_COLUMN_LEN] = 1;
				}

			}
		} else if (type == 1) {
			boolean bSet = false;
			contactListOrgBus = mergeBothList(contactListOrg,
					contactListBusinessOrg);
			count = contactListOrgBus.size();
			if (count == 0) {
				return null;
			}
			data = new byte[1 + ARR_COLUMN_LEN * count];
			data[0] = (byte) count;
			for (int i = 0; i < count; i++) {
				ModelContact mc = contactListOrgBus.get(i);
				data[1 + i * ARR_COLUMN_LEN] = (byte) i; // Byte.parseByte(mc.index)
				data[2 + i * ARR_COLUMN_LEN] = 0; // 0 闁跨喓鏄� 闁哄拋鍣ｉ敓鑺ユ皑閸忋垻绱掗敓锟�1
													// 闁跨喓鏄� 濞戞挻鑹炬慨鐔虹礃閵堝洨鐭�
				byte[] mobileNo = BCDTools.Str2BCD(mc.mobileNo, 10);
				System.arraycopy(mobileNo, 0, data, 3 + i * ARR_COLUMN_LEN, 10);
				String name = mc.name; // 鐎殿噯鎷烽柛娆愬灩缁拷(110)
				byte[] nameBytes = null;
				try {
					if (name != null && name.length() < 16) {
						name = name + getExtStr(16 - name.length());
					}
					nameBytes = name.getBytes(encoding_gb2312);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				System.arraycopy(nameBytes, 0, data, 13 + i * ARR_COLUMN_LEN,
						16);
				// online
				data[29 + i * ARR_COLUMN_LEN] = 0;
				if (mc.isOnline) {
					data[29 + i * ARR_COLUMN_LEN] = 1;
				}
				// if (i == 0) { //闁稿繑婀归懙鎴犵箔椤戣法顏遍柡澶嗭拷鐠愮喕銇愰幘鍐差枀闁圭鎷烽柛锔哄妿缁秹宕ㄩ懖鈺冪煁
				// if (!"000".equals(mc.mobileNo) && !bSet) { // 闂傚牏鍋犻惃鐔告償閿斿墽鐭�
				// setCurrentGroup(mc.mobileNo);
				// bSet = true;
				// }
				// }
			}
		}

		return data;
	}

	/*
	 * type :0 闁瑰瓨鍔曢幉锟絫ype :1缂備礁瀚划锟藉褏鍋為崸濠冪▔濮橆剙顫ょ紓浣稿閸炲锟介敓锟介柛鏍ф噹閹牏鎷崘銊ヮ唺闁告瑱鎷�
	 */
	public static byte[] getOrgContactAllBus2(int type) {

		int count = 0;
		byte[] data = new byte[1];

		if (type == 0) {
			contactListAllBus_containGlobalGroup = mergeBothList(
					contactListAll, contactListBusinessAll);
			contactListAllBus_containGlobalGroup = mergeBothList(
					contactListAllBus_containGlobalGroup,
					contactListGlobalGroupAll);
			count = contactListAllBus_containGlobalGroup.size();

			 MyLog.e(TAG,
			 "getOrgContactAllBus2  contactListAllBus_containGlobalGroup 闁轰椒鍗抽崳锟�  "+count);

			if (count == 0) {
				data[0] = (byte) count;
				return data;
			}
			data = new byte[1 + ARR_COLUMN_LEN * count];
			data[0] = (byte) count;
			for (int i = 0; i < count; i++) {
				ModelContact mc = contactListAllBus_containGlobalGroup.get(i);
				MyLog.d("", "ModelContact : " + mc.toString());
				data[1 + i * ARR_COLUMN_LEN] = (byte) i; // Byte.parseByte(mc.index)
				data[2 + i * ARR_COLUMN_LEN] = 0; // 0 闁跨喓鏄� 闁哄拋鍣ｉ敓鑺ユ皑閺併倝骞嬮敓锟�1
													// 闁跨喓鏄� 閻犲鍟�规娊宕ｉ敓锟�
				if (mc != null && mc.userType != null
						&& mc.userType.equals("3")) {
					data[2 + i * ARR_COLUMN_LEN] = 0;
				}
				if (mc != null && mc.userType != null
						&& mc.userType.equals("1")) {
					data[2 + i * ARR_COLUMN_LEN] = 1;
				}
				byte[] mobileNo = BCDTools.Str2BCD(mc.mobileNo, 10); // 19800005001
				System.arraycopy(mobileNo, 0, data, 3 + i * ARR_COLUMN_LEN, 10);
				String name = mc.name; // (19800005001)
				byte[] nameBytes = null;
				try {
					if (name != null && name.length() < 16) {
						name = name + getExtStr(16 - name.length());
					}
					nameBytes = name.getBytes(encoding_gb2312);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				System.arraycopy(nameBytes, 0, data, 13 + i * ARR_COLUMN_LEN,
						16);
				// online
				data[29 + i * ARR_COLUMN_LEN] = 0;
				if (mc.isOnline) {
					data[29 + i * ARR_COLUMN_LEN] = 1;
				}

			}
		} else if (type == 1) {
			boolean bSet = false;
			contactListOrgBus_containGlobalGroup = mergeBothList(
					contactListOrg, contactListBusinessOrg);
			contactListOrgBus_containGlobalGroup = mergeBothList(
					contactListOrgBus_containGlobalGroup,
					contactListGlobalGroupOrg);
			count = contactListOrgBus_containGlobalGroup.size();
			if (count == 0) {
				data[0] = (byte) count;
				return data;
			}
			data = new byte[1 + ARR_COLUMN_ORG * count];
			data[0] = (byte) count;
			for (int i = 0; i < count; i++) {
				ModelContact mc = contactListOrgBus_containGlobalGroup.get(i);
				data[1 + i * ARR_COLUMN_ORG] = (byte) i; // Byte.parseByte(mc.index)
				data[2 + i * ARR_COLUMN_ORG] = 0; // 0 闁跨喓鏄� 闁哄拋鍣ｉ敓鑺ユ皑閸忋垻绱掗敓锟�1
													// 闁跨喓鏄� 濞戞挻鑹炬慨鐔虹礃閵堝洨鐭�
				byte[] mobileNo = BCDTools.Str2BCD(mc.mobileNo, 10);
				System.arraycopy(mobileNo, 0, data, 3 + i * ARR_COLUMN_ORG, 10);
				String name = mc.name; // 鐎殿噯鎷烽柛娆愬灩缁拷(110)
				byte[] nameBytes = null;
				try {
					if (name != null && name.length() < 16) {
						name = name + getExtStr(16 - name.length());
					}
					nameBytes = name.getBytes(encoding_gb2312);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				System.arraycopy(nameBytes, 0, data, 13 + i * ARR_COLUMN_ORG,
						16);

				// if (i == 0) { //闁稿繑婀归懙鎴犵箔椤戣法顏遍柡澶嗭拷鐠愮喕銇愰幘鍐差枀闁圭鎷烽柛锔哄妿缁秹宕ㄩ懖鈺冪煁
				// if (!"000".equals(mc.mobileNo) && !bSet) { // 闂傚牏鍋犻惃鐔告償閿斿墽鐭�
				// setCurrentGroup(mc.mobileNo);
				// bSet = true;
				// }
				// }
			}
		}

		return data;
	}

	/**
	 * 合并两个集合的联系人
	 * 
	 * @param lst1
	 * @param lst2
	 */
	private static List mergeBothList(List lst1, List lst2) {
		List tmp = new ArrayList();
		if (lst1 == null) {
			if (lst2 == null) {
				return tmp;
			}
			tmp.addAll(lst2);
			return tmp;
		} else {
			tmp.addAll(lst1);
		}
		if (lst2 != null && lst2.size() > 0) {
			for (int i = 0; i < lst2.size(); i++) {
				if (!tmp.contains(lst2.get(i))) {
					tmp.add(lst2.get(i));
				}
			}
		}

		return tmp;
	}

	private static String getExtStr(int len) {
		String str = "";
		for (int i = 0; i < len; i++) {
			str = str + "\0";
		}
		return str;
	}

	/**
	 * 鐎电増顨呴崺宀勬儍閸曨剚笑鐟滅増鎸告晶鐕糲tivity闁汇劌瀚�垫﹢宕濋悩浣冾潶闁告熬鎷�
	 * 
	 * @return
	 */
	public static String getCurrentActivity() { // com.sunkaisens.skdroid.Main
		ActivityManager manager = (ActivityManager) NgnApplication.getContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTasks = manager.getRunningTasks(10);
		RunningTaskInfo cinfo = runningTasks.get(0);
		ComponentName component = cinfo.topActivity;
		Log.e("currentactivity", component.getClassName());
		return component.getClassName();
	}

	/**
	 * Socket闁哄倻鎳撶槐锟犳晬瀹�鍐ㄧ闁告瑦婀漡nAVSession
	 * 
	 * @return
	 */
	public static NgnAVSession getCurrentSession(int mSessionType) {
		NgnAVSession avSession = null;

		NgnObservableHashMap<Long, NgnAVSession> mAVSessions = NgnAVSession
				.getSessions();
		for (int i = 0; i < mAVSessions.size(); i++) {
			if (mAVSessions.getAt(i) != null) {
				avSession = mAVSessions.getAt(i);
				if (avSession != null
						&& mSessionType == avSession.getSessionType()) {
					break;
				}
			}
		}

		return avSession;
	}

	/**
	 * 閻犱礁澧介悿鍡氥亹閹惧啿顤呴梻鍡楁閸忋垻绱掗敓锟�
	 */
	public static void setCurrentGroup(String groupNo) {
		mSetCurrentGroup = NgnApplication.getContext().getSharedPreferences(
				BaseSocketMessage.SHARED_PREF_GROUP, Activity.MODE_PRIVATE);
		mSetCurrentGroupEditor = mSetCurrentGroup.edit();
		mSetCurrentGroupEditor.putString(
				BaseSocketMessage.DEFAULT_CURRENT_GROUP, groupNo);
		mSetCurrentGroupEditor.commit();
	}

	/**
	 * 闁兼儳鍢茶ぐ鍥亹閹惧啿顤呴梻鍡楁閸忋垻绱掗敓锟�
	 * */
	public static void getCurrentGroup() {

	}

	// public static String getGroupIndexFromPhoneNumber(String groupNumber)
	// {
	// int size = SystemVarTools.getContactOrg().size();
	// for(int i=0;i<size;i++)
	// {
	// if(getContactOrg().get(i).mobileNo.equals(groupNumber))//||contactListOrg.get(i).org.equals(orgNums[j]))
	// {
	// return getContactOrg().get(i).index;
	// }
	// }
	// return groupNumber;
	// }
	public static String getGroupMobileNoFromIndex(String groupIndex) {
		for (int i = 0; i < SystemVarTools.getContactOrg().size(); ++i) {
			if (SystemVarTools.getContactOrg().get(i).index.equals(groupIndex))// ||contactListOrg.get(i).org.equals(orgNums[j]))
			{
				return SystemVarTools.getContactOrg().get(i).mobileNo;
			}
		}
		return null;
	}

	public static boolean addContactAll(ModelContact mc) {

		Log.d("com.sunkaisens.skdroid", "addContactAll 闁烩偓鍔嶉崺娑㈠触閿燂拷"
				+ mIdentity + "|闁烩偓鍔嶉崺娑㈠触閿燂拷chk):" + mIdentityChk);
		if (mIdentityChk != null && !mIdentity.equals(mIdentityChk)) {
			mIdentity = mIdentityChk;
		}

		int number = contactListAll.size();
		for (int i = 0; i < number; i++) {
			if (contactListAll.get(i).mobileNo.equalsIgnoreCase(mc.mobileNo)) {
				return false;
			}
		}
		contactListAll.add(mc);
		if (GlobalVar.bADHocMode == false) {
			ServiceContact.addContacts(mc);
		}
		return true;
	}

	public static boolean addContactOrg(ModelContact mc) {
		int number = contactListOrg.size();
		for (int i = 0; i < number; i++) {
			if (contactListOrg.get(i).mobileNo.equalsIgnoreCase(mc.mobileNo)) {
				return false;
			}
		}
		contactListOrg.add(mc);
		return true;
	}

	public static boolean addContactBusinessAll(ModelContact mc) {

		if (mIdentityChk != null && !mIdentity.equals(mIdentityChk)) {
			mIdentity = mIdentityChk;
		}

		int number = contactListBusinessAll.size();
		int i = 0;
		for (i = 0; i < number; i++) {
			if (contactListBusinessAll.get(i).mobileNo
					.equalsIgnoreCase(mc.mobileNo)) {
				mc.imageid = contactListBusinessAll.get(i).imageid;
				if (contactListBusinessAll.get(i).parent
						.equalsIgnoreCase(mc.parent))
					break;
			}
		}
		if (i == number) {
			incContactListBusinessAllNumber();
			contactListBusinessAll.add(mc);
			if (GlobalVar.bADHocMode == false) {
				ServiceContact.addContacts(mc);
			}
			return true;
		}
		return false;
	}

	public static boolean addContactGlobalAll(ModelContact mc) {

		Log.d("com.sunkaisens.skdroid", "addContactGlobalAll 闁烩偓鍔嶉崺娑㈠触閿燂拷"
				+ mIdentity + "|闁烩偓鍔嶉崺娑㈠触閿燂拷chk):" + mIdentityChk);
		if (mIdentityChk != null && !mIdentity.equals(mIdentityChk)) {
			mIdentity = mIdentityChk;
		}

		int number = contactListGlobalGroupAll.size();
		int i = 0;
		for (i = 0; i < number; i++) {
			if (contactListGlobalGroupAll.get(i).mobileNo
					.equalsIgnoreCase(mc.mobileNo)) {
				mc.imageid = contactListGlobalGroupAll.get(i).imageid;
				if (contactListGlobalGroupAll.get(i).parent
						.equalsIgnoreCase(mc.parent))
					break;
			}
		}
		if (i == number) {
			incContactListBusinessAllNumber();
			contactListGlobalGroupAll.add(mc);
			if (GlobalVar.bADHocMode == false) {
				ServiceContact.addContacts(mc);
			}
			return true;
		}
		return false;
	}

	public static void addContactBusinessOrg(ModelContact mc) {
		int number = contactListBusinessOrg.size();
		// Log.e("SystemVarTools addContactBusinessOrg",
		// "闁告鍠嶇粭鐔煎礉閿涘嫮鐭嬮悘蹇撴惈椤曪拷+number);
		// Log.e("SystemVarTools addContactBusinessOrg",
		// "new ModelContact name"+mc.name);
		for (int i = 0; i < number; i++) {
			if (contactListBusinessOrg.get(i).mobileNo
					.equalsIgnoreCase(mc.mobileNo))
				return;
		}
		contactListBusinessOrg.add(mc);
	}

	private static void addContactCommGroupOrg(ModelContact mc) {
		// TODO Auto-generated method stub
		int number = contactListCommGroupOrg.size();
		for (int i = 0; i < number; i++) {
			if (contactListCommGroupOrg.get(i).mobileNo
					.equalsIgnoreCase(mc.mobileNo))
				return;
		}
		contactListCommGroupOrg.add(mc);
	}

	public static void addContactGlobalGroupOrg(ModelContact mc) {
		int number = contactListGlobalGroupOrg.size();
		for (int i = 0; i < number; i++) {
			if (contactListGlobalGroupOrg.get(i).mobileNo
					.equalsIgnoreCase(mc.mobileNo))
				return;
		}
		contactListGlobalGroupOrg.add(mc);
	}

	public static void addContactSubscribeGroupOrg(ModelContact mc) {
		int number = contactListSubscribeGroupOrg.size();
		for (int i = 0; i < number; i++) {
			if (contactListSubscribeGroupOrg.get(i).mobileNo
					.equalsIgnoreCase(mc.mobileNo))
				return;
		}
		contactListSubscribeGroupOrg.add(mc);
	}

	public static boolean subscrebeContacts(List<ModelContact> contacts) {

		try {
			if (contacts == null || contacts.size() == 0)
				return false;
			for (ModelContact mc : contacts) {
				ServiceContact.addContacts(mc);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static boolean subscribePublicGroup() {
		if (SKDroid.sks_version == VERSION.ADHOC)
			return true;
		try {
			Log.d("閻犱降鍨藉Σ锟�", "subscribePublicGroup()");
			final NgnSubscriptionSession subscriptionSessionPublic = NgnSubscriptionSession
					.createOutgoingSession(Engine.getInstance().getSipService()
							.getSipStack(), "sip:" + mIdentity + "@"
							+ mNetworkRealm, "sip:public-group@"
							+ mNetworkRealm,
							NgnSubscriptionSession.EventPackageType.Group);
			subscriptionSessionPublic.subscribe();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean subscribePersionInfo() {
		if (SKDroid.sks_version == VERSION.ADHOC)
			return true;
		try {
			Log.d(TAG, "闁告瑦鍨块敓鎴掑珐ms-pim閻犱降鍨藉Σ锟�");
			final NgnSubscriptionSession subscriptionSessionPublic = NgnSubscriptionSession
					.createOutgoingSession(Engine.getInstance().getSipService()
							.getSipStack(), "sip:" + mIdentity + "@"
							+ mNetworkRealm, "sip:ims-pim@" + mNetworkRealm,
							NgnSubscriptionSession.EventPackageType.Group);
			subscriptionSessionPublic.subscribe();

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean subscribeServiceGroup() {
		try {
			Log.d("閻犱降鍨藉Σ", "闁告瑦鍨块敓鎴掕ervice閻犱降鍨藉Σ");

			final NgnSubscriptionSession subscriptionSessionService = NgnSubscriptionSession
					.createOutgoingSession(Engine.getInstance().getSipService()
							.getSipStack(), "sip:" + mIdentity + "@"
							+ mNetworkRealm, "sip:service-group@"
							+ mNetworkRealm,
							NgnSubscriptionSession.EventPackageType.Group);
			subscriptionSessionService.subscribe();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean subscribeGlobalGroup() {
		try {
			Log.d("閻犱降鍨藉Σ", "闁告瑦鍨块敓鎴掔lobal閻犱降鍨藉Σ");

			final NgnSubscriptionSession subscriptionSessionService = NgnSubscriptionSession
					.createOutgoingSession(Engine.getInstance().getSipService()
							.getSipStack(), "sip:" + mIdentity + "@"
							+ mNetworkRealm, "sip:global-group@"
							+ mNetworkRealm,
							NgnSubscriptionSession.EventPackageType.Group);
			subscriptionSessionService.subscribe();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean subscribeSubscribeGroup() {
		try {
			Log.e("閻犱降鍨藉Σ", "闁告瑦鍨块敓鎴掕ubscribe閻犱降鍨藉Σ");

			final NgnSubscriptionSession subscriptionSessionService = NgnSubscriptionSession
					.createOutgoingSession(Engine.getInstance().getSipService()
							.getSipStack(), "sip:" + mIdentity + "@"
							+ mNetworkRealm, "sip:subscribe-group@"
							+ mNetworkRealm,
							NgnSubscriptionSession.EventPackageType.Group);
			subscriptionSessionService.subscribe();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static boolean addContactCommGroupAll(ModelContact mc) {
		// TODO Auto-generated method stub

		// Log.d("com.sunkaisens.skdroid", "addContactBusinessAll 闁烩偓鍔嶉崺娑㈠触閿燂拷"
		// + mIdentity + "|闁烩偓鍔嶉崺娑㈠触閿燂拷chk):" + mIdentityChk);
		if (mIdentityChk != null && !mIdentity.equals(mIdentityChk)) {
			mIdentity = mIdentityChk;
		}

		int number = contactListCommGroupAll.size();
		int i = 0;
		for (i = 0; i < number; i++) {
			if (contactListCommGroupAll.get(i).mobileNo
					.equalsIgnoreCase(mc.mobileNo)) {
				mc.imageid = contactListCommGroupAll.get(i).imageid;
				if (contactListCommGroupAll.get(i).parent
						.equalsIgnoreCase(mc.parent))
					break;
			}
		}
		if (i == number) {
			incContactListBusinessAllNumber();
			contactListCommGroupAll.add(mc);
			ServiceContact.addContacts(mc);
			return true;
		}
		return false;
	}

	private static boolean addContactGlobalGroupAll(ModelContact mc) {
		// TODO Auto-generated method stub

		// Log.d("com.sunkaisens.skdroid", "addContactBusinessAll 闁烩偓鍔嶉崺娑㈠触閿燂拷"
		// + mIdentity + "|闁烩偓鍔嶉崺娑㈠触閿燂拷chk):" + mIdentityChk);
		if (mIdentityChk != null && !mIdentity.equals(mIdentityChk)) {
			mIdentity = mIdentityChk;
		}

		int number = contactListGlobalGroupAll.size();
		int i = 0;
		for (i = 0; i < number; i++) {
			if (contactListGlobalGroupAll.get(i).mobileNo
					.equalsIgnoreCase(mc.mobileNo)) {
				mc.imageid = contactListGlobalGroupAll.get(i).imageid;
				if (contactListGlobalGroupAll.get(i).parent
						.equalsIgnoreCase(mc.parent))
					break;
			}
		}
		if (i == number) {
			incContactListBusinessAllNumber();
			contactListGlobalGroupAll.add(mc);
			ServiceContact.addContacts(mc);
			return true;
		}
		return false;
	}

	public static void sleep(long delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String mCallPeriodFormat(long period) {
		// Log.d("SystemVarToools", "mCallPeriodFormat period: "+period);
		period = period / 1000;
		StringBuilder builder = new StringBuilder();
		int hour = (int) (period / 3600);
		int min = (int) ((period / 60) % 60);
		int sed = (int) (period % 60);
		if (hour == 0) {
			builder.append("00");
		} else if (hour < 10) {
			builder.append("0");
			builder.append(hour);
		} else {
			builder.append(hour);
		}
		builder.append(":");
		if (min == 0) {
			builder.append("00");
		} else if (min < 10) {
			builder.append("0");
			builder.append(min);
		} else {
			builder.append(min);
		}
		builder.append(":");
		if (sed == 0) {
			builder.append("00");
		} else if (sed < 10) {
			builder.append("0");
			builder.append(sed);
		} else {
			builder.append(sed);
		}
		// Log.d("SystemVarToools", "mCallPeriodFormat builder: "+builder);
		return builder.toString();
	}

	public static boolean setDefaultSetting_socket() {
		Properties properties = MyProp.loadConfig();
		if (properties == null) {
			return false;
		}

		Log.d("SoocketService",
				"闁糕晝鍠庨幃锟�"
						+ properties.getProperty(
								NgnConfigurationEntry.NETWORK_REALM.substring(
										0, NgnConfigurationEntry.NETWORK_REALM
												.indexOf(".")),
								NgnConfigurationEntry.DEFAULT_NETWORK_REALM));
		Log.d("SoocketService",
				"缂傚洢鍊楃划宥夊嫉瀹ュ懎顫ら柛锝冨妼濠�鎾锤閿燂拷"
						+ properties.getProperty(
								NgnConfigurationEntry.NETWORK_GROUP_REALM
										.substring(
												0,
												NgnConfigurationEntry.NETWORK_GROUP_REALM
														.indexOf(".")),
								NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM));
		Log.d("SoocketService",
				"缂傚洢鍊楃划宥夊嫉瀹ュ懎顫ら柛锝冨妿椤忣剟宕ｉ敓锟�"
						+ properties.getProperty(
								NgnConfigurationEntry.NETWORK_GROUP_PORT
										.substring(
												0,
												NgnConfigurationEntry.NETWORK_GROUP_PORT
														.indexOf(".")),
								NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT));
		Log.d("SoocketService",
				"闁告ê妫楄ぐ璺衡槈閸喍绱栭柛锔芥緲濞硷拷"
						+ properties.getProperty(
								NgnConfigurationEntry.FILE_SERVER_HOST
										.substring(
												0,
												NgnConfigurationEntry.FILE_SERVER_HOST
														.indexOf(".")),
								String.valueOf(NgnConfigurationEntry.DEFAULT_FILE_SERVER_HOST)));
		Log.d("SoocketService",
				"闁告ê妫楄ぐ璺衡槈閸喍绱栫紒鏃戝灠瑜帮拷"
						+ properties.getProperty(
								NgnConfigurationEntry.FILE_SERVER_PORT
										.substring(
												0,
												NgnConfigurationEntry.FILE_SERVER_PORT
														.indexOf(".")),
								String.valueOf(NgnConfigurationEntry.DEFAULT_FILE_SERVER_PORT)));
		Log.d("SoocketService",

				"CSCF闁革附婢樺锟�"
						+ properties.getProperty(
								NgnConfigurationEntry.NETWORK_PCSCF_HOST
										.substring(
												0,
												NgnConfigurationEntry.NETWORK_PCSCF_HOST
														.indexOf(".")),
								NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST));
		Log.d("SoocketService",
				"CSCF缂佹棏鍨拌ぐ锟�"
						+ properties.getProperty(
								NgnConfigurationEntry.NETWORK_PCSCF_PORT
										.substring(
												0,
												NgnConfigurationEntry.NETWORK_PCSCF_PORT
														.indexOf(".")),
								String.valueOf(NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT)));

		INgnConfigurationService mConfigurationService = Engine.getInstance()
				.getConfigurationService();

		mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM,
				properties.getProperty(NgnConfigurationEntry.NETWORK_REALM
						.substring(0, NgnConfigurationEntry.NETWORK_REALM
								.indexOf(".")),
						NgnConfigurationEntry.DEFAULT_NETWORK_REALM));

		mConfigurationService.putString(
				NgnConfigurationEntry.NETWORK_GROUP_REALM,
				properties.getProperty(
						NgnConfigurationEntry.NETWORK_GROUP_REALM.substring(0,
								NgnConfigurationEntry.NETWORK_GROUP_REALM
										.indexOf(".")),
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM));

		mConfigurationService.putString(
				NgnConfigurationEntry.NETWORK_GROUP_PORT,
				properties.getProperty(NgnConfigurationEntry.NETWORK_GROUP_PORT
						.substring(0, NgnConfigurationEntry.NETWORK_GROUP_PORT
								.indexOf(".")),
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT));

		mConfigurationService
				.putBoolean(
						NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
						Boolean.getBoolean(properties.getProperty(
								NgnConfigurationEntry.NETWORK_USE_EARLY_IMS
										.substring(
												0,
												NgnConfigurationEntry.NETWORK_USE_EARLY_IMS
														.indexOf(".")),
								String.valueOf(NgnConfigurationEntry.DEFAULT_NETWORK_USE_EARLY_IMS))));

		mConfigurationService.putString(
				NgnConfigurationEntry.NETWORK_PCSCF_HOST,
				properties.getProperty(NgnConfigurationEntry.NETWORK_PCSCF_HOST
						.substring(0, NgnConfigurationEntry.NETWORK_PCSCF_HOST
								.indexOf(".")),
						NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST));
		mConfigurationService
				.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
						Integer.parseInt(properties.getProperty(
								NgnConfigurationEntry.NETWORK_PCSCF_PORT
										.substring(
												0,
												NgnConfigurationEntry.NETWORK_PCSCF_PORT
														.indexOf(".")),
								String.valueOf(NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT))));
		mConfigurationService.putString(
				NgnConfigurationEntry.NETWORK_TRANSPORT, properties
						.getProperty(NgnConfigurationEntry.NETWORK_TRANSPORT
								.substring(0,
										NgnConfigurationEntry.NETWORK_TRANSPORT
												.indexOf(".")),
								NgnConfigurationEntry.DEFAULT_NETWORK_TRANSPORT
										.toUpperCase()));
		mConfigurationService
				.putString(
						NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY,
						properties.getProperty(
								NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY
										.substring(
												0,
												NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY
														.indexOf(".")),
								NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_DISCOVERY));
		mConfigurationService
				.putBoolean(
						NgnConfigurationEntry.NETWORK_USE_SIGCOMP,
						Boolean.getBoolean(properties.getProperty(
								NgnConfigurationEntry.NETWORK_USE_SIGCOMP
										.substring(
												0,
												NgnConfigurationEntry.NETWORK_USE_SIGCOMP
														.indexOf(".")),
								String.valueOf(NgnConfigurationEntry.DEFAULT_NETWORK_USE_SIGCOMP))));
		mConfigurationService
				.putBoolean(
						NgnConfigurationEntry.NETWORK_USE_WIFI,
						Boolean.getBoolean(properties.getProperty(
								NgnConfigurationEntry.NETWORK_USE_WIFI
										.substring(
												0,
												NgnConfigurationEntry.NETWORK_USE_WIFI
														.indexOf(".")),
								String.valueOf(NgnConfigurationEntry.DEFAULT_NETWORK_USE_WIFI))));
		mConfigurationService
				.putBoolean(
						NgnConfigurationEntry.NETWORK_USE_3G,
						Boolean.getBoolean(properties.getProperty(
								NgnConfigurationEntry.NETWORK_USE_3G.substring(
										0, NgnConfigurationEntry.NETWORK_USE_3G
												.indexOf(".")),
								String.valueOf(NgnConfigurationEntry.DEFAULT_NETWORK_USE_3G))));
		mConfigurationService.putString(
				NgnConfigurationEntry.NETWORK_IP_VERSION,
				properties.getProperty(NgnConfigurationEntry.NETWORK_IP_VERSION
						.substring(0, NgnConfigurationEntry.NETWORK_IP_VERSION
								.indexOf(".")),
						NgnConfigurationEntry.DEFAULT_NETWORK_IP_VERSION));

		mConfigurationService.putString(NgnConfigurationEntry.FILE_SERVER_HOST,
				properties.getProperty(NgnConfigurationEntry.FILE_SERVER_HOST
						.substring(0, NgnConfigurationEntry.FILE_SERVER_HOST
								.indexOf(".")),
						NgnConfigurationEntry.DEFAULT_FILE_SERVER_HOST));
		mConfigurationService.putString(NgnConfigurationEntry.FILE_SERVER_PORT,
				properties.getProperty(NgnConfigurationEntry.FILE_SERVER_PORT
						.substring(0, NgnConfigurationEntry.FILE_SERVER_PORT
								.indexOf(".")),
						NgnConfigurationEntry.DEFAULT_FILE_SERVER_PORT));

		// Compute
		if (!mConfigurationService.commit()) {
			Log.e("SystemVarTools", "Failed to Commit() configuration");
		}

		return true;
	}

	public static List<ModelContact> getContactAllOnLineNoSelf(String mobileNo) {
		List<ModelContact> contactListAllOnLineNoSelf = contactListAllOnLine;
		int count = contactListAllOnLineNoSelf.size();
		// if (count > 0) {
		// for (int i = 0; i < count; i++) {
		// ModelContact mc = contactListAllOnLineNoSelf.get(i);
		// if (mc.mobileNo.equals(mobileNo)) {
		// contactListAllOnLineNoSelf.remove(i);
		// break;
		// }
		// }
		// }
		return contactListAllOnLineNoSelf;
	}

	public static void setDefaultSetting() {

		INgnConfigurationService mConfigurationService = Engine.getInstance()
				.getConfigurationService();

		mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM,
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_REALM));

		mConfigurationService.putString(
				NgnConfigurationEntry.NETWORK_GROUP_REALM,
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_GROUP_REALM,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM));

		mConfigurationService.putString(
				NgnConfigurationEntry.NETWORK_GROUP_PORT,
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_GROUP_PORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_PORT));

		mConfigurationService.putBoolean(
				NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
				mConfigurationService.getBoolean(
						NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
						NgnConfigurationEntry.DEFAULT_NETWORK_USE_EARLY_IMS));

		mConfigurationService.putString(
				NgnConfigurationEntry.NETWORK_PCSCF_HOST,
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_PCSCF_HOST,
						NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST));
		mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
				mConfigurationService.getInt(
						NgnConfigurationEntry.NETWORK_PCSCF_PORT,
						NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_PORT));
		mConfigurationService.putString(
				NgnConfigurationEntry.NETWORK_TRANSPORT, mConfigurationService
						.getString(NgnConfigurationEntry.NETWORK_TRANSPORT,
								NgnConfigurationEntry.DEFAULT_NETWORK_TRANSPORT
										.toUpperCase()));
		mConfigurationService.putString(
				NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY,
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_PCSCF_DISCOVERY,
						NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_DISCOVERY));
		mConfigurationService.putBoolean(
				NgnConfigurationEntry.NETWORK_USE_SIGCOMP,
				mConfigurationService.getBoolean(
						NgnConfigurationEntry.NETWORK_USE_SIGCOMP,
						NgnConfigurationEntry.DEFAULT_NETWORK_USE_SIGCOMP));
		mConfigurationService
				.putBoolean(NgnConfigurationEntry.NETWORK_USE_WIFI,
						mConfigurationService.getBoolean(
								NgnConfigurationEntry.NETWORK_USE_WIFI,
								NgnConfigurationEntry.DEFAULT_NETWORK_USE_WIFI));
		mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_3G,
				mConfigurationService.getBoolean(
						NgnConfigurationEntry.NETWORK_USE_3G,
						NgnConfigurationEntry.DEFAULT_NETWORK_USE_3G));
		mConfigurationService.putString(
				NgnConfigurationEntry.NETWORK_IP_VERSION,
				mConfigurationService.getString(
						NgnConfigurationEntry.NETWORK_IP_VERSION,
						NgnConfigurationEntry.DEFAULT_NETWORK_IP_VERSION));

		// Compute
		if (!mConfigurationService.commit()) {
			Log.e("SystemVarTools", "Failed to Commit() configuration");
		}
	}

	/**
	 * DNS闁哄被鍎撮锟�
	 * 
	 * @param domain
	 * @return
	 */
	public static String getDomainIp(String domain) {
		InetAddress addr;
		String hostIp = null;
		try {
			addr = InetAddress.getByName(domain);
			hostIp = addr.getHostAddress();
			Log.d("", "DNS domain=" + domain + " ip=" + hostIp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			hostIp = GlobalVar.pcscfIp;
		}
		return hostIp;
	}

	public static String toDBC(String input) {
		if (input == null) {
			return null;
		}
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375) {
				c[i] = (char) (c[i] - 65248);
			}
		}
		return new String(c);
	}

	public static String toSBC(String input) {
		if (input == null) {
			return null;
		}
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == ' ') {
				c[i] = '\u3000';
			} else if (c[i] < '\177') {
				c[i] = (char) (c[i] + 65248);
			}
		}
		return new String(c);
	}

	public static void setSettingsDefault() {
		INgnConfigurationService mConfig = Engine.getInstance()
				.getConfigurationService();
		mConfig.putString(NgnConfigurationEntry.NETWORK_REALM,
				NgnConfigurationEntry.DEFAULT_NETWORK_REALM);

		mConfig.putString(NgnConfigurationEntry.FILE_SERVER_URL,
				NgnConfigurationEntry.DEFAULT_FILE_SERVER_URL);

		mConfig.putString(NgnConfigurationEntry.MAP_SERVER_URL,
				NgnConfigurationEntry.DEFAULT_MAP_SERVER_URL);

		// 闁瑰灚鎸稿畵鍐嫉瀹ュ懎顫ら柛锝冨妼閻撴瑩宕ラ敓锟�
		String realm = mConfig.getString(NgnConfigurationEntry.NETWORK_REALM,
				NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
		Log.e("zhangjie:ScreenIdentity-onPause()",
				"鐟滅増鎸告晶鐘诲嫉瀹ュ懎顫ら柛锝冨妼閻撴瑩宕ュ鍕闁挎冻鎷� " + realm);

		mConfig.putString(NgnConfigurationEntry.NETWORK_GROUP_REALM,
				NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);

		// 闁瑰灚鎸稿畵鍐礃閵堝洨鐭嬮柡鍫濈Т婵喖宕抽妸銉у幍闁告熬鎷�
		String groupRealm = mConfig.getString(
				NgnConfigurationEntry.NETWORK_GROUP_REALM,
				NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);
		Log.e("zhangjie:ScreenIdentity-onPause()",
				"鐟滅増鎸告晶鐘电礃閵堝洨鐭嬮柡鍫濈Т婵喖宕抽妸銉у幍闁告艾绉崇拹鐔兼晬閿燂拷" + groupRealm);

		mConfig.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST,
				NgnConfigurationEntry.DEFAULT_NETWORK_PCSCF_HOST);

		mConfig.putString(NgnConfigurationEntry.NATT_STUN_SERVER,
				NgnConfigurationEntry.DEFAULT_NATT_STUN_SERVER);

		if (!mConfig.commit()) {
			Log.e("Failed", "Failed to Commit() configuration");
		}
	}

	/**
	 * 闁兼儳鍢茶ぐ鍥純閺嵮呮憻婵絽绉靛Σ绔宐c闁汇劌瀚鍥ㄧ▔閿熻姤绋夐鍕笚缂佽京濮峰▓鎴炴媴瀹ュ洨鏋�
	 * 
	 * @param abc
	 *            闁瑰吋绮庨崒銊э拷濡や胶妲�
	 * @param list
	 *            闁轰胶澧楀畵渚�宕氬Δ鍕╋拷
	 * @return
	 */
	public static int getListItemPosByAbc(String abc, List<ModelContact> list) {

		if (list == null || list.size() == 0) {
			return 0;
		}
		int pos = 0;
		for (; pos < list.size(); pos++) {
			if (list.get(pos).pyHeaders.toLowerCase().startsWith(
					abc.toLowerCase())) {
				break;
			}
		}
		return pos;
	}

	public static List<ModelContact> sortContactsByABC(
			List<ModelContact> contacts) {
		if (contacts == null || contacts.size() == 0) {
			return contacts;
		}

		// for(ModelContact model : contacts){
		// MyLog.d("",
		// "ModelStaSSS闁靛棴鎷�"+model.mobileNo+","+model.name+"闁靛棴鎷�");
		// }
		// long sta = new Date().getTime();
		//
		try {
			ModelContact mc = null;
			for (int i = 0; i < contacts.size(); i++) {
				mc = contacts.get(i);
				// Log.e("pyHeaders!!!!", i+"   "+mc.pyHeaders);
				int j = 1;
				for (; j < contacts.size() - i; j++) {
					if (contacts.get(j).pyHeaders
							.compareTo(contacts.get(j - 1).pyHeaders) < 0) {
						ModelContact tmp = contacts.get(j);
						contacts.set(j, contacts.get(j - 1));
						contacts.set(j - 1, tmp);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		long sto = new Date().getTime();
		// MyLog.d("", "闁圭儤甯掔花顓㈠箥瑜戦、鎴﹀籍閸洘锛�="+(sto-sta));
		// for(ModelContact model : contacts){
		// MyLog.d("",
		// "ModelStoSSS闁靛棴鎷�"+model.mobileNo+","+model.name+"闁靛棴鎷�");
		// }
		return contacts;
	}

	/**
	 * 闁兼儳鍢茶ぐ鍥嫉椤掞拷鍕鹃柟纰夋嫹闁哄牆鈥漰
	 * 
	 * @return
	 */
	public static Map<String, String> getAllLocalIp() {

		Map<String, String> addressMap = new HashMap<String, String>();

		try {
			Method NetworkInterface_isUp = NetworkInterface.class
					.getDeclaredMethod("isUp");

			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				final NetworkInterface intf = en.nextElement();

				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (inetAddress.isLoopbackAddress()
							|| ((inetAddress instanceof Inet6Address) && ((Inet6Address) inetAddress)
									.isLinkLocalAddress())) {
						continue;
					}
					if (((inetAddress instanceof Inet4Address) && !false)
							|| ((inetAddress instanceof Inet6Address) && false)) {
						addressMap.put(intf.getName(),
								inetAddress.getHostAddress());
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return addressMap;

	}

	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static void showicon(ImageView imageView, ModelContact model,
			Context mContext) {

		if (mImageLoader == null) {

			mImageLoader = new ImageLoader(mContext);
		}

		if (model.icon == null || model.icon == "") {

			imageView
					.setImageResource(SystemVarTools.getThumbID(model.imageid));
		} else {

			if (model.icon.endsWith("group")) {
				imageView.setImageResource(SystemVarTools
						.getThumbID(model.imageid));
			} else { // 闁艰鲸姊婚柈瀛樼閸濆嫨浠堥柛宥忔嫹
				// 闁稿繐鐗婂Ο澶岀矆濞差亝顓归柛妤�鍟垮ù姗�寮介敓锟�
				imageView.setImageResource(SystemVarTools
						.getThumbID(model.imageid));

				// if (mIdentity.trim().endsWith(model.mobileNo.trim())) { //
				// 闁烩偓鍔嶉崺娑㈡嚊椤忓嫮绠掗柣銊ュ閵囨棃宕撻敓锟�
				// // 闁哄倸娲ｅ▎銏ゅ触瀹ュ嫯绀媘yicon.jpg
				//
				// String fileSavePath = SystemVarTools.downloadIconPath
				// + "myicon.jpg";
				//
				// if (fileExists(fileSavePath)) { //
				// 濠碘�冲�归悘澶愬棘閸ワ附顐介悗娑櫭﹢顏堝礆濞嗘垶绾柟鎭掑劜濡绮堥敓锟�
				//
				// MyLog.d("", "濞戞搩浜欏Ч澶嬪緞閺夋垵鍓奸柡鍕⒔閵囷拷
				// myicon.jpg閻庢稒锚濠�顏堟儎鐎涙ê澶嶉柡鍕⒔閵囷拷);
				//
				// mImageLoader.DisplayImage(fileSavePath, imageView,
				// false);
				//
				// } else { // 濞戞挸绉撮悺銊╁捶閵娿儱鐏熷☉鎾愁儓濞达拷
				//
				// MyLog.e("", "濞戞搩浜欏Ч澶嬪緞閺夋垵鍓奸柡鍕⒔閵囷拷
				// myicon.jpg濞戞挸绉撮悺銊╁捶閵娿儳纾诲┑顔碱儎缁楀懏娼敓锟�);
				//
				// MyiconFileHttpDownLoadClient myiconDownLoadClient = new
				// MyiconFileHttpDownLoadClient();
				// myiconDownLoadClient.httpDownloadFileInThread(
				// model.icon.trim(), fileSavePath.trim());
				// }
				//
				// } else {

				String iconFileName = getIconFileName(model.icon);

				MyLog.e("showicon", "IconFileName=" + iconFileName);

				String fileSavePath = SystemVarTools.downloadIconPath
						+ iconFileName;

				if (fileExists(fileSavePath)) { // 濠碘�冲�归悘澶愬棘閸ワ附顐介悗娑櫭﹢顏堝礆濞嗘垶绾柟鎭掑劜濡绮堥敓锟�

					MyLog.e("", iconFileName
							+ "  濠㈣埖娼欓崕姘跺及閸撗佷粵    闁哄倸娲ｅ▎銏拷濡儤韬柣鈺佺摠鐢挳寮伴崜褋浠�");

					mImageLoader.DisplayImage(fileSavePath, imageView, false);

				} else { // 濞戞挸绉撮悺銊╁捶閵娿儱鐏熷☉鎾愁儓濞达拷

					MyLog.e("",
							iconFileName
									+ "  濠㈣埖娼欓崕姘跺及閸撗佷粵    闁哄倸娲ｅ▎銏＄▔瀹ュ懐鎽犻柛锔哄妼缁辨垶鎱ㄧ�ｂ晝鐟撻弶鐑囨嫹");

					MyiconFileHttpDownLoadClient myiconDownLoadClient = new MyiconFileHttpDownLoadClient();
					myiconDownLoadClient.httpDownloadFileInThread(
							model.icon.trim(), fileSavePath.trim());
				}
			}
			// }

		}

	}

	public static void showBigicon(ImageView imageView, ModelContact model,
			Context mContext, Handler handler) {

		if (mImageLoader == null) {

			mImageLoader = new ImageLoader(mContext);
		}

		if (model.bigIcon == null || model.bigIcon == "") {

			showicon(imageView, model, mContext);
		} else {

			// 闁稿繐鐗婂Ο澶岀矆閸濆嫮姣堥柛銉у亾閻栵拷
			showicon(imageView, model, mContext);

			// if (mIdentity.trim().endsWith(model.mobileNo.trim())) { //
			// 闁烩偓鍔嶉崺娑㈡嚊椤忓嫮绠掗柣銊ュ閵囨棃宕撻敓锟�
			// // 闁哄倸娲ｅ▎銏ゅ触瀹ュ嫯绀媘yicon.jpg
			//
			// String fileSavePath = SystemVarTools.downloadIconPath
			// + "myicon_big.jpg";
			//
			// if (fileExists(fileSavePath)) { //
			// 濠碘�冲�归悘澶愬棘閸ワ附顐介悗娑櫭﹢顏堝礆濞嗘垶绾柟鎭掑劜濡绮堥敓锟�
			//
			// MyLog.d("", "濞戞搩浜欏Ч澶嬪緞瑜嶉妵鏃堝磽韫囨梹鈻旂紒锟芥嫹
			// myicon_big.jpg閻庢稒锚濠�顏堟儎鐎涙ê澶嶉柡鍕⒔閵囷拷);
			//
			// mImageLoader.DisplayImage(fileSavePath, imageView,
			// false);
			//
			// } else { // 濞戞挸绉撮悺銊╁捶閵娿儱鐏熷☉鎾愁儓濞达拷
			//
			// MyLog.e("", "濞戞搩浜欏Ч澶嬪緞瑜嶉妵鏃堝磽韫囨梹鈻旂紒锟芥嫹
			// myicon_big.jpg濞戞挸绉撮悺銊╁捶閵娿儳纾诲┑顔碱儎缁楀懏娼敓锟�);
			//
			// MyiconFileHttpDownLoadClient myiconDownLoadClient = new
			// MyiconFileHttpDownLoadClient();
			// myiconDownLoadClient.setmRefreshHandler(handler);
			// myiconDownLoadClient.httpDownloadFileInThread(
			// model.bigIcon.trim(), fileSavePath.trim());
			// }
			//
			// } else {

			String iconFileName = getIconFileName(model.bigIcon);

			MyLog.e("showicon", "IconFileName" + iconFileName);

			String fileSavePath = SystemVarTools.downloadIconPath
					+ iconFileName;

			if (fileExists(fileSavePath)) { // 濠碘�冲�归悘澶愬棘閸ワ附顐介悗娑櫭﹢顏堝礆濞嗘垶绾柟鎭掑劜濡绮堥敓锟�

				MyLog.e("", iconFileName
						+ "  濠㈠爢鍐︿粓闁稿秴绻戝Ο澶岀矆閿燂拷   闁哄倸娲ｅ▎銏拷濡儤韬柣鈺佺摠鐢挳寮伴崜褋浠�");

				mImageLoader.DisplayImage(fileSavePath, imageView, false);

			} else { // 濞戞挸绉撮悺銊╁捶閵娿儱鐏熷☉鎾愁儓濞达拷

				MyLog.e("",
						iconFileName
								+ "  濠㈠爢鍐︿粓闁稿秴绻戝Ο澶岀矆閿燂拷   闁哄倸娲ｅ▎銏＄▔瀹ュ懐鎽犻柛锔哄妼缁辨垶鎱ㄧ�ｂ晝鐟撻弶鐑囨嫹");

				MyiconFileHttpDownLoadClient myiconDownLoadClient = new MyiconFileHttpDownLoadClient();
				myiconDownLoadClient.setmRefreshHandler(handler);
				myiconDownLoadClient.httpDownloadFileInThread(
						model.bigIcon.trim(), fileSavePath.trim());
			}
		}

		// }

	}

	public static String getIconFileName(String url) {
		if (url != null) {
			String[] temp = url.split("/");
			if (temp[temp.length - 1] != null) {
				return temp[temp.length - 1];

			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static boolean fileExists(String path) {
		try {
			File f = new File(path);

			if (!f.exists()) {
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}

	}

	public static void deleteIcon(String url) {
		String fileName = getIconFileName(url);

		if (fileExists(SystemVarTools.downloadIconPath + fileName)) {
			File file = new File(SystemVarTools.downloadIconPath + fileName);
			file.delete();
		}

	}

	/**
	 * 闁告帇鍊栭弻鍥蓟閹邦亪鍤嬬紓浣稿濡叉悂宕ラ敃锟借含闂侇偅淇洪鍡氥亹閺囨俺鍘�
	 * */
	public static boolean isGroupInContact(String groupNo) {

		contactListOrgBus_containGlobalGroup.clear();

		contactListOrgBus_containGlobalGroup = mergeBothList(contactListOrg,
				contactListBusinessOrg);
		contactListOrgBus_containGlobalGroup = mergeBothList(
				contactListOrgBus_containGlobalGroup, contactListGlobalGroupOrg);

		if (groupNo != null && !groupNo.equals("")) {

			if (contactListOrgBus_containGlobalGroup != null
					&& contactListOrgBus_containGlobalGroup.size() != 0) {
				// for (ModelContact model :
				// contactListOrgBus_containGlobalGroup) {

				for (int i = 0; i < contactListOrgBus_containGlobalGroup.size(); i++) {

					if (contactListOrgBus_containGlobalGroup.get(i).mobileNo
							.equals(groupNo)) {
						MyLog.e(TAG, groupNo + "闁革负鍔戦敓鑺ヤ亢椤斿棜銇愰弴鐘电煁濞戞搫鎷�");
						return true;
					}
				}
				MyLog.e(TAG, groupNo + "濞戞挸绉村﹢顏堟焻濮樻剚鍞电憸鐗堟礈缁秵绋夐敓");
				return false;

			} else {
				MyLog.e(TAG, groupNo + "濞戞挸绉村﹢顏堟焻濮樻剚鍞电憸鐗堟礈缁秵绋夐敓");
				return false;
			}

		} else {
			MyLog.e(TAG, groupNo + "濞戞挸绉村﹢顏堟焻濮樻剚鍞电憸鐗堟礈缁秵绋夐敓");
			return false;
		}

	}

	/***
	 * 闁兼儳鍢茶ぐ鍥╃磼閸曨厾鐭忓☉鎿冨幘濞堟垹绮璺伇濞戞搩浜炵划宥夋儍閸曨厾鐭嬮柛娆擃棑閻栵拷
	 * */
	public static String getFirstOrg() {
		contactListOrgBus_containGlobalGroup.clear();

		contactListOrgBus_containGlobalGroup = mergeBothList(contactListOrg,
				contactListBusinessOrg);
		contactListOrgBus_containGlobalGroup = mergeBothList(
				contactListOrgBus_containGlobalGroup, contactListGlobalGroupOrg);

		if (contactListOrgBus_containGlobalGroup != null
				&& contactListOrgBus_containGlobalGroup.size() != 0) {
			return contactListOrgBus_containGlobalGroup.get(0).mobileNo;
		} else {
			return null;
		}
	}

	/***
	 * 闁兼儳鍢茶ぐ鍥箥閿熶粙寮垫径宀�鐭�
	 * */
	public static List<ModelContact> getAllOrg() {
		contactListOrgBus_containGlobalGroup.clear();

		contactListOrgBus_containGlobalGroup = mergeBothList(contactListOrg,
				contactListBusinessOrg);
		contactListOrgBus_containGlobalGroup = mergeBothList(
				contactListOrgBus_containGlobalGroup, contactListGlobalGroupOrg);

		return contactListOrgBus_containGlobalGroup;
	}

	public static String getSimCardMdn() {
		TelephonyManager telManager = (TelephonyManager) SKDroid.getContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = telManager.getSubscriberId();
		String mdn = telManager.getLine1Number();

		String prefix = "1724250";

		if (mdn != null && mdn.contains("+86")) {
			mdn = mdn.substring(3); // 闁归潧顑嗗┃锟藉矗妞嬪海鍨抽柛妯肩帛鐢拷86
		} else if (mdn == null) {
			// if(imsi != null && imsi.length() > 4){
			// StringBuffer tmp = new StringBuffer();
			// tmp.append(prefix);
			// tmp.append(imsi.subSequence(imsi.length()-4, imsi.length()));
			// mdn = tmp.toString();
			// }
		}

		String simserialnum = telManager.getSimSerialNumber();
		String simoperaString = telManager.getSimOperator();
		String simcoun = telManager.getSimCountryIso();
		MyLog.d(TAG, "sim imsi : " + imsi);
		MyLog.d(TAG, "sim mdn : " + mdn);
		MyLog.d(TAG, "sim simserialnumm : " + simserialnum);
		MyLog.d(TAG, "sim simoperaString : " + simoperaString);
		MyLog.d(TAG, "sim simcoun : " + simcoun);
		return mdn;
	}

	public static void changeDNSToIPAddr() {
		Thread t = new Thread(new Runnable() {
			public void run() {

				String groupRealm = Engine
						.getInstance()
						.getConfigurationService()
						.getString(
								NgnConfigurationEntry.NETWORK_GROUP_REALM,
								NgnConfigurationEntry.DEFAULT_NETWORK_GROUP_REALM);
				String groupIpAddr = getIPFromDNS(groupRealm);
				Engine.getInstance()
						.getConfigurationService()
						.putString(NgnConfigurationEntry.NETWORK_GROUP_REALM,
								groupIpAddr);
				String fileRealm = Engine
						.getInstance()
						.getConfigurationService()
						.getString(NgnConfigurationEntry.FILE_SERVER_HOST,
								NgnConfigurationEntry.DEFAULT_FILE_SERVER_HOST);
				String fileIpAddr = getIPFromDNS(groupRealm);
				Engine.getInstance()
						.getConfigurationService()
						.putString(NgnConfigurationEntry.FILE_SERVER_HOST,
								fileIpAddr);
				String fileProt = Engine
						.getInstance()
						.getConfigurationService()
						.getString(NgnConfigurationEntry.FILE_SERVER_PORT,
								NgnConfigurationEntry.DEFAULT_FILE_SERVER_PORT);
				Engine.getInstance()
						.getConfigurationService()
						.putString(NgnConfigurationEntry.FILE_SERVER_URL,
								fileIpAddr + ":" + fileProt);
			}

		});
		t.start();

	}

	public static String getIPFromDNS(String dnsName) {
		String ip_addr = null;
		try {
			InetAddress x = java.net.InetAddress.getByName(dnsName);
			ip_addr = x.getHostAddress();// 寰楀埌瀛楃涓插舰寮忕殑ip鍦板潃
			MyLog.d(TAG, "DNS: " + dnsName + ", IPAddr: " + ip_addr);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			MyLog.d(TAG, e.toString());
		}
		return ip_addr;
	}

}
