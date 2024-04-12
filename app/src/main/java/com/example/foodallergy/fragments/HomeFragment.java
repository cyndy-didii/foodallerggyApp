package com.example.foodallergy.fragments;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.foodallergy.R;
import com.example.foodallergy.config.ApplicationConfig;
import com.example.foodallergy.model.RecognitionResponse;
import com.example.foodallergy.utils.Helper;
import com.example.foodallergy.utils.VolleyMultipartRequest;
import com.example.foodallergy.utils.VolleySingleton;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HomeFragment extends Fragment {

    FloatingActionButton fab;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int PICK_IMAGE_REQUEST =1 ;
    private ImageView imageSelected;
    private String filePath;
    private Bitmap bitmap;
    private Helper helper;
    private RequestQueue requestQueue;
    private TextView greeting;
    private String token;
    private String allergies;
    private ConstraintLayout layoutDialogContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("Home");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        imageSelected = (ImageView) view.findViewById(R.id.imageSelected);
        requestQueue = VolleySingleton.getmInstance(getContext()).getRequestQueue();
        greeting = (TextView) view.findViewById(R.id.imageBack);
        helper = new Helper(getActivity());
        // Find other views within the layout
        layoutDialogContainer = view.findViewById(R.id.layoutDialogContainer);
        return view;
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });

        SharedPreferences preferences = getActivity().getSharedPreferences(ApplicationConfig.APP_PREFERENCE_NAME, MODE_PRIVATE);
        String name = preferences.getString("full_name", ApplicationConfig.APP_ANONYMOUS_NAME).split(" ")[0];
        token = preferences.getString("token", "");
        allergies = preferences.getString("allergies", "");

        greeting.setText("Hi, "+ name);
    }

    private void showBottomDialog() {

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        LinearLayout cameraLayout = dialog.findViewById(R.id.layoutCamera);
        LinearLayout galleryLayout = dialog.findViewById(R.id.layoutGallery);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);

        cameraLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                openCamera();

            }
        });

        galleryLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
                if ((ContextCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getContext(),
                        android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                    if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            android.Manifest.permission.CAMERA))) {

                    } else {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                REQUEST_PERMISSIONS);
                    }
                } else {
                    Log.e("Else", "Else");
                    selectImage();
                }

            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void openCamera() {

        /*Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);*/
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestCameraPermission();
        } else {
            // Permission is granted
            startCameraIntent();
        }
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{android.Manifest.permission.CAMERA},
                REQUEST_PERMISSIONS);
    }

    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri picUri = data.getData();
            filePath = getPath(picUri);
            if (filePath != null) {
                try {

                    //textView.setText("File Selected");
                    Log.d("filePath", String.valueOf(filePath));
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), picUri);
                    //imageSelected.setImageBitmap(bitmap);
                    Toast.makeText(getContext(), "Image Selected", Toast.LENGTH_SHORT).show();
                    performImageRecognition();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(getContext(),"No image selected", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            filePath = "Not file path, captured image with camera";
            //imageSelected.setImageBitmap(bitmap);
            Toast.makeText(getContext(), "Image captured and Selected", Toast.LENGTH_SHORT).show();
            performImageRecognition();
        }

    }

    public String getPath(Uri uri) {
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        if(document_id != null) {
            document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        }
        else {
            Toast.makeText(getContext(), "This Image is not supported", Toast.LENGTH_SHORT).show();
            return null;
        }
        cursor.close();

        cursor = getActivity().getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = null;
        if (cursor != null && cursor.moveToFirst()) {
            path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
        }
        else {
            Toast.makeText(getContext(), "Error getting file path", Toast.LENGTH_SHORT).show();
        }
        cursor.close();

        return path;
    }

    public void performImageRecognition() {
        if (allergies.isEmpty()) {
            Toast.makeText(getContext(), "There are no allergies found for this user, create an allergy list", Toast.LENGTH_LONG).show();
        }
        else {
            if (bitmap == null) {
                Toast.makeText(getContext(), "Tap on the image icon to capture a Fruit or Vegetable image", Toast.LENGTH_LONG).show();
            } else {
                //Initializing progress  indicator
                ProgressDialog mDialog = new ProgressDialog(getContext());
                mDialog.setMessage("Verifying image, please wait...");
                mDialog.show();

                String url = ApplicationConfig.BASE_URL.concat("check_food");
                VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        mDialog.dismiss();
                        if (response.statusCode == 200) {
                            Toast.makeText(getContext(), "Image validated successfully", Toast.LENGTH_SHORT).show();
                            //Log.d(TAG, "onResponse: " + response);
                            String resultResponse = new String(response.data);
                            /*
                            JSONObject food = response.data..getJSONObject("user");
                             Convert JSON to Java object
                            */
                            ObjectMapper objectMapper = new ObjectMapper();
                            try {
                                RecognitionResponse res = objectMapper.readValue(resultResponse, RecognitionResponse.class);
                                showSuccessDialog(res.getRecognisedFood().getName(), res.getRecognisedFood().getIngredients());
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                            //Log.d(TAG, "onResponse: " + resultResponse);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mDialog.dismiss();
                        helper.parseVolleyError(error);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();

                        return params;
                    }

                    @Override
                    protected Map<String, DataPart> getByteData() {
                        Map<String, DataPart> params = new HashMap<>();
                        long imagename = System.currentTimeMillis();
                        params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                        return params;
                    }

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Bearer " + token);
                        return headers;
                    }
                };

                multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                this.requestQueue.add(multipartRequest);
            }
        }
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void showSuccessDialog(String name, String ingredients){


        SharedPreferences preferences = getActivity().getSharedPreferences(ApplicationConfig.APP_PREFERENCE_NAME, MODE_PRIVATE);
        allergies = preferences.getString("allergies", "");
        String[] substrings = allergies.split(",");

        String[] ingredientList = ingredients.split(",");

        String elementsInAllergyList = Arrays.stream(substrings)
                .filter(element -> Arrays.asList(ingredientList).contains(element.trim().toLowerCase()))
                .collect(Collectors.joining(",")).replaceAll("\\s*,\\s*", ",").toLowerCase();

        /*String preparedAllergyString;
        if (allergies.isEmpty()) {
            preparedAllergyString = elementsNotInAllergyInput;
        } else {
            preparedAllergyString = allergies.concat(",").concat(elementsNotInAllergyInput);
        }*/

        Log.d("TAG", elementsInAllergyList);
        String message;
        if(elementsInAllergyList.isEmpty()) {
            message = "This food does not contain any edible that you are allergic to, You are free to consume!";
        }
        else {
            message = "This food contains "+ elementsInAllergyList + " you are not advised to consume this food";
        }

        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_success_dialog);

        ((TextView) dialog.findViewById(R.id.textTitle)).setText("Result");
        ((TextView) dialog.findViewById(R.id.textMessage)).setText("Food: "+ name +" Feedback: "+ message);
        ((Button) dialog.findViewById(R.id.buttonAction)).setText("Done");
        ((ImageView) dialog.findViewById(R.id.imageIcon)).setImageResource(R.drawable.done);


        dialog.findViewById(R.id.buttonAction).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);

    }

}