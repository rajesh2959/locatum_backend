(function () {
    'use strict';

    angular
        .module('app')
        .factory('tagService', service);

    service.$inject = ['dataService', 'session', 'notificationBarService'];

    function service(dataService, session, notificationBarService) {

        var svc = {};

        svc.getinactiveAlertsListForTable = function (refresh, dataOperations, filterFn) {
            return dataService.getDataListForTable('/rest/beacon/list/checkedin?cid=' + session.cid, refresh, dataOperations, filterFn);
            //return dataService.getTaglistfortable('/rest/beacon/list/checkedin?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        svc.getInusedTagListforTable = function (refresh, dataOperations, filterFn) {
            return dataService.getDataListForTable('/rest/beacon/list/checkout?cid=' + session.cid, refresh, dataOperations, filterFn);
            //return dataService.getTaglistfortable('/rest/beacon/list/checkout?cid=' + session.cid, refresh, dataOperations, filterFn);
        };

        svc.updateDebugStatus = function (macaddr, status, cid, isDebugAll) {
            if (isDebugAll)
                return dataService.postData('/rest/beacon/debugByTag?debugflag=' + status + '&cid=' + session.cid, null);
            else
                return dataService.postData('/rest/beacon/debugByTag?macaddr=' + macaddr + '&debugflag=' + status + '&cid=' + cid, null);
        };

        svc.savetagDetails = function (tagDetails) {
            return dataService.postData('/rest/beacon/checkout/tag', tagDetails);
        };

        svc.deleteTags = function (tags) {
            return dataService.postData('/rest/beacon/delete', tags);
        };

        svc.checkInTagDetail = function (tagId) {
            return dataService.postData('/rest/beacon/checkin/beacon?id=' + tagId, null);
        };

        svc.checkInAllTags = function (tags) {
            return dataService.postData('/rest/beacon/bulkCheckInTag', tags);
        };

        svc.getTagDashboardlistfortable = function (macaddr, timeInterval, refresh, dataOperations, filterFn) {
            return dataService.getTagDashboardlistfortable('/rest/beacon/ble/networkdevice/tagactivity?time=' + timeInterval + '&macaddr=' + macaddr, refresh, dataOperations, filterFn);
        };

        svc.downloadAsPDF = function (cid,macaddr) {
            return dataService.downloadAsPDF('/rest/site/portion/networkdevice/report?filtertype=tagBased&reporttype=tagBased&macaddr=' + macaddr + '&cid=' + cid);
        };

        svc.getNetworkDeviceList = function (spid, macaddr) {
            return dataService.makeGETRequestWithoutLoading('/rest/site/portion/networkdevice/finder/list?spid=' + spid + '&param=1&macaddr=' + macaddr);
        };

        svc.getFileUploaderInstance = function () {

            var uploader = dataService.getFileUploaderInstance();

            uploader.autoUpload = false;
            uploader.removeAfterUpload = true;
            uploader.queueLimit = 10;

            uploader.filters.push({
                name: 'importFilter',
                fn: function (item, options) {
                    var fileExtension = '|' + item.name.slice(item.name.lastIndexOf('.') + 1) + '|';
                    var type = '|' + item.type.slice(item.type.lastIndexOf('/') + 1) + '|';
                    //var result = ('|txt|'.indexOf(type) !== -1 || '|txt|'.indexOf(fileExtension) !== -1);

                    //if (!result) {
                    //    notificationBarService.error("The file being uploaded needs to be of type txt Format");
                    //    return result;
                    //}

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

        svc.saveTag = function (tag) {
            return dataService.postData('/rest/beacon/savetag',tag);
        };

        svc.saveTagImport = function (fileArray) {
            return dataService.postMultipart('/rest/beacon/tagimport?cid=' + session.cid, fileArray);
        };

        svc.getTagDetail = function (macaddr) {
            return dataService.getRecord('/rest/beacon/configure?macaddr=' + macaddr);
        };

        svc.saveTagDetail = function (tag, macaddr) {
            return dataService.postData('/rest/beacon/save?macaddr=' + macaddr + '&conf=' + JSON.stringify(tag));
        };

        svc.getInusedTagList = function () {
            return dataService.getRecord('/rest/beacon/list/checkout?cid=' + session.cid);
        };

        // svc.getAllLocationList = function (venuIdLst, floorIdLst, geofenceLst) {
        //     return dataService.postData('/rest/geofence/filter/list?cid=' + session.cid + '&status=enabled&sid=' + venuIdLst + '&spid=' + floorIdLst + '&fence=' + geofenceLst);
        // };

        svc.getAllLocationList = function (venuIdLst, floorIdLst, geofenceLst) {
            return dataService.postData('/rest/beacon/device/filter/list?cid=' + session.cid + '&status=enabled&sid=' + venuIdLst + '&spid=' + floorIdLst + '&fence=' + geofenceLst+ '&type=receiver');
        };

        return svc;
    }

})();