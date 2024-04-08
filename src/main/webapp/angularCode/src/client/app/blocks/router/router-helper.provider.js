/* Help configure the state-base ui.router */
(function () {
    'use strict';

    angular
        .module('blocks.router')
        .provider('routerHelper', routerHelperProvider);

    routerHelperProvider.$inject = ['$locationProvider', '$stateProvider', '$urlRouterProvider'];
    /* @ngInject */
    function routerHelperProvider($locationProvider, $stateProvider, $urlRouterProvider) {
        /* jshint validthis:true */
        var config = {
            docTitle: undefined,
            resolveAlways: {}
        };

        //$locationProvider.html5Mode(true);

        //if (!(window.history && window.history.pushState)) {
        //    window.location.hash = '/';
        //}
        $locationProvider.html5Mode(false);

        this.configure = function (cfg) {
            angular.extend(config, cfg);
        };

        this.$get = RouterHelper;
        RouterHelper.$inject = ['$location', '$rootScope', '$state', 'logger', 'authService', 'navigation', 'insufficientPermissionService', '$window', 'environment', 'messagingService', 'session'];
        /* @ngInject */
        function RouterHelper($location, $rootScope, $state, logger, authService, navigation, insufficientPermissionService, $window, env, messagingService, session) {
            var handlingStateChangeError = false;
            var hasOtherwise = false;
            var stateCounts = {
                errors: 0,
                changes: 0
            };

            var service = {
                configureStates: configureStates,
                getStates: getStates,
                stateCounts: stateCounts
            };

            init();

            return service;

            ///////////////

            function configureStates(states, otherwiseState) {
                states.forEach(function (state) {
                    state.config.resolve =
                        angular.extend(state.config.resolve || {}, config.resolveAlways);
                    $stateProvider.state(state.state, state.config);
                });
                if (otherwiseState && !hasOtherwise) {
                    hasOtherwise = true;
                    $urlRouterProvider.otherwise(function ($injector, $location) {
                      
                        var state = $injector.get('$state');
                        $rootScope.urlPath = $location.path();
                        var checkTime = authService.checkExpirationTime();
                        if(!checkTime && state.current.name == "") {
                             $state.go('dashboard');
                        }
                        else if (authService.isAuthenticated())
                            state.go(otherwiseState);
                        else
                            state.go('login');
                        return $location.path();
                    });
                    //$urlRouterProvider.otherwise(otherwisePath);
                }
            }

            function handleRoutingStart() {
                $rootScope.$on('$stateChangeStart', function (event, next) {
                    if (next.settings && next.settings.mustBeAuthenticated && !authService.isAuthenticated()) {
                        //user not authenticated, redirect to login page
                        event.preventDefault();
                        navigation.goToLogin();
                        return;
                    }

                    if (next.settings && next.settings.requiredPermission && !authService.hasRequiredPermission(next.settings.requiredPermission)) {
                        //user does not have reuiqred permission to navigate to this route (view this screen)
                        event.preventDefault();
                        insufficientPermissionService.tellUserTheyDontHavePermissions(next.title);
                        return;
                    }

                    //handle redirection to default child states
                    if (next.redirectTo) {
                        event.preventDefault();
                        $state.go(next.redirectTo);  
                    }
                });
            }

            function handleRoutingErrors() {
                // Route cancellation:
                // On routing error, go to the dashboard.
                // Provide an exit clause if it tries to do it twice.
                $rootScope.$on('$stateChangeError',
                    function (event, toState, toParams, fromState, fromParams, error) {
                        if (handlingStateChangeError) {
                            return;
                        }
                        stateCounts.errors++;
                        handlingStateChangeError = true;
                        var destination = (toState &&
                            (toState.title || toState.name || toState.loadedTemplateUrl)) ||
                            'unknown target';
                        var msg = 'Error routing to ' + destination + '. ' +
                            (error.data || '') + '. <br/>' + (error.statusText || '') +
                            ': ' + (error.status || '');
                        logger.warning(msg, [toState]);
                        $location.path('/404');
                    }
                );
            }

            function updateDocTitle() {
                $rootScope.$on('$stateChangeSuccess',
                    function (event, toState, toParams, fromState, fromParams) {
                             $window.scrollTo(0, 0);
                             stateCounts.changes++;
                             handlingStateChangeError = false;
                             var title = config.docTitle + ' ' + (toState.title || '');
                             $rootScope.title = title; // data bind to <title>
                             $rootScope.state = toState;
                             messagingService.broadcastOnSuccessfullRouteChanged(toState);
                     
                    }
                );
            }

            function init() {
                handleRoutingStart();
                handleRoutingErrors();
                updateDocTitle();
            }

            function getStates() { return $state.get(); }
        }
    }
})();
