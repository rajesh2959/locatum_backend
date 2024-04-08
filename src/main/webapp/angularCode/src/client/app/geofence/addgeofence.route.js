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
                state: 'dashboard.addgeofence',
                config: {
                    url: 'addgeofence/{spid}/{geofenceid}',
                    title: 'Add Geo Fence',
                    ncyBreadcrumb: { label: 'geofence' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        spid: ['$stateParams', function ($stateParams) {
                            return $stateParams.spid;
                        }],
                        geofenceid: ['$stateParams', function ($stateParams) {
                            return $stateParams.geofenceid;
                        }],
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/geofence/addgeofence.html',
                            controller: 'AddGeoFenceController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
