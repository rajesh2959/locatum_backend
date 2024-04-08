(function () {
    'use strict';

    angular
        .module('app.layout')
        .directive('formdatepicker', ['dateService', function (dateService) {
            return {
                restrict: 'E',
                scope: {
                    ngModel: '=',
                    dateOptions: '=',
                    opened: '=',
                    isDisabled: '=',
                    isRequired: '=',
                    elementName: '@'
                },
                link: function ($scope, element, attrs) {

                    $scope.open = function (event) {
                        event.preventDefault();
                        event.stopPropagation();
                        $scope.opened = true;

                    };
                    $scope.clear = function () {
                        $scope.ngModel = null;
                    };
                },
                template: '<p class="input-group">' +
                            '<input name="elementName" type="text" class="form-control" uib-datepicker-popup="dd MMM yyyy" ng-model="ngModel" is-open="opened" min="minDate" max="maxDate" placeholder="DD MMM YYYY e.g. 01 Jan 2015"' +
                                'datepicker-options="dateOptions" date-disabled="disabled(date, mode)" ng-required="isRequired" close-text="Close" ng-disabled="isDisabled" />' +
                            '<span class="input-group-btn">' +
                            '<button type="button" class="btn btn-primary" style="line-height: 20px" ng-click="open($event)" ng-disabled="isDisabled"><i class="fa fa-calendar-o"></i></button>' +
                            '</span>' +
                            '</p>'
            };
        }]);
}());