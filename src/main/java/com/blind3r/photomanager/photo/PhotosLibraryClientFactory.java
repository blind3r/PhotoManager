package com.blind3r.photomanager.photo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;

import org.springframework.util.ResourceUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import com.google.photos.library.v1.PhotosLibraryClient;
import com.google.photos.library.v1.PhotosLibrarySettings;

/**
 * A factory class that helps initialize a {@link PhotosLibraryClient} instance.
 */
public class PhotosLibraryClientFactory {
	private static final java.io.File DATA_STORE_DIR = new java.io.File(
			PhotosLibraryClientFactory.class.getResource("/").getPath(), "credentials");
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String REDIRECT_URI = "http://localhost:3000/callbackPhotos";

	private PhotosLibraryClientFactory() {
	}

	/**
	 * Creates a new {@link PhotosLibraryClient} instance with credentials and
	 * scopes.
	 */
	public static String getGooglePhotosAuthURL(String credentialsPath, List<String> selectedScopes, String token)
			throws IOException, GeneralSecurityException {

		return getUserCredentialsURL(credentialsPath, selectedScopes, token);
	}

	public static PhotosLibraryClient createClient(String credentialsPath, List<String> selectedScopes,
			String refreshToken, String accessToken) throws IOException, GeneralSecurityException {
		CredentialsProvider provider = FixedCredentialsProvider
				.create(getUserCredentials(credentialsPath, selectedScopes, refreshToken, accessToken));

		PhotosLibrarySettings settings = PhotosLibrarySettings.newBuilder().setCredentialsProvider(provider).build();
		return PhotosLibraryClient.initialize(settings);
	}

	private static UserCredentials getUserCredentials(String credentialsPath, List<String> selectedScopes,
			String refreshToken, String accessToken) throws IOException, GeneralSecurityException {

		File file = ResourceUtils.getFile("classpath:" + credentialsPath);

		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
				new InputStreamReader(new FileInputStream(file)));
		String clientId = clientSecrets.getDetails().getClientId();
		String clientSecret = clientSecrets.getDetails().getClientSecret();

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, selectedScopes)
						.setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR)).setAccessType("offline").build();

		GoogleTokenResponse tokenResponse = flow.newTokenRequest(refreshToken).setRedirectUri(REDIRECT_URI).execute();

		Credential credential = flow.loadCredential(clientId);
		Date now = new Date();
		if (credential == null || now.after(new Date(credential.getExpirationTimeMilliseconds()))) {
			credential = flow.createAndStoreCredential(tokenResponse, clientId);
		}

		return UserCredentials.newBuilder().setClientId(clientId).setClientSecret(clientSecret)
				.setAccessToken(new AccessToken(credential.getAccessToken(), null))
				.setRefreshToken(credential.getRefreshToken()).build();
	}

	private static String getUserCredentialsURL(String credentialsPath, List<String> selectedScopes, String token)
			throws IOException, GeneralSecurityException {

		File file = ResourceUtils.getFile("classpath:" + credentialsPath);

		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
				new InputStreamReader(new FileInputStream(file)));

		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, selectedScopes)
						.setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR))
						.setAccessType("online")
						.setApprovalPrompt("force")
						.build();

		return flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
	}
}
