package lk.dharanimart.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.widget.ImageButton;
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

import lk.dharanimart.mobile.Adaptors.CategoryListAtaptor;
import lk.dharanimart.mobile.Adaptors.ProductListAdaptor;
import lk.dharanimart.mobile.Responses.Category;
import lk.dharanimart.mobile.Responses.CategoryResponse;
import lk.dharanimart.mobile.Responses.Product;
import lk.dharanimart.mobile.Responses.SuccessResponse;

public class MyShop extends AppCompatActivity {

    private List<Category> categories;
    private Category category;
    TextView tvCategoryTitle;
    ImageView imgCateogryIcon;
    ProgressBar progressBar;
    String SiteUrl;
    static ProductListAdaptor productListAdaptor;
    public GridView gridView;
    SharedPreferences sharedPreferences;
    List<Category> categoryList;

    public int selectedCat = 0;
    public int selectedSubCat = 0;
    public int selectedLowerSubCat = 0;
    public String membership = "";
    Spinner spnCategory, spnSubCat, spnLowerSubCat;

    static List<Product> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_shop);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        sharedPreferences = getSharedPreferences("DharanimartApp",MODE_PRIVATE);

        SiteUrl = getResources().getString(R.string.siteurl);



        gridView = findViewById(R.id.lvSearchCategoryView);

        membership = getIntent().getStringExtra("MEMBER");
        String memberName = getIntent().getStringExtra("MEMBER_NAME");
        LoadCategories loadCategories = new LoadCategories();
        loadCategories.execute();

        spnCategory = findViewById(R.id.spnCategory);
        spnSubCat = findViewById(R.id.spnSubCat);
        spnLowerSubCat = findViewById(R.id.spnSearchLowerSubCat);

        TextView tvMyShopTitle = findViewById(R.id.tvMyShopTitle);

        tvMyShopTitle.setText(memberName);

        ImageButton btnShareFb = findViewById(R.id.btnMyShopShareFb);
        ImageButton btnShareWa = findViewById(R.id.btnMyShopShareWa);
        ImageButton btnShareMsg = findViewById(R.id.btnMyShopShareMsg);
        ImageButton btnShareIns = findViewById(R.id.btnMyShopShareIns);

        String shopLink = "https://www.dharanimart.lk/index.php?page=memView&member="+ membership +"&s=" + memberName;

        btnShareFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOnFacebook(shopLink);
            }
        });
        btnShareWa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOnWhatsApp(shopLink);
            }
        });
        btnShareMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOnMessenger(shopLink);
            }
        });
        btnShareIns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareOnInstagram(shopLink, "Dharanimart.lk");
            }
        });

        Button brnFilter = findViewById(R.id.brnSearchSearch);
        brnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LoadProducts loadProducts = new LoadProducts();
                loadProducts.execute();
            }
        });

        selectedCat = 0;
        selectedSubCat = 0;
        selectedLowerSubCat = 0;

        LoadProducts loadProducts = new LoadProducts();
        loadProducts.execute();

    }
    private void shareOnFacebook(String shopLink) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shopLink);

        // Ensure Facebook is installed before launching the intent
        if (isPackageInstalled("com.facebook.katana")) {
            intent.setPackage("com.facebook.katana");
        }

        startActivity(intent);
    }

    private void shareOnWhatsApp(String shopLink) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shopLink);

        // Ensure WhatsApp is installed before launching the intent
        if (isPackageInstalled("com.whatsapp")) {
            intent.setPackage("com.whatsapp");
        }

        startActivity(intent);
    }

    private void shareOnMessenger(String shopLink) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shopLink);

        // Ensure Messenger is installed before launching the intent
        if (isPackageInstalled("com.facebook.orca")) {
            intent.setPackage("com.facebook.orca");
        }

        startActivity(intent);
    }

    private void shareOnInstagram(String shopLink, String caption) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_TEXT, shopLink);

        // Check if Instagram is installed
        if (isPackageInstalled("com.instagram.android")) {
            intent.setPackage("com.instagram.android");
        }

        startActivity(Intent.createChooser(intent, "Share to"));
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public class LoadProducts extends AsyncTask<String, Void, List<Product>> {

        @Override
        protected List<Product> doInBackground(String... strings) {
            try {
                String apiUrl = "https://www.dharanimart.lk/APP/API/?S=GET_PRODUCTS&CATEGORY=" + selectedCat + "&SUB_CATEGORY=" + selectedSubCat + "&LOWER_SUB_CATEGORY=" + selectedLowerSubCat + "&MEMBERSHIP=" + membership;

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
                    productListAdaptor = new ProductListAdaptor(MyShop.this, products);
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
    public class LoadCategories extends AsyncTask<String, Void, List<Category>> {

        @Override
        protected List<Category> doInBackground(String... strings) {
            try {
                String apiUrl = "https://www.dharanimart.lk/APP/API/?S=GET_CATEGORIES_OF_MEM&MEMBERSHIP="+ membership;

                Log.d("MY_LOG",apiUrl);

                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    urlConnection.setRequestMethod("GET");
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

                        CategoryResponse categories = gson.fromJson(response.toString(), CategoryResponse.class);
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

            }
        }
    }
}