(function () {
    'use strict';
    angular
        .module('app')
        .controller('RoleController', controller);
    controller.$inject = ['roleService', 'messagingService', 'notificationBarService', 'SimpleListScreenViewModel', '$q', 'modalService', 'navigation', '$rootScope'];
    /* @ngInject */

    function controller(roleService, messagingService, notificationBarService, SimpleListScreenViewModel, $q, modalService, navigation, $rootScope) {
        var vm = new SimpleListScreenViewModel();

        vm.roleList = [
            { "gridsno":"1","admintypes": "Superadmin", "customer": "Read/Write","site":"Read/Write","system":"Read/Write","account":"Read/Write" },
            { "gridsno":"2","admintypes": "Appadmin", "customer": "Not Allowed","site":"Read/Write","system":"Read/Write","account":"Read/Write" },
            { "gridsno":"3","admintypes": "Siteadmin", "customer": "Not Allowed","site":"Read/Write","system":"Read","account":"Read" },
            { "gridsno":"4","admintypes": "Sysadmin", "customer": "Not Allowed","site":"Read","system":"Read/Write","account":"Read" },
            { "gridsno":"5","admintypes": "Useradmin", "customer": "Read","site":"Read","system":"Read","account":"Read/Write" },
            { "gridsno":"6","admintypes": "User", "customer": "Not Allowed","site":"Read","system":"Read","account":"Read" },
        ];

        vm.getData = function (refresh) {
        //    roleService.getRoleListForTable(refresh, vm.dataOperations, vm.filterFn).then(function (result) {
        //             vm.allData = result.data;
        //             vm.pagedData = result.data;
        //        });

        vm.pagedData = vm.roleList
        };

        vm.goToPage = function () {
            if (parseInt(vm.goToPageNumber) > vm.totalPageCount) {
                vm.goToPageNumber = '';
            } else {
                vm.dataOperations.paging.currentPage = vm.goToPageNumber;
                vm.getData();
                vm.goToPageNumber = '';
            }
        };

        function activate() {
            vm.getData(true);
        };

        activate();
        return vm;
    }
})();
