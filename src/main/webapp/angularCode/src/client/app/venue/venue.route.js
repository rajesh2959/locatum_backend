(function () {
    'use strict';

    angular
        .module('app.venue')
        .run(appRun);

    appRun.$inject = ['routerHelper'];
    / @ngInject /
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.venue',
                config: {
                    url: 'venue',
                    templateUrl: 'app/venue/venue.html',
                    controller: 'venueController',
                    controllerAs: 'vm',
                    title: 'Venue',
                    ncyBreadcrumb: { label: 'Venue' },
                    settings: {
                        mustBeAuthenticated: true
                    }
                }
            }
        ];
    }
})();