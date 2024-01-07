package lk.dharanimart.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import lk.dharanimart.mobile.Adaptors.ProductListAdaptor;
import lk.dharanimart.mobile.Responses.Product;
import lk.dharanimart.mobile.Responses.SuccessResponse;

public class MainActivity extends AppCompatActivity {

    private static Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        dialog = new Dialog(this, android.R.style.Animation_Activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.setContentView(R.layout.loading_screen);

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            String scheme = data.getScheme();
            String host = data.getHost();
            String path = data.getPath();
            String query = data.getQuery();

            if (("http".equals(scheme) || "https".equals(scheme)) &&
                    ("www.dharanimart.lk".equals(host) || "dharanimart.lk".equals(host))) {

                // Handle the deep link based on path and query
                if ("/index.php".equals(path) && query != null && query.contains("page=memView")) {
                    // Handle the first type of deep link (Member activity)
                    String memberId = getQueryParameter(query, "member");
                    String memberName = getQueryParameter(query, "s");

                    if (memberId != null && memberName != null) {
                        openMemberActivity(memberId, memberName);
                        return;
                    }
                } else if ("/shortLink.php".equals(path)) {
                    // Handle the second type of deep link (Product view activity)
                    String productId = getQueryParameter(query, "id");

                    if (productId != null) {
                        openProductViewActivity(productId);
                        return;
                    }
                }

                // Default action: Open the Home activity
                openHomeActivity();
                return;
            }
        }


        // Default action: Open the Home activity
        openHomeActivity();
    }

    private String getQueryParameter(String query, String key) {
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && key.equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        return null;
    }

    private void openMemberActivity(String memberId, String memberName) {
        Intent intent = new Intent(getApplicationContext(), MyShop.class);
        intent.putExtra("MEMBER",memberId);
        intent.putExtra("MEMBER_NAME", memberName);
        startActivity(intent);
        finish();
    }

    private void openProductViewActivity(String productId) {
        Toast.makeText(this, productId, Toast.LENGTH_SHORT).show();
        LoadProducts loadProducts = new LoadProducts();
        loadProducts.execute(productId);
    }

    private void openHomeActivity() {
         Intent homeIntent = new Intent(this, Home.class);
         startActivity(homeIntent);
         finish();
    }

    public class LoadProducts extends AsyncTask<String, Void, List<Product>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected List<Product> doInBackground(String... strings) {
            try {
                String apiUrl = "https://www.dharanimart.lk/APP/API/?S=GET_PRODUCT&PROID=" + strings[0];

//                Log.d("MY_TAG",apiUrl);

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

//                        Log.d("MY_LOG",response.toString());
                        Gson gson = new Gson();
                        Type productListType = new TypeToken<SuccessResponse<Product>>() {}.getType();
                        SuccessResponse<Product> products = gson.fromJson(response.toString(), productListType);


                        return products.getData();
                    } else {
//                        Log.e("LoadCategories", "HTTP response code: " + responseCode);
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
        protected void onPostExecute(List<Product> products) {
            if (products != null) {
                if(products.size() > 0){
                    Intent productView = new Intent(getApplicationContext(), ProductView.class);
                    productView.putExtra("PRODUCT", new Gson().toJson(products.get(0)));
                    startActivity(productView);
                    dialog.dismiss();
                    finish();
                }else{
                    Toast.makeText(MainActivity.this, "Sorry, requested product was not exists.", Toast.LENGTH_SHORT).show();
                    Intent home = new Intent(getApplicationContext(), Home.class);
                    startActivity(home);
                }
            }else{
                Toast.makeText(MainActivity.this, "Sorry, requested product was not exists.", Toast.LENGTH_SHORT).show();
                Intent home = new Intent(getApplicationContext(), Home.class);
                startActivity(home);
            }
        }
    }
}


