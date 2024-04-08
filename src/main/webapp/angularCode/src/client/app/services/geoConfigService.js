(function () {
    'use strict';

    angular
        .module('app')
        .factory('geoConfigService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};
        svc.getCanvasData = function (spid, sid) {
            return dataService.getRecord('/rest/beacon/geo/plot/' + spid + '/' + sid + '/map');
        };

        svc.saveData = function (spid, uid, markerData) {
            return dataService.postData('/rest/beacon/geo/plot?spid=' + spid + "&uid=" + uid, markerData);
            // facesix/rest/beacon/geo/plot?spid=5c32f22adb9a521969d706b7&uid=
            // http://locatum.qubercomm.com/facesix/web/site/portion/planfile?sid=5c32f1d1db9a521969d706b6&spid=5c32f22adb9a521969d706b7&cid=5a65cd7ddb9a525c12dd035e
        };
        return svc;
    }
})();