(function () {
    'use strict';
    angular
        .module('app.dashboard')
        .run(appRun);
    appRun.$inject = ['routerHelper'];
    /* @ngInject */

    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.home',
                config: {
                    url: 'dashboard',
                    templateUrl: 'app/dashboard/home.html',
                    controller: 'DashboardHomeController',
                    controllerAs: 'vm',
                    title: 'Dashboard',
                    ncyBreadcrumb: { label: 'Dashboard' },
                    settings: {
                        mustBeAuthenticated: true
                    }
                }
            }
        ];
    }
})();
