(function() {
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
                state: 'dashboard',
                config: {
                    url: '/',
                    templateUrl: 'app/dashboard/dashboard.html',
                    controller: 'MainMenuController',
                    controllerAs: 'vm',
                    redirectTo: 'dashboard.adminhome',
                    title: 'Locatum Dashboard',
                    ncyBreadcrumb: { label: 'Home' },
                    settings: {
                        mustBeAuthenticated: true
                    }
                }
            }
        ];
    }
})();
