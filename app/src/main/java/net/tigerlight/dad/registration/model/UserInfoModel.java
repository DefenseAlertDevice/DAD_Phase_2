package net.tigerlight.dad.registration.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by indianic on 22/10/16.
 */

public class UserInfoModel implements Parcelable {

    private String address="";
    private String email="";
    private String username="";
    private String phone_no="";
    private String user_id="";

    public UserInfoModel() {

    }


    public static final Creator<UserInfoModel> CREATOR = new Creator<UserInfoModel>() {
        @Override
        public UserInfoModel createFromParcel(Parcel in) {
            return new UserInfoModel(in);
        }

        @Override
        public UserInfoModel[] newArray(int size) {
            return new UserInfoModel[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public UserInfoModel(Parcel in) {
        address = in.readString();
        email = in.readString();
        username = in.readString();
        phone_no = in.readString();
        user_id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(address);
        parcel.writeString(email);
        parcel.writeString(username);
        parcel.writeString(phone_no);
        parcel.writeString(user_id);
    }
}
