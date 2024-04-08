(function () {
    'use strict';

    angular
      .module('app.layout')
      .directive('formPanel', function () {
          return {
              restrict: 'E',
              transclude: true,
              replace: true,
              scope: {
                  panelHeading: '@',
                  panelCustomClass: '@'

              },
              template: '<div class="panel panel-default {{panelCustomClass}}"><div ng-show="panelHeading" class="panel-heading" role="tab">' +
                        '<h4 class="panel-title">{{panelHeading}}</h4>' +
                        '</div>' +
                        '<div class="panel-collapse collapse in" role="tabpanel">' +
                        '<div class="panel-body">' +
                        '<ng-transclude></ng-transclude>' +
                        '</div></div></div>'
          };
      });

}());
