(function () {
	search = window.location.search.substr(1)
	urlObj=JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
    //var timer = 500000;
	//var count = 1;
	var peerStats; 
	var counterIncrement  = 0;
	var counterIncrement1 = 0;
	var counterIncrement2 = 0;
	var counterIncrement3 = 0;
	var timeChartDataLoaded = false;
	var timeChartDatachart;
	
    Heatmap = {
        timeoutCount: 10000,
        charts: {
            urls: {
                devicesDeployed:'#', 
                flowchart: ''
            },
            setChart: {
              	devicesDeployed: function (initialData, params) {
                	
                    $.ajax({
                        url:Heatmap.charts.urls.devicesDeployed,
                        success: function (result) {  
                        	resdev= result.devicesConnected[9][1];
                        	//console.log("result" + JSON.stringify(result));
                        	 var str = 0;
                        		 str = resdev;
                                $('#demo-pie-2').html(str);
                                if(counterIncrement1 == 0){
                                	 $('#demo-pie-2').each(function () {
                                         $(this).prop('Counter',0).animate({
                                             Counter: $(this).text()
                                         }, {
                                             duration: 2000,
                                             easing: 'swing',
                                             step: function (now) {
                                                 $(this).text(Math.ceil(now));
                                             }
                                         });
                                     });
                                	counterIncrement1 = 1;
                                }
                                
                                resAct= result.devicesConnected[7][1];
                            	 var strAct = 0;
                            		 strAct = resAct;
                                    $('#piechart3').html(strAct);
                                    if(counterIncrement1 == 0){
                                    	 $('#piechart3').each(function () {
                                             $(this).prop('Counter',0).animate({
                                                 Counter: $(this).text()
                                             }, {
                                                 duration: 2000,
                                                 easing: 'swing',
                                                 step: function (now) {
                                                     $(this).text(Math.ceil(now));
                                                 }
                                             });
                                         });
                                    	counterIncrement1 = 1;
                                    }
                                    
                                 resAssociated= result.devicesConnected[8][1];
                               	 var strAssociated = 0;
                               	 strAssociated = resAssociated;
                                       $('#piechart4').html(strAssociated);
                                       if(counterIncrement1 == 0){
                                       	 $('#piechart4').each(function () {
                                                $(this).prop('Counter',0).animate({
                                                    Counter: $(this).text()
                                                }, {
                                                    duration: 2000,
                                                    easing: 'swing',
                                                    step: function (now) {
                                                        $(this).text(Math.ceil(now));
                                                    }
                                                });
                                            });
                                       	counterIncrement1 = 1;
                                       }
                                    
                                  peerStats = result.devicesConnected; 
                                  var c = peerStats;
                                   		
                                   	Heatmap.charts.chartConfig.typeOfDevices.data.columns = [c[0],c[1],c[2],c[3],c[4],c[5],c[6]]
                                   	Heatmap.charts.getChart.typeOfDevices = c3.generate(Heatmap.charts.chartConfig.typeOfDevices);
                                     
                                   	timeChartData.data1.push(new Date(result.chartDetails[2]));
                                   	timeChartData.data2.push(result.chartDetails[1]);
                                   	  
                                   	
                                   	if(timeChartDataLoaded == false){
                                   		timeChartDatachart = c3.generate({
                                       		bindto: '#dd-chart10', 
                                       	    data: {
                                       	        x: 'x',
//                                       	        xFormat: '%Y%m%d', // 'xFormat' can be used as custom format of 'x'
                                       	     columns: [
                                       	            ['x'].concat(timeChartData.data1),
                                       	            ['Clients'].concat(timeChartData.data2), 
                                       	        ]
                                       	    },
    	                                   	 padding: {
    	                                         top: 30,
    	                                         right: 30,
    	                                         bottom: 30,
    	                                         left: 60,
    	                                     },
                                       	    axis: {
                                       	        x: {
                                       	            type: 'timeseries',
                                       	            tick: {
                                       	            	fit: true,
                                       	                format: '%m-%d-%Y \n %H:%M:%S'
                                       	            }
                                       	        },
    	                                     	y: {
    	                                     		min: 0,
    	                                     		tick: {
    	                                     			format: d3.format('d')
    	                                     		}
    	                                     	}    	                                     
                                       	    }
                                       	});
                                   		
                                   		timeChartDataLoaded = true;
                                   	}else{
                                   		timeChartDatachart.load({
                                   	        columns: [
                                   	        	['x'].concat(timeChartData.data1),
                                   	            ['Clients'].concat(timeChartData.data2), 
                                   	        ]
                                   	    });
                                   	}
                                   	
                                   	
                                   	
                                   	// console.log("peerStats>>>>>>>" + JSON.stringify(peerStats));
                                
                           setTimeout(function () {
                             Heatmap.charts.setChart.devicesDeployed();
                           }, Heatmap.timeoutCount);
                        },
                        error: function (data) {
                            //console.log(data);
                           setTimeout(function () {
                             Heatmap.charts.setChart.devicesDeployed();
                           }, Heatmap.timeoutCount);
                        },
                        dataType: "json"
                    });
                },
                typeOfDevices: function (initialData,params) {
                	
                },
                flowchart: function (initialData) {
                	
                },
            },
            getChart: {},
            chartConfig: {
                txRx: {
                    size: {
                        height: 320,
                    },
                    bindto: '#fd_chart2',

                    padding: {
                        top: 10,
                        right: 15,
                        bottom: 0,
                        left: 40,
                    },
                    data: {
                        columns: [],
                        types: {
                            Rx: 'area',
                            Tx: 'area-spline'
                        },
                        colors: {
                            Tx: '#5cd293',
                            Rx: '#1a78dd'
                        },
                        color: {
                            pattern: ['#2F9E63', '#1a78dd']
                        },
                        point: {
                            show: true
                        }
                    },
                     legend:{
                        item:{

                            "onclick":function(id){
                               Heatmap.charts.getChart.txRx.focus(id);  
                            }
                        }
                     },
                    tooltip: {
                        show: false
                    },
                    point: {
                        show: false
                    },
                    axis: {
                        x: {
                            type: 'category',
                            padding: {
                                left: -0.5,
                                right: -0.5,
                            },
                        },
                        y: {
                            padding: { bottom: 0 },
                            min: 0,
                            tick: {
                                format: d3.format("s")
                            }
                        },
                    }
                    
                },
                typeOfDevices: {
                    size: {
                        height: 270,
                    },
                    bindto: '#dd-chart5',
                    padding: {
                        top: 0,
                        right: 15,
                        bottom: 0,
                        left: 15,
                    },
                    data: {
                        columns: [
                            ['2G', 7],
                            ['5G', 12],
                        ],
                        colors: {
                            "2G": '#85d1fb',
                            "5G": '#79d58a',
                        },
                        type: 'donut'
                    },
                    donut: {
                        title: "",
                        label: {
                            threshold: 0.03,
                            format: function (value, ratio, id) {
                                0;
                            }
                        },
                        width: 40
                    },
                    tooltip: {
                        format: {
                            value: function (value, ratio, id) {
                            	if (value == 1) { return value + ' Client';} else
                                	return value + ' Clients';
                            }
                        }
                    },
                    axis: {
                        x: {
                            show: false
                        }
                    },
                    legend: {
                        show: true
                    }

                },
                devicesDeployed: {
                    innerHTML: '',
                    showProgress: 1,
                    initialPos: 0,
                    targetPos: 3,
                    scale: 100,
                    rotateBy: 360 / 6,
                    speed: 900,
                    delayAnimation:false,
                    onFinishMoving: function (pos) {
                    }
                },
                avgDownlinkSpeed: {}
            }
        },
        init: function (params) {
            var c3ChartList = ['devicesDeployed', 'flowchart','typeOfDevices'];
            var that = this;
            $.each(c3ChartList, function (key, val) {
                that.charts.setChart[val](true,params?params:"");
            });
        },

    }
})();
currentDashboard=Heatmap;

