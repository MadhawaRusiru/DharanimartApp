package lk.dharanimart.mobile.Adaptors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lk.dharanimart.mobile.R;
import lk.dharanimart.mobile.Responses.Category;
import lk.dharanimart.mobile.Responses.Product;

public class ProductListAdaptor extends BaseAdapter {

    static Context context;
    List<Product> productList;
    LayoutInflater inflater;
    static String SiteUrl;
    ImageView productImg;
    ProgressBar progressBar;

    private List<LoadPicture> downloadTasks;

    public ProductListAdaptor(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        inflater = LayoutInflater.from(context);
        SiteUrl = context.getResources().getString(R.string.siteurl);
        downloadTasks = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return productList.size();
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return productList.get(position).getPro_id();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.product_grid_item, null);

        productImg = convertView.findViewById(R.id.imgProductImage);

        TextView productTitle = convertView.findViewById(R.id.tvProductTitle);
        TextView productPrice = convertView.findViewById(R.id.tvProductPrice);
        TextView productRealPrice = convertView.findViewById(R.id.tvProductRealPrice);
        TextView productDescount = convertView.findViewById(R.id.tvProductTileDiscount);
        ImageView imgProductTileDiscount = convertView.findViewById(R.id.imgProductTileDiscount);

        progressBar = convertView.findViewById(R.id.progressBar3);

        Product product = productList.get(position);

        productTitle.setText(product.getTitle());

        if (product.getDiscount() > 0 && product.getPrice() > 0) {
            productDescount.setText(String.valueOf(product.getDiscount()) + "%");

            int discount = (int) product.getPrice() * product.getDiscount() / 100;
            int discountedPrice = Math.round(product.getPrice() - discount);

//            Log.d("MY_TAG", String.valueOf(discountedPrice));

            String originalText = "Rs." + String.valueOf(product.getPrice()) + "/-";
            Spannable spannable = new SpannableString(originalText);
            spannable.setSpan(new StrikethroughSpan(), 0, originalText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            productPrice.setText("Rs." + String.valueOf(discountedPrice) + "/-");
            productRealPrice.setText(spannable);
        } else if (product.getPrice() > 0) {
            productDescount.setVisibility(View.INVISIBLE);
            productRealPrice.setVisibility(View.INVISIBLE);
            imgProductTileDiscount.setVisibility(View.INVISIBLE);
            productPrice.setText("Rs." + String.valueOf(product.getPrice()) + "/-");
        } else {
            productDescount.setVisibility(View.INVISIBLE);
            productRealPrice.setVisibility(View.INVISIBLE);
            imgProductTileDiscount.setVisibility(View.INVISIBLE);
            productPrice.setText("");
        }


        LoadPicture loadPicture = new LoadPicture(productImg,progressBar);
        loadPicture.execute(product.getImages().get(0), SiteUrl);
        downloadTasks.add(loadPicture);
        return convertView;
    }
    public void cancelAllTasks() {
        for (LoadPicture task : downloadTasks) {
            task.cancel(true);
        }
        downloadTasks.clear();
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
            String imageUrl = strings[0];

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
//                Log.e("CAT_ICON", "Error while loading. Icon: " + strings[0]);
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
