(function () {
    'use strict';

    angular
        .module('app')
        .factory('dateService', service);

    function service() {
        var defaultMomentDateFormat = 'DD MMM YYYY';
        var defaultMonthYearDateFormat = 'MMMM YYYY';
        var svc = {};

        svc.getDefaultFromDate = function () {
            return moment().startOf('month').subtract(1, 'month').format(defaultMomentDateFormat);// jshint ignore:line
        };

        svc.getDefaultToDate = function () {
            return moment().endOf('month').format(defaultMomentDateFormat);// jshint ignore:line
        };

        svc.getFormattedDateForWebApi = function (date) {
            var formattedDate = '';

            if (date) {
                formattedDate = moment(new Date(date)).format(defaultMomentDateFormat);// jshint ignore:line
            }

            return formattedDate;
        };

        svc.getFormattedMoment = function (date) {
            return moment(new Date(date)).format(defaultMomentDateFormat);
        }

        svc.getTodaysDate = function () {
            return moment().format(defaultMomentDateFormat);
        };

        svc.getYesterdaysDate = function () {
            return moment().add(-1, 'days').format(defaultMomentDateFormat);
        };

        svc.getFormattedMonthYearDate = function (date) {
            return moment(new Date(date)).format(defaultMonthYearDateFormat);
        }

        svc.addMonths = function (date, numberOfMonths) {
            return moment(new Date(date)).add(numberOfMonths, 'months');

        };

        svc.getStartOfMonth = function (date) {
            return moment(new Date(date)).startOf('month');
        }

        svc.getMonthNumber = function (date) {
            return date.format('M');
        }

        svc.isValidDate = function (date) {
            if (angular.isDefined(date)) {
                return moment(date).isValid();
            } else {
                return false;
            }
        }

        return svc;
    }
})();
