(function () {
    'use strict';
    angular
        .module('app.gateway')
        .run(appRun);
    appRun.$inject = ['routerHelper'];
    /* @ngInject */

    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.gatewayinfo',
                config: {
                    url: 'gatewayinfo/{uid}/{sid}/{spid}',
                    templateUrl: 'app/gateway/gatewayinfo.html',
                    controller: 'gatewayinfoController',
                    controllerAs: 'vm',
                    title: 'Gateway Info',
                    ncyBreadcrumb: { label: 'gatewayinfo' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {                        
                        uid: ['$stateParams', function ($stateParams) {
                            return $stateParams.uid;
                        }],
                        sid: ['$stateParams', function ($stateParams) {
                            return $stateParams.sid;
                        }],
                        spid: ['$stateParams', function ($stateParams) {
                            return $stateParams.spid;
                        }]
                    }
                }
            }
        ];
    }
})();