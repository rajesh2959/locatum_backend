(function () {
    'use strict';

    angular
        .module('app.layout')
        .controller('venuealertController', controller);

    controller.$inject = ['$uibModalInstance', 'res'];
    /* @ngInject */
    function controller($uibModalInstance, res) {

        var vm = this;

        vm.recentAlertsList = res;
        
        vm.ok = function () {
            $uibModalInstance.close();
        };

        activate();

        function activate() {
        }

        return vm;
    }
})();