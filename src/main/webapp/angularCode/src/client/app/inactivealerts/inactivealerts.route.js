(function () {
    'use strict';
    angular
        .module('app.inactivealerts')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.inactivealerts',
                config: {
                    url: 'inactivealerts',
                    title: 'Inactive Alerts',
                    ncyBreadcrumb: { label: 'inactivealerts' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/inactivealerts/inactivealerts.html',
                            controller: 'InactiveAlertsController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();