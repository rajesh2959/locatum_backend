(function () {
    'use strict';

    angular
        .module('app')
        .controller('InsufficientPermissionsController', controller);

    controller.$inject = ['insufficientPermissionService'];
    /* @ngInject */
    function controller(insufficientPermissionService) {

        var vm = {};

        function activate() {
            //vm.getData(); //THIS WAS REMOVED DUE TO THE CALL NEEDING PERMISSIONS.
            vm.screenName = insufficientPermissionService.getScreenNameThatUserDoesntHavePermissionFor();
        }

        activate();
        return vm;

    }
})();