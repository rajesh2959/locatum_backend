(function () {
    'use strict';

    angular.module('app', [
        'app.core',
        'app.dashboard',
        'app.login',
        'app.layout',
        'app.venue',
        'app.floorview',
        'app.floorplan',
        'app.reports',
        'app.visualization',
        'app.users',
        'app.role',
        'app.upgrade',
        'app.networkconfig',
        'app.geoconfig',
        'app.geofencealerts',
        'app.geofence',
        'app.drawfloorplan',
        'app.inactivealerts',
        'app.tag',
        'app.gateway',
        'app.adddevice',
        'app.customizedalert',
        'app.support',
        'app.license',
        'app.inactivecustomers',
        'ngSanitize',
        'angularjs-dropdown-multiselect'
    ]);

})();