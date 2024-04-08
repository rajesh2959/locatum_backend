(function () {
    'use strict';
    angular
        .module('app.visualization')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.visualization',
                config: {
                    url: 'visualization',
                    title: 'Visualization',
                    ncyBreadcrumb: { label: 'visualization' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    //resolve: {
                    //    spid: ['$stateParams', function ($stateParams) {
                    //        return $stateParams.spid;
                    //    }]
                    //},
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/visualization/visualization.html',
                            controller: 'VisualizationController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
