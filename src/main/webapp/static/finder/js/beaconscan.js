(function () {
	search = window.location.search.substr(1)
	urlObj=JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
	var timer = 10000;
	var count = 1;
	var peerStats; 
	var row_limit = 10;
    DeviceACL = {
        timeoutCount: 0,
        acltables: {
            url: {
                aclClientsTable: '/facesix/rest/beacon/list/checkedin?cid='+urlObj.cid
            },
            setTable: {
                aclClientsTable: function () {
                    $.ajax({
                        url: DeviceACL.acltables.url.aclClientsTable,
                        method: "get",
                        success: function (result) {
                            //var result=result.scanned
                           // console.log("checkedin"+JSON.stringify(result));
                            if(result == "" || result == []){
                                $(".acl-table-chart-box").hide();
                            } else {
                                $(".acl-table-chart-box").show();
                            }
                            if (result && result.length) {
                                var show_previous_button = false;
                                var show_next_button = false;
                                _.each(result, function (i, key) {
                                    i.index = key + 1;
                                })
                                 
                                DeviceACL.activeClientsData = result;

                                var filteredData = result;
                                  
                                var source = $("#chartbox-acl-template").html();
                                var template = Handlebars.compile(source);
                                var rendered = template({
                                    "data": filteredData,
                                    "current_page": 1,
                                    "show_previous_button": show_previous_button,
                                    "show_next_button": show_next_button,
                                    "startIndex": 1
                                });
                                $('.acl-table-chart-box').html(rendered);

                                if(result.length >= 10){
                                    $(".acl-table-chart-box").css("height","auto");
                                    $(".acl-table-chart-box").css("overflow","hidden");
                                    $("#scanner-table tbody").css("height","350px");
                                }
                                                                
                            }
                            
                        },
                        error: function (data) {
                                                   
                        },
                        dataType: "json"

                    });
                }
            }
        },         
      
        beacontables: {
            url: {
            	beaconClientsTable: '/facesix/rest/beacon/checkedout?cid='+urlObj.cid+'&name='+urlObj.name+'&sid='+urlObj.sid
            },
            setTable: {
            	beaconClientsTable: function (reload) {
            		var dataurl = DeviceACL.beacontables.url.beaconClientsTable;
            		if(reload == 'reload')
            		{
            			 dataurl= '/facesix/rest/beacon/checkedout?cid='+urlObj.cid;
            		}
                    $.ajax({
                        url: dataurl,
                        method: "get",
                        success: function (result) {
                            result=result
                            if (result && result.length) {
                                var show_previous_button = false;
                                var show_next_button = false;
                                _.each(result, function (i, key) {
                                    i.index = key + 1;
                                })
                                DeviceACL.activeoneClientsData = result;
                                if (result.length > 10) {
                                    var filteredData = result.slice(0, 10);
                                    show_next_button = true;
                                } else {
                                    var filteredData = result;
                                }
                               // console.log(DeviceACL.activeoneClientsData);
                                $.each(DeviceACL.activeoneClientsData, function(index, optionValue) {  
	                           		 if(optionValue.debugflag === 'checked'){
	                           			 checkedValues.push(optionValue.macaddr);
	                           		 }
	                           	}); 
                                var source = $("#chartbox-beacon-template").html();
                                var template = Handlebars.compile(source);
                                var rendered = template({
                                    "data": filteredData,
                                    "current_page": 1,
                                    "show_previous_button": show_previous_button,
                                    "show_next_button": show_next_button,
                                    "startIndex": 1
                                });
                                $('.table-chart-box').html(rendered);
                                //$('table .aclPopup ').on("tap",aclMenu);                                
                                if(checkedValues.length == DeviceACL.activeoneClientsData.length){
                                	$('#CheckAll').prop('checked', true);
                                }
                            }
                            $(".loader_boxtwo").hide();
                            $('.tblhide').show();
                        },
                        error: function (data) {
                        	$(".loader_boxtwo").hide();
                        },
                        dataType: "json"

                    });
                }
            }
        },         
        
        init: function (params) {
            var aclList     = ['aclClientsTable']
            var tableList   = ['beaconClientsTable']
            var that        = this;
            $(".loader_boxtwo").show();
            $.each(aclList, function (key, val) {
                that.acltables.setTable[val]();
            });
           
            $.each(tableList, function (key, val) {
                that.beacontables.setTable[val]();
            });    
        },
    }
    
})();
var row_limit = 10;
$("body").on('click','.submitDelete',function(evt){
		evt.preventDefault();
		$(".savearea").show();
		$("#deleteItem").attr("data-target",$(this).attr("href"));
})

  $("body").on('click','#deleteItem',function(evt){
  	   var href=$(this).attr("data-target");
  	   var action=$(this).attr("action");
  	   var url=$(this).attr("action-url");
  	   
  	 console.log(action);
  	 
  	 console.log(url);
  	 
  	 
  	   	  $.ajax({
  	   	  	url:url,
  	   	  	method:'GET',
  	   	  	data:{},
  	   	  	headers:{
  	   	  		'content-type':'application/json'
  	   	  	},
  	   	  	success:function(response,error){
               //Called on successful response
  	   	  	},
  	   	  	error:function(error){
  	   	  		//console.log(error);	
  	   	  	}
  	   	  });
  	   
  })
  $("body").on("click",'#cancelDelete',function(evt){
  		$(".rebootPopup").hide();
  		$(".savearea").hide();
  })

 $("body").on('click','.selectBox',function(){
	var checkedoutbox = $("input[name='chkme']:checked").length;
	if(checkedoutbox == 0){
		$('#checkoutBtn').attr("disabled", "disabled");
		$('#checkoutBtn').css('pointer-events', "none");
		$('#deleteCheckedIn').attr("disabled", "disabled");
		$('#deleteCheckedIn').css('pointer-events', "none");
	} else {
		$('#checkoutBtn').removeAttr("disabled");
		$('#checkoutBtn').css('pointer-events', "auto");
		$('#deleteCheckedIn').removeAttr("disabled");
		$('#deleteCheckedIn').css('pointer-events', "auto");
	}
 });

