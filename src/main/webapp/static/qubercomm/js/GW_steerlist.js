var imageW	  = 40;
var imageH	  = 40;
var fzie = 30;
var txty = 9;
var timeoutCount = 20;
var cur_tablen = 10;
var row_limit;
(function (v) {
	search = window.location.search.substr(1)
	urlObj=JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')

	var peerStats;
	var counterIncrement = 0;
	var counterIncrement1 = 0;
	var counterIncrement2 = 0;
	var counterIncrement3 = 0;
	var timeSeries = "";
	
	 steerBoard = {
        timeoutCount: 10000,
        tables: {
            url: {
                activeClientsTable: '/facesix/rest/client/cache/device_assoc_client?cid='+urlObj.cid
            },
            setTable: {
                activeClientsTable: function () {
                    $.ajax({
                        url: steerBoard.tables.url.activeClientsTable,
                        method: "get",
                        success: function (result) {
                        	
                        	var clientConnected = result.clientConnected;
                            
                           // console.log (">>>>>>>>>>>>>>>>>>>table"+ JSON.stringify(result))
                            
                        		if (clientConnected && clientConnected.length) {
                        			
                        			//Default declarations
                                	var row_limit = 5;
                                    var show_previous_button = false;
                                    var show_next_button     = false;
                                    
                                    _.each(clientConnected, function (i, key) {
                                        i.index = key + 1;
                                    })
                        			
                                    steerBoard.activeClientsData = clientConnected;
                                    
                                    var source = $("#chartbox-acl-template").html();
                                    var template = Handlebars.compile(source);
                                    
                                    //Getting the table name
                                	var tableName = $('.pageref').attr("data-table-name");
                                	
                                	/*
                                	 * If tablename is undefined, tableblock not yet initialised
                                	 * 
                                	 */
                                    
                                	if(tableName == undefined) {
    	                                if (clientConnected.length > 5) {
    	                                    var filteredData = clientConnected.slice(0, 5);
    	                                    show_next_button = true;
    	                                } else {
    	                                    var filteredData = clientConnected;
    	                                }
    	
    	                                var rendered = template({
    	                                    "data": filteredData,
    	                                    "current_page": 1,
    	                                    "show_previous_button": show_previous_button,
    	                                    "show_next_button": show_next_button,
    	                                    "startIndex": 1
    	                                });
                                	} else{
                                    	var $tableBlock  = $('#' + tableName);
                                        var current_page = $tableBlock.attr('data-current-page');
                                        current_page     = parseInt(current_page);
                                        row_limit         = parseInt($('#tablelength').val());
                                       
                                        /*
                                         * Assign the start index to slice the table
                                         */
                                        var start_index = (current_page - 1) * row_limit ;
                                         
                                        if(clientConnected.length <= start_index) {
                                        	var diff     = start_index - clientConnected.length;
                                        	current_page = current_page - parseInt(diff/row_limit) - 1;
                                        	start_index  = (current_page - 1) * row_limit ;
                                        }
                                        
                                        var previous_page = 0;
                                        
                                        /*
                                         * If current page is not equal to 1 then get the previous page
                                         */
                                        
                                        if(current_page != 1) {
                                        	previous_page = current_page - 1;
                                        }
                                        
                                        /*
                                         * Set the previous_button and next_button 
                                         */
                                        if (previous_page != 0) {
                                            show_previous_button = true;
                                        }
                                        if (steerBoard.activeClientsData.length > current_page * row_limit) {
                                            show_next_button = true;
                                        }
                                        
                                        var filteredData = clientConnected.slice(start_index, (start_index + row_limit));
                                        var rendered     = template({
    	                                    "data": filteredData,
    	                                    "current_page": current_page,
    	                                    "show_previous_button": show_previous_button,
    	                                    "show_next_button": show_next_button,
    	                                    "startIndex": start_index
    	                                });
                                        
                                    }
                                	$('.acl-table-chart-box').html(rendered);
                                	$('#tablelength').val(row_limit);
                                    $('table .showPopup ').on("tap",rightMenu);
                                    
                            }                  
                        		
                        		var radioType = result.radioType;
                        		
                        		console.log("radioType.total"+radioType.total);
								var peerCount = radioType.total;
                                    $('#peerCount').html(peerCount);
                                    if(counterIncrement1 == 0){
                                     	 $('#peerCount').each(function () {
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
                                    
                                    var twoG = radioType._2G;
                                    $('#twoG').html(twoG);
                                    if(counterIncrement1 == 0){
                                     	 $('#twoG').each(function () {
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
                                    var fiveG = radioType._5G;
                                    $('#fiveG').html(fiveG);
                                    if(counterIncrement1 == 0){
                                     	 $('#fiveG').each(function () {
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

                            setTimeout(function () {
                              steerBoard.tables.setTable.activeClientsTable();
                              steerBoard.charts.setChart.activeConnections();
                              var mac = $('.cur_drop').val();
                              if(mac !== null){
                                  openTable(mac);
                                  }  
                           }, timeoutCount*1000);                            

                        },
                        error: function (data) {
                            setTimeout(function () {
                               steerBoard.tables.setTable.activeClientsTable();
                              steerBoard.charts.setChart.activeConnections();
                              var mac = $('.cur_drop').val();
                              if(mac !== null){
                                  openTable(mac);
                                  }  
                           }, timeoutCount*1000);                            
                        },
                        dataType: "json"

                    });
                }
            }
        }, 
        charts: {
            urls: {
                activeConnections:'/facesix/rest/site/portion/networkdevice/deviceCounts?cid='+urlObj.cid, 
            },
            setChart: {
                activeConnections: function (initialData,params) {
                    $.ajax({
                        url:steerBoard.charts.urls.activeConnections,
                        success: function (result) {
                        	console.log(" activeConnections" + JSON.stringify(result));
                            steerBoard.charts.chartConfig.activeConnections.targetPos = targetPos = result;
                            steerBoard.charts.chartConfig.activeConnections.innerHTML ='<i class="fa fa-tags"></i></br>0';
                            	 
                                counter=0;
                                

                                    var deployedAp = result.devicesCounts[3][1];
                                    $('#deployedAp').html(deployedAp);
                                    if(counterIncrement1 == 0){
                                     	 $('#deployedAp').each(function () {
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
                                                                                                                              
                        },
                        error: function (data) {
                            //console.log(data);                           
                        },
                        dataType: "json"
                    });
                },
             
            },

            getChart: {},
            chartConfig: {
                activeConnections: {
                    innerHTML: '',
                    showProgress: 1,
                    initialPos: 0,
                    targetPos: 3,
                    scale: 500,
                    rotateBy: 360 / 6,
                    speed: 900,
                    delayAnimation:false,
                    onFinishMoving: function (pos) {
                        //console.log('done ', pos);
                    }
                },
            }
        },
        init: function (params) {
            var c3ChartList = ['activeConnections'];
            var that = this;
            
            var tableList   = ['activeClientsTable']
           
            $.each(tableList, function (key, val) {
                that.tables.setTable[val]();
            });   
            
            $.each(c3ChartList, function (key, val) {
                that.charts.setChart[val](true,params?params:"");
            });
          //  this.systemAlerts();
        },
    }
})();
currentDashboard=steerBoard;
function openTable(val){
	//console.log("this v;" + val);	
	var time = "5m";
	var url = '/facesix/rest/site/portion/networkdevice/networkBalancer?cid='+urlObj.cid+'&macId='+val+'&time='+time;
	$.ajax({
 	  	url:url,
 	  	method:'GET',
 	  	dataType: "json",
 	  	headers:{
 	  		'content-type':'application/json'
 	  	},
 	  	success:function(response){
 	 			//console.log("response" + JSON.stringify(response));
 	 				$('.newTab').show();
 	 			  var result=response;
          if(response == ""){

          result = [
                      {
                        "payload": "No payload Available",
                        "time": "-",
                        "opcode": "-",
                        "status": "-"
                      }
                  ]
              } 
                                                      
							if (result && result.length) {
								
								//Default declarations
                            	var row_limit = 5;
                                var show_previous_button = false;
                                var show_next_button     = false;
                                
                                _.each(result, function (i, key) {
                                    i.index = key + 1;
                                })
								
								steerBoard.activeClientsSummary = result;
								
                                var source = $("#chartbox-active-template").html();
                                var template = Handlebars.compile(source);
                                
                                //Getting the table name
                            	var tableName = $('.pagerefone').attr("data-table-name");
                            	
                            	/*
                            	 * If tablename is undefined, tableblock not yet initialised
                            	 * 
                            	 */
                            	if(tableName == undefined) {
	                                if (result.length > 5) {
	                                    var filteredData = result.slice(0, 5);
	                                    show_next_button = true;
	                                } else {
	                                    var filteredData = result;
	                                }
	
	                                var rendered = template({
	                                    "data": filteredData,
	                                    "current_page": 1,
	                                    "show_previous_button": show_previous_button,
	                                    "show_next_button": show_next_button,
	                                    "startIndex": 1
	                                });
                            	} else{
                                	var $tableBlock  = $('#' + tableName);
                                    var current_page = $tableBlock.attr('data-current-page');
                                    current_page     = parseInt(current_page);
                                    row_limit         = parseInt($('#tablelengthone').val());
                                   
                                    /*
                                     * Assign the start index to slice the table
                                     */
                                    var start_index = (current_page - 1) * row_limit ;
                                     
                                    if(result.length <= start_index) {
                                    	var diff     = start_index - result.length;
                                    	current_page = current_page - parseInt(diff/row_limit) - 1;
                                    	start_index  = (current_page - 1) * row_limit ;
                                    }
                                    
                                    var previous_page = 0;
                                    
                                    /*
                                     * If current page is not equal to 1 then get the previous page
                                     */
                                    
                                    if(current_page != 1) {
                                    	previous_page = current_page - 1;
                                    }
                                    
                                    /*
                                     * Set the previous_button and next_button 
                                     */
                                    if (previous_page != 0) {
                                        show_previous_button = true;
                                    }
                                    if (steerBoard.activeClientsSummary.length > current_page * row_limit) {
                                        show_next_button = true;
                                    }
                                    
                                    var filteredData = result.slice(start_index, (start_index + row_limit));
                                    var rendered     = template({
	                                    "data": filteredData,
	                                    "current_page": current_page,
	                                    "show_previous_button": show_previous_button,
	                                    "show_next_button": show_next_button,
	                                    "startIndex": start_index
	                                });
                                    
                                }
                            	$('.table-chart-box').html(rendered);
                                $("#tablelengthone").val(row_limit);
                                $('table .showPopup ').on("tap",rightMenu);
                                
                            }

                  loadactive(val);

 	  	},
 	  	error:function(error){
 	  		console.log(error);
 	  	}
	})


}

var GatewayFinder   = false;
var Gateway 		= false;

function solutionInfo(s1,s2,s3,s4,s5){
	Gateway 		= s1;
	GatewayFinder   = s4;
	finder 			= s5;
}

//Network config Replica

var circleval = 0;
var inactval  = 0;	

function showTag(v) {
	if (v == "1") {
		$('.person').show();
		$('.qrnd').show();
	} else {
		$('.person').hide();
		$('.qrnd').hide();
	}

}

function zoomicon(value) {
	if (value == "1") {
		$('.slider-section').show();
	} else {
		$('.slider-section').hide();
	}

}

var isReady 	=  false;
var gway 		= "false";
var extryexit 	= "false";
var locatum 	= "true"; 
var getTimer;
var spid;

var list;
var tagtype 	= "\uf007";
var color 		= "#4337AE"//"#90EE90"
var counter 	= "0";
var tagcolor 	= "#FFA500";
var bDemofound  = false;
var tagsCounter = 0;
var tagStatus 	= 0;

var filterTagactive = 1;
var filterCategories = [];
var zoomEnabled = 0;
var tagsONOFF = 1;
var inactiveONOFF = 1;
var switchONOFF = 1;
var category = [];
var toggleDevice;
$('#switchONOFF').change(function(){ 
	if($(this).prop('checked') == true){
		switchONOFF = 1; 		
	    toggleDevice = switchONOFF;
		d3.selectAll('.animatedImage').classed('tagdisable', false);
		floornetworkConfig.getDevices(toggleDevice)
	}
	else{
		switchONOFF = 0;	
		toggleDevice = switchONOFF;
		d3.selectAll('.animatedImage').classed('tagdisable', true);
		floornetworkConfig.getDevices(toggleDevice)
		
	} 	
});

var loading = 0 ;

var zoomLoaded = 0;

$(document).ready(function(){
  
	
	 
	$('body').on('change', ".tablelength", function (e) { 
	    	
	    	row_limit = $(this).val();
	    	var target = $(this).attr('data-target');
	    	$(target).attr('data-row-limit', row_limit);
	    	$(target).attr('data-current-page', '1');
	    	 
	        var show_previous_button = true;
	        var show_next_button = false;

	        var tableName = $(this).attr("data-target"); 
	        var $tableBlock = $(tableName); 
	        current_page = 1;
	        previous_page = 1
	        next_page = current_page + 1  
	        
	        if (previous_page == 1) {
	            show_previous_button = false;
	        }
	        if (steerBoard.activeClientsData.length > current_page * row_limit) {
	            show_next_button = true;
	        }
	        var filteredData = steerBoard.activeClientsData.slice((previous_page * row_limit) - row_limit, previous_page * row_limit);
	        var source = $("#chartbox-acl-template").html();
	        var template = Handlebars.compile(source);
	        var rendered = template({
	            "data": filteredData,
	            "current_page": previous_page,
	            "show_previous_button": show_previous_button,
	            "show_next_button": show_next_button,
	            "startIndex": (previous_page * row_limit) - row_limit
	        });
	        $('.acl-table-chart-box').html(rendered); 
	        $('#tablelength').val(row_limit);
	        
	    }); 
	    
	$('body').on('click', ".acl-tablePreviousPage", function (e) {

	    var show_previous_button = true;
	    var show_next_button = true;

	    var tableName = $(this).closest('span').attr("data-table-name");
	    var $tableBlock = $('#' + tableName);
	    var current_page = $tableBlock.attr('data-current-page');
	    current_page = parseInt(current_page);
	    previous_page = current_page - 1 
	    
	    if (previous_page == 1) {
	        show_previous_button = false;
	    }
	    var filteredData = steerBoard.activeClientsData.slice((previous_page * row_limit) - row_limit, previous_page * row_limit);
	    var source = $("#chartbox-acl-template").html();
	    var template = Handlebars.compile(source);
	    var rendered = template({
	        "data": filteredData,
	        "current_page": previous_page,
	        "show_previous_button": show_previous_button,
	        "show_next_button": show_next_button,
	        "startIndex": (previous_page * row_limit) - row_limit
	    });
	    $('.acl-table-chart-box').html(rendered); 
	    $('#tablelength').val(row_limit);
	});

	$('body').on('click', ".acl-tableNextPage", function (e) {

	    var show_previous_button = true;
	    var show_next_button = false;

	    var tableName = $(this).closest('span').attr("data-table-name");
	    var $tableBlock = $('#' + tableName);
	    var current_page = $tableBlock.attr('data-current-page');
	    current_page = parseInt(current_page);
	    next_page = current_page + 1 
	     
	    if (steerBoard.activeClientsData.length > next_page * row_limit) {
	        show_next_button = true;
	    }

	    var filteredData = steerBoard.activeClientsData.slice(row_limit * current_page, row_limit * next_page);
	    var source = $("#chartbox-acl-template").html();
	    var template = Handlebars.compile(source);
	    var rendered = template({
	        "data": filteredData,
	        "current_page": next_page,
	        "show_previous_button": show_previous_button,
	        "show_next_button": show_next_button,
	        "startIndex": row_limit * current_page
	    });
	    $('.acl-table-chart-box').html(rendered); 
	    $('#tablelength').val(row_limit);
	});


$('body').on('change', ".tablelengthone", function (e) { 
	    	
	    	row_limit = $(this).val();
	    	var target = $(this).attr('data-target');
	    	$(target).attr('data-row-limit', row_limit);
	    	$(target).attr('data-current-page', '1');
	    	 
	        var show_previous_button = true;
	        var show_next_button = false;

	        var tableName = $(this).attr("data-target"); 
	        var $tableBlock = $(tableName); 
	        current_page = 1;
	        previous_page = 1
	        next_page = current_page + 1  
	        
	        if (previous_page == 1) {
	            show_previous_button = false;
	        }
	        if (steerBoard.activeClientsSummary.length > current_page * row_limit) {
	            show_next_button = true;
	        }
	        var filteredData = steerBoard.activeClientsSummary.slice((previous_page * row_limit) - row_limit, previous_page * row_limit);
	        var source = $("#chartbox-active-template").html();
	        var template = Handlebars.compile(source);
	        var rendered = template({
	            "data": filteredData,
	            "current_page": previous_page,
	            "show_previous_button": show_previous_button,
	            "show_next_button": show_next_button,
	            "startIndex": (previous_page * row_limit) - row_limit
	        });
	        $('.table-chart-box').html(rendered); 
	        $('#tablelengthone').val(row_limit);
	        
	    }); 
	    
	$('body').on('click', ".steer-tablePreviousPage", function (e) {

	    var show_previous_button = true;
	    var show_next_button = true;

	    var tableName = $(this).closest('span').attr("data-table-name");
	    var $tableBlock = $('#' + tableName);
	    var current_page = $tableBlock.attr('data-current-page');
	    current_page = parseInt(current_page);
	    previous_page = current_page - 1 
	    
	    if (previous_page == 1) {
	        show_previous_button = false;
	    }
	    var filteredData = steerBoard.activeClientsSummary.slice((previous_page * row_limit) - row_limit, previous_page * row_limit);
	    var source = $("#chartbox-active-template").html();
	    var template = Handlebars.compile(source);
	    var rendered = template({
	        "data": filteredData,
	        "current_page": previous_page,
	        "show_previous_button": show_previous_button,
	        "show_next_button": show_next_button,
	        "startIndex": (previous_page * row_limit) - row_limit
	    });
	    $('.table-chart-box').html(rendered); 
	    $('#tablelengthone').val(row_limit);
	});

	$('body').on('click', ".steer-tableNextPage", function (e) {

	    var show_previous_button = true;
	    var show_next_button = false;

	    var tableName = $(this).closest('span').attr("data-table-name");
	    var $tableBlock = $('#' + tableName);
	    var current_page = $tableBlock.attr('data-current-page');
	    current_page = parseInt(current_page);
	    next_page = current_page + 1 
	     
	    if (steerBoard.activeClientsSummary.length > next_page * row_limit) {
	        show_next_button = true;
	    }

	    var filteredData = steerBoard.activeClientsSummary.slice(row_limit * current_page, row_limit * next_page);
	    var source = $("#chartbox-active-template").html();
	    var template = Handlebars.compile(source);
	    var rendered = template({
	        "data": filteredData,
	        "current_page": next_page,
	        "show_previous_button": show_previous_button,
	        "show_next_button": show_next_button,
	        "startIndex": row_limit * current_page
	    });
	    $('.table-chart-box').html(rendered); 
	    $('#tablelengthone').val(row_limit);
	});
	$('body').on('click', '.acl-refreshTable', function () {
	    steerBoard.tables.setTable.activeClientsTable();
	});
	
})

function loadactive(val){

	var url  = '/facesix/rest/client/cache/device_assoc_client?cid='+urlObj.cid;
	$.ajax({
		  	url:url,
		  	method:'GET',
		  	headers:{
		  		'content-type':'application/json'
		  	},
		  	success:function(response){  
          var cur_val;
          var j;
          if(val != null){
		  		$('.cur_drop').html('');
          $(".cur_drop").append($("<option/>").val('select').text('select'));
		  		var presentUids = [];
          $.each( response.clientConnected, function( key, value ) {
            console.log(">>>>>>" + value.mac_address)
  				     cur_val = value.mac_address;
               presentUids.push(cur_val);
  				     if(cur_val != val){
  				     	$(".cur_drop").append($("<option/>").val(cur_val).text(cur_val));
  				     }               
				});
            var selected = 'selected';  
            
            if(presentUids.includes(val)){
              console.log("val is present");
              var div_data = "<option value=" + val + " "+selected+"  >"+ val + "</option>";
            }else {
              console.log("val is not present");
            }
		  		  
             				  		
					  $(div_data).appendTo('.cur_drop');
		  		}	
        				 									
                     

		  	},
		  	error:function(error){
		  		console.log(error);
		  	}
	});

}

function intervalChange(){
	var dd = document.getElementById("tagrefresh");
	var time = dd.options[dd.selectedIndex].value;
	timeoutCount=time;	
}

function clientChange(){
	var dd = document.getElementById("tagrefresh");
	var time = dd.options[dd.selectedIndex].value;
	var mac = $('.cur_drop').val();
	var url = '/facesix/rest/site/portion/networkdevice/networkBalancer?cid='+urlObj.cid+'&macId='+mac+'&time='+time;

	   $.ajax({
	   	url:url,
	   	method:'GET',
	   		headers:{
		  		'content-type':'application/json'
		  	},
		  	success:function(response){  
		  		
		  	},
		  	error:function(error){
		  		console.log(error);
		  	}

	   });
}


	$(function() {
		var $select = $(".increment");
		for (i = 5; i <= 90; i += 5) {	
				if(i == 20){
					$select.append($('<option selected="selected"></option>').val(i).html(i + " " + "secs"))
				} else {
          $select.append($('<option></option>').val(i).html(i + " " + "secs"))
        }		
				
		}
	});

