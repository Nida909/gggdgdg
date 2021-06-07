package com.example.chatting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity3 extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LatLng previousLatLng;
    LatLng currentLatLng;
    LatLng latLng1, latLng2;
    private Polyline polyline1,polyline2,p3;
    ProgressDialog progressDialog;
    private List<LatLng> polylinePoints = new ArrayList<>();
    private Marker mCurrLocationMarker;
    String pickup,dropoff,Id,cId,RiderContact,Count,milkmanId,price,RiderId;
    Button succes,cancel,pay;
    FirebaseDatabase database;
    DatabaseReference ref;
    private Chronometer mChronometer;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    RelativeLayout rl;
    TextView Name,Contact;
    EditText text;
    ListView listview;
    Activity activity;
    TextView distn;
    double p1,p2,pp;
    double dis;
    double total,total1;
    ImageButton btn;
    String languages,lang;
    ArrayList<RiderClass> array=new ArrayList<RiderClass>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps3);
        Intent intent = getIntent();
        pickup = intent.getStringExtra("PickUp");
        dropoff = intent.getStringExtra("DropOff");
        milkmanId = intent.getStringExtra("milkmanId");
        cId = intent.getStringExtra("customerId");
        Count=intent.getStringExtra("Count");
        languages=intent.getStringExtra("language");
        Toast.makeText(MapsActivity3.this, pickup + dropoff, Toast.LENGTH_SHORT).show();
       // setHasOptionsMenu(true);
        succes = (Button) findViewById(R.id.succesfull);
        cancel = (Button) findViewById(R.id.cancel);
        pay=(Button) findViewById(R.id.pay);
        btn=(ImageButton) findViewById(R.id.img);
        registerForContextMenu(btn);
        distn=(TextView)findViewById(R.id.distance);
        activity=this;
        //Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
       // setActionBar(toolbar);
        database = FirebaseDatabase.getInstance();
         ref = database.getReference();
        ref.child("Orderlocation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                price=snapshot.child(Count).child("Price").getValue().toString();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        Name=(TextView)findViewById(R.id.cname) ;
        Contact=(TextView)findViewById(R.id.ccontact) ;
        text=(EditText) findViewById(R.id.edt);
        listview=(ListView)findViewById(R.id.list) ;
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.setHasOptionsMenu(true);

       // mapFragment. getSupportActionBar().hide();
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();
        String[] colm={DatabaseContract.Customers.COL_CONTACT};
        Cursor cr=db.query(DatabaseContract.Customers.TABLE_NAME,colm,DatabaseContract.Customers._ID+"=?", new String[] {cId}
                , null, null, null, null);
        if (cr.getCount()==0) {
            Toast.makeText(getApplicationContext(),"No Record exist",Toast.LENGTH_LONG).show();
        }
        else {

            cr.moveToFirst();
           Id = cr.getString(0);
           Toast.makeText(MapsActivity3.this,Id,Toast.LENGTH_SHORT).show();
        }
        mChronometer = new Chronometer(this);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        long elapsedMillis = SystemClock.elapsedRealtime()
                - mChronometer.getBase();
        int hours = (int) (elapsedMillis / 3600000);
        int minutes = (int) (elapsedMillis - hours * 3600000) / 60000;
        if (minutes > 4) {
            cancel.setVisibility(View.INVISIBLE);
            mChronometer.stop();
        }
        //database = FirebaseDatabase.getInstance();
         ref = database.getReference();
        ref.child("Orderlocation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.child(Count).child("Cancel").getValue().equals("Complete")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity3.this);
                    // Set a title for alert dialog
                    builder.setTitle("Order Completed");
                    // Ask the final question
                    builder.setMessage("Your Order Has Been Successfully Completed");
                    // Set the alert dialog yes button click listener
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                           // ref.child("Orderlocation").child(Id).removeValue();
                            //ref.child("Orderlocation").child(Count).child("Cancel").setValue("Successfull");
                            //Intent in = new Intent(MapsActivity3.this, MilkManList.class);
                           // startActivity(in);
                            pay.setVisibility(View.VISIBLE);
                        }
                    });
                    AlertDialog dialog = builder.create();
                    // Display the alert dialog on interface
                    dialog.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        progressDialog = new ProgressDialog(MapsActivity3.this);
        progressDialog.setMessage("Searching for Rider..."); // Setting Message
        progressDialog.setTitle("Selecting Rider"); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//if(ref.child("Rider").child(Id).)
        ref.child("Rider").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressDialog.show(); // Display Progress Dialog
                progressDialog.setCancelable(false);
                if(!snapshot.hasChild(Count))
                {
                    //progressDialog = new ProgressDialog(MapsActivity3.this);
                     // Progress Dialog Style Spinner

                }
                else
                {
                    RiderContact=snapshot.child(Count).child("Contact").getValue().toString();
                    Name.setText("You have Been Assign Ride Of "+snapshot.child(Count).child("Name").getValue().toString());
                    Contact.setText("Contact "+snapshot.child(Count).child("Contact").getValue().toString());
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        //ref=database.getReference().child("Messages");
        ref.child("Messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(Id))
                {
                    array.clear();
                    String mm=""+snapshot.child(Id).child("msg").getValue();
                    RiderClass rc=new RiderClass(mm,"White");
                    array.add(rc);
                    RiderAdapter ra=new RiderAdapter(activity,array);
                    listview.setAdapter(ra);
                    ra.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.main1, menu);
        //super.onCreateOptionsMenu(menu);
        //return true;
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.Ordersh:
                Toast.makeText(getApplicationContext(),"Record id"+ Id,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, CustomerHistory.class);
                intent.putExtra("var",   Id);
                intent.putExtra("cID",   cId);
                intent.putExtra("milkmanId",milkmanId);
                startActivity(intent);
                finish();
                return true;
            case R.id.Chat:

                Intent inten = new Intent(this, PhoneNumberActivity.class);
                //inten.putExtra("var", str);
                startActivity(inten);
                return true;
            default:
                return false;
        }
    }
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        List<Address> addressList1=null;
        Geocoder geocoder=new Geocoder(MapsActivity3.this);
        try {
            addressList1=geocoder.getFromLocationName(pickup,1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address address=addressList1.get(0);
        latLng1=new LatLng(address.getLatitude(),address.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng1).title("Milkman Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1,10));


        List<Address> addressList2=null;
        Geocoder geocoder2=new Geocoder(MapsActivity3.this);
        try {
            addressList2=geocoder2.getFromLocationName(dropoff,1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address address2=addressList2.get(0);
        latLng2=new LatLng(address2.getLatitude(),address2.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLng2).title("Drop Off Location"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1,10));
        // Add a marker  and move the camera
        dis=(SphericalUtil.computeDistanceBetween(latLng2, latLng1))/1000;
        polyline2=mMap.addPolyline(new PolylineOptions().clickable(true).add(latLng1,latLng2));
        polyline1 = mMap.addPolyline(new PolylineOptions().addAll(polylinePoints));
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("location"))
                {
                    fetchLocationUpdates();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void fetchLocationUpdates() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("location").child("device1");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.i("tag", "New location updated:" + dataSnapshot.getKey());
                updateMap(dataSnapshot);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateMap(DataSnapshot dataSnapshot) {
        double latitude = 0, longitude = 0;

        Iterable<DataSnapshot> data = dataSnapshot.getChildren();
        for(DataSnapshot d: data){
            if(d.getKey().equals("latitude")){
                latitude = (Double) d.getValue();
            }else if(d.getKey().equals("longitude")){
                longitude = (Double) d.getValue();
            }
        }

        currentLatLng = new LatLng(latitude, longitude);

        if(previousLatLng ==null || previousLatLng != currentLatLng){
            // add marker line
            if(mMap!=null) {
                previousLatLng  = currentLatLng;
                polylinePoints.add(currentLatLng);
                polyline1.setPoints(polylinePoints);
                Log.w("tag", "Key:" + currentLatLng);
                if(mCurrLocationMarker!=null){
                    mCurrLocationMarker.setPosition(currentLatLng);
                }else{
                    mCurrLocationMarker = mMap.addMarker(new MarkerOptions()
                            .position(currentLatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                            .title("Delivery"));
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16));
                p3=mMap.addPolyline(new PolylineOptions().clickable(true).add(latLng1,currentLatLng));

                distn.setText("Distance is "+(SphericalUtil.computeDistanceBetween(latLng2, currentLatLng))/1000 +"km");
            }

        }
    }

    public void Successfull(View v)
    {
        pay.setVisibility(View.VISIBLE);

    }
    public void CancelOrder(View v)
    {
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        ref.child("Orderlocation").child(Count).child("Cancel").setValue("Cancel");
        Intent intn=new Intent(MapsActivity3.this,MilkManList.class);
        startActivity(intn);
    }
    public void onbtn(View v)
    {
        if(!text.getText().toString().equals(null))
        {
            //array.clear();
            String ss=text.getText().toString();
            RiderClass rc=new RiderClass(ss,"Green");
            array.add(rc);
            RiderAdapter ra=new RiderAdapter(activity,array);
            listview.setAdapter(ra);
            ref=database.getReference();
            ref.child("Messages").child(RiderContact).child("msg").setValue(text.getText().toString());
        }

    }
    public void Showmenu(View v)
    {

        this.openOptionsMenu();

    }
    public void pay(View v)
    {

        Intent i = new Intent(MapsActivity3.this, PaymentActivity.class);
        //startActivity(new Intent(MainActivity.this, PaymentActivity.class));
     //   i.putExtra("milkmanPrice",String.valueOf(p2));
      //  i.putExtra("riderPrice",String.valueOf(pp));
i.putExtra("Price",price);
        startActivityForResult(i, 0);


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that it is the SecondActivity with an OK result
        if (requestCode == 0 && resultCode == RESULT_OK) {
            // Get String data from Intent
            String ResponseCode = data.getStringExtra("pp_ResponseCode");
            System.out.println("DateFn: ResponseCode:" + ResponseCode);
            if(ResponseCode.equals("000")) {
                Toast.makeText(getApplicationContext(), "Payment Success", Toast.LENGTH_SHORT).show();
                database = FirebaseDatabase.getInstance();
                ref = database.getReference();
                ref.child("Rider").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        RiderId=snapshot.child(Count).child("RiderId").getValue().toString();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                pp=50+(dis*5);
                p1=Double.parseDouble(price);
                p2=p1-pp;
              /*  String[] wherearg={milkmanId};

                String[] columns = { DatabaseContract.MilkMan.COL_TOTAL_PRICE};
                Cursor c = db.query(DatabaseContract.MilkMan.TABLE_NAME, columns, DatabaseContract.MilkMan._ID + "=?", wherearg
                        , null, null, null, null);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                     total = c.getLong(0)+p2;
                }
                //ref.child("Orderlocation").child(Id).removeValue();
                ContentValues args = new ContentValues();
                args.put(DatabaseContract.MilkMan.COL_TOTAL_PRICE,total);

                Integer count= db.update(DatabaseContract.MilkMan.TABLE_NAME, args, DatabaseContract.MilkMan._ID + "=?",wherearg);
                if (count > 0) {
                    Toast.makeText(this, count+"  Records updated: " , Toast.LENGTH_SHORT).show();
                }


                String[] wherearg2={RiderId};
              String[] column = { DatabaseContract.Riders.COL_TOTAL_PRICE};
               Cursor cr = db.query(DatabaseContract.Riders.TABLE_NAME, column, DatabaseContract.Riders._ID + "=?", wherearg2
                        , null, null, null, null);
                if (cr.getCount() > 0) {
                    cr.moveToFirst();
                    total1 = c.getLong(0)+pp;
                }
                ContentValues args2 = new ContentValues();
                args2.put(DatabaseContract.Riders.COL_TOTAL_PRICE,total1);

                Integer cont= db.update(DatabaseContract.Riders.TABLE_NAME, args2, DatabaseContract.Riders._ID + "=?",wherearg2);
                if (cont > 0) {
                    Toast.makeText(this, cont+"  Records updated: " , Toast.LENGTH_SHORT).show();
                }

                ContentValues args3 = new ContentValues();
                args3.put(DatabaseContract.OrderT.COL_STATUS,"Successfull");
                String[] wherearg3={cId,milkmanId};
                Integer con= db.update(DatabaseContract.OrderT.TABLE_NAME, args3, "PlacedBy=? AND PlacedTo=?",wherearg3);
                if (con > 0) {
                    Toast.makeText(this, cont+"  Records updated: " , Toast.LENGTH_SHORT).show();
                }
                db.close();*/

               // database = FirebaseDatabase.getInstance();
                //ref = database.getReference();
                ref.child("Orderlocation").child(Count).child("Cancel").setValue("Successfull");
                Intent intn=new Intent(MapsActivity3.this,MilkManList.class);
                intn.putExtra("language",languages);
                startActivity(intn);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Payment Failed", Toast.LENGTH_SHORT).show();
                pay.setText("ReTry");
            }
        }
    }
}