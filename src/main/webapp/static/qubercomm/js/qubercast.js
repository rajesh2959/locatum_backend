//Network config tree
//Device Count Map
deviceData = {
    'server': 0,
    'switch': 0,
    'ap': 0,
    'sensor': 0,
    'total':0
}
//Device Prototype
function Device(type, status, uid, pid, child) {
    if (child) {
        var device = {
            type: type,
            status: status,
            uid: uid,
            parent: pid,
            child: []
        }
        device[type + "_id"] = deviceData[type];
        return device
    }
    this.devices = {
        type: type,
        status: status,
        uid: uid,
        parent: pid,
        child: []
    }
    this.devices[type + "_id"] = deviceData[type];
}
Device.prototype.buildTree = function(current, parent) {
    var type 	= current.type;
    var uid 	= current.uid;
    var status = current.status=="Added"?"Offline":current.status;
    var id=current[type+"_id"];
    if (!parent)
        networkTree.addNode(type, uid, status,'network',id)
    else
        networkTree.addNode(type, uid, status, parent,id);
}
Device.prototype.addChildren = function() {
    var current = this.devices;
    this.buildTree(current);
    var self = this;
    var recursiveDepthAdd = function(current) {
        var children = networkTree.childDevices;
        for (var i = 0; i < children.length; i++) {
            if (current.uid == children[i].parent)
                current.child.push(children[i]);
        }
        for (var j = 0; j < current.child.length; j++) {
            self.buildTree(current.child[j], current.type + "" + current[current.type + "_id"])
            recursiveDepthAdd(current.child[j])
        }
    }
    recursiveDepthAdd(current)

}
var networkTree = {
    'fetchurlParams':function(search){
		var urlObj={}
		if(search)
		  urlObj=JSON.parse('{"' + decodeURI(search).replace(/"/g, '\\"').replace(/&/g, '","').replace(/=/g,'":"') + '"}')
		this.urlObj=urlObj
		return urlObj; 
	},
    getJSON: function() {
        var self = this;
        var urlObj=this.fetchurlParams(window.location.search.substr(1));
        $.ajax({
            url: '/facesix/rest/site/portion/networkdevice/list?spid='+urlObj.spid,
            method: "get",
            success: function(repsonse) {
                var tree = repsonse;
                for (var i = 0; i < tree.length; i++) {
                    var type = tree[i].typefs;
                    var uid = tree[i].uid;
                    var status = tree[i].status;
                    deviceData['total']+=1;
                    if (type.indexOf("server") != -1) {
                        deviceData[type] += 1;
                        var device = new Device(type, status, uid);
                        self.deviceTree[type + "" + deviceData[type]] = device;
                    } else {
                        deviceData[type] += 1;
                        var device = Device(type, status, uid, tree[i].parent, true)
                        self.childDevices.push(device);
                    }
                }
                for(var key in self.deviceTree)
                    self.deviceTree[key].addChildren();
                var devices=deviceData['total']==1?"1 Device":deviceData['total']+" Devices";
                $(".device-section span").text(devices)
                addEvents();
                $("div[data-uid='"+urlObj.uid+"']").addClass("current");
            },
            error: function(error) {
                console.log(error)
            }
        })
    },
    deviceTree: {},
    childDevices: [],
    addNode: function(type,uid,status,parent,id) {
        var network = {}
         network['server'] = '<li class="deviceInfo" id="server-id-' + id +
                 '"><a class="dashbrdLink" href="#"><div data-type="Server" data-uid="'+uid+'" data-href="/facesix/web/site/portion/flrdash?spid='+this.urlObj.spid+'&type=server"  class="device-name"><label>' +
                 '<i class="fa fa-2x fa-angle-down" aria-hidden="true"></i>' +
                 '<img src="/facesix/static/qubercomm/images/networkconfig/icon/server_inactive.png" alt=""></label>' +
                 '<span>SVR-' + uid + '</span><label class="connected device-status pull-right">' +
                 '<span>' + status + '</span></label></div></a>' +
                 '<ul class="child list-unstyled" id="server' + id + '-tree"></ul></li>';

             network['switch'] = '<li  class="deviceInfo" id="switch-id-' + id +
                 '"><a class="dashbrdLink" href="#"><div data-type="Switch" data-uid="'+uid+'" data-href="/facesix/web/site/portion/swiboard?uid='+uid+'&type=switch&spid='+this.urlObj.spid+'" class="device-name"><label>' +
                 '<img src="/facesix/static/qubercomm/images/networkconfig/icon/switch_inactive.png" alt=""></label>' +
                 '<span>SW-' + uid + '</span><label class="connected device-status pull-right">' +
                 '<span>' + status + '</span></label></div></a>' +
                 '<ul class="list-unstyled childOfChild" id="switch' + id + '-tree"></ul></li>';

             network['ap'] = '<li  class="deviceInfo" id="ap-id-' + id + '"><a class="dashbrdLink" href="#"><div data-type="AP" data-uid="'+uid+'" data-href="/facesix/web/site/portion/devboard?uid='+uid+'&type=device&spid='+this.urlObj.spid+'" class="device-name"><label><img src="/facesix/static/qubercomm/images/networkconfig/icon/ap_inactive.png" alt="">' +
                 '</label><span>AP-' +uid + '</span><label class="connected device-status pull-right"><span>' + status + '</span>' +
                 '</label></div></a>' + '<ul class="list-unstyled childOfChild" id="ap' + id + '-tree"></ul></li>';

             network['sensor'] = '<li  class="deviceInfo" id="sensor-id-' + id + '"><a class="dashbrdLink" href="#"><div data-type="Sensor" data-uid="'+uid+'" data-href="/facesix/web/site/portion/devboard?uid='+uid+'&type=device&spid='+this.urlObj.spid+'" class="device-name"><label><img src="/facesix/static/qubercomm/images/networkconfig/icon/sensor_inactive.png" alt="">' +
                 '</label><span>SNR-' + uid + '</span><label class="connected device-status pull-right ">' +
                 '<span>' + status + '</span></label></div></a></li>';
 
             $('#' + parent + '-tree').append(network[type]);
    },
   
}
networkTree.getJSON();
function addEvents(){
   $(".device-name").on("click",highlight)
}
function highlight(evt){
        evt.preventDefault();
        $(".device-name").removeClass("current")
        $(".deviceInfo a").attr("href","#");
        $(this).addClass("current");
        $(".powerBtn").attr("uid",$(this).attr("data-uid"))
        var href=$(this).attr("data-href");
        $(".dshbrdLink").attr("href",href);
}

