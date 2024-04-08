(function () {
    'use strict';

    angular
        .module('app')
        .factory('gatewayinfoService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {
        var svc = {};  
        svc.gatewayinfo = function (uid) {
            return dataService.getRecord('/rest/beacon/ble/networkdevice/getintf?uid=' + uid);
        };

        svc.gatewayCPUUtilizationinfo = function (uid) {
            return dataService.getRecord('/rest/beacon/ble/networkdevice/getcpu?uid=' + uid);
        };

        svc.gatewayMemoryUtilizationinfo = function (uid) {
            return dataService.getRecord('/rest/beacon/ble/networkdevice/getmem?uid=' + uid);
        };

        svc.gatewaybleTagsActivityChartinfo = function (uid) {
            return dataService.getRecord('/rest/beacon/ble/networkdevice/rxtx?uid=' + uid);
        };
        

        svc.gatewayActiveTagsInfo = function (uid,cid, spid) {
            return dataService.getRecord('/rest/beacon/ble/networkdevice/getpeers?uid=' + uid + '&cid=' + cid + '&spid=' + spid);
        };
        svc.gatewayActiveTagTypesChartinfo = function (uid,cid) {
            return dataService.getRecord('/rest/beacon/ble/networkdevice/venue/connectedTagType?uid=' + uid+ '&cid=' + cid);
        };
        svc.gatewayBleChartinfo = function (uid) {
            return dataService.getRecord('/rest/beacon/ble/networkdevice/rxtx?uid=' + uid);
        };
        
        

        
        return svc;
    }
})();