package com.example.wings.mainactivity.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wings.R;
import com.example.wings.mainactivity.MAFragmentsListener;
import com.example.wings.models.User;

import com.example.wings.startactivity.StartActivity;
import com.parse.ParseFile;
import com.parse.ParseUser;



import java.io.File;

import static android.app.Activity.RESULT_OK;


//All auto-filled stuff, just follow the samples I left behind!


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment# newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {
    public static final String TAG = "UserProfileFragment";

    private MAFragmentsListener listener;


    private ImageView profilePic;
    private TextView profileName;
    private TextView profilePin;
    private TextView profileEmail;
    private RatingBar profileRating;
    private Button logOutBtn;
    //Added for edit pic button...
    private ImageButton cameraBttn;
    private ImageButton galleryBttn;
    public File photoFile;
    //Added for edit pic button...
    private static final String CODEPATH_FILE_PROVIDER_KEY = "com.codepath.fileprovider";
    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 42;
    public static final int PICK_PHOTO_CODE = 1046;
    private static final String PHOTO_FILE_NAME = "photo.jpg";         //arbitrary file name to store Post photo in


    public UserProfileFragment() {}   // Required empty public constructor

    @Override
    /**
     * Purpose:     Called automatically. When this Fragment is being attached to the parent activity, REQUIRE the activity to implement MAFragmentsListener. Otherwise throw an exception!
     *              Connect the Fragment's listener to the activity!
     */
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MAFragmentsListener) {
            listener = (MAFragmentsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement MAFragmentsListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_profile, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        profilePic = view.findViewById(R.id.profilePic);
        profileName = view.findViewById(R.id.profileName);
        profilePin = view.findViewById(R.id.profilePin);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileRating = view.findViewById(R.id.profileRating);
        logOutBtn = view.findViewById(R.id.logOutBtn);
        //Added for edit pic button...
        cameraBttn = view.findViewById(R.id.cameraBttn);
        galleryBttn = view.findViewById(R.id.galleryBttn);

        ParseUser current = ParseUser.getCurrentUser();
        profileName.setText(current.getString(User.KEY_FIRSTNAME));
        profilePin.setText("Pin Code: " + Integer.toString(current.getInt(User.KEY_PIN)));
        profileEmail.setText(current.getString(User.KEY_EMAIL));
        profileRating.setRating((float) current.getInt(User.KEY_RATING));

        ParseFile image = current.getParseFile(User.KEY_PROFILEPICTURE);
        if(image != null){
            Glide.with(getContext())
                    .load(image.getUrl())
                    .override(400, 400)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(profilePic);
        }

        //Added for edit pic button...
        cameraBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCamera();
            }
        });

        galleryBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1.) Log off the user using Parse:
                current.logOut();

                //2.) Intent to go to StartActivity, finish() this activity
                Intent intent;
                Log.d(TAG, ": Trying to do intent now.");
                intent = new Intent(getContext(), StartActivity.class);
                startActivity(intent);
            }
        });



    }


// ADDED for Edit Pic Button...

    // Trigger gallery selection for a photo
    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_PHOTO_CODE);
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
            profilePic.setImageBitmap(takenImage);
        } else if (resultCode == RESULT_OK && requestCode == PICK_PHOTO_CODE) {
            Uri imageUri = data.getData();
            profilePic.setImageURI(imageUri);
        } else { // Result was a failure
            Toast.makeText(getContext(), "Picture wasn't taken nor selected!", Toast.LENGTH_SHORT).show();
        }
    }
}