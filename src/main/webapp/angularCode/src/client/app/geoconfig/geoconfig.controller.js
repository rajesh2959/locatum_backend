(function () {
    'use strict';
    angular
        .module('app.geoconfig')
        .controller('GeoconfigController', controller);
    controller.$inject = ['dashboardDataService', 'spid', 'venuedataservice', '$rootScope', 'environment', 'session', 'modalService', 'navigation', 'geoConfigService', '$scope', 'notificationBarService', 'venuesession'];

    /* @ngInject */
    function controller(dashboardDataService, spid, venuedataservice, $rootScope, env, session, modalService, navigation, geoConfigService, $scope, notificationBarService, venuesession) {
        var vm = this;
        var baseUrl = env.serverBaseUrl;
        var labels = '0';
        var iconBase = '../images/location_icon.png';
        var iconBlue = '/facesix/static/geo/images/grn-blank.png';
        var markers = [];
        var latlongs = [];
        var coordinates = [];
        var map;
        var oWidth = 0;
        var oHeight = 0;
        var jnifileW = 1000;
        var jnifileH = 600;
        var ang = 0;
        var zom = 100;
        var opa = 0.8;
        var searchBox;
        vm.spid;
        vm.baseImgUrl = baseUrl + "/web/site/portion/planfile?spid=";
        vm.postfixUrl = "&cid=" + session.cid + "&time=" + new Date();
        if (venuesession.sid) {
            vm.sid = venuesession.sid;
            $rootScope.venueId = vm.sid;
        }
        if (spid && spid != 0) {
            vm.spid = spid;
            $rootScope.spid = vm.spid;
        }

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
                    }
                    else {
                        $rootScope.spid = vm.floorDetails[0].id;
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

        vm.onFloorChanges = function (spid) {
            $rootScope.spid = spid;
            vm.selectedFloor = spid;
            vm.resetMap();
            reloadMap(vm.selectedFloor);
            vm.spid = spid;
        };

        map = new google.maps.Map(document.getElementById('map'), {
            zoom: 30,
            center: new google.maps.LatLng(-33.890542, 151.274856),
            mapTypeId: 'satellite',
            disableDefaultUI: true,
            mapTypeControl: true,
            zoomControl: true,
            mapTypeControlOptions: {
                position: google.maps.ControlPosition.TOP_RIGHT
            }
        });

        google.maps.event.addListenerOnce(map, "idle", function () {
            $scope.$emit('mapInitialized', map);
        });

        $scope.$on('mapInitialized', function (event, map) {
            activate();
        });

        function activate() {
            vm.loadServiceQueue();
        }

        vm.savePoints = function () {
            var mcnt = 0;
            var latlng = [];
            var mapZoom = (map.getZoom() - 2);
            var mystrNew = {
                "mapzoom": mapZoom.toString(),
                "opacity": opa.toString(),
                "zoom": zom.toString(),
                "rotation": ang.toString(),
            };

            markers.forEach(function (marker) {
                var latlngStr = {
                    "latitude": marker.internalPosition.lat().toString(),
                    "longitude": marker.internalPosition.lng().toString(),
                    "x": marker.coord[mcnt].x.toString(),
                    "y": marker.coord[mcnt].y.toString()
                };
                latlng.push(latlngStr);
                mcnt++;
            });

            mystrNew.latlng = latlng;
            var uid = "";
            if (mcnt === 4) {
                var json = JSON.stringify(mystrNew).toString();
                geoConfigService.saveData(vm.spid, uid, json).then(function (data) {
                    if (data != null && data.success) {
                        notificationBarService.success("" + data.body);
                    }
                });
            } else {
                notificationBarService.warning("Please select the four points");
            }
        };

        function addMarker(location, map) {
            labels = markers.length + 1;
            if (labels > 4) return;

            var oX = 0;
            var oY = 0;
            if (labels === 1) {
                oX = 0;
                oY = 0;
            }
            if (labels === 2) {
                oX = jnifileW - 1;
                oY = 0;
            }
            if (labels === 3) {
                oX = jnifileW - 1;
                oY = jnifileH - 1;
            }
            if (labels === 4) {
                oX = 0;
                oY = jnifileH - 1;
            }

            latlongs.push({ "latitude": location.lat(), "longitude": location.lng() });
            coordinates.push({ "x": oX, "y": oY });
            var marker = new google.maps.Marker({
                position: location,
                draggable: true,
                clickable: true,
                label: "" + labels,
                map: map,
                'anchorPoint': new google.maps.Point(location.lat(), location.lng()),
                icon: iconBase,
                coord: coordinates,
            });

            google.maps.event.addListener(marker, 'dragstart', function () {
                disableMovement(true);
            });

            google.maps.event.addListener(marker, 'dragend', function () {
                disableMovement(false);
            });
            markers.push(marker);
        }

        function disableMovement(disable) {
            var mapOptions;
            if (disable) {
                mapOptions = {
                    draggable: false,
                    scrollwheel: false,
                    disableDoubleClickZoom: true,
                    zoomControl: false
                };
            } else {
                mapOptions = {
                    draggable: true,
                    scrollwheel: true,
                    disableDoubleClickZoom: false,
                    zoomControl: true
                };
            }
            map.setOptions(mapOptions);
        }

        vm.resetMap = function () {
            markers.forEach(function (marker) {
                marker.setMap(null);
            });

            markers = [];
            latlongs = [];
            coordinates = [];
            labels = '0';
        };

        function reloadMap(spid) {
            geoConfigService.getCanvasData(spid, vm.sid).then(function (data) {
                if (data !== '' && data.success) {
                    var s = JSON.parse(JSON.stringify(data.body));
                    reloadMarkers(s.latlng, s.mapzoom * 1);
                    ang = s.rotation;
                    zom = s.zoom;
                    opa = s.opacity;
                    jnifileW = s.width;
                    jnifileH = s.height;
                    vm.adjOpa(0);
                    vm.adjZom(0);
                    vm.adjRot(0);
                } else {
                    var testata = { "latlng": [{ "latitude": 12.958028, "longitude": 80.247511 }] };
                    var s = JSON.parse(JSON.stringify(testata));
                    reloadMarkers(s.latlng, 10);
                    zom = 80;
                    vm.adjZom(0);
                }
            });
        }

        function reloadMarkers(map_data, mapzom) {
            if (!searchBox) {
                var input = document.getElementById('pac-input');
                searchBox = new google.maps.places.SearchBox(input);
                map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);
                map.addListener('bounds_changed', function () {
                    searchBox.setBounds(map.getBounds());
                });

                searchBox.addListener('places_changed', function () {
                    var places = searchBox.getPlaces();
                    if (places.length === 0) {
                        return;
                    }
                    markers.forEach(function (marker) {
                        marker.setMap(null);
                    });
                    markers = [];
                    labels = 0;
                    var bounds = new google.maps.LatLngBounds();
                    places.forEach(function (place) {
                        if (!place.geometry) {
                            return;
                        }
                        var icon = {
                            url: place.icon,
                            size: new google.maps.Size(71, 71),
                            origin: new google.maps.Point(0, 0),
                            anchor: new google.maps.Point(17, 34),
                            scaledSize: new google.maps.Size(25, 25)
                        };

                        if (place.geometry.viewport) {
                            bounds.union(place.geometry.viewport);
                        } else {
                            bounds.extend(place.geometry.location);
                        }
                    });
                    map.fitBounds(bounds);
                });
            }

            google.maps.event.addListener(map, 'click', function (event) {
                addMarker(event.latLng, map);
            });

            google.maps.event.addListener(map, 'dblclick', function (event) {
                return;
            });

            var myLatLng;
            var latlongs = map_data;
            var labels = 0;
            var bounds = new google.maps.LatLngBounds();
            for (var i = 0; i < latlongs.length; i++) {
                labels++;
                myLatLng = new google.maps.LatLng(latlongs[i].latitude, latlongs[i].longitude);
                coordinates.push({ "x": 0, "y": 0 });
                var marker = new google.maps.Marker({
                    position: myLatLng,
                    draggable: true,
                    clickable: true,
                    labelClass: "",
                    label: "" + labels,
                    map: map,
                    'anchorPoint': new google.maps.Point(myLatLng.lat(), myLatLng.lng()),
                    icon: iconBase,
                    coord: coordinates,
                });
                bounds.extend(marker.getPosition());

                google.maps.event.addListener(marker, 'dragstart', function () {
                    disableMovement(true);
                });

                google.maps.event.addListener(marker, 'dragend', function () {
                    disableMovement(false);
                });
                markers.push(marker);
            }

            if (markers.length > 0)
                map.fitBounds(bounds);
            map.setZoom(mapzom + 2);
            $('#planimg').delay(500).fadeIn(('slow'));
        }

        vm.adjOpa = function (v) {
            opa = opa * 1 + v * 1;
            opa = opa.toFixed(1);
            if (opa >= 1) {
                opa = 1;
                $('#op').removeClass("clkyes");
                $('#op').addClass("clkno");
            } else {
                $('#op').removeClass("clkno");
                $('#op').addClass("clkyes");
            }
            if (opa <= 0.1) {
                opa = 0.1;
                $('#om').removeClass("clkyes");
                $('#om').addClass("clkno");
            } else {
                $('#om').removeClass("clkno");
                $('#om').addClass("clkyes");
            }

            $('#map').css("opacity", opa);
        };

        vm.adjZom = function (v) {
            zom = zom * 1 + v * 1;
            if (zom >= 100) {
                zom = 100;
                $('#zp').removeClass("clkyes");
                $('#zp').addClass("clkno");
            } else {
                $('#zp').removeClass("clkno");
                $('#zp').addClass("clkyes");
            }
            if (zom <= 1) {
                zom = 1;
                $('#zm').removeClass("clkyes");
                $('#zm').addClass("clkno");
            } else {
                $('#zm').removeClass("clkno");
                $('#zm').addClass("clkyes");
            }

            if (jnifileW === undefined) {
                jnifileW = 1000;
            }
            if (jnifileH === undefined) {
                jnifileH = 600;
            }
            var pW = jnifileW * zom / 100;
            var pH = jnifileH * zom / 100;

            $('#planimg').css("width", pW + "px");
            $('#planimg').css("height", pH + "px");

            oWidth = pW;
            oHeight = pH;
        };

        function getScaledWidth(x) {
            var x1 = 0, deltaWidth = 0;
            var widthScaleUp = false;
            deltaWidth = oWidth;
            if (width > oWidth) {
                widthScaleUp = true;
                deltaWidth = width / oWidth;
            } else if (width < oWidth) {
                widthScaleUp = false;
                deltaWidth = oWidth / width;
            }

            if (widthScaleUp) {
                x1 = Math.round(x / deltaWidth);
            } else {
                x1 = Math.round(x * deltaWidth);
            }
            return x1;
        }

        function getScaledHeight(y) {
            var y1 = 0, deltaHeight = 0;
            var heightScaleUp = false;
            // calculate the scaling factor for height
            if (height > oHeight) {
                heightScaleUp = true;
                deltaHeight = height / oHeight;
            } else if (height < oHeight) {
                heightScaleUp = false;
                deltaHeight = oHeight / height;
            }
            if (heightScaleUp) {
                y1 = Math.round(y / deltaHeight);
            } else {
                y1 = Math.round(y * deltaHeight);
            }
            return y1;
        }

        vm.adjRot = function (v) {
            ang = ang * 1 + v * 1;
            if (ang > 359) ang = 0;
            if (ang < 0) ang = 359;
            $('#planimg').css("transform", "translate(-50%, -50%) rotate(" + ang + "deg)");
            $('#planimg').css("-webkit-transform", "translate(-50%, -50%) rotate(" + ang + "deg)");
        };

        return vm;
    }
})();