package de.bitdroid.flooding.utils;


public interface LogHandler {
	public void info(String msg);
	public void info(String msg, Throwable error);
	public void debug(String msg);
	public void debug(String msg, Throwable error);
	public void warning(String msg);
	public void warning(String msg, Throwable error);
	public void error(String msg);
	public void error(String msg, Throwable error);
}
