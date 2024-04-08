(function () {
    'use strict';

    angular
        .module('app')
        .factory('customizedAlertService', service);

    service.$inject = ['dataService', 'session', 'venuesession'];

    function service(dataService, session, venuesession) {

        var svc = {};
        svc.getInactivityInfo = function () {
            return dataService.getRecord('/rest/customer/getInactivityInfo?cid=' + session.cid, true);
        };

        svc.getTagTypes = function () {
            return dataService.getRecord('/rest/customer/getTagTypes?cid=' + session.cid, true);
        };

        svc.getBasedTagNames = function (tagType) {
            return dataService.getRecord('/rest/beaconAlertData/typeBasedTagNames?cid=' + session.cid + '&type=' + tagType, true);
        };

        svc.getFloorList = function () {
            return dataService.getRecord('/rest/beacon/trilaterationReports/floorlist?cid=' + session.cid + "&sid=" + venuesession.sid, true);
        };

        svc.getInactivityType = function (floor) {
            return dataService.getRecord('/rest/beaconAlertData/inactivityType?cid=' + session.cid + '&inactivityType=' + floor, true);
        };

        svc.updateInactivityInfo = function (inactivityData) {
            return dataService.postData('/rest/customer/updateInactivityInfo', inactivityData);
        };

        svc.saveAlertInfo = function (alertData) {
            return dataService.postData('/rest/beaconAlertData/save', alertData);
        };

        svc.deleteConfigAlert = function (id) {
            return dataService.delete('/rest/beaconAlertData/delete?id=' + id);
        };

        svc.getConfigAlert = function () {
            return dataService.getRecord('/rest/beaconAlertData/list?cid=' + session.cid, true);
        };

        svc.getConfigAlertForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getDataListForTable('/rest/beaconAlertData/list?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        return svc;
    }
})();
