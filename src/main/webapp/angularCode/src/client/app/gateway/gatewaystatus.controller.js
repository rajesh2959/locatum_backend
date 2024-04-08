(function () {
    'use strict';
    angular
        .module('app.gateway')
        .controller('gatewaystatusController', controller);
    controller.$inject = ['gatewaystatusService', 'navigation', 'SimpleListScreenViewModel', 'notificationBarService', 'modalService', '$linq', 'session', 'environment'];
    /* @ngInject */

    function controller(gatewaystatusService, navigation, SimpleListScreenViewModel, notificationBarService, modalService, $linq, session, environment) {
        var vm = new SimpleListScreenViewModel();
        vm.dataOperationsGwayInfo = new SimpleListScreenViewModel();
        vm.dataOperationsGwayHistory = new SimpleListScreenViewModel();
        //vm.isViewHistory = false;
        vm.selectedFilereCount = 4;
        vm.dataOperationsGwayInfo.dataOperations.paging.pageSize = vm.selectedFilereCount;
        vm.girdFilterList = [
            { "key": 5, "value": 5 },
            { "key": 10, "value": 10 },
            { "key": 25, "value": 25 },
            { "key": 100, "value": 100 }
        ];

        vm.getGatewayInfos = function (refresh) {
            gatewaystatusService.getGwayInfoListforTable(refresh, vm.dataOperationsGwayInfo.dataOperations, vm.dataOperationsGwayInfo.filterFn)
                .then(function (result) {
                    vm.allGatewayInfos = result.allData;
                    vm.pagedDataGatewayInfo = result.pagedData;
                    vm.dataOperationsGwayInfo.fullCount = result.dataCount;
                    vm.dataOperationsGwayInfo.filteredCount = result.filteredDataCount;
                });
        };

        vm.getGatewayHistory = function (refresh,time) {
            gatewaystatusService.getGwayHistoryListForTable(refresh, vm.dataOperationsGwayHistory.dataOperations, vm.dataOperationsGwayHistory.filterFn,time)
                .then(function (result) {
                    vm.allGatewayHistory = result.allData;
                    vm.pagedDataGatewayHistory = result.pagedData;
                    vm.dataOperationsGwayHistory.fullCount = result.dataCount;
                    vm.dataOperationsGwayHistory.filteredCount = result.filteredDataCount;
                });
        };

        $('.durationhistory').on("change",function(){
            var time = $('.durationhistory').val();
             vm.getGatewayHistory('true',time)
            //console.log("duration" + time)
        })

        vm.goToGatewayInfoPage = function () {
            if (parseInt(vm.goToGatewayInfoPageNo) > vm.dataOperationsGwayInfo.totalPageCount) {
                vm.goToGatewayInfoPageNo = '';
            } else {
                vm.dataOperationsGwayInfo.dataOperations.paging.currentPage = vm.goToGatewayInfoPageNo;
                vm.getGatewayInfos();
                vm.goToGatewayInfoPageNo = '';
            }
        };

        vm.goToServerPage = function () {
            if (parseInt(vm.goToServerPageNo) > vm.dataOperationsGwayHistory.totalPageCount) {
                vm.goToServerPageNo = '';
            } else {
                vm.dataOperationsGwayHistory.dataOperations.paging.currentPage = vm.goToServerPageNo;
                vm.getGatewayHistory();
                vm.goToServerPageNo = '';
            }
        };

        vm.crashDump = function (item) {

            gatewaystatusService.crashDump(item.fileName).then(function (result) {
                if (result && result.success) {
                    vm.getGatewayInfos(true);
                }
                notificationBarService.success(result.body);
            });
        };

        // vm.viewHistory = function () {
        //     vm.isViewHistory = !vm.isViewHistory;
        // };

        vm.selectedFilterChange = function (selectedCount) {
            vm.dataOperationsGwayInfo.dataOperations.paging.pageSize = selectedCount;
            vm.getGatewayInfos(false);
        }

        vm.statushistory = function(uid){
            navigation.goToGatewayStatusHistory(uid);
        }

        function activate() {
            vm.getGatewayInfos(true);
            vm.getGatewayHistory(true);
        }

        activate();

        return vm;
    }
})();