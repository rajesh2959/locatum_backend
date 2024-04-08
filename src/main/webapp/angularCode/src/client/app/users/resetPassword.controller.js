(function () {
    'use strict';
    angular
        .module('app.users')
        .controller('resetPasswordController', controller);
    controller.$inject = ['messagingService', 'notificationBarService', '$q', 'modalService', 'navigation', '$rootScope', 'userService', '$uibModalInstance', 'addUserService', 'session', 'id','userPwdReset', '$linq'];

    /* @ngInject */
    function controller(messagingService, notificationBarService, $q, modalService, navigation, $rootScope, userService, $uibModalInstance, addUserService, session, id,userPwdReset, $linq) {
        var vm = this;
        vm.pageHeight = screen.height - 180;
        vm.userid = id;
        vm.isUserPwdReset = userPwdReset;
      
        vm.reset = function (frm) {
            messagingService.broadcastCheckFormValidatity();
            if (frm.$valid) {


                userService.getprofile().then(function (result) {
                   if(result!=undefined)
                   {
                    var payload = {};
                    if(vm.isUserPwdReset == true){
                        payload.id = vm.userid;
                    } else {
                        payload.id = result.id;
                    }
                    payload.p = vm.user.password;
                    payload.cp = vm.user.cpassword;
                    addUserService.resetPassword(payload).then(function (response) {
                        if (response) {
                            $uibModalInstance.close("close");
                            if (response.body && response.success)
                                notificationBarService.success(response.body);
                            else
                                notificationBarService.error(response.body);
                        }
                    });
                   }                    
                });

               
            }
        };

        vm.cancel = function () {
            $uibModalInstance.close("cancel");
        };

        vm.close = function () {
            $uibModalInstance.close("close");
        };

        return vm;
    }
})();