package org.ayound.desktop.filesearch;

import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import javafx.util.Callback;

public class TableWithPaginationAndSorting<T> {
	private Page<T> page;
	TableView<T> tableView;
	private Pagination tableViewWithPaginationPane;

	/** getter **/

	public TableWithPaginationAndSorting(Page<T> page, TableView<T> tableView) {
		this.page = page;
		this.tableView = tableView;
		tableViewWithPaginationPane = new Pagination();
		tableViewWithPaginationPane.setMaxPageIndicatorCount(50);
		tableViewWithPaginationPane.pageCountProperty().bindBidirectional(page.getTotalPageProp());
		tableViewWithPaginationPane.currentPageIndexProperty().bindBidirectional(page.getCurrentPage());

		updatePagination();
	}

	private void updatePagination() {
		
		page.getStatus().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				SearchResult result = page.getCurrentPageDataList(0);
				tableView.setItems(FXCollections.observableList((List<T>)result.getFiles()));
			}
		});	
		
		tableViewWithPaginationPane.setPageFactory(new Callback<Integer, Node>() {

			public Node call(Integer pageIndex) {
				if (pageIndex < 0) {
					pageIndex = 0;
				}
				SearchResult result = page.getCurrentPageDataList(pageIndex);
				tableView.setItems(FXCollections.observableList((List<T>)result.getFiles()));
				return tableView;
			}
		});
	}

	public Page<T> getPage() {
		return page;
	}

	public TableView<T> getTableView() {
		return tableView;
	}

	public Pagination getTableViewWithPaginationPane() {
		return tableViewWithPaginationPane;
	}

}