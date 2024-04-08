(function () {
    'use strict';
    angular
        .module('app.inactivecustomers')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.inactivecustomers',
                config: {
                    url: 'inactivecustomers',
                    title: 'Inactive Customers',
                    ncyBreadcrumb: { label: 'inactivecustomers' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/inactivecustomers/inactivecustomers.html',
                            controller: 'InactivecustomersController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();