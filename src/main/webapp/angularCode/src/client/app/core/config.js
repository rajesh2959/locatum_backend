(function () {
    'use strict';

    var core = angular.module('app.core');

    core.config(notificationsConfig);

    notificationsConfig.$inject = ['notificationsConfigProvider'];
    /* @ngInject */
    function notificationsConfig(notificationsConfigProvider) {
        // auto hide
        notificationsConfigProvider.setAutoHide(true);

        // delay before hide
        notificationsConfigProvider.setHideDelay(4000);

        // support HTML
        notificationsConfigProvider.setAcceptHTML(true);
    }

    var config = {
        appErrorPrefix: '[Locatumclient Error] ',
        appTitle: 'Locatum'
    };

    core.value('config', config);

    core.config(configure);

    configure.$inject = ['$logProvider', 'routerHelperProvider', 'exceptionHandlerProvider'];
    /* @ngInject */
    function configure($logProvider, routerHelperProvider, exceptionHandlerProvider) {
        if ($logProvider.debugEnabled) {
            $logProvider.debugEnabled(true);
        }
        exceptionHandlerProvider.configure(config.appErrorPrefix);
        routerHelperProvider.configure({docTitle: config.appTitle + ': '});
    }

    core.config(['$httpProvider', function ($httpProvider) {
        //initialize get if not there
        if (!$httpProvider.defaults.headers.get) {
            $httpProvider.defaults.headers.get = {};
        }

        // Answer edited to include suggestions from comments
        // because previous version of code introduced browser-related errors

        //disable IE ajax request caching
        $httpProvider.defaults.headers.get['If-Modified-Since'] = 'Mon, 26 Jul 1997 05:00:00 GMT';
        // extra
        $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
        $httpProvider.defaults.headers.get['Pragma'] = 'no-cache';

        $httpProvider.interceptors.push('httpInterceptorService');
    }]);

    core.config(['showErrorsConfigProvider', function(showErrorsConfigProvider) {
        showErrorsConfigProvider.showSuccess(false);
    }]);

    core.config(['AnalyticsProvider', function (analyticsProvider) {
        analyticsProvider.setAccount('UA-68264431-1');
        analyticsProvider.trackPages(true);
        analyticsProvider.ignoreFirstPageLoad(true);
    }]);

    //core.config(['$qProvider', function ($qProvider) {
    //    $qProvider.errorOnUnhandledRejections(false);
    //}]);

})();
