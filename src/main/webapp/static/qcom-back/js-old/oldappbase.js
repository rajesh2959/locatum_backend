//var app = angular.module('myApp', ['ngRoute', 'chart.js', 'nvd3']);
var app = angular.module('myApp', ['ngRoute', 'chart.js']);

app.config(function ($routeProvider, $locationProvider) {
    $routeProvider
    .when('/topology', {
                templateUrl : '/facesix/web/site/portion/map?sid={{sid}}&spid={{id}}',
                controller  : 'topCtrl'
            })
    .when('/account', {
                templateUrl : '/facesix/web/account/profile',
                controller  : 'AccountCtrl'
            }) 
    .when('/floor', {
                templateUrl : '/facesix/web/site/portion/list?sid={{id}}',
                controller  : 'FloorCtrl'
            })
    .when('/addfloor', {
                templateUrl : '/facesix/web/site/portion/new?sid={{sid}}',
                controller  : 'FloorCtrl'
            })
    .when('/editfloor', {
                templateUrl : '/facesix/web/site/portion/open?spid={{id}}',
                controller  : 'FloorCtrl'
            })
    .when('/delfloor', {
                templateUrl : '/facesix/web/site/portion/delete?sid={{sid}}&spid={{id}}',
                controller  : 'FloorCtrl'
            })
    .when('/configfloor', {
                templateUrl : '/facesix/web/site/portion/nwcfg?sid={{sid}}&spid={{id}}&uid=?',
                controller  : 'topCtrl'
            })
    .when('/deviceconfig', {
                templateUrl : '/facesix/web/site/portion/devcfg?sid={{sid}}&spid={{id}}&uid=?',
                controller  : 'FloorCtrl'
            })
    .when('/nwconfig', {
                templateUrl : '/facesix/web/site/portion/nwcfg?sid={{sid}}&spid={{id}}&uid=?',
                controller  : 'topCtrl'
            })            
    .when('/ap', {
                templateUrl : '/facesix/web/site/portion/devboard?uid=1&spid=1',
                controller  : 'FloorCtrl'
            })
    .when('/switch', {
                templateUrl : '/facesix/web/site/portion/swiboard?uid=1&spid=1',
                controller  : 'FloorCtrl'
            })
    .when('/venue', {
                templateUrl : '/facesix/web/site/venue?sid={{id}}',
                controller  : 'VenueCtrl'
            })
    .when('/venuedashboard', {
                templateUrl : '/facesix/web/site/portion/dashview?sid={{id}}',
                controller  : 'VenueCtrl'
            })
    .when('/addvenue', {
                templateUrl : '/facesix/web/site/open?sid=',
                controller  : 'VenueCtrl1'
            })
    .when('/editvenue', {
                templateUrl : '/facesix/web/site/open?sid={{id}}',
                controller  : 'VenueCtrl1'
            })
    .when('/delvenue', {
                templateUrl : '/facesix/web/site/delete?sid={{id}}',
                controller  : 'VenueCtrl1'
            })                        
    .when('/dashboard', {
                templateUrl : 'Views/dashboard.html',
                controller  : 'DashboardCtrl'
            })
    .when('/drawfloor', {
                templateUrl : '/facesix/web/site/portion/draw?spid={{id}}',
                controller  : 'FloorCtrl'
            })            
    .when('/client', {
        templateUrl : '/facesix/client.html',
        controller  : 'ClientCtrl'
    })
    .otherwise({ redirectTo: '/client' });
});

app.directive('stringToNumber', function() {
  return {
    require: 'ngModel',
    link: function(scope, element, attrs, ngModel) {
      ngModel.$parsers.push(function(value) {
        return '' + value;
      });
      ngModel.$formatters.push(function(value) {
        return parseFloat(value);
      });
    }
  };
});
app.run(['$rootScope', '$window','$route', '$timeout', function($rootScope, $window, $route, $timeout) {
   
    // Logout Function
    
    $rootScope.Logout = function()
    {   
        //sessionStorage.removeItem(k);
        $window.sessionStorage.clear();  
        var link = "/facesix/goodbye"; 
        $window.location.href = link;
    }
    
    
}]);


app.controller('AccountCtrl', ['$scope','$http', function($scope, $http){
    console.log(screen.width);
    $scope.mobileValidate=/^[0-9]{10}$/;
    $scope.emailValidate=/^[a-z]+[a-z0-9._]+@[a-z]+\.[a-z.]{2,5}$/;
    $scope.UpdateProfile = function(profile){
        console.log(profile);
    }
    $scope.GetRoles = function(){
        $http.get('account.json').then(function successCallback(response){
            $scope.roles = response.data.roles;
            console.log($scope.profile);
        }, function errorCallback(response){
            console.log(response);
        });
    }
    $scope.GetRoles();
    $scope.GetProfileData = function(){
        $http.get('account.json').then(function successCallback(response){
            
            $scope.profile = response.data.profile;
            console.log($scope.profile);
        }, function errorCallback(response){
            console.log(response);
        });
    }
    
    $scope.GetProfileData();
    if(screen.width > 1023){
        $scope.myprofile = true;
    }
    $scope.openGP = function(){
        $scope.GPOpen = !$scope.GPOpen;
    }
    $scope.ProfileAllFalse = function(){
        $scope.myprofile = false;
        $scope.alluser = false;
        $scope.allroles = false;
        $scope.showallsupport = false;
        $scope.showalllicense = false;
        $scope.showallnotification = false;
        $scope.showProfileMenu = true;
        $scope.showProfileMenuAll = false;
        $scope.showallgp = false;
    }
    $scope.showmyprofile = function () {
        $scope.ProfileAllFalse();
        $scope.myprofile = true;
    }
    $scope.showalluser = function () {
        $scope.ProfileAllFalse();
        $scope.alluser = true;
    }
    $scope.showallrole = function () {
        $scope.ProfileAllFalse();
        $scope.allroles = true;
    }
    $scope.shownotification = function () {
        $scope.ProfileAllFalse();
        $scope.showallnotification = true;
    }
    $scope.showsupport = function () {
        $scope.ProfileAllFalse();
        $scope.showallsupport = true;
    }
    $scope.showlicence = function () {
        $scope.ProfileAllFalse();
        $scope.showalllicense = true;
    }
    $scope.showgp = function(){
        $scope.ProfileAllFalse();
        $scope.showallgp = true;
    }
    $scope.showAllProfileMenu = function(){
        $scope.showProfileMenuAll = !$scope.showProfileMenuAll; 
    }
    $scope.myprofiles = function(){
        $scope.ProfileAllFalse();
        $scope.showProfileMenu = false;
        $scope.showProfileMenuAll = false;
        
    }
    
    $scope.progressdata = {total:20, used:5}
}]);

