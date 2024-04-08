(function () {
    'use strict';

    angular
        .module('app')
        .controller('404Controller', controller);

    controller.$inject = ['$location', '$rootScope'];
    /* @ngInject */
    function controller($location, $rootScope) {

        var vm = {};

        activate();

        function activate() {
            vm.currentLocation = $location.path();
            vm.urlPath = $rootScope.urlPath;
        }

        return vm;

    }
})();