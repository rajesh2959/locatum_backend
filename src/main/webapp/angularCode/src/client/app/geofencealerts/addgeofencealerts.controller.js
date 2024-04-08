(function () {
    'use strict';
    angular
        .module('app.geofencealerts')
        .controller('AddGeoFenceAlertsController', controller);
    controller.$inject = ['dashboardDataService', 'geofencealertid', 'pagefrom', '$q', 'geoFenceAlertService', 'session', 'navigation', '$scope',
        'notificationBarService', 'messagingService', 'modalService', 'isAdd', 'spid', 'geofenceid'];

    /* @ngInject */
    function controller(dashboardDataService, geofencealertid, pagefrom, $q, geoFenceAlertService, session, navigation, $scope,
        notificationBarService, messagingService, modalService, isAdd, spid, geofenceid) {
        var vm = this;
        vm.isAdd = isAdd;
        vm.serviceQueue = [];
        vm.geoFenceAlertsDetails = {};
        vm.tagType = [];
        vm.tagList = [];
        vm.isEdit = (geofencealertid != "0");
        $scope.channel = ['sms', 'mail', 'popup'];
        vm.channelList = [
            { "key": "sms", "value": "SMS" },
            { "key": "mail", "value": "Email" },
            { "key": "dashboard-alert", "value": "Dashboard alerts" },
        ];

        $scope.toggleSelection = function toggleSelection(channel) {
            var idx = $scope.selection.indexOf(channel);
            if (idx > -1) {
                $scope.selection.splice(idx, 1);
            }
            else {
                $scope.selection.push(channel);
            }
        };

        vm.loadServiceQueue = function () {

            vm.serviceQueue.push(dashboardDataService.getTagList(session.cid));
            vm.serviceQueue.push(dashboardDataService.getTagTypes(session.cid));

            $q.all(vm.serviceQueue).then(function (serviceResponse) {
                vm.tagType = [];
                vm.tagList = serviceResponse[0];
                angular.forEach(serviceResponse[1], function (value, key) {
                    vm.tagType.push({ id: value });
                });

                if (vm.isEdit) {
                    geoFenceAlertService.getGeofenceAlert(geofencealertid).then(function (result) {
                        vm.geoFenceAlertsDetails = result;
                        angular.forEach(vm.geoFenceAlertsDetails.channel, function (channelvalue, key) {
                            angular.forEach(vm.channelList, function (value, key) {
                                if (value.key == channelvalue)
                                    value.isChecked = true;
                            });
                        });

                        if (vm.geoFenceAlertsDetails.category == "tagname") {
                            angular.forEach(vm.geoFenceAlertsDetails.associations, function (asvalue, key) {
                                angular.forEach(vm.tagList, function (value, key) {
                                    if (value.macaddr == asvalue)
                                        value.isChecked = true;
                                });
                            });
                        } else {
                            angular.forEach(vm.geoFenceAlertsDetails.associations, function (asvalue, key) {
                                angular.forEach(vm.tagType, function (value, key) {
                                    if (value.id == asvalue)
                                        value.isChecked = true;
                                });
                            });
                        }
                        vm.geoFenceAlertsDetails.statusType = (vm.geoFenceAlertsDetails.status == 'enabled');
                    });
                    vm.statusChange();
                } else
                    vm.statusChange();
            });
        };

        vm.tagTypeChange = function () {
            //if (vm.geoFenceAlertsDetails.category == 'tagtype') {
            //    vm.tagType = result;
            //} else {
            //    vm.tagList = res;
            //}
        };

        vm.cancel = function () {
            if (vm.geoFenceAlertsDetails) {
                var message = "<p>The changes to the Geofence Alert have not been saved yet. Are you sure you want to cancel the changes?</p>";
                modalService.questionModal('Geofence Alert Cancellation', message, true).result.then(function () {
                    if (pagefrom == 1) {
                        if (vm.isAdd == "true")
                            navigation.goToAddGeofence(spid, 0);
                        else
                            navigation.goToAddGeofence(spid, geofenceid);
                    }
                    else {
                        navigation.goToGeoFenceAlerts();
                    }
                });
            }
            else {
                if (pagefrom == 1) {
                    if (vm.isAdd == "true")
                        navigation.goToAddGeofence(spid, 0);
                    else
                        navigation.goToAddGeofence(spid, geofenceid);
                }
                else {
                    navigation.goToGeoFenceAlerts();
                }
            }
        };

        vm.save = function (frm) {
            vm.showValidation = false;
            messagingService.broadcastCheckFormValidatity();
            if (frm.$valid) {
                vm.statusChange();
                vm.geoFenceAlertsDetails.cid = session.cid;
                if (vm.geoFenceAlertsDetails.category == "tagname") {
                    vm.tagList.associations = [];
                    angular.forEach(vm.tagList, function (value, key) {
                        if (value.isChecked)
                            vm.tagList.associations.push(value.macaddr);
                    });
                    vm.geoFenceAlertsDetails.associations = vm.tagList.associations;
                } else {
                    vm.tagType.associations = [];
                    angular.forEach(vm.tagType, function (value, key) {
                        if (value.isChecked)
                            vm.tagType.associations.push(value.id);
                    });
                    vm.geoFenceAlertsDetails.associations = vm.tagType.associations;
                }
                vm.channel = [];
                angular.forEach(vm.channelList, function (value, key) {
                    if (value.isChecked)
                        vm.channel.push(value.key);
                });
                vm.geoFenceAlertsDetails.channel = vm.channel;
                if (!vm.geoFenceAlertsDetails.channel.length > 0 || !vm.geoFenceAlertsDetails.associations.length > 0 || !vm.geoFenceAlertsDetails.triggerType) {
                    vm.showValidation = true;
                    return;
                }
                geoFenceAlertService.saveFenceAlerts(vm.geoFenceAlertsDetails).then(function (result) {
                    notificationBarService.success(result.body);
                    if (result && result.success)
                        if (pagefrom == 1) {
                            if (vm.isAdd == "true")
                                navigation.goToAddGeofence(spid, 0);
                            else
                                navigation.goToAddGeofence(spid, geofenceid);
                        }
                        else {
                            navigation.goToGeoFenceAlerts();
                        }
                })
            }
            else {
                vm.showValidation = true;
            }
        };

        vm.statusChange = function () {
            vm.geoFenceAlertsDetails.status = vm.geoFenceAlertsDetails.statusType ? 'enabled' : 'disabled';
        };

        vm.loadServiceQueue();

        return vm;
    }
})();