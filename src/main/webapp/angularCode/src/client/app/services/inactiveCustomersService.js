(function () {
    'use strict';

    angular
        .module('app')
        .factory('inactiveCustomersService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session, notificationBarService) {

        var svc = {};

        svc.getInactiveListForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getInactiveCustomersListForTable('/rest/customer/inactive', refresh, dataOperations, filterFn);
        };

        svc.updateActivationStatus = function (activate) {
            return dataService.postData('/rest/customer/active', activate);
        };
        
        return svc;
    }

})();
