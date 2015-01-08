BusWear - EventBus for Android Wear
=======

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/pl.tajchert/buswear/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/pl.tajchert/buswear)

![BusWear logo](https://raw.githubusercontent.com/tajchert/BusWear/master/mobile/src/main/res/drawable-xxxhdpi/ic_launcher.png)

BusWear (:bus::watch:) is a simple library for EventBus to support Android Wear devices. Just adding one line of code lets you get synchronized event buses on Wear and mobile platform.

![Diagram](https://raw.githubusercontent.com/tajchert/BusWear/master/diagram_simple.png)

###What is EventBus?

A great multi-purpose tool for Android apps, great way of triggering some events in separate Activity, Fragment, Service etc.

[EventBus, origin of that project](https://github.com/greenrobot/EventBus) or [Otto](https://github.com/square/otto).

###How to start?

To start with BusWear all you need is to add a dependency. That is it!

###Add BusWear to your project

Gradle:
```gradle
    //library:
    compile 'pl.tajchert:buswear:0.9.4'
    //needed dependency:
    compile 'com.google.android.gms:play-services-wearable:+'
```

Maven:
```xml
<dependency>
    <groupId>pl.tajchert</groupId>
    <artifactId>buswear</artifactId>
    <version>0.9.4</version>
</dependency>
```

[Maven Central Link](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22pl.tajchert%22%20AND%20a%3A%22buswear%22)


###How to use?

You can post to remote branch as long as it `String, Integer, Long, Short, Float, Double` or custom object that `implements Parcelable`, other "non-Parcelable" objects still can be posted but only locally.


`post(object, context);` sends your parcelable object (or `String, Integer`...) both to local bus and to remote one as well.

`postLocal(object)` works as old `post()` of EventBus, it sends event only locally.

`postRemote(object, context)` sends your parcelable object (or `String, Integer`...) to remote bus only.

The same goes for **Sticky events** - so you get `postSticky()`, `postStickyLocal()`, `postStickyRemote()`. However not all "sticky" functionality is supported yet - ex. `removeStickyEvent()` is not implemented (TODO), and works only locally.


###Sample

To send:

```java
EventBus.getDefault().post(parcelableObject, this);     //Custom parcelable object
EventBus.getDefault().post("text", this);               //String
EventBus.getDefault().post(1.0f, this);                 //Float
//... similar with Integer, Long etc.
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


###Questions?

**How is that better than classic EventBus?**

_EventBus works on mobile na Android Wear - yes, but you got two separate buses, and BusWear gives you a feel of one bus that is shared/synchronized between those two devices._

**Why does it uses whole code of EventBus instead of "extends EventBus"?**

_As it overrides some private methods to get for example subscribed method classes of parameters for unparcelling objects after receiving them in Parcel. If that problem will be resolved I will be glad to use EventBus as dependency._

**What are some drawbacks?**

_Probably quite big one is that all your custom objects to be posted needs to implement `Parcelable` (or be `String, Integer...`). I recommend using for that purpose some library such as Parceler, Hrisey or Auto-Parcel for this._

_Other one is that you cannot have classes with same name in the same module (for example "wear") - it will lead to errors as matching is done on SimpleName of class._

###License

BusWear binaries and source code can be used according to the [Apache License, Version 2.0](LICENSE).

###Thanks

Goes to [Polidea](https://www.polidea.com/) for putting me on a project that encouraged me to work on that library, Maciej Górski for Manifest merger, and Dariusz Seweryn for idea with Class name in path String.
