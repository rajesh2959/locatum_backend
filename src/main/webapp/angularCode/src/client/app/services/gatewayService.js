(function () {
    'use strict';

    angular
        .module('app')
        .factory('gatewayService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};

        svc.getReceiverListforTable = function (refresh, dataOperations, filterFn) {
            return dataService.getGwayReceiverListfortable('/rest/beacon/device/receiver?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        svc.getServerListForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getGwayServerListfortable('/rest/beacon/device/server?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        svc.updateDebugStatus = function (uid, status) {
            return dataService.postData('/rest/beacon/device/debugByDevices?uid=' + uid + '&debugflag=' + status, null); //{ uid: uid, debugflag: status});
        };

        svc.updateAllDebugStatus = function (status, type) {
            return dataService.postData('/rest/beacon/device/debugByDevices?cid=' + session.cid + '&debugflag=' + status + '&type=' + type, null); //{ uid: uid, debugflag: status});
        };
        
        svc.delete = function (uids) {
            return dataService.postData('/rest/beacon/device/ibeaconBulkDelete', uids);
        };

        //svc.getFileUploaderInstance = function () {

        //    var uploader = dataService.getFileUploaderInstance();

        //    uploader.autoUpload = false;
        //    uploader.removeAfterUpload = true;
        //    uploader.queueLimit = 10;

        //    uploader.filters.push({
        //        name: 'importFilter',
        //        fn: function (item, options) {
        //            var fileExtension = '|' + item.name.slice(item.name.lastIndexOf('.') + 1) + '|';
        //            var type = '|' + item.type.slice(item.type.lastIndexOf('/') + 1) + '|';
        //            //var result = ('|txt|'.indexOf(type) !== -1 || '|txt|'.indexOf(fileExtension) !== -1);

        //            //if (!result) {
        //            //    notificationBarService.error("The file being uploaded needs to be of type txt Format");
        //            //    return result;
        //            //}

        //            var result = item.size < 4194304; //4MB

        //            if (!result) {
        //                notificationBarService.error("The file being uploaded can't be more than 4MB");
        //                return result;
        //            }
        //            return result;
        //        }
        //    });

        //    return uploader;
        //};

        svc.saveGatewayImport = function (fileArray) {
            return dataService.postMultipart('/rest/beacon/device/gatewayBulkUpload?cid=' + session.cid, fileArray);
        };

        svc.getmacaddressList = function (cid, refresh) {
            return dataService.getRecord('/rest/beacon/device/nonConfiguredDeviceInFloor?cid=' + session.cid, null);
        };
        svc.networkDeviceList = function (spid) {
            return dataService.getRecord('/rest/site/portion/networkdevice/list?spid=' +spid);
        };
        return svc;
    }
})();