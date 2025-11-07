---------------------------------------------------------------------
-- SQL file to insert a new business customer into the ORDER DB --
---------------------------------------------------------------------

---------------------------------------
-- Description of the CUSTOMER table --
---------------------------------------

-- CUSTOMER_ID INTEGER NOT NULL ,
-- USERNAME VARCHAR(30) NOT NULL,
-- OPEN_ORDER INTEGER , 
-- NAME VARCHAR(30) NOT NULL , 
-- BUSINESS_VOLUME_DISCOUNT CHAR(1) DEFAULT 'N', 
-- BUSINESS_PARTNER CHAR(1) DEFAULT 'N', 
-- BUSINESS_DESCRIPTION TEXT, 
-- RESIDENTIAL_HOUSEHOLD_SIZE SMALLINT , 
-- RESIDENTIAL_FREQUENT_CUSTOMER CHAR(1) DEFAULT 'Y',
-- TYPE VARCHAR(11) NOT NULL,
-- ADDRESSLINE1 VARCHAR(50),
-- ADDRESSLINE2 VARCHAR(50),
-- CITY VARCHAR(50),
-- COUNTRY VARCHAR(50),
-- STATE VARCHAR(30),
-- ZIP VARCHAR (10)

-- Example:
-- INSERT INTO CUSTOMER (CUSTOMER_ID,NAME,USERNAME,TYPE,BUSINESS_VOLUME_DISCOUNT,BUSINESS_PARTNER,BUSINESS_DESCRIPTION,addressLine1,addressLine2,city,country,state,zip)
-- VALUES (3,'Business Test User','btuser','BUSINESS','N','N','Some business description','500 Miami Bld','','Raleigh','USA','NC','21233');        
 
        
INSERT INTO CUSTOMER 
            (CUSTOMER_ID,
             NAME,
             USERNAME,
             TYPE,
             BUSINESS_VOLUME_DISCOUNT,
             BUSINESS_PARTNER,
             BUSINESS_DESCRIPTION, 
             addressLine1,
             addressLine2,
             city,
             country,
             state,
             zip)
VALUES 
            (1,
             'Business Test User',
             'btuser',
             'BUSINESS',
             'Y',
             'N',
             'Test business customer for database validation',
             '500 Main Street',
             'Suite 200',
             'Raleigh',
             'USA',
             'NC',
             '27601');
