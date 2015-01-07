package pl.tajchert.buswear;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;


public class EventCatcher extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        EventBus.syncEvent(messageEvent);
    }
}
