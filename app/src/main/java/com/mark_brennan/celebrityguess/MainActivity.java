package com.mark_brennan.celebrityguess;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Button nameButton1;
    Button nameButton2;
    Button nameButton3;
    Button nameButton4;
    ImageView celebImageView;
    int correctCeleb;
    int locationOfCorrectAnswer = 0;

    // Create a Random generator
    Random random = new Random();

    String websiteSourceCode = null;
    ArrayList<String> celebUrls = new ArrayList<>();
    ArrayList<String> celebNames = new ArrayList<>();



    public class DownloadImages extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                Bitmap celebBitmap = BitmapFactory.decodeStream(inputStream);

                return celebBitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }



    public class DownloadWebPage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char)data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }

        }
    }


    public void guessName(View view) {

        // Display toast depending on if chosen answer is correct or incorrect
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            Toast.makeText(getApplicationContext(), "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Wrong! It was " + celebNames.get(correctCeleb), Toast.LENGTH_SHORT).show();
        }

        generateCelebrity();
    }



    public void parseWebsite() {

        DownloadWebPage task = new DownloadWebPage();

        try {
            websiteSourceCode = task.execute("http://www.posh24.com/celebrities").get();
            String[] splitResult = websiteSourceCode.split("<div class=\"sidebarContainer\">");

            // Find and separate all profile images
            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);
            while (m.find()) {
                celebUrls.add(m.group(1));
            }

            // Find and separate all alt tags (Celeb Names)
            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);
            while (m.find()) {
                celebNames.add(m.group(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void generateCelebrity() {

        // Generate a new celebrity
        correctCeleb = random.nextInt(celebUrls.size());

        // Execute download image task
        DownloadImages task = new DownloadImages();
        try {
            Bitmap celebImage;
            celebImage = task.execute(celebUrls.get(correctCeleb)).get();
            celebImageView.setImageBitmap(celebImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Generate answers
        ArrayList<Integer> celebAnswers = new ArrayList<Integer>();
        locationOfCorrectAnswer = random.nextInt(4);

        for (int i = 0; i < 4; i++) {
            if (i == locationOfCorrectAnswer) {
                celebAnswers.add(correctCeleb);
            } else {
                int incorrectAnswer = random.nextInt(celebUrls.size());
                while (incorrectAnswer == correctCeleb) {
                    incorrectAnswer = random.nextInt(celebUrls.size());
                }
                celebAnswers.add(incorrectAnswer);
            }
        }

        // Add answers to buttons
        nameButton1.setText(celebNames.get(celebAnswers.get(0)));
        nameButton2.setText(celebNames.get(celebAnswers.get(1)));
        nameButton3.setText(celebNames.get(celebAnswers.get(2)));
        nameButton4.setText(celebNames.get(celebAnswers.get(3)));
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find views
        nameButton1 = (Button)findViewById(R.id.nameButton1);
        nameButton2 = (Button)findViewById(R.id.nameButton2);
        nameButton3 = (Button)findViewById(R.id.nameButton3);
        nameButton4 = (Button)findViewById(R.id.nameButton4);
        celebImageView = (ImageView)findViewById(R.id.celebImage);

        parseWebsite();
        generateCelebrity();

    }
}