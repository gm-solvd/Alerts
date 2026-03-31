INSERT INTO data_broker_sites (name, base_url, search_url_tpl, result_selector, pii_fields) VALUES
('FastPeopleSearch', 'https://www.fastpeoplesearch.com', 'https://www.fastpeoplesearch.com/name/{name}_{location}', 'div.card-block', ARRAY['name', 'phone', 'address', 'age']),
('WhitePages', 'https://www.whitepages.com', 'https://www.whitepages.com/name/{name}/{location}', 'div.serp-card', ARRAY['name', 'phone', 'address']),
('TruePeopleSearch', 'https://www.truepeoplesearch.com', 'https://www.truepeoplesearch.com/results?name={name}&citystatezip={location}', 'div.card', ARRAY['name', 'phone', 'address', 'email']),
('Spokeo', 'https://www.spokeo.com', 'https://www.spokeo.com/{name}', 'div.results-list', ARRAY['name', 'phone', 'address', 'email', 'age']),
('ThatsThem', 'https://thatsthem.com', 'https://thatsthem.com/name/{name}/{location}', 'div.ThatsThem-record', ARRAY['name', 'phone', 'address', 'email']),
('USPhoneBook', 'https://www.usphonebook.com', 'https://www.usphonebook.com/{name}/{location}', 'div.result-item', ARRAY['name', 'phone', 'address']),
('CyberBackgroundChecks', 'https://www.cyberbackgroundchecks.com', 'https://www.cyberbackgroundchecks.com/people/{name}/{location}', 'div.card', ARRAY['name', 'phone', 'address', 'age']),
('Radaris', 'https://radaris.com', 'https://radaris.com/p/{name}/', 'div.card-content', ARRAY['name', 'phone', 'address', 'email', 'age']),
('PeopleFinder', 'https://www.peoplefinder.com', 'https://www.peoplefinder.com/results?name={name}&location={location}', 'div.result-card', ARRAY['name', 'phone', 'address']),
('NumLookup', 'https://www.numlookup.com', 'https://www.numlookup.com/lookup/{phone}', 'div.result', ARRAY['name', 'phone']);
