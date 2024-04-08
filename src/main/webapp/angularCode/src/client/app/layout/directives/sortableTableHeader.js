(function () {
    'use strict';

    angular
        .module('app.layout')
        .directive('sortableTableHeader', function () {
        return {
            restrict: 'E',
            transclude: false,
            replace: true,
            scope: {
                text: '@',
                attributename: '@',
                getdata: '&',
                sortpredicate: '=',
                sortorder: '='
            },
            controller: function($scope) {
                $scope.headerClick = function () {
                    $scope.sortpredicate = $scope.attributename;
                    $scope.sortorder = !$scope.sortorder;
                    $scope.getdata();
                };
                $scope.showAsc = function() {
                    return $scope.sortpredicate === $scope.attributename && !$scope.sortorder;
                };
                $scope.showDesc = function() {
                    return $scope.sortpredicate === $scope.attributename && $scope.sortorder;
                };
            },
            template: '<a href="" ng-click="headerClick()">' +
                      '{{text}}' +
                      '<span ng-show="showDesc()" class="fa fa-caret-down"></span>' +
                      '<span ng-show="showAsc()" class="fa fa-caret-up"></span>' +
                      '</a>'

            };
        });

}());
