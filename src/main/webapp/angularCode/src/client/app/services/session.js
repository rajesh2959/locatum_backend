(function () {
    'use strict';

    angular
        .module('app')
        .service('session', session);

    session.$inject = ['localStorageService', 'environment'];

    function session(localStorageService, env) {

        var localStorageSessionKey = 'LocatumPortal-' + env.environment + '-AuthData';

        this.create = function (cid, accessToken, role, solution) {
            this.setLocalStorageProperties(cid, accessToken, role, solution);
            this.setSessionProperties(cid, accessToken, role, solution);
        };

        this.destroy = function () {
            this.setLocalStorageProperties();
            this.setSessionProperties();
        };

        this.load = function () {
            var localData = localStorageService.get(localStorageSessionKey);
            if (localData) {
                this.setSessionProperties(localData.cid, localData.accessToken, localData.role, localData.solution);
            }
        };

        this.setSessionProperties = function (cid, accessToken, role, solution) {
            this.cid = cid;
            this.accessToken = accessToken;
            this.role = role;
            this.solution = solution;
        };

        this.setLocalStorageProperties = function (cid, accessToken, role, solution) {
            localStorageService.set(localStorageSessionKey, {
                accessToken: accessToken,
                cid: cid,
                role: role,
                solution: solution
            });
        };
    }
})();
