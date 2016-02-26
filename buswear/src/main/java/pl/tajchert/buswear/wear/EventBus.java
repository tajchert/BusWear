package pl.tajchert.buswear.wear;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;

import java.lang.reflect.Constructor;

/**
 * An extension of the Greenrobot EventBus that allows syncing events over the Android Wearable API
 */
public class EventBus extends org.greenrobot.eventbus.EventBus {

    private static EventBus defaultInstance;

    public static EventBus getDefault(@NonNull Context context) {
        if (defaultInstance == null) {
            synchronized (EventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new EventBus(context);
                }
            }
        }
        return defaultInstance;
    }

    private final Context context;

    public EventBus(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    /******************** Remote Bus Methods ************************/

    /**
     * Posts the given event (object) to the remote event bus only
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postRemote(Object event) {
        sendEventOverGooglePlayServices(event, false);
    }

    /**
     * Posts the given sticky event (object) to the remote event bus only
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postStickyRemote(Object event) {
        sendEventOverGooglePlayServices(event, true);
    }

    /**
     * Remove and gets the recent sticky event for the given event type, on the remote event bus only
     *
     * @param eventType
     * @param <T>
     * @return
     */
    public <T> void removeStickyEventRemote(Class<T> eventType) {
        new SendCommandToNode(WearBusTools.PREFIX_CLASS + WearBusTools.MESSAGE_PATH_COMMAND, null, eventType, context).start();
    }

    /**
     * Removes the sticky event if it equals to the given event, on the remote event bus only
     *
     * @return true if the events matched and the sticky event was removed.
     */
    public void removeStickyEventRemote(Object event) {
        byte[] objectInArray = WearBusTools.parseToSend(event);
        if (objectInArray != null) {
            new SendCommandToNode(WearBusTools.PREFIX_EVENT + WearBusTools.MESSAGE_PATH_COMMAND, objectInArray, event.getClass(), context).start();
        }
    }

    /**
     * Removes all sticky events, on the remote event bus only
     */
    public void removeAllStickyEventsRemote() {
        new SendCommandToNode(WearBusTools.MESSAGE_PATH_COMMAND, WearBusTools.ACTION_STICKY_CLEAR_ALL.getBytes(), String.class, context).start();
    }

    /******************** Global Bus Methods ************************/

    /**
     * Posts the given event (object) to the local and remote event bus
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postGlobal(Object event) {
        postRemote(event);
        post(event);
    }

    /**
     * Posts the given sticky event (object) to the local and remote event bus
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postStickyGlobal(Object event) {
        postStickyRemote(event);
        postSticky(event);
    }

    /**
     * Remove and gets the recent sticky event for the given event type, on local and remote event bus
     *
     * @param eventType
     * @param <T>
     * @return
     */
    public <T> T removeStickyEventGlobal(Class<T> eventType) {
        removeStickyEventRemote(eventType);
        return removeStickyEvent(eventType);
    }

    /**
     * Removes the sticky event if it equals to the given event, on local and remote event bus
     *
     * @return true if the events matched and the sticky event was removed.
     */
    public boolean removeStickyEventGlobal(Object event) {
        removeStickyEventRemote(event);
        return removeStickyEvent(event);
    }

    /**
     * Removes all sticky events, on local and remote event bus
     */
    public void removeAllStickyEventsGlobal() {
        removeAllStickyEventsRemote();
        removeAllStickyEvents();
    }

    /******************** Local Bus Synchronization Methods ************************/

    /**
     * Will take a MessageEvent from GooglePlayServices and attempt to parse WearEventBus data to sync with the local EventBus
     *
     * @param messageEvent
     */
    public void syncEvent(@NonNull MessageEvent messageEvent) {
        byte[] objectArray = messageEvent.getData();

        int indexClassDelimiter = messageEvent.getPath().lastIndexOf(WearBusTools.CLASS_NAME_DELIMITER);
        String className = messageEvent.getPath().substring(indexClassDelimiter + 1);

        boolean isEventMessage = messageEvent.getPath().contains(WearBusTools.MESSAGE_PATH) || messageEvent.getPath().contains(WearBusTools.MESSAGE_PATH_STICKY);

        if (isEventMessage) {

            //Try simple types (String, Integer, Long...)
            Object obj = WearBusTools.getSendSimpleObject(objectArray, className);

            if (obj == null) {
                //Find corresponding parcel for particular object in local receivers
                obj = findParcel(objectArray, className);
            }

            if (obj != null) {

                boolean isSticky = messageEvent.getPath().contains(WearBusTools.MESSAGE_PATH_STICKY);

                //send them to local bus
                if (isSticky) {
                    postSticky(obj);
                } else {
                    post(obj);
                }
            }

        } else if (messageEvent.getPath().contains(WearBusTools.MESSAGE_PATH_COMMAND)) {

            //Commands used for managing sticky events.
            stickyEventCommand(context, messageEvent, objectArray, className);
        }
    }

    /**
     * Method used to find which command and if class/object is needed to retrieve it and call local method.
     *
     * @param context
     * @param messageEvent
     * @param objectArray
     */
    private void stickyEventCommand(@NonNull Context context, @NonNull MessageEvent messageEvent, @NonNull byte[] objectArray, @NonNull String className) {

        if (className.equals(String.class.getName())) {

            String action = new String(objectArray);
            Log.d(WearBusTools.BUSWEAR_TAG, "syncEvent action: " + action);

            if (action.equals(WearBusTools.ACTION_STICKY_CLEAR_ALL)) {

                removeAllStickyEvents();

            } else {

                //Even if it was String it should be removeStickyEventLocal instead of all, it is due to fact that action key is sent as a String.
                Class classTmp = getClassForName(className);
                removeStickyEvent(classTmp);

            }

        } else {

            if (messageEvent.getPath().startsWith(WearBusTools.PREFIX_CLASS)) {

                //Call removeStickyEventLocal so first retrieve class that needs to be removed.
                Class classTmp = getClassForName(className);
                removeStickyEvent(classTmp);

            } else {

                //Call removeStickyEventLocal so first retrieve object that needs to be removed.
                Object obj = WearBusTools.getSendSimpleObject(objectArray, className);

                if (obj == null) {
                    //Find corresponding parcel for particular object in local receivers
                    obj = findParcel(objectArray, className);
                }

                if (obj != null) {
                    removeStickyEvent(obj);
                }
            }
        }
    }

    /**
     * Attempts to locate the class specified by className to instantiate with the given objectArray
     *
     * @param objectArray
     * @param className
     * @return
     */
    private Object findParcel(@NonNull byte[] objectArray, @NonNull String className) {
        try {
            Class classTmp = getClassForName(className);

            Constructor declaredConstructor = classTmp.getDeclaredConstructor(Parcel.class);
            declaredConstructor.setAccessible(true);

            return declaredConstructor.newInstance(WearBusTools.byteToParcel(objectArray));

        } catch (Exception e) {
            Log.d(WearBusTools.BUSWEAR_TAG, "syncEvent error: " + e.getMessage());
        }

        return null;
    }

    @Nullable
    private Class getClassForName(@NonNull String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.d(WearBusTools.BUSWEAR_TAG, "syncEvent error: " + e.getMessage());
            return null;
        }
    }

    private void sendEventOverGooglePlayServices(Object event, boolean isSticky) {

        //Try to parse it for sending and then send it
        byte[] objectInArray = WearBusTools.parseToSend(event);

        try {
            new SendByteArrayToNode(objectInArray, event.getClass(), context, isSticky).start();
        } catch (Exception e) {
            Log.e(WearBusTools.BUSWEAR_TAG, "Object cannot be sent: " + e.getMessage());
        }
    }
}
