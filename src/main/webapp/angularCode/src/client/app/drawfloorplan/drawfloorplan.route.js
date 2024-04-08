(function () {
    'use strict';
    angular
        .module('app.drawfloorplan')
        .run(appRun);

    appRun.$inject = ['routerHelper'];
    /* @ngInject */

    function appRun(routerHelper) {
        routerHelper.configureStates(getStates());
    }

    function getStates() {
        return [
            {
                state: 'drawfloorplan',
                config: {
                    url: '/drawfloorplan/{sid}/{spid}/{title}/{desc}',
                    templateUrl: 'app/drawfloorplan/drawfloorplan.html',
                    controller: 'drawfloorplanController',
                    controllerAs: 'vm',
                    title: 'Draw Floor Plan',
                    settings: {
                        mustBeAuthenticated: false
                    },
                    resolve: {
                        sid: ['$stateParams', function ($stateParams) {
                            return $stateParams.sid;
                        }],
                        spid: ['$stateParams', function ($stateParams) {
                            return $stateParams.spid;
                        }],
                        title: ['$stateParams', function ($stateParams) {
                            return $stateParams.title;
                        }],
                        desc: ['$stateParams', function ($stateParams) {
                            return $stateParams.desc;
                        }]
                    }
                }
            }
        ];
    }
})();