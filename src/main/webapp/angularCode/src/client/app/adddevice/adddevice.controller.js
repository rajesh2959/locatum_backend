(function () {
    'use strict';
    angular
        .module('app.adddevice')
        .controller('adddeviceController', controller);

    controller.$inject = ['messagingService', 'notificationBarService', 'spid', 'uid', 'pageFrom', 'serverType', '$q', 'modalService', 'floordataservice', 'navigation', '$rootScope', 'venuesession', 'session'];
    /* @ngInject */

    function controller(messagingService, notificationBarService, spid, uid, pageFrom, serverType, $q, modalService, floordataservice, navigation, $rootScope, venuesession, session) {
        var vm = this;
        vm.pagename = (pageFrom == "gatewayedit" || pageFrom == "registerdevice") ? "Edit Device" : "Add Device";
        vm.isEdit = (pageFrom == "gatewayedit" || pageFrom == "registerdevice");
        vm.buttonName = "Save";
        vm.conf = {};
        vm.device = {};
        vm.deviceInfo = {};
        vm.diagnosticList = [{ "id": 1, "diag_key": "", "diag_value": "" }];
        vm.device.param = "FloorConfig";
        vm.deviceuid = uid == 0 ? '' : uid;
        vm.isGatewayPage = uid == 0 ? false : true;
        vm.devicename = uid;
        vm.device.sid = vm.sid;
        vm.device.spid = spid;
        vm.device.cid = session.cid;
        vm.device.tlu = "1";
        vm.serverDevicetype = serverType;
        vm.isAddconfig = false;
        vm.validateConfig="";
        if (vm.pagename == "Add Device") {
            vm.isAddconfig = true;
        }

        if (pageFrom == "gatewayadd") {
            vm.isAddconfig = false;
            vm.validateConfig = "CustomConfig";
        }
        else{
            vm.validateConfig="FloorConfig";
        }
        if (venuesession.networkdevice) {
            vm.device.xposition = venuesession.networkdevice.xposition;
            vm.device.yposition = venuesession.networkdevice.yposition;
            vm.device.source = venuesession.networkdevice.source;
            vm.device.gparent = venuesession.networkdevice.gparent;
            vm.device.parent = venuesession.networkdevice.parent;
            vm.device.type = venuesession.networkdevice.type;
        }

        if (venuesession.sid) {
            vm.sid = venuesession.sid;
            $rootScope.venueId = vm.sid;
        }

        if (spid && spid != 0) {
            vm.spid = spid;
            $rootScope.spid = vm.spid;
        }

        if (uid && uid != 0) {
            vm.uid = uid;
            $rootScope.uid = vm.uid;
        }

        vm.typeList = [
            { "key": "receiver", "value": "Receiver" },
            { "key": "server", "value": "Server" },
            { "key": "scanner", "value": "Scanner" },
        ];

        vm.selectedType = "receiver";
        vm.sourceList = [
            { "key": "locatum", "value": "Locatum" },
            { "key": "ruckus", "value": "Ruckus" },

        ];
        //vm.selectedSource = "locatum";
        vm.logList = [
            { "key": "all", "value": "All" },
            { "key": "verbose", "value": "Verbose" },
            { "key": "debug", "value": "Debug" },
            { "key": "warning", "value": "Warning" },
            { "key": "error", "value": "Error" },
            { "key": "critical", "value": "Critical" },
            { "key": "fatal", "value": "Fatal" },
            { "key": "none", "value": "None" },
        ];
        vm.conf.loglevel = "all";
        vm.encryptionList = [
            { "key": "wpa2-psk", "value": "wpa2-psk" },
            { "key": "open", "value": "open" },
            { "key": "wpa2", "value": "wpa2" },
        ];
        vm.conf.encryption = "wpa2-psk";
        vm.configurationList = [
            { "key": "trilateration", "value": "Trilateration" },
            { "key": "unilateration", "value": "Unilateration" },
        ];
        vm.conf.configuration = "trilateration";

        vm.showNoDevicemessage = function () {
            var message = "<p>There are no device added for this floor. Please add a device to configure details.</p>";
            modalService.messageModal('Information', message).result.then(function () {
            });
        }

        vm.tab = function (index) {
            vm.tabIndex = index;
        };

        vm.addDiagnostic = function () {
            vm.isAdd = true;
            messagingService.broadcastCheckFormValidatity();
            angular.forEach(vm.diagnosticList, function (value, key) {
                if (!value.diag_key || !value.diag_value)
                    vm.isAdd = false;
            });
            if (vm.isAdd) {
                var newItem = { "id": vm.diagnosticList[vm.diagnosticList.length - 1].id + 1, "diag_key": "", "diag_value": "" };
                vm.diagnosticList.push(newItem);
            }
        }

        vm.deleteDiagnostic = function (diagnostic) {
            angular.forEach(vm.diagnosticList, function (value, key) {
                if (diagnostic.id == value.id && vm.diagnosticList.length != 1)
                    vm.diagnosticList.splice(key, 1);
            });
            vm.diagnosticList.push(newItem);
        };

        vm.autoConfigureChange = function () {
            if (vm.isAutoConfigureOn) {
                floordataservice.getAutoConfiguration().then(function (response) {
                    if (response) {
                        if (response.attributes) {
                            var conf = response.attributes[0];
                            vm.conf = {};
                            vm.device = {};
                            vm.device.param = "FloorConfig";
                            vm.device.uid = vm.deviceuid;
                            vm.device.name = vm.devicename;
                            vm.device.sid = vm.sid;
                            vm.device.spid = spid;
                            vm.device.cid = session.cid;
                            vm.device.tlu = "1";
                            if (venuesession.networkdevice) {
                                vm.device.xposition = venuesession.networkdevice.xposition;
                                vm.device.yposition = venuesession.networkdevice.yposition;
                                vm.device.source = venuesession.networkdevice.source;
                                vm.device.gparent = venuesession.networkdevice.gparent;
                                vm.device.parent = venuesession.networkdevice.parent;
                                vm.device.type = venuesession.networkdevice.type;
                            }
                            vm.conf = conf;
                            vm.conf.loglevel = "all";
                            vm.conf.encryption = "wpa2-psk";
                            vm.conf.configuration = "trilateration";
                        }
                    }
                });
            }
            else {
                vm.conf = {};
                vm.device = {};
                vm.device.param = "FloorConfig";
                vm.device.uid = vm.deviceuid;
                vm.device.sid = vm.sid;
                vm.device.name = vm.devicename;
                vm.device.spid = spid;
                vm.device.cid = session.cid;
                vm.device.tlu = "1";
                if (venuesession.networkdevice) {
                    vm.device.xposition = venuesession.networkdevice.xposition;
                    vm.device.yposition = venuesession.networkdevice.yposition;
                    vm.device.source = venuesession.networkdevice.source;
                    vm.device.gparent = venuesession.networkdevice.gparent;
                    vm.device.parent = venuesession.networkdevice.parent;
                    vm.device.type = venuesession.networkdevice.type;
                }
                //vm.selectedType = "receiver";
                //vm.selectedSource = "locatum";
                vm.conf.loglevel = "all";
                vm.conf.encryption = "wpa2-psk";
                vm.conf.configuration = "trilateration";
            }
        }
        vm.save = function (frm, advancedForm) {
            messagingService.broadcastCheckFormValidatity();
            if (advancedForm && advancedForm.$invalid)
                vm.showAdvancedFormValidation = true;
            else
                vm.showAdvancedFormValidation = false;

            if (vm.serverDevicetype == "bleserver") {
                vm.conf = {};
                vm.device = {};
                vm.device.param = "FloorConfig";
                vm.device.uid = vm.deviceuid;
                vm.device.sid = vm.sid;
                vm.device.name = vm.devicename;
                vm.device.spid = spid;
                vm.device.cid = session.cid;
                vm.device.tlu = "1";
                if (venuesession.networkdevice) {
                    vm.device.xposition = venuesession.networkdevice.xposition;
                    vm.device.yposition = venuesession.networkdevice.yposition;
                    vm.device.gparent = venuesession.networkdevice.gparent;
                    vm.device.parent = venuesession.networkdevice.parent;

                }
                vm.device.type = "bleserver";
                vm.device.source = "guest"
                vm.conf.loglevel = "all";
                vm.conf.encryption = "wpa2-psk";
                vm.conf.configuration = "trilateration";

                
            }

            if (frm.$valid) {
                var attributes = [];
                vm.conf.type = vm.selectedType;
                attributes.push(vm.conf);
                angular.forEach(vm.diagnosticList, function (value, key) {
                    attributes.push(value);
                });
                vm.device.uid = vm.deviceuid;
                vm.device.conf = {};
                vm.device.conf.attributes = attributes;

                if (pageFrom == "gatewayadd") {
                    vm.device.param = "CustomConfig";
                }

                vm.device.type = vm.serverDevicetype;
                vm.device.debugflag
                var myJSON = JSON.stringify(vm.device);
                if (vm.isEdit) {
                    vm.device.id = vm.deviceInfo.id;
                    vm.device.uid = vm.deviceInfo.mac_address;
                    vm.device.sid = vm.deviceInfo.sid;
                    vm.device.spid = vm.deviceInfo.spid;
                    vm.device.cid = vm.deviceInfo.cid;
                    if (vm.selectedSource == "ruckus") {
                        vm.device.source = "guest";
                        vm.device.type = "bleserver";
                    }
                    else {
                        vm.device.source = "qubercomm"
                        vm.device.type = "ble";
                    }
                    vm.device.name = vm.devicename;
                    vm.device.param = "CustomConfig";
                }
                else {
                    vm.device.id = null;
                    //vm.device.source = vm.selectedSource;
                    //vm.device.alias = "qubercomm";
                }
                //vm.device.source = vm.selectedSource;
                //vm.device.alias = "qubercomm";

                if (!vm.isEdit) {
                    floordataservice.checkDuplicateDevice(vm.device.uid, vm.validateConfig ).then(function (res) {
                        if (res && res.success && res.body == "new") {
                            saveDevice();
                        }
                        else
                            notificationBarService.error(res.body);
                    });
                }
                else
                    saveDevice();
            }
        }

        function saveDevice() {
            floordataservice.saveNetworkdevice(vm.device).then(function (response) {
                if (response) {
                    if (response.body) {
                        notificationBarService.success(response.body);
                        if (pageFrom == "networkconfig")
                            navigation.goToNetworkConfig(spid);
                        else if (pageFrom == "registerdevice")
                            navigation.goToRegisterDevice();
                        else
                            navigation.goToGateWay();
                    }
                }
            });
        }


        vm.checkMacaddress = function (macaddress) {
            if (macaddress != null && macaddress != undefined && macaddress!="") {
                floordataservice.checkDuplicateDevice(macaddress,vm.validateConfig ).then(function (res) {
                    if (res && res.success && res.body == "new") {

                    }
                    else {
                        vm.deviceuid = "";
                        notificationBarService.error(res.body);
                    }

                });
            }
        }


        vm.onSourceChange = function (item) {
            if (item == "locatum")
                vm.serverDevicetype = "ble";
            else
                vm.serverDevicetype = "bleserver"
        }

        vm.cancel = function () {
            var message = "<p>The changes to the device have not been saved yet. Are you sure you want to cancel the changes?</p>";
            modalService.questionModal('Device Cancellation', message, true).result.then(function () {
                if (pageFrom == "networkconfig")
                    navigation.goToNetworkConfig(spid);
                else if (pageFrom == "registerdevice")
                    navigation.goToRegisterDevice();
                else
                    navigation.goToGateWay();
            });
        }

        vm.getDeviceInfo = function () {
            floordataservice.getDeviceById(uid, session.cid).then(function (response) {
                if (response) {
                    var conf = response.conf.attributes[0];
                    if (conf) {
                        vm.selectedType = conf.type;
                        vm.conf = conf;
                        vm.deviceInfo.id = response.id;
                        vm.deviceInfo.mac_address = response.mac_address;
                        vm.deviceInfo.alias = response.alias;
                        vm.deviceInfo.cid = response.cid;
                        vm.deviceInfo.sid = response.sid;
                        vm.deviceInfo.spid = response.spid;
                        vm.devicename = response.alias;
                        vm.device.source = response.source;
                        if (vm.device.source == "qubercomm") {
                            vm.selectedSource = "locatum";
                        } else if (vm.device.source == "guest") {
                            vm.selectedSource = "ruckus";
                        }
                        else {
                            vm.selectedSource = vm.device.source;
                        }



                    }
                }
            });
        }

        function activate() {
            if (vm.isEdit)
                vm.getDeviceInfo(true);
            else
                if (serverType == "ble") {
                    vm.selectedSource = "locatum";
                }
                else if (serverType == "bleserver") {
                    vm.selectedSource = "ruckus";
                }
                else {
                    vm.selectedSource = "locatum";
                }
        }

        activate();

        return vm;
    }
})();