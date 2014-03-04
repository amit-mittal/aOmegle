package com.messenger;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.messenger.interfaces.IAppManager;
import com.messenger.services.IMService;
import com.messenger.tools.StrangerController;
import com.messenger.types.StrangerInfo;
import com.messenger.types.STATUS;

public class StrangerList extends ListActivity 
{
	private static final int SEE_FRIEND_LIST = Menu.FIRST;
	private static final int REFRESH_STRANGER_LIST = Menu.FIRST+1;
	private static final int EXIT_APP_ID = Menu.FIRST+2;
	private IAppManager imService = null;
	private StrangerListAdapter strangerAdapter;

	private class StrangerListAdapter extends BaseAdapter 
	{		
		class ViewHolder {
			TextView text;
			ImageView icon;
		}
		private LayoutInflater mInflater;
		private Bitmap mOnlineIcon;
		private Bitmap mOfflineIcon;		

		private StrangerInfo[] strangers = null;


		public StrangerListAdapter(Context context) {
			super();			

			mInflater = LayoutInflater.from(context);

			mOnlineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.greenstar);
			mOfflineIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.redstar);

		}

		public void setStrangerList(StrangerInfo[] strangers)
		{
			this.strangers = strangers;
		}


		public int getCount() {		

			return strangers.length;
		}

		public StrangerInfo getItem(int position) {			

			return strangers[position];
		}

		public long getItemId(int position) {

			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// A ViewHolder keeps references to children views to avoid unneccessary calls
			// to findViewById() on each row.
			ViewHolder holder;

			// When convertView is not null, we can reuse it directly, there is no need
			// to reinflate it. We only inflate a new View when the convertView supplied
			// by ListView is null.
			if (convertView == null) 
			{
				convertView = mInflater.inflate(R.layout.stranger_list_screen, null);

				// Creates a ViewHolder and store references to the two children views
				// we want to bind data to.
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);                                       

				convertView.setTag(holder);
			}   
			else {
				// Get the ViewHolder back to get fast access to the TextView
				// and the ImageView.
				holder = (ViewHolder) convertView.getTag();
			}

			// Bind the data efficiently with the holder
			//strangers[position].userName
	//		holder.text.setText(strangers[position].userName);
			holder.text.setText(strangers[position].displayName);
			holder.icon.setImageBitmap(strangers[position].status == STATUS.ONLINE ? mOnlineIcon : mOfflineIcon);

			return convertView;
		}

	}

	public class MessageReceiver extends  BroadcastReceiver  {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Log.i("Broadcast receiver ", "received a message");
			Bundle extra = intent.getExtras();
			if (extra != null)
			{
				String action = intent.getAction();
				if (action.equals(IMService.STRANGER_LIST_UPDATED))
				{
					// taking stranger List from broadcast
					//String rawStrangerList = extra.getString(StrangerInfo.stranger_LIST);
					//StrangerList.this.parseStrangerInfo(rawStrangerList);
					StrangerList.this.updateData(StrangerController.getStrangersInfo());
					
				}
			}
		}

	};
	public MessageReceiver messageReceiver = new MessageReceiver();

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {          
			imService = ((IMService.IMBinder)service).getService();      
			
			StrangerInfo[] strangers = StrangerController.getStrangersInfo(); //imService.getLastRawFriendList();
			if (strangers != null) {    			
				StrangerList.this.updateData(strangers); // parseStrangerInfo(friendList);
			}    
			
			setTitle(imService.getUsername() + "'s stranger list");
		}
		public void onServiceDisconnected(ComponentName className) {          
			imService = null;
			Toast.makeText(StrangerList.this, R.string.local_service_stopped,
					Toast.LENGTH_SHORT).show();
		}
	};
	


	protected void onCreate(Bundle savedInstanceState) 
	{		
		super.onCreate(savedInstanceState);

        setContentView(R.layout.list_screen_stranger);
        
        strangerAdapter = new StrangerListAdapter(this);
	}
	public void updateData(StrangerInfo[] strangers)
	{
		if (strangers != null) {
			strangerAdapter.setStrangerList(strangers);	
			setListAdapter(strangerAdapter);				
		}
	}


	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);		

		Intent i = new Intent(this, StrangerMessaging.class);
		StrangerInfo stranger = strangerAdapter.getItem(position);
		if (stranger.status == STATUS.ONLINE)		
		{
			i.putExtra(StrangerInfo.USERNAME, stranger.userName);
			i.putExtra(StrangerInfo.PORT, stranger.port);
			i.putExtra(StrangerInfo.IP, stranger.ip);		
			startActivity(i);
		}
		else 
		{			
			Toast.makeText(StrangerList.this, R.string.user_offline, Toast.LENGTH_SHORT).show();
		}
	}




	@Override
	protected void onPause() 
	{
		unregisterReceiver(messageReceiver);		
		unbindService(mConnection);
		super.onPause();
	}

	@Override
	protected void onResume() 
	{
			
		super.onResume();
		bindService(new Intent(StrangerList.this, IMService.class), mConnection , Context.BIND_AUTO_CREATE);

		IntentFilter i = new IntentFilter();
		//i.addAction(IMService.TAKE_MESSAGE);	
		i.addAction(IMService.STRANGER_LIST_UPDATED);

		registerReceiver(messageReceiver, i);			
		

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		boolean result = super.onCreateOptionsMenu(menu);		
		
//		menu.add(0,SEE_FRIEND_LIST,0,R.string.see_friend_list);
		menu.add(0,REFRESH_STRANGER_LIST,0,R.string.refresh_stranger_list);
		menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);		
		
		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) 
	{		

		switch(item.getItemId()) 
		{	
	/*		case SEE_FRIEND_LIST:
			{
				Intent i = new Intent(StrangerList.this, FriendList.class);						
				startActivity(i);
				finish();
				return true;
			}*/
			case REFRESH_STRANGER_LIST:
			{
				imService.refreshStrangerList();
				return true;
			}
			case EXIT_APP_ID:
			{
				imService.exit();
				finish();
				return true;
			}			
		}

		return super.onMenuItemSelected(featureId, item);		
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {	
		super.onActivityResult(requestCode, resultCode, data);
	}
}
