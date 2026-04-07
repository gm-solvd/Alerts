-- Add category, privacy_policy_url, data_access_url columns to data_broker_sites
ALTER TABLE data_broker_sites ADD COLUMN category VARCHAR(50) NOT NULL DEFAULT 'PEOPLE_SEARCH';
ALTER TABLE data_broker_sites ADD COLUMN privacy_policy_url VARCHAR(512);
ALTER TABLE data_broker_sites ADD COLUMN data_access_url VARCHAR(512);

-- Update existing people-search brokers with URLs
UPDATE data_broker_sites SET category = 'PEOPLE_SEARCH',
    privacy_policy_url = 'https://www.spokeo.com/privacy-policy',
    data_access_url = 'https://www.spokeo.com/optout'
WHERE name = 'Spokeo';

UPDATE data_broker_sites SET
    privacy_policy_url = 'https://www.fastpeoplesearch.com/privacy',
    data_access_url = 'https://www.fastpeoplesearch.com/removal'
WHERE name = 'FastPeopleSearch';

UPDATE data_broker_sites SET
    privacy_policy_url = 'https://www.whitepages.com/privacy',
    data_access_url = 'https://www.whitepages.com/suppression-requests'
WHERE name = 'WhitePages';

UPDATE data_broker_sites SET
    privacy_policy_url = 'https://www.truepeoplesearch.com/privacy',
    data_access_url = 'https://www.truepeoplesearch.com/removal'
WHERE name = 'TruePeopleSearch';

UPDATE data_broker_sites SET
    privacy_policy_url = 'https://thatsthem.com/privacy',
    data_access_url = 'https://thatsthem.com/optout'
WHERE name = 'ThatsThem';

UPDATE data_broker_sites SET
    privacy_policy_url = 'https://www.usphonebook.com/privacy',
    data_access_url = 'https://www.usphonebook.com/opt-out'
WHERE name = 'USPhoneBook';

UPDATE data_broker_sites SET
    privacy_policy_url = 'https://www.cyberbackgroundchecks.com/privacy',
    data_access_url = 'https://www.cyberbackgroundchecks.com/removal'
WHERE name = 'CyberBackgroundChecks';

UPDATE data_broker_sites SET
    privacy_policy_url = 'https://radaris.com/page/privacy',
    data_access_url = 'https://radaris.com/control/privacy'
WHERE name = 'Radaris';

UPDATE data_broker_sites SET
    privacy_policy_url = 'https://www.peoplefinder.com/privacy-policy',
    data_access_url = 'https://www.peoplefinder.com/optout'
WHERE name = 'PeopleFinder';

UPDATE data_broker_sites SET
    privacy_policy_url = 'https://www.numlookup.com/privacy',
    data_access_url = 'https://www.numlookup.com/opt-out'
WHERE name = 'NumLookup';

-- Insert top-10 global data brokers (credit bureaus, marketing aggregators, people search)
INSERT INTO data_broker_sites (name, base_url, pii_fields, category, privacy_policy_url, data_access_url)
VALUES
    ('Acxiom', 'https://www.acxiom.com', ARRAY['name', 'address', 'phone', 'email', 'demographics', 'purchase_history'], 'MARKETING_DATA', 'https://www.acxiom.com/privacy/', 'https://isapps.acxiom.com/optout/optout.aspx'),
    ('Experian', 'https://www.experian.com', ARRAY['name', 'address', 'phone', 'email', 'ssn', 'credit_history', 'employment'], 'CREDIT_BUREAU', 'https://www.experian.com/privacy/', 'https://www.experian.com/consumer-products/free-credit-report'),
    ('Equifax', 'https://www.equifax.com', ARRAY['name', 'address', 'phone', 'ssn', 'credit_history', 'employment'], 'CREDIT_BUREAU', 'https://www.equifax.com/privacy/', 'https://my.equifax.com/consumer-registration/'),
    ('TransUnion', 'https://www.transunion.com', ARRAY['name', 'address', 'phone', 'ssn', 'credit_history', 'employment'], 'CREDIT_BUREAU', 'https://www.transunion.com/privacy/', 'https://www.transunion.com/annual-credit-report'),
    ('Oracle BlueKai', 'https://www.oracle.com/data-cloud/', ARRAY['name', 'email', 'browsing_history', 'purchase_history', 'demographics'], 'MARKETING_DATA', 'https://www.oracle.com/legal/privacy/', 'https://datacloudoptout.oracle.com/'),
    ('Epsilon', 'https://www.epsilon.com', ARRAY['name', 'address', 'phone', 'email', 'purchase_history', 'demographics'], 'MARKETING_DATA', 'https://www.epsilon.com/privacy-policy', 'https://www.epsilon.com/consumer-information'),
    ('CoreLogic', 'https://www.corelogic.com', ARRAY['name', 'address', 'property_records', 'mortgage_data', 'demographics'], 'DATA_AGGREGATOR', 'https://www.corelogic.com/privacy/', 'https://www.corelogic.com/privacy/consumer-data-access/'),
    ('LexisNexis', 'https://www.lexisnexis.com', ARRAY['name', 'address', 'phone', 'email', 'ssn', 'court_records', 'employment'], 'DATA_AGGREGATOR', 'https://www.lexisnexis.com/en-us/privacy/', 'https://consumer.risk.lexisnexis.com/request'),
    ('PeopleFinders', 'https://www.peoplefinders.com', ARRAY['name', 'address', 'phone', 'email', 'age', 'relatives'], 'PEOPLE_SEARCH', 'https://www.peoplefinders.com/privacy', 'https://www.peoplefinders.com/opt-out'),
    ('BeenVerified', 'https://www.beenverified.com', ARRAY['name', 'address', 'phone', 'email', 'age', 'court_records'], 'PEOPLE_SEARCH', 'https://www.beenverified.com/privacy', 'https://www.beenverified.com/app/optout/search')
ON CONFLICT (name) DO UPDATE SET
    category = EXCLUDED.category,
    privacy_policy_url = EXCLUDED.privacy_policy_url,
    data_access_url = EXCLUDED.data_access_url,
    pii_fields = EXCLUDED.pii_fields;
