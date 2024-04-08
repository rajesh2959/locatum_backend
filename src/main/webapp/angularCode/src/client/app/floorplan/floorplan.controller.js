(function () {
    'use strict';
    angular
        .module('app.floorplan')
        .controller('FloorPlanController', controller);
    controller.$inject = ['navigation', 'venuedataservice', 'notificationBarService', '$q', '$rootScope', 'session', 'environment', 'floordataservice', 'modalService', '$scope', 'venuesession'];
    /* @ngInject */

    function controller(navigation, venuedataservice, notificationBarService, $q, $rootScope, session, env, floordataservice, modalService, $scope, venuesession) {
        var vm = {};
        $rootScope.spid = "";
        var baseUrl = env.serverBaseUrl;
        vm.venueList = [];
        vm.floorsdata = [];
        vm.showdiv = false;

        var loadServiceQueue = function () {
            vm.serviceQueue = [];
            vm.serviceQueue.push(venuedataservice.getVenueList());
        };

        var executeServiceQueue = function() {
            $q.all(vm.serviceQueue).then(function(serviceResponse) {
                vm.venueList = [];
                if (serviceResponse[0]) {
                    vm.venueList = serviceResponse[0];
                    angular.forEach(vm.venueList,
                        function(value, key) {
                            if (value.uid) {
                                if (value.uid.length > 21)
                                    value.newUid = value.uid.substr(0, 20) + "...";
                                else
                                    value.newUid = value.uid.substr(0, 20);
                            } else
                                value.newUid = "";
                        });

                    if (vm.venueList.length > 0) {
                        vm.selectedVenue = vm.venueList[0].id;
                        if (venuesession.sid) {
                            vm.selectedVenue = venuesession.sid;
                            vm.onVenueChange(venuesession.sid);
                        } else {
                            $rootScope.venueId = vm.venueList[0].id;
                            vm.onVenueChange(vm.venueList[0].id);
                        }
                    }
                }
            });
        };

        vm.onVenueChange = function (venuId) {
            vm.venueId = venuId;
            venuesession.create(vm.venueId);
            venuedataservice.setCurrentVenuId(venuId);
            vm.floorsdata = [];
            floordataservice.getFloorPlanList(venuId).then(function (res) {
                if (res.body) {
                    for (var i = 0; i < res.body.length; i++) {
                        res.body[i].imagePath = baseUrl + "/web/site/portion/planfile?sid=" + venuId + "&spid=" + res.body[i].id + "&cid=" + session.cid + "&time=" + new Date();
                    }
                    vm.floorsdata = res.body;
                }
            });
        };

        vm.deleteFloor = function (floorid) {
            modalService.confirmDelete('<p>Are you sure you want to delete?</p> <p>Press No if you want to continue to work. Press Yes to delete.</p>').result.then(
                function () {
                    floordataservice.deleteFloorDetails(floorid).then(function (res) {
                        if (res && res.success) {
                            notificationBarService.success(res.body);
                            activate();
                        } else
                            notificationBarService.error(res.body);
                    });
                },
                function () {
                });
        };

        vm.multiDeleteFloor = function () {
            var floorids = "";
            angular.forEach(vm.floorsdata, function (value, key) {
                if (value.isChecked)
                    floorids = floorids + value.id + ",";
            });
            if (floorids != "") {
                modalService.confirmDelete('<p>Are you sure you want to delete selected items?</p> <p>Press No if you want to continue to work. Press Yes to delete.</p>').result.then(
                    function () {
                        floordataservice.multiDeleteFloorDetails(floorids).then(function (res) {
                            if (res && res.success) {
                                notificationBarService.success(res.body);
                                activate();
                            } else
                                notificationBarService.error(res.body);
                        });
                    },
                    function () {
                    });
            }
            else {
                var message = "<p>There are no floors selected for delete.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };

        vm.editFloor = function (floorid) {
            navigation.gotAddFloorPlan(floorid);
        };

        vm.geoConfig = function (floorid) {
            navigation.goToGeoConfig(floorid);
        };

        vm.networkConfig = function (floorid) {
            navigation.goToNetworkConfig(floorid);
        };

        vm.geofence = function (floorid) {
            navigation.goToGeoFence(floorid);
        };

        vm.viewFloor = function(floorid) {
            $rootScope.spid = floorid;
            navigation.goToFloorView(floorid);
        };

        vm.tab = function (index) {
            vm.tabIndex = index;
        };

        vm.back = function () {
            navigation.goToParentState();
        };

        function activate() {
            loadServiceQueue();
            executeServiceQueue();
        }

        activate();

        return vm;
    }
})();