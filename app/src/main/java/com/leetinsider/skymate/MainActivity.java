package com.leetinsider.skymate;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    public CurrentWeather mCurrentWeather;

    @BindView(R.id.timeLabel) TextView mTimeLabel;
    @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @BindView(R.id.humidityValue) TextView mHumidityValue;
    @BindView(R.id.precipValue) TextView mPrecipValue;
    @BindView(R.id.summaryTextView) TextView mSummaryLabel;
    @BindView(R.id.iconImageView) ImageView mIconImageView;
    @BindView(R.id.refreshImageView) ImageView mRefreshImageView;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Import view variables
        ButterKnife.bind(this);

        // Set default progressbar to invisible 
        mProgressBar.setVisibility(View.INVISIBLE);


        final double latitude = 37.8267;
        final double longitude = -122.423;

        // Create onClick for the refresh button
        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecast(latitude, longitude);
            }
        });

        getForecast(latitude, longitude);


    }

    private void getForecast(double latitude, double longitude) {
        // Compose the Forecast IO url into separate variables
        String apiKey = "4d0e45683b7534f37310e7e716272643";
        String forecastUrl = "https://api.forecast.io/forecast/" + apiKey +
                "/" + latitude + "," + longitude;

        // Check if network is available using isNetworkAvailable(). True then fire GET Request
        // Else display Toast message of no network.
        if (isNetworkAvailable()) {
                // Check the status of progress bar and update refresh image
                toggleRefresh();

                // Start a new http client request to GET JSON data
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(forecastUrl)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });
                        alertUserAboutError("Failure!", "There seems to be an issue getting current weather.");
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });
                        try {
                            String jsonData = response.body().string();
                            Log.v(TAG,jsonData);
                            if (response.isSuccessful()) {
                                //If response is good, get current weather from json
                                mCurrentWeather = getCurrentDetails(jsonData);
                                // After parsing JSON data, update the views with data on UiThread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateDisplay();
                                    }
                                });
                            } else {
                                // Dialog text appear if there is an issue with connecting to the service
                                alertUserAboutError("Sorry!", "There was an error");
                            }
                        }
                        catch (IOException e) {
                            Log.e(TAG, "Exception caught: ", e);
                        }
                        catch (JSONException e) {
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

    private void toggleRefresh() {
        //Hide Refresh Button - Show Spinner
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
                mProgressBar.setVisibility(View.VISIBLE);
                mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        //Show Refresh Button - Hide Spinner
        else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary());

        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        // New JSON object named forecast that takes the jsonData as parameter
        JSONObject forecast = new JSONObject(jsonData);
        // Set variables from the JSON key value
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON object " + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);

        Log.d(TAG, currentWeather.getFormattedTime());

        return currentWeather;
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
