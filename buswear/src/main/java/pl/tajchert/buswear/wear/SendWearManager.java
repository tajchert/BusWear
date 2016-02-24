package pl.tajchert.buswear.wear;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

public class SendWearManager {

    public interface OnSendWearConnectionCallback extends GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    }

    private static GoogleApiClient mGoogleApiClient;

    /**
     * Set the default OnSendWearConnectionCallback to receive Google Api Client connection
     * callbacks. This must be set before any EventBus methods, usually in the application
     * class.
     *
     * @param defaultOnSendWearConnectionCallback
     */
    public static void setDefaultOnSendWearConnectionCallback(OnSendWearConnectionCallback defaultOnSendWearConnectionCallback) {
        SendWearManager.defaultOnSendWearConnectionCallback = defaultOnSendWearConnectionCallback;
    }

    /**
     * Internal BusWear method, using it outside of library is not supported or tested.
     * Returns a instance of Google API Client
     */
    public static GoogleApiClient getInstance(Context context) {
        if (mGoogleApiClient == null) {

            if (context == null) {
                return null;
            }

            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(defaultOnSendWearConnectionCallback)
                    .addOnConnectionFailedListener(defaultOnSendWearConnectionCallback)
                    .addApi(Wearable.API)
                    .build();
        }

        return mGoogleApiClient;
    }

    private static OnSendWearConnectionCallback defaultOnSendWearConnectionCallback = new OnSendWearConnectionCallback() {
        @Override
        public void onConnected(Bundle bundle) {
            Log.d(WearBusTools.BUSWEAR_TAG, "onConnected");
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(WearBusTools.BUSWEAR_TAG, "onConnectionSuspended");
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            Log.d(WearBusTools.BUSWEAR_TAG, "onConnectionFailed");
        }
    };
}
