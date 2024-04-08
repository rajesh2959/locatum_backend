(function () {
    'use strict';

    angular
        .module('app.layout')
        .directive('navButton', function () {
            return {
                restrict: 'E',
                transclude: false,
                replace: true,
                scope: {
                    text: '@',
                    subText: '@',
                    sref: '@',
                    href:'@'
                },
                template: '<div class="col-md-4">' +
                            '<a class="nav-button" ng-if="sref" ui-sref="{{sref}}">' +
                            '<div class="alert alert-info"><h4>{{text}}</h4><p>{{subText}}</p></div></a>' +
                            '<a class="nav-button" ng-if="href" href="{{href}}" target="_blank">' +
                            '<div class="alert alert-info"><h4>{{text}}</h4><p>{{subText}}</p></div></a>' +
                        '</div>'

            };
        });

}());
