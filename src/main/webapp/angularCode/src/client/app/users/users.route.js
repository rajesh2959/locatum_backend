(function () {
    'use strict';
    angular
        .module('app.users')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.users',
                config: {
                    url: 'users',
                    templateUrl: 'app/users/users.html',
                    controller: 'usersController',
                    controllerAs: 'vm',
                    title: 'Users',
                    ncyBreadcrumb: { label: 'users' },
                    settings: {
                        mustBeAuthenticated: true
                    }
                }
            }
        ];
    }
})();