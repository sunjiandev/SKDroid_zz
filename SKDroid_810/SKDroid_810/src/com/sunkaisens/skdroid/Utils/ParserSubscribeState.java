package com.sunkaisens.skdroid.Utils;

import java.util.List;

import org.doubango.ngn.NgnApplication;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.doubango.ngn.utils.NgnUriUtils;
import org.doubango.utils.MyLog;

import android.util.Log;

import com.sunkaisens.skdroid.Engine;
import com.sunkaisens.skdroid.R;
import com.sunkaisens.skdroid.Screens.ScreenDownloadConcacts;
import com.sunkaisens.skdroid.Screens.ScreenTabHome;
import com.sunkaisens.skdroid.Services.ServiceContact;
import com.sunkaisens.skdroid.Services.ServiceRegiste;
import com.sunkaisens.skdroid.Services.ServiceSocketMode;
import com.sunkaisens.skdroid.component.NodeResource;
import com.sunkaisens.skdroid.model.ModelContact;
import com.sunkaisens.skdroid.util.GlobalSession;

public class ParserSubscribeState {
	
	private static final String TAG = ParserSubscribeState.class.getCanonicalName();
	// Subscription-State:
	private final String DELETE_MEMBER_ITSELF = "terminated;reason=rejected";
	private final String DELETE_WHOLE_GROUP = "terminated;reason=noresource";

