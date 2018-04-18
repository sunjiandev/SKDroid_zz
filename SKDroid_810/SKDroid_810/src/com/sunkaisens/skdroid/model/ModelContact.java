package com.sunkaisens.skdroid.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.sip.NgnSubscriptionSession;
import org.doubango.ngn.utils.NgnUriUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Utils.ParserSubscribeState;
import com.sunkaisens.skdroid.Utils.SystemVarTools;

public class ModelContact {
	public String name;// 姓名
	public String mobileNo;// 移动号码
	public int imageid;// 头像id,临时为索引值
	public String state;// 状态,
	public int sex;// 性别
	public boolean isgroup;// 类型
	public String title;// 头衔
	public String brief;// 个人简介
	public String org;// 组织
	public String index;// 索引号码
	public String uri;// 描述id
	public String parent;
	public long lasttime;// 最后交流的时间
	public boolean isOnline; // 是否在线
	public String businessType;// 业务类型

	public String userType;

	public String icon; // 头像

	public String bigIcon; // 大头像

	// 首字母字符串集合
	public String pyHeaders = new String();

	public String toString() {
		return name + "," + mobileNo + "," + imageid + "," + state + "," + sex
				+ "," + isgroup + "," + title + "," + brief + "," + org + ","
				+ index + "," + uri + "," + parent + icon;
	}

	public ModelContact() {
		isOnline = false;
	}

	public ModelContact(String formatValue) {
		fromString(formatValue);
	}

	public void fromString(String value) {
		String[] units = value.split(",");
		if (units.length < 12) {
			return;
		}
		name = units[0];
		mobileNo = units[1];
		imageid = Integer.parseInt(units[2]);
		state = units[3];
		sex = Integer.parseInt(units[4]);
		isgroup = Boolean.parseBoolean(units[5]);
		title = units[6];
		brief = units[7];
		org = units[8];
		index = units[9];
		uri = units[10];
		parent = units[11];

		pyHeaders = makePyHeaders(this.name);
	}

	public ModelContact(String index, String name, String mobileNo,
			String title, String parent, String userType, boolean isGroup,
			String icon) {
		this.index = index;
		this.name = name;
		this.mobileNo = mobileNo;
		this.title = title;
		this.uri = title;
		this.org = parent;
		this.parent = parent;
		this.userType = userType;
		this.isgroup = isGroup;

		this.isOnline = false;

		this.icon = icon;

		pyHeaders = makePyHeaders(this.name);
	}

	public ModelContact(String groupIndex, String uri, String mobileNo,
			String displayName, String bussinessType, int iconId,
			boolean isGroup, String icon) {// 业务组
		this.parent = groupIndex;
		this.org = groupIndex;
		this.title = uri;
		this.uri = uri;
		this.mobileNo = mobileNo;
		this.index = mobileNo;
		this.name = displayName;
		this.businessType = bussinessType;
		this.imageid = iconId;
		this.isgroup = isGroup;

		this.isOnline = false;

		this.icon = icon;

		pyHeaders = makePyHeaders(this.name);
	}

