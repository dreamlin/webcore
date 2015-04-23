package org.linjia.webcore.model;

import java.io.InputStream;

public class DownFileInfo {

	private String		fileName;
	private InputStream	fileStream;
	private String		extName;

	public DownFileInfo() {

	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public InputStream getFileStream() {
		return fileStream;
	}

	public void setFileStream(InputStream fileStream) {
		this.fileStream = fileStream;
	}

	public String getExtName() {
		return extName;
	}

	public void setExtName(String extName) {
		this.extName = extName;
	}

}
