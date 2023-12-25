package lk.dharanimart.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;

import lk.dharanimart.mobile.Responses.Product;

public class ProductView extends AppCompatActivity {

    public Product product;
    public ImageView mainImageView;
    public LinearLayout lnrImageContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        String productString = getIntent().getStringExtra("PRODUCT");

        Gson gson = new Gson();
        product = gson.fromJson(productString, Product.class);

        lnrImageContainer = findViewById(R.id.lnrProductViewImageContainer);
        mainImageView = findViewById(R.id.imgProductViewMainImageView);

        String[] imgArray = new String[product.getImages().size()];
        for (int i = 0; i < product.getImages().size(); i++) {
            imgArray[i] = product.getImages().get(i);
        }

        for (String imageUrl : imgArray) {
            ImageView imageView = new ImageView(this);
            new ImageDownloadTask(imageView, mainImageView).execute(imageUrl);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            lnrImageContainer.addView(imageView);
        }

    }

    private static class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<ImageView> mainImageViewReference;

        ImageDownloadTask(ImageView imageView, ImageView mainImageView) {
            imageViewReference = new WeakReference<>(imageView);
            mainImageViewReference = new WeakReference<>(mainImageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL imageUrl = new URL(params[0]);
                return BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = imageViewReference.get();
            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView mainImageView = mainImageViewReference.get();
                        if (mainImageView != null) {
                            mainImageView.setImageBitmap(bitmap);
                        }
                    }
                });
            }
        }
    }
}