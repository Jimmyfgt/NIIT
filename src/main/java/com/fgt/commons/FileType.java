package com.fgt.commons;

import org.springframework.stereotype.Component;

@Component
public class FileType {

	public String getFileType(String fileType) {
		if (fileType.equalsIgnoreCase(".bmp")) {
			return "image/bmp";
		}
		if (fileType.equalsIgnoreCase(".gif")) {
			return "image/gif";
		}
		if (fileType.equalsIgnoreCase(".jpeg") ||
				fileType.equalsIgnoreCase(".jpg") ||
				fileType.equalsIgnoreCase(".png")) {
			return "image/jpg";
		}
		if (fileType.equalsIgnoreCase(".html")) {
			return "text/html";
		}
		if (fileType.equalsIgnoreCase(".txt")) {
			return "text/plain";
		}
		if (fileType.equalsIgnoreCase(".vsd")) {
			return "application/vnd.visio";
		}
		if (fileType.equalsIgnoreCase(".pptx") ||
				fileType.equalsIgnoreCase(".ppt")) {
			return "application/vnd.ms-powerpoint";
		}
		if (fileType.equalsIgnoreCase(".xls") ||
				fileType.equalsIgnoreCase(".xlsx")) {
			return "application/vnd.ms-excel";
		}
		if (fileType.equalsIgnoreCase(".docx") ||
				fileType.equalsIgnoreCase(".doc")) {
			return "application/msword";
		}
		if (fileType.equalsIgnoreCase(".xml")) {
			return "text/xml";
		}
		return "image/jpg";
	}


}
