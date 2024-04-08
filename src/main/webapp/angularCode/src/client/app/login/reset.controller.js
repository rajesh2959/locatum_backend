(function () {
    'use strict';
    angular
        .module('app.login')
        .controller('ResetController', controller);
    controller.$inject = ['authService', 'navigation', 'messagingService', '$rootScope', '$location', 'session', 'id', 'token'];

    /* @ngInject */
    function controller(authService, navigation, messagingService, $rootScope, $location, session, id, token) {
    	var vm = this;// jshint ignore:line
        vm.isnotValid = true;
    	vm.validateUserId = function() {
            authService.validateUserIdService(id,token).then(function (res) {
                    if(res){
                        vm.isnotValid = false;
                        navigation.goToResetPassword(id,token);
                    }
                });

        };

	    function activate() { }

	    activate();
    }
  })();
