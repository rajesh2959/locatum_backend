(function () {
    'use strict';

    angular
        .module('app')
        .factory('insufficientPermissionService', service);

    service.$inject = ['navigation'];

    function service(navigation) {

        var svc = {};
        var screenName = '';

        svc.tellUserTheyDontHavePermissions = function(screen) {
            screenName = screen;
            navigation.goToInsufficientPermissions();
        };

        svc.getScreenNameThatUserDoesntHavePermissionFor = function() {
            return screenName;
        };

        return svc;
    }

})();
