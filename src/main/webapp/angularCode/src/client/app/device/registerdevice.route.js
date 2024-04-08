(function () {
    'use strict';
    angular
        .module('app.gateway')
        .run(appRun);
    appRun.$inject = ['routerHelper'];
    /* @ngInject */

    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.registerdevice',
                config: {
                    url: 'registerdevice',
                    templateUrl: 'app/device/registerdevice.html',
                    controller: 'registerdeviceController',
                    controllerAs: 'vm',
                    title: 'Register Device',
                    ncyBreadcrumb: { label: 'Register Device' },
                    settings: {
                        mustBeAuthenticated: true
                    }
                }
            }
        ];
    }
})();