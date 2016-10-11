/*global cordova, module*/
module.exports = {
    getNetworkServices: function (service, addConfigXML, successCallback, errorCallback) {
    	var processResponse = function (data) {
	    	successCallback(data);
	    }
    	};
        cordova.exec(processResponse, errorCallback, "serviceDiscovery", "getNetworkServices", [service]);
    }
};
