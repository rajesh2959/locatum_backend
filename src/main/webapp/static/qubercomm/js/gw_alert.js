(function () {
	search = window.location.search.substr(1)
	urlObj=JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
	var timer = 10000;
	var count = 1;
    DeviceACL = {
        timeoutCount: 0,     
        devtables: {
            url: {
            	devClientsTable: '/facesix/rest/site/portion/networkdevice/gw_alert?cid='+urlObj.cid
            },
            setTable: {
            	devClientsTable: function () {
                    $.ajax({
                        url: DeviceACL.devtables.url.devClientsTable,
                        method: "get",
                        success: function (result) {
                         var result=result.inactive_list;
                            console.log("devicealert"+JSON.stringify(result));
                            if (result && result.length) {
                                var show_previous_button = false;
                                var show_next_button = false;
                                _.each(result, function (i, key) {
                                    i.index = key + 1;
                                })
                                DeviceACL.serverClientsData = result;
                                if (result.length > 10) {
                                    var filteredData = result.slice(0, 10);
                                    show_next_button = true;
                                } else {
                                    var filteredData = result;
                                }

                                var b_source = $("#chartbox-dev-template").html();
                                var b_template = Handlebars.compile(b_source);
                                var b_rendered = b_template({
                                    "data": filteredData,
                                    "current_page": 1,
                                    "show_previous_button": show_previous_button,
                                    "show_next_button": show_next_button,
                                    "startIndex": 1
                                });
                                $('.dev-table-chart-box').html(b_rendered);
                                //$('table .aclPopup ').on("tap",aclMenu);                                
                                
                            }
                            
                            var hidecol = result[0].portionname;
                            if(hidecol == "NA"){
                            	$('.dumphey').remove();
                            }
                            
                            $(".loader_boxtwo").hide();
                            $(".alert-hide").show();
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
          
            var serverList    = ['devClientsTable']
            
            var that = this; 
            $(".loader_boxtwo").show();
                        
            $.each(serverList, function (key, val) {
                that.devtables.setTable[val]();
            });
                        
        },
    }
})();

function crashDump(fileName,filestatus){
 	console.log("fileName " + fileName + " filestatus  " +filestatus );
 	if(fileName != "NA" && filestatus == "0"){
 		reportlink = "/facesix/rest/beacon/ble/networkdevice/CrashDumpFileDownload?fileName="+fileName;	
     	window.open(reportlink);
 	}
 }



$(document).ready(function(){
	
	$(".alert-hide").hide();
    DeviceACL.init();
   

//Device
$('body').on('click', ".dev-tablePreviousPage", function (e) {

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
    var filteredData = DeviceACL.serverClientsData.slice((previous_page * row_limit) - row_limit, previous_page * row_limit);
    var source = $("#chartbox-dev-template").html();
    var template = Handlebars.compile(source);
    var b_rendered = template({
        "data": filteredData,
        "current_page": previous_page,
        "show_previous_button": show_previous_button,
        "show_next_button": show_next_button,
        "startIndex": (previous_page * row_limit) - row_limit
    });
    $('.dev-table-chart-box').html(b_rendered);

});

$('body').on('click', ".dev-tableNextPage", function (e) {

    var show_previous_button = true;
    var show_next_button = false;

    var tableName = $(this).closest('span').attr("data-table-name");
    var $tableBlock = $('#' + tableName);
    var current_page = $tableBlock.attr('data-current-page');
    current_page = parseInt(current_page);
    next_page = current_page + 1
    var row_limit = $tableBlock.attr('data-row-limit');
    row_limit = parseInt(row_limit);
    
    if (DeviceACL.serverClientsData.length > next_page * row_limit) {
        show_next_button = true;
    }

    var filteredData = DeviceACL.serverClientsData.slice(row_limit * current_page, row_limit * next_page);
    var source = $("#chartbox-dev-template").html();
    var template = Handlebars.compile(source);
    var b_rendered = template({
        "data": filteredData,
        "current_page": next_page,
        "show_previous_button": show_previous_button,
        "show_next_button": show_next_button,
        "startIndex": row_limit * current_page
    });
    $('.dev-table-chart-box').html(b_rendered);

});


$('body').on('click', '.dev-refreshTable', function () {
	DeviceACL.devtables.setTable.devClientsTable();
});


})


