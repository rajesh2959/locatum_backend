(function () {
    'use strict';
    angular
        .module('app')
        .run(appRun);
    appRun.$inject = ['routerHelper'];

    /* @ngInject */
    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'dashboard.venue.addvenue',
                config: {
                    url: '/{venueId}',
                    title: 'Add Venue',
                    ncyBreadcrumb: { label: 'Add Venue' },
                    settings: {
                        mustBeAuthenticated: true
                    },
                    resolve: {
                        venueId: ['$stateParams', function ($stateParams) {
                            return $stateParams.venueId;
                        }]
                    },
                    views: {
                        '@dashboard': {
                            templateUrl: 'app/venue/addvenue.html',
                            controller: 'addvenueController',
                            controllerAs: 'vm',
                        }
                    }
                }
            }
        ];
    }
})();