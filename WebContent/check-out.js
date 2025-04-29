let purchase_form = jQuery('#user-information-form');

function purchase(purchaseForm) {

    console.log(purchaseForm)

    purchaseForm.preventDefault();

    jQuery.ajax('api/purchase', {
        dataType: "json",
        method: "POST",
        data: $( this ).serialize(),
        success: (resultData) => {
            window.location.replace("confirmation.html");
        },
        error: (e) => {
            alert(e["message"]);
        }
    })

}


purchase_form.submit(purchase);