package com.hanvon.hwepen;

import com.hanvon.bean.FileInfo;

import java.util.ArrayList;
import java.util.List;

public class ListFileData {
	
	//处理file数据
	public static ArrayList<Category> listData(List<FileInfo> files) {
		ArrayList<Category> categoryList = new ArrayList<Category>();
		Category category = null;
		for (int i = 0; i < files.size(); i++) {
			if (i==0) {
				category = new Category(files.get(i).getCreateTime().substring(0, 7).replace("-", "."));
				CategoryItem item = new CategoryItem();
				item.setFuuid(files.get(i).getFuuid());
				item.setDate(files.get(i).getCreateTime());
				item.setContent(files.get(i).getContent());
				item.setTitle(files.get(i).getTitle());
				category.addItem(item);
				if (i == files.size() -1) {
					categoryList.add(category);
				}
			} else {
				String nowDate = files.get(i).getCreateTime().substring(0, 7);
				String preDate = files.get(i-1).getCreateTime().substring(0, 7);
				if (nowDate.equals(preDate)) {
					CategoryItem item = new CategoryItem();
					item.setFuuid(files.get(i).getFuuid());
					item.setDate(files.get(i).getCreateTime());
					item.setContent(files.get(i).getContent());
					item.setTitle(files.get(i).getTitle());
					category.addItem(item);
					if (i == files.size() -1) {
						categoryList.add(category);
					}
				}  else {
					categoryList.add(category);
					category = null;
					category = new Category(files.get(i).getCreateTime().substring(0, 7).replace("-", "."));
					CategoryItem item = new CategoryItem();
					item.setFuuid(files.get(i).getFuuid());
					item.setDate(files.get(i).getCreateTime());
					item.setContent(files.get(i).getContent());
					item.setTitle(files.get(i).getTitle());
					category.addItem(item);
					if (i == files.size() -1) {
						categoryList.add(category);
					}
				}
			}
		}
		return categoryList;
	}
}
