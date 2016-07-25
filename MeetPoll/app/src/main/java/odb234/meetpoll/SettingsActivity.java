package odb234.meetpoll;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.Inflater;

/**
 * Created by rspiegel on 7/17/16.
 */
public class SettingsActivity extends AppCompatActivity {
    private static TextView tv;
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;
    private static FragmentManager fm;
    private static ImageView iv;
    private static LayoutInflater inflate;
    private static BitmapFactory.Options options;

    private static Uri selectedImageUri;
    public static String picture_path;


    private static final String TAG = "Settings Activity";
    private static final int SELECT_PICTURE = 1;
    private static final int TAKE_PICTURE = 2;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 32);

        inflate = getLayoutInflater();
        tv = (TextView)findViewById(R.id.your_name);
        iv = (ImageView)findViewById(R.id.your_picture);
        sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        tv.setText(sp.getString("name",tv.getText().toString()));
        editor = sp.edit();
        fm = getFragmentManager();
        options = new BitmapFactory.Options();
        picture_path = sp.getString("picture_path",null);
        if(picture_path == null)
            iv.setImageResource(R.drawable.ic_person_black_36dp);
        else
            loadImageFromStorage(picture_path);
    }

    public void changeName(View v)
    {
        nameFragment nf = new nameFragment();
        nf.show(fm, "nameFragment");
    }

    public static class nameFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final EditText userInput = new EditText(getContext());
            userInput.setText(tv.getText());
            userInput.setSelection(userInput.length());
            userInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
         
            builder.setView(userInput);
            builder.setMessage("Change your name: ")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String input = userInput.getText().toString();
                            tv.setText(input);
                            editor.putString("name",input);
                            editor.apply();
                            dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
            return builder.create();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void changePicture(View v)
    {
        pictureFragment nf = new pictureFragment();
        nf.show(fm, "pictureFragment");
    }
    public void camera(View v)
    {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, TAKE_PICTURE);
    }
    public void gallery(View v)
    {
        iv.setImageResource(android.R.color.transparent);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case SELECT_PICTURE:
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
                    selectedImageUri = data.getData();
                    new saveImageAT().execute();
                }
                break;
        }
    }
    public class saveImageAT extends AsyncTask<Void, Void, Bitmap>{
        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                InputStream is2 = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bm = decodeSampledBitmapFromStream(is, is2, 60, 60);
                is.close();
                is2.close();
                return bm;
            } catch (Exception e){
                Log.d(TAG,"Caught " + e.toString());
            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap b) {
            iv.setImageBitmap(b);
            saveToInternalStorage(b);
        }
        //https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
        private Bitmap decodeSampledBitmapFromStream(InputStream is, InputStream is2, int reqWidth, int reqHeight) {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options);
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            Bitmap bm = BitmapFactory.decodeStream(is2,null,options);
            return bm;
        }
        //https://developer.android.com/training/displaying-bitmaps/load-bitmap.html
        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                while ((halfHeight / inSampleSize) >= reqHeight
                        && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

    }
    //http://stackoverflow.com/a/17674787
    //but changed a little.
    private void saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath=new File(directory,"your_picture.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        picture_path = directory.getAbsolutePath();
        editor.putString("picture_path",picture_path);
        editor.apply();
    }
    //http://stackoverflow.com/a/17674787
    //but changed it a little.
    public void loadImageFromStorage(String path)
    {
        try {
            File f=new File(path, "your_picture.jpg");
            selectedImageUri = Uri.fromFile(f);
            new saveImageAT().execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    public static class pictureFragment extends DialogFragment
    {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View userInput = inflate.inflate(R.layout.fragment_picture_layout,null);
            builder.setView(userInput);
            builder.setMessage("Open Camera or Gallery?")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    });
            return builder.create();
        }
    }
}
