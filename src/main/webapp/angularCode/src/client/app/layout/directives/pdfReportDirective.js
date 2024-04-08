(function () {
    'use strict';

    angular
        .module('app.layout')
        .directive('pdfReport', function () {
            return {
                restrict: 'E',
                scope: {
                    iframesrc: '='
                },
                controller: function ($scope) {
                    $scope.showSpinner = false;

                    //browser detection, if MS Edge then don't show spinner
                    if (/MSIE/i['test'](navigator['userAgent']) === true || /Edge/i['test'](navigator['userAgent']) === true) {
                        //do nothing for edge, EDGE doesn't support the onload event of iFrames which load PDF content.
                    } else {
                        $scope.$watch("iframesrc", function (newValue) {
                            if (newValue) {
                                $scope.showSpinner = true;
                            }
                        });

                        $('#reportIframe').on('load', function () {
                            $scope.showSpinner = false;
                            $scope.$apply();
                        });
                    }

                },
                template: '<div pdf-report-loader show-loader="showSpinner" ngplus-overlay-delay-in="30" ngplus-overlay-delay-out="30" ngplus-overlay-animation="dissolve-animation">' +
                    '<div class="model-spinner"></div>' +
                    '</div>' +
                    //'<div ng-hide="showSpinner" style="position: absolute;z-index: 1;text-align: center; vertical-align:middle;">Your report is loading...</div>' +
                    '<iframe id="reportIframe" ng-src="{{iframesrc}}" style="position:relative; width: 100%; height: 500px;border: solid 1px white;z-index: 2;">' +
                    '</iframe>'
            }
        });

}());
