package com.blind3r.photomanager.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.ApiException;
import org.springframework.social.google.api.Google;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.blind3r.photomanager.photo.PhotoLibrary;
import com.blind3r.photomanager.photo.PhotosLibraryClientFactory;
import com.blind3r.photomanager.social.providers.BaseProvider;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient.ListAlbumsPagedResponse;
import com.google.photos.library.v1.internal.InternalPhotosLibraryClient.SearchMediaItemsPagedResponse;
import com.google.photos.types.proto.Album;
import com.google.photos.types.proto.MediaItem;

@Controller
public class PhotosController {

	final List<String> SCOPES = Arrays.asList("https://www.googleapis.com/auth/photoslibrary.sharing",
			"https://www.googleapis.com/auth/photoslibrary");

	@Autowired
	BaseProvider socialLoginBean;

	@Autowired
	PhotoLibrary library;
	
	@RequestMapping(value = "/photos", method = RequestMethod.GET)
	public String getAccess(Model model) {
		String url = null;
		Google google = socialLoginBean.getGoogle();

		try {
			url = PhotosLibraryClientFactory.getGooglePhotosAuthURL("credentials.json", SCOPES,
					google.getAccessToken());

		} catch (IOException | GeneralSecurityException e) {
			e.printStackTrace();
		}

		return "redirect:" + url;
	}

	@RequestMapping(value = "/showPhotos", method = RequestMethod.GET)
	public String getPhotos(Model model, @RequestParam("code") String code) {
		Google google = socialLoginBean.getGoogle();

		// Set up the Photos Library Client that interacts with the API
		PhotosLibraryClient photosLibraryClient;
		try {
			photosLibraryClient = PhotosLibraryClientFactory.createClient("credentials.json", SCOPES, code,
					google.getAccessToken());
			library.setPhotosLibraryClient(photosLibraryClient);
			
			List<Album> albumList = new ArrayList<Album>();
			ListAlbumsPagedResponse listAlbums = photosLibraryClient.listAlbums();
			for (Album album : listAlbums.iterateAll()) {
				albumList.add(album);
			}
			model.addAttribute("albumList",albumList);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}

		return "photos";
	}
	
	@RequestMapping(value = "/duplicates", method = RequestMethod.GET)
	public String findDuplicates(Model model) {
		List<String> fileNames = new ArrayList<String>();
		List<String> duplicateFileIds = new ArrayList<String>();
		List<MediaItem> duplicates = new ArrayList<MediaItem>();
		
		// Set up the Photos Library Client that interacts with the API
		PhotosLibraryClient photosLibraryClient;
		try {
			photosLibraryClient = library.getPhotosLibraryClient();

			ListAlbumsPagedResponse listAlbums = photosLibraryClient.listAlbums();
			for (Album album : listAlbums.iterateAll()) {
				SearchMediaItemsPagedResponse response = photosLibraryClient.searchMediaItems(
						album.getId());

				for (MediaItem item : response.iterateAll()) {
					// Get some properties of a media item
					String fileName = item.getFilename();
					if(!fileNames.contains(fileName)) {
						fileNames.add(fileName);
					}  else if(!duplicateFileIds.contains(item.getId())) {
						duplicateFileIds.add(item.getId());
					}
				}
				
			}			
			System.out.println("Found "+ duplicateFileIds.size() + " duplicate photos");
			
			for (String mediaId : duplicateFileIds) {
				duplicates.add(photosLibraryClient.getMediaItem(mediaId));				
			}
			
		} catch (ApiException e) {
			System.out.println(e);
		}
		model.addAttribute("duplicates",duplicates);

		return "duplicates";
	}
}