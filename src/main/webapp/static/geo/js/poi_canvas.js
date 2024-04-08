var canvasWidth=1074,
    canvasHeight=698,
    canvasPadding=50,
    bg_canvas,
    zoom = 1,
    increment = 0.25,
    max_zoom = 2.5,
    min_zoom = 1,
    canvas_marker = '/facesix/static/geo/images/location_icon.png',
    fg_canvas,
    width = canvasWidth,
    height = canvasHeight,
    oWidth =0,
    oHeight=0;
var coordinates = [];
var marker_initial_left = 0, marker_initial_top = 0;


img.onload = function () {
};

$(document).on('click',".deleteBtn",function(){
    if(fg_canvas.getActiveObject())
    {
        fg_canvas.remove(fg_canvas.getActiveObject());
        $(".deleteBtn").remove();
    }
});

function addDeleteBtn(x, y){
    $(".deleteBtn").remove(); 
    var btnLeft = x-20;
    var btnTop = y-10;
    var deleteBtn = '<img src="/facesix/static/geo/images/delete_icon.png" class="deleteBtn" style="position:absolute;top:'+btnTop+'px;left:'+btnLeft+'px;cursor:pointer;width:18px;height:18px;"/>';
    $(".canvas-container").append(deleteBtn);
}

// canvas
window.onload = function() {

    bg_canvas = window._canvas = new fabric.Canvas('map_canvas');
    fg_canvas = window._canvas = new fabric.Canvas('map2_canvas');

    fg_canvas.on('mouse:down', function(options) {
        var object = fg_canvas.getActiveObject();
        if(object){
            console.log('object selected');
            marker_initial_left = object.left;
            marker_initial_top  = object.top;
            console.log("selected object current top  : "+marker_initial_top);
            console.log("selected object current left : "+marker_initial_left);
        } else {
           console.log(options.e.clientX, options.e.clientY);
           $(".deleteBtn").remove(); 
            createPoi(options);
        }        
    }); 

    fg_canvas.on('object:selected',function(e){
        addDeleteBtn(e.target.oCoords.tr.x, e.target.oCoords.tr.y);
    });

    fg_canvas.on('object:modified',function(e){
        addDeleteBtn(e.target.oCoords.tr.x, e.target.oCoords.tr.y);
    });

    fg_canvas.on('object:scaling',function(e){
        removeMarkerDeleteBtn();
    });
    
    fg_canvas.on('object:moving',function(e){
        var obj ;
        removeMarkerDeleteBtn();
        console.log('object is moving...');
        console.log(fg_canvas.getActiveObject().left);
        console.log(fg_canvas.getActiveObject().top);
        
        try{
            obj = fg_canvas.getActiveObject();
        }catch(err){    
        }
        // get the relative pixel location of the placed object (exclude the paddings)
        if(obj != null){
            var newLeft = getScaledWidth((obj.left + (obj.width /2)) - 50);
            var newTop  = getScaledHeight((obj.top + obj.height) - 50);

            if(!validateX(newLeft)){          	
                obj.left = marker_initial_left;
                obj.top  = marker_initial_top;
                obj.setCoords();
                fg_canvas.discardActiveObject();
                fg_canvas.renderAll();
                alert('Object position (left) is out of bounds !');
                return;
            }

            if(!validateX(newTop)){           	
                obj.left = marker_initial_left;
                obj.top  = marker_initial_top;
                obj.setCoords();
                fg_canvas.discardActiveObject();
                fg_canvas.renderAll();
                alert('Object position (top) is out of bounds !');
                return;
            }
            
            obj.org_x = newLeft;
            obj.org_y = newTop;
        }        
        
    });
    
    // zoom-in
    $('#zoomin').click(function () {
        console.log('zoom-in');
        removeMarkerDeleteBtn();

        if(zoom >= 2.5) {
            console.log('Reached the maximum size');
            return;
        }

        zoom = zoom + increment;        
        console.log(zoom);
        applyZoom(zoom);
    });

    // zoom-out
    $('#zoomout').click(function () {
        console.log('zoom-out')     
        removeMarkerDeleteBtn();
        if(zoom == 1){
            console.log('reached minimum level');
            return;
        }
        zoom = zoom - increment;   
        console.log(zoom);
        applyZoom(zoom);
    });
    
    // set initial size
    bg_canvas.setWidth(canvasWidth);
    bg_canvas.setHeight(canvasHeight);
    fg_canvas.setWidth(canvasWidth);
    fg_canvas.setHeight(canvasHeight);  

    setImageOnBackgroundCanvas();
    // Get the canvas data
    loadFromJson();
    
};

