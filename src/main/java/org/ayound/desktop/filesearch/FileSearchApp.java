package org.ayound.desktop.filesearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.tika.mime.MediaType;
import org.ayound.desktop.config.ConfigModalDialog;
import org.ayound.desktop.config.ConfigModel;
import org.ayound.desktop.config.ConfigUtil;
import org.ayound.desktop.config.IConfigChangeListener;
import org.ayound.desktop.fileindex.FileIndexService;
import org.ayound.desktop.fileindex.IWorkerListener;
import org.ayound.desktop.fileindex.TikaTool;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class FileSearchApp extends Application {

	public static final Logger logger = LogManager.getLogger(FileSearchApp.class);

	private final TableView<FileModel> table = new TableView<FileModel>();
	private final ObservableList<FileModel> data = FXCollections.observableArrayList();
	TextField queryTextField;
	Label statusLabel;
	Page<FileModel> page;
	TableWithPaginationAndSorting<FileModel> sortTable;
	PreviewModalDialog previewDialog;
	ProgressBar progressBar;
	FileIndexService scanWorker;
	List<String> querySegments = new ArrayList<String>();
	String sortField;
	SimpleBooleanProperty scaning = new SimpleBooleanProperty(false);
	boolean sortType;
	MenuButton scanAllButton;
	MenuItem scanInsideItem;
	MenuItem scanExternalItem;
	MenuItem scanAllItem;
	MenuItem scanStopItem;
	int scanType;
	ToggleGroup group;

	public static final int PAGE_SIZE = 25;

	public void queryFile() {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (page != null) {
					String text = queryTextField.getText();
					page.setQueryCond(text);
					page.setExternal(isExternal());

					page.setSort(getSortField());
					page.setSortType(sortType);
					page.getStatus().set(!page.getStatus().get());
				}

			}
		});
	}

	public boolean isExternal() {
		if (group.getSelectedToggle().getUserData().equals(0)) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void start(final Stage stage) throws IOException, ParseException {
		Scene scene = new Scene(new Group());
		stage.setTitle(Messages.getString("Main.FILE_SEARCH")); //$NON-NLS-1$
		stage.setResizable(false);
		Screen screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds();
		JMetro jMetro = new JMetro(scene, Style.DARK);
		jMetro.setScene(scene);
		jMetro.setAutomaticallyColorPanes(true);
		stage.setMaximized(true);
		stage.setOnCloseRequest(new EventHandler() {

			public void handle(Event event) {
				stopScan();
			}
		});
		double width = bounds.getWidth();
		double height = bounds.getHeight();
		queryTextField = new TextField(""); //$NON-NLS-1$
		// 过滤器现在用户输入
		queryTextField.setMinWidth(width * 0.98 - 260);
		queryTextField.setOnKeyPressed(new EventHandler() {

			public void handle(Event event) {
				KeyEvent keyEvent = (KeyEvent) event;
				if (keyEvent.getCode() == KeyCode.ENTER) {
					queryFile();
				}

			}
		});

		group = new ToggleGroup();

		RadioButton inButton = new RadioButton(Messages.getString("Main.INSIDE")); //$NON-NLS-1$
		inButton.setToggleGroup(group);
		inButton.setUserData(0);
		inButton.setAlignment(Pos.BOTTOM_LEFT);
		inButton.setSelected(true);

		RadioButton outButton = new RadioButton(Messages.getString("Main.EXTERNAL")); //$NON-NLS-1$
		outButton.setToggleGroup(group);
		outButton.setUserData(1);
		outButton.setAlignment(Pos.BOTTOM_LEFT);
		outButton.setSelected(false);

		// 选中某个单选框时输出选中的值
		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
				queryFile();
			}
		});

		HBox inputhbox = new HBox();
		inputhbox.setSpacing(5);
		inputhbox.setFillHeight(true);
		inputhbox.setPadding(new Insets(0, 0, 0, 0));
		inputhbox.setMinWidth(width * 0.98);

		Button configButton = new Button(Messages.getString("Main.CONFIG")); //$NON-NLS-1$
		configButton.setOnMouseClicked(new EventHandler<Event>() {

			public void handle(Event event) {
				ConfigModalDialog dialog = new ConfigModalDialog(stage);
				stopScan();
				dialog.show();
			}
		});

		scanInsideItem = new MenuItem(Messages.getString("FileSearchApp.INSIDE_INDEX_MENU")); //$NON-NLS-1$
		scanInsideItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				startScan(FileIndexService.SCAN_TYPE_INSIDE);
			}
		});
		scanExternalItem = new MenuItem(Messages.getString("FileSearchApp.EXTERNAL_INDEX_MENU")); //$NON-NLS-1$
		scanExternalItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				startScan(FileIndexService.SCAN_TYPE_EXTRENAL);
			}
		});
		scanAllItem = new MenuItem(Messages.getString("FileSearchApp.ALL_INDEX_MENU")); //$NON-NLS-1$
		scanAllItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				startScan(FileIndexService.SCAN_TYPE_ALL);
			}
		});
		scanStopItem = new MenuItem(Messages.getString("FileSearchApp.STOP_INDEX_MENU")); //$NON-NLS-1$
		scanStopItem.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				stopScan();
			}
		});

		scanAllButton = new MenuButton(Messages.getString("Main.CREATE_INDEX")); //$NON-NLS-1$

		scanAllButton.getItems().addAll(scanInsideItem, scanExternalItem, scanAllItem, scanStopItem);

		scaning.addListener(new ChangeListener<Boolean>() {

			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						if (newValue) {
							scanAllButton.setText(Messages.getString("Main.STOP_INDEX")); //$NON-NLS-1$
							scanInsideItem.setDisable(true);
							scanExternalItem.setDisable(true);
							scanAllItem.setDisable(true);
							scanStopItem.setDisable(false);
						} else {
							scanAllButton.setText(Messages.getString("Main.CREATE_INDEX")); //$NON-NLS-1$
							scanInsideItem.setDisable(false);
							scanExternalItem.setDisable(false);
							scanAllItem.setDisable(false);
							scanStopItem.setDisable(true);
						}

					}
				});

			}
		});

		inputhbox.getChildren().addAll(queryTextField, inButton, outButton, configButton, scanAllButton);

		previewDialog = new PreviewModalDialog(stage);
		table.setRowFactory(new Callback<TableView<FileModel>, TableRow<FileModel>>() {

			public TableRow<FileModel> call(TableView<FileModel> param) {
				final TableRow<FileModel> row = new TableRow<FileModel>();
				row.setOnMouseClicked(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						if (event.getClickCount() == 2 && (!row.isEmpty())) {
							openDir();
						}
					}
				});
				return row;
			}
		});
		table.setOnKeyPressed(new EventHandler() {

			public void handle(Event event) {
				KeyEvent keyEvent = (KeyEvent) event;
				if (keyEvent.getCode() == KeyCode.ENTER) {
					if (keyEvent.isAltDown() || keyEvent.isControlDown() || keyEvent.isShiftDown()
							|| keyEvent.isMetaDown()) {
						openFile();
					} else {
						openDir();
					}
				}
				if (keyEvent.getCode() == KeyCode.SPACE) {
					previewDialog.show();
					previewFile();
				}

			}
		});
