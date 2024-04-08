(function () {
    'use strict';
    angular
        .module('app.customizedalert')
        .run(appRun);
    appRun.$inject = ['routerHelper'];
    /* @ngInject */

    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.customizedalert',
                config: {
                    url: 'customizedalert',
                    title: 'Customized Alert',
                    ncyBreadcrumb: { label: 'customizedalert' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/customizedalert/customizedalert.html',
                            controller: 'CustomizedAlertController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();
