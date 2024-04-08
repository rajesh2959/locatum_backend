(function () {
    'use strict';

    angular
        .module('app')
        .factory('geoFenceAlertService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};

        svc.getGeofenceAlertsBySpid = function (cid, sid, spid) {
            return dataService.getRecord('/rest/geofence/alert/list?cid=' + cid + '&sid=' + sid + '&spid=' + spid);
        };

        svc.getGeofenceAlert = function (id) {
            return dataService.getRecord('/rest/geofence/alert/view?id=' + id);
        };

        svc.saveFenceAlerts = function (alerts) {
            return dataService.postData('/rest/geofence/alert/save?cid=' + session.cid, alerts);
        };

        svc.deleteAlerts = function (alerts) {
            return dataService.postData('/rest/geofence/alert/delete', alerts);
        };

        return svc;
    }

})();
