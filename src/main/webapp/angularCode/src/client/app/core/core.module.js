(function () {
    'use strict';

    angular
        .module('app.core', [
            'ngSanitize',
            'blocks.exception', 'blocks.logger', 'blocks.router',
            'ui.router', 'ngplus', 'ncy-angular-breadcrumb', 'LocalStorageModule', 'ui.bootstrap', 'ui.bootstrap.tooltip',
            'ui.bootstrap.showErrors', 'angularFileUpload', 'ngNotificationsBar', 'mgo-angular-wizard',
            'angular-google-analytics', 'dndLists', 'ngSlimScroll', 'localytics.directives',
            'googlechart', 'ngFileUpload', 'color.picker', 'ngIdle', 'gridstack-angular', 'chart.js','720kb.datepicker'
        ]);
})();