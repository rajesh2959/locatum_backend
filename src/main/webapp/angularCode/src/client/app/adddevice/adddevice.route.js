(function () {
    'use strict';
    angular
        .module('app.adddevice')
        .run(appRun);

    appRun.$inject = ['routerHelper'];
    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.adddevice',
                config: {
                    url: 'adddevice/{spid}/{uid}/{isap}/{pageFrom}/{serverType}',
                    title: 'Add Device',
                    ncyBreadcrumb: { label: 'adddevice' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        spid: ['$stateParams', function ($stateParams) {
                            return $stateParams.spid;
                        }],
                        uid: ['$stateParams', function ($stateParams) {
                            return $stateParams.uid;
                        }],
                        isap: ['$stateParams', function ($stateParams) {
                            return $stateParams.isap;
                        }],
                        pageFrom: ['$stateParams', function ($stateParams) {
                            return $stateParams.pageFrom;
                        }],
                        serverType: ['$stateParams', function ($stateParams) {
                            return $stateParams.serverType;
                        }]
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/adddevice/adddevice.html',
                            controller: 'adddeviceController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();