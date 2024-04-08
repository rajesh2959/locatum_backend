(function() {
    'use strict';

    angular
        .module('app.core')
        .run(appRun);

    /* @ngInject */
    function appRun(routerHelper) {
        var otherwiseState = 'dashboard.404';
        routerHelper.configureStates(getStates(), otherwiseState);
    }

    function getStates() {
        return [
            {
                state: 'dashboard.404',
                config: {
                    url: '404',
                    title: 'Page Not Found',
                    ncyBreadcrumb: { label: 'Page Not Found' },
                    settings: {
                        mustBeAuthenticated:false
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/core/404.html',
                            controller: '404Controller',
                            controllerAs: 'vm'
                        }
                    }
                }
            },
            {
                state: 'dashboard.insufficientpermissions',
                config: {
                    url: 'insufficientpermissions',
                    title: 'Insufficient Permissions',
                    ncyBreadcrumb: { label: 'insufficient permissions' },
                    settings: {
                        mustBeAuthenticated:false
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/core/InsufficientPermissions.html',
                            controller: 'InsufficientPermissionsController',
                            controllerAs: 'vm'
                        }
                    }
                }
            }
        ];
    }
})();
