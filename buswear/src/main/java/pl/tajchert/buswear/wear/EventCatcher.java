package pl.tajchert.buswear.wear;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import pl.tajchert.buswear.EventBus;


public class EventCatcher extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        EventBus.syncEvent(messageEvent);
        super.onMessageReceived(messageEvent);
    }
}
