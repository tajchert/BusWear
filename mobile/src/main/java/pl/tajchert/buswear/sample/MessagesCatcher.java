package pl.tajchert.buswear.sample;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import pl.tajchert.buswear.EventBus;


public class MessagesCatcher extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        EventBus.syncEvent(messageEvent);
    }
}