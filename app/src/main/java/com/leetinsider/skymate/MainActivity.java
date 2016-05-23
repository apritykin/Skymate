package com.leetinsider.skymate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Compose the Forecast IO url into separate variables
        String apiKey = "4d0e45683b7534f37310e7e716272643";
        double latitude = 37.8267;
        double longitude = -122.423;
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/" + latitude + "," + longitude;

        // Check if network is available using isNetworkAvailable(). True then fire GET Request
        // Else display Toast message of no network.
        if (isNetworkAvailable()) {
            // Start a new http client request to GET JSON data
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        Log.v(TAG, response.body().string());
                        if (response.isSuccessful()) {

                        } else {
                            // Dialog text appear if there is an issue with connecting to the service
                            alertUserAboutError("Sorry!", "There was an error");
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught: ", e);
                    }

                }
            });
        }
        else {
            alertUserAboutError("Network!", "Device not connected to network. Please verify " +
                    "network connectivity or signal.");
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError(String alertTitle, String alertBody) {
        Bundle messageArgs = new Bundle();

        // Use the arguments to set up the bundle
        messageArgs.putString(AlertDialogFragment.TITLE_ID, alertTitle);
        messageArgs.putString(AlertDialogFragment.MESSAGE_ID, alertBody);

        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.setArguments(messageArgs);
        dialog.show(getFragmentManager(), "error_dialog");
    }
}
