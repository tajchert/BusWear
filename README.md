BusWear - EventBus for Android Wear
=======

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/pl.tajchert/buswear/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/pl.tajchert/buswear)

![BusWear logo](https://raw.githubusercontent.com/tajchert/BusWear/master/mobile/src/main/res/drawable-xxxhdpi/ic_launcher.png)

BusWear (:bus::watch:) is a simple library for EventBus to support Android Wear devices. Just adding one line of code lets you get synchronized event buses on Wear and mobile platform.

![Diagram](https://raw.githubusercontent.com/tajchert/BusWear/master/diagram.png)

##What is EventBus?

A great multi-purpose tool for Android apps, great way of triggering some events in separate Activity, Fragment, Service etc.

[EventBus, origin of that project](https://github.com/greenrobot/EventBus) or [Otto](https://github.com/square/otto).

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

###Add BusWear to your project

BusWear is available on Maven Central.

Gradle:
```
    compile 'pl.tajchert:buswear:0.9.0'
```

Maven:
```
<dependency>
    <groupId>pl.tajchert</groupId>
    <artifactId>buswear</artifactId>
    <version>0.9.0</version>
</dependency>
```

[Maven Central Link](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22pl.tajchert%22%20AND%20a%3A%22buswear%22)

###Lets start posting!

`post(parcelableObject, context);` sends your parcelable object both to local bus and to remote one as well.

`postLocal(object)` works as old `post()` of EventBus, it sends event only locally.

`postRemote(parcelableObject, context)` sends your parcelable object to remote bus only.

The same goes for Sticky events - so you get `postSticky()`, `postStickyLocal()`, `postStickyRemote()`. However not all "sticky" functionality is supported yet - ex. `removeStickyEvent()` is not implemented (TODO), and works only locally.

###Questions?

**Why does it uses whole code of EventBus instead of "extends EventBus"?**

_As it overrides some private methods to get for example subscribed method classes of parameters for unparcelling objects after receiving them in Parcel. If that problem will be resolved I will be glad to use less EventBus as dependency._

**What are some drawbacks?**

_Probably quite big one is that all your objects to be posted needs to implement `Parcelable`. I recommend using for that purpose some library such as Parceler or Auto-Parcel for this._

###License

BusWear binaries and source code can be used according to the [Apache License, Version 2.0](LICENSE).


