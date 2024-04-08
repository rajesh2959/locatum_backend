(function () {
    'use strict';

    angular
        .module('app.layout')
        .directive('validationMessage', [function () {
            return {
                restrict: 'E',
                require: '^form',
                link: function (scope, element, attrs, formCtrl) {
                    scope.form = formCtrl;
                },
                template: "<span class='validate-error-red fade in validationmessage' ng-show='form.$submitted && form.$invalid'>There is a problem with the data on your form. Please check for mandatory fields </span>"
            };
        }]);
}());
