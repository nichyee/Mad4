package com.mad.exercise4;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * This is the Main Activity class that handles all the background work you see in this app
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    public static final String RYAN_HEISE_SERVICE_LONG = "https://www.ryanheise.com/sarcastic.cgi?len=Long";
    public static final String RYAN_HEISE_SERVICE_MEDIUM = "https://www.ryanheise.com/sarcastic.cgi?len=Medium";
    public static final String RYAN_HEISE_SERVICE_SHORT = "https://www.ryanheise.com/sarcastic.cgi?len=Short";
    public static final String LONG_STRING = "Long";
    public static final String MED_STRING = "Medium";
    private TextView firstJoke;
    private TextView secondJoke;
    private TextView thirdJoke;
    private Integer numJokes = 3;
    private Spinner lengthSpinner;
    private String[] jokeArray = new String[numJokes];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstJoke = findViewById(R.id.first_joke_tv);
        secondJoke = findViewById(R.id.second_joke_tv);
        thirdJoke = findViewById(R.id.third_joke_tv);

        Button oneJoke = findViewById(R.id.one_joke_btn);
        oneJoke.setOnClickListener(this);

        Button threeJoke = findViewById(R.id.three_joke_btn);
        threeJoke.setOnClickListener(this);

        lengthSpinner = findViewById(R.id.length_spinner);
        lengthSpinner.setOnItemSelectedListener(this);

    }

    /**
     * This  method connects to the internet joke service, and returns a single joke
     * @return a string that contains a joke
     * @throws Exception if the app cannot get a joke for whatever reason, it will throw this exception
     */
    public String getJoke() throws IOException {
        URL url;
        //Open a connection to the web service
        switch (lengthSpinner.getSelectedItem().toString()) {
            case LONG_STRING:
                url = new URL(RYAN_HEISE_SERVICE_LONG);
                break;
            case MED_STRING:
                url = new URL(RYAN_HEISE_SERVICE_MEDIUM);
                break;
            default:
                url = new URL(RYAN_HEISE_SERVICE_SHORT);
                break;
        }
        URLConnection conn = url.openConnection();
        //Obtain the input stream
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        //The joke is a one liner, so just read on line
        String joke = in.readLine();
        //Close the connection
        in.close();
        //Return the joke
        return joke;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.one_joke_btn:
                new Download1JokeAsyncTask(this).execute();
                break;
            case R.id.three_joke_btn:
                new DownloadNJokesAsyncTask(this, numJokes).execute();
                break;
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(MainActivity.this, lengthSpinner.getSelectedItem() + getString(R.string.jokes_selected), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * This downloads a single joke through an asynchronous method
     */
    private class Download1JokeAsyncTask extends AsyncTask<Void, Void, String> {

        private String joke;
        private ProgressDialog dialog;

        private Download1JokeAsyncTask(MainActivity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage(getString(R.string.downloading_joke));
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                joke = getJoke();
                return joke;
            } catch (Exception e) {
                joke = getString(R.string.single_joke_fail);
                return joke;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            firstJoke.setText(s);
            secondJoke.setText(R.string.blank);
            thirdJoke.setText(R.string.blank);
        }
    }


    /**
     * This downloads 'N' amount of jokes through an asynchronous method
     */
    private class DownloadNJokesAsyncTask extends AsyncTask<Void, Integer, String[]> {

        Resources res = getResources();
        private ProgressDialog dialog;
        private int noOfJokesToDownload;
        List<String> jokes = new ArrayList<>();
        boolean failed;
        String joke;

        private DownloadNJokesAsyncTask(MainActivity activity, Integer numJokes) {
            dialog = new ProgressDialog(activity);
            noOfJokesToDownload = numJokes;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            String download = res.getString(R.string.downloading_s, getString(R.string.one));
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage(download);
            dialog.setCancelable(false);
            dialog.setMax(noOfJokesToDownload);
            dialog.show();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Integer number = values[0]+1;
            String downloading = res.getString(R.string.downloading_s, number.toString());
            dialog.setProgress(values[0]);
            dialog.setMessage(downloading);
        }

        @Override
        protected String[] doInBackground(Void... voids) {

            for (int i = 0; i < noOfJokesToDownload; i++) {
                try {
                    joke = getJoke();
                } catch (IOException e) {
                    failed = true;
                }
                jokes.add(joke);
                publishProgress(i+1);
            }
            return jokes.toArray(jokeArray);
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            if (failed) {
                firstJoke.setText(R.string.failed_jokes);
                secondJoke.setText(R.string.blank);
                thirdJoke.setText(R.string.blank);
            } else {
                for (int i = 0; i < strings.length; i++) {
                    switch (i) {
                        case 0:
                            firstJoke.setText(strings[i]);
                            break;
                        case 1:
                            secondJoke.setText(strings[i]);
                            break;
                        case 2:
                            thirdJoke.setText(strings[i]);
                            break;
                    }
                }
            }
        }
    }

}