function removeMarkerDeleteBtn(){
    $(".deleteBtn").remove();
}

function applyZoom(zoom){
    // remove the background image
    bg_canvas.setZoom(zoom);
    fg_canvas.setZoom(zoom);
    bg_canvas.setWidth(canvasWidth * zoom).setHeight(canvasHeight * zoom);
    fg_canvas.setWidth(canvasWidth * zoom).setHeight(canvasHeight * zoom); 
}

function setCanvasSize(canvasSizeObject) {
    bg_canvas.setWidth(canvasSizeObject.width);
    bg_canvas.setHeight(canvasSizeObject.height);
    fg_canvas.setWidth(canvasSizeObject.width);
    fg_canvas.setHeight(canvasSizeObject.height);    
}

function setImageOnBackgroundCanvas() {
    
    var imgWidth  = bg_canvas.width - 100;
    var imgHeight = bg_canvas.height - 100;
    
    oWidth   = img.width;
    oHeight  = img.height;

    console.log('original width: '+oWidth+ ', original height: '+oHeight);
    
    width    = imgWidth;
    height   = imgHeight;
    console.log('rendered width: '+width+ ', rendered height: '+height);
    
    var bg_image = new fabric.Image(img);
    bg_image.set({ left: 50, top: 50, height: imgHeight, width: imgWidth, 
            id: 'bgimage', angle: 0,hasRotatingPoint :false,lockScalingX: true,
        lockScalingY: true,
        lockRotation: true,
        lockMovementX: true,
        lockMovementY: true,
        hasBorders: false,
        hasControls: false,
        hasRotatingPoint: false});
    
    bg_canvas.backgroundColor='rgb(224,234,241)';
    bg_canvas.add(bg_image);
    bg_canvas.renderAll();    
}

var oX = 0, oY = 0,posX=0,posY=0;
function createPoi(event){	
    var pointer = fg_canvas.getPointer(event.e);
    posX = pointer.x;
    posY = pointer.y;
    console.log(posX+", "+posY);

    oX = getScaledWidth(posX - 50);
    oY = getScaledHeight(posY - 50);

    if(!validateX(oX)) return;
    if(!validateY(oY)) return;	
    $('#poiModal').modal('show');
}

// plot poi 
function plot(data){   
    var circle = new fabric.Circle({
        radius: 15, fill: 'orange', left: posX - 15, top: posY - 15, 
        hasRotatingPoint :false,lockScalingX: true,
        lockScalingY: true,
        lockRotation: true,
        lockMovementX: false,
        lockMovementY: false,
        hasBorders: false,
        hasControls: true,
        hasRotatingPoint: false,
        spid: spid, // custom
        poi_id: data, // custom
        org_x: oX, // custom
        org_y: oY // custom
    });
        
    fg_canvas.add(circle);
}

// custom method to remove an element from the canvas 
function deleteElementById(c, customId){
    for (var i = 0; i < c.getObjects().length; ++i) 
    { 
        if (c.item(i).id == customId) {
            c.item(i).remove(); 
            break;
        }
    } 
}


function resetCanvas(){
    // remove delete button 
    removeMarkerDeleteBtn();
    bg_json = '';
    fg_json = '';
    coordinates = [];
    // clean-up all the markers over the canvas
    fg_canvas.clear();
    
}

