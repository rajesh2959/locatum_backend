(function () {
    'use strict';
    angular
        .module('app')
        .controller('InactiveAlertsController', controller);
    controller.$inject = ['inactiveAlertsService', 'environment', 'session', 'SimpleListScreenViewModel'];

    /* @ngInject */
    function controller(inactiveAlertsService, env, session, SimpleListScreenViewModel) {
        var vm = {};
        var baseUrl = env.serverBaseUrl;
        var pagewidth = screen.width;
        vm.dataOperationsInactivityAlerts = new SimpleListScreenViewModel();
        vm.dataOperationsBatteryAlerts = new SimpleListScreenViewModel();
        vm.dataOperationsGatewayAlerts = new SimpleListScreenViewModel();

        vm.getInactivityAlerts = function (refresh) {
            inactiveAlertsService.getinactiveAlertsListForTable(refresh, vm.dataOperationsInactivityAlerts.dataOperations, vm.dataOperationsInactivityAlerts.filterFn)
                .then(function (result) {
                    vm.allInactivityAlerts = result.allData;
                    vm.pagedDataInactivityAlerts = result.pagedData;
                    vm.dataOperationsInactivityAlerts.fullCount = result.dataCount;
                    vm.dataOperationsInactivityAlerts.filteredCount = result.filteredDataCount;
                });
        };

        vm.getBatteryAlerts = function (refresh) {
            inactiveAlertsService.getbatteryAlertsListForTable(refresh, vm.dataOperationsBatteryAlerts.dataOperations, vm.dataOperationsBatteryAlerts.filterFn)
                .then(function (result) {
                    vm.allBatteryAlerts = result.allData;
                    vm.pagedDataBatteryAlerts = result.pagedData;
                    vm.dataOperationsBatteryAlerts.fullCount = result.dataCount;
                    vm.dataOperationsBatteryAlerts.filteredCount = result.filteredDataCount;
                });
        };

        vm.getGatewayAlerts = function (refresh) {
            inactiveAlertsService.getgatewayAlertsListForTable(refresh, vm.dataOperationsGatewayAlerts.dataOperations, vm.dataOperationsGatewayAlerts.filterFn)
                .then(function (result) {
                    vm.allGatewayAlerts = result.allData;
                    vm.pagedDataGatewayAlerts = result.pagedData;
                    vm.dataOperationsGatewayAlerts.fullCount = result.dataCount;
                    vm.dataOperationsGatewayAlerts.filteredCount = result.filteredDataCount;
                });
        };

        vm.export = function () {
            var url = "";
            if (vm.selectedFile == "PDF") {
                url = baseUrl + "/rest/gatewayreport/gw_alertpdf?cid=" + session.cid;
            } else {
                url = baseUrl + "/rest/gatewayreport/gw_alertcsv?cid=" + session.cid;
            }
            window.open(url);
        };

        vm.refresh = function (refresh) {
            vm.getInactivityAlerts(refresh);
            vm.getBatteryAlerts(refresh);
            vm.getGatewayAlerts(refresh);
        };

        vm.refreshInactivityAlerts = function (refresh) {
            vm.getInactivityAlerts(refresh);
        };

        vm.refreshBatteryAlerts = function (refresh) {
            vm.getBatteryAlerts(refresh);
        };

        vm.refreshGatewayAlerts = function (refresh) {
            vm.getGatewayAlerts(refresh);
        };

        vm.goToGatewayPage = function () {
            if (parseInt(vm.goToGatewayPageNumber) > vm.dataOperationsGatewayAlerts.totalPageCount) {
                vm.goToGatewayPageNumber = '';
            } else {
                vm.dataOperationsGatewayAlerts.dataOperations.paging.currentPage = vm.goToGatewayPageNumber;
                vm.getGatewayAlerts();
                vm.goToGatewayPageNumber = '';
            }
        };

        vm.goToBatteryPage = function () {
            if (parseInt(vm.goToBatteryPageNumber) > vm.dataOperationsBatteryAlerts.totalPageCount) {
                vm.goToBatteryPageNumber = '';
            } else {
                vm.dataOperationsBatteryAlerts.dataOperations.paging.currentPage = vm.goToBatteryPageNumber;
                vm.getBatteryAlerts();
                vm.goToBatteryPageNumber = '';
            }
        };

        vm.goToInactivityPage = function () {
            if (parseInt(vm.goToInactivityPageNumber) > vm.dataOperationsInactivityAlerts.totalPageCount) {
                vm.goToInactivityPageNumber = '';
            } else {
                vm.dataOperationsInactivityAlerts.dataOperations.paging.currentPage = vm.goToInactivityPageNumber;
                vm.getInactivityAlerts();
                vm.goToInactivityPageNumber = '';
            }
        };

        function activate() {
            vm.refresh(true);
        }

        activate();

        return vm;
    }
})();