(function () {
    'use strict';
    angular
        .module('app.networkconfig')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.networkconfig',
                config: {
                    url: 'networkconfig/{spid}',
                    title: 'Network Config',
                    ncyBreadcrumb: { label: 'networkconfig' },
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
                            templateUrl: 'app/networkconfig/networkconfig.html',
                            controller: 'NetworkconfigController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
