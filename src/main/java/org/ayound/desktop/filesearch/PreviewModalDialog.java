package org.ayound.desktop.filesearch;

import java.nio.file.Paths;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ayound.desktop.config.ConfigUtil;
import org.ayound.desktop.filesearch.pdf.PdfViewer;

import com.google.gson.JsonObject;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class PreviewModalDialog {
	public static final Logger logger = LogManager.getLogger(PreviewModalDialog.class);
	
	WebView previewWebView;
	WebView previewCodeView;
	PdfViewer pdfViewer;
	Stage stage;

	public PreviewModalDialog(final Stage stg) {

		stage = new Stage();
		// Initialize the Stage with type of modal
		stage.initModality(Modality.NONE);
		// Set the owner of the Stage
		stage.initOwner(stg);
		stage.setTitle("预览文件");
		Group root = new Group();
		Scene scene = new Scene(root, 800, 600);
		stage.setResizable(false);
		JMetro jMetro = new JMetro(scene,Style.DARK);
		jMetro.setScene(scene);
		jMetro.setAutomaticallyColorPanes(true);

		previewWebView = new WebView();
		previewWebView.getEngine().loadContent("<body style='background:#252525;'></body>");
		previewCodeView = new WebView();
		String url = "file://"
				+ Paths.get(ConfigUtil.getResourcePath(), "preview.htm?t=_" + System.currentTimeMillis());
		previewCodeView.getEngine().load(url);
		previewCodeView.setVisible(false);
		pdfViewer = new PdfViewer(null, 800, 600);
		pdfViewer.getPdfViewWithPaginationPane().setVisible(false);
		root.getChildren().addAll(previewWebView, pdfViewer.getPdfViewWithPaginationPane(), previewCodeView);
		stage.setScene(scene);
		stage.hide();
	}

	public void show() {
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

	public void loadUrl(String url) {
		pdfViewer.getPdfViewWithPaginationPane().setVisible(false);
		previewCodeView.setVisible(false);
		previewWebView.setVisible(true);
		try {
			previewWebView.getEngine().executeScript("document.write('');");
		} catch (Exception e) {
			logger.error("error to load url ->" + url, e);
		}
		previewWebView.getEngine().load(url);
	}

	public void loadCode(String language, final String data) {
		if(StringUtils.isEmpty(language)) {
			language = "txt";
		}
		pdfViewer.getPdfViewWithPaginationPane().setVisible(false);
		previewWebView.setVisible(false);
		previewCodeView.setVisible(true);
		JsonObject json = new JsonObject();
		json.addProperty("lg", language);
		json.addProperty("data", data);
		previewCodeView.getEngine().executeScript("showData(" + json.toString() + ")");

	}

	public void loadText(String text) {
		pdfViewer.getPdfViewWithPaginationPane().setVisible(false);
		previewCodeView.setVisible(false);
		previewWebView.setVisible(true);
		previewWebView.getEngine().loadContent(text);
	}

	public void loadPDF(String url) {
		previewWebView.setVisible(false);
		previewCodeView.setVisible(false);
		pdfViewer.getPdfViewWithPaginationPane().setVisible(true);
		pdfViewer.setPdfPath(url);
	}

}