(function () {
    'use strict';
    angular
        .module('app.dashboard')
        .controller('MainMenuController', controller);
    controller.$inject = ['navigation'];
    /* @ngInject */
    function controller(navigation) {
        var vm = {};

        vm.back = function () {
            navigation.goToParentState();
        }

        return vm;
    }
})();