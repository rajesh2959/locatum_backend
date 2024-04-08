(function () {
    'use strict';
    angular
        .module('app.layout')
        .controller('deviceModalController', controller);
    controller.$inject = ['$uibModalInstance', 'uid', 'messagingService', '$rootScope', 'gatewayService', 'floordataservice', 'notificationBarService'];

    /* @ngInject */
    function controller($uibModalInstance, uid, messagingService, $rootScope, gatewayService, floordataservice, notificationBarService) {
        var vm = this;
        vm.uid = uid;
        vm.currentUid = uid;
        vm.config="FloorConfig"

        //vm.macList = [
        //    { "key": "22:33:44:55:66:77", "value": "22:33:44:55:66:77" },
        //    { "key": "11:33:55:77:99:00", "value": "11:33:55:77:99:00" },
        //    { "key": "88:77:66:55:44:33", "value": "88:77:66:55:44:33" }
        //];

        vm.selectuid = function (index) {
            if (vm.currentUid) {
                var urlarray = vm.currentUid.split(":");
                return urlarray[index];
            }
            return "";
        };


        vm.macaddrChange = function () {

            var selectedmacid = vm.selectedmacId;
            var macidlist = selectedmacid.split(':');
            vm.mac0 = macidlist[0];
            vm.mac1 = macidlist[1];
            vm.mac2 = macidlist[2];
            vm.mac3 = macidlist[3];
            vm.mac4 = macidlist[4];
            vm.mac5 = macidlist[5];
            vm.mac6 = macidlist[6];
        };



        vm.getmacAddress = function () {
            if ($(".macAddress").css("display") != 'none') {
                $(".macId").each(function (index, node) {
                    if ($(this).val() == "") {
                        $(this).addClass("addborder")
                        return false;
                    } else
                        vm.currentUid += $(this).val() + (index < 5 ? ":" : "");
                })
                if (vm.currentUid.replace(/:/g, "").length < 12)
                    return false;
            }
            return vm.currentUid;
        };

        vm.save = function (frm) {
            $(".macId").each(function (index, node) {
                if ($(this).val() == "") {
                    $(this).addClass("addborder")
                }
                else {
                    $(this).removeClass("addborder")
                }
            })
            if (frm.$valid) {
                vm.getmacAddress();
                if (vm.currentUid.length == 17) {
                    floordataservice.checkDuplicateDevice(vm.currentUid,vm.config).then(function (res) {
                        if (res && res.success && res.body == "new") {
                            $uibModalInstance.close(vm.currentUid);
                        }
                        else
                            notificationBarService.error(res.body);
                    });
                }
                else
                    vm.currentUid = "";
            }
        };

        vm.cancel = function () {
            $uibModalInstance.close("cancel");
        };

        vm.close = function () {
            $uibModalInstance.close("close");
        };

        function activate() {

            vm.macList = [];
            gatewayService.getmacaddressList(vm.sid, true).then(function (res) {
                vm.macList = res;
            });
        }

        activate();

        return vm;
    }
})();
