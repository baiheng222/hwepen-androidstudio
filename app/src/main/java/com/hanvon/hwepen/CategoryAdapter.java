package com.hanvon.hwepen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CategoryAdapter extends BaseAdapter
{
	
	private static final int TYPE_HEADER = 0;
	private static final int TYPE_ITEM = 1;
	
	private ArrayList<Category> mListData;
	private LayoutInflater mInflater;
	
	public CategoryAdapter(Context context, ArrayList<Category> mListData) {
		this.mListData = mListData;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		int count = 0;
		if (null != mListData) {
			for (Category category : mListData) {
				count += category.getItemCount();
			}
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		//异常情况处理
		if (null == mListData || position < 0 || position > getCount()) {
			return null;
		}
		int categoryFirstIndex = 0; //同一分类中，第一个元素的索引值
		for (Category category : mListData) {
			int size = category.getItemCount();
			int categoryIndex = position - categoryFirstIndex; //在当前分类中的索引值
			//item在当前分类内
			if (categoryIndex < size) {
				return category.getItem(categoryIndex);
			}
			//索引移动到当前分类结尾，即下一个分类第一个索引
			categoryFirstIndex += size;
		}
		return null;
	}
	
	@Override
	public int getItemViewType(int position) {
		//异常情况处理
		if (null == mListData || position < 0 || position > getCount()) {
			return TYPE_ITEM;
		}
		int categoryFirstIndex = 0;
		for (Category category : mListData) {
			int size = category.getItemCount();
			int categoryIndex = position - categoryFirstIndex;
			if (0 == categoryIndex) {
				return TYPE_HEADER;
			}
			categoryFirstIndex += size;
		}
		return TYPE_ITEM;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int itemViewType = getItemViewType(position);
		switch (itemViewType) {
		case TYPE_HEADER:
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.excerpt_listview_header, null);
			}
			TextView textView = (TextView) convertView.findViewById(R.id.excerpt_list_header);
			String itemValue = (String) getItem(position);
			textView.setText(itemValue);
			break;

		case TYPE_ITEM:
			ViewHolder viewHolder = null;
			if (null == convertView) {
				convertView = mInflater.inflate(R.layout.excerpt_listview_item, null);
				viewHolder = new ViewHolder();
				viewHolder.date = (TextView) convertView.findViewById(R.id.excerpt_list_date);
				viewHolder.summary = (TextView) convertView.findViewById(R.id.excerpt_list_summary);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			//绑定数据
			String date = ((CategoryItem)getItem(position)).getDate();
			String summary = ((CategoryItem)getItem(position)).getTitle();
			summary = summary.length()>30?summary.substring(0, 30):summary;
			viewHolder.date.setText(date);
			viewHolder.summary.setText(summary);
			break;
		}
		return convertView;
	}
	
	private class ViewHolder {
		TextView date;
		TextView summary;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) != TYPE_HEADER;
	}
}  