/*global cordova, module*/
 function httpGet(url) {
    var xmlHttp = null;
    xmlHttp = new XMLHttpRequest();
    xmlHttp.open("GET",url,false);
    xmlHttp.send(null);
    return xmlHttp.responseText;
}
function getConfig(devices, callback){ 
    for (var i = 0; i < devices.length; i++) {
        devices[i].config = httpGet(devices[i].LOCATION);
        devices[i].url = "http://"+devices[i].LOCATION.split("/")[2];
    }
    callback(devices);
}
module.exports = {
    getNetworkServices: function (service, addConfigXML, successCallback, errorCallback) {
    	var processResponse = function (data) {
    		if(addConfigXML) {
    			getConfig(data, function(devices){
    				successCallback(devices);
    			});
	    	} else {
	    		successCallback(data);
	    	}
    	};
        cordova.exec(processResponse, errorCallback, "serviceDiscovery", "getNetworkServices", [service]);
    }
};
