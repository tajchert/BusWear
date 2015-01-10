package pl.tajchert.buswear.wear;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;


public class SendWearManager {
    private static GoogleApiClient mGoogleApiClient;

    /**
     * Internal BusWear method, using it outside of library is possible but not supported or tested
     * Returns a instance of Google API Client
     */
    public static GoogleApiClient getInstance (Context context) {
        if(mGoogleApiClient == null) {
            if(context == null) {
                return null;
            }
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle connectionHint) {
                                Log.d(WearBusTools.BUSWEAR_TAG, "onConnected");
                                // Now you can use the Data Layer API
                            }
                            @Override
                            public void onConnectionSuspended ( int cause){
                                Log.d(WearBusTools.BUSWEAR_TAG, "onConnectionSuspended");
                            }
                        }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.d(WearBusTools.BUSWEAR_TAG, "onConnectionFailed");
                        }
                    }).addApi(Wearable.API).build();
                    }
            return mGoogleApiClient;
    }

    /**
     * Internal BusWear method, using it outside of library is possible but not supported or tested.
     *  Recreates received object using classname and byte[]
     */
    public static Object getSendObject(byte[] objectArray, String className, Class classTmp) {
        Object obj = null;
        if(className.equals("String")) {
            try {
                obj = new String(objectArray, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.d(WearBusTools.BUSWEAR_TAG, "syncEvent, cannot unparse event as: " + e.getMessage());
            }
        } else if(className.equals("Integer")){
            obj = ByteBuffer.wrap(objectArray).getInt();
        } else if(className.equals("Long")){
            obj = ByteBuffer.wrap(objectArray).getLong();
        } else if(className.equals("Double")){
            obj = ByteBuffer.wrap(objectArray).getDouble();
        } else if(className.equals("Float")){
            obj = ByteBuffer.wrap(objectArray).getFloat();
        } else if(className.equals("Short")){
            obj = ByteBuffer.wrap(objectArray).getShort();
        } else {
            try {
                obj = classTmp.getConstructor(Parcel.class).newInstance(WearBusTools.byteToParcel(objectArray));
            } catch (Exception e) {
                Log.d(WearBusTools.BUSWEAR_TAG, "syncEvent error: " + e.getMessage());
            }
        }
        return obj;
    }
}