app.controller('VenueCtrl', ['$scope', '$timeout','$compile', function($scope, $timeout, $compile){
    
    //Dummy Data
    
    $scope.viewVenue = true;
    $scope.venueDashboard = false;
    
    $scope.showVenueDashboard = function(){
        alert('ok');
        $scope.viewVenue = false;
        $scope.venueDashboard = true;
    }
    
    $scope.showAllVenue = function(){
        $scope.viewVenue = true;
        $scope.venueDashboard = false;
    }

    //Line Chart Sample Data;

    $scope.linedata = {
        label: ["10:54", "10:56", "10:58", "11:00", "11:02", "11:04", "11:06", "11:08", "11:10", "11:12"],
        series: ['Tx', 'Rx'],
        data: [
             [65, 59, 80, 81, 56, 55, 40, 45, 50, 60],
            [28, 48, 40, 58, 86, 47, 69, 40, 55, 65]
        ],
        colours: ["#03A9F4", "#2196F3"]
    };
    console.log($scope.linedata.data);


    $scope.options = {
        scaleShowGridLines: "rgba(0,0,0,0)",
    };


    $scope.health = {};
    $scope.health.color = "#66BB6A";
    $scope.health.label = "Connected";
    $scope.health.total = "20";

    $scope.labels = ["IOS", "Mac", "Win", "Android", "Others"];
    $scope.data = [7, 12, 83, 83, 83];
    //$scope.dougcolours = ["#27ae60", "#2980b9", "#f39c12"]
    $scope.dougcolours = ["#2196F3", "#4CAF50", "#FF5722", "#FFC107", "#e57373", "#FF5722", "#FFB300", "#F4511E", "#546E7A", "#3F51B5", "#9C27B0", "#e57373"];
    $scope.dougdate = {
        "typeOfDevices": [
            ["IOS", 7],
            ["Mac", 12],
            ["Win", 83],
            ["Android", 83],
            ["Others", 83]
        ]
    }


    $scope.sboptions = {
        chart: {
            type: 'sunburstChart',
            height: 450,
            //color: d3.scale.category20c(),
            duration: 250,
            sunburst: {
                groupColorByParent: true,
                showLabels: true
            }
        }
    };

    $scope.sbdata = [{
        "name": "QuberComm Technologies",
        "children": [
            {
                "name": "Floor 1",
                "children": [
                    {
                        "name": "Server 1",
                        "children": [
                            {
                                "name": "Switch 1",
                                "children": [
                                    {
                                        "name": "AP1",
                                        "children": [
                                            {
                                                "name": "Sensor 1",
                                                "size": 3812
                                            },
                                            {
                                                "name": "Sensor 2",
                                                "size": 6714
                                            },
                                            {
                                                "name": "Sensor 3",
                                                "size": 743
                                            }
                                        ]
                                     },
                                 ]
                                },
                            {
                                "name": "CommunityStructure",
                                "size": 3812
                            },
                            {
                                "name": "HierarchicalCluster",
                                "size": 6714
                            },
                            {
                                "name": "MergeEdge",
                                "size": 743
                            }
                            ]
                        },
                    {
                        "name": "graph",
                        "children": [
                            {
                                "name": "BetweennessCentrality",
                                "size": 3534
                            },
                            {
                                "name": "LinkDistance",
                                "size": 5731
                            },
                            {
                                "name": "MaxFlowMinCut",
                                "size": 7840
                            },
                            {
                                "name": "ShortestPaths",
                                "size": 5914
                            },
                            {
                                "name": "SpanningTree",
                                "size": 3416
                            }
                            ]
                        },
                    {
                        "name": "optimization",
                        "children": [
                            {
                                "name": "AspectRatioBanker",
                                "size": 7074
                            }
                            ]
                        }
                    ]
                },
            {
                "name": "Floor 2",
                "children": [
                    {
                        "name": "Server 1",
                        "children": [
                            {
                                "name": "Switch 1",
                                "children": [
                                    {
                                        "name": "AP1",
                                        "children": [
                                            {
                                                "name": "Sensor 1",
                                                "size": 3812
                                            },
                                            {
                                                "name": "Sensor 2",
                                                "size": 6714
                                            },
                                            {
                                                "name": "Sensor 3",
                                                "size": 743
                                            }
                                        ]
                                     },
                                 ]
                                },
                            {
                                "name": "CommunityStructure",
                                "size": 3812
                            },
                            {
                                "name": "HierarchicalCluster",
                                "size": 6714
                            },
                            {
                                "name": "MergeEdge",
                                "size": 743
                            }
                            ]
                        },
                    {
                        "name": "graph",
                        "children": [
                            {
                                "name": "BetweennessCentrality",
                                "size": 3534
                            },
                            {
                                "name": "LinkDistance",
                                "size": 5731
                            },
                            {
                                "name": "MaxFlowMinCut",
                                "size": 7840
                            },
                            {
                                "name": "ShortestPaths",
                                "size": 5914
                            },
                            {
                                "name": "SpanningTree",
                                "size": 3416
                            }
                            ]
                        },
                    {
                        "name": "optimization",
                        "children": [
                            {
                                "name": "AspectRatioBanker",
                                "size": 7074
                            }
                            ]
                        }
                    ]
                  },
            {
                "name": "Floor 3",
                "children": [
                    {
                        "name": "Server 1",
                        "children": [
                            {
                                "name": "Switch 1",
                                "children": [
                                    {
                                        "name": "AP1",
                                        "children": [
                                            {
                                                "name": "Sensor 1",
                                                "size": 3812
                                            },
                                            {
                                                "name": "Sensor 2",
                                                "size": 6714
                                            },
                                            {
                                                "name": "Sensor 3",
                                                "size": 743
                                            }
                                        ]
                                     },
                                 ]
                                },
                            {
                                "name": "CommunityStructure",
                                "size": 3812
                            },
                            {
                                "name": "HierarchicalCluster",
                                "size": 6714
                            },
                            {
                                "name": "MergeEdge",
                                "size": 743
                            }
                            ]
                        },
                    {
                        "name": "graph",
                        "children": [
                            {
                                "name": "BetweennessCentrality",
                                "size": 3534
                            },
                            {
                                "name": "LinkDistance",
                                "size": 5731
                            },
                            {
                                "name": "MaxFlowMinCut",
                                "size": 7840
                            },
                            {
                                "name": "ShortestPaths",
                                "size": 5914
                            },
                            {
                                "name": "SpanningTree",
                                "size": 3416
                            }
                            ]
                        },
                    {
                        "name": "optimization",
                        "children": [
                            {
                                "name": "AspectRatioBanker",
                                "size": 7074
                            }
                            ]
                        }
                    ]
                        },
            {
                "name": "Floor 4",
                "children": [
                    {
                        "name": "Server 1",
                        "children": [
                            {
                                "name": "Switch 1",
                                "children": [
                                    {
                                        "name": "AP1",
                                        "children": [
                                            {
                                                "name": "Sensor 1",
                                                "size": 3812
                                            },
                                            {
                                                "name": "Sensor 2",
                                                "size": 6714
                                            },
                                            {
                                                "name": "Sensor 3",
                                                "size": 743
                                            }
                                        ]
                                     },
                                 ]
                                },
                            {
                                "name": "CommunityStructure",
                                "size": 3812
                            },
                            {
                                "name": "HierarchicalCluster",
                                "size": 6714
                            },
                            {
                                "name": "MergeEdge",
                                "size": 743
                            }
                            ]
                        },
                    {
                        "name": "graph",
                        "children": [
                            {
                                "name": "BetweennessCentrality",
                                "size": 3534
                            },
                            {
                                "name": "LinkDistance",
                                "size": 5731
                            },
                            {
                                "name": "MaxFlowMinCut",
                                "size": 7840
                            },
                            {
                                "name": "ShortestPaths",
                                "size": 5914
                            },
                            {
                                "name": "SpanningTree",
                                "size": 3416
                            }
                            ]
                        },
                    {
                        "name": "optimization",
                        "children": [
                            {
                                "name": "AspectRatioBanker",
                                "size": 7074
                            }
                            ]
                        }
                    ]

                },
            {
                "name": "Floor 5",
                "children": [
                    {
                        "name": "Server 1",
                        "children": [
                            {
                                "name": "Switch 1",
                                "children": [
                                    {
                                        "name": "AP1",
                                        "children": [
                                            {
                                                "name": "Sensor 1",
                                                "size": 3812
                                            },
                                            {
                                                "name": "Sensor 2",
                                                "size": 6714
                                            },
                                            {
                                                "name": "Sensor 3",
                                                "size": 743
                                            }
                                        ]
                                     },
                                 ]
                                },
                            {
                                "name": "CommunityStructure",
                                "size": 3812
                            },
                            {
                                "name": "HierarchicalCluster",
                                "size": 6714
                            },
                            {
                                "name": "MergeEdge",
                                "size": 743
                            }
                            ]
                        },
                    {
                        "name": "graph",
                        "children": [
                            {
                                "name": "BetweennessCentrality",
                                "size": 3534
                            },
                            {
                                "name": "LinkDistance",
                                "size": 5731
                            },
                            {
                                "name": "MaxFlowMinCut",
                                "size": 7840
                            },
                            {
                                "name": "ShortestPaths",
                                "size": 5914
                            },
                            {
                                "name": "SpanningTree",
                                "size": 3416
                            }
                            ]
                        },
                    {
                        "name": "optimization",
                        "children": [
                            {
                                "name": "AspectRatioBanker",
                                "size": 7074
                            }
                            ]
                        }
                    ]
                  }

            ]
        }];

    $scope.value = {};
    $scope.value.data = "20";
    $scope.value.total = "50";
    $scope.value.label = "System Active";
    $scope.value.color = "#03A9F4";    
    $scope.ractivity = [
        {'title':'Qubercloud gateway wlan stats upload success', 'date':'2016-09-24 11:29:42.562'},
        {'title':'Qubercloud1 gateway wlan stats upload success', 'date':'2016-09-24 11:29:42.562'},
        {'title':'Qubercloud2 gateway wlan stats upload success', 'date':'2016-09-24 11:29:42.562'}
    ];
    //Dummy Data Ends
    $scope.RecentActivity = true;
    $scope.actprev = function(){
        $scope.RecentActivity = true;
        $scope.RecentAlert = false;  
    }
    $scope.actnext = function(){
        $scope.RecentActivity = false;
        $scope.RecentAlert = true;  
    }
    
}]);

