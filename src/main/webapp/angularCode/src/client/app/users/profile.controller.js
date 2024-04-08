(function () {
    'use strict';
    angular
        .module('app.users')
        .controller('profileController', controller);
    controller.$inject = ['messagingService', 'notificationBarService', '$q', 'modalService', 'navigation', '$rootScope', 'userService', 'addUserService', 'session', 'id', '$linq'];

    /* @ngInject */
    function controller(messagingService, notificationBarService, $q, modalService, navigation, $rootScope, userService, addUserService, session, id, $linq) {
        var vm = this;
        vm.pageHeight = screen.height - 180;
        vm.userid = id;
        vm.isAvailableCustomer = session && session.accessToken != "superadmin" ? false : true;
        vm.getRole = function () {
            userService.getRoleList()
                .then(function (result) {
                    vm.roleList = [];
                    angular.forEach(result, function (item) {
                        vm.roleList.push({ "key": item, "value": item });
                    });
                });
        };

        vm.getUser = function () {

            userService.getprofile().then(function (result) {
               
                vm.user = result;
                vm.user.isMailalert = result.isMailalert === "true";
                vm.user.isSmsalert = result.isSmsalert === "true";
            });
        };

        vm.save = function (frm) {
            messagingService.broadcastCheckFormValidatity();
            if (frm.$valid) {
                var payload = {};
                payload.id = vm.user.id;
                payload.fname = vm.user.fname;
                payload.lname = vm.user.lname;
                payload.designation = vm.user.designation;
                payload.email = vm.user.email;
                payload.phone = vm.user.phone;
                payload.role = vm.user.role;
                payload.password = vm.user.password;
                payload.isMailalert = vm.user.isMailalert;
                payload.isSmsalert = vm.user.isSmsalert;
                payload.solution = session.solution;
                payload.customerName = !vm.isAvailableCustomer ? session.accessToken : vm.custName;
                payload.customerId = session.cid;
                payload.loginCount = 0;

                addUserService.saveAddUser(payload).then(function (response) {
                    if (response) {
                        if (response.body && response.success) {
                            notificationBarService.success(response.body);
                        }
                        else
                            notificationBarService.error(response.body);
                    }
                });
            }
        };

        function activate() {
            vm.getRole();
            vm.getUser();
        }

        activate();

        return vm;
    }
})();