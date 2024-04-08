(function () {
    'use strict';

    angular
        .module('app')
        .factory('licenseService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session, notificationBarService) {

        var svc = {};

        svc.getActiveCustomersListForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getLicenseCustomersListForTable('/rest/customer/licence', refresh, dataOperations, filterFn);
        };
        
        svc.updateDeactivationStatus = function (deactivate) {
            return dataService.postData('/rest/customer/deactivate', deactivate);
        };

        return svc;
    }

})();
