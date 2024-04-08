(function () {
    'use strict';

    angular
        .module('app')
        .factory('supportService', service);

    service.$inject = ['dataService', 'session', 'notificationBarService'];

    function service(dataService, session, notificationBarService) {

        var svc = {};

        svc.getSupportforTable = function (refresh, dataOperations, filterFn) {
            return dataService.getDataListForTable('/rest/customer/supportDetails', refresh, dataOperations, filterFn);
            //return dataService.getTaglistfortable('/rest/customer/supportDetails', refresh, dataOperations, filterFn);
        };

        svc.getAccessSupportforTable = function (refresh, dataOperations, filterFn) {
            return dataService.getAccesslistfortable('/rest/customer/supportlist', refresh, dataOperations, filterFn);
        };

        svc.updateSupportStatus = function (support) {
            return dataService.postData('/rest/customer/emailsupport', support);
        };

        svc.updateQubercommSupport = function (support) {
            return dataService.postData('/rest/customer/support', support);
        };


        return svc;
    }

})();