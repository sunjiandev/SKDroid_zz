package com.sunkaisens.skdroid.model;

import android.util.SparseArray;

public class ModelNode {

	/**
	 * 树的节点（组） 根节点的节点列表代表当前组类型的所有组
	 */
	public SparseArray<ModelNode> mOrgList = new SparseArray<ModelNode>();

	/**
	 * 树的叶子 根节点的叶子代表此种类型通讯录的所有成员
	 */
	public SparseArray<ModelNode> mMemberList = new SparseArray<ModelNode>();

	public String index;// 索引号码

	public String name;// 姓名

	public String mobileNo;// 移动号码

	public boolean isgroup;// 是否是组

	public int mGroupType = 0;// 业务类型

	public String uri;// 描述id

	public int imageid;// 头像id,临时为索引值

	public String state;// 状态,

	public int sex;// 性别

	public String title;// 头衔

	public String brief;// 个人简介

	public String org;// 组织

	public long lasttime;// 最后交流的时间

	public boolean isOnline; // 是否在线

	public String userType;

	// 首字母字符串集合
	public String pyHeaders = new String();

	@Override
	public boolean equals(Object o) {
		ModelNode tmp = (ModelNode) o;

		return this.index.equals(tmp.index);
	}

}
