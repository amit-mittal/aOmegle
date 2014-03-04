package com.messenger.tools;

import com.messenger.types.StrangerInfo;;

/*
 * This class can store strangerInfo and check userkey and username combination 
 * according to its stored data
 */
public class StrangerController 
{
	
	private static StrangerInfo[] strangersInfo = null;
	private static StrangerInfo[] unapprovedStrangersInfo = null;
	private static String activeStranger;
	
	public static void setStrangersInfo(StrangerInfo[] strangerInfo)
	{
		StrangerController.strangersInfo = strangerInfo;
	}
	
	
	
	public static StrangerInfo checkStranger(String username, String userKey)
	{
		StrangerInfo result = null;
		if (strangersInfo != null) 
		{
			for (int i = 0; i < strangersInfo.length; i++) 
			{
				if ( strangersInfo[i].userName.equals(username) && 
						strangersInfo[i].userKey.equals(userKey)
					)
				{
					result = strangersInfo[i];
					break;
				}				
			}			
		}		
		return result;
	}
	
	public static void setActiveStranger(String strangerName){
		activeStranger = strangerName;
	}
	
	public static String getActiveStranger()
	{
		return activeStranger;
	}



	public static StrangerInfo getStrangerInfo(String username) 
	{
		StrangerInfo result = null;
		if (strangersInfo != null) 
		{
			for (int i = 0; i < strangersInfo.length; i++) 
			{
				if ( strangersInfo[i].userName.equals(username) )
				{
					result = strangersInfo[i];
					break;
				}				
			}			
		}		
		return result;
	}

	public static StrangerInfo[] getStrangersInfo() {
		return strangersInfo;
	}
	
	public static StrangerInfo[] getUnapprovedStrangersInfo() {
		return unapprovedStrangersInfo;
	}

	public static void setUnapprovedStrangersInfo(
			StrangerInfo[] unapprovedStrangers) {
		unapprovedStrangersInfo = unapprovedStrangers;	
	}

}
