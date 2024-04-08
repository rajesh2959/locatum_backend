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
                state: 'dashboard.tagedit',
                config: {
                    url: 'tagedit/{spid}/{macaddr}',
                    title: 'Tag Edit',
                    ncyBreadcrumb: { label: 'Tag Edit' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        spid: ['$stateParams', function ($stateParams) {
                            return $stateParams.spid;
                        }],
                        macaddr: ['$stateParams', function ($stateParams) {
                            return $stateParams.macaddr;
                        }]
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/tag/tagedit.html',
                            controller: 'tagEditController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();