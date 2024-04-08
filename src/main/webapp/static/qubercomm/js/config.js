var anival = '0';
var rootval = "no";
var lanbridge = "lan";
var wanbridge = "wan";
var onloadval;
var cur_channel;
var getconfig;
var cur_ind_val = 0;
var tot_size = 0;
var ip = location.hostname;
var url = 'http://' + ip + ':15000/';

var switchStatus = '';
var cur_uid;
var cur_param;
var cur_sid;
var cur_spid;
var cur_cid;
var val2ch;
var val5ch;
var cur_mode_val;
var cur_ap_val;
var cmv = 0;
var print = 0;
var elprint = 0;
var elval = "";
var inp = 0;
var add_button = $(".add_field_button");
var uni_key = 0;
var amm = 0;
var cur_color = 0;

var th_mode = 0;
var cr_mode = [];
var decider = 0;
var gk = 0;
var tabcnt = 0;
var radio2g = 0;
var interfaces2g = 0;
var radio5g = 0;
var interfaces5g = 0;
var interface_mode = "";
var txPwrError = 0;
var radio = 0;
var interfaces = 0;
var index_val = 0;
var index_name = "";
var tf_index_val = 0;
var tf_index_name = "";
var di_size = 0;
var tf_di_size = 0;
var two_index_val = 0;
var two_index_name = "";
var two_di_size = 0;
var inf2g = 0;
var inf2gone = 0;
var inf2gtwo = 0;
var inf5g = 0;
var inf5gone = 0;
var inf5gtwo = 0;
var inf2g5g = 0;
var inf2g5gone = 0;
var inf2g5gtwo = 0;

var modes = new Array(6);
modes["11b"] = new Array(5);
modes["11b"] = [ 1, 2, 5.5, 11 ];
modes["11bg"] = new Array(12);
modes["11bg"] = [ 1, 2, 5.5, 6, 9, 11, 12, 18, 24, 36, 48, 54 ];
modes["11ng"] = new Array(40);
modes["11ng"] = [ 1, 2, 5.5, 6, 6.5, 7.2, 9, 11, 12, 13, 14.4, 18, 19.5, 21.7,
		24, 26, 28.9, 36, 39, 43.3, 48, 52, 57.8, 58.5, 65, 72.2, 78, 86.7,
		104, 115.6, 117, 130.3, 144.4, 156, 173.3, 175.5, 195, 216.7 ];
modes["11a"] = new Array(8);
modes["11a"] = [ 6, 9, 12, 18, 24, 36, 48, 54 ];
modes["11na"] = new Array(58);
modes["11na"] = [ 6, 6.5, 7.2, 9, 12, 13, 13.5, 14.4, 15, 18, 19.5, 21.7, 24,
		26, 27, 28.9, 30, 36, 39, 40.5, 43.3, 45, 48, 52, 54, 57.8, 58.5, 60,
		65, 72.2, 78, 81, 86.7, 90, 104, 108, 115.6, 117, 120, 121.5, 130,
		130.3, 135, 144.4, 150, 156, 162, 173.3, 175.5, 180, 195, 216, 216.7,
		240, 243, 270, 300 ];
modes["11ac"] = new Array(100);
modes["11ac"] = [ 6, 6.5, 7.2, 9, 12, 13, 13.5, 14.4, 15, 18, 19.5, 21.7, 24,
		26, 27, 28.9, 29.3, 30, 32.5, 36, 39, 40.5, 43.3, 45, 48, 52, 54, 57.8,
		58.5, 60, 65, 72.2, 78, 81, 86.7, 87.8, 90, 97.5, 104, 108, 115.6, 117,
		120, 121.5, 130, 130.3, 135, 144.4, 150, 156, 162, 173.3, 175.5, 180,
		195, 200, 216, 216.7, 234, 240, 243, 260, 263.3, 270, 292.5, 300, 324,
		325, 351, 360, 390, 433.3, 468, 520, 526.5, 540, 585, 600, 650, 702,
		780, 866.7, 936, 1040, 1053, 1170, 1300, 1404, 1560, 1579.5, 1733.3,
		1755, 1950, 2106, 2340 ];

var req2g = new Array(50);

