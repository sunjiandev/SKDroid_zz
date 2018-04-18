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
package org.doubango.ngn.model;

import java.util.ArrayList;
import java.util.List;

import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.utils.NgnPredicate;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class NgnHistoryPushEvent extends NgnHistoryEvent {
	
	@Element(data=true, required=false)
	protected String mContent; //����������Ϣcontent
	@Element(data=true, required=false)
	protected String mId; //����������Ϣid
	@Element(data=true, required=false)
	protected String mTitle; //����������Ϣtitle
	@Element(data=true, required=false)
	protected String mDigest; //����������Ϣdigest
	@Element(data=true, required=false)
	protected String mImageUrl; //����������ϢimageUrl
	@Element(data=true, required=false)
	protected String mLinkUri; //����������ϢlinkUri
	@Element(data=true, required=false)
	protected String mTurn; //����������Ϣturn
	@Element(data=true, required=false)
	protected String mServiceType; //����������ϢserviceType
	@Element(data=true, required=false)
	protected String mMsgType; //����������ϢmsgType
	@Element(data=true, required=false)
	protected List pushList; //��ͼ�ģ�������˵�һ����Ϣ֮���������Ϣ
	
	NgnHistoryPushEvent(){
		this(null, StatusType.Failed);
	}
	
	public NgnHistoryPushEvent(String remoteParty, StatusType status) {
		super(NgnMediaType.Push, remoteParty);
		super.setStatus(status);
	}
	
	public void setContent(String content){
		this.mContent = content;
	}
	
	public String getContent(){
		return this.mContent;
	}
	
	public void setId(String mId){
		this.mId = mId;
	}
	
	public String getId(){
		return this.mId;
	}
	
	public void setTitle(String mTitle){
		this.mTitle = mTitle;
	}
	
	public String getTitle(){
		return this.mTitle;
	}
	
	public void setDigest(String mDigest){
		this.mDigest = mDigest;
	}
	
	public String getDigest(){
		return this.mDigest;
	}
	
	public void setImageUrl(String mImageUrl){
		this.mImageUrl = mImageUrl;
	}
	
	public String getImageUrl(){
		return this.mImageUrl;
	}
	
	public void setLinkUri(String mLinkUri){
		this.mLinkUri = mLinkUri;
	}
	
	public String getLinkUri(){
		return this.mLinkUri;
	}
	
	public void setTurn(String mTurn){
		this.mTurn = mTurn;
	}
	
	public String getTurn(){
		return this.mTurn;
	}
	
	public void setServiceType(String mServiceType){
		this.mServiceType = mServiceType;
	}
	
	public String getServiceType(){
		return this.mServiceType;
	}
	
	public void setMsgType(String mMsgType){
		this.mMsgType = mMsgType;
	}
	
	public String getMsgType(){
		return this.mMsgType;
	}
	
	public void setPushList(List mpushList){
		this.pushList = mpushList;
	}
	
	public List getPushList(){
		return this.pushList;
	}
	
	
	
	
	public static class HistoryEventPushIntelligentFilter implements NgnPredicate<NgnHistoryEvent> {
		private final List<String> mRemoteParties = new ArrayList<String>();
		
		protected void reset(){
			mRemoteParties.clear();
		}
		
		@Override
		public boolean apply(NgnHistoryEvent event) {
			if (event != null && (event.getMediaType() == NgnMediaType.Push)) {
				if (!mRemoteParties.contains(event.getRemoteParty())) {
					mRemoteParties.add(event.getRemoteParty());
					return true;
				}
			}
			return false;
		}
	}
	
	public static class HistoryEventPushFilter implements NgnPredicate<NgnHistoryEvent> {
		@Override
		public boolean apply(NgnHistoryEvent event) {
			return (event != null && (event.getMediaType() == NgnMediaType.Push));
		}
	}
}
