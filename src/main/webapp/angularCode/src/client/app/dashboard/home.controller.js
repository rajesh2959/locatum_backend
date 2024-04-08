(function () {
    'use strict';
    angular
        .module('app.dashboard')
        .controller('DashboardHomeController', controller);
    controller.$inject = ['dashboardDataService', 'venuedataservice', 'geoFenceService', '$rootScope', 'environment', 'session', 'modalService', 'navigation', 'venuesession', '$interval', '$linq'];

    /* @ngInject */
    function controller(dashboardDataService, venuedataservice, geoFenceService, $rootScope, env, session, modalService, navigation, venuesession, $interval, $linq) {
        var vm = this;
        var baseUrl = env.serverBaseUrl;
        vm.floors = [];
        vm.chartColors = ["#18A79D", "#F13E2D"];
        vm.color = ["#18A79D", "#F13E2D"];
        vm.sid = "";
        vm.venueDetails = {};
        vm.tagsChartdata = [];
        vm.connectedTagType = [];
        vm.inactiveTags = 0;
        vm.idleTags = 0;
        vm.activeTags = 0;
        vm.totalCheckedoutTags = 0;
        vm.floorsdata = {};
        vm.floorDetails = [];
        vm.venuedatalist = {};
        vm.venueDetailslist = [];
        vm.deviceList = [];
        vm.alertsData = [];

        vm.width = 40;
        vm.height = 40;
        vm.spid = '';
        vm.showFloorDropDown = false;
        var fnsize;
        var fzie = 30;
        var txty = 9;
        var gutterWidth;
        var gway = false;
        var extryexit = false;
        var locatum = true;
        var uniqueCat = [];
        var filterCategories = [];

        var tagsONOFF = 1;
        var inactiveONOFF = 1;
        var switchONOFF = 1;
        var toggleDevice;

        var svg = d3.select('svg');
        if (venuesession.sid) {
            vm.sid = venuesession.sid;
        }

        $('.catFilter .multiselect-ui').find('option').remove();

        $('.catFilter .multiselect-ui').multiselect({
            includeSelectAllOption: true
        });

        $('.catFilter .multiselect-ui').multiselect('rebuild');

        $('.catFilter .multiselect-ui').multiselect('selectAll', true);

        $('.catFilter .multiselect-ui').change(function () {
            filterCategories = [];
            d3.selectAll('.person').classed('tagdisable', true);
            $.each($(".catFilter .multiselect-ui option:selected"), function () {
                filterCategories.push($(this).val());
            });
            if (tagsONOFF == 1) {
                $.each(filterCategories, function (index, val) {
                    if (inactiveONOFF == 1) {
                        d3.selectAll('.person.' + val).classed('tagdisable', false);
                    }
                    else {
                        d3.selectAll('.person.inactive').classed('tagdisable', true);
                        d3.selectAll('.person.active.' + val).classed('tagdisable', false);
                        d3.selectAll('.person.idle.' + val).classed('tagdisable', false);
                    }
                });
            }
            else {
                d3.selectAll('.person').classed('tagdisable', true);
            }
        });

        function initializeData() {
            vm.tags = ['idleTags', 'activeTags', 'inactTags'];
            vm.labels = ['Idle Tags', 'Active Tags', 'Inact Tags'],
                vm.lineColors = ['#B176E9', '#1caf9a', '#F13E2D'];
        };

        function loadServiceQueue() {
           
           dashboardDataService.getVenue(vm.cid, true).then(function (res) {

                vm.venuedatalist = res;
                vm.venueDetailslist = vm.venuedatalist.site;
                if (vm.venueDetailslist.length > 0) {
                    angular.forEach(vm.venueDetailslist, function (value, key) {
                        if (value.name) {
                            if (value.name.length > 21)
                                value.newName = value.name.substr(0, 20) + "...";
                            else
                                value.newName = value.name.substr(0, 20)
                        }
                        else
                            value.newName = "";
                    });
                    vm.onVenueChanges(vm.venueDetailslist[0].id);
                }
               
            });

        };

        vm.onFloorChanges = function (spid) {
            vm.spid = spid;
            vm.floorchangeBinding();
        };

         vm.onVenueChanges = function (sid) {
            
            vm.sid = sid;
            console.log("sid changes" + sid);
            vm.selectedVenue = vm.sid;
            

             venuedataservice.getVenueDetailById(vm.sid).then(function (res) {
                vm.venueDetails = res;
                if (vm.venueDetails) {
                    if (vm.venueDetails.uid) {
                        if (vm.venueDetails.uid.length > 21)
                            vm.venueDetails.newUid = vm.venueDetails.uid.substr(0, 20) + "...";
                        else
                            vm.venueDetails.newUid = vm.venueDetails.uid.substr(0, 20)
                    }
                    else
                        vm.venueDetails.newUid = "";
                }
            });

            
            
            dashboardDataService.getTags(vm.sid, true).then(function (res) {
                vm.tagsChartdata = res;
                if (res) {
                    if (res.inactiveTags)
                        vm.inactiveTags = res.inactiveTags;
                    if (res.idleTags)
                        vm.idleTags = res.idleTags;
                    if (res.activeTags)
                        vm.activeTags = res.activeTags;
                    if (res.totalCheckedoutTags)
                        vm.totalCheckedoutTags = res.totalCheckedoutTags;

                    angular.forEach(res.floorVsTraffic, function (v, key) {
                        vm.floorVsTraffic.data.rows.push({
                            "c": [                               
                                {
                                    "v": v.Status
                                },
                                 {
                                    "v": v.activeTags
                                },
                                {
                                    "v": v.inactTags
                                },
                                {
                                    "v": v.idleTags
                                }
                                                                                                
                            ]
                        });
                       
                    });

                    angular.forEach(res.connectedTagType, function (v, key) {
                        if (v.tagType !== 'Tag') {
                            vm.connectedTagType.data.rows.push({
                                "c": [
                                    {
                                        "v": v.tagType
                                    },
                                    {
                                        "v": v.tagCount
                                    }
                                ]
                            });
                        }
                    });
                }
            });

            vm.goToFT = function () {
                modalService.ftModal(vm.tagsChartdata).result.then(function (res) {
                    // vm.getAlerts();
                }, function () {
                });
            }

            vm.goToCT = function () {
                modalService.ctModal(vm.tagsChartdata).result.then(function (res) {
                	
                    // vm.getAlerts();
                }, function () {
                });
            }
            
            
            dashboardDataService.getFloor(vm.sid, true).then(function (res) {

                vm.floorsdata = res;
                vm.floorDetails = vm.floorsdata.portion;
                if (vm.floorDetails.length > 0) {
                    angular.forEach(vm.floorDetails, function (value, key) {
                        if (value.name) {
                            if (value.name.length > 21)
                                value.newName = value.name.substr(0, 20) + "...";
                            else
                                value.newName = value.name.substr(0, 20)
                        }
                        else
                            value.newName = "";
                    });
                    vm.onFloorChanges(vm.floorDetails[0].id);
                }
                else if (vm.floorDetails.length === 0) {

                    var message = "<p>There are no floors for this venue. Please add a floor to view details.</p>";
                    modalService.messageModal('Information', message).result.then(function () {
                    });
                }
            });

            getAlerts();
            
        };

        vm.floorchangeBinding = function () {
            vm.switchONOFF = true;
            vm.tagsONOFF = true;
            vm.inactiveONOFF = true;
            venuesession.create(venuesession.sid, vm.spid);
            vm.selectedFloor = vm.spid;
            vm.getFloorImage(vm.spid);
        };

        vm.getFloorImage = function (spid) {
            getPortion(spid);
        };

        vm.connectedTagType = {
            "type": "PieChart",
            "cssStyle": "height:180px; width:100%;font-family:Roboto !important;font-size:13px;",
            "data": {
                "cols": [
                     {
                        "id": "gender",
                        "label": "Gender",
                        "type": "string",
                        "p": {}
                    },
                    {
                        "id": "male-id",
                        "label": "m",
                        "type": "number",
                        "p": {}
                    },
                    {
                        "id": "female-id",
                        "label": "Desktop",
                        "type": "number",
                        "p": {}
                    }                 
                ],
                "rows": []
            },
            "options": {
                titleTextStyle: { position: 'left', alignment: 'left', fontSize: 14, fontName: 'Roboto', fontWeight: 'bold' },
                title: 'Connected Tag Type',
                "isStacked": "true",
                is3D: true,
                "fill": 20,
                "displayExactValues": true,
                "colors": ['#18a79d', '#f13e2d','#2980C6'],
                "legend": { position: 'right', alignment: 'center', width: '50px' },
                chartArea: {
                    width: '100%',
                    right: 0,   // set this to adjust the legend width
                    left: 0,     // set this eventually, to adjust the left margin
                },
            },
            "formatters": {},
            "displayed": true
        };

        vm.floorVsTraffic = {
            "type": "ColumnChart",
            "cssStyle": "height:150px; width:100%;font-family:Roboto !important",
            "data": {
                "cols": [
                    {
                        "id": "gender",
                        "label": "idleTags",
                        "type": "string",
                        "p": {}
                    },
                    {
                        "id": "male-id",
                        "label": "ActiveTags",
                        "type": "number",
                        "p": {}
                    },
                    {
                        "id": "female-id",
                        "label": "InactiveTags",
                        "type": "number",
                        "p": {}
                    },
                     {
                        "id": "male-id",
                        "label": "IdleTags",
                        "type": "number",
                        "p": {}
                    },
                ],
                "rows": []
            },
            "options": {
                titleTextStyle: { position: 'left', alignment: 'left', fontSize: 14, fontName: 'Roboto', fontWeight: 'bold' },
                title: 'Floor vs Traffic',
                "isStacked": "true",
                "fill": 20,
                "displayExactValues": true,
                "colors": ['#008000', '#FF8000', '#F13E2D'],
                "legend": { position: 'bottom', alignment: 'center', width: '10px' },
                    hAxis: {
                    title: 'Floors',
                     textStyle : {
                        fontName:'Roboto',
                    fontSize: 17 // or the number you want
                        }
                      },
                    vAxis: {
                    title: 'Tag count',
                    fontName: 'Roboto',
                    },
                chartArea: {
                    width: '100%',
                    right: 0,   // set this to adjust the legend width
                    left: 40,     // set this eventually, to adjust the left margin
                },
            },
           
            "formatters": {},
            "displayed": true
        };

        vm.loadShapes = function (shapes) {
            if (shapes && shapes.length > 0) {
                angular.forEach(shapes, function (value, key) {
                    try {
                        var shape;
                        var shp = value;
                        var x = shp.uiCoordinates.left;
                        var y = shp.uiCoordinates.top;
                        value.uiCoordinates.left = 0;
                        value.uiCoordinates.top = 0;
                        switch (value.uiCoordinates.shapeType) {
                            case 1:
                                shape = new fabric.Rect(value.uiCoordinates);
                                break;
                            case 2:
                                shape = new fabric.Circle(value.uiCoordinates);
                                shape.hasRotatingPoint = false;
                                break;
                            case 3:
                                shape = new fabric.Triangle(value.uiCoordinates);
                                break;
                            default:
                                break;
                        }
                        shape.selectable = false;

                        var canvasElement = document.createElement('canvas');
                        var canvas = new fabric.Canvas(canvasElement);
                        canvas.setDimensions({ width: shp.uiCoordinates.width + 2, height: shp.uiCoordinates.height + 2 }, { backstoreOnly: false, cssOnly: false });

                        canvas.skipOffscreen = false;
                        canvas.add(shape);

                        var img = canvas.toDataURL("image/png");

                        var anchor = svg.selectAll('.uiFloorGroupOuter');
                        var mainGroup = anchor.append("image")
                            .attr({
                                'x': x,
                                'y': y,
                                'width': shp.uiCoordinates.width + 2,
                                'height': shp.uiCoordinates.height + 2,
                                'xlink:href': img
                            });

                    } catch (error) {

                    }
                });
            }
        };

        vm.getdata = function () {
            geoFenceService.getGeoFenceList(vm.spid)
                .then(function (result) {
                    result = $linq.Enumerable().From(result)
                        .Where(function (x) {
                            return x.status === "enabled";
                        }).ToArray();

                    angular.forEach(result, function (value, key) {
                        value.uiCoordinates = JSON.parse(value.uiCoordinates);
                    });
                    vm.loadShapes(result);
                    getDevices(vm.spid, gway, extryexit, locatum);
                });
        };

        function getAlerts() {
            venuedataservice.getVenueRecentAlert(vm.sid).then(function (res) {
                if (res) {
                    angular.forEach(res, function (value, key) {
                        var type = value.type.toLowerCase();
                        value.imageType = baseUrl + '/images/alerts/' + type + '.png';
                    });
                    vm.alertsData = res;
                }
            });
        }

        	
        vm.goToAlertData = function () {
            modalService.alertModal(vm.alertsData).result.then(function (res) {
                // vm.getAlerts();
            }, function () {
            });
        }
        
        function getPortion(spid) {
            dashboardDataService.getPortion(spid, vm.sid).then(function (res) {
                if (res) {
                    vm.width = res.body.width;
                    vm.height = res.body.height;
                    // vm.translatewidth= 'translate'+vm.width ;
                    zoomPanel(spid);
                    vm.getdata(vm.spid);
                }
            });
        }

        function getDevices(spid, p1, p2, p3) {
            var taginfo;
            var person;
            dashboardDataService.getDeviceList(spid).then(function (res) {
            	vm.deviceList = [];
                gway = p1;
                extryexit = p2;
                locatum = p3;
                vm.deviceList = res;
                var devices = res;

                var ii = 0;
                var type;
                for (ii = 0; ii < devices.length; ii++) {
                    if (devices[ii].parent === "ble")
                        type = "sensor";
                    else
                        type = devices[ii].typefs;

                    var image;

                    var status = devices[ii].status;
                    var source = devices[ii].source;

                    if (source !== "guest") {
                        image = "../images/networkicons/" + type + "_" + status + ".png";
                    } else {
                        image = "../images/networkicons/" + "guestSensor_inactive.png";
                    }

                    var uid = devices[ii].uid;
                    var bleType = devices[ii].bleType;
                    var alias = devices[ii].alias;

                    if (p3 === "false") {
                        taginfo = devices[ii].tagstring;
                        person = devices[ii].activetag;
                    }

                    plantDevices(spid, image, type, devices[ii].xposition, devices[ii].yposition, status, uid, person, taginfo, p1, p2, p3, toggleDevice, bleType, alias);
                }

                if (p3 === true) {
                    if ($rootScope.plantDevicesTagsinterval)
                        $interval.cancel($rootScope.plantDevicesTagsinterval);
                    $rootScope.plantDevicesTagsinterval = $interval(function () {
                        plantDevicesTags(spid, p1, p2, p3);
                    }, 1000);
                }
            });
        }

        function zoomPanel(spid) {
            var imagePath = baseUrl + "/web/site/portion/planfile?spid=" + spid + "&cid=" + session.cid + "&time=" + new Date();
            var svgNew = d3.select('svg.floorsvg').selectAll('.uiFloorGroupOuter');
            var imageFound = document.getElementById('bgimage');
            if (imageFound == null) {
                svgNew.insert("image")
                    .attr({
                        'x': 0,
                        'y': 0,
                        'xlink:href': imagePath,
                        'height': vm.height,
                        'width': vm.width,
                        'id': 'bgimage'
                    });
            }
            else {
                svgNew.selectAll("image").remove();
                svgNew.insert("image")
                    .attr({
                        'x': 0,
                        'y': 0,
                        'xlink:href': imagePath,
                        'height': vm.height,
                        'width': vm.width,
                        'id': 'bgimage'
                    });
            }
            var width = vm.width;
            var uiFloorWrpWidth = $('.canvas-container').innerWidth();
            var twidth = (uiFloorWrpWidth - width) / 2;
            // transform: matrix(1, 0, 0, 1, -497, -39);


            if (twidth > 0) {
                svgNew.attr('transform', 'matrix(' + twidth + ',10)');
            }
            var el = document.getElementById('enlarge'),
                elClone = el.cloneNode(true);
            el.parentNode.replaceChild(elClone, el);
            document.getElementById('enlarge').addEventListener('click', function (ev) {
                vm.showFloorDropDown = !vm.showFloorDropDown;
                ev.preventDefault();
                $('.floorCanvas').toggleClass('deviceexpand');
                $('.floorsvgSmall').toggleClass('floorsvgExpand');
                $('.floorsvg').toggleClass('svgExpand');
                var svgNew = d3.select('svg.floorsvg').selectAll('.uiFloorGroupOuter');
                var uiFloorWrpWidth = $('.canvas-container').innerWidth();
                var twidth = (uiFloorWrpWidth - width) / 2;
                if (twidth > 0) {
                    svgNew.attr('transform', 'matrix(' + twidth + ',10)');
                }
            });
            document.getElementById('enlarges').addEventListener('click', function (ev) {
                ev.preventDefault();
                $('.resizecontent').toggleClass('deviceexpands');     
            });
            var beforePan = function (oldPan, newPan) {
                var zoom = this.getSizes().realZoom;
                zoom = zoom / 5;
                gutterWidth = 100 * zoom;
                var stopHorizontal = false
                    , sizes = this.getSizes()
                    , stopVertical = false
                    , gutterHeight = 100

                    , leftLimit = -((vm.width) * sizes.realZoom) + gutterWidth
                    , rightLimit = vm.width - gutterWidth - (sizes.viewBox.x * sizes.realZoom)
                    , topLimit = -((sizes.viewBox.y + vm.height) * sizes.realZoom) + gutterHeight
                    , bottomLimit = sizes.height - gutterHeight - (sizes.viewBox.y * sizes.realZoom)
                    // , leftLimit = -((sizes.viewBox.x + sizes.viewBox.width) * sizes.realZoom) + gutterWidth
                    // , rightLimit = sizes.width - gutterWidth - (sizes.viewBox.x * sizes.realZoom)
                    // , topLimit = -((sizes.viewBox.y + sizes.viewBox.height) * sizes.realZoom) + gutterHeight
                    // , bottomLimit = sizes.height - gutterHeight - (sizes.viewBox.y * sizes.realZoom)
                    
                var customPan = {};
                customPan.x = Math.max(leftLimit, Math.min(rightLimit, newPan.x));
                customPan.y = Math.max(topLimit, Math.min(bottomLimit, newPan.y));
                return customPan;
            };
            var panZoom;
            window.initialize = function () {
                panZoom = svgPanZoom('#flrmap', {
                    zoomEnabled: true,
                    zoomScaleSensitivity: .2,
                    minZoom: 1,
                    maxZoom: 100,
                    panEnabled: true,
                    contain: false,
                    controlIconsEnabled: false,
                    fit: false,
                    center: false,
                    beforePan: beforePan,
                    onZoom: function (e) {
                        floorChange(e);
                    }
                });
            };
            initialize();
            document.getElementById('zoom-in').addEventListener('click', function (ev) {
                ev.preventDefault();
                panZoom.zoomIn();
            });
            document.getElementById('zoom-out').addEventListener('click', function (ev) {
                ev.preventDefault();
                panZoom.zoomOut();

            });
            document.getElementById('reset').addEventListener('click', function (ev) {
                ev.preventDefault();
                panZoom.destroy();
                initialize();
            });

            function floorChange(e) {
                fnsize = (10 / e) + 3;
                var newSize = e / 3;
                var cyposition = 9;
                var image = d3.select("svg.floorsvg").selectAll(".animatedImage");
                if (newSize >= 20) {
                    image.attr('width', '3px')
                        .attr('height', '3px');
                }
                else if (newSize >= 15) {
                    image.attr('width', '6px')
                        .attr('height', '6px');
                }
                else if (newSize >= 10) {
                    image.attr('width', '8px')
                        .attr('height', '8px');
                }
                else if (newSize >= 5) {
                    image.attr('width', '10px')
                        .attr('height', '10px');
                }
                else if (newSize >= 3) {
                    image.attr('width', '15px')
                        .attr('height', '15px');
                }
                else if (newSize >= 1) {
                    image.attr('width', '20px')
                        .attr('height', '20px');
                }
                else if (newSize >= .5) {
                    image.attr('width', '20px')
                        .attr('height', '20px');
                }
                else if (newSize >= .3) {
                    image.attr('width', '40px')
                        .attr('height', '40px');
                }
                else {
                    image.attr('width', '40px')
                        .attr('height', '40px');
                }

                if (newSize >= 20) {
                    fnsize = 1;
                    cyposition = .3;
                }
                else if (newSize >= 10) {
                    fnsize = 2;
                    cyposition = .8;
                }
                else if (newSize >= 5) {
                    fnsize = 3;
                    cyposition = 1;
                }
                else if (newSize >= 2) {
                    fnsize = 6;
                    cyposition = 2;
                }
                else if (newSize >= 1) {
                    fnsize = 8;
                    cyposition = 3;
                }
                else if (newSize >= .5) {
                    fnsize = 15;
                    cyposition = 5;
                }
                else {
                    fnsize = 30;
                    cyposition = 9;
                }

                fzie = fnsize;
                txty = cyposition;
                var circle = d3.select("svg.floorsvg").selectAll("circle");
                circle.attr('r', fnsize);
                var txt = d3.select("svg.floorsvg").selectAll("text");
                txt.attr('font-size', fnsize + 'px');
                txt.attr('y', cyposition);
            }
        }

        function plantDevices(spid, image, type, x, y, status, uid, cnt, tag, p1, p2, p3, toggleDevice, bleType, alias) {
            var imageW = 40;
            var imageH = 40;

            var urlMap = {
                "server": 'dashboard',
                'switch': 'swiboard',
                'ap': 'devboard',
                'sensor': 'devboard'
            };
            var url = '';
            if (type === "server") {
                url = "/facesix/web/site/portion/" + urlMap[type] + "?sid=" + vm.sid + "&spid=" + spid + "&cid=" + session.cid + "&type=" + "server";
            } else if (type == "sensor") {
                url = "/facesix/web/finder/device/" + urlMap[type] + "?sid=" + vm.sid + "&spid=" + spid + "&uid=" + uid + "&cid=" + session.cid + "&type=" + "sensor";
            } else if (p1 == true) {
                url = "/facesix/web/site/portion/" + urlMap[type] + "?sid=" + vm.sid + "&uid=" + uid + "&cid=" + session.cid + "&type=" + (type == "switch" || type == "server" ? type : "device") + "&spid=" + spid;
            }

            var anchor = svg.append("a");
            var mcId = 'devices-' + uid;
            mcId = mcId.replace(/:/g, "-");
            var deviceFound = document.getElementById(mcId);
            if (deviceFound == null) {
                anchor = svg.append("a").attr("onclick", "return false;");
                if (switchONOFF == 1 || toggleDevice == 1) {
                    var newImage = anchor.append("image")
                        .attr({
                            'x': x,
                            'y': y,
                            'xlink:href': image,
                            'status': status,
                            'height': imageH,
                            'width': imageW,
                            'class': 'animatedImage',
                            'data-uid': uid,
                            'id': mcId,
                            'type': type
                        });

                    var ble = "";
                    if (bleType)
                        ble = bleType.toUpperCase();
                    $(anchor[0]).appendTo('.uiFloorGroupOuter')
                        .attr('title', 'Device Type:' + ble + ' UID:' + uid + ' Location:' + alias);
                    $(anchor).tooltip({ container: 'body' });
                }
            }
        }

        $("svg").on("click tap", "image", function (evt) {
            //$("svg").mouseenter(function () {
                $(".tooltip").css("display", "none");
           // });
            var uid = evt.currentTarget.getAttribute("data-uid");
            if (vm.deviceList !== undefined && vm.deviceList.length > 0) {
                var selectedRoomList = $linq.Enumerable().From(vm.deviceList)
                    .Where(function (x) {
                        return x.uid === uid;
                    }).FirstOrDefault();
                if (selectedRoomList !== undefined)
                    vm.selectedRoom = selectedRoomList.parent.toUpperCase() + "-" + selectedRoomList.alias;
            };
            if (vm.floorDetails !== undefined && vm.floorDetails.length > 0) {
                var selectedFloorList = $linq.Enumerable().From(vm.floorDetails)
                    .Where(function (x) {
                        return x.id === vm.selectedFloor;
                    }).FirstOrDefault();
                if (selectedFloorList !== undefined)
                    vm.selectedFloorname = selectedFloorList.name;
            };
            var navDetail = {};
            navDetail.venue = vm.venueDetails.uid;
            navDetail.floor = vm.selectedFloorname;
            navDetail.room = vm.selectedRoom;
            localStorage.setItem("prevPageInfo", JSON.stringify(navDetail));
            navigation.goToGatewayInfo(uid, vm.sid, vm.spid);
        });

        function plantDevicesTags(spid, p1, p2, p3) {
            var callTime = new Date();
            var milli = callTime.getTime();
            dashboardDataService.getPersonInfo(spid, milli).then(function (res) {
                var jsonString = JSON.parse(res);

                personReset(jsonString, spid);
            });
        }

        function personReset(res, spid) {
            var list = res;
            $('.person').each(function () {
                var dataid = $(this).attr('data-id');
                if (dataid !== spid) {
                    $(this).remove();
                }
            });
            fzie = 25;

            if (typeof list !== "undefined" && list !== "") {

                var category = [];
                var state, tagtype, tagsCounter;

                for (var i = 0; i < list.length; i++) {
                    var tagcolor, strkcolor, color, bgColor;
                    var tags = list[i];
                    if (tags.spid == spid) {
                        category.push(tags.tagType);
                        var mcId = 'tags-' + tags.macaddr;
                        mcId = mcId.replace(/:/g, "-");
                        var tagsFound = document.getElementById(mcId);

                        if ($("g[data-x='" + tags.x + "'][data-y='" + tags.y + "']").length !== 0) {
                            var newElement = $("g[data-x='" + tags.x + "'][data-y='" + tags.y + "']").attr('id');
                            if (newElement !== mcId) {
                                tags.x = parseInt(tags.x) + getRandomFloat(-1, 1);
                                tags.y = parseInt(tags.y) + getRandomFloat(-1, 1);
                            }
                        }

                        if (tags.state !== undefined && tags.state !== "") {
                            state = tags.state;
                        }
                        if (state == "active") {
                            tagcolor = "yellow";
                            strkcolor = "green";
                            color = "#3d3ef7";
                            bgColor = "rgba(70, 191, 189, 0.5)";
                        } else if (state == "inactive") {
                            tagcolor = "gray";
                            strkcolor = "red";
                            color = "#051a08";
                            bgColor = "rgba(246, 70, 75, 0.5)";
                        } else if (state == "idle") {
                            tagcolor = "yellow";//"#FFA500";
                            strkcolor = "orange";
                            color = "#381a08";
                            bgColor = "rgba(240, 114, 0, 0.5)";
                        }
                        var myIconColor = '#fff';
                        var tagType = tags.tagType;

                        if (tagType == 'Contractor') {
                            myIconColor = 'cyan';
                        } else if (tagType == 'Employee') {
                            myIconColor = 'lime';
                        } else if (tagType == 'Visitor') {
                            myIconColor = 'forestgreen';
                        }

                        if (tags.tag_type !== undefined && tags.tag_type !== "") {
                            tagtype = dashboardDataService.updateTagType(tags.tag_type);
                        }

                        var date = tags.lastReportingTime;
                        if (date == null || date == undefined) {
                            date = "Not Seen";
			}
                        else {
                            date = moment(date).format('YYYY-MM-DD HH:mm:ss.SSS');                            
                        }

                        if (tagsFound == null) {
                            var svgTag = svg.selectAll('.uiFloorGroupOuter');
                            var mainGroup = svgTag.append('g')
                                .attr('id', mcId)
                                .attr("fill", bgColor)
                                .attr("class", "person animateZoom " + state + " " + tags.tag_type) //+ tags.tag_type
                                .attr('data-id', spid)
                                .attr('data-x', tags.x)
                                .attr('data-y', tags.y)
                                .attr("info", "Name:" + tags.assignedTo + " <br/> Last Seen:" + date + "<br/> Location :" + tags.reciveralias)
                                .attr('transform', "translate(" + tags.x + "," + tags.y + ")")
                                .attr('data-html', 'true');
                            $(mainGroup).tooltip({ container: 'body' });

                            var subGroup = mainGroup.append('g')
                                .attr('id', mcId + '-sub')
                                .attr('transform', 'translate(0,0)')
                                .attr("class", "onlyscale");

                            subGroup.append("circle")
                                .attr("r", fzie)
                                .attr("y", "0").
                                attr("class", "animateZoomCircle");

                            subGroup.append("text")
                                .attr("alignment-baseline", 'middle')
                                .attr("font-family", "FontAwesome")
                                .style("fill", myIconColor)
                                .style("cursor", "pointer")
                                .attr("text-anchor", "middle")
                                .attr("y", txty)
                                .attr('font-size', function (d) { return fzie + 'px'; })
                                .text(function (d) { return tagtype; });

                            tagsCounter = tagsCounter + 1;

                        } else {
                            svg.selectAll('#' + mcId).transition()
                                .duration(1000)
                                .attr("fill", bgColor)
                                .attr('transform', "translate(" + tags.x + "," + tags.y + ")")
                                .attr('data-original-title', "Name:" + tags.assignedTo + " <br/> Last Seen:" + date + "<br/> Location :" + tags.reciveralias);
                            svg.selectAll('#' + mcId).attr('data-x', tags.x);
                            svg.selectAll('#' + mcId).attr('data-y', tags.y);
                            svg.selectAll('#' + mcId).attr("class", "person animateZoom " + state + " " + tags.tag_type);
                        }
                    }
                }
            }
            var uniqueArray = function (arrArg) {
                return arrArg.filter(function (elem, pos, arr) {
                    return arr.indexOf(elem) == pos;
                });
            };

            function containsAny(source, target) {
                var result = source.filter(function (item) { return target.indexOf(item) > -1 });
                return (result.length > 0);
            }

            $('.catFilter .multiselect-ui option').each(function () {
                uniqueCat.push(this.value);
            });
            $.each(uniqueArray(category), function (index, optionValue) {

                if (containsAny(category, uniqueCat) === false) {
                    $('.catFilter .multiselect-ui').append($('<option selected></option>').val(optionValue).html(optionValue));
                }
            });


            $('.catFilter .multiselect-ui').multiselect('rebuild');

            d3.selectAll('.person').classed('tagdisable', true);
            $.each($(".catFilter .multiselect-ui option:selected"), function () {
                filterCategories.push($(this).val());
            });
            if (tagsONOFF == 1) {
                $.each(filterCategories, function (index, val) {
                    if (inactiveONOFF == 1) {
                        d3.selectAll('.person.' + val).classed('tagdisable', false);
                    }
                    else {
                        d3.selectAll('.person.inactive').classed('tagdisable', true);
                        d3.selectAll('.person.active.' + val).classed('tagdisable', false);
                        d3.selectAll('.person.idle.' + val).classed('tagdisable', false);
                    }
                });
            }
            else {
                d3.selectAll('.person').classed('tagdisable', true);
            }

            d3.selectAll('.animatedImage').classed('tagdisable', true);

            if (switchONOFF == 1) {
                d3.selectAll('.animatedImage').classed('tagdisable', false);
            }
            else {
                d3.selectAll('.animatedImage').classed('tagdisable', true);
            }
        }

        function getRandomFloat(min, max) {
            return Math.random() * (max - min) + min;
        }

        $('#switchONOFF').change(function () {
            if ($(this).prop('checked') == true) {
                switchONOFF = 1;
                toggleDevice = switchONOFF;
                d3.selectAll('.animatedImage').classed('tagdisable', false);
                getDevices(vm.spid, toggleDevice);
            }
            else {
                switchONOFF = 0;
                toggleDevice = switchONOFF;
                d3.selectAll('.animatedImage').classed('tagdisable', true);
                getDevices(vm.spid, toggleDevice);
            }
        });

        $('#tagsONOFF').change(function () {
            if ($(this).prop('checked') == true) {
                tagsONOFF = 1;
                d3.selectAll('.person').classed('tagdisable', false);
                if (inactiveONOFF == 1) {
                    $("#inactiveONOFF").prop("checked", true);
                    d3.selectAll('.person.inactive').classed('tagdisable', false);
                }
                else {
                    d3.selectAll('.person.inactive').classed('tagdisable', true);
                }
            }
            else {
                tagsONOFF = 0;
                d3.selectAll('.person').classed('tagdisable', true);
                if (inactiveONOFF == 1)
                    $("#inactiveONOFF").prop("checked", false);
            }
        });

        $('#inactiveONOFF').change(function () {
            if (tagsONOFF == 1) {
                if ($(this).prop('checked') == true) {
                    inactiveONOFF = 1;
                    d3.selectAll('.person.inactive').classed('tagdisable', false);
                } else {
                    inactiveONOFF = 0;
                    d3.selectAll('.person.inactive').classed('tagdisable', true);
                }
            }
            else {
                d3.selectAll('.person').classed('tagdisable', true);
                $("#inactiveONOFF").prop("checked", false);
            }
        });

        function activate() {
            initializeData();
            loadServiceQueue();
        }

        activate();

        return vm;
    }
})();