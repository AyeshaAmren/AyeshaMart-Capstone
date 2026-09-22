// AyeshaMart - mock payment page (Phase 5/7)
// Toggles the visible payment-detail fields as the buyer changes method.
(function () {
    "use strict";

    var form = document.getElementById("payment-form");
    if (!form) {
        return;
    }

    var radios = form.querySelectorAll("input[name='paymentMethod']");
    var fieldsByMethod = {
        "UPI": document.getElementById("fields-UPI"),
        "GPAY": document.getElementById("fields-GPAY"),
        "CARD": document.getElementById("fields-CARD"),
        "COD": document.getElementById("fields-COD")
    };

    function show(method) {
        Object.keys(fieldsByMethod).forEach(function (key) {
            fieldsByMethod[key].hidden = key !== method;
        });
    }

    radios.forEach(function (radio) {
        radio.addEventListener("change", function () {
            show(radio.value);
        });
    });
})();