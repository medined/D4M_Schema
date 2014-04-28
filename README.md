There are many articles talking about the benefits of Big Data and how it can 
knock down Data Silos and pool data together into a Data Lake. Most  are short 
on specifics. In this project, I'll use D4M (from MIT Lincoln Labs - 
http://www.mit.edu/~kepner/D4M/) to fill in some blanks. Hopefully, you'll 
find some ideas here you haven't seen before and you'll get inspired to 
known down some of your own Data Silos. It can done!

D4M's main page says:

<blockquote>D4M is a breakthrough in computer programming that combines the 
advantages of five distinct processing technologies (sparse linear algebra, 
associative arrays, fuzzy algebra, distributed arrays, and triple-store/NoSQL 
databases such as Hadoop HBase and Apache Accumulo) to provide a database and 
computation system that addresses the problems associated with Big 
Data.</blockquote>

For my purpose, a Big Data project has several phases: Extract, Transform, 
Load, and Query. I'm leaving the Extract phase as a reader exercise since 
so much depending on where your data is coming from.

At this point, you might be thinking that I've forgotten to mention 
something -- What is the Common Big Data Architecture? CBDA provides a NO-SQL 
based schema that enables the Data Lake. All types of storage can be combined 
into a set of three Accumulo tables that work together. The D4M project 
provides plenty of documentation regarding query performance which I will 
not repeat here.

What is the Common Big Data Architecture?
-----------------------------------------

In this simple example, I'm just putting a fact table (cities in Maine) into 
the D4M schema. A UUID is used to group all information about one record 
together. This is a simplistic technique and should not be used in production.
Each field name and field value (a fact) is stored twice - once in the Tedge 
table and once in the TedgeTranspose table. 

* The <i>Tedge</i> table allows rapid access to a whole record. 

* The <i>TedgeTranpose</i> table allows rapid access to every record where a given 
fact is referenced. 

* The <i>TedgeDegree</i> table provides counts for each fact. It can be used for 
faceting.  If your goal is high speed inserts, you need to pre-sum the inserts 
into the <i>TedgeDegree</i> table otherwise this can become a bottleneck.

<b>TODO: Discuss TedgeText table.</b>

Below is a concrete example of how these tables are populated:

```
Relational Record
   CITY_NAME | STATE_NAME
   AKRON     | MAINE

Accumulo Mutations
   TABLE          * ROW                              * CF                               * VALUE
-------------------------------------------------------------------------------------------------
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * CITY_NAME|AKRON                  * 1
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * STATE_NAME|MAINE                 * 1 
   TedgeTranspose * CITY_NAME|AKRON                  * 9a127928-b661-4e46-9103-3fc024f4 * 1
   TedgeTranspose * STATE_NAME|MAINE                 * 9a127928-b661-4e46-9103-3fc024f4 * 1
   TedgeDegree    * CITY_NAME|AKRON                  * Degree                           * 1
   TedgeDegree    * STATE_NAME|MAINE                 * Degree                           * 1
```

Then see what changes when an additional record for the BOAZ city.

```
Relational Records
   CITY_NAME | STATE_NAME
   AKRON     | MAINE
A  BOAZ      | MAINE

Accumulo Mutations
   TABLE          * ROW                              * CF                               * VALUE
-------------------------------------------------------------------------------------------------
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * CITY_NAME|AKRON                  * 1
   Tedge          * 9a127928-b661-4e46-9103-3fc024f4 * STATE_NAME|MAINE                 * 1 
A  Tedge          * a1b4d569-ee45-4466-af2a-0960ccc1 * CITY_NAME|BOAZ                   * 1
A  Tedge          * a1b4d569-ee45-4466-af2a-0960ccc1 * STATE_NAME|MAINE                 * 1
   TedgeTranspose * CITY_NAME|AKRON                  * 9a127928-b661-4e46-9103-3fc024f4 * 1
A  TedgeTranspose * CITY_NAME|BOAZ                   * a1b4d569-ee45-4466-af2a-0960ccc1 * 1
   TedgeTranspose * STATE_NAME|MAINE                 * 9a127928-b661-4e46-9103-3fc024f4 * 1
A  TedgeTranspose * STATE_NAME|MAINE                 * a1b4d569-ee45-4466-af2a-0960ccc1 * 1
   TedgeDegree    * CITY_NAME|AKRON                  * Degree                           * 1
A  TedgeDegree    * CITY_NAME|BOAZ                   * Degree                           * 1
M  TedgeDegree    * STATE_NAME|MAINE                 * Degree                           * 2
```

The 'A' lines were added. The 'M' line was modified. The rest stayed the same. 

Can you foresee a problem if some data needs to be re-ingested? Right, since a
UUID value was used, duplicate records would be ingested.

Typically, one or more of the incoming fields are designed 'primary' to 
indicate that when taken together then make the record unique. This field 
designation is specific to each use case.

Our tiny dataset is not well-formed because there might be two cities with the
same name in Maine. If I were the Data Architect on this pretend project, I'd
reject this data until Latitude and Longitude values were added.

Time has passed, geographic information has been added and another ingest 
attempt is happening. This time our row value will be the City name and
the geographic position.

```
Relational Record
   CITY_NAME | STATE_NAME | LATITUDE | LONGITUDE
   AKRON     | MAINE      | 43.22    | -70.79
   BOAZ      | MAINE      | 45.25    | -69.44

Accumulo Mutations
   TABLE          * ROW                              * CF                               * VALUE
-------------------------------------------------------------------------------------------------
   Tedge          * AKRON|43.22|-70.79 * CITY_NAME|AKRON    * 1
   Tedge          * AKRON|43.22|-70.79 * STATE_NAME|MAINE   * 1 
A  Tedge          * BOAZ|45.25|-69.44  * CITY_NAME|BOAZ     * 1
A  Tedge          * BOAZ|45.25|-69.44  * STATE_NAME|MAINE   * 1
   TedgeTranspose * CITY_NAME|AKRON    * AKRON|43.22|-70.79 * 1
A  TedgeTranspose * CITY_NAME|BOAZ     * BOAZ|45.25|-69.44  * 1
   TedgeTranspose * STATE_NAME|MAINE   * AKRON|43.22|-70.79 * 1
A  TedgeTranspose * STATE_NAME|MAINE   * BOAZ|45.25|-69.44  * 1
   TedgeDegree    * CITY_NAME|AKRON    * Degree             * 1
A  TedgeDegree    * CITY_NAME|BOAZ     * Degree             * 1
M  TedgeDegree    * STATE_NAME|MAINE   * Degree             * 2
```

Now we can re-ingest the data as often as needed without creating duplication 
in Accumulo. The new entries would overlay the old entries.

If the row value gets longer than 40 characters, you can hash it into a SHA-1
value.

Not withstanding the above suggestion of using SHA-1, good row value design is
an art. Try to design them with these guidelines in mind:

 * Human readable (i.e. actually means something), which makes debugging a lot 
easier.

 * Rapidly changing leading values which provides automatic load balancing and 
easy pre-splitting. For example, reversing an ingest date so that the faster
changing seconds come first.

Getting Started with D4M
------------------------

How to get started? Get D4M working. There are instructions how to create a 
three-node Accumulo cluster and how to install D4M at 
https://github.com/medined/Accumulo_1_5_0_By_Vagrant. Once you can start 
Octave and run ls(DB) to see the tables into Accumulo, you're ready to read 
further.


ETL
---



