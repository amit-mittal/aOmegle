package com.messenger.tools;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.messenger.interfaces.IUpdateStranger;
import com.messenger.types.StrangerInfo;
import com.messenger.types.STATUS;

/*
 * Parses the xml data to StrangerInfo array
 * XML Structure 
 * <?xml version="1.0" encoding="UTF-8"?>
 * 
 * <strangers>
 * 		<user key="..." />
 * 		<stranger username="..." status="..." IP="..." port="..." key="..." expire="..." />
 * 		<stranger username="..." status="..." IP="..." port="..." key="..." expire="..." />
 * </strangers>
 *
 *
 *status == online || status == unApproved
 * */

public class XMLStranger extends DefaultHandler
{
		private String userKey = new String();
		private IUpdateStranger updater;
		
		public XMLStranger(IUpdateStranger updater) {
			super();
			this.updater = updater;
		}

		private Vector<StrangerInfo> mStrangers = new Vector<StrangerInfo>();
		private Vector<StrangerInfo> mOnlineStrangers = new Vector<StrangerInfo>();
		private Vector<StrangerInfo> mUnapprovedStrangers = new Vector<StrangerInfo>();

		
		public void endDocument() throws SAXException 
		{
			StrangerInfo[] strangers = new StrangerInfo[mStrangers.size() + mOnlineStrangers.size()];
			
			
			int onlineStrangerCount = mOnlineStrangers.size();			
			for (int i = 0; i < onlineStrangerCount; i++) 
			{				
				strangers[i] = mOnlineStrangers.get(i);
			}
			
						
			int offlineStrangerCount = mStrangers.size();			
			for (int i = 0; i < offlineStrangerCount; i++) 
			{
				strangers[i + onlineStrangerCount] = mStrangers.get(i);
			}
			
			int unApprovedStrangerCount = mUnapprovedStrangers.size();
			StrangerInfo[] unApprovedStrangers = new StrangerInfo[unApprovedStrangerCount];
			
			for (int i = 0; i < unApprovedStrangers.length; i++) {
				unApprovedStrangers[i] = mUnapprovedStrangers.get(i);
			}
			
			
			this.updater.updateData(strangers, unApprovedStrangers, userKey);
			super.endDocument();
		}		
		
		public void startElement(String uri, String localName, String name,
				Attributes attributes) throws SAXException 
		{				
			if (localName == "stranger")
			{
				StrangerInfo stranger = new StrangerInfo();
				stranger.userName = attributes.getValue(StrangerInfo.USERNAME);				
				String status = attributes.getValue(StrangerInfo.STATUS);
				stranger.ip = attributes.getValue(StrangerInfo.IP);
				stranger.port = attributes.getValue(StrangerInfo.PORT);
				stranger.userKey = attributes.getValue(StrangerInfo.USER_KEY);
				stranger.displayName = attributes.getValue(StrangerInfo.DISPLAY_NAME);
				//stranger.expire = attributes.getValue("expire");
				
				if (status != null && status.equals("online"))
				{					
					stranger.status = STATUS.ONLINE;
					mOnlineStrangers.add(stranger);
				}
				else if (status.equals("unApproved"))
				{
					stranger.status = STATUS.UNAPPROVED;
					mUnapprovedStrangers.add(stranger);
				}
				else
				{
					stranger.status = STATUS.OFFLINE;
					mStrangers.add(stranger);	
				}											
			}
			else if (localName == "user") {
				this.userKey = attributes.getValue(StrangerInfo.USER_KEY);
			}
			super.startElement(uri, localName, name, attributes);
		}

		@Override
		public void startDocument() throws SAXException {			
			this.mStrangers.clear();
			this.mOnlineStrangers.clear();
			super.startDocument();
		}
		
		
}

