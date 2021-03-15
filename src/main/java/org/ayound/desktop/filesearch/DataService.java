package org.ayound.desktop.filesearch;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.ayound.desktop.config.ConfigUtil;

public class DataService {
	public static final Logger logger = LogManager.getLogger(DataService.class);

	public static final String RESOURCE_PATH = ConfigUtil.getFileIndexInsidePath();

	public static final String PAN_PATH = ConfigUtil.getFileIndexExternalPath();

	public String dataPath = RESOURCE_PATH;

	String word;
	int start;

	int length;

	String sortField;

	boolean reverse;

	Directory dir;

	Analyzer analyzer;
	IndexReader indexReader;
	IndexSearcher indexSearcher;

	Highlighter nameHighlighter;

	Highlighter textHighlighter;

	Query query;

	public DataService(boolean isExternal, String word, int start, int length, String sortField, boolean reverse) {
		super();
		if (isExternal) {
			this.dataPath = PAN_PATH;
		} else {
			this.dataPath = RESOURCE_PATH;
		}
		this.word = word;
		this.start = start;
		this.length = length;
		this.sortField = sortField;
		this.reverse = reverse;

		try {
			dir = FSDirectory.open(Paths.get(dataPath));
			indexReader = DirectoryReader.open(dir);
			indexSearcher = new IndexSearcher(indexReader);
			analyzer = new StandardAnalyzer();
			if (word != null) {
				// 词法分析器
				if (word.startsWith("t:")) {
					analyzer = new SmartChineseAnalyzer();
				} else {
					analyzer = new WhitespaceAnalyzer();
				}
				query = createQuery(word);
				nameHighlighter = new Highlighter(new SimpleHTMLFormatter("|||", "}$|||"), // 高亮格式，用<B>标签包裹
						new QueryScorer(query));
				textHighlighter = new Highlighter(new SimpleHTMLFormatter("<b><font color='red'>", "</font></b>"), // 高亮格式，用<B>标签包裹
						new QueryScorer(query));
				Fragmenter fragmenter = new SimpleFragmenter(10000); // 高亮后的段落范围在100字内
				nameHighlighter.setTextFragmenter(fragmenter);
				textHighlighter.setTextFragmenter(fragmenter);
			}
		} catch (IOException e) {
			logger.error("error to init query for " + word, e);
		} catch (ParseException e) {
			logger.error("error to parse query for " + word, e);
		}
	}

	public String highLightName(String field, String value) {
		String newValue = value;
		if (word == null) {
			return newValue;
		}
		if (word.indexOf(":") > 0) {
			return newValue;
		}
		try {
			newValue = nameHighlighter.getBestFragment(new SmartChineseAnalyzer(), field, value);
		} catch (IOException e) {
			logger.error("error to highLight for " + field, e);
		} catch (InvalidTokenOffsetsException e) {
			logger.error("error to highLight for " + field, e);
		}
		if (StringUtils.isEmpty(newValue)) {
			newValue = value;
		}
		return newValue;
	}

	public String highLightText(String value) {
		String newValue = value;
		if (word == null) {
			return newValue;
		}
		if (!word.startsWith("t:")) {
			return newValue;
		}

		try {
			newValue = textHighlighter.getBestFragment(new SmartChineseAnalyzer(), "text", value);
		} catch (IOException e) {
			logger.error("error to highLight for " + value, e);
		} catch (InvalidTokenOffsetsException e) {
			logger.error("error to highLight for " + value, e);
		}
		if (StringUtils.isEmpty(newValue)) {
			newValue = value;
		}
		return newValue;
	}

	public int count() throws IOException, ParseException {
		Query query = createQuery(word);
		return indexSearcher.count(query);
	}

	public SearchResult search() throws IOException, ParseException {

		SearchResult result = new SearchResult();
		if (!Paths.get(dataPath).toFile().exists() || word == null) {
			return result;
		}
		try {
			// 创建核心对象
			Sort sort = null;
			if (sortField != null) {
				Type type = SortField.Type.STRING;
				if (StringUtils.equals("fileSize", sortField)) {
					type = SortField.Type.LONG;
				}
				if (StringUtils.equals("fileLastModified", sortField)) {
					type = SortField.Type.LONG;
				}
				SortField sortFieldItem = new SortField(sortField, type, reverse);
				sort = new Sort(sortFieldItem);
			}

			long count = count();
			try {
				TopScoreDocCollector results = TopScoreDocCollector.create(start + length, -1);
				if (sort != null) {
					TopDocs lastTds = indexSearcher.search(query, start + length, sort);
					if (lastTds.scoreDocs.length > 0) {

						// 获取总条数
						// 获取数据
						// 装配数据
						for (int i = start; i < start + length; i++) {
							if (i > lastTds.scoreDocs.length - 1) {
								break;
							}
							int docId = lastTds.scoreDocs[i].doc;
							// 通过docId获取Document
							Document doc = indexSearcher.doc(docId);

							result.getFiles().add(convertModel(doc, nameHighlighter, analyzer, word));
						}
					}
				} else {
					indexSearcher.search(query, results);
					TopDocs tds = results.topDocs(start, length);
					ScoreDoc[] sd = tds.scoreDocs;
					for (int i = 0; i < sd.length; i++) {
						int docId = sd[i].doc;
						// 通过docId获取Document
						Document doc = indexSearcher.doc(docId);

						result.getFiles().add(convertModel(doc, nameHighlighter, analyzer, word));
					}
				}
			} catch (IOException e) {
				logger.error("error to query for " + word, e);
			} catch (Exception e) {
				logger.error("error to query for " + word, e);
			}
			result.setCount((int) count);
		} catch (Throwable e) {
			logger.error("error to query for " + word,e);
		}
		return result;

	}

