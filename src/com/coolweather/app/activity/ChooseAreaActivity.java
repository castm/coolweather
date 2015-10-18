package com.coolweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.coolweather.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.*;
import db.CoolWeatherDB;
import model.City;
import model.County;
import model.Province;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

public class ChooseAreaActivity extends Activity {

	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	
	//ʡ�б�
	private List<Province> provinceList;
	
	//���б�
	private List<City> cityList;
	
	//�h�б�
	private List<County> countyList;
	
	//�x�е�ʡ��
	private Province selectedProvince;
	
	//�x�еĳ���
	private City selectedCity;
	
	//��ǰ�x�еļ��e
	private int currentLevel;
	
	//�Ƿ��WeatherActivity����ת����
	private boolean isFromWeatherActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences sharedPreferences=PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPreferences.getBoolean("city_selected", false)&&!isFromWeatherActivity) {
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView=(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		coolWeatherDB=CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
				// TODO Auto-generated method stub
				if(currentLevel==LEVEL_PROVINCE){
					selectedProvince=provinceList.get(index);
					queryCities();
				}
				else if(currentLevel==LEVEL_CITY){
					selectedCity=cityList.get(index);
					queryCounties();
				}else if (currentLevel==LEVEL_COUNTY) {
					String countyCode=countyList.get(index).getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
		});
		queryProvinces();
	}
	
	//��ԃȫ�����е�ʡ�����ޏĔ������ԃ������]�в�ԃ���ą^�������ϲ�ԃ
	private void queryProvinces(){
		provinceList=coolWeatherDB.loadProvinces();
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceNmae());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�Ї�");
			currentLevel=LEVEL_PROVINCE;
		}
		else
		{
			queryFromServer(null,"province");
		}
	}
	
	//��ԃ�x��ʡ�����е��У����ȏĔ������ԃ������]�в�ԃ���ą^�������ϲ�ԃ��
	private void queryCities(){
		cityList=coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0){
			dataList.clear();
			for(City city:cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceNmae());
			currentLevel=LEVEL_CITY;
		}
		else {
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
	}
	
	//��ԃ�x���Ѓ����еĿh�����ȏĔ������ԃ������]�в�ԃ���ą^��������ԃ
	private void  queryCounties(){
		countyList=coolWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County county:countyList){
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
		}
		else {
			queryFromServer(selectedCity.getCityCode(),"county");
		}
	}
	
	//��������Ĵ�̖����͏ķ������ϲ�ԃʡ�пh����
	private void queryFromServer (final String code,final String type) {
		String address;
		if(!TextUtils.isEmpty(code)){
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}
		else {
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result=false;
				if("province".equals(type)){
					result=Utility.handleProvincesResponse(coolWeatherDB, response);
				}
				else if ("city".equals(type)) {
					result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}
				else if ("county".equals(type)) {
					result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
					
				}
				if(result){
					//ͨ�^runOnUiThread()�����ص�������̎��߉݋
					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}
							else if ("city".equals(type)) {
								queryCities();
							}
							else if ("county".equals(type)) {
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception exception) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
						@Override
						public void run(){
							closeProgressDialog();
							Toast.makeText(ChooseAreaActivity.this, "�H�۵ļ��dʧ����", Toast.LENGTH_LONG).show();
						}
					
				});
			}
		});
		
	}
		//�@ʾ�M��
		private void  showProgressDialog(){
			if(progressDialog==null){
				progressDialog=new ProgressDialog(this);
				progressDialog.setMessage("���ڼ��d������");
				progressDialog.setCanceledOnTouchOutside(false);
			}
			progressDialog.show();
		}
		
		//�P�]�M�Ȍ�Ԓ��
		private void closeProgressDialog(){
			if(progressDialog!=null){
				progressDialog.dismiss();
			}
		}
		
		@Override
		public void onBackPressed(){
			if(currentLevel==LEVEL_COUNTY){
				queryCities();
			}
			else if (currentLevel==LEVEL_CITY) {
				queryProvinces();
			}else {
				if(isFromWeatherActivity){
					Intent intent=new Intent(this,WeatherActivity.class);
					startActivity(intent);
				}
				finish();
			}
		}
}
