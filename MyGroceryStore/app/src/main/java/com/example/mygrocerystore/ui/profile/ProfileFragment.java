package com.example.mygrocerystore.ui.profile;

import static android.content.Intent.getIntent;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mygrocerystore.DepositActivity;
import com.example.mygrocerystore.R;
import com.example.mygrocerystore.activities.LoginActivity;
import com.example.mygrocerystore.activities.MainActivity;
import com.example.mygrocerystore.models.UserModel;
import com.example.mygrocerystore.zalopay.api.CreateOrder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class ProfileFragment extends Fragment {

    CircleImageView profileImg;
    EditText  number, address;

    Button update,delete,btndesposit;

    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseDatabase database;








    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {




        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        profileImg = root.findViewById(R.id.profile_img);
        number = root.findViewById(R.id.profile_number);
        address = root.findViewById(R.id.profile_address);
        update = root.findViewById(R.id.update);
        delete = root.findViewById(R.id.deletein4);
        btndesposit = root.findViewById(R.id.textView19);









        database.getReference().child("User").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);

                        Glide.with(getContext()).load(userModel.getProfileImg()).into(profileImg);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        btndesposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DepositActivity.class);
                startActivity(intent);
            }
        });

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 33);
            }
        });
        
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showcustomDialog1();
            }
        });




        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUserProfile();
            }
        });


        return root;
    }


    private void showcustomDialog1() {
        Dialog dialog1 = new Dialog(getContext());

        dialog1.setContentView(R.layout.deleteinformation);

        ImageView btncancel = dialog1.findViewById(R.id.canceldltinformation);
        Button btndelete = dialog1.findViewById(R.id.btnDelete);

        btncancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.dismiss();
            }

        });

        btndelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Xóa tài khoản người dùng từ Firebase
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // Xóa tài khoản người dùng
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseAuth.getInstance().signOut();

                                        // Xóa tài khoản và mật khẩu đã lưu trong SharedPreferences
                                        SharedPreferences preferences = getActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = preferences.edit();
                                        editor.remove("email");
                                        editor.remove("password");
                                        editor.apply();

                                        // Xóa thành công, chuyển đến LoginActivity
                                        Intent intent = new Intent(getContext(), LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        dialog1.dismiss();
                                    } else {
                                        // Xóa thất bại, xử lý tại đây
                                    }
                                }
                            });
                }
            }

        });

        dialog1.show();
    }

    private void updateUserProfile() {
        String updatedAddress = address.getText().toString().trim();
        String updateNumber = number.getText().toString().trim();
        if (!updatedAddress.isEmpty()) {
            database.getReference().child("User").child(FirebaseAuth.getInstance().getUid())
                    .child("address").setValue(updatedAddress)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getContext(), "Updated successfully", Toast.LENGTH_SHORT).show();
                                // Store the updated address in SharedPreferences
                                SharedPreferences preferences = getActivity().getSharedPreferences("loginPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("address", updatedAddress);
                                editor.putString("number", updateNumber);
                                editor.apply();
                            } else {
                                Toast.makeText(getContext(), "Failed to update !", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getContext(), "Please Enter Information", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            Uri profileUri = data.getData();
            profileImg.setImageURI(profileUri);

            final StorageReference reference = storage.getReference().child("profile_pcture")
                    .child(FirebaseAuth.getInstance().getUid());


            reference.putFile(profileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();


                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("User").child(FirebaseAuth.getInstance().getUid())
                                    .child("profileImg").setValue(uri.toString());
                            Toast.makeText(getContext(), "Profile Picture Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        }
    }


}