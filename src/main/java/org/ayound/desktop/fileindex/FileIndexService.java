package org.ayound.desktop.fileindex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.ayound.desktop.config.ConfigModel;
import org.ayound.desktop.config.ConfigUtil;

import javafx.concurrent.Task;

public class FileIndexService extends Task {
	public static final Logger logger = LogManager.getLogger(FileIndexService.class);

	public static final int SCAN_TYPE_INSIDE = 0;

	public static final int SCAN_TYPE_EXTRENAL = 1;

	public static final int SCAN_TYPE_ALL = 2;

	List<IWorkerListener> listeners = new ArrayList<IWorkerListener>();

	public int status = 0;

	ConfigModel config;
	private long fileCount = -1;
	private long fileIndex = 0;

	long lastInsideDate = 0;
	long lastExternalDate = 0;

	private int scanType = SCAN_TYPE_ALL;

	public FileIndexService(ConfigModel config, int scanType) {
		super();
		this.config = config;
		this.scanType = scanType;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * 每次都生成索引文件
	 * 
	 * @throws Exception
	 */
	public void parseDir(String dirPath, boolean isExtend, int level) throws Exception {
		if (level == 0) {
			fileIndex++;
		}
		updateMessage("scan dir-> " + fileIndex + "/" + getFileCountDesc() + " ->" + dirPath); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (fileCount > 0) {
			updateProgress(fileIndex, fileCount);
		} else {
			updateProgress(0, 1);
		}
		if (this.status == -1) {
			return;
		}
		if (dirPath == null) {
			return;
		}
		if (dirPath.startsWith(ConfigUtil.getFileIndexPath())) {
			return;
		}
		if (dirPath.startsWith(".")) {
			return;
		}
		boolean isSystemDir = false;
		for (String systemDir : ConfigUtil.SYSTEM_DIRS) {
			if (dirPath.startsWith(systemDir)) {
				isSystemDir = true;
				break;
			}
		}
		if (isSystemDir) {
			return;
		}

		boolean isBlackDir = false;

		for (String blackDir : config.getBlackDirList()) {
			if (dirPath.startsWith(blackDir)) {
				isBlackDir = true;
				break;
			}
		}
		if (isBlackDir) {
			return;
		}
		Path rootPath = Paths.get(dirPath);
		File dirFile = rootPath.toFile();
		if (!dirFile.exists()) {
			return;
		}
		if (dirFile.isHidden()) {
			return;
		}
		File[] childs = rootPath.toFile().listFiles();
		FSDirectory dir = FSDirectory.open(Paths.get(ConfigUtil.getStorePath(isExtend)));
		IndexWriter indexWriter = getIndexWriter(dir);
		try {

			indexWriter.commit();
			FSDirectory readDir = FSDirectory.open(Paths.get(ConfigUtil.getStorePath(isExtend)));
			IndexSearcher indexSearcher = getIndexSearcher(readDir);
			for (File file : childs) {
				updateMessage(
						"scan file-> " + fileIndex + "/" + getFileCountDesc() + " dir->" + file.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (this.status == -1) {
					return;
				}
				if (file.isFile() && !file.isHidden()) {

					String name = file.getName();
					boolean inBlack = false;

					for (String blackName : ConfigUtil.BLACK_FILE) {
						if (name.endsWith(blackName)) {
							inBlack = true;
							break;
						}
					}
					if (name.indexOf(".log.") > 0) { //$NON-NLS-1$
						inBlack = true;
					}
					if (inBlack) {
						continue;
					}

					try {
						if (!isExtend && file.lastModified() < lastInsideDate) {
							if (config.getInsideIncludeContent() != ConfigUtil.INCLUDE_CONTENT) {
								continue;
							}
						}
						if (isExtend && file.lastModified() < lastExternalDate) {
							if (config.getExternalIncludeContent() != ConfigUtil.INCLUDE_CONTENT) {
								continue;
							}
						}
						String fileAbsolutePath = file.getAbsolutePath();

						long fileSize = FileUtils.sizeOf(file);
						String fileName = file.getName();
						String fileDir = file.getParentFile().getAbsolutePath();
						String sha1 = SHA1.encode(fileAbsolutePath);
						Term term = new Term("id", sha1); //$NON-NLS-1$
						TopDocs topDocs = indexSearcher.search(new TermQuery(term), 1);
						boolean configParse = (isExtend
								&& config.getExternalIncludeContent() == ConfigUtil.INCLUDE_CONTENT)
								|| ((!isExtend) && config.getInsideIncludeContent() == ConfigUtil.INCLUDE_CONTENT);
						if (topDocs.scoreDocs.length > 0) {
							ScoreDoc scoreDoc = topDocs.scoreDocs[0];
							Document savedDoc = indexSearcher.doc(scoreDoc.doc);
							if (StringUtils.equals(String.valueOf(fileSize), savedDoc.get("fileSize"))) { //$NON-NLS-1$
								if (configParse) {
									if (StringUtils.equals(savedDoc.get("includeContent"), //$NON-NLS-1$
											String.valueOf(ConfigUtil.INCLUDE_CONTENT))) {
										continue;
									}
								} else {
									continue;
								}
								logger.info(
										"find scaned file ->" + fileIndex + "/" + fileCount + " ->" + fileAbsolutePath); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							}
						}
						Document doc = new Document();
						doc.add(new StringField("id", sha1, Field.Store.YES)); //$NON-NLS-1$
						doc.add(new StoredField("fileLastModified", file.lastModified())); //$NON-NLS-1$
						doc.add(new StoredField("fileSize", fileSize)); //$NON-NLS-1$
						doc.add(new NumericDocValuesField("fileSize", fileSize)); //$NON-NLS-1$

						doc.add(new StoredField("includeContent", //$NON-NLS-1$
								configParse ? ConfigUtil.INCLUDE_CONTENT : ConfigUtil.NOT_INCLUDE_CONTENT));
						doc.add(new StringField("fileDir", fileDir, Field.Store.YES)); //$NON-NLS-1$
						doc.add(new SortedDocValuesField("fileDir", new BytesRef(fileDir))); //$NON-NLS-1$
						doc.add(new StringField("fileAbsolutePath", fileAbsolutePath, Field.Store.YES)); //$NON-NLS-1$
//						doc.add(new SortedDocValuesField("fileAbsolutePath", new BytesRef(fileDir)));
						doc.add(new StringField("fileName", fileName, Field.Store.YES)); //$NON-NLS-1$
						doc.add(new SortedDocValuesField("fileName", new BytesRef(fileName))); //$NON-NLS-1$
						if (configParse) {
							boolean needParse = false;
							for (String needParseName : ConfigUtil.INDEX_FILE_CONTENT_TYPES) {
								if (fileName.toLowerCase().endsWith("." + needParseName)) { //$NON-NLS-1$
									needParse = true;
									break;
								}
							}
							if (fileSize == 0) {
								needParse = false;
							}
							if (needParse) {
								try {
									String author = ""; //$NON-NLS-1$
									Map<String, Object> meta = TikaTool.parseFile(file);
									String text = "";
									if (meta.containsKey("content")) {
										text = String.valueOf(meta.get("content"));
									} // $NON-NLS-1$
									if (StringUtils.isEmpty(text)) {
										text = String.valueOf(meta);
									}

									author = String.valueOf(meta.get("Author")); //$NON-NLS-1$
									String title = String.valueOf(meta.get("dc:title")); //$NON-NLS-1$

									doc.add(new StringField("author", author, Field.Store.YES)); //$NON-NLS-1$
									doc.add(new TextField("title", title, Field.Store.YES)); //$NON-NLS-1$
									doc.add(new TextField("text", text, Field.Store.YES));

								} catch (Exception e) {
									logger.error("error parse file ->" + file, e); //$NON-NLS-1$
								}
							}
						}

						indexWriter.updateDocument(term, doc);
					} catch (Exception e) {
						logger.error("error parse file ->" + file, e); //$NON-NLS-1$
					}
				}
			}
		} catch (Exception e) {
			logger.error("error parse dir ->" + dirPath, e); //$NON-NLS-1$
		} finally {
			try {
				indexWriter.commit();
				indexWriter.close();
			} catch (Exception e) {
				// ignore
			}
		}
		for (File file : childs) {
			if (file.isDirectory() && !file.isHidden()) {
				if (this.status == -1) {
					return;
				}
				if (StringUtils.equals(file.getAbsolutePath(), file.getCanonicalPath())) {
					parseDir(file.getAbsolutePath(), isExtend, level++);
				}
			}
		}
	}

	private String getFileCountDesc() {
		if (fileCount > 0) {
			return fileCount + Messages.getString("FileIndexService.DIRS"); //$NON-NLS-1$
		}
		return Messages.getString("FileIndexService.DIR_STATICING"); //$NON-NLS-1$
	}

	/**
	 * 获取索引输出流
	 * 
	 * @return
	 * @throws Exception
	 */
	private IndexWriter getIndexWriter(FSDirectory dir) throws Exception {
//        Analyzer analyzer = new StandardAnalyzer();
		Analyzer analyzer = new SmartChineseAnalyzer();
		IndexWriterConfig conf = new IndexWriterConfig(analyzer);
		return new IndexWriter(dir, conf);
	}

	private IndexSearcher getIndexSearcher(FSDirectory dir) throws Exception {
		IndexReader r = DirectoryReader.open(dir);
		// 创建核心对象
		return new IndexSearcher(r);
	}

	@Override
	protected Object call() throws Exception {
		for (IWorkerListener listener : listeners) {
			listener.onStart();
		}
		long start = System.currentTimeMillis();
		lastInsideDate = ConfigUtil.getLastDate(ConfigUtil.INSIDE_LAST_DATE_KEY);
		lastExternalDate = ConfigUtil.getLastDate(ConfigUtil.EXTERNAL_LAST_DATE_KEY);
		updateProgress(0, 1);
		new Thread() {

			@Override
			public void run() {
				long fileCount = 0;
				if (scanType == SCAN_TYPE_INSIDE || scanType == SCAN_TYPE_ALL) {
					for (String inside : config.getInsideDirList()) {
						fileCount = fileCount + getFileDirCount(inside);
					}
				}
				if (scanType == SCAN_TYPE_EXTRENAL || scanType == SCAN_TYPE_ALL) {
					for (String external : config.getExternalDirList()) {
						fileCount = fileCount + getFileDirCount(external);
					}
				}
				setFileCount(fileCount);
			}
		}.start();

		if (this.scanType == SCAN_TYPE_INSIDE || scanType == SCAN_TYPE_ALL) {
			for (String inside : config.getInsideDirList()) {
				parseDir(inside, false, 0);
			}
			writeInsideDate();
			clearDeleteFile(false);
		}
		if (this.scanType == SCAN_TYPE_EXTRENAL || scanType == SCAN_TYPE_ALL) {
			for (String external : config.getExternalDirList()) {
				parseDir(external, true, 0);
			}
			writeExternalDate();
		}
		long end = System.currentTimeMillis();
		if (!isCancelled()) {
			updateProgress(fileCount, fileCount);
			updateMessage(Messages.getString("FileIndexService.COMPLETE_INDEX") + ((end - start) / 1000) + "s"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			updateMessage(Messages.getString("FileIndexService.CANCEN_INDEX") + ((end - start) / 1000) + "s"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		updateProgress(-1, -1);
		for (IWorkerListener listener : listeners) {
			listener.onStop();
		}
		return true;
	}

	private void writeExternalDate() {
		if (this.status == -1) {
			return;
		}
		ConfigUtil.writeConfig(ConfigUtil.EXTERNAL_LAST_DATE_KEY, System.currentTimeMillis());
	}

	private void writeInsideDate() {
		if (this.status == -1) {
			return;
		}
		ConfigUtil.writeConfig(ConfigUtil.INSIDE_LAST_DATE_KEY, System.currentTimeMillis());
	}

	public void clearDeleteFile(boolean isExtend) throws Exception {
		if (this.status == -1) {
			return;
		}
		FSDirectory dir = FSDirectory.open(Paths.get(ConfigUtil.getStorePath(isExtend)));
		IndexWriter indexWriter = getIndexWriter(dir);
		indexWriter.commit();
		FSDirectory readDir = FSDirectory.open(Paths.get(ConfigUtil.getStorePath(isExtend)));
		IndexReader reader = DirectoryReader.open(readDir);
		for (int i = 0; i < reader.maxDoc(); i++) {
			if (this.status == -1) {
				return;
			}
			Set<String> fields = new HashSet<String>();
			fields.add("id"); //$NON-NLS-1$
			fields.add("fileAbsolutePath"); //$NON-NLS-1$
			Document doc = reader.document(i, fields);
			if (doc != null) {
				String fileAbsolutePath = doc.get("fileAbsolutePath"); //$NON-NLS-1$
				String id = doc.get("id"); //$NON-NLS-1$
				if (!new File(fileAbsolutePath).exists()) {
					indexWriter.deleteDocuments(new Term("id", id)); //$NON-NLS-1$
				} else {
					boolean inConfig = false;
					if (isExtend) {
						for (String external : config.getExternalDirList()) {
							if (fileAbsolutePath.startsWith(external)) {
								inConfig = true;
								break;
							}
						}
					} else {
						for (String inside : config.getInsideDirList()) {
							if (fileAbsolutePath.startsWith(inside)) {
								inConfig = true;
								break;
							}
						}
					}
					if (!inConfig) {
						indexWriter.deleteDocuments(new Term("id", id)); //$NON-NLS-1$
					}

				}
			}

			// do something with docId here...
		}
		try {
			indexWriter.commit();
			indexWriter.close();
		} catch (Exception e) {
			logger.error("error commit or close delete files ->" + isExtend, e); //$NON-NLS-1$
		}
	}

	private long getFileDirCount(String file) {
		if (getStatus() == -1) {
			return 0;
		}
		File dir = new File(file);
		if (!dir.exists()) {
			return 0;
		}
		File[] childs = dir.listFiles();
		long count = 0;
		for (File child : childs) {
			if (child.isDirectory()) {
				count++;
			}
		}
		return count;
	}

	public static String runCommand(String command) {
		StringBuffer bf = new StringBuffer();
		Process ps = null;
		try {
			long a = System.currentTimeMillis();
			ps = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", command }); //$NON-NLS-1$ //$NON-NLS-2$
			InputStreamReader i = new InputStreamReader(ps.getInputStream(), "GBK"); //$NON-NLS-1$
			String line;
			BufferedReader ir = new BufferedReader(i);
			while ((line = ir.readLine()) != null) {
				if (line.length() > 0) {
					bf.append(line);
				}
			}
			long b = System.currentTimeMillis();
			logger.info(command + " -> execute time is ->" + (b - a) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (IOException e) {
			logger.error("error execute cmd ->" + command, e); //$NON-NLS-1$
		}
		return bf.toString();
	}

	public void stop() {
		this.status = -1;
	}

	public long getFileCount() {
		return fileCount;
	}

	public void setFileCount(long fileCount) {
		this.fileCount = fileCount;
	}

	public long getLastInsideDate() {
		return lastInsideDate;
	}

	public void setLastInsideDate(long lastInsideDate) {
		this.lastInsideDate = lastInsideDate;
	}

	public long getLastExternalDate() {
		return lastExternalDate;
	}

	public void setLastExternalDate(long lastExternalDate) {
		this.lastExternalDate = lastExternalDate;
	}

	public void addWorkerListener(IWorkerListener listener) {
		listeners.add(listener);
	}

}