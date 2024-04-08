(function () {
    'use strict';
    angular
        .module('app.gateway')
        .run(appRun);
    appRun.$inject = ['routerHelper'];
    /* @ngInject */

    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.gateway',
                config: {
                    url: 'gateway',
                    templateUrl: 'app/gateway/gateway.html',
                    controller: 'gatewayController',
                    controllerAs: 'vm',
                    title: 'Gateway',
                    ncyBreadcrumb: { label: 'gateway' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        chartType: ['$stateParams', function ($stateParams) {
                            return $stateParams.chartType;
                        }],
                        visualid: ['$stateParams', function ($stateParams) {
                            return $stateParams.visualid;
                        }],
                    },
                }
            }
        ];
    }
})();