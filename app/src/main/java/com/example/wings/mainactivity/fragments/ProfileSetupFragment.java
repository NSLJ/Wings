package com.example.wings.mainactivity.fragments;

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
import com.parse.ParseFile;
import com.parse.ParseUser;

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
    private static final String PHOTO_FILE_NAME = "userProfilePic.jpg";                 //arbitrary file name to store photo in
    public static final String KEY_TRUSTED_CONTACTS = "receiveTrustedContacts";         //used so MainActivity/EditTrustedContactsFrag may send a List<TrustedContact> to work on/save

    private MAFragmentsListener mlistener;

    public File photoFile;
    private ImageView profileImage;
    ParseUser user = ParseUser.getCurrentUser();

    private int numtc = 1;

    private Button btnSave;
    private Button btnGoGallery;
    private Button btnGoTakePic;
    private TextView tcstatus;
    private Button btnGoEditTCFrag;
    private EditText numPIN;
    private List<TrustedContact> trustedContacts = new ArrayList<>();
    private boolean isProfilePicSet;
    private boolean hasTrustedContacts = false;

    public ProfileSetupFragment() {}        // Required empty public constructor

    @Override
    /**
     * Purpose:     Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_setup, container, false);
    }

    @Override
    //Purpose:      called once Fragment created, will obtain the List<TrustedContact> object it was passed in
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null){
            ParcelableObject receivedData = Parcels.unwrap(getArguments().getParcelable(KEY_TRUSTED_CONTACTS));
            trustedContacts.addAll(receivedData.getTrustedContactList());
            Log.d(TAG, "Received list of TrustedContacts: list=" + trustedContacts.toString());

            //Check if the received trustedContacts has info now:
            if (trustedContacts.size() > 0) {
                hasTrustedContacts = true;
            }
        }
    }

    @Override
    /**
     * Purpose:     Called automatically. When this Fragment is being attached to the parent activity, REQUIRE the activity to implement MAFragmentsListener. Otherwise throw an exception!
     *              Connect the Fragment's listener to the activity!
     */
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MAFragmentsListener) {
            mlistener = (MAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MAFragmentListener");
        }
    }
    //@SuppressLint("ResourceAsColor")
    @Override
    /**
     * Purpose:     Called automatically when creating a Fragment instance, after onCreateView(). Ensures root View is not null. Sets up all Views and event handlers here.
     */
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        //1.) Get references to Views:
        btnSave = view.findViewById(R.id.completeBtn);
        profileImage = view.findViewById(R.id.profileImage);
        btnGoTakePic = view.findViewById(R.id.takePhotoBtn);
        btnGoGallery = view.findViewById(R.id.galleryBtn);
        tcstatus = view.findViewById(R.id.tcStatus);
        btnGoEditTCFrag = view.findViewById(R.id.setupTCBtn);
        numPIN = view.findViewById(R.id.numPIN);
        isProfilePicSet = false;                    //flag used to denote when photoFile is initialized

        //Check if we were passed in a valid TrustedContacts list --> toggle it to "completed"!
        if(hasTrustedContacts){
            tcstatus.setTextColor(Color.GREEN);
            tcstatus.setText("completed");
        }
        //2.) Set up onClick listeners:
        //2a.) btnGoEditTCFrag onClick() --> go to EditTrustedContactsFragment + pass in current trustedContacts list
        photoFile = getPhotoFilePath(PHOTO_FILE_NAME);
        btnGoEditTCFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mlistener.toEditTrustedContactsFragment(trustedContacts);
            }
        });

        //2b.) Set up buttons to take picture and go to gallery: --> will toggle isProfilePicSet = true once done!
        btnGoTakePic.setOnClickListener(v -> launchCamera());
        btnGoGallery.setOnClickListener(v -> openGallery());

        //2c.) save button --> Error check all fields (Profile picture, PIN, and Trusted Contacts) + save to Parse database!
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(isValid()){
                   saveProfileSetup();
                }
            }
        });
    }

    //Purpose:          Validates that profilePic, PIN, and trustedContacts is valid. Show a Toast of errors if not valid.
    private boolean isValid() {
        boolean valid = true;           //assume everything is good
        String resultMessages = "";

        //1.) Check profile picture --> toggled by launchCamera(), openGallery(), and onActivityResult()
        if(!isProfilePicSet){
            resultMessages = "You need to set a profile picture!";
            valid = false;
        }

        //2.) check trusted contacts --> toggled by onCreate() when passed in a validated trustedContacts to save
        if(!hasTrustedContacts){
            resultMessages += "\nYou need to set at least 1 Trusted Contact.";
            valid = false;
        }

        //3.) Check the pin ourselves:
        String attemptedPin = numPIN.getText().toString();
        //If !(pin == 4 chars && every one of those chars is a digit) --> not valid pin
        if(!(attemptedPin.length()== 4 && (Character.isDigit(attemptedPin.charAt(0)) && Character.isDigit(attemptedPin.charAt(0)) &&  Character.isDigit(attemptedPin.charAt(0)) && Character.isDigit(attemptedPin.charAt(0))))){
            resultMessages += "\nYou need a valid pin.";
            valid = false;
        }

        if(!(resultMessages.equals(""))) {
            showLongTopToast(resultMessages);
        }

        Log.d(TAG, "valid = " + valid + "   resultMessages = " +resultMessages);
        return valid;
    }

    //Purpose:      In charge of saving al user information + navigating to DefaultHomeFrag! Assumes all info is validated.
    private void saveProfileSetup(){
        //1.) Save user info:
        user.put(User.KEY_PROFILESETUP, true);
        user.put(User.KEY_PROFILEPICTURE, new ParseFile(photoFile));
        user.put(User.KEY_TRUSTEDCONTACTS, trustedContacts);
        user.put(User.KEY_PIN, numPIN.getText().toString());
        // ParseACL acl = user.getACL();
        // acl.setPublicWriteAccess(true);         //for now, allow other users to change their info --> used when a buddy cancels the buddy pair
        user.saveInBackground(e -> {
            if(e==null){
                showLongTopToast("Profile set up sucessfully! You can now access the rest of the app!");
            }else{
                Log.e(TAG, "saveProfileSetup():  failed to save user's info, e="+ e.getMessage());
            }
        });

        //2.) Navigate to DefaultHomeFrag
        mlistener.setRestrictScreen(false);         //--> tells MainActivity that this user has access to whole app now
        mlistener.toDefaultHomeFragment();
    }

    /**
     * Purpose:         Launches an intent to the device's camera, take a picture, and save it into the file: "photoFileName"
     */
    private void launchCamera() {
        //1.) Create and set up the intent:
        //1a.) Create intent --> say we want to come back with an image"
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //1b.) Get the complete File path of where to store the photo! Use the filename in "photoFileName"
      //  photoFile = getPhotoFilePath(PHOTO_FILE_NAME);

        //1c.) Wrap the file path into a File provider, e.g. like wrapping Model classes in Parcelable{}!
        // Then put it into the intent!
        Uri fileProvider = FileProvider.getUriForFile(getContext(), CODEPATH_FILE_PROVIDER_KEY, photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);     //URI wraps the photo file

        // So long as the result is not null, we can use an intent to start Camera for capture!
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            //2.) Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    //Purpose:      Create intent to go to gallery to pick a photo --> deal with the response through a Result. Request key used = PICK_PHOTO_CODE constant
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Uri fileProvider = FileProvider.getUriForFile(getContext(), CODEPATH_FILE_PROVIDER_KEY, photoFile);
        gallery.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);     //URI wraps the photo file
        if (gallery.resolveActivity(getContext().getPackageManager()) != null) {
            //2.) Start the image capture intent to take photo
            startActivityForResult(gallery, PICK_PHOTO_CODE);
        }
    }

    /**
     * Purpose:             Obtains the File path of where the given file will be stored in the Activity's external directory. Helper method for launchCamera()
     * @param fileName, the file wanted a complete path for
     * @return the complete path of where the filename is stored
     */
    private File getPhotoFilePath(String fileName) {
        //1.) Get the storage directory where Activity/App has access to. Can store user's photos here until app is killed.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        //2.) Error check:    If the directory doesn't exist and an attempt to create it fails:
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        //3.) Append the filename to the directory path --> creates a complete path, then return it
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    @Override
    //Purpose:          Takes care of the photos coming back from Intent to gallery and Intent to take pictures from launchCamera() and openGallery()
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result is from taking a picture:
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            profileImage.setImageBitmap(takenImage);
            isProfilePicSet = true;
        }

        // Result is from the request to Gallery:
        else if (resultCode == RESULT_OK && requestCode == PICK_PHOTO_CODE) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            isProfilePicSet = true;
        }

        // Result didn't return a picture --> user could also just have not chosen a pic
        else {
            Log.d(TAG, "A picture wasn't taken nor selected.");
        }
    }

    private void showLongTopToast(String message){
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.show();
    }

}