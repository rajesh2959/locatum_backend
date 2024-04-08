(function () {
    'use strict';

    var app = angular.module('app')
        .run(['session', 'venuesession', '$rootScope' , function (session, venuesession, $rootScope) {
            session.load();
            venuesession.load();
            $rootScope.customerName = session.accessToken;
            session.customer = session.accessToken;
        }]);

    app.run(['Idle', function (Idle) {
        Idle.watch();
    }]);

    app.config(function (IdleProvider) {
        IdleProvider.idle(14400);
        IdleProvider.timeout(20);
    });

    app.run(function ($rootScope, $window, Idle, session, navigation, venuesession, environment, dataService) {
        $rootScope.$on('IdleTimeout', function () {
            session.destroy();
            venuesession.destroy();
            dataService.clearCache();
            navigation.goToLogin();
        });

        $rootScope.$on('IdleStart', function () {

        });

        $rootScope.$on('IdleWarn', function (e, countdown) {

        });

        $rootScope.$on('IdleEnd', function () {
        });
    });
})();
