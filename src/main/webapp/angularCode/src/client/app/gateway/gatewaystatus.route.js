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
                state: 'dashboard.gatewaystatus',
                config: {
                    url: 'gatewaystatus',
                    templateUrl: 'app/gateway/gatewaystatus.html',
                    controller: 'gatewaystatusController',
                    controllerAs: 'vm',
                    title: 'Gateway Status',
                    ncyBreadcrumb: { label: 'gatewaystatus' },
                    settings: {
                        mustBeAuthenticated: true
                    }
                }
            }
        ];
    }
})();