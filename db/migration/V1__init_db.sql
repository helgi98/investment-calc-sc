create table asset (
	id serial primary key,
	name varchar not null
);

create table portfolio (
	id serial primary key,
	risk_lower_bound integer not null,
	risk_upper_bound integer not null,
	check (risk_upper_bound >= risk_lower_bound)
);

create table portfolio_asset (
	portfolio_id integer not null references Portfolio,
	asset_id integer not null references Asset,
	weight decimal not null check (weight > 0 and weight <= 1),
	unique(portfolio_id, asset_id)
);
