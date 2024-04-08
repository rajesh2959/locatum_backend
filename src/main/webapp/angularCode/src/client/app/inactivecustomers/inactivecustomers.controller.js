(function () {
    'use strict';
    angular
        .module('app.inactivecustomers')
        .controller('InactivecustomersController', controller);
    controller.$inject = ['inactiveCustomersService', 'navigation', 'SimpleListScreenViewModel', 'notificationBarService', 'modalService', '$timeout', '$linq', 'session', 'environment'];

    /* @ngInject */
    function controller(inactiveCustomersService, navigation, SimpleListScreenViewModel, notificationBarService, modalService, $timeout, $linq, session, environment) {
    	var vm = new SimpleListScreenViewModel();
        vm.dataOperationsInactiveCustomers = new SimpleListScreenViewModel();

        vm.refresh = function (refresh) {
            vm.getInactiveCustomersList(refresh);
        };

        vm.getInactiveCustomersList = function (refresh) {
        	inactiveCustomersService.getInactiveListForTable(refresh, vm.dataOperationsInactiveCustomers.dataOperations, vm.dataOperationsInactiveCustomers.filterFn).then(function (result) {
                debugger
                vm.inactiveCustomerList = result.allData;
                console.log(vm.inactiveCustomerList);
                vm.pagedDataInactiveCustomers = result.pagedData;
                vm.dataOperationsInactiveCustomers.fullCount = result.dataCount;
                vm.dataOperationsInactiveCustomers.filteredCount = result.filteredDataCount;
            });

        };
        vm.goToInactiveCustomersPage = function () {
            if (parseInt(vm.goToInactiveCustomersPageNumber) > vm.dataOperationsInactiveCustomers.totalPageCount) {
                vm.goToInactiveCustomersPageNumber = '';
            } else {
                vm.dataOperationsInactiveCustomers.dataOperations.paging.currentPage = vm.goToInactiveCustomersPageNumber;
                vm.getInactiveCustomersList();
                vm.goToInactiveCustomersPageNumber = '';
            }
        };
        vm.onActivationStatus = function (id, activationStatus) {
            var active = {};
            active.customerId = id;
            active.flag = activationStatus;
            modalService.questionModal('Confirmation', 'Are you sure you want to change the  status?').result.then(function () {
            	inactiveCustomersService.updateActivationStatus(active).then(function (result) {
                    if (result && result.success) {
                    	vm.refresh(true);
                        notificationBarService.success(res.body);
                        
                    } else
                        notificationBarService.error(res.body);
                });
            },
            function () {
            	activationStatus = ! activationStatus;
            	activate();
            });
        };
        function activate() {
            vm.refresh(true);
        };
        activate();
        return vm;
    }
})();