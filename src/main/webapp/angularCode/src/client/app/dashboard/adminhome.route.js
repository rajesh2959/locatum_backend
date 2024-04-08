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
                state: 'dashboard.adminhome',
                config: {
                    url: 'adminhome',
                    title: 'Admin Home',
                    ncyBreadcrumb: { label: 'adminhome' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/dashboard/adminhome.html',
                            controller: 'AdminHomeController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
