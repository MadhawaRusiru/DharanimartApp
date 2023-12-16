package lk.dharanimart.mobile.Adaptors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

        Category category = categoryList.get(position);
        categoryTitle.setText(category.getName());

        LoadPicture loadPicture = new LoadPicture(catIcon);
        loadPicture.execute(category.getIcon(), SiteUrl);

        return convertView;
    }

    private static class LoadPicture extends AsyncTask<String, Void, Bitmap> {

        private final WeakReference<ImageView> imageViewReference;

        LoadPicture(ImageView imageView) {
            imageViewReference = new WeakReference<>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                InputStream inputStream = new URL(strings[1] + "/img/cat_img/" + strings[0]).openStream();
                return BitmapFactory.decodeStream(inputStream);
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
                if (imageView != null) {
                    imageView.setImageBitmap(imageBitmap);
                }
            }
        }
    }
}
