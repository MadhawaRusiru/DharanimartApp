package lk.dharanimart.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import lk.dharanimart.mobile.Adaptors.CategoryListAtaptor;
import lk.dharanimart.mobile.Adaptors.ProductListAdaptor;
import lk.dharanimart.mobile.Responses.Category;
import lk.dharanimart.mobile.Responses.CategoryResponse;
import lk.dharanimart.mobile.Responses.Product;
import lk.dharanimart.mobile.Responses.SuccessResponse;

public class CategoryView extends AppCompatActivity {
    private Category category;
    TextView tvCategoryTitle;
    ImageView imgCateogryIcon;
    EditText etvSearchKey;
    ProgressBar progressBar;
    String SiteUrl;
    static ProductListAdaptor productListAdaptor;
    GridView gridView;

    public int selectedCat = 0;
    public int selectedSubCat = 0;
    public int selectedLowerSubCat = 0;
    public String searchKey = "";

    static List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        String catJson = getIntent().getStringExtra("CATEGORY");
        category = new Gson().fromJson(catJson, Category.class);

        SiteUrl = getResources().getString(R.string.siteurl);
        gridView = findViewById(R.id.lvCategoryView);

        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        imgCateogryIcon = findViewById(R.id.imgCategoryIconView);
        progressBar = findViewById(R.id.pbLoadCat);

        etvSearchKey = findViewById(R.id.tvCatSearch);

        Spinner spnSubCat = findViewById(R.id.spnSubCat);
        Spinner spnLowerSubCat = findViewById(R.id.spnLowerSubCat);

        if(category != null){

            selectedCat = category.getId();
            tvCategoryTitle.setText(category.getName());

            LoadPicture loadPicture = new LoadPicture(imgCateogryIcon,progressBar);
            loadPicture.execute(category.getIcon(), SiteUrl);
        }

        String[] subCatArray = new String[category.getSubcat().size()];
        for (int i = 0; i < category.getSubcat().size(); i++) {
            subCatArray[i] = category.getSubcat().get(i).getName();
        }

        ArrayAdapter<String> startAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, subCatArray);
        spnSubCat.setAdapter(startAdapter);
        spnSubCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubCat = category.getSubcat().get(position).getId();
                selectedLowerSubCat = 0;
                String[] lowerSsubCatArray = new String[category.getSubcat().get(position).getLower().size()];
                for (int i = 0; i < category.getSubcat().get(position).getLower().size(); i++) {
                    lowerSsubCatArray[i] = category.getSubcat().get(position).getLower().get(i).getName();
                }
                ArrayAdapter<String> endAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, lowerSsubCatArray);
                spnLowerSubCat.setAdapter(endAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnLowerSubCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLowerSubCat = category.getSubcat().get(spnSubCat.getSelectedItemPosition()).getLower().get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button brnFilter = findViewById(R.id.brnFilter);
        brnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchKey = etvSearchKey.getText().toString();
                LoadProducts loadProducts = new LoadProducts();
                loadProducts.execute();
            }
        });

        LoadProducts loadProducts = new LoadProducts();
        loadProducts.execute();
    }

    public class LoadProducts extends AsyncTask<String, Void, List<Product>> {

        @Override
        protected List<Product> doInBackground(String... strings) {
            try {
                String apiUrl = "https://www.dharanimart.lk/APP/API/?S=GET_PRODUCTS&CATEGORY=" + category.getId() + "&SUB_CATEGORY=" + selectedSubCat + "&LOWER_SUB_CATEGORY=" + selectedLowerSubCat + "&SEARCH=" + searchKey;

                Log.d("MY_TAG",apiUrl);

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
                        Type productListType = new TypeToken<SuccessResponse<Product>>() {}.getType();
                        SuccessResponse<Product> products = gson.fromJson(response.toString(), productListType);


                        return products.getData();
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
        protected void onPostExecute(List<Product> products) {
            if (products != null) {
                if(products.size() > 0){
                    gridView.setVisibility(View.VISIBLE);
                    productList = products;
                    productListAdaptor = new ProductListAdaptor(CategoryView.this, products);
                    gridView.setAdapter(productListAdaptor);
                    Log.d("MY_TAG", products.get(0).getTitle());

                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent productView = new Intent(getApplicationContext(), ProductView.class);
                            productView.putExtra("PRODUCT", new Gson().toJson(products.get(position)));
                            startActivity(productView);
                        }
                    });
                }else{
                    gridView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    public static class LoadPicture extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<ProgressBar> progressBarReferance;
        private static LruCache<String, Bitmap> memoryCache;

        static {
            // Initialize the cache with 1/8 of the available memory
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 8;
            memoryCache = new LruCache<>(cacheSize);
        }

        LoadPicture(ImageView imageView, ProgressBar progressBar) {
            imageViewReference = new WeakReference<>(imageView);
            progressBarReferance = new  WeakReference<>(progressBar);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String imageUrl = strings[1] + "/img/cat_img/" + strings[0];

            // Check if the image is available in the memory cache
            Bitmap cachedBitmap = memoryCache.get(imageUrl);
            if (cachedBitmap != null) {
                return cachedBitmap;
            }

            try {
                InputStream inputStream = new URL(imageUrl).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Cache the bitmap in memory
                if (bitmap != null) {
                    memoryCache.put(imageUrl, bitmap);
                }

                return bitmap;
            } catch (IOException e) {
                Log.e("CAT_ICON", "Error while loading. Icon: " + strings[0]);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap imageBitmap) {
            // Update the UI with the loaded image
            if (imageBitmap != null) {
                ImageView imageView = imageViewReference.get();
                ProgressBar progressBar = progressBarReferance.get();
                if (imageView != null) {
                    imageView.setImageBitmap(imageBitmap);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}