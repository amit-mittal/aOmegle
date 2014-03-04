package com.messenger.interfaces;
import com.messenger.types.StrangerInfo;


public interface IUpdateStranger {
	public void updateData(StrangerInfo[] strangers, StrangerInfo[] unApprovedStrangers, String userKey);

}
