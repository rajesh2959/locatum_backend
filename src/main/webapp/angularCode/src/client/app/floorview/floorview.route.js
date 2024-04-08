(function () {
    'use strict';
    angular
        .module('app.floorview')
        .run(appRun);
    appRun.$inject = ['routerHelper'];
    /* @ngInject */

    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.floorview',
                config: {
                    url: 'floorview/{spid}',
                    title: 'Floor View',
                    ncyBreadcrumb: { label: 'Floorview' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        spid: ['$stateParams', function ($stateParams) {
                            return $stateParams.spid;
                        }]
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/floorview/floorview.html',
                            controller: 'FloorViewController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
