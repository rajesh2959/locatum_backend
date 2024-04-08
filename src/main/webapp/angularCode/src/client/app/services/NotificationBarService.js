(function () {
    'use strict';

    angular
        .module('app')
        .factory('notificationBarService', service);

    service.$inject = ['notifications', 'messagingService'];

    function service(notifications, messagingService) {

        var svc = {};

        svc.success = function (message) {
            notifications.showSuccess(message);
        };

        svc.warning = function (message) {
            notifications.showWarning({
                message: message,
                hide: false
            });
        };

        svc.error = function (message) {
            notifications.showError({
                message: message,
                hide: false
            });
        };

        function closeAll() {
            notifications.closeAllErrors();
            notifications.closeAllWarnings();
        };

        function globalErrorEvent(event, args) {
            svc.error(args.errorMessage);
        }

        function globalWarningEvent(event, args) {
            svc.warning(args.message);
        }

        messagingService.listenGlobalErrorEvent(globalErrorEvent);

        messagingService.listenGlobalWarningEvent(globalWarningEvent);

        messagingService.listenGlobalClearErrorEvent(closeAll);

        return svc;
    }
})();
