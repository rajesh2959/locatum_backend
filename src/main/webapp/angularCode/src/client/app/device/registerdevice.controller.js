(function () {
    'use strict';
    angular
        .module('app.gateway')
        .controller('registerdeviceController', controller);
    controller.$inject = ['registerDeviceService', 'navigation', 'SimpleListScreenViewModel', 'notificationBarService', 'modalService', '$linq'];
    /* @ngInject */

    function controller(registerDeviceService, navigation, SimpleListScreenViewModel, notificationBarService, modalService, $linq) {
        var vm = new SimpleListScreenViewModel();
        vm.dataOperationsGwayInfo = new SimpleListScreenViewModel();

        vm.getDevices = function (refresh) {
            registerDeviceService.getReceiverListforTable(refresh, vm.dataOperationsGwayInfo.dataOperations, vm.dataOperationsGwayInfo.filterFn)
                .then(function (result) {
                    vm.allReceiverDetails = result.allData;
                    vm.pagedDataReceiverDetails = result.pagedData;
                    vm.dataOperationsGwayInfo.fullCount = result.dataCount;
                    vm.dataOperationsGwayInfo.filteredCount = result.filteredDataCount;
                });
        };

        vm.refresh = function (refresh) {
            vm.getDevices(true);
        };

        vm.edit = function (item) {
            navigation.goToAdddevice(0, item.mac_address, 1, 'registerdevice');
        };

        vm.delete = function (item) {
            modalService.confirmDelete('Are you sure you want to delete the gateway?').result.then(
                function () {
                    registerDeviceService.delete(item.mac_address).then(function (result) {
                        if (result && result.success) {
                            vm.getDevices(true);
                        }
                        notificationBarService.success(result.body);
                    });
                },
                function () {
                });
        };

        vm.deleteAll = function () {
            modalService.confirmDelete('Are you sure you want to delete all registered gateways?').result.then(
                function () {
                    registerDeviceService.deleteAll().then(function (result) {
                        if (result && result.success) {
                            vm.getDevices(true);
                        }
                        notificationBarService.success(result.body);
                    });
                },
                function () {
                });
        }

        vm.goToNextPage = function () {
            if (parseInt(vm.goToGwayPageNo) > vm.dataOperationsGwayInfo.totalPageCount) {
                vm.goToGwayPageNo = '';
            } else {
                vm.dataOperationsGwayInfo.dataOperations.paging.currentPage = vm.goToGwayPageNo;
                vm.getDevices();
                vm.goToGwayPageNo = '';
            }
        };

        function activate() {
            vm.refresh(true);
        }

        activate();

        return vm;
    }
})();