app.controller('VenueCtrl1', ['$scope', '$rootScope', '$window', '$timeout', function ($scope, $rootScope, $window, $timeout){
    var currentMapCenter = null;  
        var mapOptions = {
    	   center: new google.maps.LatLng(0, 0),
    	   zoom: 2,
    	   minZoom: 1
        };
        var map = new google.maps.Map(document.getElementById('map-canvas'),mapOptions ); 
        //var marker = new google.maps.Marker({map: map});
        var marker = new google.maps.Marker({
                map: map,
                draggable:true,
                animation: google.maps.Animation.DROP
            });
        $scope.venuestep1 = true;
        $scope.searchtrue = true;
        $scope.venue = {};
        $scope.venuenext = function(){
            if($scope.venuestep2 == true){
                $scope.venuestep1 = false;
                $scope.venuestep2 = false;
                $scope.venuestep3 = true;
                $scope.nexttrue = false;
                $scope.savetrue = true;
            }
            if($scope.venuestep1 == true){
                
                $scope.venuestep1 = false;
                $scope.venuestep2 = true;
                $scope.venuestep3 = false;
                $scope.nexttrue = false;
                $scope.prevtrue = true;
                $scope.savetrue = true;
                 map.setCenter(center);
                $scope.searchtrue = false; 
            }
        }
        $scope.venueprev = function(){
            if($scope.venuestep3 == true){
                $scope.venuestep1 = false;
                $scope.venuestep2 = true;
                $scope.venuestep3 = false;
                $scope.prevtrue = false;
                $scope.savetrue = false;
                $scope.nexttrue = true;
            }
            if($scope.venuestep2 == true){
                $scope.venuestep1 = true;
                $scope.venuestep2 = false;
                $scope.venuestep3 = false;
                $scope.prevtrue = false;
                $scope.savetrue = false;
                $scope.nexttrue = true;
                $scope.searchtrue = true; 
            }
        }
        $scope.user = {'from': '', 'fromLat': '', 'fromLng' : ''};  
        var inputFrom = document.getElementById('from');
        var autocompleteFrom = new google.maps.places.Autocomplete(inputFrom);
        google.maps.event.addListener(autocompleteFrom, 'place_changed', function() {
            var place = autocompleteFrom.getPlace();
            if (!place.geometry) {
                // Inform the user that the place was not found and return.
                input.className = 'notfound';
                return;
            }
            if (place.geometry.viewport) {
                map.fitBounds(place.geometry.viewport);
                $scope.nexttrue = true;
            } else {
                map.setCenter(place.geometry.location);
                map.setZoom(17); // Why 17? Because it looks good.
                $scope.nexttrue = true;
            }
            
            marker.setIcon( /** @type {google.maps.Icon} */ ({
                url: place.icon,
                size: new google.maps.Size(71, 71),
                origin: new google.maps.Point(0, 0),
                anchor: new google.maps.Point(17, 34),
                scaledSize: new google.maps.Size(35, 35)
            }));
            marker.setPosition(place.geometry.location);
            marker.setVisible(true);
        
            console.log(place);
            $scope.place = place;
            
            $scope.venue.name = place.name;
            $scope.venue.address = place.formatted_address;
            $scope.venue.latitude = place.geometry.location.lat();
            $scope.venue.longitude = place.geometry.location.lng();
            console.log($scope.venue);
    //        $scope.user.fromLat = place.geometry.location.lat();
    //        $scope.user.fromLng = place.geometry.location.lng();
            //$scope.user.from = place.formatted_address;
            $scope.$apply();
        });
        google.maps.event.addListener(map, 'click', function(event){
            marker.setPosition(event.latLng);
            var latitude = event.latLng.lat();
            var longitude = event.latLng.lng();
            
            $scope.venue.latitude = latitude;
            $scope.venue.longitude = longitude;
            
        });
        google.maps.event.addListener(marker, 'dragend', function(event){
            
            var latitude = event.latLng.lat();
            var longitude = event.latLng.lng();
            $scope.venue.latitude = latitude;
            $scope.venue.longitude = longitude;
            
        });
        var center;
        function calculateCenter() {
          center = map.getCenter();
            console.log(center);
        }
        google.maps.event.addDomListener(map, 'idle', function() {
          calculateCenter();
        });
        google.maps.event.addDomListener(window, 'resize', function() {
          map.setCenter(center);
        });
}]);

