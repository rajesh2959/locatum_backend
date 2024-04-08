(function () {
    'use strict';

    angular
        .module('app')
        .factory('dashboardDataService', service);

    service.$inject = ['dataService', 'session'];

    function service(dataService, session) {

        var svc = {};

        svc.getTags = function (sid, refresh) {
            return dataService.getRecord('/rest/beacon/ble/networkdevice/venue/taginfo?cid=' + session.cid + '&sid=' + sid, refresh);
        };

        svc.getFloor = function (sid, refresh) {
            return dataService.getRecord('/rest/beacon/trilaterationReports/floorlist?sid=' + sid + "&cid=" + session.cid, refresh);
        };

        svc.getVenue = function (cid, refresh) {
            return dataService.getRecord('/rest/beacon/trilaterationReports/venuelist?cid=' + session.cid, refresh);
        };

        svc.getDeviceList = function (spid, refresh) {
            return dataService.getRecord('/rest/site/portion/networkdevice/list?spid=' + spid, refresh);
        };

        svc.getPersonInfo = function (spid, time, refresh) {
            return dataService.makeGETRequestWithoutLoading('/rest/site/portion/networkdevice/personinfo?spid=' + spid + "&time=" + time, true);
        };

        svc.getAlerts = function (sid, refresh) {
            return dataService.makeGETRequestWithoutLoading('/rest/beacon/ble/networkdevice/gateway_alerts?sid=' + sid + "&cid=" + session.cid, refresh);
        };

        svc.getTagList = function (cid) {
            return dataService.getRecord('/rest/beacon/list/checkedout?cid=' + cid);
        };

        svc.getTagTypes = function (cid) {
            return dataService.getRecord('/rest/customer/getTagTypes?cid=' + cid);
        };

        svc.getPortion = function (spid, sid, refresh) {
            return dataService.getRecord('/rest/beacon/geo/plot/' + spid + '/' + sid + '/type', refresh);
        };

        svc.getCustomerList = function () {
            return dataService.getRecord('/rest/customer/new/list');
        };

        svc.getUserList = function (cid) {
            return dataService.getRecord('/rest/user/findbycid?cid=' + cid);
        };

        svc.getCustomer = function (id){
            return dataService.getRecord('/rest/customer/get?id=' + id); 
        }


        svc.updateTagType = function (tag_type) {
            var code = "\uf007"; //default tag Type
            if (tag_type != null) {
                if (tag_type == "Doctor") {
                    code = "\uf0f0";
                } else if (tag_type == "WheelChair") {
                    code = "\uf193";
                } else if (tag_type == "Asset") {
                    code = "\uf217";
                } else if (tag_type == "Bed") {
                    code = "\uf236";
                } else if (tag_type == "Ambulance") {
                    code = "\uf0f9";
                } else if (tag_type == "MedicalKit") {
                    code = "\uf0fa";
                } else if (tag_type == "Heartbeat") {
                    code = "\uf21e";
                } else if (tag_type == "Cycle") {
                    code = "\uf206";
                } else if (tag_type == "Truck") {
                    code = "\uf0d1";
                } else if (tag_type == "Bus") {
                    code = "\uf207";
                } else if (tag_type == "Car") {
                    code = "\uf1b9";
                } else if (tag_type == "Child") {
                    code = "\uf1ae";
                } else if (tag_type == "Female") {
                    code = "\uf182";
                } else if (tag_type == "Male") {
                    code = "\uf183";
                } else if (tag_type == "Fax") {
                    code = "\uf1ac";
                } else if (tag_type == "User") {
                    code = "\uf007";
                } else if (tag_type == "Library") {
                    code = "\uf02d";
                } else if (tag_type == "Hotel") {
                    code = "\uf0f5";
                } else if (tag_type == "Fireextinguisher") {
                    code = "\uf134";
                } else if (tag_type == "Print") {
                    code = "\uf02f";
                } else if (tag_type == "Clock") {
                    code = "\uf017";
                } else if (tag_type == "Film") {
                    code = "\uf008";
                } else if (tag_type == "Music") {
                    code = "\uf001";
                } else if (tag_type == "Levelup") {
                    code = "\uf148";
                } else if (tag_type == "Leveldown") {
                    code = "\uf149";
                } else if (tag_type == "Trash") {
                    code = "\uf014";
                } else if (tag_type == "Home") {
                    code = "\uf015";
                } else if (tag_type == "Videocamera") {
                    code = "\uf03d";
                } else if (tag_type == "Circle") {
                    code = "\uf05a";
                } else if (tag_type == "Gift") {
                    code = "\uf06b";
                } else if (tag_type == "Exit") {
                    code = "\uf08b";
                } else if (tag_type == "Key") {
                    code = "\uf084";
                } else if (tag_type == "Camera") {
                    code = "\uf083";
                } else if (tag_type == "Phone") {
                    code = "\uf083";
                } else if (tag_type == "Creditcard") {
                    code = "\uf09d";
                } else if (tag_type == "Speaker") {
                    code = "\uf0a1";
                } else if (tag_type == "Powerroom") {
                    code = "\uf1e6";
                } else if (tag_type == "Toolset") {
                    code = "\uf0ad";
                } else if (tag_type == "Batteryroom") {
                    code = "\uf241";
                } else if (tag_type == "Computerroom") {
                    code = "\uf241";
                } else if (tag_type == "Kidsroom") {
                    code = "\uf113";
                } else if (tag_type == "TVroom") {
                    code = "\uf26c";
                } else {
                    code = "\uf007";
                }
            }

            return code;
        }
        return svc;
    }
})();