function exportFgCanvasAsJson(){
    return fg_canvas.toJSON(['spid','poi_id','org_x','org_y','lockScalingX','lockScalingY','lockRotation','hasBorders','hasControls','hasRotatingPoint','lockMovementX','lockMovementY','selectable']);
}

function reloadCanvas(){
    bg_canvas.loadFromJSON(bg_json, function() {
        bg_canvas.renderAll(); 
        },function(o,object){
        console.log(o,object)
    });

    fg_canvas.loadFromJSON(fg_json, function() {
        fg_canvas.renderAll(); 
        },function(o,object){
        console.log(o,object)
    });    
}

// Get cached poi canvas data
function loadFromJson(){
	var canvas_url = '/facesix/api/geo/poi/'+spid;
	$.ajax({
	    url:canvas_url+'/canvas',
	    type:'GET',                                          
	    success: function(data){
	    	console.log('Received poi canvas data');
	    	console.log(data);	    		    	
    		var jsonObj = JSON.parse(JSON.stringify(data));
    		if(jsonObj.fg_json != ''){
    			var s = JSON.parse(JSON.stringify(jsonObj.fg_json));	        	
    			fg_canvas.loadFromJSON(s,fg_canvas.renderAll.bind(fg_canvas));
    		}  
	    }
	 });
}

function getScaledWidth(x){
    var x1 = 0, deltaWidth  = 0;
    var widthScaleUp = false;

     if(width > oWidth){
        widthScaleUp = true;
        deltaWidth = width / oWidth;
    } else if(width < oWidth){
        widthScaleUp = false;
        deltaWidth = oWidth / width;
    }

    if(widthScaleUp){
        console.log('width scaled up');
    } else {
        console.log('width scaled down');
    }

    if(widthScaleUp){
        x1 = Math.round(x / deltaWidth);
    } else {
        x1 = Math.round(x * deltaWidth);
    }
    console.log('x : '+x+' , x1 : '+x1);
    return x1;
}

// To get the exact 
function getScaledHeight(y){
    var y1 = 0, deltaHeight = 0;
    var heightScaleUp = false;    
    // calculate the scaling factor for height
    if(height > oHeight){
        heightScaleUp = true;
        deltaHeight = height / oHeight;
    } else if(height < oHeight){
        heightScaleUp = false;
        deltaHeight = oHeight / height;
    }
    if(heightScaleUp){
        y1 = Math.round(y / deltaHeight);
    } else {
        y1 = Math.round(y * deltaHeight);
    }
    console.log('y : '+y+' , y1 : '+y1);
    return y1;
}

// checks whether x selected point is out of bind
function validateX(var_x){
    if(var_x < 0){
        console.log('width (negative index) out of boundary!');
        return false;
    }     
    if(var_x > oWidth){
        console.log('width is out of bounds!');
        return false;
    }    
    return true;
}

function validateY(var_y){
    if(var_y < 0){
        console.log('height (negative index) out of boundary!');
        return false;
    }     
    if(var_y > oHeight){
        console.log('height is out of bounds!');
        return false;
    }    
    return true;
}

function createPoiRecord(){
	$('#poiModal').modal('hide');
	
	console.log('triggered submit method');
	console.log('oX'+oX+', oY'+oY);
	var api_url = '/facesix/api/geo/poi/save';
    // Get form
    var form = $('#poi_form')[0];
	// Create an FormData object
    var data = new FormData(form);

	// If you want to add an extra field for the FormData
    data.append("spid", spid);
    data.append("id", 'letstry');

    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url:api_url,
        data: data,
        processData: false,
        contentType: false,
        cache: false,
        timeout: 600000,
        success: function (data) {
        	console.log('SUCCESS : '+data);
        	plot(data);
        },
        error: function (e) {
        	console.log("ERROR : ", e);
        }
    });
}

