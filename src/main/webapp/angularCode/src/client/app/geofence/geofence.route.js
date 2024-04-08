(function () {
    'use strict';
    angular
        .module('app.geofence')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.geofence',
                config: {
                    url: 'geofence/{spid}',
                    title: 'Geo Fence',
                    ncyBreadcrumb: { label: 'geofence' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        spid: ['$stateParams', function ($stateParams) {
                            return $stateParams.spid;
                        }]
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/geofence/geofence.html',
                            controller: 'GeoFenceController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
