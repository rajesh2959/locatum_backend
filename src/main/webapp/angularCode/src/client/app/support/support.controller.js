(function () {
    'use strict';
    angular
        .module('app')
        .controller('SupportController', controller);
    controller.$inject = ['supportService', 'messagingService', 'notificationBarService', 'SimpleListScreenViewModel', '$q', 'modalService', 'navigation', '$rootScope', 'adminHomeService', 'userService'];
    /* @ngInject */

    function controller(supportService, messagingService, notificationBarService, SimpleListScreenViewModel, $q, modalService, navigation, $rootScope, adminHomeService, userService) {
        var vm = new SimpleListScreenViewModel();
        vm.dataOperationsSupport = new SimpleListScreenViewModel();
        vm.dataOperationAccess = new SimpleListScreenViewModel();
        vm.isSuperAdmin = false;

        vm.credentials = {
            host: '',
            port: '',
            emailID: '',
            password: ''
        };
        

        getUserRole();
        function getUserRole() {

            userService.getprofile().then(function (result) {

                if(result.role == 'superadmin'){
                    vm.isSuperAdmin = true;
                    
                }else{
                    vm.isSuperAdmin = false;
                }
                
            });
        };
        
        vm.refresh = function (refresh) {
            vm.getSupport(refresh);
            vm.getAccessSupport(refresh);
        };


        vm.getSupport = function (refresh) {
            supportService.getSupportforTable(refresh, vm.dataOperationsSupport.dataOperations, vm.dataOperationsSupport.filterFn)
                .then(function (result) {
                    vm.allSupportList = result.allData;
                    vm.pagedDataSupport = result.pagedData;
                    vm.dataOperationsSupport.fullCount = result.dataCount;
                    vm.dataOperationsSupport.filteredCount = result.filteredDataCount;
                });
        };


        vm.getAccessSupport = function (refresh) {
            supportService.getAccessSupportforTable(refresh, vm.dataOperationAccess.dataOperations, vm.dataOperationAccess.filterFn).then(function (result) {
                vm.accessSupportList = result.allData;
                vm.pagedDataaccess = result.pagedData;
                vm.dataOperationAccess.fullCount = result.dataCount;
                vm.dataOperationAccess.filteredCount = result.filteredDataCount;
            });

        };

        vm.goToSupportPage = function () {
            if (parseInt(vm.goToSupportPageNumber) > vm.dataOperationsSupport.totalPageCount) {
                vm.goToSupportPageNumber = '';
            } else {
                vm.dataOperationsSupport.dataOperations.paging.currentPage = vm.goToSupportPageNumber;
                vm.getSupport();
                vm.goToSupportPageNumber = '';
            }
        };

        vm.goToAccessPage = function () {
            if (parseInt(vm.goToAccessPageNumber) > vm.dataOperationsAccess.totalPageCount) {
                vm.goToAccessPageNumber = '';
            } else {
                vm.dataOperationsAccess.dataOperations.paging.currentPage = vm.goToAccessPageNumber;
                vm.getAccessSupport();
                vm.goToAccessPageNumber = '';
            }
        };

      

        vm.onSupportStatus = function (cid, supportstatus) {
            var support = {};
            support.cid = cid;
            support.flag = supportstatus;
            var message = "<p>The changes to the device have not been saved yet. Are you sure you want to cancel the changes?</p>";
            modalService.questionModal('Device Cancellation', message, true).result.then(function () {
                supportService.updateSupportStatus(support).then(function (res) {
                    if (res && res.success) {
                        notificationBarService.success(res.body);
                        activate();
                    } else
                        notificationBarService.error(res.body);
                });
            });
        }


        vm.onAccessStatus = function (cid, supportstatus) {
            var support = {};
            support.cid = cid;
            support.flag = supportstatus;
            var message = "<p>The changes to the device have not been saved yet. Are you sure you want to cancel the changes?</p>";
            modalService.questionModal('Status Cancellation', message, true).result.then(function () {
                /*supportService.updateSupportStatus(support).then(function (res) {
                    if (res && res.success) {
                        notificationBarService.success(res.body);
                        activate();
                    } else
                        notificationBarService.error(res.body);
                });*/
                adminHomeService.updateSupport(cid,supportstatus).then(function(res) {
                    if(res){
                        console.log(res.data);
                    }
                });
            });
        }
      

       
       
        function activate() {
            vm.refresh(true);
        };


        activate();
        return vm;
    }
})();