package pl.tajchert.buswear.sample;

import android.os.Parcel;
import android.os.Parcelable;


public class CustomObject implements Parcelable {
    //implements Parcelable but I would suggest using Parceler or Auto-Parcel library (or any other).
    private String name;

    public CustomObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    public CustomObject(Parcel in){
        this.name = in.readString();
    }

    @Override
    public String toString() {
        return "CustomObject{" +
                "name='" + name + '\'' +
                '}';
    }
}
