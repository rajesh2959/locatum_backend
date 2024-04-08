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
                state: 'resetpassword',
                config: {
                    url: '/resetpassword/{id}/{token}',
                    templateUrl: 'app/login/resetpassword.html',
                    title: 'resetpassword',
                    controller: 'ResetPasswordController',
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
