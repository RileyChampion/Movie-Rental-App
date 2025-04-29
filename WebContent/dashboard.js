$('.form-control').focus(() => {
    $('.fabflix-submit-button').addClass('fabflix-submit-button-focus');
});
$('.form-control').focusout(() => {
    if(!($('.form-control')[0].validity.valid)) {
        $('.fabflix-submit-button').removeClass('fabflix-submit-button-focus');
    }
    else {
        $('.fabflix-submit-button').addClass('fabflix-submit-button-focus');
    }
});

function searchForMovie() {
    let searchText = document.getElementsByName('search-text')[0].value;
    let searchType = document.getElementsByName('search-type')[0].value;
    window.location.href = 'index.html?type=' + searchType + '&text=' + searchText + "&N=10&sorting-option1=rating&sorting-option2=&order=DESC&page=1";
    return false;
}

// $('#player').removeAttr("style");
// $('.vp-preview').style.setProperty("background-size", "cover");
// $('video').setAttribute("style", "object-fit: cover; overflow: hidden;");

//AutoComplete

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    // TODO: if you want to check past query results first, you can do it here

    // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
    // with the query data

    // let x = JSON.stringify([{'value':'Matrix'},{'value':'Matrix'},{'value':'Matrix'},{'value':'Matrix'},{'value':'Matrix'},{'value':'Matrix'},{'value':'Matrix'}, {'value':'Endgame'}, {'value':'Infinity War'}, {'value':'Snowpiercer'}, {'value':'Something'}, {'value':'something'}, {'value':'yes'}])
    //
    // handleLookupAjaxSuccess(x,query,doneCallback)

    //Check if the results are in the cache

    let cache = localStorage.getItem(query)
    let cacheResults = JSON.parse(cache)
    if(cacheResults != null) {

        console.log("Fetching query results from frontend cache");

        handleLookupAjaxSuccess(cacheResults["results"], query, doneCallback)
    }
    else {

        console.log("sending AJAX request to backend Java Servlet")

        jQuery.ajax({
            "method": "GET",
            // generate the request url from the query.
            // escape the query string to avoid errors caused by special characters
            "url": "api/fullsearch?title=" + escape(query),
            "success": function(data) {
                // pass the data, query, and doneCallback function into the success handler
                let cachedResults = JSON.stringify(data)
                localStorage.setItem(query, cachedResults)
                handleLookupAjaxSuccess(data["results"], query, doneCallback)
            },
            "error": function(errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            },
            minChars: 3,
            cache: true
        })
    }
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    console.log("lookup ajax successful")

    // parse the string into JSON

    // TODO: if you want to cache the result into a global variable you can do it here

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: data } );
}

function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion

    console.log("You selected '" + suggestion["value"] + "' redirecting...")

    window.location.href = 'single-movie.html?id=' + suggestion["id"]
}

// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    minChars: 3
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
});

function handleNormalSearch(query) {
    console.log("doing normal search with query: " + query);
    // TODO: you should do normal search here

    let cache = localStorage.getItem(query)
    let cacheResults = JSON.parse(cache)

    let movie = {}

    for(let i = 0; i < cacheResults["results"].length; i++) {
        if (cacheResults["results"][i]["value"] === query) {
            movie = cacheResults["results"][i]["value"];
            break;
        }
    }

    window.location.href = 'single-movie.html?id=' + movie["id"]
}

$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleNormalSearch($('#autocomplete').val())
    }
})