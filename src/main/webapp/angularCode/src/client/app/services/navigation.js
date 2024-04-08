(function () {
    'use strict';

    angular
        .module('app')
        .factory('navigation', service);

    service.$inject = ['$state', '$location'];

    function service($state, $location) {

        var nav = {};

        nav.gotToDashboard = function () {
            $state.go('dashboard');
        };

        nav.goToLogin = function () {
            $state.go('login');
        };

        nav.goToResetPassword = function (id,token) {
            $state.go('resetpassword', { id: id, token: token });
        };

        nav.goTologinWithCustomerUrl = function (customerurl) {
            $state.go('loginwithcustomerurl', { customer: customerurl });
        };

        nav.goToInsufficientPermissions = function () {
            $state.go('dashboard.insufficientpermissions');
        };

        nav.goToParentState = function () {
            $state.go('^');
        };

        nav.isOnLoginScreen = function () {
            return $state.is('login');
        };
        nav.isOnAdmininScreen = function () {
            return $state.is('dashboard.adminhome');
        };

        nav.isOnvenueScreen = function () {
            return $state.is('dashboard.venue.addvenue') || $state.is('dashboard.venue');
        };

        nav.isOndashboardScreen = function () {
            return $state.is('dashboard.home');
        };

        nav.isOnfloorplanScreen = function () {

            return $state.is('dashboard.floorplan') || $state.is('dashboard.floorplan.addfloorplan') || $state.is('dashboard.networkconfig') || $state.is('dashboard.geoconfig') || $state.is('dashboard.geofence') || $state.is('dashboard.adddevice') || $state.is('dashboard.addgeofence') || $state.is('dashboard.floorview');
        };

        nav.isbaseOnfloorplanScreen = function () {
            return $state.is('dashboard.floorplan') || $state.is('dashboard.floorplan.addfloorplan');
        };

        nav.isOnfloorviewScreen = function () {
            return $state.is('dashboard.floorview');
        };

        nav.isOngatewayScreen = function () {
            return $state.is('dashboard.gateway') || $state.is('dashboard.registerdevice') || $state.is('dashboard.gatewaystatus') || $state.is('dashboard.gatewayinfo') || $state.is('dashboard.statushistory');
        };

        nav.isOntagScreen = function () {
            return $state.is('dashboard.tag') || $state.is('dashboard.tagedit') || $state.is('dashboard.tagdashboard');
        };

        nav.isOnvisualizationScreen = function () {
            return $state.is('dashboard.visualization') || $state.is('dashboard.addvisualization');
        };

        nav.isOnreportScreen = function () {
            return $state.is('dashboard.reports') || $state.is('dashboard.addreport');
        };

        nav.isOninactivityalertsScreen = function () {
            return $state.is('dashboard.inactivealerts') || $state.is('dashboard.customizedalert');
        };

        nav.isOngeofencealertsScreen = function () {
            return $state.is('dashboard.geofencealerts') || $state.is('dashboard.addgeofencealerts');
        };

        nav.isuserScreen = function () {
            return $state.is('dashboard.users');
        };

        nav.issupportScreen = function () {
            return $state.is('dashboard.support');
        };
        nav.isroleScreen = function () {
            return $state.is('dashboard.role');
        };
        nav.isupgradeScreen = function () {
            return $state.is('dashboard.upgrade');
        };
        nav.isupgradehistoryScreen = function () {
            return $state.is('dashboard.upgradehistory');
        };

        nav.ismyaccountScreen = function () {
            return $state.is('dashboard.profile');
        };
        nav.islicenseCustomers = function () {
        	return $state.is('dashboard.license');
        };
        nav.isinactiveCustomers = function () {
        	return $state.is('dashboard.inactivecustomers');
        };




        nav.goToVenue = function () { $state.go('dashboard.venue'); };

        nav.gotAddVenue = function (venueId) {
            $state.go('dashboard.venue.addvenue', { venueId: venueId });
        };

        nav.goToFloorView = function (spid) {
            $state.go('dashboard.floorview', { spid: spid });
        };

        nav.goToDrawFloor = function (sid, spid, title, desc) {
            $state.go('drawfloorplan', { sid: sid, spid: spid, title: title, desc: desc });
        };

        nav.gotToHome = function (venueId) {
            $state.go('dashboard.home', { sid: venueId });
        };

        nav.gotToAdminHome = function () {
            $state.go('dashboard.adminhome');
        };


        nav.gotoFloorPlan = function () {
            $state.go('dashboard.floorplan');
        };

        nav.gotAddFloorPlan = function (spid) {
            $state.go('dashboard.floorplan.addfloorplan', { spid: spid });
        };

        nav.goToNetworkConfig = function (spid) {
            $state.go('dashboard.networkconfig', { spid: spid });
        };

        nav.goToGeoConfig = function (spid) {
            $state.go('dashboard.geoconfig', { spid: spid });
        };

        nav.goToGeoFence = function (spid) {
            $state.go('dashboard.geofence', { spid: spid });
        };

        nav.goToAddGeofence = function (spid, geofenceid) {
            $state.go('dashboard.addgeofence', { spid: spid, geofenceid: geofenceid });
        };
        
        nav.goToVisualization = function () {
            $state.go('dashboard.visualization');
        };

        nav.goToAddVisualization = function (chartType, visualid,type,visualname,desp) {
            $state.go('dashboard.addvisualization', { chartType: chartType, visualid: visualid , type: type ,visualname : visualname,desp : desp});
        };

        nav.goToAdddevice = function (spid, uid, isap, pageFrom, serverType) {
            $state.go('dashboard.adddevice', { spid: spid, uid: uid, isap: isap, pageFrom: pageFrom, serverType: serverType });
        };

        nav.goToGatewayInfo= function (uid, sid, spid) {
            $state.go('dashboard.gatewayinfo', { uid: uid, sid: sid, spid: spid });
        };

        nav.goToGeoFenceAlerts = function (spid) {
            $state.go('dashboard.geofencealerts');
        };

        nav.goToAddGeoFenceAlerts = function (geofencealertid, pagefrom) {
            $state.go('dashboard.addgeofencealerts', { geofencealertid: geofencealertid, pagefrom: pagefrom });
        };

        nav.goToTagDashboard = function (spid, macaddr, batterylevel, location) {
            $state.go('dashboard.tagdashboard', { spid: spid, macaddr: macaddr, batterylevel: batterylevel, location: location });
        };

        nav.goToRegisterDevice = function () {
            $state.go('dashboard.registerdevice');
        };

        nav.goToUpgrade = function (sid,spid,macaddr) {
            $state.go('dashboard.upgrade',{sid:sid, spid:spid, macaddr:macaddr});
        };

         nav.goToUpgradehistory = function () {
            $state.go('dashboard.upgradehistory');
        };
        
        nav.goToGateWay = function () {
            $state.go('dashboard.gateway');
        };

        nav.goToGatewayStatus = function () {
            $state.go('dashboard.gatewaystatus');
        };

         nav.goToGatewayStatusHistory = function (uid) {
            $state.go('dashboard.statushistory',{uid:uid});
        };

        nav.goToAddReport = function (cid, uid, name) {
            $state.go('dashboard.addreport', { cid: cid, uid: uid, name: name });
        };
        
        nav.goToTagEdit = function (spid, macaddr) {
            $state.go('dashboard.tagedit', { spid: spid, macaddr: macaddr });
        };

        nav.goToTag = function () {
            $state.go('dashboard.tag');
        };

        nav.goToUsers = function () {
            $state.go('dashboard.users');
        };

        nav.goToProfile = function (id) {
            $state.go('dashboard.profile', { id: id });
        };

    
        nav.goToReport = function () {
            $state.go('dashboard.reports');
        };
        
        return nav;
    }
})();