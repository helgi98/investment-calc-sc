INSERT INTO asset (id, name) VALUES (1, 'AAPL'), (2, 'PZZA'), (3, 'CAKE');
INSERT INTO portfolio (id, risk_lower_bound, risk_upper_bound) VALUES (1, 2, 5);
INSERT INTO portfolio_asset (portfolio_id, asset_id, weight) VALUES (1, 1, 0.5), (1, 2, 0.3), (1, 3, 0.2);