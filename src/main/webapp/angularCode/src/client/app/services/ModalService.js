(function () {
    'use strict';
    angular
        .module('app')
        .factory('modalService', service);
    service.$inject = ['$uibModal'];

    function service($uibModal) {

        var svc = {};

        //prevent the backspace navigation issue, when user hit backspace
        //page nagvigates to previous screen
        $(document).bind('keydown', function (e) {
            if (e.which === 8 && (e.target.className.indexOf("modal") > -1 || e.target.parentNode.className.indexOf("modal") > -1)) {
                e.preventDefault();
            }
        });

        svc.openPdfReportModal = function (reportname, pdfurl) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/layout/pdfReportModal.html',
                controller: 'PdfReportModalController',
                controllerAs: 'vm',
                size: 'lg',
                backdrop: 'static',
                resolve: {
                    reportName: function () {
                        return reportname;
                    },
                    pdfUrl: function () {
                        return pdfurl;
                    }
                }
            });
        };

        svc.confirmDelete = function (confirmationMessage) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/layout/genericDeleteConfirmationModal.html',
                controller: 'GenericDeleteConfirmationModalController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    confirmationMessage: function () {
                        return confirmationMessage;
                    }
                }
            });
        };

        svc.questionModal = function (title, message, includeDangerHeader) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/layout/genericQuestionModal.html',
                controller: 'GenericQuestionModalController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    title: function () {
                        return title;
                    },
                    message: function () {
                        return message;
                    },
                    includeDangerHeader: function () {
                        return includeDangerHeader;
                    }
                }
            });
        };

        svc.deviceModal = function (uid) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/layout/deviceModal.html',
                controller: 'deviceModalController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    uid: function () {
                        return uid;
                    }
                }
            });
        };

        svc.messageModal = function (title, message) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/layout/genericMessageModal.html',
                controller: 'GenericMessageModalController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    title: function () {
                        return title;
                    },
                    message: function () {
                        return message;
                    }
                }
            });
        };

        svc.cssmModal = function (title, includeDangerHeader, clientName, siteName, stationName, actionName) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/cssm/cssmdetails.html',
                controller: 'cssmDetailsController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    title: function () {
                        return title;
                    },
                    includeDangerHeader: function () {
                        return includeDangerHeader;
                    },
                    clientName: function () {
                        return clientName;
                    },
                    siteName: function () {
                        return siteName;
                    },
                    stationName: function () {
                        return stationName;
                    },
                    actionName: function () {
                        return actionName;
                    }
                }
            });
        };

        svc.accessV2TransactionViewModal = function (title, includeDangerHeader, id, regNo, driverId) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/accessv2transaction/accessv2transactiondetails.html',
                controller: 'accessV2TransactionDetailsController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    title: function () {
                        return title;
                    },
                    includeDangerHeader: function () {
                        return includeDangerHeader;
                    },
                    id: function () {
                        return id;
                    },
                    regNo: function () {
                        return regNo;
                    },
                    driverId: function () {
                        return driverId;
                    }
                }
            });
        };

        svc.fineManagementViewModal = function (title, includeDangerHeader, id) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/finemanagement/finemanagementview.html',
                controller: 'fineManagementViewController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    title: function () {
                        return title;
                    },
                    includeDangerHeader: function () {
                        return includeDangerHeader;
                    },
                    id: function () {
                        return id;
                    }
                }
            });
        };

        svc.copyMobileSettings = function (title, includeDangerHeader, destinationCsspmId) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/managemobilesettings/copymobilesettings.html',
                controller: 'copyMobileSettingsController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'lg',
                resolve: {
                    title: function () {
                        return title;
                    },
                    includeDangerHeader: function () {
                        return includeDangerHeader;
                    },
                    destinationCsspmId: function () {
                        return destinationCsspmId;
                    }
                }
            });
        };

        svc.addUserModal = function (id, userobject) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/users/adduser.html',
                controller: 'addUserController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    id: function () {
                        return id;
                    },
                    userobject: function () {
                        return userobject;
                    }
                }
            });
        };


        svc.addadminUserModal = function (id, customeritem) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/dashboard/adduseradmin.html',
                controller: 'addUseradminController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    id: function () {
                        return id;
                    },
                    customeritem: function(){
                        return customeritem;
                    }
                    
                }
            });
        };

        svc.resetPasswordModal = function (id, userPwdReset) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/users/resetPassword.html',
                controller: 'resetPasswordController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    id: function () {
                        return id;
                    },
                    userPwdReset : function() {
                        return userPwdReset;
                    }
                }
            });
        };

        svc.visualconfirmModal = function (name, description) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/visualization/visualconfirmation.html',
                controller: 'visualconfirmationController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    name: function () {
                        return name;
                    },
                    description: function () {
                        return description;
                    }
                }
            });
        };


        svc.reportconfirmModal = function (name, description) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/reports/reportconfirmation.html',
                controller: 'reportconfirmationController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    name: function () {
                        return name;
                    },
                    description: function () {
                        return description;
                    }
                }
            });
        };


        svc.aboutModal = function () {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/users/about.html',
                controller: 'aboutController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                }
            });
        };

        svc.devicesModal = function (devices) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/layout/devices-modal.html',
                controller: 'devicesModalController',
                controllerAs: 'vm',
                size: 'lg',
                backdrop: 'static',
                windowClass: 'userFileWindow',
                resolve: {
                    devicesType: function () {
                        return devices;
                    },
                }
            });
        };
        svc.alertModal = function (res) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/dashboard/alert.html',
                controller: 'alertController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    res: function () {
                        return res;
                    },
                }
            });
        };

        svc.venueModal = function (res) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/venue/venuealert.html',
                controller: 'venuealertController',        
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    res: function () {
                        return res;
                    },
                }
            });
        };

        svc.ftModal = function (res) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/dashboard/floortrafficmodal.html',
                controller: 'ftController',        
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    res: function () {
                        return res;
                    },
                }
            });
        };

        svc.ctModal = function (res) {
            return $uibModal.open({
                animation: true,
                templateUrl: 'app/dashboard/connectedtagtypemodal.html',
                controller: 'ctController',        
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    res: function () {
                        return res;
                    },
                }
            });
        };




        return svc;
    }
})();
