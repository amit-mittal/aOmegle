package com.messenger.interfaces;
import com.messenger.types.FriendInfo;


public interface IUpdateData {
	public void updateData(FriendInfo[] friends, FriendInfo[] unApprovedFriends, String userKey);

}
