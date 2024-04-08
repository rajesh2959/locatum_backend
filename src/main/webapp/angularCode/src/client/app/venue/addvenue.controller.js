(function() {
    'use strict';
    angular
        .module('app.venue')
        .controller('addvenueController', controller);
    controller.$inject = ['messagingService', 'notificationBarService', 'modalService', 'venueId', 'navigation', 'venuedataservice'];

    /* @ngInject */
    function controller(messagingService, notificationBarService, modalService, venueId, navigation, venuedataservice) {
        var vm = this;
        vm.latLngList = [];
        var marker, i, map;

        vm.venueDetails = {};
        vm.pageName = "Add Venue";
        vm.venueId = "";
        if (venueId)
            vm.venueId = venueId;

        vm.initializeMap = function () {
            map = new google.maps.Map(document.getElementById('map'),
                {
                    zoom: 15,
                    center: new google.maps.LatLng(-33.890542, 151.274856),
                    mapTypeId: google.maps.MapTypeId.ROADMAP
                });
            map.setTilt(30);

            var input = document.getElementById('txtsearch');
            var options = {
                componentRestrictions: null
            };

            var autocomplete = new google.maps.places.Autocomplete(input, options);
            autocomplete.addListener('place_changed',
                function () {
                    vm.place = autocomplete.getPlace();
                    if (marker)
                        marker.setMap(null);
                    vm.setMarker(vm.place.geometry.location.lat(), vm.place.geometry.location.lng(), vm.place.formatted_address);
                });
        };

        vm.setMarker = function(lat, lng, address) {
            var currentLatLong = new google.maps.LatLng(lat, lng);
            map.setTilt(30);
            marker = new google.maps.Marker({
                position: currentLatLong,
                map: map,
                draggable: false,
                animation: google.maps.Animation.DROP,
            });
            map.setCenter(marker.getPosition());
            marker.setMap(map);
            vm.venueDetails.latitude = lat;
            vm.venueDetails.longitude = lng;
            vm.venueDetails.name = address;
        };

        vm.save = function (frm) {
            messagingService.broadcastCheckFormValidatity();
            if (frm.$valid) {
                if (vm.venueDetails.latitude != null && vm.venueDetails.longitude != null) {
                    venuedataservice.saveVenueDetails(vm.venueDetails).then(function () {
                        notificationBarService.success('Saved Successfully');
                        navigation.goToVenue();
                    });
                } else if (vm.venueDetails.latitude == null && vm.venueDetails.longitude == null) {
                    var message = "<p>Please enter valid address.</p>";
                    modalService.messageModal('Information', message).result.then(function () {
                    });
                }
            }
        };

        vm.cancel = function() {
            var postData = {};
            postData.uid = vm.venueDetails.uid;
            postData.name = vm.venueDetails.name;
            postData.description = vm.venueDetails.description;

            if (postData.uid != null || postData.name != null || postData.description != null) {
                var message = "<p>The changes to the venue have not been saved yet. Are you sure you want to cancel the changes?</p>";
                modalService.questionModal('Venue Cancellation', message, true).result.then(function() {
                    navigation.goToVenue();
                });
            } else {
                navigation.goToVenue();
            }
        };

        function activate() {
            vm.initializeMap();
            if (vm.venueId && vm.venueId != 0) {
                vm.pageName = "Edit Venue";
                venuedataservice.getVenueDetailById(vm.venueId).then(function(res) {
                    if (res) {
                        vm.venueDetails.latitude = res.latitude;
                        vm.venueDetails.longitude = res.longitude;
                        vm.venueDetails.name = res.name;
                        vm.venueDetails.uid = res.uid;
                        vm.venueDetails.description = res.description;
                        vm.venueDetails.id = res.id;
                        vm.setMarker(vm.venueDetails.latitude, vm.venueDetails.longitude, vm.venueDetails.name);
                    }
                });
            }
        }

        activate();

        return vm;
    }
})();