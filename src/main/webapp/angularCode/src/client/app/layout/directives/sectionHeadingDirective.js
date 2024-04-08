(function () {
    'use strict';

    angular
      .module('app.layout')
      .directive('sectionHeading', function () {
          return {
              restrict: 'E',
              transclude: false,
              replace: true,
              scope: {
                  heading: '@',
                  panelHeadingCustomClass: '@'
          },
              template: '<div class="panel panel-default"><div class="panel-heading text-center" role="tab">' +
                        '<h4 class="panel-title {{panelHeadingCustomClass}}">{{heading}}</h4>' +
                        '</div></div>'
          };
      });

}());
