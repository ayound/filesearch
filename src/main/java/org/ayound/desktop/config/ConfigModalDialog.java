package org.ayound.desktop.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class ConfigModalDialog {

	public static final Logger logger = LogManager.getLogger(ConfigModalDialog.class);

	Stage stage;

	private ListView<String> insideList;
	private ListView<String> externalList;
	private ListView<String> blackList;
	private CheckBox insideIncludeContentCheckBox;
	private CheckBox externalIncludeContentCheckBox;

	public ConfigModalDialog(final Stage stg) {

		stage = new Stage();
		// Initialize the Stage with type of modal
		stage.initModality(Modality.APPLICATION_MODAL);
		// Set the owner of the Stage
		stage.initOwner(stg);
		stage.setTitle(Messages.getString("ConfigModalDialog.PREVIEW_FILE")); //$NON-NLS-1$
//		stage.setResizable(false);
		Group root = new Group();
		Scene scene = new Scene(root, 800, 600);
		double width = scene.getWidth();
		double height = scene.getHeight();
		double listHeight = (height - 100) / 3;
		JMetro jMetro = new JMetro(scene,Style.DARK);
		jMetro.setScene(scene);
		jMetro.setAutomaticallyColorPanes(true);
		final VBox vbox = new VBox();
		vbox.setFillWidth(true);
		vbox.setMinWidth(width);
		vbox.setSpacing(2);
		vbox.setPadding(new Insets(0, 0, 0, 0));

		HBox insideBox = new HBox();
		insideBox.setMaxHeight(20);
		insideBox.setMinWidth(width);
		Label insideLabel = new Label(Messages.getString("ConfigModalDialog.CONFI_INSIDE_STORE_PATH")); //$NON-NLS-1$ //$NON-NLS-2$
		insideLabel.setMinWidth(200);
		insideLabel.setMaxWidth(200);
		HBox btnBox = new HBox();
		btnBox.setMinWidth(width - insideLabel.getWidth() - 200);
		btnBox.setAlignment(Pos.BASELINE_RIGHT);
		insideIncludeContentCheckBox = new CheckBox(Messages.getString("ConfigModalDialog.CONTENT_INDEX")); //$NON-NLS-1$
		Button insideAddButton = new Button(Messages.getString("ConfigModalDialog.ADD")); //$NON-NLS-1$ //$NON-NLS-2$
		insideAddButton.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle(Messages.getString("ConfigModalDialog.SELECT_DIR")); //$NON-NLS-1$
				File directory = directoryChooser.showDialog(new Stage());
				if (directory != null) {
					List<String> items = toList(insideList.getItems());
					String item = directory.getAbsolutePath();
					if (!items.contains(item)) {
						items.add(item);
						insideList.setItems(FXCollections.observableArrayList(items));
					}
				}

			}
		});
		Button insideRemoveButton = new Button(Messages.getString("ConfigModalDialog.DELETE")); //$NON-NLS-1$
		insideRemoveButton.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				String selectItem = insideList.getSelectionModel().getSelectedItem();
				List<String> items = insideList.getItems();
				items.remove(selectItem);
				insideList.setItems(FXCollections.observableArrayList(items));
			}
		});

		Button insideClearButton = new Button(Messages.getString("ConfigModalDialog.CLEAR")); //$NON-NLS-1$ //$NON-NLS-2$
		insideClearButton.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				ConfigUtil.clear(false);
			}
		});
		btnBox.getChildren().addAll(insideIncludeContentCheckBox, insideAddButton, insideRemoveButton,
				insideClearButton);
		insideBox.getChildren().addAll(insideLabel, btnBox);

		insideList = new ListView<String>();
		insideList.setMaxHeight(listHeight);
		insideList.setPlaceholder(new Label(Messages.getString("ConfigModalDialog.NO_DATA"))); //$NON-NLS-1$

		insideList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		// 设置默认选择项
		insideList.getSelectionModel().select(0);

		HBox externalBox = new HBox();
		externalBox.setMinWidth(width);
		externalBox.setMaxHeight(20);
		Label externalLabel = new Label(Messages.getString("ConfigModalDialog.CONFIG_EXTERNAL_STORE_PATH")); //$NON-NLS-1$
		externalLabel.setMinWidth(200);
		externalLabel.setMaxWidth(200);
		btnBox = new HBox();
		btnBox.setMinWidth(width - externalLabel.getWidth() - 200);
		btnBox.setAlignment(Pos.BASELINE_RIGHT);
		Button externalAddButton = new Button(Messages.getString("ConfigModalDialog.ADD")); //$NON-NLS-1$
		externalAddButton.setAlignment(Pos.BASELINE_RIGHT);
		externalAddButton.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle(Messages.getString("ConfigModalDialog.SELECT_DIR")); //$NON-NLS-1$
				File directory = directoryChooser.showDialog(new Stage());
				if (directory != null) {
					List<String> items = toList(externalList.getItems());
					String item = directory.getAbsolutePath();
					if (!items.contains(item)) {
						items.add(item);
						externalList.setItems(FXCollections.observableArrayList(items));
					}
				}

			}
		});
		Button externalRemoveButton = new Button(Messages.getString("ConfigModalDialog.DELETE")); //$NON-NLS-1$
		externalRemoveButton.setAlignment(Pos.BASELINE_RIGHT);
		externalRemoveButton.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				String selectItem = externalList.getSelectionModel().getSelectedItem();
				List<String> items = externalList.getItems();
				items.remove(selectItem);
				externalList.setItems(FXCollections.observableArrayList(items));
			}
		});

		Button externalClearButton = new Button(Messages.getString("ConfigModalDialog.CLEAR")); //$NON-NLS-1$
		externalClearButton.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				ConfigUtil.clear(true);
			}
		});
		externalIncludeContentCheckBox = new CheckBox(Messages.getString("ConfigModalDialog.CONTENT_INDEX")); //$NON-NLS-1$
		btnBox.getChildren().addAll(externalIncludeContentCheckBox, externalAddButton, externalRemoveButton,
				externalClearButton);
		externalBox.getChildren().addAll(externalLabel, btnBox);

		externalList = new ListView<String>();
		externalList.setMaxHeight(listHeight);
		externalList.setPlaceholder(new Label(Messages.getString("ConfigModalDialog.NO_DATA"))); //$NON-NLS-1$

		externalList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		// 设置默认选择项
		externalList.getSelectionModel().select(0);

		HBox blackBox = new HBox();
		blackBox.setMinWidth(width);
		blackBox.maxHeight(20);
		Label blackLabel = new Label(Messages.getString("ConfigModalDialog.BLACK_PATH")); //$NON-NLS-1$
		blackLabel.setMinWidth(200);
		blackLabel.setMaxWidth(200);
		btnBox = new HBox();
		btnBox.setAlignment(Pos.BASELINE_RIGHT);
		btnBox.setMinWidth(width - blackLabel.getWidth() - 200);
		Button blackAddButton = new Button(Messages.getString("ConfigModalDialog.ADD")); //$NON-NLS-1$
		blackAddButton.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle(Messages.getString("ConfigModalDialog.SELECT_DIR")); //$NON-NLS-1$
				File directory = directoryChooser.showDialog(new Stage());
				if (directory != null) {
					List<String> items = toList(blackList.getItems());
					String item = directory.getAbsolutePath();
					if (!items.contains(item)) {
						items.add(item);
						blackList.setItems(FXCollections.observableArrayList(items));
					}
				}

			}
		});
		Button blackRemoveButton = new Button(Messages.getString("ConfigModalDialog.DELETE")); //$NON-NLS-1$
		blackRemoveButton.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				String selectItem = blackList.getSelectionModel().getSelectedItem();
				List<String> items = blackList.getItems();
				items.remove(selectItem);
				blackList.setItems(FXCollections.observableArrayList(items));
			}
		});
		btnBox.getChildren().addAll(blackAddButton, blackRemoveButton);
		blackBox.getChildren().addAll(blackLabel, btnBox);

		blackList = new ListView<String>();
		blackList.setMaxHeight(listHeight);
		blackList.setPlaceholder(new Label(Messages.getString("ConfigModalDialog.NO_DATA"))); //$NON-NLS-1$

		blackList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		// 设置默认选择项
		blackList.getSelectionModel().select(0);

		vbox.getChildren().addAll(insideBox, insideList, externalBox, externalList, blackBox, blackList);
		root.getChildren().addAll(vbox);
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			public void handle(WindowEvent event) {
				saveConfig();
			}
		});
		stage.setScene(scene);
		stage.hide();
	}

	public void loadConfig() {
		ConfigModel model = ConfigUtil.loadConfig();
		insideList.setItems(FXCollections.observableArrayList(model.getInsideDirList()));
		externalList.setItems(FXCollections.observableArrayList(model.getExternalDirList()));
		blackList.setItems(FXCollections.observableArrayList(model.getBlackDirList()));
		insideIncludeContentCheckBox.setSelected(model.getInsideIncludeContent() == ConfigUtil.INCLUDE_CONTENT);
		externalIncludeContentCheckBox.setSelected(model.getExternalIncludeContent() == ConfigUtil.INCLUDE_CONTENT);

	}

	public void saveConfig() {
		ConfigModel model = ConfigUtil.loadConfig();
		model.setInsideDirList(toList(insideList.getItems()));
		model.setExternalDirList(toList(externalList.getItems()));
		model.setBlackDirList(toList(blackList.getItems()));
		model.setInsideIncludeContent(insideIncludeContentCheckBox.isSelected() ? ConfigUtil.INCLUDE_CONTENT
				: ConfigUtil.NOT_INCLUDE_CONTENT);
		model.setExternalIncludeContent(externalIncludeContentCheckBox.isSelected() ? ConfigUtil.INCLUDE_CONTENT
				: ConfigUtil.NOT_INCLUDE_CONTENT);
		ConfigUtil.saveConfig(model);
	}

	private List<String> toList(ObservableList<String> items) {
		List<String> list = new ArrayList<String>();
		for (String item : items) {
			list.add(item);
		}
		return list;
	}

	public void show() {
		loadConfig();
		stage.show();
	}

	public void hide() {
		stage.hide();
	}

	public boolean isShowing() {
		return stage.isShowing();
	}

	public void setTitle(String title) {
		stage.setTitle(title);
	}

}