

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

let movie_title = ""

function handleResult(resultData) {

    console.log("handleResult: populating movie info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#movie_info");

    // append two html <p> created to the h3 body, which will refresh the page
    starInfoElement.append("<p><strong>Movie Title:</strong> " + resultData[0]["movie_title"] + "</p>" +
        "<p><strong>Release Year:</strong> " + resultData[0]["movie_year"] + "</p>" +
        "<p><strong>Director:</strong> " + resultData[0]["movie_director"] +"</p>" +
        "<p><strong>Rating:</strong> " + resultData[0]["movie_rating"] + "</p>");
    movie_title = resultData[0]["movie_title"]

    console.log("handleResult: populating genres table from resultData");

    let genresInfoElement = jQuery("#movie_genres");
    console.log(resultData);
    for(let i = 0; i < resultData[0]["movie_genres"].length; i++) {
        genresInfoElement.append(
            "<p class='pill'>" + resultData[0]["movie_genres"][i]["movie_genre"] + "</p>"
        )
    }

    console.log("handleResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let starTableBodyElement = jQuery("#star_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData[0]["movie_stars"].length; i++) {
        let rowHTML = "";
        rowHTML += '<tr>';
        rowHTML += "<td>" + '<a href="single-star.html?id=' + resultData[0]["movie_stars"][i]["star_id"] + '">'
                    + resultData[0]["movie_stars"][i]["star_name"] +     // display star_name for the link text
                    '</a>' + "</td>";
        rowHTML += "<td>" + (resultData[0]["movie_stars"][i]["star_dob"] !== null ? resultData[0]["movie_stars"][i]["star_dob"] : 'N/A') + "</td>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}

function returnToMovieList() {
    location.replace(localStorage.getItem("movie-results"))
}

function addToCart() {
    jQuery.ajax({
        method: "POST",// Setting request method
        url: "api/cart?movie-title=" + movie_title, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => {
            alert(movie_title + " has been added to your shopping cart!")
        }
        // Setting callback function to handle data returned successfully by the SingleStarServlet
    });

    return false;
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

