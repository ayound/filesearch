package org.ayound.desktop.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

public class ConfigUtil {

	public static final Logger logger = LogManager.getLogger(ConfigUtil.class);

	static List<IConfigChangeListener> listeners = new ArrayList<IConfigChangeListener>();

	public static final String[] BLACK_FILE = new String[] { ".DS_Store", "Thumbs.db", "nohup.out", ".log" };

	public static final String[] SYSTEM_DIRS = new String[] { "/bin", "/sbin", "/usr", "/usr/bin", "/usr/sbin",
			"/usr/local/", "/usr/local/bin", "/usr/local/etc", "/etc", "/tmp", "/var", "/Applications", "/System",
			"/Library" };

	public static String NO_PREVIEW_END_FIX[] = new String[] { "htm", "html", "txt", "xml", "json", "csv", "png", "jpg",
			"jpeg", "mp3", "gif", "bmp", "ttf", "ico" };

	public static String CODE_PREVIEW_END_FIX[] = new String[] { "1c", "4d", "abnf", "accesslog", "ada", "arduino",
			"ino", "armasm", "arm", "avrasm", "actionscript", "as", "alan", "i", "ln", "angelscript", "asc", "apache",
			"apacheconf", "applescript", "osascript", "arcade", "asciidoc", "adoc", "aspectj", "autohotkey", "autoit",
			"awk", "mawk", "nawk", "gawk", "bash", "sh", "zsh", "basic", "bbcode", "blade", "bnf", "brainfuck", "bf",
			"csharp", "cs", "c", "h", "cpp", "hpp", "cc", "hh", "c++", "h++", "cxx", "hxx", "cal", "cos", "cls",
			"cmake", "cmake.in", "coq", "csp", "css", "capnproto", "capnp", "chaos", "kaos", "chapel", "chpl", "cisco",
			"clojure", "clj", "coffeescript", "coffee", "cson", "iced", "cpc", "crmsh", "crm", "pcmk", "crystal", "cr",
			"cypher", "d", "dns", "zone", "bind", "dos", "bat", "cmd", "dart", "delphi", "dpr", "dfm", "pas", "pascal",
			"freepascal", "lazarus", "lpr", "lfm", "diff", "patch", "django", "jinja", "dockerfile", "docker",
			"dsconfig", "dts", "dust", "dst", "dylan", "ebnf", "elixir", "elm", "erlang", "erl", "extempore", "xtlang",
			"xtm", "fsharp", "fs", "fix", "fortran", "f90", "f95", "gcode", "nc", "gams", "gms", "gauss", "gss",
			"godot", "gdscript", "gherkin", "gn", "gni", "go", "golang", "gf", "golo", "gololang", "gradle", "groovy",
			"xml", "rss", "atom", "xjb", "xsd", "xsl", "plist", "svg", "http", "https", "haml", "handlebars", "hbs",
			"html.hbs", "html.handlebars", "haskell", "hs", "haxe", "hx", "hlsl", "hy", "hylang", "ini", "toml",
			"inform7", "i7", "irpf90", "json", "java", "jsp", "javascript", "js", "jsx", "jolie", "iol", "ol", "julia",
			"julia-repl", "kotlin", "kt", "tex", "leaf", "lean", "lasso", "ls", "lassoscript", "less", "ldif", "lisp",
			"livecodeserver", "livescript", "ls", "lua", "makefile", "mk", "mak", "make", "markdown", "md", "mkdown",
			"mkd", "mathematica", "mma", "wl", "matlab", "maxima", "mel", "mercury", "mirc", "mrc", "mizar",
			"mojolicious", "monkey", "moonscript", "moon", "n1ql", "nsis", "never", "nginx", "nginxconf", "nim",
			"nimrod", "nix", "ocl", "ocaml", "ml", "objectivec", "mm", "objc", "obj-c", "obj-c++", "objective-c++",
			"glsl", "openscad", "scad", "ruleslanguage", "oxygene", "pf", "pf.conf", "php", "php3", "php4", "php5",
			"php6", "php7", "php8", "parser3", "perl", "pl", "pm", "plaintext", "pony", "pgsql", "postgres",
			"postgresql", "powershell", "ps", "ps1", "processing", "prolog", "properties", "protobuf", "puppet", "pp",
			"python", "py", "gyp", "profile", "python-repl", "pycon", "qsharp", "k", "kdb", "qml", "r", "cshtml",
			"razor", "razor-cshtml", "reasonml", "re", "redbol", "rebol", "red", "red-system", "rib", "rsl", "risc",
			"riscript", "graph", "instances", "robot", "rf", "rpm-specfile", "rpm", "spec", "rpm-spec", "specfile",
			"ruby", "rb", "gemspec", "podspec", "thor", "irb", "rust", "rs", "SAS", "sas", "scss", "sql", "p21", "step",
			"stp", "scala", "scheme", "scilab", "sci", "shexc", "shell", "console", "smali", "smalltalk", "st", "sml",
			"ml", "solidity", "sol", "stan", "stanfuncs", "stata", "iecst", "scl", "stl", "structured-text", "stylus",
			"styl", "subunit", "supercollider", "sc", "svelte", "swift", "tcl", "tk", "terraform", "tf", "hcl", "tap",
			"thrift", "tp", "tsql", "twig", "craftcms", "typescript", "ts", "unicorn-rails-log", "vbnet", "vb", "vba",
			"vbscript", "vbs", "vhdl", "vala", "verilog", "v", "vim", "axapta", "x++", "x86asm", "xl", "tao", "xquery",
			"xpath", "xq", "yml", "yaml", "zephir", "zep" };

