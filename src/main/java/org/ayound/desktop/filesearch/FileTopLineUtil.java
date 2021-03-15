package org.ayound.desktop.filesearch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ayound.desktop.filesearch.pdf.PdfViewer;

public class FileTopLineUtil {
	public static final Logger logger = LogManager.getLogger(FileTopLineUtil.class);


	public static String readBigFile(File sourceFile, int maxLength, Charset charset) {
		ChannelFileReader reader = null;
		String result = "";
		try {
			reader = new ChannelFileReader(sourceFile.getAbsolutePath(), maxLength);
			reader.read();
			result = new String(reader.getArray(), charset);			
		} catch (IOException e) {
			logger.error("error to get read file ->" + sourceFile.getAbsolutePath(), e);
		}finally {
			if(reader!=null) {
				try {
					reader.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
		return result;
	}

}