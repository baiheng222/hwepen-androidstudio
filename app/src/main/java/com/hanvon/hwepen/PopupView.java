package com.hanvon.hwepen;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hanvon.util.Util;

import java.util.ArrayList;

public class PopupView extends PopupWindow
{
	
	public static final int TITLE_LEFT = 0;
	public static final int TITLE_RIGHT = 1;
	public static final int LIST_PADING = 10;
	
	private Context mContext;
	private Rect mRect = new Rect();
	private final int[] mLocation = new int[2];
	
	private int mScreenWidth;
	private int mScreenHeight;
	private boolean mIsDiry;
	
	private int popupGravity = Gravity.NO_GRAVITY;
	
	private int mDirection = TITLE_RIGHT;
	
	private OnItemOnClickListener mItemOnClickListener;
	
	private ListView mListView;
	private ArrayList<PopupItem> mPopupItems = new ArrayList<PopupItem>();
	
	public static interface OnItemOnClickListener {
		public void onItemClick(PopupItem item, int position);
	}
	
	public PopupView(Context context) {
		this(context, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	public PopupView(Context context, int width, int height) {
		this.mContext = context;
		setFocusable(true);
		setTouchable(true);
		setOutsideTouchable(true);
		mScreenWidth = Util.getScreenWidth(mContext);
		mScreenHeight = Util.getScreenHeight(mContext);
		setWidth(width);
		setHeight(height);
		setBackgroundDrawable(new BitmapDrawable()); //点击其它地方 popup 消失
		setContentView(LayoutInflater.from(mContext).inflate(R.layout.popup_view, null));
//		setAnimationStyle(R.style.AnimationPreview);
		initUI();
	}

	private void initUI() {
		mListView = (ListView) getContentView().findViewById(R.id.popup_list);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				dismiss(); //点击 item 后 popup 消失
				if (null != mItemOnClickListener) {
					mItemOnClickListener.onItemClick(mPopupItems.get(position), position);
				}
			}
		});
	}
	
	public void addPopupItem(PopupItem item) {
		if (null != item) {
			mPopupItems.add(item);
			mIsDiry = true;
		}
	}

	public void cleanPopupItem() {
		if (mPopupItems.isEmpty()) {
			mPopupItems.clear();
			mIsDiry = true;
		}
	}

	public PopupItem getPopupItem(int positon) {
		if (positon < 0 || positon > mPopupItems.size())
			return null;
		return mPopupItems.get(positon);
	}

	public void setDirection(int direction) {
		this.mDirection = direction;
	}

	public void setItemOnClickListener(OnItemOnClickListener onItemOnClickListener) {
		this.mItemOnClickListener = onItemOnClickListener;
	}

	public void show(View view) {
		view.getLocationOnScreen(mLocation);
		mRect.set(mLocation[0], mLocation[1], mLocation[0] + view.getWidth(), mLocation[1] + view.getHeight());
		if (mIsDiry) {
			populateItem();
		}
//		showAtLocation(view, popupGravity, mScreenWidth - LIST_PADING - (getWidth()/2), mRect.bottom );
		showAtLocation(view, popupGravity, mLocation[0], mRect.bottom + 3 );
	}
	
	private void populateItem() {
		mIsDiry = false;
		mListView.setAdapter(new BaseAdapter() {
			
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView textView = null;
				if (null == convertView) {
					textView = new TextView(mContext);
					textView.setTextColor(mContext.getResources().getColor(android.R.color.white));
					textView.setTextSize(18);
					textView.setPadding(0, 10, 0, 10);
					textView.setGravity(Gravity.CENTER);
					textView.setSingleLine(true);
				} else {
					textView = (TextView) convertView;
				}
				PopupItem item = mPopupItems.get(position);
				textView.setText(item.mTitle);
//				textView.setCompoundDrawablePadding(10);
				textView.setCompoundDrawablesWithIntrinsicBounds(item.mDrawable, null, null, null);
				return textView;
			}
			
			@Override
			public long getItemId(int position) {
				return position;
			}
			
			@Override
			public Object getItem(int position) {
				return mPopupItems.get(position);
			}
			
			@Override
			public int getCount() {
				return mPopupItems.size();
			}
		});
	}
}
