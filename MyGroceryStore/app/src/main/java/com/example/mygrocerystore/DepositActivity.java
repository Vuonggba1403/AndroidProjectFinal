package com.example.mygrocerystore;

import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mygrocerystore.activities.MainActivity;
import com.example.mygrocerystore.ui.profile.ProfileFragment;
import com.example.mygrocerystore.zalopay.api.CreateOrder;

import org.json.JSONObject;

import java.util.HashMap;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class DepositActivity extends AppCompatActivity {

    ImageView imgreturn;
    EditText edtmoney;
    Button btnmoney;
    TextView dispay1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);
        //zalopay
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2553, Environment.SANDBOX);


        dispay1 = findViewById(R.id.display_money);
        imgreturn = findViewById(R.id.returnhome);
        edtmoney = findViewById(R.id.edtadd_money);
        btnmoney = findViewById(R.id.btnadd_money);

        imgreturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DepositActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        btnmoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = edtmoney.getText().toString().trim();

                    // Tạo đối tượng CreateOrder để gọi API tạo đơn hàng
                    CreateOrder orderApi = new CreateOrder();
                    try {
                        // Tạo đơn hàng với giá trị amount
                        JSONObject data = orderApi.createOrder(amount);
                        String code = data.getString("return_code");

                        if (code.equals("1")) {
//                            Log.d("ok",amount);
                            String token = ((JSONObject) data).getString("order_url");
                            // Gọi ZaloPay SDK để thực hiện thanh toán
                            ZaloPaySDK.getInstance().payOrder(DepositActivity.this, token, "demozpdk://app", new PayOrderListener() {

                                @Override
                                public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
//                                    Log.d("ok",amount);
//                                    Log.d("ok",amount);
                                    //hello

                                }

                                @Override
                                public void onPaymentCanceled(String zpTransToken, String appTransID) {
                                }

                                @Override
                                public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {

                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

        });
    }

}
