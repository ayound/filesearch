package org.ayound.desktop.config;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ConfigModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2242210823287658969L;

	int insideIncludeContent = ConfigUtil.NOT_INCLUDE_CONTENT;

	int externalIncludeContent = ConfigUtil.INCLUDE_CONTENT;

	List<String> insideDirList = new ArrayList<String>();

	List<String> externalDirList = new ArrayList<String>();

	List<String> blackDirList = new ArrayList<String>();

	public ConfigModel() {
		super();
		insideDirList.add(ConfigUtil.getUserHomePath());
		blackDirList.add(Paths.get(ConfigUtil.getUserHomePath(), "Library/Application Support/MobileSync/Backup")
				.toAbsolutePath().toString());
		blackDirList.add(Paths.get(ConfigUtil.getUserHomePath(), "Library/Caches").toAbsolutePath().toString());
	}

	public List<String> getInsideDirList() {
		return insideDirList;
	}

	public void setInsideDirList(List<String> insideDirList) {
		this.insideDirList = insideDirList;
	}

	public List<String> getExternalDirList() {
		return externalDirList;
	}

	public void setExternalDirList(List<String> externalDirList) {
		this.externalDirList = externalDirList;
	}

	public List<String> getBlackDirList() {
		return blackDirList;
	}

	public void setBlackDirList(List<String> blackDirList) {
		this.blackDirList = blackDirList;
	}

	public int getInsideIncludeContent() {
		return insideIncludeContent;
	}

	public void setInsideIncludeContent(int insideIncludeContent) {
		this.insideIncludeContent = insideIncludeContent;
	}

	public int getExternalIncludeContent() {
		return externalIncludeContent;
	}

	public void setExternalIncludeContent(int externalIncludeContent) {
		this.externalIncludeContent = externalIncludeContent;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigModel other = (ConfigModel) obj;
		if (blackDirList == null) {
			if (other.blackDirList != null)
				return false;
		} else if (!blackDirList.equals(other.blackDirList))
			return false;
		if (externalDirList == null) {
			if (other.externalDirList != null)
				return false;
		} else if (!externalDirList.equals(other.externalDirList))
			return false;
		if (externalIncludeContent != other.externalIncludeContent)
			return false;
		if (insideDirList == null) {
			if (other.insideDirList != null)
				return false;
		} else if (!insideDirList.equals(other.insideDirList))
			return false;
		if (insideIncludeContent != other.insideIncludeContent)
			return false;
		return true;
	}
	
	

}
