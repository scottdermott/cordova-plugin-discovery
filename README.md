# Cordova Android Service Discovery Plugin

Simple plugin to get any SSDP / UPnP / DLNA service on a local network

## Using
Clone the plugin

    $ git clone https://.git

Create a new Cordova Project

    $ cordova create myApp com.example.myApp MyApp

Add Android platform

    cordova platform add android
    
Install the plugin

    $ cd myApp
    $ cordova plugin install ../cordova-plugin-discovery
    

Edit `www/js/index.js` and add the following code inside `onDeviceReady`

```js
    var success = function(devices) {
        console.log(devices);
    }

    var failure = function() {
        alert("Error calling Service Discovery Plugin");
    }

    serviceDiscovery.getNetworkServices(serviceTypes, true, success, failure);
```


Run the code

    cordova run android