var associateONOFF = 1;
var clientsONOFF = 1;
var toggleDevice = 1;

$('#clientsONOFF').change(function(){ 
	if($(this).prop('checked') == true){
		clientsONOFF = 1;
		//toggleDevice = clientsONOFF;	    			
		d3.selectAll('.person').classed('tagdisable', false); 
		//floormapConfig.getDevices(toggleDevice)
		$('.tagsfilters').removeClass('active');
		if($('#associateONOFF').prop('checked') == true){ 
			d3.selectAll('.activeClients').classed('tagdisable', false); 
			d3.selectAll('.associatedClients').classed('tagdisable', false);
		}else{
			d3.selectAll('.associatedClients').classed('tagdisable', true);
			d3.selectAll('.activeClients').classed('tagdisable', false); 
		}	    			
	}
	else{
		clientsONOFF = 0;
		//toggleDevice = clientsONOFF;
		d3.selectAll('.person').classed('tagdisable', true); 
		//floormapConfig.getDevices(toggleDevice)
		$('.tagsfilters').addClass('active');
	}  
});
$('#associateONOFF').change(function(){ 
	if($(this).prop('checked') == true){
		associateOn = 1;
		toggleDevice = associateOn
		floormapConfig.getDevices(toggleDevice)
		d3.selectAll('.activeClients').classed('tagdisable', false); 
		d3.selectAll('.associatedClients').classed('tagdisable', false); 
	}
	else{
		associateOn = 0;
		toggleDevice = associateOn
		floormapConfig.getDevices(toggleDevice)
		d3.selectAll('.associatedClients').classed('tagdisable', true);
		d3.selectAll('.activeClients').classed('tagdisable', false); 
	}  
});


