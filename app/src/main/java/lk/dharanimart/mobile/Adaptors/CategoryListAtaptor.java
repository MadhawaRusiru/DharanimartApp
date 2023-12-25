package lk.dharanimart.mobile.Adaptors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;

import lk.dharanimart.mobile.R;
import lk.dharanimart.mobile.Responses.Category;

public class CategoryListAtaptor extends BaseAdapter {

    Context context;
    List<Category> categoryList;
    LayoutInflater inflater;
    String SiteUrl;
    ImageView catIcon;
    ProgressBar progressBar;

    public CategoryListAtaptor(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
        inflater = LayoutInflater.from(context);
        SiteUrl = context.getResources().getString(R.string.siteurl);
    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    @Override
    public Object getItem(int position) {
        return categoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return categoryList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.cat_grid_item, null);
        catIcon = convertView.findViewById(R.id.imgCategoryIcon);
        TextView categoryTitle = convertView.findViewById(R.id.txtCategoryTitle);
        progressBar = convertView.findViewById(R.id.progressBar2);

        Category category = categoryList.get(position);
        categoryTitle.setText(category.getName());

        LoadPicture loadPicture = new LoadPicture(catIcon,progressBar);
        loadPicture.execute(category.getIcon(), SiteUrl);

        return convertView;
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
            String imageUrl = strings[1] + "/img/cat_app_img/" + strings[0];

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
