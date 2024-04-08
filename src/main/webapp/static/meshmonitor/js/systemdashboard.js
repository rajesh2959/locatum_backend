var cur_sys_status = "play";
(function () {
    search = window.location.search.substr(1)
    urlObj=JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
    var timer = 10000;
    var count = 1;
    var timeSeries = "";
    var defaultTimer = 1000;
    

    var timeChartDataLoaded = false;
    var memChartDatachart = false;
    var txrxChartDatachart = false;
    var meterChartDatachart = false;
     
    var timeChartData =  { 
                "data_cpu": [],
                "date": [],
            };

    var memChartData =  { 
                "data_mem": [],
                "date": [],
            };

    var txrxChartData =  { 
                "data_tx": [],
                "data_rx": [],
            };
    var meterChartData ={
                "data_meter":[],
            }
     var channelChartData = {
        "data_channel":[],
     }
    var ringChartData ={
                "data_avgcpu":[],
                "data_avgmem":[],
                "data_avgtx":[],
                "data_avgrx":[],
            }
    
     var n = 0;
    DeviceACL = {
        //timeoutCount: 10000,
        acltables: {
           
            setTable: {
                aclClientsTable: function () {
                    $.ajax({
                        url:'/facesix/rest/ui/systemStats?uid='+urlObj.uid,
                        method: "get",
                        cache: false,
                        success: function (result) {
                        	
                        	$('.sysview').show();                        
                            var sysResult = result.currentStats;
                            var sysAVg= result.systemStatsAvg;      
                            //console.log("n value >>" + n)
                           console.log("sys result" + JSON.stringify(result));
                           

                           
                            var channel = sysResult.currChannel;
                            var cpudata = result.cpu;
                            var memdata = result.mem;
                            var txrxdata = result.network;
                            var chusage = result.chennelusage;
                            var meter = sysAVg.avgMem;
                            var avgcpu =sysAVg.avgCpu;
                            var mincpu =sysAVg.minCpu;
                            var maxcpu =sysAVg.maxCpu;
                            var minmem =sysAVg.minMem;
                            var maxmem =sysAVg.maxMem;
                            var avgmem =sysAVg.avgMem;
                            var avgtx = sysAVg.avgMbpsUplink;
                            var avgrx = sysAVg.avgMbpsDownlink;
                            var batterystatus = sysResult.currBatteryStatus;
                            var batterypercentage = sysResult.currBatteryPercent;
                            var batremtime = sysResult.currbattRemainingTime;
                            var mode = sysResult.currPowerMode;
                            var maxtxdata = sysAVg.maxUplink;
                            var mintxdata = sysAVg.minUplink;
                            var avgtxdata = sysAVg.avgUplink;

                            var maxrxdata= sysAVg.maxDownlink;                            
                            var minrxdata = sysAVg.minDownlink;
                            var avgrxdata = sysAVg.avgDownlink;
                            
                            maxtxdata=((maxtxdata)/(1024*1024)).toFixed(3);
                            mintxdata=((mintxdata)/(1024*1024)).toFixed(3);
                            avgtxdata=((avgtxdata)/(1024*1024)).toFixed(3);
                            
                            maxrxdata=((maxrxdata)/(1024*1024)).toFixed(3);
                            minrxdata=((minrxdata)/(1024*1024)).toFixed(3);
                            avgrxdata=((avgrxdata)/(1024*1024)).toFixed(3);
                            

                            $("#maxtx").html(maxtxdata);
                            $("#mintx").html(mintxdata);
                            $("#avgtx").html(avgtxdata);

                            $("#maxrx").html(maxrxdata);
                            $("#minrx").html(minrxdata);
                            $("#avgrx").html(avgrxdata);

                            $('#lowbattery').hide();
                          
                            if(mode == "battery"){
                                $("#pwrname").html("Battery");
                                if(batterystatus == 1){
                                $('.bolt').show();
                                } 

                                if(batterypercentage <= 10){
                                    $('#lowbattery').show();
                                     $('.bolt').css("color","red");
                                } else if(batterypercentage <= 25){
                                    $('#quaterbattery').show();
                                    $('.bolt').css("color","orange");
                                } else if(batterypercentage <= 50){
                                        $('#halfbattery').show();
                                } else if(batterypercentage <= 80){
                                        $('#almostbattery').show();
                                } else {
                                    $('#fullbattery').show();
                                }
                                $('.battery').addClass('batterynew');
                            } else {
                                $("#pwrname").html("AC Power");
                                $('#acpower').show();
                                $('#usage').hide();
                                $('.battery-bolt').hide();
                                $('.pause').addClass('pausenew');
                                $('.play').addClass('playnew');
                            }
                                                                            
                            $('#usage').html(batterypercentage+ "%");                       
                            $('#channel').html(channel);
                            $('#batremtime').html(batremtime);

                            
                                                      
                            n++;    

                            var da = new Date();
                            //console.log(d)

                            //timeChartData.data_cpu.push(cpudata);

                            var cpuval=[];
                            var cputime=[];

                            for (var i = 0; i < cpudata.length; i++){                                                                                                           
                                    cpuval.push(cpudata[i].cpu);
                                    cputime.push(cpudata[i].time);
                                }

                                var req_len = 120 - cpudata.length;
                                 for (var i = 0; i < req_len; i++){                                                                                                           
                                    cpuval.push(0);                                    
                                }
                                   
                            timeChartDatachart = c3.generate({
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#cpugraph', 
                                    data: {
                                     columns: [
                                            ['x'].concat(cputime),
                                            ['Usage'].concat(cpuval),
/*                                            ['cpugraph',80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,50,49,57,75,10,23,49,100,50,20] 
*/
                                        ],
                                        type : 'area-spline',
                                       colors: {
                                            Usage: 'rgb(293, 224, 39)',
                                        },
                                    },               
                                    point: {
                                             show: false
                                        },
                                    grid: {
								        x: {
								             lines: [{value: 13},{value: 26},{value: 39},{value: 52},{value: 65},{value: 78},{value: 91},{value: 104},{value: 117}]
								        },
								        y: {
								           lines: [{value: 20},{value:40},{value:60},{value:80},{value:100}]
								        },
                                         lines:{
                                           front : false
                                        },
								    },
                                     padding: {
                                        top: 10,
                                        right: 15,
                                        bottom: 0,
                                        left: 55,
                                     },
                                    axis: {
                                        x:{
                                            min:0.5,                                        
                                             label:{
                                                text:'Time(seconds)',
                                                position: 'outer-center'
                                             },                                             
                                        },
                                        y:{
                                            max:100,
                                            tick : {values: [0,20,40,60,80,100]},
                                            label:{
                                                text:'Percentage',
                                                position: 'outer-middle',
                                            },                                            
                                        },
                                    },
                                  /*  tooltip:{
                                  
                                    contents: function (data, defaultTitleFormat, defaultValueFormat) { 
                                 
                                        var $$ = this, config = $$.config,
                                        titleFormat = config.tooltip_format_title || defaultTitleFormat,
                                        nameFormat = config.tooltip_format_name || function (name) { return name; },
                                        valueFormat = config.tooltip_format_value || defaultValueFormat,
                                        text, i, title, value;
                                        text = '<table class="c3-tooltip"><tbody>';
                                        for (var i = 0; i < cpudata.length; i++){                                                                                                           
                                                    var time = cpudata[i].time;

                                                            text += '<tr><th colspan="2">Status: '+time+'</th></tr>'; 
                                                    

                                            
                                        }
                                             
                                                                                                                                                                                                                       
                                        text += '</tbody></table>';
                                        return text;
                                    },
                                },*/
                                  tooltip: {
									        format: {
									            title: function (d) { return "" },
									            value: function (value, ratio, id) {
									                return value + "%";
									            },
									
									        },
									        
									    }, 
                                    legend:{
                                        show : false
                                    }
                                });


                                var memval=[];
                                for (var i = 0; i < memdata.length; i++){                                                                                                           
                                    memval.push(memdata[i].mem);
                                }

                                var mem_len = 120 - memdata.length;
                                 for (var i = 0; i < mem_len; i++){                                                                                                           
                                    memval.push(0);                                    
                                }
                                               
                              memChartDatachart = c3.generate({
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#memorygraph', 
                                    data: {
//                                          xFormat: '%Y%m%d', // 'xFormat' can be used as custom format of 'x'
                                     columns: [
                                            ['Usage'].concat(memval),
                                             // ['memorygraph',10,56,58,65,14,36,25,84,69,48,17,48,49,57,75,10,23,49,54,100,0,10,56,58,65,14,36,25,84,69,48,17,48,49,57,75,10,23,49,54,100,0,10,56,58,65,14,36,25,84,69,48,17,48,49,57,75,10,23,49,54,100,0,17,48,49,57,75,10,23,49,54,100,0,10,56,58,65,14,36,25,84,69,48,17,48,49,57,75,10,23,49,54,100,0,100,0,10,56,58,65,14,36,25,84,69,48,17,48,49,57,75,10,23,49] 
                                        ],
                                        type : 'area-spline',
                                        colors:{
                                            Usage:'#33cc33',
                                        },
                                    },
                                    point: {
                                             show: false
                                        },
                                     padding: {
                                        top: 10,
                                        right: 15,
                                        bottom: 0,
                                        left: 55,
                                     },
                                    axis: {
                                        x:{
                                            min:0.5,
                                           
                                              label:{
                                                text:'Time(seconds)',
                                                position: 'outer-center'
                                             }, 
                                        },
                                        y:{
                                            max:100,
                                            tick : {values: [0,20,40,60,80,100]},
                                            label:{
                                                text:'Percentage',
                                                position: 'outer-middle',
                                            }, 
                                        },
                                    },grid: {
                                        x: {
                                             lines: [{value: 13},{value: 26},{value: 39},{value: 52},{value: 65},{value: 78},{value: 91},{value: 104},{value: 117}]
                                        },
                                        y: {
                                           lines: [{value: 20},{value:40},{value:60},{value:80},{value:100}]
                                        },
                                        lines:{
                                           front : false
                                        }
                                    },
                                    tooltip: {
									        format: {
									            title: function (d) { return "" },
									            value: function (value, ratio, id) {
									                return value + "%";
									            },
									
									        },
									        
									    },                         
                                    legend:{
                                        show : false
                                    }
                                    
                                });

                          
                              var txval=[];
                              var rxval=[];
                                for (var i = 0; i < txrxdata.length; i++){ 
                                	
                                	var txBytes = txrxdata[i].uplink;
                                	var rxBytes = txrxdata[i].downlink;
                                	
                                	var txBytesMbps = ((txBytes/1024)/1024)
                                	var rxBytesMbps = ((rxBytes/1024)/1024)
                                	
                                    txval.push(txBytesMbps.toFixed(2));
                                    rxval.push(rxBytesMbps.toFixed(2));
                                }

                                var txrx_len = 120 - txrxdata.length;
                                 for (var i = 0; i < txrx_len; i++){                                                                                                           
                                    txval.push(0);
                                    rxval.push(0);                                    
                                }
                           
                              txrxChartDatachart = c3.generate({
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#txrx', 
                                    data: {
//                                          xFormat: '%Y%m%d', // 'xFormat' can be used as custom format of 'x'
                                     columns: [
                                            ['Tx'].concat(txval),
                                            ['Rx'].concat(rxval)
                                            //['tx',80,100,80,75,14,36,25,99,10,5,0,50,49,57,75,0,90,49,54,100,0,80,100,80,75,14,36,25,99,10,5,0,50,49,57,75,0,90,49,54,100,0,80,100,80,75,14,36,25,99,10,5,0,50,49,57,75,0,90,49,54,100,0] ,
                                              //['rx',10,56,58,65,14,36,25,84,69,48,17,48,49,57,75,10,23,49,54,0,100,10,56,58,65,14,36,25,84,69,48,17,48,49,57,75,10,23,49,54,0,100,10,56,58,65,14,36,25,84,69,48,17,48,49,57,75,10,23,49,54,0,100] 
                                        ],
                                        //columns: [
                                            /*['memorygraph'].concat(memChartData.data_mem),*/
//['tx',7879693766,1056465,565656,65656,6566550,66656549,65657,7656565665,6565656,9066666,4554454549,5565465464,106546546540,654646546] ,
//['rx',1054350,15450,553453456,553453458,65345345345,553453414,353453456,253453453455,85435345344,653453453459,48534534534] 
                                       // ],
                                        type : 'spline',
                                        colors:{
                                            Tx:'#ff4d4d',
                                            Rx:'#66d9ff',
                                        }
                                    },
                                    point: {
                                             show: false
                                        },
                                     padding: {
                                        top: 10,
                                        right: 15,
                                        bottom: 0,
                                        left: 55,
                                     },
                                    axis: {
                                        x:{
                                           min:0.5,
                                            
                                              label:{
                                                text:'Time(seconds)',
                                                position: 'outer-center'
                                             }, 
                                        },
                                        y:{
                                           // max:100,
                                            //tick : {values: [0,20,40,60,80,100]},
                                             label:{
                                                text:'Mbps',
                                                position: 'outer-middle',
                                            }, 
                                        },
                                    },grid: {
                                        x: {
                                             show:true
                                        },
                                        y: {
                                            show: true
                                        }
                                    },
                                      tooltip: {
									        format: {
									            title: function (d) { return ""},
									            value: function (value, ratio, id) {
									                return value + " Mbps ";
									            },
									
									        },
									        
									    },                         
                                    legend:{
                                        show : false
                                    }
                                });

                             /* maxtx();
                              mintx();
                              avgt();
                              maxrx();
                              minrx();
                              avgr();*/

                           var channelval=[];
                                for (var i = 0; i < chusage.length; i++){                                                                                                           
                                    channelval.push(chusage[i].channelUsage);
                                }

                                var ch_len = 120 - chusage.length;
                                 for (var i = 0; i < ch_len; i++){                                                                                                           
                                    channelval.push(0);
                                }

                            channelChartDatachart = c3.generate({
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#channelgraph', 
                                    data: {
                                     columns: [
                                            ['Usage'].concat(channelval),
                                            //['Usage',80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,50,49,57,75,10,23,49,100,50,20] 

                                        ],
                                        type : 'area-spline',
                                       colors: {
                                            Usage: 'rgb(173,216,230)',
                                        },
                                    },
                                    point: {
                                             show: false
                                        },
                                    grid: {
                                        x: {
                                             lines: [{value: 13},{value: 26},{value: 39},{value: 52},{value: 65},{value: 78},{value: 91},{value: 104},{value: 117}]
                                        },
                                        y: {
                                           lines: [{value: 20},{value:40},{value:60},{value:80},{value:100}]
                                        },
                                         lines:{
                                           front : false
                                        },
                                    },
                                     padding: {
                                        top: 10,
                                        right: 15,
                                        bottom: 0,
                                        left: 55,
                                     },
                                    axis: {
                                        x:{
                                            min:0.5,
                                              label:{
                                                text:'Time(seconds)',
                                                position: 'outer-center'
                                             },                                            
                                        },
                                        y:{
                                            max:100,
                                            tick : {values: [0,20,40,60,80,100]},
                                             label:{
                                                text:'Percentage',
                                                position: 'outer-middle',
                                            }, 
                                        },
                                    },      
                                    tooltip: {
								        format: {
								            title: function (d) { return ""},
								            value: function (value, ratio, id) {
								                return value + "%";
								            },
								
								        },
								        
								    },                         
                                    legend:{
                                        show : false
                                    }
                                });
                                                                                            
                              maxcpuChartDatachart = c3.generate({
                                  size: {
                                      height: 60,
                                  },
                                   padding: {
                                      top: 0,
                                      right: 15,
                                      bottom: 0,
                                      left: 40,
                                   },
                                  bindto: '#ring', 
                                 data: {
								        columns: [
								            ['CPU',maxcpu],
								        ],
								        type : 'gauge',
                                      colors:{
                                          CPU:"#e9967a",
                                      },
								    },
                                  gauge:{
                                    label: {
                                        format: function(value, ratio) {
                                            return value;
                                            },
                                    show: true 
                                     },
                                      width:6,                                    
                                  },
								    donut: {
								       /* title: "CPU/MEM/NET"*/
								    },
                                  legend:{
                                      show : false
                                  }
                              });
                               

                                 mincpuChartDatachart = c3.generate({
                                    size: {
                                        height: 60,
                                    },
                                     padding: {
                                        top: 0,
                                        right: 15,
                                        bottom: 0,
                                        left: 40,
                                     },
                                    bindto: '#mincpu', 
                                   data: {
                                        columns: [
                                            ['CPU',mincpu],
                                            //['cpu',30],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            CPU :"#8fbc8f",
                                        }
                                    },
                                    gauge:{
                                        label: {
                                        format: function(value, ratio) {
                                            return value;
                                            },
                                    show: true 
                                     },
                                        width:6,
                                    },
                                    legend:{
                                        show : false
                                    }
                                });

                                
                                 avgcpuChartDatachart = c3.generate({
                                    size: {
                                        height: 60,
                                    },
                                     padding: {
                                        top: 0,
                                        right: 15,
                                        bottom: 0,
                                        left: 40,
                                     },
                                    bindto: '#avgcpu', 
                                   data: {
                                        columns: [
                                           ['CPU',avgcpu],
                                             //['cpu',48],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            CPU : "#87cefa",
                                        },
                                       
                                    },
                                    gauge:{
                                        label: {
                                        format: function(value, ratio) {
                                            return value;
                                            },
                                    show: true 
                                     },
                                        width:6,
                                    },
                                    legend:{
                                        show : false
                                    }
                                });


                                 minmemChartDatachart = c3.generate({
                                    size: {
                                        height: 60,
                                    },
                                     padding: {
                                        top: 0,
                                        right: 15,
                                        bottom: 0,
                                        left: 40,
                                     },
                                    bindto: '#minmem', 
                                   data: {
                                        columns: [
                                            ['mem',minmem],
                                            //['mem',30]
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            mem :"#8fbc8f",
                                        }
                                    },
                                    gauge:{
                                        label: {
                                        format: function(value, ratio) {
                                            return value;
                                            },
                                    show: true 
                                     },
                                        width:6,
                                    },
                                    legend:{
                                        show : false
                                    }
                                });


                                 maxmemChartDatachart = c3.generate({
                                    size: {
                                        height: 60,
                                    },
                                     padding: {
                                        top: 0,
                                        right: 15,
                                        bottom: 0,
                                        left: 40,
                                     },
                                    bindto: '#maxmem', 
                                   data: {
                                        columns: [
                                            ['MEM',maxmem],
                                             //['mem',48],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            MEM : "#e9967a",
                                        },
                                    },
                                    gauge:{
                                        label: {
                                        format: function(value, ratio) {
                                            return value;
                                            },
                                    show: true 
                                     },
                                        width:6,
                                    },
                                    legend:{
                                        show : false
                                    }
                                });

                                 avgmemChartDatachart = c3.generate({
                                    size: {
                                        height: 60,
                                    },
                                     padding: {
                                        top: 0,
                                        right: 15,
                                        bottom: 0,
                                        left: 40,
                                     },
                                    bindto: '#avgmem', 
                                   data: {
                                        columns: [
                                            ['CPU',avgmem],
                                            //['mem',30],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            CPU :"#87cefa",
                                        }
                                    },
                                    gauge:{
                                        label: {
                                        format: function(value, ratio) {
                                            return value;
                                            },
                                    show: true 
                                     },
                                        width:6,
                                    },
                                    legend:{
                                        show : false
                                    }
                                });

                                 /*  function maxtx() {
                                      var elem = document.getElementById("maxtx");   
									  var width = 70;
									      elem.style.width = width + '%'; 
									      elem.innerHTML = width * 1 + '%';
									    }
									  
                                   function mintx() {
                                       var elem = document.getElementById("mintx");   
                                       var width = 15;
                                          elem.style.width = width + '%'; 
                                          elem.innerHTML = width * 1 + '%'; 
 									    }
 									                                    
                                   function avgt() {
                                       var elem = document.getElementById("avgtx");   
                                      var width = 40;
                                          elem.style.width = width + '%'; 
                                          elem.innerHTML = width * 1 + '%';
 									    }
 									
                                     function maxrx() {
                                      var elem = document.getElementById("maxrx");   
                                      var width = 80;                                     
                                          elem.style.width = width + '%'; 
                                          elem.innerHTML = width * 1 + '%';
                                        }
                                                                       
                                   function minrx() {
                                          var elem = document.getElementById("minrx");   
                                          var width = 30;                                   
                                          elem.style.width = width + '%'; 
                                          elem.innerHTML = width * 1 + '%'; 
                                        }
                                                                        
                                   function avgr() {
                                       var elem = document.getElementById("avgrx");   
                                       var width = 50;                                   
                                          elem.style.width = width + '%'; 
                                          elem.innerHTML = width * 1 + '%';
                                        }*/
                                var cur_sys_status = localStorage.getItem("someVarKeyVal");
                              if(cur_sys_status == "play" || cur_sys_status == null){
                                $('.pause').show();
                                $('.play').hide();
                            } else {
                                $('.pause').hide();
                                $('.play').show();
                            }
                               
                               setTimeout(function () { 
                                if(cur_sys_status == "play"){                               
                                DeviceACL.acltables.setTable.aclClientsTable();
                                }                                                                     
                                  }, defaultTimer)  
                            
                            
                       },
                        error: function (data) {                        	
                        	setTimeout(function () {
                                 if(cur_sys_status == "play"){                               
                                DeviceACL.acltables.setTable.aclClientsTable();
                                }  
                             }, defaultTimer)
                        },

                   
                        dataType: "json"

                    });
                }
            }
        },         

        init: function (params) {
            var aclList = ['aclClientsTable']
            var that = this;            
            $.each(aclList, function (key, val) {
                that.acltables.setTable[val]();
            });
        },
    }
      
    
})();

$(document).ready(function(){
    DeviceACL.init();
      
});

$("#root").on("change",function(){
  var cur_dev = $("#root").val();
  location.href = "/facesix/web/mesh/systemdashboard?uid="+cur_dev+"&cid="+urlObj.cid+"&sid="+urlObj.sid+"&spid="+urlObj.spid;
})

$(".pause").on("click",function(){
    $('.pause').hide();
    $('.play').show();
    cur_sys_status = "pause";
    localStorage.setItem("someVarKeyVal", cur_sys_status);
})
 
$(".play").on("click",function(){
    $('.play').hide();
    $('.pause').show();
    DeviceACL.acltables.setTable.aclClientsTable();
    cur_sys_status = "play";
     localStorage.setItem("someVarKeyVal", cur_sys_status);
}) 
