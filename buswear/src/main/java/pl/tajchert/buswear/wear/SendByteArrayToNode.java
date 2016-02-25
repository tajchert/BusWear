package pl.tajchert.buswear.wear;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

public class SendByteArrayToNode extends Thread {

    private final byte[] objectArray;
    private final Context context;
    private final boolean sticky;
    private final Class clazzToSend;

    /**
     * Internal BusWear method, using it outside of library is possible but not supported or tested
     */
    public SendByteArrayToNode(byte[] objArray, Class classToSend, Context ctx, boolean isSticky) {
        objectArray = objArray;
        context = ctx;
        sticky = isSticky;
        clazzToSend = classToSend;

        if ((objectArray.length / 1024) > 100) {
            throw new RuntimeException("Object is too big to push it via Google Play Services");
        }
    }

    public void run() {
        GoogleApiClient googleApiClient = SendWearManager.getInstance(context);
        googleApiClient.blockingConnect(WearBusTools.CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result;
            if (sticky) {
                result = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), WearBusTools.MESSAGE_PATH_STICKY + WearBusTools.CLASS_NAME_DELIMITER + clazzToSend.getName(), objectArray).await();
            } else {
                result = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), WearBusTools.MESSAGE_PATH + WearBusTools.CLASS_NAME_DELIMITER + clazzToSend.getName(), objectArray).await();
            }
            if (!result.getStatus().isSuccess()) {
                Log.v(WearBusTools.BUSWEAR_TAG, "ERROR: failed to send Message via Google Play Services to node " + node.getDisplayName());
            }
        }
    }
}