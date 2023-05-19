package com.example.mygrocerystore;

import static com.google.android.material.internal.ContextUtils.getActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.example.mygrocerystore.activities.PlacedOrderActivity;
import com.example.mygrocerystore.ui.profile.ProfileFragment;
import com.example.mygrocerystore.zalopay.api.CreateOrder;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class DepositActivity extends AppCompatActivity {

    ImageView imgreturn;
    HashMap<Integer, Integer> moneyMap = new HashMap<>(); // HashMap để lưu trữ số tiền vừa nhập

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
        // test

        dispay1 = findViewById(R.id.display_money);
        imgreturn = findViewById(R.id.returnhome);
        edtmoney = findViewById(R.id.edtadd_money);
        btnmoney = findViewById(R.id.btnadd_money);
        dispay1 = findViewById(R.id.display_money);

        dispay1.setVisibility(View.INVISIBLE);

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

                if (!amount.isEmpty()) {
                    CreateOrder orderApi = new CreateOrder();

                    try {
                        JSONObject data = orderApi.createOrder(amount);
                        String code = data.getString("return_code");
                        int amountInCents = Integer.parseInt(amount);

                        int currentAmount = moneyMap.getOrDefault(amountInCents, 0);

                        currentAmount += amountInCents;
                        moneyMap.put(amountInCents, currentAmount);

                        dispay1.setText(String.valueOf(currentAmount) + " VND");

                        edtmoney.setText("");


                        SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("currentAmount", currentAmount);
                        editor.apply();

                        if (code.equals("1")) {
//
//                            dispay1.setText(amountover);
                            // Get the transaction token
                            String token = data.getString("zp_trans_token");
                            ZaloPaySDK.getInstance().payOrder(DepositActivity.this, token, "demozpdk://app", new PayOrderListener() {
                                @Override
                                public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
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
                } else {
                    // Display an error message if the amount is empty
                    Toast.makeText(DepositActivity.this, "Please enter the amount", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
