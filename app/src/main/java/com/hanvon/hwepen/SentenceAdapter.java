package com.hanvon.hwepen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hanvon.bean.TransInfo;

import java.util.ArrayList;

public class SentenceAdapter extends BaseAdapter
{
	
	private ArrayList<TransInfo> listData;
	private LayoutInflater layoutInflater;

	public SentenceAdapter(Context context, ArrayList<TransInfo> listData) {
		this.listData = listData;
		layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return listData.size();
	}

	@Override
	public TransInfo getItem(int position) {
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.sentence_list, null);
			holder = new ViewHolder();
			holder.itemView = (TextView) convertView.findViewById(R.id.sentence_list_item);
			holder.tranView = (TextView) convertView.findViewById(R.id.sentence_list_trans);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.itemView.setText(listData.get(position).getWord());
		holder.tranView.setText(listData.get(position).getTrans());
		return convertView;
	}

	static class ViewHolder {
		TextView itemView;
		TextView tranView;
	}
}  