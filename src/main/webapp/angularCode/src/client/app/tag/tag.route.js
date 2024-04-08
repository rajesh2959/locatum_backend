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
                state: 'dashboard.tag',
                config: {
                    url: 'tag',
                    templateUrl: 'app/tag/tag.html',
                    controller: 'tagController',
                    controllerAs: 'vm',
                    title: 'Tags',
                    ncyBreadcrumb: { label: 'tag' },
                    settings: {
                        mustBeAuthenticated: true
                    }
                }
            }
        ];
    }
})();