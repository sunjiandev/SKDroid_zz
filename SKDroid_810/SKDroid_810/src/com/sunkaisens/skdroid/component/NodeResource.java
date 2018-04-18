package com.sunkaisens.skdroid.component;

public class NodeResource {
	public String getIndex() {
		return index;
	}

	public String getSuperIndex() {
		return superIndex;
	}

	public String getName() {
		return name;
	}

	public String getUri() {
		return uri;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getNumber() {
		return number;
	}

	public boolean getIsGroup() {
		return isGroup;
	}

	public int getIconId() {
		return iconId;
	}

	public boolean hasCheckBox() {
		return hasCheckBox;
	}

	//
	public void setIndex(String index) {
		this.index = index;
	}

	public void setSuperIndex(String superIndex) {
		this.superIndex = superIndex;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getIcon() {
		return this.icon;
	}

	public void setBigIcon(String icon) {
		this.bigIcon = icon;
	}

	public String getBigIcon() {
		return this.bigIcon;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void setIsGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	public void setHasCheckBox(boolean hasCheckBox) {
		this.hasCheckBox = hasCheckBox;
	}

	protected String index;
	protected String superIndex;
	protected String name;
	protected String uri;
	protected String displayName;
	protected String number;

	protected boolean isGroup;
	protected int iconId;

	protected String icon;
	protected String bigIcon;

	protected String userType;

	protected String bussinessType;

	public String getBussinessType() {
		return bussinessType;
	}

	public void setBussinessType(String bussinessType) {
		this.bussinessType = bussinessType;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	protected boolean hasCheckBox;

	public NodeResource(String index, String superIndex, String name,
			String uri, String displayName, String number, boolean isGroup,
			int iconId, boolean hasCheckBox, String icon, String bigicon) {
		super();
		this.index = index;
		this.superIndex = superIndex;
		this.name = name;
		this.uri = uri;
		this.displayName = displayName;
		this.number = number;
		this.isGroup = isGroup;
		this.iconId = iconId;
		this.hasCheckBox = hasCheckBox;
		this.icon = icon;
		this.bigIcon = bigicon;

	}

	public NodeResource(String groupIndex, String uri, String name,
			String displayName, String bussinessType, int iconId,
			boolean isGroup, String userType, String icon, String bigicon) {
		this.superIndex = groupIndex;
		this.uri = uri;
		this.name = name;
		this.number = name;
		this.displayName = displayName;
		this.bussinessType = bussinessType;
		this.isGroup = isGroup;
		this.iconId = iconId;
		this.userType = userType;
		this.icon = icon;
		this.bigIcon = bigicon;

	}

	public NodeResource(String index, String superIndex, String name,
			String uri, String displayName, String number, boolean isGroup,
			int iconId, boolean hasCheckBox, String userType, String icon,
			String bigicon) {
		super();
		this.index = index;
		this.superIndex = superIndex;
		this.name = name;
		this.uri = uri;
		this.displayName = displayName;
		this.number = number;
		this.isGroup = isGroup;
		this.iconId = iconId;

		this.hasCheckBox = hasCheckBox;
		this.userType = userType;

		this.icon = icon;
		this.bigIcon = bigicon;

	}

	// ×Ô×éÍøµÄ
	public NodeResource(String ipAddress, String nickName, String uri,
			String mobileNumber, boolean group) {
		super();
		this.index = ipAddress;
		this.superIndex = ipAddress;
		this.name = nickName;
		this.displayName = nickName;
		this.uri = uri;
		this.number = mobileNumber;
		this.isGroup = false;
	}
}
