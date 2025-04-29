DELIMITER $$

CREATE PROCEDURE add_movie (IN movie_title VARCHAR(100),
                            movie_year INT,
                            movie_director VARCHAR(100),
                            star_name VARCHAR(100),
                            star_dob INT,
                            genre_name VARCHAR(32))
BEGIN
    IF (SELECT count(*)
    FROM movies
    where movies.title = movie_title and movies.year = movie_year and movies.director = movie_director)>0 then
        BEGIN
            SELECT CONCAT('Fail') AS message;
        end;
    ELSE
        BEGIN
            SET @star = (SELECT stars.id FROM stars where stars.name = star_name and stars.birthYear = star_dob limit 1);
            SET @genre = (SELECT genres.id FROM genres where genres.name = genre_name limit 1);
            IF (SELECT count(@star)) = 0 then
                BEGIN
                    SET @currStarMax = (SELECT concat('nm', LPAD(cast(substr(max(id),3) AS UNSIGNED) + 1, 7, 0)) FROM stars);
                    INSERT INTO stars VALUES((SELECT @currStarMax),
                                             star_name, star_dob);
                    IF star_dob IS NULL THEN
                        begin
                            SET @star = (SELECT stars.id FROM stars where stars.name = star_name and stars.birthYear IS NULL limit 1);
                        end;
                    ELSE
                        BEGIN
                            SET @star = (SELECT stars.id FROM stars where stars.name = star_name and stars.birthYear = star_dob limit 1);
                        end;
                    end if;
                end;
            end if;
            IF (SELECT count(@genre)) = 0 then
                BEGIN
                    SET @currNewId = ((SELECT max(id) FROM genres) + 1);
                    INSERT INTO genres VALUES((SELECT @currNewId),
                                              genre_name);
                    SET @genre = (SELECT genres.id FROM genres where genres.name = genre_name limit 1);
                end;
            end if;

            SET @currNewMovie =  (SELECT concat('tt', LPAD(cast(substr(max(id),3) AS UNSIGNED) + 1, 7, 0)) FROM movies);
            INSERT INTO movies VALUES ((SELECT @currNewMovie),
                                       movie_title,
                                       movie_year,
                                       movie_director);
            INSERT INTO stars_in_movies VALUES ((SELECT @star),(SELECT @currNewMovie));
            INSERT INTO genres_in_movies VALUES ((SELECT @genre), (SELECT @currNewMovie));
            
            INSERT INTO ratings VALUES((SELECT @currNewMovie), 0.0, 0);

            SELECT CONCAT('Success') AS message;
        end;
    end if;
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_star (IN star_name VARCHAR(100),
                            star_dob INT)
BEGIN
	SET @currStarMax = (SELECT concat('nm', LPAD(cast(substr(max(id),3) AS UNSIGNED) + 1, 7, 0)) FROM stars);
	INSERT INTO stars VALUES((SELECT @currStarMax),star_name, star_dob);
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_genre (genre_name VARCHAR(32))
BEGIN
	SET @genre = (SELECT genres.id FROM genres where genres.name = genre_name limit 1);
    IF (SELECT count(@genre)) = 0 then
    BEGIN
		SET @currNewId = ((SELECT max(id) FROM genres) + 1);
		INSERT INTO genres VALUES((SELECT @currNewId),
								  genre_name);
	end;
    end if;
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_movie_only (IN movie_id varchar(10),
							movie_title VARCHAR(100),
                            movie_year INT,
                            movie_director VARCHAR(100))
BEGIN
	IF (SELECT count(*)
    FROM movies
    where movies.title = movie_title and movies.year = movie_year and movies.director = movie_director)>0 then
        BEGIN
            SELECT CONCAT('Fail') AS message;
        end;
	ELSE
    BEGIN
		INSERT INTO movies VALUES (movie_id, movie_title, movie_year, movie_director);
        INSERT INTO ratings VALUES(movie_id, 0.0, 0);
	end;
    end if;
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_star_in_movies (IN movie_id varchar(10),
                                    star_name VARCHAR(100))
BEGIN
	IF (SELECT count(*)
    FROM stars
    where stars.name=0) then
        BEGIN
			SET @currStarMax = (SELECT concat('nm', LPAD(cast(substr(max(id),3) AS UNSIGNED) + 1, 7, 0)) FROM stars);
			INSERT INTO stars VALUES((SELECT @currStarMax),star_name, null);
        end;
	ELSE
    BEGIN
		SET @currStarMax = (SELECT stars.id FROM stars where stars.name = star_name);
	end;
    end if;
    
    INSERT INTO stars_in_movies VALUES (@currStarMax, movie_id);
END$$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE add_genre_in_movies (IN movie_id varchar(10),
										genre_name varchar(32))
BEGIN
	INSERT INTO genres_in_movies VALUES ((SELECT genres.id FROM genres where genres.name = genre_name), movie_id);
END$$
DELIMITER ;