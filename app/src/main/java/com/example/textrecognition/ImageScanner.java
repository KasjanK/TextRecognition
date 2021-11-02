package com.example.textrecognition;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;

public class ImageScanner extends AppCompatActivity {

    Button btn;
    Button detectBtn;
    private TextView resultTV;
    private ImageView imageView;
    private Uri uri;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_scanner);

        btn = findViewById(R.id.btn);
        imageView = findViewById(R.id.pickedImage);
        resultTV = findViewById(R.id.detectedText);
        detectBtn = findViewById(R.id.detectBtn);

        textToSpeech = new TextToSpeech(this, status -> {
        });

        detectBtn.setOnClickListener(view -> detectText());

        btn.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            getContent.launch(intent);
        });
    }

    ActivityResultLauncher<Intent> getContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                uri = data.getData();
                imageView.setImageURI(uri);
            }
        }
    });

    // recognizing and saying the text from images
    private void detectText() {
        // textrecognizer from google's api
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // preparing the chosen image
        InputImage image = null;
        try {
            image = InputImage.fromFilePath(this, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // processing of image
        Task<Text> result = recognizer.process(image).addOnSuccessListener(text -> {
            for (Text.TextBlock block: text.getTextBlocks()) {
                String blockText = block.getText();
                resultTV.setText(blockText);

                // converting the block to string so the tts can recognize it
                String words = resultTV.getText().toString();

                // making tts say the text
                textToSpeech.speak(words, TextToSpeech.QUEUE_FLUSH, null, null);
            }
            // if the app fail, show a message
        }).addOnFailureListener(e -> Toast.makeText(ImageScanner.this, "failed to detect text from image", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}