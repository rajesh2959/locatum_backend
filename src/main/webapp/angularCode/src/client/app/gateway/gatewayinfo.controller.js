(function () {
  'use strict';
  angular
    .module('app.gateway')
    .controller('gatewayinfoController', controller);

    controller.$inject = ['gatewayinfoService', 'floordataservice', 'navigation', 'SimpleListScreenViewModel', 'notificationBarService', 'modalService', '$linq', '$timeout', 'session', 'environment', 'uid', '$interval', '$scope', 'sid', 'spid'];
  /* @ngInject */

    function controller(gatewayinfoService, floordataservice, navigation, SimpleListScreenViewModel, notificationBarService, modalService, $linq, $timeout, session, environment, uid, $interval, $scope, sid, spid) {

    var vm = this;
    var gatewayInfoChartUID = uid;
    vm.sid = sid;
    vm.spid = spid;
    var cid = session.cid;
    vm.prevPageDetail = JSON.parse(localStorage.getItem("prevPageInfo"));
    vm.devicesList = [{id: 1, label: "Ble", isChecked: false, isVisible: false}, {id: 2, label: "Ruckus-Ble",isChecked: false, isVisible: false},{id: 3, label: "All",isChecked: false, isVisible: false}];
    //BEGIN - Connected Interface
    document.getElementById("titleConnectedInterfaces").innerHTML = "Connected Interfaces";
    vm.wlan2gColour = "#999999";
    vm.wlan5gColour = "#999999";
    vm.bleColour = "#999999";
    vm.xbeeColour = "#999999";

    vm.gatewayinfo = function () {
      gatewayinfoService.gatewayinfo(gatewayInfoChartUID).then(function (result) {
        if (result && result.connectedInterfaces.length > 0) {
          for (var i = 0; i < result.connectedInterfaces.length; i++) {
            if (result.connectedInterfaces[i].device == "wlan2g" && result.connectedInterfaces[i].status == "enabled") {
              vm.wlan2gColour = "#20E2E2";
            }
            if (result.connectedInterfaces[i].device == "wlan5g" && result.connectedInterfaces[i].status == "enabled") {
              vm.wlan5gColour = "#20E2E2";
            }
            if (result.connectedInterfaces[i].device == "ble" && result.connectedInterfaces[i].status == "enabled") {
              vm.bleColour = "#20E2E2";
            }
            if (result.connectedInterfaces[i].device == "xbee" && result.connectedInterfaces[i].status == "enabled") {
              vm.xbeeColour = "#20E2E2";
            }
          }
        }

        Chart.defaults.doughnutLabels = Chart.helpers.clone(Chart.defaults.doughnut);
        var helpers = Chart.helpers;
        var defaults = Chart.defaults;
        Chart.defaults.global.animation.duration = 0
        Chart.controllers.doughnutLabels = Chart.controllers.doughnut.extend({
          updateElement: function (arc, index, reset) {
            var _this = this;
            var chart = _this.chart,
              chartArea = chart.chartArea,
              opts = chart.options,
              animationOpts = opts.animation,
              arcOpts = opts.elements.arc,
              centerX = (chartArea.left + chartArea.right) / 2,
              centerY = (chartArea.top + chartArea.bottom) / 2,
              startAngle = opts.rotation, // non reset case handled later
              endAngle = opts.rotation, // non reset case handled later
              dataset = _this.getDataset(),
              circumference = reset && animationOpts.animateRotate ? 0 : arc.hidden ? 0 : _this.calculateCircumference(dataset.data[index]) * (opts.circumference / (1.0 * Math.PI)),
              innerRadius = reset && animationOpts.animateScale ? 0 : _this.innerRadius,
              outerRadius = reset && animationOpts.animateScale ? 0 : _this.outerRadius,
              custom = arc.custom || {},
              valueAtIndexOrDefault = helpers.getValueAtIndexOrDefault;

            helpers.extend(arc, {
              // Utility
              _datasetIndex: _this.index,
              _index: index,

              // Desired view properties
              _model: {
                x: centerX + chart.offsetX,
                y: centerY + chart.offsetY,
                startAngle: startAngle,
                endAngle: endAngle,
                circumference: circumference,
                outerRadius: outerRadius,
                innerRadius: innerRadius,
                label: valueAtIndexOrDefault(dataset.label, index, chart.data.labels[index])
              },

              draw: function () {
                var ctx = this._chart.ctx,
                  vm = this._view,
                  sA = vm.startAngle,
                  eA = vm.endAngle,
                  opts = this._chart.config.options;

                var labelPos = this.tooltipPosition();
                var segmentLabel = vm.circumference / opts.circumference * 10;

                ctx.beginPath();
                this._chart.canvas.parentNode.style.height = '175px';
                this._chart.canvas.parentNode.style.width = 'auto';
                ctx.arc(vm.x, vm.y, vm.outerRadius, sA, eA);
                ctx.arc(vm.x, vm.y, vm.innerRadius, eA, sA, true);

                ctx.closePath();
                ctx.strokeStyle = vm.borderColor;
                ctx.lineWidth = vm.borderWidth;

                ctx.fillStyle = vm.backgroundColor;

                ctx.fill();
                ctx.lineJoin = 'bevel';

                if (vm.borderWidth) {
                  ctx.stroke();
                }

                if (vm.circumference > 0.0015) { // Trying to hide label when it doesn't fit in segment
                  ctx.beginPath();
                  ctx.font = helpers.fontString(opts.defaultFontSize, opts.defaultFontStyle, opts.defaultFontFamily);
                  ctx.fillStyle = "#ffffff";
                  ctx.textBaseline = "top";
                  ctx.textAlign = "center";
                  // Round percentage in a way that it always adds up to 100%
                  ctx.fillText("wlan2g", vm.x - 45, vm.y - 50);
                  ctx.fillText("wlan5g", vm.x + 45, vm.y - 50);
                  ctx.fillText("blee", vm.x - 43, vm.y + 40);
                  ctx.fillText("xbee", vm.x + 42, vm.y + 40);
                }

                // var total = dataset.data.reduce((sum, val) => sum + val, 0);                
                var total = dataset.data.reduce(function(sum, val) {
                  return sum + val;
                },0);
              }
            });

            var model = arc._model;
            model.backgroundColor = custom.backgroundColor ? custom.backgroundColor : valueAtIndexOrDefault(dataset.backgroundColor, index, arcOpts.backgroundColor);
            model.hoverBackgroundColor = custom.hoverBackgroundColor ? custom.hoverBackgroundColor : valueAtIndexOrDefault(dataset.hoverBackgroundColor, index, arcOpts.hoverBackgroundColor);
            model.borderWidth = custom.borderWidth ? custom.borderWidth : valueAtIndexOrDefault(dataset.borderWidth, index, arcOpts.borderWidth);
            model.borderColor = custom.borderColor ? custom.borderColor : valueAtIndexOrDefault(dataset.borderColor, index, arcOpts.borderColor);

            // Set correct angles if not resetting
            if (!reset || !animationOpts.animateRotate) {
              if (index === 0) {
                model.startAngle = opts.rotation;
              } else {
                model.startAngle = _this.getMeta().data[index - 1]._model.endAngle;
              }
              model.endAngle = model.startAngle + model.circumference;
            }
            arc.pivot();
          }
        });
        
        var config = {
          type: 'doughnutLabels',
          maintainAspectRatio: false,
          data: {
            datasets: [{
              data: [10, 10, 10, 10],
              backgroundColor: [
                vm.wlan2gColour,
                vm.wlan5gColour,
                vm.bleColour,
                vm.xbeeColour

              ],
              hoverBackgroundColor: [
                vm.wlan2gColour,
                vm.wlan5gColour,
                vm.bleColour,
                vm.xbeeColour
              ]
            }],
            labels: ["wlan5g", "xbee", "blee", "wlan2g"]
          },
          options: {
            circumference: Math.PI,
            responsive: true,
          }
        };
        var ctx = document.getElementById("connectedInterfacesChart").getContext("2d");
        window.upDownChart = new Chart(ctx, config);


      });
    };
    //END - Connected Interface

    //BEGIN Active Tags
    document.getElementById("titleActiveTags").innerHTML = "Active Tags";
    vm.gatewayActiveTagsInfo = function () {

      var CurrentActiveTags = 0;
      var CurrentInActiveTags = 0;
      gatewayinfoService.gatewayActiveTagsInfo(gatewayInfoChartUID, session.cid, vm.spid).then(function (result) {
        //$('#divcpuUtilizationChart').show();
        if (result && result.devicesConnected != undefined && result.devicesConnected.length > 0) {
          if (result.devicesConnected[1][0] == "Active Tags")
            CurrentActiveTags = result.devicesConnected[1][1];
          CurrentInActiveTags = 100 - CurrentActiveTags;
        }
        Chart.defaults.doughnutLabels = Chart.helpers.clone(Chart.defaults.doughnut);
        var helpers = Chart.helpers;
        var defaults = Chart.defaults;
        Chart.defaults.global.animation.duration = 0;
        Chart.controllers.doughnutLabels = Chart.controllers.doughnut.extend({
          updateElement: function (arc, index, reset) {
            var _this = this;
            var chart = _this.chart,
              chartArea = chart.chartArea,
              opts = chart.options,
              animationOpts = opts.animation,
              arcOpts = opts.elements.arc,
              centerX = (chartArea.left + chartArea.right) / 2,
              centerY = (chartArea.top + chartArea.bottom) / 2,
              startAngle = opts.rotation, // non reset case handled later
              endAngle = opts.rotation, // non reset case handled later
              dataset = _this.getDataset(),
              circumference = reset && animationOpts.animateRotate ? 0 : arc.hidden ? 0 : _this.calculateCircumference(dataset.data[index]) * (opts.circumference / (1.0 * Math.PI)),
              innerRadius = reset && animationOpts.animateScale ? 0 : _this.innerRadius,
              outerRadius = reset && animationOpts.animateScale ? 0 : _this.outerRadius,
              custom = arc.custom || {},
              valueAtIndexOrDefault = helpers.getValueAtIndexOrDefault;

            helpers.extend(arc, {
              // Utility
              _datasetIndex: _this.index,
              _index: index,

              // Desired view properties
              _model: {
                x: centerX + chart.offsetX,
                y: centerY + chart.offsetY,
                startAngle: startAngle,
                endAngle: endAngle,
                circumference: circumference,
                outerRadius: outerRadius,
                innerRadius: innerRadius,
                label: valueAtIndexOrDefault(dataset.label, index, chart.data.labels[index])
              },

              draw: function () {
                var ctx = this._chart.ctx,
                  vm = this._view,
                  sA = vm.startAngle,
                  eA = vm.endAngle,
                  opts = this._chart.config.options;

                var labelPos = this.tooltipPosition();
                var segmentLabel = vm.circumference / opts.circumference * 10;

                ctx.beginPath();
                this._chart.canvas.parentNode.style.height = '175px';
                this._chart.canvas.parentNode.style.width = 'auto';
                ctx.arc(vm.x, vm.y, vm.outerRadius, sA, eA);
                ctx.arc(vm.x, vm.y, vm.innerRadius, eA, sA, true);

                ctx.closePath();
                ctx.strokeStyle = vm.borderColor;
                ctx.lineWidth = vm.borderWidth;

                ctx.fillStyle = vm.backgroundColor;

                ctx.fill();
                ctx.lineJoin = 'bevel';

                if (vm.borderWidth) {
                  ctx.stroke();
                }

                if (vm.circumference > 0.0015) { // Trying to hide label when it doesn't fit in segment
                  ctx.beginPath();
                  ctx.font = helpers.fontString(opts.defaultFontSize, opts.defaultFontStyle, opts.defaultFontFamily);
                  ctx.fillStyle = "#190707";
                  ctx.textBaseline = "top";
                  ctx.textAlign = "center";
                }
                // var total = dataset.data.reduce((sum, val) => sum + val, 0);
                var total = dataset.data.reduce(function(sum, val) {
                  return sum + val;
                },0);
                ctx.fillText(CurrentActiveTags, vm.x, vm.y - 5, 200);
              }
            });

            var model = arc._model;
            model.backgroundColor = custom.backgroundColor ? custom.backgroundColor : valueAtIndexOrDefault(dataset.backgroundColor, index, arcOpts.backgroundColor);
            model.hoverBackgroundColor = custom.hoverBackgroundColor ? custom.hoverBackgroundColor : valueAtIndexOrDefault(dataset.hoverBackgroundColor, index, arcOpts.hoverBackgroundColor);
            model.borderWidth = custom.borderWidth ? custom.borderWidth : valueAtIndexOrDefault(dataset.borderWidth, index, arcOpts.borderWidth);
            model.borderColor = custom.borderColor ? custom.borderColor : valueAtIndexOrDefault(dataset.borderColor, index, arcOpts.borderColor);

            // Set correct angles if not resetting
            if (!reset || !animationOpts.animateRotate) {
              if (index === 0) {
                model.startAngle = opts.rotation;
              } else {
                model.startAngle = _this.getMeta().data[index - 1]._model.endAngle;
              }
              model.endAngle = model.startAngle + model.circumference;
            }
            arc.pivot();
          }
        });

        var config = {
          type: 'doughnutLabels',
          data: {
            datasets: [{
              data: [CurrentActiveTags, CurrentInActiveTags],
              backgroundColor: [
                "#20E2E2",
                "#999999"
              ]
            }],
            labels: ["Active Tags", "InActive Tags"]
          },
          options: {
            circumference: Math.PI,
            responsive: true,
          },
        };
        var ctx = document.getElementById("ativeTagsChart").getContext("2d");
        window.upDownChart = new Chart(ctx, config);

      });
    };
    //--END Active Tags

    //BEGIN CPU Utilization
    document.getElementById("titlecpuUtilizationChart").innerHTML = "CPU Utilization";
    vm.gatewayCPUUtilizationinfo = function () {
      var CurrentCpuUsage = 0;
      var RemainingCpuUsage = 0;
      var backgroundColor = 0;
      gatewayinfoService.gatewayCPUUtilizationinfo(gatewayInfoChartUID).then(function (result) {
        if (result && result.length > 0) {
          CurrentCpuUsage = result[0].cpu;
          RemainingCpuUsage = 100 - CurrentCpuUsage;
        }
        backgroundColor = CurrentCpuUsage <= 30 ? '#60b044' : CurrentCpuUsage <= 60 ? '#f6c600' : '#fe1101'; 
        if (result && result.length > 0) {
          Chart.defaults.global.animation.duration = 0;
          Chart.defaults.doughnutLabels = Chart.helpers.clone(Chart.defaults.doughnut);
          var helpers = Chart.helpers;
          var defaults = Chart.defaults;

          Chart.controllers.doughnutLabels = Chart.controllers.doughnut.extend({
            updateElement: function (arc, index, reset) {
              var _this = this;
              var chart = _this.chart,
                chartArea = chart.chartArea,
                opts = chart.options,
                animationOpts = opts.animation,
                arcOpts = opts.elements.arc,
                centerX = (chartArea.left + chartArea.right) / 2,
                centerY = (chartArea.top + chartArea.bottom) / 2,
                startAngle = opts.rotation, // non reset case handled later
                endAngle = opts.rotation, // non reset case handled later
                dataset = _this.getDataset(),
                circumference = reset && animationOpts.animateRotate ? 0 : arc.hidden ? 0 : _this.calculateCircumference(dataset.data[index]) * (opts.circumference / (2.0 * Math.PI)),
                innerRadius = reset && animationOpts.animateScale ? 0 : _this.innerRadius,
                outerRadius = reset && animationOpts.animateScale ? 0 : _this.outerRadius,
                custom = arc.custom || {},
                valueAtIndexOrDefault = helpers.getValueAtIndexOrDefault;

              helpers.extend(arc, {
                // Utility
                _datasetIndex: _this.index,
                _index: index,

                // Desired view properties
                _model: {
                  x: centerX + chart.offsetX,
                  y: centerY + chart.offsetY,
                  startAngle: startAngle,
                  endAngle: endAngle,
                  circumference: circumference,
                  outerRadius: outerRadius,
                  innerRadius: innerRadius,
                  label: valueAtIndexOrDefault(dataset.label, index, chart.data.labels[index])
                },

                draw: function () {
                  var ctx = this._chart.ctx,
                    vm = this._view,
                    sA = vm.startAngle,
                    eA = vm.endAngle,
                    opts = this._chart.config.options;

                  var labelPos = this.tooltipPosition();
                  var segmentLabel = vm.circumference / opts.circumference * 10;

                  ctx.beginPath();
                  this._chart.canvas.parentNode.style.height = '175px';
                  this._chart.canvas.parentNode.style.width = 'auto';
                  ctx.arc(vm.x, vm.y, vm.outerRadius, sA, eA);
                  ctx.arc(vm.x, vm.y, vm.innerRadius, eA, sA, true);

                  ctx.closePath();
                  ctx.strokeStyle = vm.borderColor;
                  ctx.lineWidth = vm.borderWidth;

                  ctx.fillStyle = vm.backgroundColor;

                  ctx.fill();
                  ctx.lineJoin = 'bevel';

                  if (vm.borderWidth) {
                    ctx.stroke();
                  }

                  if (vm.circumference > 0.0015) { // Trying to hide label when it doesn't fit in segment
                    ctx.beginPath();
                    ctx.font = helpers.fontString(opts.defaultFontSize + 5, opts.defaultFontStyle, opts.defaultFontFamily);
                    ctx.fillStyle = "#190707";
                    ctx.textBaseline = "top";
                    ctx.textAlign = "center";

                    // Round percentage in a way that it always adds up to 100%
                    //  ctx.fillText(segmentLabel.toFixed(2) + "%", labelPos.x, labelPos.y);
                  }
                  //display in the center the total sum of all segments
                  // var total = dataset.data.reduce((sum, val) => sum + val, 0);
                  var total = dataset.data.reduce(function(sum, val) {
                    return sum + val;
                  },0);
                  //ctx.fillText('Total = ' + total, vm.x, vm.y - 20, 200);
                  ctx.fillText(CurrentCpuUsage + "%", vm.x, vm.y - 5, 200);
                  // ctx.fillText('0', vm.x - 65, vm.y - 5);
                  // ctx.fillText('100', vm.x + 65, vm.y - 5);
                }
              });

              var model = arc._model;
              model.backgroundColor = custom.backgroundColor ? custom.backgroundColor : valueAtIndexOrDefault(dataset.backgroundColor, index, arcOpts.backgroundColor);
              model.hoverBackgroundColor = custom.hoverBackgroundColor ? custom.hoverBackgroundColor : valueAtIndexOrDefault(dataset.hoverBackgroundColor, index, arcOpts.hoverBackgroundColor);
              model.borderWidth = custom.borderWidth ? custom.borderWidth : valueAtIndexOrDefault(dataset.borderWidth, index, arcOpts.borderWidth);
              model.borderColor = custom.borderColor ? custom.borderColor : valueAtIndexOrDefault(dataset.borderColor, index, arcOpts.borderColor);

              // Set correct angles if not resetting
              if (!reset || !animationOpts.animateRotate) {
                if (index === 0) {
                  model.startAngle = opts.rotation;
                } else {
                  model.startAngle = _this.getMeta().data[index - 1]._model.endAngle;
                }
                model.endAngle = model.startAngle + model.circumference;
              }
              arc.pivot();
            }
          });

          var config = {
            type: 'doughnutLabels',
            data: {
              datasets: [{
                data: [CurrentCpuUsage, RemainingCpuUsage],
                backgroundColor: [
                  backgroundColor,
                  "#999999"
                ]
              }],
              labels: ["Current CPU Usage", "Remaining CPU Usage"]
            },
            options: {
              // circumference: Math.PI,
              // rotation: 1.0 * Math.PI,
              responsive: true,
              animation:{
                duration: 0
            }
            }
          };
          var ctx = document.getElementById("cpuUtilizationChart").getContext("2d");
          window.upDownChart = new Chart(ctx, config);
        }
        else
          $('#divcpuUtilizationChart').show();
      });
    };
    //--END CPU Utilization
    //BEGIN - Memory Utilization
    document.getElementById("titleMemoryUtilizationChart").innerHTML = "Memory Utilization";
    vm.gatewayMemoryUtilizationinfo = function () {
      var CurrentMemoryUsage = 0;
      var RemainingMemoryUsage = 0;
      var backgroundColor = 0;
      gatewayinfoService.gatewayMemoryUtilizationinfo(gatewayInfoChartUID).then(function (result) {

        if (result && result.length > 0) {
          CurrentMemoryUsage = result[0].mem;
          RemainingMemoryUsage = 100 - CurrentMemoryUsage;
        }
        backgroundColor = CurrentMemoryUsage <= 30 ? '#60b044' : CurrentMemoryUsage <= 60 ? '#f6c600' : '#fe1101'; 
        if (result && result.length > 0) {
          Chart.defaults.doughnutLabels = Chart.helpers.clone(Chart.defaults.doughnut);
          var helpers = Chart.helpers;
          var defaults = Chart.defaults;

          Chart.controllers.doughnutLabels = Chart.controllers.doughnut.extend({
            updateElement: function (arc, index, reset) {
              var _this = this;
              var chart = _this.chart,
                chartArea = chart.chartArea,
                opts = chart.options,
                animationOpts = opts.animation,
                arcOpts = opts.elements.arc,
                centerX = (chartArea.left + chartArea.right) / 2,
                centerY = (chartArea.top + chartArea.bottom) / 2,
                startAngle = opts.rotation, // non reset case handled later
                endAngle = opts.rotation, // non reset case handled later
                dataset = _this.getDataset(),
                circumference = reset && animationOpts.animateRotate ? 0 : arc.hidden ? 0 : _this.calculateCircumference(dataset.data[index]) * (opts.circumference / (2.0 * Math.PI)),
                innerRadius = reset && animationOpts.animateScale ? 0 : _this.innerRadius,
                outerRadius = reset && animationOpts.animateScale ? 0 : _this.outerRadius,
                custom = arc.custom || {},
                valueAtIndexOrDefault = helpers.getValueAtIndexOrDefault;

              helpers.extend(arc, {
                // Utility
                _datasetIndex: _this.index,
                _index: index,

                // Desired view properties
                _model: {
                  x: centerX + chart.offsetX,
                  y: centerY + chart.offsetY,
                  startAngle: startAngle,
                  endAngle: endAngle,
                  circumference: circumference,
                  outerRadius: outerRadius,
                  innerRadius: innerRadius,
                  label: valueAtIndexOrDefault(dataset.label, index, chart.data.labels[index])
                },

                draw: function () {
                  var ctx = this._chart.ctx,
                    vm = this._view,
                    sA = vm.startAngle,
                    eA = vm.endAngle,
                    opts = this._chart.config.options;

                  var labelPos = this.tooltipPosition();
                  var segmentLabel = vm.circumference / opts.circumference * 10;

                  ctx.beginPath();

                  ctx.arc(vm.x, vm.y, vm.outerRadius, sA, eA);
                  ctx.arc(vm.x, vm.y, vm.innerRadius, eA, sA, true);

                  ctx.closePath();
                  ctx.strokeStyle = vm.borderColor;
                  ctx.lineWidth = vm.borderWidth;

                  ctx.fillStyle = vm.backgroundColor;

                  ctx.fill();
                  ctx.lineJoin = 'bevel';

                  if (vm.borderWidth) {
                    ctx.stroke();
                  }

                  if (vm.circumference > 0.0015) { // Trying to hide label when it doesn't fit in segment
                    ctx.beginPath();
                    ctx.font = helpers.fontString(opts.defaultFontSize + 5, opts.defaultFontStyle, opts.defaultFontFamily);
                    ctx.fillStyle = "#190707";
                    ctx.textBaseline = "top";
                    ctx.textAlign = "center";
                  }
                  //display in the center the total sum of all segments
                  // var total = dataset.data.reduce((sum, val) => sum + val, 0);
                  var total = dataset.data.reduce(function(sum, val) {
                    return sum + val;
                  },0);
                  ctx.fillText(CurrentMemoryUsage + "%", vm.x, vm.y - 20, 200);
                  ctx.fillText('0', vm.x - 80, vm.y);
                  ctx.fillText('100', vm.x + 80, vm.y);
                }
              });

              var model = arc._model;
              model.backgroundColor = custom.backgroundColor ? custom.backgroundColor : valueAtIndexOrDefault(dataset.backgroundColor, index, arcOpts.backgroundColor);
              model.hoverBackgroundColor = custom.hoverBackgroundColor ? custom.hoverBackgroundColor : valueAtIndexOrDefault(dataset.hoverBackgroundColor, index, arcOpts.hoverBackgroundColor);
              model.borderWidth = custom.borderWidth ? custom.borderWidth : valueAtIndexOrDefault(dataset.borderWidth, index, arcOpts.borderWidth);
              model.borderColor = custom.borderColor ? custom.borderColor : valueAtIndexOrDefault(dataset.borderColor, index, arcOpts.borderColor);

              // Set correct angles if not resetting
              if (!reset || !animationOpts.animateRotate) {
                if (index === 0) {
                  model.startAngle = opts.rotation;
                } else {
                  model.startAngle = _this.getMeta().data[index - 1]._model.endAngle;
                }

                model.endAngle = model.startAngle + model.circumference;
              }

              arc.pivot();
            }
          });

          var config = {
            type: 'doughnutLabels',
            data: {
              datasets: [{
                data: [CurrentMemoryUsage, RemainingMemoryUsage],
                backgroundColor: [
                  backgroundColor,
                  "#999999"
                ]
              }],
              labels: ["Current Memory Usage", "Remaining Memmory Usage"]
            },
            options: {
              circumference: Math.PI,
              rotation: 1.0 * Math.PI,
              responsive: true,
              legend: {
                display: false,
              },
              animation: {
                animateScale: true,
                animateRotate: true
              },
            }
          };
          var ctx = document.getElementById("memoryUtilizationChart").getContext("2d");
          window.upDownChart = new Chart(ctx, config);

        }
        else
          $('#divmemoryUtilizationChart').show();
      });
    };
    //END - Memory Utilization

    //BEGIN - BLE Tags Activity Chart
    //document.getElementById("titleBLETagsActivityChart").innerHTML = "BLE Tags Activity";
    vm.gatewaybleTagsActivityChartinfo = function () {
      var yAxisTime = [];
      var xAxisID = [];
      gatewayinfoService.gatewaybleTagsActivityChartinfo(gatewayInfoChartUID).then(function (result) {
        if (result && result.length > 0) {
          debugger;
          for (var i = 0; i < result.length; i++) {

            var sDate = new Date(result[i].time);
            vm.hours = sDate.getHours(); //returns 0-23
            vm.minutes = sDate.getMinutes(); //returns 0-59
            vm.seconds = sDate.getSeconds(); //returns 0-59
            if (vm.minutes < 10)
              vm.minutesString = 0 + vm.minutes + "";
            else
              vm.minutesString = vm.minutes;

            vm.time = vm.hours + ':' + vm.minutes + ':' + vm.seconds;
            xAxisID.push(result[i].Tx)
            yAxisTime.push(vm.time)
          }
        }
        if (result && result.length > 0) {
          var ctx = document.getElementById("bleTagsActivityChart");
          var myChart = new Chart(ctx, {
            type: 'bar',
            data: {
             
              datasets: [{ 
                label: 'UP Link', 
                backgroundColor:"#20E2E2",              
                data: xAxisID
              },
              {
              label: 'Down Link', 
              backgroundColor:"#18a79d",              
              data: xAxisID
            }
            ],
              labels: yAxisTime
            },
            options: {
              legend: { display: true,
            },
              plugins: {
                  filler: {
                      propagate: true
                  }
              },
              scales: {
                yAxes: [{
                    stacked: true
                }]
            }
          }
          });
        } else {
          $('#divbleTagsActivityChart').show();
        }

        





        
      });
    };
    //END - BLE Tags Activity Chart 

    //BEGIN -  //END - Active Tags Type  
    document.getElementById("titleActiveTagsTypesChart").innerHTML = "Active Tags Types";
    vm.gatewayActiveTagTypesChartinfo = function () {
      var yAxisType = [];
      var xAxisCount = [];
     

     
      gatewayinfoService.gatewayActiveTagTypesChartinfo(gatewayInfoChartUID, cid).then(function (result) {

        if (result && result.length > 0) {
          var checkData = 2;
          for (var i = 0; i < result.length; i++) {
            if(result[i].tagCount == 0 || checkData == 2) {
              checkData = 2;
            } else {
              checkData = 1;
            }         
            xAxisCount.push(result[i].tagCount)
            yAxisType.push(result[i].tagType)           
          }
        }
        if (result && result.length > 0 && checkData == 1) {
          var ctx = document.getElementById("activeTagsTypesChart");
          var myChart = new Chart(ctx, {
            type: 'pie',
            data: {
              datasets: [{               
                data: xAxisCount,
                backgroundColor: [
                  "aqua", "salmon", "darkgray", "pink", "coral" ,"Purple","Yellow","red", "green", "blue", "magenta"           
                ],
                hoverBackgroundColor: [
                  "aqua", "salmon", "darkgray", "pink", "coral" ,"Purple","Yellow","red", "green", "blue", "magenta"                              
                ]
              }],
              labels: yAxisType,             
            },
          });

        } else {
          $('#divactiveTagsTypesChart').show();
        }
      });
    };
    //END - Active Tags Type  
    vm.BLEChart = function () {
      document.getElementById("titleBLETagsActivityChart").innerHTML = "BLE Tags Activity";
      var ctx = document.getElementById('BLEtagschart').getContext('2d');
      gatewayinfoService.gatewayBleChartinfo(gatewayInfoChartUID).then(function (result) {
       //result = [{"Tx":396507837,"Rx":29844706758,"time":"2019-04-10 06:36:53.401"},{"Tx":396507029,"Rx":59844706758,"time":"2019-04-10 06:36:51.391"},{"Tx":396506652,"Rx":89844706758,"time":"2019-04-10 06:36:50.386"},{"Tx":396506275,"Rx":29844706758,"time":"2019-04-10 06:36:49.380"},{"Tx":396505899,"Rx":29844706758,"time":"2019-04-10 06:36:48.375"},{"Tx":396505522,"Rx":29844706758,"time":"2019-04-10 06:36:47.369"},{"Tx":396505145,"Rx":29844706758,"time":"2019-04-10 06:36:46.364"},{"Tx":396504768,"Rx":29844706758,"time":"2019-04-10 06:36:45.354"},{"Tx":396504391,"Rx":29844706758,"time":"2019-04-10 06:36:44.343"},{"Tx":396504014,"Rx":29844706758,"time":"2019-04-10 06:36:43.339"}];
        console.log('result', result);
        var uplink = [];
        var downlink = [];
        var xaxisTime = [];
        var stepFactorTx = 0;
        var maxTx = 0.0;
          if(result.length == 1) {
            maxTx = parseFloat(result[0].Tx);
        } else {
            for (i = 1; i < result.length; i++) { 
                if(parseFloat(result[i].Tx) > maxTx) {
                    maxTx = parseFloat(result[i].Tx);
                }
            }
        }
         stepFactorTx = 1;
        var stepSize = 25;                                        
        if(maxTx/10 <= 10) {
            // Values < 100
            stepFactorTx = 1;
            maxTx = 100;
        } else if(maxTx/100 <= 10) {
            // Values from 100 - 1000
            stepFactorTx = 10;
            maxTx = 1000;
        }else if(maxTx/1000 <= 10) {
            // Values from 1000 - 10000
            stepFactorTx = 100;
            maxTx = 10000;
        }else if(maxTx/10000 <= 10){
            // Values from 10000 - 100000
            stepFactorTx = 1000;
            maxTx = 100000;
        }else if(maxTx/100000 <= 10){
            // Values from 100000 - 10000000
            stepFactorTx = 10000;
            maxTx = 1000000;
        }else if(maxTx/1000000 <= 10){
            // Values from 1000000 - 100000000
            stepFactorTx = 100000;
            maxTx = 10000000;
        }else if(maxTx/10000000 <= 10){
            // Values from 10000000 - 1000000000
            stepFactorTx = 1000000;
            maxTx = 100000000;
        } else if(maxTx/100000000 <= 10){
          // Values from 100000000 - 10000000000
          stepFactorTx = 10000000;
          maxTx = 1000000000;
       } else if(maxTx/1000000000 <= 10){
          // Values from 1000000000 - 10000000000
          stepFactorTx = 100000000;
          maxTx = 10000000000;
      }
        var stepFactorRx = 0;
        var maxRx = 0.0;
          if(result.length == 1) {
            maxRx = parseFloat(result[0].Rx);
        } else {
            for (i = 1; i < result.length; i++) { 
                if(parseFloat(result[i].Rx) > maxRx) {
                    maxRx = parseFloat(result[i].Rx);
                }
            }
        }
        stepFactorRx = 1;
        var stepSize = 25;                                        
        if(maxRx/10 <= 10) {
            // Values < 100
            stepFactorRx = 1;
            maxRx = 100;
        } else if(maxRx/100 <= 10) {
            // Values from 100 - 1000
            stepFactorRx = 10;
            maxRx = 1000;
        }else if(maxRx/1000 <= 10) {
            // Values from 1000 - 10000
            stepFactorRx = 100;
            maxRx = 10000;
        }else if(maxRx/10000 <= 10){
            // Values from 10000 - 100000
            stepFactorRx = 1000;
            maxRx = 100000;
        }else if(maxRx/100000 <= 10){
            // Values from 100000 - 10000000
            stepFactorRx = 10000;
            maxRx = 1000000;
        }else if(maxRx/1000000 <= 10){
            // Values from 1000000 - 100000000
            stepFactorRx = 100000;
            maxRx = 10000000;
        }else if(maxRx/10000000 <= 10){
            // Values from 10000000 - 1000000000
            stepFactorRx = 1000000;
            maxRx = 100000000;
        } else if(maxRx/100000000 <= 10){
           // Values from 100000000 - 10000000000
           stepFactorRx = 10000000;
           maxRx = 1000000000;
        } else if(maxRx/1000000000 <= 10){
           // Values from 1000000000 - 10000000000
           stepFactorRx = 100000000;
           maxRx = 10000000000;
       }
        var stepTx = 0;
        var stepRx = 0;
        var max = 0.0;
        var stepFactor = 0.0;
        var bytesTx = '';
        var bytesRx = '';
        if(maxTx >= maxRx) {
          stepTx = maxTx / maxRx;
          max = maxRx;
          stepFactor = stepFactorRx;
          bytesRx = 'B';
          if (stepTx > 1000) {
            bytesTx = 'Kb';
          } else if(stepTx >= 1000 && stepTx <= 1e6) {
            bytesTx = 'KB';
          } else if(stepTx >= 1e6 && stepTx <= 1.049e6) {
            bytesTx = 'Mb'
          } else if(stepTx >= 1.049e6 && stepTx <= 1e9) {
            bytesTx = 'MB'
          } else if(stepTx >= 1e9 && stepTx <= 1.074e9) {
            bytesTx = 'Gb'
          } else if(stepTx >= 1.074e9 && stepTx <= 1e12) {
            bytesTx = 'GB'
          } else if(stepTx >= 1e12 && stepTx <= 1.1e12) {
            bytesTx = 'Tb'
          } else if(stepTx >= 1.1e12) {
            bytesTx = 'TB'
          }
          bytesRx = 'B';
        } else if (maxRx >= maxTx) {
          stepRx = maxRx / maxTx;
          max = maxTx;
          stepFactor = stepFactorTx;
          bytesTx = 'B';
          if (stepRx > 1000) {
            bytesRx = 'Kb';
          } else if(stepRx >= 1000 && stepTx <= 1e6) {
            bytesRx = 'KB';
          } else if(stepRx >= 1e6 && stepTx <= 1.049e6) {
            bytesRx = 'Mb'
          } else if(stepRx >= 1.049e6 && stepTx <= 1e9) {
            bytesRx = 'MB'
          } else if(stepRx >= 1e9 && stepTx <= 1.074e9) {
            bytesRx = 'Gb'
          } else if(stepRx >= 1.074e9 && stepTx <= 1e12) {
            bytesRx = 'GB'
          } else if(stepRx >= 1e12 && stepTx <= 1.1e12) {
            bytesRx = 'Tb'
          } else if(stepRx >= 1.1e12) {
            bytesRx = 'TB'
          }
          bytesTx = 'B';
        }
           for(var i=0; i< result.length; i++) {
            // result[i].Tx = Math.round(result[i].Tx/1e+6);
            // result[i].Rx = Math.round(result[i].Rx/1e+9);
            if(stepTx != 0) {
              uplink.push(Math.round(result[i].Tx / stepTx));
              downlink.push(result[i].Rx);
            } else if(stepRx != 0) {
              downlink.push(Math.round(result[i].Rx / stepRx));
              uplink.push(result[i].Tx);
            }
            var sDate = new Date(result[i].time);
            vm.hours = sDate.getHours(); //returns 0-23
            vm.minutes = sDate.getMinutes(); //returns 0-59
            vm.seconds = sDate.getSeconds(); //returns 0-59
            if (vm.minutes < 10)
                 vm.minutesString = 0 + vm.minutes + "";
            else
                 vm.minutesString = vm.minutes;
   
            vm.time = vm.hours + ':' + vm.minutes + ':' + vm.seconds;
            xaxisTime.push(vm.time)
           }     
           console.log(uplink, downlink, stepFactor);
        var myChart = new Chart(ctx,{
        type: 'line',  // <-- define overall chart type
        data: {
          labels: xaxisTime,
          datasets: [{
            label: 'Uplink',
            backgroundColor: '#5cd293',
            borderColor: '#5cd293',
            fill: false,
            data: uplink,
            yAxisID: 'left-axis'
          }, {
            label: 'Downlink',
            backgroundColor: '#1a78dd',
            borderColor: '#1a78dd',
            fill: false,
            data: downlink,
            yAxisID: 'right-axis'
          }]
        },
        options: {
          layout: {
            padding: {
                left: 0,
                right: 50,
                top:20,
                bottom: 0
            }
        },
          legend: {
            display: true,
            position: 'bottom',
          },
          
          scales: {
            // xAxes: [{display: true, stacked:true, scaleLabel: {display: false, labelString: 'time'}}],
            yAxes: [{
              type:'linear',
              id:'left-axis',
              display: true,
              position: 'left',
              ticks: {
                callback: function(value, index, values) {
                        return value + bytesTx;
                        },
                         min: 0,
                         stepSize: stepSize * stepFactor ,
                         max: max,
                         fontSize: 10,
                         fontColor: "#000000",
                         fontStyle: 'bold',
              },
              scaleLabel: {display: false, labelString: 'Uplink'}
            },{
              type:'linear',
              id:'right-axis',
              display: true,
              position: 'right',
              scaleLabel: {display: false, labelString: 'Downlink'},
              ticks: {
                callback: function(value, index, values) {
                        return value + bytesRx;
                        },
              min: 0,
              stepSize: stepSize * stepFactor,
              max: max,
              fontSize: 10,
              fontColor: "#000000",
              fontStyle: 'bold',
              },
              gridLines: {drawOnChartArea:false}
            }],
            xAxes: [{
              ticks: {
               fontSize: 10,
               fontColor: "#000000",
               fontStyle: 'bold',
              }
             }]
          }
        }
      });
     })
    };
    function getTree(){
      var spid = vm.prevPageDetail.spid;
      floordataservice.getNetworkdevice(spid).then(function (response) {
                if (response) {
                    var tree = response;
                    vm.deviceList = [];
                    vm.deviceList = response;
                     vm.networkDeviceList = [];
                      for(var i=0; i< response.length; i++) {
                         var source = response[i].source;
                         var imagePath = '';
                         if(source == "guest") {
                            imagePath = "../images/networkicons/guestSensor_inactive.png";
                         } else if(source == "qubercomm") {
                            imagePath = "../images/networkicons/sensor_inactive.png";
                         }

                          response[i].imagePath = imagePath;
                         vm.networkDeviceList.push(response[i]);
                      }
                      }
                    });
    };
    vm.checkDeviceId = function(item) {
        vm.networkDeviceList = vm.deviceList;
        vm.isClicked = !vm.isClicked;
        var searchDevice = "";
        if(item.id == 1) {
         searchDevice = "qubercomm";    
        } else if(item.id == 2){
            searchDevice = "guest";
        }
        var selectedDevice = [];
        angular.forEach(vm.networkDeviceList,function(category) {
            if(searchDevice == category.source){
                selectedDevice.push(category);
            }
        });
        item.isChecked = false;
        if(selectedDevice.length > 0 ) {
            vm.networkDeviceList = selectedDevice;
        } else {
            vm.networkDeviceList = vm.deviceList;
        }
    }

    vm.hoverIn = function(uid) {
            document.getElementById(uid+'li').style.background="#e2e2e2";
            document.getElementById(uid).style.display="block";        
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

         vm.showDevice = function(device) {
        $('svg').children().each(function() {
            var $this = $(this);
           $this.children().each(function() {
               var $that = $(this);
               if(device.uid == $that.attr('dev-uid')) {
                var width = $that.attr('width')?$that.attr('width'):40;
                $($that).animate({width:width/5+'px',opacity:0},'1000000');
                $($that).animate({width:width/4+'px',opacity:1},'slow');
                $($that).animate({width:width/3+'px',opacity:1},'slow');
                $($that).animate({width:width/2+'px',opacity:1},'slow');
                $($that).animate({width:width/1+'px',opacity:1},'slow');
               }
           })
            // console.log(this)
        })
    }

     vm.goToPage = function(device, page) { 
        var uuid    = device.uid;
        var spid   = device.spid;
        var sid    = device.sid;
        switch(page) {
          case 'gatewayinfo':
                if(uuid) { 
                  var navDetail = {};
                  navDetail.venue = vm.prevPageDetail.venue;
                  navDetail.floor = vm.prevPageDetail.floor;
                  navDetail.room  = device.alias;
                  navDetail.spid  = device.spid;
                  localStorage.setItem("prevPageInfo", JSON.stringify(navDetail));
                  navigation.goToGatewayInfo(uuid,sid,spid);
                }
              break;  
          case 'upgrade':
             if(uuid) {
              var sid = localStorage.getItem('sid');
              var cid = localStorage.getItem('cid');
              var macadr = uuid;
              navigation.goToUpgrade(sid, '0', macadr);
             }
             break;
          case 'gatewayedit':
             navigation.goToAdddevice(0, uuid, 1, 'gatewayedit');   
             break;             
        }

       }
    
    function activate() {
      vm.gatewayinfo()
      vm.gatewayActiveTagsInfo();
      vm.gatewayCPUUtilizationinfo()
      vm.gatewayMemoryUtilizationinfo();
      //vm.gatewaybleTagsActivityChartinfo();
      vm.gatewayActiveTagTypesChartinfo();
      vm.BLEChart();
    }
    getTree();
    activate();
        var clearInterval = $interval(startRefresh, 10000);
        $scope.$on("$destroy",function(){
          if (angular.isDefined(clearInterval)) {
              $interval.cancel(clearInterval);
          }
      });

    function startRefresh() {
        // $.get('gatewayinfo.html', function(data) {
         activate();   
        // });
    }

    return vm;
  }
})();