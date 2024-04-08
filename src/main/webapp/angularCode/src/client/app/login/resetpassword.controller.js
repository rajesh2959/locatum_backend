(function () {
    'use strict';
    angular
        .module('app.login')
        .controller('ResetPasswordController', controller);
    controller.$inject = ['authService', 'navigation', 'messagingService', '$rootScope', '$location', 'session', 'id', 'token'];

    /* @ngInject */
    function controller(authService, navigation, messagingService, $rootScope, $location, session, id, token) {
    	var vm = this;// jshint ignore:line
        
    	vm.credentials = {
            newpassword :'',
            confirmpassword : ''
        };

        vm.resetpassword =function(credentials, resetPasswordForm){
            messagingService.broadcastCheckFormValidatity();
            if (!resetPasswordForm.$invalid) {
                 authService.resetPassword(credentials,id,token).then(function (res) {
                    if(res){
                       navigation.goToLogin();
                    }
                });
            }
        }
	    function activate() { }

	    activate();
    }
  })();
