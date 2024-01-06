package lk.dharanimart.mobile.Adaptors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import java.util.ArrayList;
import java.util.List;

import lk.dharanimart.mobile.Home;
import lk.dharanimart.mobile.R;
import lk.dharanimart.mobile.Responses.Category;

public class CategoryListAtaptor extends BaseAdapter {

    Context context;
    List<Category> categoryList;
    LayoutInflater inflater;
    String SiteUrl;
    ImageView catIcon;
    ProgressBar progressBar;
    static int completedTaskCount = 0;
    static int currentTasksCount = 0;
    static Home homeActivity;


    public CategoryListAtaptor(Context context, List<Category> categoryList, Home home) {
        this.context = context;
        this.categoryList = categoryList;
        inflater = LayoutInflater.from(context);
        SiteUrl = context.getResources().getString(R.string.siteurl);
        homeActivity = home;
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

        currentTasksCount = categoryList.size();
        completedTaskCount++;

        catIcon.setImageDrawable(getCatIcon(category.getId()));

        if(completedTaskCount > currentTasksCount){
               homeActivity.closeLoader();
        }
        return convertView;
    }

    private Drawable getCatIcon(int id) {
        int resourceId = context.getResources().getIdentifier("cat_" + id, "drawable", context.getPackageName());
        return context.getDrawable(resourceId);
    }
}
