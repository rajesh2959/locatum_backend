(function () {
    'use strict';

    angular
        .module('app')
        .factory('geoFenceService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};
                
        svc.getByGeoFenceId = function (id) {
            return dataService.getRecord('/rest/geofence/view?id=' + id);
        };

        svc.getGeoFenceListForTable = function (spid, refresh, dataOperations, filterFn) {
            return dataService.getDataListForTable('/rest/geofence/list?spid=' + spid, refresh, dataOperations, filterFn);
        };

        svc.saveFence = function (fenceDetails) {
            return dataService.postData('/rest/geofence/save', fenceDetails);
        };

        svc.deleteFence = function (fences) {
            return dataService.postData('/rest/geofence/delete', fences);
        };

        svc.getGeoFenceList = function (spid) {
            return dataService.getRecord('/rest/geofence/list?spid=' + spid);
        };

         svc.getAllGeoFenceIds = function () {
             return dataService.getRecord('/rest/geofence/list?cid=' + session.cid);
         };

        svc.getAllGeoFenceList = function (venuIdLst, floorIdLst, geofenceLst) {
            return dataService.postData('/rest/geofence/filter/list?cid=' + session.cid + '&status=enabled&sid=' + venuIdLst + '&spid=' + floorIdLst + '&fence=' + geofenceLst);
        };
        
        return svc;
    }

})();
