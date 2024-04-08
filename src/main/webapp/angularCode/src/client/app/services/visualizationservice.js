(function () {
    'use strict';

    angular
        .module('app')
        .factory('visualizationService', service);

    service.$inject = ['dataService', 'session', 'notificationBarService'];

    function service(dataService, session, notificationBarService) {

        var svc = {};

        svc.getVisualListForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getDataListForTable('/rest/report/visuals/list?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        svc.getVisual = function (id) {
           return dataService.getRecord('/rest/report/visuals/view?id=' + id);
        };

        svc.preview = function (preview, from, to) {
            preview.cid = session.cid;
            var route = '/rest/report/visuals/preview?from=' + from + '&to=' + to;
            console.log("route:-- " + route);
            console.log("Request:-- " + JSON.stringify(preview));
            return dataService.postData(route, preview);
        };

        svc.save = function (data, from, to) {
            data.cid = session.cid;
            var route = '/rest/report/visuals/save?from=' + from + '&to=' + to;
            return dataService.postData(route, data);
        };

        svc.delete = function (ids) {
            return dataService.postData('/rest/report/visuals/delete', ids);
        };

        return svc;
    }

})();