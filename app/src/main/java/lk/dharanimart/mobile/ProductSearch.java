package lk.dharanimart.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import lk.dharanimart.mobile.Adaptors.ProductListAdaptor;
import lk.dharanimart.mobile.Responses.Category;
import lk.dharanimart.mobile.Responses.CategoryResponse;
import lk.dharanimart.mobile.Responses.Product;
import lk.dharanimart.mobile.Responses.SuccessResponse;

public class ProductSearch extends AppCompatActivity {

    private List<Category> categories;
    private Category category;
    TextView tvCategoryTitle;
    ImageView imgCateogryIcon;
    EditText etvSearchKey;
    ProgressBar progressBar;
    String SiteUrl;
    static ProductListAdaptor productListAdaptor;
    public GridView gridView;
    SharedPreferences sharedPreferences;

    public int selectedCat = 0;
    public int selectedSubCat = 0;
    public int selectedLowerSubCat = 0;
    public String searchKey = "";

    static List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_search);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        sharedPreferences = getSharedPreferences("DharanimartApp",MODE_PRIVATE);

        SiteUrl = getResources().getString(R.string.siteurl);

        String catJson = sharedPreferences.getString("CAT_STRING",null);
        if(catJson != null ){
            Gson gson = new Gson();
            CategoryResponse categoriesResponse = gson.fromJson(catJson, CategoryResponse.class);
            categories = categoriesResponse.getData();
        }
        Log.d("X",categories.get(0).getName());


        gridView = findViewById(R.id.lvSearchCategoryView);

        etvSearchKey = findViewById(R.id.tvCatSearch);
        etvSearchKey.setText(getIntent().getStringExtra("SEARCH_KEY"));
        searchKey = etvSearchKey.getText().toString();

        Spinner spnCategory = findViewById(R.id.spnCategory);
        Spinner spnSubCat = findViewById(R.id.spnSubCat);
        Spinner spnLowerSubCat = findViewById(R.id.spnSearchLowerSubCat);

        Category allCat = new Category(0,"All");
        categories.add(0, allCat);

        String[] catArray = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            catArray[i] = categories.get(i).getName();
        }


        ArrayAdapter<String> catAdaptor = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.custom_spinner_item,
                catArray
        );

        catAdaptor.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

        spnCategory.setAdapter(catAdaptor);

        spnCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCat = categories.get(position).getId();
                selectedSubCat = 0;
                selectedLowerSubCat = 0;
                category = categories.get(position);
                String[] subCatArray;
                if(categories.get(position).getSubcat() != null) {
                    subCatArray = new String[categories.get(position).getSubcat().size()];
                    for (int i = 0; i < categories.get(position).getSubcat().size(); i++) {
                        subCatArray[i] = categories.get(position).getSubcat().get(i).getName();
                    }

                    ArrayAdapter<String> startAdapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            R.layout.custom_spinner_item,
                            subCatArray
                    );
                    startAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

                    spnSubCat.setAdapter(startAdapter);
                }else{
                    subCatArray = new String[]{"All"};

                    ArrayAdapter<String> startAdapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            R.layout.custom_spinner_item,
                            subCatArray
                    );
                    startAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

                    spnSubCat.setAdapter(startAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spnSubCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(category.getSubcat() != null){
                    selectedSubCat = category.getSubcat().get(position).getId();
                    selectedLowerSubCat = 0;
                    String[] lowerSsubCatArray = new String[category.getSubcat().get(position).getLower().size()];
                    for (int i = 0; i < category.getSubcat().get(position).getLower().size(); i++) {
                        lowerSsubCatArray[i] = category.getSubcat().get(position).getLower().get(i).getName();
                    }

                    ArrayAdapter<String> endAdapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            R.layout.custom_spinner_item,
                            lowerSsubCatArray
                    );
                    endAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
                    spnLowerSubCat.setAdapter(endAdapter);
                }else{
                    selectedSubCat = 0;
                    selectedLowerSubCat = 0;
                    String[] lowerSsubCatArray = new String[]{"All"};
                    ArrayAdapter<String> endAdapter = new ArrayAdapter<String>(
                            getApplicationContext(),
                            R.layout.custom_spinner_item,
                            lowerSsubCatArray
                    );
                    endAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
                    spnLowerSubCat.setAdapter(endAdapter);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnLowerSubCat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(category.getSubcat() != null){
                    selectedLowerSubCat = category.getSubcat().get(spnSubCat.getSelectedItemPosition()).getLower().get(position).getId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button brnFilter = findViewById(R.id.brnSearchSearch);
        brnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchKey = etvSearchKey.getText().toString();
                LoadProducts loadProducts = new LoadProducts();
                loadProducts.execute();
            }
        });

        selectedCat = 0;
        selectedSubCat = 0;
        selectedLowerSubCat = 0;
        searchKey = "";

        LoadProducts loadProducts = new LoadProducts();
        loadProducts.execute();

    }
    public class LoadProducts extends AsyncTask<String, Void, List<Product>> {

        @Override
        protected List<Product> doInBackground(String... strings) {
            try {
                String apiUrl = "https://www.dharanimart.lk/APP/API/?S=GET_PRODUCTS&CATEGORY=" + selectedCat + "&SUB_CATEGORY=" + selectedSubCat + "&LOWER_SUB_CATEGORY=" + selectedLowerSubCat + "&SEARCH=" + searchKey;

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
                    productListAdaptor = new ProductListAdaptor(ProductSearch.this, products);
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


}

