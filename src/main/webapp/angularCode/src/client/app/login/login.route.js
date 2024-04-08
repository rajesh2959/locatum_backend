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
                state: 'loginwithcustomerurl',
                config: {
                    url: '/login?customer={customerurl}',
                    templateUrl: 'app/login/login.html',
                    controller: 'LoginController',
                    controllerAs: 'vm',
                    title: 'Login',
                    settings: {
                        mustBeAuthenticated: false
                    },
                    resolve: {
                        customerurl: ['$stateParams', function ($stateParams) {
                            return $stateParams.customerurl;
                        }]
                    }
                }
            },
            {
                state: 'login',
                config: {
                    url: '/login',
                    templateUrl: 'app/login/login.html',
                    controller: 'LoginController',
                    controllerAs: 'vm',
                    title: 'login',
                    settings: {
                        mustBeAuthenticated: false
                    },
                    resolve: {
                        customerurl: ['$stateParams', function ($stateParams) {
                            return $stateParams.customerurl;
                        }]
                    }
                }
            },
                

        ];
    }
})();
