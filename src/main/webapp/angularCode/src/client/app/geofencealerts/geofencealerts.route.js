(function () {
    'use strict';
    angular
        .module('app.geofencealerts')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.geofencealerts',
                config: {
                    url: 'fencealerts',
                    title: 'Geofence Alerts',
                    ncyBreadcrumb: { label: 'geofencealerts' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/geofencealerts/geofencealerts.html',
                            controller: 'GeoFenceAlertsController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
