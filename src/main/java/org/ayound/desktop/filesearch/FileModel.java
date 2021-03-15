package org.ayound.desktop.filesearch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class FileModel {
	public static final Logger logger = LogManager.getLogger(FileModel.class);

	private SimpleStringProperty id;
	private SimpleStringProperty fileName;
	private SimpleFloatProperty fileSize;
	private SimpleStringProperty fileLastModified;
	private SimpleStringProperty fileAbsolutePath;
	private SimpleStringProperty fileDir;
	private SimpleStringProperty author;
	private SimpleStringProperty text;
	private SimpleStringProperty title;

	public FileModel(String id, String fileName, String fileSize, String fileLastModified, String fileAbsolutePath,
			String author, String text, String title, String fileDir) {
		super();
		this.id = new SimpleStringProperty(id);
		this.fileName = new SimpleStringProperty(fileName);
		this.fileSize = new SimpleFloatProperty(Float.parseFloat(fileSize));
		this.fileLastModified = new SimpleStringProperty(fileLastModified);
		this.fileAbsolutePath = new SimpleStringProperty(fileAbsolutePath);
		this.author = new SimpleStringProperty(author);
		this.text = new SimpleStringProperty(text);
		this.title = new SimpleStringProperty(title);
		this.fileDir = new SimpleStringProperty(fileDir);
	}

	public String getId() {
		return id.get();
	}

	public void setId(String id) {
		this.id.set(id);
	}

	public String getFileName() {
		return fileName.get();
	}

	public void setFileName(String fileName) {
		this.fileName.set(fileName);
	}

	public float getFileSize() {
		return fileSize.get();
	}

	public void setFileSize(String fileSize) {

		this.fileSize.set(Float.parseFloat(fileSize));
	}

	public String getFileLastModified() {
		return fileLastModified.get();
	}

	public void setFileLastModified(String fileLastModified) {
		this.fileLastModified.set(fileLastModified);
	}

	public String getFileAbsolutePath() {
		return fileAbsolutePath.get();
	}

	public void setFileAbsolutePath(String fileAbsolutePath) {
		this.fileAbsolutePath.set(fileAbsolutePath);
	}

	public String getAuthor() {
		return author.get();
	}

	public void setAuthor(String author) {
		this.author.set(author);
	}

	public String getText() {
		return text.get();
	}

	public void setText(String text) {
		this.text.set(text);
	}

	public String getTitle() {
		return title.get();
	}

	public void setTitle(String title) {
		this.title.set(title);
	}

	public String getFileDir() {
		return fileDir.get();
	}

	public void setFileDir(String fileDir) {
		this.fileDir.set(fileDir);
	}

}
