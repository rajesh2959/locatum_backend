(function () {
    'use strict';

    angular
        .module('app')
        .factory('registerDeviceService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};

        svc.getReceiverListforTable = function (refresh, dataOperations, filterFn) {
            return dataService.getGwayReceiverListfortable('/rest/beacon/device/registered?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        svc.getServerListForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getGwayServerListfortable('/rest/beacon/device/server?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        svc.delete = function (uid) {
            return dataService.delete('/rest/beacon/device/ibeacondelete?uid=' + uid, null);
        };

        svc.deleteAll = function () {
            return dataService.postData('/rest/beacon/regdevice/deleteall', null);
        };

        return svc;
    }

})();