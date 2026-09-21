// AyeshaMart - global JS
(function () {
    "use strict";

    var alerts = document.querySelectorAll(".alert");
    for (var i = 0; i < alerts.length; i++) {
        (function (el) {
            setTimeout(function () {
                el.style.transition = "opacity 0.6s ease";
                el.style.opacity = "0";
                setTimeout(function () {
                    if (el.parentNode) {
                        el.parentNode.removeChild(el);
                    }
                }, 600);
            }, 5000);
        })(alerts[i]);
    }
})();