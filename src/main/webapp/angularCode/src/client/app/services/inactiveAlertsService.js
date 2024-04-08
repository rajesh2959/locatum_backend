(function () {
    'use strict';

    angular
        .module('app')
        .factory('inactiveAlertsService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};

        svc.getinactiveAlertsListForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getDataInactiveAlertsListForTable('/rest/beacon/ble/networkdevice/inactivetags?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        svc.getbatteryAlertsListForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getDataBatteryAlertsListForTable('/rest/beacon/ble/networkdevice/beaconbattery?cid=' + session.cid + '&level=40', refresh, dataOperations, filterFn);
        };

        svc.getgatewayAlertsListForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getDataGatewayAlertsListForTable('/rest/beacon/ble/networkdevice/beacondevicealert?cid=' + session.cid, refresh, dataOperations, filterFn);
        };
        return svc;
    }
})();