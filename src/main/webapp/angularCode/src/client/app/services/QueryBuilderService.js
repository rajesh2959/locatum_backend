(function () {
    'use strict';

    angular
        .module('app')
        .factory('queryBuilderService', service);

    function service() {

        var svc = {};

        svc.getQueryUrl = function (url, params) {
            var qs = 'timestamp=' + new Date().getTime() + '&';
            for (var key in params) {
                var value = params[key];
                qs += encodeURIComponent(key) + "=" + encodeURIComponent(value) + '&';
            }
            if (qs.length > 0) {
                qs = qs.substring(0, qs.length - 1);
                url = url + '?' + qs;
            }
            return url;
        };

        return svc;
    }
})();
