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
package org.doubango.ngn.events;

/**
 * List of all supported types associated to SIP INVITE event arguments
 */
public enum NgnInviteEventTypes {
	/** ���� */
	INCOMING,
	/** ���� */
    INPROGRESS,
    /** ���� */
    RINGING,
    /** ����ý�� */
    EARLY_MEDIA,
    /** ��ͨ */
    CONNECTED,
    /** �Ҷ� */
    TERMWAIT,
    /** �ѹҶ� */
    TERMINATED,
    LOCAL_HOLD_OK,
    LOCAL_HOLD_NOK,
    LOCAL_RESUME_OK,
    LOCAL_RESUME_NOK,
    REMOTE_HOLD,
    REMOTE_RESUME,
    MEDIA_UPDATING,
    /** ý����� */
    MEDIA_UPDATED,
    /** SIP��Ӧ */
    SIP_RESPONSE,
    REMOTE_DEVICE_INFO_CHANGED,
    /** ���xunzy+ */
    GROUP_PTT_INFO,
    PTT_INFO_REQUEST, /*add by gle*/
    /** xunzy+ �ܻ� */
    ENCRYPT_INFO,
    //KEYDIS_ENCRYPT_INFO,//xunzy+ ��Կ�ַ�
    
    /** ��Ƶ���*/
    GROUP_VIDEO_MONITORING,
    
    /** ��Ƶת��*/
    VIEDO_TRANSMINT,    //�Զ˾ܽ�    REMOTE_REFUSE,

    LOCAL_TRANSFER_TRYING,
    LOCAL_TRANSFER_ACCEPTED,
    LOCAL_TRANSFER_COMPLETED,
    LOCAL_TRANSFER_FAILED,
    LOCAL_TRANSFER_NOTIFY,
    REMOTE_TRANSFER_REQUESTED,
    REMOTE_TRANSFER_NOTIFY,
    REMOTE_TRANSFER_INPROGESS,
    REMOTE_TRANSFER_FAILED,
    REMOTE_TRANSFER_COMPLETED,
    
    REMOTE_MEDIA_NOT_EXIST,
    
    /** network error**/
    CURRENT_NETWORK_UNGOOD,

}
