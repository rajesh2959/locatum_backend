(function () {
    'use strict';
    angular
        .module('app')
        .controller('VisualizationController', controller);
    controller.$inject = ['visualizationService', 'messagingService', 'notificationBarService', 'SimpleListScreenViewModel', '$q', 'modalService', 'navigation', '$rootScope', '$linq'];
    /* @ngInject */

    function controller(visualizationService, messagingService, notificationBarService, SimpleListScreenViewModel, $q, modalService, navigation, $rootScope, $linq) {
        var vm = new SimpleListScreenViewModel();
        vm.isSelectVisual = false;
        vm.chartType = 'bar';
        vm.visualname = 'name';
        vm.desp = 'description';
        vm.istaginitialload = true;
        vm.refresh = function (refresh) {
            vm.getVisualization(refresh);
        };

        vm.getVisualization = function (refresh) {
            visualizationService.getVisualListForTable(refresh, vm.dataOperations, vm.filterFn).then(function (result) {
                if (vm.istaginitialload) {
                    angular.forEach(result.allData,
                        function (value, key) {
                            value.isChecked = false;
                        });
                    vm.istaginitialload = false;
                }

                vm.allVisualList = result.allData;
                vm.pagedData = result.pagedData;
                vm.fullCount = result.dataCount;
                vm.filteredCount = result.filteredDataCount;
                if(vm.pagedData.length > 0){
                    $('.del-all').css("pointer-events","auto");
                    $('.del-all').css("color","red");
                } else {
                    $('.del-all').css("pointer-events","none");
                    $('.del-all').css("color","lightgray");
                }

            });
        };


        vm.inputCheckAll = function (isChecked) {
            angular.forEach(vm.allVisualList,
                function (value, key) {
                    if (isChecked == true) {
                        value.isChecked = true;
                    } else {
                        value.isChecked = false;
                    }
                });
        };

        vm.isRowChecked = function () {
            var isAnyRowChecked = !$linq.Enumerable().From(vm.allVisualList)
                .Any(function (x) {
                    return x.isChecked == false;
                });
            if (!isAnyRowChecked)
                vm.ischekAll = false;
            else
                vm.ischekAll = true;
        }

        vm.addVisualization = function () {
            vm.isSelectVisual = true;
        };

        vm.cancelVisual = function () {
            vm.isSelectVisual = false;
        };

        vm.createVisual = function () {
            navigation.goToAddVisualization(vm.chartType, 0);
        };

        vm.edit = function (visual,type) {
            // Before navigating set the chart type to the selected visual
            vm.chartType = visual.chartType;
            vm.visualname = visual.name;
            vm.desp = visual.description;
            navigation.goToAddVisualization(vm.chartType, visual.id,type,vm.visualname,vm.desp);
        };

        vm.deleteAll = function () {
            var visualisationIds = [];
            angular.forEach(vm.allVisualList,
                function (value, key) {
                    if (value.isChecked) {
                        visualisationIds.push(value.id);
                    }
                });
            if (visualisationIds.length > 0) {
                modalService.confirmDelete('Are you sure you want to delete the visualization?').result.then(
                    function () {
                        visualizationService.delete(visualisationIds).then(function (result) {
                            if (result && result.success) {
                                vm.refresh(true);
                                vm.ischekAll = false;
                            }
                            notificationBarService.success(result.body);
                        });
                    },
                    function () {
                    });
            } else {
                var message = "<p>There are no visualization selected for delete.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };

        vm.searchBy = function (search) {
            if (search && search.length > 0) {
                vm.filterFn = function (data) {
                    var lowerCaseSearchTerm = search.toLowerCase();
                    var result = (data.name && data.name.toLowerCase().contains(lowerCaseSearchTerm)) ||
                        (data.description && data.description.toLowerCase().contains(lowerCaseSearchTerm))
                    return result;
                };
            } else {
                vm.filterFn = null;
            }
            vm.getVisualization(false);
        };

        vm.delete = function (visual) {
            var visualisationIds = [];
            visualisationIds.push(visual.id);
            if (visualisationIds.length > 0) {
                modalService.confirmDelete('Are you sure you want to delete the visualization?').result.then(
                    function () {
                        visualizationService.delete(visualisationIds).then(function (result) {
                            if (result && result.success) {
                                vm.refresh(true);
                                vm.ischekAll = false;
                            }
                            notificationBarService.success(result.body);
                        });
                    },
                    function () {
                    });
            } else {
                var message = "<p>There are no visualization selected for delete.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };


        vm.getChartType = function (chartType) {
            vm.chartType = chartType;
        };

        vm.goToPage = function () {
            if (parseInt(vm.goToPageNumber) > vm.totalPageCount) {
                vm.goToPageNumber = '';
            } else {
                vm.dataOperations.paging.currentPage = vm.goToPageNumber;
                vm.getVisualization();
                vm.goToPageNumber = '';
            }
        };


        function activate() {
            vm.refresh(true);
        };

        activate();

        return vm;
    }
})();