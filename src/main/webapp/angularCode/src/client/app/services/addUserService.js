(function () {
    'use strict';

    angular
        .module('app')
        .factory('addUserService', service);

    service.$inject = ['dataService', 'session', 'notificationBarService'];

    function service(dataService, session, notificationBarService) {

        var svc = {};

        svc.saveAddUser = function (payload) {
            return dataService.postData('/rest/user/save', payload);
        };

        svc.saveadminuser = function (payload) {
            return dataService.postData('/rest/customer/save', payload);
        };

        svc.editUser = function (payload) {
            return dataService.postData('/rest/user/save', payload);
        };

        svc.getCustomer = function () {
            return dataService.getRecord('/rest/customer/customerList');
        };

        svc.deleteUser = function (user) {
            return dataService.postData('/rest/user/delete', user);
        };

        svc.deleteCustomer = function (payload) {
            return dataService.postData('/rest/customer/delete', payload);
        }

        svc.resetPassword = function (user) {
            return dataService.postData('/rest/user/chpwd', user);
        };

        svc.checkEmailDuplicate = function (email) {
            return dataService.getRecord('/rest/user/checkDuplicateUID?email='+email);
        };

        svc.gettimezonedetails = function () {
            return dataService.getRecord('/rest/customer/timeZone');
        };

        svc.getresttoken = function (postData) {
            return dataService.postData('/rest/token/restToken', postData);
           
        }

        svc.getmqtttoken = function (postData) {
            return dataService.postData('/rest/token/mqttToken', postData);
           
        }

        svc.imgupload = function (formData) {
            return dataService.postFilepart('/rest/customer/upload',formData);
        }
        
        return svc;
    }

})();