//Network config Replica
var imageW=40;
var imageH=40;
var count;

 var timeSeries = "";

 var timeChartDataLoaded = false;
 var timeChartData =  { 
		    "data1": [],
		    "data2": []
		};
 
 
	 
var floormapConfig={
		
	    'plantDevices':function(image,type,x,y,status,uid,taginfo,toggleDevice){
	        var obj;
	        var urlMap={
	            "server":'dashboard',
	            'switch':'swiboard',
	            'ap':'devboard',
	            'sensor':'devboard'
	        }
	       
	        if (type == "server") {
	            var url="javascript:void(0)" 
	        }
	        else {
	            var url="javascript:void(0)" 
	        }
	        
	        var mcIds = 'device-'+uid;
			mcIds = mcIds.replace(/:/g , "-");
   			var tagsFound = document.getElementById(mcIds); 
   			if(tagsFound == null){
   				var anchor   = this.svg.append("a").attr("xlink:href",url);   				
   		        var newImage = anchor.append("image") 
   		        .attr({
   		            'x':x,
   		            'y':y,
   		            'xlink:href':image,
   		            'status':status,
   		            'height':imageH,
   		            'width':imageW,
   		            'id':mcIds,
   		            'data-uid':uid,
   		            'class': 'animatedImage',
   		            'type':type
   		        });
   			}
   			
	        var tagtype  = "\uf10a";
	        var counter  = "0";
	        var color 	 = "rgba(246, 70, 75, 0.5)"//"#90EE90";
		    	var tagcolor = "#FFCCCB";
		    	var newClass = '';
		    	var disabledClass = '';
		    	if (taginfo != undefined) {
		    		obj = taginfo;
		    		count = obj.length;
		    		
		    	}
	    	
	    	
	    	d3.selectAll('.'+mcIds).classed('tagdisables', true);
	    	
	    	//console.log (" UID " + uid  +" count " +count)
	    	
	    	
	    	for(var i = 0; i < count; i++) { 
	    		
	    		tags = obj[i];
	    		var mcId = 'tags-'+tags.mac_address;
   				mcId = mcId.replace(/:/g , "-");
   				mcId = mcId.split('.').join("");
   				mcId = mcId.split(' ').join("");
    			var tagsFound = document.getElementById(mcId); 
    			    			
    			myx = Math.floor(Math.random() * 51) - 20
    			myy = Math.floor(Math.random() * 51) - 20
    			myx = x*1+5+myx;
    			myy = y*1+13+myy;
    			maa = myx*1+8;
    			mbb = myy*1-5;
    			
    			if (tags.associated == undefined) {
    				tags.associated = "NA";
    			}
    			
    			if(tags.associated == "Yes" || tags.associated == "true"){
	    			newClass = 'associatedClients';
	    			color 	= 'rgba(64, 224, 208, 0.5)';
	    		} else {
   					newClass = 'activeClients';
   					color 	 = "rgba(246, 70, 75, 0.5)"
   				}
    			
    			if(clientsONOFF == 1 || toggleDevice == 1){
    			if(tagsFound == null)
    			{
     				var mainGroup = this.svg.append('g')
	    				.attr('id', mcId)
	    				.attr("class","person animateZoom "+newClass+' '+mcIds+' '+mcId) 
	    				.attr('data-x',myx) 
	    				.attr('data-y',myy)  
	    				.attr('transform', "translate("+myx+","+myy+")") 
	    				.attr('data-html', 'true')
	    				.attr('title',"Client : "+tags.mac_address + " <br/> Channel:" + tags.channel + " <br/> Signal:" + tags.signal + " <br/> Associated:" + tags.associated );
	    				$(mainGroup).tooltip({container:'body'});
	    			
	    			var subGroup = mainGroup.append('g')
	    				.attr('id', mcId+'-sub') 
	    				.attr('transform','translate(0,0)') 
					.attr("class","onlyscale"); 
	    			
	    			var circle = subGroup.append("circle")
	    				.attr("r", '30')
	    				.attr("fill", color) 
	    				.attr("y", "0").
	    				attr("class", "animateZoomCircle");
	    			var txt = subGroup.append("text") 
	    				.attr("alignment-baseline",'middle')  
	    				.attr("font-family","FontAwesome")
	    				.style("fill",'#fff')
	    				.style("cursor","pointer")  
	    				.attr("text-anchor", "middle") 
	    				.attr("y", '9') 
	    				.attr('font-size', function(d) { return '30px';} )
	    				.text(function(d) { return tagtype; });
	    			
	    			//console.log('Welcome');

    			}
	    		else{
	    			//console.log ("Found Count " + mcId)
	    			d3.selectAll('.'+mcId).classed('tagdisables', false);
	    			d3.selectAll('.associatedClients').classed('tagdisable', false);
	    			d3.selectAll('.activeClients').classed('tagdisable', false); 
	    	    	
    			}
	    	} 
	    	}
	    		    	
	    	var zoom = $('#plan').attr('data-zoom');
	    	floorChange(zoom);
	    	function floorChange(e){
	    		var circleRadius = 10;
	    		var fSize = 10;
	    		fnsize = (10/e)+3; 
	    		circleRadius = (10/e)+3; 
	    		var newSize = e/3;
	    		//console.log(newSize);
	    		var cyposition = 9;
	    		var image = d3.select("svg.floorsvg").selectAll(".animatedImage");
	    		if(newSize >= 20){
	    			image.attr('width', '3px')
	    			 .attr('height', '3px'); 
	    		}  
	    		else if(newSize >= 15){
	    			image.attr('width', '6px')
	    			 .attr('height', '6px'); 
	    		}
	    		else if(newSize >= 10){
	    			image.attr('width', '8px')
	    			 .attr('height', '8px'); 
	    		}
	    		else if(newSize >= 5){
	    			image.attr('width', '10px')
	    			 .attr('height', '10px'); 
	    		}
	    		else if(newSize >= 3){
	    			image.attr('width', '15px')
	    			 .attr('height', '15px'); 
	    		}
	    		else if(newSize >= 1){
	    			image.attr('width', '20px')
	    			 .attr('height', '20px'); 
	    		}
	    		else if(newSize >= .5){
	    			image.attr('width', '20px')
	    			 .attr('height', '20px'); 
	    		}
	    		else if(newSize >= .3){
	    			image.attr('width', '40px')
	    			 .attr('height', '40px'); 
	    		}
	    		else{
	    			image.attr('width', '40px')
	    			 .attr('height', '40px'); 
	    		}	
	    		 
	    		if(newSize >= 20){
	    			fnsize=1;
	    			cyposition = .3;
	    		} 
	    		else if(newSize >= 10){
	    			fnsize=2;
	    			cyposition = .8;
	    		}
	    		else if(newSize >= 5){
	    			fnsize=3;
	    			cyposition = 1;
	    		}
	    		else if(newSize >= 2){
	    			fnsize=6;
	    			cyposition = 2;
	    		}
	    		else if(newSize >= 1){
	    			fnsize=8;
	    			cyposition = 3;
	    		}
	    		else if(newSize >= .5){
	    			fnsize = 15;
	    			cyposition = 5;
	    		} 
	    		else{
	    			fnsize = 30;
	    			cyposition = 9;
	    		} 
	    		
	    		var circle = d3.select("svg.floorsvg").selectAll("circle");
	    		circle.attr('r', fnsize);
	    		var txt = d3.select("svg.floorsvg").selectAll("text");
	    		txt.attr('font-size', fnsize+'px');
	    		txt.attr('y', cyposition);
	    	} 
	    	
	     },
	     
	     showPopupMenu:function(evt){
	            evt.preventDefault();
	            var offsets=$(this).offset();
	            var status=$(evt.target).attr("status");
	            var uid=$(evt.target).attr("data-uid");
	            $(".viewActivity").attr("href",$(this).attr("href"))
	            $(".powerBtn").attr("uid",uid);
	            floornetworkConfig.moveElementandShow(offsets,uid,status)
	     },
	     moveElementandShow:function(offsets,uid,status){
	            $("#deviceHeading").text(uid);
	            $("#status").text(status);
	            $(".networkconfig").show().css({
	                'position':'absolute',
	                'left':offsets.left+(imageW/2),
	                'top':offsets.top+(imageH/2)
	            });
	     },
	     'fetchurlParams':function(search){
	        var urlObj={}
	        if(search)
	          urlObj = JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
	        this.urlObj = urlObj;
	        return urlObj; 
	    },
	    
	    
	     getDevices:function(){
	    	 
	    	var that 	= this;
	        var urlObj  = this.fetchurlParams(window.location.search.substr(1));
	        var cur_dev = urlObj.uid;
        	urlLink 	= '/facesix/rest/site/portion/networkdevice/heatMapDeviceList?spid='+urlObj.spid
        	
        	 var probCount 	= 0;
        	 var assCount 	= 0;
        	 var devCount 	= 0;
        	 var devType  	= "";
        	 
	        $.ajax({
	            url:urlLink,
	            method:'get',
	            success:function(response){
	            	 var devices=response;

	            	 devices = devices.list;
	            	
	            	 probCount  = response.probCount;
                     assCount   = response.assCount;
                     devCount   = response.devCount;
                     timeSeries = response.chartDetails;
                     var dupsCount = response.dupsCount;
                     devType	   = response.devType;
                     
	            	// console.log("Device LIst " +JSON.stringify(devices));
	            	 
                     $('#demo-pie-2').html(devCount);
                     $('#piechart3').html(probCount);
                     $('#piechart4').html(assCount);
                     
                   // console.log("Active Clients " +probCount + " Duplicate  Clients " +dupsCount)
                     
                	Heatmap.charts.chartConfig.typeOfDevices.data.columns = [devType[0],devType[1],devType[2],devType[3],
                		 devType[4],devType[5]]
                   	Heatmap.charts.getChart.typeOfDevices = c3.generate(Heatmap.charts.chartConfig.typeOfDevices);
                
                    	
                     timeChartData.data1.push(new Date(response.chartDetails[2]));
                        if(response.chartDetails[1] > 0){
                        timeChartData.data2.push(response.chartDetails[1]);
                    } else {
                    }
                   
                     

                     if(timeChartDataLoaded == false){
                    		timeChartDatachart = c3.generate({
                        		bindto: '#dd-chart10', 
                        	    data: {
                        	        x: 'x',
//                        	        xFormat: '%Y%m%d', // 'xFormat' can be used as custom format of 'x'
                        	     columns: [
                        	            ['x'].concat(timeChartData.data1),
                        	            ['Clients'].concat(timeChartData.data2), 
                        	        ]
                        	    },
                            	 padding: {
                                  top: 30,
                                  right: 30,
                                  bottom: 30,
                                  left: 60,
                              },
                        	    axis: {
                        	        x: {
                        	            type: 'timeseries',
                        	            tick: {
                        	                format: '%m-%d-%Y \n %H:%M:%S'
                        	            }
                        	        },
                        	    	y: {
                                 		min: 1,
                                 		tick: {
                                 			format: d3.format('d')
                                 		}
                                 	}    
                        	    }
                        	});
                    		
                    		timeChartDataLoaded = true;
                    	}else{
                    		timeChartDatachart.load({
                    	        columns: [
                    	        	['x'].concat(timeChartData.data1),
                    	            ['Clients'].concat(timeChartData.data2), 
                    	        ]
                    	    });
                    	}
                   	
                     
	            	var ii = 0;
	            	for(ii = 0; ii < devices.length; ii++)
	            	{
	            		if(devices[ii].parent=="ble")
	            			var type = "sensor";
	            		else
	            			var type = devices[ii].typefs;

	            		var status  = devices[ii].status;
	            		var image	= "/facesix/static/qubercomm/images/networkicons/"+type+"_"+status+".png";
	            		var uid		= devices[ii].uid;
	            		var taginfo = devices[ii].heatmap; 
	            		that.plantDevices(image,type,devices[ii].xposition,devices[ii].yposition,status,uid,taginfo);

	            	}
	            	
	            	setTimeout(function() {
	            			// d3.selectAll('.person').classed('tagsinactive', true);
		    		        //$('.tooltip').remove();  
	            		 	floormapConfig.getDevices(); 
	            		 	
	            	},10000);

	            },
	            error:function(err){
	                //console.log(	err);    
	            }
	        });
	     },
	     
	   /* getCount:function(u_id){
	       
	    	var urlObj = this.fetchurlParams(window.location.search.substr(1));
	     	var person = 0;
	     	var spid = urlObj.spid;
	     	var scantimer = "5m";
	     	var u_id;
	     	//console.log("uiddddddd" + u_id);
	     	
	     	if (u_id == undefined || u_id == null) {
	     		u_id = urlObj.uid;
	     	}
	     	urlLink	= '/facesix/rest/qubercomm/scanner/heatmap_list?spid='+spid;
	        $.ajax({
	            url:urlLink,
	            method:'get',
	            success:function(response){
	            	
	            	 var heat = response.probe_req_stats;
	            	 if (heat != undefined) {
		            	 count	= heat.length;
		            	 taginfo = heat;
		            	// console.log("Taginfo>>>>>>>>" + JSON.stringify(taginfo));	            		 
	            	 }
	            	 	    				
	            	 setTimeout(function() {
		    				//$('.person').remove();
		    				//$('.heatcircle').remove();
	            		 	floormapConfig.getCount(u_id);
	            		 	floormapConfig.getDevices();
	            	 },10000);
	            },
	            error:function(err){
	                console.log(	err);    
	            }
	        });
	     }*/
}

