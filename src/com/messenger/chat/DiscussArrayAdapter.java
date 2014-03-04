package com.messenger.chat;

import java.util.ArrayList;
import java.util.List;

import com.messenger.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
/*
public class DiscussArrayAdapter extends ArrayAdapter<OneComment> {

	private TextView countryName;
	private List<OneComment> countries = new ArrayList<OneComment>();
	private LinearLayout wrapper;

	@Override
	public void add(OneComment object) {
		countries.add(object);
		super.add(object);
	}

	public DiscussArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public int getCount() {
		return this.countries.size();
	}

	public OneComment getItem(int index) {
		return this.countries.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.listitem_discuss, parent, false);
		}


		wrapper = (LinearLayout) row.findViewById(R.id.wrapper);

		OneComment coment = getItem(position);

		countryName = (TextView) row.findViewById(R.id.comment);

		countryName.setText(coment.comment + "\n");
		
		String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
		
		countryName.append(Html.fromHtml("<small><span align=\"right\"><font color=grey size='1sp'>"
											+currentDateTimeString+"</font></span></small>"));

		countryName.setBackgroundResource(coment.left ? R.drawable.bubble_yellow : R.drawable.bubble_green);
		wrapper.setGravity(coment.left ? Gravity.LEFT : Gravity.RIGHT);

		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
	}
}	*/

public class DiscussArrayAdapter extends ArrayAdapter<MessageBubble> {

	private TextView countryName;
	private List<MessageBubble> countries = new ArrayList<MessageBubble>();
	private LinearLayout wrapper;

	@Override
	public void add(MessageBubble object) {
		countries.add(object);
		super.add(object);
	}

	public DiscussArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}

	public int getCount() {
		return this.countries.size();
	}

	public MessageBubble getItem(int index) {
		return this.countries.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.listitem_discuss, parent, false);
		}

		wrapper = (LinearLayout) row.findViewById(R.id.wrapper);

		MessageBubble coment = getItem(position);

		countryName = (TextView) row.findViewById(R.id.comment);

		countryName.setText(coment.message + "\n");
		
	/*	String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
		
		countryName.append(Html.fromHtml("<small><span align=\"right\"><font color=grey size='1sp'>"
											+currentDateTimeString+"</font></span></small>"));
		*/
		countryName.append(Html.fromHtml("<small><span align=\"right\"><font color=grey size='1sp'>"
				+coment.time+"</font></span></small>"));

		countryName.setBackgroundResource(coment.left ? R.drawable.bubble_yellow : R.drawable.bubble_green);
		wrapper.setGravity(coment.left ? Gravity.LEFT : Gravity.RIGHT);

		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
	}
}