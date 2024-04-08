(function () {
    'use strict';
    angular
        .module('app.upgrade')
        .controller('upgradeController', controller);
    controller.$inject = ['sid','spid','macaddr','upgradedataservice','messagingService', 'notificationBarService', '$q', 'modalService', 'navigation', '$rootScope', '$scope','$timeout'];

    /* @ngInject */
    function controller(sid,spid,macaddr,upgradedataservice,messagingService, notificationBarService, $q, modalService, navigation, $rootScope, $scope,$timeout) {
       
        var vm = this;
        var target;
        var fileReader;        
        vm.pageHeight = screen.height - 180
        vm.upgradeDetails = {};
        vm.upgrade={};
        var filename;
             angular.element(document).ready(function () {
                    var de = "<option value = 'all'>ALL</option>";
                    $(de).appendTo('#venueid');
                 upgradedataservice.getVenueList()
                .then(function (result) {
                   $.each(result.site, function(i, obj) {                                                       
                                var floor_data = "<option value=" + obj.id + ">"+ obj.name + "</option>";                                                                                                                                           
                               $(floor_data).appendTo('#venueid');                        
                    });
                });

                 upgradedataservice.getCustList()
                .then(function (result) {
                   //console.log("result data>>>>>>>>>>>>>>>>" + JSON.stringify(result[0].customerName));
                   vm.upgradeDetails.customer = result[0].customerName;
                });
               
                if(macaddr == ""){
                    $scope.showall = false;
                } else {
                    $scope.showall = true;
                }

                vm.upgradeDetails.macaddr = macaddr
                vm.upgradeDetails.venue = sid
                vm.upgradeDetails.floor = spid
                
            });
           
            if(macaddr == ""){

             $('#venueid').on('change',function(){ //function to get the floor list
                $("#floorid").html('');
                   var cur_venue = $("#venueid").val();
                   var de = "<option value = 'all'>ALL</option>";
                   $(de).appendTo('#floorid');
                   upgradedataservice.getFloorList(cur_venue)
                .then(function (result) {
                   //console.log("result data" + JSON.stringify(result.portion));

                   $.each(result.portion, function(i, obj) {                                                       
                                var floor_data = "<option value=" + obj.id + ">"+ obj.name + "</option>";                                                                                                                                           
                               $(floor_data).appendTo('#floorid');                        
                    });                                        
                });

                if(cur_venue == "all"){
                    $('#floorid').css('pointer-events','none');
                    $('#locationid').css('pointer-events','none');
                } else {
                    $('#floorid').css('pointer-events','auto');
                    $('#locationid').css('pointer-events','auto');
                }

             })

               $('#floorid').on('change',function(){ //function to get the location list
                $("#locationid").html('');
                    var cur_venue = $("#venueid").val();
                    var cur_floor = $("#floorid").val();
                    var de = "<option value = 'all'>ALL</option>";
                    $(de).appendTo('#locationid');
                   upgradedataservice.getLocationList(cur_venue,cur_floor)
                .then(function (result) {
                   console.log("result data" + JSON.stringify(result));

                   $.each(result.location, function(i, obj) {                                                       
                                var loc_data = "<option value=" + obj.uid + ">"+ obj.name + "</option>";                                                                                                                                           
                               $(loc_data).appendTo('#locationid');                        
                    });                                     
                 });
               })                

            }
             
        vm.upgradeSubmit = function(upg){

               /* if(upg.$valid){
                    console.log("valid");
                } else {
                    console.log("In valid");
                }*/

                var custname = $('#customerid').val();
                var venuename = $('#venueid').val();
                var floorname = $('#floorid').val();
                var locationName = $('#locationid').val();
                var upgradeToType ;
                var binarytype = $('#upgradetype').val();
                var blob = null;
                var postData = {};
                postData.customer = vm.upgradeDetails.customer;
                postData.venue = vm.upgradeDetails.venue;
                postData.floor = vm.upgradeDetails.floor;                

                if(macaddr != ""){
                    locationName = $('#macid').val();
                    venuename = vm.upgradeDetails.venue;
                    floorname = vm.upgradeDetails.floor;
                    upgradeToType = "location";
                } else {
                    locationName = $('#locationid').val();
                    if(locationName == "? undefined:undefined ?"){
                        locationName ="all";
                    }
                }

                
                 var imageDataUR = "";
                if (fileReader.result.indexOf("base64") != -1) {
                    imageDataUR = fileReader.result; //contains the file read valule 
                }

                if(macaddr == ""){
                if(floorname == "all"){
                        upgradeToType = "venue";
                } else if(floorname != "all" && locationName =="all") {
                        upgradeToType = "floor";
                } else if(floorname != "all" && locationName !="all"){
                        upgradeToType = "location";
                }
                }

                var fd = new FormData(); //fd is gonna hold all the put along the multipart file
                var data = atob(imageDataUR.replace(/^.*?base64,/, ''));
                var asArray = new Uint8Array(data.length);
                for (var i = 0, len = data.length; i < len; ++i) {
                    asArray[i] = data.charCodeAt(i);
                }
                var fileType = getB64Type(imageDataUR);
                blob = new Blob([asArray.buffer], { type: fileType }); //blob stores the file type and content 
                fd.append('file', blob);
                fd.append('upgradeType', upgradeToType); 
                fd.append('binaryType', binarytype);
                fd.append('sid', venuename);
                fd.append('spid', floorname);
                fd.append('location', locationName);
                fd.append('cid', custname);
        
                     upgradedataservice.saveNetworkdevice(fd).then(function () {
                        notificationBarService.success('Upgraded Successfully');
                        navigation.goToUpgrade();
                    });
                           
        }
         
        function getB64Type($str) {
            return $str.split(';')[0].split(':')[1];
        }

        function initUploader() {
            vm.uploader = upgradedataservice.getFileUploaderInstance();
            vm.uploader.onErrorItem = function() {
                $timeout(function() { progressModel.close(); }, 10);
                modalService.warrningMessageModel('Error!', '<div class="text-center"><h4 class="text-danger"><i class="fa fa-times text-danger faa-pulse animated" aria-hidden="true"></i>Unexpected error occurred,please try again!</h4></div>', false).result.then(
                    function(item) {
                    });
            };

            vm.uploader.onAfterAddingFile = function (FileUploader) { //here file reading of the uploading file happens

                filename = FileUploader.file.name;
                $('#dispfilename').html(filename);
                fileReader = new FileReader();
                fileReader.readAsDataURL(FileUploader._file);
                fileReader.onload = function(fileReader) {
                    $timeout(function() {
                        target = fileReader.result;
                       // vm.upgradeDetails.selectedfile = target;
                    });
                };
            };
        }

        $('#selectedfile').on('change',function(){
                console.log("yay being called " + filename);
        })
            
  function activate() {
             initUploader();
        }

        activate();

        return vm;
    
    }

})();