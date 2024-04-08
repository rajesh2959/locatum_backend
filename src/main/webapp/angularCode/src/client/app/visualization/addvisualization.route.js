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
                state: 'dashboard.addvisualization',
                config: {
                    url: 'addvisualization/{chartType}/{visualid}/{type}/{visualname}/{desp}',
                    title: 'Add Visualization',
                    ncyBreadcrumb: { label: 'visualization' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        chartType: ['$stateParams', function ($stateParams) {
                            return $stateParams.chartType;
                        }],
                        visualid: ['$stateParams', function ($stateParams) {
                            return $stateParams.visualid;
                        }],
                         type: ['$stateParams', function ($stateParams) {
                            return $stateParams.type;
                        }],
                         visualname: ['$stateParams', function ($stateParams) {
                            return $stateParams.visualname;
                        }],
                         desp: ['$stateParams', function ($stateParams) {
                            return $stateParams.desp;
                        }],
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/visualization/addvisualization.html',
                            controller: 'AddVisualizationController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
