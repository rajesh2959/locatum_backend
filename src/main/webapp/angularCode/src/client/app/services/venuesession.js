(function () {
    'use strict';

    angular
        .module('app')
        .service('venuesession', session);

    session.$inject = ['localStorageService', 'environment'];

    function session(localStorageService, env) {

        var localStorageSessionKey = 'LocatumPortal-' + env.environment + '-VenueData';

        this.create = function (sid, networkdevice) {// jshint ignore:line
            this.setLocalStorageProperties(sid, networkdevice);
            this.setSessionProperties(sid, networkdevice);
        };

        this.destroy = function () {// jshint ignore:line
            this.setLocalStorageProperties();
            this.setSessionProperties();
        };

        this.load = function () {// jshint ignore:line
            var localData = localStorageService.get(localStorageSessionKey);
            if (localData) {
                this.setSessionProperties(localData.sid, localData.networkdevice);
            }
        };

        this.setSessionProperties = function (sid, networkdevice) {// jshint ignore:line
            this.sid = sid;
            this.networkdevice = networkdevice;
        };

        this.setLocalStorageProperties = function (sid, networkdevice) {// jshint ignore:line
            localStorageService.set(localStorageSessionKey, {
                // jshint ignore:line
                sid: sid,
                networkdevice: networkdevice
            });
        };
    }


})();
