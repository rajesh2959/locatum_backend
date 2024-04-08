var cur_video_status = "play";
(function () {
    search = window.location.search.substr(1)
    urlObj=JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
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
     var constant_max = 10240;
     var constant_max_audio = 10240;
     var re_app;
    videoStats = {
        timeoutCount: 10000,
        acltables: {
           
            setTable: {
                aclClientsTable: function () {
                    $.ajax({
                        url:'/facesix/rest/ui/videoStats?uid='+urlObj.uid,
                        method: "get",
                        cache: false,
                        //async:true,
                        success: function (result) {

                         //   $('html[manifest=saveappoffline.appcache]').attr('content', '');
                           
                        	$('.vidview').show();
                            var sysResult = result.currentStats;
                            var sysAVg= result.avgStats;                    

                          console.log("video stats response" + JSON.stringify(sysResult));
                                                    
                            var lagDuration = result.lagDuration;
                            var avLatency = result.avLatency;
                            var frameDrops = result.frameDrops;
                            var audioVideoBuffSize = result.audioVideoBuffSize;

                            var frameRate = sysResult.frameRate+" fps";
                            var videoBitRate = sysResult.videoBitRate+" kbps";
                            var fileName = sysResult.fileName;
                            var videoDuration = sysResult.videoDuration;
                            var initialBuffTime = sysResult.initialBuffTime+" sec";
                            var audioBitRate = sysResult.audioBitRate+" kbps";
                            var resolution= sysResult.resolution;

                            var maxVideoBuffSize = sysAVg.maxVideoBuffSize;
                            var minVideoBuffSize = sysAVg.minVideoBuffSize;
                            var maxAudioBuffSize = sysAVg.maxAudioBuffSize;
                            var minAudioBuffSize = sysAVg.minAudioBuffSize;
                            var avgVideoBuffTime = sysAVg.avgVideoBuffTime;
                            var avgAudioBuffTime = sysAVg.avgAudioBuffTime;
                            var maxAvLatency = sysAVg.maxAvLatency;
                            var minAvLatency = sysAVg.minAvLatency;
                            var avgAvLatency = sysAVg.avgAvLatency;
                            var maxLagDuration = sysAVg.maxLagDuration;
                            var minLagDuration = sysAVg.minLagDuration;
                            var avgLagDuration = sysAVg.avgLagDuration;
                            var maxFrameDrops = sysAVg.maxFrameDrops;
                            var minFrameDrops = sysAVg.minFrameDrops;
                            var avgFrameDropLatency = sysAVg.avgFrameDropLatency;
                            
                            var last = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length);
                            $(".fullname").attr("title",last);
                            if(last.length>10){
                            	var firstname = last.substring(0,4);
                            	var lastname = last.substring(last.length-5,last.length);
                            	last = firstname + "..."+lastname;
                            }
                            
                            $('#file').html(last); 
                            
                            $('#resolution').html(resolution);                     
                            $('#frameRate').html(frameRate); 
                            $('#audiorate').html(audioBitRate); 
                            $('#video').html(videoDuration); 
                            $('#initialbuffer').html(initialBuffTime); 
                            $('#videoBitRate').html(videoBitRate);
                            
                            $('#ring').html(maxAvLatency);
                            $('#mincpu').html(minAvLatency);
                            $('#avgcpu').html(avgAvLatency);
                            
                            $('#maxmem').html(maxFrameDrops);
                            $('#minmem').html(minFrameDrops);
                            $('#avgmem').html(avgFrameDropLatency);
                            
                            $('#maxlag').html(maxLagDuration);
                            $('#minlag').html(minLagDuration);
                            $('#avglag').html(avgLagDuration);
                            
                            $('#maxtx').html(maxAudioBuffSize);
                            $('#mintx').html(minAudioBuffSize);
                            $('#avgtx').html(avgAudioBuffTime);
                            $('#maxrx').html(maxVideoBuffSize);
                            $('#minrx').html(minVideoBuffSize);
                            $('#avgrx').html(avgVideoBuffTime);
                            
                                                      
                            n++;    
                          
                            var avLatencyval=[];
                            var avLatencytime=[];
                            for (var i = 0; i < avLatency.length; i++){                                                                                                           
                                    avLatencyval.push(avLatency[i].avLatency);
                                    avLatencytime.push(avLatency[i].time);
                                }

                                 var req_len = 120 - avLatency.length;
                                 for (var i = 0; i < req_len; i++){                                                                                                           
                                    avLatencyval.push(0);
                                }
                                    

                            timeChartDatachart = c3.generate({ 
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#cpugraph', 
                                    data: {
                                     columns: [
                                            ['Latency'].concat(avLatencyval),
                                            ['time'].concat(avLatencytime),
                                        ],
                                        type : 'area-spline',
                                       colors: {
                                    	   Latency: 'rgb(293, 224, 39)',
                                        },
                                    },               
                                    point: {
                                             show: false
                                        },
                                    grid: {
								        x: {
								             lines: [{value: 13},{value: 26},{value: 39},{value: 52},{value: 65},{value: 78},{value: 91},{value: 104},{value: 117}]
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
                                                text:'Time (seconds)',
                                                position: 'outer-center'
                                             },                                             
                                        },
                                        y:{
                                            /*max:100,
                                            tick : {values: [0,20,40,60,80,100]},*/
                                            label:{
                                                text:'AV Latency (seconds)',
                                                position: 'outer-middle',
                                            },                                            
                                        },
                                    }, 
                                         /*   tooltip: {
                                                value:function(value){
                                                    return value
                                                },
                                            contents: function (data, defaultTitleFormat, defaultValueFormat, color) { 
                                                var $$ = this, config = $$.config,
                                                titleFormat = config.tooltip_format_title || defaultTitleFormat,
                                                nameFormat = config.tooltip_format_name || function (name) { return name; },
                                                valueFormat = config.tooltip_format_value || defaultValueFormat,
                                                text, i, title, value;
                                                text = '<table class="c3-tooltip"><tbody>';
                                                for (var i = 0; i < avLatency.length; i++){  
                                                if (! (data[i] && (data[i].value || data[i].value === 0))) { continue; }                                                                                                          
                                                         avLatencyval = avLatency[i].avLatency;
                                                         avLatencytime = avLatency[i].time;                                                                                                                                   
                                                            text += '<tr><th colspan="2">'+avLatencytime+'</th></tr><tr class="c3-tooltip-name--r2"><td class="name text-left"><span style="background-color:#8c564b"></span>AV Latency</td><td class="value">'+[avLatencyval]+'</td></tr>';

                                }
                                                /*for (i = 0; i < data.length; i++) {
                                                    if (! (data[i] && (data[i].value || data[i].value === 0))) { continue; } 
                                                    var tagids = data[i].id;
                                                    var jk = data[i].index; 
                                                     
                                                    text += '<tr><th colspan="2">'+tagids+'</th></tr><tr class="c3-tooltip-name--r2"><td class="name text-left"><span style="background-color:#8c564b"></span>Location Name</td><td class="value">'+activityData[tagids][jk]["locationname"]+'</td></tr><tr class="c3-tooltip-name--r2"><td class="name text-left"><span style="background-color:#8c564b"></span>Entry Time</td><td class="value">'+activityData[tagids][jk]["entry_loc"]+'</td></tr><tr class="c3-tooltip-name--r2"><td class="name text-left"><span style="background-color:#8c564b"></span>Exit Time</td><td class="value">'+activityData[tagids][jk]["exit_loc"]+'</td></tr><tr class="c3-tooltip-name--r2"><td class="name text-left"><span style="background-color:#8c564b"></span>Time Spent</td><td class="value">'+activityData[tagids][jk]["timespent"]+'</td></tr>';
                                                }*/
                                               // text += '</tbody></table>';
                                                //return text;
                                            //},
                                        //}, */

                                        tooltip: {
                                            format: {
                                                title: function (d) { return "" },
                                                value: function (value, ratio, id) {
                                                    return value;
                                                },
                                    
                                            },
                                            
                                        },   
                                            
                                                                      
                                    legend:{
                                        show : false
                                    }
                                });


                                var frame=[];
                                for (var i = 0; i < frameDrops.length; i++){                                                                                                           
                                    frame.push(frameDrops[i].frameDrop);
                                }

                                 var frame_len = 120 - frameDrops.length;
                                 for (var i = 0; i < frame_len; i++){                                                                                                           
                                    frame.push(0);
                                }
                                               
                              memChartDatachart = c3.generate({
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#memorygraph', 
                                    data: {
                                     columns: [
                                            ['FrameDrops'].concat(frame),
                                        ],
                                        type : 'area-spline',
                                        colors:{
                                            FrameDrops:'#33cc33',
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
                                                text:'Time (seconds)',
                                                position: 'outer-center'
                                             }, 
                                        },
                                        y:{
                                            /*max:100,
                                            tick : {values: [0,20,40,60,80,100]},*/
                                            label:{
                                                text:'Drop Count (frames)',
                                                position: 'outer-middle',
                                            }, 
                                        },
                                    },grid: {
                                        x: {
                                             lines: [{value: 13},{value: 26},{value: 39},{value: 52},{value: 65},{value: 78},{value: 91},{value: 104},{value: 117}]
                                        },                                       
                                        lines:{
                                           front : false
                                        }
                                    },
                                    tooltip: {
									        format: {
									            title: function (d) { return "" },
									            value: function (value, ratio, id) {
									                return value;
									            },
									
									        },
									        
									    },                         
                                    legend:{
                                        show : false
                                    }
                                    
                                });

                          
                              var videobuff=[];
                              var audiobuff=[];
                                for (var i = 0; i < audioVideoBuffSize.length; i++){   
                                	var videoBufferSize = audioVideoBuffSize[i].videoBuffSize;
                                	var videoBuff = (videoBufferSize/1024).toFixed(2)
                                    videobuff.push(videoBuff);
                                	
                                	var audioBufferSize = audioVideoBuffSize[i].audioBuffSize;
                                	var audioBuff = (audioBufferSize/1024).toFixed(2)
                                	
                                    audiobuff.push(audioBuff);
                                }

                                 var avbuff_len = 120 - audioVideoBuffSize.length;
                                 for (var i = 0; i < avbuff_len; i++){                                                                                                           
                                     videobuff.push(0);
                                     audiobuff.push(0);
                                }
                           
                              txrxChartDatachart = c3.generate({
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#txrx', 
                                    data: {
                                     columns: [
                                            ['VideobufferSize'].concat(videobuff),
                                            ['AudiobufferSize'].concat(audiobuff)
                                        ],                                        
                                        type : 'spline',
                                        colors:{
                                        	VideobufferSize:'#ff4d4d',
                                        	AudiobufferSize:'#66d9ff',
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
                                                text:'Queue Size (Mbps)',
                                                position: 'outer-middle',
                                            }, 
                                        },
                                    },grid: {
                                        x: {
                                             lines: [{value: 13},{value: 26},{value: 39},{value: 52},{value: 65},{value: 78},{value: 91},{value: 104},{value: 117}]
                                        },                                       
                                        lines:{
                                           front : false
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

                             

                           var lag=[];
                                for (var i = 0; i < lagDuration.length; i++){                                                                                                           
                                    lag.push(lagDuration[i].lagDuration);
                                }
                                var lag_len = 120 - lagDuration.length;
                                 for (var i = 0; i < lag_len; i++){                                                                                                           
                                     lag.push(0);
                                }

                            channelChartDatachart = c3.generate({
                                    size: {
                                        height: 270,
                                    },
                                    bindto: '#channelgraph', 
                                    data: {
                                     columns: [
                                            ['Duration'].concat(lag),
                                        ],
                                        type : 'area-spline',
                                       colors: {
                                    	   Duration: 'rgb(173,216,230)',
                                        },
                                    },
                                    point: {
                                             show: false
                                        },
                                    grid: {
                                        x: {
                                             lines: [{value: 13},{value: 26},{value: 39},{value: 52},{value: 65},{value: 78},{value: 91},{value: 104},{value: 117}]
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
                                                text:'Time (seconds)',
                                                position: 'outer-center'
                                             },                                            
                                        },
                                        y:{
                                           /* max:100,
                                            tick : {values: [0,20,40,60,80,100]},*/
                                             label:{
                                                text:'Lag Duration (seconds)',
                                                position: 'outer-middle',
                                            }, 
                                        },
                                    },      
                                    tooltip: {
								        format: {
								            title: function (d) { return ""},
								            value: function (value, ratio, id) {
								                return value + " Seconds";
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
								            ['CPU',maxAvLatency],
								        ],
								        type : 'gauge',
                                      colors:{
                                          CPU:"transparent",
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
                                      fontsize:20,
                                  },
								    donut: {
								        title: "CPU/MEM/NET"
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
                                            ['CPU',minAvLatency],
                                            //['cpu',30],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            CPU :"transparent",
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
                                           ['CPU',avgAvLatency],
                                             //['cpu',48],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            CPU : "transparent",
                                        },
                                       
                                    },
                                    gauge:{
                                        label: {
                                        format: function(value, ratio) {
                                            return value;
                                            },
                                    show: true, 
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
                                            ['mem',minFrameDrops],
                                            //['mem',30]
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            mem :"transparent",
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
                                            ['MEM',maxFrameDrops],
                                             //['mem',48],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            MEM : "transparent",
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
                                            ['CPU',avgFrameDropLatency],
                                            //['mem',30],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            CPU :"transparent",
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


                                    minlagChartDatachart = c3.generate({
                                    size: {
                                        height: 60,
                                    },
                                     padding: {
                                        top: 0,
                                        right: 15,
                                        bottom: 0,
                                        left: 40,
                                     },
                                    bindto: '#minlag', 
                                   data: {
                                        columns: [
                                            ['mem',minLagDuration],
                                            //['mem',30]
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            mem :"transparent",
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


                                 maxlagChartDatachart = c3.generate({
                                    size: {
                                        height: 60,
                                    },
                                     padding: {
                                        top: 0,
                                        right: 15,
                                        bottom: 0,
                                        left: 40,
                                     },
                                    bindto: '#maxlag', 
                                   data: {
                                        columns: [
                                            ['MEM',maxLagDuration],
                                             //['mem',48],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            MEM : "transparent",
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

                                 avglagChartDatachart = c3.generate({
                                    size: {
                                        height: 60,
                                    },
                                     padding: {
                                        top: 0,
                                        right: 15,
                                        bottom: 0,
                                        left: 40,
                                     },
                                    bindto: '#avglag', 
                                   data: {
                                        columns: [
                                            ['CPU',avgLagDuration],
                                            //['mem',30],
                                        ],
                                        type : 'gauge',
                                        colors:{
                                            CPU :"transparent",
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


                            /*     
                                 if(constant_max < maxVideoBuffSize){

                                        constant_max = maxVideoBuffSize;
                                    }
                                        var maxv_percentage = (maxVideoBuffSize / constant_max) * 100;
                                        var maxv = maxv_percentage.toFixed(1);

                                        var minv_percentage = (minVideoBuffSize / constant_max) * 100;
                                        var minv = minv_percentage.toFixed(1);

                                        var avgv_percentage = (avgVideoBuffTime / constant_max) * 100;
                                        var avgv = avgv_percentage.toFixed(1);


                                if(constant_max_audio < maxAudioBuffSize){

                                        constant_max_audio = maxAudioBuffSize;
                                    }
                                        var maxa_percentage = (maxAudioBuffSize / constant_max_audio) * 100;
                                        var maxa = maxa_percentage.toFixed(1);

                                        var mina_percentage = (minAudioBuffSize / constant_max_audio) * 100;
                                        var mina = mina_percentage.toFixed(1);

                                        var avga_percentage = (avgAudioBuffTime / constant_max_audio) * 100;
                                        var avga = avga_percentage.toFixed(1);

                                        //console.log(">>>>" + percentage + " ??" + constant_max)

                                        maxtx();
                                        mintx();
                                        avgt();
                                        maxrx();
                                        minrx();
                                        avgr();


                                   function maxtx() {
                                      var elem = document.getElementById("maxtx");   
									  var width = maxa;
									      elem.style.width = width + '%'; 
									      elem.innerHTML = width;
									    
                                        }
                                        									  
                                   function mintx() {
                                       var elem = document.getElementById("mintx");   
                                       var width = mina;
                                          elem.style.width = width + '%'; 
                                          elem.innerHTML = width; 
 									    }
 									                                    
                                   function avgt() {
                                       var elem = document.getElementById("avgtx");   
                                      var width = avga;
                                          elem.style.width = width + '%'; 
                                          elem.innerHTML = width;
 									    }
 									
                                     function maxrx() {
                                      var elem = document.getElementById("maxrx");   
                                      var width = maxv;                                     
                                          elem.style.width = width + '%'; 
                                          elem.innerHTML = width;
                                        }
                                                                       
                                   function minrx() {
                                          var elem = document.getElementById("minrx");   
                                          var width = minv;                                   
                                          elem.style.width = width + '%'; 
                                          elem.innerHTML = width; 
                                        }
                                                                        
                                   function avgr() {
                                       var elem = document.getElementById("avgrx");   
                                       var width = avgv;                                   
                                          elem.style.width = width + '%'; 
                                          elem.innerHTML = width;
                                        }
                                            */ 
                                                                          
                            var cur_video_status = localStorage.getItem("someVarKey");
                            if(cur_video_status == "play" || cur_video_status == null){
                                    $('.pause').show();
                                    $('.play').hide();
                                 } else {
                                    $('.pause').hide();
                                    $('.play').show();
                                }
                            
						          setTimeout(function () { 
                                    if(cur_video_status == "play"){                                   
                                       videoStats.acltables.setTable.aclClientsTable(); 
                                     }                                
                                  }, 1000)
                                                    
                       },
                        error: function (data) {
                                setTimeout(function () {
                                      if(cur_video_status == "play"){                                   
                                       videoStats.acltables.setTable.aclClientsTable(); 
                                     }                                 
                                  }, 1000)

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
    videoStats.init();

});

$("#root").on("change",function(){
  var cur_dev = $("#root").val();
  location.href = "/facesix/web/mesh/videostats?uid="+cur_dev+"&cid="+urlObj.cid+"&sid="+urlObj.sid+"&spid="+urlObj.spid;
})
 
$(".pause").on("click",function(){
    $('.pause').hide();
    $('.play').show();
    cur_video_status = "pause";
    localStorage.setItem("someVarKey", cur_video_status);
})
 
$(".play").on("click",function(){
    $('.play').hide();
    $('.pause').show();
    videoStats.acltables.setTable.aclClientsTable();
    cur_video_status = "play";
    localStorage.setItem("someVarKey", cur_video_status);
}) 