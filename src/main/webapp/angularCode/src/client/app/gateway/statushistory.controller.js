﻿(function () {
    'use strict';
    angular
        .module('app.gateway')
        .controller('statushistoryController', controller);
    controller.$inject = ['statushistoryService','uid', 'navigation', 'SimpleListScreenViewModel', 'notificationBarService', 'modalService', '$linq', '$rootScope', 'session', 'environment', '$timeout', 'tagService'];
    /* @ngInject */

    function controller(statushistoryService, uid,navigation, SimpleListScreenViewModel, notificationBarService, modalService, $linq, $rootScope, session, environment, $timeout, tagService) {
        var vm = new SimpleListScreenViewModel();
        vm.dataOperationsGwayInfo = new SimpleListScreenViewModel();
        vm.dataOperationsGwayHistory = new SimpleListScreenViewModel();
        vm.dataOperationsGatewayAlerts = new SimpleListScreenViewModel();
        vm.selectedFilereCount = 4;
        vm.dataOperationsGatewayAlerts.dataOperations.paging.pageSize = vm.selectedFilereCount;
        vm.girdFilterList = [
            { "key": 5, "value": 5 },
            { "key": 10, "value": 10 },
            { "key": 25, "value": 25 },
            { "key": 100, "value": 100 }
        ];
        vm.userRole = session.role;
        
   

  vm.getGwayList = function (refresh) {
  statushistoryService.getGwayList(refresh, vm.dataOperationsGwayInfo.dataOperations, vm.dataOperationsGwayInfo.filterFn).then(function (result) {
                    
                    var filterCategories = [];
                    var category        = [];
                    var result = result.allData;

                    $.each(result, function(i, obj) { 
                        category.push(obj);
                    });
                    
                    $('#tagids').empty();   
                        $.each(category, function(index, optionValue) {  
                            var value = optionValue.dev_name +" - "+ optionValue.mac_address;
                            var selected = 'selected';
                            if(uid == optionValue.mac_address){
                                var div_data = "<option value=" + optionValue.mac_address + " "+selected+"  >"+ value + "</option>";
                                $(div_data).appendTo('#tagids');
                            }  else {
                                $('#tagids').append( $('<option></option>').val(optionValue.mac_address).html(value));
                            }                  
                                
                        });
                        
                    $('#tagids').multiselect('rebuild');  
                    
                    $.each($("#tagids"), function(){            
                        filterCategories.push($(this).val());   
                    });

                    var multipush =[];
                    var multival = $('#tagids').val();
                    var time = $('.timeInterval').val();
                    
                    $.each($("#tagids"), function(){            
                         multipush.push($(this).val());   
                    });
                    
                    vm.getGatewayInfos(multipush,time,true);
                                      
         
      });
};

 vm.getGatewayInfos = function (multipush,time, refresh) {

	 				if(multipush == undefined){
	 					var url = $(location).attr('href'),
	 				    parts = url.split("/"),
	 				    last_part = parts[parts.length-1];
	 					multipush = last_part;
	 				}
	 				
	 				if(time == undefined){
	 					time = "4h";
	 				}
	 
                    var multipush =[];
                    var multival = $('#tagids').val();
                    var time = $('.timeInterval').val();
                    
                    $.each($("#tagids"), function(){            
                         multipush.push($(this).val());   
                    });

  		statushistoryService.gatewayActiveTagTypesChartinfo(multipush, time ,vm.dataOperationsGatewayAlerts.dataOperations, refresh).then(function (result) {
  	  		vm.allGatewayAlerts = result.allData;
      		vm.pagedDataGatewayAlerts = result.pagedData;
      		vm.dataOperationsGatewayAlerts.fullCount = result.dataCount;
      		vm.dataOperationsGatewayAlerts.filteredCount = result.filteredDataCount;
 
   		});
};


vm.goToGatewayPage = function () {
    if (parseInt(vm.goToGatewayPageNumber) > vm.dataOperationsGatewayAlerts.totalPageCount) {
        vm.goToGatewayPageNumber = '';
    } else {
        vm.dataOperationsGatewayAlerts.dataOperations.paging.currentPage = vm.goToGatewayPageNumber;
        vm.getGatewayInfos();
        vm.goToGatewayPageNumber = '';
    }
};

        function activate(){
            vm.getGwayList(true);
        }

        activate();        

        return vm;
    }
})();