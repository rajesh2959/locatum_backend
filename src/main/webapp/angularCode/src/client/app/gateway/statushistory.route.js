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
                state: 'dashboard.statushistory',
                config: {
                    url: 'gatewayhistory/{uid}',
                    templateUrl: 'app/gateway/statushistory.html',
                    controller: 'statushistoryController',
                    controllerAs: 'vm',
                    title: 'Gatewaystatushistory',
                    ncyBreadcrumb: { label: 'Gatewaystatushistory' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                     resolve: {                        
                        uid: ['$stateParams', function ($stateParams) {
                            return $stateParams.uid;
                        }]
                    }
                }
            }
        ];
    }
})();