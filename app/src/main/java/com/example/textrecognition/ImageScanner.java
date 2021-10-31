package com.example.textrecognition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    int SELECT_IMAGE_CODE = 1;

    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_scanner);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
            }
        });

        btn = findViewById(R.id.btn);
        imageView = findViewById(R.id.pickedImage);
        resultTV = findViewById(R.id.detectedText);
        detectBtn = findViewById(R.id.detectBtn);

        detectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectText();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {

            // välj bild-menyn
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Title"), SELECT_IMAGE_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            uri = data.getData();
            imageView.setImageURI(uri);
        }
    }

    // känner igen text från bilden
    private void detectText() {
        // implementerar en textrecognizer från googles api
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // förbereder den valda bilden
        InputImage image = null;
        try {
            image = InputImage.fromFilePath(this, uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // bearbetar bilden
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            // om den lyckas, så extraheras text ur bilden
            public void onSuccess(@NonNull Text text) {
                StringBuilder result = new StringBuilder();
                for (Text.TextBlock block: text.getTextBlocks()) {
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for (Text.Line line : block.getLines()) {
                        String lineText = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineFrame = line.getBoundingBox();
                        for (Text.Element element : line.getElements()) {
                            String elementText = element.getText();
                            Point[] elementCornerPoints = element.getCornerPoints();
                            Rect elementFrame = element.getBoundingBox();
                            result.append(elementText);
                        }
                        resultTV.setText(blockText);
                        // making the block to string so the tts can recognize it
                        String words = resultTV.getText().toString();
                        // making tts say the text
                        textToSpeech.speak(words, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            // om appen misslyckas så får man ett meddelande att nåt är fel
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ImageScanner.this, "failed to detect text from image", Toast.LENGTH_SHORT).show();

            }
        });


    }
}