(function () {
    'use strict';
    angular
        .module('app.layout')
        .controller('shellController', shellController);
    shellController.$inject = ['session', 'authService', 'environment', '$q', 'navigation', 'notificationBarService', 'modalService',
        '$location', '$window', '$rootScope', '$interval', 'venuesession', 'dataService', '$state', 'messagingService', 'userService', '$timeout'];
    /* @ngInject */

    function shellController(session, authService, env, $q, navigation, notificationBarService, modalService,
        $location, $window, $rootScope, $interval, venuesession, dataService, $state, messagingService, userService, $timeout) {
        var vm = this;
        vm.isAuthenticated = authService.isAuthenticated();
        session.load();
        vm.getUsername = function () { return session.username; };
        vm.environment = env.environment;
        vm.version = env.version;
        vm.endpoint = env.serverBaseUrl;
        vm.showEnvDetails = env.showEnvDetails;
        vm.versionNotificationModalShowing = false;
        vm.mainMenu = ['/reports', '/users', '/upgrade'];
        vm.role = session.role;
        vm.venuesession = venuesession;
        vm.showTreeActive = false;
        vm.isMyAccount = false;
        messagingService.listenForBroadcastSuccessfullRouteChanged(stateChanged);

        function activate() {        
            onresize();
            vm.isOnLoginScreen = navigation.isOnLoginScreen;
            vm.isOnAdmininScreen = navigation.isOnAdmininScreen;
            vm.isOnvenueScreen = navigation.isOnvenueScreen;
            vm.isOndashboardScreen = navigation.isOndashboardScreen;
            vm.isOnfloorplanScreen = navigation.isOnfloorplanScreen;
            vm.isOnfloorviewScreen = navigation.isOnfloorviewScreen;
            vm.isOngatewayScreen = navigation.isOngatewayScreen;
            vm.isOntagScreen = navigation.isOntagScreen;
            vm.isOnvisualizationScreen = navigation.isOnvisualizationScreen;
            vm.isOnreportScreen = navigation.isOnreportScreen;
            vm.isOninactivityalertsScreen = navigation.isOninactivityalertsScreen;
            vm.isOngeofencealertsScreen = navigation.isOngeofencealertsScreen;
            vm.isuserScreen = navigation.isuserScreen;
            vm.issupportScreen = navigation.issupportScreen;
            vm.isroleScreen = navigation.isroleScreen;
            vm.isupgradeScreen = navigation.isupgradeScreen;
            vm.isupgradehistoryScreen = navigation.isupgradehistoryScreen;
            vm.ismyaccountScreen = navigation.ismyaccountScreen;
            vm.isbaseOnfloorplanScreen = navigation.isbaseOnfloorplanScreen;
            vm.islicenseCustomers = navigation.islicenseCustomers;
            vm.isinactiveCustomers = navigation.isinactiveCustomers;

            onresize();
            initUploader();
        }


        vm.getUser = function () {
            if (window.location.hash != '#!/login') {
                userService.getprofile().then(function (result) {
                    vm.user = result;
                    vm.username = result.fname + ' ' + result.lname;
                    vm.userId = result.id;
                    //vm.cid = $rootScope.customerId;
                    vm.cid = session.cid;

                    vm.logoUrl = "http://locatum.qubercomm.com/facesix/preferredLogoUrl?id=" + vm.cid;
                    // userService.getprofilepic(vm.user.id).then(function (result) {

                    //     vm.userimagepath = JSON.stringify(result);  

                    // });


                });
            }
        };


        function stateChanged(toState) {
            vm.isAuthenticated = authService.isAuthenticated();
            session.load();
            vm.role = session.role;
            vm.isVenue = false;
            vm.isReports = false;
            vm.isUsers = false;
            vm.isUpgrade = false;
            vm.isgeofencealerts = false;
            vm.isMyAccount = false;
            vm.isVenueVisited = false;
            var test = vm.isOnvenueScreen();
            if (vm.isOnvenueScreen() || vm.isOndashboardScreen() || vm.isOnfloorplanScreen() || vm.isOnfloorviewScreen() || vm.isOngatewayScreen() || vm.isOntagScreen() || vm.isbaseOnfloorplanScreen()) {
                vm.isVenue = true;
                vm.isVenueVisited = true;
            }
            else if (vm.isOninactivityalertsScreen() || vm.isOngeofencealertsScreen()) {
                vm.isgeofencealerts = true;
            }
            else if (vm.isOnvisualizationScreen() || vm.isOnreportScreen()) {
                vm.isReports = true;
            }
            else if (vm.isuserScreen() || vm.issupportScreen() || vm.isroleScreen() || vm.islicenseCustomers() || vm.isinactiveCustomers()) {
                vm.isUsers = true;
            }
            else if (vm.isupgradeScreen() || vm.isupgradehistoryScreen()) {
                vm.isUpgrade = true;
            }
            else if (vm.ismyaccountScreen()) {
                vm.isMyAccount = true;
            }


            if (session.role == 'superadmin') {
                if ($state.current.title == 'Profile' || $state.current.title == 'Role' || $state.current.title == 'Support' || $state.current.title == 'Users') {
                    $rootScope.customerName = session.role + ' | ' + $state.current.title;
                }
                else {
                    $rootScope.customerName = session.customer + ' | ' + $state.current.title;
                }
            }
            else {
                $rootScope.customerName = session.customer + ' | ' + $state.current.title;
            }


            // $rootScope.customerName = session.accessToken + ' | ' + $state.current.title;

            vm.getUser();
            var checkTime = authService.checkExpirationTime();
            if (checkTime) {
                $state.go('login');
            }

        }

        $rootScope.$on('customEvent', function (event, message) {
            if (message === "venue") {
                vm.isVenue = true;
                vm.isReports = false;
                vm.isUsers = false;
                vm.isUpgrade = false;
                vm.isgeofencealerts = false;
                vm.isMyAccount = false;
            }
            else {
                vm.isVenue = false;
                vm.isReports = false;
                vm.isUsers = false;
                vm.isUpgrade = false;
                vm.isgeofencealerts = false;
                vm.isMyAccount = false;
            }
        });

        vm.isActive = function (viewLocation) {
            var path = $location.path().indexOf(viewLocation) > -1;
            return path;
        };

        vm.dashboard = function () {
            navigation.gotToDashboard(vm.venueId);
        };

        if (window.innerWidth <= 605) {
            $(".topmenuview").addClass("row");
        }
        else if (window.innerWidth > 605) {
            $(".topmenuview").removeClass("row");
        }

        vm.drawFloor = function () {
            navigation.goToDrawFloor();
        };

        /* Triggered when user clicks on LocatumLogo */
        vm.onLocatumLogoClick = function () {

            vm.isReports = false;
            vm.isUsers = false;
            vm.isUpgrade = false;
            vm.isgeofencealerts = false;
            if (session.role == "superadmin") {
                vm.isVenue = false;
                vm.floorDetails = []
                vm.venueDetails = null
                vm.venuesession.create();
                navigation.gotToAdminHome();
            } else if (session.role == "appadmin") {
                vm.isVenue = true;
                navigation.goToVenue();
            }
        };

        vm.logout = function () {
            var message = "<p>Are you sure you want to log out?</p> <p>Press No if you want to continue to work. Press Yes to logout.</p>";
            modalService.questionModal('Logout Confirmation', message, true).result.then(function () {
                authService.logOut().then(function (result) {
                    $rootScope.venueId = "";
                    $rootScope.spid = "";
                    vm.isVenue = true;
                    vm.isReports = false;
                    vm.isUsers = false;
                    vm.isUpgrade = false;
                    vm.isgeofencealerts = false;
                    vm.isMyAccount = false;
                });
            }, function () {
            });
        };

        $(".x-navigation-minimize").click(function () {
            if ($(".page-sidebar .x-navigation").hasClass("x-navigation-minimized")) {
                $(".page-container").removeClass("page-navigation-toggled");
                x_navigation_minimize("open");
            } else {
                $(".page-container").addClass("page-navigation-toggled");
                x_navigation_minimize("close");
            }
            onresize();
            return false;
        });

        function x_navigation_minimize(action) {
            if (action == 'open') {
                $(".page-container").removeClass("page-container-wide");
                $(".page-sidebar .x-navigation").removeClass("x-navigation-minimized");
                $(".x-navigation-minimize").find(".fa").removeClass("fa-indent").addClass("fa-dedent");
            }

            if (action == 'close') {
                $(".page-container").addClass("page-container-wide");
                $(".page-sidebar .x-navigation").addClass("x-navigation-minimized");
                $(".x-navigation-minimize").find(".fa").removeClass("fa-dedent").addClass("fa-indent");

            }
            $(".x-navigation li.active").removeClass("active");
        }



        function onresize(timeout) {
            timeout = timeout ? timeout : 200;

        }

        $(".x-navigation-control").click(function () {
            $(this).parents(".x-navigation").toggleClass("x-navigation-open");
            onresize();
            return false;
        });

        if ($(".page-navigation-toggled").length > 0) {
            x_navigation_minimize("close");
        }

        $(".x-navigation  li > a").click(function () {
            var li = $(this).parent('li');
            var ul = li.parent("ul");
            ul.find(" > li").not(li).removeClass("active");
        });

        $(".xn-openable").click(function () {
            var li = $(this).parent('li');
            var parentul = li.children('ul');
            var childli = parentul.children('li');
            var ul = li.parent("ul");
        });

        $(".x-navigation li").click(function (event) {
            event.stopPropagation();
            if ($rootScope.plantDevicesTagsinterval)
                $interval.cancel($rootScope.plantDevicesTagsinterval);
            var li = $(this);
            if (li.children("a").length > 0 || li.children("ul").length > 0 || li.children(".panel").length > 0 || $(this).hasClass("xn-profile") > 0) {
                if (li.hasClass("active") && li.hasClass("xn-openable")) {
                    li.removeClass("active");
                    li.find("li.active").removeClass("active");
                } else if (!(li.hasClass("layoutmenu"))) {
                    li.addClass("active");
                    var childli = li.find("ul").find("li");
                    angular.forEach(childli, function (item) {
                        item.className = "";
                    });
                    if (childli[0])
                        childli[0].className = "active";
                }
                onresize();
                if ($(this).hasClass("xn-profile") > 0)
                    return true;
                else
                    return false;
            }
        });

        vm.myaccount = function () {
            navigation.goToProfile(0);
        }

        vm.resetPassword = function () {
            modalService.resetPasswordModal(0).result.then(function (res) {
                //
            }, function () {
            });
        }


        vm.about = function () {
            modalService.aboutModal().result.then(function (res) {
                //
            }, function () {
            });
        }

        function getB64Type($str) {
            return $str.split(';')[0].split(':')[1];
        }

        function initUploader() {

            vm.uploader = userService.getFileUploaderInstance();

            vm.uploader.onErrorItem = function (item, response, status, headers) {
                $timeout(function () { progressModel.close(); }, 10);
                modalService.warrningMessageModel('Error!', '<div class="text-center"><h4 class="text-danger"><i class="fa fa-times text-danger faa-pulse animated" aria-hidden="true"></i>Unexpected error occurred,please try again!</h4></div>', false).result.then(
                    function (item) {
                    });
            };

            vm.uploader.onAfterAddingFile = function (FileUploader) {
                var fileReader = new FileReader();
                fileReader.readAsDataURL(FileUploader._file);
                fileReader.onload = function (e) {
                    $timeout(function () {
                        var target = e.target.result;
                        vm.userimagepath = target;
                        var blob = null;
                        var imageDataUR = "";
                        if (vm.userimagepath.indexOf("base64") != -1) {
                            imageDataUR = vm.userimagepath;
                        }

                        var fd = new FormData();
                        var data = atob(imageDataUR.replace(/^.*?base64,/, ''));
                        var asArray = new Uint8Array(data.length);
                        for (var i = 0, len = data.length; i < len; ++i) {
                            asArray[i] = data.charCodeAt(i);
                        }
                        var fileType = getB64Type(imageDataUR);
                        blob = new Blob([asArray.buffer], { type: fileType });
                        fd.append('file', blob);

                        userService.profileupload(fd).then(function (result) {
                            notificationBarService.success(result.body);
                            activate();
                        });
                    });
                };
            };
            // vm.showTree = function() {
            //         vm.showTreeActive = true;
            //         modalService.devicesModal('networkconfig').result.then(() => {
            //             vm.showTreeActive = false;
            //     })  
            // }
            vm.showTree = function () {
                vm.showTreeActive = true;
                modalService.devicesModal('networkconfig').result.then(function () {
                    vm.showTreeActive = false;
                });
            };
        }

        vm.fencecache = function () {
            localStorage.clear()
        }

        activate();
    }
})();