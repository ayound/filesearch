package org.ayound.desktop.filesearch.pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ayound.desktop.config.ConfigUtil;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

public class PdfViewer {
	public static final Logger logger = LogManager.getLogger(PdfViewer.class);

	String pdfPath;
	private Pagination pdfViewWithPaginationPane;
	ScrollPane scrollPane;
	ObjectProperty<ImageView> currentImage;
	private SimpleIntegerProperty currentPage;
	private SimpleIntegerProperty pageCount;
	double width;
	double height;
	Document document = null;

	public PdfViewer(String pdfPath, double width, double height) {
		super();
		if (pdfPath == null) {
			pdfPath = Paths.get(ConfigUtil.getResourcePath(), "test.pdf").toString();
		}
		this.pdfPath = pdfPath;
		int maxPage = 0;
		if (pdfPath != null) {
			maxPage = getpageCount();
		}

		this.width = width;
		this.height = height;
		scrollPane = new ScrollPane();
		ImageView imageView = new ImageView();
		currentImage = new SimpleObjectProperty<ImageView>(imageView);
		scrollPane.contentProperty().bind(currentImage);
		pdfViewWithPaginationPane = new Pagination();
		pdfViewWithPaginationPane.setMaxPageIndicatorCount(20);
		currentPage = new SimpleIntegerProperty();
		pageCount = new SimpleIntegerProperty(maxPage);
		pdfViewWithPaginationPane.setPrefSize(width, height);
		pdfViewWithPaginationPane.currentPageIndexProperty().bindBidirectional(currentPage);
		pdfViewWithPaginationPane.pageCountProperty().bindBidirectional(pageCount);
		pdfViewWithPaginationPane.setPageFactory(new Callback<Integer, Node>() {

			public Node call(Integer pageIndex) {
				PdfPage page = getPage(pageIndex);
				Image image = page.getData();
				currentImage.get().setFitWidth(getWidth());
				currentImage.get().setFitHeight(page.getHeight() * getWidth() / page.getWidth());
				currentImage.get().setImage(image);

				return scrollPane;
			}
		});

	}

	public int getpageCount() {
		document = new Document();
		if (this.pdfPath != null) {
			try {
				document.setFile(pdfPath);
			} catch (Exception e) {
				logger.error("error to get page count ->" + pdfPath, e);
			}
		}
		return document.getNumberOfPages();
	}

	public PdfPage getPage(int page) {

		PdfPage pdfPage = new PdfPage();
		document = new Document();
		if (page > pageCount.doubleValue()) {
			return pdfPage;
		}
		if (this.pdfPath != null) {
			try {
				document.setFile(pdfPath);
			} catch (Exception e) {
				logger.error("error to set file ->" + pdfPath, e);
			}
			float rotation = 0f;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {

				BufferedImage img = (BufferedImage) document.getPageImage(page, GraphicsRenderingHints.SCREEN,
						Page.BOUNDARY_ARTBOX, rotation, 1);
				pdfPage.setWidth(img.getWidth());
				pdfPage.setHeight(img.getHeight());
				pdfPage.setData(javafx.embed.swing.SwingFXUtils.toFXImage(img, null));
			} catch (Exception e) {
				logger.error("error to get page ->" + pdfPath, e);

			} finally {
				try {
					out.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
		return pdfPage;
	}

	public Pagination getPdfViewWithPaginationPane() {
		return pdfViewWithPaginationPane;
	}

	public ScrollPane getScrollPane() {
		return scrollPane;
	}

	public void setScrollPane(ScrollPane scrollPane) {
		this.scrollPane = scrollPane;
	}

	public String getPdfPath() {
		return pdfPath;
	}

	public void setPdfPath(String pdfPath) {
		this.pdfPath = pdfPath;
		if (this.pdfPath != null) {
			pageCount.set(getpageCount());
			this.currentPage.set(0);
		}
	}

	public SimpleIntegerProperty getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(SimpleIntegerProperty currentPage) {
		this.currentPage = currentPage;
	}

	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

}
