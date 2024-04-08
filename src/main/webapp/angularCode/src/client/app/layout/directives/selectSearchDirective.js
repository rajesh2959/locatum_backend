(function () {
    'use strict';

    angular
     .module('app.layout')
     .directive('selectWatcher', function ($timeout) {
         return {
             restrict: 'A',
             scope: {
                
             },
             link: function (scope, element, attr) {
                 var last = attr.last;
                 if (last === "true") {
                     $timeout(function () {
                         $(element).parent().selectpicker('val', '');
                         $(element).parent().selectpicker('refresh');
                     });
                 }
             }
         };
     });
}());



