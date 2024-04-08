var timeoutCount = 5;
var cur_tablen = 10;
var row_limit = 10;
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
        tables: {
            url: {
                activeClientsTable: '/facesix/rest/ui/pathSelection?uid='+urlObj.uid,
            },
            setTable: {
                activeClientsTable: function () {
                    $.ajax({
                        url: steerBoard.tables.url.activeClientsTable,
                        method: "get",
                        success: function (result) {                        	                                                                                              
                            //console.log (">>>>>>>>>>>>>>>>>>>table"+ JSON.stringify(result))  
                            $('.path').show(); 
                            if(result == ""){                                                         
                                  var result = [
                                              {
                                                "timeStamp": "-",
                                                 "nextHop": "-",
                                                 "score": "-",
                                                 "prevScore": "-",
                                                 "destination": "-",
                                                 "prevHop": "-",
                                                 "numHope" : "-",
                                                 "prevNumHope" : "-"
                                              }
                                          ]
                             }                   
                        	 if (result && result.length) {                    			
                    			//Default declarations
                            	var row_limit = 10;
                                var show_previous_button = false;
                                var show_next_button     = false;
                                
                                _.each(result, function (i, key) {
                                    i.index = key + 1;
                                })
                    			
                                steerBoard.activeClientsData = result;
                                
                                var source = $("#chartbox-acl-template").html();
                                var template = Handlebars.compile(source);
                                
                                //Getting the table name
                            	var tableName = $('.pageref').attr("data-table-name");
                            	
                            	/*
                            	 * If tablename is undefined, tableblock not yet initialised
                            	 * 
                            	 */
                                
                            	if(tableName == undefined) {
	                                if (result.length > 10) {
	                                    var filteredData = result.slice(0, 10);
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
                                    row_limit         = parseInt($('#tablelength').val());
                                   
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
                                    if (steerBoard.activeClientsData.length > current_page * row_limit) {
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
                            	$('.acl-table-chart-box').html(rendered);
                            	$('#tablelength').val(row_limit);
                              //  $('table .showPopup ').on("tap",rightMenu);
                                
                        }                  
                        	  						
                            setTimeout(function () {
                              steerBoard.tables.setTable.activeClientsTable();
                              openTable();
                           }, timeoutCount*1000);                            

                        },
                        error: function (data) {
                            setTimeout(function () {
                               steerBoard.tables.setTable.activeClientsTable();
                              openTable();
                           }, timeoutCount*1000);                            
                        },
                        dataType: "json"

                    });
                }
            }
        }, 
        charts: {
            urls: {
            },
            setChart: {},

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
function openTable(){
	
	var time = "5m";
	var url = '/facesix/rest/ui/pathSelectionHistogram?uid='+urlObj.uid;
	$.ajax({
 	  	url:url,
 	  	method:'GET',
 	  	dataType: "json",
 	  	headers:{
 	  		'content-type':'application/json'
 	  	},
 	  	success:function(response){
        $('.histogram').show();
 	 			//console.log("response" + JSON.stringify(response));
 	 			  var result=response;   
          if(result == ""){
           var result = [
    {
        "timeStamp": "-",
        "nextHop": "-",
        "score": "-",
        "prevScore": "-",
        "destination": "-",
        "prevHop": "-",
        "numHope" : "-",
    }
]
          } 	 			     	 			   	 			  
 	 			  if (result && result.length) {
						
					  //Default declarations
                  	  var row_limit = 10;
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
                          if (result.length > 10) {
                              var filteredData = result.slice(0, 10);
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
                     // $('table .showPopup ').on("tap",rightMenu);
                      
                  } 	 			         

 	  	},
 	  	error:function(error){
 	  		console.log(error);
 	  	}
	})

}

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



$("#root").on("change",function(){
  var cur_dev = $("#root").val();
  location.href = "/facesix/web/mesh/pathselection?uid="+cur_dev+"&cid="+urlObj.cid+"&sid="+urlObj.sid+"&spid="+urlObj.spid;
})
	
})

