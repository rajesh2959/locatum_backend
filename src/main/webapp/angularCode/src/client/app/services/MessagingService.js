(function () {
    'use strict';

    angular
        .module('app')
        .factory('messagingService', service);

    service.$inject = ['$rootScope'];

    function service($rootScope) {

        var svc = {};

        svc.broadcastCheckFormValidatity = function () {
            $rootScope.$broadcast('show-errors-check-validity');
        };

        svc.broadcastResetFormValidatity = function () {
            $rootScope.$broadcast('show-errors-reset');
        };

        svc.broadcastLoginSuccess = function () {
            $rootScope.$broadcast('login.success');
        };

        svc.listenForLoginSuccess = function (callback) {
            $rootScope.$on('login.success', callback);
        };

        svc.broadcastGlobalErrorEvent = function (args) {
            $rootScope.$broadcast('globalErrorEvent', args);
        };

        svc.listenGlobalErrorEvent = function (callback) {
            $rootScope.$on('globalErrorEvent', callback);
        };

        svc.broadcastGlobalWarningEvent = function (args) {
            $rootScope.$broadcast('globalWarningEvent', args);
        };

        svc.listenGlobalWarningEvent = function (callback) {
            $rootScope.$on('globalWarningEvent', callback);
        };

        svc.broadcastGlobalClearErrorEvent = function () {
            $rootScope.$broadcast('globalClearErrorEvent');
        };

        svc.listenGlobalClearErrorEvent = function (callback) {
            $rootScope.$on('globalClearErrorEvent', callback);
        };

        svc.broadcastOnSuccessfullRouteChanged = function () {
            $rootScope.$broadcast('route.changedsuccess');
        };

        svc.listenForBroadcastSuccessfullRouteChanged = function (callback) {
            $rootScope.$on('route.changedsuccess', callback);
        };

        return svc;
    }
})();
