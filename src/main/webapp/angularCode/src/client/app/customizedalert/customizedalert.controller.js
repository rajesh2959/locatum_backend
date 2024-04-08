(function () {
    'use strict';
    angular
        .module('app.customizedalert')
        .controller('CustomizedAlertController', controller);

    controller.$inject = ['customizedAlertService', 'session', 'notificationBarService', 'SimpleListScreenViewModel', 'modalService'];

    /* @ngInject */
    function controller(customizedAlertService, session, notificationBarService, SimpleListScreenViewModel, modalService) {
        var vm = new SimpleListScreenViewModel();
        vm.info;
        vm.batterythreshold = 0;
        vm.gatewayinactivity = 0;
        vm.taginactivity = 0;
        vm.switchTagONOFF = false;
        vm.switchDeviceONOFF = false;
        vm.configTagAlert = [];
        vm.tagtype;
        vm.tagNames;
        vm.placeName;
        vm.place = [
            { "key": "floor", "value": "Floor" },
            { "key": "venue", "value": "Venue" },
            { "key": "location", "value": "Location" },
        ];

        vm.getConfigAlert = function (refresh) {
            customizedAlertService.getConfigAlertForTable(refresh, vm.dataOperations, vm.filterFn)
                .then(function (result) {
                    vm.pagedData = result.pagedData;
                    vm.fullCount = result.dataCount;
                    vm.filteredCount = result.filteredDataCount;
                });
        };

        vm.update = function () {
            var res = {};
            res.cid = session.cid;
            res.battery_threshold = vm.batterythreshold;
            res.default_dev_inactivity_time = vm.gatewayinactivity;
            res.default_inactivity_time = vm.taginactivity;
            res.inactivityMail = vm.switchTagONOFF;
            res.inactivitydevMail = vm.switchDeviceONOFF;

            if (res.battery_threshold != null && res.default_dev_inactivity_time != null && res.default_inactivity_time != null) {
                if (res.battery_threshold != 0 && res.default_dev_inactivity_time != 0 && res.default_inactivity_time != 0)
                    updateInactivityInfo(res);
                else
                    vm.requiredvalidation = true;
            }
            else
                vm.requiredvalidation = true;
        };

        vm.saveAlert = function () {
            var res = {};
            res.cid = session.cid;
            res.tagtype = vm.selectedTagtype;
            res.tagids = [vm.selectedTagNames];
            res.place = vm.selectedplace;
            res.placeIds = [vm.selectedPlaceId];
            res.duration = vm.inactivityDuration;
            if (res.cid != undefined && res.tagtype != undefined && res.tagids != undefined && res.place != undefined && res.placeIds != undefined && res.duration != undefined && vm.selectedPlaceId != undefined && vm.selectedTagNames != undefined)
                saveAlertInfo(res);
            else
                vm.configValidation = true;
        };

        vm.onTagTypeChange = function (tagType) {
            getTagNames(tagType);
        };

        vm.onPlaceChange = function (place) {
            getChoosePlace(place);
        };

        vm.goToPage = function () {
            if (parseInt(vm.goToPageNumber) > vm.totalPageCount) {
                vm.goToPageNumber = '';
            } else {
                vm.dataOperations.paging.currentPage = vm.goToPageNumber;
                vm.getConfigAlert();
                vm.goToPageNumber = '';
            }
        };

        vm.onDelete = function (id) {
            var message = "<p>Are you sure you want to delete?</p> <p>Press No if you want to continue to work. Press Yes to delete.</p>";
            modalService.confirmDelete(message).result.then(
                function () {
                    customizedAlertService.deleteConfigAlert(id).then(function (res) {
                        vm.deleteInfo = res;
                        notificationBarService.success(res.body);
                        vm.getConfigAlert(true);
                    });
                },
                function () {
                });
        };

        vm.onRefreshConfigAlert = function () {
            vm.getConfigAlert(true);
        };

        function updateInactivityInfo(data) {
            customizedAlertService.updateInactivityInfo(data).then(function (res) {
                if (res) {
                    if (res.body) {
                        notificationBarService.success(res.body);
                        vm.requiredvalidation = false;
                    }
                }
                vm.updateInfo = res;
            });
        }

        function saveAlertInfo(data) {
            customizedAlertService.saveAlertInfo(data).then(function (res) {
                vm.alertSaveInfo = res;
                if (res.code === 200) {
                    notificationBarService.success(res.body);
                    vm.getConfigAlert(true);
                    vm.selectedTagNames = undefined;
                    vm.selectedPlaceId = undefined;

                    vm.inactivityDuration = "";
                    vm.configValidation = false;
                }
            });
        }

        function getTagNames(tagType) {
            customizedAlertService.getBasedTagNames(tagType).then(function (res) {
                vm.tagNames = res;
            });
        }

        function getChoosePlace(place) {
            customizedAlertService.getInactivityType(place).then(function (res) {
                vm.placeName = res;
            });
        }

        function getInactivityInfo() {
            customizedAlertService.getInactivityInfo().then(function (res) {
                vm.info = res;
                if (res) {
                    vm.switchTagONOFF = (res.inactivityMail.toLowerCase() === 'true');
                    vm.switchDeviceONOFF = (res.finderEmailSms.toLowerCase() === 'true');
                    vm.batterythreshold = parseInt(res.battery_threshold);
                    vm.gatewayinactivity = parseInt(res.finderInactime);
                    vm.taginactivity = parseInt(res.default_inactivity_time);
                }
            });
        }

        function getTagsConfigInfo() {
            customizedAlertService.getTagTypes().then(function (res) {
                vm.tagtype = res;
                vm.selectedTagtype = vm.tagtype[0];
                vm.onTagTypeChange(vm.selectedTagtype)
            });
        }

        $("#seconds").on("keypress", function (evt) {
            var keycode = evt.charCode || evt.keyCode;
            if (keycode == 46) {
                return false;
            }
        });

        $("#battery").on("keypress", function (evt) {
            var keycode = evt.charCode || evt.keyCode;
            if (keycode == 46) {
                return false;
            }
        });

        function activate() {
            vm.getConfigAlert(true);
            getInactivityInfo();
            getTagsConfigInfo();
            vm.selectedplace = vm.place[0].key;
            vm.onPlaceChange(vm.selectedplace);
        }

        activate();

        return vm;

    }
})();