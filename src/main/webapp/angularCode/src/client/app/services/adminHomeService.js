(function () {
    'use strict';

    angular
        .module('app')
        .factory('adminHomeService', service);

    service.$inject = ['$rootScope', 'session', '$http', 'environment', 'notificationBarService'];

    function service($rootScope, session, $http, environment) {

        var adminHomeService = {};
         var baseUrl = environment.serverBaseUrl;

        adminHomeService.updateLog = function(customeritem){
            var paramData = {};
            paramData.id        = customeritem.id;
            paramData.logState  = customeritem.logs;

            var start = moment();
            var oauthRoute = '/rest/customer/cloudlog';

            return $http
                .post(baseUrl + oauthRoute, paramData,
                    { headers: { 'Content-Type': 'application/json' } })
                .then(function (res) {
                    var end = moment();
                    if (angular.isDefined(console)) console.log(oauthRoute + ' took: ' + Math.round(end - start) + ' milliseconds, from: ' + start.format('h:mm:ss.SSS') + ' to: ' + end.format('h:mm:ss.SSS'));
                    if (res.status == 200) {
                        return res;
                    }
                    return null;
                    
               });
        };

        adminHomeService.updateVpn = function(customeritem){
            var paramData = {};
            paramData.id        = customeritem.id;
            paramData.vpnState  = customeritem.vpn;

            var start = moment();
            var oauthRoute = '/rest/customer/vpn';

            return $http
                .post(baseUrl + oauthRoute, paramData,
                    { headers: { 'Content-Type': 'application/json' } })
                .then(function (res) {
                    var end = moment();
                    if (angular.isDefined(console)) console.log(oauthRoute + ' took: ' + Math.round(end - start) + ' milliseconds, from: ' + start.format('h:mm:ss.SSS') + ' to: ' + end.format('h:mm:ss.SSS'));
                    if (res.status == 200) {
                        return res;
                    }
                    return null;
                    
               });
        };

        adminHomeService.updateSupport = function(cid,supportstatus){
            var paramData = {};
            paramData.id        = cid;
            paramData.flag  = supportstatus;

            var start = moment();
            var oauthRoute = '/rest/customer/support';

            return $http
                .post(baseUrl + oauthRoute, paramData,
                    { headers: { 'Content-Type': 'application/json' } })
                .then(function (res) {
                    var end = moment();
                    if (angular.isDefined(console)) console.log(oauthRoute + ' took: ' + Math.round(end - start) + ' milliseconds, from: ' + start.format('h:mm:ss.SSS') + ' to: ' + end.format('h:mm:ss.SSS'));
                    if (res.status == 200) {
                        return res;
                    } else if (res.status == 404) {
                        notificationBarService.error('Account not found.');
                    }
                    return null;
                    
               });
        };
        return adminHomeService;
    }

})();
