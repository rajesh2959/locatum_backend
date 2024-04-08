(function () {
    'use strict';

    angular
        .module('app')
        .factory('httpInterceptorService', auth);

    auth.$inject = ['messagingService', '$q', '$injector', 'session', 'environment'];

    function auth(messagingService, $q, $injector, session, environment) {

        function broadcastFriendlyErrorMessage(rejection) {
            /*jshint maxcomplexity:false */
            console.log('global generic error handler: rejection:', rejection);
            var msg = '';

            //the case where the client cannot connect to the server
            if (!rejection.data && rejection.status === 0 && rejection.statusText === '') {
                messagingService.broadcastGlobalErrorEvent({ errorMessage: 'Unable to connect to the server, please try again in a couple of seconds.' });
            }
            else if (rejection.status === 400) {
                //the case where we push a custom error description down the wire
                if (rejection.data && rejection.data.error_description) {// jshint ignore:line
                    msg = rejection.data.error_description;// jshint ignore:line
                }
                //the case where asp.net modelstate errors come down the wire
                else if (rejection.data && rejection.data.message === 'The request is invalid.' &&
                    rejection.data.modelState) {
                    var errors = [];
                    for (var key in rejection.data.modelState) {
                        if (rejection.data.modelState.hasOwnProperty(key)) {
                            for (var i = 0; i < rejection.data.modelState[key].length; i++) {
                                errors.push(rejection.data.modelState[key][i]);
                            }
                        }
                    }
                    msg = errors.join('<br/>');
                }
                else if (rejection.data && rejection.data.message) {
                    msg = rejection.data.message;
                }
                else if (rejection.data) {
                    msg = rejection.data;
                }
                messagingService.broadcastGlobalWarningEvent({ message: msg });
            }
            else if (rejection.status === 404) {
                if (rejection.data && rejection.data.message) {
                    messagingService.broadcastGlobalErrorEvent({ errorMessage: '(404): ' + rejection.data.message });
                }
            }
            else if (rejection.status === 500) {
                if (rejection.data) {
                    var ex = rejection.data;
                    while (ex.innerException) {
                        ex = ex.innerException;
                    }
                  
                }
            }
        }

        //attache jwt bearer token to each and every http request via authorization header
        //also attach api_auth_key to each and every http request
        var _request = function (config) {
            config = config || {};
            config.headers = config.headers || {};

            messagingService.broadcastGlobalClearErrorEvent();

            return config;
        };

        //handle http request errors
        var _responseError = function (rejection) {
            var deferred = $q.defer();
            if (rejection.status === 401) {
                //unauthorized, redirect to login
                var authService = $injector.get('authService');
                authService.logOut();
                var navigation = $injector.get('navigation');
                navigation.goToLogin();
                deferred.reject(rejection);
            }
            else {
                //generic error handling logic goes here
                broadcastFriendlyErrorMessage(rejection);
                deferred.reject(rejection);
            }
            return deferred.promise;
        };

        var authInterceptorServiceFactory = {};

        authInterceptorServiceFactory.request = _request;
        authInterceptorServiceFactory.responseError = _responseError;

        return authInterceptorServiceFactory;
    }
})();
