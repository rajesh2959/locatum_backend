(function () {
    'use strict';

    angular
        .module('app')
        .factory('roleService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};

        svc.getRoleListForTable = function (spid, refresh, dataOperations, filterFn) {
           return dataService.getRoleDataListForTable('/rest/role/list', refresh, dataOperations, filterFn);
        };

        return svc;
    }

})();
