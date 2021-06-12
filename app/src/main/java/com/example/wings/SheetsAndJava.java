package com.example.wings;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.example.wings.mainactivity.MainActivity;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;


// 6/11: Currently unusable. Buggy and it doesn't make sense
public class SheetsAndJava {
    private static final String TAG = "SheetsAndJava";
    private static Sheets sheetsService;
    private static String APPLICATION_NAME = "Wings Google Sheets";
    public static String SPREADSHEET_ID = "1-sChMu-2OSCRBmRV4EoXvQhF8f3ljGCFX3ZYTYLwrMM";
    public static String CREDENTIALS_FILE_PATH = "src/main/assets/credentials.json";
    public static String TOKENS_DIRECTORY_PATH = "tokens";

    public SheetsAndJava(){}

    //Purpose:      Credentials needed I think".
    public Credential authorize(Context context) throws IOException, GeneralSecurityException {
        InputStream in = context.getAssets().open("credentials.json");//SheetsAndJava.class.getResourceAsStream("/credentials.json");
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " );
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JacksonFactory.getDefaultInstance(), new InputStreamReader(in)
        );

        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);
        File tokenFolder = new File(Environment.getExternalStorageDirectory() +
                File.separator + TOKENS_DIRECTORY_PATH);
        if (!tokenFolder.exists()) {
            tokenFolder.mkdirs();
        }
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(), JacksonFactory.getDefaultInstance(),
                clientSecrets, scopes)
                .setDataStoreFactory(new FileDataStoreFactory(tokenFolder))
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver())
                .authorize("user");
        return credential;
    }

    public Sheets getSheetsService(Context context){
        try {
            Credential credential = authorize(context);
            return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(), credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }catch(IOException ioException){
            Log.e(TAG, "ioException = ", ioException);
        }catch (GeneralSecurityException generalSecurityException){
            Log.e(TAG, "generalSecurityException = ", generalSecurityException);
        }
        return null;
    }
   /* public void makeNewSheet(){
        Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties()
                        .setTitle(title));
        spreadsheet = service.spreadsheets().create(spreadsheet)
                .setFields("spreadsheetId")
                .execute();
        System.out.println("Spreadsheet ID: " + spreadsheet.getSpreadsheetId());
    }*/
}
