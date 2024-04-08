(function () {
    'use strict';

    angular
        .module('app')
        .factory('reportService', service);

    service.$inject = ['dataService', 'session', 'notificationBarService'];

    function service(dataService, session, notificationBarService) {

        var svc = {};

        svc.getReportListForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getDataListForTable('/rest/report/dashboard/list?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        svc.getReportById = function (id) {
            //return ["90", "99", "80", "91", "76", "75", "60", "67", "59", "55"];
            return dataService.getRecord('/rest/report/dashboard/view?id=' + id);
        };


        svc.getMultipleVisualdatabyId = function (ids,fromDate,toDate) {
            var route = '/rest/report/visuals/bulkReportPreview?from=' + fromDate + '&to=' + toDate;
            return dataService.postData(route, ids);
        };


        svc.getVisualdatabyId = function (id, fromDate, toDate) {
            //5c616375ee8da049388b71b9&from=&to=
            return dataService.getRecord('/rest/report/visuals/reportPreview?id=' + id + '&from=' + fromDate + '&to=' + toDate);
        };

        svc.save = function (data) {
            data.cid = session.cid;
            var route = '/rest/report/dashboard/save';
            return dataService.postData(route, data);
        };

        svc.delete = function (ids) {
            return dataService.postData('/rest/report/dashboard/delete', ids);
        };

        return svc;
    }

})();