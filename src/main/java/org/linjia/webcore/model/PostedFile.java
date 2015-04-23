package org.linjia.webcore.model;

public class PostedFile {

	private String	fieldName;		// 参数名
	private String	extName;		// 后缀
	private String	fileName;		// 文件名
	private String	contentType;	// 文件大小
	private int		size;			// 尺寸
	private String	savedPath;		// 保存的临时路径

	public PostedFile() {

	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void setSavedPath(String savedPath) {
		this.savedPath = savedPath;
	}

	public String getSavedPath() {
		return savedPath;
	}

	public void setExtName(String extName) {
		this.extName = extName.toLowerCase();
	}

	public String getExtName() {
		return extName;
	}

}