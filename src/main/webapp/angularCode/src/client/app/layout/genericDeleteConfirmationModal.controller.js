(function () {
    'use strict';

    angular
        .module('app.layout')
        .controller('GenericDeleteConfirmationModalController', controller);

    controller.$inject = ['$uibModalInstance', 'confirmationMessage'];
    /* @ngInject */
    function controller($uibModalInstance, confirmationMessage) {

        var vm = this;
        vm.confirmationMessage = confirmationMessage;

        vm.yes = function () {
            $uibModalInstance.close();
        };

        vm.no = function() {
            $uibModalInstance.dismiss();
        };

        return vm;
    }
})();
