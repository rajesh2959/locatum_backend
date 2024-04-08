(function () {
    'use strict';

    angular
        .module('app.venue')
        .controller('venueController', controller);

    controller.$inject = ['messagingService', 'notificationBarService', '$q', 'modalService', 'venuedataservice', 'navigation', '$rootScope', 'venuesession', 'environment'];
    /* @ngInject */

    function controller(messagingService, notificationBarService, $q, modalService, venuedataservice, navigation, $rootScope, venuesession, env) {

        var vm = this;
        var baseUrl = env.serverBaseUrl;
        vm.latLngList = [];
        var marker, i, map;
        vm.floorCount = 0;
        vm.finderCount = 0;
        vm.tagsCount = 0;
        vm.deviceCount = 0;

        document.getElementById('enlarges').addEventListener('click', function (ev) {
            ev.preventDefault();
            $('.resizecontent').toggleClass('deviceexpands');
        });

        vm.initializeMap = function () {
            map = new google.maps.Map(document.getElementById('map'), {
                zoom: 15,
                center: new google.maps.LatLng(-33.890542, 151.274856),
                mapTypeId: google.maps.MapTypeId.ROADMAP
            });
            map.setTilt(30);
        };

        function loadServiceQueue() {
            vm.serviceQueue = [];
            vm.serviceQueue.push(venuedataservice.getVenueList());
        };

        vm.executeServiceQueue = function () {
            $q.all(vm.serviceQueue).then(function (serviceResponse) {
                vm.venueList = [];
                if (serviceResponse[0]) {
                    vm.venueList = serviceResponse[0];
                    if (vm.venueList.length > 0) {

                        angular.forEach(vm.venueList, function (value, key) {
                            if (value.uid) {
                                if (value.uid.length > 21)
                                    value.newUid = value.uid.substr(0, 20) + "...";
                                else
                                    value.newUid = value.uid.substr(0, 20)
                            }
                            else
                                value.newUid = "";
                        });
                        vm.selectedVenue = vm.venueList[0].id;
                        if (venuesession.sid > 0 && venuesession.sid != undefined) {
                            vm.selectedVenue = venuesession.sid;
                            vm.onVenueChange(venuesession.sid);
                        }
                        else {
                            venuesession.create(vm.venueList[0].id);
                            vm.onVenueChange(vm.venueList[0].id);
                        }

                    }
                }
            });
        }

        function activate() {

            //var recentAlertId = window.innerHeight;
            //var innerWidth = window.innerWidth;
            //alert(innerWidth,recentAlertId);
            // var recentAlertheight = 215;
            // if (recentAlertId >= 700)
            //     recentAlertheight = 240;
            // else if (recentAlertId >= 650)
            //     recentAlertheight = 230;
            // else if (recentAlertId >= 600)
            //     recentAlertheight = 220;
            // else if (recentAlertId >= 550)
            //     recentAlertheight = 200;
            //     else if (recentAlertId >= 500)
            //     recentAlertheight = 180;
            // var height = 'height:' + recentAlertheight + 'px !important';
            // $("#recentAlertId").attr("style", height);


            $rootScope.$emit('customEvent', "venue");
            vm.initializeMap();
            loadServiceQueue();
            vm.executeServiceQueue();

        }

        vm.editVenue = function () {
            navigation.gotAddVenue(vm.venueId);
        };

        vm.deleteVenue = function () {
            var message = "<p>Are you sure you want to delete?</p> <p>Press No if you want to continue to work. Press Yes to delete.</p>";
            modalService.questionModal('Delete Confirmation', message, true).result.then(function () {
                venuedataservice.deleteVenueDetails(vm.venueId).then(function (res) {
                    if (res && res.success) {
                        notificationBarService.success(res.body);
                        venuesession.create("");
                        activate();
                    } else
                        notificationBarService.error(res.body);
                });
            });
        };

        vm.viewDashboard = function () {
            navigation.gotToHome(vm.venueId);
        };

        vm.onVenueChange = function (venuId) {
            vm.floorCount = 0;
            vm.finderCount = 0;
            vm.tagsCount = 0;
            vm.deviceCount = 0;
            vm.recentAlertsList = [];
            vm.venueId = venuId;
            venuesession.create("");
            venuesession.create(vm.venueId);
            if (marker)
                marker.setMap(null);

            venuedataservice.getVenueDetailCardCound(1, venuId).then(function (res) {
                if (res) {
                    vm.floorCount = res.floorCount;
                    vm.finderCount = res.gatewayCount;
                    vm.tagsCount = res.checkedoutTagCount;
                    vm.deviceCount = res.activeTagCount;
                }
            });

            /*
             
            venuedataservice.getVenueDetailCardCound(2, venuId).then(function (res) {
                if (res) {
                    vm.finderCount = res;
                }
            });
           
            venuedataservice.getVenueDetailCardCound(3, venuId).then(function (res) {
                if (res) {
                    vm.tagsCount = res;
                }
            });
            venuedataservice.getVenueDetailCardCound(4, venuId).then(function (res) {
                if (res) {
                    vm.deviceCount = res;
                }
            });*/

            venuedataservice.getVenueRecentAlert(venuId).then(function (res) {
                if (res) {
                    angular.forEach(res, function (value, key) {
                        var type = value.type.toLowerCase();
                        value.imageType = baseUrl + '/images/alerts/' + type + '.png';
                    });
                    vm.recentAlertsList = res;
                }
            });

            vm.goToVenueData = function () {
                modalService.venueModal(vm.recentAlertsList).result.then(function (res) {
                }, function () {
                });
            }

            venuedataservice.getVenueDetailById(venuId).then(function (res) {
                if (res) {
                    vm.setMarker(res.latitude, res.longitude);
                }
            });
        };

        vm.setMarker = function (lat, lng) {
            var currentLatLong = new google.maps.LatLng(lat, lng);
            map.setTilt(30);
            marker = new google.maps.Marker({
                position: currentLatLong,
                map: map,
                draggable: false,
                animation: google.maps.Animation.DROP
            });
            map.setCenter(marker.getPosition());
            marker.setMap(map);
        };

        activate();

        return vm;
    }
})();