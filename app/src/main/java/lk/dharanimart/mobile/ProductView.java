package lk.dharanimart.mobile;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
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
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import lk.dharanimart.mobile.Requests.HttpGetRequestTask;
import lk.dharanimart.mobile.Responses.Product;

public class ProductView extends AppCompatActivity {

    public Product product;
    private static ImageView mainImageView;
    private PointF lastTouchPoint = new PointF();
    private ValueCallback<Uri[]> uploadMessage;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;

    private static final int INVALID_POINTER_ID = -1;
    private Matrix matrix = new Matrix();
    private float[] matrixValues = new float[9];
    private float lastTouchX, lastTouchY;
    private int activePointerId = INVALID_POINTER_ID;
    private float initialDistance = 0f;
    ImageView popupImageView;

    private GestureDetector gestureDetector;
    private static final long DOUBLE_TAP_TIMEOUT = 200; // Adjust as needed
    private long lastTapTime = 0;


    private static final float MAX_ZOOM_FACTOR = 2.0f;
    private static final float MIN_ZOOM_FACTOR = 0.5f;

    public ProgressBar[] progressBar;
    public LinearLayout lnrImageContainer;
    public int x;
    WebView wbDescription, wbComments;

    private ScaleGestureDetector scaleGestureDetector;

    int smallerSize = 128;


    ArrayList<ImageDownloadTask> imageDownloadTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_view);

        x = 0;

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        String productString = getIntent().getStringExtra("PRODUCT");

        Gson gson = new Gson();
        product = gson.fromJson(productString, Product.class);

        lnrImageContainer = findViewById(R.id.lnrProductViewImageContainer);
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
        TextView tvSellerName = findViewById(R.id.tvProductViewProSeller);
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


        tvAddress.setText(Html.fromHtml(product.getAddress()));
        tvCategory.setText(product.getCat());
        tvProductNo.setText(String.valueOf(product.getPro_id()));
        tvTitle.setText(product.getTitle());
        tvSellerName.setText(product.getSeller_name());

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
        }
        else {
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
        wbDescription.loadData("<html><body style='max-width:100%;overflow:hidden;'><div style='max-width:100vw;overflow:hidden;'><style>iframe{width:100vw;}</style>" + product.getDescription() + "</div></body></html>", "text/html", "UTF-8");

        mainImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showZoomableImageDialog(mainImageView);
            }
        });

        int imgCount = product.getImages().size();
        String[] imageUrls = new String[imgCount];
        progressBar = new ProgressBar[imgCount];

        for (int i = 0; i < imgCount; i++) {
            imageUrls[i] = product.getImages().get(i);
            progressBar[i] = new ProgressBar(this);
            lnrImageContainer.addView(progressBar[i]);
        }



        imageDownloadTasks = new ArrayList<>();
        x = 0;
        for (int i = 0; i < imgCount; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    smallerSize,
                    smallerSize));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mainImageView != null) {
                        mainImageView.setImageDrawable(imageView.getDrawable());
                    }
                }
            });
            lnrImageContainer.addView(imageView);

            // Start the image download task
            ImageDownloadTask imageDownloadTask = new ImageDownloadTask( progressBar[i], imageView,this);
            imageDownloadTask.execute(imageUrls[i]);
            imageDownloadTasks.add(imageDownloadTask);

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

        Button btnBuy = findViewById(R.id.btnBuyNow);
        btnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showContactDialog(product.getWhatsapp(), product.getContact(), product.getSms(), product.getSeller_name());
            }
        });

        ImageButton btnShare = findViewById(R.id.imgBtnShare);
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareProduct("https://dharanimart.lk/shortLink.php?id=" + product.getPro_id());
            }
        });
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        gestureDetector = new GestureDetector(this, new GestureListener());
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
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        handleTouch(event);
        return true;
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
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = event.getPointerId(0);
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() > 1) {
                    handlePinchZoom(event);
                } else {
                    handlePan(event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activePointerId = INVALID_POINTER_ID;
                break;
        }
    }
    private void resetZoom() {
        // Reset the matrix to the original state
        matrix.reset();
        initializeMatrix();
    }
    private void handlePan(MotionEvent event) {
        float dx = event.getX() - lastTouchX;
        float dy = event.getY() - lastTouchY;
        matrix.postTranslate(dx, dy);
        lastTouchX = event.getX();
        lastTouchY = event.getY();

        updateImageView();
    }

    private void handlePinchZoom(MotionEvent event) {
        int pointerIndex0 = event.findPointerIndex(activePointerId);
        int pointerIndex1 = event.findPointerIndex(activePointerId == event.getPointerId(0) ? 1 : 0);

        float x0 = event.getX(pointerIndex0);
        float y0 = event.getY(pointerIndex0);
        float x1 = event.getX(pointerIndex1);
        float y1 = event.getY(pointerIndex1);

        float distance = calculateDistance(x0, y0, x1, y1);

        if (initialDistance == 0f) {
            initialDistance = distance;
        }

        float scaleFactor = distance / initialDistance;

        float currentScale = getCurrentScale();
        float newScale = currentScale * scaleFactor;

        if (newScale > MAX_ZOOM_FACTOR || newScale < MIN_ZOOM_FACTOR) {
            return;
        }

        matrix.postScale(scaleFactor, scaleFactor, (x0 + x1) / 2, (y0 + y1) / 2);

        lastTouchX = (x0 + x1) / 2;
        lastTouchY = (y0 + y1) / 2;

        updateImageView();
    }

    private float getCurrentScale() {
        matrix.getValues(matrixValues);
        return matrixValues[Matrix.MSCALE_X];
    }
    private void updateImageView() {
        if(popupImageView != null){
            popupImageView.setImageMatrix(matrix);
            popupImageView.invalidate();
        }
    }

