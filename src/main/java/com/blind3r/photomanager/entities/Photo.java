package com.blind3r.photomanager.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Photo {

	@Id
	private String id;
	private String fileName;
	@Column(length = 1500)
	private String baseUrl;

	private long width;
	private long height;

	public Photo() {
		super();
	}
	
	public Photo(String id, String fileName, String baseUrl, long width, long height) {
		super();
		this.id = id;
		this.fileName = fileName;
		this.baseUrl = baseUrl;
		this.width = width;
		this.height = height;
	}

	public String getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public long getWidth() {
		return width;
	}

	public long getHeight() {
		return height;
	}

	@Override
	public String toString() {
		return "Photo [id=" + id + ", fileName=" + fileName + ", baseUrl=" + baseUrl + ", width=" + width + ", height="
				+ height + "]";
	}


}
