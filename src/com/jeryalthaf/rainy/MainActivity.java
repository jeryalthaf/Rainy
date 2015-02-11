package com.jeryalthaf.rainy;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class MainActivity extends Activity {

	public static final String TAG = MainActivity.class.getSimpleName();
	private CurrentWeather mCurrentWeather;

	@InjectView(R.id.timeLabel)
	TextView mTimeLabel;
	@InjectView(R.id.temperatureLabel)
	TextView mTemperatureLabel;
	@InjectView(R.id.humidityValue)
	TextView mHumidityValue;
	@InjectView(R.id.precipValue)
	TextView mPrecipValue;
	@InjectView(R.id.summaryLabel)
	TextView mSummaryLabel;
	@InjectView(R.id.iconImageView)
	ImageView mIconImageView;
	@InjectView(R.id.refreshImageView)
	ImageView mRefreshImageView;
	@InjectView(R.id.progressBar)
	ProgressBar mProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.inject(this);

		mProgressBar.setVisibility(View.INVISIBLE);

		final double latitude = 37.8267;
		final double longitude = -122.423;

		mRefreshImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getForecast(latitude, longitude);
			}
		});

	}

	private void getForecast(double latitude, double longitude) {
		String apiKey = "27ea13b7fa08cfffa30999625fc65169";
		String forecastUrl = "https://api.forecast.io/forecast/" + apiKey + "/"
				+ latitude + "," + longitude;

		if (isNetworkAvailable()) {

			toggleRefresh();

			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder().url(forecastUrl).build();

			Call call = client.newCall(request);
			call.enqueue(new Callback() {

				@Override
				public void onResponse(Response response) throws IOException {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							toggleRefresh();
						}
					});
					String jsonData = response.body().string();
					try {
						Log.v(TAG, jsonData);

						if (response.isSuccessful()) {
							mCurrentWeather = getCurrentDetails(jsonData);
							runOnUiThread(new Runnable() {

								@Override
								public void run() {
									updateDisplay();
								}
							});

						} 
						else {
							alertUserAboutError();
						}
					}
					
					catch (JSONException e) {
						Log.e(TAG, "Exception caught:", e);
					}
					

				}

				@Override
				public void onFailure(Request response, IOException e) {
					// TODO Auto-generated method stub
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							toggleRefresh();
						}
					});
					alertUserAboutError();
				}
			});

		} else {
			Toast.makeText(this,
					getString(R.string.network_unavailable_message),
					Toast.LENGTH_LONG).show();
		}
	}

	private void toggleRefresh() {
		if (mProgressBar.getVisibility() == View.INVISIBLE) {
			mProgressBar.setVisibility(View.VISIBLE);
			mRefreshImageView.setVisibility(View.INVISIBLE);
		} else {
			mProgressBar.setVisibility(View.INVISIBLE);
			mRefreshImageView.setVisibility(View.VISIBLE);
		}
	}

	protected void updateDisplay() {

		mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
		mPrecipValue.setText("At" + mCurrentWeather.getFormattedTime()
				+ "it will be");
		mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
		mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
		mSummaryLabel.setText(mCurrentWeather.getSummary());

		Drawable drawable = getResources().getDrawable(
				mCurrentWeather.getIconId());
		mIconImageView.setImageDrawable(drawable);

	}

	protected CurrentWeather getCurrentDetails(String jsonData)
			throws JSONException {
		JSONObject forecast = new JSONObject(jsonData);
		String timezone = forecast.getString("timezone");
		Log.i(TAG, "FROM JSON:" + timezone);

		JSONObject currently = forecast.getJSONObject("currently");

		CurrentWeather currentWeather = new CurrentWeather();
		currentWeather.setHumidity(currently.getDouble("humidity"));
		currentWeather.setIcon(currently.getString("icon"));
		currentWeather
				.setPrecipChance(currently.getDouble("precipProbability"));
		currentWeather.setTime(currently.getLong("time"));
		currentWeather.setSummary(currently.getString("summary"));
		currentWeather.setTemperature(currently.getDouble("temperature"));
		currentWeather.setTimezone(timezone);

		return new CurrentWeather();

	}

	private boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		boolean isAvailable = false;
		if (networkinfo != null && networkinfo.isConnected()) {
			isAvailable = true;
		}
		return isAvailable;
	}

	protected void alertUserAboutError() {
		AlertDialogFragment dialog = new AlertDialogFragment();
		dialog.show(getFragmentManager(), "error_dialog");
	}

}
