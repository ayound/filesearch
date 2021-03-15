package org.ayound.desktop.filesearch;

import java.lang.ProcessBuilder.Redirect;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandUtil {
	public static final Logger logger = LogManager.getLogger(CommandUtil.class);

	public static int executeCommand(String... cmds) {
		ProcessBuilder processBuilder = null;
		Process process = null;
		try {
			processBuilder = new ProcessBuilder();
			processBuilder.command(cmds);
			logger.info("execute command ->" + StringUtils.join(cmds, " ")); //$NON-NLS-1$
			Map<String, String> envs = System.getenv();
			for (Map.Entry<String, String> entry : envs.entrySet()) {
				processBuilder.environment().put(entry.getKey(), entry.getValue());
			}
			System.out.println(processBuilder.environment());
			processBuilder.redirectErrorStream(false);
			processBuilder.redirectOutput(Redirect.PIPE);
			process = processBuilder.start();
			int ret = process.waitFor();
//			logger.info("Result->" + ret + " =>" + out1 + out2); //$NON-NLS-1$
			return ret;
		} catch (Exception e) {
			logger.error("error to execute command ->" + StringUtils.join(cmds, " "), e); //$NON-NLS-1$
		} finally {
			if (process != null) {
				process.destroyForcibly();
			}
		}
		return 0;
	}

}
