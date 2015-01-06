BusWear - EventBus for Android Wear
=======
![BusWear logo](https://raw.githubusercontent.com/tajchert/BusWear/master/mobile/src/main/res/drawable-xxxhdpi/ic_launcher.png)

BusWear is simple library of EventBus to support Android Wear devices. Just adding one line of code lets you get synchronized event buses on Wear and mobile platform.

![Diagram](https://raw.githubusercontent.com/tajchert/BusWear/master/diagram.png)

##Usage
To start with BusWear you need to add `EventBus.syncEvent(messageEvent);` in your `onMessageReceived()` method, both in mobile and wear projects. If you don't have one add a new Service such as:
```java
public class MessagesCatcher extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        EventBus.syncEvent(messageEvent);
    }
}
```
And define it in your `Manifest.xml` file:
```xml
<service android:name=".MessagesCatcher">
  <intent-filter>
    <action android:name="com.google.android.gms.wearable.BIND_LISTENER" />
  </intent-filter>
</service>
```

If you don't do this events will be send from device but other will not receive it.

This is all you need to do to start sending objects back and forth!

###Lets start posting!

`post(parcelableObject, context);` sends your parcelable object both to local bus and to remote one as well.

`postLocal(object)` works as old `post()` of EventBus, it sends event only locally.

`postRemote(parcelableObject, context)` sends your parcelable object to remote bus only.

The same goes for Sticky events - so you get `postSticky()`, `postStickyLocal()`, `postStickyRemote()`. However not all "sticky" functionality is supported yet - ex. `removeStickyEvent()` is not implemented (TODO), and works only locally.

###Questions?

**Will it be on the Maven?**

_Yes, it is matter of days._

**Why does it uses whole code of EventBus instead of "extends EventBus"?**

_As it overrides some private methods to get for example subscribed method classes of parameters for unparcelling objects after receiving them in Parcel. If that problem will be resolved I will be glad to use less EventBus as dependency._

**What are some drawbacks?**

_Probably quite big one is that all your objects to be posted needs to implement `Parcelable`. I recommend using for that purpose some library such as Parceler or Auto-Parcel for this._

###License

BusWear binaries and source code can be used according to the [Apache License, Version 2.0](LICENSE).


