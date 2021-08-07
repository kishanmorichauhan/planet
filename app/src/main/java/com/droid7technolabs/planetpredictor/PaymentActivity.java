package com.droid7technolabs.planetpredictor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.droid7technolabs.planetpredictor.ui.home.HomeFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity implements PaymentResultListener {

    Button payment_btn;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    String userId;
    String prize;
    String senderUid;
    String senderRoom, receiverRoom;
    String msg;

    TextView tv_Prize;
    String adminid = "RB1AwI7NHZOsg3Y5IePe9yuZabM2";
    Date date = new Date();
    DateFormat df = new SimpleDateFormat("d MMM,h:mm a");
    String date1 = df.format(Calendar.getInstance().getTime());
    FirebaseDatabase database;
    Messages wlcMsg2 = new Messages("Thank you! One of our astrologers will contact you within 24 hours.", adminid, date.getTime(), "");

    //Messages message = new Messages(messageTxt, senderUid, date.getTime(), date1);

    Messages paymentMsg = new Messages("Payment Received for normal question", adminid, date.getTime(), "");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Checkout.preload(getApplicationContext());
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        tv_Prize = findViewById(R.id.price);
        database = FirebaseDatabase.getInstance();

        payment_btn = findViewById(R.id.payment_btn);

        //Fatching Question price from fireStore
        DocumentReference docRef = firestore.collection("Prices").document("RB1AwI7NHZOsg3Y5IePe9yuZabM2");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull  Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        prize = document.getString("normal_price");
                        int amount = Math.round(Float.parseFloat(prize)/100);
                        String p = String.valueOf(amount);
                        tv_Prize.setText("Rs. "+p);
                    } else {
                        Log.d("LOGGER", "No such document");
                    }
                } else {
                    Log.d("LOGGER", "get failed with ", task.getException());
                }
            }
        });

        payment_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Checkout checkout = new Checkout();
                //API key of razorpay
                checkout.setKeyID("rzp_live_q1YBy2AxpwLY04");
                checkout.setImage(R.drawable.dialog_icon);//set icon
                JSONObject object = new JSONObject();

                try {
                    //name
                    object.put("name","Planets Predictors");
                    //description
                    object.put("description","Planets Predictors Payment");
                    //theme color
                    object.put("theme.color","#040438");
                    //current unit
                    object.put("currency","INR");
                    //amount
                    object.put("amount",prize);
                    //mobile no
                    object.put("prefill.contact", null);
                    //email
                    object.put("prefill.email", null);
                    //open razor checkout activity
                    checkout.open(PaymentActivity.this,object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    public void onPaymentSuccess(String s) {
        //dialog box
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Intent i = new Intent(PaymentActivity.this, Navigation2Activity.class);
        startActivity(i);

        //store PaymentId and UId is Successful
        userId = auth.getCurrentUser().getUid();
        DocumentReference documentReference = firestore.collection("Payment").document(userId);
        Map<String,Object> payment = new HashMap<>();
        payment.put("paymentId",s);
        payment.put("userid", userId);


        //showing payment message to admin
        documentReference.set(payment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {

                SharedPreferences preferences = getSharedPreferences("com.example.counter", 0);
                preferences.edit().remove("pref_total_key").commit();

                senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

                Intent iin= getIntent();
                Bundle b = iin.getExtras();

                if(b!=null)
                {
                    msg =(String) b.get("msg");
                }
                Messages message = new Messages(msg, senderUid, date.getTime(), date1);
                String randomKey = database.getReference().push().getKey();
                senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                adminid = "RB1AwI7NHZOsg3Y5IePe9yuZabM2";
                //Room
                senderRoom = senderUid + adminid;
                receiverRoom = adminid + senderUid;

                database.getReference().child("chats")
                        .child(senderRoom)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                database.getReference().child("chats")
                                        .child(receiverRoom)
                                        .child("messages")
                                        .child(randomKey)
                                        .setValue(message)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //sending notification to admin
                                                //clear message in messageBox
                                                //clear message in sharePreference
                                                SharedPreferences preferences = getSharedPreferences("mypref", 0);
                                                SharedPreferences.Editor rm = preferences.edit().clear();
                                                rm.clear();
                                                rm.apply();
                                                preferences.edit().remove("name").commit();

                                                //for welcome message 2
                                                String randomKey2 = database.getReference().push().getKey();

                                                database.getReference().child("chats")
                                                        .child(senderRoom)
                                                        .child("messages")
                                                        .child(randomKey2)
                                                        .setValue(wlcMsg2)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                database.getReference().child("chats")
                                                                        .child(receiverRoom)
                                                                        .child("messages")
                                                                        .child(randomKey2)
                                                                        .setValue(wlcMsg2)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void unused) {

                                                                            }
                                                                        });
                                                            }
                                                        });

                                            }
                                        });
                            }
                        });
                senderRoom = senderUid + adminid;
                receiverRoom = adminid + senderUid;

                    database.getReference().child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .push()
                            .setValue(paymentMsg)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(PaymentActivity.this, "Payment Successful!", Toast.LENGTH_SHORT).show();

                                }
                            });
                }


        });

    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment cancelled", Toast.LENGTH_SHORT).show();
    }
}