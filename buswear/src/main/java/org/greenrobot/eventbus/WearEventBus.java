package org.greenrobot.eventbus;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;

import java.lang.reflect.Constructor;

import pl.tajchert.buswear.wear.LocalEventWrapper;
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

    /**
     * Posts the given event (object) to the local event bus only
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postLocal(Object event) {
        post(new LocalEventWrapper(event));
    }


    /**
     * Posts the given sticky event (object) to the local event bus only
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postStickyLocal(Object event) {
        postSticky(new LocalEventWrapper(event));
    }

    /**
     * Remove and gets the recent sticky event for the given event type, only on local bus.
     * @param eventType
     * @param <T>
     * @return
     */
    public <T> T removeStickyEventLocal(Class<T> eventType) {
        //TODO match old BusWear
        return removeStickyEvent(eventType);
    }

    /**
     * Removes the sticky event if it equals to the given event, only on local bus.
     *
     * @return true if the events matched and the sticky event was removed.
     */
    public boolean removeStickyEventLocal(Object event) {
        return removeStickyEvent(event);
    }

    /**
     * Removes all sticky events, only on local bus.
     */
    public void removeAllStickyEventsLocal() {
        removeAllStickyEvents();
    }

    @Override
    void invokeSubscriber(Subscription subscription, Object event) {
        if (event instanceof LocalEventWrapper) {

            LocalEventWrapper localEventWrapper = (LocalEventWrapper) event;
            super.invokeSubscriber(subscription, localEventWrapper.getEvent());

        } else {

            //Try to parse it for sending and then send it
            byte[] objectInArray = WearBusTools.parseToSend(event);

            try {
                new SendByteArrayToNode(objectInArray, event.getClass(), context, false).start();
            } catch (Exception e) {
                Log.e(WearBusTools.BUSWEAR_TAG, "Object cannot be sent: " + e.getMessage());
            }

            super.invokeSubscriber(subscription, event);
        }
    }

    public static void syncEvent(@NonNull Context context, @NonNull MessageEvent messageEvent) {
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
                getDefault(context).postLocal(obj);
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
                getDefault(context).postStickyLocal(obj);
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
    private static void stickyEventCommand(@NonNull Context context, @NonNull MessageEvent messageEvent, @NonNull byte[] objectArray) {

        //TODO cannot take last index of . anymore, fully qualified class names are required
        String className = messageEvent.getPath().substring(messageEvent.getPath().lastIndexOf(".") + 1);

        if (className.equals(String.class.getName())) {

            String action = new String(objectArray);
            Log.d(WearBusTools.BUSWEAR_TAG, "syncEvent action: " + action);

            if (action.equals(WearBusTools.ACTION_STICKY_CLEAR_ALL)) {
                getDefault(context).removeAllStickyEventsLocal();
            } else {

                //Even if it was String it should be removeStickyEventLocal instead of all, it is due to fact that action key is send as a String.
                Class classTmp = getClassForName(className);
                getDefault(context).removeStickyEventLocal(classTmp);

            }

        } else {

            //TODO cannot take last index of . anymore, fully qualified class names are required
            int dotPlace = messageEvent.getPath().lastIndexOf(".");
            String typeOfRemove = messageEvent.getPath().substring(dotPlace - 5, dotPlace);
            if (typeOfRemove.equals("class")) {

                //Call removeStickyEventLocal so first retrieve class that needs to be removed.
                Class classTmp = getClassForName(className);
                getDefault(context).removeStickyEventLocal(classTmp);

            } else {
                //Call removeStickyEventLocal so first retrieve object that needs to be removed.
                Object obj = WearBusTools.getSendSimpleObject(objectArray, className);

                if (obj == null) {
                    //Find corresponding parcel for particular object in local receivers
                    obj = findParcel(objectArray, className);
                }
                if (obj != null) {
                    getDefault(context).removeStickyEventLocal(obj);
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
    private static Object findParcel(@NonNull byte[] objectArray, @NonNull String className) {
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
    private static Class getClassForName(@NonNull String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            Log.d(WearBusTools.BUSWEAR_TAG, "syncEvent error: " + e.getMessage());
            return null;
        }
    }
}
