﻿(function () {
    'use strict';

    angular
        .module('app.reports')
        .controller('reportsController', controller);

    controller.$inject = ['$scope', '$log', '$timeout', 'SimpleListScreenViewModel', 'visualizationService', 'reportService', 'messagingService', 'notificationBarService', '$q', 'modalService',  'navigation', '$rootScope'];
    /* @ngInject */

    function controller($scope, $log, $timeout, SimpleListScreenViewModel, visualizationService, reportService, messagingService, notificationBarService, $q, modalService,  navigation, $rootScope) {

        var vm = new SimpleListScreenViewModel();
        vm.refresh = function (refresh) {
            vm.getReport(refresh);
        };

        vm.getReport = function (refresh) {
            reportService.getReportListForTable(refresh, vm.dataOperations, vm.filterFn).then(function (result) {
                vm.allReportList = result.allData;
                vm.pagedData = result.pagedData;
                vm.fullCount = result.dataCount;
                vm.filteredCount = result.filteredDataCount;
            });
        };


        vm.getData = function (refresh) {
            reportService.getReportListForTable(refresh, vm.dataOperations, vm.filterFn).then(function (result) {
                vm.allReportList = result.allData;
                vm.pagedData = result.pagedData;
                vm.fullCount = result.dataCount;
                vm.filteredCount = result.filteredDataCount;
            });
        };

        vm.inputCheckAll = function (isChecked) {
            angular.forEach(vm.allReportList,
                function (value, key) {
                    if (isChecked == true) {
                        value.isChecked = true;
                    } else {
                        value.isChecked = false;
                    }
                });
        };

        vm.isRowChecked = function () {
            var isAnyRowChecked = !$linq.Enumerable().From(vm.allReportList)
                .Any(function (x) {
                    return x.isChecked == false;
                });
            if (!isAnyRowChecked)
                vm.ischekAll = false;
            else
                vm.ischekAll = true;
        };

        vm.addReport = function () {
            navigation.goToAddReport(0, 0, 0);
        };  

        vm.edit = function (report) {
            navigation.goToAddReport(report.cid, report.id, report.name);
        };

        vm.deleteAll = function () {
            var reportIds = [];
            angular.forEach(vm.allReportList,
                function (value, key) {
                    if (value.isChecked) {
                        reportIds.push(value.id);
                    }
                });
            if (reportIds.length > 0) {
                modalService.confirmDelete('Are you sure you want to delete the report?').result.then(
                    function () {
                        reportService.delete(reportIds).then(function (result) {
                            if (result && result.success) {
                                vm.refresh(true);
                                vm.ischekAll = false;
                            }
                            notificationBarService.success(result.body);
                        });
                    },
                    function () {
                    });
            } else {
                var message = "<p>There are no report selected for delete.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };

        vm.searchBy = function (search) {
            if (search && search.length > 0) {
                vm.filterFn = function (data) {
                    var lowerCaseSearchTerm = search.toLowerCase();
                    var result = (data.name && data.name.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.description && data.description.toLowerCase().contains(lowerCaseSearchTerm))
                    return result;
                };
            } else {
                vm.filterFn = null;
            }
            vm.getReport(false);
        };

        vm.delete = function (report) {
            var reportIds = [];
            reportIds.push(report.id);
            if (reportIds.length > 0) {
                modalService.confirmDelete('Are you sure you want to delete the report?').result.then(
                    function () {
                        reportService.delete(reportIds).then(function (result) {
                            if (result && result.success) {
                                vm.refresh(true);
                                vm.ischekAll = false;
                            }
                            notificationBarService.success(result.body);
                        });
                    },
                    function () {
                    });
            } else {
                var message = "<p>There are no report selected for delete.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };

        vm.goToPage = function () {
            if (parseInt(vm.goToPageNumber) > vm.totalPageCount) {
                vm.goToPageNumber = '';
            } else {
                vm.dataOperations.paging.currentPage = vm.goToPageNumber;
                vm.getReport();
                vm.goToPageNumber = '';
            }
        };


        function activate() {
            vm.refresh(true);
        };

        activate();

        return vm;
    }
})();