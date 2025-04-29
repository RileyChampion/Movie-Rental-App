/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

let TOTAL = 0;

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function updateCart(i) {
    let updateRequest = jQuery("#movie-title-"+i);
    let updateQuantity = jQuery("#movie-quantity-"+i);
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "PUT", // Setting request method
        url: "api/cart?movie-title=" + updateRequest[0].innerHTML + "&movie-quantity=" + updateQuantity[0].firstElementChild.value,
        success: (resultData) => {alert(updateRequest[0].innerHTML + " quantity has been updated to " + updateQuantity[0].firstElementChild.value + "!")}
    });
    location.reload(); //reloads page
}

function deleteMovie(i) {
    let deleteRequest = jQuery("#movie-title-"+i);
    jQuery.ajax({
        dataType: "json", // Setting return data type
            method: "DELETE", // Setting request method
            url: "api/cart?movie-title=" + deleteRequest[0].innerHTML,
            success: (resultData) => {alert(deleteRequest[0].innerHTML + " has been deleted!")}
    });
    location.reload(); //reloads page
}

function proceedToPurchase() {
    window.location.replace('check-out.html');
}

function handleCartResult(resultData) {
    console.log("handleStarResult: populating shopping cart from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let cartTableBodyElement = jQuery("#shopping_cart_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData['shoppingCartItems'].length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<td id='movie-title-"+i+"'>"+
            // Add a link to single-star.html with id passed with GET url parameter
            //resultData = {"shoppingCartItems:[{"movie-title": string, "quantity": 1}]"}
            resultData['shoppingCartItems'][i]["movie-title"] +     // display star_name for the link text
            "</td>";
        rowHTML += "<td id='movie-quantity-"+i+"'>" + "<input type='text' value='" + resultData['shoppingCartItems'][i]["quantity"]+"'>"+ "</td>";
        rowHTML += "<td id='movie-price-"+i+"'>" + "$1.00" + "</td>";
        rowHTML += "<td id='movie-options-"+i+"'>" + "<button type=\"button\"onclick=\"return updateCart(" + i + ")\">Update Cart</button>" +
            "<button type=\"button\"onclick=\"return deleteMovie( " + i+ ")\">Delete Movie</button>" + "</td>";
        //For loop through all the genres
        //rowHTML += "<td>";

        //rowHTML += "</td>"
        //For loop through all the stars
        //rowHTML += "<td>";
        //rowHTML += "</td>"
        rowHTML += "</tr>";
        TOTAL += resultData['shoppingCartItems'][i]["quantity"]; //quantity * $1

        // Append the row created to the table body, which will refresh the page
        cartTableBodyElement.append(rowHTML);
    }
    let totalElement = jQuery("#shoppingCart-total");
    let total = "<p>" +
        "Total: $" + TOTAL + ".00" + "</p>";
    totalElement.append(total);

    let buttonContainer = jQuery(".proceed-button-container")
    let proceedButton = "";
    if(TOTAL > 0) {
        proceedButton += "<button type='submit'  onclick='return proceedToPurchase()'>Proceed to Checkout</button>";
    }
    else {
        proceedButton += "<button type='submit' onclick='return proceedToPurchase()' disabled='true'>Proceed to Checkout</button>";
    }
    buttonContainer.append(proceedButton)
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/cart",
    success: (resultData) => handleCartResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});
// else {
//     jQuery.ajax({
//         dataType: "json", // Setting return data type
//         method: "GET", // Setting request method
//         url: "api/movies?movie-search=" + movieid, // Setting request url, which is mapped by StarsServlet in Stars.java
//         success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
//     });
// }
