angular.module('app.layout')
.directive('multipleEmailsValidation', function () {
    var EMAIL_REGEXP = /^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;
    function validateAll(ctrl, validatorName, value, formCtrl) {
        var validity = ctrl.$isEmpty(value) || value.split(';').every(
            function(email) {
                 return EMAIL_REGEXP.test(email.trim());
            }
        );
        ctrl.$setValidity(validatorName, validity);
        if (formCtrl.$dirty == true)
            return validity ? value : undefined;
        else
            return value;
    }

    return {
        restrict: 'A',
        require: ['^form','ngModel'],
        link: function postLink(scope, elem, attrs,formCtrl) {
            function multipleEmailsValidator(value) {
                return validateAll(formCtrl[1], 'multipleEmailsValidation', value, formCtrl[0]);
            };
            formCtrl[1].$formatters.push(multipleEmailsValidator);
            formCtrl[1].$parsers.push(multipleEmailsValidator);
        }
    };
});