package com.taxi_test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import com.nas.cruzer.R;
import com.taxi_test.util.JSONParser;
import com.taxi_test.util.UserInfo;
import com.taxi_test.util.Util;

public class UserRequestActivity extends Activity{

	ListView requestList;
	Context con;
	JSONParser jparser=new JSONParser();
	public static final String getRidesUrl = "http://chakron.com/demo/cruzer/user-rides-list.php";
	ArrayList<HashMap<String, String>> rides;
	Integer selected_opt;
	Spinner categoryFilter;
	String dropdownItems[]={"All","Pending Requests","Accepted Requests","Completed Rides","Cancelled"};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_request_layout);

		con=UserRequestActivity.this;
		categoryFilter=(Spinner) findViewById(R.id.userRequestListFilter);
		categoryFilter.setAdapter(new ArrayAdapter<String>(this	, android.R.layout.simple_dropdown_item_1line, dropdownItems));
		
		requestList=(ListView) findViewById(R.id.userRequestList);
		selected_opt = categoryFilter.getSelectedItemPosition();
		new GetRequestedRides().execute();
		
		requestList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int i,
					long arg3) {
				UserRequestDetailsActivity.request=rides.get(i);
				startActivityForResult(new Intent(con, UserRequestDetailsActivity.class), 101);
			}
		});

		
		categoryFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int i, long arg3) {
				selected_opt = categoryFilter.getSelectedItemPosition();
				new GetRequestedRides().execute();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
	        switch (item.getItemId()) {
	        case android.R.id.home: 
	           finish();
	            return true;
	        }

	    return super.onOptionsItemSelected(item);
	}
	
	
	class GetRequestedRides extends AsyncTask<String, String, String>{
		ProgressDialog pDialog;
		String s="";
		int success=-1;
		int error=0;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog=new ProgressDialog(con);
			pDialog.setMessage("Getting data. Please wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... st) {

			List<NameValuePair> params=new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("user_id", UserInfo.getId()));

			try {
				JSONObject jobj=jparser.makeHttpRequest(getRidesUrl, "POST", params);
				success=jobj.getInt("success");
				s=jobj.getString("message");
				if(success==1){
					rides=new ArrayList<HashMap<String,String>>();
					JSONArray jAr=jobj.getJSONArray("ridelist");
					for (int i = 0; i < jAr.length(); i++) {
						JSONObject job=jAr.getJSONObject(i);
						HashMap<String , String> map=new HashMap<String, String>();
						map.put("id", job.getString("id"));
						map.put("driver_name", job.getString("driver_name"));
						map.put("driver_email", job.getString("driver_email"));
						map.put("sender_id", job.getString("sender_id"));
						map.put("name", job.getString("name"));
						map.put("phone", job.getString("phone"));
						map.put("droplocation", job.getString("droplocation"));
						map.put("location", job.getString("location"));
						map.put("latitude", job.getString("latitude"));
						map.put("longitude", job.getString("longitude"));
						map.put("timedate", job.getString("timedate"));
						map.put("accept", job.getString("accept"));
						
						if(selected_opt==0)
						rides.add(map);
						else if(selected_opt==1){
							if(job.getString("accept").equals("0"))
								rides.add(map);
						}else if(selected_opt==2){
							if(job.getString("accept").equals("1"))
								rides.add(map);
						}else if(selected_opt==3){
							if(job.getString("accept").equals("2"))
								rides.add(map);
						}else if(selected_opt==4){
							if(job.getString("accept").equals("3"))
								rides.add(map);
						}
					} 
				}

			}catch(Exception e){
				error=1;
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			pDialog.dismiss();


			if(error==1){
				if(Util.isConnectingToInternet(con)){
					Toast.makeText(con, "Server is down, Please try again later", Toast.LENGTH_SHORT).show();
				}else
					Util.showNoInternetDialog(con);
				return;
			}

			if(success==1){
				ListAdapter adapter=new ListAdapter();
				requestList.setAdapter(adapter);
			}else{
				Toast.makeText(con, s, Toast.LENGTH_SHORT).show();
			}
		}
	}

	class ListAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return rides.size();
		}

		@Override
		public Object getItem(int i) {
			return rides.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View rowView= inflater.inflate(R.layout.request_ride_single_view, parent, false);
			TextView name=(TextView) rowView.findViewById(R.id.driverNameTv);
			TextView from=(TextView) rowView.findViewById(R.id.driverFromTv);
			TextView destination=(TextView) rowView.findViewById(R.id.driverDestinationTv);
			TextView timedate=(TextView) rowView.findViewById(R.id.drivertimeDate);
			TextView accept=(TextView) rowView.findViewById(R.id.driverAccept);

			name.setText("Driver Name: "+rides.get(i).get("driver_name").trim());
			from.setText("Pickup Location : "+rides.get(i).get("location").trim());
			timedate.setText("Time : " +rides.get(i).get("timedate"));
			destination.setText("Drop Location: "+rides.get(i).get("droplocation").trim());


			if(rides.get(i).get("accept").equals("0")){
				accept.setText("Not accepted yet");
				accept.setTextColor(Color.parseColor("#AD1400"));
			}else if(rides.get(i).get("accept").equals("1")){
				accept.setText("Accepted");
				accept.setTextColor(Color.parseColor("#5AB83B"));
			}else if(rides.get(i).get("accept").equals("2")){
				accept.setText("Completed");
			}else if(rides.get(i).get("accept").equals("3")){
				accept.setText("Cancelled");
			}

			return rowView;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==101 && resultCode==RESULT_OK)
            selected_opt = categoryFilter.getSelectedItemPosition();
			new GetRequestedRides().execute();
	}
}
