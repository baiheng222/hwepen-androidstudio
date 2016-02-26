package com.hanvon.hwepen;

import java.util.ArrayList;
import java.util.List;

/**
 * excerpt list category
 * @author Hu
 * 
 */
public class Category {

	private String mCategoryHeader;
	private List<CategoryItem> mCategoryItem = new ArrayList<CategoryItem>();

	public Category(String mCategoryHeader) {
		this.mCategoryHeader = mCategoryHeader;
	}

	public String getmCategoryHeader() {
		return mCategoryHeader;
	}

	public void addItem(CategoryItem item) {
		mCategoryItem.add(item);
	}

	/**
	 * 获取Item内容
	 * @param pPosition
	 * @return
	 */
	public Object getItem(int pPosition) {
		if (pPosition == 0) {// Category排在第一位
			return mCategoryHeader;
		} else {
			return mCategoryItem.get(pPosition - 1);
		}
	}

	/**
	 * 当前类别Item总数。Category也需要占用一个Item
	 * @return
	 */
	public int getItemCount() {
		return mCategoryItem.size() + 1;
	}

}
