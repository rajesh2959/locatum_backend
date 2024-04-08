(function () {
    'use strict';

    angular
        .module('app.layout')
        .controller('ctController', controller);

        controller.$inject = ['$uibModalInstance','dashboardDataService', 'venuedataservice','session', 'venuesession'];

        /* @ngInject */
        function controller($uibModalInstance,dashboardDataService, venuedataservice,session, venuesession) {
           
        	console.log("venue session" + JSON.stringify(venuesession))
        	
            var vm = this;
           
            vm.sid = "";

            // vm.venueDetails = {};
            vm.tagsChartdata = [];
            vm.connectedTagType = [];
            vm.inactiveTags = 0;
            vm.idleTags = 0;
            vm.activeTags = 0;
          
            if (venuesession.sid) {
                vm.sid = venuesession.sid;
            } 
        
    
            vm.ok = function () {
                $uibModalInstance.close();
            };
    
            function initializeData() {
                vm.tags = ['idleTags', 'activeTags', 'inactTags'];
                vm.labels = ['Idle Tags', 'Active Tags', 'Inact Tags'],
                vm.lineColors = ['#B176E9', '#1caf9a', '#F13E2D'];
            };
    
         
            function loadServiceQueue() {
                // venuedataservice.getVenueDetailById(vm.sid).then(function (res) {
                //     vm.venueDetails = res;
                //     if (vm.venueDetails) {
                //         if (vm.venueDetails.uid) {
                //             if (vm.venueDetails.uid.length > 21)
                //                 vm.venueDetails.newUid = vm.venueDetails.uid.substr(0, 20) + "...";
                //             else
                //                 vm.venueDetails.newUid = vm.venueDetails.uid.substr(0, 20)
                //         }
                //         else
                //             vm.venueDetails.newUid = "";
                //     }
                // });
    
                dashboardDataService.getTags(vm.sid, true).then(function (res) {
                	
                	console.log("res darta" + JSON.stringify(res));
                	
                    vm.tagsChartdata = res;
                    if (res) {
                        if (res.inactiveTags)
                            vm.inactiveTags = res.inactiveTags;
                        if (res.idleTags)
                            vm.idleTags = res.idleTags;
                        if (res.activeTags)
                            vm.activeTags = res.activeTags;
                        if (res.totalCheckedoutTags)
                            vm.totalCheckedoutTags = res.totalCheckedoutTags;
    
                        angular.forEach(res.connectedTagType, function (v, key) {
                            if (v.tagType !== 'Tag') {
                                vm.connectedTagType.data.rows.push({
                                    "c": [
                                        {
                                            "v": v.tagType
                                        },
                                        {
                                            "v": v.tagCount
                                        }
                                    ]
                                });
                            }
                        });
                    }
                });
    
            };
    
      
            vm.connectedTagType = {
                "type": "PieChart",
                "cssStyle": "height:350px; width:100%;font-family:Roboto !important;font-size:13px;padding-top: 55px;",
                "data": {
                    "cols": [
                        {
                            "id": "gender",
                            "label": "Gender",
                            "type": "string",
                            "p": {}
                        },
                        {
                            "id": "male-id",
                            "label": "Male",
                            "type": "number",
                            "p": {}
                        },
                        {
                            "id": "female-id",
                            "label": "Desktop",
                            "type": "number",
                            "p": {}
                        }
                    ],
                    "rows": []
                },
                "options": {
                    //  titleTextStyle: { position: 'left', alignment: 'left', fontSize: 14, fontName: 'Roboto', fontWeight: 'bold' },
                    //  title: 'Connected Tag Type',
                    "isStacked": "true",
                    is3D: true,
                    "fill": 20,
                    "displayExactValues": true,
                    "colors": ['#18a79d', '#f13e2d','#2980C6'],
                    "legend": { position: 'bottom', alignment: 'center', width: '100px' },
                    chartArea: {
                        width: '200%',
                        right: 0,   // set this to adjust the legend width
                        left: 0,     // set this eventually, to adjust the left margin
                    },
                },
                "formatters": {},
                "displayed": true
            };
    
           
               
            function activate() {
                initializeData();
                loadServiceQueue();
            }
    
            activate();
    
            return vm;
        }
    })();