app.controller('FloorCtrl', ['$scope', '$rootScope', '$http',  function ($scope, $rootScope, $http) {
    
    $scope.floorlist = true;
    $scope.floordashboard = false;
    
    $scope.showFloorList = function(){
        $scope.floorlist = true;
        $scope.floordashboard = false;
    }
    $scope.showFloorDashboard = function(){
        $scope.floorlist = false;
        $scope.floordashboard = true;
    }
    
    $scope.barlabels = ['2006', '2007', '2008', '2009', '2010', '2011', '2012', '2013', '2014', '2015','2016' ,'2017', '2018'];
  $scope.barseries = ['Series A'];

  $scope.bardata = [
    [65, 59, 80, 81, 56, 55, 40, 30, 35, 60, 70, 20, 30]
  ];
    
    
    //$scope.floordata = {};
    $scope.value = {};
    $scope.value.data = "20";
    $scope.value.total = "50";
    $scope.value.label = "System Active";
    $scope.value.color = "#03A9F4";
    $scope.linedata = {
        label: ["10:54", "10:56", "10:58", "11:00", "11:02", "11:04", "11:06", "11:08", "11:10", "11:12"],
        series: ['Tx', 'Rx'],
        data: [
             [65, 59, 80, 81, 56, 55, 40, 45, 50, 60],
            [28, 48, 40, 58, 86, 47, 69, 40, 55, 65]
        ],
        colours: ["#03A9F4", "#2196F3"]
    };
    console.log($scope.linedata.data);


    $scope.options = {
        scaleShowGridLines: "rgba(0,0,0,0)",
    };

    $scope.baroptions = {
        scaleShowLabels : false,
        scaleShowHorizontalLines:false,
        showScale:false
    }

    $scope.health = {};
    $scope.health.color = "#66BB6A";
    $scope.health.label = "Connected";
    $scope.health.total = "20";

    $scope.labels = ["IOS", "Mac", "Win", "Android", "Others"];
    $scope.data = [7, 12, 83, 83, 83];
    //$scope.dougcolours = ["#27ae60", "#2980b9", "#f39c12"]
    $scope.dougcolours = ["#2196F3", "#4CAF50", "#FF5722", "#FFC107", "#e57373", "#FF5722", "#FFB300", "#F4511E", "#546E7A", "#3F51B5", "#9C27B0", "#e57373"];
    $scope.barcolours = ["#4CAF50"]
    $scope.dougdate = {
        "typeOfDevices": [
            ["IOS", 7],
            ["Mac", 12],
            ["Win", 83],
            ["Android", 83],
            ["Others", 83]
        ]
    }
    
//    $scope.getTools = function(){
//        
//        data.getTools().then(function successCallback(response){
//            console.log(response);
//            $scope.floordata = response.data.toolsItem;
//        }, function errorCallback(response){
//            console.log(response);
//        });
//    }
//    $scope.getTools();
   // $scope.floordata = $scope.getTools();
    
    
    

//    $scope.createfloor = function (floor) {
//        console.log(floor);
//        var datas = floor;
//        data.CreateFloor(datas).then(function successCallback(response) {
//            if (response.data.Status == true) {
//                $rootScope.SuccessMessage(response);
//            } else {
//                $rootScope.ErrorMessage(response);
//            }
//        }, function errorCallback(response) {
//            $rootScope.errorMethod(response);
//        });
//    }
//    $scope.getallfloor = function () {
//        data.GetAllFloor.then(function successCallback(response) {
//            if (response.data.Status == true) {
//                $rootScope.SuccessMessage(response);
//            } else {
//                $rootScope.ErrorMessage(response);
//            }
//        }, function errorCallback(response) {
//            $rootScope.errorMethod(response);
//        });
//    }
//    $scope.getfloor = function (floorlist) {
//        var datas = floorlist;
//        data.GetFloor(datas).then(function successCallback(response) {
//            if (response.data.Status == true) {
//                $rootScope.SuccessMessage(response);
//            } else {
//                $rootScope.ErrorMessage(response);
//            }
//        }, function errorCallback(response) {
//            $rootScope.errorMethod(response);
//        });
//    }
//    $scope.deletefloor = function (floorlist) {
//        var datas = venuelist;
//        data.DeleteVenue(datas).then(function successCallback(response) {
//            if (response.data.Status == true) {
//                $rootScope.SuccessMessage(response);
//                $scope.getallvenue();
//            } else {
//                $rootScope.ErrorMessage(response);
//            }
//        }, function errorCallback(response) {
//            $rootScope.errorMethod(response);
//        });
//    }

    $scope.floordata = {
        img: "/facesix/static/qcom/img/ground.png",
        device: [
            {
                dev_type: "server",
                x: 200,
                y: 300
            },
            {
                dev_type: "server",
                x: 250,
                y: 350
            },
            {
                dev_type: "ap",
                x: 270,
                y: 380
            }
        ]
    };
    $scope.tools = {
    "toolsItem": {
        "items": [
            {
                "type": "fillRect",
                "x": 1,
                "y": 35,
                "w": 35,
                "h": 35,
                "id": "undo",
                "child": {
                    "preset": "draw",
                    "type": "undo",
                    "img": "/facesix/static/qcom/img/tools/undo.png",
                    "x": 10,
                    "y": 43,
                    "sx": 10,
                    "sy": 60
                }
            },
            {
                "type": "fillRect",
                "x": 39,
                "y": 35,
                "w": 35,
                "h": 35,
                "id": "redo",
                "child": {
                    "preset": "draw",
                    "type": "redo",
                    "img": "/facesix/static/qcom/img/tools/redo.png",
                    "x": 46.5,
                    "y": 42.5,
                    "w": 24,
                    "h": 24
                }     
            },
            {
                "type": "fillRect",
                "x": 1,
                "y": 72,
                "w": 35,
                "h": 35,
                "id": "line",
                "child": {
                    "preset": "draw",
                    "type": "line",
                    "img": "/facesix/static/qcom/img/tools/line.png",
                    "x": 10.5,
                    "y": 80.5,
                    "w": 20,
                    "h": 20
                }
            },
            {
                "type": "fillRect",
                "x": 39,
                "y": 72,
                "w": 35,
                "h": 35,
                "id": "rect",
                "child": {
                    "preset": "draw",
                    "type": "rect",
                    "img": "/facesix/static/qcom/img/tools/rectangle.png",
                    "x": 45.5,
                    "y": 80.5,
                    "w": 20,
                    "h": 20
                }       
            },
            {
                "type": "fillRect",
                "x": 1,
                "y": 109,
                "w": 35,
                "h": 35,
                "id": "circle",
                "child": {
                    "preset": "draw",
                    "type": "circle",
                    "img": "/facesix/static/qcom/img/tools/circle.png",
                    "x": 10.5,
                    "y": 120.5,
                    "w": 24,
                    "h": 24
                }
            },
            {
                "type": "fillRect",
                "x": 39,
                "y": 109,
                "w": 35,
                "h": 35,
                "id": "lline",
                "child": {
                    "preset": "draw",
                    "type": "lline",
                    "img": "/facesix/static/qcom/img/tools/lline.png",
                    "x": 46.5,
                    "y": 120.5,
                    "w": 20,
                    "h": 20
                }
            }, 
            {
                "type": "fillRect",
                "x": 1,
                "y": 146,
                "w": 35,
                "h": 35,
                "id": "doubledoor",
                "child": {
                    "preset": "image",
                    "type": "doubledoor",
                    "img": "/facesix/static/qcom/img/tools/doubledoor.png",
                    "x": 10.5,
                    "y": 155.5,
                    "w": 20,
                    "h": 20
                }
            },
            {
                "type": "fillRect",
                "x": 39,
                "y": 146,
                "w": 35,
                "h": 35,
                "id": "singledoor",
                "child": {
                    "preset": "image",
                    "type": "singledoor",
                    "img": "/facesix/static/qcom/img/tools/singledoor.png",
                    "x": 46.5,
                    "y": 155.5,
                    "w": 20,
                    "h": 20
                }
            }, 
            {
                "type": "fillRect",
                "x": 1,
                "y": 183,
                "w": 35,
                "h": 35,
                "id": "ofcdesk",
                "child": {
                    "preset": "image",
                    "type": "ofcdesk",
                    "img": "/facesix/static/qcom/img/tools/ofcdesk.png",
                    "x": 10.5,
                    "y": 190.5,
                    "w": 20,
                    "h": 20
                }
            },
            {
                "type": "fillRect",
                "x": 39,
                "y": 183,
                "w": 35,
                "h": 35,
                "id": "table",
                "child": {
                    "preset": "image",
                    "type": "table",
                    "img": "/facesix/static/qcom/img/tools/table.png",
                    "x": 46.5,
                    "y": 190.5,
                    "w": 20,
                    "h": 20
                }
            },
            {
                "type": "fillRect",
                "x": 1,
                "y": 223,
                "w": 35,
                "h": 35,
                "id": "threesetshoba",
                "child": {
                    "preset": "image",
                    "type": "threesetshoba",
                    "img": "/facesix/static/qcom/img/tools/threesetshoba.png",
                    "x": 10.5,
                    "y": 230.5,
                    "w": 20,
                    "h": 20
                }
            },
            {
                "type": "fillRect",
                "x": 39,
                "y": 223,
                "w": 35,
                "h": 35,
                "id": "roundtable",
                "child": {
                    "preset": "image",
                    "type": "roundtable",
                    "img": "/facesix/static/qcom/img/tools/roundtable.png",
                    "x": 46.5,
                    "y": 230.5,
                    "w": 20,
                    "h": 20
                }
            }
        ]
    }
}; 
    $scope.scale = {"length" : 200, "breadth": 40, "unit":"m"};
        //$scope.$apply();

}]);

