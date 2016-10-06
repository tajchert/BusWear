/*
 * Copyright (C) 2015 Michal Tajchert (http://tajchert.pl), Polidea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.tajchert.buswear;

import android.content.Context;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.reflect.Constructor;

import pl.tajchert.buswear.wear.SendByteArrayToNode;
import pl.tajchert.buswear.wear.SendCommandToNode;
import pl.tajchert.buswear.wear.WearBusTools;

/**
 * EventBus is a central publish/subscribe event system for Android. Events are posted ({@link #post(Object)}) to the
 * bus, which delivers it to subscribers that have a matching handler method for the event type. To receive events,
 * subscribers must register themselves to the bus using {@link #register(Object)}. Once registered,
 * subscribers receive events until {@link #unregister(Object)} is called. By convention, event handling methods must
 * have the "@Subscribe" annotation, be public, return nothing (void), and have exactly one parameter (the event).
 *
 *@author Michal Tajchert, Polidea
 * Author of EventBus (90% of that code) Markus Junginger, greenrobot
 */
public class EventBus {

    private static EventBus defaultInstance;

    public static EventBus getDefault(@NonNull Context context) {
        if (defaultInstance == null) {
            synchronized (EventBus.class) {
                if (defaultInstance == null) {
                    defaultInstance = new EventBus(context.getApplicationContext());
                }
            }
        }
        return defaultInstance;
    }

    private final Context context;
    private final org.greenrobot.eventbus.EventBus eventBus;

    public EventBus(@NonNull Context context) {
        this(context, org.greenrobot.eventbus.EventBus.getDefault());
    }

    public EventBus(@NonNull Context context, @NonNull org.greenrobot.eventbus.EventBus eventBus) {
        this.context = context.getApplicationContext();
        this.eventBus = eventBus;
    }

    /******************** Greenrobot Proxy Methods ************************/

    /**
     * Registers the given subscriber to receive events. Subscribers must call {@link #unregister(Object)} once they
     * are no longer interested in receiving events.
     * <p/>
     * Subscribers have event handling methods that must be annotated by {@link Subscribe}.
     * The {@link Subscribe} annotation also allows configuration like {@link
     * ThreadMode} and priority.
     */
    public void register(Object subscriber) {
        eventBus.register(subscriber);
    }

    /** Unregisters the given subscriber from all event classes. */
    public void unregister(Object subscriber) {
        eventBus.unregister(subscriber);
    }

    public boolean isRegistered(Object subscriber) {
        return eventBus.isRegistered(subscriber);
    }

    /**
     * Called from a subscriber's event handling method, further event delivery will be canceled. Subsequent
     * subscribers
     * won't receive the event. Events are usually canceled by higher priority subscribers (see
     * {@link Subscribe#priority()}). Canceling is restricted to event handling methods running in posting thread
     * {@link ThreadMode#POSTING}.
     */
    public void cancelEventDelivery(Object event) {
        eventBus.cancelEventDelivery(event);
    }

    /**
     * Checks if the given event has any subscribers. This will only check the local bus and will not detect
     * remote subscribers, consider posting remote in that case.
     * @param eventClass
     * @return
     */
    public boolean hasSubscriberForEvent(Class<?> eventClass) {
        return eventBus.hasSubscriberForEvent(eventClass);
    }

    /******************** Local Bus Methods ************************/

    /**
     * Posts the given event (object) to the local event bus only
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postLocal(Object event) {
        eventBus.post(event);
    }

    /**
     * Posts the given sticky event (object) to the local event bus only
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postStickyLocal(Object event) {
        eventBus.postSticky(event);
    }

    /**
     * Remove and gets the recent sticky event for the given event type, on the local event bus only
     *
     * @param <T>
     * @param eventType
     * @return
     */
    public <T> T removeStickyEventLocal(Class<T> eventType) {
        return eventBus.removeStickyEvent(eventType);
    }

    /**
     * Removes the sticky event if it equals to the given event, on the local event bus only
     *
     * @return true if the events matched and the sticky event was removed.
     */
    public boolean removeStickyEventLocal(Object event) {
        return eventBus.removeStickyEvent(event);
    }

    /**
     * Removes all sticky events, on the local event bus only
     */
    public void removeAllStickyEventsLocal() {
        eventBus.removeAllStickyEvents();
    }

    /**
     * Gets the most recent sticky event for the given type, on the local event bus only
     *
     * @see #postSticky(Object)
     */
    public <T> T getStickyEventLocal(Class<T> eventType) {
        return eventBus.getStickyEvent(eventType);
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
    public void post(Object event) {
        postRemote(event);
        postLocal(event);
    }

    /**
     * Posts the given sticky event (object) to the local and remote event bus
     *
     * @param event any kind of Object, no restrictions.
     */
    public void postSticky(Object event) {
        postStickyRemote(event);
        postStickyLocal(event);
    }

    /**
     * Remove and gets the recent sticky event for the given event type, on local and remote event bus
     *
     * @param eventType
     * @param <T>
     * @return
     */
    public <T> T removeStickyEvent(Class<T> eventType) {
        removeStickyEventRemote(eventType);
        return removeStickyEventLocal(eventType);
    }

    /**
     * Removes the sticky event if it equals to the given event, on local and remote event bus
     *
     * @return true if the events matched and the sticky event was removed.
     */
    public boolean removeStickyEvent(Object event) {
        removeStickyEventRemote(event);
        return removeStickyEventLocal(event);
    }

    /**
     * Removes all sticky events, on local and remote event bus
     */
    public void removeAllStickyEvents() {
        removeAllStickyEventsRemote();
        removeAllStickyEventsLocal();
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
                    postStickyLocal(obj);
                } else {
                    postLocal(obj);
                }
            }

        } else if (messageEvent.getPath().contains(WearBusTools.MESSAGE_PATH_COMMAND)) {

            //Commands used for managing sticky events.
            stickyEventCommand(messageEvent, objectArray, className);
        }
    }

    /**
     * Method used to find which command and if class/object is needed to retrieve it and call local method.
     *
     * @param messageEvent
     * @param objectArray
     */
    private void stickyEventCommand(@NonNull MessageEvent messageEvent, @NonNull byte[] objectArray, @NonNull String className) {

        if (className.equals(String.class.getName())) {

            String action = new String(objectArray);
            Log.d(WearBusTools.BUSWEAR_TAG, "syncEvent action: " + action);

            if (action.equals(WearBusTools.ACTION_STICKY_CLEAR_ALL)) {

                removeAllStickyEventsLocal();

            } else {

                //Even if it was String it should be removeStickyEventLocal instead of all, it is due to fact that action key is sent as a String.
                Class classTmp = getClassForName(className);
                removeStickyEventLocal(classTmp);

            }

        } else {

            if (messageEvent.getPath().startsWith(WearBusTools.PREFIX_CLASS)) {

                //Call removeStickyEventLocal so first retrieve class that needs to be removed.
                Class classTmp = getClassForName(className);
                removeStickyEventLocal(classTmp);

            } else {

                //Call removeStickyEventLocal so first retrieve object that needs to be removed.
                Object obj = WearBusTools.getSendSimpleObject(objectArray, className);

                if (obj == null) {
                    //Find corresponding parcel for particular object in local receivers
                    obj = findParcel(objectArray, className);
                }

                if (obj != null) {
                    removeStickyEventLocal(obj);
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
