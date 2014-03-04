package com.messenger;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class ChatList extends TabActivity{
	TabHost tabhost;
	Intent in;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chatlist);
		Resources res=getResources();
		
		tabhost=getTabHost();
		TabHost.TabSpec spec;
		
		in=new Intent(ChatList.this,FriendList.class);
		spec=tabhost.newTabSpec("Friend").setIndicator("Friend", res.getDrawable(R.drawable.icon)).setContent(in);
		tabhost.addTab(spec);
		
		in=new Intent(ChatList.this,StrangerList.class);
		spec=tabhost.newTabSpec("Stranger").setIndicator("Stranger",res.getDrawable(R.drawable.icon)).setContent(in);
		tabhost.addTab(spec);
	}
}