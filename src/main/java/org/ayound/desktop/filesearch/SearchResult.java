package org.ayound.desktop.filesearch;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
	List<FileModel> files = new ArrayList<FileModel>();
	int count = 0;

	int pageCount = 0;

	public List<FileModel> getFiles() {
		return files;
	}

	public void setFiles(List<FileModel> files) {
		this.files = files;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

}
