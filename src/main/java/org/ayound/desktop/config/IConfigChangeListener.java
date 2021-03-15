package org.ayound.desktop.config;

public interface IConfigChangeListener {
	public void onChange(ConfigModel oldConfig,ConfigModel newConfig);
}
