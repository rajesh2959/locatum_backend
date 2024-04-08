(function () {
    'use strict';

    angular
        .module('app')
        .factory('upgradedataservice', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};
       // svc.venueID=0;
        svc.saveNetworkdevice = function (networkdeviceDetails) {

            return dataService.postMultipart('/rest/beacon/device/binary/save', networkdeviceDetails);
        };


         svc.getVenueList = function () {
            return dataService.getRecord('/rest/beacon/trilaterationReports/venuelist?cid='+ session.cid);
        };

          svc.getCustList = function () {
            return dataService.getRecord('/rest/customer/get?id='+ session.cid);
        };

          svc.getFloorList = function (sid) {
            return dataService.getRecord('/rest/beacon/trilaterationReports/floorlist?cid='+ session.cid +'&sid='+sid);
        };
                
         svc.getLocationList = function (sid,spid) {
            return dataService.getRecord('/rest/beacon/device/binaryDeviceUid?cid='+ session.cid +'&sid='+sid+'&spid='+spid);
        };
        
        svc.getFloorPlanById = function (spid) {
            return dataService.getRecord('/rest/site/portion/edit?spid=' + spid);
        };

    svc.getFileUploaderInstance = function () {

            var uploader = dataService.getFileUploaderInstanceWithData('/rest/beacon/device/binary/save');

            uploader.autoUpload = false;
            uploader.removeAfterUpload = true;
            uploader.queueLimit = 10;

            uploader.filters.push({
                name: 'bankStatementFilter',
                fn: function (item, options) {
                    var fileExtension = '|' + item.name.slice(item.name.lastIndexOf('.') + 1) + '|';
                    var type = '|' + item.type.slice(item.type.lastIndexOf('/') + 1) + '|';
                    var result = ('|bin|ipk|'.indexOf(type) !== -1 || '|bin|ipk|'.indexOf(fileExtension) !== -1);

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

        return svc;

    }

})();
