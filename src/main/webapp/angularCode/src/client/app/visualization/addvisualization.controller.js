(function () {
    'use strict';
    angular
        .module('app')
        .controller('AddVisualizationController', controller);
    controller.$inject = ['messagingService', 'notificationBarService', '$q', 'chartType', 'visualid', 'type', 'visualname','desp', 'modalService', 'navigation', '$rootScope','$timeout', 'tagService', 'visualizationService', '$linq', 'venuedataservice', 'floordataservice', 'geoFenceService', 'SimpleListScreenViewModel'];
    /* @ngInject */

    function controller(messagingService, notificationBarService, $q, chartType, visualid, type,visualname,desp, modalService, navigation, $rootScope,$timeout, tagService, visualizationService, $linq, venuedataservice, floordataservice, geoFenceService, SimpleListScreenViewModel) {
        var vm = this;
        var isEdit = false;
        vm.visual = {};
        vm.chartType = chartType;
        vm.filterList = [];
        vm.data = [];
        vm.labels = [];
        vm.series = [];
        vm.options = [];
        vm.datasets = [];
        vm.stackedData = [];
        vm.barcolor =[];
        var ctx;
        vm.chart = chartType.toLowerCase();
        if(chartType == 'stackedbar'){
            vm.chartType = "Stacked Bar"
        }
        vm.dataOperationsTag = new SimpleListScreenViewModel();
        vm.selectedFiterItems = [];
        vm.todayDate = new Date();
        if(type == "view"){
            $('.panelview').show();
            $('.paneledit').hide();
        } else {
            $('.paneledit').show();
            $('.panelview').hide();
            $('.customiz-panel-flex-row').removeClass('paneltype');
        }

        vm.paneltype = function(){
            $('.paneledit').show();
            $('.panelview').hide();
            $('.customiz-panel-flex-row').removeClass('paneltype');
        }

        if(visualname != ""){
            $('.visualcss').show();
            $('#name').text(visualname);
            $('#description').text(desp);
        }
        
        vm.onFilterChange = function (selectedFilter) {
            //vm.filterItemList = [];
            switch (selectedFilter.label) {
                case "Venue":
                    var venueList = [];
                    venuedataservice.getVenueList()
                        .then(function (result) {
                            result.forEach(function (element) {
                                venueList.push({ id: element.id, label: element.uid, isVisible: true, isChecked: false });
                            });
                            vm.filterList.forEach(function (item) {
                                if (item.label == "Venue") {
                                    item.filterItemList = venueList;
                                }
                            });
                        });
                    break;
                case "Floor":
                    var floorList = [];
                    venuedataservice.getVenueList()
                        .then(function (result) {
                            result.forEach(function (element) {
                                floorList.push({ id: "Floor 1", label: "Floor 1", isVisible: true, isChecked: true });
                            });
                            vm.filterList.forEach(function (item) {
                                if (item.label == "Floor") {
                                    item.filterItemList = venueList;
                                }
                            });
                        });
                    break;
                case "Geofence":
                    var geofenceList = [];
                    geofenceList.push({ id: "Geofence 1", label: "Geofence 1", isVisible: true, isChecked: true });
                    geofenceList.push({ id: "Geofence 2", label: "Geofence 2", isVisible: true, isChecked: true });

                    vm.filterList.forEach(function (item) {
                        if (item.label == "Geofence") {
                            //item.isChecked = true;
                            item.filterItemList = geofenceList;
                        }
                        //else
                        //    item.isChecked = false;
                    });
                    break;
                case "Location":
                    var locationList = [];
                    locationList.push({ id: "Location 1", label: "Location 1", isVisible: true, isChecked: true });
                    locationList.push({ id: "Location 2", label: "Location 2", isVisible: true, isChecked: true });

                    vm.filterList.forEach(function (item) {
                        if (item.label == "Location") {
                            //item.isChecked = true;
                            item.filterItemList = locationList;
                        }
                        //else
                        //    item.isChecked = false;
                    });
                    break;
                default:
            }
        };

        //vm.colors = ['#777777', '#E74C3C', '#0B62A4', '#777777'];
        //vm.opt = {
        //    'showScale': false,
        //    'showTooltips': false,
        //    'responsive': false,
        //    'maintainAspectRatio': false,
        //    'pointDot': false,
        //    'bezierCurve': true,
        //    'datasetFill': true,
        //    'animation': false,

        //    'barShowStroke': true,
        //    'barStrokeWidth': 5,
        //    'barValueSpacing': 1,
        //    'barDatasetSpacing': 1,

        //    'fillColor': 'rgba(220,220,220,0.9)'
        //};

        vm.previewOrSave = function (frm, isPreview) {
            messagingService.broadcastCheckFormValidatity();
            validateForm(frm, isPreview);
        };

        function validateForm(frm, isPreview) {
            if (frm.$valid) {
                vm.payload = {};
                vm.payload.filters = [];
                vm.payload.metrics = [];
                vm.payload.buckets = [];
                vm.payload.chartType = vm.chart;
                var filter = {};
                var value = [];

                var selectedFilterList = $linq.Enumerable().From(vm.filterList)
                    .Where(function (x) {
                        return x.isChecked == true
                    }).ToArray();

                if (selectedFilterList && selectedFilterList.length > 0) {
                    selectedFilterList.forEach(function (element) {
                        value = [];
                        if (element.selectedfilterItemList && element.selectedfilterItemList.length > 0) {
                            element.selectedfilterItemList.forEach(function (item) {
                                if (element.filterItemList.length > 0) {
                                    var tagNameLst = $linq.Enumerable().From(element.filterItemList)
                                        .Where(function (x) {
                                            return x.id == item.id
                                        }).ToArray();
                                    if (tagNameLst != undefined && tagNameLst.length > 0) {
                                        if(element.fieldName && (element.fieldName == 'sid' || element.fieldName == 'spid' || element.fieldName == 'location' || element.fieldName == 'geofence')) {
                                            value.push(tagNameLst[0].id);
                                        } else {
                                            value.push(tagNameLst[0].label);
                                        }
                                        
                                    }
                                }
                            });
                        }
                        if (value.length != undefined && value.length != 0) {
                            vm.payload.filters.push({
                                "fieldname": element.fieldName,
                                "operation": "is",
                                "value": value
                            });
                        }
                    });
                }

                var selectedMetrics = $linq.Enumerable().From(vm.metricsList)
                    .Where(function (x) {
                        return x.customLabel == vm.selectedCategory1
                    }).ToArray();
                if (selectedMetrics && selectedMetrics.length > 0) {
                    if (vm.category1Name != undefined && vm.category1Name != "") {
                        vm.payload.metrics.push({
                            "customLabel": vm.category1Name,
                            "aggOperation": selectedMetrics[0].aggOperation,
                            "fieldName": selectedMetrics[0].fieldName
                        });
                    }
                    else {
                        vm.payload.metrics.push({
                            "customLabel": selectedMetrics[0].customLabel,
                            "aggOperation": selectedMetrics[0].aggOperation,
                            "fieldName": selectedMetrics[0].fieldName
                        });
                    }
                }

                var selectedBuckets = $linq.Enumerable().From(vm.bucketList)
                    .Where(function (x) {
                        return x.customLabel == vm.selectedCategory2
                    }).ToArray();
                if (selectedBuckets && selectedBuckets.length > 0) {
                    if (vm.category2Name != undefined && vm.category2Name != "") {
                        vm.payload.buckets.push({
                            "customLabel": vm.category2Name,
                            "aggOperation": selectedBuckets[0].customLabel,
                            "fieldName": selectedBuckets[0].fieldName
                        });
                    }
                    else {
                        vm.payload.buckets.push({
                            "customLabel": selectedBuckets[0].customLabel,
                            "aggOperation": selectedBuckets[0].customLabel,
                            "fieldName": selectedBuckets[0].fieldName
                        });
                    }
                }

                if (vm.fromDate && vm.toDate && new Date(vm.toDate) <= new Date()) {
                    if (new Date(vm.toDate) < new Date(vm.fromDate)) {
                        notificationBarService.error("To date cannot be less than the from date");
                        return;
                    }
                    var from = moment(vm.fromDate).format('YYYY-MM-DD[T]HH:mm:ss.SSS');
                    // var endTs = new Date();
                    // var toDate = vm.toDate.setHours(23, 59, 59, 999);
                    // var to = moment(toDate).format('YYYY-MM-DD[T]HH:mm:ss.SSS');
                    var to = moment((new Date((new Date(vm.toDate.toString())).setHours(23,59,59)))).format('YYYY-MM-DD[T]HH:mm:ss.SSS');
                    
                    if (isPreview) {
                        // Preview
                        visualizationService.preview(vm.payload, from, to)
                            .then(function (result) {
                                vm.labels = [];
                                vm.data = [];
                                vm.series = [];
                                vm.options = [];
                                vm.tableChart = [];
                                console.log(JSON.stringify(result));
                                //vm.labels = ["Qubercomm floor"];
                                //vm.series = ['AVG_TIMESPENT'];
                                //vm.data = [
                                //    [111.34]
                                //];

                                if (result && result.status) {
                                    if (result.data.length >= 1) {
                                        var chart = angular.copy(result);
                                        if(result.columns.length == 0 && result.data.length > 0) {
                                            return null;
                                        }
                                        angular.forEach(chart.data, function (value, key) {
                                            value.splice(0, 1);
                                        });
                                        vm.tableChart = chart;

                                        var cdata = [];
                                        var max = 0.0;
                                        var min = 0.0;
                                        var stepSize = 0;
                                        var i;

                                    if(vm.chart == "stackedbar") {
                                        vm.labels = result.data[0];
                                        vm.series = result.data[1];
                                        vm.stackedData = result.data[2];
                                        vm.data.push(vm.stackedData);

                                    } else{
                                        result.data[0].splice(0, 1);
                                        result.data[1].splice(0, 1);


                                        vm.labels = result.data[0];
                                        cdata = result.data[1];
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
                                        // Check if further expansion is required
                                     
                                        vm.data.push(cdata);
                                        result.columns.splice(0, 1);
                                        vm.series = result.columns;
                                     }
                                        
                                        // if (vm.category1Name && vm.category2Name) {
                                            var cat1Name = "";
                                            var cat2Name = "";
                                            
                                            if(vm.category1Name) {
                                                cat1Name = vm.category1Name + ' (' + result.units + ')';
                                            } else {
                                                cat1Name = vm.selectedCategory1 + ' (' + result.units + ')';
                                            }
                                            if(vm.category2Name) {
                                                cat2Name = vm.category2Name;
                                            } else {
                                                cat2Name = vm.selectedCategory2;
                                            }
                                            vm.options = {
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
                                   
                                        console.log("-------------------");
                                        console.log(JSON.stringify(vm.labels));
                                        console.log(JSON.stringify(vm.series));
                                        console.log(JSON.stringify(vm.data));
                                        
                                        console.log("-------------------");
                                        console.log(JSON.stringify(vm.labels));
                                        console.log(JSON.stringify(vm.series));
                                        console.log(JSON.stringify(vm.data));
                                        
                                        vm.datasets = [];
                                        var backgroundColors = [];
                                        var pointBackgroundColors = [];
                                        if (cdata != undefined && cdata.length > 0) {
                                            for (var i in cdata) {
                                                if (vm.chart == 'line')
                                                    pointBackgroundColors.push(dynamicColors());
                                                else
                                                    backgroundColors.push(dynamicColors());
                                            }
                                        }
                                        var chartType = "";
                                        if (vm.chart == 'bar'){
                                            chartType = document.getElementById('barchartdiv');
                                            chartType.innerHTML='&nbsp';
                                            $('#barchartdiv').append('<canvas id="barchart" chart-click="vm.onClick"></canvas>');
                                            ctx = document.getElementById('barchart').getContext('2d');
                                        }
                                        if (vm.chart == 'line'){
                                            chartType = document.getElementById('linechartdiv');
                                            chartType.innerHTML = '&nbsp';
                                            $('#linechartdiv').append('<canvas id="linechart" chart-click="vm.onClick"></canvas>');
                                            ctx = document.getElementById('linechart').getContext('2d');
                                        }
                                        if (vm.chart == 'pie'){
                                            chartType = document.getElementById('piechartdiv');
                                            chartType.innerHTML = '&nbsp';
                                            $('#piechartdiv').append('<canvas id="piechart" chart-click="vm.onClick"></canvas>');
                                            ctx = document.getElementById('piechart').getContext('2d');
                                        }
                                        if (vm.chart == 'line') {
                                            vm.datasets = [{
                                                label: vm.series,
                                                data: cdata,
                                                pointBackgroundColor: pointBackgroundColors
                                            }];
                                        }
                                        else if(vm.chart == 'stackedbar') {
                                            // vm.labels=['Geo 1', 'Geo 2', 'Geo 3', 'Geo 4'];
                                            // vm.stackedData =  [[6500, 5900, 3000, 8100],[2800, 4800, 4000, 1900]];
                                            // vm.series = ["A","B"];
                                            if (vm.series != undefined && vm.series.length > 0) {
                                                for (var i in vm.series) {
                                                    backgroundColors.push(dynamicColors());
                                                }
                                            }
                                             vm.datasets = [{
                                                backgroundColor: backgroundColors,
                                            }];
                                            vm.barcolor = backgroundColors;
                                            vm.options.scales.yAxes[0].stacked = true;
                                            vm.options.scales.xAxes[0].stacked = true;
                                            vm.options.scales.xAxes[0].ticks = {};
                                            vm.options.scales.yAxes[0].ticks = {};
                                            vm.options.scales.xAxes[0].scaleLabel.labelString = "Tag ID";
                                        }
                                        else {
                                            vm.datasets = [{
                                                label: vm.series,
                                                data: cdata,
                                                backgroundColor: backgroundColors,
                                            }];
                                        }
                                        var myChart = new Chart(ctx, {
                                            type: vm.chart,
                                            data: {
                                                labels: vm.labels,
                                                datasets: vm.datasets
                                            },
                                            options: {
                                                scales: {
                                                    yAxes: vm.options.scales.yAxes,
                                                    xAxes: vm.options.scales.xAxes
                                                }
                                            }
                                        });
                                        myChart.update();
                                    }
                                }
                            });
                    } else {
                        // Save
                        var name = vm.selectedVisual != undefined ? vm.selectedVisual.name : '';
                        var description = vm.selectedVisual != undefined ? vm.selectedVisual.description : '';
                        modalService.visualconfirmModal(name, description).result.then(function (res) {
                            vm.payload.name = res.name;
                            vm.payload.description = res.description;
                            vm.payload.entityType = "visualization";
                            if (visualid != '' && visualid != '0')
                                vm.payload.id = visualid;
                            visualizationService.save(vm.payload, from, to).then(function (result) {
                                if (result && result.success)
                                    navigation.goToVisualization();
                                notificationBarService.success(result.body);
                            });
                        }, function () {
                        });
                    }
                }
                else
                    notificationBarService.error("To date cannot be greater than the today date");
            }
        }
        var dynamicColors = function () {
            var r = Math.floor(Math.random() * 255);
            var g = Math.floor(Math.random() * 255);
            var b = Math.floor(Math.random() * 255);
            return "rgb(" + r + "," + g + "," + b + ")";
        };
        vm.cancel = function () {
            var message = "<p>The changes to the Visualization have not been saved yet. Are you sure you want to cancel the changes?</p>";
            modalService.questionModal('Visualization Cancellation', message, true).result.then(function () {
                navigation.goToVisualization();
            });

        };

        function activate() {
            vm.fromDate = moment(new Date()).format('YYYY/MM/DD');
            vm.toDate = moment(new Date()).format('YYYY/MM/DD');
            vm.selectedFiterItems = [];
            vm.dropdownsettings = { enableSearch: false, scrollable: true, showCheckAll: false, showUncheckAll: false };
            vm.searchFilter = '';

            vm.metricsList = [];
            vm.bucketList = [];
            vm.filterList = [];
            if(vm.chart == 'stackedbar'){
                vm.metricsList.push({ customLabel: "Timespent by Tags", label: "Timespent by Tags", fieldName: "Timespent by Tags", aggOperation: "timespent by tags" });
                vm.metricsList.push({ customLabel: "Geofence Occupancy", label: "Geofence Occupancy", fieldName: "Geofence Occupancy", aggOperation: "geofence occupancy" });
                
            }else {
                vm.metricsList.push({ customLabel: "Total Timespent", label: "Total Timespent", fieldName: "Total Timespent", aggOperation: "total timespent" });
                vm.metricsList.push({ customLabel: "Average Timespent", label: "Average Timespent", fieldName: "Average Timespent", aggOperation: "avg timespent" });
                vm.metricsList.push({ customLabel: "Tag Count", label: "Tag Count", fieldName: "Tag Count", aggOperation: "tag count" });
                vm.metricsList.push({ customLabel: "Attendence", label: "Attendence", fieldName: "Attendence", aggOperation: "Attendence" });
                vm.metricsList.push({ customLabel: "Hourly Geofence Occupancy", label: "occupancy_geofence", fieldName: "occupancy_geofence", aggOperation: "occupancy_geofence" });

            }
            
            if (visualid != '' && visualid != '0')
                vm.getVisualization(visualid);
        }

        vm.getVisualization = function (visualid) {
            visualizationService.getVisual(visualid).then(function (result) {
                isEdit = true;
                vm.selectedVisual = result;
                vm.selectedCategory1 = vm.selectedVisual.metrics[0].fieldName;
                vm.selectedCategory2 = vm.selectedVisual.buckets[0].aggOperation;
                vm.onCategory1Change(vm.selectedCategory1);
                //trigger preview
                $timeout(function() {
                 $(".preclick").click(); 
                }, 1000);                
            });
        };

        vm.onCategory1Change = function (selectedFilter) {
            // debugger;
            vm.bucketList = [];
            vm.selectedCategory2 = "";
            switch (selectedFilter) {
                case "Total Timespent":
                    createYAxis(false, selectedFilter);
                    break;
                case "Average Timespent":
                    createYAxis(false, selectedFilter);
                    break;
                case "Tag Count":
                    createYAxis(true, selectedFilter);
                    break;
                case "Attendence":
                        createYAxis('attendence', selectedFilter);
                        break;
                case "Hourly Geofence Occupancy":
                        createYAxis('Hourly Geofence Occupancy', selectedFilter);
                        break;
                default:
                    createYAxis(false, selectedFilter);
            }

        };

        function createYAxis(isAll, selectedFilter) {
            // debugger;
            if (isAll) {
                if(isAll == "attendence"){                   
                    vm.bucketList.push({ id: 6, label: "tags", aggOperation: "", customLabel: "tags", fieldName: "tagid" });                   
                } else if(isAll == "Hourly Geofence Occupancy") {
                    vm.bucketList.push({ id: 7, label: "geofence", aggOperation: "", customLabel: "geofence", fieldName: "geofence" }); 
                } else {
                    vm.bucketList.push({ id: 1, label: "Venue", aggOperation: "", customLabel: "Venue", fieldName: "sid" });
                    vm.bucketList.push({ id: 2, label: "Floor", aggOperation: "", customLabel: "Floor", fieldName: "spid" });
                    vm.bucketList.push({ id: 3, label: "Geofence", aggOperation: "", customLabel: "Geofence", fieldName: "geofence" });
                    vm.bucketList.push({ id: 4, label: "Location", aggOperation: "", customLabel: "Location", fieldName: "location" });
                    vm.bucketList.push({ id: 5, label: "Tag Type", aggOperation: "", customLabel: "Tag Type", fieldName: "tagtype" });
                } 
                
            }
            else {
                // vm.bucketList.push({ id: 0, label: "Select", aggOperation: "", customLabel: "", fieldName: "" });
                if(selectedFilter == 'Timespent by Tags'){
                    vm.bucketList.push({ id: 1, label: "Venue", aggOperation: "", customLabel: "Venue", fieldName: "sid" });
                    vm.bucketList.push({ id: 2, label: "Floor", aggOperation: "", customLabel: "Floor", fieldName: "spid" }); 
                }
                else if(selectedFilter == 'Geofence Occupancy'){
                    vm.bucketList.push({ id: 3, label: "Geofence", aggOperation: "", customLabel: "Geofence", fieldName: "geofence" });
                }
                else {
                    vm.bucketList.push({ id: 1, label: "Venue", aggOperation: "", customLabel: "Venue", fieldName: "sid" });
                    vm.bucketList.push({ id: 2, label: "Floor", aggOperation: "", customLabel: "Floor", fieldName: "spid" });
                    vm.bucketList.push({ id: 3, label: "Geofence", aggOperation: "", customLabel: "Geofence", fieldName: "geofence" });
                    vm.bucketList.push({ id: 4, label: "Location", aggOperation: "location", customLabel: "Location", fieldName: "location" });
                }
                
            }

            if (vm.selectedVisual != '' && vm.selectedVisual != undefined) {
                var selectedBucket = $linq.Enumerable().From(vm.bucketList)
                    .Where(function (x) {
                        return x.customLabel.toLowerCase() == vm.selectedVisual.buckets[0].aggOperation.toLowerCase()
                    }).ToArray();
                if (vm.selectedVisual.metrics[0].customLabel) {
                    vm.category1Name = vm.selectedVisual.metrics[0].customLabel;
                }

                if (vm.selectedVisual.buckets[0].customLabel) {
                    vm.category2Name = vm.selectedVisual.buckets[0].customLabel;
                }

                if (selectedBucket.length != 0) {
                    if (isEdit)
                        vm.selectedCategory2 = selectedBucket[0].customLabel;
                    else
                        vm.selectedCategory2 = "Venue";
                }
                vm.onCategory2Change(vm.selectedCategory1);
            }
            else
                vm.onCategory2Change(selectedFilter);
        }

        vm.onCategory2Change = function (selectedFilter) {
            if(selectedFilter == "Attendence" || selectedFilter == "Hourly Geofence Occupancy"){
                $(".temphide").hide();
                $(".tempdate").show();
                $(".temphidecat").css("pointer-events","none");
             } else {
                $(".temphide").show();
                $(".tempdate").hide();
                $(".temphidecat").css("pointer-events","auto");
             }
            vm.filterList = [];
            vm.selectedFiterItems = [];
            vm.filterItemList = [];
            switch (selectedFilter) {
                case "Total Timespent":
                case "Timespent by Tags":{
                    vm.getAllTagIds();
                    break;
                };
                case "Average Timespent":
                    vm.getAllTagType();
                    break;
                case "Attendence":                    
                    vm.getAllTagType();
                    vm.getAllTagIds();
                    break;
                case "Hourly Geofence Occupancy":
                    vm.getAllGeofenceIds();
                    break;
                case "Tag Count":
                    // Fill the filter list item for tag
                    vm.fillFilterListItemsForTag();
                    vm.tagCount();                    
                    if (vm.selectedVisual != undefined) {
                        vm.selectedfilterItemList = [];
                        if (isEdit) {
                            vm.selectedVisual.filters.forEach(function (element, index) {
                                var selectedFilter = $linq.Enumerable().From(vm.filterList)
                                    .Where(function (x) {
                                        return x.fieldName == element.fieldname
                                    }).ToArray();
                                if (selectedFilter && selectedFilter.length > 0) {
                                    var filter = selectedFilter[0];
                                    filter.isChecked = true;
                                    bindExstingFilter(filter);
                                }
                            });
                            isEdit = false;
                        }
                    }
                    break;
                case "Geofence Occupancy":
                    vm.filterList.push({ id: "Geofence", label: "Geofence", isVisible: true, isChecked: false, fieldName: "geofence", filterItemList: [], selectedfilterItemList: [] });
                    break;
                default:
            }
        };

        vm.tagCount = function () {
            vm.filterList.push({ id: "Venue", label: "Venue", isVisible: true, isChecked: false, fieldName: "sid", filterItemList: [], selectedfilterItemList: [] });
            vm.filterList.push({ id: "Floor", label: "Floor", isVisible: true, isChecked: false, fieldName: "spid", filterItemList: [], selectedfilterItemList: [] });
            vm.filterList.push({ id: "Geofence", label: "Geofence", isVisible: true, isChecked: false, fieldName: "geofence", filterItemList: [], selectedfilterItemList: [] });
            vm.filterList.push({ id: "Location", label: "Location", isVisible: true, isChecked: false, fieldName: "location", filterItemList: [], selectedfilterItemList: [] });
            vm.filterList.push({ id: "Tag Type", label: "Tag Type", isVisible: true, isChecked: false, fieldName: "tagtype", filterItemList: [], selectedfilterItemList: [] });
        }

        vm.getAllGeofenceIds = function () {
            vm.filterItemList = [];
            geoFenceService.getAllGeoFenceIds()
                .then(function (result) {
                   
                    result.forEach(function (element) {
                        // console.log("the ids are" + element.name)
                        vm.filterItemList.push({ id: element.id, label: element.name, isVisible: true, isChecked: false });
                    });
                    
                });
                if (vm.selectedVisual != undefined) {
                    vm.selectedfilterItemList = [];
                    if (!isEdit)
                        vm.filterList.push({ id: 1, label: "GeofenceIdId", isVisible: true, isChecked: false, fieldName: "location", filterItemList: vm.filterItemList, selectedfilterItemList: vm.selectedfilterItemList });                  
                }
                else {
                    vm.filterList.push({ id: 1, label: "GeofenceId", isVisible: true, isChecked: false, fieldName: "location", filterItemList: vm.filterItemList, selectedfilterItemList: [] });
                }
            }

        vm.getAllTagIds = function () {
            vm.filterItemList = [];
            tagService.getInusedTagList()
                .then(function (result) {
                    result.forEach(function (element) {
                        vm.filterItemList.push({ id: element.id, label: element.macaddr, isVisible: true, isChecked: false });
                    });
                    if(isEdit && vm.filterItemList != undefined && vm.filterItemList.length > 0){
                        vm.selectedVisual.filters.forEach(function (element) {
                            element.value.forEach(function (item) {
                                if (vm.filterItemList.length > 0) {
                                    var tagIdLst = $linq.Enumerable().From(vm.filterItemList)
                                        .Where(function (x) {
                                            return x.label == item
                                        }).FirstOrDefault();
                                    if (tagIdLst != undefined) {
                                        vm.selectedfilterItemList.push({ id: tagIdLst.id });
                                    }
                                }
                            });
                            vm.filterList.push({ id: 1, label: "Tag Id", isVisible: true, isChecked: true, fieldName: "tagid", filterItemList: vm.filterItemList, selectedfilterItemList: vm.selectedfilterItemList });
                        });
                        isEdit = false;
                }
                });
            if (vm.selectedVisual != undefined) {
                vm.selectedfilterItemList = [];
                if (!isEdit)
                    vm.filterList.push({ id: 1, label: "Tag Id", isVisible: true, isChecked: false, fieldName: "tagid", filterItemList: vm.filterItemList, selectedfilterItemList: vm.selectedfilterItemList });
                // else {
                //     vm.selectedVisual.filters.forEach(function (element) {
                //         element.value.forEach(function (item) {
                //             vm.selectedfilterItemList.push({ id: item });
                //         });
                //         vm.filterList.push({ id: 1, label: "Tag Id", isVisible: true, isChecked: true, fieldName: "tagid", filterItemList: vm.filterItemList, selectedfilterItemList: vm.selectedfilterItemList });
                //     });
                //     isEdit = false;
                // }
            }
            else
                vm.filterList.push({ id: 1, label: "Tag Id", isVisible: true, isChecked: false, fieldName: "tagid", filterItemList: vm.filterItemList, selectedfilterItemList: [] });
        }
        vm.fillFilterListItemsForTag = function() {
            vm.filterItemList = [];
            vm.filterItemList.push({ id: "Contractor", label: "Contractor", isVisible: true, isChecked: false });
            vm.filterItemList.push({ id: "Employee", label: "Employee", isVisible: true, isChecked: false });
            vm.filterItemList.push({ id: "Visitor", label: "Visitor", isVisible: true, isChecked: false });
        }

        vm.getAllTagType = function () {
            vm.filterItemList = [];
            vm.selectedfilterItemList = [];
            vm.filterItemList.push({ id: "Contractor", label: "Contractor", isVisible: true, isChecked: false });
            vm.filterItemList.push({ id: "Employee", label: "Employee", isVisible: true, isChecked: false });
            vm.filterItemList.push({ id: "Visitor", label: "Visitor", isVisible: true, isChecked: false });

            if (vm.selectedVisual != undefined) {
                if (!isEdit)
                    vm.filterList.push({ id: 1, label: "Tag Type", isVisible: true, isChecked: false, fieldName: "tagtype", filterItemList: vm.filterItemList, selectedfilterItemList: vm.selectedfilterItemList });
                else {
                    vm.selectedVisual.filters.forEach(function (element) {
                        element.value.forEach(function (item) {
                            vm.selectedfilterItemList.push({ id: item });
                        });
                        vm.filterList.push({ id: 1, label: "Tag Type", isVisible: true, isChecked: true, fieldName: "tagtype", filterItemList: vm.filterItemList, selectedfilterItemList: vm.selectedfilterItemList });
                    });
                    isEdit = false;
                }
            }
            else
                vm.filterList.push({ id: 1, label: "Tag Type", isVisible: true, isChecked: false, fieldName: "tagtype", filterItemList: vm.filterItemList, selectedfilterItemList: [] });
        }

        vm.selectFilter = {
            'onItemSelect': function (item) {
                handleSelectAndUnselectedItem(item);
            },
            'onItemDeselect': function (item) {
                handleSelectAndUnselectedItem(item);
            }
        }

        function handleSelectAndUnselectedItem(item) {
            var filter = [];
            var isFilterArray = false;
            var result = [];
            vm.filterList.forEach(function (element) {
                if (!isFilterArray) {
                    result = $linq.Enumerable().From(element.filterItemList)
                        .Where(function (x) {
                            return x.id == item.id
                        }).ToArray();

                    if (result.length > 0) {
                        isFilterArray = true;
                        filter = element;
                    }
                }
            });
            if (filter) {
                switch (filter.label) {
                    case "Venue":
                        if (filter.selectedfilterItemList.length >= 0) {
                            vm.filterList.forEach(function (item) {
                                if (item.label != "Venue") {
                                    item.isChecked = false;
                                    item.filterItemList = [];
                                    item.selectedfilterItemList = [];
                                }
                            });
                        }
                        break;
                    case "Floor":
                        if (filter.selectedfilterItemList.length >= 0) {
                            vm.filterList.forEach(function (item) {
                                if (item.label != "Venue" && item.label != "Floor") {
                                    item.isChecked = false;
                                    item.filterItemList = [];
                                    item.selectedfilterItemList = [];
                                }
                            });
                        }
                        break;
                    case "Geofence":
                        if (filter.selectedfilterItemList.length >= 0) {
                            vm.filterList.forEach(function (item) {
                                if (item.label != "Venue" && item.label != "Floor" && item.label != "Geofence") {
                                    item.isChecked = false;
                                    item.filterItemList = [];
                                    item.selectedfilterItemList = [];
                                }
                            });
                        }
                        break;
                    case "Location":
                        if (filter.selectedfilterItemList.length >= 0) {
                            vm.filterList.forEach(function (item) {
                                if (item.label == "Tag Type") {
                                    item.isChecked = false;
                                    item.filterItemList = [];
                                    item.selectedfilterItemList = [];
                                }
                            });
                        }
                        break;
                    case "Tag Type":
                        if (filter.selectedfilterItemList.length == 0) {
                            vm.filterList.forEach(function (item) {
                                if (item.label == "Tag Type") {
                                    //item.isChecked = false;
                                    //item.filterItemList = [];
                                    item.selectedfilterItemList = [];
                                }
                            });
                        }
                        break;
                    default:
                    // handle
                }
            }
        }

        vm.filterChecked = function (selectedFilter) {
            switch (selectedFilter.label) {
                case "Venue":
                    if (!selectedFilter.isChecked) {
                        vm.filterList.forEach(function (item) {
                            item.filterItemList = [];
                            item.isChecked = false;
                            item.selectedfilterItemList = [];
                        });
                    }
                    else {
                        var venueList = [];
                        venuedataservice.getVenueList()
                            .then(function (result) {
                                result.forEach(function (element) {
                                    venueList.push({ id: element.id, label: element.uid, isVisible: true, isChecked: false });
                                });

                                vm.filterList.forEach(function (item) {
                                    if (item.label == "Venue") {
                                        item.filterItemList = venueList;
                                    }
                                    else {
                                        item.isChecked = false;
                                        item.filterItemList = [];
                                        item.selectedfilterItemList = [];
                                    }
                                });
                            });
                    }
                    break;
                case "Floor":
                    if (!selectedFilter.isChecked) {
                        vm.filterList.forEach(function (item) {
                            if (item.label != "Venue") {
                                item.filterItemList = [];
                                item.isChecked = false;
                                item.selectedfilterItemList = [];
                            }
                        });
                    }
                    else {
                        var selectedFilterList = $linq.Enumerable().From(vm.filterList)
                            .Where(function (x) {
                                return x.isChecked == true && x.label == 'Venue'
                            }).ToArray();
                        if (selectedFilterList && selectedFilterList.length > 0) {
                            if (selectedFilterList[0].selectedfilterItemList.length == 0) {
                                angular.forEach(vm.filterList, function (item) {
                                    if (item.label == selectedFilter.label)
                                        item.isChecked = false;
                                });
                                notificationBarService.warning("Atleast select any one venue");
                                return;
                            }
                            var commaSeperatedList = '';
                            angular.forEach(selectedFilterList[0].selectedfilterItemList, function (value, index) {
                                if (index > 0)
                                    commaSeperatedList += ',';
                                commaSeperatedList += value.id;
                            });
                            getAllFloor(commaSeperatedList);
                        }
                        else {
                            var commaSeperatedList = '';
                            getAllFloor(commaSeperatedList);
                        }
                    }
                    break;
                case "Geofence":
                    if (!selectedFilter.isChecked) {
                        vm.filterList.forEach(function (item) {
                            if (item.label != "Venue" && item.label != "Floor") {
                                item.filterItemList = [];
                                item.isChecked = false;
                                item.selectedfilterItemList = [];
                            }
                        });
                    }
                    else {
                        var selectedFilterList = $linq.Enumerable().From(vm.filterList)
                            .Where(function (x) {
                                return (x.isChecked == true && x.label == 'Venue') || (x.isChecked == true && x.label == 'Floor')
                            }).ToArray();
                        var isValid = true;
                        if (selectedFilterList && selectedFilterList.length > 0) {
                            angular.forEach(selectedFilterList, function (item) {
                                if (item.selectedfilterItemList.length == 0) {
                                    angular.forEach(vm.filterList, function (item) {
                                        if (item.label == selectedFilter.label)
                                            item.isChecked = false;
                                    });
                                    isValid = false;
                                    notificationBarService.warning("Atleast select any one " + item.label);
                                    return;
                                }
                                return;
                            });

                            if (isValid) {
                                var venue = $linq.Enumerable().From(vm.filterList)
                                    .Where(function (x) {
                                        return x.isChecked == true && x.label == 'Venue'
                                    }).ToArray();

                                var commaSeperatedVenueList = '';
                                if (venue && venue.length > 0) {
                                    angular.forEach(venue[0].selectedfilterItemList, function (value, index) {
                                        if (index > 0)
                                            commaSeperatedVenueList += ',';
                                        commaSeperatedVenueList += value.id;
                                    });
                                }

                                var flr = $linq.Enumerable().From(vm.filterList)
                                    .Where(function (x) {
                                        return x.isChecked == true && x.label == 'Floor'
                                    }).ToArray();

                                var commaSeperatedFlrList = '';
                                if (flr && flr.length > 0) {
                                    angular.forEach(flr[0].selectedfilterItemList, function (value, index) {
                                        if (index > 0)
                                            commaSeperatedFlrList += ',';
                                        commaSeperatedFlrList += value.id;
                                    });
                                }

                                var geofence = $linq.Enumerable().From(vm.filterList)
                                    .Where(function (x) {
                                        return x.isChecked == true && x.label == 'Geofence'
                                    }).ToArray();

                                var commaSeperatedGeofenceList = '';
                                if (geofence && geofence.length > 0) {
                                    angular.forEach(geofence[0].selectedfilterItemList, function (value, index) {
                                        if (index > 0)
                                            commaSeperatedGeofenceList += ',';
                                        commaSeperatedGeofenceList += value.id;
                                    });
                                }

                                getAllGeofence(commaSeperatedVenueList, commaSeperatedFlrList, commaSeperatedGeofenceList);
                            }
                        }
                        else {
                            if (isValid) {
                                var commaSeperatedVenueList = '';
                                var commaSeperatedFlrList = '';
                                var commaSeperatedGeofenceList = '';
                                getAllGeofence(commaSeperatedVenueList, commaSeperatedFlrList, commaSeperatedGeofenceList);
                            }
                        }
                    }
                    break;
                case "Location":
                    if (!selectedFilter.isChecked) {
                        vm.filterList.forEach(function (item) {
                            if (item.label == "Location" || item.label == "Tag Type") {
                                item.filterItemList = [];
                                item.isChecked = false;
                                item.selectedfilterItemList = [];
                            }
                        });
                    }
                    else {
                        var selectedFilterList = $linq.Enumerable().From(vm.filterList)
                            .Where(function (x) {
                                return (x.isChecked == true && x.label == 'Venue') || (x.isChecked == true && x.label == 'Floor') || (x.isChecked == true && x.label == 'Geofence')
                            }).ToArray();
                        var isValid = true;
                        if (selectedFilterList && selectedFilterList.length > 0) {
                            angular.forEach(selectedFilterList, function (item) {
                                if (item.selectedfilterItemList.length == 0) {
                                    angular.forEach(vm.filterList, function (item) {
                                        if (item.label == selectedFilter.label)
                                            item.isChecked = false;
                                    });
                                    isValid = false;
                                    notificationBarService.warning("Atleast select any one " + item.label);
                                    return;
                                }
                            });
                            if (isValid) {
                                var venue = $linq.Enumerable().From(vm.filterList)
                                    .Where(function (x) {
                                        return x.isChecked == true && x.label == 'Venue'
                                    }).ToArray();

                                var commaSeperatedVenueList = '';
                                if (venue && venue.length > 0) {
                                    angular.forEach(venue[0].selectedfilterItemList, function (value, index) {
                                        if (index > 0)
                                            commaSeperatedVenueList += ',';
                                        commaSeperatedVenueList += value.id;
                                    });
                                }

                                var flr = $linq.Enumerable().From(vm.filterList)
                                    .Where(function (x) {
                                        return x.isChecked == true && x.label == 'Floor'
                                    }).ToArray();

                                var commaSeperatedFlrList = '';
                                if (flr && flr.length > 0) {
                                    angular.forEach(flr[0].selectedfilterItemList, function (value, index) {
                                        if (index > 0)
                                            commaSeperatedFlrList += ',';
                                        commaSeperatedFlrList += value.id;
                                    });
                                }

                                var geofence = $linq.Enumerable().From(vm.filterList)
                                    .Where(function (x) {
                                        return x.isChecked == true && x.label == 'Geofence'
                                    }).ToArray();

                                var commaSeperatedGeofenceList = '';
                                if (geofence && geofence.length > 0) {
                                    angular.forEach(geofence[0].selectedfilterItemList, function (value, index) {
                                        if (index > 0)
                                            commaSeperatedGeofenceList += ',';
                                        commaSeperatedGeofenceList += value.id;
                                    });
                                }

                                getAllLocation(commaSeperatedVenueList, commaSeperatedFlrList, commaSeperatedGeofenceList);
                            }
                        }
                        else {
                            if (isValid) {
                                var venuIdLst = [];
                                var floorIdLst = [];
                                var geofenceLst = [];
                                getAllLocation(venuIdLst, floorIdLst, geofenceLst);
                            }
                        }
                    }
                    break;
                case "Tag Type":
                    if (!selectedFilter.isChecked) {
                        vm.filterList.forEach(function (item) {
                            if (item.label == "Tag Type") {
                                item.filterItemList = [];
                                item.isChecked = false;
                                item.selectedfilterItemList = [];
                            }
                        });
                    }
                    else {
                        var selectedFilterList = $linq.Enumerable().From(vm.filterList)
                            .Where(function (x) {
                                return x.isChecked == true && x.label == 'Venue' && x.label == 'Floor' && x.label == 'Geofence' && x.label == 'Location'
                            }).ToArray();
                        var isValid = true;
                        if (selectedFilterList && selectedFilterList.length > 0) {
                            angular.forEach(selectedFilterList, function (item) {
                                if (item.selectedfilterItemList.length == 0) {
                                    angular.forEach(vm.filterList, function (item) {
                                        if (item.label == selectedFilter.label)
                                            item.isChecked = false;
                                    });
                                    isValid = false;
                                    notificationBarService.warning("Atleast select any one " + item.label);
                                    return;
                                }
                            });
                            if (isValid) {
                                var venuIdLst = [];
                                var floorIdLst = [];
                                var geofenceLst = [];
                                var locationLst = [];
                                getAllTagTypeLst(venuIdLst, floorIdLst, geofenceLst, locationLst);
                            }
                        }
                        else {
                            if (isValid) {
                                var venuIdLst = [];
                                var floorIdLst = [];
                                var geofenceLst = [];
                                var tagTypeLst = [];
                                getAllTagTypeLst(venuIdLst, floorIdLst, geofenceLst, locationLst);
                            }
                        }
                    }
                    break;
                default:
            }
        };

        function bindExstingFilter(selectedFilter) {
            var venuIdLst = '';
            var floorIdLst = '';
            var geofenceIdLst = '';
            if (vm.selectedVisual != undefined) {
                vm.selectedVisual.filters.forEach(function (element) {
                    if (element.fieldname == "sid") {
                        element.value.forEach(function (value, index) {
                            if (index > 0)
                                venuIdLst += ',';
                            venuIdLst += value;
                        });
                    }
                    if (element.fieldname == "spid") {
                        element.value.forEach(function (value, index) {
                            if (index > 0)
                                floorIdLst += ',';
                            floorIdLst += value;
                        });
                    }
                    if (element.fieldname == "geofence") {
                        element.value.forEach(function (value, index) {
                            if (index > 0)
                                geofenceIdLst += ',';
                            geofenceIdLst += value;
                        });
                    }
                });
            }

            switch (selectedFilter.label) {
                case "Venue":
                    var venueList = [];
                    venuedataservice.getVenueList()
                        .then(function (result) {
                            result.forEach(function (element) {
                                venueList.push({ id: element.id, label: element.uid, isVisible: true, isChecked: false });
                            });

                            var selectedfilterVenueList = [];
                            if (vm.selectedVisual != undefined) {
                                vm.selectedVisual.filters.forEach(function (element) {
                                    if (element.fieldname == "sid") {
                                        element.value.forEach(function (value, index) {
                                            selectedfilterVenueList.push({ id: value });
                                        });
                                    }
                                });
                            }

                            vm.filterList.forEach(function (item) {
                                if (item.label == "Venue") {
                                    item.isChecked = true;
                                    item.filterItemList = venueList;
                                    item.selectedfilterItemList = selectedfilterVenueList;
                                }
                            });
                        });
                    break;
                case "Floor":
                    var floorList = [];
                    floordataservice.getFloorList(venuIdLst)
                        .then(function (result) {
                            result.forEach(function (element) {
                                floorList.push({ id: element.id, label: element.name, isVisible: true, isChecked: true });
                            });

                            var selectedFloorItemList = [];
                            if (vm.selectedVisual != undefined) {
                                vm.selectedVisual.filters.forEach(function (element) {
                                    if (element.fieldname == "spid") {
                                        element.value.forEach(function (value, index) {
                                            selectedFloorItemList.push({ id: value });
                                        });
                                    }
                                });

                                vm.filterList.forEach(function (item) {
                                    if (item.label == "Floor") {
                                        item.isChecked = true;
                                        item.filterItemList = floorList;
                                        item.selectedfilterItemList = selectedFloorItemList;
                                    }
                                });
                            }
                        });
                    break;
                case "Geofence":
                    var geofenceList = [];
                    geoFenceService.getAllGeoFenceList(venuIdLst, floorIdLst, geofenceIdLst)
                        .then(function (result) {
                            result.forEach(function (element) {
                                geofenceList.push({ id: element.id, label: element.name, isVisible: true, isChecked: true });
                            });

                            var selectedGeofenceItemList = [];
                            if (vm.selectedVisual != undefined) {
                                vm.selectedVisual.filters.forEach(function (element) {
                                    if (element.fieldname == "geofence") {
                                        element.value.forEach(function (value, index) {
                                            selectedGeofenceItemList.push({ id: value });
                                        });
                                    }
                                });
                            }

                            vm.filterList.forEach(function (item) {
                                if (item.label == "Geofence") {
                                    item.isChecked = true;
                                    item.filterItemList = geofenceList;
                                    item.selectedfilterItemList = selectedGeofenceItemList;
                                }
                            });

                        });
                    break;
                case "Location":
                    var locationList = [];
                    tagService.getAllLocationList(venuIdLst, floorIdLst, geofenceIdLst)
                        .then(function (result) {
                            result.forEach(function (element) {
                                locationList.push({ id: element.id, label: element.name, isVisible: true, isChecked: true });
                            });

                            var selectedLocationItemList = [];
                            if (vm.selectedVisual != undefined) {
                                vm.selectedVisual.filters.forEach(function (element) {
                                    if (element.fieldname == "location") {
                                        element.value.forEach(function (value, index) {
                                            selectedLocationItemList.push({ id: value });
                                        });
                                    }
                                });
                            }

                            vm.filterList.forEach(function (item) {
                                if (item.label == "Location") {
                                    item.isChecked = true;
                                    item.filterItemList = locationList;
                                    item.selectedfilterItemList = selectedLocationItemList;
                                }
                            });
                        });
                    break;
                case "Tag Type":
                    var selectedTagtypeItemList = [];
                    if (vm.selectedVisual != undefined) {
                        vm.selectedVisual.filters.forEach(function (element) {
                            if (element.fieldname == "tagtype") {
                                element.value.forEach(function (value, index) {
                                    selectedTagtypeItemList.push({ id: value });
                                });
                            }
                        });
                    }
                    var tagTypeList = [];
                    tagTypeList.push({ id: "Contractor", label: "Contractor", isVisible: true, isChecked: false });
                    tagTypeList.push({ id: "Employee", label: "Employee", isVisible: true, isChecked: false });
                    tagTypeList.push({ id: "Visitor", label: "Visitor", isVisible: true, isChecked: false });

                    vm.filterList.forEach(function (item) {
                        if (item.label == "Tag Type") {
                            item.isChecked = true;
                            item.filterItemList = tagTypeList;
                            item.selectedfilterItemList = selectedTagtypeItemList;
                        }
                    });
                    break;
                default:
            }
        };

        function getAllFloor(venuIdLst) {
            var floorList = [];
            floordataservice.getFloorList(venuIdLst)
                .then(function (result) {
                    result.forEach(function (element) {
                        floorList.push({ id: element.id, label: element.name, isVisible: true, isChecked: true });
                    });
                    vm.filterList.forEach(function (item) {
                        if (item.label == "Floor") {
                            item.filterItemList = floorList;
                        }
                        else if (item.label == "Geofence" || item.label == "Location" || item.label == "Tag Type") {
                            item.isChecked = false;
                            item.selectedfilterItemList = [];
                            item.filterItemList = [];
                        }
                    });
                });
        }

        function getAllGeofence(venuIdLst, floorIdLst, geofenceList) {
            var geofenceList = [];
            geoFenceService.getAllGeoFenceList(venuIdLst, floorIdLst, geofenceList)
                .then(function (result) {
                    result.forEach(function (element) {
                        geofenceList.push({ id: element.id, label: element.name, isVisible: true, isChecked: true });
                    });
                    vm.filterList.forEach(function (item) {
                        if (item.label == "Geofence") {
                            item.filterItemList = geofenceList;
                        }
                        else if (item.label == "Location" || item.label == "Tag Type") {
                            item.isChecked = false;
                            item.selectedfilterItemList = [];
                            item.filterItemList = [];
                        }
                    });
                });
        }

        function getAllLocation(venuIdLst, floorIdLst, geofenceLst) {
            var locationList = [];
            tagService.getAllLocationList(venuIdLst, floorIdLst, geofenceLst)
                .then(function (result) {
                    result.forEach(function (element) {
                        locationList.push({ id: element.id, label: element.name, isVisible: true, isChecked: true });
                    });
                    vm.filterList.forEach(function (item) {
                        if (item.label == "Location") {
                            item.filterItemList = locationList;
                        }
                        else if (item.label == "Tag Type") {
                            item.isChecked = false;
                            item.selectedfilterItemList = [];
                            item.filterItemList = [];
                        }
                    });
                });
        }

        function getAllTagTypeLst(venuIdLst, floorIdLst, geofenceLst, locationLst) {
            var tagTypeList = [];
            tagTypeList.push({ id: "Contractor", label: "Contractor", isVisible: true, isChecked: false });
            tagTypeList.push({ id: "Employee", label: "Employee", isVisible: true, isChecked: false });
            tagTypeList.push({ id: "Visitor", label: "Visitor", isVisible: true, isChecked: false });

            vm.filterList.forEach(function (item) {
                if (item.label == "Tag Type") {
                    item.filterItemList = tagTypeList;
                }
            });
        }

        activate();

        return vm;
    }
})();