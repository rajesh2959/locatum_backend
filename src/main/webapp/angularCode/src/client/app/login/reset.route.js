(function () {
    'use strict';

    angular
        .module('app.login')
        .run(appRun);

    appRun.$inject = ['routerHelper'];
    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'reset',
                config: {
                    url: '/reset/{id}/{token}',
                    templateUrl: 'app/login/reset.html',
                    title: 'reset',
                    controller: 'ResetController',
                    controllerAs: 'vm',
                    resolve: {
                        id: ['$stateParams', function ($stateParams) {
                            return $stateParams.id;
                        }],
                        token: ['$stateParams', function ($stateParams) {
                            return $stateParams.token;
                        }],
                    },
                    
                }
            }
                

        ];
    }
})();