$("#closebutton").on('click',function(evt){
    evt.preventDefault();
    $(".networkconfig").hide();
})
$(document).on('click',function(evt){
    $(".networkconfig").hide();
})  


//fullscreen network map
    $('.enlarge').click(function(e){
        $('.floorCan').toggleClass('deviceexpand');
        $('.floorCan').toggleClass('pad0');
        $('.na-panel').toggleClass('height100');
    });


function setHW (h, w) {
	imageW = w;
	imageH = h;
	//console.log (imageW + " " + imageH)
	
}
var fidselected = 0;
function venuename( name = null){
	//console.log(name);
	 search = window.location.search.substr(1)
	 u =JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
	 	  
	 var cid = u.cid;
	 var sid = u.sid;
	 
	 if (u.sid == "" || u.sid == undefined || u.sid == null) {
		 sid =  $('#venuename').val();
	 } 
	 sid =  $('#venuename').val();
	 //console.log ("SID" +  sid)
	 //console.log ("SPID" + u.spid)
	 //console.log(sid);
	 $.ajax({
			type : "GET",
			url : "/facesix/rest/beacon/trilaterationReports/floorlist?cid="+cid+"&sid="+sid,
			dataType : "json",
			success : function(data) {
				$('#floorname').html('');
				 
				$.each(data.portion, function(i, obj) {
					var div_data = "<option value=" + obj.id + ">"+ obj.name + "</option>";
					$(div_data).appendTo('#floorname'); 
				});
				$('#floorname').val();
				$("#floorname option:first").attr('selected',true); 
				if (u.spid == "" || u.spid == undefined || u.spid == null) {
					$("#floorname option:nth-child(1)").attr('selected',true); 
					 $('#floorname').trigger('change');
				}
				
				 function getParameterByName(name, url) {
					    if (!url) url = window.location.href;
					    name = name.replace(/[\[\]]/g, "\\$&");
					    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
					        results = regex.exec(url);
					    if (!results) return null;
					    if (!results[2]) return '';
					    return decodeURIComponent(results[2].replace(/\+/g, " "));
					}
					var newspid = getParameterByName('spid'); 
					if(newspid != null && newspid != '' && fidselected <= 0){
						$('#floorname').val(newspid);
						fidselected = fidselected + 1;
					}
					 
					if(name != null)
					{
						$('#floorname').trigger('change');
					} 	 
					//console.log(newspid + ' newspid'); 
			}
	 		
	 });
	  
}

function floorname(){
	 search = window.location.search.substr(1)
	 u = JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
	 
	  var spid = $('#floorname').val();
	  var sid  =  $('#venuename').val();
	  
	  if (spid != null && spid != "" && spid != undefined) {
		  location.href= "/facesix/web/site/portion/fullheatmap?sid="+sid+"&spid="+spid+"&cid="+u.cid;
	  }
}

$('#heatmapreport').click(function(e) {
	search = window.location.search.substr(1)
	u = JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
	 
    reportlink = "/facesix/rest/heatMapReport/pdf?cid="+u.cid+"&sid="+u.sid+"&spid="+u.spid;
	//console.log(reportlink);
	// reportlink = "/facesix/rest/heatMapReport/pdf?cid=5a13e095db9a524c5e12d3be&sid=5a13e0bedb9a524c5e12d3c0&spid=5a13ece9db9a523b5953d8a4";
    window.open(reportlink);
});

