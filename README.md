BusWear - EventBus for Android Wear
=======

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/pl.tajchert/buswear/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/pl.tajchert/buswear)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-BusWear-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/1336)

![BusWear logo](https://raw.githubusercontent.com/tajchert/BusWear/master/mobile/src/main/res/drawable-xxxhdpi/ic_launcher.png)

BusWear (:bus::watch:) is a simple library for EventBus to support Android Wear devices. Just adding one line of code lets you get synchronized event buses on Wear and mobile platform.

![Diagram](https://raw.githubusercontent.com/tajchert/BusWear/master/docs/diagram_simple.png)

###What is EventBus?

A great multi-purpose tool for Android apps, way of triggering some events in separate Activity, Fragment, Service etc. [EventBus, origin of that project](https://github.com/greenrobot/EventBus) or [Otto](https://github.com/square/otto).

###How to start?

To start with BusWear all you need is to add a dependency and include it in your manifest files.

###Add BusWear to your project

Gradle:
```gradle
    //library:
    compile 'pl.tajchert:buswear:0.9.5'
    //needed dependency:
    compile 'com.google.android.gms:play-services-wearable:+'
```

Maven:
```xml
<dependency>
    <groupId>pl.tajchert</groupId>
    <artifactId>buswear</artifactId>
    <version>0.9.5</version>
</dependency>
```

[Maven Central Link](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22pl.tajchert%22%20AND%20a%3A%22buswear%22)

For standard behavior add the following service to both your handheld and wearable manifest files. You can extend EventCatcher to add additional behavior to the EventCatcher.

```xml
<service android:name="pl.tajchert.buswear.wear.EventCatcher">
    <intent-filter>
        <action android:name="com.google.android.gms.wearable.BIND_LISTENER" />
    </intent-filter>
</service>
```

###How to use?

You can post to remote branch as long as it `String, Integer, Long, Short, Float, Double` or custom object that `implements Parcelable`, other "non-Parcelable" objects still can be posted but only locally.


`post(object, context);` sends your parcelable object (or `String, Integer`...) both to local bus and to remote one as well.

`postLocal(object)` works as old `post()` of EventBus, it sends event only locally.

`postRemote(object, context)` sends your parcelable object (or `String, Integer`...) to remote bus only.

The same goes for **Sticky events** - so you get `postSticky()`, `postStickyLocal()`, `postStickyRemote()`. Also methods such `removeStickyEvent(Object)`, `removeStickyEvent(Class)`, `removeAllStickyEvents()` work in same manner - you get everywhere, remote, local flavours of each method.


###Sample

To send:

```java
EventBus.getDefault().post(parcelableObject, this);     //Custom parcelable object
EventBus.getDefault().postRemote("text", this);         //String
//... similar with Integer, Long etc.
EventBus.getDefault().postLocal('c', this);            //Character - to local function you can pass any object that you like
```

To receive:
```java
protected void onCreate(Bundle savedInstanceState) {
    EventBus.getDefault().register(this);
}

//Called every time when post() is send (with that particular object), needs to be named "onEvent(ObjectType)"
public void onEvent(ParcelableObject parcelableObject){
    //Do your stuff with that object
}

public void onEvent(String text){
    //Do your stuff with that object
}

//... more onEvent() if you want!
```

###Event propagation

<img src="docs/diagram_post.png" width="600" height="361"/>
------------------------
<img src="docs/diagram_postremote.png" width="600" height="361"/>
------------------------
<img src="docs/diagram_postlocal.png" width="600" height="361"/>

###Questions?

**How is that better than classic EventBus?**

_EventBus works on mobile na Android Wear - yes, but you got two separate buses, and BusWear gives you a feel of one bus that is shared/synchronized between those two devices._

**Why does it uses whole code of EventBus instead of "extends EventBus"?**

_As it overrides some private methods to get for example subscribed method classes of parameters for unparcelling objects after receiving them in Parcel. If that problem will be resolved I will be glad to use EventBus as dependency._

**What are some drawbacks?**

_Probably quite big one is that all your custom objects to be posted needs to implement `Parcelable` (or be `String, Integer...`)._

_Other one is that you cannot have classes with same name in the same module (for example "wear") - it will lead to errors as matching is done on SimpleName of class._

###License

BusWear binaries and source code can be used according to the [Apache License, Version 2.0](LICENSE).

###Thanks

Goes to [Polidea](https://www.polidea.com/) for putting me on a project that encouraged me to work on that library, Maciej Górski for Manifest merger, and Dariusz Seweryn for idea with Class name in path String.
