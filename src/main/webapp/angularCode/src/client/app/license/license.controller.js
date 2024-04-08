(function () {
    'use strict';
    angular
        .module('app.license')
        .controller('licenseController', controller);
    controller.$inject = ['licenseService', 'navigation', 'SimpleListScreenViewModel', 'notificationBarService', 'modalService', '$timeout', '$linq', 'session', 'environment'];

    /* @ngInject */
    function controller(licenseService, navigation, SimpleListScreenViewModel, notificationBarService, modalService, $timeout, $linq, session, environment) {
    	var vm = new SimpleListScreenViewModel();
        vm.dataOperationsLicenseCustomers = new SimpleListScreenViewModel();

        vm.refresh = function (refresh) {
            vm.getlicenseCustomersList(refresh);
        };

        vm.getlicenseCustomersList = function (refresh) {
        	licenseService.getActiveCustomersListForTable(refresh, vm.dataOperationsLicenseCustomers.dataOperations, vm.dataOperationsLicenseCustomers.filterFn).then(function (result) {
                debugger
                vm.licenseCustomerList = result.allData;
                vm.pagedDataLicense = result.pagedData;
                vm.dataOperationsLicenseCustomers.fullCount = result.dataCount;
                vm.dataOperationsLicenseCustomers.filteredCount = result.filteredDataCount;
            });

        };
        vm.goToLicensePage = function () {
            if (parseInt(vm.goToLicensePageNumber) > vm.dataOperationsLicenseCustomers.totalPageCount) {
                vm.goToLicensePageNumber = '';
            } else {
                vm.dataOperationsLicenseCustomers.dataOperations.paging.currentPage = vm.goToLicensePageNumber;
                vm.getlicenseCustomersList();
                vm.goToLicensePageNumber = '';
            }
        };
        vm.onDeactivationStatus = function (id, deactivationStatus) {
            var deactivate = {};
            deactivate.id = id;
            deactivate.flag = deactivationStatus;
            modalService.questionModal('Confirmation', 'Are you sure you want to change the  status?').result.then(function () {
            	licenseService.updateDeactivationStatus(deactivate).then(function (result) {
                    if (result && result.success) {
                    	vm.refresh(true);
                        notificationBarService.success(res.body);
                        
                    } else
                        notificationBarService.error(res.body);
                });
            },
            function () {
            	deactivationStatus = ! deactivationStatus;
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