req2g["AT"] = new Array(11);
req2g["AT"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["AU"] = new Array(11);
req2g["AU"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["BE"] = new Array(13);
req2g["BE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];
req2g["BR"] = new Array(11);
req2g["BR"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["CA"] = new Array(11);
req2g["CA"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["CH"] = new Array(11);
req2g["CH"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["CN"] = new Array(13);
req2g["CN"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];
req2g["CY"] = new Array(11);
req2g["CY"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["CZ"] = new Array(11);
req2g["CZ"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["DE"] = new Array(11);
req2g["DE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["DK"] = new Array(11);
req2g["DK"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["EE"] = new Array(11);
req2g["EE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["ES"] = new Array(11);
req2g["ES"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["FI"] = new Array(11);
req2g["FI"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["FR"] = new Array(11);
req2g["FR"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["GB"] = new Array(11);
req2g["GB"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["GR"] = new Array(11);
req2g["GR"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["HK"] = new Array(11);
req2g["HK"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["HU"] = new Array(11);
req2g["HU"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["ID"] = new Array(13);
req2g["ID"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];
req2g["IE"] = new Array(11);
req2g["IE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["IL"] = new Array(11);
req2g["IL"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];
req2g["ILO"] = new Array(9);
req2g["ILO"] = [ 5, 6, 7, 8, 9, 10, 11, 12, 13 ];
req2g["IN"] = new Array(11);
req2g["IN"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["IS"] = new Array(11);
req2g["IS"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["IT"] = new Array(11);
req2g["IT"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["J1"] = new Array(14);
req2g["J1"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 ];
req2g["JP"] = new Array(14);
req2g["JP"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 ];
req2g["KE"] = new Array(13);
req2g["KE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];
req2g["KR"] = new Array(13);
req2g["KR"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];
req2g["LT"] = new Array(11);
req2g["LT"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["LU"] = new Array(11);
req2g["LU"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["LV"] = new Array(11);
req2g["LV"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["MY"] = new Array(13);
req2g["MY"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];
req2g["NL"] = new Array(11);
req2g["NL"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["NO"] = new Array(11);
req2g["NO"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["NZ"] = new Array(11);
req2g["NZ"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["PH"] = new Array(11);
req2g["PH"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["PL"] = new Array(11);
req2g["PL"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["PT"] = new Array(11);
req2g["PT"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["SE"] = new Array(11);
req2g["SE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["SG"] = new Array(13);
req2g["SG"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];
req2g["SI"] = new Array(11);
req2g["SI"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["SK"] = new Array(11);
req2g["SK"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["TH"] = new Array(13);
req2g["TH"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];
req2g["TW"] = new Array(13);
req2g["TW"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];
req2g["US"] = new Array(11);
req2g["US"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["USE"] = new Array(11);
req2g["USE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["USL"] = new Array(11);
req2g["USL"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];
req2g["ZA"] = new Array(13);
req2g["ZA"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];

var req5g = new Array(44);
req5g["AT"] = new Array(4);
req5g["AT"] = [ 36, 40, 44, 48 ];
req5g["AU"] = new Array(12);
req5g["AU"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["BE"] = new Array(8);
req5g["BE"] = [ 36, 40, 44, 48, 52, 56, 60, 64 ];
req5g["BR"] = new Array(12);
req5g["BR"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["CA"] = new Array(12);
req5g["CA"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["CH"] = new Array(8);
req5g["CH"] = [ 36, 40, 44, 48, 52, 56, 60, 64 ];
req5g["CN"] = new Array(4);
req5g["CN"] = [ 149, 153, 157, 161 ];
req5g["CY"] = new Array(12);
req5g["CY"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["CZ"] = new Array(12);
req5g["CZ"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["DE"] = new Array(17);
req5g["DE"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req5g["DK"] = new Array(17);
req5g["DK"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req5g["EE"] = new Array(12);
req5g["EE"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["ES"] = new Array(17);
req5g["ES"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req5g["FI"] = new Array(17);
req5g["FI"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req5g["FR"] = new Array(8);
req5g["FR"] = [ 32, 36, 40, 44, 48 ];
req5g["GB"] = new Array(17);
req5g["GB"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
//req5g["GR"]=new Array(11);     req5g["GR"] = [36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161];

req5g["HK"] = new Array(12);
req5g["HK"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["HU"] = new Array(8);
req5g["HU"] = [ 36, 40, 44, 48, 52, 56, 60, 64 ];
//req5g["ID"]=new Array(13);     req5g["ID"] = [1,2,3,4,5,6,7,8,9,10,11,12,13];

req5g["IE"] = new Array(8);
req5g["IE"] = [ 36, 40, 44, 48, 52, 56, 60, 64 ];
req5g["IL"] = new Array(8);
req5g["IL"] = [ 36, 40, 44, 48, 52, 56, 60, 64 ];
req5g["ILO"] = new Array(8);
req5g["ILO"] = [ 36, 40, 44, 48, 52, 56, 60, 64 ];
//req5g["IN"]=new Array(11);     req5g["IN"] = [1,2,3,4,5,6,7,8,9,10,11];

req5g["IS"] = new Array(17);
req5g["IS"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req5g["IT"] = new Array(17);
req5g["IT"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req5g["J1"] = new Array(8);
req5g["J1"] = [ 36, 40, 44, 48, 52, 56, 60, 64 ];
req5g["JP"] = new Array(4);
req5g["JP"] = [ 34, 38, 42, 46 ];
req5g["KE"] = new Array(18);
req5g["KE"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		149, 153, 157, 161 ];
req5g["KR"] = new Array(4);
req5g["KR"] = [ 149, 153, 157, 161 ];
req5g["LT"] = new Array(12);
req5g["LT"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["LU"] = new Array(17);
req5g["LU"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req5g["LV"] = new Array(12);
req5g["LV"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
//req2g["MY"]=new Array(13);     req5g["MY"] = [1,2,3,4,5,6,7,8,9,10,11,12,13];

req5g["NL"] = new Array(17);
req5g["NL"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req5g["NO"] = new Array(17);
req5g["NO"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req5g["NZ"] = new Array(12);
req5g["NZ"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["PH"] = new Array(12);
req5g["PH"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["PL"] = new Array(12);
req5g["PL"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["PT"] = new Array(17);
req5g["PT"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req5g["SE"] = new Array(17);
req5g["SE"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req5g["SG"] = new Array(12);
req5g["SG"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["SI"] = new Array(12);
req5g["SI"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["SK"] = new Array(12);
req5g["SK"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
//req5g["TH"]=new Array(13);     req5g["TH"] = [1,2,3,4,5,6,7,8,9,10,11,12,13];

req5g["TW"] = new Array(17);
req5g["TW"] = [ 56, 60, 64, 100, 104, 108, 112, 116, 120, 124, 128, 132, 140,
		149, 153, 157, 161 ];
req5g["US"] = new Array(12);
req5g["US"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 149, 153, 157, 161 ];
req5g["USE"] = new Array(8);
req5g["USE"] = [ 36, 40, 44, 48, 52, 56, 60, 64 ];
req5g["USL"] = new Array(8);
req5g["USL"] = [ 36, 40, 44, 48, 52, 56, 60, 64 ];
//req5g["ZA"]=new Array(13);     req5g["ZA"] = [1,2,3,4,5,6,7,8,9,10,11,12,13];

/*template_2G5G*/

var req2g5g = new Array(50);
req2g5g["AT"] = new Array(4);
req2g5g["AT"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48 ];
req2g5g["AU"] = new Array(12);
req2g5g["AU"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["BE"] = new Array(8);
req2g5g["BE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 36, 40, 44, 48,
		52, 56, 60, 64 ];
req2g5g["BR"] = new Array(12);
req2g5g["BR"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["CA"] = new Array(12);
req2g5g["CA"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["CH"] = new Array(8);
req2g5g["CH"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64 ];
req2g5g["CN"] = new Array(4);
req2g5g["CN"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 149, 153, 157, 161 ];
req2g5g["CY"] = new Array(12);
req2g5g["CY"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["CZ"] = new Array(12);
req2g5g["CZ"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["DE"] = new Array(17);
req2g5g["DE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 104, 108, 112, 116, 120, 124, 128, 132, 140 ];
req2g5g["DK"] = new Array(17);
req2g5g["DK"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 104, 108, 112, 116, 120, 124, 128, 132, 140 ];
req2g5g["EE"] = new Array(12);
req2g5g["EE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["ES"] = new Array(17);
req2g5g["ES"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 104, 108, 112, 116, 120, 124, 128, 132, 140 ];
req2g5g["FI"] = new Array(17);
req2g5g["FI"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 104, 108, 112, 116, 120, 124, 128, 132, 140 ];
req2g5g["FR"] = new Array(8);
req2g5g["FR"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 32, 36, 40, 44, 48 ];
req2g5g["GB"] = new Array(17);
req2g5g["GB"] = [ 36, 40, 44, 48, 52, 56, 60, 64, 104, 108, 112, 116, 120, 124,
		128, 132, 140 ];
req2g5g["GR"] = new Array(11);
req2g5g["GR"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];

req2g5g["HK"] = new Array(12);
req2g5g["HK"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["HU"] = new Array(8);
req2g5g["HU"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64 ];
req2g5g["ID"] = new Array(13);
req2g5g["ID"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];

req2g5g["IE"] = new Array(8);
req2g5g["IE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64 ];
req2g5g["IL"] = new Array(8);
req2g5g["IL"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 36, 40, 44, 48,
		52, 56, 60, 64 ];
req2g5g["ILO"] = new Array(8);
req2g5g["ILO"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64 ];
req2g5g["IN"] = new Array(11);
req2g5g["IN"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 ];

req2g5g["IS"] = new Array(17);
req2g5g["IS"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 104, 108, 112, 116, 120, 124, 128, 132, 140 ];
req2g5g["IT"] = new Array(17);
req2g5g["IT"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 104, 108, 112, 116, 120, 124, 128, 132, 140 ];
req2g5g["J1"] = new Array(8);
req2g5g["J1"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 36, 40, 44,
		48, 52, 56, 60, 64 ];
req2g5g["JP"] = new Array(4);
req2g5g["JP"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 34, 38, 42, 46 ];
req2g5g["KE"] = new Array(18);
req2g5g["KE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 36, 40, 44, 48,
		52, 56, 60, 64, 104, 108, 112, 116, 120, 124, 149, 153, 157, 161 ];
req2g5g["KR"] = new Array(4);
req2g5g["KR"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 149, 153, 157, 161 ];
req2g5g["LT"] = new Array(12);
req2g5g["LT"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["LU"] = new Array(17);
req2g5g["LU"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 104, 108, 112, 116, 120, 124, 128, 132, 140 ];
req2g5g["LV"] = new Array(12);
req2g5g["LV"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g["MY"] = new Array(13);
req2g5g["MY"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];

req2g5g["NL"] = new Array(17);
req2g5g["NL"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 104, 108, 112, 116, 120, 124, 128, 132, 140 ];
req2g5g["NO"] = new Array(17);
req2g5g["NO"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 104, 108, 112, 116, 120, 124, 128, 132, 140 ];
req2g5g["NZ"] = new Array(12);
req2g5g["NZ"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["PH"] = new Array(12);
req2g5g["PH"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["PL"] = new Array(12);
req2g5g["PL"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["PT"] = new Array(17);
req2g5g["PT"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 104, 108, 112, 116, 120, 124, 128, 132, 140 ];
req2g5g["SE"] = new Array(17);
req2g5g["SE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 104, 108, 112, 116, 120, 124, 128, 132, 140 ];
req2g5g["SG"] = new Array(12);
req2g5g["SG"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 36, 40, 44, 48,
		52, 56, 60, 64, 149, 153, 157, 161 ];
req2g5g["SI"] = new Array(12);
req2g5g["SI"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["SK"] = new Array(12);
req2g5g["SK"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["TH"] = new Array(13);
req2g5g["TH"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];

req2g5g["TW"] = new Array(17);
req2g5g["TW"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 56, 60, 64, 100,
		104, 108, 112, 116, 120, 124, 128, 132, 140, 149, 153, 157, 161 ];
req2g5g["US"] = new Array(12);
req2g5g["US"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64, 149, 153, 157, 161 ];
req2g5g["USE"] = new Array(8);
req2g5g["USE"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64 ];
req2g5g["USL"] = new Array(8);
req2g5g["USL"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 36, 40, 44, 48, 52, 56,
		60, 64 ];
req2g5g["ZA"] = new Array(13);
req2g5g["ZA"] = [ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13 ];

var myar = Array();

myar["AT"] = 20;
myar["AU"] = 23;
myar["BE"] = 20;
myar["BR"] = 30;
myar["CA"] = 36;
myar["CH"] = 20;
myar["CN"] = 27;
myar["CY"] = 30;
myar["CZ"] = 23;
myar["DE"] = 20;
myar["DK"] = 20;
myar["EE"] = 30;
myar["ES"] = 20;
myar["FI"] = 20;
myar["FR"] = 20;
myar["GB"] = 20;
myar["GR"] = 20;
myar["HK"] = 20;
myar["HU"] = 30;
myar["ID"] = 20;
myar["IE"] = 20;
myar["IL"] = 20;
myar["ILO"] = 20;
myar["IN"] = 36;
myar["IS"] = 20;
myar["IT"] = 20;
myar["J1"] = 23;
myar["JP"] = 23;
myar["KE"] = 27;
myar["KR"] = 27;
myar["LT"] = 30;
myar["LU"] = 20;
myar["LV"] = 20;
myar["MY"] = 20;
myar["NL"] = 20;
myar["NO"] = 20;
myar["NZ"] = 30;
myar["PH"] = 30;
myar["PL"] = 20;
myar["PT"] = 20;
myar["SE"] = 20;
myar["SG"] = 23;
myar["SI"] = 30;
myar["SK"] = 30;
myar["TH"] = 20;
myar["TW"] = 30;
myar["US"] = 36;
myar["USE"] = 30;
myar["USL"] = 30;
myar["ZA"] = 30;

// 5G
var myar5 = Array();
myar5["AT"] = 20;
myar5["AU"] = 23;
myar5["BE"] = 20;
myar5["BR"] = 30;
myar5["CA"] = 36;
myar5["CH"] = 20;
myar5["CN"] = 27;
myar5["CY"] = 30;
myar5["CZ"] = 23;
myar5["DE"] = 20;
myar5["DK"] = 20;
myar5["EE"] = 30;
myar5["ES"] = 20;
myar5["FI"] = 20;
myar5["FR"] = 20;
myar5["GB"] = 20;
myar5["GR"] = 20;
myar5["HK"] = 20;
myar5["HU"] = 30;
myar5["ID"] = 20;
myar5["IE"] = 20;
myar5["IL"] = 20;
myar5["ILO"] = 20;
myar5["IN"] = 36;
myar5["IS"] = 20;
myar5["IT"] = 20;
myar5["J1"] = 23;
myar5["JP"] = 23;
myar5["KE"] = 27;
myar5["KR"] = 27;
myar5["LT"] = 30;
myar5["LU"] = 20;
myar5["LV"] = 20;
myar5["MY"] = 20;
myar5["NL"] = 20;
myar5["NO"] = 20;
myar5["NZ"] = 30;
myar5["PH"] = 30;
myar5["PL"] = 20;
myar5["PT"] = 20;
myar5["SE"] = 20;
myar5["SG"] = 23;
myar5["SI"] = 30;
myar5["SK"] = 30;
myar5["TH"] = 20;
myar5["TW"] = 30;
myar5["US"] = 36;
myar5["USE"] = 30;
myar5["USL"] = 30;
myar5["ZA"] = 30;

function makempty() {
	radio = 0;
	interfaces = 0;
	radio5g = 0;
	interfaces5g = 0;
	$('#div2gr,#div2gi,#div5gr,#div5gi').html('');
}

function prefilldata(srvrdta) {
	makempty();
	
	var inpid = "";
	var ctryval = "";
	var txpwr = "";
	$('.aclGrp').hide();
    


	var srvrlen = srvrdta.length;
	var tag = 0;
	var twogi = 0;
	var fivegi = 0;
	var twofivegi = 0;
	
	
	
	
	if (srvrlen < 30) {
	 
		const svr_temp = srvrdta.slice(1, -1).split(',');

		$('.display-name').css("display", "none");
		$('.btn-default').css("display", "none");

		$.each(svr_temp, function(index, value) {
			
			var template = value.trim();

			//if (index == "template") {
				//$.each(data, function(i, data) {
					if (template == "template_2G") {
						if (twogi == "0") {
							$('.radio0').show();
							addtab('#hid2gr', '#div2gr', 0);
							// $('.radio0').show();
						} else if (twogi == "1") {
							addtab('#hid2grone', '#div2grone', 0);
							$('.radio1').show();
						} else if (twogi == "2") {
							addtab('#hid2grtwo', '#div2grtwo', 0);
							$('.radio2').show();
						}
						twogi++;

					} else if (template == "template_5G") {

						if (fivegi == "0") {
							addtab('#hid5gr', '#div5gr', 0);
							$('.five0').show();
						} else if (fivegi == "1") {
							addtab('#hid5grone', '#div5grone', 0);
							$('.five1').show();
						} else if (fivegi == "2") {
							addtab('#hid5grtwo', '#div5grtwo', 0);

							$('.five2').show();
						}
						fivegi++;

					} else if (template == "template_2G5G") {

						if (twofivegi == "0") {
							addtab('#hid2g5gr', '#div2g5gr', 0);
							$('.twofive0').show();
						} else if (twofivegi == "1") {
							addtab('#hid2g5grone', '#div2g5grone', 0);
							$('.twofive1').show();
						} else if (twofivegi == "2") {
							addtab('#hid2g5grtwo', '#div2g5grtwo', 0);
							$('.twofive2').show();
						}
						twofivegi++;

					}

				//});
			//}
		})

		$(".trigger").trigger("change");

	} else {

		//srvrdta = {"radio":[{"index":0,"type":"2g","reg":"US","channel":1,"width":"40","txpwr":36,"sgi":0,"nss":0,"dcs":"false","stbc":0,"ldpc":0},{"index":1,"type":"5g","reg":"US","channel":36,"width":"80","txpwr":36,"sgi":0,"nss":0,"dcs":"false","stbc":0,"ldpc":0}],"lan_bridge":"br-lan","wan_bridge":"br-wan","network_balancer":0,"root":"yes","interfaces":[{"index":0,"type":"2g","radio_local_index":0,"mode":"ap","encryption":"wpa2-psk","ssid":"fbox_asus_test","key":"fbox_asus_test","acl":0,"bridge":"br-lan","hotspot":0},{"index":1,"type":"5g","radio_local_index":0,"mode":"ap","encryption":"wpa2-psk","ssid":"fbox_asus_test","key":"fbox_asus_test","acl":0,"bridge":"br-lan","hotspot":0},{"index":1,"type":"5g","radio_local_index":1,"mode":"mesh","encryption":"wpa2-psk","ssid":"fbox_asus_mesh","key":"fbox_asus_test","bridge":"br-lan","acl":0,"hotspot":0}],"macaddress_list":["18:31:bf:57:de:f0","18:31:bf:57:de:f1"],"opcode":"device_config","uid":"18:31:bf:57:d9:60","status":"success"}

		var jsonpayload = JSON.parse(srvrdta);
		//var jsondata = srvrdta.replace(/&quot;/g,'"');

         var jsondata = jsonpayload;
		 console.log("srvrdta" + JSON.stringify(jsondata));		 
		
        var iradio = jsondata.radio;

        function GetSortOrder(prop) {  // json array sorting
    return function(a, b) {  
        if (a[prop] > b[prop]) {  
            return 1;  
        } else if (a[prop] < b[prop]) {  
            return -1;  
        }  
        return 0;  
    }  
} 

iradio.sort(GetSortOrder("index"));


 var iface = jsondata.interfaces;

        function GetSortOrderface(prop) {  // json array sorting
    return function(a, b) {  
        if (a[prop] > b[prop]) {  
            return 1;  
        } else if (a[prop] < b[prop]) {  
            return -1;  
        }  
        return 0;  
    }  
} 

iface.sort(GetSortOrderface("index"));

console.log("the interface" + JSON.stringify(iradio));

		$('.display-name').css("display", "none");
		$('.btn-default').css("display", "none");

		var ik = 0;
		var jk = 0;
		var ijk = 0;
		var i = 0;
		var j = 0;
		var m = 0;
		var n = 0;
		var x = 0;
		var y = 0;
		var aa = 0;
		var bb = 0;
		var cc = 0;
		var aaa = 0;
		var yes;
		var yestwo;
		var yestwofive;
		var yesnew;
		var cur_type;
		var full = jsondata.interfaces;
		var temp_mod = [];
		$.each(iradio, function(i, data) {
			if (data.type == "2g") {
				//console.log("2 g value >>>>>>>" + i);                         
				if (ik == "0") {
					addtab('#hid2gr', '#div2gr', 0);
					$('.radio0').show();
					jk = "2gi";
					temp_mod.push(jk);
					//console.log("data" + data.index);
				} else if (ik == "1") {
					addtab('#hid2grone', '#div2grone', 0);
					$('.radio1').show();
					jk = "2gione";
					temp_mod.push(jk)
				} else if (ik == "2") {
					addtab('#hid2grtwo', '#div2grtwo', 0);
					$('.radio2').show();
					jk = "2gitwo";
					temp_mod.push(jk);
				}
				ik++;
			}

			if (data.type == "5g") {
				//console.log("5 g value >>>>>>>" + j);                
				if (j == "0") {
					addtab('#hid5gr', '#div5gr', 0);
					$('.five0').show();
					jk = "5gi";
					temp_mod.push(jk);
				} else if (j == "1") {
					addtab('#hid5grone', '#div5grone', 0);
					$('.five1').show();
					jk = "5gione";
					temp_mod.push(jk);
				} else if (j == "2") {
					addtab('#hid5grtwo', '#div5grtwo', 0);
					$('.five2').show();
					jk = "5gitwo";
					temp_mod.push(jk);
				}
				j++;
			}

			if (data.type == "2g5g") {
				//console.log("2 g value >>>>>>>" + i);                         
				if (ijk == "0") {
					addtab('#hid2g5gr', '#div2g5gr', 0);
					$('.twofive0').show();
					jk = "2g5gi";
					temp_mod.push(jk);
					//console.log("data" + data.index);
				} else if (ijk == "1") {
					addtab('#hid2g5grone', '#div2g5grone', 0);
					$('.twofive1').show();
					jk = "2g5gione";
					temp_mod.push(jk)
				} else if (ijk == "2") {
					addtab('#hid2g5grtwo', '#div2g5grtwo', 0);
					$('.twofive2').show();
					jk = "2g5gitwo";
					temp_mod.push(jk);
				}
				ijk++;
			}

		})

		var count = 0;

		if (jsondata.interfaces != undefined || jsondata.interfaces != "") {
			$.each(jsondata.interfaces, function(i, data) {

                 console.log(temp_mod);

				if (data.type == "2g") {
					var jar = data.index;
					yestwo = temp_mod[jar]
					if (yestwo == "2gi") {
						addtab("#hid2gi", "#div2gi", 0);
					} else if (yestwo == "2gione") {
						addtab("#hid2gione", "#div2gione", 0);
					} else if (yestwo == "2gitwo") {
						addtab("#hid2gitwo", "#div2gitwo", 0);
					}

				}

				if (data.type == "5g") {
					var kar = data.index;
					yes = temp_mod[kar];
					if (yes == "5gi") {
						addtab('#hid5gi', '#div5gi', 0);
					} else if (yes == "5gione") {
						addtab('#hid5gione', '#div5gione', 0);
					} else if (yes == "5gitwo") {
						addtab('#hid5gitwo', '#div5gitwo', 0);
					}

				}

				if (data.type == "2g5g") {
					var jarkar = data.index;
					yestwofive = temp_mod[jarkar];
					if (yestwofive == "2g5gi") {
						addtab('#hid2g5gi', '#div2g5gi', 0);
					} else if (yestwofive == "2g5gione") {
						addtab('#hid2g5gione', '#div2g5gione', 0);
					} else if (yestwofive == "2g5gitwo") {
						addtab('#hid2g5gitwo', '#div2g5gitwo', 0);
					}

				}

			})
		}

		$.each(iradio, function(key, data) {


			var cur_d = data;

			if (data.type == "2g") {
				//console.log("2 g value >>>>>>>" + i);                         
				if (aa == "0") {
					inpid = "2gr_";
				} else if (aa == "1") {
					inpid = "2grone_";
				} else if (aa == "2") {
					inpid = "2grtwo_";
				}
				aa++;
			}

			if (data.type == "5g") {
				//console.log("2 g value >>>>>>>" + i);                         
				if (bb == "0") {
					inpid = "5gr_";
				} else if (bb == "1") {
					inpid = "5grone_";
				} else if (bb == "2") {
					inpid = "5grtwo_";
				}
				bb++;
			}

			if (data.type == "2g5g") {
				//console.log("2 g value >>>>>>>" + i);                         
				if (aaa == "0") {
					inpid = "2g5gr_";
				} else if (aaa == "1") {
					inpid = "2g5grone_";
				} else if (aaa == "2") {
					inpid = "2g5grtwo_";
				}
				aaa++;
			}

			$.each(data,function(index, data) {

						if (index == "channel") {
							if (cur_type == "2g") {
								channel = data;
								val2ch = channel;
								$('#' + inpid + 'channel_' + y).val(data);
							} else {
								channel = data;
								val5ch = channel;
								$('#' + inpid + 'channel_' + y).val(data);
							}

						} else if (index == "fixedrate") {
							fixedrate = data;
						} else if (index == "mcast") {
							mcast = data;
						} else if (index == "acl" || index == "aclone") {
							$('#' + inpid + index + "_" + i).val(data);
							$('.aclGrp').show();

						} else if (index == "dcs") {
							$('#' + inpid + index + "_" + i).val(data);
						} else {
							$('#' + inpid + index + "_" + i).val(data);
						}

						if (index == "index") {
							$('#' + inpid + index + "_" + i).val(data);
						}

						if (index == "type") {
							$('#' + inpid + index + "_" + i).val(data);
							cur_type = data;
						}
                          //console.log("the data is" + cur_d.type)
						if (index == "reg") {
							ctryval = data;
							
							$('#' + inpid + index + "_" + i).val(data);
							if (cur_d.type == "2g") {
								reg2g(data, 0, 0, inpid, val2ch);
							} else if (cur_d.type == "5g") {
								reg5g(data, inpid, 0, 0, val5ch);
							} else {
								reg2g5g(data, inpid, 0, 0, val5ch);
							}
						}
						if (index == "txpwr") {
							txpwr = data;
						}
						if (index == "encryption") {
							encryption = data;
							if (encryption == "open") {
								index = "key";
								$('#' + inpid + index + "_" + i).prop(
										"disabled", true);
							}
						}
						if (index == "mode") {
							ff = data;

							var cur_dynamic = $("#lan_dhcp_checkbox").prop(
									"checked");
							var curWanDynamic = $("#wan_dhcp_checkbox").prop(
									"checked");
							var cur_lanonly = $("#lan_only_checkbox").prop(
									"checked");

							if (cur_dynamic == true || curWanDynamic == false
									|| cur_lanonly == true) {
								$("#wan_checkbox_spanOne_dhcp").hide();
							} else {
								$("#wan_checkbox_spanOne_dhcp").show();
							}
						}

					})

		})

		//interfaces data prefill area
		var xx = 0, yy = 0, zz = 0;
		var ll = 0, mm = 0, nn = 0;
		var pp = 0, qq = 0, rr = 0;

		if (jsondata.interfaces != undefined || jsondata.interfaces != "") {

			$.each(jsondata.interfaces, function(key, data) {

				var js = data.radio_local_index;
				var kartwo = data.index;
				yestwo = temp_mod[kartwo];
				
				if (data.type == "2g") {
					if (yestwo == "2gi") {
						inpid = "2gi_";
						if (xx == 0) {
							i = 0;
						} else {
							i = 1;
						}
						xx++;
					} else if (yestwo == "2gione") {
						inpid = "2gione_";

						if (yy == 0) {
							i = 0;
						} else {
							i = 1;
						}
						yy++;
					} else if (yestwo == "2gitwo") {
						inpid = "2gitwo_";
						if (zz == 0) {
							i = 0;
						} else {
							i = 1;
						}
						zz++;
					}

				}

				var karfive = data.index;
				yesnew = temp_mod[karfive];
				
				if (data.type == "5g") {
					if (yesnew == "5gi") {
						inpid = "5gi_";
						if (ll == 0) {
							i = 0;
						} else {
							i = 1;
						}
						ll++;
					} else if (yesnew == "5gione") {
						inpid = "5gione_";
						if (mm == 0) {
							i = 0;
						} else {
							i = 1;
						}
						mm++;
					} else if (yesnew == "5gitwo") {
						inpid = "5gitwo_";
						if (nn == 0) {
							i = 0;
						} else {
							i = 1;
						}
						nn++;
					}

				}

				var karjarnew = data.index;
				yestwofive = temp_mod[karjarnew];
				
				if (data.type == "2g5g") {
					if (yestwofive == "2g5gi") {
						inpid = "2g5gi_";
						if (pp == 0) {
							i = 0;
						} else {
							i = 1;
						}
						pp++;
					} else if (yestwofive == "2g5gione") {
						inpid = "2g5gione_";
						if (qq == 0) {
							i = 0;
						} else {
							i = 1;
						}
						qq++;
					} else if (yestwofive == "2g5gitwo") {
						inpid = "2g5gitwo_";
						if (rr == 0) {
							i = 0;
						} else {
							i = 1;
						}
						rr++;
					}

				}

				$.each(data, function(index, data) {

					if (index == "channel") {
						channel = data;
						$('#' + inpid + index + "_" + i).val(data);
					} else if (index == "mode") {
						$('#' + inpid + index + "_" + i).val(data);

					} else if (index == "encryption") {
						$('#' + inpid + index + "_" + i).val(data);

					} else if (index == "erp") {
						$('#' + inpid + index + "_" + i).val(data);

					} else if (index == "ssid") {
						$('#' + inpid + index + "_" + i).val(data);

					} else if (index == "key") {
						if (data) {
							$('#' + inpid + index + "_" + i).val(data);
							$('#' + inpid + index + "_" + i).css("pointer-events", "auto");
							$('#' + inpid + index + "_" + i).css("background-color", "white");
						} else {
							$('#' + inpid + index + "_" + i).val("");
							$('#' + inpid + index + "_" + i).css("pointer-events", "none");
							$('#' + inpid + index + "_" + i).css("background-color", "lightgray");
						}

					} else if (index == "bcastssid") {
						$('#' + inpid + index + "_" + i).val(data);

					} else if (index == "amsdu") {
						$('#' + inpid + index + "_" + i).val(data);

					} else if (index == "hotspot") {
						$('#' + inpid + index + "_" + i).val(data);

					} else if (index == "ampdu") {
						$('#' + inpid + index + "_" + i).val(data);

					} else if (index == "acl") {
						$('#' + inpid + index + "_" + i).val(data);

					} else if (index == "bridge") {
						$('#' + inpid + index + "_" + i).val(data);

					}

				})

			})

		}

		var divzero = $('.divclasszero').val();
		$('.twogcurindex').val(divzero);
		var divone = $('.divclassone').val();
		$('.twogcurindexone').val(divone);
		var divtwo = $('.divclasstwo').val();
		$('.twogcurindextwo').val(divtwo);

		var tzero = $('.classzero').val();
		$('.curindex').val(tzero);
		var tone = $('.classone').val();
		$('.curindexone').val(tone);
		var two = $('.classtwo').val();
		$('.curindextwo').val(two);

		var tfzero = $('.tfclasszero').val();
		$('.tfcurindex').val(tfzero);
		var tfone = $('.tfclassone').val();
		$('.tfcurindexone').val(tfone);
		var tfwo = $('.tfclasstwo').val();
		$('.tfcurindextwo').val(tfwo);

		

		/* if (rv == "yes") {
			$('.rootfun').prop("checked", true);
		} else {
			$('.rootfun').prop("checked", false);
		} */

		$('#lanval').val(jsondata.lan_bridge);
		$('#wanval').val(jsondata.wan_bridge);

		// A function call to check the mesh mode
		//  dcsGreyedOut_2G();
		//  dcsGreyedOut_5G();

		if (jsondata.allow_mesh_macs) {
			console.log("allow macs");
			$.each(jsondata.allow_mesh_macs, function(i, data) {

				$(add_button).trigger('click');
				$("#input_" + i).val(data);

			});
		}

	}

}


function datapass(a,b,c,d,e){

cur_uid = a;
cur_param = b;
cur_cid = c;
cur_sid = d;
cur_spid = e;

		//console.log("the value" + themainval);
		/* var result = themainval;
		//$('#uuid').val(result.uid);
		$.get('/facesix/template/qubercomm/config-source', function(data) {
			$('#onloadata').html(data);
			prefilldata(result);
		
		}); */

	$.ajax({
		type : 'GET',
		url : "/facesix/rest/device/info?uid="+cur_uid ,
		contentType : "application/json; charset=utf-8",
		dataType : "json",
		success : function(data) {
			console.log("data" + JSON.stringify(data));
			if (data.template) {
				var result = data.template;
			} else {
                var result = data.conf;
			}
			
			$('.lan_bridge').val(data.lanbridge);
			$('.wan_bridge').val(data.wanbridge);_
			
			$("#spanAddr").val(data.lan_ipv4_Addr);
			$("#spanMask").val(data.lan_ipv4_mask);
			$("#lan_ipv4_gateway").val(data.lan_ipv4_gateway);
			$("#lan_ipv4_dns").val(data.lan_ipv4_dns);
			$("#lan_ipv4_dns1").val(data.lan_ipv4_dns1);
			$("#lan_ipv4_dhcp_dns").val(data.lan_ipv4_dhcp_dns);
			$("#lan_ipv4_dhcp_dns1").val(data.lan_ipv4_dhcp_dns1);
			$("#wan_ipv4_addr").val(data.wan_ipv4_Addr);
			$("#wan_ipv4_mask").val(data.wan_ipv4_mask);
			$("#wan_ipv4_gateway").val(data.wan_ipv4_gateway);
			$("#wan_ipv4_dns").val(data.wan_ipv4_dns);
			$("#wan_ipv4_dns1").val(data.wan_ipv4_dns1);
			$("#wan_ipv4_dhcp_dns").val(data.wan_ipv4_dhcp_dns);
			$("#wan_ipv4_dhcp_dns1").val(data.wan_ipv4_dhcp_dns1);


			if(data.lan_Flag == "lan_static"){
				$("#lan_static_checkbox").prop("checked",true);
				$('#lan_static_checkbox').trigger('click');
			} else {
				$("#lan_dhcp_checkbox").prop("checked",true);
				$('#lan_dhcp_checkbox').trigger('click');
			}

			if(data.lan_Only == "lan_only"){
				$("#lan_only_checkbox").prop("checked",true);
			}
			

			if(data.wan_Flag == "wan_static"){
				$("#wan_static_checkbox").prop("checked",true);
				$('#wan_static_checkbox').trigger('click');
				
			} else {
				$("#wan_dhcp_checkbox").prop("checked",true);
				$('#wan_dhcp_checkbox').trigger('click');
			}
			


            var nb = data.network_balancer;
		    var rv = data.root;
		    if (nb == "7") {
			    $('#network_balancer').prop("checked", true);
		      } else {
			   $('#network_balancer').prop("checked", false);
			  }
			  
			  
	  if ($('#network_balancer').is(':checked')) {
        switchStatus = 7;
    }
    else {
       switchStatus = 0;
    }
			$("#root").val(rv);
			
			$('#uuid').val(data.uid);
			$.get('/facesix/template/qubercomm/config-source', function(data) {
				$('#onloadata').html(data);
				prefilldata(result);
			});
		}
	});


}

$(document).ready(function(){
	
	
		


});

$(function() {
	$(document).on('click', '.tabtn', function() {
		var attr = $(this).attr('id');
		sowide(attr);
	});
	$(document).on('change', '.livcbx', function() {
		var tmpid = '#' + $(this).attr('id').replace("cbx_", "");
		if (this.checked) {
			$(tmpid).val(1);

		} else {
			$(tmpid).val(0);
		}

	});

	var myPattern = /\b(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\b/;
	var x = 46;

	$(".keypressing").keypress(
			function(event) {
				if (event.which != 8 && event.which != 0 && event.which != x
						&& (event.which < 48 || event.which > 57)) {
					console.log(event.which);
					return false;
				}
			}).keyup(function() {
		var this1 = $(this);

		var thatId = event.target.id;
		if (!myPattern.test(this1.val())) {
			$('#' + thatId).css("border", "1px solid red");
			$('#' + thatId).focus();
			while (this1.val().indexOf("..") !== -1) {
				this1.val(this1.val().replace('..', '.'));
			}
			x = 46;
		} else {
			x = 0;
			var lastChar = this1.val().substr(this1.val().length - 1);
			if (lastChar == '.') {
				this1.val(this1.val().slice(0, -1));
			}
			var ip = this1.val().split('.');
			if (ip.length == 4) {
				$('#' + thatId).css("border", "1px solid #ccc");
			}
		}
	});

	$('#valsubmit')
			.click(
					function() {

						uni_key = 0;
						cur_color = 0;

						$('#duplicate').hide();
						$("#deconfig").val('');

						var mytxt = '{"conf":{';
						var myit = 0;

						var t1, t2, t3, t4, t5, t6 = "";
						var p1, p2, p3, p4, p5, t6 = "";

						t1 = makeJstr(
								'#div2gr *,#div2grone *,#div2grtwo *,#div5gr *,#div5grone *,#div5grtwo *,#div2g5gr *,#div2g5grone *,#div2g5grtwo *',
								'radio', myit);
						mytxt += t1;
						if (mytxt != "{")
							myit = 1;

						t2 = makeJstr(
								'#div2gi *,#div2gione *,#div2gitwo *,#div5gi *,#div5gione *,#div5gitwo *,#div2g5gi *,#div2g5gione *,#div2g5gitwo *',
								'interfaces', myit);
						mytxt += t2;
						if (mytxt != "{")
							myit = 1;

						var cur_in_val = $(".field-long").val();
						if (cur_in_val) {
							t3 = makeJstr('.input_fields_wrap *',
									'allow_mesh_macs', myit);
							mytxt += t3;
							if (mytxt != "{")
								myit = 1;

						}
						 t4 = makeJstr('#lanwanset *','network_settings',myit);
						 mytxt += t4;

						mytxt += '}';
				
						console.log("full jsondata" + mytxt);

						var ok = 0;
						var requir = 0;
						$('#allfromdata .requir').each(function() {
							if ($(this).val() == "") {
								requir = 1;
								$(this).css("border", "1px solid red");
								$(this).focus();
							}
						});

						var key_req = 0;
						$('#allfromdata .key_req').each(function() {

							console.log("Key Value" + $(this).val());

							var isDisabled = $(this).prop('disabled');

							if ($(this).val() == "" && isDisabled == false) {
								key_req = 1;
								$(this).css("border", "1px solid red");
								$(this).focus();
							} else {
								ok = 49;
							}
						});

						var aclist = $(".field-long").val();
						if (aclist == "") {
							ok = 85;
							$('.field-long').css("border", "1px solid red");
							$('.field-long').focus();

						}

						/*$('#h32gr,#h35gr,#h32gi,#h35gi').css("color", "#333");
						if(requir==1 || key_req==1) {
						    ok = 1
						} 
						if(t1=="" && t3==""){
						    console.log(1)
						    $('#h32gr,#h35gr').css("color", "red");
						    ok = 2
						} 
						if(t1!="" && t2==""){
						    $('#h32gi').css("color", "red");
						    console.log(222);
						    ok = 3
						} 
						if(t3!="" && t4==""){
						    $('#h35gi').css("color", "red");
						    
						    //console.log(333)
						    ok = 4
						} */

						var rts2g, fmt2g, rts5g, fmt5g;
						rts2g = $('#2gr_rtsth_0').val();
						fmt2g = $('#2gr_fgmth_0').val();
						rts5g = $('#5gr_rtsth_0').val();
						fmt5g = $('#5gr_fgmth_0').val();

						if (rts2g == "" || fmt2g == "") {
							$("#tabtn-1-2").click();
							$('#tabtn-1-2').css("color", "red");
							$("html, body").animate({
								scrollTop : 0
							}, 100);
						} else {
							$('#tabtn-1-2').css("color", "black");
						}

						if (rts5g == "" || fmt5g == "") {
							$("#tabtn-3-2").click();
							$('#tabtn-3-2').css("color", "red");
						} else {
							$('#tabtn-3-2').css("color", "black");
						}

						if (txPwrError == 1) {
							ok = 7
						}

						if ($('#flag').length > 0) {
							console.log("--Venue/Floor----");
						} else {
							if ($('#alias').val() == "") {
								$('#alias').css("border", "1px solid red");
								$('#alias').focus();
								ok = 5
							}
							var landhcpVal = $('#lan_dhcp_checkbox').prop(
									'checked');
							if (landhcpVal == false) {
								if ($('#spanAddr').val() == "") {
									$('#spanAddr').css('border',
											'1px solid red');
									$('#spanAddr').focus();
									//   ok = 8;
								} else {
									$('#spanAddr').css('border',
											'1px solid #ccc');
								}
								if ($('#spanMask').val() == "") {
									$('#spanMask').css('border',
											'1px solid red');
									$('#spanMask').focus();
									//  ok = 34;
								} else {

									$('#spanMask').css('border',
											'1px solid #ccc');
								}
								if ($('#lan_ipv4_gateway').val() == "") {
									$('#lan_ipv4_gateway').css('border',
											'1px solid red')
									$('#lan_ipv4_gateway').focus();
									// ok=10;
								} else {
									$('#lan_ipv4_gateway').css('border',
											'1px solid #ccc');
								}
							} else {
							//	document.getElementById('spanAddr').value = "";
							//	document.getElementById('spanMask').value = "";
							}

							var wan_static_val = $('#wan_static_checkbox')
									.prop('checked');
							var lanOnly = $("#lan_only_checkbox").prop(
									'checked');
							if (lanOnly == false) {
								if (wan_static_val == true) {
									if ($('#wan_ipv4_addr').val() == "") {
										$('#wan_ipv4_addr').css('border',
												'1px solid red');
										$('#wan_ipv4_addr').focus();
										ok = 11;
									} else {
										$('#wan_ipv4_addr').css('border',
												'1px solid #ccc');
									}
									if ($('#wan_ipv4_mask').val() == "") {
										$('#wan_ipv4_mask').css('border',
												'1px solid red')
										$('#wan_ipv4_mask').focus();
										ok = 12;
									} else {
										$('#wan_ipv4_mask').css('border',
												'1px solid #ccc');
									}
									if ($('#wan_ipv4_gateway').val() == "") {
										$('#wan_ipv4_gateway').css('border',
												'1px solid red');
										$('#wan_ipv4_gateway').focus();
										ok = 13;
									} else {
										$('#wan_ipv4_gateway').css('border',
												'1px solid #ccc');
									}
								}
							}
							  
							var tt = $("#2gi_ssid_0").val();
							if(tt == ""){
								ok = 88;
							}
							 
							if ($('#uuid').val() == ""
									|| $('#uuid').val().length < 17) {
								$('#uuid').css("border", "1px solid red");
								$('#uuid').focus();
								ok = 6
							}
							if ($('#statusInterval').val() == "") {
								$('#statusInterval').css("border",
										"1px solid red");
								$('#statusInterval').focus();
								ok = 7
							}
						}

						if (ok == 49) {
							if ($("#lanval").val() == "") {

								$('#lanval').css('border', '1px solid red');
								$('#lanval').focus();
								ok = 33;

							} else if ($("#wanval").val() == "") {
								$('#wanval').css('border', '1px solid red');
								$('#wanval').focus();
								ok = 35;

							} else {

								ok = 9;

							}
						}

						if (amm == 0 && cur_color == 0) {
							//console.log("color border clear");
							if (ok == 9 && uni_key == 0) {

								$('html, body').animate({
									scrollTop : 0
								}, '100');
								$("#deconfig").val(mytxt);
								$(".loader").show();
								$('.loader').addClass("loader");
								$('body').addClass("overlay");

								$.ajax({
											type : 'POST',
											url : "/facesix/rest/device/save",
											contentType : "application/json; charset=utf-8",
											data : mytxt,
											dataType : "json",
											success : function(data) {
												console.log("data success");
												$('.loader').addClass("loader");
												$('body').addClass("overlay");
												$(".loader").hide();
												$(".popval").html("configuration success !");
												$(".popupbox").show();
												setTimeout(function() {
													location.reload();
												}, 1000);

											},
											error : function(data) {
												console.log("data error");
												$(body).addClass("loader");
												$(body).addClass("overlay");
												$(".popval").html("configuration failed !");
												$(".popupbox").show();
												setTimeout(function() {
													location.reload();
												}, 1000);
												//location.reload();                                                  
											}

										});

							}
						} else {
							$('html, body').animate({
								scrollTop : 0
							}, '100');
						}
						return false;

					});

});

var kar = 0;
function makeJstr(mydiv, mynam, ite) {
	var aclmesh = 0;
	var mytxt = "";
	var mode = "";
	var avl;
	var lastindex = -1;
	var last_val = $(mydiv).serializeArray().length;

	var cur_R = $("#root").val();
	var cur_L = $(".lan_bridge").val();
    var cur_W = $(".wan_bridge").val();

	avl = 0;
	//console.log("avllllllllllllllllllll" + field);
	$.each($(mydiv).serializeArray(), function(i, field) {
		//console.log("field value" + JSON.stringify(field));
		avl++;
		if (avl == 1) {

			if (ite == 1)
				mytxt += ',';
			if (mynam == "allow_mesh_macs") {
				mytxt += '"' + mynam + '":[';
			} else if(mynam =="network_settings"){
                mytxt += '"' + mynam + '":{';
			} else {
				mytxt += '"' + mynam + '":[';
				mytxt += '{';
			}
			lastindex = 0;
		}
		var ar = field.name.split('__');
		ar[0] = ar[0] * 1;

		if (lastindex < ar[0]) {
			mytxt = mytxt.replace(/,\s*$/, "");
			//  mytxt += '},{';
			lastindex = ar[0];
		}

		if (ar[1] == "mode") {
			mode = field.value.trim();
		}
		if (ar[1] == "acl" && (mode == "mesh" || mode == "sta")) {
			return true;
		}

		if (mynam == "radio") {
			if (ar[1] != "dcs") {
				if (ar[1] == "txpwr" && avl != last_val) {
					mytxt += '"' + ar[1] + '":"' + field.value.trim() + '"},{';
				} else if (ar[1] == "txpwr" && avl == last_val) {
					mytxt += '"' + ar[1] + '":"' + field.value.trim() + '"}';
				} else {
					mytxt += '"' + ar[1] + '":"' + field.value + '",';
				}
			}

		} else if(mynam == "interfaces") {
			if (ar[1] != "hotspot") {

				if (ar[1] == "mode") {
					if (field.value == "mesh") {
						if (cmv == 0) {
							cur_mode_val = "mesh";
							cmv++;
						}
					} else {

						cur_ap_val = "ap";
					}
				}

				if (ar[1] == "mode") {
					th_mode = field.value;
					cr_mode.push(th_mode);
					/* if (field.value == "mesh") {
						decider = 1;
					} */

				}

               

				if (ar[1] == "key" && avl != last_val) {


						mytxt += '"' + ar[1] + '":"' + field.value.trim() + '"},{';
					
				
					kar = 0;
					if (field.value) {
						var uni_keyval = field.value;
						var uni_keylen = uni_keyval.length;
						if (uni_keylen < 8) {
							uni_key = 1;
						}
					} else {

						var id = $('input[name="' + field.name + '"]').attr(
								"id");
						var color = $("#" + id).css("background-color");
						if (color == "rgb(255, 255, 255)") {
							cur_color = 1;
						}

					}
				} else if (ar[1] == "key" && avl == last_val) {

					mytxt += '"' + ar[1] + '":"' + field.value.trim() + '"}';
					if (field.value) {
						var uni_keyval = field.value;
						var uni_keylen = uni_keyval.length;
						if (uni_keylen < 8) {
							uni_key = 1;
						}
					} else {

						var id = $('input[name="' + field.name + '"]},').attr(
								"id");
						var color = $("#" + id).css("background-color");
						if (color == "rgb(255, 255, 255)") {
							cur_color = 1;
						}
					}

				} else {
				   if (mynam == "allow_mesh_macs") {
						console.log("nan is nan")
						if (field.value) {
							mytxt += '"' + field.value.trim() + '",';
							$("#input_" + aclmesh).css("border",
									"1px solid lightgray");
							aclmesh++;
						} else {
							amm = 1;
							$("#input_" + aclmesh).css("border",
									"1px solid red");
							aclmesh++;
						}
					} 
					 else {

                         console.log("the ar values" + ar[1]);
                            if(ar[1] == "bridge"){
					if(cur_R == "yes"){
                        mytxt += '"' + ar[1] + '":"' + cur_L + '",';
					 } else {
                        mytxt += '"' + ar[1] + '":"' + cur_W + '",';
					}
				} else {
                     mytxt += '"' + ar[1] + '":"' + field.value.trim()
								+ '",';
				}
						
					}
				}
			}

		} else if(mynam == "network_settings") {
             if(field.value){			

                 	if (avl != last_val){				
               			mytxt += '"' +  field.name.trim() + '":"' + field.value.trim()+ '",';
					} else {				
               			mytxt += '"' +  field.name.trim() + '":"' + field.value.trim()+ '"}';
					}
				 }		
			}
		

	});
	mytxt = mytxt.replace(/,\s*$/, "");
    var csk;
	console.log("the name is" + mynam);
	
	if(mynam == "radio"){
		csk = "typeradio"
	}

	if(mynam == "interfaces"){
		csk = "typeifs"
	}


	if (avl > 0 && csk == "typeradio"){
       mytxt += ']';
	} else if (avl > 0 && csk == "typeifs") {
       mytxt += ']}';
	}
		
	
		if(csk == "typeifs"){

		
		
	if (avl > 0 && tot_size == "0") {

		var k = $('input[type=checkbox]').attr('checked');

		if (k == "checked") {
			rootval = "yes";
		} else {
			rootval = "yes";
		}

		var q = $('input[type=checkbox]').attr('checked');
		var lan_cur_val = $("#lanval").val();
		var wan_cur_val = $("#wanval").val();
		
        
	    var uid  = cur_uid;
		var cid  = cur_cid;
		var sid  = cur_sid;
		var spid = cur_spid;
		var keep_alive = $("#statusInterval").val();
		var cur_root = $("#root").val();
		var cur_mode = $("#workingMode").val();
		//var net = switchStatus;
		var cur_alias = $("#alias").val();
		
		

		//var nb_int = parseInt(net); 


     

		mytxt += ',' + '"uid":"' + uid + '"';
		mytxt += ',' + '"alias":"' + cur_alias + '"';
		mytxt += ',' + '"cid":"' + cid + '"';
		mytxt += ',' + '"sid":"' + sid + '"';
		mytxt += ',' + '"spid":"' + spid + '"';
		mytxt += ',' + '"keepAliveInterval":"' + keep_alive +'"';
		mytxt += ',' + '"root":"' + cur_root + '"';
		mytxt += ',' + '"workingMode":"' + cur_mode + '"';
		mytxt += ',' + '"network_balancer":' + switchStatus;     
		mytxt += ',' + '"param":"' + cur_param + '"';
		mytxt += ',' + '"lan_bridge":"' + cur_L + '"';
		mytxt += ',' + '"wan_bridge":"' + cur_W + '"';
		
		tot_size++;
	}
}

	if (cr_mode.length > 0) {
		if (gk == 0) {
			console.log("decider" + decider);
			/* if (decider == 1) {

				var a = $('.ani').attr('checked');
				if (a == "checked") {
					anival = '7';
				} else {
					anival = '0';
				}

			} else {

				anival = 0;

			} */

			//mytxt += ',' + '"network_balancer":"' + anival + '"';
			//mytxt += ',' + '"root":"' + rootval + '"';

		}
		gk++;
	}

	// console.log('final'+mytxt);
	return mytxt;
}

function switchbutton() {
	var k = $('input[type=checkbox]').attr('checked');
	var lan_cur_val = $("#lanval").val();
	var wan_cur_val = $("#wanval").val();
	if (k == "checked") {
		rootval = "yes";
		$('.curbridge').val(lan_cur_val);
	} else {
		rootval = "no";
		$('.curbridge').val(wan_cur_val);
	}

}


$("#network_balancer").on('change', function() {
    if ($(this).is(':checked')) {
        switchStatus = 7;
    }
    else {
       switchStatus = 0;
    }
});



function sowide(attr) {
	//console.log(attr);
	var tid = attr.split("-");
	$(".tabtn" + tid[1]).removeClass('active');
	$("#tabtn-" + tid[1] + "-" + tid[2]).addClass('active');

	$(".tabc" + tid[1]).hide();
	$("#tabc-" + tid[1] + "-" + tid[2]).show();
}
function makedropdownArray(ddid, ary) {
	var ddopt;
	for (i = 0; i < ary.length; i++) {
		ddopt += "<option value='" + ary[i] + "'>" + ary[i] + "</option>";
	}
	$(ddid).html(ddopt);
}
function makedropdown(ddid, cnt) {
	var ddopt = "<option value='auto'>auto</option>";

	for (i = 1; i <= cnt; i++) {
		ddopt += "<option value='" + i + "'>" + i + "</option>";
	}
	$(ddid).html(ddopt);
}

function makecbx5g(x, ary) {
	var ddopt = "";
	var cbxid = "#cbx_grp_" + x;
	for (i = 0; i < ary.length; i++) {
		ddopt += '<span><input type="checkbox" id="5gr_acs_' + x + '_' + ary[i]
				+ '" class="5gr_acs_' + x + '" value="' + ary[i]
				+ '" onClick="getACS(\'.5gr_acs_' + x + '\',\'#5gr_hid_' + x
				+ '\')">' + ary[i] + '</span>';
	}
	$(cbxid).html(ddopt);
}
function makecbx2g(x, ary) {
	var ddopt = "";
	var cbxid = "#cbx_grp2g_" + x;
	for (i = 0; i < ary.length; i++) {
		ddopt += '<span><input type="checkbox" id="2gr_acs_' + x + '_' + ary[i]
				+ '" class="2gr_acs_' + x + '" value="' + ary[i]
				+ '" onClick="getACS(\'.2gr_acs_' + x + '\',\'#2gr_hid_' + x
				+ '\')">' + ary[i] + '</span>';
	}
	//console.log(ddopt);
	$(cbxid).html(ddopt);
}
function addtab(frmd, tod, makmnu) {
	makmnu = 0;

	tabcnt++;
	var mydata = $(frmd).html().replace(/zzz/g, tabcnt);

	if (tod == "#div2gr") {
		//$('.divclasszero').val(cur_ind_val);	
		mydata = mydata.replace(/yyy/g, radio);
		//	$('.divclasszero').val("0"); 
	} else if (tod == "#div2grone") {
		mydata = mydata.replace(/yyy/g, radio);
		$('.divclassone').val(cur_ind_val);

	} else if (tod == "#div2grtwo") {
		mydata = mydata.replace(/yyy/g, radio);
		$('.divclasstwo').val(cur_ind_val);

	} else if (tod == "#div5gr") {

		mydata = mydata.replace(/yyy/g, radio);
		$('.classzero').val(cur_ind_val);
	} else if (tod == "#div5grone") {

		mydata = mydata.replace(/yyy/g, radio);
		$('.classone').val(cur_ind_val);
	} else if (tod == "#div5grtwo") {
		mydata = mydata.replace(/yyy/g, radio);
		$('.classtwo').val(cur_ind_val);
	} else if (tod == "#div2g5gr") {

		mydata = mydata.replace(/yyy/g, radio);
		$('.tfclasszero').val(cur_ind_val);
	} else if (tod == "#div2g5grone") {

		mydata = mydata.replace(/yyy/g, radio);
		$('.tfclassone').val(cur_ind_val);
	} else if (tod == "#div2g5grtwo") {
		mydata = mydata.replace(/yyy/g, radio);
		$('.tfclasstwo').val(cur_ind_val);
	}

	else if (tod == "#div2gi") {
		two_index_val = $('.divclasszero').val();
		two_index_name = "div2gi";
		mydata = mydata.replace(/yyy/g, inf2g);
		two_di_size = $('#div2gi div').size();

		if(two_di_size == 30){
					$("#twogib").prop("disabled",true);
				}

		inf2g++;

	} else if (tod == "#div2gione") {
		two_index_val = $('.divclassone').val();
		two_index_name = "div2gione";
		mydata = mydata.replace(/yyy/g, inf2gone);
		two_di_size = $('#div2gione div').size();
		if(two_di_size == 30){
					$("#twogibone").prop("disabled",true);
				}
		inf2gone++;

	} else if (tod == "#div2gitwo") {
		two_index_val = $('.divclasstwo').val();
		two_index_name = "div2gitwo";
		mydata = mydata.replace(/yyy/g, inf2gtwo);
		two_di_size = $('#div2gitwo div').size();
		 if(two_di_size == 30){
					$("#twogibtwo").prop("disabled",true);
				} 
		inf2gtwo++;
	}

	else if (tod == "#div5gi") {
		index_val = $('.classzero').val();
		index_name = "div5gi";
		mydata = mydata.replace(/yyy/g, inf5g);
		di_size = $('#div5gi div').size();
		if(di_size == 30){
					$("#fivegib").prop("disabled",true);
				}
		inf5g++;

	} else if (tod == "#div5gione") {
		index_val = $('.classone').val();
		index_name = "div5gione";
		mydata = mydata.replace(/yyy/g, inf5gone);
		di_size = $('#div5gione div').size();
		if(di_size == 30){
					$("#fivegibone").prop("disabled",true);
				}
		inf5gone++;
	} else if (tod == "#div5gitwo") {
		index_val = $('.classtwo').val();
		index_name = "div5gitwo";
		mydata = mydata.replace(/yyy/g, inf5gtwo);
		di_size = $('#div5gitwo div').size();
		 if(di_size == 30){
					$("#fivegibtwo").prop("disabled",true);
				}
		inf5gtwo++;

	} else if (tod == "#div2g5gi") {
		tf_index_val = $('.tfclasszero').val();
		tf_index_name = "div2g5gi";
		mydata = mydata.replace(/yyy/g, inf2g5g);
		tf_di_size = $('#div2g5gi div').size();
		if(tf_di_size == 30){
					$("#twofivegib").prop("disabled",true);
				}
		inf2g5g++;

	} else if (tod == "#div2g5gione") {
		tf_index_val = $('.tfclassone').val();
		tf_index_name = "div2g5gione";
		mydata = mydata.replace(/yyy/g, inf2g5gone);
		tf_di_size = $('#div2g5gione div').size();
		if(tf_di_size == 30){
					$("#twofivegibone").prop("disabled",true);
				}
		inf2g5gone++;
	} else if (tod == "#div2g5gitwo") {
		tf_index_val = $('.tfclasstwo').val();
		tf_index_name = "div2g5gitwo";
		mydata = mydata.replace(/yyy/g, inf2g5gtwo);
		tf_di_size = $('#div2g5gitwo div').size();
		if(tf_di_size == 30){
					$("#twofivegibtwo").prop("disabled",true);
				}
		inf2g5gtwo++;

	}

	//console.log("my data" + mydata);

	$(tod).append(mydata);
	sowide('tabtn-' + tabcnt + '-1')

	//append index value

	if (tod == "#div2gr") {
		$('.divclasszero').val(cur_ind_val);
	} else if (tod == "#div2grone") {
		$('.divclassone').val(cur_ind_val);

	} else if (tod == "#div2grtwo") {
		$('.divclasstwo').val(cur_ind_val);

	} else if (tod == "#div5gr") {
		$('.classzero').val(cur_ind_val);
	} else if (tod == "#div5grone") {
		$('.classone').val(cur_ind_val);
	} else if (tod == "#div5grtwo") {
		$('.classtwo').val(cur_ind_val);
	} else if (tod == "#div2g5gr") {
		$('.tfclasszero').val(cur_ind_val);
	} else if (tod == "#div2g5grone") {
		$('.tfclassone').val(cur_ind_val);
	} else if (tod == "#div2g5grtwo") {
		$('.tfclasstwo').val(cur_ind_val);
	}

	cur_ind_val++;

	var k = $('input[type=checkbox]').attr('checked');
	var lan_cur_val = $("#lanval").val();
	var wan_cur_val = $("#wanval").val();
	/* if (k == "checked") {

		$('.curbridge').val(lan_cur_val);
	} else {

		$('.curbridge').val(wan_cur_val);
	} */

	interfacefill(index_val, index_name, di_size);
	interfacefilltwo(two_index_val, two_index_name, two_di_size);
	interfacefilltwofive(tf_index_val, tf_index_name, tf_di_size);

}
function getModes(v, g, i) {

	var ddopt = "";
	var ddid = "";

	if (g == 2) {
		ddid = "#2gi_fixedrate_" + i;
		mcid = "#2gi_mcast_" + i;
	} else {
		ddid = "#5gi_fixedrate_" + i;
		mcid = "#5gi_mcast_" + i;
	}
	//console.log(v+'---'+g+'---'+i);
	ddopt = "";
	for (j = 0; j < modes[v].length; j++) {
		ddopt += "<option value='" + modes[v][j] + "'>" + modes[v][j]
				+ "</option>";
	}
	$(ddid).html(ddopt);
	$(mcid).html(ddopt);

}

function getEncr(v, g, i) {
	var ddid = "";

	if (g == 2) {
		ddid = "#2gi_key_" + i;
	} else {
		ddid = "#5gi_key_" + i;
	}
	if (v == "open") {
		$(ddid).val("");
		$(ddid).css("pointer-events", "none");
		$(ddid).css("background-color", "lightgray");
	} else {
		$(ddid).css("pointer-events", "auto");
		$(ddid).css("background-color", "white");
	}

}

function getHotspot(v, g, i) {
	var bridgeVal = "";

	if (g == 2) {
		bridgeVal = "#2gi_bridge_" + i;
	} else {
		bridgeVal = "#5gi_bridge_" + i;
	}
	if (v == "on") {
		$(bridgeVal).css('pointer-events', 'none');
		$(bridgeVal).css('background-color', 'lightgray');
		$(bridgeVal).val("lan");
	} else {
		$(bridgeVal).val("wan");
		$(bridgeVal).css('pointer-events', 'auto');
		$(bridgeVal).css('background-color', '');
	}

}

function getTxpwr(v, g, i) {
	var ddid = "";
	txPwrError = 0;
	if (g == 2) {
		ddid = "#2gr_reg_" + i;
		txid = "#2gr_txpwr_" + i;
	} else {
		ddid = "#5gr_reg_" + i;
		txid = "#5gr_txpwr_" + i;
	}

	var cid = $(ddid).val();

	console.log("cntry" + cid + "tx" + v + "CC" + myar[cid]);

	if (myar[cid] >= v) {
		txPwrError = 0;
		$(txid).css("border", "1px black");
		$(txid).val(v)
	} else {
		console.log("ErrorK");
		txPwrError = 1;
		$(txid).css("border", "1px solid red");
		$(txid).focus();
	}

}

function deltab(tod) {

	 			if(tod == "#div2gi"){
				   $("#twogib").prop("disabled",false);	
                   inf2g--; 
                } else if (tod == "#div2gione"){
					$("#twogibone").prop("disabled",false);
                    inf2gone--;
                } else if(tod == "#div2gitwo"){
					$("#twogibtwo").prop("disabled",false);
                    inf2gtwo--;
                } else if(tod == "#div5gi"){
					$("#fivegib").prop("disabled",false);
                    inf5g--;
                } else if(tod == "#div5gione"){
					$("#fivegibone").prop("disabled",false);
                    inf5gone--;
                } else if(tod =="#div5gitwo"){
					$("#fivegibtwo").prop("disabled",false);
                    inf5gtwo--;
                } else if(tod == "#div2g5gi"){
					$("#twofivegib").prop("disabled",false);
                    inf2g5g--;
                } else if(tod == "#div2g5gione"){
					$("#twofivegibone").prop("disabled",false);
                    inf2g5gone--;
                } else if(tod =="#div2g5gitwo"){
					$("#twofivegibtwo").prop("disabled",false);
                    inf2g5gtwo--;
                }

	$(tod + ' .tabgrp:last').fadeOut().remove();

	/*if(tod == "#div2gr") radio2g--;
	
	if(tod == "#div2gi"){
	    --interfaces2g;
	    //Function call to Check mesh vap for 2G
	    dcsGreyedOut_2G();
	}
	else if(tod == "#div5gi"){
	    --interfaces5g;
	    //Function call to Check mesh vap for 5G
	    dcsGreyedOut_5G();
	}*/
	/*
	 var twogbox = $('#div2gi > div').size();
	 var fivegbox = $('#div5gi > div').size();

	 if(twogbox == 1 && fivegbox == 1){
	 $('input[name=wan]').attr("disabled",false);
	 $("#lan_checkbox_span").show();
	 $("#lan_static_checkbox").prop("checked",true);
	 $("#lan_checkbox_spanOne_dhcp").hide();
	 $("#wan_checkbox_spanOne_dhcp").show();
	 $("#lan_only_checkbox").prop("disabled",false);
	 $("#lan_only_checkbox").prop("checked",false);
	 $('#lanLabel').css("pointer-events","auto");
	 //$('#wan_ipv4_dhcp_dns').val('8.8.8.8');
	 //$('#wan_ipv4_dhcp_dns1').val('8.8.4.4');
	 //$('#spanAddr').val('192.168.1.1');
	 //$('#spanMask').val('255.255.255.0');
	 $("#root").val('yes');
	 }*/

}

function reg2g(vl, txpwr, y, inpid, cur_ch) {

	if (txpwr != 0) {
		$('#' + inpid + 'txpwr_' + y).val(txpwr);
	} else {
		$('#' + inpid + 'txpwr_' + y).val(myar[vl]);
	}

	makedropdownArray('#' + inpid + 'channel_' + y, req2g[vl]);
	makecbx2g(y, req2g[vl]);

}
function reg5g(vl, txpwr, y, inpid) {

	if (txpwr != 0) {
		$('#' + txpwr + 'txpwr_' + y).val(txpwr);
	} else {
		$('#' + txpwr + 'txpwr_' + y).val(myar[vl]);
	}

	makedropdownArray('#' + txpwr + 'channel_' + y, req5g[vl]);
	makecbx5g(y, req5g[vl]);

}

function reg2g5g(vl, txpwr, y, inpid) {

	if (txpwr != 0) {
		$('#' + txpwr + 'txpwr_' + y).val(txpwr);
	} else {
		$('#' + txpwr + 'txpwr_' + y).val(myar[vl]);
	}

	makedropdownArray('#' + txpwr + 'channel_' + y, req2g5g[vl]);
	makecbx5g(y, req2g5g[vl]);

}

function reg2gext(vl, txpwr, y, id) {

	if (id == "2gr_reg_0") {
		id = "2gr_";
	} else if (id == "2grone_reg_0") {
		id = "2grone_"
	} else {
		id = "2grtwo_"
	}
	if (txpwr != 0) {
		$('#' + id + 'txpwr_' + y).val(txpwr);
	} else {
		$('#' + id + 'txpwr_' + y).val(myar[vl]);
	}

	makedropdownArray('#' + id + 'channel_' + y, req2g[vl]);
	makecbx2g(y, req2g[vl]);

	$('#' + id + 'channel_' + y).val(req2g[vl]);
}

function reg5gext(vl, txpwr, y, id) {

	if (id == "5gr_reg_0") {
		id = "5gr_";
	} else if (id == "5grone_reg_0") {
		id = "5grone_"
	} else {
		id = "5grtwo_"
	}

	if (txpwr != 0) {
		$('#' + id + 'txpwr_' + y).val(txpwr);
	} else {
		$('#' + id + 'txpwr_' + y).val(myar[vl]);
	}

	makedropdownArray('#' + id + 'channel_' + y, req5g[vl]);
	makecbx5g(y, req5g[vl]);

	$('#' + id + 'channel_' + y).val(req5g[vl]);
}

function reg2g5gext(vl, txpwr, y, id) {

	if (id == "2g5gr_reg_0") {
		id = "2g5gr_";
	} else if (id == "2g5grone_reg_0") {
		id = "2g5grone_"
	} else {
		id = "2g5grtwo_"
	}

	if (txpwr != 0) {
		$('#' + id + 'txpwr_' + y).val(txpwr);
	} else {
		$('#' + id + 'txpwr_' + y).val(myar[vl]);
	}

	makedropdownArray('#' + id + 'channel_' + y, req2g5g[vl]);
	makecbx5g(y, req2g5g[vl]);

	$('#' + id + 'channel_' + y).val(req2g5g[vl]);
}

function getACS(frm, toc) {
	var sThisVal = "";
	var cnd = 0;
	$('input:checkbox' + frm).each(function() {
		if (this.checked) {
			sThisVal += $(this).val() + " ";
			cnd++;
		}
	});
	var acslen = toc.replace("hid", "acs_len")
	$(toc).val(sThisVal);
	$(acslen).val(cnd)
	//console.log(acslen+'---'+cnd)

}
function sethw(x) {
	if (x == "AU" || x == "CZ" || x == "J1" || x == "JP") {
		$('#2gr_hwmode_0').val('11b');
		getModes('11b', 2, 0);
	}
}
function setacs(x) {
	y = $('#2gr_reg_0').val();
	if (x != "11b") {
		if (y == "J1" || y == "JP") {
			$('#2gr_acs_0_14').parent().hide();
		}
	} else {
		$('#2gr_acs_0_14').parent().show();
	}
}
var two;
function set_acl(val, y, x) {
	console.log('val ' + val + 'y ' + y + 'x ' + x);

	if (val == "ap") {
		// console.log("Device is in AP mode");
		if (x == 0) {
			//$('#2gi_acl_' + y).parent().parent().show();
			//Function call to Check mesh vap for 2G
			dcsGreyedOut_2G();
		} else {

			//$('#5gi_acl_' + y).parent().parent().show();
			//Function call to Check mesh vap for 5G
			dcsGreyedOut_5G();

		}
	} else {

		// console.log("Device is in mesh or station mode");
		if (x == 0) {
			//$('#2gi_acl_' + y).parent().parent().hide();
			$('#2gr_dcs_0').css('background', "lightgray");
			$('#2gr_dcs_0').css('pointer-events', "none");
			$('#2gi_hotspot_' + y).parent().parent().hide();
		} else {
			//$('#5gi_acl_' + y).parent().parent().hide();
			$('#5gr_dcs_0').css('background', "lightgray");
			$('#5gr_dcs_0').css('pointer-events', "none");
			$('#5gi_hotspot_' + y).parent().parent().hide();
		}
	}

	/*
	 * var cur_val = $('#2gi_mode_1').val(); //console.log(cur_val); if(cur_val ==
	 * "mesh"){ $('#root').val('yes'); } else { $('#root').val('no'); }
	 */

	if ($("#root").val() == "no") {
		var cur_interface = val;
		var num = y;
		if (num != 0 && cur_interface == "mesh") {
			$("#lan_dhcp_checkbox").prop("checked", true);
			$("#lan_only_checkbox").prop("checked", true);
			$('input[name=wan]').attr("disabled", true);
			$("#lan_checkbox_span").hide();
			$("#lan_checkbox_spanOne_dhcp").show();
			$('#lan_ipv4_dhcp_dns').val('8.8.8.8');
			$('#lan_ipv4_dhcp_dns1').val('8.8.4.4');
			$("#wan_checkbox_spanOne_dhcp").hide();
			$("#wan_checkbox_span").hide();
			$("#wan_checkbox_spanOne").hide();
			$('#lanLabel').css("pointer-events", "none");
		} else if (cur_interface == "ap") {
			$("#lan_static_checkbox").prop("checked", true);
			$("#lan_dhcp_checkbox").prop("checked", false);
			$("#lan_only_checkbox").prop("checked", false);
			$('input[name=wan]').attr("disabled", false);
			$("#wan_checkbox_spanOne_dhcp").show();
			$("#wan_ipv4_dhcp_dns").val('8.8.8.8');
			$("#wan_ipv4_dhcp_dns1").val('8.8.4.4');

		} else {
			$('input[name=wan]').attr("disabled", false);
			$("#lan_checkbox_span").hide();
			$('#lanLabel').css("pointer-events", "auto");
		}
	}

	console.log(">>>>>>>" + val + y);

}

function set_gVal(val, y, x) {
	cur_val_2g = $('#div2gi div').size();
	if (cur_val_2g == '104' && val != "ap") {
		$("#2gr_dcs_0").val('false');
	}
}

function set_fivegVal(val, y, x) {
	cur_val_5g = $('#div5gi div').size();
	if (cur_val_5g == '104' && val != "ap") {
		$("#5gr_dcs_0").val('false');
	}
}

$("#upload-file-selector").on('change', prepareLoad);
var files;
function prepareLoad(event) {
	files = event.target.files;
	var oMyForm = new FormData();
	oMyForm.append("file", files[0]);
	var url = "/facesix/rest/device/uploadconfig";
	var result = $.ajax({
		dataType : 'json',
		url : url,
		data : oMyForm,
		type : "POST",
		enctype : 'multipart/form-data',
		processData : false,
		contentType : false,
		success : function(result) {
			prefilldata(JSON.stringify(result));
		},
		error : function(result) {
		}
	});
}

/* function currentRoot() {

	//$('#'+main).children('id').each(function(){

	$('#div2gi').children('.tabgrp').each(function() {
		var two = $('.aclTwo', this).val();
		//console.log("vvv" + two)
		if (two == "mesh") {
			if ($("#root").val() == "yes") {
				$('input[name=wan]').attr("disabled", false);
				$("#lan_checkbox_span").show();
				$("#wan_dhcp_checkbox").prop("checked", true);
				$("#lan_checkbox_spanOne_dhcp").hide();
				$("#wan_checkbox_spanOne_dhcp").show();
				$("#lan_only_checkbox").prop("disabled", false);
				$("#lan_only_checkbox").prop("checked", false);
				$('#lanLabel').css("pointer-events", "auto");
				$('#wan_ipv4_dhcp_dns').val('8.8.8.8');
				$('#wan_ipv4_dhcp_dns1').val('8.8.4.4');
				$('#spanAddr').val('192.168.1.1');
				$('#spanMask').val('255.255.255.0');
			} else {
				$("#lan_dhcp_checkbox").prop("checked", true);
				$("#lan_only_checkbox").prop("checked", true);
				$('input[name=wan]').attr("disabled", true);
				$("#lan_checkbox_span").hide();
				$("#lan_checkbox_spanTwo").hide();
				$("#lan_checkbox_spanOne").hide();
				$("#lan_checkbox_spanOne_dhcp").show();
				$('#lan_ipv4_dhcp_dns').val('8.8.8.8');
				$('#lan_ipv4_dhcp_dns1').val('8.8.4.4');
				$("#wan_checkbox_spanOne_dhcp").hide();
				$("#wan_checkbox_span").hide();
				$("#wan_checkbox_spanOne").hide();
				$('#lanLabel').css("pointer-events", "none");
			}
		}

	})

	var lanOnlyCheckBox = $('#lan_only_checkbox').prop('checked');
	var bridgeGreyedOut = false;

	if (lanOnlyCheckBox) {
		bridgeGreyedOut = true;
		interface2g_bridge("lan", bridgeGreyedOut);
		interface5g_bridge("lan", bridgeGreyedOut);
	} else {
		//getting current root value

		if ($("#root").val() == "yes") {
			interface2g_bridge("lan", bridgeGreyedOut);
			interface5g_bridge("lan", bridgeGreyedOut);
		} else {
			interface2g_bridge("wan", bridgeGreyedOut);
			interface5g_bridge("wan", bridgeGreyedOut);
		}
	}

	$('#div5gi').children('.tabgrp').each(function() {
		console.log("lanOnlyChecking" + lanLabel)
		var two = $('.aclTwo', this).val();
		console.log("vvv" + two)
		if (two == "mesh") {
			if ($("#root").val() == "yes") {
				$('input[name=wan]').attr("disabled", false);
				$("#lan_checkbox_span").show();
				$("#wan_dhcp_checkbox").prop("checked", true);
				$("#lan_checkbox_spanOne_dhcp").hide();
				$("#wan_checkbox_spanOne_dhcp").show();
				$("#lan_only_checkbox").prop("disabled", false);
				$("#lan_only_checkbox").prop("checked", false);
				$('#lanLabel').css("pointer-events", "auto");
				$('#wan_ipv4_dhcp_dns').val('8.8.8.8');
				$('#wan_ipv4_dhcp_dns1').val('8.8.4.4');
				$('#spanAddr').val('192.168.1.1');
				$('#spanMask').val('255.255.255.0');
			} else {
				$("#lan_dhcp_checkbox").prop("checked", true);
				$("#lan_only_checkbox").prop("checked", true);
				$('input[name=wan]').attr("disabled", true);
				$("#lan_checkbox_span").hide();
				$("#lan_checkbox_spanTwo").hide();
				$("#lan_checkbox_spanOne").hide();
				$("#lan_checkbox_spanOne_dhcp").show();
				$('#lan_ipv4_dhcp_dns').val('8.8.8.8');
				$('#lan_ipv4_dhcp_dns1').val('8.8.4.4');
				$("#wan_checkbox_spanOne_dhcp").hide();
				$("#wan_checkbox_span").hide();
				$("#wan_checkbox_spanOne").hide();
				$('#lanLabel').css("pointer-events", "none");
			}
		}

	})

} */

function interfacefill(val, name, size) {
	if (name == "div5gi" && size == 0) {
		$('#5gi_index_0').val(val);
		$('#5gi_radio_local_index_0').val(0);
	} else if (name == "div5gi" && size != 0) {
		$('.curindex').val(val);
		$('#5gi_radio_local_index_1').val(1);
	} else if (name == "div5gione" && size == 0) {
		$('#5gione_index_0').val(val);
		$('#5gione_radio_local_index_0').val(0);
	} else if (name == "div5gione" && size != 0) {
		$('.curindexone').val(val);
		$('#5gione_radio_local_index_1').val(1);
	} else if (name == "div5gitwo" && size == 0) {
		$('#5gitwo_index_0').val(val);
		$('#5gitwo_radio_local_index_0').val(0);
	} else if (name == "div5gitwo" && size != 0) {
		$('.curindextwo').val(val);
		$('#5gitwo_radio_local_index_1').val(1);
	}

}

function interfacefilltwo(val, name, size) {
	if (name == "div2gi" && size == 0) {
		$('#2gi_index_0').val(val);
		$('#2gi_radio_local_index_0').val(0);
	} else if (name == "div2gi" && size != 0) {
		$('.twogcurindex').val(val);
		$('#2gi_radio_local_index_1').val(1);
	} else if (name == "div2gione" && size == 0) {
		$('#2gione_index_0').val(val);
		$('#2gione_radio_local_index_0').val(0);
	} else if (name == "div2gione" && size != 0) {
		$('.twogcurindexone').val(val + 1);
		$('#2gione_radio_local_index_1').val(1);
	} else if (name == "div2gitwo" && size == 0) {
		$('#2gitwo_index_0').val(val);
		$('#2gitwo_radio_local_index_0').val(0);
	} else if (name == "div2gitwo" && size != 0) {
		$('.twogcurindextwo').val(val + 1);
		$('#2gitwo_radio_local_index_1').val(1);
	}
}

function interfacefilltwofive(val, name, size) {
	if (name == "div2g5gi" && size == 0) {
		$('#2g5gi_index_0').val(val);
		$('#2g5gi_radio_local_index_0').val(0);
	} else if (name == "div2g5gi" && size != 0) {
		$('.tfcurindex').val(val);
		$('#2g5gi_radio_local_index_1').val(1);
	} else if (name == "div2g5gione" && size == 0) {
		$('#2g5gione_index_0').val(val);
		$('#2g5gione_radio_local_index_0').val(0);
	} else if (name == "div2g5gione" && size != 0) {
		$('.tfcurindexone').val(val + 1);
		$('#2g5gione_radio_local_index_1').val(1);
	} else if (name == "div2g5gitwo" && size == 0) {
		$('#2g5gitwo_index_0').val(val);
		$('#2g5gitwo_radio_local_index_0').val(0);
	} else if (name == "div2g5gitwo" && size != 0) {
		$('.tfcurindextwo').val(val + 1);
		$('#2g5gitwo_radio_local_index_1').val(1);
	}
}

function channelchange(val) {
	console.log("the channnel is" + val);
	if (val > 14) {

		$(".fc").show();
		$(".tc").hide();
		$(".fc").prop("disabled", false);
		$(".tc").prop("disabled", true);

	} else {

		$(".fc").hide();
		$(".tc").show();
		$(".tc").prop("disabled", false);
		$(".fc").prop("disabled", true);

	}

}

$(document).ready(function() {

					var max_fields = 100;
					var wrapper = $(".input_fields_wrap");
					add_button = $(".add_field_button");
					var remove_button = $(".remove_field_button");

					$(add_button)
							.click(
									function(e) {
										e.preventDefault();
										var total_fields = wrapper[0].childNodes.length;
										if (total_fields < max_fields) {
											var kr = inp++;
											$(wrapper)
													.append(
															'<div style="margin-top: -20px;">'
																	+ '<br/>'
																	+ '<input type="text" name="answer[]" class="field-long" id="input_'
																	+ kr
																	+ '" '
																	+ 'maxlength="17" onkeyup = "doInsert(this)"><button type="button" onClick="remove(this)" style="position:relative;left:25px;background: steelblue;color: white" >Remove</button></input>'
																	+ '</div>');
										}
									});

				});

function remove(argument) {
	console.log("yes");
	$(argument).parent().remove();
}

