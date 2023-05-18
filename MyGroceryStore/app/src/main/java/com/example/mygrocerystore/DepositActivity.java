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
        dispay1 = findViewById(R.id.display_money);

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

                // Check if the amount is not empty
                if (!amount.isEmpty()) {
                    // Convert the amount to the appropriate format (e.g., cents)
                    int amountInCents = Integer.parseInt(amount) * 100;

                    // Call the ZaloPay SDK to create an order
                    CreateOrder orderApi = new CreateOrder();

                    try {
                        // Create the order with the specified amount
                        JSONObject data = orderApi.createOrder(String.valueOf(amountInCents));
                        String code = data.getString("return_code");

                        if (code.equals("1")) {
                            // Get the transaction token
                            String token = data.getString("zp_trans_token");

                            // Use the ZaloPay SDK to initiate the payment
                            ZaloPaySDK.getInstance().payOrder(DepositActivity.this, token, "demozpdk://app", new PayOrderListener() {
                                @Override
                                public void onPaymentSucceeded(String transactionId, String transToken, String appTransID) {
                                    // Handle successful payment
                                    // You can perform necessary actions here, such as updating the user's balance or displaying a success message.
                                    Toast.makeText(DepositActivity.this, "Payment succeeded", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPaymentCanceled(String zpTransToken, String appTransID) {
                                    // Handle payment cancellation
                                    // You can perform necessary actions here, such as displaying a cancellation message.
                                    Toast.makeText(DepositActivity.this, "Payment canceled", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onPaymentError(ZaloPayError zaloPayError, String zpTransToken, String appTransID) {
                                    // Handle payment error
                                    // You can perform necessary actions here, such as displaying an error message.
                                    Toast.makeText(DepositActivity.this, "Payment error: ", Toast.LENGTH_SHORT).show();
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
