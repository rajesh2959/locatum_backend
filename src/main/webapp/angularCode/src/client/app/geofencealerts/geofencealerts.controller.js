(function () {
    'use strict';
    angular
        .module('app')
        .controller('GeoFenceAlertsController', controller);
    controller.$inject = ['venuedataservice', 'geoFenceAlertService', 'notificationBarService', 'modalService', 'navigation', 'SimpleListScreenViewModel', '$linq'];

    /* @ngInject */
    function controller(venuedataservice, geoFenceAlertService, notificationBarService, modalService, navigation, SimpleListScreenViewModel, $linq) {
        var vm = new SimpleListScreenViewModel();

        vm.pageHeight = screen.height - 180;
        vm.Isinitialload = true;
        vm.getData = function (refresh) {
            venuedataservice.getVenueListForTable(refresh, vm.dataOperations, vm.filterFn)
                .then(function (result) {
                    if (vm.Isinitialload) {
                        angular.forEach(result.allData, function (item) {
                            item.isChecked = false;
                        });
                        vm.Isinitialload = false;
                    }
                    vm.allData = result.allData;
                    angular.forEach(result.pagedData, function (v, k) {
                        v.channels = "";
                        angular.forEach(v.channel, function (value, key) {
                            v.channels += (value + ", ");
                        });
                        if (v.channels.endsWith(", ")) {
                            v.channels = v.channels.substring(0, (v.channels.length - 2));
                        }
                    });
                    vm.pagedData = result.pagedData;
                    vm.fullCount = result.dataCount;
                    vm.filteredCount = result.filteredDataCount;
                });
        };

        vm.searchBy = function (search) {
            if (search && search.length > 0) {
                vm.filterFn = function (datum) {
                    var lowerCaseSearchTerm = search.toLowerCase();
                    var result =
                        (datum.name && datum.name.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (datum.status && datum.status.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (datum.channels && datum.channels.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (datum.category && datum.category.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (datum.triggerType && datum.triggerType.toLowerCase().contains(lowerCaseSearchTerm));
                    return result;
                };
            } else {
                vm.filterFn = null;
            }
            vm.getData(false);
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

        vm.delete = function (alerts) {
            var data = { ids: [alerts.id] };
            modalService.confirmDelete('Are you sure you want to delete the alert?').result.then(
                function () {
                    geoFenceAlertService.deleteAlerts(data).then(function (result) {
                        notificationBarService.success(result.body);
                        vm.getData(true);
                        vm.ischekAll = false;
                    });
                },
                function () {
                });
        };

        vm.multiDeleteFence = function () {
            var alertIds = [];
            angular.forEach(vm.allData, function (value, key) {
                if (value.isChecked) {
                    alertIds.push(value.id);
                }
            });
            var data = { ids: alertIds };
            if (alertIds != "") {
                modalService.confirmDelete('Are you sure you want to delete the alerts?').result.then(
                    function () {
                        geoFenceAlertService.deleteAlerts(data).then(function (result) {
                            notificationBarService.success(result.body);
                            vm.getData(true);
                            vm.ischekAll = false;

                        });
                    },
                    function () {
                    });
            }
            else {
                var message = "<p>There are no alert selected for delete.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };

        vm.edit = function (alert) {
            navigation.goToAddGeoFenceAlerts(alert.id);
        };

        vm.inputCheckAll = function (isChecked) {
            angular.forEach(vm.allData, function (value, key) {
                if (isChecked == true) {
                    value.isChecked = true;
                } else {
                    value.isChecked = false;
                }
            });
        };

        vm.isRowChecked = function () {
            var isAnyRowChecked = !$linq.Enumerable().From(vm.allData)
                .Any(function (x) {
                    return x.isChecked == false;
                });
            if (!isAnyRowChecked)
                vm.ischekAll = false;
            else
                vm.ischekAll = true;
        }

        function activate() {
            vm.getData(true);
        }

        activate();

        return vm;
    }
})();