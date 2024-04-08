(function () {
    'use strict';
    angular
        .module('app')
        .controller('GeoFenceController', controller);
    controller.$inject = ['dashboardDataService', 'spid', 'notificationBarService', 'geoFenceService', 'modalService', 'navigation', 'SimpleListScreenViewModel', 'venuesession', '$rootScope', 'venuedataservice', '$linq'];

    /* @ngInject */
    function controller(dashboardDataService, spid, notificationBarService, geoFenceService, modalService, navigation, SimpleListScreenViewModel, venuesession, $rootScope, venuedataservice, $linq) {
        var vm = new SimpleListScreenViewModel();
        vm.pageHeight = screen.height - 180;
        vm.Isinitialload = true;
        if (venuesession.sid) {
            vm.sid = venuesession.sid;
        }
        if (spid && spid !== "0") {
            vm.spid = spid;
            $rootScope.spid = vm.spid;
        }

        vm.addgeofence = function () {
            navigation.goToAddGeofence(vm.spid, 0);
        };

        vm.loadServiceQueue = function () {
            venuedataservice.getVenueDetailById(vm.sid).then(function (res) {
                vm.venueDetails = res;
                if (vm.venueDetails) {
                    if (vm.venueDetails.uid) {
                        if (vm.venueDetails.uid.length > 10)
                            vm.venueDetails.newUid = vm.venueDetails.uid.substr(0, 10) + "...";
                        else
                            vm.venueDetails.newUid = vm.venueDetails.uid.substr(0, 10)
                    }
                    else
                        vm.venueDetails.newUid = "";
                }
            });

            dashboardDataService.getFloor(vm.sid, true).then(function (res) {
                vm.floorsdata = res;
                vm.floorDetails = vm.floorsdata.portion;
                if (vm.floorDetails.length > 0) {
                    if ($rootScope.spid) {
                        vm.onFloorChanges($rootScope.spid);
                        vm.selectedFloor = $rootScope.spid;
                    }
                    else {
                        vm.spid = vm.floorDetails[0].id;
                        vm.selectedFloor = vm.spid;
                        vm.onFloorChanges(vm.floorDetails[0].id);
                    }
                }
                else if (vm.floorDetails.length === 0) {
                    var message = "<p>Oops! No record Found. Please Add Floor to View Details.</p>";
                    modalService.messageModal('Information', message).result.then(function () {
                        navigation.gotoFloorPlan();
                    });
                }
            });
        };

        vm.getData = function (refresh) {
            geoFenceService.getGeoFenceListForTable(vm.spid, refresh, vm.dataOperations, vm.filterFn)
                .then(function (result) {

                    if (!vm.Isinitialload) {
                        angular.forEach(result.allData, function (item) {
                            item.isChecked = false;
                        });
                        vm.Isinitialload = false;
                    }
                    vm.allData = result.allData;
                    angular.forEach(result.pagedData, function (v, k) {
                        v.alerts = "";
                        v.isChecked = false;
                        angular.forEach(v.associatedAlerts, function (value, key) {
                            v.alerts += (value.name + ", ");
                        });
                        if (v.alerts.endsWith(", ")) {
                            v.alerts = v.alerts.substring(0, (v.alerts.length - 2));
                        }
                        v.statusType = (v.status.toLowerCase() === "enabled") ? "Enabled" : "Disabled";
                    });
                    vm.pagedData = result.pagedData;
                    vm.fullCount = result.dataCount;
                    vm.filteredCount = result.filteredDataCount;
                });
        };

        vm.searchBy = function (search) {
            if (search && search.length > 0) {
                vm.filterFn = function (data) {
                    var lowerCaseSearchTerm = search.toLowerCase();
                    var result = (data.name && data.name.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.fenceType && data.fenceType.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.statusType && data.statusType.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.alerts && data.alerts.toLowerCase().contains(lowerCaseSearchTerm));
                    return result;
                };
            } else {
                vm.filterFn = null;
            }
            vm.getData(false);
        };

        vm.delete = function (fence) {
            var data = { ids: [fence.id] };
            modalService.confirmDelete('Are you sure you want to delete the geofence?').result.then(
                function () {
                    geoFenceService.deleteFence(data).then(function (result) {
                        notificationBarService.success(result.body);
                        vm.clearControl();
                        vm.getData(true);
                    });
                },
                function () {
                });
        };

        vm.multiDeleteFence = function () {
            var fenceIds = [];
            angular.forEach(vm.allData, function (value, key) {
                if (value.isChecked) {
                    fenceIds.push(value.id);
                }
            });
            var data = { ids: fenceIds };
            if (fenceIds.length > 0) {
                modalService.confirmDelete('Are you sure you want to delete the geofence?').result.then(
                    function () {
                        geoFenceService.deleteFence(data).then(function (result) {
                            notificationBarService.success(result.body);
                            vm.clearControl();
                            vm.getData(true);
                        });
                    },
                    function () {
                    });
            }
            else {
                var message = "<p>There are no geofence selected for delete.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };

        vm.edit = function (fence) {
            navigation.goToAddGeofence(vm.spid, fence.id);
        };

        vm.onFloorChanges = function (selectedfloor) {
            vm.spid = selectedfloor;
            $rootScope.spid = vm.spid;
            vm.getData(true);
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
            vm.loadServiceQueue();
        }

        vm.clearControl = function () {
            vm.ischekAll = false;
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

        activate();

        return vm;
    }
})();