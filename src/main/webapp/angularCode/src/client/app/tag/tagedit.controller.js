(function () {
    'use strict';
    angular
        .module('app.tag')
        .controller('tagEditController', controller);
    controller.$inject = ['$rootScope', 'tagService', 'spid', 'macaddr', 'venuesession', 'environment', 'session', 'geoFenceService', '$linq', 'SimpleListScreenViewModel', 'notificationBarService', 'modalService', 'navigation', 'messagingService'];

    /* @ngInject */
    function controller($rootScope, tagService, spid, macaddr, venuesession, environment, session, geoFenceService, $linq, SimpleListScreenViewModel, notificationBarService, modalService, navigation, messagingService) {
        var baseUrl = environment.serverBaseUrl;
        var vm = new SimpleListScreenViewModel();
        vm.macaddr = macaddr;
        vm.pagename = "Configuration Settings";
        vm.width = 300;
        vm.height = 200;
        vm.pageHeight = "";
        vm.spid = spid;
        vm.tagEdit = {};
        vm.tagEdit.assignedTo = macaddr;
        vm.tagEdit.refTxPower = -59;
        vm.tagEdit.publisher = "Customer";
        if (venuesession.sid) {
            vm.sid = venuesession.sid;
        }

        vm.tagtypeList = [
            { "key": "Contractor", "value": "Contractor" },
            { "key": "Employee", "value": "Employee" },
            { "key": "Visitor", "value": "Visitor" }
        ];

        vm.publisherList = [
            { "key": "Customer", "value": "Customer" },
            { "key": "Venue", "value": "Venue" }
        ];

        vm.tagmodelList = [
            { "key": "neck", "value": "Neck" },
            { "key": "sticker", "value": "Sticker" },
            { "key": "label", "value": "Label" },
            { "key": "rock", "value": "Rock" },
            { "key": "box", "value": "Box" },
            { "key": "key", "value": "Key" },
            { "key": "coin", "value": "Coin" },
            { "key": "card", "value": "Card" }
        ];

        vm.txPowerList = [
            { "key": 4, "value": "4" },
            { "key": 0, "value": "0" },
            { "key": -4, "value": "-4" },
            { "key": -8, "value": "-8" },
            { "key": -12, "value": "-12" },
            { "key": -16, "value": "-16" },
            { "key": -20, "value": "-20" },
            { "key": -30, "value": "-30" }
        ];

        function createBeaconIntervalList() {
            var i = 100;
            vm.beaconIntervalList = [];
            for (i = 100; i <= 10000; i++) {
                vm.beaconIntervalList.push({ "key": i, "value": i });
                i += 99;
            }
        }

        vm.tab = function (index) {
            vm.tabIndex = index;
        };

        vm.cancel = function () {
            var message = "<p>The changes to the tag edit have not been saved yet. Are you sure you want to cancel the changes?</p>";
            modalService.questionModal('Tag Edit Cancellation', message, true).result.then(function () {
                navigation.goToTag();
            });

        }

        vm.getTagDetail = function () {
            tagService.getTagDetail(vm.macaddr)
                .then(function (result) {
                    vm.tagEdit = result.body;
                });
        };

        vm.saveTagDetail = function (basicForm, advancedForm) {
            messagingService.broadcastCheckFormValidatity();
            if (basicForm.$valid && advancedForm.$valid) {
                vm.attributes = [];
                var tag = {};
                tag.minor = vm.tagEdit.minor;
                tag.major = vm.tagEdit.major;
                tag.assignedto = vm.tagEdit.assignedTo;
                tag.uuid = vm.tagEdit.uid;
                tag.tagtype = vm.tagEdit.tag_type;
                tag.name = vm.tagEdit.name;
                tag.txpower = vm.tagEdit.txpower;
                tag.interval = vm.tagEdit.interval;
                tag.tagmod = vm.tagEdit.tagmodel;
                tag.reftx = vm.tagEdit.reftxpwr;

                vm.attributes.push(tag);
                tagService.saveTagDetail({ attributes: vm.attributes }, vm.tagEdit.macaddr)
                    .then(function (result) {
                        notificationBarService.success(result.body);
                        navigation.goToTag();
                    });
            }
        };

        function activate() {
            createBeaconIntervalList();
            vm.getTagDetail();
        }

        activate();

        return vm;
    }
})();
