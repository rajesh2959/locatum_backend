(function () {
    'use strict';
    angular
        .module('app.floorplan')
        .controller('addfloorplanController', controller);
    controller.$inject = ['messagingService', 'notificationBarService', 'modalService', 'environment', 'navigation', 'venuedataservice', '$rootScope', 'floordataservice', '$timeout', 'spid', 'session', 'venuesession'];
    /* @ngInject */

    function controller(messagingService, notificationBarService, modalService, env, navigation, venuedataservice, $rootScope, floordataservice, $timeout, spid, session, venuesession) {
        var vm = this;
        vm.latLngList = [];
        var baseUrl = env.serverBaseUrl;
     
        vm.venueDetails = {};
        vm.floorplan = {};
        vm.venueId = "";
        vm.pagename = "Add Floor Plan";
        vm.buttonName = "Save";
        vm.sid = "";
        if (venuesession.sid) {
            vm.sid = venuesession.sid;
            $rootScope.venueId = vm.sid;
        }

        if (spid && spid != 0) {
            vm.spid = spid;
            $rootScope.spid = vm.spid;
            vm.pagename = "Edit Floor Plan";
            vm.buttonName = "Update";
        }

        var loadServiceQueue = function () {
            venuedataservice.getVenueDetailById(vm.sid).then(function (res) {
                vm.venueDetails = res;
                if (vm.venueDetails) {
                    if (vm.venueDetails.uid) {
                        if (vm.venueDetails.uid.length > 21)
                            vm.venueDetails.newUid = vm.venueDetails.uid.substr(0, 20) + "...";
                        else
                            vm.venueDetails.newUid = vm.venueDetails.uid.substr(0, 20);
                    }
                    else
                        vm.venueDetails.newUid = "";
                }
            });

            floordataservice.getFloorPlanById(vm.spid).then(function (res) {
                if (res.body) {
                    vm.floorplan = res.body;
                    vm.floorplan.imagePath = baseUrl + "/web/site/portion/planfile?spid=" + vm.spid + "&cid=" + session.cid + "&time=" + new Date();
                }
            });
        };
                
        vm.save = function (frm) {
            messagingService.broadcastCheckFormValidatity();
            if (!vm.floorplan.imagePath) {
                notificationBarService.error("Please select the floor image");
            }
            else if (frm.$valid) {
                var blob = null;
                var postData = {};
                postData.id = spid;
                postData.siteId = vm.sid;
                postData.cid = session.cid;
                postData.uid = vm.floorplan.uid;
                postData.description = vm.floorplan.description;
                postData.width = vm.floorplan.width;
                postData.height = vm.floorplan.height;
                if (postData.id === "") {
                    postData.id = null;
                }
                var imageDataUR = "";
                if (vm.floorplan.imagePath.indexOf("base64") != -1) {
                    imageDataUR = vm.floorplan.imagePath;
                }

                var fd = new FormData();
                var data = atob(imageDataUR.replace(/^.*?base64,/, ''));
                var asArray = new Uint8Array(data.length);
                for (var i = 0, len = data.length; i < len; ++i) {
                    asArray[i] = data.charCodeAt(i);
                }
                var fileType = getB64Type(imageDataUR);
                blob = new Blob([asArray.buffer], { type: fileType });
                fd.append('file', blob);
                fd.append('floor', angular.toJson(postData));
                floordataservice.saveFloorPlan(fd).then(function (result) {
                    notificationBarService.success(result.body);
                    navigation.gotoFloorPlan();
                });
            }
        };

        vm.cancel = function () {
            var postData = {};
            postData.uid = vm.floorplan.uid;
            postData.description = vm.floorplan.description;
            postData.width = vm.floorplan.width;
            postData.height = vm.floorplan.height;
            postData.meter = vm.floorplan.meter;
            if (postData.uid != null || postData.description != null || postData.width != null || postData.height != null || postData.meter != null) {
                var message = "<p>The changes to the Floorplan have not been saved yet. Are you sure you want to cancel the changes?</p>";
                modalService.questionModal('Floorplan Cancellation', message, true).result.then(function () {
                    navigation.gotoFloorPlan();
                });
            }
            else {
                navigation.gotoFloorPlan();
            }
        };

        vm.drawFloorPlan = function () {
            if (vm.floorplan.uid != null && vm.floorplan.description != null) {
                navigation.goToDrawFloor(vm.sid, spid, vm.floorplan.uid, vm.floorplan.description);
            } else {
                var message = "<p>Please fill the title and Description.</p>";
                modalService.messageModal('Message', message, true).result.then(function () {
                    vm.tab(1);
                });
            }
        };

        vm.tab = function (index) {
            vm.tabIndex = index;
        };

        vm.clearControl = function () {
            vm.uploader.clearQueue();
            document.getElementById("upload").value = "";
            vm.floorplan.imagePath = "";
        };

        function getB64Type($str) {
            return $str.split(';')[0].split(':')[1];
        }

        function initUploader() {
            vm.uploader = floordataservice.getFileUploaderInstance();
            vm.uploader.onErrorItem = function() {
                $timeout(function() { progressModel.close(); }, 10);
                modalService.warrningMessageModel('Error!', '<div class="text-center"><h4 class="text-danger"><i class="fa fa-times text-danger faa-pulse animated" aria-hidden="true"></i>Unexpected error occurred,please try again!</h4></div>', false).result.then(
                    function(item) {
                    });
            };

            vm.uploader.onAfterAddingFile = function (FileUploader) {
                var fileReader = new FileReader();
                fileReader.readAsDataURL(FileUploader._file);
                fileReader.onload = function(e) {
                    $timeout(function() {
                        var target = e.target.result;
                        vm.floorplan.imagePath = target;
                    });
                };
            };
        }

        function activate() {
             loadServiceQueue();
            initUploader();
        }

        activate();

        return vm;
    }
})();