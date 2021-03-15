package org.ayound.desktop.filesearch;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Page<T> {

	public static final Logger logger = LogManager.getLogger(Page.class);

	private SimpleIntegerProperty totalRecord; // total record number in source data
	private SimpleIntegerProperty pageSize; // the number of data in per page
	private SimpleIntegerProperty totalPage; // total page number
	private SimpleIntegerProperty currentPage; // total page number

	private SimpleStringProperty queryCond;
	private SimpleStringProperty sort;
	private boolean isExternal;

	DataService service;
	private SimpleBooleanProperty sortType = new SimpleBooleanProperty(true);

	private SimpleBooleanProperty status = new SimpleBooleanProperty(false);

	/**
	 * setter ** /** getter
	 **/

	public int getTotalRecord() {
		return totalRecord.get();
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord.set(totalRecord);
	}

	public int getPageSize() {
		return pageSize.get();
	}

	public void setPageSize(int pageSize) {
		this.pageSize.set(pageSize);
	}

	public int getTotalPage() {
		return totalPage.get();
	}

	public SimpleIntegerProperty getTotalPageProp() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage.set(totalPage);
	}

	public SimpleStringProperty getQueryCond() {
		return queryCond;
	}

	public void setQueryCond(String queryCond) {
		this.queryCond.set(queryCond);
	}

	public SimpleStringProperty getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort.set(sort);
	}

	public void setSortType(boolean sortType) {
		this.sortType.set(sortType);
	}

	public SimpleIntegerProperty getCurrentPage() {
		return currentPage;
	}

	public SimpleBooleanProperty getStatus() {
		return status;
	}

	/**
	 * @param rowDataList
	 * @param pageSize    the number of data in per page
	 */
	public Page(int pageSize) {
		this.totalRecord = new SimpleIntegerProperty();
		this.totalPage = new SimpleIntegerProperty(1);
		this.pageSize = new SimpleIntegerProperty(pageSize);
		this.currentPage = new SimpleIntegerProperty(0);
		this.queryCond = new SimpleStringProperty("");
		this.sort = new SimpleStringProperty();
		initialize();

	}

	private void initialize() {
		totalRecord.set(0);

		// calculate the number of total pages
		totalPage.set(totalRecord.get() % pageSize.get() == 0 ? totalRecord.get() / pageSize.get()
				: totalRecord.get() / pageSize.get() + 1);

		// add listener: the number of total pages need to be change if the page size
		// changed
		pageSize.addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				totalPage.set(totalRecord.get() % pageSize.get() == 0 ? totalRecord.get() / pageSize.get()
						: totalRecord.get() / pageSize.get() + 1);

			}
		});
	}

	public String highlightText(String text) {
		String newText = text;
		if (service != null) {
			newText = service.highLightText(text);
		}
		if (StringUtils.isEmpty(text)) {
			newText = text;
		}
		return newText;
	}

	/**
	 * current page number(0-based system)
	 *
	 * @param currentPage current page number
	 * @return
	 */
	public SearchResult getCurrentPageDataList(int currentPage) {
		SearchResult result = new SearchResult();
		int fromIndex = pageSize.get() * currentPage;
		try {
			String sortField = sort.get();
			if (StringUtils.isEmpty(sortField)) {
				sortField = null;
			}
			service = new DataService(isExternal, queryCond.get(), fromIndex, getPageSize(), sortField, sortType.get());

			result = service.search();
			int count = result.getCount();
			int pageCount = count % pageSize.get() == 0 ? count / pageSize.get() : count / pageSize.get() + 1;
			this.totalPage.set(pageCount);
		} catch (IOException e) {
			logger.error("error to get page data for " + currentPage);
		} catch (ParseException e) {
			logger.error("error to get page data for " + currentPage);
		}
		return result;
	}

	public boolean isExternal() {
		return isExternal;
	}

	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}

	public void updateCount() {
		String sortField = sort.get();
		if (StringUtils.isEmpty(sortField)) {
			sortField = null;
		}
		service = new DataService(isExternal, queryCond.get(), 0, getPageSize(), sortField, sortType.get());
		int count;
		try {
			count = service.count();
			int pageCount = count % pageSize.get() == 0 ? count / pageSize.get() : count / pageSize.get() + 1;
			this.totalPage.set(pageCount);
		} catch (IOException e) {
			logger.error("error to update count data for " + currentPage);
		} catch (ParseException e) {
			logger.error("error to update count data for " + currentPage);
		}

	}

}