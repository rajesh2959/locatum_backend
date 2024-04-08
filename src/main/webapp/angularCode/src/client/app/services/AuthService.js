
(function () {
    'use strict';

    angular
        .module('app')
        .factory('authService', service);

    service.$inject = ['$rootScope', '$http', 'session', 'navigation', 'environment', 'dataService', 'notificationBarService', '$q', '$location', 'venuesession', 'localStorageService'];

    function service($rootScope, $http, session, navigation, env, dataService, notificationBarService, $q, $location, venuesession, localStorageService) {

        var authService = {};
        var baseUrl = env.serverBaseUrl;
        authService.login = function (credentials) {
            var payload = 'userId=' + credentials.username + '&password=' + credentials.password;
            var start = moment();// jshint ignore:line
            var oauthRoute = '/rest/qubercloud/login?' + payload;
            return $http
                .get(baseUrl + oauthRoute,
                    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then(function (res) {
                    var end = moment();// jshint ignore:line
                    if (angular.isDefined(console)) console.log(oauthRoute + ' took: ' + Math.round(end - start) + ' milliseconds, from: ' + start.format('h:mm:ss.SSS') + ' to: ' + end.format('h:mm:ss.SSS'));
                    if (res.data) {
                        if (res.data.code === 401) {
                            notificationBarService.error('Invalid username or password, please try again.');
                            return null;
                        }
                        else if (res.data.code === 200) {
                            if (res.data.body.role === "appadmin") {
                                var currentDate = new Date().getTime();
                                var setDate = currentDate + res.data.body.ttls;
                                localStorageService.set('exipirationTime', setDate);
                                session.create(res.data.body.cid, res.data.body.custname, res.data.body.role, res.data.body.solution);
                                $rootScope.customerName = res.data.body.custname;
                                session.customer = res.data.body.custname;
                            }
                            else if (res.data.body.role === "superadmin") {
                                var currentDate = new Date().getTime();
                                var setDate = currentDate + res.data.body.ttls;
                                localStorageService.set('exipirationTime', setDate);
                                session.create(res.data.body.userId, res.data.body.role, res.data.body.role, 'geofinder');
                                $rootScope.customerName = res.data.body.role;
                                session.customer = res.data.body.role;
                            }

                            console.log('res>>>>>>>>>>'+JSON.stringify(res));
                            return res;
                        }
                    }
                    return null;
                });
        };

        /*
            Service to validate the user's mail Id and send the resetPassword link to the validated mail Id
        */
        authService.verifyUserMailId = function (credentials){
            var payload = 'emailId=' + credentials.emailUserName;
            var start = moment();// jshint ignore:line
            var oauthRoute = '/rest/qubercloud/forgetpassword?' + payload;
            return $http
                .get(baseUrl + oauthRoute,
                    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then(function (res) {
                    var end = moment();// jshint ignore:line
                    if (angular.isDefined(console)) console.log(oauthRoute + ' took: ' + Math.round(end - start) + ' milliseconds, from: ' + start.format('h:mm:ss.SSS') + ' to: ' + end.format('h:mm:ss.SSS'));
                    if (res.data) {
                        console.log('Forget password res>>>>>>>>>>'+res);
                        if (res.data.code === 400) {
                            notificationBarService.error('User not alive.');
                            return null;
                        }
                        else if (res.data.code === 404) {
                            notificationBarService.error('User not found.');
                            return null;
                        }
                        else if (res.data.code === 200) {
                            return res;
                        }
                        else{
                            notificationBarService.error('While password reset requst error');
                            return null;
                        }
                    }
                    return null;
                    
               });
        };

        /*
            Service to validate the user's Id, token and navigate to resetPassword page
        */

        authService.validateUserIdService = function(id,token){
            var payload = 'id=' + id + '&token=' + token;
            var start = moment();// jshint ignore:line
            var oauthRoute = '/rest/qubercloud/resetpassword?' + payload;

            return $http
                .get(baseUrl + oauthRoute,
                    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then(function (res) {
                    var end = moment();// jshint ignore:line
                    if (angular.isDefined(console)) console.log(oauthRoute + ' took: ' + Math.round(end - start) + ' milliseconds, from: ' + start.format('h:mm:ss.SSS') + ' to: ' + end.format('h:mm:ss.SSS'));
                    if (res.data) {
                        if (res.data.code === 400) {
                            notificationBarService.error(res.data.body);
                            return null;
                        }
                        else if (res.data.code === 404) {
                            notificationBarService.error('User not found.');
                            return null;
                        }
                        else if (res.data.code === 200) {
                            return res;
                        }
                        else{
                            notificationBarService.error('While reset password occurring error');
                            return null;
                        }
                    }
                    return null;
                    
               });
        };

        /*
            Service to validate the user's Id and token and save the new password 
        */
        authService.resetPassword = function(credentials,id,token){
            var payload = 'id=' + id + '&token=' + token +'&password='+ credentials.newpassword + '&cpassword='+ credentials.confirmpassword ;
            var start = moment();// jshint ignore:line
            var oauthRoute = '/rest/qubercloud/changepassword?' + payload;

            return $http
                .get(baseUrl + oauthRoute,
                    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then(function (res) {
                    var end = moment();// jshint ignore:line
                    if (angular.isDefined(console)) console.log(oauthRoute + ' took: ' + Math.round(end - start) + ' milliseconds, from: ' + start.format('h:mm:ss.SSS') + ' to: ' + end.format('h:mm:ss.SSS'));
                    if (res.data) {
                        if (res.data.code === 400) {
                            notificationBarService.error(res.data.body);
                            return null;
                        }
                        else if (res.data.code === 404) {
                            notificationBarService.error('User not found.');
                            return null;
                        }
                        else if (res.data.code === 200) {
                            return res;
                        }
                        else{
                            notificationBarService.error('While reset password occurring error');
                            return null;
                        }
                    }
                    return null;
                    
               });  
        };

        authService.logOut = function () {
            var defer = $q.defer()
            session.destroy();
            venuesession.destroy();
            dataService.clearCache();
            navigation.goToLogin();
            defer.resolve();
            return defer.promise;
            //return $http.get(baseUrl + '/goodbye').then(function (result) {
            //    session.destroy();
            //    venuesession.destroy();
            //    navigation.goToLogin();
            //    dataService.clearCache();
            //});
        };

        authService.isAuthenticated = function () {
            return !!session.cid && !!session.accessToken;
        };

        authService.hasRequiredPermission = function (permission) {
            //TODO: implement permissions in portal
            return (session.userPermissions & permission) === permission;
        };
        authService.checkExpirationTime = function() {
            var ttls = localStorageService.get('exipirationTime')
            if(ttls) {
                var currentDate = new Date().getTime();
                var endDate = ttls;
                return currentDate >= endDate;
            }
            return false;

        }
        return authService;
    }
})();
