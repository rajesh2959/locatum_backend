(function () {
    'use strict';

    angular
        .module('app')
        .factory('statushistoryService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};

        svc.getGwayList = function (refresh, dataOperations, filterFn) {
            return dataService.getDataListForTable('/rest/beacon/device/receiver?cid=' + session.cid ,refresh, dataOperations, filterFn);
        };

        svc.gatewayActiveTagTypesChartinfo = function (multipush, time , dataOperations, refresh) {
            return dataService.getDataListForTable('/rest/beacon/ble/networkdevice/gatewaystatushistory?uid='+ multipush+"&time="+time,refresh, dataOperations);
        };
       

        return svc;
    }
})();