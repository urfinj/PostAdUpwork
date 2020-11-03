package com.example.postad.ui.home;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.postad.BuildConfig;
import com.example.postad.FileCompressor;
import com.example.postad.R;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = "PostFragment";
    private static final int CAMERA_REQUEST_CODE = 1234;
    private static final int GALLERY_REQUEST_CODE = 4321;


    /**Posts Widgets*/
    private EditText mTitle, mDescription, mPrice;
    private Button mPost;
    private ProgressBar mProgressBar;
    private Spinner spinnerCategories;
    private ImageView picture1,picture2,picture3,picture4;


    /**Image Uris*/
    private Uri mImageUri1,mImageUri2,mImageUri3,mImageUri4;
    private Bitmap[] mImagesBitmap;


    private File mPhotoFile1,mPhotoFile2,mPhotoFile3,mPhotoFile4;


    private FileCompressor mCompressor;

    /*
    @Inject
    public PostFragmentContractMVP.presenter presenter;
     */




    @Override
    public void onStart() {
        super.onStart();
        //presenter.setView(this);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Post");
        mCompressor = new FileCompressor(getActivity());


    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mTitle = view.findViewById(R.id.input_title);
        mDescription = view.findViewById(R.id.input_description);
        mPrice = view.findViewById(R.id.input_price);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        picture1 = view.findViewById(R.id.add_offer_picture1);
        picture2 = view.findViewById(R.id.add_offer_picture2);
        picture3 = view.findViewById(R.id.add_offer_picture3);
        picture4 = view.findViewById(R.id.add_offer_picture4);
        mPost = (Button)view.findViewById(R.id.btnAddPost);

        picture1.setOnClickListener(this);
        picture2.setOnClickListener(this);
        picture3.setOnClickListener(this);
        picture4.setOnClickListener(this);
        mPost.setOnClickListener(this);


        //((App) getActivity().getApplication()).getComponent().injectPostFragment(this);


        /**MAPS*/
        // mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());


        spinnerCategories = (Spinner)view.findViewById(R.id.fragment_spinner_category_post);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategories.setAdapter(adapter);
        spinnerCategories.setOnItemSelectedListener(this);


        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);


        return view;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {

            if (requestCode == CAMERA_REQUEST_CODE) {



                mImageUri1 = data.getData();

                Glide.with(getActivity())
                        .load(mPhotoFile1)
                        .into(picture1);

                try {
                    mPhotoFile1 = mCompressor.compressToFile(mPhotoFile1);
                    mImageUri1 =Uri.fromFile(mPhotoFile1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == GALLERY_REQUEST_CODE) {
                mImageUri1 = data.getData();
                picture1.setImageURI(mImageUri1);

                try {
                    mPhotoFile1 = mCompressor.compressToFile(new File(getRealPathFromUri(mImageUri1)));
                    //presenter.savePostsImageFilesToPresenter(mPhotoFile);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }







    private void selectImage(int imageNumber) {
        final CharSequence[] items = {
                "Take Photo", "Choose from Library",
                "Cancel"
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(items, (dialog, item) -> {
            if (items[item].equals("Take Photo")) {
                //requestStoragePermission(true);
                dispatchTakePictureIntent();
                /**
                Intent sendImageNumber = new Intent();
                startActivityForResult(sendImageNumber, imageNumber);
                 */

            } else if (items[item].equals("Choose from Library")) {
                //requestStoragePermission(false);

                dispatchGalleryIntent();
                /**
                Intent sendImageNumber = new Intent();
                startActivityForResult(sendImageNumber, imageNumber);
                 */


            } else if (items[item].equals("Cancel")) {
                dialog.dismiss();

            }
        });
        builder.show();
    }




    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }
            if (photoFile != null) {

                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile);
                mPhotoFile1 = photoFile;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private void dispatchGalleryIntent() {

        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhoto.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(pickPhoto, GALLERY_REQUEST_CODE);
    }



    /**
     * Create file with current timestamp name @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String mFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File mFile = File.createTempFile(mFileName, ".jpg", storageDir);
        return mFile;
    }


    /**Get real file path from URI */
    public String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
            assert cursor != null;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }







    @Override
    public void onClick(View v) {


        switch (v.getId()){
            case R.id.add_offer_picture1:
                selectImage(1);
                break;

            case R.id.add_offer_picture2:
                selectImage(2);
                break;

            case R.id.add_offer_picture3:
                selectImage(3);
                break;
            case R.id.add_offer_picture4:
                selectImage(4);
                break;

            case R.id.btnAddPost:
                Log.d(TAG, "onClick: attempting to post...");
                //presenter.sendPostDataToPresenter(mImageUri1, mTitle.getText().toString(),spinnerCategories.getSelectedItem().toString(),mDescription.getText().toString(),
                //  mPrice.getText().toString(),mPhotoFile1);

                /**
                Intent toUploadActivity = new Intent(getContext(), PreviewUploadPostView.class);
                toUploadActivity.putExtra("postImageUri1",mImageUri1.toString());
                toUploadActivity.putExtra("postTitle",mTitle.getText().toString());
                toUploadActivity.putExtra("postCategory",spinnerCategories.getSelectedItem().toString());
                toUploadActivity.putExtra("postDescription",mDescription.getText().toString());
                toUploadActivity.putExtra("postPrice",mPrice.getText().toString());
                toUploadActivity.putExtra("postImageFile",mPhotoFile1);
                startActivity(toUploadActivity);
                 */

                break;

            default:
                break;


        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()){

            case R.id.fragment_spinner_category_post:

                break;

            default:
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }







    private void resetFields(){

        mTitle.setText("");
        mDescription.setText("");
        mPrice.setText("");
        spinnerCategories.setOnItemSelectedListener(null);
        Glide.with(getContext()).load(R.drawable.camera_logo).into(picture1);
        Glide.with(getContext()).load(R.drawable.camera_logo).into(picture2);
        Glide.with(getContext()).load(R.drawable.camera_logo).into(picture3);
        Glide.with(getContext()).load(R.drawable.camera_logo).into(picture4);

    }


}