	private FileModel convertModel(Document doc, Highlighter highlighter, Analyzer analyzer, String word) {
		String text = doc.get("text");
		String fileName = doc.get("fileName");
		String fileDir = doc.get("fileDir");
		if (word != null && word.length() > 0) {
			if (!word.startsWith("t:")) {
				fileName = highLightName("fileName", fileName);
				fileDir = highLightName("fileDir", fileDir);
			}
		}
		FileModel fileModel = new FileModel(doc.get("id"), fileName, doc.get("fileSize"), doc.get("fileLastModified"),
				doc.get("fileAbsolutePath"), doc.get("author"), text, doc.get("title"), fileDir);
		return fileModel;
	}

	private ScoreDoc getLastScoreDoc(int pageIndex, int pageSize, Query query, IndexSearcher searcher, Sort sort)
			throws IOException {
		if (pageIndex == 1)
			return null;// 如果是第一页就返回空
		int num = pageSize * (pageIndex - 1);// 获取上一页的数量
		TopDocs tds = searcher.search(query, num, sort);
		return tds.scoreDocs[num - 1];
	}

	private Query createQuery(String word) throws ParseException {
		if (word == null) {
			word = "";
		}
		String key = "fileAbsolutePath";
		String value = word;
		if (word.indexOf(":") > 0) {
			key = StringUtils.substringBefore(word, ":");
			value = StringUtils.substringAfter(word, ":");
			if (StringUtils.equals(key, "p")) {
				key = "fileAbsolutePath";
				if (StringUtils.isEmpty(value)) {
					value = "*";
				}
				QueryParser parse = new QueryParser("fileAbsolutePath", analyzer);
//			      parse.setDefaultOperator(Operator.AND);//将空格默认 定义为AND
				parse.setAllowLeadingWildcard(true);// 设定第一个* 可以匹配
				return parse.parse(key + ":" + value);
				// 其中空格默认就是OR
			}
			if (StringUtils.equals(key, "t")) {
				key = "text";
				if (StringUtils.isEmpty(value)) {
					value = "*";
				}
				QueryParser parse = new QueryParser("text", analyzer);
				return parse.parse(value);
			}
			if (StringUtils.equals(key, "n")) {
				key = "fileName";
			}
			if (StringUtils.isEmpty(value)) {
				value = "*";
			}
			QueryParser parse = new QueryParser("fileAbsolutePath", analyzer);
//		      parse.setDefaultOperator(Operator.AND);//将空格默认 定义为AND
			parse.setAllowLeadingWildcard(true);// 设定第一个* 可以匹配
			return parse.parse(key + ":" + value);
		}
		key = "fileAbsolutePath";
		QueryParser parse = new QueryParser("fileAbsolutePath", analyzer);
		parse.setDefaultOperator(Operator.AND);// 将空格默认 定义为AND
		parse.setAllowLeadingWildcard(true);// 设定第一个* 可以匹配
		String newValue = QueryParser.escape(value);
		String[] values = newValue.split(" ");
		List<String> items = new ArrayList<String>();
		for (String item : values) {
			if (item.length() > 0) {
				if (!item.startsWith("*")) {
					item = "*" + item;
				}
				if (!item.endsWith("*")) {
					item = item + "*";
				}
			}
			items.add(item);
		}
		String parsedValue = StringUtils.join(items.iterator(), " ");
		if (parsedValue.length() == 0) {
			parsedValue = "*";
		}
		return parse.parse(parsedValue);
	}

	public static void main(String[] args) throws IOException, ParseException {
		DataService service = new DataService(false, "\"Users/ayound\"", 0, 100, "fileName", true);
		SearchResult ret = service.search();
		System.out.println(ret.count);
		System.out.println(ret.getFiles().size());
	}
}