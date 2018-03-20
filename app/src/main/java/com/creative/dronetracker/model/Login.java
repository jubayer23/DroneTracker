package com.creative.dronetracker.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by jubayer on 8/22/2017.
 */

public class Login implements Parcelable
{

    @SerializedName("success")
    @Expose
    private Integer success;
    @SerializedName("user")
    @Expose
    private User user;

    protected Login(Parcel in) {
        user = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(user, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Login> CREATOR = new Creator<Login>() {
        @Override
        public Login createFromParcel(Parcel in) {
            return new Login(in);
        }

        @Override
        public Login[] newArray(int size) {
            return new Login[size];
        }
    };

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}