package com.coolweather.app.activity;

import org.w3c.dom.Text;

import com.example.coolweather.R;

import android.R.raw;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import android.widget.AdapterView.OnItemClickListener;

public class WeatherActivity extends Activity implements OnClickListener{

	private LinearLayout weatherInfoLayout;
	
	private TextView cityNameText;
	
	private TextView publishText;
	
	private TextView weatherDespText;
	
	private TextView temp1Text;
	
	private TextView temp2Text;
	
	private TextView currentDateText;
	
	private Button switchCity;
	
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//初始化各种控件
		weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
		publishText=(TextView)findViewById(R.id.publish_text);
		weatherDespText=(TextView)findViewById(R.id.weather_desp);
		temp1Text=(TextView)findViewById(R.id.temp1);
		temp2Text=(TextView)findViewById(R.id.temp2);
		currentDateText=(TextView)findViewById(R.id.current_date);
		switchCity=(Button)findViewById(R.id.switch_city);
		refreshWeather=(Button)findViewById(R.id.refresh_weather);
		String countyCode=getIntent().getStringExtra("county_code");
		
		if(!TextUtils.isEmpty(countyCode)){
			publishText.setText("数据同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else{
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}
	
	private void queryWeatherCode(String countyCode) {
		// TODO Auto-generated method stub
		String address="http://www.weather.com.cn/data/list3/city"
			+countyCode+".xml";
		queryFromServer(address,"countyCode");
	}
	
	private void queryWeatherInfo(String weatherCode) {
		// TODO Auto-generated method stub
		String address="http://www.weather.com.cn/data/cityinfo/"
		+weatherCode+".html";
		queryFromServer(address, "weatherCode");
	}

	@Override
	public void onClick(View view){
		switch(view.getId()){
		case R.id.switch_city:
			Intent intent=new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中。。。");
			SharedPreferences preferences=PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode=preferences.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)){
				queryWeatherInfo(weatherCode);
			}
			break;
			default:
				break;
		}
	}

	private void queryFromServer(final String address,final String type) {
		// TODO Auto-generated method stub
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(final String response) {
				// TODO Auto-generated method stub
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						//从服务器返回的数据中解析出天气代号
						String[] array=response.split("//|");
						if (array!=null&&array.length==2) {
							String weatherCode=array[1];
							queryWeatherInfo(weatherCode);
						}
					}
					else if ("weatherCode".equals(type)) {
						//处理服务器返回的天气信息
						Utility.handleWeatherResponse(WeatherActivity.this, response);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								showWeather();
							}
						});
					}
				}
			}
			
			@Override
			public void onError(Exception exception) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						publishText.setText("同步失败！");
					}
				});
			}
		});
	}
	
	private void showWeather() {
		// TODO Auto-generated method stub
	SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
	cityNameText.setText(sharedPreferences.getString("city_name", ""));
	temp1Text.setText(sharedPreferences.getString("temp1", ""));
	temp2Text.setText(sharedPreferences.getString("temp2", ""));
	weatherDespText.setText(sharedPreferences.getString("weather_desp", ""));
	publishText.setText("今天:"+sharedPreferences.getString("publish_time", "")+"发布");
	currentDateText.setText(sharedPreferences.getString("current_date", ""));
	weatherInfoLayout.setVisibility(View.VISIBLE);
	cityNameText.setVisibility(View.VISIBLE);
	}
}
