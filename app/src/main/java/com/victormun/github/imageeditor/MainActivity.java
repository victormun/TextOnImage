package com.victormun.github.imageeditor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileDescriptor;
import java.io.IOException;

import static com.victormun.github.imageeditor.utils.Constants.MAIN_IMAGE_INTENT_TYPE;
import static com.victormun.github.imageeditor.utils.Constants.MAIN_REQUEST_OPEN;

public class MainActivity extends AppCompatActivity {

    // Class fields
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button openButton;
    private boolean isClicked = false;
    private ImageView imageView;
    private FloatingActionButton addTextButton;

    /**
     * Class OnCreate method
     *
     * @param savedInstanceState state of the rotation
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initListeners();
    }

    /**
     * Method that opens Android's File Opener
     */
    private void openFile() {
        isClicked = true;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(MAIN_IMAGE_INTENT_TYPE);
        startActivityForResult(intent, MAIN_REQUEST_OPEN);
    }

    /**
     * Method that handles the {@link Activity} return data
     *
     * @param requestCode is the code sent through the {@link Intent}
     * @param resultCode  is the result given back from the opened {@link Activity}
     * @param resultData  is the {@link Intent} sent back from the previous {@link Activity}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == MAIN_REQUEST_OPEN && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                showImage(uri);
            }
        }
    }

    /**
     * Method that paints the chosen image inside the ImageView
     *
     * @param uri of the selected {@link android.graphics.drawable.Drawable}
     */
    private void showImage(Uri uri) {
        new Thread(() -> {
            final Bitmap bitmap;
            try {
                bitmap = getBitmapFromUri(uri);
                imageView.post(() -> {
                    imageView.setImageBitmap(bitmap);
                    openButton.setVisibility(View.GONE);
                    addTextButton.show();
                });
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
                Log.e(TAG, "Error loading image: " + e);
            }
        }).start();
    }

    /**
     * Method that adds a {@link android.widget.TextView} to the layout
     */
    private void addTextView() {
        TextView textView = new TextView(this);
        textView.setId(View.generateViewId());
        textView.setText("Introduce your text here");
        textView.setTextSize(20);
        textView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        ConstraintLayout baseLayout = findViewById(R.id.main_base_layout);
        baseLayout.addView(textView);
        ConstraintSet set = new ConstraintSet();
        set.clone(baseLayout);
        set.connect(textView.getId(), ConstraintSet.TOP, baseLayout.getId(), ConstraintSet.TOP, 0);
        set.connect(textView.getId(), ConstraintSet.BOTTOM, baseLayout.getId(), ConstraintSet.BOTTOM, 0);
        set.connect(textView.getId(), ConstraintSet.START, baseLayout.getId(), ConstraintSet.START, 0);
        set.connect(textView.getId(), ConstraintSet.END, baseLayout.getId(), ConstraintSet.END, 0);
        set.applyTo(baseLayout);
    }

    /**
     * Method that initializes all the listeners
     */
    private void initListeners() {
        openButton.setOnClickListener(v -> {
            if (!isClicked)
                openFile();
        });
        addTextButton.setOnClickListener(v -> addTextView());
    }

    /**
     * Method that initialized all the views
     */
    private void initViews() {
        openButton = findViewById(R.id.main_button_open);
        imageView = findViewById(R.id.main_image_view);
        addTextButton = findViewById(R.id.main_button_text);
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException, NullPointerException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
}
