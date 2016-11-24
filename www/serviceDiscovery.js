/*global cordova, module*/
module.exports = {
    getNetworkServices: function (service, successCallback, errorCallback) {
    	var processResponse = function (data) {
	    	successCallback(data);
    	};
        cordova.exec(processResponse, errorCallback, "serviceDiscovery", "getNetworkServices", [service]);
    }
};
