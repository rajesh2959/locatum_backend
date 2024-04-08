(function () {
    'use strict';

    angular
        .module('app.layout')
        .directive('alphabetFilter', function () {
            return {
                restrict: 'E',
                transclude: false,
                replace: true,
                scope: {
                    filterByNumbers: '&',
                    filterBy: '&',
                    clearFilterBy: '&'
                },
                template: '<p class="alphabetFilter">' +
                            '<a href="" ng-click="filterByNumbers()()">#</a>' +
                            '<a href="" ng-click="filterBy()(\'a\')">A</a>' +
                            '<a href="" ng-click="filterBy()(\'b\')">B</a>' +
                            '<a href="" ng-click="filterBy()(\'c\')">C</a>' +
                            '<a href="" ng-click="filterBy()(\'d\')">D</a>' +
                            '<a href="" ng-click="filterBy()(\'e\')">E</a>' +
                            '<a href="" ng-click="filterBy()(\'f\')">F</a>' +
                            '<a href="" ng-click="filterBy()(\'g\')">G</a>' +
                            '<a href="" ng-click="filterBy()(\'h\')">H</a>' +
                            '<a href="" ng-click="filterBy()(\'i\')">I</a>' +
                            '<a href="" ng-click="filterBy()(\'j\')">J</a>' +
                            '<a href="" ng-click="filterBy()(\'k\')">K</a>' +
                            '<a href="" ng-click="filterBy()(\'l\')">L</a>' +
                            '<a href="" ng-click="filterBy()(\'m\')">M</a>' +
                            '<a href="" ng-click="filterBy()(\'n\')">N</a>' +
                            '<a href="" ng-click="filterBy()(\'o\')">O</a>' +
                            '<a href="" ng-click="filterBy()(\'p\')">P</a>' +
                            '<a href="" ng-click="filterBy()(\'q\')">Q</a>' +
                            '<a href="" ng-click="filterBy()(\'r\')">R</a>' +
                            '<a href="" ng-click="filterBy()(\'s\')">S</a>' +
                            '<a href="" ng-click="filterBy()(\'t\')">T</a>' +
                            '<a href="" ng-click="filterBy()(\'u\')">U</a>' +
                            '<a href="" ng-click="filterBy()(\'v\')">V</a>' +
                            '<a href="" ng-click="filterBy()(\'w\')">W</a>' +
                            '<a href="" ng-click="filterBy()(\'x\')">X</a>' +
                            '<a href="" ng-click="filterBy()(\'y\')">Y</a>' +
                            '<a href="" ng-click="filterBy()(\'z\')">Z</a>' +
                            '<a href="" ng-click="clearFilterBy()">ALL</a>' +
                        '</p>'
            };
        });

}());



