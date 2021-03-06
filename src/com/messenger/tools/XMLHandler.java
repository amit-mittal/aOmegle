package com.messenger.tools;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.messenger.interfaces.IUpdateData;
import com.messenger.types.FriendInfo;
import com.messenger.types.STATUS;

/*
 * Parses the xml data to FriendInfo array
 * XML Structure 
 * <?xml version="1.0" encoding="UTF-8"?>
 * 
 * <friends>
 * 		<user key="..." />
 * 		<friend username="..." status="..." IP="..." port="..." key="..." expire="..." />
 * 		<friend username="..." status="..." IP="..." port="..." key="..." expire="..." />
 * </friends>
 *
 *
 *status == online || status == unApproved
 * */

public class XMLHandler extends DefaultHandler
{
		private String userKey = new String();
		private IUpdateData updater;
		
		public XMLHandler(IUpdateData updater) {
			super();
			this.updater = updater;
		}

		private Vector<FriendInfo> mFriends = new Vector<FriendInfo>();
		private Vector<FriendInfo> mOnlineFriends = new Vector<FriendInfo>();
		private Vector<FriendInfo> mUnapprovedFriends = new Vector<FriendInfo>();

		
		public void endDocument() throws SAXException 
		{
			FriendInfo[] friends = new FriendInfo[mFriends.size() + mOnlineFriends.size()];
			
			
			int onlineFriendCount = mOnlineFriends.size();			
			for (int i = 0; i < onlineFriendCount; i++) 
			{				
				friends[i] = mOnlineFriends.get(i);
			}
			
						
			int offlineFriendCount = mFriends.size();			
			for (int i = 0; i < offlineFriendCount; i++) 
			{
				friends[i + onlineFriendCount] = mFriends.get(i);
			}
			
			int unApprovedFriendCount = mUnapprovedFriends.size();
			FriendInfo[] unApprovedFriends = new FriendInfo[unApprovedFriendCount];
			
			for (int i = 0; i < unApprovedFriends.length; i++) {
				unApprovedFriends[i] = mUnapprovedFriends.get(i);
			}
			
			
			this.updater.updateData(friends, unApprovedFriends, userKey);
			super.endDocument();
		}		
		
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException 
		{				
			if (localName == "friend")
			{
				FriendInfo friend = new FriendInfo();
				friend.userName = attributes.getValue(FriendInfo.USERNAME);
				String status = attributes.getValue(FriendInfo.STATUS);
				friend.ip = attributes.getValue(FriendInfo.IP);
				friend.port = attributes.getValue(FriendInfo.PORT);
				friend.userKey = attributes.getValue(FriendInfo.USER_KEY);
				//friend.expire = attributes.getValue("expire");
				
				if (status != null && status.equals("online"))
				{					
					friend.status = STATUS.ONLINE;
					mOnlineFriends.add(friend);
				}
				else if (status.equals("unApproved"))
				{
					friend.status = STATUS.UNAPPROVED;
					mUnapprovedFriends.add(friend);
				}	
				else
				{
					friend.status = STATUS.OFFLINE;
					mFriends.add(friend);	
				}											
			}
			else if (localName == "user") {
				this.userKey = attributes.getValue(FriendInfo.USER_KEY);
			}
			super.startElement(uri, localName, name, attributes);
		}

		@Override
		public void startDocument() throws SAXException {			
			this.mFriends.clear();
			this.mOnlineFriends.clear();
			super.startDocument();
		}
		
		
}