app.controller('ClientCtrl', ['$scope', '$rootScope', '$http', '$filter', function($scope, $rootScope, $http, $filter){
    $scope.newCus = {};
    $scope.$watch('newCus.serviceDuration', function (newDate) {
        
        var d = new Date($scope.newCus.serviceStartdate); 
        d.setMonth(d.getMonth() + $scope.newCus.serviceDuration); 
        d.setDate(d.getDate() - 1); 
        $scope.newCus.serviceExpirydate = d; 
        console.log($scope.newCus.serviceExpirydate);
    });
    $scope.accounts = [];
    $scope.CreateOpen = false;
    $http.get('/facesix/static/qcom/sample.json').then(function successCallback(response){
        $scope.accounts = response.data.account;
        console.log($scope.accounts);
    }, function errorCallback(response){
       
    });
    $scope.singleAccount = function(x){
          console.log(x);
      }
    $scope.newCustomerOpen = function(){
        $scope.CreateOpen = !$scope.CreateOpen;
        $scope.CreateVenue = false;
        $scope.EditOpen = false;
        
        $scope.CusStep5();
        $scope.CusStep4();
        $scope.CusStep3();
        $scope.CusStep2();
        $scope.CusStep1();
        $scope.stepCus5 = false;
        $scope.stepCus6 = false;
        $scope.stepCus7 = false;
    }
//    $scope.OpenVenue = function(){
//        $scope.CreateOpen = false;
//        window.location.href('addvenue');
//    }
    $scope.EditCus = function(){
        $scope.CreateOpen = false;
        
        $scope.CusStep5();
        $scope.CusStep4();
        $scope.CusStep3();
        $scope.CusStep2();
        $scope.CusStep1();
        $scope.stepCus5 = false;
        $scope.stepCus6 = false;
        $scope.EditOpen = !$scope.EditOpen;
    }
    $scope.customerstep1 = true;
    $scope.stepCus1 = true;
    $scope.CusStep1 = function(){
        $scope.customerstep1 = true;
        $scope.stepCus1 = true;
        $scope.stepCus2 = false;
        $scope.customerstep2 = false;
        
    }
    $scope.CusStep2 = function(){
        $scope.customerstep1 = true;
        $scope.stepCus1 = false;
        $scope.stepCus2 = true;
        $scope.stepCus3 = false;
        $scope.customerstep2 = true;
        $scope.customerstep3 = false;
        
    }
    $scope.CusStep3 = function(){
        $scope.customerstep1 = true;
        $scope.stepCus2 = false;
        $scope.stepCus3 = true;
        $scope.stepCus4 = false;
        $scope.customerstep2 = true;
        $scope.customerstep3 = true;
        $scope.customerstep4 = false;
        
    }
    $scope.CusStep4 = function(){
        $scope.customerstep1 = true;
        $scope.stepCus2 = false;
        $scope.stepCus3 = false;
         $scope.stepCus4 = true;
        $scope.customerstep2 = true;
        $scope.customerstep3 = true;
        $scope.customerstep4 = true;
        
    }
    $scope.CusStep5 = function(){
        $scope.customerstep1 = true;
        $scope.stepCus2 = false;
        $scope.stepCus3 = false;
         $scope.stepCus4 = false;
         $scope.stepCus5 = true;
        $scope.customerstep2 = true;
        $scope.customerstep3 = true;
        $scope.customerstep4 = true;
        
    }
    
    $scope.generateemail = function()
    {
        $scope.customerstep1 = true;
        $scope.stepCus2 = false;
        $scope.stepCus3 = false;
         $scope.stepCus4 = false;
         $scope.stepCus5 = false;
         $scope.stepCus6 = true;
        $scope.customerstep2 = true;
        $scope.customerstep3 = true;
        $scope.customerstep4 = true;
    }
    
    
    $scope.sendMail = function()
    {
        $scope.customerstep1 = true;
        $scope.stepCus2 = false;
        $scope.stepCus3 = false;
         $scope.stepCus4 = false;
         $scope.stepCus5 = false;
         $scope.stepCus6 = false;
         $scope.stepCus7 = true;
        $scope.customerstep2 = true;
        $scope.customerstep3 = true;
        $scope.customerstep4 = true;
    }
    
    
}]);

