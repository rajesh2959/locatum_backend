(function () {
    'use strict';
    angular
        .module('app.license')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.license',
                config: {
                    url: 'license',
                    title: 'License',
                    ncyBreadcrumb: { label: 'license' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/license/license.html',
                            controller: 'licenseController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
