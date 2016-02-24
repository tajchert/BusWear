package org.greenrobot.eventbus;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;

import java.lang.reflect.Constructor;

import pl.tajchert.buswear.wear.SendByteArrayToNode;
import pl.tajchert.buswear.wear.WearBusTools;

/**
 * TODO: Add a class header comment!
 */
public class WearEventBus extends EventBus {

    public static WearEventBus getDefault(@NonNull Context context) {
        if (defaultInstance == null) {
            synchronized (EventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new WearEventBus(context);
                }
            }
        }
        return (WearEventBus) defaultInstance;
    }

    private final Context context;

    public WearEventBus(@NonNull Context context) {
        this.context = context.getApplicationContext();
    }

    /******************** Remote Bus Methods ************************/

    /**
     * Posts the given event (object) to the remote event bus only
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postRemote(Object event) {
        sendEventOverGooglePlayServices(event);
        post(event);
    }

    /**
     * Posts the given sticky event (object) to the remote event bus only
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postStickyRemote(Object event) {
        //TODO make a sticky send
        sendEventOverGooglePlayServices(event);
        postSticky(event);
    }

    /**
     * Remove and gets the recent sticky event for the given event type, on the remote event bus only
     *
     * @param eventType
     * @param <T>
     * @return
     */
    public <T> T removeStickyEventRemote(Class<T> eventType) {
        //TODO remove remote sticky
        return removeStickyEvent(eventType);
    }

    /**
     * Removes the sticky event if it equals to the given event, on the remote event bus only
     *
     * @return true if the events matched and the sticky event was removed.
     */
    public boolean removeStickyEventRemote(Object event) {
        //TODO remove remote sticky
        return removeStickyEvent(event);
    }

    /**
     * Removes all sticky events, on the remote event bus only
     */
    public void removeAllStickyEventsRemote() {
        //TODO remove remote sticky
        removeAllStickyEvents();
    }

    /******************** Global Bus Methods ************************/

    /**
     * Posts the given event (object) to the local and remote event bus
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postGlobal(Object event) {
        post(event);
    }

    /**
     * Posts the given sticky event (object) to the local and remote event bus
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postStickyGlobal(Object event) {
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
        return removeStickyEvent(eventType);
    }

    /**
     * Removes the sticky event if it equals to the given event, on local and remote event bus
     *
     * @return true if the events matched and the sticky event was removed.
     */
    public boolean removeStickyEventGlobal(Object event) {
        return removeStickyEvent(event);
    }

    /**
     * Removes all sticky events, on local and remote event bus
     */
    public void removeAllStickyEventsGlobal() {
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

        if (messageEvent.getPath().contains(WearBusTools.MESSAGE_PATH)) {

            //TODO cannot take last index of . anymore, fully qualified class names are required
            String className = messageEvent.getPath().substring(messageEvent.getPath().lastIndexOf(".") + 1);

            //Try simple types (String, Integer, Long...)
            Object obj = WearBusTools.getSendSimpleObject(objectArray, className);
            if (obj == null) {
                //Find corresponding parcel for particular object in local receivers
                obj = findParcel(objectArray, className);
            }

            if (obj != null) {
                //send them to local bus
                post(obj);
            }

        } else if (messageEvent.getPath().contains(WearBusTools.MESSAGE_PATH_STICKY)) {

            //Catch sticky events
            //TODO cannot take last index of . anymore, fully qualified class names are required
            String className = messageEvent.getPath().substring(messageEvent.getPath().lastIndexOf(".") + 1);
            //Try simple types (String, Integer, Long...)
            Object obj = WearBusTools.getSendSimpleObject(objectArray, className);

            if (obj == null) {
                //Find corresponding parcel for particular object in local receivers
                obj = findParcel(objectArray, className);
            }

            if (obj != null) {
                //send them to local bus
                postSticky(obj);
            }

        } else if (messageEvent.getPath().contains(WearBusTools.MESSAGE_PATH_COMMAND)) {

            //Commands used for managing sticky events.
            stickyEventCommand(context, messageEvent, objectArray);
        }
    }

    /**
     * Method used to find which command and if class/object is needed to retrieve it and call local method.
     *
     * @param context
     * @param messageEvent
     * @param objectArray
     */
    private void stickyEventCommand(@NonNull Context context, @NonNull MessageEvent messageEvent, @NonNull byte[] objectArray) {

        //TODO cannot take last index of . anymore, fully qualified class names are required
        String className = messageEvent.getPath().substring(messageEvent.getPath().lastIndexOf(".") + 1);

        if (className.equals(String.class.getName())) {

            String action = new String(objectArray);
            Log.d(WearBusTools.BUSWEAR_TAG, "syncEvent action: " + action);

            if (action.equals(WearBusTools.ACTION_STICKY_CLEAR_ALL)) {
                removeAllStickyEvents();
            } else {

                //Even if it was String it should be removeStickyEventLocal instead of all, it is due to fact that action key is send as a String.
                Class classTmp = getClassForName(className);
                removeStickyEvent(classTmp);

            }

        } else {

            //TODO cannot take last index of . anymore, fully qualified class names are required
            int dotPlace = messageEvent.getPath().lastIndexOf(".");
            String typeOfRemove = messageEvent.getPath().substring(dotPlace - 5, dotPlace);
            if (typeOfRemove.equals("class")) {

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
     * Searches through the classList to locate the class to instantiate with the given objectArray
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

    private void sendEventOverGooglePlayServices(Object event) {

        //Try to parse it for sending and then send it
        byte[] objectInArray = WearBusTools.parseToSend(event);

        try {
            new SendByteArrayToNode(objectInArray, event.getClass(), context, false).start();
        } catch (Exception e) {
            Log.e(WearBusTools.BUSWEAR_TAG, "Object cannot be sent: " + e.getMessage());
        }
    }
}
