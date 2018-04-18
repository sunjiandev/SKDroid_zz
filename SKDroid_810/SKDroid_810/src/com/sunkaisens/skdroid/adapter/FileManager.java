package com.sunkaisens.skdroid.adapter;


public class FileManager {

	public static String getSaveFilePath() {
		if (CommonUtil.hasSDCard()) {
			return CommonUtil.getRootFilePath() + "SKDroidFiles/files/";
		} else {
			return CommonUtil.getRootFilePath() + "SKDroidFiles/files";
		}
	}
}
