(function () {
    'use strict';
    angular
        .module('app.upgrade')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.upgradehistory',
                config: {
                    url: 'upgradehistory',
                    templateUrl: 'app/upgrade/upgradehistory.html',
                    controller: 'upgradeHistoryController',
                    controllerAs: 'vm',
                    title: 'upgradehistory',
                    ncyBreadcrumb: { label: 'upgradehistory' },
                    settings: {
                        mustBeAuthenticated: true
                    },
               
                }
            }
        ];
    }
})();


