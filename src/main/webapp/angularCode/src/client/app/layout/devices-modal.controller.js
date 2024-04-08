(function () {
    'use strict';

    angular
        .module('app.layout')
        .controller('devicesModalController', controller);

    controller.$inject = ['$uibModalInstance', 'devicesType', '$timeout', 'gatewayService', 'navigation', '$rootScope'];
    /* @ngInject */
    function controller($uibModalInstance, devicesType, $timeout, gatewayService, navigation, $rootScope) {

        var vm = this;
        vm.devicesType = devicesType;
        vm.devices = [];
        vm.devicesList = vm.devicesList = [{id: 1, label: "Server", isChecked: true, isVisible: false}, {id: 2, label: "Switch",isChecked: false, isVisible: false},
        {id: 3, label: "Ap", isChecked: true, isVisible: false},  {id: 4, label: "Sensor",isChecked: false, isVisible: false}, {id: 5, label: "RukusSensor", isChecked: true, isVisible: false}];
        vm.searchFilter = '';
        vm.customTexts = {buttonDefaultText: ''};
        vm.searchItems = "";
        // vm.filters = [ {id: 1, label: "Switch", isChecked: true, isVisible: false}, {id: 2, label: "Ap",isChecked: false, isVisible: false}, {id: 3, label: "Sensor", isChecked: true, isVisible: false}];
        vm.selectedItem =[];
        vm.smartButtonTextProviderModel = [];
        vm.dropdownsettings = {
            dynamicTitle: false,
            scrollable: false,
            scrollableHeight: '300px',
            closeOnBlur: true,
            displayProp: 'label',
            idProp: 'id',
            externalIdProp: 'id',
            enableSearch: false,
            showCheckAll: false,
            showUncheckAll: false,
            closeOnSelect: false,
            buttonClasses: 'btn btn-default',
            closeOnDeselect: false,
            groupByTextProvider: null,
            smartButtonMaxItems: 0,
        };
        //  vm.dropdownsettings = { enableSearch: false, scrollable: true, showCheckAll: false, showUncheckAll: false };
        vm.ok = function () {
            $uibModalInstance.close();
        };
         var suid = 0;

        vm.hoverIn = function(uid) {
                document.getElementById(uid+'li').style.background="#e2e2e2";
                document.getElementById(uid).style.display="block";        
                suid = uid;
                // console.log(suid, uid);
        }
        vm.hoverOut = function(uid) {
                document.getElementById(uid+'li').style.background="transparent";
                document.getElementById(uid).style.display="none";
        }
        
        vm.colorIn = function(id) {
                $timeout(function() {
                    var ele = document.getElementById(id);
                    if(ele) {
                        ele.style.color="#29b1a8";
                    }
                }, 0)            
            
        }
        vm.colorOut = function(id) {
            $timeout(function() {
                var ele = document.getElementById(id);
                if(ele) {
                    ele.style.color="#908888";
                }
            }, 0)
                
        }
        vm.selectFilter = {
            'onItemSelect': function (item) {
                console.log('item selected', item);
                handleSelectedItem();
            },
            'onItemDeselect': function (item) {
                console.log(item);
                //handleSelectAndUnselectedItem(item);
            }
        }
        function  handleSelectedItem() {

        } 
        function getDeviceList(spid) {
            gatewayService.networkDeviceList(spid).then(function(res) {
                if(res.length > 0) {
                    for(var i=0; i< res.length; i++) {
                     var source = res[i].source;
                     var imagePath = '';
                     if(source == "guest") {
                        imagePath = "../images/networkicons/" + "guestSensor_inactive.png";
                     } else {
                        imagePath = "../images/networkicons/" + res[i].typefs + "_inactive.png";
                     }
                     if(res[i].parent == "ble") {
                    if (res[i].source == "guest") {
                        imagePath = "../images/networkicons/guestSensor_inactive.png";
                       } else {
                        imagePath= "../images/networkicons/sensor_inactive.png";
                      } 
                     } else if(res[i].parent == "ap") {
                        imagePath = "../images/networkicons/ap_inactive.png";
                     }
                     res[i].imagePath = imagePath;
                     vm.devices.push(res[i]);
                    }
                }  
            })
        }
        vm.goToPage = function(uid, page) { 
          switch(page) {
            case 'gatewayinfo':
                  if(uid) {
                    $uibModalInstance.close();  
                    navigation.goToGatewayInfo(uid);
                  }
                break;  
            case 'upgrade':
               if(uid) {
                var sid = localStorage.getItem('sid');
                var cid = localStorage.getItem('cid');
                var macadr = uid;
                $uibModalInstance.close();
                navigation.goToUpgrade(sid, '0', macadr);
               }
               break;
            case 'gatewayedit':
               $uibModalInstance.close();
               navigation.goToAdddevice(0, uid, 1, 'gatewayedit');   
               break;             
          }
          
        }
        vm.searchFilterDevices = function(searchItem) {
            if(searchItem && searchItem != '' && searchItem != undefined) {
                for(var i=0; i< vm.devices.length; i++) {
                    if(searchItem == vm.devices[i].uid) {
                        document.getElementById(vm.devices[i].uid+'li').style.background="#e2e2e2";
                    } else {
                        document.getElementById(vm.devices[i].uid+'li').style.background="transparent";
                    }
                }
            }
        }
        vm.cloneDevice = function(device) {
            console.log(device);
            $('svg').children().each(function() {
                var $this = $(this);
               $this.children().each(function() {
                   var $that = $(this);
                   if(device.uid == $that.attr('dev-uid')) {
                    //var anchor = $('svg').append($this).attr("onclick", "return false;");
                    var newObject = $that.attr({
                        "xlink:href": device.imagePath,
                        "x": device.y,
                        "y": device.y,
                        "hyperLink": window.location.href,
                        "onclick": "return false;",
                        "width": device.width,
                        "height": device.height,
                        "isnewobject": false,
                        "type": device.typefs,
                        "ismovable":  false,
                        "parent": device.parent
                    })
                    $this.append(newObject);
                    $that.addClass('clones');
                    //var newObject = $this.
                    // $('svg').appendTo(newObject);
                    console.log($that.attr('dev-uid'), $this, $that);
                   } else {
                       $that.removeClass('clones');
                   }
               })
                // console.log(this)
            })
        }
        function activate() {   
         getDeviceList($rootScope.spid);   
        // searchFilterDevices();
        }
           activate();
        return vm;
    }
})();