# Cordova Service Discovery

Simple plugin to get any SSDP / UPnP / DLNA service on a local network

## Using
Clone the plugin

    $ git clone https://github.com/scottdermott/cordova-plugin-discovery.git

Create a new Cordova Project

    $ cordova create myApp com.example.myApp MyApp

Add Android platform

    cordova platform add android
    cordova platform add ios
    
Install the plugin

    $ cd myApp
    $ cordova plugin add ../cordova-plugin-discovery
    

Edit `www/js/index.js` and add the following code inside `onDeviceReady`

```js
    var serviceType = "ssdp:all";
    
    var success = function(devices) {
        console.log(devices);
    }
    
    var failure = function() {
        alert("Error calling Service Discovery Plugin");
    }
    
    /**
	 * Similar to the W3C specification for Network Service Discovery api 'http://www.w3.org/TR/discovery-api/'
	 * @method getNetworkServices
	 * @param {String} serviceType e.g. "urn:schemas-upnp-org:service:ContentDirectory:1", "ssdp:all", "urn:schemas-upnp-org:service:AVTransport:1"
	 * @param {Function} success callback an array of services
	 * @param {Function} failure callback 
	*/
    serviceDiscovery.getNetworkServices(serviceType, success, failure);
```


Run the code

    cordova run android
    cordova run ios

## Supported Platforms
- Android
- iOS