//		table.setSortPolicy(new Callback<TableView<FileModel>, Boolean>() {
//			
//			public Boolean call(TableView<FileModel> param) {
//				queryFile();
//				return true;
//			}
//		});
		table.setEditable(false);
		table.getSelectionModel().selectedItemProperty().addListener(// 选中某一行
				new ChangeListener<FileModel>() {
					public void changed(ObservableValue<? extends FileModel> observableValue, FileModel oldItem,
							FileModel newItem) {
						if (newItem != null) {
							statusLabel.setText(newItem.getFileAbsolutePath());
							previewFile();
						}
					}
				});

		TableColumn fileNameCol = new TableColumn(Messages.getString("Main.FILE")) { //$NON-NLS-1$
			{
				// 15%
				prefWidthProperty().bind(table.widthProperty().multiply(0.2));
			}
		};
		fileNameCol.setSortable(true);
		fileNameCol.setSortType(SortType.ASCENDING);
		fileNameCol.sortTypeProperty().addListener(new ChangeListener<SortType>() {

			public void changed(ObservableValue<? extends SortType> observable, SortType oldValue, SortType newValue) {
				setSortField("fileName"); //$NON-NLS-1$
				setSortType(newValue == SortType.ASCENDING ? false : true);
				queryFile();
			}
		});
		fileNameCol.setCellValueFactory(new PropertyValueFactory<FileModel, String>("fileName")); //$NON-NLS-1$
		fileNameCol.setCellFactory(new Callback<TableColumn<FileModel, String>, TableCell<FileModel, String>>() {

			public TableCell<FileModel, String> call(TableColumn<FileModel, String> param) {
				TableCell<FileModel, String> cell = new TableCell<FileModel, String>() {

					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						String queryText = queryTextField.getText();
						if (item != null) {
							setText(""); //$NON-NLS-1$
							setGraphic(null);
							setGraphic(computeBox(item));
						} else {
							setGraphic(new Label(item));
						}
//						
					}

				};
				cell.setMaxHeight(16);
				return cell;
			}

		});

		TableColumn filePathCol = new TableColumn(Messages.getString("Main.PATH")) { //$NON-NLS-1$
			{
				// 15%
				prefWidthProperty().bind(table.widthProperty().multiply(0.70));
			}
		};
		filePathCol.setCellValueFactory(new PropertyValueFactory<FileModel, String>("fileDir")); //$NON-NLS-1$
		filePathCol.setCellFactory(new Callback<TableColumn<FileModel, String>, TableCell<FileModel, String>>() {

			public TableCell<FileModel, String> call(TableColumn<FileModel, String> param) {
				TableCell<FileModel, String> cell = new TableCell<FileModel, String>() {

					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						String queryText = queryTextField.getText();
						if (item != null) {
							setText(""); //$NON-NLS-1$
							setGraphic(null);
							setGraphic(computeBox(item));
						} else {
							setGraphic(new Label(item));
						}

					}

				};
				cell.setMaxHeight(16);
				return cell;
			}

		});
		filePathCol.setSortable(true);
		filePathCol.sortTypeProperty().addListener(new ChangeListener<SortType>() {

			public void changed(ObservableValue<? extends SortType> observable, SortType oldValue, SortType newValue) {
				setSortField("fileDir"); //$NON-NLS-1$
				setSortType(newValue == SortType.ASCENDING ? false : true);
				queryFile();
			}
		});
		TableColumn fileSizeCol = new TableColumn(Messages.getString("Main.SIZE")) { //$NON-NLS-1$
			{
				// 15%
				prefWidthProperty().bind(table.widthProperty().multiply(0.06));
			}
		};
		fileSizeCol.setCellFactory(new Callback<TableColumn<FileModel, Float>, TableCell<FileModel, Float>>() {

			public TableCell<FileModel, Float> call(TableColumn<FileModel, Float> param) {
				TableCell<FileModel, Float> cell = new TableCell<FileModel, Float>() {

					@Override
					protected void updateItem(Float item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null) {
							String size = "0B"; //$NON-NLS-1$
							if (item > 1024 * 1024) {
								size = String.format("%.2f", item / (1024 * 1024)) + "Mb"; //$NON-NLS-1$ //$NON-NLS-2$
							} else if (item > 1024) {
								size = String.format("%.2f", item / (1024)) + "Kb"; //$NON-NLS-1$ //$NON-NLS-2$
							} else {
								size = item + "B"; //$NON-NLS-1$
							}
							setGraphic(new Label(size));
						} else {
							setGraphic(new Label("")); //$NON-NLS-1$
						}
					}

				};
				cell.setMaxHeight(16);
				return cell;
			}

		});
		fileSizeCol.setCellValueFactory(new PropertyValueFactory<FileModel, String>("fileSize")); //$NON-NLS-1$
		fileSizeCol.setSortable(true);
		fileSizeCol.sortTypeProperty().addListener(new ChangeListener<SortType>() {

			public void changed(ObservableValue<? extends SortType> observable, SortType oldValue, SortType newValue) {
				setSortField("fileSize"); //$NON-NLS-1$
				setSortType(newValue == SortType.ASCENDING ? false : true);
				queryFile();
			}
		});
		page = new Page<FileModel>(PAGE_SIZE);

		table.getColumns().addAll(fileNameCol, filePathCol, fileSizeCol);
		table.setItems(data);
		table.setMinHeight(height - 170);
		table.setMinWidth(width * 0.99);
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		sortTable = new TableWithPaginationAndSorting<FileModel>(page, table);

		// 右键菜单===================================================================
		// 创建右键菜单
		ContextMenu contextMenu = new ContextMenu();
		// 菜单项

		// 菜单项
		MenuItem openDirItem = new MenuItem(Messages.getString("Main.OPEN_DIR")); //$NON-NLS-1$
		openDirItem.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				openDir();
			}
		});

		// 菜单项
		MenuItem openFileItem = new MenuItem(Messages.getString("Main.OPEN_FILE")); //$NON-NLS-1$
		openFileItem.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				openFile();
			}
		});
		MenuItem searchPathItem = new MenuItem(Messages.getString("Main.SEARCH_DIR")); //$NON-NLS-1$
		searchPathItem.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				searchPath();
			}
		});

		contextMenu.getItems().addAll(openDirItem, openFileItem, searchPathItem);
		// 右键菜单===================================================================
		table.setContextMenu(contextMenu);
		statusLabel = new Label();
		final VBox vbox = new VBox();
		vbox.setFillWidth(true);
		vbox.setSpacing(2);
		vbox.setPadding(new Insets(0, 0, 0, 0));

		HBox hbox = new HBox();
		hbox.setSpacing(2);
		hbox.setFillHeight(true);
		hbox.setPadding(new Insets(0, 0, 0, 0));
		hbox.setMinHeight(height - 90);
		hbox.setMinWidth(width * 0.99);
		sortTable.getTableViewWithPaginationPane().setMinWidth(width * 0.99);
		sortTable.getTableViewWithPaginationPane().setMaxWidth(width * 0.99);
		hbox.getChildren().addAll(sortTable.getTableViewWithPaginationPane());
		progressBar = new ProgressBar(0);
		progressBar.setMinWidth(300);
		progressBar.setVisible(false);
		HBox statusHbox = new HBox();
		statusHbox.setMinWidth(width * 0.99);
		statusHbox.getChildren().addAll(progressBar, statusLabel);
		vbox.getChildren().addAll(inputhbox, hbox, statusHbox);
		vbox.setMinWidth(width);
		vbox.setMinHeight(height);

		((Group) scene.getRoot()).getChildren().addAll(vbox);

		stage.setScene(scene);
		stage.show();
		ConfigUtil.addListener(new IConfigChangeListener() {

			@Override
			public void onChange(ConfigModel oldConfig, ConfigModel newConfig) {
				if (!oldConfig.equals(newConfig)) {
					stopScan();
					startScan(scanType);
				} else {
					if (scaning.get()) {
						stopScan();
						startScan(scanType);
					}
				}
			}

		});
		startScan(FileIndexService.SCAN_TYPE_INSIDE);
	}

	protected void searchPath() {
		FileModel model = table.getSelectionModel().getSelectedItem();
		File file = new File(model.getFileAbsolutePath());
		queryTextField.setText(file.getParent());
		queryFile();
	}

	public HBox computeBox(String value) {
		HBox box = new HBox();
		box.setStyle("-fx-background-color: transparent"); //$NON-NLS-1$
		if (StringUtils.isEmpty(value)) {
			return box;
		}

		String[] items = StringUtils.splitByWholeSeparator(value, "|||"); //$NON-NLS-1$

		for (int i = 0; i < items.length; i++) {
			String item = items[i];
			Label childLabel = new Label();
			String itemText = item;
			if (item.endsWith("}$")) { //$NON-NLS-1$
				itemText = StringUtils.substringBeforeLast(item, "}$"); //$NON-NLS-1$
				childLabel.setStyle("-fx-text-fill: rgba(255,0,0,0.96);-fx-font-weight: bold"); //$NON-NLS-1$
				Text theText = new Text(itemText);
				double width = theText.getBoundsInLocal().getWidth();
				childLabel.setMinWidth(width);
			}
			childLabel.setText(itemText);
			box.getChildren().add(childLabel);
		}
		return box;
	}

	protected void stopScan() {
		if (getScaning().get()) {
			scanWorker.stop();
		}
	}

	protected void startScan(int scanType) {
		this.scanType = scanType;
		getScaning().set(true);
		statusLabel.setText(Messages.getString("Main.WAITING_SCAN")); //$NON-NLS-1$
		scanWorker = new FileIndexService(ConfigUtil.loadConfig(), scanType);
		progressBar.progressProperty().unbind();
		progressBar.progressProperty().bind(scanWorker.progressProperty());
		progressBar.setVisible(true);
		scanWorker.messageProperty().addListener(new ChangeListener<String>() {
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				statusLabel.setText(newValue);
			}
		});
		scanWorker.addWorkerListener(new IWorkerListener() {

			@Override
			public void onStop() {

				getScaning().set(false);
				queryFile();

			}

			@Override
			public void onStart() {
				// TODO Auto-generated method stub

			}
		});
		new Thread(scanWorker).start();
	}

	protected void previewFile() {
		if (!previewDialog.isShowing()) {
			return;
		}
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				doPreviewFile();

			}
		});
	}

	protected boolean doPreviewFile() {

		FileModel model = table.getSelectionModel().getSelectedItem();
		File modelFile = new File(model.getFileAbsolutePath());
		String fileName = modelFile.getName();
		float size = model.getFileSize();
		String lowerName = fileName.toLowerCase();
		previewDialog.setTitle(fileName);
		if (needPreviewResult(lowerName, size) || !modelFile.exists()) {
			String text = model.getText();
			if (text == null) {
				text = ""; //$NON-NLS-1$
			}
			if (!StringUtils.isEmpty(text)) {
				text = page.highlightText(text);
				previewDialog.loadText("<body style='background-color:#252525;color:white;'>" + text + "</body>"); //$NON-NLS-1$ //$NON-NLS-2$
				return true;
			}
		}
		if (lowerName.endsWith(".pdf")) { //$NON-NLS-1$
			previewDialog.loadPDF(model.getFileAbsolutePath());
			return true;
		}
		if (lowerName.endsWith(".eml") || lowerName.endsWith(".emlx")) { //$NON-NLS-1$ //$NON-NLS-2$
			String email = readEmail(modelFile);
			previewDialog.loadText(email);
			return true;
		}
		boolean isCode = false;
		for (String code : ConfigUtil.CODE_PREVIEW_END_FIX) {
			if (lowerName.endsWith("." + code)) { //$NON-NLS-1$
				isCode = true;
				break;
			}
		}

		boolean needMacPreview = true;

		for (String end : ConfigUtil.NO_PREVIEW_END_FIX) {
			if (lowerName.endsWith("." + end)) { //$NON-NLS-1$
				needMacPreview = false;
				break;
			}
		}
		Path outpath = Paths.get(System.getProperty("user.home"), ".filesearch", "preview", model.getId()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (needMacPreview && !isCode) {
			try {
				if (!outpath.toFile().exists()) {
					FileUtils.forceMkdir(outpath.toFile());
				}

				Path htmlPath = Paths.get(outpath.toAbsolutePath().toString(), fileName + ".qlpreview", "Preview.html"); //$NON-NLS-1$ //$NON-NLS-2$
				if (FileUtils.listFiles(outpath.toFile(), new String[] { "url", "html" }, true).size() == 0) { //$NON-NLS-1$ //$NON-NLS-2$
					CommandUtil.executeCommand("/usr/bin/qlmanage", "-p", model.getFileAbsolutePath(), "-o", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							outpath.toAbsolutePath().toString());
				}
				if (htmlPath.toFile().exists()) {
					previewDialog.loadUrl(htmlPath.toUri().toString());
					return true;
				}
				htmlPath = Paths.get(outpath.toAbsolutePath().toString(), fileName + ".qlpreview", "Preview.url"); //$NON-NLS-1$ //$NON-NLS-2$
				if (htmlPath.toFile().exists()) {
					String url = URLDecoder.decode(FileUtils.readFileToString(htmlPath.toFile()));
					previewDialog.loadUrl(url);
					return true;
				}

				if (MediaType.TEXT_PLAIN == TikaTool.detectType(modelFile)) {
					return previewTextFile(modelFile, size);
				}

				if (!StringUtils.isEmpty(model.getText())) {
					previewDialog.loadText(model.getText());
					return true;
				}

			} catch (IOException e) {
				logger.error("error to preview ->" + modelFile.getAbsolutePath(), e); //$NON-NLS-1$
			}
		} else {
			if (isCode || lowerName.endsWith(".eml") || MediaType.TEXT_PLAIN == TikaTool.detectType(modelFile)) { //$NON-NLS-1$
				return previewTextFile(modelFile, size);
			}
		}
		if (size < 1 * 1024 * 1024) {
			previewDialog.loadUrl("file://" + model.getFileAbsolutePath()); //$NON-NLS-1$
		} else {
			previewDialog.loadText(getText(model));
		}
		return true;

	}

	private boolean needPreviewResult(String lowerName, float size) {
		if (!queryTextField.getText().startsWith("t:")) { //$NON-NLS-1$
			return false;
		}
		if (size == 0) {
			return false;
		}
		for (String type : ConfigUtil.INDEX_FILE_CONTENT_TYPES) {
			if (lowerName.endsWith("." + type)) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}

	private String getText(FileModel model) {
		String str = ""; //$NON-NLS-1$
		str += "<h4>" + model.getFileAbsolutePath() + "</h4>"; //$NON-NLS-1$ //$NON-NLS-2$
		return str;
	}

	public boolean previewTextFile(File modelFile, float size) {
		String fileName = modelFile.getName();
		try {
			Charset charset = TikaTool.detect(modelFile);
			if (size < 100 * 1024) {
				String text = FileUtils.readFileToString(modelFile, charset);
				previewDialog.loadCode(StringUtils.substringAfter(fileName, "."), text); //$NON-NLS-1$
				return true;
			} else {
				String text = FileTopLineUtil.readBigFile(modelFile, 2048, charset);
				previewDialog.loadCode(StringUtils.substringAfter(fileName, "."), text + "\n......"); //$NON-NLS-1$ //$NON-NLS-2$
				return true;
			}

		} catch (IOException e) {
			logger.error("error to preview text file ->" + modelFile.getAbsolutePath(), e); //$NON-NLS-1$
		}
		return false;
	}

	private String readEmail(File modelFile) {
		Properties props = System.getProperties();
		props.put("mail.host", "smtp.test.com"); //$NON-NLS-1$ //$NON-NLS-2$
		props.put("mail.transport.protocol", "smtp"); //$NON-NLS-1$ //$NON-NLS-2$
		String email = ""; //$NON-NLS-1$
		InputStream source = null;
		try {
			Session mailSession = Session.getDefaultInstance(props, null);
			source = new FileInputStream(modelFile);
			MimeMessage message = new MimeMessage(mailSession, source);
			if (message == null) {
				logger.error("error to read xml ->" + modelFile.getAbsolutePath()); //$NON-NLS-1$
				return null;
			}
			email = email + "<h4>" + message.getSubject() + "</h4>"; //$NON-NLS-1$ //$NON-NLS-2$
			if (message.getFrom() != null && message.getFrom().length > 0) {
				email = email + "From : " + message.getFrom()[0].toString() + "<br/>"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (message.getRecipients(RecipientType.TO) != null && message.getRecipients(RecipientType.TO).length > 0) {
				email = email + "To : " + message.getRecipients(RecipientType.TO)[0].toString() + "<br/>"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			email = email + "Body : "; //$NON-NLS-1$
			Object content = message.getContent();
			if (content instanceof Multipart) {

				// 得到邮件的Multipart（内容总部件--【包涵附件】）
				Multipart multipart = (Multipart) message.getContent();
				int count = multipart.getCount(); // 部件个数
				for (int i = 0; i < count; i++) {
					// 单个部件 注意：单个部件有可能又为一个Multipart，层层嵌套
					BodyPart part = multipart.getBodyPart(i);
					// 单个部件类型
					String type = part.getContentType().split(";")[0]; //$NON-NLS-1$
					/**
					 * 类型众多，逐一判断，其中TEXT、HTML类型可以直接用字符串接收，其余接收为内存地址 可能不全，如有没判断住的，请自己打印查看类型，在新增判断
					 */
					if (type.equals("multipart/alternative")) { // HTML （文本和超文本组合） //$NON-NLS-1$
						Multipart m = (Multipart) part.getContent();
						for (int k = 0; k < m.getCount(); k++) {
							if (m.getBodyPart(k).getContentType().startsWith("text/plain")) { //$NON-NLS-1$
								// 处理文本正文
								email = email + ("\n" + m.getBodyPart(k).getContent().toString().trim() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
							} else {
								// 处理 HTML 正文
								email = email + ("\n" + m.getBodyPart(k).getContent() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
							}
						}
					} else if (type.equals("text/plain")) { // 纯文本 //$NON-NLS-1$
						email = email + (part.getContent().toString());
					} else if (type.equals("text/html")) { // HTML标签元素 //$NON-NLS-1$
						email = email + (part.getContent().toString());
					} else if (type.equals("multipart/related")) { // 内嵌资源 (包涵文本和超文本组合) //$NON-NLS-1$
						email = email + (part.getContent().toString());
					} else if (type.contains("application/")) { // 应用附件 （zip、xls、docx等） //$NON-NLS-1$
						email = email + (part.getContent().toString());
					} else if (type.contains("image/")) { // 图片附件 （jpg、gpeg、gif等） //$NON-NLS-1$
						email = email + (part.getContent().toString());
					}

				}
			} else {
				return content.toString();
			}
		} catch (Exception e) {
			logger.error("error to parse email ->" + modelFile.getAbsolutePath(), e); //$NON-NLS-1$
		} finally {
			try {
				source.close();
			} catch (IOException e) {
				// ignore
			}
		}
		return email;
	}

	protected void openFile() {
		FileModel model = table.getSelectionModel().getSelectedItem();
		if (!new File(model.getFileAbsolutePath()).exists()) {
			statusLabel.setText(
					String.format(Messages.getString("FileSearchApp.FILE_NOT_EXIST"), model.getFileAbsolutePath())); //$NON-NLS-1$
			return;
		}
		CommandUtil.executeCommand("/usr/bin/open", "-n", model.getFileAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$

	}

	protected void openDir() {
		FileModel model = table.getSelectionModel().getSelectedItem();
		if (!new File(model.getFileAbsolutePath()).exists()) {
			statusLabel.setText(
					String.format(Messages.getString("FileSearchApp.FILE_NOT_EXIST"), model.getFileAbsolutePath())); //$NON-NLS-1$
			return;
		}
		CommandUtil.executeCommand("/usr/bin/open", "-n", "-R", model.getFileAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	}

	public List<String> getQuerySegments() {
		return querySegments;
	}

	public void setQuerySegments(List<String> querySegments) {
		this.querySegments = querySegments;
	}

	public String getSortField() {
		return sortField;
	}

	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	public boolean isSortType() {
		return sortType;
	}

	public void setSortType(boolean sortType) {
		this.sortType = sortType;
	}

	public SimpleBooleanProperty getScaning() {
		return scaning;
	}

}