(function () {
    'use strict';
    angular
        .module('app.users')
        .controller('addUserController', controller);
    controller.$inject = ['messagingService', 'notificationBarService', '$q', 'modalService', 'navigation', '$rootScope', 'userService', '$uibModalInstance', 'addUserService', 'session', 'id', '$linq', 'userobject', '$browser'];

    /* @ngInject */
    function controller(messagingService, notificationBarService, $q, modalService, navigation, $rootScope, userService, $uibModalInstance, addUserService, session, id, $linq, userobject, $browser) {
        var vm = this;
        vm.pageHeight = screen.height - 180;
        vm.user = { "email":""};
        vm.userid = id;
        // vm.user.role = role;
        vm.isCustomerRequired=false;
        // Need to store the existing attributes to find out if key ones are changed
        vm.userobject = {};
        if(vm.userid == 0){
            vm.user.email = "";
            vm.user.password = "";
        }

        vm.isUserPwdReset = false;
        vm.isAvailableCustomer = session && session.accessToken != "superadmin" ? false : true;
        // var role = session.role;
        vm.getRole = function () {
            userService.getRoleList()
                .then(function (result) {
                    vm.roleList = [];
                    angular.forEach(result, function (item) {
                        vm.roleList.push({ "key": item, "value": item });
                    });
                });
        };


        vm.validate = function(){
            vm.isCustomerRequired = true;         
            if (vm.user.role == 'superadmin'){
                vm.isCustomerRequired = false;
            }            
        };


        vm.getUser = function () {
        
            if (vm.userid != null && vm.userid != 0 && userobject != null && userobject != '') {
                vm.user = {};
                vm.user = userobject;
                // To check if email changed later
                vm.userobject = userobject;
                vm.emailtocheck = userobject.email;               
            }
        };

        vm.getCustomer = function () {
            if (vm.isAvailableCustomer) {
                addUserService.getCustomer().then(function (response) {
                    vm.customerList = [];
                    if (response && response.customer) {
                        vm.customerList = response.customer;
                    }
                    if(vm.userid == 0){
                        vm.user.email = "";
                        vm.user.password = "";
                    }
                });
            }
        };

        vm.save = function (frm) {
            messagingService.broadcastCheckFormValidatity();
            vm.validate();
            if (frm.$valid) {
                if (vm.user.email != "" && vm.user.email!=undefined && vm.user.email != vm.emailtocheck) {
                    // Check if the email is a duplicate
                    addUserService.checkEmailDuplicate(vm.user.email).then(function (res) {

                        if (res && res.data == "duplicate") {
                            var errormsg = "Email Id already exists. Please enter another Email Id."
                            notificationBarService.error(errormsg);                       
                        }
                        else if (res && res.data == "new"){
    
                            var payload = {};
                            payload.id = vm.userid == 0 ? null : vm.user.id;
                            payload.fname = vm.user.fname;
                            payload.lname = vm.user.lname;
                            payload.designation = vm.user.designation;
                            payload.email = vm.user.email;
                            payload.phone = vm.user.phone;
                            payload.role = vm.user.role;
                            payload.password = vm.user.password;
                            payload.isMailalert = vm.user.isMailalert;
                            payload.isSmsalert = vm.user.isSmsalert;
                            payload.solution = session.solution;
                            payload.customerName = !vm.isAvailableCustomer ? session.accessToken : vm.custName;
                            if(vm.user.role != "superadmin"){
                                payload.customerId = vm.user.customerId;
                            }
                            payload.loginCount = 0;
            
                            addUserService.saveAddUser(payload).then(function (response) {
                                if (response) {
                                    if (response.body && response.success) {
                                        notificationBarService.success(response.body);
                                    }
                                    else
                                        notificationBarService.error(response.body);
                                    $uibModalInstance.close("close");
                                }
                            });
                        }
                        else{
                            var errormsg = "Unable to validate email. Please try again.";
                            notificationBarService.error(errormsg);
                            //vm.user.email = "";
                        }
                    });
                } else {
                    // Save
                    var payload = {};
                    payload.id = vm.userid == 0 ? null : vm.user.id;
                    payload.fname = vm.user.fname;
                    payload.lname = vm.user.lname;
                    payload.designation = vm.user.designation;
                    payload.email = vm.user.email;
                    payload.phone = vm.user.phone;
                    payload.role = vm.user.role;
                    payload.password = vm.user.password;
                    payload.isMailalert = vm.user.isMailalert;
                    payload.isSmsalert = vm.user.isSmsalert;
                    payload.solution = session.solution;
                    payload.customerName = !vm.isAvailableCustomer ? session.accessToken : vm.custName;
                    if(vm.user.role != "superadmin"){
                        payload.customerId = vm.user.customerId;
                    }
                    
                    payload.loginCount = 0;
    
                    addUserService.saveAddUser(payload).then(function (response) {
                        if (response) {
                            if (response.body && response.success) {
                                notificationBarService.success(response.body);
                            }
                            else
                                notificationBarService.error(response.body);
                            $uibModalInstance.close("close");
                        }

                        else{
                            var errormsg = "Unable to validate email. Please try again.";
                            notificationBarService.error(errormsg);
                            //vm.user.email = "";
                        }
                    });
                
                }          
        }
    };

        vm.customerChange = function () {
            var customer = $linq.Enumerable().From(vm.customerList)
                .Where(function (x) {
                    return x.id == vm.user.customerId
                }).ToArray();
            if (customer && customer.length > 0)
                vm.custName = customer[0].custName;
            vm.validate();
        }

    if(vm.userid == 0){
        vm.cancel = function () {
            var message = "<p>Do you want to cancel creating this New User?</p>";
            modalService.questionModal('User Cancellation', message, true).result.then(function () {
                $uibModalInstance.close("cancel");
            });
        };
    }

    else if(vm.userid != 0){
            vm.cancel = function () {
                var message = "<p> Do you want to cancel updating this User?</p>";
                modalService.questionModal('User Cancellation', message, true).result.then(function () {
                    $uibModalInstance.close("cancel");
                });
            };
        }

    if(vm.userid == 0){
        vm.close = function () {
            var message = "<p>Do you want to cancel creating this User?</p>";
            modalService.questionModal('User Cancellation', message, true).result.then(function () {
                $uibModalInstance.close("close");
            });
        };
    }

    else if(vm.userid != 0){
        vm.close = function () {
            var message = "<p>Do you want to cancel updating this User?</p>";
            modalService.questionModal('User Cancellation', message, true).result.then(function () {
                $uibModalInstance.close("close");
            });
        };
    }

        vm.deleteUser = function () {
            modalService.confirmDelete('Are you sure you want to delete the user?').result.then(
                function () {
                    var user = {};
                    user.id = vm.userid;
                    addUserService.deleteUser(user).then(function (result) {
                        if (result && result.success) {
                            notificationBarService.success(result.body);
                            $uibModalInstance.close("close");
                            navigation.goToUsers();
                        }
                    });
                },
                function () {
                });
        }

        vm.resetPassword = function () {
            $uibModalInstance.close("close");
            vm.isUserPwdReset = true;
            modalService.resetPasswordModal(vm.userid,vm.isUserPwdReset).result.then(function (res) {
                modalService.addUserModal(vm.userid, userobject);
            }, function () {
            });
        }

        // Set Panel Size for the Popup - Add and Edit Scenarios.
        vm.setPanelSize = function() {
            if(vm.userid == 0){
                $(".modal-body").prop('height','830px');
            }else {
                $(".modal-body").prop('height','640px');
            }
        }

        function activate() {
            vm.getRole();
            vm.getUser();
            vm.getCustomer();
            vm.setPanelSize();
        }

        activate();

        /*
            Function to close the "Add User" popup automatically,
            when user clicks the back button
        */
        $browser.onUrlChange(function(newUrl) {
            $uibModalInstance.close("close");
        });


        return vm;
    }
})();