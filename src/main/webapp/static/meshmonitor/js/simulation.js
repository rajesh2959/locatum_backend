(function () {
    search = window.location.search.substr(1)
     urlObj=JSON.parse('{"' + decodeURI(search).replace(/"/g,
	 '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
    var timer = 10000;
    var count = 1;
    var timeSeries = "";

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
    simulationACL = {
        timeoutCount: 10000,
        acltables: {
           
            setTable: {
                aclClientsTable: function () {
                    $.ajax({
                        url:'/facesix/rest/ui/systemStats?uid='+urlObj.uid,
                        method: "get",
                        success: function (result) {
                            $(".simulationview").show();
                            var sysResult = result.currentStats;
                            var sysAVg= result.systemStatsAvg;      

                          // console.log("sys result" + JSON.stringify(result));
                            
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
                            var mode = sysResult.currPowerMode;
                           
                            $('#lowbattery').hide();
                            if(mode == "battery"){
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

                            } else {
                                $('#acpower').show();
                                $('#usage').hide();
                            }
                                                                            
                            $('#usage').html(batterypercentage+ "%");                       
                            $('#channel').html(channel);
                            
                                                      
                            n++;    

                          /*  var d = new Date();
                            console.log(d)
*/
                            // timeChartData.data_cpu.push(cpudata);

                            var cpuval=[];                           
                            for (var i = 0; i < cpudata.length; i++){                                                                                                           
                                    cpuval.push(cpudata[i].cpu);
                                }
                          

                            timeChartDatachart = c3.generate({
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#cpugraph', 
                                    data: {
                                     columns: [
                                            ['Usage'].concat(cpuval),
/*
 * ['cpugraph',80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,50,49,57,75,10,23,49,100,50,20]
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
								             lines: [{value: 10},{value: 20},{value: 30},{value: 40},{value: 50},{value: 60}]
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
								                return value + " % ";
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
                                               
                              memChartDatachart = c3.generate({
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#memorygraph', 
                                    data: {
// xFormat: '%Y%m%d', // 'xFormat' can be used as custom format of 'x'
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
                                        left:55,
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
                                             lines: [{value: 10},{value: 20},{value: 30},{value: 40},{value: 50},{value: 60}]
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
								            title: function (d) { return ""},
								            value: function (value, ratio, id) {
								                return value + " % ";
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
                                    txval.push((txrxdata[i].uplink/1024.0)/1024.0).toFixed(2);
                                    rxval.push((txrxdata[i].downlink/1024.0)/1024.0).toFixed(2);
                                }
                           
                              txrxChartDatachart = c3.generate({
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#txrx', 
                                    data: {
// xFormat: '%Y%m%d', // 'xFormat' can be used as custom format of 'x'
                                     columns: [
                                            ['Tx'].concat(txval),
                                            ['Rx'].concat(rxval)
                                            // ['tx',80,100,80,75,14,36,25,99,10,5,0,50,49,57,75,0,90,49,54,100,0,80,100,80,75,14,36,25,99,10,5,0,50,49,57,75,0,90,49,54,100,0,80,100,80,75,14,36,25,99,10,5,0,50,49,57,75,0,90,49,54,100,0]
											// ,
                                              // ['rx',10,56,58,65,14,36,25,84,69,48,17,48,49,57,75,10,23,49,54,0,100,10,56,58,65,14,36,25,84,69,48,17,48,49,57,75,10,23,49,54,0,100,10,56,58,65,14,36,25,84,69,48,17,48,49,57,75,10,23,49,54,0,100]
                                        ],
                                        // columns: [
                                            /* ['memorygraph'].concat(memChartData.data_mem), */
// ['tx',7879693766,1056465,565656,65656,6566550,66656549,65657,7656565665,6565656,9066666,4554454549,5565465464,106546546540,654646546]
// ,
// ['rx',1054350,15450,553453456,553453458,65345345345,553453414,353453456,253453453455,85435345344,653453453459,48534534534]
                                       // ],
                                        type : 'area-spline',
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
                                            // tick : {values:
											// [0,20,40,60,80,100]},
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
								                return value + " bytes ";
								            },
								
								        },
								        
								    },  
                                    legend:{
                                        show : false
                                    }
                                });

                              maxtx();
                              mintx();
                              avgt();
                              maxrx();
                              minrx();
                              avgr();

                           var channelval=[];
                                for (var i = 0; i < chusage.length; i++){                                                                                                           
                                    channelval.push(chusage[i].channelUsage);
                                }

                            channelChartDatachart = c3.generate({
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#channelgraph', 
                                    data: {
                                     columns: [
                                            ['Usage'].concat(channelval),
/*
 * ['cpugraph',80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,49,57,75,10,23,49,100,50,20,80,56,58,65,14,36,25,99,10,5,0,50,49,57,75,10,23,49,100,50,20,50,49,57,75,10,23,49,100,50,20]
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
                                             lines: [{value: 10},{value: 20},{value: 30},{value: 40},{value: 50},{value: 60}]
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
                                            min:0.9,
                                            max:60,
                                             tick : {values: [0,10,20,30,40,50,60]},
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
								                return value + " % ";
								            },
								
								        },
								        
								    },  
                                    legend:{
                                        show : false
                                    }
                                });
                                                                                            
                              maxcpuChartDatachart = c3.generate({
                                  size: {
                                      height: 70,
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
                                          CPU:"red",
                                      },
								    },
                                  gauge:{
                                    label: {
                                        format: function(value, ratio) {
                                            return value + "%";
                                            },
                                    show: true 
                                     },
                                      width:8,
                                      fontsize:20,
                                  },
								    donut: {
								       /* title: "CPU/MEM/NET" */
								    },
                                  legend:{
                                      show : false
                                  }
                              });
                               

                                 mincpuChartDatachart = c3.generate({
                                    size: {
                                        height: 70,
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
                                            // ['cpu',30],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            CPU :"green",
                                        }
                                    },
                                    gauge:{
                                        label: {
                                        format: function(value, ratio) {
                                            return value + "%";
                                            },
                                    show: true 
                                     },
                                        width:8,
                                    },
                                    legend:{
                                        show : false
                                    }
                                });

                                
                                 avgcpuChartDatachart = c3.generate({
                                    size: {
                                        height: 70,
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
                                             // ['cpu',48],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            CPU : "blue",
                                        },
                                       
                                    },
                                    gauge:{
                                        label: {
                                        format: function(value, ratio) {
                                            return value + "%";
                                            },
                                    show: true 
                                     },
                                        width:8,
                                    },
                                    legend:{
                                        show : false
                                    }
                                });


                                 minmemChartDatachart = c3.generate({
                                    size: {
                                        height: 70,
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
                                            // ['mem',30]
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            mem :"green",
                                        }
                                    },
                                    gauge:{
                                        label: {
                                        format: function(value, ratio) {
                                            return value + "%";
                                            },
                                    show: true 
                                     },
                                        width:8,
                                    },
                                    legend:{
                                        show : false
                                    }
                                });


                                 maxmemChartDatachart = c3.generate({
                                    size: {
                                        height: 70,
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
                                             // ['mem',48],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            MEM : "red",
                                        },
                                    },
                                    gauge:{
                                        label: {
                                        format: function(value, ratio) {
                                            return value + "%";
                                            },
                                    show: true 
                                     },
                                        width:8,
                                    },
                                    legend:{
                                        show : false
                                    }
                                });

                                 avgmemChartDatachart = c3.generate({
                                    size: {
                                        height: 70,
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
                                            // ['mem',30],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            CPU :"blue",
                                        }
                                    },
                                    gauge:{
                                        label: {
                                        format: function(value, ratio) {
                                            return value + "%";
                                            },
                                    show: true 
                                     },
                                        width:8,
                                    },
                                    legend:{
                                        show : false
                                    }
                                });

                                   function maxtx() {
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
                                        }
                                                                                           
                              
								/*  setTimeout(function () {
								  DeviceACL.acltables.setTable.aclClientsTable(); },
								  1000)*/
								 
                            
                       },
                        error: function (data) {},
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
    simulationACL.init();     

     var url = '/facesix/rest/ui/simulationPersistence?uid='+urlObj.uid

            $.ajax({
             url:url,
                method:'GET',
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify(),
                success:function(response){
                    console.log("success " +JSON.stringify(response))
                        var result = response;
                        var profile_type = result.profile_type;
                        var buttonval = response.simulation;

                        if(profile_type != "basic"){
                                if (profile_type == "gaming"){
                                        $('.firstradio').prop("checked",true);
                                } else if(profile_type == "moderate"){
                                        $('.secondradio').prop("checked",true);
                                } else {
                                        $('.thirdradio').prop("checked",true);
                                }

                                    if(buttonval == "true"){
                                   $('.slider').prop("checked",true);
                                        } else {
                                    $('.slider').prop("checked",false);
                                        }
                                $('.network_balancer').prop("disabled",true);
                                $('.commonlabel').prop("disabled",true); 

                        } else {                             
                             $('#cpupercantage').val(result.free_cpu);
                             $('#rampercentage').val(result.free_ram);
                             $('#batterytime').val(result.batt_remaining_time);
                             //$('#link').val(result.primary_link_rssi);
                             //$('#hopcount').val(result.hops_count);
                             if(buttonval == "true"){
                                   $('.network_balancer').prop("checked",true);
                                   $('.network_balancer').prop("disabled",false);
                                   $('.commonlabel').prop("disabled",true); 
                                        } else {
                                    $('.network_balancer').prop("checked",false);
                                }
                            }
                      


                },
                error:function(error){
                    $('.network_balancer').prop("disabled",true);
                    console.log("error " +error);
                }
     });

});


$("#powermode").on("change",function(){
        var pwMode = $("#powermode").val();
        if(pwMode == "battery"){
                $("#powerpercentage").css("pointer-events","auto");
                $("#powerpercentage").css("background","white");
                $("#batterystatus").css("pointer-events","auto");
                $("#batterystatus").css("background","white");

        } else {
                $("#powerpercentage").css("pointer-events","none");
                $("#powerpercentage").css("background","lightgray");
                $("#batterystatus").css("pointer-events","none");
                $("#batterystatus").css("background","lightgray");
        }
});

var switchtype ;
var decider = "false";
//var netdecider = "false";

   $('.network_balancer').on("change",function(){

	     
        var simulation = "false";

        if ($(this).is(':checked')) {
            $(this).attr('value', 'true');
            decider = "true";
            $('.commonlabel').prop("disabled",true); 
            $('.commoncheck').prop("disabled",true); 
        } else {
                $(this).attr('value', 'false');
                decider = "false";
                $('.commonlabel').prop("disabled",false); 
                $('.commoncheck').prop("disabled",false);
             }

            if(decider == "true"){
                simulation = "true";
             } else {
                simulation = "false";
             }

             var free_cpu = $("#cpupercantage").val();
             var free_ram = $("#rampercentage").val();
             var batt_remaining_time = $("#batterytime").val();
             //var primary_link_rssi = $("#link").val();
             //var hops_count = $("#hopcount").val();   
             var uid		= urlObj.uid;

        var simulation ={
        		uid : uid,
                free_cpu : free_cpu,
                free_ram : free_ram,
                batt_remaining_time : batt_remaining_time,
                //primary_link_rssi : primary_link_rssi,
                //hops_count:hops_count,
                profile_type:"basic",
                simulation:simulation              
        }

        console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>" + JSON.stringify(simulation));
            
           var url = "/facesix/rest/ui/simulation";
            
            $.ajax({
             url:url,
                method:'POST',
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify(simulation),
                success:function(response){
                    console.log("success " +response)
                },
                error:function(error){
                    console.log("error " +error)
                }
     });

            
             

    })

   $('.maxval').keypress(function(){
  if ($(this).val() > 100){
      $(this).val('100');
  }
   if ( event.which == 45 || event.which == 189 ) {
      event.preventDefault();
   }
});

    $('.maxvalBat').keypress(function(){
        if ($(this).val() > 3000){
      $(this).val('3000');
  }
  if ( event.which == 45 || event.which == 189 ) {
      event.preventDefault();
   }
  
});

$('.maxvalLink').keypress(function(){
       
});
     $('.hops').keypress(function(){
        if ($(this).val() > 16){
        $(this).val('16');
    }
       
});
     /*
      * A function to check the empty Field and to validate the value to be numeric
      * 
      */
     var fieldEmpty = true;     
     $('.valueCheck').on("change",function(){
    	 
    	 /*
    	  * Validating the range for the values
    	  * 1)Free CPU - 0-100%
    	  * 2)Free Memory - 0-100%
    	  * 3)Battery Remaining Time - 0-2000 min
    	  */
    	 
    	 var curr_id = $(this).attr('id');
    	 var curr_value =$(this).val();
    	 if((curr_id === "cpupercantage") || (curr_id === "rampercentage")){
    		 if(curr_value<0){
    			 $(this).val('0');
    		 }
    		 else if(curr_value>100){
    			 $(this).val('100');
    		 }
    	 }
    	 else if(curr_id === "batterytime"){
    		 if(curr_value<0){
    			 $(this).val('0');
    		 }
    		 else if(curr_value>2000){
    			 $(this).val('2000');
    		 }
    	 }
    	 
    	 
    	 
    	 var free_cpu 				= 	Number($("#cpupercantage").val());
         var free_ram 				= 	Number($("#rampercentage").val());
         var batt_remaining_time 	= 	Number($("#batterytime").val());
         //var primary_link_rssi 		= 	Number($("#link").val());
         //var hops_count 			= 	Number($("#hopcount").val());
         
         /*
          *Based on the following conditions the Toggle button will be disabled.
          *
          *1.If all the field are empty i.e field value equal to zero
          *								(or)
          *2. Atleast one field have invalid value i.e Not a Number(may be string)
          */
         
         if(((free_cpu==0) && (free_ram==0) && (batt_remaining_time==0))
        		|| ((isNaN(free_cpu)) || (isNaN(free_ram)) || (isNaN(batt_remaining_time)))){
        	 $('.network_balancer').prop("disabled",true);
        	 $(this).attr('value', 'false');
        	 fieldEmpty = true;
         }
         else{
        	 $('.network_balancer').prop("disabled",false);
        	 $(this).attr('value', 'true');
        	 fieldEmpty = false;
         }
         
         /*
          * If a current field value is not a number then Display Error message
          */
         
         var curr_value =$(this).val();
         if(isNaN(curr_value)){
        	 document.getElementById("Error-message").innerHTML = "Invalid value";
        	 $(this).val('');
        	 $(this).css("border","1px solid red");
         }
         else{
        	 document.getElementById("Error-message").innerHTML ="";
        	 $(this).css("border","1px solid #ccc");
         }
    	
     });


var cur_selection;

$('.commoncheck').on("change",function(){
            
         if($('.commoncheck').is(':checked') && decider == "false"){
               $('.slider').prop("disabled",false);
               cur_selection = $(this).val();
               $('.network_balancer').prop("disabled",true);
               
        } else {
             $('.slider').prop("disabled",true);
             if(fieldEmpty === false){
            	 $('.network_balancer').prop("disabled",false);
             }
             
        }

         if ($('.slider').is(':checked')) {
                sliderclass("no");
                $('.slider').prop("checked",false);
         } 


});

   function sliderclass(val){
        
    	var uid = urlObj.uid;
        var cur_switch;
        var simulation;
        if(val == "no") {
            cur_switch = "false";
            $('.commonlabel').prop("disabled",false); 
        } else {
             if ($('.slider').is(':checked')) {
            cur_switch = "true";
            $('.commonlabel').prop("disabled",true); 
             } else {
            cur_switch = "false";
            $('.commonlabel').prop("disabled",false); 
           }
        }
       
        if(cur_switch == "true"){ 
                simulation = "true";
            } else {
                simulation = "false";
                
                if(fieldEmpty){
                	$('.network_balancer').prop("disabled",true);
                }
            }
            if(cur_selection == "one"){
                 var free_cpu = "15";
                 var free_ram = "20";
                 var batt_remaining_time = "240";
                 var simulation ={
                		 uid :  uid,
                        free_cpu : free_cpu,
                        free_ram : free_ram,
                        batt_remaining_time : batt_remaining_time,
                        profile_type:"gaming",
                        simulation:simulation
                    }

            } else if(cur_selection == "two"){
                var free_cpu = "50";
                var free_ram = "50";
                var batt_remaining_time = "240";

               var simulation ={
            		   uid :  uid,
                        free_cpu : free_cpu,
                        free_ram : free_ram,
                        batt_remaining_time : batt_remaining_time,
                        profile_type:"moderate",
                        simulation:simulation
                    }

            } else {
                var free_cpu = "90";
                var free_ram = "90";
                var simulation ={
                		 uid :  uid,
                        free_cpu : free_cpu,
                        free_ram : free_ram,
                        profile_type:"idle",
                        simulation:simulation                             
                    }

            }

            console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>" + JSON.stringify(simulation));
            
           var url = "/facesix/rest/ui/simulation";
            
            $.ajax({
                url:url,
                method:'POST',
                contentType: "application/json",
                dataType: "json",
                data: JSON.stringify(simulation),
                success:function(response){
                    console.log("success " +response)
                },
                error:function(error){
                    console.log("error " +error)
                }
         });     
        
    }

$("#root").on("change",function(){
  var cur_dev = $("#root").val();
  location.href = "/facesix/web/mesh/simulation?uid="+cur_dev+"&cid="+urlObj.cid+"&sid="+urlObj.sid+"&spid="+urlObj.spid;
})
 