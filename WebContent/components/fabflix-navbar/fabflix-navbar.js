$('#browse').click(() => {

    if($('#browse').attr('aria-expanded') === 'true') {
        $('.navbar').removeClass('shadow');
        $('#navbarToggleExternalContent').addClass('shadow');
        $('#browse').addClass('nav-item-active');
    }
    else {
        $('.navbar').addClass('shadow');
        $('#navbarToggleExternalContent').removeClass('shadow');
        $('#browse').removeClass('nav-item-active');
    }
});

$('#profile').click(() => {
    if($('#profile').attr('aria-expanded') === 'true') {
        $('#profile').removeClass('nav-item-active');
    }
    else {

        $('#profile').addClass('nav-item-active');
    }
});


function handleGenreResult(resultData) {
    let movieGenresDiv = $("#movie-genres");

    for(let i = 0; i < resultData.length; i++) {
        let genre = "";
        genre += "<div class='pill-container'><span class='pill'>"
        genre += "<a href='index.html?type=genre&text=" + resultData[i]['genre_name'] + "&N=10&sorting-option1=rating&sorting-option2=&order=DESC&page=1'>" + resultData[i]['genre_name'] + "</a>"
        genre += "</span></div>"
        movieGenresDiv.append(genre)
    }

    let movieAlphabet = $("#alphabet");

    let alphabet = '0123456789abcdefghijklmnopqrstuvwxyz*'

    for(let i = 0; i < alphabet.length; i++) {
        let letter = "";
        letter += "<div class='pill-container'><span class='pill'>"
        letter += "<a href='index.html?type=alphabet&text=" + alphabet[i] + "&N=10&sorting-option1=rating&sorting-option2=&order=DESC&page=1'>" + alphabet[i] + "</a>"
        letter += "</span></div>"
        movieAlphabet.append(letter)
    }

}

$.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genres", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleGenreResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});