	// Content-Type
	private final String MEMBER_ONLINE_STATE = "application/pidf+xml";
	private final String SUBSCRIBE_GROUP = "application/public-group+xml";
	private final String GROUP_STATE_CHANGE = "application/xcap-diff+xml";
	private boolean InviteUsertoJoinOtherGroup = false;
	private final String myselfNumber = Engine
			.getInstance()
			.getConfigurationService()
			.getString(NgnConfigurationEntry.IDENTITY_IMPI,
					NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

	private static ParserSubscribeState instance;
	private final String REALM = Engine
			.getInstance()
			.getConfigurationService()
			.getString(NgnConfigurationEntry.NETWORK_REALM,
					NgnConfigurationEntry.DEFAULT_NETWORK_REALM);
	private final String IDENTIFY = Engine
			.getInstance()
			.getConfigurationService()
			.getString(NgnConfigurationEntry.IDENTITY_IMPI,
					NgnConfigurationEntry.DEFAULT_IDENTITY_IMPI);

	private ParserSubscribeState() {

	}

	public String getIdentify() {
		return IDENTIFY;
	}

	public String getRealm() {
		return REALM;
	}

	public static ParserSubscribeState getInstance() {
		if (instance == null)
			instance = new ParserSubscribeState();
		return instance;
	}

	private void recursiveDelGroup(StringBuffer orgNumStr, String myselfNumber) {
		if (orgNumStr == null || orgNumStr.toString().equals("")
				|| orgNumStr.length() < 0)
			return;

		String[] orgNums = orgNumStr.toString().split(",");
		// if(orgNums.length<0) orgNums[0] = orgNumStr.toString();
		int num = orgNums.length;
		StringBuffer orgNumNew = new StringBuffer();
		for (int j = 0; j < num; ++j) {
			for (int i = 0; i < SystemVarTools.getContactAll().size(); ++i)// d删除本群组中
			{
				ModelContact modelContact = SystemVarTools.getContactAll().get(
						i);
				MyLog.d("", modelContact.mobileNo + "'s org="
						+ modelContact.org);
				if (modelContact.org.equals(orgNums[j])) {
					if (modelContact.mobileNo.equals(myselfNumber)) {
						// 清除订阅记录
						for (ModelContact mcTmp : SystemVarTools
								.getContactAll()) {
							ServiceContact.deleteContact(mcTmp);
						}
						SystemVarTools.getContactAll().clear();
						SystemVarTools.getContactOrg().clear();
						return;
					}
					SystemVarTools.getContactAll().remove(i);
					--i;

				}
			}
			for (int i = 0; i < SystemVarTools.getContactOrg().size(); ++i) {
				if (SystemVarTools.getContactOrg().get(i).org
						.equals(orgNums[j]))// ||contactListOrg.get(i).org.equals(orgNums[j]))
				{
					orgNumNew
							.append(SystemVarTools.getContactOrg().get(i).index
									+ ",");
					SystemVarTools.getContactOrg().remove(i);
					--i;
				}
			}
		}
		recursiveDelGroup(orgNumNew, myselfNumber);
	}

	public void parserGroupMemberState2(String contentType, String notifyContent) {
		MyLog.d(TAG, "parserGroupMemberState2()");
		if (contentType.equals(MEMBER_ONLINE_STATE))
			updateContactListState(notifyContent); // 解析呈现状态
		else if (contentType.equals(SUBSCRIBE_GROUP))
			;// 暂不处理
		else if (contentType.equals(GROUP_STATE_CHANGE)) {
			if (notifyContent.contains("public-group")) {
				dealNormalGroup2(notifyContent);
			} else if (notifyContent.indexOf("service-group") >= 0)// 业务组
			{
				dealServiceGroup2(notifyContent);
			} else if (notifyContent.indexOf("global-group") >= 0) {
				dealGlobalGroup2(notifyContent);

			} else if (notifyContent.indexOf("subscribe-group") >= 0) { // 订阅号
				dealSubscribeGroup2(notifyContent);
			} else if (notifyContent.indexOf("ims-pim") >= 0) {

				dealPersionInfoChanged(notifyContent);
			}
		}// end modify <add/remove/replace> application/xcap-diff+xml
	}

	private void dealPersionInfoChanged(String notification) {
		if (notification.indexOf("person-info") >= 0) {
			String mobileNumber = NgnUriUtils
					.RemoveGroupNumGetMemberNum(notification);
			String newUrl = getIconUrl(notification);

			String newBigUrl = getBigIconUrl(notification);

			// if (!mobileNumber.equals(SystemVarTools.getmIdentity())) {

			if (mobileNumber != null) {
				int Size = SystemVarTools.getContactAll().size();

				for (int i = 0; i < Size; i++) {
					if (SystemVarTools.getContactAll().get(i).mobileNo.trim()
							.endsWith(mobileNumber.trim())) {
						// 删除本地图片

						if (SystemVarTools.getContactAll().get(i).icon != null) {

							SystemVarTools.deleteIcon(SystemVarTools
									.getContactAll().get(i).icon.trim());
						}

						if (SystemVarTools.getContactAll().get(i).bigIcon != null) {
							SystemVarTools.deleteIcon(SystemVarTools
									.getContactAll().get(i).bigIcon.trim());
						}
						if (newUrl != null && !newUrl.equals("")) {
							// 赋值新的URL
							SystemVarTools.getContactAll().get(i).icon = newUrl
									.trim();
						}

						if (newBigUrl != null && !newBigUrl.equals("")) {
							SystemVarTools.getContactAll().get(i).bigIcon = newBigUrl
									.trim();
						}

						break;
					}
				}

				Size = SystemVarTools.getContactListBusinessAll().size();

				for (int i = 0; i < Size; i++) {
					if (SystemVarTools.getContactListBusinessAll().get(i).mobileNo
							.trim().endsWith(mobileNumber.trim())) {
						// 删除本地图片

						if (SystemVarTools.getContactListBusinessAll().get(i).icon != null) {

							SystemVarTools.deleteIcon(SystemVarTools
									.getContactListBusinessAll().get(i).icon
									.trim());
						}

						if (SystemVarTools.getContactListBusinessAll().get(i).bigIcon != null) {
							SystemVarTools.deleteIcon(SystemVarTools
									.getContactListBusinessAll().get(i).bigIcon
									.trim());
						}

						// 赋值新的URL
						if (newUrl != null && !newUrl.equals("")) {
							SystemVarTools.getContactListBusinessAll().get(i).icon = newUrl
									.trim();
						}
						if (newBigUrl != null && !newBigUrl.equals("")) {
							SystemVarTools.getContactListBusinessAll().get(i).bigIcon = newBigUrl
									.trim();
						}
						break;
					}
				}

			}
			// }
		}
	}

	public String getIconUrl(String notification) {

		int start = notification.indexOf("portraitUrl=")
				+ "portraitUrl=".length() + 1;

		if (start != "portraitUrl=".length()) { // 找不到portraitUrl=，indexOf返回-1

			String tempString = notification.substring(start);

			int end = tempString.indexOf(";");

			if (end != -1) {
				String result = tempString.substring(0, end);
				return result;
			}
		}
		return null;

	}

	public String getBigIconUrl(String notification) {

		int start = notification.indexOf("portraitUrl=")
				+ "portraitUrl=".length() + 1;

		if (start != "portraitUrl=".length()) { // 找到了portraitUrl=
			String tempString = notification.substring(start);

			String[] reusltStrings = tempString.split(";");

			if (reusltStrings != null) {
				if (reusltStrings[2] != null) {
					return reusltStrings[2];
				}
			}
		}
		return null;

	}

	private void dealNormalGroup2(String notifyContent) {
		if (notifyContent.indexOf("remove") >= 0) // 删除操作
		{
			if (notifyContent.indexOf("list") >= 0) // 删除组
			{
				String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);

				String groupName = null;

				List<ModelContact> normalOrg = SystemVarTools.getContactOrg();

				if (normalOrg != null) {
					for (int i = 0; i < normalOrg.size(); i++) {
						if (normalOrg.get(i).mobileNo
								.equals(groupNumber.trim())) {
							groupName = normalOrg.get(i).name;
							break;
						}

					}
				}

				delNormalGroup(groupNumber, myselfNumber);
				if (groupName != null) {
					SystemVarTools.showToast(String.format(
							NgnApplication.getContext().getString(
									R.string.group_with_colon)
									+ "\'%s\'"
									+ NgnApplication.getContext().getString(
											R.string.already_dissolve),
							groupName));
				} else {
					SystemVarTools.showToast(String.format(
							NgnApplication.getContext().getString(
									R.string.group_with_colon)
									+ "\'%s\'"
									+ NgnApplication.getContext().getString(
											R.string.already_dissolve),
							groupNumber));
				}
			} else if (notifyContent.indexOf("entry") >= 0) // 删除成员
			{
				// NgnUriUtils.setGroupAndMemberNum2(notifyContent);
				String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);
				String memberNumber = NgnUriUtils
						.RemoveGroupNumGetMemberNum(notifyContent);

				String groupName = null;

				List<ModelContact> normalOrg = SystemVarTools.getContactOrg();

				if (normalOrg != null) {
					for (int i = 0; i < normalOrg.size(); i++) {
						if (normalOrg.get(i).mobileNo
								.equals(groupNumber.trim())) {
							groupName = normalOrg.get(i).name;
							break;
						}

					}
				}

				String memberName = null;

				List<ModelContact> contactsAll = SystemVarTools.getContactAll();

				if (contactsAll != null) {
					for (int i = 0; i < contactsAll.size(); i++) {
						if (contactsAll.get(i).mobileNo.equals(memberNumber
								.trim())) {
							memberName = contactsAll.get(i).name;
							break;
						}

					}
				}

				if (memberNumber != null && memberNumber.equals(myselfNumber))// ||SystemVarTools.getContactOrgSize(groupNumber)==1)
																				// //删除的是自己
				{
					// delNormalGroup(groupNumber,myselfNumber);

					// 清除订阅记录
					for (ModelContact mc : SystemVarTools.getContactAll()) {
						ServiceContact.deleteContact(mc);
					}

					SystemVarTools.getContactAll().clear();
					SystemVarTools.getContactOrg().clear();

					if (groupName != null) {
						SystemVarTools.showToast(String.format(NgnApplication
								.getContext().getString(R.string.already_exit)
								+ "\'%s\'", groupName));
					} else {
						SystemVarTools.showToast(String.format(NgnApplication
								.getContext().getString(R.string.already_exit)
								+ "\'%s\'", groupNumber));
					}

				} else {
					if (delNormalGroupMember(memberNumber, groupNumber)) {

						if (memberName != null && groupName != null) {
							SystemVarTools
									.showToast(String
											.format("\'%s\'"
													+ NgnApplication
															.getContext()
															.getString(
																	R.string.already_exit_group)
													+ "\'%s\'", memberName,
													groupName));
						} else if (memberName == null && groupName != null) {
							SystemVarTools
									.showToast(String
											.format("\'%s\'"
													+ NgnApplication
															.getContext()
															.getString(
																	R.string.already_exit_group)
													+ "\'%s\'", memberNumber,
													groupName));
						} else if (memberName != null && groupName == null) {
							SystemVarTools
									.showToast(String
											.format("\'%s\'"
													+ NgnApplication
															.getContext()
															.getString(
																	R.string.already_exit_group)
													+ "\'%s\'", memberName,
													groupNumber));
						} else if (memberName == null && groupName == null) {
							SystemVarTools
									.showToast(String
											.format("\'%s\'"
													+ NgnApplication
															.getContext()
															.getString(
																	R.string.already_exit_group)
													+ "\'%s\'", memberNumber,
													groupNumber));
						}

					}
				}
			}

		} else if (notifyContent.indexOf("add") >= 0) // 添加组操作
		{// add
			// NgnUriUtils.setGroupAndMemberNum(notifyContent);
			String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);

