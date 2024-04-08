(function () {
    'use strict';
    angular
        .module('app.geofence')
        .controller('AddGeoFenceController', controller);
    controller.$inject = ['dashboardDataService', 'floordataservice', 'geoFenceService', 'geoFenceAlertService', 'spid', 'geofenceid', 'environment', 'session', 'modalService', 'navigation', '$linq', 'venuesession', 'notificationBarService', 'messagingService', 'venuedataservice'];

    /* @ngInject */
    function controller(dashboardDataService, floordataservice, geoFenceService, geoFenceAlertService, spid, geofenceid, env, session, modalService, navigation, $linq, venuesession, notificationBarService, messagingService, venuedataservice) {
        var vm = this;
        var token,tokentype,tokenstatus,tokenshape,coor,fenceclick,shapeclick;

        var baseUrl = env.serverBaseUrl;
        vm.shapes = [];
        vm.canvasWidth = 0;
        vm.canvasHeight = 0;
        var tempId = 0;
        vm.isEdit = (geofenceid != "0");
        vm.pageName = "Add Geofence";
        vm.isAdd = true;
        vm.geofenceid = geofenceid;
        if (vm.isEdit) {
            vm.isAdd = false;
            vm.pageName = "Edit Geofence";
        }
        vm.fence = {};
        vm.colorList = [
            { name: 'black', color: '#000000' },
            { name: 'red', color: '#ff0000' },
            { name: 'green', color: '#00ff00' },
            { name: 'blue', color: '#0000ff' }
        ];
        vm.color = '#FF0000';
        vm.editableColor = '#0000FF';
        vm.options = {
            required: true,
            format: 'hexString',
            case: 'upper',
            round: false,
            inline: false,
        };
    

        if (venuesession.sid) {
            vm.sid = venuesession.sid;
        }

        if (spid && spid != 0) {
            vm.spid = spid;
        }

        vm.colorChange = function () {
            //vm.color = vm.selectedColor.color;
            //if (vm.fence.uiCoordinates) {
            //    //vm.fence.uiCoordinates.stroke = vm.color;
            //    //vm.fence.stroke = vm.color;
            //}

            //var objs = canvas.getObjects().map(function (o) {
            //    return o;
            //});
            //var ddata = $linq.Enumerable().From(objs)
            //    .Where(function (x) {
            //        return x.selectable == true
            //    }).FirstOrDefault();
            //canvas.remove(ddata);
            //ddata.stroke = vm.color;
            //vm.fence.uiCoordinates.stroke = vm.color;
            //canvas.add(ddata);
        };

        vm.getFloorImage = function () {
            floordataservice.getNetworkConfig(vm.spid).then(function (response) {
                if (response.body) {
                    vm.updateCanvas(response.body);
                }
            });
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
                    if (vm.spid) {
                        vm.onFloorChanges(vm.spid);
                    }
                    else {
                        vm.spid = vm.floorDetails[0].id;
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

        var edgedetection = 0;
        var canvas = this.__canvas = new fabric.Canvas('canvas');

        vm.updateCanvas = function (floorData) {
            var imagePath = baseUrl + "/web/site/portion/planfile?spid=" + vm.spid + "&cid=" + session.cid + "?time=" + new Date();
            vm.canvasWidth = floorData.width;
            vm.canvasHeight = floorData.height;
            canvas.backgroundColor = "white";
            canvas.setBackgroundImage(imagePath, canvas.renderAll.bind(canvas), {
                backgroundImageOpacity: 0.5,
                backgroundImageStretch: false
            });
            canvas.setDimensions({ width: vm.canvasWidth + 1, height: vm.canvasHeight + 1 });
            canvas.selection = false;
            vm.init();
        };

        var modifiedHandler = function (evt) {
            try {
                var obj = evt.target;

                var shape = (vm.isEdit) ? getShapeByGeoFenceId(geofenceid) : getShape(obj.id);
                if(shape == undefined){
                   shape = JSON.parse(localStorage.getItem("fenceclick"));
                }
                shape.uiCoordinates.angle = obj.angle;
                shape.uiCoordinates.top = obj.top;
                shape.uiCoordinates.left = obj.left;
                var height = obj.height * obj.scaleY;
                var width = obj.width * obj.scaleX;
                shape.uiCoordinates.height = height;
                shape.uiCoordinates.width = width;
                shape.uiCoordinates.coords = obj.oCoords;
                if (shape.uiCoordinates.shapeType == 3) {
                    shape.fenceType = "Triangle";
                    shape.xyPoints = [];

                    if ((obj.flipX && !obj.flipY) || (!obj.flipX && !obj.flipY)) {
                        shape.xyPoints.push({ x: vm.roundOff(obj.oCoords.bl.x), y: vm.roundOff(obj.oCoords.bl.y) });
                        shape.xyPoints.push({ x: vm.roundOff(obj.oCoords.br.x), y: vm.roundOff(obj.oCoords.br.y) });
                        shape.xyPoints.push({ x: vm.roundOff(obj.oCoords.mt.x), y: vm.roundOff(obj.oCoords.mt.y) });
                    } else {
                        shape.xyPoints.push({ x: vm.roundOff(obj.oCoords.tl.x), y: vm.roundOff(obj.oCoords.tl.y) });
                        shape.xyPoints.push({ x: vm.roundOff(obj.oCoords.tr.x), y: vm.roundOff(obj.oCoords.tr.y) });
                        shape.xyPoints.push({ x: vm.roundOff(obj.oCoords.mb.x), y: vm.roundOff(obj.oCoords.mb.y) });
                    }
                } else if (shape.uiCoordinates.shapeType == 1) {
                    shape.fenceType = "Rectangle";
                    shape.xyPoints = [];
                    shape.xyPoints.push({ x: vm.roundOff(obj.oCoords.bl.x), y: vm.roundOff(obj.oCoords.bl.y) });
                    shape.xyPoints.push({ x: vm.roundOff(obj.oCoords.br.x), y: vm.roundOff(obj.oCoords.br.y) });
                    shape.xyPoints.push({ x: vm.roundOff(obj.oCoords.tr.x), y: vm.roundOff(obj.oCoords.tr.y) });
                    shape.xyPoints.push({ x: vm.roundOff(obj.oCoords.tl.x), y: vm.roundOff(obj.oCoords.tl.y) });
                } else {
                    var x = ((obj.oCoords.mr.x - obj.oCoords.ml.x) / 2) + obj.oCoords.ml.x;
                    var y = ((obj.oCoords.mb.y - obj.oCoords.mt.y) / 2) + obj.oCoords.mt.y;
                    shape.xyPoints = [];
                    shape.xyPoints.push({ x: vm.roundOff(x), y: vm.roundOff(y) });
                    shape.xyPoints.push({ x: vm.roundOff(obj.oCoords.mr.x), y: vm.roundOff(obj.oCoords.mr.y) });
                    shape.uiCoordinates.radius = obj.getRadiusX();
                }

                vm.bindJsonData();
                shape.uiCoordinates.stroke = vm.color;
                shape.stroke = vm.color;
                shape.spid = vm.spid;
                vm.fence = shape;


            localStorage.setItem("coor", JSON.stringify(shape.uiCoordinates));
            localStorage.setItem("tokenshape", JSON.stringify(shape));

            } catch (error) {
            }
        };

        vm.roundOff = function (value) {
            return value.toFixed(2);
        };

        var moveHandler = function (e) {
            var obj = e.target;
            obj.setCoords();

            if (obj.left < edgedetection) {
                obj.left = 0;
            }

            if (obj.top < edgedetection) {
                obj.top = 0;
            }

            var height = obj.height * obj.scaleY;
            var width = obj.width * obj.scaleX;

            if ((width + obj.left) > (vm.canvasWidth - edgedetection)) {
                obj.left = (vm.canvasWidth - width);
            }

            if ((height + obj.top) > (vm.canvasHeight - edgedetection)) {
                obj.top = (vm.canvasHeight - height);
            }
        };

        canvas.on({
            'object:moving': moveHandler,
            'object:modified': modifiedHandler,
            //'mouse:down': mouseDownHandler,
        });

        vm.bindJsonData = function () {
            var qdata = [];
            for (var index = 0; index < vm.shapes.length; index++) {
                qdata.push(vm.shapes[index].xyPoints);
            }
        };

        vm.addShape = function (shapeType) {

                 if(shapeType == "1"){
                        tokentype = "Rectangle";
                 } else if(shapeType =="2"){
                        tokentype = "Circle"
                 } else {
                        tokentype = "Triangle"

                 }
                 localStorage.setItem("tokentype", tokentype);
            
            var shape;
            var shapeId = ++tempId;
            var objs = canvas.getObjects().map(function (o) {
                return o;
            });
            var ddata = $linq.Enumerable().From(objs)
                .Where(function (x) {
                    return x.stroke === vm.color
                }).FirstOrDefault();
            canvas.remove(ddata);
            vm.fence.uiCoordinates = {
                id: shapeId,
                shapeType: shapeType,
                left: 10,
                top: 10,
                width: 80,
                height: 40,
                fill: 'rgba(0,0,0,0)',
                stroke: vm.color,
                strokeWidth: 2,
                angle: 0,
                radius: 20,
                opacity: 0.8,
            };

            switch (shapeType) {
                case 1:
                    vm.fence.fenceType = "Rectangle";
                    shape = new fabric.Rect(vm.fence.uiCoordinates);
                    break;
                case 2:
                    vm.fence.fenceType = "Circle";
                    vm.fence.uiCoordinates.lockUniScaling = true;
                    vm.fence.uiCoordinates.centeredScaling = true;
                    shape = new fabric.Circle(vm.fence.uiCoordinates);
                    shape.hasRotatingPoint = false;
                    shape.maxWidth = vm.canvasWidth;
                    shape.maxHeight = vm.canvasHeight;
                    break;
                case 3:
                    vm.fence.fenceType = "Triangle";
                    vm.fence.uiCoordinates.centeredScaling = true;
                    shape = new fabric.Triangle(vm.fence.uiCoordinates);
                    break;
                case 4:
                    break;
            }
            vm.fence.cid = session.cid;
            vm.fence.sid = venuesession.sid
            vm.fence.spid = vm.spid;
            vm.fence.xyPoints = [];
            vm.shapes.push(vm.fence);
            canvas.add(shape);
            canvas.renderAll();

            localStorage.setItem("shapeclick", JSON.stringify(vm.fence));
        };

        vm.cancel = function () {
            var message = "<p>The changes to the Geofence have not been saved yet. Are you sure you want to cancel the changes?</p>";
            modalService.questionModal('Geofence Cancellation', message, true).result.then(function () {
                navigation.goToGeoFence(vm.spid);
            });
        };

        vm.init = function () {
            vm.shapes = [];
            geoFenceService.getGeoFenceList(vm.spid)
                .then(function (result) {
                    vm.fence.status = "disabled";
                    vm.fence.statusType = vm.getStatusType(vm.fence.status);
                    for (var i = 0; i < result.length; i++) {
                        result[i].uiCoordinates = JSON.parse(result[i].uiCoordinates);
                        result[i].isActive = (result[i].id == geofenceid);
                        if (result[i].id == geofenceid) {
                            result[i].statusflag = (result[i].status.toLowerCase() == "enabled");
                            result[i].statusType = vm.getStatusType(result[i].status);
                            vm.fence = result[i];
                        }
                    }

                    //console.log("result" + JSON.stringify(result))
                    vm.loadShapes(result);
                });
        };

        vm.getStatusType = function (type) {
            return (type.toLowerCase() == "enabled") ? "Enabled" : "Disabled";
        };

        vm.onStatusChange = function () {

            vm.fence.status = (vm.fence.statusflag) ? "enabled" : "enabled"; // Temp fix
            vm.fence.statusType = vm.getStatusType(vm.fence.status);

                 token = vm.fence.name;                 
                 tokenstatus = vm.fence.statusflag;
                 localStorage.setItem("token", token);
                 localStorage.setItem("tokenstatus", tokenstatus);
        };

        vm.loadShapes = function (shapes) {

            if (shapes && shapes.length > 0) {
                tempId = shapes.length;
                for (var i = 0; i < shapes.length; i++) {
                    var shape;
                    var cachefence = shapes[i].pkid;
                    //console.log("shapes>>>>" + JSON.stringify());
                    switch (shapes[i].uiCoordinates.shapeType) {
                        case 1:
                            shape = new fabric.Rect(shapes[i].uiCoordinates);
                            break;
                        case 2:
                            shape = new fabric.Circle(shapes[i].uiCoordinates);
                            shape.hasRotatingPoint = false;
                            shape.maxWidth = vm.canvasWidth;
                            shape.maxHeight = vm.canvasHeight;
                            break;
                        case 3:
                            shape = new fabric.Triangle(shapes[i].uiCoordinates);
                            break;

                        default:
                            break;
                    }
                    if (shapes[i].id != geofenceid) {
                        shape.stroke = vm.editableColor;
                        shape.selectable = false;
                    }
                    if(cachefence == undefined){
                        shape.stroke = vm.color;
                        shape.selectable = true;
                    }
                    canvas.skipOffscreen = false;
                    canvas.add(shape);
                    canvas.renderAll();
                    vm.shapes.push(shapes[i]);
                }
            }
            vm.getAlerts();
            vm.bindJsonData();
        };

        function getShape(controlId) {
            return $linq.Enumerable().From(vm.shapes)
                .Where(function (x) {
                    return !x.id
                }).FirstOrDefault();
        }

        function getShapeByGeoFenceId(fenceId) {
            return $linq.Enumerable().From(vm.shapes)
                .Where(function (x) {
                    return x.id === fenceId
                }).FirstOrDefault();
        }

        vm.onFloorChanges = function (selectedfloor) {
            vm.spid = selectedfloor;
            vm.getFloorImage();
        };

        vm.delete = function () {
            var activeObject = canvas.getActiveObject();
            if (activeObject) {
                var message = "<p>Are you sure you want to delete the fence?</p>";
                modalService.questionModal('Remove Fence', message, true).result.then(function () {
                    canvas.remove(activeObject);
                    var shape = getShape(activeObject.id);
                    var index = vm.shapes.indexOf(shape);
                    if (index >= 0) {
                        vm.shapes[index].uiCoordinates = [];
                        vm.fence = vm.shapes[index];
                    }
                });
            }
        };

        vm.save = function (frm) {

            ////getting the local json (local storage cache json) when we reload the page
            //vm.fence = JSON.parse(localStorage.getItem("shapeclick"));
            //if(vm.fence != undefined) {
            //    vm.fence.status = "enabled";
            //    vm.fence.statusType = "Enabled";
            //    vm.fence.satatusFlag = true;
            //}
            //if(vm.fence.uiCoordinates == undefined || vm.fence.uiCoordinates == ''){
            //    vm.fence.uiCoordinates = JSON.parse(localStorage.getItem("coor"));
            //}

            messagingService.broadcastCheckFormValidatity();
            if (frm.$valid) {
                vm.fence.associatedAlerts = [];
                angular.forEach(vm.alertList, function (value, key) {
                    if (value.isChecked)
                        vm.fence.associatedAlerts.push(value.id);
                });



                //console.log("the fence uiCoordinates" + JSON.stringify(vm.fence.uiCoordinates));
                if (vm.fence.uiCoordinates) {
                    //JSON.parse(fenceshape.replace(/&quot;/g,'"'));
                    vm.fence.uiCoordinates = JSON.stringify(vm.fence.uiCoordinates);
                    geoFenceService.saveFence(vm.fence).then(function (result) {
                        if (result && result.success)
                            navigation.goToGeoFence(vm.spid);
                        notificationBarService.success(result.body);
                    });
                }
                else {
                    var message = "<p>There are no geofence type for this geofence. Please add a geofence type to save.</p>";
                    modalService.messageModal('Information', message).result.then(function () {
                    });
                }
            }



      localStorage.clear();

        };

        vm.getAlerts = function () {
            if (vm.spid != 0) {
                geoFenceAlertService.getGeofenceAlertsBySpid(session.cid, venuesession.sid, vm.spid)
                    .then(function (result) {
                        vm.alertList = result;
                        angular.forEach(vm.fence.associatedAlerts, function (v, k) {
                            angular.forEach(vm.alertList, function (value, key) {
                                if (v.id == value.id) {
                                    value.isChecked = true
                                }
                            });
                        });
                    });
            }
        };

        vm.getdevices = function () {
            dashboardDataService.getDeviceList(vm.spid).then(function (res) {
                vm.bindDeviceImages(res);
            });
        };

        vm.bindDeviceImages = function (devices) {
            angular.forEach(devices, function (device, key) {
                device.id = "imgxy";
                var type = (device.parent == "ble") ? "sensor" : device.typefs;
                var image = "../images/" + type + "_" + status + ".png";
                if (device.source != "guest") {
                    image = "../images/networkicons/" + type + "_" + device.status + ".png";
                } else {
                    image = "../images/networkicons/" + "guestSensor_inactive.png";
                }
                fabric.Image.fromURL(image, function (img) {
                    img.height = 40;
                    img.width = 40;
                    img.selectable = false;
                    canvas.add(img).renderAll();
                }, {
                        id: device.id,
                        left: parseInt(device.xposition),
                        top: parseInt(device.yposition),
                        hasBorders: false,
                        hasControls: false,
                        hasRotatingPoint: false,
                        lockMovementX: true,
                        lockMovementY: true,
                        selectable: false
                    });
            });
        };

        function activate() {
            vm.getdevices();
            vm.loadServiceQueue();
        }

        activate();
        //localstorage values get area
        var fencename = localStorage.getItem("token");
        var fencetype = localStorage.getItem("tokentype");
        var fencestatus = "enabled"; //localStorage.getItem("tokenstatus");
        var fenceshape = localStorage.getItem("tokenshape");

        if(fenceshape != null){
             var k = JSON.parse(fenceshape.replace(/&quot;/g,'"'));
        var fencearray = [];
        fencearray.push(k);
         vm.loadShapes(fencearray);
        }
          
        vm.fence.name = fencename;
        vm.fence.fenceType = fencetype;
        if(fencestatus == "true"){
            vm.fence.statusflag = true;
        } else {
            vm.fence.statusflag = false;
        }
        
        return vm;
    
    }
})();
