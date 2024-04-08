(function () {
    'use strict';

    angular
        .module('app.layout')
        .controller('GenericQuestionModalController', controller);

    controller.$inject = ['$uibModalInstance', 'title', 'message', 'includeDangerHeader'];
    /* @ngInject */
    function controller($uibModalInstance, title, message, includeDangerHeader) {

        var vm = this;
        vm.title = title;
        vm.message = message;
        vm.includeDangerHeader = includeDangerHeader;

        vm.yes = function () {
            $uibModalInstance.close();
        };

        vm.no = function() {
            $uibModalInstance.dismiss('cancel');
        };

        return vm;
    }
})();