$(document).ready(function(){
    DeviceACL.init();
$('body').on('click', "#tableSortChanged th:not(.tableFilter)", function (e) {
	checkCheckboxstatus(); updateCheckboxstatus();
}); 
$('body').on('change', ".tablelength", function (e) { 
	
	row_limit = $(this).val();
	var target = $(this).attr('data-target');
	$(target).attr('data-row-limit', row_limit);
	$(target).attr('data-current-page', '1');
	
	checkCheckboxstatus();
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
    if (DeviceACL.activeoneClientsData.length > current_page * row_limit) {
        show_next_button = true;
    }
    var filteredData = DeviceACL.activeoneClientsData.slice((previous_page * row_limit) - row_limit, previous_page * row_limit);
    
    var source = $("#chartbox-beacon-template").html();
    var template = Handlebars.compile(source);
    var rendered = template({
        "data": filteredData,
        "current_page": previous_page,
        "show_previous_button": show_previous_button,
        "show_next_button": show_next_button,
        "startIndex": (previous_page * row_limit) - row_limit
    });
    
    $('.table-chart-box').html(rendered);
    
    updateCheckboxstatus(); 
    $('#tablelength').val(row_limit);
    
}); 
$('body').on('click', ".acl-tablePreviousPage", function (e) {

	checkCheckboxstatus();
    var show_previous_button = true;
    var show_next_button = false;

    var tableName = $(this).closest('span').attr("data-table-name"); 
    var $tableBlock = $('#' + tableName);
    var current_page = $tableBlock.attr('data-current-page');
    current_page = parseInt(current_page);
    previous_page = current_page - 1;
    if (previous_page == 1) {
        show_previous_button = false;
    }
    if (DeviceACL.activeoneClientsData.length > previous_page * row_limit) {
        show_next_button = true;
    }
    var filteredData = DeviceACL.activeoneClientsData.slice((previous_page * row_limit) - row_limit, previous_page * row_limit);
    var source = $("#chartbox-beacon-template").html();
    var template = Handlebars.compile(source);
    var rendered = template({
        "data": filteredData,
        "current_page": previous_page,
        "show_previous_button": show_previous_button,
        "show_next_button": show_next_button,
        "startIndex": (previous_page * row_limit) - row_limit
    });
    
    $('.table-chart-box').html(rendered);
    
    updateCheckboxstatus(); 
    $('#tablelength').val(row_limit); 

});
var checkStatus = false; 
var selectStatus = false;
$('body').on('click', ".acl-tableNextPage", function (e) {
 
	checkCheckboxstatus();
	
    var show_previous_button = true;
    var show_next_button = false;

    var tableName = $(this).closest('span').attr("data-table-name");
    var $tableBlock = $('#' + tableName);
    var current_page = $tableBlock.attr('data-current-page');
    current_page = parseInt(current_page);
    next_page = current_page + 1
      

    if (DeviceACL.activeoneClientsData.length > next_page * row_limit) {
        show_next_button = true;
    }

    var filteredData = DeviceACL.activeoneClientsData.slice(row_limit * current_page, row_limit * next_page);
    var source = $("#chartbox-beacon-template").html();
    var template = Handlebars.compile(source);
    var rendered = template({
        "data": filteredData,
        "current_page": next_page,
        "show_previous_button": show_previous_button,
        "show_next_button": show_next_button,
        "startIndex": row_limit * current_page
    });
    $('.table-chart-box').html(rendered); 
    updateCheckboxstatus();
    $('#tablelength').val(row_limit); 
});  
function checkCheckboxstatus(){ 

	if ($('#CheckAll').prop('checked')) {
		checkStatus = true;
	} else {
		checkStatus = false;
	}

	if($('.checkoutGlobal').prop('checked')){
		selectStatus = true;
	} else {
		selectStatus = false;
	}
}

function checkforDeleteButton(){
	var checkedoutbox = selectedValues.length;
	if(checkedoutbox == 0){
		$('#deleteCheckout').attr("disabled", "disabled");
		$('#deleteCheckout').css('pointer-events', "none");
	} else {
		$('#deleteCheckout').removeAttr("disabled");
		$('#deleteCheckout').css('pointer-events', "auto");
	}
}
function updateCheckboxstatus(){
	
	var target = $('#CheckAll').attr('data-children');
	var name;
	$('.'+target).each(function(){
		name = $(this).attr('name'); 
		if(checkedValues.indexOf(name) > -1){
			
		}
		else{
			if(checkStatus === true){
				$(this).prop('checked', true);
			}
			else{
				$(this).prop('checked', false);
			}
		}
	});
    if(checkStatus === true){ 
    	$('#CheckAll').prop('checked', true);
    }else{ 
    	$('#CheckAll').prop('checked', false);
    }
    
	$.each(checkedValues, function(index, optionValue) {   
		$('.'+target+'[name="'+optionValue+'"]').prop('checked', true);
	}); 

	$('.checkoutLocal').each(function(){
		var lclId = this.id;
		if(selectedValues.indexOf(lclId) != -1){
			$(this).prop('checked', true);
		}else {
			selectStatus = false;
		}
	});

	var datalength = DeviceACL.activeoneClientsData.length;
	if(selectedValues.length == datalength){
		$('.checkoutGlobal').prop("checked",true);
	}else {
		$('.checkoutGlobal').prop("checked",false);
	}
	checkforDeleteButton();
}

$('body').on('click', ".checkoutLocal", function () {
	var ischecked = $(this).prop("checked");
	var index = selectedValues.indexOf(this.id);
	if(ischecked && index == -1){
		var datalength = DeviceACL.activeoneClientsData.length;
		selectedValues.push(this.id);
		if(selectedValues.length == datalength){
			$('.checkoutGlobal').prop("checked",true);
		}
	} else if(!ischecked){
		$('.checkoutGlobal').prop("checked",false);
		if(index != -1){
			selectedValues.splice(index,1);
		}
	}
	checkforDeleteButton();
});
//scanned

$('body').on('click', ".tablePreviousPage", function (e) {

    var show_previous_button = true;
    var show_next_button = true;

    var tableName = $(this).closest('span').attr("data-table-name");
    var $tableBlock = $('#' + tableName);
    var current_page = $tableBlock.attr('data-current-page');
    current_page = parseInt(current_page);
    previous_page = current_page - 1
    var row_limit = $tableBlock.attr('data-row-limit');
    row_limit = parseInt(row_limit);

    if (previous_page == 1) {
        show_previous_button = false;
    }
    var filteredData = DeviceACL.activeClientsData.slice((previous_page * row_limit) - row_limit, previous_page * row_limit);
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

});

$('body').on('click', ".tableNextPage", function (e) {

    var show_previous_button = true;
    var show_next_button = false;

    var tableName = $(this).closest('span').attr("data-table-name");
    var $tableBlock = $('#' + tableName);
    var current_page = $tableBlock.attr('data-current-page');
    current_page = parseInt(current_page);
    next_page = current_page + 1
    var row_limit = $tableBlock.attr('data-row-limit');
    row_limit = parseInt(row_limit);

    if (DeviceACL.activeClientsData.length > next_page * row_limit) {
        show_next_button = true;
    }

    var filteredData = DeviceACL.activeClientsData.slice(row_limit * current_page, row_limit * next_page);
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

});


$('body').on('click', '.refreshTable', function () {
    DeviceACL.acltables.setTable.aclClientsTable();
});
$('body').on('click', '.chk-refreshTable', function () {
	DeviceACL.beacontables.setTable.beaconClientsTable('reload');
});

$("body").on('click','.deleteAllCheck',function(evt){
	evt.preventDefault();
	$(".saveareadel").show();
});

$("body").on('click','#deleteCheckedIn',function(evt){
	evt.preventDefault();
	$(".delcheckedintags").show();
});

 $("body").on("click",'#cancelAllCheck',function(evt){
  		$(".rebootPopup").hide();
  		$(".saveareadel").hide();
  })
  
   $("body").on("click",'#cancelAllCheckin',function(evt){
  		$(".rebootPopup").hide();
  		$(".delcheckedintags").hide();
  })
  
  $("body").on("click",'#canceldelchecked',function(evt){
  		$(".rebootPopup").hide();
  		$(".saveareadelone").hide();
  		$("#triggersan").prop("disabled",false);
  })
  
$('#deleteAllCheckedout').on('click',function(){
	
	var cid  = location.search.split("&")[2].replace("?","").split("=")[1];
	var url  = "/facesix/rest/beacon/bulkCheckInTag"; 	
	$(".loader_boxone").show();
		$.ajax({
 	   	  	url:url,
 	   	  	method:'POST',
	 	   	contentType : "application/json",
	   	  	data: JSON.stringify(selectedValues),
 	   	  	success:function(response,error){
 	   	  	location.reload();
 	   		$(".loader_boxone").hide();
 	   		
 	   	  	},
 	   	  	error:function(error){
 	   	  		 console.log(error);
 	   	  	}
 });
 $(".saveareadel").hide();
});

$('#deleteCheckedin').on('click',function(){
	
	var cid  = location.search.split("&")[2].replace("?","").split("=")[1];
	var url  = "/facesix/rest/beacon/delete"; 	
	$(".loader_boxone").show();
	var macList = [];
    $.each($("input[name='chkme']:checked"), function(){  
		mac = $(this).val();
		macList.push(mac);
    });
	$.ajax({
   	  	url:url,
   	  	method:'POST',
   	  	contentType : "application/json",
   	  	data: JSON.stringify(macList),
   	  	success:function(response,error){
   	  	location.reload();
   		$(".loader_boxone").hide();
   		
   	  	},
   	  	error:function(error){
   	  		 console.log(error);
   	  	}
	});
	$(".delcheckedintags").hide();
});

$("#upload-file-selector").on('change', prepareLoad);
var files;
function prepareLoad(event) {
	console.log("tag importing...");
	var cid  = location.search.split("&")[2].replace("?","").split("=")[1];
	files = event.target.files;
	var oMyForm = new FormData();
	oMyForm.append("file", files[0]);
	var url = "/facesix/rest/beacon/tagimport?cid="+cid;
	$(".loader_boxtwo").show();
	var result= $.ajax({
		dataType : 'json',
		url : url,
		data : oMyForm,
		type : "POST",
		enctype : 'multipart/form-data',
		processData : false,
		contentType : false,
		success : function(result) {
			console.log("tagimport Success" + JSON.stringify(result));
		if (result.code=='200') {
			  location.reload();
		}
		 $("#importMessage").text(result.body);
		},
		error : function(result) {
			//console.log("tagimport Failed")
			$(".loader_boxtwo").hide();
		}
	});
}


})
 
