package pl.tajchert.buswear.sample;

import android.os.Parcel;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import pl.tajchert.buswear.EventBus;
import pl.tajchert.buswear.util.WearBusTools;


public class MessagesCatcher extends WearableListenerService {
    private static final String TAG = "MessagesCatcher";

    //TODO inclide in library instead of here - problem with getting class for name
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(messageEvent.getPath().contains(WearBusTools.MESSAGE_PATH)) {
            String className = getPackageName() + "." + messageEvent.getPath().replace(WearBusTools.MESSAGE_PATH, "");
            try {
                Object obj = Class.forName(className).getConstructor(Parcel.class).newInstance(WearBusTools.byteToParcel(messageEvent.getData()));
                EventBus.getDefault().postLocal(obj);
            } catch (Exception e) {
                Log.d(TAG, "syncEvent error: " + e.getMessage());
            }
        }
    }
}
