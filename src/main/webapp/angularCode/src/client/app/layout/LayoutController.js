(function () {
    'use strict';

    angular
        .module('app.layout')
        .controller('LayoutController', controller);

    /* @ngInject */
    function controller() {
        var vm = this;// jshint ignore:line

        vm.styleSheetUrl = '//maxcdn.bootstrapcdn.com/bootswatch/3.3.4/paper/bootstrap.min.css';

        activate();

        function activate() { }

        return vm;
    }
})();
