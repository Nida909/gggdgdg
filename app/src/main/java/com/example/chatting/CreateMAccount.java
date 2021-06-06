package com.example.chatting;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class CreateMAccount extends AppCompatActivity {
    EditText name, location, email, password, contact;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;
    Context context;
    Resources resources;
    TextView createaccount, details;
    Button btn;
    String str,check;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_m_account);
        name = findViewById(R.id.name);
        location = findViewById(R.id.location);
        dbHelper = new DatabaseHelper(this);



        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        contact = findViewById(R.id.contactnumber);
        createaccount=findViewById(R.id.createaccount);
        details=findViewById(R.id.details);
        btn=findViewById(R.id.btn);
        Intent intent = getIntent();
        String languages = intent.getExtras().getString("language");
        check= intent.getStringExtra("Check");
        if(languages.equals("ENGLISH"))
        {

            context = LocalHelper.setLocale(CreateMAccount.this, "en");
            resources = context.getResources();
            details.setText(resources.getString(R.string.details));
            name.setHint(resources.getString(R.string.name1));
            location.setHint(resources.getString(R.string.location));
            email.setHint(resources.getString(R.string.email1));
            password.setHint(resources.getString(R.string.password1));
            contact.setHint(resources.getString(R.string.number));
            btn.setText(resources.getString(R.string.savedetails));




            str="ENGLISH";




        }

        if(languages.equals("اردو"))
        {
            context = LocalHelper.setLocale(CreateMAccount.this, "an");
            resources = context.getResources();
            details.setText(resources.getString(R.string.details));
            name.setHint(resources.getString(R.string.name1));
            location.setHint(resources.getString(R.string.location));
            email.setHint(resources.getString(R.string.email1));
            password.setHint(resources.getString(R.string.password1));
            contact.setHint(resources.getString(R.string.number));
            btn.setText(resources.getString(R.string.savedetails));

            str="اردو";


        }


    }
    boolean isEmail(EditText text) {
        CharSequence email = text.getText().toString();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
    public void SaveDetails(View view) {
        db = dbHelper.getWritableDatabase();
        boolean opt=false;
        boolean opt1=false;
        String Name = name.getText().toString();
        String Location = location.getText().toString();
        String Email = email.getText().toString();
        String Password = password.getText().toString();
        String ContactNumber = contact.getText().toString();
        String[] column = {"Email", "Name"};
        String MobilePattern = "[0-9]{11}";
        final Pattern Password_Pattern=
                Pattern.compile("^"+
                        "(?=.*[0-9])"+ //ATLEAST ONE DIGIT
                        "(?=.*[a-z])" + //ATLEAST ON LOWERCASE LETTER
                        "(?=.*[A-Z])" + //ATLEAST ONE UPPERCASE LETTER
                        "(?=.*[@#$%^&+=])" + //ATLEAST ONE SPECIAL CHARACTER
                        ".{6,}"+ //ATLEAST 6 CHARACTERS
                        "$");

        if (Name.equals("") || Location.equals("") || Email.equals("") || Password.equals("") || ContactNumber.equals("")) {
            Toast.makeText(this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
        }
        else if(Character.isDigit(Name.charAt(0)))
        {
         name.setError("Name must start with Alphabet");
        }
        else if (isEmail(email) == false) {
            email.setError("Invalid email");
        }
        else if (!Password_Pattern.matcher(Password).matches())
        {

            password.setError("password too Weak\n Use atleast one digit\n one uppercase letter\n one small case letter\n one sepcial charater\n atleast 6 charaters");
        }
        else if(!contact.getText().toString().matches(MobilePattern)) {
            contact.setError("Please enter valid 11 digit phone number");
        }

        else {

if(check.equals("MilkMan"))
{
    Cursor cursor = db.query("MilkMan", column, null, null, null, null, null);
            if(cursor !=null)
            {
                while(cursor.moveToNext())
                {
                    String s1,s2;
                    s1=cursor.getString(0);
                    s2=cursor.getString(1);
                    if(s2.equals(Name))
                    {
                        opt=true;
                    }
                    if(s1.equals(Email))
                    {
                        opt1=true;
                    }
                }

            }
            if(opt) {
                Toast.makeText(CreateMAccount.this,"This name already exist",Toast.LENGTH_SHORT).show();
            }else if(opt1)
            {
                Toast.makeText(CreateMAccount.this,"This Email already exist",Toast.LENGTH_SHORT).show();
            }
                else
             {

                    ContentValues values = new ContentValues();
                    values.put(DatabaseContract.MilkMan.COL_NAME, Name);
                    values.put(DatabaseContract.MilkMan.COL_CONTACT, ContactNumber);
                    values.put(DatabaseContract.MilkMan.COL_LOCATION, Location);
                    values.put(DatabaseContract.MilkMan.COL_EMAIL, Email);
                    values.put(DatabaseContract.MilkMan.COL_PASSWORD, Password);
                    values.put(DatabaseContract.MilkMan.COL_TOTAL_PRICE, 0);
                    long newRowId = db.insert(DatabaseContract.MilkMan.TABLE_NAME, null, values);
                    if (newRowId > 0) {
                        Toast.makeText(this, "New Record Inserted: " + newRowId, Toast.LENGTH_LONG).show();
                    }
                    db.close();
                    Intent intent = new Intent(CreateMAccount.this, AddMilkInfo.class);
                    intent.putExtra("val1", Email);
                    intent.putExtra("language", str);

                    startActivity(intent);

            }
}else if(check.equals("Customer"))
{
    Cursor cursor = db.query(DatabaseContract.Customers.TABLE_NAME, column, null, null, null, null, null);
    if(cursor !=null)
    {
        while(cursor.moveToNext())
        {
            String s1,s2;
            s1=cursor.getString(0);
            s2=cursor.getString(1);
            if(s2.equals(Name))
            {
                opt=true;
            }
            if(s1.equals(Email))
            {
                opt1=true;
            }
        }

    }
    if(opt) {
        Toast.makeText(CreateMAccount.this,"This name already exist",Toast.LENGTH_SHORT).show();
    }else if(opt1)
    {
        Toast.makeText(CreateMAccount.this,"This Email already exist",Toast.LENGTH_SHORT).show();
    }
    else
    {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.Customers.COL_NAME, Name);
            values.put(DatabaseContract.Customers.COL_CONTACT, ContactNumber);
            values.put(DatabaseContract.Customers.COL_LOCATION, Location);
            values.put(DatabaseContract.Customers.COL_EMAIL, Email);
            values.put(DatabaseContract.Customers.COL_PASSWORD, Password);
            long newRowId = db.insert(DatabaseContract.Customers.TABLE_NAME, null, values);
            if (newRowId > 0) {
                Toast.makeText(this, "New Record Inserted: " + newRowId, Toast.LENGTH_LONG).show();
            }
            String[] columns = {DatabaseContract.Customers._ID, DatabaseContract.Customers.COL_EMAIL};
            Cursor c = db.query(DatabaseContract.Customers.TABLE_NAME, columns, DatabaseContract.Customers.COL_EMAIL + "=?", new String[]{Email}
                    , null, null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                String vall = String.valueOf(c.getLong(0));
                Toast.makeText(getApplicationContext(), "Record id" + vall, Toast.LENGTH_SHORT).show();

                db.close();

                Intent intent = new Intent(CreateMAccount.this, MilkManList.class);
                intent.putExtra("val", vall);
                intent.putExtra("language", str);
                startActivity(intent);

        }
    }
}else
{
    Cursor cursor = db.query(DatabaseContract.Riders.TABLE_NAME, column, null, null, null, null, null);
    if(cursor !=null)
    {
        while(cursor.moveToNext())
        {
            String s1,s2;
            s1=cursor.getString(0);
            s2=cursor.getString(1);
            if(s2.equals(Name))
            {
                opt=true;
            }
            if(s1.equals(Email))
            {
                opt1=true;
            }
        }

    }
    if(opt) {
        Toast.makeText(CreateMAccount.this,"This name already exist",Toast.LENGTH_SHORT).show();
    }else if(opt1)
    {
        Toast.makeText(CreateMAccount.this,"This Email already exist",Toast.LENGTH_SHORT).show();
    }
    else
    {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.Riders.COL_NAME, Name);
            values.put(DatabaseContract.Riders.COL_CONTACT, ContactNumber);
            values.put(DatabaseContract.Riders.COL_LOCATION, Location);
            values.put(DatabaseContract.Riders.COL_EMAIL, Email);
            values.put(DatabaseContract.Riders.COL_PASSWORD, Password);
            values.put(DatabaseContract.Riders.COL_TOTAL_PRICE, 0);
            long newRowId = db.insert(DatabaseContract.Riders.TABLE_NAME, null, values);
            if (newRowId > 0) {
                Toast.makeText(this, "New Record Inserted: " + newRowId, Toast.LENGTH_LONG).show();
            }
            String[] columns = {DatabaseContract.Riders._ID, DatabaseContract.Riders.COL_EMAIL};
            Cursor c = db.query(DatabaseContract.Riders.TABLE_NAME, columns, DatabaseContract.Riders.COL_EMAIL + "=?", new String[]{Email}
                    , null, null, null, null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                String vall = String.valueOf(c.getLong(0));
                Toast.makeText(getApplicationContext(), "Record id" + vall, Toast.LENGTH_SHORT).show();

                db.close();

                Intent intent = new Intent(this, orderlist.class);
                intent.putExtra("val", vall);
                intent.putExtra("language", str);
                startActivity(intent);
            }

    }
}
        }


    }
    public static boolean isValidPassword(String s) {
        Pattern PASSWORD_PATTERN
                = Pattern.compile(
                "[a-zA-Z0-9\\!\\@\\#\\$]{8,24}");

        return !TextUtils.isEmpty(s) && PASSWORD_PATTERN.matcher(s).matches();
    }
}


