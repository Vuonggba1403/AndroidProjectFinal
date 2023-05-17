package com.example.mygrocerystore.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mygrocerystore.PaymentOnline;
import com.example.mygrocerystore.R;
import com.example.mygrocerystore.models.MyCartModel;
import com.example.mygrocerystore.zalopay.api.CreateOrder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PlacedOrderActivity extends AppCompatActivity
{

    FirebaseAuth auth;
    FirebaseFirestore firestore;

    TextView overTotalAmount;
    Button paymentonline,zalopay;
    EditText phonenumber,gmail,delivery;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placed_order);

        //zalopay
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // ZaloPay SDK Init
        ZaloPaySDK.init(2553, Environment.SANDBOX);


        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        overTotalAmount = findViewById(R.id.totalmoney);
        paymentonline = findViewById(R.id.directpayment);
        phonenumber = findViewById(R.id.phonenumber);
        gmail = findViewById(R.id.gmail);
        delivery = findViewById(R.id.delivery);
        zalopay = findViewById(R.id.btnzalopay);


        gmail.setText(auth.getCurrentUser().getEmail());

        // Nhận giá trị totalAmount từ Intent
        int totalAmount = getIntent().getIntExtra("totalAmount", 0);

        overTotalAmount.setText(totalAmount + " VNĐ");

        zalopay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = phonenumber.getText().toString().trim();
                String gm = gmail.getText().toString().trim();
                String deli = delivery.getText().toString().trim();
                //kiểm tra nhập tt đầy đủ mới cho click
                if (phone.isEmpty() || gm.isEmpty() || deli.isEmpty()) {
                    Toast.makeText(PlacedOrderActivity.this, "Please Enter Information !", Toast.LENGTH_SHORT).show();
                    return;
                } else {
//                    Toast.makeText(PlacedOrderActivity.this, "dc", Toast.LENGTH_SHORT).show();
                    CreateOrder orderApi = new CreateOrder();

                    try {
                        // Tạo đơn hàng với giá trị overtotalamount
                        JSONObject data = orderApi.createOrder(String.valueOf(totalAmount));
                        String code = data.getString("return_code");

                        if (code.equals("1")) {
                            String token = data.getString("zp_trans_token");
                            ZaloPaySDK.getInstance().payOrder(PlacedOrderActivity.this, token, "demozpdk://app", new PayOrderListener() {
                                @Override
                                public void onPaymentSucceeded(final String transactionId, final String transToken, final String appTransID) {
                                    firestore.collection("AddToCart")
                                            .document(auth.getCurrentUser().getUid())
                                            .collection("CurrentUser")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    Context context;
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            document.getReference().delete();
                                                            overTotalAmount.setText(" 0 VNĐ");
                                                        }
                                                        Toast.makeText(PlacedOrderActivity.this, "Back To Home", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(PlacedOrderActivity.this, "Error deleting items: " + task.getException(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });


                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(PlacedOrderActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }, 1000);
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
                }

        });




        // Retrieve the stored address from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
        String storedAddress = preferences.getString("address", "");
        String storeNumber = preferences.getString("number", "");
        if (!storedAddress.isEmpty()) {
            delivery.setText(storedAddress);
            phonenumber.setText(storeNumber);
        }



        paymentonline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = phonenumber.getText().toString().trim();
                String gm = gmail.getText().toString().trim();
                String deli = delivery.getText().toString().trim();

                if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", gm)) {
                    // Nếu chuỗi không đúng định dạng email, hiển thị thông báo lỗi
                    Toast.makeText(PlacedOrderActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!Pattern.matches("^\\d{10}$", phone)) {
                    // Nếu chuỗi không đúng định dạng số điện thoại, hiển thị thông báo lỗi
                    Toast.makeText(PlacedOrderActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                }


                //kiểm tra nhập tt đầy đủ mới cho click
                if (phone.isEmpty() || gm.isEmpty() || deli.isEmpty()) {
                    Toast.makeText(PlacedOrderActivity.this, "Please Enter Information !", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    Intent intent = new Intent(PlacedOrderActivity.this, PaymentOnline.class);
                    startActivity(intent);
                }

                firestore.collection("AddToCart")
                        .document(auth.getCurrentUser().getUid())
                        .collection("CurrentUser")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                Context context;
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        document.getReference().delete();
                                    }
                                    Toast.makeText(PlacedOrderActivity.this, "Back To Home", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(PlacedOrderActivity.this, "Error deleting items: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(PlacedOrderActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }, 3000);
            }
        });


        List<MyCartModel> list = (ArrayList<MyCartModel>) getIntent().getSerializableExtra("itemList");

        if (list != null && list.size() > 0) {
            for (MyCartModel model : list) {
                final HashMap<String, Object> cartMap = new HashMap<>();

                // lưu trữ thông tin sản phẩm vào giỏ hàng
                cartMap.put("productName", model.getProductName());
                cartMap.put("productPrice", model.getProductPrice());
                cartMap.put("currentDate", model.getCurrentDate());
                cartMap.put("currentTime", model.getCurrentTime());
                cartMap.put("totalQuantity", model.getTotalQuantity());
                cartMap.put("totalPrice", model.getTotalPrice());
                // Check if user is authenticated before accessing UID
                if (auth.getCurrentUser() != null) {
                    firestore.collection("CurrentUser").document(auth.getCurrentUser().getUid())
                            .collection("MyOrder").add(cartMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                }
                            });
                }
            }
        }


    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }

}