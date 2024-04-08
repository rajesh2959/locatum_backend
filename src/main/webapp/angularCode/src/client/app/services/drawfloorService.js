(function () {
    'use strict';
    angular
        .module('app')
        .factory('drawfloorservice', service);
    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};
        svc.getParamValue = function () {
            return dataService.getRecord('/rest/customer/paramValue?cid=' + session.cid);
        };

        svc.getFloorData = function (spid) {
            return dataService.getRecord('/rest/site/portion/open?spid=' +spid);
        };

        svc.saveFloorPlan = function (data) {
            return dataService.postMultipart('/rest/site/portion/floorplan/save',data);
        };

        return svc;
    }
})();
