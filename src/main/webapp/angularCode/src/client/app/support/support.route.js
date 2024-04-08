(function () {
    'use strict';
    angular
        .module('app.support')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.support',
                config: {
                    url: 'support',
                    title: 'Support',
                    ncyBreadcrumb: { label: 'support' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    //resolve: {
                    //    spid: ['$stateParams', function ($stateParams) {
                    //        return $stateParams.spid;
                    //    }]
                    //},
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/support/support.html',
                            controller: 'SupportController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
