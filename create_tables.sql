create schema if not exists moviedb;
use moviedb;

create table if not exists movies(
	id varchar(10) not null,
	title varchar(100)not null,
	year int not null,
	director varchar(100) not null,
    primary key (id)
);
create table if not exists stars(
	id varchar(10) not null,
	name varchar(100) not null,
	birthYear int null,
    primary key (id)
);
create table if not exists stars_in_movies(
	starId varchar(10) not null references stars.id,
	movieId varchar(10) not null references movies.id
);
create table if not exists genres(
	id int not null auto_increment,
	name varchar(32) not null,
    primary key (id)
);
create table if not exists genres_in_movies(
	genreId int not null references genres.id,
	movieId varchar(10) not null references movies.id
);
create table if not exists creditcards(
	id varchar(20) not null,
	firstName varchar(50) not null,
	lastName varchar(50) not null,
	expiration date not null,
    primary key (id)
);
create table if not exists customers(
	id int not null auto_increment,
	firstName varchar(50) not null,
	lastName varchar(50) not null,
	ccId varchar(20) not null references creditcards.id,
	address varchar(200) not null,
	email varchar(50) not null,
	password varchar(20) not null,
    primary key (id)
);
create table if not exists sales(
	id int not null auto_increment,
	customerId int not null references customers.id,
	movieId varchar(10) not null references movies.id,
	saleDate date not null,
    primary key (id)
);
create table if not exists ratings(
	movieId varchar(10) not null references movies.id,
	rating float not null,
	numVotes int not null
);