// Add the following helper methods:

    private float calculateDistance(float x0, float y0, float x1, float y1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void initializeMatrix() {
        matrix.reset();
        popupImageView.setImageMatrix(matrix);
    }

    private void showZoomableImageDialog(ImageView clickedImageView) {

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_zoomable_image);

        ImageView dialogImageView = dialog.findViewById(R.id.popupImageView);
        dialogImageView.setImageDrawable(clickedImageView.getDrawable());
        popupImageView = dialogImageView;
        setZoomableImageListeners(dialogImageView);
        initializeMatrix();

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
    private void showContactDialog(String whatsapp, String mobile, String message, String sellerName) {
        String url = getResources().getString(R.string.siteurl) + "/APP/API/?S=SET_PRO_CONTACT_HIT&PRODUCT_ID=" + product.getPro_id();
        HttpGetRequestTask getRequestTask = new HttpGetRequestTask();
        getRequestTask.execute(url);

        final Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.contact_seller_dialogue);

        TextView tvWhatsapp = dialog.findViewById(R.id.tvWhatsapp);
        TextView tvContact = dialog.findViewById(R.id.tvContact);
        TextView tvMessage = dialog.findViewById(R.id.tvMessage);
        TextView tvSellerName = dialog.findViewById(R.id.tvSellerNameWithMessage);

        Button btnClose = dialog.findViewById(R.id.btnCloseDialogue);
        ImageButton btnWhatsapp = dialog.findViewById(R.id.btnWhatsapp);
        ImageButton btnContact = dialog.findViewById(R.id.btnContact);
        ImageButton btnMessage = dialog.findViewById(R.id.btnMessage);

        String content = "Request information about Product ID:"+ product.getPro_id() + " http://dharanimart.lk/shortLink.php?id=" + product.getPro_id();

        btnWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWhatsappChat(whatsapp, content);
            }
        });
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCallApp(mobile);
            }
        });
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMessageApp(message, content);
            }
        });

        if (!whatsapp.equals(""))
            tvWhatsapp.setText(whatsapp);
        else
            tvWhatsapp.setText("Whatsapp not available.");

        if (!mobile.equals(""))
            tvContact.setText(mobile);
        else
            tvContact.setText("Call not available.");

        if (!message.equals(""))
            tvMessage.setText(message);
        else
            tvMessage.setText("Message not available.");

        if (!sellerName.equals("")) {
            // Format the text using HTML to make only the seller's name bold
            String formattedText = "Contact <b>" + sellerName + "</b> for more information.";

            // Set the formatted text to the TextView using Html.fromHtml
            tvSellerName.setText(Html.fromHtml(formattedText));
        }
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void openWhatsappChat(String phoneNumber, String message) {
        try {
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + URLEncoder.encode(message, "UTF-8");
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException | UnsupportedEncodingException e) {
            // Handle exception if Whatsapp is not installed or encoding fails
            Toast.makeText(this, "Whatsapp is not installed or encoding failed", Toast.LENGTH_SHORT).show();
        }
    }
    private void openMessageApp(String phoneNumber, String message) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + phoneNumber));
            intent.putExtra("sms_body", message);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Handle exception if the messaging app is not available
            Toast.makeText(this, "Messaging app not available", Toast.LENGTH_SHORT).show();
        }
    }
    private void openCallApp(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Handle exception if the dialer app is not available
            Toast.makeText(this, "Dialer app not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareProduct(String link) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");

        String message = "Check out this product: " + link;
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(shareIntent, "Share Product via"));
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
        }
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
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTapTime < DOUBLE_TAP_TIMEOUT) {
                resetZoom();
            }
            lastTapTime = currentTime;
            return true;
        }
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            updateImageView();
            return true;
        }
    }
    private class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<ProgressBar> progressBarWeakReference;
        private final WeakReference<Context> contextReference;

        ImageDownloadTask(ProgressBar progressBar, ImageView imageView, Context context) {
            imageViewReference = new WeakReference<>(imageView);
            progressBarWeakReference = new WeakReference<>(progressBar);
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
            ProgressBar progressBar = progressBarWeakReference.get();

            if (imageView != null && bitmap != null) {
                imageView.setImageBitmap(bitmap);
                if (mainImageView.getVisibility() == View.INVISIBLE) {
                    mainImageView.setVisibility(View.VISIBLE);
                    mainImageView.setImageDrawable(imageView.getDrawable());
                }

                lnrImageContainer.removeView(progressBar);
            }
        }
    }

}
