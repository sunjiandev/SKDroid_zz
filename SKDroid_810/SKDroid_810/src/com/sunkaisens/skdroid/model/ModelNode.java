package com.sunkaisens.skdroid.model;

import android.util.SparseArray;

public class ModelNode {

	/**
	 * ���Ľڵ㣨�飩 ���ڵ�Ľڵ��б����ǰ�����͵�������
	 */
	public SparseArray<ModelNode> mOrgList = new SparseArray<ModelNode>();

	/**
	 * ����Ҷ�� ���ڵ��Ҷ�Ӵ����������ͨѶ¼�����г�Ա
	 */
	public SparseArray<ModelNode> mMemberList = new SparseArray<ModelNode>();

	public String index;// ��������

	public String name;// ����

	public String mobileNo;// �ƶ�����

	public boolean isgroup;// �Ƿ�����

	public int mGroupType = 0;// ҵ������

	public String uri;// ����id

	public int imageid;// ͷ��id,��ʱΪ����ֵ

	public String state;// ״̬,

	public int sex;// �Ա�

	public String title;// ͷ��

	public String brief;// ���˼��

	public String org;// ��֯

	public long lasttime;// �������ʱ��

	public boolean isOnline; // �Ƿ�����

	public String userType;

	// ����ĸ�ַ�������
	public String pyHeaders = new String();

	@Override
	public boolean equals(Object o) {
		ModelNode tmp = (ModelNode) o;

		return this.index.equals(tmp.index);
	}

}
