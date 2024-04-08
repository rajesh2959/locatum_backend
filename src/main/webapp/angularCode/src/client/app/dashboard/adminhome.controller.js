(function () {
    'use strict';
    angular
        .module('app.dashboard')
        .controller('AdminHomeController', controller);
    controller.$inject = ['dashboardDataService', '$q', 'venuedataservice', '$rootScope', 'environment', 'session', 'modalService', 'navigation', 'venuesession', '$linq', 'adminHomeService'];

    /* @ngInject */
    function controller(dashboardDataService, $q, venuedataservice, $rootScope, env, session, modalService, navigation, venuesession, $linq, adminHomeService) {
        var vm = this;
        vm.venuesession = venuesession;
        $rootScope.$emit('customEvent', "");
        vm.noCustomer = false;
        $rootScope.customerId = '';
        
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

        vm.loadServiceQueue = function (refresh) {
            vm.serviceQueue = [];
            vm.serviceQueue.push(dashboardDataService.getCustomerList());
        };

        vm.executeServiceQueue = function () {
            $q.all(vm.serviceQueue).then(function (serviceResponse) {
                vm.customerList = [];
                if (serviceResponse[0].customer) {
                    vm.allcustomerList = serviceResponse[0].customer;
                    vm.customerList = serviceResponse[0].customer;
                }
            });
        };

        vm.venueClick = function (customeritem) {
            if (customeritem) {
                session.create(customeritem.id, session.accessToken, session.role);
                $rootScope.customerName = customeritem.customerName;
                $rootScope.customerId = customeritem.id;
                session.customer = customeritem.customerName;
                navigation.goToVenue();
            }
        };

        vm.gatewayClick = function (customeritem) {
            if (customeritem) {
                session.create(customeritem.id, session.accessToken, session.role);
                $rootScope.customerName = customeritem.customerName;
                $rootScope.customerId = customeritem.id;
                session = customeritem.customerName;
                navigation.goToGateWay();
            }
        };

        vm.search = function () {
            vm.customerList = $linq.Enumerable().From(vm.allcustomerList)
                .Where(function (x) {
                    return x.customerName.toLowerCase().indexOf(vm.searchText.toLowerCase()) !== -1
                }).ToArray();

            if (vm.customerList.length == 0) {
                vm.noCustomer = true;
            }
            else {
                vm.noCustomer = false;
            }
        };

        vm.getData = function () {
            dashboardDataService.getCustomerList()
                .then(function (result) {
                    vm.customerAllList = result.customer;
                    vm.customerList = vm.customerAllList;
                });
        };

        vm.goToUser = function (id, customeritem) {
            modalService.addadminUserModal(id, customeritem).result.then(function (res) {
                vm.getData();
            }, function () {
            });
        }

        vm.updatelog = function (customeritem) {
            adminHomeService.updateLog(customeritem).then(function (res) {
                if(res){
                    console.log(res.data);
                    
                }
            });
        }

        vm.updatevpn = function (customeritem) {
            adminHomeService.updateVpn(customeritem).then(function (res) {
                if(res){
                    console.log(res.data);

                }
            });
        }

      vm.customerTooltip = function () {
        $('[data-toggle=tooltip]').hover(function(){
            // on mouseenter
            $(this).tooltip('show');
        }, function(){
            // on mouseleave
            $(this).tooltip('hide');
        });
      }

        function activate() {
            //vm.getData();
            vm.loadServiceQueue();
            vm.executeServiceQueue();
            vm.customerTooltip();
        }

        activate();
        return vm;
    }
})();