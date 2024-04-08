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
                state: 'dashboard.profile',
                config: {
                    url: 'profile/{id}',
                    title: 'Profile',
                    ncyBreadcrumb: { label: 'Profile' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        id: ['$stateParams', function ($stateParams) {
                            return $stateParams.id;
                        }]
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/users/profile.html',
                            controller: 'profileController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();