	public static final String[] INDEX_FILE_CONTENT_TYPES = new String[] { "doc", "docx", "ppt", "pptx", "xls", "xlsx",
			"pdf" };

	public static final String INSIDE_PATH = "insidePath";

	public static final String EXTERNAL_PATH = "externalPath";

	public static final String INSIDE_LAST_DATE_KEY = "insidePath";

	public static final String EXTERNAL_LAST_DATE_KEY = "externalPath";

	public static final int INCLUDE_CONTENT = 1;

	public static final int NOT_INCLUDE_CONTENT = 0;

	public static final String getUserHomePath() {
		return System.getProperty("user.home");
	}

	public static final String getFileIndexPath() {
		return Paths.get(System.getProperty("user.home"), ".filesearch").toString();
	}

	public static final String getFileIndexConfigPath() {
		return Paths.get(getFileIndexPath(), "config.json").toString();
	}

	public static final String getFileIndexInsidePath() {
		return Paths.get(getFileIndexPath(), INSIDE_PATH).toString();
	}

	public static final String getFileIndexExternalPath() {
		return Paths.get(getFileIndexPath(), EXTERNAL_PATH).toString();
	}

	public static String getStorePath(boolean isExtend) {
		if (isExtend) {
			return getFileIndexExternalPath();
		} else {
			return getFileIndexInsidePath();
		}
	}

	public static void saveConfig(ConfigModel newConfig) {
		ConfigModel oldConfig = loadConfig();
		Gson gson = new Gson();
		String jsonContent = gson.toJson(newConfig);
		try {
			FileUtils.writeStringToFile(new File(getFileIndexConfigPath()), jsonContent, "utf-8");
		} catch (Exception e) {
			logger.error("error to save config ->", e);
		}
		if(!oldConfig.equals(newConfig)) {			
			ConfigUtil.writeConfig(ConfigUtil.INSIDE_LAST_DATE_KEY, -1);
			ConfigUtil.writeConfig(ConfigUtil.EXTERNAL_LAST_DATE_KEY, -1);
		}
		for (IConfigChangeListener listener : listeners) {
			listener.onChange(oldConfig, newConfig);
		}
	}

	public static ConfigModel loadConfig() {
		ConfigModel config = new ConfigModel();
		File configFile = new File(getFileIndexConfigPath());
		if (configFile.exists()) {
			Gson gson = new Gson();
			FileReader reader = null;
			try {
				reader = new FileReader(configFile);
				config = gson.fromJson(reader, ConfigModel.class);
			} catch (Exception e) {
				logger.error("error to load config ->", e);
			} finally {
				try {
					reader.close();
				} catch (Exception e) {
					// ignore
				}
			}
		}
		return config;
	}

