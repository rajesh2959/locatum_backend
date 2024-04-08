(function () {
    'use strict';
    angular
        .module('app.tag')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.tagdashboard',
                config: {
                    url: 'tagdashboard/{spid}/{macaddr}/{batterylevel}/{location}',
                    title: 'Tag Dashboard',
                    ncyBreadcrumb: { label: 'Tag Dashboard' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        spid: ['$stateParams', function ($stateParams) {
                            return $stateParams.spid;
                        }],
                        macaddr: ['$stateParams', function ($stateParams) {
                            return $stateParams.macaddr;
                        }],
                        batterylevel: ['$stateParams', function ($stateParams) {
                            return $stateParams.batterylevel;
                        }],
                        location: ['$stateParams', function ($stateParams) {
                            return $stateParams.location;
                        }]
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/tag/tagdashboard.html',
                            controller: 'tagdashboardController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();