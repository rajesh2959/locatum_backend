(function () {
    'use strict';

    angular
        .module('app.layout')
        .controller('PdfReportModalController', controller);

    controller.$inject = ['$uibModalInstance', 'reportName', 'pdfUrl'];
    /* @ngInject */
    function controller($uibModalInstance, reportName, pdfUrl) {

        var vm = this;
        vm.reportName = reportName;
        vm.pdfUrl = pdfUrl;
        vm.ok = function () {
            $uibModalInstance.close();
        };

        activate();

        function activate() {
        }

        return vm;
    }
})();
