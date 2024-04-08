(function () {
    'use strict';
    angular
        .module('app.upgrade')
        .controller('upgradeHistoryController', controller);
    controller.$inject = ['upgradedatahistoryservice','messagingService', 'notificationBarService', '$q', 'modalService', 'navigation','SimpleListScreenViewModel','$rootScope', '$scope','$timeout'];

    /* @ngInject */
    function controller(upgradedatahistoryservice,messagingService, notificationBarService, $q, modalService, navigation,SimpleListScreenViewModel, $rootScope, $scope,$timeout) {
       
         var vm = new SimpleListScreenViewModel();
        vm.dataOperationsGwayInfo = new SimpleListScreenViewModel();
        vm.selectedFilereCount = 5;
        vm.dataOperationsGwayInfo.dataOperations.paging.pageSize = vm.selectedFilereCount;
        vm.girdFilterList = [
            { "key": 5, "value": 5 },
            { "key": 10, "value": 10 },
            { "key": 25, "value": 25 },
            { "key": 100, "value": 100 }
        ];
 angular.element(document).ready(function(refresh){
       // vm.getGatewayInfos = function (refresh) {
            upgradedatahistoryservice.getHistoryList(refresh, vm.dataOperationsGwayInfo.dataOperations, vm.dataOperationsGwayInfo.filterFn)
                .then(function (result) {
                    console.log("result data" + JSON.stringify(result));
                    vm.allGatewayInfos = result.allData;
                    vm.pagedDataGatewayInfo = result.pagedData;
                    vm.dataOperationsGwayInfo.fullCount = result.dataCount;
                    vm.dataOperationsGwayInfo.filteredCount = result.filteredDataCount;
                });
        
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
    
        vm.selectedFilterChange = function (selectedCount) {
            vm.dataOperationsGwayInfo.dataOperations.paging.pageSize = selectedCount;
            vm.getGatewayInfos(false);
        }





        return vm;
    
    }

})();