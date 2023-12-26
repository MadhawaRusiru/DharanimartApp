package lk.dharanimart.mobile;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import lk.dharanimart.mobile.Responses.Product;

public class ProductView extends AppCompatActivity {

    public Product product;
    private static ImageView mainImageView;
    private Matrix matrix = new Matrix();
    private PointF lastTouchPoint = new PointF();
    private ValueCallback<Uri[]> uploadMessage;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;


    WebView wbDescription, wbComments;

    ArrayList<ImageDownloadTask> imageDownloadTasks;

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

        LinearLayout lnrImageContainer = findViewById(R.id.lnrProductViewImageContainer);
        mainImageView = findViewById(R.id.imgProductViewMainImageView);
        wbDescription = findViewById(R.id.wbProductViewDescription);
        wbComments  = findViewById(R.id.wbProductViewComments);

        WebSettings webSettings = wbDescription.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.78 Mobile Safari/537.36");
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

        WebSettings webSettings2 = wbComments.getSettings();
        webSettings2.setJavaScriptEnabled(true);
        webSettings2.setUserAgentString("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.78 Mobile Safari/537.36");
        webSettings2.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);


        webSettings2.setAllowFileAccess(true);
        webSettings2.setAllowContentAccess(true);
        wbComments.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                uploadMessage = filePathCallback;
                openFileChooser();
                return true;
            }
        });

        wbComments.loadUrl("https://www.dharanimart.lk/comments.php?ref=" + product.getPro_id());

        double price = product.getPrice();
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        numberFormat.setMaximumFractionDigits(2);
        String formattedPrice = "Rs. " + numberFormat.format(price) + "/-";

        TextView tvPrice = findViewById(R.id.tvProductViewPrice);
        TextView tvTitle = findViewById(R.id.tvProductViewTitle);
        TextView tvProductNo = findViewById(R.id.tvProductViewProNum);
        TextView tvCategory = findViewById(R.id.tvProductViewCategory);
        TextView tvAddress = findViewById(R.id.tvProductViewAddress);
        TextView tvProductRealPrice = findViewById(R.id.tvProductViewRealPrice);
        TextView tvDiscount = findViewById(R.id.tvProductViewDiscount);
        TextView tvAvailability = findViewById(R.id.tvProductViewAvailability);

        switch(product.getAvailable()){
            case 1:
                tvAvailability.setText("Available");
                tvAvailability.setBackgroundColor(getResources().getColor(R.color.ProductAvailable));
                break;
            case 2:
                tvAvailability.setText("Ask seller");
                tvAvailability.setBackgroundColor(getResources().getColor(R.color.ProductAskSelelr));
                break;
            default:
                tvAvailability.setText("Not-available");
                tvAvailability.setBackgroundColor(getResources().getColor(R.color.ProductNotAvailable));
                break;
        }

        tvAddress.setText(product.getAddress());
        tvCategory.setText(product.getCat());
        tvProductNo.setText(String.valueOf(product.getPro_id()));




        tvPrice.setText(formattedPrice);
