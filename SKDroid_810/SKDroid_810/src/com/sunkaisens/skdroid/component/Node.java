package com.sunkaisens.skdroid.component;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Dragon
 * 
 */
public class Node {
	private Node parent = null; // ���ڵ�
	private List<Node> childrens = new ArrayList<Node>();// �ӽڵ�

	private int icon = -1; // icon(R.drawable��id)
	private boolean isChecked = false; // �Ƿ�ѡ��
	private boolean isExpand = true;// �Ƿ�����չ״̬
	private boolean hasCheckBox = true;// �Ƿ��и�ѡ��
	private boolean isVisiable = true;

	private String index = null; // GroupIndex || PersonIndex
	private String superIndex = null; // ParentIndex

	private String name = null; // Serial number for future use
	private String uri = null; // Unique
	private String displayName = null;
	private String number = null;

	private boolean isGroup;

	public Node(String index, String superIndex, String name, String uri,
			String displayName, String number, boolean isGroup, int iconId,
			boolean hasCheckBox) {
		this.index = index;
		this.superIndex = superIndex;
		this.name = name;
		this.uri = uri;
		this.displayName = displayName;
		this.number = number;
		this.isGroup = isGroup;
		this.icon = iconId;

		this.hasCheckBox = hasCheckBox;
	}

	/**
	 * �õ����ڵ�
	 * 
	 * @return
	 * 
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * ���ø��ڵ�
	 * 
	 * @param parent
	 * 
	 */
	public void setParent(Node parent) {
		this.parent = parent;
	}

	/**
	 * �õ��ӽڵ�
	 * 
	 * @return
	 * 
	 */
	public List<Node> getChildrens() {
		return childrens;
	}

	/**
	 * �Ƿ���ڵ�
	 * 
	 * @return
	 * 
	 */
	public boolean isRoot() {
		return parent == null ? true : false;
	}

	/**
	 * �Ƿ�����ͼ��
	 * 
	 * @return
	 * 
	 */
	public int getIcon() {
		return icon;
	}

	/**
	 * ����ͼ��
	 * 
	 * @param icon
	 * 
	 */
	public void setIcon(int icon) {
		this.icon = icon;
	}

	/**
	 * �Ƿ�ѡ��
	 * 
	 * @return
	 * 
	 */
	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	/**
	 * �Ƿ���չ��״̬
	 * 
	 * @return
	 * 
	 */
	public boolean isExplaned() {
		return isExpand;
	}

	/**
	 * ����չ��״̬
	 * 
	 * @param isExplaned
	 * 
	 */
	public void setExplaned(boolean isExplaned) {
		this.isExpand = isExplaned;
	}

	/**
	 * �Ƿ��и�ѡ��
	 * 
	 * @return
	 * 
	 */
	public boolean hasCheckBox() {
		return hasCheckBox;
	}

	/**
	 * �����Ƿ��и�ѡ��
	 * 
	 * @param hasCheckBox
	 * 
	 */
	public void setHasCheckBox(boolean hasCheckBox) {
		this.hasCheckBox = hasCheckBox;
	}

	/**
	 * ����һ���ӽڵ�
	 * 
	 * @param node
	 * 
	 */
	public void addNode(Node node) {
		if (!childrens.contains(node)) {
			childrens.add(node);
		}
	}

	/**
	 * �Ƴ�һ���ӽڵ�
	 * 
	 * @param node
	 * 
	 */
	public void removeNode(Node node) {
		if (childrens.contains(node))
			childrens.remove(node);
	}

	/**
	 * �Ƴ�ָ��λ�õ��ӽڵ�
	 * 
	 * @param location
	 * 
	 */
	public void removeNode(int location) {
		childrens.remove(location);
	}

	/**
	 * ��������ӽڵ�
	 * 
	 */
	public void clears() {
		childrens.clear();
	}

	/**
	 * �жϸ����Ľڵ��Ƿ�ǰ�ڵ�ĸ��ڵ�
	 * 
	 * @param node
	 * @return
	 * 
	 */
	public boolean isParent(Node node) {
		if (parent == null)
			return false;
		if (parent.equals(node))
			return true;
		return parent.isParent(node);
	}

	/**
	 * �ݹ��ȡ��ǰ�ڵ㼶��
	 * 
	 * @return
	 * 
	 */
	public int getLevel() {
		return parent == null ? 0 : parent.getLevel() + 1;
	}

	/**
	 * ���ڵ��Ƿ����۵���״̬
	 * 
	 * @return
	 * 
	 */
	public boolean isParentCollapsed() {
		if (parent == null)
			return false;
		if (!parent.isExplaned())
			return true;
		return parent.isParentCollapsed();
	}

	/**
	 * �Ƿ�Ҷ�ڵ㣨û��չ���¼��ļ��㣩
	 * 
	 * @return
	 * 
	 */
	public boolean isLeaf() {
		return childrens.size() < 1 ? true : false;
	}

	public String getIndex() {
		return index;
	}

	public String getSuperIndex() {
		return superIndex;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public boolean getIsGroup() {
		return isGroup;
	}

	public void setIsGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}
}
