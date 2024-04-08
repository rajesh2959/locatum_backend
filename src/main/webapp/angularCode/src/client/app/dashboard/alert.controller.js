(function () {
    'use strict';

    angular
        .module('app.layout')
        .controller('alertController', controller);

    controller.$inject = ['$uibModalInstance', 'res'];
    /* @ngInject */
    function controller($uibModalInstance, res) {

        var vm = this;
        vm.alertsData = res;
        vm.ok = function () {
            $uibModalInstance.close();
        };

        activate();

        function activate() {
        }

        return vm;
    }
})();