app.directive('open', function(){
    function link(scope, element, attrs){
        var ele = element[0];
        var ele2 = document.getElementById(scope.open);
        
        ele.addEventListener('click', function(e){
            //console.log(ele.classList.contains("open"));
            if(ele.classList.contains("open")){
                ele.classList.remove("open");
                ele2.classList.remove("div-open");
            }
            else{
                ele.classList.add("open");
                ele2.classList.add("div-open");
            }
        });
        ele2.addEventListener('click', function(e){
            //console.log(ele.classList.contains("open"));
            if(ele.classList.contains("open")){
                ele.classList.remove("open");
                ele2.classList.remove("div-open");
            }
            else{
                ele.classList.add("open");
                ele2.classList.add("div-open");
            }
        });  
    }
     return {
        restrict: 'EA',
         scope:{
             open:"@"
         },
        link: link
    };
});

app.directive('barprogress', function(){
    function link(scope, element, attrs){
        scope.$watch('data', function(value){
            scope.percentage = value.used/value.total * 100;
        });
    }
    return{
        restrict: 'EA',
        replace: true,
        scope: {
            data:'='
                },
        link: link,
        template:'<div class="bar-progress"><div class="icon"><i class="fa fa-wifi"></i></div><div class="back"><div class="front" style="width:{{percentage}}%"></div></div><div class="value tc">{{data.used}}/{{data.total}}</div></div>'      
    };
});

app.directive('roundprogress', function($timeout){
    function link(scope, element, attrs){
        //alert('ok');
        var ctx = element[0].getContext('2d');
        ctx.clearRect(0,0,element[0].width,element[0].height);
        
        function hexToRgb (hex) {
            var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
            return result ? {
                r: parseInt(result[1], 16),
                g: parseInt(result[2], 16),
                b: parseInt(result[3], 16)
            } : null;
        }
        
        function rgba (color, alpha) {
            return 'rgba(' + color.r + ','+ color.g +','+color.b+','+ alpha +')';
        }
        
//        if(!(scope.percent == null || scope.percent == undefined)){
//                    var percent = scope.percent;
//                } else{var percent = 0;}
        if(!(scope.data == null || scope.data == undefined)){
                    var data = scope.data;
                } else{var data = 0;}
        
        if(!(scope.label == null || scope.label == undefined)){
                    var label = scope.label;
                } else{var label = "";}
                
        if(!(scope.size == null || scope.size == undefined)){
                    var size = scope.size;
                } else{var size = scope.$eval(attrs.width);}
        
        if(!(scope.color == null || scope.color == undefined)){
                    var color = hexToRgb(scope.color);
                } else{var color = "#4CAF50";}
                
        if(!(scope.linewidth == null || scope.linewidth == undefined)){
                    var lineWidth = scope.linewidth;
                } else{var lineWidth = 5;}
        
        if(!(scope.total == null || scope.total == undefined)){
                    var total = scope.total;
                } else{var total = 100;}
        
        var percent = data / total * 100;
        
        var radius = (size - lineWidth) / 2;
        var curPerc = 0;
        var full = 1;      
        var drawCircle = function(color, lineWidth, full) {
                ctx.clearRect(0,0,element[0].width,element[0].height);
                ctx.beginPath();
                ctx.arc(size/2, size/2, radius, -1.5, (Math.PI * 2 * full) - 1.5, false);
                ctx.strokeStyle = color;
                ctx.lineCap = 'round'; // butt, round or square
                ctx.lineWidth = lineWidth;
                ctx.stroke();
        };
        
        function getcolor(color){
                    return{
                        strokeColor: rgba(color, 0.7)
                    }
                }
            var getcolor = getcolor(color);
        
        var drawCircle1 = function(color, lineWidth, curPerc, percent1) {
                
                var endangle = percent1 / 100;
                //console.log(endangle);
                ctx.clearRect(0,0,element[0].width,element[0].height);
                ctx.imageSmoothingEnabled=true;
                drawCircle('#efefef', lineWidth, 100 / 100);  
                if(!(endangle == 0)){
                    ctx.beginPath();
                    ctx.arc(size/2, size/2, radius, -1.5, (Math.PI * 2 * curPerc) - 1.5, false);
                    ctx.strokeStyle = getcolor.strokeColor;
                    ctx.fillStyle = getcolor.strokeColor;
                    //console.log(ctx.strokeStyle)
                    ctx.lineCap = 'round'; // butt, round or square
                    ctx.lineWidth = lineWidth;
                    ctx.stroke(); 
                    if (curPerc < endangle) {
                        var curPerc = curPerc + 0.01;
                        $timeout(function(){drawCircle1(color, lineWidth, curPerc, percent1);}, 20);
                    }
                }
                ctx.textAlign ='center';
                ctx.font = "18px Arial";
                ctx.fillStyle = "grey";
                ctx.fillText(data, size/2 , size/2);
                ctx.font = "10px Arial";
                ctx.fillText(label, size/2, (size/2) +13);
                
        };
        scope.$watch('data', function(value){
            if(!(value == null || value == undefined || value =="")){
                if(value >=100){var percent1 = 100;}
                else{var percent1 = value / total *100;}   
            } else{var percent1 = 0;}
            drawCircle1(color, lineWidth, curPerc / 100, percent1);
        });
    }
    return{
    restrict: 'EA',
    replace: true,
    scope: {    
                data:'=',
                percent:'=',
                label:'=', 
                size:'=',
                linewidth :'=',
                color:'=',
                total:'='
              },
    link: link
        
    };
});

app.directive('timer', function($timeout){
    function link(scope, element, attrs){   
        function link(){
            //console.log(scope.timer);
           var elm = element[0].children;
            var length = elm.length;
            console.log(length);
            elm[0].classList.add('active');
            setInterval(function () {
                for(var i=0; i < length; i++){
                    //console.log(i);
                    var activeclass = angular.element(elm[i]).hasClass('active');
                    //console.log(activeclass);
                   if(activeclass){
                       if(length - 1 == i){
                           elm[0].classList.add('active');
                           
                       }
                       else{
                           elm[i + 1].classList.add('active');
                       }
                        
                       
                       
                        elm[i].classList.remove('active');
                       return false;
                   }
                    
                }
            }, 5000);
        }
        scope.$watch(scope.timer, function(newVal) {
            link();
        }, true);
    }
    return {
        restrict: 'EA',
        scope:{
            timer:"@"
        },
        link: link,
        controller:'VenueCtrl'
    };
});


