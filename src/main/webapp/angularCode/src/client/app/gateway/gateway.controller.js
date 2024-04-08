(function () {
    'use strict';
    angular
        .module('app.gateway')
        .controller('gatewayController', controller);
    controller.$inject = ['gatewayService', 'navigation', 'SimpleListScreenViewModel', 'notificationBarService', 'modalService', '$linq', '$rootScope', 'session', 'environment', '$timeout', 'tagService'];
    /* @ngInject */

    function controller(gatewayService, navigation, SimpleListScreenViewModel, notificationBarService, modalService, $linq, $rootScope, session, environment, $timeout, tagService) {
        var vm = new SimpleListScreenViewModel();
        vm.userRole = session.role;
        
        vm.envRole = environment.userRole;
        vm.isReceiverDebugAllOn = false;
        vm.isServerDebugAllOn = false;
        vm.dataOperationsReceiver = new SimpleListScreenViewModel();
        vm.dataOperationsServer = new SimpleListScreenViewModel();
        vm.isAdmin = false;
        vm.ischekAll = false;
        vm.isInitialLoad = true;
        vm.gateway = {};
        if (vm.userRole == vm.envRole) {
            vm.isAdmin = true;
        }

        vm.getReceivers = function (refresh) {
            gatewayService.getReceiverListforTable(refresh, vm.dataOperationsReceiver.dataOperations, vm.dataOperationsReceiver.filterFn)
                .then(function (result) {
                    vm.allReceiverDetails = result.allData;
                    vm.pagedDataReceiverDetails = result.pagedData;
                    vm.dataOperationsReceiver.fullCount = result.dataCount;
                    vm.dataOperationsReceiver.filteredCount = result.filteredDataCount;
                    if (vm.isInitialLoad) {
                        angular.forEach(result.allData, function (value, key) {
                            value.isChecked = false;
                            value.debugstatus = (value.debugflag === "checked");
                        });
                        vm.isInitialLoad = false;
                    }

                    angular.forEach(result.allData, function (value, key) {
                        value.debugstatus = (value.debugflag === "checked");
                    });
                    vm.isReceiverDebugAllOn = !$linq.Enumerable().From(result.allData)
                        .Any(function (x) {
                            return !x.debugstatus
                        });
                });
        };

        vm.getServers = function (refresh) {
            gatewayService.getServerListForTable(refresh, vm.dataOperationsServer.dataOperations, vm.dataOperationsServer.filterFn)
                .then(function (result) {
                    vm.allServerDetails = result.allData;
                    vm.pagedDataServerDetails = result.pagedData;
                    vm.dataOperationsServer.fullCount = result.dataCount;
                    vm.dataOperationsServer.filteredCount = result.filteredDataCount;
                    angular.forEach(result.allData, function (value, key) {
                        value.debugstatus = (value.debugflag === "checked");
                    });
                    vm.isServerDebugAllOn = !$linq.Enumerable().From(result.allData)
                        .Any(function (x) {
                            return !x.debugstatus
                        });
                });
        };

        vm.ssh = function (item) {
            if (item.cmd_enable == '1') {
                var str = 'http://' + item.ip + ':4200';
                window.open(str, item.ip, 'location=1;width=700,height=350');
            }
        };

        vm.searchBy = function (search) {
            if (search && search.length > 0) {
                vm.dataOperationsReceiver.filterFn = function (data) {
                    var lowerCaseSearchTerm = search.toLowerCase();
                    var result = (data.dev_name && data.dev_name.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.mac_address && data.mac_address.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.state && data.state.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.ip && data.ip.toLowerCase().contains(lowerCaseSearchTerm));
                    return result;
                };
            } else {
                vm.dataOperationsReceiver.filterFn = null;
            }
            vm.getReceivers(false);
        };

        vm.goToReceiverPage = function () {
            if (parseInt(vm.goToReceiverPageNo) > vm.dataOperationsReceiver.totalPageCount) {
                vm.goToReceiverPageNo = '';
            } else {
                vm.dataOperationsReceiver.dataOperations.paging.currentPage = vm.goToReceiverPageNo;
                vm.getReceivers();
                vm.goToReceiverPageNo = '';
            }
        };

        vm.goToServerPage = function () {
            if (parseInt(vm.goToServerPageNo) > vm.dataOperationsServer.totalPageCount) {
                vm.goToServerPageNo = '';
            } else {
                vm.dataOperationsServer.dataOperations.paging.currentPage = vm.goToServerPageNo;
                vm.getServers();
                vm.goToServerPageNo = '';
            }
        };

        vm.inputCheckAll = function (isChecked) {
            angular.forEach(vm.allReceiverDetails,
                function (value, key) {
                    if (isChecked == true) {
                        value.isChecked = true;
                    } else {
                        value.isChecked = false;
                    }
                });
        };

        vm.isRowChecked = function () {
            var isallchecked = true;
            angular.forEach(vm.allReceiverDetails, function (item) {
                if (item.isChecked == false) {
                    isallchecked = false;
                }
            });
            if (isallchecked)
                vm.ischekAll = true;
            else
                vm.ischekAll = false;
        }

        vm.addDevice = function () {
            navigation.goToAdddevice(0, 0, 1, 'gatewayadd');
        };

        vm.onDebugChange = function (receiver, type) {
            modalService.questionModal('Confirmation', 'Are you sure you want to change the debug status?').result.then(
                function () {
                    gatewayService.updateDebugStatus(receiver.mac_address, receiver.debugstatus).then(function (result) {
                        if (result && result.success) {
                            if (type === 1) vm.getReceivers(true);
                            vm.getServers(true);
                        }
                        notificationBarService.success(result.body);
                    });
                },
                function () {
                    receiver.debugstatus = !receiver.debugstatus
                });
        };

        vm.onReceiverDebugAllChange = function (type) {
            if (vm.allReceiverDetails.length > 0) {
                modalService.questionModal('Confirmation', 'Are you sure you want to change all debug status?').result.then(
                    function () {
                        gatewayService.updateAllDebugStatus(vm.isReceiverDebugAllOn, "receiver").then(function (result) {
                            if (result && result.success) {
                                vm.getReceivers(true);
                            }
                            notificationBarService.success(result.body);
                        });
                    },
                    function () {
                        vm.isReceiverDebugAllOn = !vm.isReceiverDebugAllOn
                    });
            } else {
                var message = "<p>There are no receiver found.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                    vm.isReceiverDebugAllOn = !vm.isReceiverDebugAllOn;
                });
            }
        };

        vm.onServerDebugAllChange = function (type) {
            if (vm.allServerDetails.length > 0) {
                modalService.questionModal('Confirmation', 'Are you sure you want to change all debug status?').result.then(
                    function () {
                        gatewayService.updateAllDebugStatus(vm.isServerDebugAllOn, "server").then(function (result) {
                            if (result && result.success) {
                                vm.getServers(true);
                            }
                            notificationBarService.success(result.body);
                        });
                    },
                    function () {
                        vm.isServerDebugAllOn = !vm.isServerDebugAllOn
                    });
            } else {
                var message = "<p>There are no server found.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                    vm.isServerDebugAllOn = !vm.isServerDebugAllOn;
                });
            }
        };

        vm.edit = function (item) {
            navigation.goToAdddevice(0, item.mac_address, 1, 'gatewayedit');
        };

        vm.gatewayinfo = function (uid, receiver) {
            var navDetail = {};
            navDetail.venue = receiver.siteName;
            navDetail.floor = receiver.floorName;
            navDetail.room = receiver.dev_name;
            navDetail.spid = receiver.spid;
            localStorage.setItem("prevPageInfo", JSON.stringify(navDetail));
            navigation.goToGatewayInfo(uid, 0, 0);
        };

        vm.upgrade = function (item) {
            console.log("itrm " + JSON.stringify(item));
            navigation.goToUpgrade(item.siteName, item.floorName, item.mac_address);
        };

        vm.delete = function (item, type) {
            var data = (type === 1) ? 'receiver' : 'server';
            var gatewayMacIds = [];
            gatewayMacIds.push(item.mac_address);
            if (gatewayMacIds.length > 0) {
                modalService.confirmDelete('Are you sure you want to delete the gateway?').result.then(
                    function () {
                        gatewayService.delete(gatewayMacIds).then(function (result) {
                            if (result && result.success) {
                                vm.ischekAll = false;
                                if (type === 1) vm.getReceivers(true);
                                else vm.getServers(true);
                            }
                            notificationBarService.success(result.body);
                        });
                    },
                    function () {
                    });
            } else {
                var message = "<p>There are no gateway selected for delete.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };

        vm.deleteAll = function (type) {
            var data = (type === 1) ? 'receiver' : 'server';
            var gatewayMacIds = [];
            angular.forEach(vm.allReceiverDetails,
                function (value, key) {
                    if (value.isChecked) {
                        gatewayMacIds.push(value.mac_address);
                    }
                });
            if (gatewayMacIds.length > 0) {
                var gatewayList = JSON.stringify(gatewayMacIds);
                modalService.confirmDelete('Are you sure you want to delete the gateway?').result.then(
                    function () {
                        gatewayService.delete(gatewayList).then(function (result) {
                            if (result && result.success) {
                                vm.ischekAll = false;
                                if (type === 1) vm.getReceivers(true);
                                else vm.getServers(true);
                            }
                            notificationBarService.success(result.body);
                        });
                    },
                    function () {
                    });
            } else {
                var message = "<p>There are no gateway selected for delete.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };

        vm.registerDevice = function () {
            navigation.goToRegisterDevice();
        };

        vm.gatewaystatus = function () {
            navigation.goToGatewayStatus();
        };

        function activate() {
            $rootScope.$emit('customEvent', "venue");
            vm.clearControl();
            vm.getReceivers(true);
            vm.getServers(true);
        }

        function initUploader() {

            vm.uploader = tagService.getFileUploaderInstance();

            vm.uploader.onErrorItem = function (item, response, status, headers) {
                $timeout(function () { progressModel.close(); }, 10);
                modalService.warrningMessageModel('Error!', '<div class="text-center"><h4 class="text-danger"><i class="fa fa-times text-danger faa-pulse animated" aria-hidden="true"></i>Unexpected error occurred,please try again!</h4></div>', false).result.then(
                    function (item) {
                    });
            };

            vm.uploader.onAfterAddingFile = function (FileUploader) {
                var fileReader = new FileReader();
                fileReader.readAsDataURL(FileUploader._file);
                fileReader.onload = function (e) {
                    $timeout(function () {
                        var target = e.target.result;
                        vm.gateway.imagePath = target;
                        var blob = null;
                        var imageDataUR = "";
                        if (vm.gateway.imagePath.indexOf("base64") != -1) {
                            imageDataUR = vm.gateway.imagePath;
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

                        gatewayService.saveGatewayImport(fd).then(function (result) {
                            notificationBarService.success(result.body);
                            activate();
                        });
                    });
                };
            };
        }

        function getB64Type($str) {
            return $str.split(';')[0].split(':')[1];
        }

        vm.clearControl = function () {
            document.getElementById("upload").value = "";
            vm.gateway.imagePath = "";
        };

        activate();
        initUploader();

        return vm;
    }
})();