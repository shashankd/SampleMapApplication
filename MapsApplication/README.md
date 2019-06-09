## Maps Sample Application

git clone the repository
```
git clone git@github.com:shashankd/SampleMapApplication.git
```

The Android project has minimum sdk version 16 and targets to api level 28

Open the project in Android Studio

Configure customised api key from 

[https://console.developers.google.com/flows/enableapi?apiid=maps_android_backend&keyType=CLIENT_SIDE_ANDROID]

Put apikey in src/debug/res/values/google_maps_api.xml

Enable the following services for the apikey 
* Places API
* Maps SDK for Android

Run the application on device/emulator in debug mode
 