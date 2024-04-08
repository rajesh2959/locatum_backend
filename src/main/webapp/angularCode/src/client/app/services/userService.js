(function () {
    'use strict';

    angular
        .module('app')
        .factory('userService', service);

    service.$inject = ['dataService', 'session', 'notificationBarService'];

    function service(dataService, session, notificationBarService) {

        var svc = {};

        svc.getUserList = function () {
            return dataService.getRecord('/rest/user/list');
        };

        svc.getRoleList = function () {
            return dataService.getRecord('/rest/role/list');
        };

        svc.getprofilepic = function (userId) {
            return dataService.getRecord('/web/account/profilepic?userid=' + userId);
        };

        svc.getprofile = function () {
            return dataService.getRecord('/rest/user/profile');
        };

        svc.updateProfile = function (payload) {
            return dataService.postData('/rest/user/profile', payload);
        };


        svc.profileupload = function (payload) {
            return dataService.postData('/rest/user/profileupload', payload);
        };

        svc.shellProfileImage = function (userId) {
            return dataService.getImagepart('/preferredLogoUrl?id=', + userId);
        }

        svc.saveUserImport = function (fileArray){
            return dataService.postMultipart('/rest/user/userbulkimport?cid=' + session.cid, fileArray);
        }

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
                    var result = (('|png|'.indexOf(type) !== -1 || '|png|'.indexOf(fileExtension) !== -1)  || ('|jpg|'.indexOf(type) !== -1 || '|jpg|'.indexOf(fileExtension) !== -1));

                    if (!result) {
                       notificationBarService.error("The file being uploaded needs to be of type image Format");
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