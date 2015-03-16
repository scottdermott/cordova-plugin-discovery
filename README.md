# Cordova Android Service Discovery

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
	 * @param {Boolean} gets the config.xml from the device
	 * @param {Function} success callback an array of services
	 * @param {Function} failure callback 
	*/
    serviceDiscovery.getNetworkServices(serviceType, true, success, failure);
```


Run the code

    cordova run android

