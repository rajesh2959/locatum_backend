(function () {
    'use strict';

    //Directive that uses the httpInterceptor factory above to monitor XHR calls
    //When a call is made it displays an overlay and a content area
    //No attempt has been made at this point to test on older browsers
    angular
    .module('app.layout')
    .directive('pdfReportLoader', ['$q', '$timeout', '$window', overlay]);

    function overlay($q, $timeout, $window) {
        var directive = {
            scope: {
                showLoader: '=',
                ngplusOverlayDelayIn: "@",
                ngplusOverlayDelayOut: "@",
                ngplusOverlayAnimation: "@"
            },
            restrict: 'EA',
            transclude: true,
            template: getTemplate(),
            link: link
        };
        return directive;

        function getTemplate() {
            return '<div id="ngplus-overlay-container" ' +
                'class="{{ngplusOverlayAnimation}}" data-ng-show="!!show">' +
                '<div class="ngplus-overlay-background"></div>' +
                '<div id="ngplus-overlay-content" class="ngplus-overlay-content" data-ng-transclude>' +
                '</div>' +
                '</div>';
        }

        function link(scope, element, attrs) {
            var defaults = {
                overlayDelayIn: 500,
                overlayDelayOut: 500
            };
            var delayIn = scope.ngplusOverlayDelayIn ? scope.ngplusOverlayDelayIn : defaults.overlayDelayIn;
            var delayOut = scope.ngplusOverlayDelayOut ? scope.ngplusOverlayDelayOut : defaults.overlayDelayOut;
            var overlayContainer = null;
            var queue = [];
            var timerPromise = null;
            var timerPromiseHide = null;

            init();

            function init() {
                scope.$watch('showLoader', function() {
                    if (scope.showLoader) {
                        switchLoaderOn();
                    } else {
                        switchLoaderOff();
                    }
                });
                overlayContainer = document.getElementById('ngplus-overlay-container');
            }

            function switchLoaderOn() {
                queue.push({});
                if (queue.length == 1) {
                    timerPromise = $timeout(function () {
                        if (queue.length) showOverlay();
                    }, delayIn); //Delay showing for 500 millis to avoid flicker
                }
            }

            function switchLoaderOff() {
                queue.pop();
                if (queue.length == 0) {
                    //Since we don't know if another XHR request will be made, pause before
                    //hiding the overlay. If another XHR request comes in then the overlay
                    //will stay visible which prevents a flicker
                    timerPromiseHide = $timeout(function () {
                        //Make sure queue is still 0 since a new XHR request may have come in
                        //while timer was running
                        if (queue.length == 0) {
                            hideOverlay();
                            if (timerPromiseHide) $timeout.cancel(timerPromiseHide);
                        }
                    }, delayOut);
                }
            }

            function showOverlay() {
                var w = 0;
                var h = 0;
                if (!$window.innerWidth) {
                    if (!(document.documentElement.clientWidth == 0)) {
                        w = document.documentElement.clientWidth;
                        h = document.documentElement.clientHeight;
                    }
                    else {
                        w = document.body.clientWidth;
                        h = document.body.clientHeight;
                    }
                }
                else {
                    w = $window.innerWidth;
                    h = $window.innerHeight;
                }
                var content = document.getElementById('ngplus-overlay-content');
                var contentWidth = parseInt(getComputedStyle(content, 'width').replace('px', ''));
                var contentHeight = parseInt(getComputedStyle(content, 'height').replace('px', ''));

                content.style.top = h / 2 - contentHeight / 2 + 'px';
                content.style.left = w / 2 - contentWidth / 2 + 'px';

                scope.show = true;
            }

            function hideOverlay() {
                if (timerPromise) $timeout.cancel(timerPromise);
                scope.show = false;
            }

            var getComputedStyle = function () {
                var func = null;
                if (document.defaultView && document.defaultView.getComputedStyle) {
                    func = document.defaultView.getComputedStyle;
                } else if (typeof (document.body.currentStyle) !== "undefined") {
                    func = function (element, anything) {
                        return element["currentStyle"];
                    };
                }

                return function (element, style) {
                    return func(element, null)[style];
                }
            }();
        }
    }
}());