﻿(function () {
    'use strict';
    angular
        .module('app.reports')
        .controller('AddReportController', controller);
    controller.$inject = ['$log', '$timeout', '$linq', 'visualizationService', 'SimpleListScreenViewModel', 'messagingService', 'notificationBarService', 'cid', 'uid', 'name', '$q', 'modalService', 'navigation', 'reportService'];

    /* @ngInject */
    function controller($log, $timeout, $linq, visualizationService, SimpleListScreenViewModel, messagingService, notificationBarService, cid, reportId, name, $q, modalService, navigation, reportService) {
        var vm = new SimpleListScreenViewModel();
        var colorIndex = 2;
        vm.colors = ["rgba(250, 100, 38, 0.7)", "rgba(185, 116, 118, 0.7)", "rgba(0, 128, 0, 0.7)", "rgba(0, 0, 128, 0.7)", "rgba(255, 0, 0, 0.7)"];
        vm.widgets = [];
        vm.chartDataCheck = false; 
        vm.onClick = function (points, evt) {
            console.log(points, evt);
        };
        vm.isedit = false;

        vm.widgets = [];


        vm.options = {
            cellHeight: 40,
            verticalMargin: 10
            
        };
        vm.titleChange = 'Add Report';
        vm.name = name;
        vm.getReportById = function (reportId) {
            reportService.getReportById(reportId).then(function (result) {
                vm.selectedreport = result;
                vm.isedit = true;
                var visualIdlist = [];
                var visualsvalues = [];
                vm.chartList = JSON.parse(result.uiContent);
                angular.forEach(vm.allVisualList, function (value, key) {
                    for (var i = 0; i < vm.chartList.length; i++) {

                        if (vm.chartList[i] != null) {
                            if (value.id == vm.chartList[i].chartId) {
                                value.isChecked = true;
                                visualIdlist.push(value.id);
                                if (value.isChecked = true) {
                                    visualsvalues.push(value)
                                }
                                //vm.addWidget(value, chartList[i]);
                            }  
                        }


                    }
                });
                vm.addMutipleWidgets(visualIdlist, visualsvalues);

            });
        };

        vm.addWidget = function (visual, widget) {
            var from;
            var to; 
            if (vm.fromDate && vm.toDate && new Date(vm.toDate) <= new Date()) {
                if (new Date(vm.toDate) < new Date(vm.fromDate)) {
                    notificationBarService.error("To date cannot be less than the from date");
                    return;
                }
                from = moment(vm.fromDate).format('YYYY-MM-DD[T]HH:mm:ss.SSS');
                to = moment((new Date((new Date(vm.toDate.toString())).setHours(23,59,59)))).format('YYYY-MM-DD[T]HH:mm:ss.SSS');
        }
        else {
            notificationBarService.error("To date cannot be greater than the today date");
            return;
        }            
            reportService.getVisualdatabyId(visual.id, from, to).then(function (result) {
                if (result && result.data) {
                    // result.data[0].splice(0, 1);
                    // result.data[1].splice(0, 1);
                    // result.columns.splice(0, 1);
                    vm.bindWidgetData(visual, result, widget);
                }
            });

            // vm.ssdata=["90"]
            // return vm.ssdata;
        };

        vm.addMutipleWidgets = function (visualIds, visualsvalues) {

            var from;
            var to; 
            if (vm.fromDate && vm.toDate && new Date(vm.toDate) <= new Date()) {
                    if (new Date(vm.toDate) < new Date(vm.fromDate)) {
                        notificationBarService.error("To date cannot be less than the from date");
                        return;
                    }
                    from = moment(vm.fromDate).format('YYYY-MM-DD[T]HH:mm:ss.SSS');
                    to = moment((new Date((new Date(vm.toDate.toString())).setHours(23,59,59)))).format('YYYY-MM-DD[T]HH:mm:ss.SSS');
            }
            else {
                notificationBarService.error("To date cannot be greater than the today date");
                return;
            }

            console.log("fromDate : "+from + "  toDate : "+to);
            reportService.getMultipleVisualdatabyId(visualIds, from, to).then(function (result) {
                if (result) {
                    var widget = "";
                    vm.tempResult = result;
                    var i=0;
                    angular.forEach(vm.tempResult, function (resultvalue, key) {
                        angular.forEach(visualsvalues, function (visuallist) {
                            if (resultvalue.id == visuallist.id) {
                                if (reportId && reportId != "0" && vm.chartList[i] != null) {
                                    widget = {x:vm.chartList[i].x, y: vm.chartList[i].y, width: vm.chartList[i].width, height: vm.chartList[i].height};
                                } 
                                vm.bindWidgetData(visuallist, resultvalue, widget);
                                i++;   
                            }                            
                        });

                    });
                }
            });
        };

        vm.bindWidgetData = function (visual, reportData, widget) {
            //var data = vm.getVisualData(visual);
            var newWidget = {};
            var options = [];
            if (visual.chartType == "table") {
                //var chart = angular.copy(result);
                //angular.forEach(data, function (value, key) {
                //    value.splice(0, 1);
                //});
                //vm.tableChart = chart;
                //result.data[0].splice(0, 1);
                //result.data[1].splice(0, 1);

                //vm.labels = result.data[0];
                //var cdata = result.data[1];
                //vm.data.push(cdata);

                //result.columns.splice(0, 1);
                //vm.series = result.columns;

                //newWidget = { x: 0, y: 0, width: 6, height: 4, chart: vm.data, chartType: visual.chartType, chartId: visual.id };

            } 
           
            else {
                var cdata = reportData.data[1];                
                var max = 0.0;
                var min = 0.0;
                var stepSize = 0;
                var i;
                if(reportData != undefined) {
                    if(reportData.data != undefined && reportData.data.length >1) {
                        if(reportData.data[0].length >= 2) {
                            reportData.data[0].splice(0, 1);        
                        }
                        if(reportData.data[1].length >= 2) {
                            reportData.data[1].splice(0, 1);        
                        }
                    }
                }
                // if(reportId != undefined && reportId != 0) {
                //     reportData.data[0].splice(0, 1);
                //     reportData.data[1].splice(0, 1);
                // }                
               
                    if(reportData.columns.length > 0 && reportData.data.length > 0) {
                        if(cdata.length == 1) {
                            max = parseFloat(cdata);
                        } else {
                            max = parseFloat(cdata[0]);
                            for (i = 1; i < cdata.length; i++) { 
                                if(parseFloat(cdata[i]) > max) {
                                    max = parseFloat(cdata[i]);
                                }
                            }
                        }
                        var stepFactor = 1;
                        var stepSize = 25;                                        
                        if(max/10 <= 10) {
                            // Values < 100
                            stepFactor = 1;
                            max = 100;
                        } else if(max/100 <= 10) {
                            // Values from 100 - 1000
                            stepFactor = 10;
                            max = 1000;
                        }else if(max/1000 <= 10) {
                            // Values from 1000 - 10000
                            stepFactor = 100;
                            max = 10000;
                        }else if(max/10000 <= 10){
                            // Values from 10000 - 100000
                            stepFactor = 1000;
                            max = 100000;
                        }else if(max/100000 <= 10){
                            // Values from 100000 - 10000000
                            stepFactor = 10000;
                            max = 1000000;
                        }else if(max/1000000 <= 10){
                            // Values from 1000000 - 100000000
                            stepFactor = 100000;
                            max = 10000000;
                        }else if(max/10000000 <= 10){
                            // Values from 10000000 - 1000000000
                            stepFactor = 1000000;
                            max = 100000000;
                        }
                       
                            var cat1Name = "";
                            var cat2Name = "";
                            
                            if(visual.metrics[0].customLabel) {
                                cat1Name = visual.metrics[0].customLabel +' ('+ reportData.units + ')';
                            }
                            if(visual.buckets[0].customLabel) {
                                cat2Name = visual.buckets[0].customLabel;
                            } 
                        vm.options = {
                            title: {
                                display: true,
                                text: visual.name
                            },
                            scales: {
                                yAxes: [
                                    {
                                        id: 'y-axis-1',
                                        display: true,
                                        position: 'left',
                                        ticks: {
                                            min: 0,
                                            max: max,
                                            stepSize: stepSize * stepFactor,
                                        },
                                        scaleLabel: {
                                            display: true,
                                            labelString: cat1Name,
                                            fontColor: "#546372"
                                        },
                                    }
                                ],
                                xAxes: [
                                    {
                                        id: 'x-axis-1',
                                        display: true,
                                        position: 'right',
                                        ticks: {
                                            callback: function (value, index, values) {
                                                return value;
                                            }
                                        },
                                        scaleLabel: {
                                            display: true,
                                            labelString: cat2Name,
                                            fontColor: "#546372"
                                        }
                                    }
                                ]
                            }
                        };
    
              vm.options_pie = {title: {
                display: true,
                text: visual.name
            }}
                
                
                    console.log("------------color-----------" + colorIndex);
                    vm.data = {
                        series: ["SeriesA"],
                       data: reportData.data[1],
                     
                        labels: reportData.data[0],
                        //labels: ["01", "02", "03", "04", "05", "06", "07", "08", "09", "10"],
                        colors: [{
                            fill: "true",
                            fillColor: vm.colors[colorIndex],
                            //borderColor: "rgba(168, 81, 84, 1)",
                            borderColor: vm.colors[colorIndex],
                            //borderColor: "rgba(185, 116, 118, 0)",
                            backgroundColor: vm.colors[colorIndex],
                            strokeColor: vm.colors[colorIndex],
                            pointColor: vm.colors[colorIndex],
                            pointStrokeColor: vm.colors[colorIndex],
                            pointHighlightFill: vm.colors[colorIndex],
                            pointHighlightStroke: vm.colors[colorIndex]
                        }]
                    };
                    if (reportId && reportId != "0") {
                        newWidget = { x: widget.x, y: widget.y, width: widget.width, height: widget.height, chart: vm.data, chartType: visual.chartType, chartId: visual.id, chartoptionspie: vm.options_pie,chartoptions:vm.options };
                    } else {
                        newWidget = { x: 0, y: 0, width: 6, height: 6, chart: vm.data, chartType: visual.chartType, chartId: visual.id, chartoptionspie: vm.options_pie,chartoptions:vm.options };
                    if (widget && widget) {
                        newWidget.x = widget.x;
                        newWidget.y = widget.y;
                        newWidget.width = widget.width;
                        newWidget.height = widget.height;
                    }
                    }  
                    vm.widgets.push(newWidget);
                    colorIndex++;
                } else {
                    vm.widgets.push(reportData);
                }

                    }
                    
        };

        vm.moveWidget = function () {
            vm.widgets[0].x = 0;
            vm.widgets[0].y = 0;
            vm.widgets[0].width = 2;
            vm.widgets[0].height = 2;
        };

        vm.removeWidget = function (w) {
            var index = vm.widgets.indexOf(w);
            vm.widgets.splice(index, 1);
        };

        vm.removeChart = function (visual) {
            var widget = $linq.Enumerable().From(vm.widgets)
                .Where(function (x) {
                    if(!x.chartId) {
                        return x.id == visual.id
                    } else {
                        return x.chartId == visual.id;
                    }
                    
                }).FirstOrDefault();
            if (widget) {
                vm.removeWidget(widget);
            }
        };

        vm.onChange = function (event, items) {
        };

        vm.onDragStart = function (event, ui) {
        };

        vm.onDragStop = function (event, ui) {
        };

        vm.onResizeStart = function (event, ui) {
        };

        vm.onResizeStop = function (event, ui) {
        };

        vm.onItemAdded = function (item) {
        };

        vm.onItemRemoved = function (item) {
        };

        vm.getVisualization = function (refresh) {
            visualizationService.getVisualListForTable(refresh, vm.dataOperations, vm.filterFn).then(function (result) {
                angular.forEach(result.allData, function (value, key) {
                    value.isChecked = false;
                });
                angular.forEach(result.pagedData, function (value, key) {
                    value.isChecked = false;
                });
                vm.allVisualList = result.allData;
                vm.pagedData = result.pagedData;
                vm.fullCount = result.dataCount;
                vm.filteredCount = result.filteredDataCount;
                if (reportId && reportId != "0")
                    vm.getReportById(reportId);


            });
        };

        vm.getReportData = function (refresh) {
            visualizationService.getVisualListForTable(refresh, vm.dataOperations, vm.filterFn).then(function (result) {
                vm.allVisualList = result.allData;
                vm.pagedData = result.pagedData;
                vm.fullCount = result.dataCount;
                vm.filteredCount = result.filteredDataCount;
            });
        };



        vm.getData = function (refresh) {
            visualizationService.getVisualListForTable(refresh, vm.dataOperations, vm.filterFn).then(function (result) {
                //vm.visualList = result;
                vm.allVisualList = result.allData;
                vm.pagedData = result.pagedData;
                vm.fullCount = result.dataCount;
                vm.filteredCount = result.filteredDataCount;
            });
        };

        vm.addVisual = function (visual) {
            if (visual.isChecked)
                vm.addWidget(visual);
            else
                vm.removeChart(visual);
        };

        vm.cancel = function () {
            navigation.goToReport();
        };

        vm.save = function () {
            var uiContent = [];
            vm.payload = {};
            var visualList = [];
            angular.forEach(vm.allVisualList,
                function (value, key) {
                    if (value.isChecked) {
                        var widget = $linq.Enumerable().From(vm.widgets)
                            .Where(function (x) {
                                return x.chartId == value.id
                            }).FirstOrDefault();
                        //widget.chart = [];
                        if(widget && widget != null) {
                            uiContent.push(widget);
                            visualList.push(value.id);
                        }
                        
                    }
                });
            if (visualList.length > 0) {

                var name = vm.selectedreport != undefined ? vm.selectedreport.name : '';
                var description = vm.selectedreport != undefined ? vm.selectedreport.description : '';
                if (!vm.isedit) {


                    modalService.reportconfirmModal(name, description).result.then(function (res) {
                        if (res != undefined) {
                            vm.payload.entityType = "report";
                            vm.payload.name = res.name;
                            vm.payload.description = res.description;
                            vm.payload.visualId = visualList;
                            vm.payload.uiContent = JSON.stringify(uiContent);
                            reportService.save(vm.payload).then(function (result) {
                                if (result && result.success)
                                    navigation.goToReport();
                                notificationBarService.success(result.body);
                            });
                        }
                        else {
                            //vm.widgets=tempWidget;

                            // var selectedCharts = [];
                            // vm.payload.visualId = "";
                            // vm.payload.uiContent = "";
                            // vm.widgets = [];

                            // angular.forEach(vm.allVisualList,
                            //     function (value, key) {
                            //         if (value.isChecked) {
                            //             selectedCharts.push(value);
                            //         }
                            //     });

                            // angular.forEach(selectedCharts,
                            //     function (value, key) {
                            //         vm.removeChart(value);
                            //         vm.addVisual(value);
                            //     });

                        }
                    }, function (err) {

                    });
                }
                else {
                    vm.payload.id = vm.selectedreport.id;
                    vm.payload.entityType = vm.selectedreport.entityType;
                    vm.payload.name = name;
                    vm.payload.description = description;
                    vm.payload.visualId = visualList;
                    vm.payload.uiContent = JSON.stringify(uiContent);
                    reportService.save(vm.payload).then(function (result) {
                        if (result && result.success)
                            navigation.goToReport();
                        notificationBarService.success(result.body);
                    });
                }
            }
            else {
                var message = "<p>There are no reports selected for save.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        };

        vm.go = function(){
            //console.log(vm.fromDate + " " + vm.toDate);
            var uiContent = [];
            vm.payload = {};
            var visualList = [];
             var visualsvalues = [];
             vm.widgets = [];
            angular.forEach(vm.allVisualList,
                function (value, key) {
                    if (value.isChecked) {
                        var widget = $linq.Enumerable().From(vm.widgets)
                            .Where(function (x) {
                                return x.chartId == value.id
                            }).FirstOrDefault();
                        //widget.chart = [];
                        uiContent.push(widget);
                        visualList.push(value.id);
                        visualsvalues.push(value);
                    }

                });
            if (visualList.length > 0) {
                vm.addMutipleWidgets(visualList, visualsvalues);
            }
            else{
                var message = "<p>There are no visuals selected.</p>";
                modalService.messageModal('Information', message).result.then(function () {
                });
            }
        }

        function activate() {
            //vm.colors = ['#803690', '#00ADF9', '#DCDCDC', '#46BFBD', '#FDB45C', '#949FB1', '#4D5360'];

            vm.fromDate = moment(new Date()).format('YYYY/MM/DD');
            vm.toDate = moment(new Date()).format('YYYY/MM/DD');
            
            // vm.labels = ["January", "February", "March", "April", "May", "June", "July"];
            // vm.series = ['Series A', 'Series B'];
            // vm.data = [
            //     [65, 59, 80, 81, 56, 55, 40],
            //     [28, 48, 40, 19, 86, 27, 90]
            // ];
            if(reportId != undefined && reportId != '0') {
                vm.titleChange = 'View Report';
                vm.name = name;
            }

            vm.getVisualization(true);
        }

        activate();
       
        return vm;
    }
})();