package org.ayound.desktop.filesearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ayound.desktop.config.ConfigUtil;

import javafx.application.Application;

public class Main {

	public static final Logger logger = LogManager.getLogger(Main.class);
	
	private static void install() {
		for (String file : ConfigUtil.HIGH_LIGHT_FILES) {
			InputStream srcFileIn = null;
			FileOutputStream destOutput = null;
			try {
				srcFileIn = FileSearchApp.class.getClassLoader().getResourceAsStream(file);
				String destFilePath = Paths.get(ConfigUtil.getResourcePath(), file).toString();
				File destFile = new File(destFilePath);
				if (!destFile.exists()) {
					File parentDir = destFile.getParentFile();
					if (!parentDir.exists()) {
						FileUtils.forceMkdir(parentDir);
					}
					destOutput = new FileOutputStream(destFile);
					IOUtils.copyLarge(srcFileIn, destOutput);
				}
			} catch (Exception e) {
				logger.error("Error to copy file ->" + file, e); //$NON-NLS-1$
			} finally {
				if (srcFileIn != null) {
					try {
						srcFileIn.close();
					} catch (IOException e) {
						// ignore
					}
				}
				if (destOutput != null) {
					try {
						srcFileIn.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		}

	}
	public static void main(String[] args) {
		install();
		Application.launch(FileSearchApp.class, args);
	}

}
