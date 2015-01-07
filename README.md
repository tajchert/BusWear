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

BusWear is available on Maven Central.

Gradle:
```
    compile 'pl.tajchert:buswear:0.9.3'
    compile 'com.google.android.gms:play-services-wearable:+'
```

Maven:
```
<dependency>
    <groupId>pl.tajchert</groupId>
    <artifactId>buswear</artifactId>
    <version>0.9.3</version>
</dependency>
```

[Maven Central Link](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22pl.tajchert%22%20AND%20a%3A%22buswear%22)


###How to use?

You can post to remote branch as long as it `implements Parcelable`, other "non-Parcelable" object you still can post but only locally.


`post(parcelableObject, context);` sends your parcelable object both to local bus and to remote one as well.

`postLocal(object)` works as old `post()` of EventBus, it sends event only locally.

`postRemote(parcelableObject, context)` sends your parcelable object to remote bus only.

The same goes for **Sticky events** - so you get `postSticky()`, `postStickyLocal()`, `postStickyRemote()`. However not all "sticky" functionality is supported yet - ex. `removeStickyEvent()` is not implemented (TODO), and works only locally.


###Sample

To send:

`EventBus.getDefault().post(parcelableObject, this);`

To receive:
```
@Override
protected void onCreate(Bundle savedInstanceState) {
    //...
    EventBus.getDefault().register(this);
}

//Called every time when post() is ParcelableObject as an object
public void onEvent(ParcelableObject parcelableObject){
    //Do your stuff with that object
}
```


###Questions?

**Why does it uses whole code of EventBus instead of "extends EventBus"?**

_As it overrides some private methods to get for example subscribed method classes of parameters for unparcelling objects after receiving them in Parcel. If that problem will be resolved I will be glad to use EventBus as dependency._

**What are some drawbacks?**

_Probably quite big one is that all your objects to be posted needs to implement `Parcelable`. I recommend using for that purpose some library such as Parceler, Hrisey or Auto-Parcel for this._

_Other one is that you cannot have classes with same name in the same module (for example "wear") - it will lead to errors as matching is done on SimpleName of class._

###License

BusWear binaries and source code can be used according to the [Apache License, Version 2.0](LICENSE).

###Thanks

Goes to [Polidea](https://www.polidea.com/) for putting me on a project that encouraged me to work on that library, Maciej Górski for Manifest merger, and Dariusz Seweryn for idea with Class name in path String.
