(function () {
    'use strict';
    angular
        .module('app.role')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.role',
                config: {
                    url: 'role',
                    title: 'Role',
                    ncyBreadcrumb: { label: 'role' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/role/role.html',
                            controller: 'RoleController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
