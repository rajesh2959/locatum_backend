(function () {
    'use strict';

    angular
        .module('app.layout')
        .controller('ftController', controller);

        controller.$inject = ['$uibModalInstance','dashboardDataService', 'venuedataservice', 'venuesession'];

        /* @ngInject */
        function controller($uibModalInstance,dashboardDataService, venuedataservice,  venuesession) {
           
            var vm = this;
           
            vm.sid = "";

            // vm.venueDetails = {};
            vm.tagsChartdata = [];
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
    
                        angular.forEach(res.floorVsTraffic, function (v, key) {
                            vm.floorVsTraffic.data.rows.push({
                                "c": [                               
                                    {
                                        "v": v.Status
                                    },
                                     {
                                        "v": v.activeTags
                                    },
                                    {
                                        "v": v.inactTags
                                    },
                                    {
                                        "v": v.idleTags
                                    }
                                                                                                    
                                ]
                            });
                           
                        });
                    }
                });
    
            };
    
            vm.floorVsTraffic = {
                    "type": "ColumnChart",
                    "cssStyle": "height:150px; width:100%;font-family:Roboto !important",
                    "data": {
                        "cols": [
                            {
                                "id": "gender",
                                "label": "idleTags",
                                "type": "string",
                                "p": {}
                            },
                            {
                                "id": "male-id",
                                "label": "ActiveTags",
                                "type": "number",
                                "p": {}
                            },
                            {
                                "id": "female-id",
                                "label": "InactiveTags",
                                "type": "number",
                                "p": {}
                            },
                             {
                                "id": "male-id",
                                "label": "IdleTags",
                                "type": "number",
                                "p": {}
                            },
                        ],
                        "rows": []
                    },
                    "options": {
                        titleTextStyle: { position: 'left', alignment: 'left', fontSize: 14, fontName: 'Roboto', fontWeight: 'bold' },
                        "isStacked": "true",
                        "fill": 20,
                        "displayExactValues": true,
                        "colors": ['#008000', '#FF8000', '#F13E2D'],
                        "legend": { position: 'bottom', alignment: 'center', width: '10px' },
                            hAxis: {
                            title: 'Floors',
                             textStyle : {
                                fontName:'Roboto',
                            fontSize: 17 // or the number you want
                                }
                              },
                            vAxis: {
                            title: 'Tag count',
                            fontName: 'Roboto',
                            },
                        chartArea: {
                            width: '80%',
                            right: 0,   // set this to adjust the legend width
                            left: 40,     // set this eventually, to adjust the left margin
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