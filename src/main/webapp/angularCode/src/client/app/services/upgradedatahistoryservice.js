(function () {
    'use strict';

    angular
        .module('app')
        .factory('upgradedatahistoryservice', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};
     
         svc.getHistoryList = function (refresh, dataOperations, filterFn) {
            return dataService.getDataListForTable('/rest/beacon/device/upgradeHistory?cid='+ session.cid, refresh, dataOperations, filterFn);
        };

        return svc;

    }

})();
