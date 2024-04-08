(function () {
    'use strict';

    angular
        .module('app.layout')
        .directive('navButtonPlaceholder', function () {
            return {
                restrict: 'E',
                transclude: false,
                replace: true,
                template: '<div class="col-md-4  p-holder" ><a class="nav-button"><div class="alert alert-info"><h4>&nbsp;</h4><p>&nbsp;</p></div></a></div>'

            };
        });

}());
