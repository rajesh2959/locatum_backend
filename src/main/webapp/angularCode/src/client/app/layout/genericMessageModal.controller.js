(function () {
    'use strict';

    angular
        .module('app.layout')
        .controller('GenericMessageModalController', controller);

    controller.$inject = [
        '$uibModalInstance', 'title', 'message', '$sanitize'];
    /* @ngInject */
    function controller($uibModalInstance, title, message, $sanitize) {

        var vm = this;
        vm.title = title;
        vm.message = message;

        vm.ok = function () {
            $uibModalInstance.close();
        };

        return vm;
    }
})();
