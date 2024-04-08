(function () {
    'use strict';
    angular
        .module('app.login')
        .controller('LoginController', controller);
    controller.$inject = ['authService', 'navigation', 'messagingService', '$rootScope', '$location', 'session'];

    /* @ngInject */
    function controller(authService, navigation, messagingService, $rootScope, $location, session) {
        var vm = this;// jshint ignore:line
        vm.credentials = {
            username: '',
            password: '',
            emailUserName: '',
            newpassword :'',
            confirmpassword : ''
        };
        
        $rootScope.venueId = "";
        $rootScope.spid = "";
        vm.clkforgetpassword = false;

        vm.clkforgetpasswordevnt = function () {
            vm.clkforgetpassword = true;
        };

        vm.undoclkforgetpasswordevnt = function () {
            vm.clkforgetpassword = false;
        };

        vm.mailverifyevnt = function () {
            var mailIdRegex = "/^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/";
            if (mailIdRegex.test(vm.credentials.emailUserName))
            {
                vm.mailverify = true;
            }
                vm.mailverify = false;
        };

        vm.pswdchngevnt = function () {
            vm.pswdchng = true;
        };

        vm.rstpswdchngdevnt = function () {
            vm.rstpswdchngd = true;
        };

        vm.navigateloginevnt = function () {
            location.reload();
        };

        if ($location.$$search.customer) {
            vm.credentials.customerurl = $location.$$search.customer;
        }

        $rootScope.customerName = "Login";

        vm.login = function (credentials, form) {
            messagingService.broadcastCheckFormValidatity();
            if (!form.$invalid) {
                authService.login(credentials).then(function (res) {
                    if (res) {
                        messagingService.broadcastLoginSuccess();
                        if (session.role == "superadmin")
                            navigation.gotToAdminHome();
                        else
                            navigation.gotToDashboard();
                    }
                });
            }
        };
        
        vm.reLogin = function(credentials,form){
        	messagingService.broadcastCheckFormValidatity();
        	if (!form.$invalid) {
                 authService.verifyUserMailId(credentials).then(function (res) {
                    if(res){
                        messagingService.broadcastLoginSuccess();
                        navigation.goToLogin();
                    }
                });
        	}
        };

        function activate() { }

        activate();
    }
})();
