package com.example.skincare.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.facebook.FacebookSdk.getApplicationContext;


public class HomeFragment extends Fragment {
    private ImageButton imageTake;

    private static final int CAMERA_REQUEST =1;
    private static final int Gallery_REQUEST =2;

    private ImageView imageView;
    private LinearLayout buttonLayout;
    private RelativeLayout buttonLayout2;
    private Button continuePic;
    private Button discardPic;

    private FirebaseAuth fAuth;

    private static final String TAG = "UploadActivity";
    private StorageReference reference;
    private FirebaseStorage storage;
    private String userID;

    private Uri filePath;

    private Desease desease;
    private DeseaseDbHelper deseaseDbHelper;


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
        buttonLayout = v.findViewById(R.id.buttonLayout);
        buttonLayout2 = v.findViewById(R.id.buttonLayout2);
        continuePic = v.findViewById(R.id.continuePic);
        discardPic = v.findViewById(R.id.discardPic);

        deseaseDbHelper = new DeseaseDbHelper(getActivity());
        fAuth = FirebaseAuth.getInstance();
        FirebaseUser user = fAuth.getCurrentUser();
        userID = user.getUid();

        storage = FirebaseStorage.getInstance();
        reference = storage.getReference();

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
                res = "";
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
              /*  if (filePath != null) {
                    //displaying a progress dialog while upload is going on
                    final ProgressDialog progressDialog = new ProgressDialog(getApplicationContext());
                    progressDialog.setTitle("Uploading");
                    progressDialog.show();

                    StorageReference riversRef = reference.child("images/pic.jpg");
                    riversRef.putFile(filePath)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    //if the upload is successfull
                                    //hiding the progress dialog
                                    progressDialog.dismiss();
                                    //and displaying a success toast
                                    Toast.makeText(getApplicationContext(), "File Uploaded ", Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    //if the upload is not successfull
                                    //hiding the progress dialog
                                    progressDialog.dismiss();
                                    //and displaying error message
                                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    //calculating progress percentage
                                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                    //displaying percentage in progress dialog
                                    progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                                }
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "filepath empty", Toast.LENGTH_LONG).show();

                }*/

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
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageView.setImageBitmap(imageBitmap);
                }
                break;

            case Gallery_REQUEST:

                if (resultCode == RESULT_OK) {
                    Uri selectedImage = data.getData();
                    imageView.setImageURI(selectedImage);
                }
                break;
        }


            imageView.setVisibility(View.VISIBLE);
            buttonLayout.setVisibility(View.VISIBLE);
            buttonLayout2.setVisibility(View.INVISIBLE);


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
                                String ress="";

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


                                for (int i = 0; i < probabilities.length; i++) {
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


                           /*for (int i = 0; i < probabilities.length; i++) {
                                    try {
                                        label = reader.readLine();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    String label3 =getLabel(label);

                                    String x3=String.format("%1.2f", probabilities[i]*100);
                                    ress = ress+" "+label3+" "+ x3+ "\n";

                                }*/
                                String x1=String.format("%1.2f", max*100);
                                String x2=String.format("%1.2f", max2*100);
                                String x3=String.format("%1.2f", max3*100);

                           res = res+" "+labels[l]+": "+ x1+"%"+ "\n"+labels[l2]+": "+ x2+"%"+ "\n"+labels[l3]+": "+ x3+"%";
                           Toast.makeText(getApplicationContext(),res + ress, Toast.LENGTH_LONG).show();

                            }
                        })


                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "failed to upload image"+ e.getMessage().toString(), Toast.LENGTH_LONG).show();

                            }
                        });



    }

    private Bitmap getYourInputImage() {
        // This method is just for show
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
