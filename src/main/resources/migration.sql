CREATE TABLE IF NOT EXISTS fns_notams (
    fnsid int primary key,
    correlationid bigint,
    issuedtimestamp timestamp,
    storedtimestamp timestamp,
    updatedtimestamp timestamp,
    validfromtimestamp timestamp,
    validtotimestamp timestamp,
    classification varchar(4),
    locationdesignator varchar(12),
    notamaccountability varchar(12),
    notamtext text,
    aixmnotammessage xml,
    status varchar(12),
    icaolocation varchar(4)
);

-- Add missing columns if they don't exist
DO $$ 
BEGIN
    -- Add correlationId if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'correlationid') THEN
        ALTER TABLE NOTAMS ADD COLUMN correlationId bigint;
    END IF;

    -- Add issuedTimestamp if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'issuedtimestamp') THEN
        ALTER TABLE fns_notams ADD COLUMN issuedTimestamp timestamp;
    END IF;

    -- Add storedTimeStamp if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'storedtimestamp') THEN
        ALTER TABLE fns_notams ADD COLUMN storedTimeStamp timestamp;
    END IF;

    -- Add updatedTimestamp if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'updatedtimestamp') THEN
        ALTER TABLE fns_notams ADD COLUMN updatedTimestamp timestamp;
    END IF;

    -- Add validFromTimestamp if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'validfromtimestamp') THEN
        ALTER TABLE fns_notams ADD COLUMN validFromTimestamp timestamp;
    END IF;

    -- Add validToTimestamp if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'validtotimestamp') THEN
        ALTER TABLE fns_notams ADD COLUMN validToTimestamp timestamp;
    END IF;

    -- Add classification if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'classification') THEN
        ALTER TABLE fns_notams ADD COLUMN classification varchar(4);
    END IF;

    -- Add locationDesignator if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'locationdesignator') THEN
        ALTER TABLE fns_notams ADD COLUMN locationDesignator varchar(12);
    END IF;

    -- Add notamAccountability if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'notamaccountability') THEN
        ALTER TABLE fns_notams ADD COLUMN notamAccountability varchar(12);
    END IF;

    -- Add notamText if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'notamtext') THEN
        ALTER TABLE fns_notams ADD COLUMN notamText text;
    END IF;

    -- Add aixmNotamMessage if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'aixmnotammessage') THEN
        ALTER TABLE fns_notams ADD COLUMN aixmNotamMessage xml;
    END IF;

    -- Add status if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'status') THEN
        ALTER TABLE fns_notams ADD COLUMN status varchar(12);
    END IF;

    -- Add icaoLocation if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'icaolocation') THEN
        ALTER TABLE fns_notams ADD COLUMN icaolocation varchar(4);
    END IF;

    -- Add primary key if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                  WHERE table_name = 'fns_notams' AND constraint_name = 'fns_notams_pkey') THEN
        ALTER TABLE fns_notams ADD PRIMARY KEY (fnsid);
    END IF;

    -- Add notamSeries if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'notam_series') THEN
        ALTER TABLE fns_notams ADD COLUMN notam_series varchar(12);
    END IF;

    -- Add notamNumber if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'notam_number') THEN
        ALTER TABLE fns_notams ADD COLUMN notam_number bigint;
    END IF;

    -- Add notamYear if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'notam_year') THEN
        ALTER TABLE fns_notams ADD COLUMN notam_year varchar(4);
    END IF;
    
    -- Add icaoMessage if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                  WHERE table_name = 'fns_notams' AND column_name = 'icao_message') THEN
        ALTER TABLE fns_notams ADD COLUMN icao_message text;
    END IF;
END $$;