//
        if (product.getDiscount() > 0 && product.getPrice() > 0) {
            tvDiscount.setText(String.valueOf(numberFormat.format(product.getDiscount())) + "% off");

            double discount = (int) product.getPrice() * product.getDiscount() / 100;
            double discountedPrice = price - discount;

            Log.d("MY_TAG", String.valueOf(numberFormat.format(discountedPrice)));

            String originalText = "Rs." + numberFormat.format(price) + ".00";
            Spannable spannable = new SpannableString(originalText);
            spannable.setSpan(new StrikethroughSpan(), 0, originalText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            tvPrice.setText("Rs." + String.valueOf(numberFormat.format(discountedPrice)) + ".00");
            tvProductRealPrice.setText(spannable);
        } else if (product.getPrice() > 0) {
            LinearLayout lnrProductViewDiscountInfo = findViewById(R.id.lnrProductViewDiscountInfo);
            lnrProductViewDiscountInfo.setVisibility(View.INVISIBLE);
            tvDiscount.setVisibility(View.INVISIBLE);
            tvProductRealPrice.setVisibility(View.INVISIBLE);
            lnrProductViewDiscountInfo.removeView(tvDiscount);
            lnrProductViewDiscountInfo.removeView(tvProductRealPrice);
            lnrProductViewDiscountInfo.setPadding(0, 0, 0,0);
            tvPrice.setText("Rs." + String.valueOf(numberFormat.format(price)) + ".00");
        } else {
            tvDiscount.setVisibility(View.INVISIBLE);
            tvProductRealPrice.setVisibility(View.INVISIBLE);
            LinearLayout lnrProductViewDiscountInfo = findViewById(R.id.lnrProductViewDiscountInfo);
            lnrProductViewDiscountInfo.setVisibility(View.INVISIBLE);
            lnrProductViewDiscountInfo.removeView(tvDiscount);
            lnrProductViewDiscountInfo.removeView(tvProductRealPrice);
            lnrProductViewDiscountInfo.setPadding(0, 0, 0,0);
            LinearLayout lnrMainScroll = findViewById(R.id.lnrMainScroll);
            lnrMainScroll.removeView(tvPrice);
            lnrMainScroll.removeView(findViewById(R.id.ProductViewtextView4));
        }
//
        tvTitle.setText(product.getTitle());
        wbDescription.loadData("<html><body style='max-width:100%;overflow:hidden;'><div style='max-width:100vw;overflow:hidden;'><style>iframe{width:100vw;}</style>" + product.getDescription() + "</div></body></html>", "text/html", "UTF-8");

        mainImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showZoomableImageDialog(mainImageView);
            }
        });

        String[] imageUrls = new String[product.getImages().size()];
        for (int i = 0; i < product.getImages().size(); i++) {
            imageUrls[i] = product.getImages().get(i);
        }
        int smallerSize = 96;

        imageDownloadTasks = new ArrayList<>();
        for (String imageUrl : imageUrls) {
            ImageView imageView = new ImageView(this);
            ImageDownloadTask imageDownloadTask = new ImageDownloadTask(imageView, this);
            imageDownloadTask.execute(imageUrl);
            imageDownloadTasks.add(imageDownloadTask);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    smallerSize,
                    smallerSize));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainImageView.setImageDrawable(imageView.getDrawable());
                }
            });
            lnrImageContainer.addView(imageView);
        }

        ImageButton btnMyShop = findViewById(R.id.btnProductViewMyShop);
        btnMyShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyShop.class);
                intent.putExtra("MEMBER",product.getMembership());
                Log.d("MY_LOG",product.getMembership());
                intent.putExtra("MEMBER_NAME", product.getSeller_name());
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Pause all running AsyncTask instances
        for (ImageDownloadTask task : imageDownloadTasks) {
            task.cancel(true);
        }
    }
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            startActivityForResult(Intent.createChooser(intent, "Choose File"), FILE_CHOOSER_REQUEST_CODE);
        } else {
            startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (uploadMessage == null) {
                return;
            }

            Uri[] result = null;
            if (resultCode == RESULT_OK && data != null) {
                String dataString = data.getDataString();
                if (dataString != null) {
                    result = new Uri[]{Uri.parse(dataString)};
                }
            }

            uploadMessage.onReceiveValue(result);
            uploadMessage = null;
        }
    }
    private void handleTouch(MotionEvent event) {
        // Handle pan gestures
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchPoint.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastTouchPoint.x;
                float dy = event.getY() - lastTouchPoint.y;
                matrix.postTranslate(dx, dy);
                mainImageView.setImageMatrix(matrix);
                lastTouchPoint.set(event.getX(), event.getY());
                break;
        }
    }
    private void showZoomableImageDialog(ImageView clickedImageView) {
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_zoomable_image);

        ImageView dialogImageView = dialog.findViewById(R.id.popupImageView);
        dialogImageView.setImageDrawable(clickedImageView.getDrawable());
        setZoomableImageListeners(dialogImageView);

        dialogImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                handleTouch(event);
                return true;
            }
        });

        dialogImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog if needed, or perform other actions
                dialog.dismiss();
            }
        });

        // Replace the mainImageView with the clicked image
        mainImageView.setImageDrawable(clickedImageView.getDrawable());

        dialog.show();
    }
    private void setZoomableImageListeners(final ImageView imageView) {
        // Set up GestureDetector for detecting gestures
        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Double tap to reset the zoom
                matrix.reset();
                imageView.setImageMatrix(matrix);
                return true;
            }
        });

        // Set up ScaleGestureDetector for scaling gestures
        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                // Scale the image
                float scaleFactor = detector.getScaleFactor();
                matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                imageView.setImageMatrix(matrix);
                return true;
            }
        });

        // Set up touch listener to handle pan gestures
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Pass the touch event to the gesture detectors
                gestureDetector.onTouchEvent(event);
                scaleGestureDetector.onTouchEvent(event);
                handleTouch(event);
                return true;
            }
        });
    }

    private static class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<Context> contextReference;

        ImageDownloadTask(ImageView imageView, Context context) {
            imageViewReference = new WeakReference<>(imageView);
            contextReference = new WeakReference<>(context);
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
                if(mainImageView.getVisibility() == View.INVISIBLE){
                    mainImageView.setVisibility(View.VISIBLE);
                    mainImageView.setImageDrawable(imageView.getDrawable());
                }
            }
        }
    }
}
