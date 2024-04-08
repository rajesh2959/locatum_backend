(function () {
    'use strict';

    angular
        .module('app')
        .factory('dataService', service);

    service.$inject = ['$q', '$http', '$linq', '$sce', 'queryBuilderService', 'session', 'environment', 'FileUploader', '$location'];

    function service($q, $http, $linq, $sce, queryBuilderService, session, env, fileUploader, $location) {

        var svc = {};
        var cache = {};
        var baseUrl = env.serverBaseUrl;
        var api_auth_key = env.api_auth_key;
        var api_auth_key_header = 'api_auth_key';

        svc.getFileUploaderInstance = function (route) {
            return new fileUploader({
                url: baseUrl + route,
                headers: {
                    Authorization: 'Bearer ' + session.accessToken,
                    api_auth_key: api_auth_key
                }
            });
        };

        svc.getFileUploaderInstanceWithData = function (route) {
            return new fileUploader({
                url: baseUrl + route,
                formData: [],
                headers: {
                    Authorization: 'Bearer ' + session.accessToken,
                    api_auth_key: api_auth_key
                }
            });
        };

        svc.clearCache = function () {
            cache = {};
        };

        svc.clearRouteCache = function (route) {
            cache[route] = null;
        };

        svc.postData = function (route, data) {
            var start = moment(); // jshint ignore:line
            return $http.post(baseUrl + route, data).then(function (result) {
                return result.data;
            });
        };

        svc.postMultipart = function (route, data) {
            return $http.post(baseUrl + route,
                data,
                {
                    headers: { 'Content-Type': undefined },
                    enctype: 'multipart/form-data',
                    processData: false,
                    contentType: false
                }
            ).then(function (result) {
                return result.data;
            });
        };

        svc.postFilepart = function (route, data) {
            return $http.post(baseUrl + route,
                data,
                {
                    headers: { 'Content-Type': undefined },
                    // enctype: 'multipart/form-data',
                    // processData: true,
                    // contentType: true
                }
            ).then(function (result) {
                return result.data;
            });
        };

        svc.getImagepart = function (route, data) {
            return $http.get(baseUrl + route,
                data,
                {
                    headers: { 'Content-Type': undefined },
                    // enctype: 'multipart/form-data',
                    // processData: true,
                    // contentType: true
                }
            ).then(function (result) {
                return result.data;
            });
        };

        svc.getformData = function (obj) {
            var str = [];
            for (var p in obj)
                str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
            return str.join("&");
        };

        svc.post = function (route, data) {

            var start = moment();// jshint ignore:line
            return $http
                .post(baseUrl + route,
                    svc.getformData(data),
                    { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
                .then(function (res) {
                    var end = moment();// jshint ignore:line
                    if (angular.isDefined(console)) console.log(route + ' took: ' + Math.round(end - start) + ' milliseconds, from: ' + start.format('h:mm:ss.SSS') + ' to: ' + end.format('h:mm:ss.SSS'));
                    return res.data;
                });
        };

        svc.delete = function (route) {
            var start = moment(); // jshint ignore:line
            return $http.delete(baseUrl + route).then(function (result) {
                consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                return result.data;
            });
        };

        svc.deletemultiple = function (route, data) {
            var start = moment(); // jshint ignore:line
            return $http.delete(baseUrl + route, data).then(function (result) {
                    consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                    return result.data;
                });
        };


        svc.getReport = function (route) {
            var authTokenParam = [];
            authTokenParam["authtoken"] = session.accessToken;
            authTokenParam[api_auth_key_header] = api_auth_key;
            return $q.when($sce.trustAsResourceUrl(queryBuilderService.getQueryUrl(baseUrl + route, authTokenParam)));
        };

        svc.getReportWithParams = function (route, paramValues) {
            paramValues["authtoken"] = session.accessToken;
            paramValues[api_auth_key_header] = api_auth_key;
            return $q.when($sce.trustAsResourceUrl(queryBuilderService.getQueryUrl(baseUrl + route, paramValues)));
        };

        svc.getRecord = function (route) {
            var start = moment(); // jshint ignore:line
            var params = {
                hideOverlay: false
            }
            if($location.path().indexOf('/gatewayinfo') > -1) {
             params.hideOverlay = true;
            }
            return $http.get(baseUrl + route, params).then(function (result) {
                consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                return result.data;
            });
        };

        svc.makeGETRequestWithoutLoading = function (url) {
            return $q(function (resolve, reject) {
                var xhr;
                if (window.XMLHttpRequest) {
                    // code for modern browsers
                    xhr = new XMLHttpRequest();
                } else {
                    // code for old IE browsers
                    xhr = new ActiveXObject("Microsoft.XMLHTTP");
                }
                xhr.open('GET', baseUrl + url, true);
                xhr.onload = function () {
                    if (this.status >= 200 && this.status < 300) {
                        resolve(xhr.response);
                    } else {
                        reject({
                            status: this.status,
                            statusText: xhr.statusText
                        });
                    }
                };
                xhr.onerror = function () {
                    reject({
                        status: this.status,
                        statusText: xhr.statusText
                    });
                };
                xhr.send();
            });
        };

        svc.getRecordWithParams = function (route, paramValues) {
            var start = moment(); // jshint ignore:line
            return $http.get(baseUrl + route, { params: paramValues }).then(function (result) {
                consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                return result.data;
            });
        };

        svc.getLookupData = function (route, refresh) {
            var paramValues = {
                t: moment().millisecond()
            }
            return svc.getLookupDataWithParams(route, paramValues, refresh);
        };

        svc.getLookupDataWithParams = function (route, paramValues, refresh) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(cache[route]);
            } else {
                var start = moment(); // jshint ignore:line
                var params = paramValues ? { params: paramValues } : null;
                return $http.get(baseUrl + route, params).then(function (result) {
                    consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                    cache[route] = result.data;
                    return result.data;
                });
            }
        };

        svc.getDataWithParams = function (route, paramValues, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                paramValues.t = start.millisecond();
                return $http.get(baseUrl + route, { params: paramValues })
                    .then(function (result) {
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        cache[route] = result.data;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };

        svc.getData = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        cache[route] = result.data;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };

        svc.getDataListForTable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        result.data = appendSno(result.data);
                        cache[route] = result.data;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };


        svc.getRoleDataListForTable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        //result.data = appendSno(result.data);
                        cache[route] = result.data;
                        return result;
                    });
            }
        };

        svc.getDataInactiveAlertsListForTable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        result.data.inactivetags = appendSno(result.data.inactivetags);
                        cache[route] = result.data.inactivetags;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };

        //svc.getTaglistfortable = function (route, refresh, dataOperations, filterFn) {
        //    if (dataForRouteIsCached(route, refresh)) {
        //        return $q.when(getPagedData(cache[route], dataOperations, filterFn));
        //    } else { //no cached data or refresh requested
        //        var start = moment(); // jshint ignore:line
        //        return $http.get(baseUrl + route)
        //            .then(function (result) {
        //                consoleLogRequestTime(route, start, moment()); // jshint ignore:line
        //                result.data = appendSno(result.data);
        //                cache[route] = result.data;
        //                return getPagedData(cache[route], dataOperations, filterFn);
        //            });
        //    }
        //};

        svc.getGwayReceiverListfortable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        //result.data = result.data.blereceiver
                       /* result.data = $linq.Enumerable().From(result.data)
                            .OrderBy(function (x) { return x.mac_address })
                            .ToArray();*/
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        result.data = appendSno(result.data);
                        cache[route] = result.data;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };

        svc.getGwayServerListfortable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        //result.data = result.data.bleserver
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        result.data = appendSno(result.data);
                        cache[route] = result.data;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };

        svc.getInUseTaglistfortable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        result.data = result.data.checkedout
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        result.data = appendSno(result.data);
                        cache[route] = result.data;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };


        svc.getAccesslistfortable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        result.data = result.data.support
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        result.data = appendSno(result.data);
                        cache[route] = result.data;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };

        svc.getTagDashboardlistfortable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        result.data = result.data.bottleneck
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        result.data = appendSno(result.data);
                        cache[route] = result.data;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };

        svc.getDataBatteryAlertsListForTable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        result.data.beaconbattery = appendSno(result.data.beaconbattery);
                        cache[route] = result.data.beaconbattery;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };

        svc.getDataGatewayAlertsListForTable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        result.data.beacondevicealert = appendSno(result.data.beacondevicealert);
                        cache[route] = result.data.beacondevicealert;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };

        svc.downloadAsPDF = function (route) {
            var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route,{ responseType: 'arraybuffer' })
                    .then(function (result) {
                        var blob=new Blob([result.data],{type:"application/pdf"});
                        var link=document.createElement('a');
                        link.href=window.URL.createObjectURL(blob);
                        link.download="Tag_Report.pdf";
                        link.click();
                });
        };

		svc.getLicenseCustomersListForTable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        result.data.licence = appendSno(result.data.licence);
                        cache[route] = result.data.licence;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };
        
        svc.getInactiveCustomersListForTable = function (route, refresh, dataOperations, filterFn) {
            if (dataForRouteIsCached(route, refresh)) {
                return $q.when(getPagedData(cache[route], dataOperations, filterFn));
            } else { //no cached data or refresh requested
                var start = moment(); // jshint ignore:line
                return $http.get(baseUrl + route)
                    .then(function (result) {
                        consoleLogRequestTime(route, start, moment()); // jshint ignore:line
                        result.data.inactivecustomer = appendSno(result.data.inactivecustomer);
                        cache[route] = result.data.inactivecustomer;
                        return getPagedData(cache[route], dataOperations, filterFn);
                    });
            }
        };
        
        function appendSno(dataList) {
            var gridsno = 1;
            return angular.forEach(dataList, function (v, k) {
                v.gridsno = gridsno++;
            });
        }

        function getPagedData(data, dataOperations, filterFn) {
            var take = dataOperations.paging.pageSize;
            var skip = dataOperations.paging.currentPage ? (dataOperations.paging.currentPage - 1) * dataOperations.paging.pageSize : 0;
            var filteredData = $linq.Enumerable().From(data).Where(filterFn);
            var sortedData;
            //var sortFn = function (datum) {
            //    if (datum[dataOperations.sortPredicate])
            //        return datum[dataOperations.sortPredicate].toLowerCase;
            //    else
            //        return datum[dataOperations.sortPredicate];
            //};
            if (dataOperations.sortPredicate) {
                console.log(JSON.stringify(filteredData.ToArray()));
                console.log(dataOperations.sortPredicate);
                var sortingPredicate = "$." + dataOperations.sortPredicate;
                sortedData = (dataOperations.sortOrder) ? $linq.Enumerable().From(filteredData.ToArray()).OrderBy(sortingPredicate)
                    : $linq.Enumerable().From(filteredData.ToArray()).OrderByDescending(sortingPredicate);
            } else {
                sortedData = filteredData;
            }

            return {
                allData: data,
                pagedData: (sortedData && sortedData.ToArray().length <= skip) ? sortedData.ToArray() : sortedData.Skip(skip).Take(take).ToArray(),
                filteredDataCount: filteredData.Count(),
                dataCount: data.length
            };
        }

        function consoleLogRequestTime(action, start, end) {
            if (angular.isDefined(console))
                console.log(action + ' took: ' + Math.round(end - start) + ' milliseconds, from: ' + start.format('h:mm:ss.SSS') + ' to: ' + end.format('h:mm:ss.SSS'));
        }

        function dataForRouteIsCached(route, refresh) {
            return cache[route] && (refresh === false || refresh == undefined);
        }

        return svc;
    }
})();