(function () {
    'use strict';

    angular
        .module('app.layout')
        .directive('hyperlinkButton', function () {
        return {
            restrict: 'E',
            transclude: false,
            replace: true,
            scope: {
                titleText: '@',
                buttonType: '@',
                buttonSize: '@',
                eventHandler: '&ngClick',
                isDisabled: '=',
                icon: '@'
            },
            template: '<span title="{{titleText}}">' +
                '<button class="btn {{buttonType}} {{buttonSize}}" ng-disabled="isDisabled" ng-click="eventHandler()"><i class="{{icon}}"></i></button>' +
                '</span>'

        };
    });

}());