			List<String> memberNumberList = NgnUriUtils
					.AddGroupNumGetMemberNum(notifyContent);

			String groupName = null;

			List<ModelContact> normalOrg = SystemVarTools.getContactOrg();

			if (normalOrg != null) {
				for (int i = 0; i < normalOrg.size(); i++) {
					if (normalOrg.get(i).mobileNo.equals(groupNumber.trim())) {
						groupName = normalOrg.get(i).name;
						break;
					}

				}
			}

			if (memberNumberList != null && memberNumberList.size() > 0) {

				for (int i = 0; i < memberNumberList.size(); i++) {
					String memberNumber = memberNumberList.get(i);

					if (memberNumber != null
							&& memberNumber.equals(myselfNumber)) // 拉入自己
																	// 我被移到了新的组里，组织架构发生变化，重新下载通讯录
					{
						addMyselfinPublicGroup();

						if (groupName != null) {
							SystemVarTools.showToast(String.format(
									NgnApplication.getContext().getString(
											R.string.already_jopined_group)
											+ "\'%s\'", groupName));
						} else {
							SystemVarTools.showToast(String.format(
									NgnApplication.getContext().getString(
											R.string.already_jopined_group)
											+ "\'%s\'", groupNumber));
						}
						return;
					}
				}
			}
			ModelContact mc = new ModelContact();
			mc.addNormalGroupMember(groupNumber, notifyContent);
		} else if (notifyContent.indexOf("replace") >= 0) // 修改组操作
		{
			ModelContact mc = new ModelContact();
			mc.replaceNormalGroupMember(notifyContent);
			SystemVarTools.showToast(NgnApplication.getContext().getString(
					R.string.modify_success));
		} else if (notifyContent.indexOf("change") >= 0) { // 跨站组网又断开又组网后，群组服务器发生通讯录合并，重新下载通讯录

			// addMyselfinPublicGroup();
			MyLog.d("change", "重新下载通讯录，发送下载通讯录广播");
			ServiceRegiste
					.sendContactStatus(ServiceRegiste.NET_DOWNLOAD_CONTACTS);

		}
	}

	private void dealServiceGroup2(String notifyContent) {
		MyLog.d("", "dealServiceGroup2()");
		if (notifyContent.indexOf("remove") >= 0) // 删除操作
		{
			MyLog.d("", "action = remove");
			if (notifyContent.indexOf("list") >= 0) // 删除组织
			{
				MyLog.d("", "target = list");
				String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);

				String groupName = null;

				List<ModelContact> ServiceOrg = SystemVarTools
						.getContactListBusinessOrg();

				if (ServiceOrg != null) {
					for (int i = 0; i < ServiceOrg.size(); i++) {
						if (ServiceOrg.get(i).mobileNo.equals(groupNumber
								.trim())) {
							groupName = ServiceOrg.get(i).name;
							break;
						}

					}
				}

				delBusinessGroup(groupNumber);

				if (groupName != null) {
					SystemVarTools.showToast(String.format(
							NgnApplication.getContext().getString(
									R.string.business_group_with_colon)
									+ "\'%s\' "
									+ NgnApplication.getContext().getString(
											R.string.already_dissolve),
							groupName));
				} else {
					SystemVarTools.showToast(String.format(
							NgnApplication.getContext().getString(
									R.string.business_group_with_colon)
									+ "\'%s\' "
									+ NgnApplication.getContext().getString(
											R.string.already_dissolve),
							groupNumber));
				}
			} else {
				// NgnUriUtils.setGroupAndMemberNum(notifyContent);
				// String groupNumber = NgnUriUtils.getGroupNum();
				// String memberNumber = NgnUriUtils.getMemberNum();
				MyLog.d("", "target = entry");
				String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);
				String memberNumber = NgnUriUtils
						.RemoveGroupNumGetMemberNum(notifyContent);

				String groupName = null;

				List<ModelContact> serviceOrg = SystemVarTools
						.getContactListBusinessOrg();

				if (serviceOrg != null) {
					for (int i = 0; i < serviceOrg.size(); i++) {
						if (serviceOrg.get(i).mobileNo.equals(groupNumber
								.trim())) {
							groupName = serviceOrg.get(i).name;
							break;
						}

					}
				}

				String memberName = null;

				List<ModelContact> serviceAll = SystemVarTools
						.getContactListBusinessAll();

				if (serviceAll != null) {
					for (int i = 0; i < serviceAll.size(); i++) {
						if (serviceAll.get(i).mobileNo.equals(memberNumber
								.trim())) {
							memberName = serviceAll.get(i).name;
							break;
						}

					}
				}

				if (memberNumber != null && memberNumber.equals(myselfNumber)) // 删除的是自己
				{
					delBusinessGroup(groupNumber);
					if (groupName != null) {
						SystemVarTools.showToast(String.format(
								NgnApplication.getContext().getString(
										R.string.already_exit_business_group)
										+ "\'%s\'", groupName));
					} else {
						SystemVarTools.showToast(String.format(
								NgnApplication.getContext().getString(
										R.string.already_exit_business_group)
										+ "\'%s\'", groupNumber));
					}

				} else {
					delBusinessMember(groupNumber, memberNumber);

					if (memberName != null && groupName != null) {
						SystemVarTools
								.showToast(String
										.format("\'%s\'"
												+ NgnApplication
														.getContext()
														.getString(
																R.string.already_exit_business_group2)
												+ "\'%s\'", memberName,
												groupName));
					} else if (memberName == null && groupName != null) {
						SystemVarTools
								.showToast(String
										.format("\'%s\'"
												+ NgnApplication
														.getContext()
														.getString(
																R.string.already_exit_business_group2)
												+ "\'%s\'", memberNumber,
												groupName));
					} else if (memberName != null && groupName == null) {
						SystemVarTools
								.showToast(String
										.format("\'%s\'"
												+ NgnApplication
														.getContext()
														.getString(
																R.string.already_exit_business_group2)
												+ "\'%s\'", memberName,
												groupNumber));
					} else if (memberName == null && groupName == null) {
						SystemVarTools
								.showToast(String
										.format("\'%s\'"
												+ NgnApplication
														.getContext()
														.getString(
																R.string.already_exit_business_group2)
												+ "\'%s\'", memberNumber,
												groupNumber));
					}

				}
			}

		} else if (notifyContent.indexOf("add") >= 0) {// add
														// if(notifyContent.indexOf("list")>=0)
			// {
			// ModelContact mc = new ModelContact();
			// mc.addBusinessGroupMember(notifyContent);
			// }
			// else
			// {
			// NgnUriUtils.setGroupAndMemberNum(notifyContent);
			// String groupNumber = NgnUriUtils.getGroupNum();
			// String memberNumber = NgnUriUtils.getMemberNum();

			String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);
			List<String> memberNumberList = NgnUriUtils
					.AddGroupNumGetMemberNum(notifyContent);

			String groupName = null;

			List<ModelContact> serviceOrg = SystemVarTools
					.getContactListBusinessOrg();

			if (serviceOrg != null) {
				for (int i = 0; i < serviceOrg.size(); i++) {
					if (serviceOrg.get(i).mobileNo.equals(groupNumber.trim())) {
						groupName = serviceOrg.get(i).name;
						break;
					}

				}
			}

			if (memberNumberList != null) {
				for (int i = 0; i < memberNumberList.size(); i++) {
					String memberNumber = memberNumberList.get(i);
					if (memberNumber != null
							&& memberNumber.equals(myselfNumber)) {
						addMyselfinServiceGroup();
						if (groupName != null) {
							SystemVarTools
									.showToast(String
											.format(NgnApplication
													.getContext()
													.getString(
															R.string.already_joined_business_group)
													+ "\'%s\'", groupName));
						} else {
							SystemVarTools
									.showToast(String
											.format(NgnApplication
													.getContext()
													.getString(
															R.string.already_joined_business_group)
													+ "\'%s\'", groupNumber));
						}
						return;
					}

					ModelContact mc = new ModelContact();
					mc.addBusinessGroupMember(groupNumber, memberNumber,
							notifyContent);
				}
			}
			// }

		}// end add
		else if (notifyContent.indexOf("replace") >= 0) {
			ModelContact mc = new ModelContact();
			mc.replaceBusinessGroupMember(notifyContent);
			SystemVarTools.showToast(NgnApplication.getContext().getString(
					R.string.modify_success));
		} else if (notifyContent.indexOf("change") >= 0) {

			// addMyselfinServiceGroup();

		}
		// if (!GlobalSession.bSocketService) {
		// ((Engine) Engine.getInstance()).getScreenService().show(
		// ScreenTabHome.class);
		// }

	}

	private void dealGlobalGroup2(String notifyContent) {
		if (notifyContent.indexOf("remove") >= 0) // 删除操作
		{
			if (notifyContent.indexOf("list") >= 0) // 删除组织
			{
				String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);

				String groupName = null;

				List<ModelContact> globalOrg = SystemVarTools
						.getContactListGlobalGroupOrg();

				if (globalOrg != null) {
					for (int i = 0; i < globalOrg.size(); i++) {
						if (globalOrg.get(i).mobileNo
								.equals(groupNumber.trim())) {
							groupName = globalOrg.get(i).name;
							break;
						}

					}
				}

				delGlobalGroup(groupNumber);

				if (groupName != null) {
					SystemVarTools.showToast(String.format(
							NgnApplication.getContext().getString(
									R.string.global_group_with_colon)
									+ "\'%s\'"
									+ NgnApplication.getContext().getString(
											R.string.already_dissolve),
							groupName));
				} else {
					SystemVarTools.showToast(String.format(
							NgnApplication.getContext().getString(
									R.string.global_group_with_colon)
									+ "\'%s\'"
									+ NgnApplication.getContext().getString(
											R.string.already_dissolve),
							groupNumber));
				}
			} else {
				// NgnUriUtils.setGroupAndMemberNum(notifyContent);
				// String groupNumber = NgnUriUtils.getGroupNum();
				// String memberNumber = NgnUriUtils.getMemberNum();
				String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);
				String memberNumber = NgnUriUtils
						.RemoveGroupNumGetMemberNum(notifyContent);

				String groupName = null;

				List<ModelContact> globalOrg = SystemVarTools
						.getContactListGlobalGroupOrg();

				if (globalOrg != null) {
					for (int i = 0; i < globalOrg.size(); i++) {
						if (globalOrg.get(i).mobileNo
								.equals(groupNumber.trim())) {
							groupName = globalOrg.get(i).name;
							break;
						}

					}
				}

				String memberName = null;

				List<ModelContact> serviceAll = SystemVarTools
						.getContactListBusinessAll();

				if (serviceAll != null) {
					for (int i = 0; i < serviceAll.size(); i++) {
						if (serviceAll.get(i).mobileNo.equals(memberNumber
								.trim())) {
							memberName = serviceAll.get(i).name;
							break;
						}

					}
				}

				if (memberNumber != null && memberNumber.equals(myselfNumber)) // 删除的是自己
				{
					delGlobalGroup(groupNumber);
					if (groupName != null) {
						SystemVarTools.showToast(String.format(
								NgnApplication.getContext().getString(
										R.string.already_exit_global_group)
										+ "\'%s\'", groupName));
					} else {
						SystemVarTools.showToast(String.format(
								NgnApplication.getContext().getString(
										R.string.already_exit_global_group)
										+ "\'%s\'", groupNumber));
					}
				} else {
					delGlobalMember(groupNumber, memberNumber);
					if (memberName != null && groupName != null) {
						SystemVarTools
								.showToast(String
										.format("\'%s\'"
												+ NgnApplication
														.getContext()
														.getString(
																R.string.already_exit_global_group2)
												+ "\'%s\'", memberName,
												groupName));
					} else if (memberName == null && groupName != null) {
						SystemVarTools
								.showToast(String
										.format("\'%s\'"
												+ NgnApplication
														.getContext()
														.getString(
																R.string.already_exit_global_group2)
												+ "\'%s\'", memberNumber,
												groupName));
					} else if (memberName != null && groupName == null) {
						SystemVarTools
								.showToast(String
										.format("\'%s\'"
												+ NgnApplication
														.getContext()
														.getString(
																R.string.already_exit_global_group2)
												+ "\'%s\'", memberName,
												groupNumber));
					} else if (memberName == null && groupName == null) {
						SystemVarTools
								.showToast(String
										.format("\'%s\'"
												+ NgnApplication
														.getContext()
														.getString(
																R.string.already_exit_global_group2)
												+ "\'%s\'", memberNumber,
												groupNumber));
					}
				}
			}

		} else if (notifyContent.indexOf("add") >= 0) {// add
														// if(notifyContent.indexOf("list")>=0)
			// {
			// ModelContact mc = new ModelContact();
			// mc.addBusinessGroupMember(notifyContent);
			// }
			// else
			// {
			// NgnUriUtils.setGroupAndMemberNum(notifyContent);
			// String groupNumber = NgnUriUtils.getGroupNum();
			// String memberNumber = NgnUriUtils.getMemberNum();

			String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);
			List<String> memberNumberList = NgnUriUtils
					.AddGroupNumGetMemberNum(notifyContent);

			String groupName = null;

			List<ModelContact> globalOrg = SystemVarTools
					.getContactListGlobalGroupOrg();

			if (globalOrg != null) {
				for (int i = 0; i < globalOrg.size(); i++) {
					if (globalOrg.get(i).mobileNo.equals(groupNumber.trim())) {
						groupName = globalOrg.get(i).name;
						break;
					}

				}
			}

			if (memberNumberList != null) {
				for (int i = 0; i < memberNumberList.size(); i++) {
					String memberNumber = memberNumberList.get(i);
					if (memberNumber != null
							&& memberNumber.equals(myselfNumber)) {
						// addMyselfinServiceGroup();
						addMyselfinGlobalGroup();

						if (groupName != null) {
							SystemVarTools
									.showToast(String
											.format(NgnApplication
													.getContext()
													.getString(
															R.string.already_joined_global_group)
													+ "\'%s\'", groupName));
						} else {
							SystemVarTools
									.showToast(String
											.format(NgnApplication
													.getContext()
													.getString(
															R.string.already_joined_global_group)
													+ "\'%s\'", groupNumber));
						}
						return;
					}

				}
			} else {
				MyLog.d("", "memberNumberList is null.");
			}
			ModelContact mc = new ModelContact();
			mc.addGlobalGroupMember(groupNumber, notifyContent);
			// }

		}// end add
		else if (notifyContent.indexOf("replace") >= 0) {
			ModelContact mc = new ModelContact();
			mc.replaceGlobalGroupMember(notifyContent);
			SystemVarTools.showToast(NgnApplication.getContext().getString(
					R.string.modify_success));
		} else if (notifyContent.indexOf("change") >= 0) {

			// addMyselfinGlobalGroup();

		}
		// if (!GlobalSession.bSocketService) {
		// ((Engine) Engine.getInstance()).getScreenService().show(
		// ScreenTabHome.class);
		// }

	}

	private void dealSubscribeGroup2(String notifyContent) {
		if (notifyContent.indexOf("remove") >= 0) // 删除操作
		{
			if (notifyContent.indexOf("list") >= 0) // 订阅号被删除
			{
				String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);

				String groupName = null;

				List<ModelContact> subscribeOrg = SystemVarTools
						.getContactListSubscribeGroupOrg();

				if (subscribeOrg != null) {
					for (int i = 0; i < subscribeOrg.size(); i++) {
						if (subscribeOrg.get(i).mobileNo.equals(groupNumber
								.trim())) {
							groupName = subscribeOrg.get(i).name;
							break;
						}

					}
				}

				delSubscribeGroup(groupNumber);

				if (groupName != null) {
					SystemVarTools
							.showToast(String
									.format(NgnApplication
											.getContext()
											.getString(
													R.string.subcrible_with_colon)
											+ "\'%s\'"
											+ NgnApplication
													.getContext()
													.getString(
															R.string.already_cancel),
											groupName));
				} else {
					SystemVarTools.showToast(String.format(
							NgnApplication.getContext().getString(
									R.string.subcrible_with_colon)
									+ "\'%s\'"
									+ NgnApplication.getContext().getString(
											R.string.already_cancel),
							groupNumber));
				}
			} else {
				String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);
				String memberNumber = NgnUriUtils
						.RemoveGroupNumGetMemberNum(notifyContent);

				String groupName = null;

				List<ModelContact> subscribeOrg = SystemVarTools
						.getContactListSubscribeGroupOrg();

				if (subscribeOrg != null) {
					for (int i = 0; i < subscribeOrg.size(); i++) {
						if (subscribeOrg.get(i).mobileNo.equals(groupNumber
								.trim())) {
							groupName = subscribeOrg.get(i).name;
							break;
						}

					}
				}

				if (memberNumber != null && memberNumber.equals(myselfNumber)) // 删除的是自己
				{
					delSubscribeGroup(groupNumber);
					if (groupName != null) {
						SystemVarTools.showToast(String.format(
								NgnApplication.getContext().getString(
										R.string.already_cancel_subscrible)
										+ "\'%s\'", groupName));
					} else {
						SystemVarTools.showToast(String.format(
								NgnApplication.getContext().getString(
										R.string.already_cancel_subscrible)
										+ "\'%s\'", groupNumber));
					}
				}
			}

		} else if (notifyContent.indexOf("add") >= 0) { // 被加入新的订阅号

			String groupNumber = NgnUriUtils.GetGroupNum2(notifyContent);
			List<String> memberNumberList = NgnUriUtils
					.AddGroupNumGetMemberNum(notifyContent);
			String groupName = null;
			List<ModelContact> subscribeOrg = SystemVarTools
					.getContactListSubscribeGroupOrg();
			if (subscribeOrg != null) {
				for (int i = 0; i < subscribeOrg.size(); i++) {
					if (subscribeOrg.get(i).mobileNo.equals(groupNumber.trim())) {
						groupName = subscribeOrg.get(i).name;
						break;
					}

				}
			}

			if (memberNumberList != null) {
				for (int i = 0; i < memberNumberList.size(); i++) {
					String memberNumber = memberNumberList.get(i);

					if (memberNumber != null
							&& memberNumber.equals(myselfNumber)) {
						addMyselfinSubscribeGroup();

						if (groupName != null) {
							SystemVarTools
									.showToast(String
											.format(NgnApplication
													.getContext()
													.getString(
															R.string.already_subscrible_subscrible)
													+ "\'%s\'", groupName));
						} else {
							SystemVarTools
									.showToast(String
											.format(NgnApplication
													.getContext()
													.getString(
															R.string.already_subscrible_subscrible)
													+ "\'%s\'", groupNumber));
						}
						return;
					}
				}
			}

		}

		if (!GlobalSession.bSocketService) {
			((Engine) Engine.getInstance()).getScreenService().show(
					ScreenTabHome.class);
		}

	}

	private static void addMyselfinPublicGroup() {
		Thread addmeThread = new Thread("MSG_DOWNLOAD_CONTACTS-下载联系人") { // 获取不到通讯录；
																			// android.os.NetworkOnMainThreadException
			public void run() {

				Log.d("ServiceSocketMode--onReceive()",
						" MSG_DOWNLOAD_CONTACTS-下载联系人");
				if (ScreenDownloadConcacts.getInstance().downloadPublicGroup()) {
					// 适配软件获取通讯录
					MyLog.d("addMyselfinPublicGroup",
							" MSG_DOWNLOAD_CONTACTS_FINISH-下载联系人完成");
					List<NodeResource> resList = ScreenDownloadConcacts
							.getInstance().parserContactsTree();
					SystemVarTools.setContactAll(resList);

					ServiceContact.sendContactFrashMsg();

					if (GlobalSession.bSocketService) { // 大终端下载完通讯录重新上传通讯录
						if (ServiceSocketMode.contactRefreshhandler != null)
							ServiceSocketMode.contactRefreshhandler
									.obtainMessage(
											ServiceSocketMode.CONTACTREFRESHMESSAGE)
									.sendToTarget();
					}

				}
			}
		};
		addmeThread.start();
	}

	private static void addMyselfinServiceGroup() {
		Thread addmeThread = new Thread("MSG_DOWNLOAD_CONTACTS-下载联系人") { // 获取不到通讯录；
																			// android.os.NetworkOnMainThreadException
			public void run() {

				Log.d("ServiceSocketMode--onReceive()",
						" MSG_DOWNLOAD_CONTACTS-下载联系人");

				ServiceContact.clearBusinessGroup();

				if (ScreenDownloadConcacts.getInstance().downloadServiceGroup()) {
					// 适配软件获取通讯录
					Log.d("ServiceSocketMode--onReceive()",
							" MSG_DOWNLOAD_CONTACTS_FINISH-下载联系人完成");
					List<NodeResource> resList = ScreenDownloadConcacts
							.getInstance().parserContactsNetTree();
					SystemVarTools.setContactBussiness(resList);

					ServiceContact.sendContactFrashMsg();

					if (GlobalSession.bSocketService) { // 大终端下载完通讯录重新上传通讯录
						if (ServiceSocketMode.contactRefreshhandler != null)
							ServiceSocketMode.contactRefreshhandler
									.obtainMessage(
											ServiceSocketMode.CONTACTREFRESHMESSAGE)
									.sendToTarget();
					}

				}
			}
		};
		addmeThread.start();
	}

	private static void addMyselfinGlobalGroup() {
		Thread addmeThread = new Thread("MSG_DOWNLOAD_CONTACTS-下载联系人") { // 获取不到通讯录；
																			// android.os.NetworkOnMainThreadException
			public void run() {

				Log.d("ServiceSocketMode--onReceive()",
						" MSG_DOWNLOAD_CONTACTS-下载联系人");
				if (ScreenDownloadConcacts.getInstance().downloadGlobalGroup()) {
					// 适配软件获取通讯录
					Log.d("ServiceSocketMode--onReceive()",
							" MSG_DOWNLOAD_CONTACTS_FINISH-下载联系人完成");
					List<NodeResource> resList = ScreenDownloadConcacts
							.getInstance().parserContactsGlobalGroupTree();
					SystemVarTools.setContactGlobalGroupOrg(resList);

					ServiceContact.sendContactFrashMsg();

					if (GlobalSession.bSocketService) { // 大终端下载完通讯录重新上传通讯录
						if (ServiceSocketMode.contactRefreshhandler != null)
							ServiceSocketMode.contactRefreshhandler
									.obtainMessage(
											ServiceSocketMode.CONTACTREFRESHMESSAGE)
									.sendToTarget();
					}

				}
			}
		};
		addmeThread.start();
	}

	private static void addMyselfinSubscribeGroup() {
		Thread addmeThread = new Thread("MSG_DOWNLOAD_CONTACTS-下载联系人") { // 获取不到通讯录；
																			// android.os.NetworkOnMainThreadException
			public void run() {

				Log.d("ServiceSocketMode--onReceive()",
						" MSG_DOWNLOAD_CONTACTS-下载联系人");
				if (ScreenDownloadConcacts.getInstance()
						.downloadSubscribeGroup()) {
					// 适配软件获取通讯录
					Log.d("ServiceSocketMode--onReceive()",
							" MSG_DOWNLOAD_CONTACTS_FINISH-下载联系人完成");
					List<NodeResource> resList = ScreenDownloadConcacts
							.getInstance().parserContactsSubscribeGroupTree();
					SystemVarTools.setContactSubscribeGroupOrg(resList);

					ServiceContact.sendContactFrashMsg();
				}
			}
		};
		addmeThread.start();
	}

	private static void replaceContactMember(String phoneNumber,
			String notifyContent) {
		// replace
		int number = SystemVarTools.getContactAll().size();
		for (int i = 0; i < number; ++i) {
			if (SystemVarTools.getContactAll().get(i).mobileNo
					.equals(phoneNumber)) {
				SystemVarTools.getContactAll().get(i)
						.replaceProperity(notifyContent);
				return;
			}
		}
		if (notifyContent.indexOf("replaceg") >= 0) {// replace
			for (int i = 0; i < SystemVarTools.getContactOrg().size(); ++i) {
				if (SystemVarTools.getContactOrg().get(i).mobileNo
						.equals(phoneNumber)) {
					SystemVarTools.getContactOrg().get(i)
							.replaceProperity(notifyContent);
					return;
				}
			}
		}

	}

	private static void delBusinessGroup(String groupNumber) {
		MyLog.d("", "delBusinessGroup(" + groupNumber + ")");

		List<ModelContact> BusOrgAll = SystemVarTools
				.getContactListBusinessOrg();
		for (int i = 0; i < BusOrgAll.size(); ++i) {

			String no = BusOrgAll.get(i).mobileNo;
			MyLog.d("", "orgMobileNo = " + BusOrgAll.get(i).mobileNo);
			if (no != null && no.equals(groupNumber)) {
				BusOrgAll.remove(i);
				i--;
			}

		}
		// 对于这个组中的所有成员，是否存在其它组中，若在，则绝对人数不变，否则-1
		String memberNumbers = null;
		List<ModelContact> BusAll = SystemVarTools.getContactListBusinessAll();
		for (int i = 0; i < BusAll.size(); ++i) {
			String org = BusAll.get(i).org;
			MyLog.d("", BusAll.get(i).mobileNo + "'s org = " + org);
			if (org != null && org.equals(groupNumber)) {
				memberNumbers = BusAll.get(i).mobileNo + "," + memberNumbers;
				ServiceContact.deleteContact(BusAll.get(i));
				BusAll.remove(i);
				i--;
			}
		}
		MyLog.d("", "memberNumbers = " + memberNumbers);
		if (memberNumbers == null || memberNumbers.isEmpty())
			return;
		String[] memberNumberStr = memberNumbers.split(",");
		if (memberNumberStr == null || memberNumberStr.toString().equals("")
				|| memberNumberStr.length < 0)
			return;// 这个组中的所有成员

		int nSize = memberNumberStr.length;
		for (int j = 0; j < nSize; ++j) {
			int k = 0;
			int allMemberSize = SystemVarTools.getContactListBusinessAll()
					.size();
			for (k = 0; k < allMemberSize; ++k) {
				String no = SystemVarTools.getContactListBusinessAll().get(k).mobileNo;
				if (no != null && no.equals(memberNumberStr[j]))
					break;
			}
			if (k == allMemberSize)
				SystemVarTools.decContactListBusinessAllNumber();
		}
	}

	private static void delGlobalGroup(String groupNumber) {
		for (int i = 0; i < SystemVarTools.getContactListGlobalGroupOrg()
				.size(); ++i) {
			if (SystemVarTools.getContactListGlobalGroupOrg().get(i).mobileNo
					.equals(groupNumber)) {
				SystemVarTools.getContactListGlobalGroupOrg().remove(i);
				i--;
			}

		}
		// 对于这个组中的所有成员，是否存在其它组中，若在，则绝对人数不变，否则-1
		String memberNumbers = null;
		for (int i = 0; i < SystemVarTools.getContactListGlobalGroupAll()
				.size(); ++i) {
			String no = SystemVarTools.getContactListGlobalGroupAll().get(i).parent;
			if (no != null && no.equals(groupNumber)) {
				memberNumbers = SystemVarTools.getContactListGlobalGroupAll()
						.get(i).mobileNo + "," + memberNumbers;
				SystemVarTools.getContactListGlobalGroupAll().remove(i);
				i--;
			}
		}
		if (memberNumbers == null || memberNumbers.isEmpty())
			return;
		String[] memberNumberStr = memberNumbers.split(",");
		if (memberNumberStr == null || memberNumberStr.toString().equals("")
				|| memberNumberStr.length < 0)
			return;// 这个组中的所有成员

		int nSize = memberNumberStr.length;
		for (int j = 0; j < nSize; ++j) {
			int k = 0;
			int allMemberSize = SystemVarTools.getContactListGlobalGroupAll()
					.size();
			for (k = 0; k < allMemberSize; ++k) {
				if (SystemVarTools.getContactListGlobalGroupAll().get(k).mobileNo
						.equals(memberNumberStr[j]))
					break;
			}
			if (k == allMemberSize)
				SystemVarTools.decContactListBusinessAllNumber();
		}
	}

	private static void delSubscribeGroup(String groupNumber) {
		for (int i = 0; i < SystemVarTools.getContactListSubscribeGroupOrg()
				.size(); ++i) {
			String no = SystemVarTools.getContactListSubscribeGroupOrg().get(i).mobileNo;
			if (no != null && no.equals(groupNumber)) {
				SystemVarTools.getContactListSubscribeGroupOrg().remove(i);
				i--;
			}

		}
	}

	/**
	 * 删除业务组用户
	 * 
	 * @param groupNumber
	 * @param memberNumber
	 */
	private static void delBusinessMember(String groupNumber,
			String memberNumber) {
		MyLog.d("", "delBusinessMember()");
		String memberNumbers = null;
		for (int i = 0; i < SystemVarTools.getContactListBusinessAll().size(); ++i) {
			String no = SystemVarTools.getContactListBusinessAll().get(i).mobileNo;
			if (no != null
					&& no.equals(memberNumber)
					&& SystemVarTools.getContactListBusinessAll().get(i).parent
							.equals(groupNumber)) {
				memberNumbers = SystemVarTools.getContactListBusinessAll().get(
						i).mobileNo
						+ "," + memberNumbers;
				ServiceContact.deleteContact(SystemVarTools
						.getContactListBusinessAll().get(i));

				SystemVarTools.getContactListBusinessAll().remove(i);
				i--;

			}
		}
		if (memberNumbers == null || memberNumbers.isEmpty())
			return;
		String[] memberNumberStr = memberNumbers.split(",");
		if (memberNumberStr == null || memberNumberStr.toString().equals("")
				|| memberNumberStr.length < 0)
			return;// 这个组中的所有成员

		int nSize = memberNumberStr.length;
		if (nSize == 1) {
			for (int i = 0; i < SystemVarTools.getContactListBusinessOrg()
					.size(); ++i) {
				String no = SystemVarTools.getContactListBusinessOrg().get(i).mobileNo;
				if (no != null && no.equals(groupNumber)) {
					SystemVarTools.getContactListBusinessOrg().remove(i);
					i--;
				}

			}
		}

		for (int j = 0; j < nSize; ++j) {
			int k = 0;
			int allMemberSize = SystemVarTools.getContactListBusinessAll()
					.size();
			for (k = 0; k < allMemberSize; ++k) {
				String no = SystemVarTools.getContactListBusinessAll().get(k).mobileNo;
				if (no != null && no.equals(memberNumberStr[j]))
					break;
			}
			if (k == allMemberSize)
				SystemVarTools.decContactListBusinessAllNumber();
		}

	}

	private static void delGlobalMember(String groupNumber, String memberNumber) {
		String memberNumbers = null;
		for (int i = 0; i < SystemVarTools.getContactListGlobalGroupAll()
				.size(); ++i) {
			String no = SystemVarTools.getContactListGlobalGroupAll().get(i).mobileNo;
			if (no != null
					&& no.equals(memberNumber)
					&& SystemVarTools.getContactListGlobalGroupAll().get(i).parent
							.equals(groupNumber)) {
				memberNumbers = SystemVarTools.getContactListGlobalGroupAll()
						.get(i).mobileNo + "," + memberNumbers;
				ServiceContact.deleteContact(SystemVarTools
						.getContactListGlobalGroupAll().get(i));
				SystemVarTools.getContactListGlobalGroupAll().remove(i);
				i--;
			}
		}
		if (memberNumbers == null || memberNumbers.isEmpty())
			return;
		String[] memberNumberStr = memberNumbers.split(",");
		if (memberNumberStr == null || memberNumberStr.toString().equals("")
				|| memberNumberStr.length < 0)
			return;// 这个组中的所有成员

		int nSize = memberNumberStr.length;
		if (nSize == 1) {
			for (int i = 0; i < SystemVarTools.getContactListGlobalGroupOrg()
					.size(); ++i) {
				String no = SystemVarTools.getContactListGlobalGroupOrg()
						.get(i).mobileNo;
				if (no != null && no.equals(groupNumber)) {
					SystemVarTools.getContactListGlobalGroupOrg().remove(i);
					i--;
				}

			}
		}

		for (int j = 0; j < nSize; ++j) {
			int k = 0;
			int allMemberSize = SystemVarTools.getContactListGlobalGroupAll()
					.size();
			for (k = 0; k < allMemberSize; ++k) {
				String no = SystemVarTools.getContactListGlobalGroupAll()
						.get(k).mobileNo;
				if (no != null && no.equals(memberNumberStr[j]))
					break;
			}
			if (k == allMemberSize)
				SystemVarTools.decContactListBusinessAllNumber();
		}

	}

	private void delNormalGroup(String groupNumber, String myselfNumber) {
		// String groupIndex = null;
		// for (int i = 0; i < SystemVarTools.getContactOrg().size(); ++i) {
		// String no = SystemVarTools.getContactOrg().get(i).mobileNo;
		// if (no != null && no.equals(groupNumber))//
		// ||contactListOrg.get(i).org.equals(orgNums[j]))
		// {
		// groupIndex = SystemVarTools.getContactOrg().get(i).index;
		// }
		// }
		for (int i = 0; i < SystemVarTools.getContactOrg().size(); ++i) {
			String no = SystemVarTools.getContactOrg().get(i).mobileNo;
			if (no != null && no.equals(groupNumber))// ||contactListOrg.get(i).org.equals(orgNums[j]))
			{
				SystemVarTools.getContactOrg().remove(i);
				--i;
			}
		}
		StringBuffer orgNum = new StringBuffer(groupNumber + ",");
		recursiveDelGroup(orgNum, myselfNumber);

		// SystemVarTools.showToast("群组："+groupNumber+"已解散！");
	}

	private boolean delNormalGroupMember(String phoneNumber, String groupNumber) {
		for (int i = 0; i < SystemVarTools.getContactAll().size(); ++i) {
			String no = SystemVarTools.getContactAll().get(i).mobileNo;
			if (no != null && no.equals(phoneNumber)) {
				// 清除被删除用户订阅记录
				ServiceContact.deleteContact(SystemVarTools.getContactAll()
						.get(i));
				SystemVarTools.getContactAll().remove(i);
				return true;
				// SystemVarTools.showToast(phoneNumber+"已退出群组："+groupNumber);
			}
		}
		return false;
	}

	private void updateContactListState(String notifyContent) {
		String phoneNumber = NgnUriUtils.getRemoteNumber(notifyContent);
		boolean isOnline = NgnUriUtils.getContactIsOnline(notifyContent);

		if (ServiceContact.mContactAll.keySet().contains(phoneNumber)) {
			ServiceContact.mContactAll.get(phoneNumber).contact.isOnline = isOnline;
			MyLog.d(TAG, phoneNumber + " isOnline: " + isOnline);
		}

		/*
		 * int size = SystemVarTools.getContactAll().size(); for (int i = 0; i <
		 * size; i++) { if (SystemVarTools.getContactAll().get(i) != null &&
		 * SystemVarTools.getContactAll().get(i).mobileNo != null &&
		 * SystemVarTools.getContactAll().get(i).mobileNo
		 * .equalsIgnoreCase(phoneNumber)) {
		 * SystemVarTools.getContactAll().get(i).isOnline = isOnline;
		 * 
		 * MyLog.d("updateContactListState", SystemVarTools
		 * .getContactAll().get(i).name + "" + " " + (isOnline ? "online" :
		 * "offline"));
		 * 
		 * // 动态维护在线用户列表 if (isOnline) { if
		 * (!ScreenMap.contactMapOnLine.containsKey(phoneNumber)) {
		 * SystemVarTools.getContactAllOnLine().add(
		 * SystemVarTools.getContactAll().get(i));
		 * ScreenMap.contactMapOnLine.put(phoneNumber, null); } } else { if
		 * (ScreenMap.contactMapOnLine.containsKey(phoneNumber)) { int count =
		 * SystemVarTools.getContactAllOnLine().size(); if (count > 0) { for
		 * (int j = 0; j < count; j++) { ModelContact mc = SystemVarTools
		 * .getContactAllOnLine().get(j); if (mc.mobileNo.equals(phoneNumber)) {
		 * SystemVarTools.getContactAllOnLine() .remove(j); break; } } }
		 * ScreenMap.contactMapOnLine.remove(phoneNumber); } }
		 * 
		 * break; } } size = SystemVarTools.getContactOrg().size(); for (int i =
		 * 0; i < size; i++) { if (SystemVarTools.getContactOrg().get(i) != null
		 * && SystemVarTools.getContactOrg().get(i).mobileNo != null &&
		 * SystemVarTools.getContactOrg().get(i).mobileNo
		 * .equalsIgnoreCase(phoneNumber)) {
		 * SystemVarTools.getContactOrg().get(i).isOnline = isOnline; break; } }
		 * 
		 * size = SystemVarTools.getContactRecent().size(); for (int i = 0; i <
		 * size; i++) { if (SystemVarTools.getContactRecent().get(i) != null &&
		 * SystemVarTools.getContactRecent().get(i).mobileNo != null &&
		 * SystemVarTools.getContactRecent().get(i).mobileNo
		 * .equalsIgnoreCase(phoneNumber)) {
		 * SystemVarTools.getContactRecent().get(i).isOnline = isOnline; break;
		 * } }
		 * 
		 * // 业务组的呈现 size = SystemVarTools.getContactListBusinessAll().size();
		 * for (int i = 0; i < size; i++) { if
		 * (SystemVarTools.getContactListBusinessAll().get(i) != null &&
		 * SystemVarTools.getContactListBusinessAll().get(i).mobileNo != null &&
		 * SystemVarTools.getContactListBusinessAll().get(i).mobileNo
		 * .equalsIgnoreCase(phoneNumber)) {
		 * SystemVarTools.getContactListBusinessAll().get(i).isOnline =
		 * isOnline; // break; } }
		 * 
		 * size = SystemVarTools.getContactListGlobalGroupAll().size(); for (int
		 * i = 0; i < size; i++) { if
		 * (SystemVarTools.getContactListGlobalGroupAll().get(i) != null &&
		 * SystemVarTools.getContactListGlobalGroupAll().get(i).mobileNo != null
		 * && SystemVarTools.getContactListGlobalGroupAll().get(i).mobileNo
		 * .equalsIgnoreCase(phoneNumber)) {
		 * SystemVarTools.getContactListGlobalGroupAll().get(i).isOnline =
		 * isOnline; // break; } }
		 * 
		 * SystemVarTools.reSetContactList();
		 */
	}

	// public static void inviteUsertoJoinOtherGroup(String
	// notifyContent)//String to,
	// {
	// // String userNum = NgnUriUtils.getUserName(to);//
	// InviteUsertoJoinOtherGroup = true;
	// // if(notifyContent.contains("servicegroup-invite-notify"))
	// // return;
	// String groupNum = NgnUriUtils.getGroupUri(notifyContent);
	// String mIdentity = getIdentity();
	// String mNetworkRealm = getNetworkRealm();
	// final NgnSubscriptionSession subscriptionSession = NgnSubscriptionSession
	// .createOutgoingSession(
	// Engine.getInstance().getSipService().getSipStack(),
	// "sip:" + mIdentity + "@"+ mNetworkRealm,
	// "sip:" + groupNum +"@" +
	// mNetworkRealm,NgnSubscriptionSession.EventPackageType.Group);
	// subscriptionSession.subscribe();
	// SystemVarTools.showToast("您被管理员加入群组："+groupNum);
	// }
}
