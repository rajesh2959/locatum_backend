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
                state: 'dashboard.addgeofencealerts',
                config: {
                    url: 'addgeofencealerts/{geofencealertid}/{pagefrom}/{isAdd}/{spid}/{geofenceid}',
                    title: 'Geo Fence Alerts',
                    ncyBreadcrumb: { label: 'geofencealerts' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        geofencealertid: ['$stateParams', function ($stateParams) {
                            return $stateParams.geofencealertid;
                        }],
                        pagefrom: ['$stateParams', function ($stateParams) {
                            return $stateParams.pagefrom;
                        }],
                        isAdd: ['$stateParams', function ($stateParams) {
                            return $stateParams.isAdd;
                        }],
                        spid: ['$stateParams', function ($stateParams) {
                            return $stateParams.spid;
                        }],
                        geofenceid: ['$stateParams', function ($stateParams) {
                            return $stateParams.geofenceid;
                        }],
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/geofencealerts/addgeofencealerts.html',
                            controller: 'AddGeoFenceAlertsController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
