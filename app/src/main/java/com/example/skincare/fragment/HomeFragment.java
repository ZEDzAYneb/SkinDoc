package com.example.skincare.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.skincare.R;
import com.example.skincare.adapter.DeseaseDbHelper;
import com.example.skincare.models.Desease;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.facebook.FacebookSdk.getApplicationContext;


public class HomeFragment extends Fragment {
    private static final int CAMERA_REQUEST = 33;
    private static final int Gallery_REQUEST = 333;

    private ImageButton imageTake;
    private ImageView imageView;

    private Button continuePic;
    private Button discardPic;
    private LinearLayout layout1;
    private RelativeLayout layout2;

    private CropImageView image;

    private FirebaseAuth fAuth;
    private String userID;

    private Desease desease;


    private FirebaseCustomRemoteModel remoteModel;
    private FirebaseModelInterpreterOptions options;
    private FirebaseModelDownloadConditions conditions;
    private FirebaseModelInterpreter interpreter;
    private FirebaseModelInputOutputOptions inputOutputOptions;
    private FirebaseModelInputs inputs;

    private String res = "";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.home_fragment,container,false);

        imageTake = v.findViewById(R.id.imageTake);
        imageView = v.findViewById(R.id.imageView);

        image = v.findViewById(R.id.cropImageView);


        continuePic= v.findViewById(R.id.continuePic);
        discardPic= v.findViewById(R.id.discardPic);
        layout1= v.findViewById(R.id.buttonLayout);
        layout2= v.findViewById(R.id.buttonLayout2);

        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();
        userID = user.getUid();


        final FragmentManager fragmentManager = getFragmentManager();

        imageTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });
        discardPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });


        remoteModel = new FirebaseCustomRemoteModel.Builder("diagnosis_ham1000").build();
        conditions = new FirebaseModelDownloadConditions.Builder().requireWifi().build();

        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "model downloaded successfully", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "model didn't download ", Toast.LENGTH_LONG).show();

                        }
                    }
                });

        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel)
                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean isDownloaded) {
                        if (isDownloaded) {
                            options = new FirebaseModelInterpreterOptions.Builder(remoteModel).build();
                            try {
                                interpreter = FirebaseModelInterpreter.getInstance(options);
                                inputOutputOptions = new FirebaseModelInputOutputOptions.Builder()
                                        .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 224, 224, 3})
                                        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 7})
                                        .build();
                            } catch (FirebaseMLException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();

                            }
                        }
                    }

                });

        continuePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap image = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                try{
                    desease = new Desease(userID, res,image);

                    ResultFragment resultFragment = new ResultFragment ();
                    Bundle args = new Bundle();
                    args.putParcelable("Desease", desease);
                    resultFragment.setArguments(args);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, resultFragment).commit();
                        // fragmentManager.beginTransaction().replace(R.id.fragment_container, new SavedFragment()).commit();

                }catch (Exception e){
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });

        return v;
    }


    private void SelectImage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA}, 99);
        }else {

            if(checkCameraHardware(this.getContext())) {
                final CharSequence[] items = {"Camera", "Gallery", "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Upload your disease picture");

                builder.setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (items[i].equals("Camera")) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
                            Uri uri = FileProvider.getUriForFile(getContext(), getApplicationContext().getPackageName() + ".provider", file);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                            startActivityForResult(intent, CAMERA_REQUEST);

                        } else if (items[i].equals("Gallery")) {
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto, Gallery_REQUEST);
                        } else if (items[i].equals("Cancel")) {
                            dialogInterface.dismiss();
                        }
                    }
                });
                builder.show();
            }
        }
    }



    private boolean checkCameraHardware(Context ctx) {
        if(ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            return true;
        } else {
            return false;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 99 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            imageTake.setEnabled(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CAMERA_REQUEST:

                if (resultCode == RESULT_OK) {
                    File file = new File(Environment.getExternalStorageDirectory(), "MyPhoto.jpg");
                    Uri uri = FileProvider.getUriForFile(getContext(), getApplicationContext().getPackageName() + ".provider", file);
                    startCropImageActivity(uri);
                }
                break;

            case Gallery_REQUEST:

                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    startCropImageActivity(selectedImage);
                }
                break;
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();
                image.setVisibility(View.INVISIBLE);
                layout1.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageURI(resultUri);


                Bitmap bitmap = getYourInputImage();
                int batchNum = 0;
                float[][][][] input = new float[1][224][224][3];
                for (int x = 0; x < 224; x++) {
                    for (int y = 0; y < 224; y++) {
                        int pixel = bitmap.getPixel(x, y);
                        input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
                        input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
                        input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
                    }
                }

                try {
                    inputs = new FirebaseModelInputs.Builder().add(input).build();
                } catch (FirebaseMLException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_LONG).show();
                }

                interpreter.run(inputs, inputOutputOptions)
                        .addOnSuccessListener(
                                new OnSuccessListener<FirebaseModelOutputs>() {
                                    @Override
                                    public void onSuccess(FirebaseModelOutputs result) {
                                        Toast.makeText(getApplicationContext(),"uploaded image successfully", Toast.LENGTH_LONG).show();
                                        float[][] output = result.getOutput(0);
                                        float[] probabilities = output[0];
                                        String label = null;
                                        BufferedReader reader = null;

                                        String []labels = new String[7];
                                        float max = probabilities[0];
                                        float max2 = probabilities[0];
                                        float max3 = probabilities[0];
                                        int l=0;
                                        int l2=0;
                                        int l3=0;

                                        try {
                                            reader = new BufferedReader(new InputStreamReader(getApplicationContext().getAssets().open("labels")));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            Toast.makeText(getApplicationContext(),"reader"+ e.getMessage(), Toast.LENGTH_LONG).show();
                                        }


                                      /*  for (int i = 0; i < probabilities.length; i++) {
                                            if( probabilities[i] > max ){
                                                max3=max2;
                                                max2 = max;
                                                max= probabilities[i];
                                                l=i;
                                            }else if(probabilities[i] > max2 ){
                                                max3=max2;
                                                max2 = probabilities[i];
                                                l2=i;
                                            }else if(probabilities[i] > max3 ){
                                                max3 = probabilities[i];
                                                l3=i;
                                            }
                                        }*/

                                        for (int i = 0; i < probabilities.length; i++) {
                                            if( probabilities[i] > max ){
                                                max= probabilities[i];
                                                l=i;
                                            }
                                        }

                                        for (int i = 0; i < probabilities.length; i++) {
                                            if( probabilities[i] >= max2 && probabilities[i] < max){
                                                max2= probabilities[i];
                                                l2=i;
                                            }
                                        }

                                         for (int i = 0; i < probabilities.length; i++) {
                                            if( probabilities[i] >= max3 && probabilities[i] < max2){
                                                max3= probabilities[i];
                                                l3=i;
                                            }
                                         }


                                        for (int i = 0; i < probabilities.length; i++) {
                                            try {
                                                label = reader.readLine();

                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            String label2 =getLabel(label);
                                            labels[i]=label2;
                                        }


                                        String x1=String.format("%1.2f", max*100);
                                        String x2=String.format("%1.2f", max2*100);
                                        String x3=String.format("%1.2f", max3*100);

                                        res = res+" "+labels[l]+": "+ x1+"%"+ "\n"+labels[l2]+": "+ x2+"%"+ "\n"+labels[l3]+": "+ x3+"%";
                                        Toast.makeText(getApplicationContext(),res, Toast.LENGTH_LONG).show();

                                    }
                                })


                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "failed to upload image"+ e.getMessage().toString(), Toast.LENGTH_LONG).show();

                                    }
                                });



            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }




    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(getContext(),this);
    }




    private Bitmap getYourInputImage() {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        Bitmap bitmapp=Bitmap.createScaledBitmap(bitmap,224,224,true);
        Bitmap bitmap2= bitmapp.copy(Bitmap.Config.ARGB_8888, true);
        return bitmap2;
    }

    private String getLabel(String label){
        if(label.equals("mel")){
            return "Melanoma";
        }
        if(label.equals("nv")){
            return "Melanocytic nevi";
        }
        if(label.equals("df")){
            return "Dermatofibroma";
        }
        if(label.equals("bkl")){
            return "Benign keratosis";
        }
        if(label.equals("vasc")){
            return "Vascular lesion";
        }
        if(label.equals("akiec")){
            return "Actinic Keratoses";
        }
        if(label.equals("bcc")){
            return "Basal cell carcinoma";
        }
        return "";
    }

}
