(function () {
    'use strict';

    angular
        .module('app')
        .factory('gatewaystatusService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};

        svc.getGwayInfoListforTable = function (refresh, dataOperations, filterFn) {
            return dataService.getDataListForTable('/rest/beacon/trilaterationReports/deviceInfo?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        svc.getGwayHistoryListForTable = function (refresh, dataOperations, filterFn,time) {
            if(time == undefined){
                time = "1d";
            }
            return dataService.getDataListForTable('/rest/beacon/ble/networkdevice/finder_Device_crash_info?cid=' + session.cid +'&time='+time, refresh, dataOperations, filterFn);
        };

        svc.crashDump = function (fileName) {
            return dataService.postData('/rest/beacon/ble/networkdevice/CrashDumpFileDownload?fileName=' + fileName, null);
        };

        return svc;
    }
})();