(function () {
    'use strict';
    angular
        .module('app.tag')
        .controller('tagdashboardController', controller);
    controller.$inject = ['$rootScope', 'tagService', 'dashboardDataService', 'spid', 'macaddr', 'batterylevel', 'location', 'venuesession', 'environment', 'session', 'geoFenceService', '$linq', 'SimpleListScreenViewModel', 'notificationBarService', 'modalService', '$interval'];

    /* @ngInject */
    function controller($rootScope, tagService, dashboardDataService, spid, macaddr, batterylevel, location, venuesession, environment, session, geoFenceService, $linq, SimpleListScreenViewModel, notificationBarService, modalService, $interval) {
        var baseUrl = environment.serverBaseUrl;
        var vm = new SimpleListScreenViewModel();
        vm.macaddr = macaddr;
        vm.batterylevel = batterylevel;
        var svg = d3.select('svg');
        vm.width = 40;
        vm.height = 40;
        vm.pageHeight = "";
        var txty = 9;
        var gutterWidth;
        var fnsize;
        var fzie;
        vm.batteryInfo = batterylevel;
        vm.batterclass = "";
        vm.spid = spid;
        if (venuesession.sid) {
            vm.sid = venuesession.sid;
        }
        vm.floorName = location;


        vm.refreshtimer = [
            { "key": "1000", "value": "1 Sec" },
            { "key": "5000", "value": "5 Sec" },
            { "key": "10000", "value": "10 Sec" },
            { "key": "15000", "value": "15 Sec" },
            { "key": "20000", "value": "20 Sec" },
            { "key": "25000", "value": "25 Sec" },
            { "key": "30000", "value": "30 Sec" },
            { "key": "35000", "value": "35 Sec" },
            { "key": "40000", "value": "40 Sec" },
            { "key": "45000", "value": "45 Sec" },
            { "key": "50000", "value": "50 Sec" },
            { "key": "55000", "value": "55 Sec" },
            { "key": "60000", "value": "60 Sec" },
            { "key": "65000", "value": "65 Sec" },
            { "key": "70000", "value": "70 Sec" },
            { "key": "75000", "value": "75 Sec" },
            { "key": "80000", "value": "80 Sec" },
            { "key": "85000", "value": "85 Sec" },
            { "key": "90000", "value": "90 Sec" },
            { "key": "95000", "value": "95 Sec" },
            { "key": "100000", "value": "100 Sec" }

        ];

        vm.refreshSeconds = [
            { "key": "12h", "value": "Last 12 hours" },
            { "key": "1d", "value": "Last 1 Day" },
            { "key": "7d", "value": "Last 7 Days" },
            { "key": "15d", "value": "Last 15 Days" }
        ];

        vm.getInusedTags = function (refresh) {
            tagService.getTagDashboardlistfortable(macaddr, vm.timeRefreshInterval, refresh, vm.dataOperations, vm.filterFn)
                .then(function (result) {
                    vm.allInusedTagDetails = result.allData;
                    vm.pagedInusedTagDetails = result.pagedData;
                    vm.fullCount = result.dataCount;
                    vm.filteredCount = result.filteredDataCount;
                });
        };

        vm.downloadPDF = function(){
            tagService.downloadAsPDF(session.cid,macaddr);
        }

        vm.refresh = function (refresh) {
            vm.timeRefreshInterval = "12h";
            vm.getInusedTags(refresh);
        };

        if ($rootScope.usedTagsinterval)
            $interval.cancel($rootScope.usedTagsinterval);
        $rootScope.usedTagsinterval = $interval(function () {
            vm.getdevices(true);
        }, 1000);

        function activate() {
            vm.timeRefreshInterval = "12h";
            vm.floorrefreshtimer = "1000";
            vm.refresh(true);
            vm.getImageDetails();
        }

        vm.goToPage = function () {
            if (parseInt(vm.goToPageNumber) > vm.totalPageCount) {
                vm.goToPageNumber = '';
            } else {
                vm.dataOperations.paging.currentPage = vm.goToPageNumber;
                vm.getInusedTags();
                vm.goToPageNumber = '';
            }
        };

        vm.getImageDetails = function () {
            dashboardDataService.getPortion(vm.spid, vm.sid).then(function (res) {
                if (res) {
                    vm.width = res.body.width;
                    vm.height = res.body.height;
                    zoomPanel(vm.spid);
                    vm.getdata(vm.spid);
                }
            });
        };

        function zoomPanel(spid) {
            var imagePath = baseUrl + "/web/site/portion/planfile?spid=" + spid + "&cid=" + session.cid + "&time=" + new Date();
            var svgNew = d3.select('svg.floorsvg').selectAll('.uiFloorGroupOuter');
            var imageFound = document.getElementById('bgimage');
            if (imageFound == null) {
                var newImage = svgNew.insert("image")
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
                var newImage = svgNew.insert("image")
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
            if (twidth > 0) {
                svgNew.attr('transform', 'translate(' + twidth + ',10)');
            }

            var el = document.getElementById('enlarge'),
                elClone = el.cloneNode(true);
            el.parentNode.replaceChild(elClone, el);
            document.getElementById('enlarge').addEventListener('click', function (ev) {
                vm.showFloorDropDown = !vm.showFloorDropDown;
                if(vm.showFloorDropDown == true){
                   $('body,html').css('overflow','hidden');
                } else {
                   $('body,html').css('overflow','visible');
                }
                ev.preventDefault();
                $('.floorCanvas').toggleClass('deviceexpand');
                $('.floorsvgSmall').toggleClass('floorsvgExpand');
                $('.floorsvg').toggleClass('svgExpand');
                var svgNew = d3.select('svg.floorsvg').selectAll('.uiFloorGroupOuter');
                var uiFloorWrpWidth = $('.canvas-container').innerWidth();
                var twidth = (uiFloorWrpWidth - width) / 2;
                if (twidth > 0) {
                    svgNew.attr('transform', 'translate(' + twidth + ',10)');
                }
            });

            var beforePan;
            beforePan = function (oldPan, newPan) {
                var zoom = this.getSizes().realZoom;
                zoom = zoom / 5;
                gutterWidth = 100 * zoom;
                var stopHorizontal = false
                    , sizes = this.getSizes()
                    , stopVertical = false
                    , gutterHeight = 100
                    
                    // , leftLimit = -((sizes.viewBox.x + sizes.viewBox.width) * sizes.realZoom) + gutterWidth
                    // , rightLimit = sizes.width - gutterWidth - (sizes.viewBox.x * sizes.realZoom)
                    // , topLimit = -((sizes.viewBox.y + sizes.viewBox.height) * sizes.realZoom) + gutterHeight
                    // , bottomLimit = sizes.height - gutterHeight - (sizes.viewBox.y * sizes.realZoom)
                    , leftLimit = -((vm.width) * sizes.realZoom) + gutterWidth
                    , rightLimit = vm.width - gutterWidth - (sizes.viewBox.x * sizes.realZoom)
                    , topLimit = -((sizes.viewBox.y + vm.height) * sizes.realZoom) + gutterHeight
                    , bottomLimit = sizes.height - gutterHeight - (sizes.viewBox.y * sizes.realZoom)
                var customPan = {};
                customPan.x = Math.max(leftLimit, Math.min(rightLimit, newPan.x));
                customPan.y = Math.max(topLimit, Math.min(bottomLimit, newPan.y));

                alert(customPan.x);
                alert(customPan.y);

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
                    //beforePan: beforePan,
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
        }

        function floorChange(e) {
            var circleRadius = 10;
            var fSize = 10;
            fnsize = (10 / e) + 3;
            circleRadius = (10 / e) + 3;
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

        vm.getdata = function () {
            geoFenceService.getGeoFenceList(vm.spid)
                .then(function (result) {
                    result = $linq.Enumerable().From(result)
                        .Where(function (x) {
                            return x.status == "enabled"
                        }).ToArray();

                    angular.forEach(result, function (value, key) {
                        value.uiCoordinates = JSON.parse(value.uiCoordinates);
                    });
                    vm.loadShapes(result);
                    
                    if (vm.batteryInfo < 15) {
                        vm.color = "red";
                        vm.batteryclass = "fa fa-battery-empty fa-2x";
                    }
                    else if (vm.batteryInfo >= 15 && vm.batteryInfo < 25) {
                        vm.color = "red";
                        vm.batteryclass = "fa fa-battery-quarter fa-2x";
                    }
                    else if (vm.batteryInfo >= 25 && vm.batteryInfo < 50) {
                        vm.color = "orange";
                        vm.batteryclass = "fa fa-battery-half fa-2x";
                    }
                    else if (vm.batteryInfo >= 50 && vm.batteryInfo <= 75) {
                        vm.color = "green";
                        vm.batteryclass = "fa fa-battery-three-quarters fa-2x";
                    }
                    else if (vm.batteryInfo > 75) {
                        vm.color = "green";
                        vm.batteryclass = "fa fa-battery-full fa-2x";
                    }
                });
        };

        vm.bindImages = function (devices) {
            var type;
            angular.forEach(devices, function (device, key) {
                if (device.parent == "ble")
                    type = "sensor";
                else
                    type = device.typefs;

                var image = "../images/" + type + "_" + status + ".png";

                var status = device.status;
                var source = device.source;

                if (source != "guest") {
                    image = "../images/networkicons/" + type + "_" + status + ".png";
                } else {
                    image = "../images/networkicons/" + "guestSensor_inactive.png";
                }
                var uid = device.uid;
                var bleType = device.bleType;
                var alias = device.alias;

                var anchor = svg.append("a");
                var mcId = 'devices-' + uid;
                mcId = mcId.replace(/:/g, "-");
                var deviceFound = document.getElementById(mcId);
                if (deviceFound == null) {
                    anchor = svg.append("a").attr("onclick", "return false;");
                    var newImage = anchor.append("image")
                        .attr({
                            'x': device.xposition,
                            'y': device.yposition,
                            'xlink:href': image,
                            'height': 20,
                            'width': 20,
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
            });
        };

        vm.getdevices = function () {
            tagService.getNetworkDeviceList(vm.spid, vm.macaddr).then(function (result) {
                var jsonStringResult = JSON.parse(result);
                var devices = jsonStringResult.list;
                vm.bindImages(devices);
                if ($rootScope.bindTagsinterval)
                    $interval.cancel($rootScope.bindTagsinterval);
                $rootScope.bindTagsinterval = $interval(function () {
                    vm.bindTags(jsonStringResult.taglist);
                }, vm.floorrefreshtimer);
                floorChange(1);
            });
        };

        vm.loadShapes = function (shapes) {
            if (shapes && shapes.length > 0) {
                angular.forEach(shapes, function (value, key) {
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
                });
            }
            vm.getdevices();
        };

        vm.bindTags = function (tagList) {
            $('.person').each(function () {
                var dataid = $(this).attr('data-id');
                if (dataid !== spid) {
                    $(this).remove();
                }
            });
            angular.forEach(tagList, function (value, key) {
                var category = [];
                var state;
                var tagcolor;
                var strkcolor;
                var color, bgColor, myIconColor, tagtype, tagsCounter, fzie = 25;
                var tags = tagList[0];
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
                        tagcolor = "yellow";
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
                            .attr("class", "person animateZoom " + state + " " + tags.tagType) //+ tags.tag_type
                            .attr('data-id', spid)
                            .attr('data-x', tags.x)
                            .attr('data-y', tags.y)
                            .attr("info", "Name:" + tags.assignedto + " <br/> Last Seen:" + date + "<br/> Location :"+tags.location)
                            .attr('transform', "translate(" + tags.x + "," + tags.y + ")")
                            .attr('data-html', 'true');
                        $(mainGroup).tooltip({ container: 'body' });

                        var subGroup = mainGroup.append('g')
                            .attr('id', mcId + '-sub')
                            .attr('transform', 'translate(0,0)')
                            .attr("class", "onlyscale");

                        var circle = subGroup.append("circle")
                            .attr("r", fzie)
                            .attr("y", "0").
                            attr("class", "animateZoomCircle");
                        var txt = subGroup.append("text")
                            .attr("alignment-baseline", 'middle')
                            .attr("font-family", "FontAwesome")
                            .style("fill", myIconColor)
                            .style("cursor", "pointer")
                            .attr("text-anchor", "middle")
                            .attr("y", txty)
                            .attr('font-size', function (d) { return fzie + 'px'; })
                            .text(function (d) { return tags.tagtype; });

                        tagsCounter = tagsCounter + 1;

                    } else {
                        var macaddr = tags.macaddr;

                        if (macaddr == "3F:23:AC:22:FF:F3" || macaddr == "3F:23:AC:22:FF:F4") { // test

                            tags.x = tags.x * 1 + 5 + Math.floor(Math.random() * 200);
                            tags.y = tags.y * 1 + 13 + Math.floor(Math.random() * 200);
                            if (tags.x >= 1376) {
                                tags.x = 500;
                            }
                            if (tags.y >= 768) {
                                tags.y = 250;
                            }
                            bDemofound = true;
                        }
                        svg.selectAll('#' + mcId).transition()
                            .duration(1000)
                            .attr("fill", bgColor)
                            .attr('transform', "translate(" + tags.x + "," + tags.y + ")")
                            .attr('data-original-title', "Name:" + tags.assignedto + " <br/> Last Seen:" + date + "<br/> Location :"+tags.location);
                        svg.selectAll('#' + mcId).attr('data-x', tags.x);
                        svg.selectAll('#' + mcId).attr('data-y', tags.y);
                        svg.selectAll('#' + mcId).attr("class", "person animateZoom " + state + " " + tags.tagType);
                    }
                }
            });
        };

        activate();

        return vm;
    }
})();
