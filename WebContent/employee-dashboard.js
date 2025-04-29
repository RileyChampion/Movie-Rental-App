let add_movie_form = $('#add-movie-form');
let add_star_form = $('#add-star-form');
let metadata_tab = $('#metadata-tab');


function addMovie(formSubmitEvent) {

    formSubmitEvent.preventDefault();

    $.ajax(
        "api/add-movie", {
            dataType: "json",
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: add_movie_form.serialize(),
            success: (s) => {
                if(s["status"] == "Success") {
                    alert('Movie has been added.');
                }
                else {
                    alert('Movie already exists.');
                }
            }
        }
    );
}

function addMovieStar(formSubmitEvent) {

    formSubmitEvent.preventDefault();

    $.ajax(
        "api/add-star", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: add_star_form.serialize(),
            success: () => {
                alert('Star has been added.');
            }
        }
    );
}

function populateMetadataHTML(resultsData) {
    let res = resultsData["result"];

    let metaContainer = $('#metadata-container');

    for(let i = 0; i < res.length; i++) {
        let rowHTML = "<div class='metadata-container-item'>"

        rowHTML += "<h4>Table Name: " + res[i]["tableName"] + "</h4>"

        rowHTML += "<h5>Columns:</h5>"

        for(let j = 0; j < res[i]["columns"].length; j++) {
            rowHTML += "<div class='metadata-container-item-column'>"
            rowHTML += res[i]["columns"][j]["colName"] + " : " + res[i]["columns"][j]["colType"]
            rowHTML += "</div>"
        }

        rowHTML += "</div>"
        metaContainer.append(rowHTML);
    }
}


add_movie_form.submit(addMovie);
add_star_form.submit(addMovieStar);
metadata_tab.click(() => {
    $.ajax("api/moviedb-metadata", {
        dataType: "json",
        method: "GET",
        success: (resultData) => populateMetadataHTML(resultData)
    })
});