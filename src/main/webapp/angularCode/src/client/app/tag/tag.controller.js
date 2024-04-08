(function () {
    'use strict';
    angular
        .module('app.tag')
        .controller('tagController', controller);
    controller.$inject = ['tagService', 'navigation', 'SimpleListScreenViewModel', 'notificationBarService', 'modalService', '$timeout', '$linq', 'session', 'environment'];

    /* @ngInject */
    function controller(tagService, navigation, SimpleListScreenViewModel, notificationBarService, modalService, $timeout, $linq, session, environment) {
        var vm = new SimpleListScreenViewModel();
        vm.pageHeight = "";
        vm.tags = {};
        vm.userRole = session.role;
        vm.envRole = environment.userRole;
        vm.isTagDebugAllOn = false;
        vm.macAddressRegex = /^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$/gmi;
        vm.ischekAll = false;
        vm.istaginitialload = true;
        vm.istaginusedinitialload = true;
        vm.istagEdit = true;
        vm.isSave = false;
        vm.isCanceled = false;

        vm.assignedTo = "";
        vm.tag_type   = "";
        vm.tagModel   = "";
        vm.refTxPwr   = "";
        vm.tagtypeList = [
            { "key": "Contractor", "value": "Contractor" },
            { "key": "Employee", "value": "Employee" },
            { "key": "Visitor", "value": "Visitor" }
        ];

        vm.tagmodelList = [
            { "key": "Neck", "value": "Neck" },
            { "key": "Sticker", "value": "Sticker" },
            { "key": "Label", "value": "Label" },
            { "key": "Rock", "value": "Rock" },
            { "key": "Box", "value": "Box" },
            { "key": "Key", "value": "Key" },
            { "key": "Coin", "value": "Coin" },
            { "key": "Card", "value": "Card" }
        ];

        vm.dataOperationsTag = new SimpleListScreenViewModel();
        vm.dataOperationsInUseTag = new SimpleListScreenViewModel();

        vm.getTaglist = function (refresh) {
            tagService.getinactiveAlertsListForTable(refresh, vm.dataOperationsTag.dataOperations, vm.dataOperationsTag.filterFn)
                .then(function (result) {
                    if (vm.istaginitialload) {
                        if (result.allData && result.allData.length > 0) {
                            angular.forEach(result.allData, function (item) {
                                item.isChecked = false;
                                item.assignedTo = item.assignedTo != undefined && item.assignedTo != "" ? item.assignedTo : item.macaddr;
                                item.tag_type = item.tag_type != undefined && item.tag_type != "" ? item.tag_type : vm.tagtypeList[0].key;
                                item.tagmodel = item.tagmodel != undefined && item.tagmodel != "" ? item.tagmodel : vm.tagmodelList[0].key;
                                item.refTxPwr = item.refTxPwr != undefined && item.refTxPwr != "" ? item.refTxPwr : '-59';
                                item.istagEdit = false;
                            });
                            vm.istaginitialload = false; 
                        }      
                                        
                    }

                    vm.allTagDetails = result.allData;
                    if (result.pagedData && result.pagedData.length > 0) {
                        angular.forEach(result.pagedData,
                            function (item) {
                                item.assignedTo = item.assignedTo != undefined && item.assignedTo != "" ? item.assignedTo : item.macaddr;
                                item.tag_type = item.tag_type != undefined && item.tag_type != "" ? item.tag_type : vm.tagtypeList[0].key;
                                item.tagmodel = item.tagmodel != undefined && item.tagmodel != "" ? item.tagmodel : vm.tagmodelList[0].key;
                                item.refTxPwr = item.refTxPwr != undefined && item.refTxPwr != "" ? item.refTxPwr : '-59';
                                item.istagEdit = false;
                            });
                    }
                    vm.pagedDataTagDetails = result.pagedData;
                    vm.dataOperationsTag.fullCount = result.dataCount;
                    vm.dataOperationsTag.filteredCount = result.filteredDataCount;
                });

        };

        vm.getInusedTags = function (refresh) {

            tagService.getInusedTagListforTable(refresh, vm.dataOperationsInUseTag.dataOperations, vm.dataOperationsInUseTag.filterFn)
                .then(function (result) {
                    if (vm.istaginusedinitialload) {
                        angular.forEach(result.allData,
                            function (value, key) {
                                value.isChecked = false;
                                value.debugstatus = (value.debug != 'disable');
                            });
                            vm.istaginusedinitialload = false;
                    }

                    angular.forEach(result.allData,
                        function (value, key) {
                            value.debugstatus = (value.debug != 'disable');
                        });

                    vm.allInusedTagDetails = result.allData;
                    if (result.pagedData && result.pagedData.length > 0) {
                        angular.forEach(result.pagedData,
                            function (item) {
                                vm.battaryColor = setBatteryColor(item.battery_level);
                            });
                    }
                    vm.pagedInusedTagDetails = result.pagedData;
                    vm.dataOperationsInUseTag.fullCount = result.dataCount;
                    vm.dataOperationsInUseTag.filteredCount = result.filteredDataCount;
                    vm.isTagDebugAllOn = !$linq.Enumerable().From(result.allData)
                        .Any(function (x) {
                            return !x.debugstatus;
                        });
                });

        };

        function setBatteryColor(intBattery) {
            var color = 'red';
            if (intBattery >= 75) {
                color = "green";
            } else if (intBattery > 50 && intBattery <= 75) {
                color = "green";
            } else if (intBattery > 25 && intBattery <= 50) {
                color = "orange";
            } else if (intBattery >= 15 && intBattery <= 25) {
                color = "red";
            } else if (intBattery < 15) {
                color = "red";
            }
            return color;
        }

        vm.refresh = function (refresh) {
            vm.clearControl();
            vm.getTaglist(refresh);
            vm.getInusedTags(refresh);
        };

        vm.refreshGetinuseTag = function (refresh) {
            vm.getInusedTags(refresh);
        };

        function activate() {
            vm.refresh(true);
        };

        vm.onInUseDebugChange = function (isDebugAll, tagdetail) {
            if (vm.allInusedTagDetails.length > 0) {
                modalService.questionModal('Confirmation', 'Are you sure you want to change the debug status?').result.then(
                    function () {
                        if (isDebugAll) {
                            tagdetail = {};
                            tagdetail.debugstatus = vm.isTagDebugAllOn;
                            tagdetail.macaddr = null;
                            tagdetail.cid = null;
                        } else
                            if (tagdetail == null)
                                tagdetail = {}
                        tagdetail.debug = (tagdetail.debugstatus) ? 'enable' : 'disable';
                        tagService.updateDebugStatus(tagdetail.macaddr, tagdetail.debugstatus, tagdetail.cid, isDebugAll).then(function (result) {
                            if (result && result.success) {
                                vm.getInusedTags(true);
                            }
                            notificationBarService.success(result.body);
                        });
                    },
                    function () {
                        if (tagdetail != null) {
                            tagdetail.debug = !tagdetail.debugstatus;
                            tagdetail.debugstatus = tagdetail.debug;
                            var isAnyRowDisable = !$linq.Enumerable().From(vm.allInusedTagDetails)
                                .Any(function (x) {
                                    return x.debugstatus == false;
                                });
                            vm.isTagDebugAllOn = !isAnyRowDisable;
                        }

                        vm.isTagDebugAllOn = !vm.isTagDebugAllOn;
                    });
            } else {
                var message = "<p>There are no tags found.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                    vm.isTagDebugAllOn = !vm.isTagDebugAllOn;
                });
            }
        };

        vm.inputCheckAll = function (isChecked) {
            angular.forEach(vm.allTagDetails,
                function (value, key) {
                    if (isChecked == true) {
                        value.isChecked = true;
                    } else {
                        value.isChecked = false;
                    }
                });
        };

        vm.inUseTaginputCheckAll = function (isChecked) {
            angular.forEach(vm.allInusedTagDetails,
                function (value, key) {
                    if (isChecked == true) {
                        value.isChecked = true;
                    } else {
                        value.isChecked = false;
                    }
                });
        };

        vm.tagCheckInAll = function () {
            vm.selectedTags = [];
            angular.forEach(vm.allInusedTagDetails,
                function (value, key) {
                    if (value.isChecked) {
                        vm.selectedTags.push(value.id);
                    }
                });

            if (vm.selectedTags && vm.selectedTags.length > 0) {
                modalService.questionModal('Confirmation', 'Are you sure you want to checkin the tags?').result.then(
                    function () {
                        tagService.checkInAllTags(vm.selectedTags).then(function (result) {
                            if (result && result.success) {
                                vm.refresh(true);
                            }
                            notificationBarService.success(result.body);
                        });
                    },
                    function () {
                    });
            } else {
                var message = "<p>There are no tag selected for checkin.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };

        vm.tagCheckOut = function () {

            if (vm.frm.$valid) {
                vm.selectedTags = [];
                angular.forEach(vm.allTagDetails,
                    function (value, key) {
                        if (value.isChecked) {
                            vm.selectedTags.push({ macaddr: value.macaddr, tag_type: value.tag_type, assignedTo: value.assignedTo, tagmod: value.tagmodel, reftx: value.refTxPwr });
                        }
                    });
                if (vm.selectedTags && vm.selectedTags.length > 0) {
                    modalService.questionModal('Confirmation', 'Are you sure you want to checkout the selected tag?', 'Test').result.then(
                        function () {
                            var data = { cid: vm.allTagDetails[0].cid, beacon: vm.selectedTags };
                            var ss = JSON.stringify(data);
                            tagService.savetagDetails(ss).then(function (result) {
                                if (result && result.success) {
                                    vm.refresh(true);
                                }
                                notificationBarService.success(result.body);
                            });
                        },
                        function () {
                        });
                } else {
                    var message = "<p>There are no tag selected for checkout.</p>";
                    modalService.messageModal('Information', message).result.then(function () {
                    });
                }
            }
            else {
                var message = "<p>Select All required fields.</p>";
                modalService.messageModal('Error', message).result.then(function () {
                });

                modalService.warrningMessageModel('Error!', '<div class="text-center"><h4 class="text-danger"><i class="fa fa-times text-danger faa-pulse animated" aria-hidden="true"></i>Select All required fields.!</h4></div>', false).result.then(
                    function (item) {
                    });
            }
        };

        vm.goToInUseTagPage = function () {

            if (parseInt(vm.goToInUseTagPageNumber) > vm.dataOperationsInUseTag.totalPageCount) {
                vm.goToInUseTagPageNumber = '';
            } else {
                vm.dataOperationsInUseTag.dataOperations.paging.currentPage = vm.goToInUseTagPageNumber;
                vm.getInusedTags();
                vm.goToInUseTagPageNumber = '';
            }
        };

        vm.goToTagPage = function () {
            if (parseInt(vm.goToTagPageNumber) > vm.dataOperationsTag.totalPageCount) {
                vm.goToTagPageNumber = '';
            } else {
                vm.dataOperationsTag.dataOperations.paging.currentPage = vm.goToTagPageNumber;
                vm.getTaglist();
                vm.goToTagPageNumber = '';
            }
        };

        vm.tagDashboard = function (tagInusedDetailsList) {
            if (tagInusedDetailsList && tagInusedDetailsList.spid)
                navigation.goToTagDashboard(tagInusedDetailsList.spid, tagInusedDetailsList.macaddr, tagInusedDetailsList.battery_level, tagInusedDetailsList.location);
        };

        vm.deleteTags = function () {
            var tagMacIds = [];
            angular.forEach(vm.allTagDetails,
                function (value, key) {
                    if (value.isChecked) {
                        tagMacIds.push(value.macaddr);
                    }
                });
            if (tagMacIds.length > 0) {
                modalService.confirmDelete('Are you sure you want to delete the tags?').result.then(
                    function () {
                        tagService.deleteTags(tagMacIds).then(function (result) {
                            if (result && result.success) {
                                vm.refresh(true);
                            }
                            notificationBarService.success(result.body);
                        });
                    },
                    function () {
                    });
            } else {
                var message = "<p>There are no tag selected for delete.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };

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
                        vm.tags.imagePath = target;
                        var blob = null;
                        var imageDataUR = "";
                        if (vm.tags.imagePath.indexOf("base64") != -1) {
                            imageDataUR = vm.tags.imagePath;
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

                        tagService.saveTagImport(fd).then(function (result) {
                            notificationBarService.success(result.body);
                            activate();
                        });
                    });
                };
            };
        }

        vm.clearControl = function () {
            document.getElementById("upload").value = "";
            vm.tags.imagePath = "";
            vm.ischekAll = false;
            vm.inUseischekAll = false;
        };

        function getB64Type($str) {
            return $str.split(';')[0].split(':')[1];
        }

        vm.macaddresscheck = function (e) {
            var r = /([a-f0-9]{2})([a-f0-9]{2})/i,
                str = e.target.value.replace(/[^a-f0-9]/ig, "");
            while (r.test(str)) {
                str = str.replace(r, '$1' + ':' + '$2');
            }
            e.target.value = str.slice(0, 17);
        };

        vm.availableTagSearch = function (search) {
            if (search && search.length > 0) {
                vm.dataOperationsTag.filterFn = function (data) {
                    var lowerCaseSearchTerm = search.toLowerCase();
                    var result = (data.macaddr && data.macaddr.toLowerCase().contains(lowerCaseSearchTerm));
                    return result;
                };
            } else {
                vm.dataOperationsTag.filterFn = null;
            }
            vm.getTaglist(false);
        };

        vm.inuseTagSearch = function (search) {
            if (search && search.length > 0) {
                vm.dataOperationsInUseTag.filterFn = function (data) {
                    var lowerCaseSearchTerm = search.toLowerCase();
                    var result = (data.macaddr && data.macaddr.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.state && data.state.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.tag_type && data.tag_type.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.assignedTo && data.assignedTo.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.reciveralias && data.reciveralias.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.location && data.location.toLowerCase().contains(lowerCaseSearchTerm));
                    return result;
                };
            } else {
                vm.dataOperationsInUseTag.filterFn = null;
            }
            vm.getInusedTags(false);
        }

        vm.edit = function (item) {
            navigation.goToTagEdit(0, item.macaddr);
        };

        //vm.isRowChecked = function () {
        //    var isAnyRowChecked = !$linq.Enumerable().From(vm.allTagDetails)
        //        .Any(function (x) {
        //            return x.isChecked == false;
        //        });
        //    if (!isAnyRowChecked)
        //        vm.ischekAll = false;
        //    else
        //        vm.ischekAll = true;
        //}

        vm.isRowChecked = function () {
            var isallchecked = true;
            angular.forEach(vm.allTagDetails, function (item) {
                if (item.isChecked == false) {
                    isallchecked = false;
                }
            });
            if (isallchecked)
                vm.ischekAll = true;
            else
                vm.ischekAll = false;
        }


        vm.editagrows = function (macid) {
            /*
                In edit Tag action, if tag details are modified and neither saved nor canceled,
                reset those modified details to the previous details of that tag
            */
            angular.forEach(vm.allTagDetails, function (item) {
                if(item.istagEdit == true && (!vm.isSaved &&  !vm.isCanceled)) {
                    item.assignedTo = vm.assignedTo;
                    item.tag_type   = vm.tag_type;
                    item.tagmodel   = vm.tagModel ;
                    item.refTxPwr   = vm.refTxPwr;
                }
            });

            angular.forEach(vm.allTagDetails, function (item) {
                
                item.istagEdit = false;
                if (item.macaddr == macid) {
                    item.istagEdit = true;
                    vm.assignedTo = item.assignedTo;
                    vm.tag_type   = item.tag_type;
                    vm.tagModel   = item.tagmodel;
                    vm.refTxPwr   = item.refTxPwr;

                }
            });
            vm.isSaved = false;
            vm.isCanceled = false;

        };

        vm.saveTag = function (tag) {
            vm.isSaved = true;
            if(tag.istagEdit == true && isTagDetailsChanged(tag)) {
                tagService.saveTag (tag).then(function (result) {
                        notificationBarService.success(result.body);
                        activate();
                        
                    });
            }
            tag.istagEdit = false;
        }

        function isTagDetailsChanged (item) {
            var isChanged = true;
            if(vm.assignedTo == item.assignedTo && vm.tag_type == item.tag_type 
                && vm.tagModel == item.tagmodel && vm.refTxPwr == item.refTxPwr) {
                isChanged = false;
            }
            return isChanged;
        }

        vm.canceltagrows = function (macid) {
            vm.isCanceled = true;
            angular.forEach(vm.allTagDetails, function (item) {
                if (item.macaddr == macid) {
                    item.istagEdit = false;
                    item.assignedTo = vm.assignedTo;
                    item.tag_type   = vm.tag_type;
                    item.tagmodel   = vm.tagModel ;
                    item.refTxPwr   = vm.refTxPwr;
                }
            });

        };

        vm.resettable = function () {
            vm.istagEdit = true;
        }

        vm.isInusedGridRowChecked = function () {
            //var isAnyRowChecked = !$linq.Enumerable().From(vm.allInusedTagDetails)
            //    .Any(function (x) {
            //        return x.isChecked == false;
            //    });
            //if (!isAnyRowChecked)
            //    vm.inUseischekAll = false;
            //else
            //    vm.inUseischekAll = true;
            var isallchecked = true;
            angular.forEach(vm.allInusedTagDetails, function (item) {
                if (item.isChecked == false) {
                    isallchecked = false;
                }
            });
            if (isallchecked)
                vm.inUseischekAll = true;
            else
                vm.inUseischekAll = false;
        }

        activate();

        initUploader();

        return vm;
    }
})();