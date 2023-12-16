package lk.dharanimart.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class Login extends AppCompatActivity {

    Button btnGo;
    EditText edtName;
    SharedPreferences sp;
    String text_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sp = getSharedPreferences("DharanimartApp", MODE_PRIVATE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        btnGo = findViewById(R.id.btnGo);
        edtName = findViewById(R.id.edtName);


        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                text_name = edtName.getText().toString();

                if(String.valueOf(edtName.getFreezesText()).equals("")){

                }else{
                    SharedPreferences.Editor se = sp.edit();
                    se.putString("user_name",text_name);

                    Intent intent = new Intent(getApplicationContext(), Home.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}