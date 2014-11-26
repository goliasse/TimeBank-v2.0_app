package me.mariotti.timebank.classes;


import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Listing implements Parcelable{
    public static final DateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
    public int id;
    public String description;
    public int categoryId;
    public String categoryName=null;
    public Date dateCreation;
    public int owner;
    public int applicant;
    public boolean requested;
    // TODO add owner class and an owner field


    public Listing(JSONObject json) {
        try {
            id = json.getInt("id");
            description = json.getString("description");
            categoryId = json.getInt("category");
            dateCreation = dateParser.parse(json.getString("creation_date"));
            owner=json.getInt("owner");
            applicant = json.getInt("applicant");
            requested = json.getBoolean("requested");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String toString() {
        return "Description: " + description + "\t-\t" + "Category id: " + categoryId + "\t-\t" + "Category name: " +
               categoryName + "\t-\t" + "Pubblishing Date: " + dateFormatter.format(dateCreation) + "\t-\t" + "Apllicant: " +
               applicant + "\t-\t" + "Requested: " + requested;
    }

    //Parcelling
    public Listing(Parcel in) {
        try {
            Bundle mBundle = in.readBundle();
            id = mBundle.getInt("id");
            description = mBundle.getString("description");
            categoryId = mBundle.getInt("categoryId");
            categoryName = mBundle.getString("categoryName");
            dateCreation = dateParser.parse(mBundle.getString("dateCreation"));
            owner = mBundle.getInt("owner");
            applicant = mBundle.getInt("applicant");
            requested = mBundle.getBoolean("requested");
        }
         catch (ParseException e) {
            e.printStackTrace();
        }

    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Bundle mBundle = new Bundle();
        mBundle.putInt("id", id);
        mBundle.putString("description",description);
        mBundle.putInt("categoryId", categoryId);
        mBundle.putString("categoryName", categoryName);
        mBundle.putString("dateCreation", dateParser.format(dateCreation));
        mBundle.putInt("owner", owner);
        mBundle.putInt("applicant", applicant);
        mBundle.putBoolean("requested",requested);
        parcel.writeBundle(mBundle);

    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Listing createFromParcel(Parcel in) {
            return new Listing(in);
        }

        public Listing[] newArray(int size) {
            return new Listing[size];
        }
    };
}