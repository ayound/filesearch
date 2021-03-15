package org.ayound.desktop.fileindex;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.detect.AutoDetectReader;
import org.apache.tika.detect.TextDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;

public class TikaTool {

	public static final Logger logger = LogManager.getLogger(TikaTool.class);

	public static Map<String, Object> parseFile(File file) {
		Map<String, Object> meta = new HashMap<String, Object>();
		Parser parser = new AutoDetectParser();
		InputStream input = null;
		try {
			Metadata metadata = new Metadata();
			metadata.set(Metadata.CONTENT_ENCODING, "utf-8");
			metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
			input = new FileInputStream(file);
			BodyContentHandler handler = new BodyContentHandler(10 * 1024 * 1024);
			ParseContext context = new ParseContext();
			context.set(Parser.class, parser);
			parser.parse(input, handler, metadata, context);
			for (String name : metadata.names()) {
				meta.put(name, metadata.get(name));
			}
			String content = handler.toString();
			if (StringUtils.isNotEmpty(content)) {
				content = StringUtils.replace(content, "\t|\r|\n|\f", "");

				meta.put("content", content);
			} else {
				meta.put("content", content);
			}
			return meta;
		} catch (Exception e) {
			logger.error("error to parse file ->" + file.getAbsolutePath());
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
		return null;
	}

	public static Charset detect(File file) {
		AutoDetectReader detector = null;
		FileInputStream inputStream = null;
		Charset result = null;
		try {
			inputStream = new FileInputStream(file);
			detector = new AutoDetectReader(inputStream);
			result = detector.getCharset();
		} catch (FileNotFoundException e) {
			logger.error("error to detect file ->" + file.getAbsolutePath());
		} catch (IOException e) {
			logger.error("error to detect file ->" + file.getAbsolutePath());
		} catch (TikaException e) {
			logger.error("error to detect file ->" + file.getAbsolutePath());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
			if (detector != null) {
				try {
					detector.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return result;
	}

	public static MediaType detectType(File file) {
		TextDetector detector = new TextDetector();

		FileInputStream inputStream = null;
		MediaType result = MediaType.EMPTY;
		try {
			inputStream = new FileInputStream(file);
			result = detector.detect(new BufferedInputStream(inputStream), new Metadata());
		} catch (Exception e) {
			logger.error("error to detect file type ->" + file.getAbsolutePath());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return result;

	}

	public static void main(String[] args) {
		String path = "/Users/ayound/Library/Mail/V8/EFDD4461-237E-44D7-8C36-26C2BAF474A1/INBOX.mbox/F6863BFB-A54A-4D42-911E-DE1618805028/Data/9/4/1/Attachments/149340/5/2.png";
		System.out.println(detectType(new File(path)));
	}

}