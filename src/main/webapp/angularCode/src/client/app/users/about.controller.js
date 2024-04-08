(function () {
    'use strict';
    angular
        .module('app.users')
        .controller('aboutController', controller);
    controller.$inject = ['$uibModalInstance'];

    /* @ngInject */
    function controller($uibModalInstance) {
        var vm = this;
        vm.pageHeight = screen.height - 180;

        vm.close = function () {
            $uibModalInstance.close("close");
        };

        return vm;
    }
})();