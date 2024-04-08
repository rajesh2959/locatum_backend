(function () {
    'use strict';
    angular
        .module('app.users')
        .controller('usersController', controller);
    controller.$inject = ['messagingService', 'notificationBarService', '$q', 'modalService', 'navigation', '$rootScope', '$timeout', 'userService', 'environment', '$linq', 'tagService'];

    /* @ngInject */
    function controller(messagingService, notificationBarService, $q, modalService, navigation, $rootScope, $timeout, userService, environment, $linq, tagService) {
        var vm = this;
        vm.pageHeight = screen.height - 180;
        vm.serverBaseURL = environment.serverBaseUrl;
        vm.users={};
        vm.getData = function () {
            userService.getUserList()
                .then(function (result) {
                    vm.userAllList = result.users;
                    vm.userList = vm.userAllList;
                });
        };

        vm.goToUser = function (id, user) {
            modalService.addUserModal(id, user).result.then(function (res) {
                vm.getData();
            }, function () {
            });
        }

       function initUploader() {

            vm.uploader = tagService.getFileUploaderInstance();

            vm.uploader.onErrorItem = function (item, response, status, headers) {
                $timeout(function () { progressModel.close(); }, 10);
                modalService.warrningMessageModel('Error!', '<div class="text-center"><h4 class="text-danger"><i class="fa fa-times text-danger faa-pulse animated" aria-hidden="true"></i>Unexpected error occurred,please try again!</h4></div>', false).result.then(
                    function (item) {
                    });
            };

            vm.uploader.onAfterAddingFile = function (FileUploader) {
                var fileReader = new FileReader();
                fileReader.readAsDataURL(FileUploader._file);
                fileReader.onload = function (e) {
                    $timeout(function () {
                        var target = e.target.result;
                        vm.users.imagePath = target;
                        var blob = null;
                        var imageDataUR = "";
                        if (vm.users.imagePath.indexOf("base64") != -1) {
                            imageDataUR = vm.users.imagePath;
                        }

                        var fd = new FormData();
                        var data = atob(imageDataUR.replace(/^.*?base64,/, ''));
                        var asArray = new Uint8Array(data.length);
                        for (var i = 0, len = data.length; i < len; ++i) {
                            asArray[i] = data.charCodeAt(i);
                        }
                        var fileType = getB64Type(imageDataUR);
                        blob = new Blob([asArray.buffer], { type: fileType });
                        fd.append('file', blob);

                        userService.saveUserImport(fd).then(function (result) {
                            notificationBarService.success(result.body);
                            activate();
                            
                        });
                    });
                };
                activate();
                initUploader();
            };
        }

        function getB64Type($str) {
            return $str.split(';')[0].split(':')[1];
        }

        /*
            To be removed later

        vm.searchBy = function (item) {
            if (item && item.length > 0 && item != '') {
                var searchUserList = $linq.Enumerable().From(vm.userAllList)
                    .Where(function (x) {
                        return x.fname.toLowerCase().contains(item.toLowerCase()) ||
                            x.lname.toLowerCase().contains(item.toLowerCase()) ||
                            x.designation.toLowerCase().contains(item.toLowerCase()) ||
                            x.role.toLowerCase().contains(item.toLowerCase()) ||
                            x.email.toLowerCase().contains(item.toLowerCase()) ||
                            x.phone.contains(item) 
                    }).ToArray();
                vm.userList = searchUserList;
            }
            else
                vm.getData();
        }*/

        function activate() {
            vm.getData();
        }

        activate();
        initUploader();

        return vm;
    }
})();