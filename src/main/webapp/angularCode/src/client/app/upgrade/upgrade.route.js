(function () {
    'use strict';
    angular
        .module('app.upgrade')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.upgrade',
                config: {
                    url: 'upgrade/{sid}/{spid}/{macaddr}',
                    templateUrl: 'app/upgrade/upgrade.html',
                    controller: 'upgradeController',
                    controllerAs: 'vm',
                    title: 'upgrade',
                    ncyBreadcrumb: { label: 'upgrade' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                     resolve: {
                         sid: ['$stateParams', function ($stateParams) {
                            return $stateParams.sid;
                        }],
                        spid: ['$stateParams', function ($stateParams) {
                            return $stateParams.spid;
                        }],
                            macaddr: ['$stateParams', function ($stateParams) {
                            return $stateParams.macaddr;
                        }]
                        
                    },
                }
            }
        ];
    }
})();


