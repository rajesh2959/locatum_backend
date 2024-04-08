(function () {
    'use strict';

    angular
        .module('app.layout')
        .controller('bodyController', BodyController);

    BodyController.$inject = ['navigation', 'notificationBarService'];
    /* @ngInject */
    /**
     * NotificationBarService:
  
     * DO NOT remove this dependency EVEN though it is not used directly in this controller, 
     * long story but it enables notifications appearing for child controllers that do not depend directly on NotificationBarService
     
     */
    function BodyController(navigation, notificationBarService) {
        var vm = this;
        vm.isOnLoginScreen = navigation.isOnLoginScreen;
    }
})();
