package lk.dharanimart.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import lk.dharanimart.mobile.Adaptors.CategoryListAtaptor;
import lk.dharanimart.mobile.Responses.Category;
import lk.dharanimart.mobile.Responses.CategoryResponse;
import lk.dharanimart.mobile.Responses.SuccessResponse;

public class Home extends AppCompatActivity {

    public String siteUrl;
    List<Category> categoryList;
    public CategoryListAtaptor categoryListAdaptor;
    GridView gridView;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedPreferences = getSharedPreferences("DharanimartApp",MODE_PRIVATE);

        siteUrl = getResources().getString(R.string.siteurl);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        gridView = findViewById(R.id.gridView);

        LoadCategories loadCategories = new LoadCategories();
        loadCategories.execute();
    }

    public class LoadCategories extends AsyncTask<String, Void, List<Category>> {

        @Override
        protected List<Category> doInBackground(String... strings) {
            try {
                String apiUrl = "https://www.dharanimart.lk/APP/API";

                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    urlConnection.setRequestMethod("POST");
                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream in = urlConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String line;

                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }

                        Log.d("MY_LOG",response.toString());
                        Gson gson = new Gson();
//                        Type categoryListType = new TypeToken<SuccessResponse<Category>>() {}.getType();
//                        SuccessResponse<Category> categories = gson.fromJson(response.toString(), categoryListType);

                        CategoryResponse categories = gson.fromJson(response.toString(), CategoryResponse.class);
                        SharedPreferences.Editor sd = sharedPreferences.edit();
                        sd.putString("CAT_STRING", response.toString());
                        sd.apply();
                        Log.d("MY_LOG",categories.getData().get(0).getName());

                        return categories.getData();
                    } else {
                        Log.e("LoadCategories", "HTTP response code: " + responseCode);
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Category> categories) {
            if (categories != null) {
                categoryList = categories;

                categoryListAdaptor = new CategoryListAtaptor(Home.this, categories);
                gridView.setAdapter(categoryListAdaptor);
                Log.d("MY_TAG", categories.get(0).getName());

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MyJourneyNewFragment routeRequestViewFragment = new MyRouteRequestsFragment();
//                Bundle args = new Bundle();
//                args.putString("route_request",gson.toJson(myJourneyList.get(position)));
//                args.putString("notification_description","");
//
//                routeRequestViewFragment.setArguments(args);
//
//                FragmentTransaction transaction = requireFragmentManager().beginTransaction();
//                transaction.replace(R.id.passengerHomeFrame, routeRequestViewFragment);
//                transaction.addToBackStack(null);
//                transaction.commit();
                    }
                });
            }
            }
        }
    }