app.directive('configfloor1', ['$http', function($http){
    function link(scope, element, attrs){
        var s = element[0].children[0];
        console.log(s);
        var contexthtml = element[0].children[1];
        var ctx = s.getContext('2d');
        var canvas = s;
        var img = new Image();
         var server = new Image();   
            function drawImageScaled(img, ctx){
                
                ctx.fillStyle = "#ffffff";
                ctx.fillRect(0,0,canvas.width,canvas.height);
                
                //console.log(img);
                var wrh = img.width / img.height;
                var hrw = img.height/ img.width;
                var newWidth = canvas.height / hrw;
                var wcenter = (canvas.width / 2) - (newWidth /2);
                
                ctx.drawImage(img,wcenter,0, newWidth , canvas.height);  
            }
            
            img.src = scope.data.img;
            
         function Shape(x, y, type) {
             console.log(this);
              // This is a very simple and unsafe constructor. All we're doing is checking if the values exist.
              // "x || 0" just means "if there is a value for x, use that. Otherwise use 0."
              // But we aren't checking anything else! We could put "Lalala" for the value of x 
              this.x = x || 0;
              this.y = y || 0;
              this.w = 18 || 1;
              this.h = 18 || 1;
              this.type = type;
            } 
        
        Shape.prototype.draw = function(ctx) {
            //console.log(this.x);
            server.src = "/facesix/static/qcom/img/server.png";
            //ctx.strokeRect(this.x, this.y, this.w, this.h, this.strokeStyle);
            //ctx.fillRect(this.x,this.y,this.w,this.h);
            ctx.drawImage(server, this.x, this.y, this.w, this.h);
             
            //console.log(server.src);
            
            if(this.type == "server"){
                server.src = "/facesix/static/qcom/img/server.png";
            }
            else{
                server.src = "/facesix/static/qcom/img/server.png";
            }
            
        }
        
        Shape.prototype.contains = function(mx, my) {
          // All we have to do is make sure the Mouse X,Y fall in the area between
          // the shape's X and (X + Height) and its Y and (Y + Height)
          return  (this.x <= mx) && (this.x + this.w >= mx) &&
                  (this.y <= my) && (this.y + this.h >= my);
        }
        
        
        function CanvasState(canvas){
            console.log(canvas);
            this.canvas = canvas;
            this.width = canvas.width;
            this.height = canvas.height;
           // console.log(this);
            this.ctx = canvas.getContext('2d');
            var ctx = this.ctx;
            
            var stylePaddingLeft, stylePaddingTop, styleBorderLeft, styleBorderTop;
            
            if (document.defaultView && document.defaultView.getComputedStyle) {
            this.stylePaddingLeft = parseInt(document.defaultView.getComputedStyle(canvas, null)['paddingLeft'], 15)      || 0;
            this.stylePaddingTop  = parseInt(document.defaultView.getComputedStyle(canvas, null)['paddingTop'], 100)       || 0;
            this.styleBorderLeft  = parseInt(document.defaultView.getComputedStyle(canvas, null)['borderLeftWidth'], 0)  || 0;
            this.styleBorderTop   = parseInt(document.defaultView.getComputedStyle(canvas, null)['borderTopWidth'], 0)   || 0;
          }
            var html = document.body.parentNode;
              this.htmlTop = html.offsetTop;
              this.htmlLeft = html.offsetLeft;
            
            this.valid = false;
            
            this.shapes = [];
            
            this.dragging = false;
            
            this.selection = null;
            
            this.dragoffx = 0;
            
            this.dragoffy = 0;
            
            var myState = this;
            //console.log(myState);
            
            //var canvas = ctx.canvas;
            var img = new Image();
            
            function drawImageScaled(img, ctx){
                //console.log(img);
                var wrh = img.width / img.height;
                var hrw = img.height/ img.width;
                var newWidth = myState.height / hrw;
                var wcenter = (myState.width / 2) - (newWidth /2);
                ctx.drawImage(img,wcenter,0, newWidth , myState.height);  
            }
            
            img.src = scope.data.img;
            
            drawImageScaled(img, ctx);
            
            canvas.addEventListener('selectstart', function(e) { e.preventDefault(); return false; }, false);
            
            canvas.addEventListener('mousedown', function(e) {
                var mouse = myState.getMouse(e);
                contexthtml.style.display="none";
                contexthtml.style.top = "0";
                contexthtml.style.left ="0";
                contexthtml.innerHTML ="";
                //console.log(mouse);
                var mx = mouse.x;
                var my = mouse.y;
                var shapes = myState.shapes;
                //console.log(shapes);
                var l = shapes.length;
                for (var i = l-1; i >= 0; i--) {
                  if (shapes[i].contains(mx, my)) {
                    var mySel = shapes[i];
                     // console.log(mySel);
                    // Keep track of where in the object we clicked
                    // so we can move it smoothly (see mousemove)
                    myState.dragoffx = mx - mySel.x;
                    myState.dragoffy = my - mySel.y;
                    myState.dragging = true;
                    myState.selection = mySel;
        //console.log(myState.selection);
                    myState.valid = false;
                      
                    return;
                  }
                }
                if (myState.selection) {
                      myState.selection = null;
                      myState.valid = false;
                    //console.log(myState.selection);
                }
                
                
                
            }, true);
            
            canvas.addEventListener('mousemove', function(e) {
                if (myState.dragging){
                    var mouse = myState.getMouse(e);
                    myState.selection.x = mouse.x - myState.dragoffx;
                    myState.selection.y = mouse.y - myState.dragoffy;   
                    myState.valid = false; // Something's dragging so we must redraw
                }
                
                else{
                }
            }, true);
            
            canvas.addEventListener('mouseup', function(e) {
                myState.dragging = false;
              }, true);
            canvas.addEventListener('mouseout', function(e) {
                myState.dragging = false;
              }, true);
            
            canvas.addEventListener('contextmenu', function(e) {
                e.preventDefault();
                var mouse = myState.getMouse(e);
                console.log(mouse);
                var mx = mouse.x;
                var my = mouse.y;
                var shapes = myState.shapes;
                //console.log(shapes);
                var l = shapes.length;
                for (var i = l-1; i >= 0; i--) {
                  if (shapes[i].contains(mx, my)) {
                    var mySel = shapes[i];
                        var link = "http://google.com";
                      contexthtml.innerHTML = "<li><a href='"+ link +"'><i class='fa fa-trash'></i>Trash</a></li>"
                    //contexthtml.style.top = mx "px";
                      contexthtml.style.display="block";
                      contexthtml.style.top = my + "px";
                      contexthtml.style.left =mx + "px";
                      
                   
                    return;
                  }
                }
                //console.log(myState);
                
              }, true);
            
            
            
            this.selectionColor = '#CC0000';
            this.selectionWidth = 2;  
            this.interval = 30;
              setInterval(function() { 
                  myState.draw(); 
                  //console.log(myState);
              }, myState.interval);
            
        }
        
        CanvasState.prototype.addShape = function(shape) {
            //console.log(this);
          this.shapes.push(shape);
           // console.log(this.shapes);
          this.valid = false;
        }
        
        CanvasState.prototype.clear = function() {
          this.ctx.clearRect(0, 0, this.width, this.height);
        }
        
        CanvasState.prototype.draw = function() {
            if (!this.valid) {
                var ctx = this.ctx;
                var shapes = this.shapes;
                this.clear();
                //console.log(shapes);
                drawImageScaled(img, ctx);
                //console.log(shapes);
                var l = shapes.length;
                
                for (var i = 0; i < l; i++) {
                    var shape = shapes[i];
                    if (shape.x > this.width || shape.y > this.height ||
          shape.x + shape.w < 0 || shape.y + shape.h < 0) continue;
      shapes[i].draw(ctx);
                    
                }
                
                
               if (this.selection != null){
                   //var server = new Image();
                   //console.log(this.selection);
//                   ctx.strokeStyle = this.selectionColor;
//                  ctx.lineWidth = this.selectionWidth;
                  var mySel = this.selection;
                   //console.log(mySel);
                  ctx.strokeRect(mySel.x,mySel.y,mySel.w,mySel.h);
               } 
                
              this.valid = true;  
                
                
            }
        }
        
        
        
        CanvasState.prototype.getMouse = function(e) {
          var element = this.canvas, offsetX = 0, offsetY = 0, mx, my;

          // Compute the total offset
          if (element.offsetParent !== undefined) {
            do {
              offsetX += element.offsetLeft;
              offsetY += element.offsetTop;
            } while ((element = element.offsetParent));
          }

          // Add padding and border style widths to offset
          // Also add the <html> offsets in case there's a position:fixed bar
          offsetX += this.stylePaddingLeft + this.styleBorderLeft + this.htmlLeft;
          offsetY += this.stylePaddingTop + this.styleBorderTop + this.htmlTop;

          mx = e.pageX - offsetX;
          my = e.pageY - offsetY;

          // We return a simple javascript object (a hash) with x and y defined
          return {x: mx, y: my};
        }
        
        var shapes =[];
        function init(){
            //var s = element[0];
            var s = new CanvasState(element[0].children[0]);
            var data = scope.data.device;
            var len = data.length;
            //s.addShape(new Shape(data[0].x,data[0].y,data[0].dev_type)); 
            
//            for (var i = 0; i < len; i++) {
//                s.addShape(new Shape(data[i].x,data[i].y,data[i].dev_type)); 
//            }
            
            
            
            console.log(s);
            s.addShape(new Shape(40,40,50,50)); // The default is gray
  s.addShape(new Shape(60,140,40,60, 'lightskyblue'));
  // Lets make some partially transparent
  s.addShape(new Shape(80,150,60,30, 'rgba(127, 255, 212, .5)'));
  s.addShape(new Shape(125,80,30,80, 'rgba(245, 222, 179, .7)'));
        }
        
        init();
        
        
        
    }
    return{
        restrict:'E',
        replace:true,
        scope:{
            data:'='
        },
        link:link,
        template:'<div><canvas id="drawfloor" class="canvaselm" width="1319" height="550"></canvas><ul id="ctm" class="canvas-context"><li><i class="fa fa-trash"></i>Delete</li></ul></div>'
    };

}]);

