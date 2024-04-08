/**
 * Heavily adapted from the `type="number"` directive in Angular's
 * /src/ng/directive/input.js
 */

angular.module('app.layout')
    .directive('money', ['$filter', function ($filter) {
    'use strict';

    var NUMBER_REGEXP = /^\s*(\-|\+)?(\d+|(\d*(\.\d*)))\s*$/;

    function link(scope, el, attrs, ngModelCtrl) {
        var min = 0, max, precision, lastValidValue;
        //var min = 0, max, precision = 2, lastValidValue;

        /**
         * Returns a string that represents the rounded number
         * @param  {Number} value Number to be rounded
         * @return {String}       The string representation
         */
        function formatPrecision(value) {
            if (!angular.isDefined(precision)) {
                precision = 2;
            }
            //return parseFloat(value).toFixed(precision);
            return $filter('number')(value, precision);
        }

        function formatViewValue(value) {
            return ngModelCtrl.$isEmpty(value) ? '' : '' + value;
        }

        function minValidator(value) {
            if (!ngModelCtrl.$isEmpty(value) && value < min) {
                ngModelCtrl.$setValidity('min', false);
                return undefined;
            } else {
                ngModelCtrl.$setValidity('min', true);
                return value;
            }
        }

        function maxValidator(value) {
            if (!ngModelCtrl.$isEmpty(value) && value > max) {
                ngModelCtrl.$setValidity('max', false);
                return undefined;
            } else {
                ngModelCtrl.$setValidity('max', true);
                return value;
            }
        }

        ngModelCtrl.$parsers.push(function (value) {
            //Custom code that removes thousand separator ','
            if (value != null && (angular.isUndefined(value) || value.indexOf(',') > 0)) {
                value = value.replace(/\,/g, '');
            }

            if (value == null || isNaN(value) || angular.isUndefined(value)) {
                value = '';
            }

            // Handle leading decimal point, like ".5"
            if (value.indexOf('.') === 0) {
                value = '0' + value;
            }

            // Allow "-" inputs only when min < 0
            if (value.indexOf('-') === 0) {
                if (min >= 0) {
                    value = null;
                    ngModelCtrl.$setViewValue('');
                    ngModelCtrl.$render();
                } else if (value === '-') {
                    value = '';
                }
            }

            var empty = ngModelCtrl.$isEmpty(value);
            if (empty || NUMBER_REGEXP.test(value)) {
                lastValidValue = (value === '')
                  ? null
                  : (empty ? value : parseFloat(value));
            } else {
                // Render the last valid input in the field
                ngModelCtrl.$setViewValue(null);
                ngModelCtrl.$render();
            }

            ngModelCtrl.$setValidity('number', true);

            return lastValidValue;
        });
        ngModelCtrl.$formatters.push(formatViewValue);


        // Min validation
        attrs.$observe('min', function (value) {
            min = parseFloat(value || 0);
            minValidator(ngModelCtrl.$modelValue);
        });

        ngModelCtrl.$parsers.push(minValidator);
        ngModelCtrl.$formatters.push(minValidator);


        // Max validation (optional)
        if (angular.isDefined(attrs.max)) {
            attrs.$observe('max', function (val) {
                max = parseFloat(val);
                maxValidator(ngModelCtrl.$modelValue);
            });

            ngModelCtrl.$parsers.push(maxValidator);
            ngModelCtrl.$formatters.push(maxValidator);
        }


        // Round off (disabled by "-1")
        if (attrs.precision !== '-1') {
            attrs.$observe('precision', function (value) {
                var parsed = parseFloat(value);
                precision = !isNaN(parsed) ? parsed : 2;

                // Trigger $parsers and $formatters pipelines
                ngModelCtrl.$setViewValue(formatPrecision(ngModelCtrl.$modelValue));
            });

            ngModelCtrl.$parsers.push(function (value) {
                if (value || value === 0) {//for some reason if value is zero, it falls through to the else which then returns undefined
                    // Save with rounded value
                    //lastValidValue = round(value);
                    lastValidValue = value;

                    return lastValidValue;
                } else {
                    return undefined;
                }
            });
            ngModelCtrl.$formatters.push(function (value) {
                return value ? formatPrecision(value) : value;
            });

            // Auto-format precision on blur
            el.bind('blur', function () {
                var value = ngModelCtrl.$modelValue;
                if (value) {
                    ngModelCtrl.$viewValue = formatPrecision(value);
                    ngModelCtrl.$render();
                }
            });
        }

        //prevent entry of more than 2 decimal places
        //needs more work before going live. we need to be able to perform the regex on the value taking into consideration the keypress but then reject it if the regex fails
        //el.bind('keydown keypress', function (e) {
        //    var twoDecimalPlacesRegex = /^\s*(?=.*[1-9])\d*(?:\.\d{1})?\s*$/;
        //    var value = ngModelCtrl.$modelValue;
        //    if (value && !twoDecimalPlacesRegex.test(value) && ((e.keyCode >= 48 && e.keyCode <= 57) || (e.keyCode >= 96 && e.keyCode <= 105))) {
        //        e.preventDefault();
        //    }
        //});

    }

    return {
        restrict: 'A',
        require: 'ngModel',
        link: link
    };
}]);
