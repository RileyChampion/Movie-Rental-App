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

function handleStarResult(resultData) {
    console.log("handleStarResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData['results'].length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<td id='movie-title-" + i + "'>" +
            // Add a link to single-star.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData['results'][i]['movie_id'] + '">'
            + resultData['results'][i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</td>";
        rowHTML += "<td id='movie-year-" + i + "'>" + resultData['results'][i]["movie_year"] + "</td>";
        rowHTML += "<td id='movie-director-" + i + "'>" + resultData['results'][i]["movie_director"] + "</td>";
        //For loop through all the genres
        rowHTML += "<td id='movie-genres-" + i + "'>";

        for(let j = 0; j < resultData['results'][i]["movie_genres"].length; j++) {
            rowHTML += "<div class='pill-container'><span class='pill'>" +
                        "<a href='index.html?type=genre&text=" + resultData['results'][i]["movie_genres"][j]["movie_genre"] + "&N=10&sorting-option1=rating&sorting-option2=&order=DESC&page=1'>" + resultData['results'][i]["movie_genres"][j]["movie_genre"] + "</a>" +
                        "</span></div>"
            // rowHTML +=  ""   "+ resultData['results'][i]["movie_genres"][j]["movie_genre"] + "</span></div>";
        }
        rowHTML += "</td>"
        //For loop through all the stars
        rowHTML += "<td id='movie-stars-" + i + "'>";
        for(let j = 0; j < resultData['results'][i]["movie_stars"].length; j++) {
            rowHTML += '<p><a href="single-star.html?id=' + resultData['results'][i]["movie_stars"][j]["star_id"] + '">'
                + resultData['results'][i]["movie_stars"][j]["star_name"] +     // display star_name for the link text
                '</a></p>';
        }
        rowHTML += "</td>"
        rowHTML += "<td id='movie-rating-" + i + "'>" + resultData['results'][i]["movie_rating"] + "</td>";
        rowHTML += "<td id='movie-add-"+i+"'>" + "<button type=\"button\"onclick=\"return addToCart( " + i + ")\">Add to Cart</button>" + "</td>"
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);

        //Create buttons to align with content
        if(moviePage == 1) {
            $('#previous').attr("disabled", true);
        }
        else {
            $('#previous').attr("disabled", false);
        }

        if(resultData['hasNext']) {
            $('#next').attr("disabled", false);
        }
        else {
            $('#next').attr("disabled", true);
        }
    }
}


function addToCart(i) {

    let addToCartElement = jQuery("#movie-title-"+i);

    jQuery.ajax({
        method: "POST",// Setting request method
        url: "api/cart?movie-title=" + addToCartElement[0].firstElementChild.innerHTML, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => {
            alert(addToCartElement[0].firstElementChild.innerHTML + " has been added to your shopping cart!")
        }
        // Setting callback function to handle data returned successfully by the SingleStarServlet
    });

    return false;
}

function previousButton() {
    window.location.href = 'index.html?type=' + movieType +
                            "&text=" + movieTextSearch +
                            "&N=" + movieLimit +
                            "&sorting-option1=" + sortingOption1 +
                            "&sorting-option2=" + sortingOption2 +
                            "&order=" + sortingOrder +
                            "&page=" + (parseInt(moviePage) - 1);
    return false;
}

function nextButton() {
    window.location.href = 'index.html?type=' + movieType +
                                "&text=" + movieTextSearch +
                                "&N=" + movieLimit  +
                                "&sorting-option1=" + sortingOption1 +
                                "&sorting-option2=" + sortingOption2 +
                                "&order=" + sortingOrder +
                                "&page=" + (parseInt(moviePage) + 1);
    return false;
}

function changeLimitNumber() {
    let limitNumber = document.getElementsByName('limit-number')[0].value;
    let sortOption1 = document.getElementsByName('sort-choice1')[0].value;
    let sortOption2 = document.getElementsByName('sort-choice2')[0].value;
    let sortOption = document.getElementsByName('sort-direction')[0].value;
    window.location.href = 'index.html?type=' + movieType +
                                "&text=" + movieTextSearch +
                                "&N=" + limitNumber +
                                "&sorting-option1=" + sortOption1 +
                                "&sorting-option2=" + sortOption2 +
                                "&order=" + sortOption +
                                "&page=" + moviePage;
    return false;
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

let movieType = getParameterByName('type');
let movieTextSearch = getParameterByName('text');
let movieLimit = getParameterByName('N');
let sortingOption1 = getParameterByName('sorting-option1');
let sortingOption2 = getParameterByName('sorting-option2');
sortingOption2 = (sortingOption2 === " " ? "" : sortingOption2);
let sortingOrder = getParameterByName('order');
let moviePage = getParameterByName('page');
let hasNext = true;

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies?type=" + movieType +
        "&text=" + movieTextSearch +
        "&N=" + movieLimit  +
        "&sorting-option1=" + sortingOption1 +
        "&sorting-option2=" + sortingOption2 +
        "&order=" + sortingOrder +
        "&page=" + moviePage, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

if(window.localStorage.getItem("movie-results")) {
    if(window.localStorage.getItem("movie-results") == window.location.href) {
    }
    else {
        window.localStorage.setItem("movie-results" , window.location.href)
    }

}
else {
    window.localStorage.setItem("movie-results" , window.location.href)
}
// else {
//     jQuery.ajax({
//         dataType: "json", // Setting return data type
//         method: "GET", // Setting request method
//         url: "api/movies?movie-search=" + movieid, // Setting request url, which is mapped by StarsServlet in Stars.java
//         success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
//     });
// }
