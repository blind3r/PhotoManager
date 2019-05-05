package com.blind3r.photomanager.photo;

import org.springframework.stereotype.Service;

import com.google.photos.library.v1.PhotosLibraryClient;

@Service
public class PhotoLibrary {
	
	private PhotosLibraryClient photosLibraryClient;

	public PhotosLibraryClient getPhotosLibraryClient() {
		return photosLibraryClient;
	}

	public void setPhotosLibraryClient(PhotosLibraryClient photosLibraryClient) {
		this.photosLibraryClient = photosLibraryClient;
	}
}
