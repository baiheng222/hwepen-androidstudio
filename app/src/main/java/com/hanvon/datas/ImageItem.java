package com.hanvon.datas;

import java.io.Serializable;

/**
 * 图片对象
 *
 */
public class ImageItem implements Serializable
{
	private static final long serialVersionUID = -7188270558443739436L;
	public String imageId;
	public String thumbnailPath;
	public String sourcePath;
	public String imageBase64;
	public boolean isSelected = false;
	@Override
	public String toString() {
		return "ImageItem [imageId=" + imageId + ", thumbnailPath="
				+ thumbnailPath + ", sourcePath=" + sourcePath
				+ ", isSelected=" + isSelected + "]";
	}
	
	public String getSourcePath(){
		return sourcePath;
	}
	
	public String getImageBase64(){
		return imageBase64;
	}
	
	public void setImageBase64(String base64){
		this.imageBase64 = base64;
	}
}
