package com.example.wings.mainactivity.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wings.R;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.ParcelableObject;
import com.example.wings.models.User;
import com.example.wings.models.inParseServer.TrustedContact;
import com.example.wings.startactivity.SAFragmentsListener;
import com.parse.ParseACL;
import com.parse.ParseUser;

import org.parceler.Parcel;
import org.parceler.Parcels;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private static final String PHOTO_FILE_NAME = "photo.jpg";         //arbitrary file name to store Post photo in
    public static final String KEY_TRUSTED_CONTACTS = "receiveTrustedContacts";         //used so MainActivity may send a List<TrustedContact> to work on/save

    private MAFragmentsListener mlistener;

    public File photoFile;
    private ImageView profileImage;
    ParseUser user = ParseUser.getCurrentUser();

    private int numtc = 1;

    private Button completeBtn;
    private Button galleryBtn;
    private Button tPhotoBtn;
    private TextView tcstatus;
    private Button setupTCBtn;
    private EditText numPIN;
    private List<TrustedContact> trustedContacts = new ArrayList<>();

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
    //Purpose:      called once Fragment created, will obtain the User object it was passed in
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            ParcelableObject receivedData = (ParcelableObject) Parcels.unwrap(getArguments().getParcelable(KEY_TRUSTED_CONTACTS));
            trustedContacts.addAll(receivedData.getTrustedContactList());
        }
    }

    @Override
    /**
     * Purpose:     Called automatically. When this Fragment is being attached to the parent activity, REQUIRE the activity to implement SAFragmentsListener. Otherwise throw an exception!
     *              Connect the Fragment's listener to the activity!
     */
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MAFragmentsListener) {
            mlistener = (MAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MAFragmentListener");
        }
//        if (context instanceof SAFragmentsListener) {
 //           slistener = (SAFragmentsListener) context;
 //       } else {
 //           throw new ClassCastException(context.toString() + " must implement SAFragmentListener");
 //       }
    }
    @SuppressLint("ResourceAsColor")
    @Override
    /**
     * Purpose:     Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        //1.) Get references to Views:
        completeBtn = view.findViewById(R.id.completeBtn);
        profileImage = view.findViewById(R.id.profileImage);
        tPhotoBtn = view.findViewById(R.id.takePhotoBtn);
        galleryBtn = view.findViewById(R.id.galleryBtn);
        tcstatus = view.findViewById(R.id.tcStatus);
        setupTCBtn = view.findViewById(R.id.setupTCBtn);
        numPIN = view.findViewById(R.id.numPIN);

        // To update the status of creating a Trusted Contact List on fragment
        if (numtc > 0) {
            tcstatus.setTextColor(Color.GREEN);
            tcstatus.setText("completed");
        }

        // To create a Trusted Contact List
        setupTCBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saveProfileSetup();
                Log.d(TAG, "trusted contact button clicked --> going to EditTrustedContactsFrag");
                mlistener.toEditTrustedContactsFragment(trustedContacts);
            }
        });

        // TODO : check if all profile req set up: profile pic, Trusted contacts, and PIN
        completeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String errorString = isValid();

                //For sake of testing, commenting out for now to mimic setup of profile!
               /* if(errorString.equals("")){
                    user.put(User.KEY_PROFILESETUP, true);
                    listener.setRestrictScreen(false);
                    listener.toHomeFragment();
                }
                else{
                    showLongTopToast(errorString);
                }
                */
                //Delete this once fragment is fully done!
                saveProfileSetup();         //makes profileSetup = true and saves it!
                mlistener.setRestrictScreen(false);
                Toast.makeText(getContext(), "Refresh the page to see your current location!", Toast.LENGTH_LONG).show();
                mlistener.toCurrentHomeFragment();

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
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    private String isValid() {
        Log.d(TAG, "in isValid()");
        String result = "";

        //1.) Check if the profile picture works
        if(profileImage.getDrawable() == null) {    // if picture don't exist in both strategy
            result += "Profile picture not available\n";
        }

        boolean hasTrustedContacts = true;

        //2). Check if the user has PIN
        if (numPIN == null) {
            result += "Please create your 4 digit PIN";
        }
        if (numPIN.length() != 4) {
            result += "Your PIN must have 4 digits\n";
        }

        //3). Check Trusted Contacts is null
        if (!hasTrustedContacts) {
            result += "Create at least 1 Trusted Contacts\n";
        }

        return result;
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
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

    private void saveProfileSetup(){
        Log.d(TAG, "in saveProfileSetup():");
        user.put(User.KEY_PROFILESETUP, true);
       // ParseACL acl = user.getACL();
       // acl.setPublicWriteAccess(true);         //for now, allow other users to change their info --> used when a buddy cancels the buddy pair
        user.saveInBackground(e -> {
            if(e==null){
                //Save successfull
               // showLongTopToast("Profile set up sucessfully!");
            }else{
                // Something went wrong while saving
                Log.e(TAG, "saveProfileSetup():      failed e="+ e.getMessage());
               // showLongTopToast("Error setting up profile!");
            }
        });
    }

    private void showLongTopToast(String message){
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

}