	public static final String[] HIGH_LIGHT_FILES = new String[] { "highlight/highlight.pack.js",
			"highlight/CHANGES.md", "highlight/styles/brown-papersq.png", "highlight/README.md",
			"highlight/README.ru.md", "highlight/styles/nord.css", "highlight/styles/night-owl.css",
			"highlight/styles/purebasic.css", "highlight/styles/grayscale.css",
			"highlight/styles/atom-one-dark-reasonable.css", "highlight/LICENSE", "highlight/styles/a11y-light.css",
			"highlight/styles/a11y-dark.css", "highlight/styles/gruvbox-light.css", "highlight/styles/gruvbox-dark.css",
			"highlight/styles/vs2015.css", "highlight/styles/isbl-editor-dark.css",
			"highlight/styles/shades-of-purple.css", "highlight/styles/isbl-editor-light.css",
			"highlight/styles/nnfx-dark.css", "highlight/styles/hybrid.css", "highlight/styles/nnfx.css",
			"highlight/styles/gradient-light.css", "highlight/styles/atelier-estuary-light.css",
			"highlight/styles/atelier-plateau-light.css", "highlight/styles/atelier-savanna-light.css",
			"highlight/styles/atelier-estuary-dark.css", "highlight/styles/atelier-plateau-dark.css",
			"highlight/styles/atelier-savanna-dark.css", "highlight/styles/atelier-cave-light.css",
			"highlight/styles/agate.css", "highlight/styles/gradient-dark.css",
			"highlight/styles/atelier-cave-dark.css", "highlight/styles/atom-one-light.css",
			"highlight/styles/atom-one-dark.css", "highlight/styles/routeros.css", "highlight/styles/xcode.css",
			"highlight/styles/railscasts.css", "highlight/styles/pojoaque.jpg", "highlight/styles/an-old-hope.css",
			"highlight/styles/sunburst.css", "highlight/styles/idea.css", "highlight/styles/default.css",
			"highlight/styles/tomorrow-night-blue.css", "highlight/styles/atelier-sulphurpool-light.css",
			"highlight/styles/atelier-sulphurpool-dark.css", "highlight/styles/tomorrow-night.css",
			"highlight/styles/github.css", "highlight/styles/solarized-dark.css",
			"highlight/styles/solarized-light.css", "highlight/styles/docco.css",
			"highlight/styles/atelier-lakeside-light.css", "highlight/styles/atelier-lakeside-dark.css",
			"highlight/styles/pojoaque.css", "highlight/styles/atelier-seaside-light.css",
			"highlight/styles/atelier-seaside-dark.css", "highlight/styles/atelier-forest-light.css",
			"highlight/styles/atelier-forest-dark.css", "highlight/styles/atelier-heath-light.css",
			"highlight/styles/atelier-heath-dark.css", "highlight/styles/atelier-dune-light.css",
			"highlight/styles/atelier-dune-dark.css", "highlight/styles/foundation.css",
			"highlight/styles/tomorrow-night-eighties.css", "highlight/styles/tomorrow-night-bright.css",
			"highlight/styles/hopscotch.css", "highlight/styles/obsidian.css", "highlight/styles/kimbie.light.css",
			"highlight/styles/kimbie.dark.css", "highlight/styles/lightfair.css", "highlight/styles/googlecode.css",
			"highlight/styles/lioshi.css", "highlight/styles/xt256.css", "highlight/styles/github-gist.css",
			"highlight/styles/arduino-light.css", "highlight/styles/srcery.css", "highlight/styles/monokai-sublime.css",
			"highlight/styles/dracula.css", "highlight/styles/paraiso-light.css", "highlight/styles/paraiso-dark.css",
			"highlight/styles/ocean.css", "highlight/styles/stackoverflow-dark.css",
			"highlight/styles/stackoverflow-light.css", "highlight/styles/school-book.css",
			"highlight/styles/rainbow.css", "highlight/styles/qtcreator_light.css", "highlight/styles/tomorrow.css",
			"highlight/styles/qtcreator_dark.css", "highlight/styles/zenburn.css", "highlight/styles/gml.css",
			"highlight/styles/monokai.css", "highlight/styles/darcula.css", "highlight/styles/magula.css",
			"highlight/styles/color-brewer.css", "highlight/styles/ir-black.css", "highlight/styles/arta.css",
			"highlight/styles/far.css", "highlight/styles/brown-paper.css", "highlight/styles/codepen-embed.css",
			"highlight/styles/vs.css", "highlight/styles/dark.css", "highlight/styles/androidstudio.css",
			"highlight/styles/mono-blue.css", "highlight/styles/ascetic.css", "highlight/styles/school-book.png",
			"preview.htm", "test.pdf" };

	public static String getResourcePath() {
		return Paths.get(getFileIndexPath(), "resource").toString();
	}
	
	public static String getTmpPath() {
		return Paths.get(getFileIndexPath(), "tmp").toString();
	}

	public static String getDateConfigPath() {
		return Paths.get(getFileIndexPath(), "date.properties").toString();
	}

	public static long getLastDate(String key) {
		Properties props = new Properties();
		FileReader reader = null;
		try {
			reader = new FileReader(getDateConfigPath());
			props.load(reader);
		} catch (Exception e) {
			logger.error("error to get  config key ->" + key, e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		String value = String.valueOf(props.getOrDefault(key, "0"));
		if (StringUtils.isNumeric(value)) {
			return Long.parseLong(value);
		}
		return 0;
	}

	public static void writeConfig(String key, Object value) {
		Properties props = new Properties();
		FileReader reader = null;
		try {
			reader = new FileReader(getDateConfigPath());
			props.load(reader);
		} catch (Exception e) {
			logger.error("error to save  config key ->" + key, e);
		} finally {
			IOUtils.closeQuietly(reader);
		}
		props.setProperty(key, String.valueOf(value));
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(getDateConfigPath());
			props.save(out, "update");
		} catch (FileNotFoundException e) {
			logger.error("error to save  config key ->" + key, e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static void clear(boolean isExternal) {
		if (isExternal) {
			try {
				FileUtils.deleteDirectory(new File(ConfigUtil.getFileIndexExternalPath()));
				ConfigUtil.writeConfig(ConfigUtil.EXTERNAL_LAST_DATE_KEY, -1);
			} catch (IOException e) {
				logger.error("error to clear ext data", e); //$NON-NLS-1$
			}
		} else {
			try {
				FileUtils.deleteDirectory(new File(ConfigUtil.getFileIndexInsidePath()));
				ConfigUtil.writeConfig(ConfigUtil.INSIDE_LAST_DATE_KEY, -1);
			} catch (IOException e) {
				logger.error("error to clear data", e); //$NON-NLS-1$
			}
		}
	}

	public static void addListener(IConfigChangeListener listener) {
		listeners.add(listener);
	}

}
