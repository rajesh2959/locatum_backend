(function () {
    'use strict';
    angular
        .module('app.networkconfig')
        .controller('NetworkconfigController', controller);
    controller.$inject = ['dashboardDataService', 'spid', 'venuedataservice', '$linq', '$rootScope', 'environment', 'session', 'modalService', 'navigation', 'floordataservice', 'venuesession', 'notificationBarService', '$timeout'];

    /* @ngInject */
    function controller(dashboardDataService, spid, venuedataservice, $linq, $rootScope, env, session, modalService, navigation, floordataservice, venuesession, notificationBarService, $timeout) {

        var vm = this;
        vm.zoom = 1;
        var hexaError;
        vm.searchItems = "";
        vm.previousItem = "";
        if (venuesession.sid) {
            vm.sid = venuesession.sid;
            $rootScope.venueId = vm.sid;
        }
        if (spid && spid != 0) {
            vm.spid = spid;
            $rootScope.spid = vm.spid;
        }
        vm.devicesList = [{id: 1, label: "Ble", isChecked: false, isVisible: false}, {id: 2, label: "Ruckus-Ble",isChecked: false, isVisible: false}, {id: 3, label: "All",isChecked: false, isVisible: false}];
        var serverdevType = "";
        var networkVariable = {};
        var Device = {};
        var networkTree = {};
        var baseUrl = env.serverBaseUrl;
        vm.server_inactive = "../images/networkicons/server_inactive.png";
        vm.switch_inactive = "../images/networkicons/switch_inactive.png";
        vm.ap_inactive = "../images/networkicons/ap_inactive.png";
        vm.sensor_inactive = "../images/networkicons/sensor_inactive.png";
        vm.guestSensor_inactive = "../images/networkicons/guestSensor_inactive.png";
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
                else if (vm.floorDetails.length == 0) {
                    var message = "<p>Oops! No record Found. Please Add Floor to View Details.</p>";
                    modalService.messageModal('Information', message).result.then(function () {
                        navigation.gotoFloorPlan();
                    });
                }
            });
        };

        vm.onFloorChanges = function (selectedfloor) {
            $rootScope.spid = selectedfloor;
            vm.selectedFloor = selectedfloor;
            vm.getFloorImage(selectedfloor);
        };

        vm.getFloorImage = function (spid) {
            vm.spid = spid;
            initializenetworkVariable();
            networkVariable.urlObj.spid = vm.spid;
            floordataservice.getNetworkConfig(spid).then(function (response) {
                if (response.body) {
                    vm.selectedFloorname = response.body.uid;
                    var height = response.body.height;
                    var width = response.body.width;
                    var imagePath = baseUrl + "/web/site/portion/planfile?spid=" + spid + "&time=" + new Date();
                    $('#mapSVG').html("");
                    var style = "background: url('" + imagePath + "') no-repeat; width:" + width + "px; height: " + height + "px;"
                    $("#mapSVG").attr("style", style);
                    var isTouchDevice = 'ontouchstart' in document.documentElement ? true : false;
                    if (isTouchDevice) {
                        $("svg").on("taphold", "image", svgImageEvent);
                        $("#mapSVG").on("click", svgEvent);
                    } else {
                        $("#mapSVG").on("click", svgEvent);
                    }

                    getDevices('{{Gateway}}', '{{GeoFinder}}', '{{Heatmap}}', '{{GatewayFinder}}');

                    var GeoFinder = '{{GeoFinder}}';
                    console.log("hello" + GeoFinder)
                    if (GeoFinder == "true") {
                        $(".apIcon").hide();
                    } else {
                        $(".apIcon").show();
                    }

                    $(".dontClick").prop("disabled", true);

                    getTree();
                }
            });
        };

        var $elem = $(".panzoom").panzoom({
            $zoomIn: $(".zoom-in"),
            $zoomOut: $(".zoom-out"),
            $zoomRange: $(".zoom-range"),
            $reset: $(".reset"),
            contain: 'automatic',
            increment: 0.1,
            minScale: 1,
            maxScale: 5,
        });

        if ($(window.width) <= 1020) {
            $('.overflow-Hide').css("overflow", "hidden");
        }

        $('.pdf-Option a').click(function (e) {
            e.preventDefault();
        });

        $(".panzoom").panzoom("enable");
        if ($(window).width() > 1024) {

            if ($(".device-notification").length) {
                $(".device-notification").niceScroll({
                    cursorcolor: "#2496d8",
                    cursoropacitymin: 0,
                    cursoropacitymax: 1,
                    cursorwidth: "4px",
                    touchbehavior: true,
                    cursorborder: "1px solid #2496d8",
                    cursorborderradius: "0px",
                    smoothscroll: true,
                    preventmultitouchscrolling: false,
                });
            }
        }


        var svg = d3.select("svg");
        var svgWidth = $('#mapSVG').width();
        var svgOffset = $('#mapSVG').offset();
        var svgHeight = $('#mapSVG').height();
        var reposition = true;
        var objectW = 40;
        var objectH = 40;
        var isTouchDevice = 'ontouchstart' in document.documentElement ? true : false;
        var isnewobject = false;
        var startX = 100;
        var startY = 100;

        var gateway = false;
        var finder = false;
        var heatmap = false;
        var GatewayFinder = false;
        var imageTypeOne = $("#guestSensorAdd").val();
        function getDevices(param1, param2, param3, param4) {
            gateway = param1;
            finder = param2;
            heatmap = param3;
            GatewayFinder = param4;
        }

        var nodeData = {
            'server': 0,
            'switch': 0,
            'ap': 0,
            'sensor': 0,
            'total': 0
        };

        if (!isTouchDevice) {
            $(".reposition-modal label").text("To reposition the device, Drag to a correct location on the floorplan.");
        } else {
            $(".reposition-modal label").text("To reposition the device, click anywhere on the floorplan.");
        }

        /* ========================================================================
         * Qubercomm NetworkConfig Events
         * ======================================================================== */
        $('#switchAdd').on('click',
            function (event) {

                $elem.panzoom("disable");
                if ($(".draggable[ismovable='true']").length)
                    return;

                var popupOffset = $(this).offset();
                var serverCount = [];
                for (var key in networkVariable.networkTree) {
                    if ((networkVariable.networkTree[key].devices.parent != "ble") && (networkVariable.networkTree[key].devices.parent != "ap")) {
                        var string = "SVR-" + networkVariable.networkTree[key].devices.uid;
                        serverCount.push(string);
                    }
                }
                delete networkVariable.initialParent;
                if (serverCount.length > 0)
                    networkVariable.toggleDevicePopup(popupOffset.left - ($(".popup-switch").width()) + ($(this).width() / 2), popupOffset.top + 60, 'server', 'addSwitch', serverCount);
                else {
                    networkVariable.showNewObjectImage(event, 'switch', "ap");
                    var parentId = networkVariable.initialParent;
                    if (!parentId) {
                        for (var key in networkVariable.networkTree)
                            parentId = "SVR-" + networkVariable.networkTree[key].devices.uid;
                    }
                    $('#main-div').data('parentValue', parentId);
                }
                $("#addSwitch").on('click',
                    function (event) {
                        event.stopPropagation();
                        event.preventDefault();
                        var parentId = $('#deviceSelect').val();
                        if (parentId == "Select") {
                            $("#deviceSelect").addClass("border-error");
                            return;
                        }
                        $('#main-div').data('parentValue', parentId);
                        networkVariable.showNewObjectImage(event, 'switch', "ap");
                    });
            });

        // add new AP to the canvas
        // ============================================================

        $('#apAdd').on('click',
            function (event) {

                $elem.panzoom("disable");
                if ($(".draggable[ismovable='true']").length)
                    return

                var popupOffset = $(this).offset();
                var switchCount = [];
                delete networkVariable.initialParent;
                for (var key in networkVariable.networkTree)
                    networkVariable.networkTree[key].getChild("switch", switchCount);
                if (switchCount.length > 1)
                    networkVariable.toggleDevicePopup(popupOffset.left - ($(".popup-switch").width()) + ($(this).width() / 2), popupOffset.top + 60, 'switch', 'addAp', switchCount);
                else {
                    networkVariable.showNewObjectImage(event, 'ap', "sensor");
                    var parentId = networkVariable.initialParent;
                    if (!parentId) {
                        for (var key in networkVariable.networkTree)
                            networkVariable.networkTree[key].findDevice("switch");

                        parentId = networkVariable.initialParent;
                    }
                    if (parentId == undefined) {
                        parentId = "AP-Master";
                    }
                    $('#main-div').data('parentValue', parentId);
                }
                $("#addAp").on('click',
                    function (event) {
                        event.stopPropagation();
                        event.preventDefault();
                        var parentId = $('#deviceSelect').val();
                        if (parentId == "Select") {
                            $("#deviceSelect").addClass("border-error");
                            return;
                        }
                        $('#main-div').data('parentValue', parentId);
                        networkVariable.showNewObjectImage(event, 'ap', "sensor");
                    });
            });

        // add new sensor to the canvas
        // ============================================================

        $('#sensorAdd').on('click',
            function (event) {
                $elem.panzoom("disable");
                if ($(".draggable[ismovable='true']").length)
                    return;

                serverdevType = "ble";

                var popupOffset = $(this).offset();
                var apCount = [];
                delete networkVariable.initialParent;

                for (var key in networkVariable.networkTree)
                    networkVariable.networkTree[key].getChild("ap", apCount);
                if (apCount.length > 1) {
                    networkVariable.toggleDevicePopup(popupOffset.left - ($(".popup-switch").width()) + ($(this).width() / 2), popupOffset.top + 60, 'ap', 'addSensor', apCount);
                } else {
                    networkVariable.showNewObjectImage(event, 'sensor');
                    var parentId = networkVariable.initialParent;
                    if (!parentId) {
                        for (var key in networkVariable.networkTree) {
                            networkVariable.networkTree[key].findDevice("ap");
                        }
                        parentId = networkVariable.initialParent;
                    }
                    if (parentId == undefined) {
                        parentId = "BLE-Master";
                    }
                    $('#main-div').data('parentValue', parentId);
                }

                $("#addSensor").on('click',
                    function (event) {
                        event.stopPropagation();
                        event.preventDefault();
                        var parentId = $('#deviceSelect').val();
                        if (parentId == "Select") {
                            $("#deviceSelect").addClass("border-error");
                            return;
                        }
                        $('#main-div').data('parentValue', parentId);
                        networkVariable.showNewObjectImage(event, 'sensor');
                    });
            });

        $('#guestSensorAdd').on('click',
            function (event) {
                $elem.panzoom("disable");
                if ($(".draggable[ismovable='true']").length || $("#reposition-modal").css("display") != "none")
                    return;

                serverdevType = "bleserver";
                var popupOffset = $(this).offset();
                var apCount = [];

                delete networkVariable.initialParent;
                console.log("image" + imageTypeOne);
                for (var key in networkVariable.networkTree)
                    networkVariable.networkTree[key].getChild("ap", apCount);
                if (apCount.length > 1) {
                    networkVariable.toggleDevicePopup(popupOffset.left - ($(".popup-switch").width()) + ($(this).width() / 2), popupOffset.top + 60, 'ap', 'addSensor', apCount);
                } else {
                    networkVariable.showNewObjectImage(event, 'sensor', "sensor", imageTypeOne);
                    var parentId = networkVariable.initialParent;
                    if (!parentId) {
                        for (var key in networkVariable.networkTree) {
                            networkVariable.networkTree[key].findDevice("ap");
                        }
                        parentId = networkVariable.initialParent;
                    }
                    if (parentId === undefined) {
                        parentId = "BLE-Master";
                    }
                    $('#main-div').data('parentValue', parentId);
                }

                $("#addSensor").on('click',
                    function (event) {
                        event.stopPropagation();
                        event.preventDefault();
                        var parentId = $('#deviceSelect').val();
                        if (parentId == "Select") {
                            $("#deviceSelect").addClass("border-error");
                            return;
                        }
                        $('#main-div').data('parentValue', parentId);
                        networkVariable.showNewObjectImage(event, 'sensor', "sensor", imageTypeOne);
                    });
            });

        $('#addMasterSwitch').on("click", function (evt) {
            $elem.panzoom("disable");
            if ($(".draggable[ismovable='true']").length)
                return
            var image = '../images/networkicons/server_inactive.png';
            $('#main-div').data('parentValue', 'network');
            networkVariable.showNewObjectImage(evt, 'server', "switch");
        });

        function svgImageEvent(e) {
            e.preventDefault();
            if (isTouchDevice && networkVariable.isNewDevice) {
                var left = parseInt($(this).attr("x"), 10);
                var top = parseInt($(this).attr("y"), 10);
                networkVariable.newObject(networkVariable.newImage, networkVariable.newDevice, left + 10, top + 10);
                networkVariable.isNewDevice = false;
                return;
            }

            disableZoom: true;
            $('.popup-switch').addClass('hide');
            var modelType = $(this).attr("class") == "draggable" ? $('.reposition-modal') : $('.networkconfig');
            if ($('image[isnewobject="true"]').length) {
                $(".networkconfig").hide();
                var offset = $('image[isnewobject="true"]').offset()
                networkVariable.setModalPosition($(".reposition-modal"), offset.left, offset.top);
                return
            } else if ((!$('image[isnewobject="true"]').length) && $(".draggable").length) {
                var offset = $(".draggable").offset();
                $(".networkconfig").hide();
                networkVariable.currentPosition = $(".draggable").offset();
                networkVariable.currentElement = $(".draggable");
                networkVariable.setModalPosition($(".reposition-modal"), offset.left, offset.top);
                return;
            }

            var xAxis = $(this).offset().left;
            var yAxis = $(this).offset().top;
            networkVariable.currentPosition = $(this).offset();
            networkVariable.currentElement = $(this)[0];
            $('.reposition-modal').hide();
            $('body').removeClass('draggingEnabled');
            $(".newobject-modal").hide();

            setTimeout(function () {
                $(this).appendTo("svg");
                var uid = $(this).attr("dev-uid");
                var status = $(this).attr("dev-status");
                $("#deviceHeading").text(uid);
                $("#status").text(status);
                networkVariable.setModalPosition(modelType, xAxis, yAxis);
            }.bind(this),
                200);
        }

        /* ==================================================
       Reposition Modal Functions
       ==================================================*/
        $('#reposition-menu').on('click', function (e) {
            e.stopPropagation();
            $("#duplicate").hide();
            $elem.panzoom("disable");
            $(networkVariable.currentElement).attr('class', "draggable");
            if (!isTouchDevice) {
                networkVariable.init();
            }

            reposition = true;
            $(".left-section").children().show();
            $(".macAddress").hide();
            networkVariable.originalPositon = {
                "x": $(networkVariable.currentElement).attr("x"),
                "y": $(networkVariable.currentElement).attr("y")
            }
            $(networkVariable.currentElement).attr('ismovable', true);
            $(".networkconfig").hide();
            $("#reposition-modal").show();
            $('body').addClass('draggingEnabled');
            $(".left-section p").text("To reposition the device, Drag to a correct location on the floorplan.");
            $("#delete-device").addClass("hide");
            $("#save-reposition").show();
            $("#cancel-reposition span").text("Undo");
            $("#cancel-reposition label img").hide().last().show();
        });

        vm.cancel = function () {
            $elem.panzoom("enable");
            var attr = $(this).attr("isDelete");
            if (attr == "true") {
                $("#reposition-modal").hide();
                $('body').removeClass('draggingEnabled');
                $(this).attr("isDelete", false);
                return;
            }
            var is_newobject = $(networkVariable.currentElement[0]).attr("isnewobject");
            if (is_newobject == "true") {
                $(networkVariable.currentElement).remove();
            } else {
                $(networkVariable.currentElement).attr({
                    "x": networkVariable.originalPositon.x,
                    "y": networkVariable.originalPositon.y,
                    "ismovable": "false"
                });
            }
            $(".draggable").attr({
                "class": "image",
                'ismovable': false
            });
            $("#reposition-modal").hide();
            $('body').removeClass('draggingEnabled');
            reposition = false;
            disableZoom: false;
            $('.plus.zoom-out').trigger('click');
        };

        $("#deleteDevice").on("click",
            function () {

                $("#save-reposition").hide();
                $("#delete-device").removeClass("hide");
                $("#reposition-modal .macAddress").hide();
                $("#cancel-reposition").attr("isDelete", true);
                $("#duplicate").hide();
                $(".macId").val("");
                $("#cancel-reposition span").text("Cancel");
                $('body').addClass('draggingEnabled');
                var message = "<p>Are you sure you want to delete the device?</p> <p>Press No if you want to continue to work. Press Yes to delete.</p>";

                modalService.confirmDelete(message).result.then(
                    function () {
                        vm.deleteDevice();
                    },
                    function () {
                    });
            });

        vm.deleteDevice = function () {
            var elementType = $(networkVariable.currentElement).attr("type");
            var deviceName = $(networkVariable.currentElement).attr("name");
            var parent = $(networkVariable.currentElement).attr("parent");
            var str = "";

            $.each(networkVariable.currentElement.attributes, function () {
            });

            var i = 0;
            var num = deviceName.match(/\d+/g);
            if (parent === "ble") {

                if (elementType === "server") {
                    str = "server" + num;
                }
                else {
                    str = "sensor" + num;
                }
                networkVariable.networkTree[str].deleteDevices(str, 0);
            }
            else if (parent == "ap") {
                str = "server" + num;
                networkVariable.networkTree[str].deleteDevices(str, 0);
            } else if (elementType === "server") {
                networkVariable.networkTree[deviceName].deleteDevices(deviceName, 0);
            } else {
                for (var key in networkVariable.networkTree) {
                    networkVariable.networkTree[key].deleteDevices(deviceName, i);
                    i++;
                }
            }
            var noServerFound = '<li id="noServerFound">No Servers found!</li>';
            if (nodeData["total"] === 0) {
                $("#network-tree").html(noServerFound);
            }
            var deviceText = nodeData["total"] == 1 ? " Device" : " Devices";
            $(".device-section span").text(nodeData["total"] + deviceText);
            $("#" + $(networkVariable.currentElement).attr("deviceId")).remove();
            $(".networkconfig").hide();
        };

        vm.saveReposition = function (isFromReposition) {
            networkVariable.currentUid = "";
            var is_newobject = $(networkVariable.currentElement).attr("isnewobject");

            if (!isFromReposition) {
                if (!networkVariable.validateMacid()) {
                    return;
                }
            }
            if (is_newobject == "true") {
                var nodeType = $('#main-div').data('type');
                var parentValue = $('#main-div').data('parentValue');
                var source = $('#main-div').data('source');
                parentValue = parentValue.split("-");
                $("#noServerFound").addClass("hide");
                nodeData[nodeType] += 1;
                nodeData["total"] += 1;
                var deviceText = nodeData["total"] == 1 ? " Device" : " Devices";
                $(".device-section span").text(nodeData["total"] + deviceText);
                $(networkVariable.currentElement[0]).attr({
                    "ismovable": "false",
                    "isnewobject": "false",
                    'dev-uid': networkVariable.currentUid,
                    "dev-status": "Offline",
                    "class": "draggable",
                    "name": nodeType + "" + nodeData[nodeType],
                    "deviceId": nodeType + "-id-" + nodeData[nodeType]
                })
                var parentNode = parentValue[1];
                networkVariable.addNode(nodeType, parentNode, 'Offline', networkVariable.currentUid);
                var x = $(networkVariable.currentElement[0]).attr("x");
                var y = $(networkVariable.currentElement[0]).attr("y");
                var deviceId = nodeType + "" + nodeData[nodeType];
                //var networkTree = new NetworkTree();
                if (nodeType == "server") {
                    var NetworkTree = new networkTree();
                    var newObject = networkTree.createParent(nodeType, nodeData[nodeType], x, y, networkVariable.currentUid);
                    networkVariable.networkTree[deviceId] = newObject;

                } else if (parentNode == "Master") {
                    nodeType = $('#main-div').data('type');
                    var NetworkTree = new networkTree();
                    var newObject = NetworkTree.createParent(nodeType, nodeData[nodeType], x, y, networkVariable.currentUid)
                    networkVariable.networkTree[deviceId] = newObject;
                } else {
                    networkVariable.updateTree(nodeType, x, y, "addChild", parentNode, networkVariable.currentUid);
                }
            }

            $("#deviceHeading").text($(networkVariable.currentElement).attr("name"));
            $(networkVariable.currentElement).attr({
                "ismovable": "false"
            });
            $(".draggable").attr({
                "class": "image",
                'ismovable': false
            });
            reposition = false;
            if (is_newobject == "false") {
                var nodeName = $(networkVariable.currentElement).attr("name");
                var offsetX = $(networkVariable.currentElement).attr("x");
                var offsetY = $(networkVariable.currentElement).attr("y");
                var parent = $(networkVariable.currentElement).attr("parent");
                networkVariable.currentUid = $(networkVariable.currentElement).attr("dev-uid");
                networkVariable.updateTree(nodeName, offsetX, offsetY, 'updateDevices', parent, networkVariable.currentUid);
            }
            disableZoom: false;
        };

        function svgEvent(evt) {
            disableZoom: true;
            if (isnewobject || networkVariable.isNewDevice) {
                svgWidth = $('#mapSVG').width();
                svgOffset = $('#mapSVG').offset();
                svgHeight = $('#mapSVG').height();
                $(document).unbind("mousemove");
                var image = $("#" + networkVariable.newDevice + "Offline").attr("src");
                var type = $("#" + networkVariable.newDevice + "Offline").attr("type");
                var x = evt.offsetX ? (evt.offsetX + objectW / 2 > svgWidth ? evt.offsetX - objectW / 2 : evt.offsetX) : evt.originalEvent.layerX;
                var y = evt.offsetY ? (evt.offsetY + objectH / 2 > svgHeight ? evt.offsetY - objectH / 2 : evt.offsetY) : evt.originalEvent.layerY;
                if (networkVariable.isNewDevice) {
                    networkVariable.isNewDevice = false;
                    type = networkVariable.newDeviceType;
                }
                networkVariable.newObject(image, type, x, y);
                isnewobject = false;
                $("#" + networkVariable.newDevice + "Offline").hide();
                disableZoom: true;
                $(".left-section").children().hide();
                $(".macAddress").show();
                var message = "test";
                modalService.deviceModal('').result.then(function (res) {
                    $("#reposition-modal").hide();
                    if (res == "cancel") {
                        vm.cancel();
                    }
                    else if (res == "close") {
                        $("#reposition-modal").hide();
                        vm.cancel();
                    }
                    else if (res) {
                        vm.saveReposition();
                    }
                }, function () {
                });
                $("#reposition-modal").show();
                $('body').addClass('draggingEnabled');
                $("#reposition-modal .left-section p").text("To reposition the device, Drag to a correct location on the floorplan.");
                $("#reposition-modal .rightsection #save-reposition").show();
                $("#reposition-modal .rightsection #delete-device").addClass("hide");
                $("#reposition-modal .rightsection").show();
                return;
            }
            
            if (reposition && isTouchDevice) {
                var left = evt.offsetX ? evt.offsetX : evt.layerX;
                var top = evt.offsetY ? evt.offsetY : evt.layerY;
                if (networkVariable.isNewDevice)
                    networkVariable.newObject(networkVariable.newImage, networkVariable.newDevice, left, top);
                else
                    $(networkVariable.currentElement).attr({
                        'x': left,
                        'y': top
                    })
                $("#reposition-modal").show();
                $('body').addClass('draggingEnabled');
                networkVariable.isNewDevice = false;
                disableZoom: true;
                $("#reposition-modal").show();
                $('body').addClass('draggingEnabled');
                $("#reposition-modal .left-section p").text("To reposition the device, click on the floorplan.");
                $("#reposition-modal .rightsection").show();
                evt.stopPropagation();
                return;
            }
            disableZoom: true;
            $(".networkconfig").hide();
            if (!reposition)
                disableZoom: false;
        }

        window.hexaError = false;

        function autohexaFill(evt) {
            if (hexaError)
                return;
            if (evt.target.value.length == 1) {
                var value = $(evt.target).val();
                value = "0" + value;
                $(evt.target).val(value);
            }
        }

        function checkHexaValues(evt) {
            var regx = new RegExp(/^[a-f0-9]+$/i);
            if (regx.test(evt.target.value)) {
                hexaError = false;
                return true;
            } else {
                var value = $(evt.target).val();
                value = value.substr(0, value.length - 1);
                $(evt.target).val(value);
                hexaError = true;
                return false;
            }
        }

        $('.macId').on('input', function (evt) {
            checkHexaValues(evt);
            if (evt.target.value.length == 2)
                $(evt.target).parent().next().next().find("input").focus();
            if (evt.target.value.length > 2) {
                var value = $(evt.target).val();
                var value = value.substr(0, value.length - 1);
                $(evt.target).val(value);
                return false;
            }
        });

        $(".macId").on('blur', autohexaFill);
        $(".zoomControl").on('click', function () {
            if ($(".draggable[ismovable='true']").length)
                return
            disableZoom: false;
            $(".networkconfig").hide();
            $(".reposition-modal").hide();
            $('body').removeClass('draggingEnabled');
        })

        $(document).click(function (e) {
            if ($(e.target).parent().hasClass("addobject") || $(".addobject").is(e.target) || $("image").is(e.target) || $(e.target).closest(".deviceForm").length) {
                return;
            }

            if (!$(e.target).is($("#mapSVG")) && isnewobject) {
                $("#reposition-modal").hide();
                $('body').removeClass('draggingEnabled');
                isnewobject = false;
                $(document).unbind("mousemove");
            }
            if ((!$(e.target).is($("#mapSVG")) || !isnewobject) && !networkVariable.isNewDevice) {
                $("#" + networkVariable.newDevice + "Offline").hide();
                isnewobject = false;
                $(document).unbind("mousemove");
            }
            $(".popup-switch").addClass("hide");
            $(".networkconfig").hide();
        });

        $("svg").on("click tap", "image", function (evt) {
            if (reposition)
                return;
            var uid = evt.currentTarget.getAttribute("dev-uid");
            if (vm.deviceList != undefined && vm.deviceList.length > 0) {
                var selectedRoomList = $linq.Enumerable().From(vm.deviceList)
                    .Where(function (x) {
                        return x.uid === uid;
                    }).FirstOrDefault();
                if (selectedRoomList != undefined)
                    vm.selectedRoom = selectedRoomList.alias;
            };
            var navDetail = {};
            navDetail.venue = vm.venueDetails.uid;
            navDetail.floor = vm.selectedFloorname;
            navDetail.room = vm.selectedRoom;
            navDetail.spid = vm.spid;
            localStorage.setItem("prevPageInfo", JSON.stringify(navDetail));
            navigation.goToGatewayInfo(uid, vm.sid, vm.spid);
            //if (heatmap != "true") {
            //    window.location.href = evt.currentTarget.getAttribute("hyperLink");
            //}
        });

        function moveDevice(evt) {
            if ($(evt.target).closest("#mapSVG").length)
                $("#" + networkVariable.newDevice + "Offline").show().css({
                    position: 'absolute',
                    left: evt.pageX - 215,
                    top: evt.pageY
                });
            else
                $("#" + networkVariable.newDevice + "Offline").hide();
        }

        function initializenetworkVariable() {
            /* =============================
                Network Functions 
                =============================*/

            networkVariable = {
                'deviceOptions': {
                    'server': [],
                    'switch': [],
                    'sensor': [],
                    'guestSensor': [],
                    'ap': []
                },
                networkTree: {},
                childDevices: [],
                init: function () {
                    Snap("#mapSVG .draggable").drag(networkVariable.dragMove, networkVariable.dragStart, networkVariable.dragEnd);
                },
                dragStart: function (x, y, evt) {
                    $('.draggable').closest('a').click(function (e) {
                        e.preventDefault();
                    });
                    try {
                        startX = parseInt(Snap(".draggable").attr("x"), 10);
                        startY = parseInt(Snap(".draggable").attr("y"), 10);
                        $("reposition-modal").toggle()
                        $('body').toggleClass('draggingEnabled');
                    } catch (err) {

                    }
                },
                dragMove: function (dx, dy, x, y, evt) {
                    var ismovable = $(".draggable").attr("ismovable")
                    if (ismovable == "true") {
                        $(".reposition-modal").hide();
                        $('body').addClass('draggingEnabled');
                        $(".networkconfig").hide();
                        if ($(evt.target).is($("#mapSVG")) || $(evt.target).is($("image"))) {
                            Snap(".draggable").attr({
                                "x": typeof InstallTrigger == "undefined" ? evt.offsetX - 10 : evt.layerX - 10,
                                "y": typeof InstallTrigger == "undefined" ? evt.offsetY - 5 : evt.layerY - 5
                            });
                        } else {
                            return;
                        }
                    }
                    $('.draggable').closest('a').click(function (e) {
                        e.preventDefault();
                    });
                },
                dragEnd: function () {
                    $('image[ismovable="true"]').trigger("click");
                    var imageOffset = $(".draggable").offset();
                    $(".networkconfig").hide();
                    $(".reposition-modal #save-reposition").show();
                    disableZoom: true;
                    var message = "<p>Are you sure you want to save current location on the floorplan?</p> <p>Press No if you want to undo. Press Yes to save.</p>";
                    modalService.questionModal('Reposition Confirmation', message, true).result.then(function () {
                        vm.saveReposition(true);
                        activate();
                    });
                },
                'moveElementCSS': function (elem, top, left) {
                    $(elem).css({
                        position: 'absolute',
                        top: top,
                        left: left
                    });
                },
                'newObject': function (image, type, x, y, uid, initial, id, status, parent, source) {
                    var x = x ? x : "";
                    var y = y ? y : "";
                    $('#main-div').data('newElement', 1);
                    $('#main-div').data('type', type);
                    $(".popup-switch").addClass("hide");
                    var isNewObject = $(".draggable").attr("isnewobject");
                    var urlMap = {
                        "server": 'flrdash',
                        'switch': 'swiboard',
                        'ap': 'devboard',
                        'sensor': 'devboard',
                        'guestSensor': 'devboard'
                    }

                    this.fetchurlParams(window.location.search.substr(1));
                    if (source != "guest") {
                        if (type == "server") {
                            if (finder == "true") {
                                var url = baseUrl + "/web/site/portion/dashboard" + "?sid=" + this.urlObj.sid + "&spid=" + this.urlObj.spid + "&cid=" + this.urlObj.cid + "&type=" + "server"
                            } else {
                                var url = baseUrl + "/web/site/portion/" + urlMap[type] + "?sid=" + this.urlObj.sid + "&spid=" + this.urlObj.spid + "&cid=" + this.urlObj.cid + "&type=" + "server"
                            }
                        }
                        else if (type == "sensor") {
                            if (gateway != "true") {
                                var url = baseUrl + "/web/finder/device/" + urlMap[type] + "?sid=" + this.urlObj.sid + "&spid=" + this.urlObj.spid + "&uid=" + uid + "&cid=" + this.urlObj.cid + "&type=" + "sensor"
                            } else {
                                var url = "javascript:void(0)"
                            }
                        } else if (type == "switch") {
                            if (finder != "true") {
                                var url = baseUrl + "/web/site/portion/" + urlMap[type] + "?sid=" + this.urlObj.sid + "&spid=" + this.urlObj.spid + "&uid=" + uid + "&cid=" + this.urlObj.cid + "&type=" + "sensor"
                            } else {
                                var url = "javascript:void(0)"
                            }
                        }
                        else if (type == "ap") {
                            if (finder != "true") {
                                var url = baseUrl + "/web/site/portion/" + urlMap[type] + "?sid=" + this.urlObj.sid + "&spid=" + this.urlObj.spid + "&uid=" + uid + "&cid=" + this.urlObj.cid + "&type=" + "device"
                            } else {
                                var url = "javascript:void(0)"
                            }
                        } else if (type == "guestSensor") {
                            var url = "javascript:void(0)"
                        }
                    } else {
                        var url = "javascript:void(0)";
                    }

                    if (isNewObject == undefined || isNewObject == "false" || initial) {
                        $("#cancel-reposition span").text("Cancel")
                        $("#cancel-reposition label img").hide().first().show();
                        disableZoom: true;
                        if (!$("image[type=" + type + "]").length && !initial)
                            nodeData[type] = 0;
                        if (!initial) {
                            var anchor = svg.append("a").attr("onclick", "return false;");
                            var newObject = anchor.append("image").attr({
                                "class": "draggable",
                                "xlink:href": image,
                                "x": x,
                                "y": y,
                                "hyperLink": window.location.href,
                                "onclick": "return false;",
                                "width": objectW,
                                "height": objectH,
                                "isnewobject": initial ? false : true,
                                "type": type,
                                "ismovable": initial ? false : true,
                                "parent": parent
                            });
                            $(anchor[0]).appendTo('svg');
                        }
                        disableZoom: true;
                        if (!initial) {
                            $(".reposition-modal").show();
                            $('body').addClass('draggingEnabled');
                            networkVariable.currentElement = newObject[0];
                            if (!isTouchDevice)
                                networkVariable.init();
                        } else {
                            var anchor = svg.append("a").attr("onclick", "return false;");
                            var newObject = anchor.append("image").attr({
                                "xlink:href": image,
                                "x": x,
                                "y": y,
                                "width": objectW,
                                "height": objectH,
                                "isnewobject": initial ? false : true,
                                "type": type,
                                "hyperLink":window.location.href,
                                "onclick": "return false;",
                                "dev-status": status,
                                "dev-uid": uid,
                                "ismovable": initial ? false : true,
                                "class": "image",
                                "name": type + "" + id,
                                "deviceId": type + "-id-" + id,
                                "parent": parent
                            })
                            $(anchor[0]).appendTo("svg")
                        }
                        $("image").bind('contextmenu', svgImageEvent);
                    }
                    if (heatmap == "true") {
                        $('#mapSVG a').attr('href', 'javascript:void(0)');
                        $('#mapSVG image').removeAttr('hyperLink');
                    }
                },
                "updateTree": function (nodeType, x, y, exec, parentValue, uid) {
                    // var obj = this.networkTree;
                    // $.each(obj, function (key, value) {
                    //     if (key == nodeType && uid == value.devices.uid)
                    //         eval("key." + exec + "(nodeType, x, y,parentValue,uid)");
                    // });

                    // for (let index = 0; index < this.networkTree.length; index++) {
                    //     if (this.networkTree[index] == nodeType  && this.networkTree[index].uid == uid) {
                    //                 eval("this.networkTree[index]." + exec + "(nodeType, x, y,parentValue,uid)");
                    //             }
                    //         }

                    for (var key in this.networkTree) {
                        var deviceUid = this.networkTree[key].devices.uid;
                        if (uid == deviceUid) {
                            eval("this.networkTree[key]." + exec + "(nodeType, x, y,parentValue,uid)");
                        }
                    }
                },

                'addNode': function (type, parent, status, uid, id, source) {
                    var id = id ? id : nodeData[type]
                    var network = {}
                    if (finder == "true") {
                        network['server'] = '<li class="deviceInfo" id="server-id-' + id +
                            '"><a class="dashbrdLink" href="#"><div data-status="' + status + '" data-type="Server" data-uid="' + uid + '" data-href=#"' +/* baseUrl + '/web/site/portion/dashboard?sid=' + this.urlObj.sid + '&uid=' + uid + '&type=server&spid=' + this.urlObj.spid + '&cid=' + this.urlObj.cid +*/ '" data-bref="#" data-cref="#" data-sref="#" class="device-name"><label>' +
                            '<i class="fa fa-2x fa-angle-down" aria-hidden="true"></i>' +
                            '<img src="../images/networkconfig/icon/server_inactive.png" alt=""></label>' +
                            '<span>SVR-' + uid + '</span><label class="connected device-status pull-right">' +
                            '<span>' + status + '</span></label></div></a>' +
                            '<ul class="child list-unstyled" parent-id="' + uid + '" id="server' + id + '-tree"></ul></li>';
                    } else if (GatewayFinder == "true") {
                        network['server'] = '<li class="deviceInfo" id="server-id-' + id +
                            '"><a class="dashbrdLink"><div data-status="' + status + '" data-type="Server" data-uid="' + uid + '" data-href=#"' +/* baseUrl + '/web/site/portion/dashboard?sid=' + this.urlObj.sid + '&uid=' + uid + '&type=server&spid=' + this.urlObj.spid + '&cid=' + this.urlObj.cid + '&param=1" */'data-bref="#" data-cref="#" data-sref="#" class="device-name"><label>' +
                            '<i class="fa fa-2x fa-angle-down" aria-hidden="true"></i>' +
                            '<img src="../images/networkconfig/icon/server_inactive.png" alt=""></label>' +
                            '<span>SVR-' + uid + '</span><label class="connected device-status pull-right">' +
                            '<span>' + status + '</span></label></div></a>' +
                            '<ul class="child list-unstyled" parent-id="' + uid + '" id="server' + id + '-tree"></ul></li>';

                    } else if (heatmap == "true") {
                        network['server'] = '<li class="deviceInfo" id="server-id-' + id +
                            '"><a class="dashbrdLink" href="#"><div data-status="' + status + '" data-type="Server" data-uid="' + uid + '" data-href="#" data-bref="#" data-cref="#" data-sref="#"  class="device-name"><label>' +
                            '<i class="fa fa-2x fa-angle-down" aria-hidden="true"></i>' +
                            '<img src="../images/networkconfig/icon/server_inactive.png" alt=""></label>' +
                            '<span>SVR-' + uid + '</span><label class="connected device-status pull-right">' +
                            '<span>' + status + '</span></label></div></a>' +
                            '<ul class="child list-unstyled" parent-id="' + uid + '" id="server' + id + '-tree"></ul></li>';
                    }
                    else {
                        network['server'] = '<li class="deviceInfo" id="server-id-' + id +
                            '"><a class="dashbrdLink" href="#"><div data-status="' + status + '" data-type="Server" data-uid="' + uid + '" data-href=#"' + /*baseUrl + '/web/site/portion/dashboard?sid=' + this.urlObj.sid + '&uid=' + uid + '&type=server&spid=' + this.urlObj.spid + '&cid=' + this.urlObj.cid + */'" data-bref="#" data-cref="#" data-sref="#" class="device-name"><label>' +
                            '<i class="fa fa-2x fa-angle-down" aria-hidden="true"></i>' +
                            '<img src="../images/networkconfig/icon/server_inactive.png" alt=""></label>' +
                            '<span>SVR-' + uid + '</span><label class="connected device-status pull-right">' +
                            '<span>' + status + '</span></label></div></a>' +
                            '<ul class="child list-unstyled" parent-id="' + uid + '" id="server' + id + '-tree"></ul></li>';
                    }
                    if (finder == "true") {
                        network['switch'] = '<li  class="deviceInfkey == nodeType && o" id="switch-id-' + id +
                            '"><a class="dashbrdLink" href="#"><div data-status="' + status + '" data-type="Switch" data-uid="' + uid + '" data-href="#" data-bref="#" data-cref="#" data-sref="#" class="device-name"><label>' +
                            '<img src="../images/networkconfig/icon/switch_inactive.png" alt=""></label>' +
                            '<span>SW-' + uid + '</span><label class="connected device-status pull-right">' +
                            '<span>' + status + '</span></label></div></a>' +
                            '<ul class="list-unstyled childOfChild" parent-id="' + uid + '" id="switch' + id + '-tree"></ul></li>';
                    } else if (heatmap == "true") {
                        network['switch'] = '<li  class="deviceInfo" id="switch-id-' + id +
                            '"><a class="dashbrdLink" href="#"><div data-status="' + status + '" data-type="Switch" data-uid="' + uid + '" data-href="#" data-bref="#" data-cref="#" data-sref="#" class="device-name"><label>' +
                            '<img src="../images/networkconfig/icon/switch_inactive.png" alt=""></label>' +
                            '<span>SW-' + uid + '</span><label class="connected device-status pull-right">' +
                            '<span>' + status + '</span></label></div></a>' +
                            '<ul class="list-unstyled childOfChild" parent-id="' + uid + '" id="switch' + id + '-tree"></ul></li>';
                    } else {
                        network['switch'] = '<li  class="deviceInfo" id="switch-id-' + id +
                            '"><a class="dashbrdLink" href="#"><div data-status="' + status + '" data-type="Switch" data-uid="' + uid + '" data-href=#"' + /*baseUrl + '/web/site/portion/swiboard?sid=' + this.urlObj.sid + '&uid=' + uid + '&type=switch&spid=' + this.urlObj.spid + '&cid=' + this.urlObj.cid +*/ '" data-bref="#" data-cref="#" data-sref="#" class="device-name"><label>' +
                            '<img src="../images/networkconfig/icon/switch_inactive.png" alt=""></label>' +
                            '<span>SW-' + uid + '</span><label class="connected device-status pull-right">' +
                            '<span>' + status + '</span></label></div></a>' +
                            '<ul class="list-unstyled childOfChild" parent-id="' + uid + '" id="switch' + id + '-tree"></ul></li>';
                    }
                    if (finder == "true") {
                        network['ap'] = '<li  class="deviceInfo" id="ap-id-' + id + '"><a class="dashbrdLink" href="#"><div data-status="' + status + '" data-type="Ap" data-uid="' + uid + '" data-href="#" data-cref="#" data-bref="#" data-sref="#" class="device-name"><label><img src="../images/networkconfig/icon/ap_inactive.png" alt="">' +
                            '</label><span>AP-' + uid + '</span><label class="connected device-status pull-right"><span>' + status + '</span>' +
                            '</label></div></a>' + '<ul class="list-unstyled childOfChild" parent-id="' + uid + '" id="ap' + id + '-tree"></ul></li>';
                    } else {
                        network['ap'] = '<li  class="deviceInfo" id="ap-id-' + id + '"><a class="dashbrdLink" href="#"><div data-status="' + status + '" data-type="Ap" data-uid="' + uid + '" data-href=#"' +/* baseUrl + '/web/site/portion/devboard?sid=' + this.urlObj.sid + '&uid=' + uid + '&type=device&spid=' + this.urlObj.spid + '&cid=' + this.urlObj.cid +*/ '" data-cref="#"' +/*+ baseUrl + '/web/device/custconfig?sid=' + this.urlObj.sid + '&spid=' + this.urlObj.spid + '&uid=' + uid + '&cid=' + this.urlObj.cid +*/ '" data-bref="#"' + /*baseUrl + '/web/finder/device/binary?sid=' + this.urlObj.sid + '&spid=' + this.urlObj.spid + '&uid=' + uid + '&cid=' + this.urlObj.cid + '" data-sref="' + baseUrl + '/scan?sid=' + this.urlObj.sid + '&spid=' + this.urlObj.spid + '&uid=' + uid + '&cid=' + this.urlObj.cid +*/ '" class="device-name"><label><img src="../images/networkconfig/icon/ap_inactive.png" alt="">' +
                            '</label><span>AP-' + uid + '</span><label class="connected device-status pull-right"><span>' + status + '</span>' +
                            '</label></div></a>' + '<ul class="list-unstyled childOfChild" parent-id="' + uid + '" id="ap' + id + '-tree"></ul></li>';
                    }
                    if (source != "guest") {
                        network['sensor'] = '<li  class="deviceInfo" id="sensor-id-' + id + '"><a class="dashbrdLink" href="#"><div data-status="' + status + '" data-type="Sensor" data-uid="' + uid + '" data-href=#"' + /*baseUrl + '/web/finder/device/devboard?sid=' + this.urlObj.sid + '&uid=' + uid + '&type=device&spid=' + this.urlObj.spid + '&cid=' + this.urlObj.cid + */'" data-cref="#"' +/* baseUrl + '/web/finder/device/configure?sid=' + this.urlObj.sid + '&spid=' + this.urlObj.spid + '&uid=' + uid + '&cid=' + this.urlObj.cid + '" data-bref="' + baseUrl + '/web/finder/device/binary?sid=' + this.urlObj.sid + '&spid=' + this.urlObj.spid + '&uid=' + uid + '&cid=' + this.urlObj.cid + '" data-sref="' + baseUrl + '/web/beacon/list?sid=' + this.urlObj.sid + '&spid=' + this.urlObj.spid + '&uid=' + uid + '&cid=' + this.urlObj.cid +*/ '" class="device-name"><label><img src="../images/networkconfig/icon/sensor_inactive.png" alt="">' +
                            '</label><span>BLE-' + uid + '</span><label class="connected device-status pull-right ">' +
                            '<span>123' + status + '</span></label></div></a></li>';
                    } else {
                        network['sensor'] = '<li  class="deviceInfo" id="sensor-id-' + id + '"><a class="" href="#"><div data-status="' + status + '" data-type="Sensor" data-uid="' + uid + '"  data-cref="' + /*baseUrl + '/web/finder/device/configure?sid=' + this.urlObj.sid + '&spid=' + this.urlObj.spid + '&uid=' + uid + '&cid=' + this.urlObj.cid +*/ '"  class=""><label><img src="../images/networkconfig/icon/guestSensor_inactive.png" alt="">' +
                            '</label><span>BLE-' + uid + '</span><label class="connected device-status pull-right ">' +
                            '<span>123' + status + '</span></label></div></a></li>';
                    }
                    if (parent == "ble")
                        $('#network-tree').append(network["sensor"]);
                    else if (parent == "ap")
                        $('#network-tree').append(network["ap"]);
                    else if (type == "guestSensor")
                        $('#network-tree').append(network["guestSensor"]);
                    else if (type == "server")
                        $('#network-tree').append(network[type]);
                    else
                        $("ul[parent-id='" + parent + "']").append(network[type])

                    var parentSelect = type + id;
                    networkVariable.deviceOptions[type].push(parentSelect);
                },
                'toggleDevicePopup': function (left, top, type, addId, count) {
                    if (!count.length) {
                        $(".errorModal").show();
                        $(".errorModal span").html("Please select a " + type.toUpperCase() + "")
                        return;
                    }
                    $(".popup-switch").addClass("hide")
                    $(".errorModal").hide();
                    $(".master-form").html(template({
                        options: count,
                        id: addId
                    }));

                    $(".popup-switch").removeClass("hide");
                    $(".networkconfig").hide();
                    networkVariable.moveElementCSS($(".popup-switch"), top, left);
                },
                'validateMacid': function () {
                    if ($(".macAddress").css("display") != 'none') {
                        $(".macId").each(function (index, node) {
                            if ($(this).val() == "") {
                                $(this).addClass("addborder")
                                return false;
                            } else
                                networkVariable.currentUid += $(this).val() + (index < 5 ? ":" : "");
                        })
                        if (networkVariable.currentUid.replace(/:/g, "").length < 12)
                            return false;
                    }
                    return true;
                },
                'deleteImages': function (deviceTree) {
                    var deviceId = deviceTree.type + "" + deviceTree[deviceTree.type + "_id"]
                    var nodeId = deviceTree.type + "-id-" + deviceTree[deviceTree.type + "_id"]
                    $("image[name=" + deviceId + "]").remove();
                    nodeData["total"] -= 1;
                    for (var i = 0; i < deviceTree.child.length; i++)
                        this.deleteImages(deviceTree.child[i])
                    var deviceCount = nodeData['total'] == 1 ? "1 Device" : nodeData['total'] + " Devices"
                    $(".device-section span").text(deviceCount)
                    $("#" + nodeId).remove();
                    disableZoom: false;
                },
                'buildInitialNetwork': function () {
                    for (var key in this.networkTree)
                        this.networkTree[key].initialSetup();
                    $("#noServerFound").addClass("hide");
                    var deviceText = nodeData["total"] == 1 ? " Device" : " Devices";
                    $(".device-section span").text(nodeData["total"] + deviceText);
                    reposition = false;
                },
                setModalPosition: function (modeltype, xAxis, yAxis) {
                    xAxis = xAxis - 200;
                    var xValue, yValue;
                    var modelWidth = parseInt(modeltype.width());
                    var modelHeight = parseInt(modeltype.height());
                    var sidebarWidth = $('#sidebar-wrapper').hasClass("hide") ? 0 : $('#sidebar-wrapper').width() - 20;
                    var mapWidth = $(".main-section-activity").width() + sidebarWidth;
                    var mapHeight = $(".main-section-activity").height();
                    var totalWidth = modelWidth + xAxis + (objectW / 2);
                    var totalHeight = modelHeight + yAxis;
                    var widthCheck = totalWidth > mapWidth ? "widthPlus" : "widthMinus";
                    var heightCheck = totalHeight > mapHeight ? "heightPlus" : "heightMinus";

                    if (widthCheck == "widthPlus" && heightCheck == "heightPlus") {
                        xValue = xAxis - modelWidth;
                        yValue = yAxis - modelHeight;
                        networkVariable.showModal(modeltype, xValue, yValue);

                    } else if (widthCheck == "widthMinus" && heightCheck == "heightMinus") {
                        xValue = xAxis + (objectW / 2);
                        yValue = yAxis + (objectW / 2);
                        networkVariable.showModal(modeltype, xValue, yValue);
                    } else if (widthCheck == "widthPlus" && heightCheck == "heightMinus") {
                        xValue = xAxis - modelWidth + (objectW / 2);
                        yValue = yAxis + (objectW / 2);
                        networkVariable.showModal(modeltype, xValue, yValue);
                    } else if (widthCheck == "widthMinus" && heightCheck == "heightPlus") {
                        xValue = xAxis + (objectW / 2);
                        yValue = yAxis - modelHeight + (objectW / 2);
                        networkVariable.showModal(modeltype, xValue, yValue);
                    }
                },
                cancelReposition: function () {
                    $(".draggable").attr({
                        "x": networkVariable.originalPositon.x,
                        "y": networkVariable.originalPositon.y,
                        "ismovable": "false"
                    })
                    $('#reposition-modal').hide();
                    $('body').removeClass('draggingEnabled');
                },
                showModal: function (modeltype, xValue, yValue) {
                    modeltype.show().css({
                        "left": xValue,
                        "top": yValue,
                        "z-index": 99,
                        "position": "absolute"
                    });
                },
                toJSON: function () {
                    var json = {};
                },
                'showNewObjectImage': function (evt, type, child, imageTypeOne) {
                    if (isTouchDevice) {
                        if (type == "sensor" && imageTypeOne == 0) {
                            var type = "guestSensor";
                        }
                        networkVariable.newImage = '../images/networkicons/' + type + '_inactive.png';
                        networkVariable.isNewDevice = true;
                        networkVariable.newDeviceType = type;
                    } else {
                        isnewobject = true;
                        if (type == "sensor" && imageTypeOne == 0) {
                            var type = "guestSensor";
                        }
                        var imageArea = $("#" + type + "Offline").show().css({
                            'position': 'absolute',
                            left: evt.pageX - 200,
                            top: evt.pageY,
                            'z-index': 9999
                        })

                        imageArea.attr({
                            type: type
                        })

                        evt.stopPropagation();
                        $(document).bind("mousemove", moveDevice);
                    }
                    reposition = true;
                    networkVariable.newDevice = type;
                    networkVariable.childType = child;
                    $(".left-section").children().show();
                    $(".macAddress").hide();
                    $(".macId").val("")
                    $("#deviceSelect").removeClass("border-error");
                    $(".popup-switch").addClass("hide");
                    disableZoom: true;
                    $(".errorModal").hide();
                    $("#reposition-modal").show();
                    $('body').addClass('draggingEnabled');
                    $(".rebootPopup").hide();
                    $("#reposition-modal .left-section p").text("Click on the floorplan to place the device");
                    $("#reposition-modal .rightsection").hide();
                    $("#duplicate").hide();
                    $('.draggable').closest('a').click(function (e) {
                        e.preventDefault();
                    });
                },
                'saveNetworkDevice': function (uid, devices, parent, deviceType, pid, currentObj, ble, ap, source) {
                    devices['parent'] = pid ? pid : '';
                    var myble = devices.parent;
                    var devtyp = devices.type;
                    var source = ap;
                    if (ble == "ble") {
                        myble = "ble";
                        devtyp = "bleserver"
                    } else if (ble == "ap") {
                        myble = "ap";
                        devtyp = "apserver"
                    }
                    var networkconfig = {
                        uid: devices.uid,
                        xposition: devices.xposition,
                        yposition: devices.yposition,
                        status: devices.status,
                        type: devtyp,
                        sid: devices.sid,
                        spid: devices.spid,
                        parent: myble,
                        gparent: "",// currentObj.parent,
                        source: source
                    };
                    vm.networkConfig = networkconfig;
                    $.ajax({
                        method: 'post',
                        url: '' + baseUrl + '/rest/site/portion/networkdevice/save',
                        data: JSON.stringify(networkconfig),
                        headers: {
                            'content-type': 'application/json'
                        },
                        success: function (response) {
                            if (!response) {
                                var nodeId = devices.type + "-id-" + devices[devices.type + "_id"]
                                nodeData[devices.type] -= 1;
                                nodeData["total"] -= 1;
                                if (!parent)
                                    delete networkVariable.networkTree[devices.type + "" + devices[devices.type + "_id"]]
                                $(networkVariable.currentElement).attr({
                                    "isnewobject": true,
                                    "class": "draggable"
                                })
                                $("#" + nodeId).remove();
                                return;
                            }
                            $(".macId").each(function () {
                                $(this).val("")
                                $(this).removeClass("addborder");
                            });
                            if (!parent)
                                currentObj.devices = devices
                            else
                                currentObj.child.push(devices);
                            var deviceMap = {
                                "server": "SVR",
                                "switch": "SW",
                                "ap": "AP",
                                "sensor": "SNR"
                            }
                            if (nodeData[devices.type] == 1) {
                                var parentNode = deviceMap[devices.type] + "-" + devices.uid;
                                if (!networkVariable.initialParent)
                                    networkVariable.initialParent = parentNode;
                            }
                            $('#reposition-modal').hide();
                            $('body').removeClass('draggingEnabled');

                            if (response.success) {
                                if (devices.type == "ap") {
                                    venuesession.create(venuesession.sid, vm.networkConfig);
                                    navigation.goToAdddevice(vm.spid, networkVariable.currentUid, 1, 'networkconfig', serverdevType);
                                }
                                if (devices.type == "sensor" || devices.type == "guestSensor") {
                                    venuesession.create(venuesession.sid, vm.networkConfig);
                                    navigation.goToAdddevice(vm.spid, networkVariable.currentUid, 2, 'networkconfig', serverdevType);
                                }
                            } else {
                                var errormessage = response.body;
                                notificationBarService.error(errormessage);
                            }
                        },
                        error: function (err) {
                            console.log(err);
                            $(".macId").each(function () {
                                $(this).val("")
                                $(this).removeClass("addborder");
                            });
                            var noServerFound = '<li id="noServerFound">No Servers found!</li>';
                            $(networkVariable.currentElement).remove();
                            nodeData[devices.type] -= 1;
                            if (nodeData["total"] == 0) {
                                $("#network-tree").html(noServerFound);
                            }

                            var deviceText = nodeData["total"] == 1 ? " Device" : " Devices";
                            $(".device-section span").text(nodeData["total"] + deviceText);
                            $("#" + $(networkVariable.currentElement).attr("deviceId")).remove();
                            $("div[data-uid='" + deviceTree.uid + "']").closest("li").remove();
                            $(".networkconfig").hide();
                        }
                    });
                },
                'updateCoords': function (device, x, y) {
                    var self = this;
                    var networkconfig = {
                        uid: device.uid,
                        xposition: x,
                        yposition: y,
                        status: device.status,
                        type: device.type,
                        sid: device.sid,
                        spid: device.spid,
                        parent: device.parent
                    }
                    $.ajax({
                        url: '' + baseUrl + '/rest/site/portion/networkdevice/update',
                        method: 'post',
                        data: JSON.stringify(networkconfig),
                        headers: {
                            'content-type': 'application/json'
                        },
                        success: function (response) {
                            device.xposition = x,
                                device.yposition = y
                            $("#reposition-modal").hide();
                            $('body').removeClass('draggingEnabled');
                        },
                        error: function (error) {
                            $(self.currentElement).attr({
                                'x': self.originalPositon.x,
                                'y': self.originalPositon.y
                            });
                        }
                    })
                },
                'deleteNetworkDevice': function (device, prevArray, index) {
                    var uid = device.uid;
                    var spid = device.spid;
                    var type = device.type;
                    console.log("uid " + uid + " spid " + spid + " type " + type);
                    $.ajax({
                        method: 'post',
                        url: '' + baseUrl + '/rest/site/portion/networkdevice/delete?spid=' + spid + '&uid=' + uid + '&type=' + type,

                        success: function (response) {
                            console.log("Deleted successfully");
                            console.log(" respose " + JSON.stringify(response));
                            if (!prevArray) {
                                networkVariable.deleteImages(networkVariable.networkTree[device.type + device[device.type + "_id"]].devices);

                                delete networkVariable.networkTree[device.type + "" + device[device.type + "_id"]];
                            } else {
                                networkVariable.deleteImages(device);
                                prevArray.splice(index, 1);
                            }
                            delete networkVariable.initialParent;
                            $("#cancel-reposition").attr("isDelete", false);
                            $("#reposition-modal").hide();
                            $('body').removeClass('draggingEnabled');
                            activate();
                        },
                        error: function (err) {
                        }
                    })
                },
                'fetchurlParams': function (search) {
                    var urlObj = {};
                    if (search)
                        urlObj = JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g, '":"') + '"}');

                    urlObj = {
                        "cid": session.cid,
                        "sid": $rootScope.venueId,
                        "spid": $rootScope.spid,
                        "uid": "",
                    };
                    networkVariable.urlObj = urlObj;
                },
            }
            networkVariable.fetchurlParams(window.location.search.substr(1));
            Device = function (device, id, x, y, uid, childType, parent, source) {
                vm.node = {
                    type: device,
                    xposition: x,
                    yposition: y,
                    uid: uid,
                    createdBy: 'Qubercomm',
                    modifiedBy: 'Qubercomm',
                    createdOn: Date.now(),
                    modifiedOn: "",
                    score: 0,
                    ver: "1",
                    status: "Added",
                    template: null,
                    tags: null,
                    conf: "",
                    childType: childType,
                    name: device,
                    version: "1",
                    child: [],
                    source: source
                }
                vm.node.sid = networkVariable.urlObj.sid;
                vm.node.spid = networkVariable.urlObj.spid;
                vm.node[device + "_id"] = id;
                parent ? vm.node['parent'] = parent : '';
                return vm.node
            }
            networkTree = function (device) {
                if (device) {
                    this.devices = device;
                    return this;
                }
                this.devices = {};
            }
            networkTree.prototype.createParent = function (device, id, x, y, uid) {
                var newDevice = Device(device, id, x, y, uid, networkVariable.childType);
                var noparent = "";
                var source = "qubercomm";
                if (device == "sensor" || device == "guestSensor") {
                    noparent = "ble";
                    if (device == "guestSensor") {
                        var source = "guest";
                    }
                } else if (device == "ap") {
                    noparent = "ap";
                }
                networkVariable.saveNetworkDevice(uid, newDevice, false, "server", false, this, noparent, source);
                return this;
            }

            networkTree.prototype.addChild = function (nodeType, x, y, parentValue, uid) {
                var current = this.devices;
                var i = 0;
                var server = this.devices;
                var deviceMap = {
                    "switch": "SW",
                    "ap": "AP",
                    "sensor": "SNR"
                }
                var newDevice = Device(nodeType, nodeData[nodeType], x, y, uid, networkVariable.childType)
                var addSubChild = function (currentDevice, newDevice, parentValue) {
                    var id = currentDevice[currentDevice.type + "_id"]
                    if (currentDevice.uid == parentValue) {
                        networkVariable.saveNetworkDevice(uid, newDevice, currentDevice.type, newDevice.type, currentDevice.uid, currentDevice)
                        return
                    }
                    if (currentDevice.child.length) {
                        for (var i = 0; i < currentDevice.child.length; i++)
                            addSubChild(currentDevice.child[i], newDevice, parentValue);
                    }
                }
                addSubChild(current, newDevice, parentValue)
            }
            networkTree.prototype.getChild = function (device, deviceCount) {
                var current = this.devices;
                var devicemap = {
                    "switch": 'SW',
                    "ap": "AP",
                    "sensor": "SNR"
                }

                var getSubChild = function (currentDevice, device) {
                    var id = currentDevice[currentDevice.type + "_id"];
                    if (currentDevice.type == device) {
                        deviceCount.push(devicemap[currentDevice.type] + "-" + currentDevice.uid);
                        return;
                    }
                    for (var i = 0; i < currentDevice.child.length; i++)
                        getSubChild(currentDevice.child[i], device)
                };
                if (device == "ap") {
                    if (device == "ap" && deviceCount.length == 0) deviceCount.push("BLE-Master");
                    getSubChild(current, device);
                }
                if (device == "switch") {
                    if (device == "switch" && deviceCount.length == 0) deviceCount.push("AP-Master");
                    getSubChild(current, device);
                }
            };

            networkTree.prototype.deleteDevices = function (device, index) {
                var current = this.devices;
                if (device.indexOf("server") != -1) {
                    networkVariable.deleteNetworkDevice(current);
                    return
                }
                var deleteDevice = function (currentDevice, device, index, prevArray) {
                    var id = currentDevice[currentDevice.type + "_id"];
                    if (currentDevice.type + "" + id == device) {
                        networkVariable.deleteNetworkDevice(currentDevice, prevArray, index);
                        return;
                    }
                    for (var i = 0; i < currentDevice.child.length; i++)
                        deleteDevice(currentDevice.child[i], device, i, currentDevice.child);
                }
                deleteDevice(current, device, 0);
            }
            networkTree.prototype.updateDevices = function (nodeType, x, y, parentValue) {
                var current = this.devices;
                var updateChildDevices = function (nodeType, currentDevice, x, y) {
                    var id = currentDevice[currentDevice.type + "_id"];
                    var nodeArray = nodeType.replace(/[^0-9]+/ig, "");


                    if(parentValue=="bleserver")
                    {
                        parentValue="ble"
                    }
                    
                    if (parentValue == "ble" && (nodeType.indexOf("sensor") > -1)) {
                        if (id == nodeArray) {
                            networkVariable.updateCoords(currentDevice, x, y);
                        }
                        return
                    }
                    if (parentValue == "ble" && (nodeType.indexOf("server") > -1)) {
                        if (id == nodeArray) {
                            networkVariable.updateCoords(currentDevice, x, y);
                        }
                        return
                    }
                    if (parentValue == "ap" && (nodeType.indexOf("ap") > -1)) {
                        if (id == nodeArray) {
                            networkVariable.updateCoords(currentDevice, x, y);
                        }
                        return
                    }
                    if (currentDevice.type + "" + id == nodeType) {
                        networkVariable.updateCoords(currentDevice, x, y);
                        return
                    }
                    for (var i = 0; i < currentDevice.child.length; i++)
                        updateChildDevices(nodeType, currentDevice.child[i], x, y);
                };
                updateChildDevices(nodeType, current, x, y);
            };

            networkTree.prototype.initialSetup = function () {
                var current = this.devices;
                var buildInitalSetup = function (currentDevice, parent) {
                    var image = "'../images/networkicons/" + currentDevice.type + "_active.png";
                    var type = currentDevice.type
                    var id = currentDevice[currentDevice.type + "_id"]
                    networkVariable.newObject(image, type, currentDevice.x, currentDevice.y, true /*initial flag*/, id)
                    if (type.indexOf("server") != -1)
                        networkVariable.addNode(type, "network", 'Online', id)
                    else
                        networkVariable.addNode(type, parent, 'Online', id)
                    for (var i = 0; i < currentDevice.child.length; i++)
                        buildInitalSetup(currentDevice.child[i], currentDevice.type + "" + id)
                }
                buildInitalSetup(current, 'network');
            }
            networkTree.prototype.plantDevices = function (currentDevice, parent) {
                var source = currentDevice.source;
                if (source == "guest") {
                    var image = "../images/networkicons/" + "guestSensor_inactive.png";
                } else {
                    var image = "../images/networkicons/" + currentDevice.type + "_inactive.png";
                }
                var type = currentDevice.type
                var id = currentDevice[currentDevice.type + "_id"]
                var status = currentDevice.status;

                if (currentDevice.parent == "ble") {
                    if (source == "guest") {
                        var newimg = "../images/networkicons/guestSensor_inactive.png";
                    } else {
                        var newimg = "../images/networkicons/sensor_inactive.png";
                    }
                    networkVariable.newObject(newimg, type, currentDevice.xposition, currentDevice.yposition, currentDevice.uid, true, id, status, currentDevice.parent, source)
                } else if (currentDevice.parent == "ap") {
                    var newimg = "../images/networkicons/ap_inactive.png";
                    networkVariable.newObject(newimg, "ap", currentDevice.xposition, currentDevice.yposition, currentDevice.uid, true, id, status, currentDevice.parent, source)
                } else {
                    networkVariable.newObject(image, type, currentDevice.xposition, currentDevice.yposition, currentDevice.uid, true, id, status, currentDevice.parent, source)
                }
                if (type.indexOf("server") != -1)
                    networkVariable.addNode(type, currentDevice.parent, (status == "Added" ? "Offline" : status), currentDevice.uid, id, currentDevice.source)
                else
                    networkVariable.addNode(type, parent, (status == "Added" ? "Offline" : status), currentDevice.uid, id, currentDevice.source)
            }
            networkTree.prototype.findDevice = function (type) {
                var current = this.devices;
                var deviceMap = {
                    "switch": "SW",
                    "ap": "AP",
                    "sensor": "SNR"
                }
                var findDeviceRecursively = function (currentDevice, type) {
                    if (currentDevice.type == type) {
                        var parent = deviceMap[currentDevice.type] + "-" + currentDevice.uid
                        networkVariable.initialParent = parent;
                    }
                    for (var i = 0; i < currentDevice.child.length; i++)
                        findDeviceRecursively(currentDevice.child[i], type)
                }
                findDeviceRecursively(current, type);

            }
            networkTree.prototype.addChildren = function () {
                var current = this.devices;
                this.plantDevices(current);
                var that = this;
                var recursiveDepthAdd = function (current) {
                    var children = networkVariable.childDevices
                    for (var i = 0; i < children.length; i++) {
                        if (current.uid == children[i].parent)
                            current.child.push(children[i]);
                    }
                    for (var i = 0; i < current.child.length; i++) {
                        that.plantDevices(current.child[i], current.uid)
                        recursiveDepthAdd(current.child[i])
                    }
                }
                recursiveDepthAdd(current)
            }
        }

        function getTree() {
            floordataservice.getNetworkdevice(networkVariable.urlObj.spid).then(function (response) {
                if (response) {
                    var tree = response;
                    vm.deviceList = [];
                    vm.deviceList = response;
                     vm.networkDeviceList = [];
                        for(var i=0; i< response.length; i++) {
                         var source = response[i].source;
                         var imagePath = '';
                         if(source == "guest") {
                            imagePath = "../images/networkicons/" + "guestSensor_inactive.png";
                         } else {
                            imagePath = "../images/networkicons/" + response[i].typefs + "_inactive.png";
                         }
                         if(response[i].parent == "ble") {
                        if (response[i].source == "guest") {
                            imagePath = "../images/networkicons/guestSensor_inactive.png";
                           } else {
                            imagePath= "../images/networkicons/sensor_inactive.png";
                          } 
                         } else if(response[i].parent == "ap") {
                            imagePath = "../images/networkicons/ap_inactive.png";
                         }
                         response[i].imagePath = imagePath;
                         vm.networkDeviceList.push(response[i]);
                        }
                    for (var i = 0; i < tree.length; i++) {
                        var type = tree[i].typefs;
                        var parent = tree[i].parent;

                        if (parent != undefined && parent == 'ap') {
                            type = 'server';
                        }
                        nodeData[type.toLowerCase()] += 1;
                        nodeData['total'] += 1;
                        var id = nodeData[type.toLowerCase()];

                        if (type.toLowerCase().indexOf("server") != -1) {
                            var device = Device("server", id, tree[i].xposition, tree[i].yposition, tree[i].uid, '', tree[i].parent, tree[i].source);
                            networkVariable.networkTree[type.toLowerCase() + "" + id] = new networkTree(device);

                        } else {
                            var device = Device(type.toLowerCase(), id, tree[i].xposition, tree[i].yposition, tree[i].uid, '', tree[i].parent, tree[i].source);
                            networkVariable.networkTree[type.toLowerCase() + "" + id] = new networkTree(device);
                            networkVariable.childDevices.push(device);
                        }
                    }
                    for (var key in networkVariable.networkTree) {
                        networkVariable.networkTree[key].addChildren();
                    }
                    $("#noServerFound").addClass("hide");
                    var deviceText = nodeData["total"] == 1 ? " Device" : " Devices";
                    $(".device-section span").text(nodeData["total"] + deviceText);
                    reposition = false;
                    disableZoom: false;
                }
            });
        }

        $(document).on("click", ".device-name", highlight);
        $(".location").on("click",
            function (evt) {
                evt.preventDefault();
                var uid = $(this).attr("data-uid");
                var type = $(this).attr("data-type");
                var status = $(this).attr("data-status");

                $("image").each(function () {
                    var datauri = $(this).attr("data-orig");
                    if (datauri)
                        $(this).attr("href", datauri);
                });
                if (uid && type && status) {
                    var url = $("image[dev-uid='" + uid + "']").attr("href");
                    $("image[dev-uid='" + uid + "']").attr("data-orig", url);
                    $("image[dev-uid='" + uid + "']").attr("href", "../images/networkicons/" + type.toLowerCase() + "_" + status.toLowerCase() + "_locate.gif");
                }
            });

        function highlight(evt) {
            evt.preventDefault();
            var statusMap = {
                'offline': 'inactive',
                'online': 'active',
                'idle': 'idle',
            }
            $(".device-name").removeClass("current");
            $(".deviceInfo a").attr("href", "#");
            $(this).addClass("current")
            $(".powerBtn").attr("uid", $(this).attr("data-uid"));

            var uid = $(this).attr("data-uid");
            var href = $(this).attr("data-href");
            var cref = $(this).attr("data-cref");
            var bref = $(this).attr("data-bref");
            var sref = $(this).attr("data-sref");
            var type = $(this).attr("data-type");
            var status = $(this).attr("data-status")
            $(".dshbrdLink").attr("href", href);
            $(".devcfgLink").attr("href", cref);
            $(".binaryLink").attr("href", bref);
            $(".scanLink").attr("href", sref);
            $("svg a").each(function (index, item) {
                $(item).find(".clone").hide();
                $($(item).children()[0]).show();
            })
            if (uid && type && status) {
                var $elem = $("image[dev-uid='" + uid + "']");
                var $parent = $elem.parent();
                $parent.find(".clone").remove();
                var $clone = $elem.clone(true);
                $clone.attr("class", "clone");
                $parent.append($clone)
                $elem.hide();
                $clone.show();
                $clone.attr("href", "../images/networkicons/" + type.toLowerCase() + "_" + statusMap[status.toLowerCase()].toLowerCase() + "_locate.gif")
            }
            $(".powerBtn").attr("devtype", type);
        }

        function activate() {
            vm.loadServiceQueue();
            document.getElementById('enlarge').addEventListener('click', function (ev) {
                $('.floorCanvas').toggleClass('deviceexpand');
                $('.floorsvgSmall').toggleClass('floorsvgExpand');
                $('.floorsvg').toggleClass('svgExpand');
            });
        }

        vm.zoomOutClick = function () {
            vm.zoom--;
        };

        vm.zoomInClick = function () {
            vm.zoom++;
        };
        vm.hoverIn = function(uid) {
            document.getElementById(uid+'li').style.background="#e2e2e2";
            document.getElementById(uid).style.display="block";        
            // suid = uid;
            // console.log(suid, uid);
       }
        vm.hoverOut = function(uid) {
            document.getElementById(uid+'li').style.background="transparent";
            document.getElementById(uid).style.display="none";
       }
    
        vm.colorIn = function(id) {
                $timeout(function() {
                    var ele = document.getElementById(id);
                    if(ele) {
                        ele.style.color="#29b1a8";
                    }
                }, 0)            
            
        }
        vm.colorOut = function(id) {
            $timeout(function() {
                var ele = document.getElementById(id);
                if(ele) {
                    ele.style.color="#908888";
                }
            }, 0)
                
        }
        vm.colorIn = function(id) {
            $timeout(function() {
                var ele = document.getElementById(id);
                if(ele) {
                    ele.style.color="#29b1a8";
                }
            }, 0)            
        
    }
    vm.colorOut = function(id) {
        $timeout(function() {
            var ele = document.getElementById(id);
            if(ele) {
                ele.style.color="#908888";
            }
        }, 0)
            
    }
    vm.goToPage = function(device, page) { 
        var uid    = device.uid;
        var spid   = device.spid;
        var sid    = device.sid;
        switch(page) {
          case 'gatewayinfo':
            if(uid) {                  
                  var navDetail = {};
                  navDetail.venue = vm.venueDetails.uid;
                  navDetail.floor = vm.selectedFloorname;
                  navDetail.room  = device.alias;
                  navDetail.spid  = device.spid;
                  localStorage.setItem("prevPageInfo", JSON.stringify(navDetail));
                  navigation.goToGatewayInfo(uid,sid,spid);
            }
            break;  
          case 'upgrade':
             if(uid) {
              var sid = localStorage.getItem('sid');
              var cid = localStorage.getItem('cid');
              var macadr = uid;
              navigation.goToUpgrade(sid, '0', macadr);
             }
             break;
          case 'gatewayedit':
             navigation.goToAdddevice(0, uid, 1, 'gatewayedit');   
             break;             
        }
        
      }
      vm.showDevice = function(device) {
        $('svg').children().each(function() {
            var $this = $(this);
           $this.children().each(function() {
               var $that = $(this);
               if(device.uid == $that.attr('dev-uid')) {
                var width = $that.attr('width')?$that.attr('width'):40;
                $($that).animate({width:width/5+'px',opacity:0},'1000000');
                $($that).animate({width:width/4+'px',opacity:1},'slow');
                $($that).animate({width:width/3+'px',opacity:1},'slow');
                $($that).animate({width:width/2+'px',opacity:1},'slow');
                $($that).animate({width:width/1+'px',opacity:1},'slow');
               }
           })
            // console.log(this)
        })
    }
    /*vm.searchFilterDevices = function(searchItem) {
        if(searchItem && searchItem != '' && searchItem != undefined) {
            for(var i=0; i< vm.networkDeviceList.length; i++) {
                if(searchItem == vm.networkDeviceList[i].uid) {
                    document.getElementById(vm.networkDeviceList[i].uid+'li').style.background="#e2e2e2";
                    document.getElementById(vm.networkDeviceList[i].uid).style.display="block"; 
                } else {
                    document.getElementById(vm.networkDeviceList[i].uid+'li').style.background="transparent";
                    document.getElementById(vm.networkDeviceList[i].uid).style.display="none";
                }
            }
        }
    }*/
    vm.checkDeviceId = function(item) {
        vm.networkDeviceList = vm.deviceList;
        vm.isClicked = !vm.isClicked;
        var searchDevice = "";
        if(item.id == 1) {
         searchDevice = "qubercomm";    
        } else if(item.id == 2){
            searchDevice = "guest";
        }
        var selectedDevice = [];
        angular.forEach(vm.networkDeviceList,function(category) {
            if(searchDevice == category.source){
                selectedDevice.push(category);
            }
        });
        item.isChecked = false;
        if(selectedDevice.length > 0 ) {
            vm.networkDeviceList = selectedDevice;
        } else {
            vm.networkDeviceList = vm.deviceList;
        }
    }


        activate();

        return vm;
    }
})();