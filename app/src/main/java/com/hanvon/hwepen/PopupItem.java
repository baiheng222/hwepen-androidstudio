package com.hanvon.hwepen;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class PopupItem {
	
	public Drawable mDrawable;
	public CharSequence mTitle;

	public PopupItem(Drawable drawable, CharSequence title) {
		this.mDrawable = drawable;
		this.mTitle = title;
	}

	public PopupItem(Context context, int titleId, int drawableId) {
		this.mTitle = context.getResources().getText(titleId);
		this.mDrawable = context.getResources().getDrawable(drawableId);
	}

	public PopupItem(Context context, CharSequence title, int drawableId) {
		this.mTitle = title;
		this.mDrawable = context.getResources().getDrawable(drawableId);
	}
}
