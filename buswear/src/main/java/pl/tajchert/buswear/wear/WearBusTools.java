package pl.tajchert.buswear.wear;


import android.os.Parcel;
import android.os.Parcelable;

public class WearBusTools {
    public static final String BUSWEAR_TAG = "BusWearTag";
    public final static String MESSAGE_PATH = "pl.tajchert.buswear.event.";
    public final static String MESSAGE_PATH_STICKY = "pl.tajchert.buswear.stickyevent.";
    public final static String MESSAGE_PATH_COMMAND = "pl.tajchert.buswear.command.";

    //Commands
    public final static String ACTION_STICKY_CLEAR_ALL = "pl.tajchert.buswear.clearall";


    public static final long CONNECTION_TIME_OUT_MS = 100;

    public static byte[] parcelToByte(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static Parcel byteToParcel(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        return parcel;
    }
}
