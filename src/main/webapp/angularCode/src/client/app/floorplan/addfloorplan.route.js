(function () {
    'use strict';
    angular
        .module('app.floorplan')
        .run(appRun);
    appRun.$inject = ['routerHelper'];
    /* @ngInject */

    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.floorplan.addfloorplan',
                config: {
                    url: '/{spid}',
                    title: 'Add Floor Plan',
                    ncyBreadcrumb: { label: 'Add FloorPlan' },
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
                            templateUrl: 'app/floorplan/addfloorplan.html',
                            controller: 'addfloorplanController',
                            controllerAs: 'vm'
                        }
                    }
                }
            }
        ];
    }
})();