(function() {
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
                state: 'dashboard.floorplan',
                config: {
                    url: 'floorplan',
                    templateUrl: 'app/floorplan/floorplan.html',
                    controller: 'FloorPlanController',
                    controllerAs: 'vm',
                    title: 'Floor Plan',
                    ncyBreadcrumb: { label: 'Floor Plan' },
                    settings: {
                        mustBeAuthenticated: true
                    }
                }
            }
        ];
    }
})();
