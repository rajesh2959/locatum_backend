(function () {
    'use strict';

    angular
        .module('app')
        .factory('venuedataservice', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};
        svc.venueID=0;

        svc.saveVenueDetails = function (venuDetails) {
            if (venuDetails) {
                if (venuDetails.id) {
                    return dataService.post('/web/site/save?sid=' + venuDetails.id, venuDetails);
                }
            }
            return dataService.post('/web/site/save?sid=', venuDetails);
        };

        svc.getVenueList = function () {
            return dataService.getRecord('/rest/site/list?cid=' + session.cid);
        };

        svc.getVenueListForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getDataListForTable('/rest/geofence/alert/list?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        svc.getVenueDetailById = function (venuId) {
            return dataService.getRecord('/rest/site/get?id='+venuId);
        };

        svc.getVenueDetailCardCound = function (type, sid) {
            return dataService.getRecord('/rest/site/portion/networkdevice/gatewayMetrics?type=' + type + '&cid=' +session.cid+ '&sid=' + sid);
        };

        svc.getVenueRecentAlert = function (sid) {
            return dataService.getRecord('/rest/beacon/ble/networkdevice/gateway_alerts?sid=' + sid + "&cid=" + session.cid);
        };

        svc.deleteVenueDetails = function (venuDetailId) {
            return dataService.post('/rest/site/delete?sid=' + venuDetailId);
        };              

        svc.setCurrentVenuId = function (sid) {
            svc.venueID = sid;
        };

        svc.getCurrentVenuId = function (sid) {
            return svc.venueID;
        };

        return svc;

    }

})();