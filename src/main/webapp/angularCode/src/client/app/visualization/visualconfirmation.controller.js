(function () {
    'use strict';
    angular
        .module('app.users')
        .controller('visualconfirmationController', controller);
    controller.$inject = ['messagingService', '$uibModalInstance', 'name', 'description'];

    /* @ngInject */
    function controller(messagingService, $uibModalInstance, name, description) {
        var vm = this;
        vm.name = name;
        vm.description = description;
        vm.save = function (frm) {
            messagingService.broadcastCheckFormValidatity();
            if (frm.$valid) {
                $uibModalInstance.close(vm);
                //addUserService.resetPassword(payload).then(function (response) {
                //    if (response) {
                //        $uibModalInstance.close("close");
                //        if (response.body && response.success)
                //            notificationBarService.success(response.body);
                //        else
                //            notificationBarService.error(response.body);
                //    }
                //});
            }
        };

        vm.cancel = function () {
            $uibModalInstance.close();
        };

        vm.close = function () {
            $uibModalInstance.close();
        };

        return vm;
    }
})();