	public String makePyHeaders(String str) {
		try {
			StringBuilder pyHeaderStringBuilder = new StringBuilder();
			if (str == null || str.trim().equals(""))
				return " ";
			str = str.trim();
			char[] chars = str.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				if (chars[i] > 128) {
					String[] headers = PinyinHelper
							.toHanyuPinyinStringArray(chars[i]);
					if (headers == null || headers.length == 0)
						return String.valueOf(chars[0]);
					for (String header : headers) {
						char[] headerChars = header.toCharArray();
						// if(!pyHeaderStringBuilder.toString().contains(String.valueOf(headerChars[0]))){
						pyHeaderStringBuilder.append(headerChars[0]);
						// }
					}
				} else {
					pyHeaderStringBuilder.append(chars[i]);
				}
			}
			return pyHeaderStringBuilder.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return " ";
		}
	}

	public void addBusinessGroupMember(String groupNum, String Number,
			String notifyInfo)// 业务组
	{
		String mIdentity = ParserSubscribeState.getInstance().getIdentify();
		String mNetworkRealm = ParserSubscribeState.getInstance().getRealm();

		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(notifyInfo));
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				int number = SystemVarTools.getContactListBusinessAllNumber();
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					Log.d("updataGroupMemberState ", "tagName = " + tagName);
					if (tagName.equals("list")) {
						// String groupIndex,String uri,String mobileNo,String
						// displayName,String bussinessType,int iconId, boolean
						// isGroup
						ModelContact groupMc = new ModelContact(
								parser.getAttributeValue(null, "name"),
								parser.getAttributeValue(null, "uri"),
								parser.getAttributeValue(null, "name"),
								parser.getAttributeValue(null, "displayName"),
								parser.getAttributeValue(null, "serviceType"),
								14 + (number + 1) % 2, true, "group");
						SystemVarTools.addContactBusinessOrg(groupMc);
						SystemVarTools
								.showToast(
										String.format(
												NgnApplication
														.getContext()
														.getString(
																R.string.business_group_with_colon)
														+ "\'%s\'"
														+ NgnApplication
																.getContext()
																.getString(
																		R.string.created_success),
												groupMc.name), false);
					} else if (tagName.equals("entry")) {
						// ModelContact member = new
						// ModelContact(parser.getAttributeValue(null,
						// "name"),//name,//groupIndex
						// parser.getAttributeValue(null, "uri"),//uri
						// parser.getAttributeValue(null, "name"),//name
						// parser.getAttributeValue(null, "displayName"),
						// parser.getAttributeValue(null, "deviceType"),
						// (number+1)%14, false);

						// ModelContact parent = SystemVarTools
						// .getContactFromRemoteParty(groupNum);
						//
						// String org = null;
						//
						if (groupNum.contains("@")) {
							org = NgnUriUtils.getUserName(groupNum);
						} else {
							org = groupNum;
						}

						String uri = parser.getAttributeValue(null, "uri");

						ModelContact member = new ModelContact();
						member.parent = org;
						member.org = org;
						member.mobileNo = NgnUriUtils.getUserName(uri);
						member.isgroup = false;
						member.name = parser.getAttributeValue(null,
								"displayName");
						member.imageid = (number + 1) % 14;
						member.uri = uri;

						member.pyHeaders = member.makePyHeaders(member.name);

						String myServiceName = null;

						List<ModelContact> serviceOrg = SystemVarTools
								.getContactListBusinessOrg();

						if (serviceOrg != null) {
							for (int i = 0; i < serviceOrg.size(); i++) {
								if (serviceOrg.get(i).mobileNo.equals(groupNum
										.trim())) {
									myServiceName = serviceOrg.get(i).name;
									break;
								}

							}
						}

						if (SystemVarTools.addContactBusinessAll(member)) {
							if (myServiceName != null) {
								SystemVarTools
										.showToast(
												String.format(
														"\'%s\'"
																+ NgnApplication
																		.getContext()
																		.getString(
																				R.string.joined_business_group_with_colon)
																+ "\'%s\'",
														member.name,
														myServiceName), false);
							} else {
								SystemVarTools
										.showToast(
												String.format(
														"\'%s\'"
																+ NgnApplication
																		.getContext()
																		.getString(
																				R.string.joined_business_group_with_colon)
																+ "\'%s\'",
														member.name,
														member.parent), false);
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
	}

	public void addGlobalGroupMember(String groupNum, String notifyInfo)// 公共台
	{
		String mIdentity = ParserSubscribeState.getInstance().getIdentify();
		String mNetworkRealm = ParserSubscribeState.getInstance().getRealm();

		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(notifyInfo));
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				int number = SystemVarTools.getContactListBusinessAllNumber();
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					Log.d("updataGroupMemberState ", "tagName = " + tagName);
					if (tagName.equals("list")) {
						// String groupIndex,String uri,String mobileNo,String
						// displayName,String bussinessType,int iconId, boolean
						// isGroup
						ModelContact groupMc = new ModelContact(
								parser.getAttributeValue(null, "name"),
								parser.getAttributeValue(null, "uri"),
								parser.getAttributeValue(null, "name"),
								parser.getAttributeValue(null, "displayName"),
								parser.getAttributeValue(null, "serviceType"),
								14 + (number + 1) % 2, true, "group");

						SystemVarTools.addContactGlobalGroupOrg(groupMc);
						SystemVarTools
								.showToast(
										String.format(
												NgnApplication
														.getContext()
														.getString(
																R.string.global_group_with_colon)
														+ "\'%s\'"
														+ NgnApplication
																.getContext()
																.getString(
																		R.string.created_success),
												groupMc.name), false);
					} else if (tagName.equals("entry")) {
						// ModelContact member = new
						// ModelContact(parser.getAttributeValue(null,
						// "name"),//name,//groupIndex
						// parser.getAttributeValue(null, "uri"),//uri
						// parser.getAttributeValue(null, "name"),//name
						// parser.getAttributeValue(null, "displayName"),
						// parser.getAttributeValue(null, "deviceType"),
						// (number+1)%14, false);

						// ModelContact parent = SystemVarTools
						// .getContactFromRemoteParty(groupNum);

						String org = null;

						if (groupNum.contains("@")) {
							org = NgnUriUtils.getUserName(groupNum);
						} else {
							org = groupNum;
						}

						String uri = parser.getAttributeValue(null, "uri");

						ModelContact member = new ModelContact();
						member.parent = org;
						member.org = org;
						member.mobileNo = NgnUriUtils.getUserName(uri);
						member.isgroup = false;
						member.name = parser.getAttributeValue(null,
								"displayName");
						member.imageid = (number + 1) % 14;
						member.userType = "1";
						member.uri = uri;

						member.pyHeaders = member.makePyHeaders(member.name);

						String myGlobalName = null;

						List<ModelContact> globalOrg = SystemVarTools
								.getContactListGlobalGroupOrg();

						if (globalOrg != null) {
							for (int i = 0; i < globalOrg.size(); i++) {
								if (globalOrg.get(i).mobileNo.equals(groupNum
										.trim())) {
									myGlobalName = globalOrg.get(i).name;
									break;
								}

							}
						}

						if (SystemVarTools.addContactGlobalAll(member)) {
							if (myGlobalName != null) {
								SystemVarTools
										.showToast(
												String.format(
														"\'%s\'"
																+ NgnApplication
																		.getContext()
																		.getString(
																				R.string.joined_global_group_with_colon)
																+ "\'%s\'",
														member.name,
														myGlobalName), false);
							} else {
								SystemVarTools
										.showToast(
												String.format(
														"\'%s\'"
																+ NgnApplication
																		.getContext()
																		.getString(
																				R.string.joined_global_group_with_colon)
																+ " \'%s\'",
														member.name,
														member.parent), false);
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
	}

	public void addNormalGroupMember(String groupNumber, String notifyInfo)// add
																			// group
																			// or
																			// member
	{
		String mIdentity = ParserSubscribeState.getInstance().getIdentify();
		String mNetworkRealm = ParserSubscribeState.getInstance().getRealm();
		ModelContact mc = null;

		int number = SystemVarTools.getContactAll().size();
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(notifyInfo));
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					Log.d("updataGroupMemberState ", "tagName = " + tagName);

					if (notifyInfo.contains("public-group"))// 普通组
					{
						if (tagName.equals("list"))// 增加组
						{
							Log.d("", "增加组");
							mc = new ModelContact();
							mc.isgroup = true;
							mc.index = parser
									.getAttributeValue(null, "listIdx");
							mc.mobileNo = parser
									.getAttributeValue(null, "name");
							mc.org = parser.getAttributeValue(null, "superIdx");
							mc.name = parser.getAttributeValue(null,
									"displayName");
							mc.parent = mc.org;
							mc.imageid = 14 + (number + 1) % 2;
							String myGroupNumber = SystemVarTools
									.getGroupMobileNoFromIndex(mc.parent);
							if (myGroupNumber == null)
								myGroupNumber = groupNumber;

							String myGroupName = null;

							List<ModelContact> normalOrg = SystemVarTools
									.getContactOrg();

							if (normalOrg != null) {
								for (int i = 0; i < normalOrg.size(); i++) {
									if (normalOrg.get(i).mobileNo
											.equals(myGroupNumber.trim())) {
										myGroupName = normalOrg.get(i).name;
										break;
									}

								}
							}

							SystemVarTools.addContactOrg(mc);

							if (myGroupName != null) {
								SystemVarTools
										.showToast(
												String.format(
														"\'%s\'"
																+ NgnApplication
																		.getContext()
																		.getString(
																				R.string.subgroup_joined)
																+ "\'%s\'",
														mc.name, myGroupName),
												false);
							} else {
								SystemVarTools
										.showToast(
												String.format(
														"\'%s\'"
																+ NgnApplication
																		.getContext()
																		.getString(
																				R.string.subgroup_joined)
																+ "\'%s\'",
														mc.name, myGroupNumber),
												false);
							}

							// mc.businessType =
							// parser.getAttributeValue(null,"serviceType");
						} else if (tagName.equals("entry")) {// 增加成员
							mc = new ModelContact();
							String uri = parser.getAttributeValue(null, "uri");
							mc.isgroup = false;
							mc.mobileNo = NgnUriUtils.getUserName(uri);
							mc.index = NgnUriUtils.getUserName(uri);

							// ModelContact parent = SystemVarTools
							// .getContactFromRemoteParty(groupNumber);

							// String org = null;
							//
							if (groupNumber.contains("@")) {
								org = NgnUriUtils.getUserName(groupNumber);
							} else {
								org = groupNumber;
							}

							mc.org = org;
							mc.parent = mc.org;
							mc.title = uri;
							mc.uri = mc.title;
							mc.userType = parser.getAttributeValue(null,
									"usertype");
							mc.businessType = parser.getAttributeValue(null,
									"deviceType");
						} else if (tagName.equals("display-name")) {
							if (mc == null) {
								return;
							}
							mc.name = parser.nextText(); //
							mc.pyHeaders = mc.makePyHeaders(mc.name);
							if (!mc.isgroup) {
								Log.e("", "我执行了");
								mc.imageid = (number + 1) % 14;
								// String myGroupNumber =
								// SystemVarTools.getGroupMobileNoFromIndex(mc.parent);
								// if(myGroupNumber==null)
								// myGroupNumber = groupNumber;

								String myGroupName = null;

								List<ModelContact> normalOrg = SystemVarTools
										.getContactOrg();

								if (normalOrg != null) {
									for (int i = 0; i < normalOrg.size(); i++) {
										if (normalOrg.get(i).mobileNo
												.equals(groupNumber.trim())) {
											myGroupName = normalOrg.get(i).name;
											break;
										}

									}
								}

								if (SystemVarTools.addContactAll(mc)) {

									if (myGroupName != null) {
										SystemVarTools
												.showToast(
														String.format(
																"\'%s\'"
																		+ NgnApplication
																				.getContext()
																				.getString(
																						R.string.join_group)
																		+ "\'%s\'",
																mc.name,
																myGroupName),
														false);
									} else {
										SystemVarTools
												.showToast(
														String.format(
																"\'%s\'"
																		+ NgnApplication
																				.getContext()
																				.getString(
																						R.string.join_group)
																		+ "\'%s\'",
																mc.name,
																groupNumber),
														false);
									}
								}
							}
							// else{
							// mc.imageid = 14+(number+1)%2;
							// String myGroupNumber =
							// SystemVarTools.getGroupMobileNoFromIndex(mc.parent);
							// if(myGroupNumber==null)
							// myGroupNumber = groupNumber;
							// SystemVarTools.addContactOrg(mc);
							// SystemVarTools.showToast(String.format("\'%s\'子组，加入群组：\'%s\'",mc.mobileNo,myGroupNumber));
							// }

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
	}

	public void replaceNormalGroupMember(String notifyContent) {
		String mIdentity = ParserSubscribeState.getInstance().getIdentify();
		String mNetworkRealm = ParserSubscribeState.getInstance().getRealm();
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(notifyContent));
			String memberNumber = null;
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					Log.d("updataGroupMemberState ", "tagName = " + tagName);
					if (tagName.equals("list")) {
						int number = SystemVarTools.getContactOrg().size();
						String groupNumber = parser.getAttributeValue(null,
								"name");
						for (int i = 0; i < number; ++i) {
							if (SystemVarTools.getContactOrg().get(i).mobileNo
									.equals(groupNumber))
								SystemVarTools.getContactOrg().get(i).name = parser
										.getAttributeValue(null, "displayName");
						}

					} else if (tagName.equals("entry")) {
						memberNumber = parser.getAttributeValue(null, "name");
					} else if (tagName.equals("display-name")) {
						String displayName = parser.nextText();
						// NgnUriUtils.setGroupAndMemberNum(notifyContent);
						String groupNumber = NgnUriUtils
								.GetGroupNum2(notifyContent);
						// String groupIndex = null;
						// for (int i = 0; i < SystemVarTools.getContactOrg()
						// .size(); ++i) {
						// if (SystemVarTools.getContactOrg().get(i).mobileNo
						// .equals(groupNumber))//
						// ||contactListOrg.get(i).org.equals(orgNums[j]))
						// {
						// groupIndex = SystemVarTools.getContactOrg()
						// .get(i).index;
						// }
						// }
						int number = SystemVarTools.getContactAll().size();
						for (int i = 0; i < number; ++i) {
							if (SystemVarTools.getContactAll().get(i).mobileNo
									.equals(memberNumber)
									&& SystemVarTools.getContactAll().get(i).parent
											.equals(groupNumber))
								SystemVarTools.getContactAll().get(i).name = displayName;
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
	}

	public void replaceProperity(String notifyInfo) {
		boolean isgroup = notifyInfo.contains("replaceg");
		this.isgroup = isgroup;
		if (this.isgroup) {
			int nsStart = notifyInfo.indexOf("displayName=\"")
					+ "displayName=\"".length();
			if (nsStart < 0) {
				NgnUriUtils.catchError("ModelContact",
						"replaceProperity <displayName>", "nsStart", nsStart);
				return;
			}
			notifyInfo = notifyInfo.substring(nsStart);
			int nsEnd = notifyInfo.indexOf("\"");
			this.name = notifyInfo.substring(0, nsEnd).trim();
		} else {

			int nsStart = notifyInfo.indexOf("<defaultns:display-name>")
					+ "<defaultns:display-name>".length();
			if (nsStart < 0) {
				NgnUriUtils.catchError("ModelContact",
						"replaceProperity <defaultns:display-name>", "nsStart",
						nsStart);
				return;
			}
			notifyInfo = notifyInfo.substring(nsStart);
			int nsEnd = notifyInfo.indexOf("<");
			this.name = notifyInfo.substring(0, nsEnd).trim();
		}
	}

	public void getAllMemberofGroup(String XML) {
		String mIdentity = ParserSubscribeState.getInstance().getIdentify();
		String mNetworkRealm = ParserSubscribeState.getInstance().getRealm();
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(XML));
			ModelContact mc = null;
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					Log.d("updataGroupMemberState ", "tagName = " + tagName);

					int number = SystemVarTools.getContactAll().size();
					if (tagName.equals("list"))// 增加组
					{
						// String index,String name,String mobileNo,String
						// title,String parent,String userType,boolean isGroup
						ModelContact groupMc = new ModelContact(
								parser.getAttributeValue(null, "listIdx"),
								parser.getAttributeValue(null, "displayName"),
								parser.getAttributeValue(null, "name"),
								parser.getAttributeValue(null, "uri"),
								parser.getAttributeValue(null, "superIdx"),
								parser.getAttributeValue(null, "usertype"),
								true, "group");
						groupMc.imageid = 14 + (number + 1) % 2;
						SystemVarTools.addContactOrg(groupMc);
						final NgnSubscriptionSession subscriptionSession = NgnSubscriptionSession
								.createOutgoingSession(
										Engine.getInstance().getSipService()
												.getSipStack(),
										"sip:" + mIdentity + "@"
												+ mNetworkRealm,
										"sip:" + groupMc.mobileNo + "@"
												+ mNetworkRealm,
										NgnSubscriptionSession.EventPackageType.Group);
						subscriptionSession.subscribe();
					} else if (tagName.equals("entry")) {// 增加成员
						mc = new ModelContact();
						mc.mobileNo = parser.getAttributeValue(null, "name");
						mc.index = parser.getAttributeValue(null, "name");
						mc.org = parser.getAttributeValue(null, "superIdx");
						mc.parent = mc.org;
						mc.title = parser.getAttributeValue(null, "uri");
						mc.userType = parser
								.getAttributeValue(null, "usertype");
					} else if (tagName.equals("display-name")) {
						if (mc == null)
							continue;
						mc.name = parser.nextText(); //
						mc.imageid = (number + 1) % 14;
						SystemVarTools.addContactAll(mc);
						final NgnSubscriptionSession subscriptionSession = NgnSubscriptionSession
								.createOutgoingSession(
										Engine.getInstance().getSipService()
												.getSipStack(),
										"sip:" + mIdentity + "@"
												+ mNetworkRealm,
										"sip:" + mc.mobileNo + "@"
												+ mNetworkRealm,
										NgnSubscriptionSession.EventPackageType.Presence);
						subscriptionSession.subscribe();
					}
				}
				parser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void replaceBusinessGroupMember(String notifyContent) {
		String mIdentity = ParserSubscribeState.getInstance().getIdentify();
		String mNetworkRealm = ParserSubscribeState.getInstance().getRealm();
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(notifyContent));
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					Log.d("updataGroupMemberState ", "tagName = " + tagName);
					if (tagName.equals("list")) {
						// String groupIndex,String uri,String mobileNo,String
						// displayName,String bussinessType,int iconId, boolean
						// isGroup
						int number = SystemVarTools.getContactListBusinessOrg()
								.size();
						String groupNumber = parser.getAttributeValue(null,
								"name");
						for (int i = 0; i < number; ++i) {
							if (SystemVarTools.getContactListBusinessOrg().get(
									i).mobileNo.equals(groupNumber))
								SystemVarTools.getContactListBusinessOrg().get(
										i).name = parser.getAttributeValue(
										null, "displayName");
						}

					} else if (tagName.equals("entry")) {
						// NgnUriUtils.setGroupAndMemberNum(notifyContent);
						// String groupNumber = NgnUriUtils.getGroupNum();

						String groupNumber = NgnUriUtils
								.GetGroupNum2(notifyContent);
						int number = SystemVarTools.getContactListBusinessAll()
								.size();
						// String memberNumber =
						// parser.getAttributeValue(null,"name");
						String memberNumber = NgnUriUtils
								.getReplaceNumber(notifyContent);
						for (int i = 0; i < number; ++i) {
							if (SystemVarTools.getContactListBusinessAll().get(
									i).mobileNo.equals(memberNumber)
									&& SystemVarTools
											.getContactListBusinessAll().get(i).parent
											.equals(groupNumber))
								SystemVarTools.getContactListBusinessAll().get(
										i).name = parser.getAttributeValue(
										null, "displayName");
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
	}

	public void replaceGlobalGroupMember(String notifyContent) {
		String mIdentity = ParserSubscribeState.getInstance().getIdentify();
		String mNetworkRealm = ParserSubscribeState.getInstance().getRealm();
		XmlPullParserFactory factory;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(notifyContent));
			while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String tagName = parser.getName();
					Log.d("updataGroupMemberState ", "tagName = " + tagName);
					if (tagName.equals("list")) {
						// String groupIndex,String uri,String mobileNo,String
						// displayName,String bussinessType,int iconId, boolean
						// isGroup
						int number = SystemVarTools
								.getContactListGlobalGroupOrg().size();
						String groupNumber = parser.getAttributeValue(null,
								"name");
						for (int i = 0; i < number; ++i) {
							if (SystemVarTools.getContactListGlobalGroupOrg()
									.get(i).mobileNo.equals(groupNumber))
								SystemVarTools.getContactListGlobalGroupOrg()
										.get(i).name = parser
										.getAttributeValue(null, "displayName");
						}

					} else if (tagName.equals("entry")) {
						// NgnUriUtils.setGroupAndMemberNum(notifyContent);
						// String groupNumber = NgnUriUtils.getGroupNum();

						String groupNumber = NgnUriUtils
								.GetGroupNum2(notifyContent);
						int number = SystemVarTools
								.getContactListGlobalGroupAll().size();
						// String memberNumber =
						// parser.getAttributeValue(null,"name");
						String memberNumber = NgnUriUtils
								.getReplaceNumber(notifyContent);
						for (int i = 0; i < number; ++i) {
							if (SystemVarTools.getContactListGlobalGroupAll()
									.get(i).mobileNo.equals(memberNumber)
									&& SystemVarTools
											.getContactListGlobalGroupAll()
											.get(i).parent.equals(groupNumber)) {
								Log.e("原来name",
										SystemVarTools
												.getContactListGlobalGroupAll()
												.get(i).name);
								Log.e("displayname",
										""
												+ parser.getAttributeValue(
														null, "displayName"));
								SystemVarTools.getContactListGlobalGroupAll()
										.get(i).name = parser
										.getAttributeValue(null, "displayName");
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
	}

	@Override
	public boolean equals(Object o) {
		ModelContact mc = (ModelContact) o;
		if (mc != null) {
			return this.mobileNo.equals(mc.mobileNo);
		} else {
			return false;
		}
	}

}
