(function () {
    'use strict';
    angular
        .module('app.reports')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.addreport',
                config: {
                    url: 'addreport/{cid}/{uid}/{name}',
                    title: 'Add Reports',
                    ncyBreadcrumb: { label: 'report' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        cid: ['$stateParams', function ($stateParams) {
                            return $stateParams.cid;
                        }],
                        uid: ['$stateParams', function ($stateParams) {
                            return $stateParams.uid;
                        }],
                        name: ['$stateParams', function ($stateParams) {
                            return $stateParams.name;
                        }],
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/reports/addreport.html',
                            controller: 'AddReportController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
