(function () {
    'use strict';
    angular
        .module('app')
        .factory('floordataservice', service);
    service.$inject = ['dataService', 'session', 'notificationBarService'];

    function service(dataService, session, notificationBarService) {

        var svc = {};

        svc.getFloorPlanList = function (sid) {
            return dataService.getRecord('/rest/site/portion/list?sid=' + sid);
        };
        svc.deleteFloorDetails = function (floorId) {
            return dataService.post('/rest/site/portion/delete?spid=' + floorId);
        };
        svc.multiDeleteFloorDetails = function (floorIds) {
            return dataService.post('/rest/site/portion/multiDelete?spids=' + floorIds);
        };

        svc.getNetworkdevice = function (spid) {
            return dataService.getRecord('/rest/site/portion/networkdevice/list?spid=' + spid);
        };
        svc.getNetworkConfig = function (spid) {
            return dataService.getRecord('/rest/site/portion/networkconfig?spid=' + spid);
        };

        svc.saveFloorPlan = function (venuDetails) {

            return dataService.postMultipart('/rest/site/portion/save', venuDetails);
        };

        svc.saveNetworkdevice = function (networkdeviceDetails) {

            return dataService.postData('/rest/site/portion/networkdevice/ibeaconsave', networkdeviceDetails);
        };

        svc.checkDuplicateDevice = function (uid,config) {
            return dataService.getRecord('/rest/beacon/device/checkDuplicate?uid=' + uid + '&config=' + config);
        };

        svc.getAutoConfiguration = function () {

            return dataService.postData('/rest/beacon/device/beacondefaultconfig');
        };

        svc.getFloorPlanById = function (spid) {
            return dataService.getRecord('/rest/site/portion/edit?spid=' + spid);
        };

        svc.getDeviceById = function (uid, cid) {
            return dataService.getRecord('/rest/beacon/device/configure?uid=' + uid + '&cid=' + cid);
        };

        svc.getFileUploaderInstance = function () {

            var uploader = dataService.getFileUploaderInstanceWithData('/rest/site/portion/save');

            uploader.autoUpload = false;
            uploader.removeAfterUpload = true;
            uploader.queueLimit = 10;

            uploader.filters.push({
                name: 'bankStatementFilter',
                fn: function (item, options) {
                    var fileExtension = '|' + item.name.slice(item.name.lastIndexOf('.') + 1) + '|';
                    var type = '|' + item.type.slice(item.type.lastIndexOf('/') + 1) + '|';
                    var result = ('|png|jpeg|jpg|'.indexOf(type) !== -1 || '|png|jpeg|jpg|'.indexOf(fileExtension) !== -1);

                    if (!result) {
                        notificationBarService.error("The file being uploaded needs to be of type png or jpeg or  jpg  Format");
                        return result;
                    }

                    var result = item.size < 4194304; //4MB

                    if (!result) {
                        notificationBarService.error("The file being uploaded can't be more than 4MB");
                        return result;
                    }
                    return result;
                }
            });

            return uploader;
        };

        svc.getFloorList = function (venuidlst) {
            return dataService.postData('/rest/site/portion/filter/list?cid=' + session.cid + '&sid=' + venuidlst);
        };

        return svc;
    }
})();
