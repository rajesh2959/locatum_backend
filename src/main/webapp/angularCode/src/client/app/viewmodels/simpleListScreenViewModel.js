(function () {
    'use strict';
    angular
        .module('app')
        .factory('SimpleListScreenViewModel', viewmodel);
    viewmodel.$inject = ['dateService'];

    /* @ngInject */
    function viewmodel(dateService) {
        var vm = function () {
            this.data = [];
            this.pagedData = [];
            this.filteredCount = null;
            this.fullCount = null;
            this.totalPageCount = 0;
            this.dataOperations = {
                search: null,
                paging: {
                    pageSize: 4,
                    currentPage: 1,
                    maxPagesToShow: 1,
                },
                sortPredicate: null,
                sortOrder: false
            };
            this.alphabetFilterAttributeName = null;
        };
        vm.prototype.filterFn = null;

        vm.prototype.setFilterSettings = function (filters) {
            this.dataOperations.search = filters.search;
            this.fromDate = filters.from;
            this.toDate = filters.to;
        };

        vm.prototype.getFilterSettings = function (filterSettings) {
            filterSettings.search = this.dataOperations.search;
            filterSettings.from = this.fromDate ? dateService.getFormattedMoment(this.fromDate) : null;
            filterSettings.to = this.toDate ? dateService.getFormattedMoment(this.toDate) : null;
        };

        vm.prototype.recordCountDescription = function () {
            if (this.filteredCount || this.fullCount) {
                if (this.filteredCount === this.fullCount) {
                    return '(' + this.filteredCount + ')';
                }
                else {
                    return '(' + this.filteredCount + ' of ' + this.fullCount + ')';
                }
            } else {
                return '';
            }
        };

        vm.prototype.currentPageDescription = function () {
            if (this.filteredCount || this.fullCount) {
                var page = (Math.floor(this.filteredCount / this.dataOperations.paging.pageSize));
                this.totalPageCount = (Math.floor(this.filteredCount % this.dataOperations.paging.pageSize) === 0) ? page : page + 1;
                var curPage = (this.dataOperations.paging.currentPage > this.totalPageCount) ? this.totalPageCount : this.dataOperations.paging.currentPage;
                return 'Showing ' + curPage + ' of ' + this.totalPageCount;
            }
            else {
                return '';
            }
        };

        //vm.prototype.goto = function (pageno) {
        //    var result = false;
        //    if (this.filteredCount || this.fullCount) {
        //        var page = (Math.floor(this.filteredCount / this.dataOperations.paging.pageSize));
        //        this.totalPageCount = (Math.floor(this.filteredCount % this.dataOperations.paging.pageSize) === 0) ? page : page + 1;
        //        return (parseInt(pageno) <= this.totalPageCount);
        //    }
        //};

        vm.prototype.refreshData = function () {
            this.getData(true);
        };

        vm.prototype.filterBy = function (vm) {
            return function (letter) {
                vm.dataOperations.search = null;
                vm.filterFn = function (datum) {
                    return datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith(letter.toLowerCase());
                };
                vm.getData();
            };
        };

        vm.prototype.filterByNumbers = function (vm) {
            return function () {
                vm.dataOperations.search = null;
                vm.filterFn = function (datum) {
                    return !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('a') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('b') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('c') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('d') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('e') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('f') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('g') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('h') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('i') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('j') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('k') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('l') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('m') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('n') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('o') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('p') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('q') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('r') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('s') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('t') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('u') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('v') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('w') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('x') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('y') &&
                        !datum[vm.alphabetFilterAttributeName].toLowerCase().startsWith('z');
                };

                vm.getData();
            };
        };

        vm.prototype.clearFilterBy = function () {
            this.filterFn = null;
            this.dataOperations.search = null;
            this.getData();
        };

        return vm;
    }

})();