app.directive('gvenue', ['$http', '$compile', function($http,$compile){
    function link(scope, element, attrs){
        var geocoder;
        var map;
        console.log(document.getElementById("venuelist"));
        
//        google.maps.event.addDomListener(window, "load", initialize);
//        var locations = [
//          ['Bondi Beach', -33.890542, 151.274856,,, 'Bondi Beach', 4],
//          ['Coogee Beach', -33.923036, 151.259052,,,'Coogee Beach', 5],
//          ['Cronulla Beach', -34.028249, 151.157507,,,'Cronulla Beach', 3],
//          ['Manly Beach', -33.80010128657071, 151.28747820854187,,, 'Manly Beach', 2],
//          ['Maroubra Beach', -33.950198, 151.259302,,,'Maroubra Beach', 1]
//        ];
        var locations = [
            {Info:"M A Chidambram Stadium", Info2:"2 Venues", Lat: 13.070642, Lng:80.2038323},
            {Info:"M A Chidambram Stadium1", Info2:"2 Venues", Lat: 41.926979, Lng:12.517385},
            {Info:"M A Chidambram Stadium2", Info2:"2 Venues", Lat: 61.926979, Lng:12.517385}
        ]
        
        function setMarkers(map, locations) {
            var bounds = new google.maps.LatLngBounds();
            for (var i = 0; i < locations.length; i++) {
                var item = locations[i];

//                var myLatLng = new google.maps.LatLng(item[1], item[2]);
//                console.log(item[1]);
                var myLatLng = new google.maps.LatLng(locations[i].Lat, locations[i].Lng);
                console.log(locations[i].Lat);
                bounds.extend(myLatLng);
                
                 var address = '<div class="content" ng-click="showVenueDashboard()">'+locations[i].Info+'</div>'
                
                
                //var address = locations[i].Info;
//                var address = item[5];
                var marker = new google.maps.Marker({
                    position: myLatLng,
                    map: map,
                });

//                var content = $compile(address)(scope) ;
                var content = address ;

                var infowindow = new google.maps.InfoWindow()
                
                google.maps.event.addListener(marker, 'click', (function (marker, content, infowindow) {
                    return function () {
                        console.log(marker);
//                        infowindow.setContent(content);
//                        infowindow.open(map, marker);
                        window.location.href="#/venuedashboard"
                    };
                })(marker, content, infowindow));

                google.maps.event.addListener(marker, 'mouseover', (function (marker, content, infowindow) {
                    return function () {
                        infowindow.setContent(content);
                        infowindow.open(map, marker);
                    };
                })(marker, content, infowindow));
                google.maps.event.addListener(marker, 'mouseout', (function (marker, content, infowindow) {
                    return function () {
                        infowindow.close();
                    };
                })(marker, content, infowindow));

            }
            map.fitBounds(bounds);
        }
        function initialize() {
            var map = new google.maps.Map(
            document.getElementById("venuelist"), {
                center: new google.maps.LatLng(37.4419, -122.1419),
                zoom: 13,
                mapTypeId: google.maps.MapTypeId.ROADMAP
            });

        setMarkers(map,locations);
        }
        initialize();
    }
    
    return{
        restrict:'EA',
        scope:{},
        link:link
    };
    
}]);
