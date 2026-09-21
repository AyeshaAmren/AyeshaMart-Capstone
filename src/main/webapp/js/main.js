// AyeshaMart - global JS
(function () {
    "use strict";

    // hide the server-rendered loading overlay once the page is interactive
    var loading = document.querySelector(".page-loading");
    if (loading) {
        requestAnimationFrame(function () {
            loading.classList.add("loaded");
        });
    }

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