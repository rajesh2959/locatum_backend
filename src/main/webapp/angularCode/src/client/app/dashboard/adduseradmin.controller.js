(function () {
    'use strict';
    angular
        .module('app.dashboard')
        .controller('addUseradminController', controller);
    controller.$inject = ['notificationBarService', '$q', 'addUserService', 'dashboardDataService', '$rootScope', 'environment', 'session', 'modalService', 'navigation', 'venuesession', '$linq', '$uibModalInstance', 'id', 'customeritem' , '$http', '$browser'];

    /* @ngInject */
    function controller(notificationBarService, $q, addUserService, dashboardDataService, $rootScope, env, session, modalService, navigation, venuesession, $linq, $uibModalInstance, id, customeritem, $http, $browser) {
        var vm = this;
        vm.venuesession = venuesession;
        $rootScope.$emit('customEvent', "");
        vm.noCustomer = false;
        vm.user = [];
        vm.userList = [];
        vm.userid=id;
        vm.user.oauth="false";
        vm.oauth=false;
        vm.isExistingUser = true;
        vm.isNewUser = false;
        //Existing User details 
        vm.existingMailId = "";
        vm.existingFirstName = "";
        vm.existingLastName = "";
        vm.existingmobileNumber = "";
        vm.existingemail = "";
        vm.existingpassword = "";
        vm.existingcontactNumber = "";
        vm.existinginactivityMail = 'false';
         vm.existinginactivitySMS = 'false';        

        vm.customerid = customeritem == 'undefined' ? 0 : customeritem;
        if(vm.customerid != null && vm.customerid != 0) {
            vm.user = customeritem;
            getExistingUserDetails(customeritem);
            vm.user.serviceStartDate  = dateFormat(vm.user.serviceStartDate);
            vm.user.serviceExpiryDate = dateFormat(vm.user.serviceExpiryDate);
            vm.user.serviceDurationinMonths = parseInt(vm.user.serviceDurationinMonths,10);
            console.log(JSON.stringify(vm.user));
        }


        function getExistingUserDetails(customeritem){
            
            vm.existingFirstName        = customeritem.contactPerson;
            vm.existingLastName         = customeritem.contactPersonlname;
            vm.existingmobileNumber     = customeritem.mobileNumber;
            vm.existingMailId           = customeritem.email;
            vm.existingdesignation      = customeritem.designation;
            vm.existingcontactNumber    = customeritem.contactNumber;
            vm.existinginactivityMail   = customeritem.inactivityMail;
             vm.existinginactivitySMS   = customeritem.inactivitySMS;  

        }

        if (session.role == "appadmin") {
            vm.isVenue = true;
            vm.floorDetails = []
            vm.venueDetails = null
            vm.venuesession.create();
            navigation.goToVenue();
        }
        else if (session.role == "superadmin") {
            $rootScope.customerName = session.role;
        }

        vm.ok = function () {
            var message = "<p>The changes to the customer have not been saved yet. Are you sure you want to cancel the changes?</p>";
            modalService.questionModal('User Cancellation', message, true).result.then(function () {
                $uibModalInstance.close();
            });
        };
        
        vm.GenerateToken = function (){ 
            if(vm.user.oauth == 'true'){
               vm.newToken = false;
   
               vm.restToken = '';
               vm.jwtrestToken = '';
               // var tokenData = {name: vm.user.customerName, address: vm.user.address}
               addUserService.getresttoken({name: vm.user.customerName, address: vm.user.address}).then(function (response) {
                   console.log('Response', response);
                   if(response.restToken){
                       vm.user.restToken = response.restToken;      
                   }
                   if(response.jwtrestToken)                 
                       vm.user.jwtrestToken = response.jwtrestToken;                       
                   });
               
            }        
           }
           
           vm.AskGenerateNewToken = function(){
               vm.newToken = true;
           }
           
           vm.GenerateNewTokenClose = function(){
               vm.newToken = false;
           }
        
        vm.GenerateMqtt_token = function(){ 
            if(vm.user.oauth == 'true'){

                vm.newMqtt_token = false;

                vm.mqttToken = '';
                vm.jwtmqttToken = '';
                // var postData = {name: vm.user.customerName, postalCode: vm.user.postalCode};
                
                addUserService.getmqtttoken({name: vm.user.customerName, address: vm.user.postalCode}).then(function (response) {
                    console.log('Response', response);
                    if(response.mqttToken){
                        vm.user.mqttToken = response.mqttToken;      
                    }
                    if(response.jwtmqttToken)                 
                        vm.user.jwtmqttToken = response.jwtmqttToken;                       
                    });
                
             }        
            }
            
           
        vm.AskGenerateNewMqtt_token = function(){
            vm.newMqtt_token = true;
        }
        
        vm.GenerateNewMqtt_tokenClose = function(){
            vm.newMqtt_token = false;
        }

        vm.deleteCustomer = function () {
            modalService.confirmDelete('Are you sure you want to delete the Customer?').result.then(
                function () {
                    var payload = {};
                    payload.customerId = vm.userid;
                    //console.log("Customer id : "+vm.customerid+"  User id : "+vm.userid);
                    addUserService.deleteCustomer(payload).then(function (result) {
                        if (result && result.success) {
                            notificationBarService.success(result.body);
                        }
                    });
                    $uibModalInstance.close("close");
                    navigation.gotToAdminHome();
                },
                function () {
                });
        }

        /*
            Function to set the service expiry date of the customer,
            based on the service duration .
        */
        vm.setServiceExpiryDate = function() {
            var d = new Date(vm.user.serviceStartDate);
            var duration = parseInt(vm.user.serviceDurationinMonths);
            d.setMonth(d.getMonth()+duration);
            d.setDate(d.getDate()-1);
            vm.user.serviceExpiryDate = dateFormat(d);
        }
        
        /*
            Function to Format the date in "YYYY-MM-DD" pattern 
        */
        function dateFormat(date) {
            var d = new Date(date);
            var month =""+ (d.getMonth()+1);
            var date = ""+ d.getDate() ;
            if(month.length<2){
                month = '0'+ month;
            }
            if(date.length<2){
                date = '0'+ date;
            }
            var formattedDate = "";
            if(date != "NaN"){
                formattedDate =  d.getFullYear()+"-"+month+"-"+date;
            }
            return formattedDate;
        }

        /* 
            Function to Check whether the email Id is new  or duplicate
        */
        vm.checkemailDuplication = function(emailId) {
            if (emailId != "" && emailId != undefined && emailId != vm.existingMailId) {
                    
                    addUserService.checkEmailDuplicate(emailId).then(function (res) {
                        if (res && res.data == "duplicate") {
                            var errormsg = "Email Id already exists. Please enter another Email Id."
                            notificationBarService.error(errormsg); 
                            vm.user.email = "";
                        }

                    });
            }
        }
        /*
            In Edit customer, Based on the check box(Existing or new user),
            get the details of the user
        */
        vm.existingUser = function(){
            vm.isNewUser = !vm.isNewUser;
            vm.getUserDetails(null);
        }

        vm.newUser = function(){
            vm.isExistingUser = !vm.isExistingUser;
            vm.getUserDetails(null);
        }

        /*
            In edit customer, for contact person details
                1)If it is an existing user, Email field will be the only editable field as a dropdown and
                     other fields are auto filled based on the selected option.
                2)If it is a new user, all fields will be empty.
        */
        vm.getUserDetails = function(usermailId){
            
            if(vm.isNewUser && usermailId == null) {
                vm.user.contactPerson = "";
                vm.user.contactPersonlname = "";
                vm.user.mobileNumber = "";
                vm.user.email = "";
                vm.user.password = "";
                vm.user.cpassword = "";
                vm.user.designation ="";
                vm.user.contactNumber = "";
                vm.user.inactivityMail = 'false';
                vm.user.inactivitySMS = 'false';
            } else if (vm.isExistingUser && usermailId == null) {
                vm.user.contactPerson       = vm.existingFirstName;
                vm.user.contactPersonlname  = vm.existingLastName;
                vm.user.mobileNumber        = vm.existingmobileNumber;
                vm.user.email               = vm.existingMailId ;
                vm.user.designation         = vm.existingdesignation;
                vm.user.contactNumber       =  vm.existingcontactNumber;
                vm.user.inactivityMail      = vm.existinginactivityMail ;
                vm.user.inactivitySMS       = vm.existinginactivitySMS;
            } else if (vm.isExistingUser && usermailId != null){
                for(var i=0;i< vm.userList.length;i++) {
                    var user = vm.userList[i];
                    if(user.email == usermailId){
                            vm.user.contactPerson       = user.fname;
                            vm.user.contactPersonlname  = user.lname;
                            vm.user.mobileNumber        = user.phone;
                            vm.user.email               = user.email ;
                            vm.user.designation         = user.designation;
                            vm.user.contactNumber       = vm.existingcontactNumber;
                            vm.user.inactivityMail      = user.isMailalert ;
                            vm.user.inactivitySMS       = user.isSmsalert;

                            break;
                    }
                }   
            }

        }

        vm.save = function (frm) {
            if (frm.$valid) {

                var payload = {};

                var formData = new FormData();
                /*
                    Commented out code and dummy initializations related to 
                    "Type, Solution, Package and customer contact link(FB,Twitter,Linkedin)" need to be removed later
                */
                formData.append('logo', document.getElementById('file_logo').files[0]);
                //formData.append('background', document.getElementById('file_background').files[0]);
                formData.append('cid', vm.userid == 0 ? null : vm.userid);
        
                 if(document.getElementById('logofile')) {
                    vm.user.logofile = document.getElementById('logofile').value;
                 }                                     
                 /*if(document.getElementById('background')) {
                    vm.user.background = document.getElementById('background').value;
                 }*/
        
                 console.log('logo', document.getElementById('logofile').value);
                 //console.log('background', document.getElementById('background').value);
        
                payload.id = vm.userid == 0 ? null : vm.user.id;

                payload.qubercommAssist = vm.user.qubercommAssist;
                payload.threshold = vm.user.threshold;
                payload.tagcount = vm.user.tagcount;
                payload.oauth = vm.user.oauth;
                payload.customerName = vm.user.customerName;
                payload.address = vm.user.address;
                payload.city = vm.user.city;
                payload.state = vm.user.state;
                payload.country = vm.user.country;
                payload.venueType = "";
                payload.timezone = vm.user.timezone;
                payload.postalCode = vm.user.postalCode;
                payload.noOfGateway = null;
                payload.tagInact = vm.user.tagInact;
                payload.preferedUrlName = vm.user.customerName;
                payload.background = vm.user.background;
                payload.logofile = vm.user.logofile;
                payload.alexa = "false";
                payload.simulationStatus = vm.user.simulationStatus;
                payload.solution = "";
                payload.offerPackage = "";
                payload.mqttToken = vm.user.mqttToken;
                payload.restToken = vm.user.restToken;
                payload.jwtmqttToken = vm.user.jwtmqttToken;
                payload.jwtrestToken = vm.user.jwtrestToken;
                payload.serviceDurationinMonths = vm.user.serviceDurationinMonths;
                payload.serviceStartDate = vm.user.serviceStartDate;
                payload.serviceExpiryDate =vm.user.serviceExpiryDate;
                payload.contactPerson = vm.user.contactPerson;
                payload.contactPersonlname = vm.user.contactPersonlname;
                payload.mobileNumber = vm.user.mobileNumber;
                payload.email = vm.user.email;
                payload.password = vm.user.password;
                payload.confirmPassword = vm.user.cpassword;
                payload.designation = vm.user.designation;
                payload.contactNumber = vm.user.contactNumber;
                payload.inactivityMail = vm.user.inactivityMail;
                payload.inactivitySMS = vm.user.inactivitySMS;              
                payload.facebook = "";
                payload.twitter = "";
                payload.linkedin = "";
                payload.discover_link = "";
                payload.background_image = {};
                payload.get_logo = {};

                               
                // payload.status = false;                
                // payload.bleserverip = "104.154.36.63";
                // payload.logs = false;               
                // payload.logofile ="";
                // payload.background = "";              
                // payload.userAccId = vm.userAccId;             
                // payload.spotsIcon = "fa fa-mixcloud";
                // payload.spots ="/facesix/spots?cid=5998be75faf13e2ca463bbf1";
                // payload.findIcon = "fa fa-bluetooth";
                // payload.find = "/facesix/web/finder/device/list?cid=5998be75faf13e2ca463bbf1";
                // payload.totalVenue = "fa fa-map-marker";
                // payload.icon = "fa fa-map-marker";
                // payload.text = "Venue List";
                // payload.link ="facesix/web/site/list?cid=5998be75faf13e2ca463bbf1";

                addUserService.imgupload(formData).then(function (response) {
                    console.log('Image', response);
                    // var image = response.name;
                    // $rootScope.$broadcast('eventName', image);
        
                    if (response) {
                        if (vm.customerid == null && response.success) {
                            // var successmsg="File has been added successfully !!"
                            notificationBarService.success();
                        }
                        else if (vm.customerid != null && response.success) {
                            // var updatemsg="File has been updated successfully !!"
                            notificationBarService.success();
                        }
                        else
                            notificationBarService.error(response.body);
                            $uibModalInstance.close("close");
                    }
                });
               
               
                addUserService.saveadminuser(payload).then(function (response) {
                    if (response) {
                        if (vm.customerid == null && response.success) {
                            var successmsg="Customer has been added successfully !!"
                            notificationBarService.success(successmsg);
                        }
                        else if (vm.customerid != null && response.success) {
                            var updatemsg="Customer has been updated successfully !!"
                            notificationBarService.success(updatemsg);
                        }
                        else
                            notificationBarService.error(response.body);
                            $uibModalInstance.close("close");
                    }
                });
            }
        };

        function loadServiceQueue() {
            addUserService.gettimezonedetails().then(function (res) {
                vm.timezonelist = res.timezone;               
            });
        }

        function loadUserList() {
            if(id != 0){
                dashboardDataService.getUserList(id)
                .then(function (result) {
                        vm.userList = result.UserList;
                });
            }
        }

        function activate() {
            loadServiceQueue();
            loadUserList();
            // vm.executeServiceQueue();
        }

        /*
            Function to close the "Add Customer" popup automatically,
            when user clicks the back button
        */
        $browser.onUrlChange(function(newUrl) {
            $uibModalInstance.close("close");
        });

        activate();
        return vm;
    }

})();