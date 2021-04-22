package com.example.wings.mainactivity.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.startactivity.SAFragmentsListener;
import com.example.wings.startactivity.StartActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;


/**
 * ProfileSetupFragment.java
 * Purpose:            This displays the profile setup screen for the user to complete all required user information before using the app. This includes
 *                     PIN number, profile picture, and Trusted Contacts.
 */
public class ProfileSetupFragment extends Fragment {

    private static final String TAG = "ProfileSetupFragment";
    private static final String CODEPATH_FILE_PROVIDER_KEY = "com.codepath.fileprovider";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public static final int PICK_PHOTO_CODE = 1046;
    private static final String PHOTO_FILE_NAME = "photo.jpg";                             //arbitrary file name to store Post photo in

    private SAFragmentsListener listener;

    private Button completeBtn;
    private Button galleryBtn;
    private Button tPhotoBtn;

    public File photoFile;
    private ImageView profileImage;


    public ProfileSetupFragment() {}        // Required empty public constructor

    @Override
    /**
     * Purpose:     Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile_setup, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);        //let know that there is an options menu to inflate
    }

    @Override
    /**
     * Purpose:     Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);

        //1.) Get references to Views:
        completeBtn = view.findViewById(R.id.completeBtn);
        profileImage = view.findViewById(R.id.profileImage);
        tPhotoBtn = view.findViewById(R.id.takePhotoBtn);
        galleryBtn = view.findViewById(R.id.galleryBtn);

        // TODO : check if all profile req set up: profile pic, Trusted contacts, and PIN
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onLogin();
                if (photoFile == null || profileImage.getDrawable() == null) {
                    Toast.makeText(getContext(), "There is no image!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Toast.makeText(getContext(), "Profile Completed! Now find your buddies!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Take photo for profile picture
        tPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { openGallery(); }
        });
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    private void launchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(PHOTO_FILE_NAME);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
         Uri fileProvider = FileProvider.getUriForFile(getContext(), CODEPATH_FILE_PROVIDER_KEY, photoFile);
         intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            // by this point we have the camera photo on disk
            Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            // resize bitmap
            //Bitmap resizedBitmap = Bitmap.createScaledBitmap(takenImage, 150, 150, true);
            // Load the taken image into a preview
            profileImage.setImageBitmap(takenImage);
        } else if (resultCode == RESULT_OK && requestCode == PICK_PHOTO_CODE) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        } else { // Result was a failure
            Toast.makeText(getContext(), "Picture wasn't taken nor selected!", Toast.LENGTH_SHORT).show();
        }
    }

    // Trigger gallery selection for a photo
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_PHOTO_CODE);
    }



    @Override
    /**
     * Purpose:     Called automatically. When this Fragment is being attached to the parent activity, REQUIRE the activity to implement SAFragmentsListener. Otherwise throw an exception!
     *              Connect the Fragment's listener to the activity!
     */
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SAFragmentsListener) {
            listener = (SAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement SAFragmentsListener");
        }
    }

    @Override
    /**
     * Purpose:     Inflates a specific top navigation bar for this fragment, allowing the user to logout without setting up their profile, to go to SettingsFragments, or go to HelpFragment
     */
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profilesetup_navigation, menu);
    }

    /**
     * Purpose:     Attaches events when menu items are pushed.
     * @param item, which item was selected
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_logout:
                /*1.) If there is an actual user logged in, log them out
                    if(ParseUser.getCurrentUser() != null){
                        ParseUser.logOut();
                    }
                */

                //2.) Switch to LoginFragment:
                listener.toLoginFragment();
                return true;
            case R.id.action_settings:
                listener.toSettingsFragment();
                break;
            case R.id.action_help:
                listener.toHelpFragment();
                break;
            default:
                return false;
        }
        return false;
    }
}