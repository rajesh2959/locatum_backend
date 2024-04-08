(function () {
    'use strict';

    angular
        .module('app.reports')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.reports',
                config: {
                    url: 'reports',
                    templateUrl: 'app/reports/reports.html',
                    controller: 'reportsController',
                    controllerAs: 'vm',
                    title: 'Reports',
                    ncyBreadcrumb: { label: 'reports' },
                    settings: {
                        mustBeAuthenticated: true
                    }
                }
            }
        ];
    }
})();