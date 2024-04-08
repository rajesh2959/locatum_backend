(function () {
    'use strict';
    angular
        .module('app.geoconfig')
        .run(appRun);
    appRun.$inject = ['routerHelper'];
    /* @ngInject */

    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.geoconfig',
                config: {
                    url: 'geoconfig/{spid}',
                    title: 'Geo Config',
                    ncyBreadcrumb: { label: 'geoconfig' },
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
                            templateUrl: 'app/geoconfig/geoconfig.html',
                            controller: 'GeoconfigController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
